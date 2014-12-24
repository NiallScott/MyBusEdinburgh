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

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 * Tests for {@link EdinburghLiveBusStop}.
 * 
 * @author Niall Scott
 */
public class EdinburghLiveBusStopTests extends TestCase {
    
    /**
     * Test that the getters return the correct values.
     */
    public void testGeneric() {
        final List<EdinburghLiveBusService> services =
                new ArrayList<EdinburghLiveBusService>();
        final EdinburghLiveBusStop busStop = new EdinburghLiveBusStop("123456",
                "stop name", services, false);
        
        assertEquals("123456", busStop.getStopCode());
        assertEquals("stop name", busStop.getStopName());
        assertEquals(services, busStop.getServices());
        assertFalse(busStop.isDisrupted());
    }
    
    /**
     * Test that {@link EdinburghLiveBusStop#isDisrupted()} returns true when
     * the disrupted flag has been set.
     */
    public void testIsDisrupted() {
        final EdinburghLiveBusStop busStop = new EdinburghLiveBusStop("123456",
                "stop name", new ArrayList<EdinburghLiveBusService>(), true);
        assertTrue(busStop.isDisrupted());
    }
}