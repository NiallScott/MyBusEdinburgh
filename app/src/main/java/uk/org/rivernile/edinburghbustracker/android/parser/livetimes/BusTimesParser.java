/*
 * Copyright (C) 2011 - 2016 Niall 'Rivernile' Scott
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
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBus;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusService;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusStop;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusTimes;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveTimesException;
import uk.org.rivernile.android.utils.JSONUtils;

/**
 * This class contains static methods to turn the JSON response for {@code getBusTimes} from the
 * Edinburgh bus tracker API in to an object graph.
 *
 * @author Niall Scott
 */
class BusTimesParser {

    /**
     * This constructor is private and empty to prevent instantiation.
     */
    private BusTimesParser() { }

    /**
     * Parse a bus times JSON response from the {@code getBusTimes} API method.
     *
     * @param joRoot The root {@link JSONObject} in the response.
     * @return A {@link LiveBusTimes} instance which is the root of the object graph for the
     * response.
     * @throws LiveTimesException When there was a problem during parsing.
     */
    @NonNull
    static LiveBusTimes parseBusTimes(@NonNull final JSONObject joRoot) throws LiveTimesException {
        final LiveTimesException exception = ErrorParser.getExceptionIfError(joRoot);

        if (exception != null) {
            throw exception;
        }

        try {
            final JSONArray jaBusTimes = joRoot.getJSONArray("busTimes");
            final LiveBusTimes.Builder builder = new LiveBusTimes.Builder()
                    .setReceiveTime(SystemClock.elapsedRealtime());
            final int busTimesLen = jaBusTimes.length();

            if (busTimesLen == 0) {
                return builder.setBusStops(Collections.<String, LiveBusStop>emptyMap()).build();
            }

            final HashMap<String, TempBusStop> tempBusStops = new HashMap<>();

            for (int i = 0; i < busTimesLen; i++) {
                try {
                    final JSONObject joBusService = jaBusTimes.getJSONObject(i);
                    final String stopCode;

                    try {
                        // If there's no stopId, there's no point in continuing for this service.
                        stopCode = JSONUtils.getString(joBusService, "stopId");
                    } catch (JSONException e) {
                        continue;
                    }

                    if (TextUtils.isEmpty(stopCode)) {
                        // This iteration cannot continue without a stopCode.
                        continue;
                    }

                    final LiveBusService service = parseBusTimesBusService(joBusService);

                    if (service == null) {
                        // If the service could not be parsed, then progress to the next service,
                        // ignoring this service.
                        continue;
                    }

                    TempBusStop tempBusStop = tempBusStops.get(stopCode);

                    if (tempBusStop == null) {
                        tempBusStop = new TempBusStop(new LiveBusStop.Builder()
                                .setStopCode(stopCode)
                                .setStopName(JSONUtils.optString(joBusService, "stopName", null))
                                .setIsDisrupted(joBusService.optBoolean("busStopDisruption")));
                        tempBusStops.put(stopCode, tempBusStop);
                    }

                    tempBusStop.services.add(service);
                    builder.setHasGlobalDisruption(joBusService.optBoolean("globalDisruption"));
                } catch (JSONException e) {
                    // Do nothing.
                }
            }

            final HashMap<String, LiveBusStop> busStops = new HashMap<>();

            for (TempBusStop temp : tempBusStops.values()) {
                try {
                    final LiveBusStop busStop = temp.build();
                    busStops.put(busStop.getStopCode(), busStop);
                } catch (IllegalArgumentException e) {
                    // Do nothing.
                }
            }

            return builder.setBusStops(Collections.unmodifiableMap(busStops)).build();
        } catch (JSONException e) {
            throw new LiveTimesException(e);
        } catch (IllegalArgumentException e) {
            throw new LiveTimesException(e);
        }
    }

    /**
     * Parse a bus service from the JSON response for the {@code getBusTimes} API method.
     *
     * @param joBusService The {@link JSONObject} to parse.
     * @return  A new instance of {@link LiveBusService} created from the JSON data, including
     * the {@link List} of {@link EdinburghLiveBus}es.
     * This will be {@code null} if there was a problem parsing the data or a required field is
     * missing or set as {@code null}.
     */
    @Nullable
    static LiveBusService parseBusTimesBusService(
            @NonNull final JSONObject joBusService) {
        try {
            final JSONArray jaBuses = joBusService.getJSONArray("timeDatas");
            final int busesLen = jaBuses.length();

            if (busesLen == 0) {
                // If there's no buses, there's no point in this service existing.
                return null;
            }

            final ArrayList<LiveBus> buses = new ArrayList<>(busesLen);

            for (int i = 0; i < busesLen; i++) {
                try {
                    final EdinburghLiveBus bus = parseBusTimesBus(jaBuses.getJSONObject(i));

                    if (bus != null) {
                        buses.add(bus);
                    }
                } catch (JSONException e) {
                    // Do nothing.
                }
            }

            // If there's no buses, there's no point in this service existing.
            if (buses.isEmpty()) {
                return null;
            }

            Collections.sort(buses);

            return new LiveBusService.Builder()
                    .setServiceName(EdinburghParser.serviceNameConversion(
                        JSONUtils.getString(joBusService, "mnemoService")))
                    .setBuses(Collections.unmodifiableList(buses))
                    .setOperator(JSONUtils.optString(joBusService, "operatorId", null))
                    .setRoute(JSONUtils.optString(joBusService, "nameService", null))
                    .setIsDisrupted(joBusService.optBoolean("serviceDisruption"))
                    .setIsDiverted(joBusService.optBoolean("serviceDiversion"))
                    .build();
        } catch (JSONException e) {
            return null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Parse a bus object in the JSON response for the {@code getBusTimes} API method.
     *
     * @param joBus The {@link JSONObject} to parse.
     * @return A new instance of {@link EdinburghLiveBus} created from the JSON data. This will
     * be {@code null} there was a problem parsing the data or a required field is missing or set
     * as {@code null}.
     */
    @Nullable
    static EdinburghLiveBus parseBusTimesBus(@NonNull final JSONObject joBus) {
        try {
            final String reliabilityString = JSONUtils.getString(joBus, "reliability");
            final String typeString = JSONUtils.getString(joBus, "type");

            if (TextUtils.isEmpty(reliabilityString) || TextUtils.isEmpty(typeString)) {
                return null;
            }

            final char reliability = reliabilityString.charAt(0);
            final char type = typeString.charAt(0);
            final int minutes = joBus.getInt("minutes");
            final EdinburghLiveBus.Builder builder = new EdinburghLiveBus.Builder();

            builder.setDepartureMinutes(minutes)
                    .setDestination(JSONUtils.getString(joBus, "nameDest"))
                    .setDepartureTime(EdinburghParser.addMinutes(null, minutes))
                    .setTerminus(JSONUtils.optString(joBus, "terminus", null))
                    .setJourneyId(JSONUtils.optString(joBus, "journeyId", null))
                    .setIsEstimatedTime(reliability == EdinburghConstants.RELIABILITY_ESTIMATED)
                    .setIsDelayed(reliability == EdinburghConstants.RELIABILITY_DELAYED)
                    .setIsDiverted(reliability == EdinburghConstants.RELIABILITY_DIVERTED)
                    .setIsTerminus(type == EdinburghConstants.TYPE_TERMINUS)
                    .setIsPartRoute(type == EdinburghConstants.TYPE_PART_ROUTE);

            return builder.build();
        } catch (JSONException e) {
            return null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * This class holds temporary bus stop data. The reason for this is because of the way that
     * the bus tracker API is formed. There is no single object for a bus stop that then holds a
     * list of services. Instead, it holds a list of services and inside that object is the bus
     * stop data. Because the model level objects are immutable, then the bus stop data has to be
     * held in this temporary object until parsing is complete and the object graph is then
     * constructed.
     */
    private static class TempBusStop {

        /** The builder that will eventually build the {@link LiveBusStop}. */
        final LiveBusStop.Builder builder;
        /** The {@link List} of {@link LiveBusService}s at this stop. */
        final ArrayList<LiveBusService> services = new ArrayList<>();

        /**
         * Create a new temporary holder for bus stop data.
         *
         * @param builder The builder to be used to construct the {@link LiveBusStop} later on.
         */
        TempBusStop(@NonNull final LiveBusStop.Builder builder) {
            this.builder = builder;
        }

        /**
         * Build the {@link LiveBusStop} object.
         *
         * @return A new {@link LiveBusStop} object.
         */
        @NonNull
        LiveBusStop build() {
            Collections.sort(services);
            return builder.setServices(services).build();
        }
    }
}
