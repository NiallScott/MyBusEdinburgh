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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.support.test.runner.AndroidJUnit4;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link LiveBusStop}.
 * 
 * @author Niall Scott
 */
@RunWith(AndroidJUnit4.class)
public class LiveBusStopTests {

    /**
     * Test that {@link LiveBusStop.Builder#build()} throws an {@link IllegalArgumentException}
     * when the stop code is set to {@code null}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithNullStopCode() {
        new LiveBusStop.Builder()
                .setStopCode(null)
                .setServices(Collections.<LiveBusService>emptyList())
                .build();
    }

    /**
     * Test that {@link LiveBusStop.Builder#build()} throws an {@link IllegalArgumentException}
     * when the stop code is set to empty.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithEmptyStopCode() {
        new LiveBusStop.Builder()
                .setStopCode("")
                .setServices(Collections.<LiveBusService>emptyList())
                .build();
    }

    /**
     * Test that {@link LiveBusStop.Builder#build()} does not throw an exception when the stop
     * name is set to empty.
     */
    @Test
    public void testBuilderWithEmptyStopName() {
        new LiveBusStop.Builder()
                .setStopCode("123456")
                .setStopName("")
                .setServices(Collections.<LiveBusService>emptyList())
                .build();
    }

    /**
     * Test that {@link LiveBusStop.Builder#build()} throws an {@link IllegalArgumentException}
     * when the services is set to {@code null}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithNullServices() {
        new LiveBusStop.Builder()
                .setStopCode("123456")
                .setServices(null)
                .build();
    }

    /**
     * Test the default values of a {@link LiveBusStop} object.
     */
    @Test
    public void testDefault() {
        final LiveBusStop busStop = new LiveBusStop.Builder()
                .setStopCode("123456")
                .setServices(Collections.<LiveBusService>emptyList())
                .build();

        assertEquals("123456", busStop.getStopCode());
        assertNull(busStop.getStopName());
        assertTrue(busStop.getServices().isEmpty());
        assertFalse(busStop.isDisrupted());
    }

    /**
     * Test building a {@link LiveBusStop} with valid values produces an object that returns
     * expected values.
     */
    @Test
    public void testValid() {
        final LiveBusService service = new LiveBusService.Builder()
                .setServiceName("1")
                .setBuses(Collections.<LiveBus>emptyList())
                .build();
        final ArrayList<LiveBusService> services = new ArrayList<>();
        services.add(service);
        final LiveBusStop busStop = new LiveBusStop.Builder()
                .setStopCode("123456")
                .setStopName("Stop name")
                .setServices(services)
                .setIsDisrupted(true)
                .build();

        assertEquals("123456", busStop.getStopCode());
        assertEquals("Stop name", busStop.getStopName());
        assertEquals(1, busStop.getServices().size());
        assertEquals("1", busStop.getServices().get(0).getServiceName());
        assertTrue(busStop.isDisrupted());
    }
}