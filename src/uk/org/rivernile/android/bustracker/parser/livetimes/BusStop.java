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

package uk.org.rivernile.android.bustracker.parser.livetimes;

import com.davekoelle.alphanum.AlphanumComparator;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A bus stop is a collection of bus services, in the context of this class.
 * 
 * @author Niall Scott
 */
public class BusStop {
    
    private final String stopCode;
    private final String stopName;
    private final ArrayList<BusService> busServices;
    
    /**
     * Create a new BusStop. This is an immutable class.
     * 
     * @param stopCode The stop code for this bus stop.
     * @param stopName The name for this bus stop.
     */
    public BusStop(final String stopCode, final String stopName) {
        if(stopCode == null || stopCode.length() == 0)
            throw new IllegalArgumentException("The stop code must not be " +
                    "null or blank.");
        
        if(stopName == null || stopName.length() == 0)
            throw new IllegalArgumentException("The stop name must not be " +
                    "null or blank.");
        
        this.stopCode = stopCode;
        this.stopName = stopName;
        
        busServices = new ArrayList<BusService>();
    }
    
    /**
     * Get the stop code.
     * 
     * @return The stop code.
     */
    public String getStopCode() {
        return stopCode;
    }
    
    /**
     * Get the stop name.
     * 
     * @return The stop name.
     */
    public String getStopName() {
        return stopName;
    }
    
    /**
     * Add a new bus service to this bus stop.
     * 
     * @param busService The bus service to add.
     */
    public void addBusService(final BusService busService) {
        if(busService == null)
            throw new IllegalArgumentException("The bus service must not be " +
                    "null.");
        
        busServices.add(busService);
    }
    
    /**
     * Get the ArrayList of all bus services for this bus stop.
     * @return The ArrayList of all bus services for this bus stop.
     */
    public ArrayList<BusService> getBusServices() {
        Collections.sort(busServices, new AlphanumComparator());
        return (ArrayList<BusService>)busServices.clone();
    }
}