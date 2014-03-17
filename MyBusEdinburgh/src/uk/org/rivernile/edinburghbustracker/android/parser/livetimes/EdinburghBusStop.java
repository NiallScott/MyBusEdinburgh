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

package uk.org.rivernile.edinburghbustracker.android.parser.livetimes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusService;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusStop;

/**
 * This is the Edinburgh specific implementation of a BusStop. Essentially it
 * adds sorting by time to the results.
 * 
 * @author Niall Scott
 */
public class EdinburghBusStop extends BusStop {
    
    private final boolean disruption;
    
    /**
     * Create a new EdinburghBusStop instance.
     * 
     * @param stopCode The bus stop code.
     * @param stopName The bus stop name.
     * @param disruption The disruption status of the bus stop.
     * @throws IllegalArgumentException When an illegal argument is passed.
     */
    public EdinburghBusStop(final String stopCode, final String stopName,
            final boolean disruption) throws IllegalArgumentException {
        super(stopCode, stopName);
        
        this.disruption = disruption;
    }
    
    /**
     * Get the list sorted by time.
     * 
     * @return The list sorted by time.
     */
    public ArrayList<BusService> getSortedByTimeBusServices() {
        final ArrayList<BusService> busServices = getBusServices();
        Collections.sort(busServices, serviceComparator);
        return busServices;
    }
    
    /**
     * Get the disruption status for this bus stop.
     * 
     * @return The disruption status of this bus stop.
     */
    public boolean getDisruption() {
        return disruption;
    }
    
    private static Comparator<BusService> serviceComparator =
            new Comparator<BusService>() {
        @Override
        public int compare(final BusService a, final BusService b) {
            final EdinburghBus busA = (EdinburghBus)a.getFirstBus();
            final EdinburghBus busB = (EdinburghBus)b.getFirstBus();
            
            if(busA == null || busB == null) return 0;
            
            return busA.getArrivalMinutes() - busB.getArrivalMinutes();
        }
    };
}