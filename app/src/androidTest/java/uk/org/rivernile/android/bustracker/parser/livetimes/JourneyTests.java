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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import junit.framework.TestCase;

/**
 * Tests for {@link Journey}.
 * 
 * @author Niall Scott
 */
public class JourneyTests extends TestCase {
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the journeyId is set as null.
     */
    public void testConstructorWithNullJourneyId() {
        try {
            new MockJourney(null, "22", new ArrayList<JourneyDeparture>(),
                    123456789L);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The journeyId is set as null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the journeyId is set as empty.
     */
    public void testConstructorWithEmptyJourneyId() {
        try {
            new MockJourney("", "22", new ArrayList<JourneyDeparture>(),
                    123456789L);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The journeyId is set as empty, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the service name is set as null.
     */
    public void testConstructorWithNullServiceName() {
        try {
            new MockJourney("123456", null, new ArrayList<JourneyDeparture>(),
                    123456789L);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The serviceName is set as null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the service name is set as empty.
     */
    public void testConstructorWithEmptyServiceName() {
        try {
            new MockJourney("123456", "", new ArrayList<JourneyDeparture>(),
                    123456789L);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The serviceName is set as empty, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the departures is set as null.
     */
    public void testConstructorWithNullDepartures() {
        try {
            new MockJourney("123456", "22", null, 123456789L);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The departures is set as empty, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the getters return correct data with the data that was supplied
     * in the constructor.
     */
    public void testValid() {
        final ArrayList<JourneyDeparture> departuresIn =
                new ArrayList<JourneyDeparture>();
        departuresIn.add(new MockJourneyDeparture("123", "A", new Date(), 1));
        departuresIn.add(new MockJourneyDeparture("456", "B", new Date(), 2));
        departuresIn.add(new MockJourneyDeparture("789", "C", new Date(), 3));
        
        final Journey journey = new MockJourney("123", "12", departuresIn,
                123456789L);
        assertEquals(123456789L, journey.getReceiveTime());
        assertEquals("123", journey.getJourneyId());
        assertEquals("12", journey.getServiceName());
        
        final List<JourneyDeparture> departuresOut = journey.getDepartures();
        assertNotNull(departuresOut);
        assertEquals(3, departuresOut.size());
        
        JourneyDeparture departure = departuresOut.get(0);
        assertEquals("123", departure.getStopCode());
        
        departure = departuresOut.get(1);
        assertEquals("456", departure.getStopCode());
        
        departure = departuresOut.get(2);
        assertEquals("789", departure.getStopCode());
    }
}