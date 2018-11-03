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

import java.util.Collections;

import org.junit.Test;

/**
 * Tests for {@link Journey}.
 * 
 * @author Niall Scott
 */
public class JourneyTests {

    /**
     * Test that {@link Journey.Builder#build()} throws an {@link IllegalArgumentException} when the
     * journey ID is set to {@code null}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithNullJourneyId() {
        new Journey.Builder()
                .setJourneyId(null)
                .setServiceName("1")
                .setDepartures(Collections.emptyList())
                .setTerminus("123456")
                .build();
    }

    /**
     * Test that {@link Journey.Builder#build()} throws an {@link IllegalArgumentException} when the
     * journey ID is set to empty.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithEmptyJourneyId() {
        new Journey.Builder()
                .setJourneyId("")
                .setServiceName("1")
                .setDepartures(Collections.emptyList())
                .setTerminus("123456")
                .build();
    }

    /**
     * Test that {@link Journey.Builder#build()} throws an {@link IllegalArgumentException} when the
     * service name is set to {@code null}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithNullServiceName() {
        new Journey.Builder()
                .setJourneyId("abc123")
                .setServiceName(null)
                .setDepartures(Collections.emptyList())
                .setTerminus("123456")
                .build();
    }

    /**
     * Test that {@link Journey.Builder#build()} throws an {@link IllegalArgumentException} when the
     * service name is set to empty.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithEmptyServiceName() {
        new Journey.Builder()
                .setJourneyId("abc123")
                .setServiceName("")
                .setDepartures(Collections.emptyList())
                .setTerminus("123456")
                .build();
    }

    /**
     * Test that {@link Journey.Builder#build()} throws an {@link IllegalArgumentException} when the
     * departures is set to {@code null}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithNullDepartures() {
        new Journey.Builder()
                .setJourneyId("abc123")
                .setServiceName("1")
                .setDepartures(null)
                .setTerminus("123456")
                .build();
    }

    /**
     * Test that {@link Journey.Builder#build()} does not throw any exceptions when the operator is
     * set as empty.
     */
    @Test
    public void testBuilderWithEmptyOperator() {
        new Journey.Builder()
                .setJourneyId("abc123")
                .setServiceName("1")
                .setDepartures(Collections.emptyList())
                .setOperator("")
                .setTerminus("123456")
                .build();
    }

    /**
     * Test that {@link Journey.Builder#build()} does not throw any exceptions when the route is
     * set as empty.
     */
    @Test
    public void testBuilderWithEmptyRoute() {
        new Journey.Builder()
                .setJourneyId("abc123")
                .setServiceName("1")
                .setDepartures(Collections.emptyList())
                .setRoute("")
                .setTerminus("123456")
                .build();
    }

    /**
     * Test that {@link Journey.Builder#build()} does not throw any exceptions when the
     * destination is set as empty.
     */
    @Test
    public void testBuilderWithEmptyDestination() {
        new Journey.Builder()
                .setJourneyId("abc123")
                .setServiceName("1")
                .setDepartures(Collections.emptyList())
                .setDestination("")
                .setTerminus("123456")
                .build();
    }

    /**
     * Test that {@link Journey.Builder#build()} throws an {@link IllegalArgumentException} when the
     * terminus is set to {@code null}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithNullTerminus() {
        new Journey.Builder()
                .setJourneyId("abc123")
                .setServiceName("1")
                .setDepartures(Collections.emptyList())
                .setTerminus(null)
                .build();
    }

    /**
     * Test that {@link Journey.Builder#build()} throws an {@link IllegalArgumentException} when the
     * terminus is set to empty.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithEmptyTerminus() {
        new Journey.Builder()
                .setJourneyId("abc123")
                .setServiceName("1")
                .setDepartures(Collections.emptyList())
                .setTerminus("")
                .build();
    }

    /**
     * Test the default values of a {@link Journey} object.
     */
    @Test
    public void testDefault() {
        final Journey journey = new Journey.Builder()
                .setJourneyId("abc123")
                .setServiceName("1")
                .setDepartures(Collections.emptyList())
                .setTerminus("123456")
                .build();

        assertEquals("abc123", journey.getJourneyId());
        assertEquals("1", journey.getServiceName());
        assertTrue(journey.getDepartures().isEmpty());
        assertNull(journey.getOperator());
        assertNull(journey.getRoute());
        assertNull(journey.getDestination());
        assertEquals("123456", journey.getTerminus());
        assertFalse(journey.hasGlobalDisruption());
        assertFalse(journey.hasServiceDisruption());
        assertFalse(journey.hasServiceDiversion());
        assertEquals(0L, journey.getReceiveTime());
    }

    /**
     * Test building a {@link Journey} with valid values produces an object that returns expected
     * values.
     */
    @Test
    public void testValid() {
        final Journey journey = new Journey.Builder()
                .setJourneyId("abc123")
                .setServiceName("1")
                .setDepartures(Collections.emptyList())
                .setOperator("LB")
                .setRoute("A -- B")
                .setDestination("End")
                .setTerminus("123456")
                .setHasGlobalDisruption(true)
                .setHasServiceDisruption(true)
                .setHasServiceDiversion(true)
                .setReceiveTime(123456789L)
                .build();

        assertEquals("abc123", journey.getJourneyId());
        assertEquals("1", journey.getServiceName());
        assertTrue(journey.getDepartures().isEmpty());
        assertEquals("LB", journey.getOperator());
        assertEquals("A -- B", journey.getRoute());
        assertEquals("End", journey.getDestination());
        assertEquals("123456", journey.getTerminus());
        assertTrue(journey.hasGlobalDisruption());
        assertTrue(journey.hasServiceDisruption());
        assertTrue(journey.hasServiceDiversion());
        assertEquals(123456789L, journey.getReceiveTime());
    }
}