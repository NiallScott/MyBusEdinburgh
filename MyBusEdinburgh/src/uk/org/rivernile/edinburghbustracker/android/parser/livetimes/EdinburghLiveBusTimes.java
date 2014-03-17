/*
 * Copyright (C) 2014 Niall 'Rivernile' Scott
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

import java.util.Map;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusTimes;

/**
 * This class is an Edinburgh-specific implementation of {@link LiveBusTimes}.
 * 
 * @author Niall Scott
 */
public class EdinburghLiveBusTimes extends LiveBusTimes<EdinburghLiveBusStop> {
    
    private final boolean globalDisruption;
    
    /**
     * Create a new EdinburghLiveBusTimes instance.
     * 
     * @param busStops The mapping of bus stop codes to
     * {@link EdinburghLiveBusStop} instances. Must not be null.
     * @param globalDisruption true if there is a global disruption in place,
     * false if not.
     */
    public EdinburghLiveBusTimes(
            final Map<String, EdinburghLiveBusStop> busStops,
            final boolean globalDisruption) {
        super(busStops);
        
        this.globalDisruption = globalDisruption;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGlobalDisruption() {
        return globalDisruption;
    }
}