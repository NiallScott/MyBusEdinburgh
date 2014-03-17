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

package uk.org.rivernile.android.bustracker.parser.livetimes;

import java.util.List;

/**
 * A MockLiveBusStop extends {@link LiveBusStop} to provide mock implementations
 * for abstract methods.
 * 
 * @author Niall Scott
 */
class MockLiveBusStop extends LiveBusStop {
    
    /**
     * Create a new MockLiveBusStop.
     * 
     * @param stopCode The unique code of this bus stop. Must not be null or
     * empty.
     * @param stopName The name of this bus stop.
     * @param services A List of {@link LiveBusService}s that have live
     * departures at this bus stop.
     */
    public MockLiveBusStop(final String stopCode, final String stopName,
            final List<LiveBusService> services) {
        super(stopCode, stopName, services);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDisrupted() {
        return false;
    }
}