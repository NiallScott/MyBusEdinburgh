/*
 * Copyright (C) 2011 - 2012 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android.livetimes.parser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusParser;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusParserException;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusStop;
import uk.org.rivernile.edinburghbustracker.android.ApiKey;

/**
 * This is the Edinburgh specific implementation of the bus times parser. To
 * get an instance of this class, call EdinburghParser.getInstance()
 * 
 * @author Niall Scott
 */
public final class EdinburghParser implements BusParser {
    
    /** This error is called when an invalid key has been specified. */
    public static final byte ERROR_INVALID_APP_KEY = 6;
    /** This error is called when an invalid parameter has been specified. */
    public static final byte ERROR_INVALID_PARAMETER = 7;
    /** This error is called when the system encounters a processing error. */
    public static final byte ERROR_PROCESSING_ERROR = 8;
    /** This error is called when the system is under maintenance. */
    public static final byte ERROR_SYSTEM_MAINTENANCE = 9;
    /** This error is called when the system is overloaded. */
    public static final byte ERROR_SYSTEM_OVERLOADED = 10;
    
    private static final String URL =
            "http://www.mybustracker.co.uk/ws.php?module=json&key=";
    private static final Random rand = new Random(System.currentTimeMillis());
    
    private boolean globalDisruption = false;
    
    /**
     * Create a new EdinburghParser object.
     */
    public EdinburghParser() {
        // Nothing to do here.
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public HashMap<String, BusStop> getBusStopData(final String[] stopCodes,
            final int numDepartures) throws BusParserException {
        if(stopCodes == null || stopCodes.length == 0) return null;
        
        // Build the URL.
        final StringBuilder sb = new StringBuilder();
        sb.append(URL);
        sb.append(ApiKey.getHashedKey());
        sb.append("&function=getBusTimes&");
        final int len = stopCodes.length;
        
        if(len == 1) {
            sb.append("stopId=");
            sb.append(stopCodes[0]);
            sb.append('&');
        } else {
            for(int i = 0; i < len; i++) {
                if(i >= 6) break;
                
                sb.append("stopId");
                sb.append(i + 1);
                sb.append('=');
                sb.append(stopCodes[i]);
                sb.append('&');
            }
        }
        
        sb.append("nb=");
        sb.append(numDepartures);
        // Add a random arg so the response isn't cached by the network proxies.
        sb.append("&random=");
        sb.append(rand.nextInt());
        
        // TODO: review this code. I'm sure it could be done better.
        try {
            final URL url = new URL(sb.toString());
            // Reset the StringBuilder because we're going to reuse it.
            sb.setLength(0);
            final HttpURLConnection conn = (HttpURLConnection)url
                    .openConnection();
            try {
                final BufferedInputStream is = new BufferedInputStream(
                        conn.getInputStream());
                int data;
                
                while((data = is.read()) != -1) {
                    sb.append((char)data);
                }
            } finally {
                conn.disconnect();
            }

            return parseJSON(sb.toString());
        } catch(MalformedURLException e) {
            throw new BusParserException(ERROR_CANNOTRESOLVE);
        } catch(IOException e) {
            throw new BusParserException(ERROR_NOCONNECTION);
        } catch(JSONException e) {
            throw new BusParserException(ERROR_PARSEERR);
        }
    }
    
    /**
     * Parse the JSON string returned from the bus tracker web services.
     * 
     * @param jsonString The JSON string to parse.
     * @return A HashMap which has String -> BusStop mappings containing the
     * bus stop data.
     * @throws JSONException When a JSON exception occurs.
     * @throws BusParserException When a BusParserException occurs.
     */
    private HashMap<String, BusStop> parseJSON(final String jsonString)
            throws JSONException, BusParserException {
        final HashMap<String, BusStop> data = new HashMap<String, BusStop>();
        JSONObject jo = new JSONObject(jsonString);
        
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
        JSONArray jBuses;
        JSONObject jBus;
        EdinburghBusStop currentBusStop;
        EdinburghBus currentBus;
        EdinburghBusService currentBusService;
        int i, j, lenDatas;
        // Make sure there's array elements.
        final int len = ja.length();
        if(len == 0) {
            throw new BusParserException(ERROR_NODATA);
        }
        
        String temp;
        char reliability, type;
        
        for(i = 0; i < len; i++) {
            jo = ja.getJSONObject(i);
            
            // Check to see if there are any global disruptions.
            globalDisruption = jo.getBoolean("globalDisruption");
            temp = jo.getString("stopId");
            // Get data for the bus stop.
            currentBusStop = (EdinburghBusStop)data.get(temp);
            if(currentBusStop == null) {
                currentBusStop = new EdinburghBusStop(temp,
                        jo.getString("stopName"),
                        jo.getBoolean("busStopDisruption"));
                data.put(temp, currentBusStop);
            }
            
            // Add a bus service to the current bus stop.
            currentBusService = new EdinburghBusService(
                    jo.getString("mnemoService"),
                    jo.getString("nameService"),
                    jo.getBoolean("serviceDisruption"));
            currentBusStop.addBusService(currentBusService);
            
            jBuses = jo.getJSONArray("timeDatas");
            lenDatas = jBuses.length();
            
            // Loop through the times for each bus.
            for(j = 0; j < lenDatas; j++) {
                jBus = jBuses.getJSONObject(j);
                
                reliability = jBus.getString("reliability").charAt(0);
                type = jBus.getString("type").charAt(0);
                currentBus = new EdinburghBus(jBus.getString("nameDest"),
                        jBus.getInt("day"), jBus.getString("time"),
                        jBus.getInt("minutes"), reliability, type,
                        jBus.getString("terminus"));
                currentBusService.addBus(currentBus);
            }
        }
        
        return data;
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