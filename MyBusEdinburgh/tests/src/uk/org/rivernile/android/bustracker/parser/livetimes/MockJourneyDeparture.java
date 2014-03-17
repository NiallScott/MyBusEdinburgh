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

import java.util.Date;

/**
 * A MockJourneyDeparture extends {@link JourneyDeparture} to provide mock
 * implementations for abstract methods.
 * 
 * @author Niall Scott
 */
class MockJourneyDeparture extends JourneyDeparture {

    /**
     * Create a MockJourneyDeaprture.
     * 
     * @param stopCode Mock stopCode.
     * @param stopName Mock stopName. Can be null.
     * @param departureTime Mock departure time.
     * @param order Mock order value.
     */
    public MockJourneyDeparture(final String stopCode,
            final String stopName, final Date departureTime,
            final int order) {
        super(stopCode, stopName, departureTime, order);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBusStopDisrupted() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEstimatedTime() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDelayed() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDiverted() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTerminus() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPartRoute() {
        return false;
    }
}