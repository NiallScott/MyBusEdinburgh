/*
 * Copyright (C) 2015 - 2018 Niall 'Rivernile' Scott
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

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;

/**
 * Tests for {@link LiveBusTimesLoader}.
 *
 * @author Niall Scott
 */
public class LiveBusTimesLoaderTests {

    /**
     * Test that the constructor for {@link LiveBusTimesLoader} throws an
     * {@link IllegalArgumentException} when the {@link String} array of bus stop codes is empty.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyStopCodes() {
        new LiveBusTimesLoader(ApplicationProvider.getApplicationContext(), new String[] { }, 1);
    }

    /**
     * Test that the constructor for {@link LiveBusTimesLoader} throws an
     * {@link IllegalArgumentException} when the number of departures is less than 1.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithZeroDepartures() {
        new LiveBusTimesLoader(ApplicationProvider.getApplicationContext(),
                new String[] { "123456" }, 0);
    }

    /**
     * Test that the constructor for {@link LiveBusTimesLoader} does not throw any exceptions when
     * the correct data has been passed in.
     */
    @Test
    public void testConstructorSuccess() {
        new LiveBusTimesLoader(ApplicationProvider.getApplicationContext(),
                new String[] { "123456" }, 1);
    }
}
