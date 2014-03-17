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
 * A MockJourney extends {@link LiveBus} to provide mock implementations for
 * abstract methods.
 * 
 * @author Niall Scott
 */
class MockLiveBus extends LiveBus {
    
    /**
     * Create a new MockLiveBus.
     * 
     * @param destination Mock destination. Must not be null or empty.
     * @param departureTime Mock departureTime. Must not be null.
     */
    public MockLiveBus(final String destination, final Date departureTime) {
        super(destination, departureTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTerminus() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getJourneyId() {
        return null;
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