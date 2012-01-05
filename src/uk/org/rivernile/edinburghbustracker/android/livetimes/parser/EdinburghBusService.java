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

import uk.org.rivernile.android.bustracker.parser.livetimes.BusService;

/**
 * This class contains an Edinburgh specific implementation of BusService.
 * 
 * @author Niall Scott
 */
public class EdinburghBusService extends BusService {
    
    private boolean disruption = false;
    
    /**
     * Create a new instance of EdinburghBusService.
     * 
     * @param serviceName The name of the service.
     * @param route The route of the service.
     * @param disruption The disruption status of this service.
     */
    public EdinburghBusService(final String serviceName, final String route,
            final boolean disruption) {
        super(serviceName, route);
        
        this.disruption = disruption;
    }
    
    /**
     * Get the disruption status for this bus service.
     * 
     * @return The disruption status for this bus service.
     */
    public boolean getDisruption() {
        return disruption;
    }
}