/*
 * Copyright (C) 2011 - 2014 Niall 'Rivernile' Scott
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusParser;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusParserException;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusStop;
import uk.org.rivernile.android.fetchers.Fetcher;
import uk.org.rivernile.android.fetchers.UrlMismatchException;
import uk.org.rivernile.android.fetchers.readers.JSONFetcherStreamReader;

/**
 * This is the Edinburgh specific implementation of the bus times parser. To
 * get an instance of this class, call EdinburghParser.getInstance()
 * 
 * @author Niall Scott
 */
public final class EdinburghParser implements BusParser {
    
    /** This error is called when an invalid key has been specified. */
    public static final byte ERROR_INVALID_APP_KEY = 7;
    /** This error is called when an invalid parameter has been specified. */
    public static final byte ERROR_INVALID_PARAMETER = 8;
    /** This error is called when the system encounters a processing error. */
    public static final byte ERROR_PROCESSING_ERROR = 9;
    /** This error is called when the system is under maintenance. */
    public static final byte ERROR_SYSTEM_MAINTENANCE = 10;
    /** This error is called when the system is overloaded. */
    public static final byte ERROR_SYSTEM_OVERLOADED = 11;
    
    private boolean globalDisruption = false;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public HashMap<String, BusStop> getBusTimes(final Fetcher fetcher)
            throws BusParserException {
        final JSONFetcherStreamReader reader = new JSONFetcherStreamReader();
        
        try {
            fetcher.executeFetcher(reader);

            return parseJSON(reader.getJSONObject());
        } catch(MalformedURLException e) {
            throw new BusParserException(ERROR_CANNOTRESOLVE);
        } catch (UrlMismatchException e) {
            throw new BusParserException(ERROR_URLMISMATCH);
        } catch(IOException e) {
            throw new BusParserException(ERROR_NOCONNECTION);
        } catch(JSONException e) {
            throw new BusParserException(ERROR_PARSEERR);
        }
    }
    
    /**
     * Parse the JSON string returned from the bus tracker web services.
     * 
     * @param jo The JSON object to parse.
     * @return A HashMap which has String -> BusStop mappings containing the
     * bus stop data.
     * @throws JSONException When a JSON exception occurs.
     * @throws BusParserException When a BusParserException occurs.
     */
    private HashMap<String, BusStop> parseJSON(JSONObject jo)
            throws JSONException, BusParserException {
        final HashMap<String, BusStop> data = new HashMap<String, BusStop>();
        
        // Check to see if the API returns errors.
        if(jo.has("faultcode")) {
            final String err = jo.getString("faultcode");
            if("INVALID_APP_KEY".equals(err)) {
                throw new BusParserException(ERROR_INVALID_APP_KEY);
            } else if("INVALID_PARAMETER".equals(err)) {
                throw new BusParserException(ERROR_INVALID_PARAMETER);
            } else if("PROCESSING_ERROR".equals(err)) {
                throw new BusParserException(ERROR_PROCESSING_ERROR);
            } else if("SYSTEM_MAINTENANCE".equals(err)) {
                throw new BusParserException(ERROR_SYSTEM_MAINTENANCE);
            } else if("SYSTEM_OVERLOADED".equals(err)) {
                throw new BusParserException(ERROR_SYSTEM_OVERLOADED);
            } else {
                throw new BusParserException(ERROR_UNKNOWN);
            }
        }
        
        final JSONArray ja = jo.getJSONArray("busTimes");
        EdinburghBusStop currentBusStop;
        EdinburghBusService currentBusService;
        // Make sure there's array elements.
        final int len = ja.length();
        if(len == 0) {
            throw new BusParserException(ERROR_NODATA);
        }
        
        String temp;
        
        for(int i = 0; i < len; i++) {
            jo = ja.getJSONObject(i);
            
            // Check to see if there are any global disruptions.
            globalDisruption = jo.getBoolean("globalDisruption");
            temp = jo.getString("stopId");
            // Get data for the bus stop.
            currentBusStop = (EdinburghBusStop)data.get(temp);
            if(currentBusStop == null) {
                currentBusStop = parseEdinburghBusStop(jo);
                if(currentBusStop != null) {
                    data.put(temp, currentBusStop);
                } else {
                    continue;
                }
            }
            
            // Add a bus service to the current bus stop.
            currentBusService = parseEdinburghBusService(jo);
            if(currentBusService != null) {
                currentBusStop.addBusService(currentBusService);
            }
        }
        
        return data;
    }
    
    /**
     * Create an EdinburghBusStop object from a JSONObject.
     * 
     * @param joStop The JSONObject to parse.
     * @return An EdinburghBusStop object, or null if there was a problem.
     */
    private static EdinburghBusStop parseEdinburghBusStop(
            final JSONObject joStop) {
        try {
            return new EdinburghBusStop(joStop.getString("stopId"),
                    joStop.getString("stopName"),
                    joStop.getBoolean("busStopDisruption"));
        } catch(JSONException e) {
            // Nothing to do.
        } catch(IllegalArgumentException e) {
            // Nothing to do.
        }
        
        return null;
    }
    
    /**
     * Create an EdinburghBus object from a JSONObject.
     * 
     * @param joService The JSONObject to parse.
     * @return An EdinburghBusService object, or null if there was a problem.
     */
    private static EdinburghBusService parseEdinburghBusService(
            final JSONObject joService) {
        try {
            final EdinburghBusService service = new EdinburghBusService(
                    joService.getString("mnemoService"),
                    joService.getString("nameService"),
                    joService.getBoolean("serviceDisruption"));
            final JSONArray jaBuses = joService.getJSONArray("timeDatas");
            final int len = jaBuses.length();
            EdinburghBus currentBus;
            
            // Loop through the times for each bus.
            for(int i = 0; i < len; i++) {
                currentBus = parseEdinburghBus(jaBuses.getJSONObject(i));
                
                if(currentBus != null) {
                    service.addBus(currentBus);
                }
            }
            
            return service;
        } catch(JSONException e) {
            // Nothing to do.
        } catch(IllegalArgumentException e) {
            // Nothing to do.
        }
        
        return null;
    }
    
    /**
     * Create an EdinburghBus object from a JSONObject.
     * 
     * @param joBus The JSONObject to parse.
     * @return An EdinburghBus object, or null if there was a problem.
     */
    private static EdinburghBus parseEdinburghBus(final JSONObject joBus) {
        try {
            final char reliability = joBus.getString("reliability").charAt(0);
            final char type = joBus.getString("type").charAt(0);
            
            return new EdinburghBus(joBus.getString("nameDest"),
                        joBus.getInt("day"), joBus.getString("time"),
                        joBus.getInt("minutes"), reliability, type,
                        joBus.getString("terminus"));
        } catch(JSONException e) {
            // Nothing to do.
        } catch(IllegalArgumentException e) {
            // Nothing to do.
        }
        
        return null;
    }
    
    /**
     * Get the global disruption status.
     * 
     * @return The global disruption status.
     */
    public boolean getGlobalDisruption() {
        return globalDisruption;
    }
}