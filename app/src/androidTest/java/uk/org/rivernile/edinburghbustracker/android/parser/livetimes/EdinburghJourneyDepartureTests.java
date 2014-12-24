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

import java.util.Date;
import junit.framework.TestCase;
import uk.org.rivernile.android.bustracker.parser.livetimes.JourneyDeparture;

/**
 * Tests for {@link EdinburghJourneyDeparture}.
 * 
 * @author Niall Scott
 */
public class EdinburghJourneyDepartureTests extends TestCase {
    
    /**
     * Test that the getters return the correct values.
     */
    public void testGeneralValid() {
        final Date time = new Date();
        final JourneyDeparture departure =
                new EdinburghJourneyDeparture("123456", "stop name", time, 2,
                        'H', 'N', false, 1);
        
        assertEquals("123456", departure.getStopCode());
        assertEquals("stop name", departure.getStopName());
        assertEquals(time, departure.getDepartureTime());
        assertEquals(2, departure.getDepartureMinutes());
        assertFalse(departure.isBusStopDisrupted());
        assertFalse(departure.isEstimatedTime());
        assertFalse(departure.isDelayed());
        assertFalse(departure.isDiverted());
        assertFalse(departure.isTerminus());
        assertFalse(departure.isPartRoute());
    }
    
    /**
     * Test that {@link EdinburghJourneyDeparture#isBusStopDisrupted()} returns
     * true when the isDisrupted flag is set to true.
     */
    public void testBusStopDisrupted() {
        final JourneyDeparture departure =
                new EdinburghJourneyDeparture("123456", "stop name", new Date(),
                        2, 'H', 'N', true, 1);
        assertTrue(departure.isBusStopDisrupted());
    }
    
    /**
     * Test that {@link EdinburghJourneyDeparture#isEstimatedTime()} returns
     * true when reliability field indicates the time is estimated.
     */
    public void testIsEstimatedTime() {
        final JourneyDeparture departure =
                new EdinburghJourneyDeparture("123456", "stop name", new Date(),
                        2, 'T', 'N', false, 1);
        assertTrue(departure.isEstimatedTime());
        assertFalse(departure.isDelayed());
        assertFalse(departure.isDiverted());
        assertFalse(departure.isTerminus());
        assertFalse(departure.isPartRoute());
    }
    
    /**
     * Test that {@link EdinburghJourneyDeparture#isDelayed()} returns true when
     * the reliability field indicates the bus is delayed.
     */
    public void testIsDelayed() {
        final JourneyDeparture departure =
                new EdinburghJourneyDeparture("123456", "stop name", new Date(),
                        2, 'B', 'N', false, 1);
        assertFalse(departure.isEstimatedTime());
        assertTrue(departure.isDelayed());
        assertFalse(departure.isDiverted());
        assertFalse(departure.isTerminus());
        assertFalse(departure.isPartRoute());
    }
    
    /**
     * Test that {@link EdinburghJourneyDeparture#isDiverted()} returns true
     * when the reliability field indicates the bus is diverted.
     */
    public void testIsDiverted() {
        final JourneyDeparture departure =
                new EdinburghJourneyDeparture("123456", "stop name", new Date(),
                        2, 'V', 'N', false, 1);
        assertFalse(departure.isEstimatedTime());
        assertFalse(departure.isDelayed());
        assertTrue(departure.isDiverted());
        assertFalse(departure.isTerminus());
        assertFalse(departure.isPartRoute());
    }
    
    /**
     * Test that {@link EdinburghJourneyDeparture#isTerminus()} returns true
     * when the type field indicates the departure point is a terminus.
     */
    public void testIsTerminus() {
        final JourneyDeparture departure =
                new EdinburghJourneyDeparture("123456", "stop name", new Date(),
                        2, 'H', 'D', false, 1);
        assertFalse(departure.isEstimatedTime());
        assertFalse(departure.isDelayed());
        assertFalse(departure.isDiverted());
        assertTrue(departure.isTerminus());
        assertFalse(departure.isPartRoute());
    }
    
    /**
     * Test that {@link EdinburghJourneyDeparture#isPartRoute()} returns true
     * when the type field indicates that the bus is on part-route.
     */
    public void testIsPartRoute() {
        final JourneyDeparture departure =
                new EdinburghJourneyDeparture("123456", "stop name", new Date(),
                        2, 'H', 'P', false, 1);
        assertFalse(departure.isEstimatedTime());
        assertFalse(departure.isDelayed());
        assertFalse(departure.isDiverted());
        assertFalse(departure.isTerminus());
        assertTrue(departure.isPartRoute());
    }
}