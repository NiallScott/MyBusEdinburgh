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
import java.util.List;
import junit.framework.TestCase;

/**
 * Tests for {@link LiveBusService}.
 * 
 * @author Niall Scott
 */
public class LiveBusServiceTests extends TestCase {
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the serviceName is set to null.
     */
    public void testConstructorWithNullServiceName() {
        try {
            new MockLiveBusService(null, new ArrayList<LiveBus>());
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The serviceName is set as null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the serviceName is set to empty.
     */
    public void testConstructorWithEmptyServiceName() {
        try {
            new MockLiveBusService("", new ArrayList<LiveBus>());
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The serviceName is set as empty, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the List of buses is set to null.
     */
    public void testConstructorWithNullBusList() {
        try {
            new MockLiveBusService("22", null);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The buses List is set as null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the getters return the correct data.
     */
    public void testValidLiveBusService() {
        final List<LiveBus> buses = new ArrayList<LiveBus>();
        final LiveBusService service = new MockLiveBusService("41", buses);
        assertEquals("41", service.getServiceName());
        assertEquals(buses, service.getLiveBuses());
    }
    
    /**
     * Test that instances of LiveBusService order correctly when held inside a
     * List.
     */
    public void testOrdering() {
        final List<LiveBus> buses = new ArrayList<LiveBus>();
        final LiveBusService serviceA = new MockLiveBusService("25", buses);
        final LiveBusService serviceB = new MockLiveBusService("X25", buses);
        
        assertTrue(serviceA.compareTo(null) < 0);
        assertEquals(0, serviceA.compareTo(serviceA));
        assertTrue(serviceA.compareTo(serviceB) < 0);
        assertTrue(serviceB.compareTo(serviceA) > 0);
    }
}