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

/**
 * Tests for {@link EdinburghLiveBus}.
 * 
 * @author Niall Scott
 */
public class EdinburghLiveBusTests extends TestCase {
    
    /**
     * Test that the getters return the correct values.
     */
    public void testGenericValid() {
        final Date time = new Date();
        final EdinburghLiveBus liveBus = new EdinburghLiveBus("Destination",
                time, 2, 'H', 'N', "123456", "9876");
        
        assertEquals("Destination", liveBus.getDestination());
        assertEquals(time, liveBus.getDepartureTime());
        assertEquals(2, liveBus.getDepartureMinutes());
        assertEquals("123456", liveBus.getTerminus());
        assertEquals("9876", liveBus.getJourneyId());
        assertFalse(liveBus.isEstimatedTime());
        assertFalse(liveBus.isDelayed());
        assertFalse(liveBus.isDiverted());
        assertFalse(liveBus.isTerminus());
        assertFalse(liveBus.isPartRoute());
    }
    
    /**
     * Test that {@link EdinburghLiveBus#isEstimatedTime()} returns true when
     * reliability field indicates the time is estimated.
     */
    public void testIsEstimatedTime() {
        final EdinburghLiveBus liveBus = new EdinburghLiveBus("Destination",
                new Date(), 2, 'T', 'N', "123456", "9876");
        assertTrue(liveBus.isEstimatedTime());
        assertFalse(liveBus.isDelayed());
        assertFalse(liveBus.isDiverted());
        assertFalse(liveBus.isTerminus());
        assertFalse(liveBus.isPartRoute());
    }
    
    /**
     * Test that {@link EdinburghLiveBus#isDelayed()} returns true when the
     * reliability field indicates the bus is delayed.
     */
    public void testIsDelayed() {
        final EdinburghLiveBus liveBus = new EdinburghLiveBus("Destination",
                new Date(), 2, 'B', 'N', "123456", "9876");
        assertFalse(liveBus.isEstimatedTime());
        assertTrue(liveBus.isDelayed());
        assertFalse(liveBus.isDiverted());
        assertFalse(liveBus.isTerminus());
        assertFalse(liveBus.isPartRoute());
    }
    
    /**
     * Test that {@link EdinburghLiveBus#isDiverted()} returns true when the
     * reliability field indicates the bus is diverted.
     */
    public void testIsDiverted() {
        final EdinburghLiveBus liveBus = new EdinburghLiveBus("Destination",
                new Date(), 2, 'V', 'N', "123456", "9876");
        assertFalse(liveBus.isEstimatedTime());
        assertFalse(liveBus.isDelayed());
        assertTrue(liveBus.isDiverted());
        assertFalse(liveBus.isTerminus());
        assertFalse(liveBus.isPartRoute());
    }
    
    /**
     * Test that {@link EdinburghLiveBus#isTerminus()} returns true when the
     * type field indicates the departure point is a terminus.
     */
    public void testIsTerminus() {
        final EdinburghLiveBus liveBus = new EdinburghLiveBus("Destination",
                new Date(), 2, 'H', 'D', "123456", "9876");
        assertFalse(liveBus.isEstimatedTime());
        assertFalse(liveBus.isDelayed());
        assertFalse(liveBus.isDiverted());
        assertTrue(liveBus.isTerminus());
        assertFalse(liveBus.isPartRoute());
    }
    
    /**
     * Test that {@link EdinburghLiveBus#isPartRoute()} returns true when the
     * type field indicates that the bus is on part-route.
     */
    public void testIsPartRoute() {
        final EdinburghLiveBus liveBus = new EdinburghLiveBus("Destination",
                new Date(), 2, 'H', 'P', "123456", "9876");
        assertFalse(liveBus.isEstimatedTime());
        assertFalse(liveBus.isDelayed());
        assertFalse(liveBus.isDiverted());
        assertFalse(liveBus.isTerminus());
        assertTrue(liveBus.isPartRoute());
    }
}