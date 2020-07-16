/*
 * Copyright (C) 2014 - 2020 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.endpoints;

import static org.junit.Assert.assertSame;

import androidx.annotation.NonNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.org.rivernile.android.bustracker.parser.livetimes.BusParser;
import uk.org.rivernile.android.bustracker.parser.livetimes.Journey;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusTimes;

/**
 * Tests for {@link BusTrackerEndpoint}.
 * 
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner.class)
public class BusTrackerEndpointTests {

    @Mock
    private BusParser parser;

    /**
     * Test that {@link BusTrackerEndpoint#getParser()} returns the same {@link BusParser} object
     * given to it in the constructor.
     */
    @Test
    public void testNotNullConstructor() {
        final MockBusTrackerEndpoint endpoint = new MockBusTrackerEndpoint(parser);
        
        assertSame(parser, endpoint.getParser());
    }
    
    /**
     * Because this is testing an abstract class, it's necessary to mock it out in to a concrete
     * class.
     */
    private static class MockBusTrackerEndpoint extends BusTrackerEndpoint {
        
        /**
         * Create a new {@code MockBusTrackerEndpoint}.
         * 
         * @param parser The parser to use.
         */
        MockBusTrackerEndpoint(@NonNull final BusParser parser) {
            super(parser);
        }

        @NonNull
        @Override
        public LiveBusTimes getBusTimes(@NonNull final String[] stopCodes,
                final int numDepartures) {
            throw new UnsupportedOperationException();
        }

        @NonNull
        @Override
        public Journey getJourneyTimes(@NonNull final String stopCode,
                @NonNull final String journeyId) {
            throw new UnsupportedOperationException();
        }
    }
}