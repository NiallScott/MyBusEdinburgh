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
import junit.framework.TestCase;

/**
 * Tests for {@link JourneyDeparture}.
 * 
 * @author Niall Scott
 */
public class JourneyDepartureTests extends TestCase {
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the stopCode is set as null.
     */
    public void testConstructorWithNullStopCode() {
        try {
            new MockJourneyDeparture(null, "stop name", new Date(), 1);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The stopCode is set as null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the stopCode is set as empty.
     */
    public void testConstructorWithEmptyStopCode() {
        try {
            new MockJourneyDeparture("", "stop name", new Date(), 1);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The stopCode is set as empty, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the constructor can accept a null stop name.
     */
    public void testConstructorWithNullStopName() {
        final JourneyDeparture departure =
                new MockJourneyDeparture("123456", null, new Date(), 1);
        assertNull(departure.getStopName());
    }
    
    /**
     * Test that the constructor can accept an empty stop name.
     */
    public void testConstructorWithEmptyStopName() {
        final JourneyDeparture departure =
                new MockJourneyDeparture("123456", "", new Date(), 1);
        assertEquals("", departure.getStopName());
    }
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the departureTime is set as null.
     */
    public void testConstructorWithNullDepartureTime() {
        try {
            new MockJourneyDeparture("123456", "stop name", null, 1);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The departureTime is set as null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the getters correctly return the data passed in to the
     * constructor.
     */
    public void testValidDeparture() {
        final Date date = new Date();
        final JourneyDeparture departure =
                new MockJourneyDeparture("123456", "stop name", date, 1);
        
        assertEquals("123456", departure.getStopCode());
        assertEquals("stop name", departure.getStopName());
        assertEquals(date, departure.getDepartureTime());
    }
    
    /**
     * Test that the compareTo() method correctly returns -1 when the object to
     * compare with is set to null.
     */
    public void testOrderingWithNullObjectToCompare() {
        final JourneyDeparture departure =
                new MockJourneyDeparture("123456", "stop name", new Date(), 1);
        assertEquals(-1, departure.compareTo(null));
    }
    
    /**
     * Test that ordering works correctly based on the value of the 'order'
     * parameter.
     */
    public void testOrdering() {
        JourneyDeparture departure1 =
                new MockJourneyDeparture("123456", "stop name", new Date(), 1);
        JourneyDeparture departure2 =
                new MockJourneyDeparture("654321", "another", new Date(), 2);
        assertTrue(departure1.compareTo(departure2) < 0);
        
        departure1 =
                new MockJourneyDeparture("123456", "stop name", new Date(), 1);
        departure2 =
                new MockJourneyDeparture("654321", "another", new Date(), 1);
        assertEquals(0, departure1.compareTo(departure2));
        
        departure1 =
                new MockJourneyDeparture("123456", "stop name", new Date(), 2);
        departure2 =
                new MockJourneyDeparture("654321", "another", new Date(), 1);
        assertTrue(departure1.compareTo(departure2) > 0);
    }
}