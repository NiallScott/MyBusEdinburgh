/*
 * Copyright (C) 2011 Niall 'Rivernile' Scott
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusParser;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusParserException;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusService;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusStop;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusTimes;

public final class EdinburghParser implements BusParser {
    
    private final static String STOP_DATA_COMMAND = "getBusTimesByStopCode";
    
    private static EdinburghParser instance = null;
    
    private EdinburghParser() {
        
    }
    
    public static EdinburghParser getInstance() {
        if(instance == null) instance = new EdinburghParser();
        return instance;
    }
    
    @Override
    public HashMap<String, BusStop> getBusStopData(final String[] stopCodes)
            throws BusParserException {
        try {
            Socket sock = new Socket();
            sock.setSoTimeout(30000);
            sock.connect(new InetSocketAddress("bustracker.selfip.org", 4876),
                    20000);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(sock.getInputStream()));
            PrintWriter writer = new PrintWriter(sock.getOutputStream(), true);
            writer.println(STOP_DATA_COMMAND + ":" + stopCodes[0]);
            String jsonString = "";
            String tmp = "";
            boolean readJson = false;
            while ((tmp = reader.readLine()) != null) {
                if(tmp.startsWith("Error:")) {
                    writer.println("exit");
                    reader.close();
                    writer.close();
                    sock.close();
                    throw new BusParserException(BusTimes.ERROR_SERVER);
                }
                if (tmp.equals("+")) {
                    readJson = true;
                } else if (tmp.equals("-")) {
                    break;
                } else if (readJson) {
                    jsonString = jsonString + tmp;
                }
            }
            writer.println("exit");
            reader.close();
            writer.close();
            sock.close();
            
            return parseJSON(jsonString);
        } catch(UnknownHostException e) {
            throw new BusParserException(BusTimes.ERROR_CANNOTRESOLVE);
        } catch(IOException e) {
            throw new BusParserException(BusTimes.ERROR_NOCONNECTION);
        } catch(JSONException e) {
            throw new BusParserException(BusTimes.ERROR_PARSEERR);
        }
    }
    
    private HashMap<String, BusStop> parseJSON(final String jsonString)
            throws JSONException, BusParserException {
        HashMap<String, BusStop> data = new HashMap<String, BusStop>();
        
        JSONObject jo = new JSONObject(jsonString);
        
        String stopCode = jo.getString("stopCode");
        if(stopCode.length() == 0)
            throw new BusParserException(BusTimes.ERROR_NODATA);
        String stopName = jo.getString("stopName");
        EdinburghBusStop busStop = new EdinburghBusStop(stopCode, stopName);
        data.put(stopCode, busStop);
        
        JSONArray services = jo.getJSONArray("services");
        int a = services.length();
        if(a == 0) return data;
        
        JSONObject currService, currBus;
        JSONArray buses;
        int i, j, b;
        BusService busService;
        
        for(i = 0; i < a; i++) {
            currService = services.getJSONObject(i);
            busService = new BusService(currService.getString("serviceName"),
                    currService.getString("route"));
            busStop.addBusService(busService);
            
            buses = currService.getJSONArray("buses");
            b = buses.length();
            if(b == 0) continue;
            
            for(j = 0; j < b; j++) {
                currBus = buses.getJSONObject(j);
                busService.addBus(new EdinburghBus(currService
                        .getString("serviceName"),
                        currBus.getString("destination"),
                        currBus.getString("arrivalTime")));
            }
        }
        return data;
    }
}