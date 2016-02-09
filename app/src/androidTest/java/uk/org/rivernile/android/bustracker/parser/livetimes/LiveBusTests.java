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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link LiveBus}.
 * 
 * @author Niall Scott
 */
@RunWith(AndroidJUnit4.class)
public class LiveBusTests {

    /**
     * Test that {@link LiveBus.Builder#build()} throws an {@link IllegalArgumentException} when the
     * destination is set to {@code null}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithNullDestination() {
        new LiveBus.Builder()
                .setDestination(null)
                .setDepartureTime(new Date())
                .setTerminus("123456")
                .setJourneyId("abc123")
                .build();
    }

    /**
     * Test that {@link LiveBus.Builder#build()} throws an {@link IllegalArgumentException} when the
     * destination is set to empty.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithEmptyDestination() {
        new LiveBus.Builder()
                .setDestination("")
                .setDepartureTime(new Date())
                .setTerminus("123456")
                .setJourneyId("abc123")
                .build();
    }

    /**
     * Test that {@link LiveBus.Builder#build()} throws an {@link IllegalArgumentException} when the
     * departure time is set to {@code null}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithNullDepartureTime() {
        new LiveBus.Builder()
                .setDestination("Destination")
                .setDepartureTime(null)
                .setTerminus("123456")
                .setJourneyId("abc123")
                .build();
    }

    /**
     * Test that {@link LiveBus.Builder#build()} does not throw any exceptions when the terminus is
     * set as {@code null}.
     */
    @Test
    public void testBuilderWithNullTerminus() {
        new LiveBus.Builder()
                .setDestination("Destination")
                .setDepartureTime(new Date())
                .setTerminus(null)
                .setJourneyId("abc123")
                .build();
    }

    /**
     * Test that {@link LiveBus.Builder#build()} does not throw any exceptions when the terminus is
     * set as empty.
     */
    @Test
    public void testBuilderWithEmptyTerminus() {
        new LiveBus.Builder()
                .setDestination("Destination")
                .setDepartureTime(new Date())
                .setTerminus("")
                .setJourneyId("abc123")
                .build();
    }

    /**
     * Test that {@link LiveBus.Builder#build()} does not throw any exceptions when the journey
     * ID is set as {@code null}.
     */
    @Test
    public void testBuilderWithNullJourneyId() {
        new LiveBus.Builder()
                .setDestination("Destination")
                .setDepartureTime(new Date())
                .setTerminus("123456")
                .setJourneyId(null)
                .build();
    }

    /**
     * Test that {@link LiveBus.Builder#build()} does not throw any exceptions when the journey
     * ID is set as empty.
     */
    @Test
    public void testBuilderWithEmptyJourneyId() {
        new LiveBus.Builder()
                .setDestination("Destination")
                .setDepartureTime(new Date())
                .setTerminus("123456")
                .setJourneyId("")
                .build();
    }

    /**
     * Test the default values of a {@link LiveBus} object.
     */
    @Test
    public void testDefault() {
        final Date date = new Date();
        final LiveBus bus = new LiveBus.Builder()
                .setDestination("Destination")
                .setDepartureTime(date)
                .build();

        assertEquals("Destination", bus.getDestination());
        assertEquals(date, bus.getDepartureTime());
        assertNull(bus.getTerminus());
        assertNull(bus.getJourneyId());
        assertFalse(bus.isEstimatedTime());
        assertFalse(bus.isDelayed());
        assertFalse(bus.isDiverted());
        assertFalse(bus.isTerminus());
        assertFalse(bus.isPartRoute());
    }

    /**
     * Test building a {@link LiveBus} with valid values produces an object that returns expected
     * values.
     */
    @Test
    public void testWithValidValues() {
        final Date date = new Date();
        final LiveBus bus = new LiveBus.Builder()
                .setDestination("Destination")
                .setDepartureTime(date)
                .setTerminus("123456")
                .setJourneyId("abc123")
                .setIsEstimatedTime(true)
                .setIsDelayed(true)
                .setIsDiverted(true)
                .setIsTerminus(true)
                .setIsPartRoute(true)
                .build();

        assertEquals("Destination", bus.getDestination());
        assertEquals(date, bus.getDepartureTime());
        assertEquals("123456", bus.getTerminus());
        assertEquals("abc123", bus.getJourneyId());
        assertTrue(bus.isEstimatedTime());
        assertTrue(bus.isDelayed());
        assertTrue(bus.isDiverted());
        assertTrue(bus.isTerminus());
        assertTrue(bus.isPartRoute());
    }
    
    /**
     * Test that the comparator compares correctly.
     */
    @Test
    public void testComparator() {
        final Calendar cal1 = new GregorianCalendar(2014, Calendar.FEBRUARY, 1);
        final Calendar cal2 = new GregorianCalendar(2014, Calendar.MARCH, 1);

        final LiveBus bus1 = new LiveBus.Builder()
                .setDestination("A")
                .setDepartureTime(cal1.getTime())
                .build();
        final LiveBus bus2 = new LiveBus.Builder()
                .setDestination("B")
                .setDepartureTime(cal2.getTime())
                .build();
        final LiveBus bus3 = new LiveBus.Builder()
                .setDestination("C")
                .setDepartureTime(cal1.getTime())
                .build();

        assertTrue(bus1.compareTo(bus2) < 0);
        assertTrue(bus2.compareTo(bus1) > 0);
        assertEquals(0, bus1.compareTo(bus3));
    }
}