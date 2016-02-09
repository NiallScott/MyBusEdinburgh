/*
 * Copyright (C) 2014 - 2016 Niall 'Rivernile' Scott
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.support.test.runner.AndroidJUnit4;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link LiveBusTimes}.
 * 
 * @author Niall Scott
 */
@RunWith(AndroidJUnit4.class)
public class LiveBusTimesTests {

    /**
     * Test that {@link LiveBusTimes.Builder#build()} throws an {@link IllegalArgumentException}
     * when the bus stops is set to {@code null}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithNullBusStops() {
        new LiveBusTimes.Builder()
                .setBusStops(null)
                .build();
    }
    
    /**
     * Test that the getters return the correct data when an empty {@link Map} is supplied.
     */
    @Test
    public void testWithEmptyBusStops() {
        final Map<String, LiveBusStop> map =
                Collections.unmodifiableMap(Collections.<String, LiveBusStop>emptyMap());
        final LiveBusTimes busTimes = new LiveBusTimes.Builder()
                .setBusStops(map)
                .setReceiveTime(123456789L)
                .setHasGlobalDisruption(false)
                .build();
        final Set<String> stopCodes = busTimes.getBusStops();
        
        assertNotNull(stopCodes);
        assertTrue(stopCodes.isEmpty());
        assertTrue(busTimes.isEmpty());
        assertEquals(0, busTimes.size());
        assertEquals(123456789L, busTimes.getReceiveTime());
    }
    
    /**
     * Test that the getters return the correct data when a {@link Map} with a single
     * {@link LiveBusStop} is supplied.
     */
    @Test
    public void testWithSingleBusStop() {
        final HashMap<String, LiveBusStop> map = new HashMap<>();
        map.put("123456", new LiveBusStop.Builder()
                .setStopCode("123456")
                .setStopName("stop name")
                .setServices(Collections.<LiveBusService>emptyList())
                .build());
        final LiveBusTimes busTimes = new LiveBusTimes.Builder()
                .setBusStops(Collections.unmodifiableMap(map))
                .setReceiveTime(123456789L)
                .build();
        
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
     * Test that the getters return the correct data when a {@link Map} with multiple
     * {@link LiveBusStop}s is supplied.
     */
    @Test
    public void testWithMultipleBusStops() {
        final HashMap<String, LiveBusStop> map = new HashMap<>();
        map.put("123456", new LiveBusStop.Builder()
                .setStopCode("123456")
                .setStopName("stop name")
                .setServices(Collections.<LiveBusService>emptyList())
                .build());
        map.put("112233", new LiveBusStop.Builder()
                .setStopCode("112233")
                .setStopName("stop name")
                .setServices(Collections.<LiveBusService>emptyList())
                .build());
        map.put("214365", new LiveBusStop.Builder()
                .setStopCode("214365")
                .setStopName("stop name")
                .setServices(Collections.<LiveBusService>emptyList())
                .build());
        final LiveBusTimes busTimes = new LiveBusTimes.Builder()
                .setBusStops(Collections.unmodifiableMap(map))
                .setReceiveTime(123456789L)
                .build();
        
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