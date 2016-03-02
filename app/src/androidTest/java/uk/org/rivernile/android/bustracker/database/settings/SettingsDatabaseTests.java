/*
 * Copyright (C) 2016 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.database.settings;

import static org.junit.Assert.assertEquals;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link SettingsDatabase}.
 */
@RunWith(AndroidJUnit4.class)
public class SettingsDatabaseTests {

    /**
     * Test that sending an empty {@link String} array in to
     * {@link SettingsDatabase#packServices(String[])} returns an empty {@link String}.
     */
    @Test
    public void testPackServicesWithEmptyArray() {
        assertEquals("", SettingsDatabase.packServices(new String[] { }));
    }

    /**
     * Test that sending a single item in to {@link SettingsDatabase#packServices(String[])} returns
     * a correctly formatted {@link String}.
     */
    @Test
    public void testPackServicesWithSingleItem() {
        assertEquals("1", SettingsDatabase.packServices(new String[] { "1" }));
    }

    /**
     * Test that sending multiple items in to {@link SettingsDatabase#packServices(String[])}
     * returns a correctly formatted {@link String}.
     */
    @Test
    public void testPackServicesWithMultipleItems() {
        assertEquals("1,2,3", SettingsDatabase.packServices(new String[] { "1", "2", "3" }));
    }
}
