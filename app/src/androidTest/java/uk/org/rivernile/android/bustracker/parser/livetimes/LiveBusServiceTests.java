/*
 * Copyright (C) 2014 - 2018 Niall 'Rivernile' Scott
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

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Tests for {@link LiveBusService}.
 * 
 * @author Niall Scott
 */
public class LiveBusServiceTests {

    /**
     * Test that {@link LiveBusService.Builder#build()} throws an {@link IllegalArgumentException}
     * when the service name is set to {@code null}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithNullServiceName() {
        new LiveBusService.Builder()
                .setServiceName(null)
                .setBuses(Collections.emptyList())
                .build();
    }

    /**
     * Test that {@link LiveBusService.Builder#build()} throws an {@link IllegalArgumentException}
     * when the service name is set to empty.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithEmptyServiceName() {
        new LiveBusService.Builder()
                .setServiceName("")
                .setBuses(Collections.emptyList())
                .build();
    }

    /**
     * Test that {@link LiveBusService.Builder#build()} throws an {@link IllegalArgumentException}
     * when the {@link List} of {@link LiveBus}es is set to {@code null}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithNullBuses() {
        new LiveBusService.Builder()
                .setServiceName("1")
                .setBuses(null)
                .build();
    }

    /**
     * Test that {@link LiveBusService.Builder#build()} does not throw an exception when the
     * operator is set to empty.
     */
    @Test
    public void testBuilderWithEmptyOperator() {
        new LiveBusService.Builder()
                .setServiceName("1")
                .setBuses(Collections.emptyList())
                .setOperator("")
                .build();
    }

    /**
     * Test that {@link LiveBusService.Builder#build()} does not throw an exception when the
     * route is set to empty.
     */
    @Test
    public void testBuilderWithEmptyRoute() {
        new LiveBusService.Builder()
                .setServiceName("1")
                .setBuses(Collections.emptyList())
                .setRoute("")
                .build();
    }

    /**
     * Test the default values of a {@link LiveBusService} object.
     */
    @Test
    public void testDefault() {
        final LiveBusService service = new LiveBusService.Builder()
                .setServiceName("1")
                .setBuses(Collections.emptyList())
                .build();

        assertEquals("1", service.getServiceName());
        assertTrue(service.getLiveBuses().isEmpty());
        assertNull(service.getOperator());
        assertNull(service.getRoute());
        assertFalse(service.isDisrupted());
        assertFalse(service.isDiverted());
    }

    /**
     * Test building a {@link LiveBusService} with valid values produces an object that returns
     * expected values.
     */
    @Test
    public void testValid() {
        final LiveBus bus = new LiveBus.Builder()
                .setDestination("A")
                .setDepartureTime(new Date())
                .build();
        final ArrayList<LiveBus> buses = new ArrayList<>();
        buses.add(bus);
        final LiveBusService service = new LiveBusService.Builder()
                .setServiceName("2")
                .setBuses(buses)
                .setOperator("LB")
                .setRoute("A -- B")
                .setIsDisrupted(true)
                .setIsDiverted(true)
                .build();

        assertEquals("2", service.getServiceName());
        assertEquals(1, service.getLiveBuses().size());
        assertEquals("A", service.getLiveBuses().get(0).getDestination());
        assertEquals("LB", service.getOperator());
        assertEquals("A -- B", service.getRoute());
        assertTrue(service.isDisrupted());
        assertTrue(service.isDiverted());
    }
    
    /**
     * Test that instances of {@link LiveBusService} order correctly when held inside a
     * {@link List}.
     */
    @Test
    public void testOrdering() {
        final LiveBusService serviceA = new LiveBusService.Builder()
                .setServiceName("25")
                .setBuses(Collections.emptyList())
                .build();
        final LiveBusService serviceB = new LiveBusService.Builder()
                .setServiceName("X25")
                .setBuses(Collections.emptyList())
                .build();

        assertEquals(0, serviceA.compareTo(serviceA));
        assertTrue(serviceA.compareTo(serviceB) < 0);
        assertTrue(serviceB.compareTo(serviceA) > 0);
    }
}