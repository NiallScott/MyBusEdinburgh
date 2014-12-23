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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;

/**
 * Tests for {@link LiveBusTimes}.
 * 
 * @author Niall Scott
 */
public class LiveBusTimesTests extends TestCase {
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the busStops Map is set as null.
     */
    public void testConstructorWithNullBusStops() {
        try {
            new MockLiveBusTimes(null, 123456789L);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The busStops is set as null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the getters return the correct data when an empty Map is
     * supplied.
     */
    public void testWithEmptyBusStops() {
        final Map<String, LiveBusStop> map =
                Collections.unmodifiableMap(
                        Collections.<String, LiveBusStop>emptyMap());
        final LiveBusTimes busTimes = new MockLiveBusTimes(map, 123456789L);
        final Set<String> stopCodes = busTimes.getBusStops();
        
        assertNotNull(stopCodes);
        assertTrue(stopCodes.isEmpty());
        assertTrue(busTimes.isEmpty());
        assertEquals(0, busTimes.size());
        assertEquals(123456789L, busTimes.getReceiveTime());
    }
    
    /**
     * Test that the getters return the correct data when a Map with a single
     * {@link LiveBusStop} is supplied.
     */
    public void testWithSingleBusStop() {
        final HashMap<String, LiveBusStop> map =
                new HashMap<String, LiveBusStop>();
        map.put("123456", new MockLiveBusStop("123456", "stop name",
                Collections.<LiveBusService>emptyList()));
        final LiveBusTimes busTimes =
                new MockLiveBusTimes(Collections.unmodifiableMap(map),
                123456789L);
        
        assertEquals(123456789L, busTimes.getReceiveTime());
        assertFalse(busTimes.isEmpty());
        assertEquals(1, busTimes.size());
        
        final Set<String> stopCodes = busTimes.getBusStops();
        assertNotNull(stopCodes);
        assertFalse(stopCodes.isEmpty());
        assertEquals(1, stopCodes.size());
        
        assertTrue(busTimes.containsBusStop("123456"));
        assertTrue(stopCodes.contains("123456"));
        assertFalse(busTimes.containsBusStop("987654"));
        assertFalse(stopCodes.contains("987654"));
        
        final LiveBusStop busStop = busTimes.getBusStop("123456");
        assertNotNull(busStop);
        assertEquals("123456", busStop.getStopCode());
        assertNull(busTimes.getBusStop("987654"));
    }
    
    /**
     * Test that the getters return the correct data when a Map with multiple
     * {@link LiveBusStop}s is supplied.
     */
    public void testWithMultipleBusStops() {
        final HashMap<String, LiveBusStop> map =
                new HashMap<String, LiveBusStop>();
        map.put("123456", new MockLiveBusStop("123456", "stop name",
                Collections.<LiveBusService>emptyList()));
        map.put("112233", new MockLiveBusStop("112233", "stop name",
                Collections.<LiveBusService>emptyList()));
        map.put("214365", new MockLiveBusStop("214365", "stop name",
                Collections.<LiveBusService>emptyList()));
        final LiveBusTimes busTimes =
                new MockLiveBusTimes(Collections.unmodifiableMap(map),
                123456789L);
        
        assertEquals(123456789L, busTimes.getReceiveTime());
        assertFalse(busTimes.isEmpty());
        assertEquals(3, busTimes.size());
        
        final Set<String> stopCodes = busTimes.getBusStops();
        assertNotNull(stopCodes);
        assertFalse(stopCodes.isEmpty());
        assertEquals(3, stopCodes.size());
        
        assertTrue(busTimes.containsBusStop("123456"));
        assertTrue(stopCodes.contains("123456"));
        assertTrue(busTimes.containsBusStop("112233"));
        assertTrue(stopCodes.contains("112233"));
        assertTrue(busTimes.containsBusStop("214365"));
        assertTrue(stopCodes.contains("214365"));
        assertFalse(busTimes.containsBusStop("987654"));
        assertFalse(stopCodes.contains("987654"));
        
        LiveBusStop busStop = busTimes.getBusStop("123456");
        assertNotNull(busStop);
        assertEquals("123456", busStop.getStopCode());
        
        busStop = busTimes.getBusStop("112233");
        assertNotNull(busStop);
        assertEquals("112233", busStop.getStopCode());
        
        busStop = busTimes.getBusStop("214365");
        assertNotNull(busStop);
        assertEquals("214365", busStop.getStopCode());
        
        assertNull(busTimes.getBusStop("987654"));
    }
}