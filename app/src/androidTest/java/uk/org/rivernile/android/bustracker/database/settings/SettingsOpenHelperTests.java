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

package uk.org.rivernile.android.bustracker.database.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import uk.org.rivernile.android.utils.DatabaseRenamingContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link SettingsOpenHelper}.
 *
 * @author Niall Scott
 */
public class SettingsOpenHelperTests {

    private static final String DB_NAME = "settings.db";

    private DatabaseRenamingContext context;

    @Before
    public void setUp() {
        context = new DatabaseRenamingContext(ApplicationProvider.getApplicationContext(),
                "test_");
    }

    @After
    public void tearDown() {
        context.deleteDatabase(DB_NAME);
        context = null;
    }

    /**
     * Test that a newly created database has the correct schema.
     */
    @Test
    public void testDatabaseDefaultState() {
        final SettingsOpenHelper openHelper = new SettingsOpenHelper(context);
        final SQLiteDatabase db = openHelper.getReadableDatabase();

        assertDatabaseSchema(db);
        assertFavouritesTableEmpty(db);
        assertAlertsTableEmpty(db);
        db.close();
    }

    /**
     * Test that a database upgraded from version 1 has the correct schema.
     */
    @Test
    public void testEmptyUpgradeFromV1() {
        createV1Database();
        final SettingsOpenHelper openHelper = new SettingsOpenHelper(context);
        final SQLiteDatabase db = openHelper.getReadableDatabase();

        assertDatabaseSchema(db);
        assertFavouritesTableEmpty(db);
        assertAlertsTableEmpty(db);
        db.close();
    }

    /**
     * Test that a database upgraded from version 2 has the correct schema.
     */
    @Test
    public void testEmptyUpgradeFromV2() {
        createV2Database();
        final SettingsOpenHelper openHelper = new SettingsOpenHelper(context);
        final SQLiteDatabase db = openHelper.getReadableDatabase();

        assertDatabaseSchema(db);
        assertFavouritesTableEmpty(db);
        assertAlertsTableEmpty(db);
        db.close();
    }

    /**
     * Test that an existing version 3 database does not have its schema changed.
     */
    @Test
    public void testEmptyStateV3Database() {
        createV3Database();
        final SettingsOpenHelper openHelper = new SettingsOpenHelper(context);
        final SQLiteDatabase db = openHelper.getReadableDatabase();

        assertDatabaseSchema(db);
        assertFavouritesTableEmpty(db);
        assertAlertsTableEmpty(db);
        db.close();
    }

    /**
     * Test that a favourite stop with a {@code null stopCode} does not get populated in the
     * upgraded table. This is a very unlikely scenario.
     */
    @Test
    public void testOldFavouriteTableWithNullStopCode() {
        createV1Database();
        SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        final ContentValues cv = new ContentValues();
        cv.putNull(SettingsContract.Favourites._ID);
        cv.put(SettingsContract.Favourites.STOP_NAME, "Blah");
        db.insertOrThrow("favourite_stops", null, cv);
        db.close();

        final SettingsOpenHelper openHelper = new SettingsOpenHelper(context);
        db = openHelper.getReadableDatabase();
        assertDatabaseSchema(db);

        final Cursor c = db.query("favourite_stops", null, null, null, null, null, null);
        assertEquals(0, c.getCount());
        c.close();
        db.close();
    }

    /**
     * Test that migrating an old database to version 3 with a single favourite stop successfully
     * preserves the single favourite stop.
     */
    @Test
    public void testOldFavouriteTableWithSingleEntry() {
        createV1Database();
        SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        final ContentValues cv = new ContentValues();
        cv.put(SettingsContract.Favourites._ID, "123456");
        cv.put(SettingsContract.Favourites.STOP_NAME, "Blah");
        db.insertOrThrow("favourite_stops", null, cv);
        db.close();

        final SettingsOpenHelper openHelper = new SettingsOpenHelper(context);
        db = openHelper.getReadableDatabase();
        assertDatabaseSchema(db);

        final Cursor c = db.query("favourite_stops", null, null, null, null, null, null);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToFirst());
        assertEquals("123456", c.getString(1));
        assertEquals("Blah", c.getString(2));
        c.close();
        db.close();
    }

    /**
     * Test that migrating an old database to version 3 with multiple favourite stops successfully
     * preserves all favourite stops.
     */
    @Test
    public void testOldFavouriteTableWithMultipleEntries() {
        createV1Database();
        SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);

        ContentValues cv = new ContentValues();
        cv.put(SettingsContract.Favourites._ID, "123456");
        cv.put(SettingsContract.Favourites.STOP_NAME, "First");
        db.insertOrThrow("favourite_stops", null, cv);

        cv = new ContentValues();
        cv.put(SettingsContract.Favourites._ID, "987654");
        cv.put(SettingsContract.Favourites.STOP_NAME, "Second");
        db.insertOrThrow("favourite_stops", null, cv);

        cv = new ContentValues();
        cv.put(SettingsContract.Favourites._ID, "111111");
        cv.put(SettingsContract.Favourites.STOP_NAME, "Third");
        db.insertOrThrow("favourite_stops", null, cv);
        db.close();

        final SettingsOpenHelper openHelper = new SettingsOpenHelper(context);
        db = openHelper.getReadableDatabase();
        assertDatabaseSchema(db);

        final Cursor c = db.query("favourite_stops", null, null, null, null, null, "stopCode ASC");
        assertEquals(3, c.getCount());

        assertTrue(c.moveToNext());
        assertEquals("111111", c.getString(1));
        assertEquals("Third", c.getString(2));

        assertTrue(c.moveToNext());
        assertEquals("123456", c.getString(1));
        assertEquals("First", c.getString(2));

        assertTrue(c.moveToNext());
        assertEquals("987654", c.getString(1));
        assertEquals("Second", c.getString(2));

        c.close();
        db.close();
    }

    /**
     * Test that migrating an old database to version 3 with multiple favourite stops, but one of
     * them invalid, successfully preserves the valid stops.
     */
    @Test
    public void testOldFavouriteTableWithMultipleEntriesWithInvalidRow() {
        createV1Database();
        SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);

        ContentValues cv = new ContentValues();
        cv.put(SettingsContract.Favourites._ID, "123456");
        cv.put(SettingsContract.Favourites.STOP_NAME, "First");
        db.insertOrThrow("favourite_stops", null, cv);

        cv = new ContentValues();
        cv.putNull(SettingsContract.Favourites._ID);
        cv.put(SettingsContract.Favourites.STOP_NAME, "Second");
        db.insertOrThrow("favourite_stops", null, cv);

        cv = new ContentValues();
        cv.put(SettingsContract.Favourites._ID, "111111");
        cv.put(SettingsContract.Favourites.STOP_NAME, "Third");
        db.insertOrThrow("favourite_stops", null, cv);
        db.close();

        final SettingsOpenHelper openHelper = new SettingsOpenHelper(context);
        db = openHelper.getReadableDatabase();
        assertDatabaseSchema(db);

        final Cursor c = db.query("favourite_stops", null, null, null, null, null, "stopCode ASC");
        assertEquals(2, c.getCount());

        assertTrue(c.moveToNext());
        assertEquals("111111", c.getString(1));
        assertEquals("Third", c.getString(2));

        assertTrue(c.moveToNext());
        assertEquals("123456", c.getString(1));
        assertEquals("First", c.getString(2));

        c.close();
        db.close();
    }

    /**
     * Test that inserting an alert when an expired alert exists in the database causes the trigger
     * to fire that deletes the expired alert from the database.
     */
    @Test
    public void testInsertAlertFiresTrigger() {
        final SettingsOpenHelper openHelper = new SettingsOpenHelper(context);
        final SQLiteDatabase db = openHelper.getReadableDatabase();
        assertDatabaseSchema(db);

        ContentValues cv = new ContentValues();
        cv.put(SettingsContract.Alerts.TYPE, SettingsContract.Alerts.ALERTS_TYPE_PROXIMITY);
        cv.put(SettingsContract.Alerts.TIME_ADDED, 0L);
        cv.put(SettingsContract.Alerts.STOP_CODE, "123456");
        cv.put(SettingsContract.Alerts.DISTANCE_FROM, 1);
        db.insert("active_alerts", null, cv);
        assertExpiredAlertExists(db);

        cv = new ContentValues();
        cv.put(SettingsContract.Alerts.TYPE, SettingsContract.Alerts.ALERTS_TYPE_TIME);
        cv.put(SettingsContract.Alerts.TIME_ADDED, System.currentTimeMillis());
        cv.put(SettingsContract.Alerts.STOP_CODE, "123456");
        cv.put(SettingsContract.Alerts.SERVICE_NAMES, "1,2,3");
        cv.put(SettingsContract.Alerts.TIME_TRIGGER, 1);
        db.insert("active_alerts", null, cv);

        final Cursor c = db.query("active_alerts", new String[] { SettingsContract.Alerts.TYPE },
                null, null, null, null, null);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals(SettingsContract.Alerts.ALERTS_TYPE_TIME, c.getInt(0));
        c.close();
        db.close();
    }

    /**
     * Test that updating an alert when an expired alert exists in the database causes the trigger
     * to fire that deletes the expired alert from the database.
     */
    @Test
    public void testUpdateAlertFiresTrigger() {
        final SettingsOpenHelper openHelper = new SettingsOpenHelper(context);
        final SQLiteDatabase db = openHelper.getReadableDatabase();
        assertDatabaseSchema(db);

        ContentValues cv = new ContentValues();
        cv.put(SettingsContract.Alerts.TYPE, SettingsContract.Alerts.ALERTS_TYPE_TIME);
        cv.put(SettingsContract.Alerts.TIME_ADDED, System.currentTimeMillis());
        cv.put(SettingsContract.Alerts.STOP_CODE, "123456");
        cv.put(SettingsContract.Alerts.SERVICE_NAMES, "1,2,3");
        cv.put(SettingsContract.Alerts.TIME_TRIGGER, 1);
        db.insert("active_alerts", null, cv);

        cv = new ContentValues();
        cv.put(SettingsContract.Alerts.TYPE, SettingsContract.Alerts.ALERTS_TYPE_PROXIMITY);
        cv.put(SettingsContract.Alerts.TIME_ADDED, 0L);
        cv.put(SettingsContract.Alerts.STOP_CODE, "24680");
        cv.put(SettingsContract.Alerts.DISTANCE_FROM, 1);
        db.insert("active_alerts", null, cv);
        assertExpiredAlertExists(db);

        cv = new ContentValues();
        cv.put(SettingsContract.Alerts.STOP_CODE, "987654");
        db.update("active_alerts", cv, "stopCode = ?", new String[] { "123456" });

        final Cursor c = db.query("active_alerts", new String[] { SettingsContract.Alerts.TYPE },
                null, null, null, null, null);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals(SettingsContract.Alerts.ALERTS_TYPE_TIME, c.getInt(0));
        c.close();
        db.close();
    }

    /**
     * Test that deleting an alert when an expired alert exists in the database causes the trigger
     * to fire that deletes the expired alert from the database.
     */
    @Test
    public void testDeleteAlertFiresTrigger() {
        final SettingsOpenHelper openHelper = new SettingsOpenHelper(context);
        final SQLiteDatabase db = openHelper.getReadableDatabase();
        assertDatabaseSchema(db);

        ContentValues cv = new ContentValues();
        cv.put(SettingsContract.Alerts.TYPE, SettingsContract.Alerts.ALERTS_TYPE_TIME);
        cv.put(SettingsContract.Alerts.TIME_ADDED, System.currentTimeMillis());
        cv.put(SettingsContract.Alerts.STOP_CODE, "123456");
        cv.put(SettingsContract.Alerts.SERVICE_NAMES, "1,2,3");
        cv.put(SettingsContract.Alerts.TIME_TRIGGER, 1);
        db.insert("active_alerts", null, cv);

        cv = new ContentValues();
        cv.put(SettingsContract.Alerts.TYPE, SettingsContract.Alerts.ALERTS_TYPE_PROXIMITY);
        cv.put(SettingsContract.Alerts.TIME_ADDED, 0L);
        cv.put(SettingsContract.Alerts.STOP_CODE, "24680");
        cv.put(SettingsContract.Alerts.DISTANCE_FROM, 1);
        db.insert("active_alerts", null, cv);
        assertExpiredAlertExists(db);

        db.delete("active_alerts", "stopCode = ?", new String[] { "123456" });

        final Cursor c = db.query("active_alerts", null, null, null, null, null, null);
        assertEquals(0, c.getCount());
        c.close();
        db.close();
    }

    /**
     * Assert that the database schema is as expected.
     *
     * @param db The {@link SQLiteDatabase} to check the schema in.
     */
    private static void assertDatabaseSchema(@NonNull final SQLiteDatabase db) {
        assertEquals(3, db.getVersion());

        final Cursor tablesCursor = db.rawQuery("SELECT name FROM sqlite_master WHERE " +
                "type = 'table' AND " +
                "name IN ('favourite_stops','active_alerts','temp_favourites') " +
                "ORDER BY name ASC;", null);
        assertEquals(2, tablesCursor.getCount());

        assertTrue(tablesCursor.moveToNext());
        assertEquals("active_alerts", tablesCursor.getString(0));

        assertTrue(tablesCursor.moveToNext());
        assertEquals("favourite_stops", tablesCursor.getString(0));

        tablesCursor.close();

        final Cursor favouritesCursor = db.rawQuery("PRAGMA table_info(favourite_stops);", null);
        assertEquals(3, favouritesCursor.getCount());

        assertTrue(favouritesCursor.moveToNext());
        assertEquals("_id", favouritesCursor.getString(1));
        assertEquals("INTEGER", favouritesCursor.getString(2));
        assertEquals(0, favouritesCursor.getInt(3));
        assertNull(favouritesCursor.getString(4));
        assertEquals(1, favouritesCursor.getInt(5));

        assertTrue(favouritesCursor.moveToNext());
        assertEquals("stopCode", favouritesCursor.getString(1));
        assertEquals("TEXT", favouritesCursor.getString(2));
        assertEquals(1, favouritesCursor.getInt(3));
        assertNull(favouritesCursor.getString(4));
        assertEquals(0, favouritesCursor.getInt(5));

        assertTrue(favouritesCursor.moveToNext());
        assertEquals("stopName", favouritesCursor.getString(1));
        assertEquals("TEXT", favouritesCursor.getString(2));
        assertEquals(1, favouritesCursor.getInt(3));
        assertNull(favouritesCursor.getString(4));
        assertEquals(0, favouritesCursor.getInt(5));

        favouritesCursor.close();

        final Cursor alertsCursor = db.rawQuery("PRAGMA table_info(active_alerts);", null);
        assertEquals(7, alertsCursor.getCount());

        assertTrue(alertsCursor.moveToNext());
        assertEquals("_id", alertsCursor.getString(1));
        assertEquals("INTEGER", alertsCursor.getString(2));
        assertEquals(0, alertsCursor.getInt(3));
        assertNull(alertsCursor.getString(4));
        assertEquals(1, alertsCursor.getInt(5));

        assertTrue(alertsCursor.moveToNext());
        assertEquals("type", alertsCursor.getString(1));
        assertEquals("NUMERIC", alertsCursor.getString(2));
        assertEquals(1, alertsCursor.getInt(3));
        assertNull(alertsCursor.getString(4));
        assertEquals(0, alertsCursor.getInt(5));

        assertTrue(alertsCursor.moveToNext());
        assertEquals("timeAdded", alertsCursor.getString(1));
        assertEquals("INTEGER", alertsCursor.getString(2));
        assertEquals(1, alertsCursor.getInt(3));
        assertNull(alertsCursor.getString(4));
        assertEquals(0, alertsCursor.getInt(5));

        assertTrue(alertsCursor.moveToNext());
        assertEquals("stopCode", alertsCursor.getString(1));
        assertEquals("TEXT", alertsCursor.getString(2));
        assertEquals(1, alertsCursor.getInt(3));
        assertNull(alertsCursor.getString(4));
        assertEquals(0, alertsCursor.getInt(5));

        assertTrue(alertsCursor.moveToNext());
        assertEquals("distanceFrom", alertsCursor.getString(1));
        assertEquals("INTEGER", alertsCursor.getString(2));
        assertEquals(0, alertsCursor.getInt(3));
        assertNull(alertsCursor.getString(4));
        assertEquals(0, alertsCursor.getInt(5));

        assertTrue(alertsCursor.moveToNext());
        assertEquals("serviceNames", alertsCursor.getString(1));
        assertEquals("TEXT", alertsCursor.getString(2));
        assertEquals(0, alertsCursor.getInt(3));
        assertNull(alertsCursor.getString(4));
        assertEquals(0, alertsCursor.getInt(5));

        assertTrue(alertsCursor.moveToNext());
        assertEquals("timeTrigger", alertsCursor.getString(1));
        assertEquals("INTEGER", alertsCursor.getString(2));
        assertEquals(0, alertsCursor.getInt(3));
        assertNull(alertsCursor.getString(4));
        assertEquals(0, alertsCursor.getInt(5));

        alertsCursor.close();

        final Cursor alertsTriggerCursor = db.rawQuery("SELECT name, tbl_name FROM sqlite_master " +
                "WHERE type = 'trigger' ORDER BY name ASC;", null);
        assertEquals(3, alertsTriggerCursor.getCount());

        assertTrue(alertsTriggerCursor.moveToNext());
        assertEquals("delete_alert", alertsTriggerCursor.getString(0));
        assertEquals("active_alerts", alertsTriggerCursor.getString(1));

        assertTrue(alertsTriggerCursor.moveToNext());
        assertEquals("insert_alert", alertsTriggerCursor.getString(0));
        assertEquals("active_alerts", alertsTriggerCursor.getString(1));

        assertTrue(alertsTriggerCursor.moveToNext());
        assertEquals("update_alert", alertsTriggerCursor.getString(0));
        assertEquals("active_alerts", alertsTriggerCursor.getString(1));

        alertsTriggerCursor.close();
    }

    /**
     * Assert that the favourite stops table has no entries.
     *
     * @param db The {@link SQLiteDatabase} containing the favourite stops table.
     */
    private static void assertFavouritesTableEmpty(@NonNull final SQLiteDatabase db) {
        assertTableEmpty(db, "favourite_stops");
    }

    /**
     * Assert that the alerts table has no entries.
     *
     * @param db The {@link SQLiteDatabase} containing the alerts table.
     */
    private static void assertAlertsTableEmpty(@NonNull final SQLiteDatabase db) {
        assertTableEmpty(db, "active_alerts");
    }

    /**
     * Assert that the table with the given {@code tableName} has no rows.
     *
     * @param db The {@link SQLiteDatabase} containing the table.
     * @param tableName The name of the table to assert for emptiness.
     */
    private static void assertTableEmpty(@NonNull final SQLiteDatabase db,
            @NonNull final String tableName) {
        final Cursor c = db.query(tableName, null, null, null, null, null, null);
        assertEquals(0, c.getCount());
        c.close();
    }

    /**
     * Assert that the alerts table contains an expired alert.
     *
     * @param db The {@link SQLiteDatabase} containing the data.
     */
    private static void assertExpiredAlertExists(@NonNull final SQLiteDatabase db) {
        final Cursor c = db.query("active_alerts", new String[] { "timeAdded" }, "timeAdded = ?",
                new String[] { String.valueOf(0) }, null, null, null);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToNext());
        assertEquals(0L, c.getLong(0));
        c.close();
    }

    /**
     * Create a version 1 database schema.
     */
    private void createV1Database() {
        final SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        createFavouritesTableV1V2(db);
        db.setVersion(1);
        db.close();
    }

    /**
     * Create a version 2 database schema.
     */
    private void createV2Database() {
        final SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        createFavouritesTableV1V2(db);
        createAlertsTable(db);
        db.setVersion(2);
        db.close();
    }

    /**
     * Create a version 3 database schema.
     */
    private void createV3Database() {
        final SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        createFavouritesTableV3(db);
        createAlertsTable(db);
        db.setVersion(3);
        db.close();
    }

    /**
     * Create the favourite stops table in the version 1 and version 2 database with the expected
     * schema.
     *
     * @param db The {@link SQLiteDatabase} to create the schema in.
     */
    private static void createFavouritesTableV1V2(@NonNull final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE favourite_stops (" +
                "_id TEXT PRIMARY KEY," +
                "stopName TEXT NOT NULL);");
    }

    /**
     * Create the favourite stops table in the version 3 database with the expected schema.
     *
     * @param db The {@link SQLiteDatabase} to create the schema in.
     */
    private static void createFavouritesTableV3(@NonNull final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE favourite_stops (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "stopCode TEXT NOT NULL UNIQUE," +
                "stopName TEXT NOT NULL);");
    }

    /**
     * Create the alerts table in the database with the expected schema.
     *
     * @param db The {@link SQLiteDatabase} to create the schema in.
     */
    private static void createAlertsTable(@NonNull final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE active_alerts (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "type NUMERIC NOT NULL," +
                "timeAdded INTEGER NOT NULL," +
                "stopCode TEXT NOT NULL," +
                "distanceFrom INTEGER," +
                "serviceNames TEXT," +
                "timeTrigger INTEGER);");
        createAlertsTriggers(db);
    }

    /**
     * Create the expected triggers for alerts.
     *
     * @param db The {@link SQLiteDatabase} to create the alerts triggers in.
     */
    private static void createAlertsTriggers(@NonNull final SQLiteDatabase db) {
        db.execSQL("CREATE TRIGGER IF NOT EXISTS insert_alert BEFORE INSERT ON active_alerts " +
                "FOR EACH ROW BEGIN " +
                "DELETE FROM active_alerts " +
                "WHERE timeAdded < ((SELECT strftime('%s','now') * 1000) - 3600000); END;");
        db.execSQL("CREATE TRIGGER IF NOT EXISTS delete_alert AFTER DELETE ON active_alerts " +
                "FOR EACH ROW BEGIN " +
                "DELETE FROM active_alerts " +
                "WHERE timeAdded < ((SELECT strftime('%s','now') * 1000) - 3600000); END;");
        db.execSQL("CREATE TRIGGER IF NOT EXISTS update_alert AFTER UPDATE ON active_alerts " +
                "FOR EACH ROW BEGIN " +
                "DELETE FROM active_alerts " +
                "WHERE timeAdded < ((SELECT strftime('%s','now') * 1000) - 3600000); END;");
    }
}
