/*
 * Copyright (C) 2011 - 2015 Niall 'Rivernile' Scott
 *
 * This software is provided 'as-is', without any express or implied
 * warranty.  In no event will the authors or contributors be held liable for
 * any damages arising from the use of this software.
 *
 * The aforementioned copyright holder(s) hereby grant you a
 * non-transferrable right to use this software for any purpose (including
 * commercial applications), and to modify it and redistribute it, subject to
 * the following conditions:
 *
 *  1. This notice may not be removed or altered from any file it appears in.
 *
 *  2. Any modifications made to this software, except those defined in
 *     clause 3 of this agreement, must be released under this license, and
 *     the source code of any modifications must be made available on a
 *     publically accessible (and locateable) website, or sent to the
 *     original author of this software.
 *
 *  3. Software modifications that do not alter the functionality of the
 *     software but are simply adaptations to a specific environment are
 *     exempt from clause 2.
 */

package uk.org.rivernile.edinburghbustracker.android.parser.livetimes;

import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.org.rivernile.android.bustracker.parser.livetimes.AuthenticationException;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusParser;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveTimesException;
import uk.org.rivernile.android.bustracker.parser.livetimes.Journey;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusTimes;
import uk.org.rivernile.android.bustracker.parser.livetimes.MaintenanceException;
import uk.org.rivernile.android.bustracker.parser.livetimes.ServerErrorException;
import uk.org.rivernile.android.bustracker.parser.livetimes.SystemOverloadedException;
import uk.org.rivernile.android.fetchutils.fetchers.Fetcher;
import uk.org.rivernile.android.fetchutils.fetchers.readers.JSONFetcherStreamReader;
import uk.org.rivernile.android.utils.JSONUtils;

/**
 * This is the Edinburgh specific implementation of {@link BusParser}.
 * 
 * @author Niall Scott
 */
public final class EdinburghParser implements BusParser {
    
    private static final String ERROR_INVALID_APP_KEY = "INVALID_APP_KEY";
    private static final String ERROR_INVALID_PARAMETER = "INVALID_PARAMETER";
    private static final String ERROR_PROCESSING_ERROR = "PROCESSING_ERROR";
    private static final String ERROR_SYSTEM_MAINTENANCE = "SYSTEM_MAINTENANCE";
    private static final String ERROR_SYSTEM_OVERLOADED = "SYSTEM_OVERLOADED";

    @NonNull
    @Override
    public LiveBusTimes getBusTimes(@NonNull final Fetcher fetcher)
            throws LiveTimesException {
        final JSONFetcherStreamReader reader = new JSONFetcherStreamReader();
        
        try {
            fetcher.executeFetcher(reader);
            
            return parseBusTimes(reader.getJSONObject());
        } catch (IOException e) {
            throw new LiveTimesException(e);
        } catch (JSONException e) {
            throw new LiveTimesException(e);
        }
    }

    @NonNull
    @Override
    public Journey getJourneyTimes(@NonNull final Fetcher fetcher)
            throws LiveTimesException {
        final JSONFetcherStreamReader reader = new JSONFetcherStreamReader();
        
        try {
            fetcher.executeFetcher(reader);
            
            return parseJourneyTimes(reader.getJSONObject());
        } catch (IOException e) {
            throw new LiveTimesException(e);
        } catch (JSONException e) {
            throw new LiveTimesException(e);
        }
    }
    
    /**
     * Parse bus times from the API JSON response.
     * 
     * @param joRoot The JSON object to parse.
     * @return An {@link EdinburghLiveBusTimes} object which encapsulates the
     * whole response, if successful. Will never be <code>null</code>.
     * @throws LiveTimesException When there was a problem parsing the response.
     */
    protected static EdinburghLiveBusTimes parseBusTimes(
            final JSONObject joRoot) throws LiveTimesException {
        if (joRoot == null) {
            throw new LiveTimesException("There is no root JSON object.");
        }
        
        if (joRoot.has("faultcode")) {
            throw parseError(joRoot);
        }
        
        try {
            final JSONArray jaBusTimes = joRoot.getJSONArray("busTimes");
            final int busTimesLen = jaBusTimes.length();
            
            if (busTimesLen == 0) {
                return new EdinburghLiveBusTimes(
                        Collections.<String, EdinburghLiveBusStop>emptyMap(),
                        SystemClock.elapsedRealtime(), false);
            }
            
            final HashMap<String, TemporaryBusStop> tempBusStops =
                    new HashMap<String, TemporaryBusStop>();
            TemporaryBusStop tempBusStop;
            JSONObject joBusService;
            String stopCode, stopName;
            boolean stopDisrupted, globalDisruption = false;
            EdinburghLiveBusService service;
            
            for (int i = 0; i < busTimesLen; i++) {
                joBusService = jaBusTimes.getJSONObject(i);
                
                try {
                    // If there's no stopId, there's no point in continuing
                    // for this service.
                    stopCode = JSONUtils.getString(joBusService, "stopId");
                } catch (JSONException e) {
                    continue;
                }
                
                if (TextUtils.isEmpty(stopCode)) {
                    // This iteration cannot continue without a stopCode.
                    continue;
                }
                
                service = parseBusTimesBusService(joBusService);
                
                if (service == null) {
                    // If the service could not be parsed, then progress to the
                    // next service, ignoring this service.
                    continue;
                }
                
                tempBusStop = tempBusStops.get(stopCode);
                
                if (tempBusStop == null) {
                    stopName = JSONUtils.optString(joBusService, "stopName",
                            null);
                    stopDisrupted = joBusService
                            .optBoolean("busStopDisruption");
                    tempBusStop = new TemporaryBusStop(stopCode, stopName,
                            stopDisrupted);
                    tempBusStops.put(stopCode, tempBusStop);
                }
                
                tempBusStop.services.add(service);
                globalDisruption = joBusService.optBoolean("globalDisruption");
            }
            
            EdinburghLiveBusStop busStop;
            
            final HashMap<String, EdinburghLiveBusStop> busStops =
                    new HashMap<String, EdinburghLiveBusStop>();
            for (TemporaryBusStop temp : tempBusStops.values()) {
                busStop = temp.toEdinburghBusStop();
                busStops.put(busStop.getStopCode(), busStop);
            }
            
            return new EdinburghLiveBusTimes(
                    Collections.unmodifiableMap(busStops),
                    SystemClock.elapsedRealtime(), globalDisruption);
        } catch (JSONException e) {
            throw new LiveTimesException(e);
        }
    }
    
    /**
     * Parse a bus service from the JSON response for the getBusTimes API
     * method.
     * 
     * @param joBusService The JSON object to parse.
     * @return  A new instance of {@link EdinburghLiveBusService} created from
     * the JSON data, including the {@link List} of {@link EdinburghLiveBus}es.
     * This will be <code>null</code> if <code>joBusService</code> is
     * <code>null</code>, there was a problem parsing the data or a required
     * field is missing or set as <code>null</code>.
     */
    protected static EdinburghLiveBusService parseBusTimesBusService(
            final JSONObject joBusService) {
        if (joBusService == null) {
            return null;
        }
        
        try {
            final String serviceName = serviceNameConversion(
                    JSONUtils.getString(joBusService, "mnemoService"));
            final String operator = JSONUtils.optString(joBusService,
                    "operatorId", null);
            final String route = JSONUtils.optString(joBusService,
                    "nameService", null);
            final boolean isDisrupted = joBusService
                    .optBoolean("serviceDisruption");
            final boolean isDiverted = joBusService
                    .optBoolean("serviceDiversion");
            
            final JSONArray jaBuses = joBusService.getJSONArray("timeDatas");
            final int busesLen = jaBuses.length();
            
            if (busesLen == 0) {
                // If there's no buses, there's no point in this service
                // existing.
                return null;
            }
            
            final ArrayList<EdinburghLiveBus> buses =
                    new ArrayList<EdinburghLiveBus>(busesLen);
            EdinburghLiveBus bus;

            for (int i = 0; i < busesLen; i++) {
                bus = parseBusTimesBus(jaBuses.getJSONObject(i));

                if (bus != null) {
                    buses.add(bus);
                }
            }
            
            // If there's no buses, there's no point in this service existing.
            if (buses.isEmpty()) {
                return null;
            }

            Collections.sort(buses);
            
            return new EdinburghLiveBusService(serviceName,
                    Collections.unmodifiableList(buses), operator, route,
                    isDisrupted, isDiverted);
        } catch (JSONException e) {
            return null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Parse a bus object in the JSON response for the getBusTimes API method.
     * 
     * @param joBus The JSON object to parse.
     * @return A new instance of {@link EdinburghLiveBus} created from the JSON
     * data. This will be <code>null</code> if <code>joBus</code> is
     * <code>null</code>, there was a problem parsing the data or a required
     * field is missing or set as null.
     */
    protected static EdinburghLiveBus parseBusTimesBus(final JSONObject joBus) {
        if (joBus == null) {
            return null;
        }
        
        try {
            final String reliabilityString = JSONUtils.getString(joBus,
                    "reliability");
            final String typeString = JSONUtils.getString(joBus, "type");
            
            if (TextUtils.isEmpty(reliabilityString) ||
                    TextUtils.isEmpty(typeString)) {
                return null;
            }
            
            final String destination = JSONUtils.getString(joBus, "nameDest");
            final int minutes = joBus.getInt("minutes");
            final Date time = addMinutes(null, minutes);
            final String terminus = JSONUtils.optString(joBus, "terminus",
                    null);
            final String journeyId = JSONUtils.optString(joBus, "journeyId",
                    null);
            
            final char reliability = reliabilityString.charAt(0);
            final char type = typeString.charAt(0);
            
            return new EdinburghLiveBus(destination, time, minutes, reliability,
                    type, terminus, journeyId);
        } catch (JSONException e) {
            return null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Parse a whole journey times response.
     * 
     * @param joRoot The {@link JSONObject} containing the journey times data.
     * @return An {@link EdinburghJourney} object with its data supplied by
     * <code>joRoot</code> and its children.
     * @throws LiveTimesException When there was a problem parsing the response.
     */
    protected static EdinburghJourney parseJourneyTimes(final JSONObject joRoot)
            throws LiveTimesException {
        if (joRoot == null) {
            throw new LiveTimesException("There is no root JSON object.");
        }
        
        if (joRoot.has("faultcode")) {
            throw parseError(joRoot);
        }
        
        try {
            final JSONArray jaJourneyTimes = joRoot
                    .getJSONArray("journeyTimes");
            
            if (jaJourneyTimes.length() == 0) {
                throw new LiveTimesException("There are no journey times.");
            }
            
            final EdinburghJourney journey =
                    parseJourney(jaJourneyTimes.getJSONObject(0));
            
            if (journey == null) {
                throw new LiveTimesException("The journey could not be "
                        + "parsed.");
            } else {
                return journey;
            }
        } catch (JSONException e) {
            throw new LiveTimesException(e);
        }
    }
    
    /**
     * Parse a whole journey from a single journey object.
     * 
     * @param joJourney The {@link JSONObject} containing the journey details.
     * @return An {@link EdinburghJourney} object with its data supplied by
     * <code>joJourney</code>, or <code>null</code> if there was a problem
     * handling the data (e.g. null/empty fields).
     */
    protected static EdinburghJourney parseJourney(final JSONObject joJourney) {
        if (joJourney == null) {
            return null;
        }
        
        try {
            final String journeyId = JSONUtils.getString(joJourney,
                    "journeyId");
            final String serviceName = serviceNameConversion(
                    JSONUtils.getString(joJourney, "mnemoService"));
            final String terminus = JSONUtils.getString(joJourney, "terminus");
            final String operator = JSONUtils.optString(joJourney, "operatorId",
                    null);
            final String route = JSONUtils.optString(joJourney, "nameService",
                    null);
            final String destination = JSONUtils.optString(joJourney,
                    "nameDest", null);
            final boolean globalDisruption = joJourney
                    .optBoolean("globalDisruption");
            final boolean serviceDisruption = joJourney
                    .optBoolean("serviceDisruption");
            final boolean serviceDiversion = joJourney
                    .optBoolean("serviceDiversion");

            final JSONArray jaDepartures = joJourney
                    .getJSONArray("journeyTimeDatas");
            final int departuresLen = jaDepartures.length();
            final List<EdinburghJourneyDeparture> departures;

            if (departuresLen > 0) {
                final ArrayList<EdinburghJourneyDeparture> tempDepartures =
                        new ArrayList<EdinburghJourneyDeparture>(departuresLen);
                EdinburghJourneyDeparture departure;

                for (int i = 0; i < departuresLen; i++) {
                    departure = parseJourneyDeparture(
                            jaDepartures.getJSONObject(i));

                    if (departure != null) {
                        tempDepartures.add(departure);
                    }
                }

                Collections.sort(tempDepartures);
                departures = Collections.unmodifiableList(tempDepartures);
            } else {
                departures = Collections.<EdinburghJourneyDeparture>emptyList();
            }

            return new EdinburghJourney(journeyId, serviceName, departures,
                    operator, route, destination, terminus, globalDisruption,
                    serviceDisruption, serviceDiversion,
                    SystemClock.elapsedRealtime());
        } catch (JSONException e) {
            return null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Parse a single departure from a list of journey departures.
     * 
     * @param joDeparture The {@link JSONObject} containing the departure
     * details.
     * @return An {@link EdinburghJourneyDeparture} object with its data
     * supplied by <code>joDeparture</code>, or <code>null</code> if there was a
     * problem in handling this data (e.g. null/empty fields).
     */
    protected static EdinburghJourneyDeparture parseJourneyDeparture(
            final JSONObject joDeparture) {
        if (joDeparture == null) {
            return null;
        }
        
        try {
            final String reliabilityString = JSONUtils.getString(joDeparture,
                    "reliability");
            final String typeString = JSONUtils.getString(joDeparture, "type");
            
            if (TextUtils.isEmpty(reliabilityString) ||
                    TextUtils.isEmpty(typeString)) {
                return null;
            }
            
            final String stopCode = JSONUtils.getString(joDeparture, "stopId");
            final String stopName = JSONUtils.optString(joDeparture, "stopName",
                    null);
            final int minutes = joDeparture.getInt("minutes");
            final Date time = addMinutes(null, minutes);
            final boolean isDisrupted =
                    joDeparture.optBoolean("busStopDisruption");
            final int order = joDeparture.optInt("order", Integer.MAX_VALUE);
            final char reliability = reliabilityString.charAt(0);
            final char type = typeString.charAt(0);
            
            return new EdinburghJourneyDeparture(stopCode, stopName, time,
                    minutes, reliability, type, isDisrupted, order);
        } catch (JSONException e) {
            return null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Parse the error object given by the server. The returned
     * {@link LiveTimesException} is guaranteed to never be <code>null</code>.
     * 
     * @param joError The JSONObject to parse the error from.
     * @return An instance of {@link LiveTimesException} or one of its children.
     */
    protected static LiveTimesException parseError(final JSONObject joError) {
        if (joError == null) {
            return new LiveTimesException("There was an error, and then there "
                    + "was a problem parsing the error message.");
        }
        
        final String faultCode;
        
        try {
            faultCode = JSONUtils.getString(joError, "faultcode");
        } catch (JSONException e) {
            return new LiveTimesException("There was an error, and then there "
                    + "was a problem parsing the error message.");
        }
        
        if (ERROR_INVALID_APP_KEY.equals(faultCode)) {
            return new AuthenticationException("The API key was not accepted "
                    + "by the server.");
        } else if (ERROR_INVALID_PARAMETER.equals(faultCode) ||
                ERROR_PROCESSING_ERROR.equals(faultCode)) {
            return new ServerErrorException();
        } else if (ERROR_SYSTEM_MAINTENANCE.equals(faultCode)) {
            return new MaintenanceException();
        } else if (ERROR_SYSTEM_OVERLOADED.equals(faultCode)) {
            return new SystemOverloadedException();
        } else {
            return new LiveTimesException("An unknown error occurred.");
        }
    }
    
    /**
     * A utility method to add minutes on to a {@link GregorianCalendar}.
     * 
     * @param time A GregorianCalendar instance to use. If <code>null</code>, a
     * new instance will be created with the UK locale and will be initialised
     * to the current time.
     * @param minutes The number of minutes to add.
     * @return A Date instance which points to the newly added time.
     */
    protected static Date addMinutes(GregorianCalendar time,
            final int minutes) {
        if (time == null) {
            time = new GregorianCalendar(Locale.UK);
        }
        
        time.add(Calendar.MINUTE, minutes);
        
        return time.getTime();
    }
    
    /**
     * Convert service names in to their public displays. For example, the tram
     * isn't in the system as a tram.
     * 
     * @param serviceName The service name to possibly convert.
     * @return The converted service name, or the same service name if no
     * conversion was required.
     */
    private static String serviceNameConversion(final String serviceName) {
        return "50".equals(serviceName) || "T50".equals(serviceName) ?
                "TRAM" : serviceName;
    }
    
    /**
     * This class holds temporary bus stop data. The reason for this is because
     * of the way that the bus tracker API is formed. There is no single object
     * for a bus stop that then holds a list of services. Instead, it holds a
     * list of services and inside that object is the bus stop data. Because the
     * model level objects are immutable, then the bus stop data has to be held
     * in this temporary object until parsing it complete and the object graph
     * is then constructed.
     * 
     * All fields are public and final - they cannot be assigned to except via
     * the constructor. This is to held speed up the operation.
     */
    private static class TemporaryBusStop {
        
        /** The list of {@link EdinburghLiveBusService}s. */
        public final ArrayList<EdinburghLiveBusService> services =
                new ArrayList<EdinburghLiveBusService>();
        /** The code of the bus stop. */
        public final String stopCode;
        /** The name of the bus stop. */
        public final String stopName;
        /** Whether the bus stop is disrupted or not. */
        public final boolean disrupted;
        
        /**
         * Create a new TemporaryBusStop. The {@link ArrayList} of
         * {@link EdinburghLiveBusService}s is already instantiated.
         * 
         * @param stopCode The code of the bus stop.
         * @param stopName The name of the bus stop.
         * @param disrupted Whether the bus stop is disrupted or not.
         */
        public TemporaryBusStop(final String stopCode, final String stopName,
                final boolean disrupted) {
            this.stopCode = stopCode;
            this.stopName = stopName;
            this.disrupted = disrupted;
        }
        
        /**
         * Convert the data held in this object to an
         * {@link EdinburghLiveBusStop}.
         * 
         * @return An {@link EdinburghLiveBusStop} instance.
         */
        public EdinburghLiveBusStop toEdinburghBusStop() {
            Collections.sort(services);
            return new EdinburghLiveBusStop(stopCode, stopName,
                    Collections.unmodifiableList(services), disrupted);
        }
    }
}