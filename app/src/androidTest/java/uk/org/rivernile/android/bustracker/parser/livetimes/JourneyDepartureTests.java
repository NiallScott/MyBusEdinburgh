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

import java.util.Date;

import org.junit.Test;

/**
 * Tests for {@link JourneyDeparture}.
 * 
 * @author Niall Scott
 */
public class JourneyDepartureTests {

    /**
     * Test that {@link JourneyDeparture.Builder#build()} throws an {@link IllegalArgumentException}
     * when the stop code is set to {@code null}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithNullStopCode() {
        new JourneyDeparture.Builder()
                .setStopCode(null)
                .setDepartureTime(new Date())
                .build();
    }

    /**
     * Test that {@link JourneyDeparture.Builder#build()} throws an {@link IllegalArgumentException}
     * when the stop code is set to empty.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithEmptyStopCode() {
        new JourneyDeparture.Builder()
                .setStopCode("")
                .setDepartureTime(new Date())
                .build();
    }

    /**
     * Test that {@link JourneyDeparture.Builder#build()} does not throw any exceptions when the
     * stop name is set as empty.
     */
    @Test
    public void testBuilderWithEmptyStopName() {
        new JourneyDeparture.Builder()
                .setStopCode("123456")
                .setStopName("")
                .setDepartureTime(new Date())
                .build();
    }

    /**
     * Test that {@link JourneyDeparture.Builder#build()} throws an {@link IllegalArgumentException}
     * when the departure time is set to {@code null}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithNullDepartureTime() {
        new JourneyDeparture.Builder()
                .setStopCode("123456")
                .setDepartureTime(null)
                .build();
    }

    /**
     * Test the default values of a {@link JourneyDeparture} object.
     */
    @Test
    public void testDefault() {
        final Date date = new Date();
        final JourneyDeparture departure = new JourneyDeparture.Builder()
                .setStopCode("123456")
                .setDepartureTime(date)
                .build();

        assertEquals("123456", departure.getStopCode());
        assertNull(departure.getStopName());
        assertEquals(date, departure.getDepartureTime());
        assertFalse(departure.isBusStopDisrupted());
        assertFalse(departure.isEstimatedTime());
        assertFalse(departure.isDelayed());
        assertFalse(departure.isDiverted());
        assertFalse(departure.isTerminus());
        assertFalse(departure.isPartRoute());
    }

    /**
     * Test building a {@link JourneyDeparture} with valid values produces an object that returns
     * expected values.
     */
    @Test
    public void testWithValidValues() {
        final Date date = new Date();
        final JourneyDeparture departure = new JourneyDeparture.Builder()
                .setStopCode("123456")
                .setStopName("Stop name")
                .setDepartureTime(date)
                .setIsBusStopDisrupted(true)
                .setIsEstimatedTime(true)
                .setIsDelayed(true)
                .setIsDiverted(true)
                .setIsTerminus(true)
                .setIsPartRoute(true)
                .build();

        assertEquals("123456", departure.getStopCode());
        assertEquals("Stop name", departure.getStopName());
        assertEquals(date, departure.getDepartureTime());
        assertTrue(departure.isBusStopDisrupted());
        assertTrue(departure.isEstimatedTime());
        assertTrue(departure.isDelayed());
        assertTrue(departure.isDiverted());
        assertTrue(departure.isTerminus());
        assertTrue(departure.isPartRoute());
    }
    
    /**
     * Test that ordering works correctly based on the value of the 'order' parameter.
     */
    @Test
    public void testComparator() {
        final JourneyDeparture departure1 = new JourneyDeparture.Builder()
                .setStopCode("123456")
                .setDepartureTime(new Date())
                .setOrder(1)
                .build();
        final JourneyDeparture departure2 = new JourneyDeparture.Builder()
                .setStopCode("654321")
                .setDepartureTime(new Date())
                .setOrder(2)
                .build();
        assertTrue(departure1.compareTo(departure2) < 0);
        assertTrue(departure2.compareTo(departure1) > 0);
        assertEquals(0, departure1.compareTo(departure1));
    }
}