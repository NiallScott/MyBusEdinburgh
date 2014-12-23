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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import junit.framework.TestCase;

/**
 * Tests for {@link LiveBus}.
 * 
 * @author Niall Scott
 */
public class LiveBusTests extends TestCase {
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the destination is set to null.
     */
    public void testConstructorWithNullDestination() {
        try {
            new MockLiveBus(null, new Date());
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The destination is set to null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the destination is set to empty.
     */
    public void testConstructorWithEmptyDestination() {
        try {
            new MockLiveBus("", new Date());
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The destination is set to empty, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the departureTime is set to null.
     */
    public void testConstructorWithNullDepartureTime() {
        try {
            new MockLiveBus("destination", null);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The departureTime is set to empty, so an "
                + "IllegalArgumentException should be thrown.");
    }
    
    /**
     * Test that the getters correctly return their data.
     */
    public void testValidDeparture() {
        final Date time = new Date();
        final LiveBus liveBus = new MockLiveBus("somewhere", time);
        assertEquals("somewhere", liveBus.getDestination());
        assertEquals(time, liveBus.getDepartureTime());
    }
    
    /**
     * Test that the compareTo() method correctly returns -1 when the object to
     * compare with is set to null.
     */
    public void testOrderingWithNullObjectToCompare() {
        final LiveBus liveBus = new MockLiveBus("somewhere", new Date());
        assertEquals(-1, liveBus.compareTo(null));
    }
    
    /**
     * Test that ordering works correctly.
     */
    public void testOrdering() {
        final Calendar cal1 = new GregorianCalendar(2014, Calendar.FEBRUARY, 1);
        final Calendar cal2 = new GregorianCalendar(2014, Calendar.MARCH, 1);
        
        final LiveBus bus1 = new MockLiveBus("A", cal1.getTime());
        final LiveBus bus2 = new MockLiveBus("B", cal2.getTime());
        final LiveBus bus3 = new MockLiveBus("C", cal1.getTime());
        assertTrue(bus1.compareTo(bus2) < 0);
        assertTrue(bus2.compareTo(bus1) > 0);
        assertEquals(0, bus1.compareTo(bus3));
    }
}