/*
 * Copyright (C) 2016 - 2021 Niall 'Rivernile' Scott
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.test.mock.MockContentProvider;
import android.test.mock.MockContentResolver;
import android.test.mock.MockContext;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link SettingsDatabase}.
 */
public class SettingsDatabaseTests {

    private Context mockContext;
    private MockContentResolver mockContentResolver;

    @Before
    public void setUp() {
        mockContentResolver = new MockContentResolver();
        mockContext = new MockContext() {
            @Override
            public ContentResolver getContentResolver() {
                return mockContentResolver;
            }
        };
    }

    /**
     * Test that deleting a favourite stop passes the data through correctly to the
     * {@link android.content.ContentProvider}.
     */
    @Test
    public void testDeleteFavouriteStop() {
        mockContentResolver.addProvider(SettingsContract.AUTHORITY, new MockContentProvider() {
            @Override
            public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
                assertEquals(SettingsContract.Favourites.CONTENT_URI, uri);
                assertEquals(SettingsContract.Favourites.STOP_CODE + " = ?", selection);
                assertArrayEquals(new String[] { "123456" }, selectionArgs);

                return 1;
            }
        });

        SettingsDatabase.deleteFavouriteStop(mockContext, "123456");
    }
}
