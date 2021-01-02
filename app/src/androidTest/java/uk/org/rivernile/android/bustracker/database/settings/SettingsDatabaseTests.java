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
import static org.junit.Assert.assertTrue;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
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
     * Test that adding a favourite stop passes the data through correctly to the
     * {@link android.content.ContentProvider}.
     */
    @Test
    public void testAddFavouriteStop() {
        mockContentResolver.addProvider(SettingsContract.AUTHORITY, new MockContentProvider() {
            @Override
            public Uri insert(final Uri uri, final ContentValues values) {
                assertEquals(SettingsContract.Favourites.CONTENT_URI, uri);
                assertEquals(2, values.size());
                assertEquals("123456", values.getAsString(SettingsContract.Favourites.STOP_CODE));
                assertEquals("Name", values.getAsString(SettingsContract.Favourites.STOP_NAME));

                return ContentUris.withAppendedId(uri, 1);
            }
        });

        SettingsDatabase.addFavouriteStop(mockContext, "123456", "Name");
    }

    /**
     * Test that updating a favourite stop passes the data through correctly to the
     * {@link android.content.ContentProvider}.
     */
    @Test
    public void testUpdateFavouriteStop() {
        mockContentResolver.addProvider(SettingsContract.AUTHORITY, new MockContentProvider() {
            @Override
            public int update(final Uri uri, final ContentValues values, final String selection,
                    final String[] selectionArgs) {
                assertEquals(ContentUris.withAppendedId(SettingsContract.Favourites.CONTENT_URI, 1),
                        uri);
                assertEquals(1, values.size());
                assertEquals("New name", values.getAsString(SettingsContract.Favourites.STOP_NAME));

                return 1;
            }
        });

        SettingsDatabase.updateFavouriteStop(mockContext, 1, "New name");
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

    /**
     * Test that adding a time alert passes the data through correctly to the
     * {@link android.content.ContentProvider}.
     */
    @Test
    public void testAddTimeAlert() {
        mockContentResolver.addProvider(SettingsContract.AUTHORITY, new MockContentProvider() {
            @Override
            public Uri insert(final Uri uri, final ContentValues values) {
                assertEquals(SettingsContract.Alerts.CONTENT_URI, uri);
                assertEquals(5, values.size());
                assertEquals(SettingsContract.Alerts.ALERTS_TYPE_TIME,
                        (int) values.getAsInteger(SettingsContract.Alerts.TYPE));
                assertTrue(values.getAsLong(SettingsContract.Alerts.TIME_ADDED) > 0);
                assertEquals("123456", values.getAsString(SettingsContract.Alerts.STOP_CODE));
                assertEquals("1,2,3", values.getAsString(SettingsContract.Alerts.SERVICE_NAMES));
                assertEquals(2, (int) values.getAsInteger(SettingsContract.Alerts.TIME_TRIGGER));

                return ContentUris.withAppendedId(uri, 1);
            }
        });

        SettingsDatabase.addTimeAlert(mockContext, "123456", new String[] { "1", "2", "3" }, 2);
    }

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
