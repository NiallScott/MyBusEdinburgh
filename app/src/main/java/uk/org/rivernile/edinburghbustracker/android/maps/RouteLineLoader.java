/*
 * Copyright (C) 2013 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android.maps;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.HashMap;
import java.util.LinkedList;
import uk.org.rivernile.android.utils.SimpleResultLoader;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;

/**
 * This Loader retrieves all the route lines for given route names and creates
 * PolylineOption objects for each poly line. Each bus service can have more
 * than one poly line (known as "chainage"s). This is why each service has a
 * LinkedList of PolylineOptions objects.
 * 
 * @author Niall Scott
 */
public class RouteLineLoader extends SimpleResultLoader<
        HashMap<String, LinkedList<PolylineOptions>>> {
    
    private final String[] services;
    private final BusStopDatabase bsd;
    
    /**
     * Create a new RouteLineLoader.
     * 
     * @param context A Context instance.
     * @param services A String array of service names to load route lines for.
     */
    public RouteLineLoader(final Context context, final String[] services) {
        super(context);
        
        bsd = BusStopDatabase.getInstance(context.getApplicationContext());
        
        this.services = services;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HashMap<String, LinkedList<PolylineOptions>> loadInBackground() {
        final HashMap<String, LinkedList<PolylineOptions>> result =
                new HashMap<String, LinkedList<PolylineOptions>>();
        
        // If we've not been given services, there's no point continuing.
        if(services == null || services.length == 0) {
            return result;
        }
        
        synchronized(bsd) {
            // Get the colours for all the services we're loading here.
            final HashMap<String, String> colours = bsd.getServiceColours(
                    services);
            LinkedList<PolylineOptions> polyLines;
            PolylineOptions plOption = null;
            Cursor c;
            int currentChainage, chainage;
            String hexColour;
            int currentColour;
            
            for(String service : services) {
                // Get the hex colour for the service.
                hexColour = colours.get(service);
                if(hexColour != null) {
                    try {
                        // Hopefully the colour can be parsed.
                        currentColour = Color.parseColor(hexColour);
                    } catch(IllegalArgumentException e) {
                        // If not, default to black.
                        currentColour = Color.BLACK;
                    }
                } else {
                    // If the hex colour is not known, default to black.
                    currentColour = Color.BLACK;
                }
                
                // Create the data structures and then query the database for
                // service points.
                polyLines = new LinkedList<PolylineOptions>();
                result.put(service, polyLines);
                c = bsd.getServicePointsForService(service);
                // Default the chainage to -1 as it's always a positive number.
                currentChainage = -1;
                
                if(c != null) {
                    while(c.moveToNext()) {
                        chainage = c.getInt(0);
                        
                        // If the chainage has changed (or this is the first
                        // pass), then create a new PolyLineOptions object.
                        if(chainage != currentChainage) {
                            plOption = new PolylineOptions();
                            polyLines.add(plOption);
                            plOption.color(currentColour);
                            
                            currentChainage = chainage;
                        }
                        
                        // Add each LatLng pair as a Polyline point.
                        plOption.add(new LatLng(c.getDouble(1),
                                c.getDouble(2)));
                    }
                    
                    c.close();
                }
            }
        }
        
        return result;
    }
}