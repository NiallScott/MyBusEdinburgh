/*
 * Copyright (C) 2015 Niall 'Rivernile' Scott
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
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ProviderTestCase2;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link SettingsProvider}.
 *
 * @author Niall Scott
 */
@RunWith(AndroidJUnit4.class)
public class SettingsProviderTests extends ProviderTestCase2<SettingsProvider> {

    public SettingsProviderTests() {
        super(SettingsProvider.class, SettingsContract.AUTHORITY);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        setContext(InstrumentationRegistry.getTargetContext());

        super.setUp();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        getMockContext().deleteDatabase(SettingsContract.DB_NAME);
    }

    /**
     * Test that {@link SettingsProvider#getType(Uri)} returns the correct MIME types for the
     * supplied content {@link Uri}s.
     */
    @Test
    public void testGetTypeSuccess() {
        assertEquals(SettingsContract.Favourites.CONTENT_TYPE,
                getMockContentResolver().getType(SettingsContract.Favourites.CONTENT_URI));
        assertEquals(SettingsContract.Favourites.CONTENT_ITEM_TYPE,
                getMockContentResolver().getType(
                        ContentUris.withAppendedId(SettingsContract.Favourites.CONTENT_URI, 1)));
        assertEquals(SettingsContract.Alerts.CONTENT_TYPE,
                getMockContentResolver().getType(SettingsContract.Alerts.CONTENT_URI));
        assertEquals(SettingsContract.Alerts.CONTENT_ITEM_TYPE,
                getMockContentResolver().getType(
                        ContentUris.withAppendedId(SettingsContract.Alerts.CONTENT_URI, 1)));
    }

    /**
     * Test that {@link SettingsProvider#getType(Uri)} returns {@code null} when invalid
     * {@link Uri}s are supplied.
     */
    @Test
    public void testGetTypeWithInvalidUris() {
        assertNull(getMockContentResolver().getType(Uri.parse("content://invalid.uri/thing")));
        assertNull(getMockContentResolver()
                .getType(Uri.parse("content://" + SettingsContract.AUTHORITY)));
        assertNull(getMockContentResolver()
                .getType(Uri.parse("content://" + SettingsContract.AUTHORITY + "/invalid")));
    }

    /**
     * Test that {@link SettingsProvider#query(Uri, String[], String, String[], String)} throws an
     * {@link IllegalArgumentException} when an invalid {@link Uri} has been supplied.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testQueryWithInvalidUri() {
        getMockContentResolver()
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
        getMockContentResolver()
                .insert(Uri.parse("content://" + SettingsContract.AUTHORITY + "/invalid"), cv);
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
        getMockContentResolver().insert(
                ContentUris.withAppendedId(SettingsContract.Favourites.CONTENT_URI, 1), cv);
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
        getMockContentResolver().insert(
                ContentUris.withAppendedId(SettingsContract.Alerts.CONTENT_URI, 1), cv);
    }

    /**
     * Test that {@link SettingsProvider#delete(Uri, String, String[])} throws an
     * {@link IllegalArgumentException} when an invalid {@link Uri} has been supplied.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDeleteWithInvalidUri() {
        getMockContentResolver()
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
        getMockContentResolver()
                .update(Uri.parse("content://" + SettingsContract.AUTHORITY + "/invalid"), cv,
                        null, null);
    }

    /**
     * Test that {@link SettingsProvider#query(Uri, String[], String, String[], String)} returns a
     * {@link Cursor} with {@code 0} rows when no data has been inserted in to the database with
     * favourites.
     */
    @Test
    public void testQueryFavouritesWithEmptyTable() {
        final Cursor c = getMockContentResolver().query(SettingsContract.Favourites.CONTENT_URI,
                null, null, null, null);
        assertEquals(0, c.getCount());
        assertNotificationUri(SettingsContract.Favourites.CONTENT_URI, c);
        c.close();
    }

    /**
     * Test various permutations of
     * {@link SettingsProvider#query(Uri, String[], String, String[], String)} with favourites.
     */
    @Test
    public void testQueryFavourites() {
        populateFavouritesWithTestData();

        Cursor c = getMockContentResolver().query(SettingsContract.Favourites.CONTENT_URI,
                null, null, null, null);
        assertEquals(3, c.getCount());

        c.moveToNext();
        assertEquals("A", c.getString(2));
        c.moveToNext();
        assertEquals("B", c.getString(2));
        c.moveToNext();
        assertEquals("C", c.getString(2));
        assertNotificationUri(SettingsContract.Favourites.CONTENT_URI, c);
        c.close();

        c = getMockContentResolver().query(SettingsContract.Favourites.CONTENT_URI,
                null, null, null, SettingsContract.Favourites.STOP_NAME + " DESC");
        assertEquals(3, c.getCount());

        c.moveToNext();
        assertEquals("C", c.getString(2));
        c.moveToNext();
        assertEquals("B", c.getString(2));
        c.moveToNext();
        assertEquals("A", c.getString(2));
        assertNotificationUri(SettingsContract.Favourites.CONTENT_URI, c);
        c.close();

        c = getMockContentResolver().query(SettingsContract.Favourites.CONTENT_URI,
                new String[] { SettingsContract.Favourites.STOP_NAME },
                SettingsContract.Favourites.STOP_CODE + " = ?",
                new String[] { "323456" }, null);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals("C", c.getString(0));
        assertNotificationUri(SettingsContract.Favourites.CONTENT_URI, c);
        c.close();
    }

    /**
     * Test {@link SettingsProvider#query(Uri, String[], String, String[], String)} with favourites
     * for a known item ID.
     */
    @Test
    public void testQueryFavouritesById() {
        populateFavouritesWithTestData();

        Cursor c = getMockContentResolver().query(
                ContentUris.withAppendedId(SettingsContract.Favourites.CONTENT_URI, 1), null,
                null, null, null);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals("B", c.getString(2));
        c.close();

        c = getMockContentResolver().query(
                ContentUris.withAppendedId(SettingsContract.Favourites.CONTENT_URI, 42), null,
                null, null, null);
        assertEquals(0, c.getCount());
        c.close();

        c = getMockContentResolver().query(
                ContentUris.withAppendedId(SettingsContract.Favourites.CONTENT_URI, 1),
                new String[] { SettingsContract.Favourites.STOP_NAME },
                SettingsContract.Favourites.STOP_CODE + " = ?",
                new String[] { "323456" }, null);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals("B", c.getString(0));
        c.close();
    }

    /**
     * Test {@link SettingsProvider#insert(Uri, ContentValues)} with favourites.
     */
    @Test
    public void testInsertFavourites() {
        Cursor c = getMockContentResolver().query(SettingsContract.Favourites.CONTENT_URI,
                null, null, null, null);
        assertEquals(0, c.getCount());
        c.close();

        final ContentValues cv = new ContentValues();
        cv.put(SettingsContract.Favourites.STOP_CODE, "24680");
        cv.put(SettingsContract.Favourites.STOP_NAME, "Test stop");
        final Uri returnedUri = getMockContentResolver()
                .insert(SettingsContract.Favourites.CONTENT_URI, cv);
        assertEquals(ContentUris.withAppendedId(SettingsContract.Favourites.CONTENT_URI, 1),
                returnedUri);

        c = getMockContentResolver().query(returnedUri, null, null, null, null);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals(1, c.getInt(0));
        assertEquals("24680", c.getString(1));
        assertEquals("Test stop", c.getString(2));
        c.close();
    }

    /**
     * Test {@link SettingsProvider#delete(Uri, String, String[])} with deleting all favourites.
     */
    @Test
    public void testDeleteAllFavourites() {
        populateFavouritesWithTestData();

        assertEquals(3, getMockContentResolver()
                .delete(SettingsContract.Favourites.CONTENT_URI, null, null));
        final Cursor c = getMockContentResolver().query(SettingsContract.Favourites.CONTENT_URI,
                null, null, null, null);
        assertEquals(0, c.getCount());
        c.close();
    }

    /**
     * Test {@link SettingsProvider#delete(Uri, String, String[])} with favourites.
     */
    @Test
    public void testDeleteFavourites() {
        populateFavouritesWithTestData();

        int count = getMockContentResolver().delete(SettingsContract.Favourites.CONTENT_URI,
                SettingsContract.Favourites.STOP_NAME + " = ?", new String[] { "A" });
        assertEquals(1, count);

        Cursor c = getMockContentResolver().query(
                SettingsContract.Favourites.CONTENT_URI,
                new String[] { SettingsContract.Favourites.STOP_CODE },
                null, null, null);
        assertEquals(2, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals("123456", c.getString(0));
        assertTrue(c.moveToNext());
        assertEquals("323456", c.getString(0));
        c.close();

        count = getMockContentResolver().delete(SettingsContract.Favourites.CONTENT_URI,
                SettingsContract.Favourites.STOP_NAME + " = ?", new String[] { "C" });
        assertEquals(1, count);

        c = getMockContentResolver().query(
                SettingsContract.Favourites.CONTENT_URI,
                new String[] { SettingsContract.Favourites.STOP_CODE },
                null, null, null);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals("123456", c.getString(0));
        c.close();
    }

    /**
     * Test {@link SettingsProvider#delete(Uri, String, String[])} with deleting a favourite by ID.
     */
    @Test
    public void testDeleteFavouritesById() {
        populateFavouritesWithTestData();

        Uri deleteUri = ContentUris.withAppendedId(SettingsContract.Favourites.CONTENT_URI, 2);
        int count = getMockContentResolver().delete(deleteUri,
                SettingsContract.Favourites.STOP_NAME + " = ?", new String[] { "C" });
        assertEquals(1, count);

        Cursor c = getMockContentResolver().query(
                SettingsContract.Favourites.CONTENT_URI,
                new String[] { SettingsContract.Favourites.STOP_CODE },
                null, null, null);
        assertEquals(2, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals("123456", c.getString(0));
        assertTrue(c.moveToNext());
        assertEquals("323456", c.getString(0));
        c.close();

        deleteUri = ContentUris.withAppendedId(SettingsContract.Favourites.CONTENT_URI, 3);
        count = getMockContentResolver().delete(deleteUri, null, null);
        assertEquals(1, count);

        c = getMockContentResolver().query(
                SettingsContract.Favourites.CONTENT_URI,
                new String[] { SettingsContract.Favourites.STOP_CODE },
                null, null, null);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals("123456", c.getString(0));
        c.close();
    }

    /**
     * Test {@link SettingsProvider#update(Uri, ContentValues, String, String[])} with updating all
     * favourites.
     */
    @Test
    public void testUpdateAllFavourites() {
        populateFavouritesWithTestData();

        final ContentValues cv = new ContentValues();
        cv.put(SettingsContract.Favourites.STOP_NAME, "Z");
        assertEquals(3, getMockContentResolver()
                .update(SettingsContract.Favourites.CONTENT_URI, cv, null, null));

        final Cursor c = getMockContentResolver().query(
                SettingsContract.Favourites.CONTENT_URI,
                new String[] { SettingsContract.Favourites.STOP_NAME },
                null, null, null);
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

        ContentValues cv = new ContentValues();
        cv.put(SettingsContract.Favourites.STOP_NAME, "Z");
        int count = getMockContentResolver().update(SettingsContract.Favourites.CONTENT_URI, cv,
                SettingsContract.Favourites.STOP_CODE + " = ?", new String[] { "123456" });
        assertEquals(1, count);

        Cursor c = getMockContentResolver().query(
                SettingsContract.Favourites.CONTENT_URI, null, null, null,
                SettingsContract.Favourites._ID + " ASC");
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
        count = getMockContentResolver().update(SettingsContract.Favourites.CONTENT_URI, cv,
                SettingsContract.Favourites.STOP_NAME + " = ?", new String[] { "C" });
        assertEquals(1, count);

        c = getMockContentResolver().query(
                SettingsContract.Favourites.CONTENT_URI, null, null, null,
                SettingsContract.Favourites._ID + " ASC");
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

        Uri deleteUri = ContentUris.withAppendedId(SettingsContract.Favourites.CONTENT_URI, 2);
        ContentValues cv = new ContentValues();
        cv.put(SettingsContract.Favourites.STOP_NAME, "Z");
        int count = getMockContentResolver().update(deleteUri, cv,
                SettingsContract.Favourites.STOP_NAME + " = ?", new String[] { "C" });
        assertEquals(1, count);

        Cursor c = getMockContentResolver().query(SettingsContract.Favourites.CONTENT_URI, null,
                null, null, SettingsContract.Favourites._ID + " ASC");
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
        count = getMockContentResolver().update(deleteUri, cv, null, null);
        assertEquals(1, count);

        c = getMockContentResolver().query(SettingsContract.Favourites.CONTENT_URI, null,
                null, null, SettingsContract.Favourites._ID + " ASC");
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
    @TargetApi(19)
    private static void assertNotificationUri(final Uri expected, final Cursor cursor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            assertEquals(expected, cursor.getNotificationUri());
        }
    }

    /**
     * Populate the favourites table with some test data.
     */
    private void populateFavouritesWithTestData() {
        final SQLiteDatabase db = new SettingsOpenHelper(getMockContext()).getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(SettingsContract.Favourites.STOP_CODE, "123456");
        cv.put(SettingsContract.Favourites.STOP_NAME, "B");
        db.insertOrThrow(SettingsContract.Favourites.TABLE_NAME, null, cv);

        cv = new ContentValues();
        cv.put(SettingsContract.Favourites.STOP_CODE, "223456");
        cv.put(SettingsContract.Favourites.STOP_NAME, "A");
        db.insertOrThrow(SettingsContract.Favourites.TABLE_NAME, null, cv);

        cv = new ContentValues();
        cv.put(SettingsContract.Favourites.STOP_CODE, "323456");
        cv.put(SettingsContract.Favourites.STOP_NAME, "C");
        db.insertOrThrow(SettingsContract.Favourites.TABLE_NAME, null, cv);

        db.close();
    }
}
