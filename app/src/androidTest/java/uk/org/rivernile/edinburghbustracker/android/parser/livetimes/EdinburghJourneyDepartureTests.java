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

package uk.org.rivernile.edinburghbustracker.android.parser.livetimes;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

/**
 * Tests for {@link EdinburghJourneyDeparture}.
 * 
 * @author Niall Scott
 */
public class EdinburghJourneyDepartureTests {

    /**
     * Test the default state of the object when the bare minimum properties are supplied.
     */
    @Test
    public void testDefault() {
        final EdinburghJourneyDeparture departure =
                (EdinburghJourneyDeparture) new EdinburghJourneyDeparture.Builder()
                        .setStopCode("123456")
                        .setDepartureTime(new Date())
                        .build();

        assertEquals(0, departure.getDepartureMinutes());
    }

    /**
     * Test the state of the object when a departure time is supplied.
     */
    @Test
    public void testValid() {
        final EdinburghJourneyDeparture departure =
                (EdinburghJourneyDeparture) new EdinburghJourneyDeparture.Builder()
                        .setDepartureMinutes(5)
                        .setStopCode("123456")
                        .setDepartureTime(new Date())
                        .build();

        assertEquals(5, departure.getDepartureMinutes());
    }
}