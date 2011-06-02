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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusService;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusStop;

public class EdinburghBusStop extends BusStop {
    
    public EdinburghBusStop(final String stopCode, final String stopName)
            throws IllegalArgumentException {
        super(stopCode, stopName);
    }
    
    public ArrayList<BusService> getSortedByTimeBusServices() {
        ArrayList<BusService> busServices = getBusServices();
        Collections.sort(busServices, serviceComparator);
        return busServices;
    }
    
    private static Comparator<BusService> serviceComparator =
            new Comparator<BusService>() {
        @Override
        public int compare(final BusService a, final BusService b) {
            EdinburghBus busA = (EdinburghBus)a.getFirstBus();
            EdinburghBus busB = (EdinburghBus)b.getFirstBus();
            
            if(busA == null || busB == null) return 0;
            
            return busA.getArrivalDateObject()
                    .compareTo(busB.getArrivalDateObject());
        }
    };
}