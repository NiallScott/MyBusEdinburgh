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

package uk.org.rivernile.android.bustracker.endpoints;

import java.util.HashMap;
import junit.framework.TestCase;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusParser;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusParserException;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusStop;
import uk.org.rivernile.edinburghbustracker.android.livetimes.parser
        .EdinburghParser;

/**
 * Tests for BusTrackerEndpoint.
 * 
 * @author Niall Scott
 */
public class BusTrackerEndpointTests extends TestCase {
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentExxception
     * when the parser is set to null.
     */
    public void testNullConstructor() {
        try {
            new MockBusTrackerEndpoint(null);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The parser is set to null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that {@link BusTrackerEndpoint#getParser()} returns the same
     * {@link BusParser} object given to it in the constructor.
     */
    public void testNotNullConstructor() {
        final EdinburghParser parser = new EdinburghParser();
        final MockBusTrackerEndpoint endpoint =
                new MockBusTrackerEndpoint(parser);
        
        assertEquals(parser, endpoint.getParser());
    }
    
    /**
     * Because this is testing an abstract class, it's necessary to mock it out
     * in to a concrete class.
     */
    private static class MockBusTrackerEndpoint extends BusTrackerEndpoint {
        
        /**
         * Create a new MockBusTrackerEndpoint.
         * 
         * @param parser The parser to use.
         */
        public MockBusTrackerEndpoint(final BusParser parser) {
            super(parser);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public HashMap<String, BusStop> getBusTimes(final String[] stopCodes,
                final int numDepartures) throws BusParserException {
            return null;
        }
    }
}