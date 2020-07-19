/*
 * Copyright (C) 2015 - 2020 Niall 'Rivernile' Scott
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

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import androidx.test.rule.provider.ProviderTestRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link SettingsProvider}.
 *
 * @author Niall Scott
 */
public class SettingsProviderTests {

    private static final String STATEMENT_CREATE_FAVOURITES_TABLE =
            "CREATE TABLE IF NOT EXISTS " + SettingsContract.Favourites.TABLE_NAME + " (" +
                SettingsContract.Favourites._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SettingsContract.Favourites.STOP_CODE + " TEXT NOT NULL UNIQUE," +
                SettingsContract.Favourites.STOP_NAME + " TEXT NOT NULL)";
    private static final String STATEMENT_CREATE_ALERTS_TABLE =
            "CREATE TABLE IF NOT EXISTS " + SettingsContract.Alerts.TABLE_NAME + " (" +
                SettingsContract.Alerts._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SettingsContract.Alerts.TYPE + " NUMERIC NOT NULL," +
                SettingsContract.Alerts.TIME_ADDED + " INTEGER NOT NULL," +
                SettingsContract.Alerts.STOP_CODE + " TEXT NOT NULL," +
                SettingsContract.Alerts.DISTANCE_FROM + " INTEGER," +
                SettingsContract.Alerts.SERVICE_NAMES + " TEXT," +
                SettingsContract.Alerts.TIME_TRIGGER + " INTEGER)";

    @Rule
    public ProviderTestRule providerRule =
            new ProviderTestRule.Builder(SettingsProvider.class, SettingsContract.AUTHORITY)
                    .setDatabaseCommands(SettingsContract.DB_NAME,
                            STATEMENT_CREATE_FAVOURITES_TABLE,
                            STATEMENT_CREATE_ALERTS_TABLE)
                    .build();

    private long currentTime;

    @Before
    public void setUp() {
        currentTime = System.currentTimeMillis();
    }

    /**
     * Test that {@link SettingsProvider#getType(Uri)} returns the correct MIME types for the
     * supplied content {@link Uri}s.
     */
    @Test
    public void testGetTypeSuccess() {
        final ContentResolver resolver = providerRule.getResolver();

        assertEquals(SettingsContract.Favourites.CONTENT_TYPE,
                resolver.getType(SettingsContract.Favourites.CONTENT_URI));
        assertEquals(SettingsContract.Favourites.CONTENT_ITEM_TYPE,
                resolver.getType(
                        ContentUris.withAppendedId(SettingsContract.Favourites.CONTENT_URI, 1)));
        assertEquals(SettingsContract.Alerts.CONTENT_TYPE,
                resolver.getType(SettingsContract.Alerts.CONTENT_URI));
        assertEquals(SettingsContract.Alerts.CONTENT_ITEM_TYPE,
                resolver.getType(
                        ContentUris.withAppendedId(SettingsContract.Alerts.CONTENT_URI, 1)));
    }

    /**
     * Test that {@link SettingsProvider#getType(Uri)} returns {@code null} when invalid
     * {@link Uri}s are supplied.
     */
    @Test
    public void testGetTypeWithInvalidUris() {
        final ContentResolver resolver = providerRule.getResolver();

        assertNull(resolver.getType(Uri.parse("content://invalid.uri/thing")));
        assertNull(resolver.getType(Uri.parse("content://" + SettingsContract.AUTHORITY)));
        assertNull(resolver
                .getType(Uri.parse("content://" + SettingsContract.AUTHORITY + "/invalid")));
    }

    /**
     * Test that {@link SettingsProvider#query(Uri, String[], String, String[], String)} throws an
     * {@link IllegalArgumentException} when an invalid {@link Uri} has been supplied.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testQueryWithInvalidUri() {
        providerRule.getResolver()
                .query(Uri.parse("content://" + SettingsContract.AUTHORITY + "/invalid"), null,
                        null, null, null);
    }

    /**
     * Test that {@link SettingsProvider#insert(Uri, ContentValues)} throws an
     * {@link IllegalArgumentException} when an invalid {@link Uri} has been supplied.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInsertWithInvalidUri() {
        final ContentValues cv = new ContentValues();
        cv.put("a", "1");
        cv.put("b", "2");
        providerRule.getResolver()
                .insert(Uri.parse("content://" + SettingsContract.AUTHORITY + "/invalid"), cv);
    }

    /**
     * Test that {@link SettingsProvider#bulkInsert(Uri, ContentValues[])} throws an
     * {@link IllegalArgumentException} when an invalid {@link Uri} has been supplied.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBulkInsertWithInvalidUri() {
        final ContentValues cv = new ContentValues();
        cv.put("a", "1");
        cv.put("b", "2");
        final ContentValues[] cvArray = new ContentValues[] { cv };
        providerRule.getResolver()
                .bulkInsert(Uri.parse("content://" + SettingsContract.AUTHORITY + "/invalid"),
                        cvArray);
    }

    /**
     * Test that {@link SettingsProvider#insert(Uri, ContentValues)} throws an
     * {@link IllegalArgumentException} when a favourites {@link Uri} has been specified that
     * includes a specific item ID.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInsertWithFavouritesId() {
        final ContentValues cv = new ContentValues();
        cv.put(SettingsContract.Favourites.STOP_CODE, "123456");
        cv.put(SettingsContract.Favourites.STOP_NAME, "Name");
        providerRule.getResolver().insert(
                ContentUris.withAppendedId(SettingsContract.Favourites.CONTENT_URI, 1), cv);
    }

    /**
     * Test that {@link SettingsProvider#bulkInsert(Uri, ContentValues[])} throws an
     * {@link IllegalArgumentException} when a favourites {@link Uri} has been specified that
     * includes a specific item ID.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBulkInsertWithFavouritesId() {
        final ContentValues cv = new ContentValues();
        cv.put(SettingsContract.Favourites.STOP_CODE, "123456");
        cv.put(SettingsContract.Favourites.STOP_NAME, "Name");
        final ContentValues[] cvArray = new ContentValues[] { cv };
        providerRule.getResolver().bulkInsert(
                ContentUris.withAppendedId(SettingsContract.Favourites.CONTENT_URI, 1), cvArray);
    }

    /**
     * Test that {@link SettingsProvider#insert(Uri, ContentValues)} throws an
     * {@link IllegalArgumentException} when an alerts {@link Uri} has been specified that
     * includes a specific item ID.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInsertWithAlertsId() {
        final ContentValues cv = new ContentValues();
        cv.put(SettingsContract.Alerts.TYPE, SettingsContract.Alerts.ALERTS_TYPE_PROXIMITY);
        cv.put(SettingsContract.Alerts.TIME_ADDED, 1234567890L);
        cv.put(SettingsContract.Alerts.STOP_CODE, "123456");
        cv.put(SettingsContract.Alerts.DISTANCE_FROM, 1);
        providerRule.getResolver().insert(
                ContentUris.withAppendedId(SettingsContract.Alerts.CONTENT_URI, 1), cv);
    }

    /**
     * Test that {@link SettingsProvider#bulkInsert(Uri, ContentValues[])} throws an
     * {@link IllegalArgumentException} when an alerts {@link Uri} has been specified that
     * includes a specific item ID.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBulkInsertWithAlertsId() {
        final ContentValues cv = new ContentValues();
        cv.put(SettingsContract.Alerts.TYPE, SettingsContract.Alerts.ALERTS_TYPE_PROXIMITY);
        cv.put(SettingsContract.Alerts.TIME_ADDED, 1234567890L);
        cv.put(SettingsContract.Alerts.STOP_CODE, "123456");
        cv.put(SettingsContract.Alerts.DISTANCE_FROM, 1);
        final ContentValues[] cvArray = new ContentValues[] { cv };
        providerRule.getResolver().bulkInsert(
                ContentUris.withAppendedId(SettingsContract.Alerts.CONTENT_URI, 1), cvArray);
    }

    /**
     * Test that {@link SettingsProvider#delete(Uri, String, String[])} throws an
     * {@link IllegalArgumentException} when an invalid {@link Uri} has been supplied.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDeleteWithInvalidUri() {
        providerRule.getResolver()
                .delete(Uri.parse("content://" + SettingsContract.AUTHORITY + "/invalid"), null,
                        null);
    }

    /**
     * Test that {@link SettingsProvider#update(Uri, ContentValues, String, String[])} throws an
     * {@link IllegalArgumentException} when an invalid {@link Uri} has been supplied.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateWithInvalidUri() {
        final ContentValues cv = new ContentValues();
        cv.put("a", "1");
        cv.put("b", "2");
        providerRule.getResolver()
                .update(Uri.parse("content://" + SettingsContract.AUTHORITY + "/invalid"), cv,
                        null, null);
    }

    /**
     * Test that {@link SettingsProvider#update(Uri, ContentValues, String, String[])} throws an
     * {@link IllegalArgumentException} when an alerts {@link Uri} has been specified.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateWithAlerts() {
        final ContentValues cv = new ContentValues();
        cv.put(SettingsContract.Alerts.STOP_CODE, "24680");
        providerRule.getResolver()
                .update(SettingsContract.Alerts.CONTENT_URI, cv, null, null);
    }

    /**
     * Test that {@link SettingsProvider#update(Uri, ContentValues, String, String[])} throws an
     * {@link IllegalArgumentException} when an alerts {@link Uri} has been specified that includes
     * a specific item ID.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateWithAlertsUri() {
        final ContentValues cv = new ContentValues();
        cv.put(SettingsContract.Alerts.STOP_CODE, "24680");
        providerRule.getResolver()
                .update(ContentUris.withAppendedId(SettingsContract.Alerts.CONTENT_URI, 1), cv,
                        null, null);
    }

    /**
     * Test that {@link SettingsProvider#query(Uri, String[], String, String[], String)} returns a
     * {@link Cursor} with {@code 0} rows when no data has been inserted in to the database with
     * favourites.
     */
    @Test
    public void testQueryFavouritesWithEmptyTable() {
        final Cursor c = providerRule.getResolver().query(SettingsContract.Favourites.CONTENT_URI,
                null, null, null, null);
        assertNotNull(c);
        assertEquals(0, c.getCount());
        assertNotificationUri(SettingsContract.Favourites.CONTENT_URI, c);
        c.close();
    }

    /**
     * Test that {@link SettingsProvider#query(Uri, String[], String, String[], String)} returns
     * a {@link Cursor} with {@code 0} rows when no data has been inserted in to the database with
     * alerts.
     */
    @Test
    public void testQueryAlertsWithEmptyTable() {
        final Cursor c = providerRule.getResolver().query(SettingsContract.Alerts.CONTENT_URI, null,
                null, null, null);
        assertNotNull(c);
        assertEquals(0, c.getCount());
        assertNotificationUri(SettingsContract.Alerts.CONTENT_URI, c);
        c.close();
    }

    /**
     * Test various permutations of
     * {@link SettingsProvider#query(Uri, String[], String, String[], String)} with favourites.
     */
    @Test
    public void testQueryFavourites() {
        populateFavouritesWithTestData();
        final ContentResolver resolver = providerRule.getResolver();

        Cursor c = resolver.query(SettingsContract.Favourites.CONTENT_URI, null, null, null, null);
        assertNotNull(c);
        assertEquals(3, c.getCount());

        c.moveToNext();
        assertEquals("A", c.getString(2));
        c.moveToNext();
        assertEquals("B", c.getString(2));
        c.moveToNext();
        assertEquals("C", c.getString(2));
        assertNotificationUri(SettingsContract.Favourites.CONTENT_URI, c);
        c.close();

        c = resolver.query(SettingsContract.Favourites.CONTENT_URI, null, null, null,
                SettingsContract.Favourites.STOP_NAME + " DESC");
        assertNotNull(c);
        assertEquals(3, c.getCount());

        c.moveToNext();
        assertEquals("C", c.getString(2));
        c.moveToNext();
        assertEquals("B", c.getString(2));
        c.moveToNext();
        assertEquals("A", c.getString(2));
        assertNotificationUri(SettingsContract.Favourites.CONTENT_URI, c);
        c.close();

        c = resolver.query(SettingsContract.Favourites.CONTENT_URI,
                new String[] {
                        SettingsContract.Favourites.STOP_NAME,
                        SettingsContract.Favourites._COUNT
                },
                SettingsContract.Favourites.STOP_CODE + " = ?",
                new String[] { "323456" }, null);
        assertNotNull(c);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals("C", c.getString(0));
        assertEquals(1, c.getInt(1));
        assertNotificationUri(SettingsContract.Favourites.CONTENT_URI, c);
        c.close();
    }

    /**
     * Test various permutations of
     * {@link SettingsProvider#query(Uri, String[], String, String[], String)} with alerts.
     */
    @Test
    public void testQueryAlerts() {
        populateAlertsWithTestData();
        final ContentResolver resolver = providerRule.getResolver();

        Cursor c = resolver.query(SettingsContract.Alerts.CONTENT_URI, null, null, null, null);
        assertNotNull(c);
        assertEquals(2, c.getCount());

        c.moveToNext();
        assertEquals("123456", c.getString(3));
        c.moveToNext();
        assertEquals("223456", c.getString(3));
        assertNotificationUri(SettingsContract.Alerts.CONTENT_URI, c);
        c.close();

        c = resolver.query(SettingsContract.Alerts.CONTENT_URI,
                new String[] {
                        SettingsContract.Alerts.STOP_CODE,
                        SettingsContract.Alerts._COUNT
                },
                SettingsContract.Alerts.TYPE + " = ?",
                new String[] { String.valueOf(SettingsContract.Alerts.ALERTS_TYPE_PROXIMITY) },
                null);
        assertNotNull(c);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals("123456", c.getString(0));
        assertEquals(1, c.getInt(1));
        assertNotificationUri(SettingsContract.Alerts.CONTENT_URI, c);
        c.close();
    }

    /**
     * Test {@link SettingsProvider#query(Uri, String[], String, String[], String)} with favourites
     * for a known item ID.
     */
    @Test
    public void testQueryFavouritesById() {
        populateFavouritesWithTestData();
        final ContentResolver resolver = providerRule.getResolver();

        Cursor c = resolver.query(
                ContentUris.withAppendedId(SettingsContract.Favourites.CONTENT_URI, 1), null,
                null, null, null);
        assertNotNull(c);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals("B", c.getString(2));
        c.close();

        c = resolver.query(
                ContentUris.withAppendedId(SettingsContract.Favourites.CONTENT_URI, 42), null,
                null, null, null);
        assertNotNull(c);
        assertEquals(0, c.getCount());
        c.close();

        c = resolver.query(
                ContentUris.withAppendedId(SettingsContract.Favourites.CONTENT_URI, 1),
                new String[] {
                        SettingsContract.Favourites.STOP_NAME,
                        SettingsContract.Favourites._COUNT
                },
                SettingsContract.Favourites.STOP_CODE + " = ?",
                new String[] { "323456" }, null);
        assertNotNull(c);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals("B", c.getString(0));
        assertEquals(1, c.getInt(1));
        c.close();
    }

    /**
     * Test {@link SettingsProvider#query(Uri, String[], String, String[], String)} with alerts for
     * a known item ID.
     */
    @Test
    public void testQueryAlertsById() {
        populateAlertsWithTestData();
        final ContentResolver resolver = providerRule.getResolver();

        Cursor c = resolver.query(
                ContentUris.withAppendedId(SettingsContract.Alerts.CONTENT_URI, 1), null, null,
                null, null);
        assertNotNull(c);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals("123456", c.getString(3));
        c.close();

        c = resolver.query(
                ContentUris.withAppendedId(SettingsContract.Alerts.CONTENT_URI, 42), null, null,
                null, null);
        assertNotNull(c);
        assertEquals(0, c.getCount());
        c.close();

        c = resolver.query(
                ContentUris.withAppendedId(SettingsContract.Alerts.CONTENT_URI, 1),
                new String[] {
                        SettingsContract.Alerts.STOP_CODE,
                        SettingsContract.Alerts._COUNT
                },
                SettingsContract.Alerts.TYPE + " = ?",
                new String[] { String.valueOf(SettingsContract.Alerts.ALERTS_TYPE_TIME) }, null);
        assertNotNull(c);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals("123456", c.getString(0));
        assertEquals(1, c.getInt(1));
        c.close();
    }

    /**
     * Test {@link SettingsProvider#insert(Uri, ContentValues)} with favourites.
     */
    @Test
    public void testInsertFavourites() {
        final ContentResolver resolver = providerRule.getResolver();
        Cursor c = resolver.query(SettingsContract.Favourites.CONTENT_URI, null, null, null, null);
        assertNotNull(c);
        assertEquals(0, c.getCount());
        c.close();

        final ContentValues cv = new ContentValues();
        cv.put(SettingsContract.Favourites.STOP_CODE, "24680");
        cv.put(SettingsContract.Favourites.STOP_NAME, "Test stop");
        final Uri returnedUri = resolver
                .insert(SettingsContract.Favourites.CONTENT_URI, cv);
        assertNotNull(returnedUri);
        assertEquals(ContentUris.withAppendedId(SettingsContract.Favourites.CONTENT_URI, 1),
                returnedUri);

        c = resolver.query(returnedUri, null, null, null, null);
        assertNotNull(c);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals(1, c.getInt(0));
        assertEquals("24680", c.getString(1));
        assertEquals("Test stop", c.getString(2));
        c.close();
    }

    /**
     * Test {@link SettingsProvider#bulkInsert(Uri, ContentValues[])} with favourites.
     */
    @Test
    public void testBulkInsertFavourites() {
        final ContentResolver resolver = providerRule.getResolver();
        Cursor c = resolver.query(SettingsContract.Favourites.CONTENT_URI, null, null, null, null);
        assertNotNull(c);
        assertEquals(0, c.getCount());
        c.close();

        final ContentValues cv1 = new ContentValues();
        cv1.put(SettingsContract.Favourites.STOP_CODE, "100001");
        cv1.put(SettingsContract.Favourites.STOP_NAME, "Test stop 1");
        final ContentValues cv2 = new ContentValues();
        cv2.put(SettingsContract.Favourites.STOP_CODE, "100002");
        cv2.put(SettingsContract.Favourites.STOP_NAME, "Test stop 2");
        final ContentValues[] cvArray = new ContentValues[] { cv1, cv2 };
        final int numberInserted = resolver
                .bulkInsert(SettingsContract.Favourites.CONTENT_URI, cvArray);
        assertEquals(2, numberInserted);

        c = resolver.query(
                SettingsContract.Favourites.CONTENT_URI,
                new String[] {
                        SettingsContract.Favourites.STOP_CODE,
                        SettingsContract.Favourites.STOP_NAME
                },
                null,
                null,
                null);
        assertNotNull(c);
        assertEquals(2, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals("100001", c.getString(0));
        assertEquals("Test stop 1", c.getString(1));
        assertTrue(c.moveToNext());
        assertEquals("100002", c.getString(0));
        assertEquals("Test stop 2", c.getString(1));
        c.close();
    }

    /**
     * Test {@link SettingsProvider#insert(Uri, ContentValues)} with alerts.
     */
    @Test
    public void testInsertAlerts() {
        final ContentResolver resolver = providerRule.getResolver();
        Cursor c = resolver.query(SettingsContract.Alerts.CONTENT_URI, null, null, null, null);
        assertNotNull(c);
        assertEquals(0, c.getCount());
        c.close();

        final ContentValues cv = new ContentValues();
        cv.put(SettingsContract.Alerts.TYPE, SettingsContract.Alerts.ALERTS_TYPE_PROXIMITY);
        cv.put(SettingsContract.Alerts.TIME_ADDED, currentTime);
        cv.put(SettingsContract.Alerts.STOP_CODE, "123456");
        cv.put(SettingsContract.Alerts.DISTANCE_FROM, 10);
        final Uri returnedUri = resolver.insert(SettingsContract.Alerts.CONTENT_URI, cv);
        assertNotNull(returnedUri);
        assertEquals(ContentUris.withAppendedId(SettingsContract.Alerts.CONTENT_URI, 1),
                returnedUri);

        c = resolver.query(returnedUri, null, null, null, null);
        assertNotNull(c);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals(1, c.getInt(0));
        assertEquals(SettingsContract.Alerts.ALERTS_TYPE_PROXIMITY, c.getInt(1));
        assertEquals(currentTime, c.getLong(2));
        assertEquals("123456", c.getString(3));
        assertEquals(10, c.getInt(4));
        assertTrue(c.isNull(5));
        assertTrue(c.isNull(6));
        c.close();
    }

    /**
     * Test {@link SettingsProvider#bulkInsert(Uri, ContentValues[])} with alerts.
     */
    @Test
    public void testBulkInsertAlerts() {
        final ContentResolver resolver = providerRule.getResolver();
        Cursor c = resolver.query(SettingsContract.Alerts.CONTENT_URI, null, null, null, null);
        assertNotNull(c);
        assertEquals(0, c.getCount());
        c.close();

        final ContentValues cv1 = new ContentValues();
        cv1.put(SettingsContract.Alerts.TYPE, SettingsContract.Alerts.ALERTS_TYPE_PROXIMITY);
        cv1.put(SettingsContract.Alerts.TIME_ADDED, currentTime);
        cv1.put(SettingsContract.Alerts.STOP_CODE, "123456");
        cv1.put(SettingsContract.Alerts.DISTANCE_FROM, 10);
        final ContentValues cv2 = new ContentValues();
        cv2.put(SettingsContract.Alerts.TYPE, SettingsContract.Alerts.ALERTS_TYPE_TIME);
        cv2.put(SettingsContract.Alerts.TIME_ADDED, currentTime);
        cv2.put(SettingsContract.Alerts.STOP_CODE, "123456");
        cv2.put(SettingsContract.Alerts.SERVICE_NAMES, "1,2,3");
        cv2.put(SettingsContract.Alerts.TIME_TRIGGER, 5);
        final ContentValues[] cvArray = new ContentValues[] { cv1, cv2 };
        final int numberInserted = resolver.bulkInsert(SettingsContract.Alerts.CONTENT_URI,
                cvArray);
        assertEquals(2, numberInserted);

        c = resolver.query(
                SettingsContract.Alerts.CONTENT_URI,
                new String[] {
                        SettingsContract.Alerts.TYPE,
                        SettingsContract.Alerts.TIME_ADDED,
                        SettingsContract.Alerts.STOP_CODE,
                        SettingsContract.Alerts.DISTANCE_FROM,
                        SettingsContract.Alerts.SERVICE_NAMES,
                        SettingsContract.Alerts.TIME_TRIGGER
                },
                null,
                null,
                null);
        assertNotNull(c);
        assertEquals(2, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals(SettingsContract.Alerts.ALERTS_TYPE_PROXIMITY, c.getInt(0));
        assertEquals(currentTime, c.getLong(1));
        assertEquals("123456", c.getString(2));
        assertEquals(10, c.getInt(3));
        assertTrue(c.isNull(4));
        assertTrue(c.isNull(5));
        assertTrue(c.moveToNext());
        assertEquals(SettingsContract.Alerts.ALERTS_TYPE_TIME, c.getInt(0));
        assertEquals(currentTime, c.getLong(1));
        assertEquals("123456", c.getString(2));
        assertTrue(c.isNull(3));
        assertEquals("1,2,3", c.getString(4));
        assertEquals(5, c.getInt(5));
        c.close();
    }

    /**
     * Test {@link SettingsProvider#delete(Uri, String, String[])} with deleting all favourites.
     */
    @Test
    public void testDeleteAllFavourites() {
        populateFavouritesWithTestData();
        final ContentResolver resolver = providerRule.getResolver();

        assertEquals(3, resolver.delete(SettingsContract.Favourites.CONTENT_URI, null, null));
        final Cursor c = resolver.query(SettingsContract.Favourites.CONTENT_URI, null, null,
                null, null);
        assertNotNull(c);
        assertEquals(0, c.getCount());
        c.close();
    }

    /**
     * Test {@link SettingsProvider#delete(Uri, String, String[])} with deleting all alerts.
     */
    @Test
    public void testDeleteAllAlerts() {
        populateAlertsWithTestData();
        final ContentResolver resolver = providerRule.getResolver();

        assertEquals(2, resolver.delete(SettingsContract.Alerts.CONTENT_URI, null, null));
        final Cursor c = resolver.query(SettingsContract.Alerts.CONTENT_URI, null, null, null,
                null);
        assertNotNull(c);
        assertEquals(0, c.getCount());
        c.close();
    }

    /**
     * Test {@link SettingsProvider#delete(Uri, String, String[])} with favourites.
     */
    @Test
    public void testDeleteFavourites() {
        populateFavouritesWithTestData();
        final ContentResolver resolver = providerRule.getResolver();

        int count = resolver.delete(SettingsContract.Favourites.CONTENT_URI,
                SettingsContract.Favourites.STOP_NAME + " = ?", new String[] { "A" });
        assertEquals(1, count);

        Cursor c = resolver.query(
                SettingsContract.Favourites.CONTENT_URI,
                new String[] { SettingsContract.Favourites.STOP_CODE },
                null, null, null);
        assertNotNull(c);
        assertEquals(2, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals("123456", c.getString(0));
        assertTrue(c.moveToNext());
        assertEquals("323456", c.getString(0));
        c.close();

        count = resolver.delete(SettingsContract.Favourites.CONTENT_URI,
                SettingsContract.Favourites.STOP_NAME + " = ?", new String[] { "C" });
        assertEquals(1, count);

        c = resolver.query(
                SettingsContract.Favourites.CONTENT_URI,
                new String[] { SettingsContract.Favourites.STOP_CODE },
                null, null, null);
        assertNotNull(c);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals("123456", c.getString(0));
        c.close();
    }

    /**
     * Test {@link SettingsProvider#delete(Uri, String, String[])} with alerts.
     */
    @Test
    public void testDeleteAlerts() {
        populateAlertsWithTestData();
        final ContentResolver resolver = providerRule.getResolver();

        int count = resolver.delete(SettingsContract.Alerts.CONTENT_URI,
                SettingsContract.Alerts.TYPE + " = ?",
                new String[] { String.valueOf(SettingsContract.Alerts.ALERTS_TYPE_PROXIMITY) });
        assertEquals(1, count);

        Cursor c = resolver.query(
                SettingsContract.Alerts.CONTENT_URI,
                new String[] { SettingsContract.Alerts.STOP_CODE },
                null, null, null);
        assertNotNull(c);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals("223456", c.getString(0));
        c.close();

        count = resolver.delete(SettingsContract.Alerts.CONTENT_URI,
                SettingsContract.Alerts.TYPE + " = ?",
                new String[] { String.valueOf(SettingsContract.Alerts.ALERTS_TYPE_TIME) });
        assertEquals(1, count);

        c = resolver.query(
                SettingsContract.Alerts.CONTENT_URI,
                new String[] { SettingsContract.Alerts.STOP_CODE },
                null, null, null);
        assertNotNull(c);
        assertEquals(0, c.getCount());
        c.close();
    }

    /**
     * Test {@link SettingsProvider#delete(Uri, String, String[])} with deleting a favourite by ID.
     */
    @Test
    public void testDeleteFavouritesById() {
        populateFavouritesWithTestData();
        final ContentResolver resolver = providerRule.getResolver();

        Uri deleteUri = ContentUris.withAppendedId(SettingsContract.Favourites.CONTENT_URI, 2);
        int count = resolver.delete(deleteUri,
                SettingsContract.Favourites.STOP_NAME + " = ?", new String[] { "C" });
        assertEquals(1, count);

        Cursor c = resolver.query(
                SettingsContract.Favourites.CONTENT_URI,
                new String[] { SettingsContract.Favourites.STOP_CODE },
                null, null, null);
        assertNotNull(c);
        assertEquals(2, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals("123456", c.getString(0));
        assertTrue(c.moveToNext());
        assertEquals("323456", c.getString(0));
        c.close();

        deleteUri = ContentUris.withAppendedId(SettingsContract.Favourites.CONTENT_URI, 3);
        count = resolver.delete(deleteUri, null, null);
        assertEquals(1, count);

        c = resolver.query(
                SettingsContract.Favourites.CONTENT_URI,
                new String[] { SettingsContract.Favourites.STOP_CODE },
                null, null, null);
        assertNotNull(c);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals("123456", c.getString(0));
        c.close();
    }

    /**
     * Test {@link SettingsProvider#delete(Uri, String, String[])} with deleting an alert by ID.
     */
    @Test
    public void testDeleteAlertsById() {
        populateAlertsWithTestData();
        final ContentResolver resolver = providerRule.getResolver();

        Uri deleteUri = ContentUris.withAppendedId(SettingsContract.Alerts.CONTENT_URI, 2);
        int count = resolver.delete(deleteUri,
                SettingsContract.Alerts.TYPE + " = ?",
                new String[] { String.valueOf(SettingsContract.Alerts.ALERTS_TYPE_PROXIMITY) });
        assertEquals(1, count);

        Cursor c = resolver.query(
                SettingsContract.Alerts.CONTENT_URI,
                new String[] { SettingsContract.Favourites.STOP_CODE },
                null, null, null);
        assertNotNull(c);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals("123456", c.getString(0));
        c.close();

        deleteUri = ContentUris.withAppendedId(SettingsContract.Alerts.CONTENT_URI, 1);
        count = resolver.delete(deleteUri, null, null);
        assertEquals(1, count);

        c = resolver.query(
                SettingsContract.Alerts.CONTENT_URI,
                new String[] { SettingsContract.Alerts.STOP_CODE },
                null, null, null);
        assertNotNull(c);
        assertEquals(0, c.getCount());
        c.close();
    }

    /**
     * Test {@link SettingsProvider#update(Uri, ContentValues, String, String[])} with updating all
     * favourites.
     */
    @Test
    public void testUpdateAllFavourites() {
        populateFavouritesWithTestData();
        final ContentResolver resolver = providerRule.getResolver();

        final ContentValues cv = new ContentValues();
        cv.put(SettingsContract.Favourites.STOP_NAME, "Z");
        assertEquals(3, resolver.update(SettingsContract.Favourites.CONTENT_URI, cv, null, null));

        final Cursor c = resolver.query(
                SettingsContract.Favourites.CONTENT_URI,
                new String[] { SettingsContract.Favourites.STOP_NAME },
                null, null, null);
        assertNotNull(c);
        assertEquals(3, c.getCount());

        while (c.moveToNext()) {
            assertEquals("Z", c.getString(0));
        }

        c.close();
    }

    /**
     * Test {@link SettingsProvider#update(Uri, ContentValues, String, String[])} with favourites.
     */
    @Test
    public void testUpdateFavourites() {
        populateFavouritesWithTestData();
        final ContentResolver resolver = providerRule.getResolver();

        ContentValues cv = new ContentValues();
        cv.put(SettingsContract.Favourites.STOP_NAME, "Z");
        int count = resolver.update(SettingsContract.Favourites.CONTENT_URI, cv,
                SettingsContract.Favourites.STOP_CODE + " = ?", new String[] { "123456" });
        assertEquals(1, count);

        Cursor c = resolver.query(
                SettingsContract.Favourites.CONTENT_URI, null, null, null,
                SettingsContract.Favourites._ID + " ASC");
        assertNotNull(c);
        assertEquals(3, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals("123456", c.getString(1));
        assertEquals("Z", c.getString(2));
        assertTrue(c.moveToNext());
        assertEquals("223456", c.getString(1));
        assertEquals("A", c.getString(2));
        assertTrue(c.moveToNext());
        assertEquals("323456", c.getString(1));
        assertEquals("C", c.getString(2));
        c.close();

        cv = new ContentValues();
        cv.put(SettingsContract.Favourites.STOP_NAME, "X");
        count = resolver.update(SettingsContract.Favourites.CONTENT_URI, cv,
                SettingsContract.Favourites.STOP_NAME + " = ?", new String[] { "C" });
        assertEquals(1, count);

        c = resolver.query(
                SettingsContract.Favourites.CONTENT_URI, null, null, null,
                SettingsContract.Favourites._ID + " ASC");
        assertNotNull(c);
        assertEquals(3, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals("123456", c.getString(1));
        assertEquals("Z", c.getString(2));
        assertTrue(c.moveToNext());
        assertEquals("223456", c.getString(1));
        assertEquals("A", c.getString(2));
        assertTrue(c.moveToNext());
        assertEquals("323456", c.getString(1));
        assertEquals("X", c.getString(2));
        c.close();
    }

    /**
     * Test {@link SettingsProvider#update(Uri, ContentValues, String, String[])} with updating a
     * favourite by ID.
     */
    @Test
    public void testUpdateFavouritesById() {
        populateFavouritesWithTestData();
        final ContentResolver resolver = providerRule.getResolver();

        Uri deleteUri = ContentUris.withAppendedId(SettingsContract.Favourites.CONTENT_URI, 2);
        ContentValues cv = new ContentValues();
        cv.put(SettingsContract.Favourites.STOP_NAME, "Z");
        int count = resolver.update(deleteUri, cv,
                SettingsContract.Favourites.STOP_NAME + " = ?", new String[] { "C" });
        assertEquals(1, count);

        Cursor c = resolver.query(SettingsContract.Favourites.CONTENT_URI, null,
                null, null, SettingsContract.Favourites._ID + " ASC");
        assertNotNull(c);
        assertEquals(3, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals("123456", c.getString(1));
        assertEquals("B", c.getString(2));
        assertTrue(c.moveToNext());
        assertEquals("223456", c.getString(1));
        assertEquals("Z", c.getString(2));
        assertTrue(c.moveToNext());
        assertEquals("323456", c.getString(1));
        assertEquals("C", c.getString(2));
        c.close();

        deleteUri = ContentUris.withAppendedId(SettingsContract.Favourites.CONTENT_URI, 3);
        cv = new ContentValues();
        cv.put(SettingsContract.Favourites.STOP_NAME, "X");
        count = resolver.update(deleteUri, cv, null, null);
        assertEquals(1, count);

        c = resolver.query(SettingsContract.Favourites.CONTENT_URI, null,
                null, null, SettingsContract.Favourites._ID + " ASC");
        assertNotNull(c);
        assertEquals(3, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals("123456", c.getString(1));
        assertEquals("B", c.getString(2));
        assertTrue(c.moveToNext());
        assertEquals("223456", c.getString(1));
        assertEquals("Z", c.getString(2));
        assertTrue(c.moveToNext());
        assertEquals("323456", c.getString(1));
        assertEquals("X", c.getString(2));
        c.close();
    }

    /**
     * Getting the set notification {@link Uri} on a {@link Cursor} previously set with
     * {@link Cursor#setNotificationUri(ContentResolver, Uri)} only became possible on
     * {@link Build.VERSION_CODES#KITKAT}. This method allows the assertion of notification
     * {@link Uri} to be made safely.
     *
     * @param expected The expected notification {@link Uri}.
     * @param cursor The {@link Cursor} that the notification {@link Uri} should have been set on.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static void assertNotificationUri(final Uri expected, final Cursor cursor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            assertEquals(expected, cursor.getNotificationUri());
        }
    }

    /**
     * Populate the favourites table with some test data.
     */
    private void populateFavouritesWithTestData() {
        providerRule.runDatabaseCommands(SettingsContract.DB_NAME,
                "INSERT INTO favourite_stops (stopCode, stopName) VALUES (" +
                        "\"123456\", \"B\")",
                "INSERT INTO favourite_stops (stopCode, stopName) VALUES (" +
                        "\"223456\", \"A\")",
                "INSERT INTO favourite_stops (stopCode, stopName) VALUES (" +
                        "\"323456\", \"C\")");
    }

    /**
     * Populate the alerts table with some test data.
     */
    private void populateAlertsWithTestData() {
        providerRule.runDatabaseCommands(SettingsContract.DB_NAME,
                "INSERT INTO active_alerts (type, timeAdded, stopCode, distanceFrom) VALUES (" +
                        "1, " + currentTime + ", \"123456\", 10)",
                "INSERT INTO active_alerts (type, timeAdded, stopCode, serviceNames, timeTrigger)" +
                        " VALUES (2, " + currentTime + ", \"223456\", \"1,2,3\", 5)");
    }
}
