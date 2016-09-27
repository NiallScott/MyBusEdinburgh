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
 * 1. This notice may not be removed or altered from any file it appears in.
 *
 * 2. Any modifications made to this software, except those defined in
 *    clause 3 of this agreement, must be released under this license, and
 *    the source code of any modifications must be made available on a
 *    publically accessible (and locateable) website, or sent to the
 *    original author of this software.
 *
 * 3. Software modifications that do not alter the functionality of the
 *    software but are simply adaptations to a specific environment are
 *    exempt from clause 2.
 */

package uk.org.rivernile.android.bustracker.database.busstop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import uk.org.rivernile.android.fetchutils.fetchers.AssetFileFetcher;
import uk.org.rivernile.android.fetchutils.fetchers.readers.FileWriterFetcherStreamReader;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * Tests for {@link BusStopOpenHelper}.
 *
 * @author Niall Scott
 */
@RunWith(AndroidJUnit4.class)
public class BusStopOpenHelperTests {

    private DatabaseRenamingContext context;
    private BusStopOpenHelper helper;
    private long assetDbVersion;

    @Before
    public void setUp() {
        context = new DatabaseRenamingContext(InstrumentationRegistry.getTargetContext(), "test_");
        // Delete the db if it exists here incase a previous one is left over from a failed test.
        context.deleteDatabase(BusStopContract.DB_NAME);
        assetDbVersion = Long.parseLong(
                InstrumentationRegistry.getTargetContext().getString(R.string.asset_db_version));
        helper = new BusStopOpenHelper(context, assetDbVersion);
    }

    @After
    public void tearDown() {
        helper.close();
        context.deleteDatabase(BusStopContract.DB_NAME);
        context = null;
        helper = null;
    }

    /**
     * Test that calling {@link BusStopOpenHelper#getWritableDatabase()} throws an
     * {@link UnsupportedOperationException} as the bus stop database is read-only.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetWritableDatabaseIsNoOp() {
        helper.getWritableDatabase();
    }

    /**
     * Given that no database file exists, test that the bundled database is successfully extracted
     * from the assets and put in the correct location. Also perform some sanity checks on the
     * database.
     */
    @Test
    public void testDatabaseExtractedFromAssetsSuccessfully() {
        final File databaseFile = context.getDatabasePath(BusStopContract.DB_NAME);
        assertFalse(databaseFile.exists());

        final SQLiteDatabase db = helper.getReadableDatabase();
        assertTrue(databaseFile.exists());

        final Cursor correctVersionCursor = db.query(BusStopContract.DatabaseInformation.TABLE_NAME,
                new String[] { BusStopContract.DatabaseInformation.LAST_UPDATE_TIMESTAMP },
                null, null, null, null, null);
        assertEquals(1, correctVersionCursor.getCount());
        assertTrue(correctVersionCursor.moveToFirst());
        assertEquals(assetDbVersion,
                correctVersionCursor.getLong(correctVersionCursor.getColumnIndex(
                        BusStopContract.DatabaseInformation.LAST_UPDATE_TIMESTAMP)));
        correctVersionCursor.close();

        final Cursor servicesCursor = db.query("service",
                new String[] { BusStopContract.Services._ID }, null, null, null, null, null);
        assertTrue(servicesCursor.getCount() > 0);
        servicesCursor.close();

        final Cursor busStopsCursor = db.query("bus_stops",
                new String[] { BusStopContract.BusStops._ID }, null, null, null, null, null);
        assertTrue(busStopsCursor.getCount() > 0);
        busStopsCursor.close();

        final Cursor serviceStopsCursor = db.query(BusStopContract.ServiceStops.TABLE_NAME,
                new String[] { BusStopContract.ServiceStops._ID }, null, null, null, null, null);
        assertTrue(serviceStopsCursor.getCount() > 0);
        serviceStopsCursor.close();

        final Cursor servicePointsCursor = db.query("service_point",
                new String[] { BusStopContract.ServicePoints._ID }, null, null, null, null, null);
        assertTrue(servicePointsCursor.getCount() > 0);
        servicePointsCursor.close();

        final Cursor indexListCursor = db.rawQuery("PRAGMA index_list(service_point);", null);
        assertEquals(1, indexListCursor.getCount());
        assertTrue(indexListCursor.moveToFirst());
        assertEquals("service_point_index",
                indexListCursor.getString(indexListCursor.getColumnIndex("name")));
        indexListCursor.close();

        final Cursor indexInfoCursor = db.rawQuery("PRAGMA index_info(service_point_index);", null);
        assertEquals(3, indexInfoCursor.getCount());
        assertTrue(indexInfoCursor.moveToFirst());
        assertEquals("service_id",
                indexInfoCursor.getString(indexInfoCursor.getColumnIndex("name")));
        assertTrue(indexInfoCursor.moveToNext());
        assertEquals("chainage",
                indexInfoCursor.getString(indexInfoCursor.getColumnIndex("name")));
        assertTrue(indexInfoCursor.moveToNext());
        assertEquals("order_value",
                indexInfoCursor.getString(indexInfoCursor.getColumnIndex("name")));
        indexInfoCursor.close();

        final Cursor viewsCursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type = " +
                "'view' ORDER BY name ASC;", null);
        assertEquals(3, viewsCursor.getCount());
        assertTrue(viewsCursor.moveToFirst());
        assertEquals("view_bus_stops", viewsCursor.getString(viewsCursor.getColumnIndex("name")));
        assertTrue(viewsCursor.moveToNext());
        assertEquals("view_service_points",
                viewsCursor.getString(viewsCursor.getColumnIndex("name")));
        assertTrue(viewsCursor.moveToNext());
        assertEquals("view_services",
                viewsCursor.getString(viewsCursor.getColumnIndex("name")));
        viewsCursor.close();
    }

    /**
     * Test that when a newer database is available in the application assets, it is moved in to
     * replace what is already there.
     *
     * @throws IOException When there was a problem copying the database.
     */
    @Test
    public void testMoreRecentDatabaseAvailableInAssets() throws IOException {
        final File databaseFile = context.getDatabasePath(BusStopContract.DB_NAME);
        assertFalse(databaseFile.exists());

        copyTestDatabaseInToPlace("databases/busstops10-testupgrade.db");
        SQLiteDatabase db = context.openOrCreateDatabase(BusStopContract.DB_NAME, 0, null);
        final Cursor oldTsCursor = db.query(BusStopContract.DatabaseInformation.TABLE_NAME,
                new String[] { BusStopContract.DatabaseInformation.LAST_UPDATE_TIMESTAMP },
                null, null, null, null, null);
        assertTrue(oldTsCursor.moveToFirst());
        assertEquals(1, oldTsCursor.getLong(oldTsCursor.getColumnIndex(
                BusStopContract.DatabaseInformation.LAST_UPDATE_TIMESTAMP)));
        oldTsCursor.close();
        db.close();

        db = helper.getReadableDatabase();
        final Cursor correctVersionCursor = db.query(BusStopContract.DatabaseInformation.TABLE_NAME,
                new String[] { BusStopContract.DatabaseInformation.LAST_UPDATE_TIMESTAMP },
                null, null, null, null, null);
        assertTrue(correctVersionCursor.moveToFirst());
        assertEquals(assetDbVersion,
                correctVersionCursor.getLong(correctVersionCursor.getColumnIndex(
                        BusStopContract.DatabaseInformation.LAST_UPDATE_TIMESTAMP)));
        correctVersionCursor.close();
    }

    /**
     * Test that when the information table is empty, the database is assumed corrupt and is
     * replaced with the bundled database instead.
     *
     * @throws IOException When there was a problem copying the database.
     */
    @Test
    public void testDatabaseWithNoInformationGetsReplaced() throws IOException {
        final File databaseFile = context.getDatabasePath(BusStopContract.DB_NAME);
        assertFalse(databaseFile.exists());

        copyTestDatabaseInToPlace("databases/busstops10-testemptyinformation.db");
        SQLiteDatabase db = context.openOrCreateDatabase(BusStopContract.DB_NAME, 0, null);
        final Cursor oldTsCursor = db.query(BusStopContract.DatabaseInformation.TABLE_NAME,
                new String[] { BusStopContract.DatabaseInformation.LAST_UPDATE_TIMESTAMP },
                null, null, null, null, null);
        assertEquals(0, oldTsCursor.getCount());
        oldTsCursor.close();
        db.close();

        db = helper.getReadableDatabase();
        final Cursor correctVersionCursor = db.query(BusStopContract.DatabaseInformation.TABLE_NAME,
                new String[] { BusStopContract.DatabaseInformation.LAST_UPDATE_TIMESTAMP },
                null, null, null, null, null);
        assertTrue(correctVersionCursor.moveToFirst());
        assertEquals(assetDbVersion,
                correctVersionCursor.getLong(correctVersionCursor.getColumnIndex(
                        BusStopContract.DatabaseInformation.LAST_UPDATE_TIMESTAMP)));
        correctVersionCursor.close();
    }

    /**
     * Test that when the information table does not exist, the database is assumed corrupt and is
     * replaced with the bundled database instead.
     *
     * @throws IOException When there was a problem copying the database.
     */
    @Test
    public void testDatabaseWithNoInformationTableGetsReplaced() throws IOException {
        final File databaseFile = context.getDatabasePath(BusStopContract.DB_NAME);
        assertFalse(databaseFile.exists());

        copyTestDatabaseInToPlace("databases/busstops10-testnoinformation.db");
        SQLiteDatabase db = context.openOrCreateDatabase(BusStopContract.DB_NAME, 0, null);
        boolean exceptionThrown = false;

        try {
            db.query(BusStopContract.DatabaseInformation.TABLE_NAME,
                    new String[] { BusStopContract.DatabaseInformation.LAST_UPDATE_TIMESTAMP },
                    null, null, null, null, null);
        } catch (SQLiteException e) {
            exceptionThrown = true;
        }

        if (!exceptionThrown) {
            fail("SQLiteException was expected to be thrown but was not.");
            return;
        }

        db = helper.getReadableDatabase();
        final Cursor correctVersionCursor = db.query(BusStopContract.DatabaseInformation.TABLE_NAME,
                new String[] { BusStopContract.DatabaseInformation.LAST_UPDATE_TIMESTAMP },
                null, null, null, null, null);
        assertTrue(correctVersionCursor.moveToFirst());
        assertEquals(assetDbVersion,
                correctVersionCursor.getLong(correctVersionCursor.getColumnIndex(
                        BusStopContract.DatabaseInformation.LAST_UPDATE_TIMESTAMP)));
        correctVersionCursor.close();
    }

    /**
     * Test that if a newer version of the database already exists than what is bundled in the
     * application assets, don't replace the existing database with the asset version.
     *
     * @throws IOException When there was a problem copying the database.
     */
    @Test
    public void testMoreRecentDatabaseAlreadyInPlace() throws IOException {
        final File databaseFile = context.getDatabasePath(BusStopContract.DB_NAME);
        assertFalse(databaseFile.exists());

        copyTestDatabaseInToPlace("databases/busstops10-testnewerdbalreadyexists.db");
        SQLiteDatabase db = context.openOrCreateDatabase(BusStopContract.DB_NAME, 0, null);
        final Cursor newTsCursor = db.query(BusStopContract.DatabaseInformation.TABLE_NAME,
                new String[] { BusStopContract.DatabaseInformation.LAST_UPDATE_TIMESTAMP },
                null, null, null, null, null);
        assertTrue(newTsCursor.moveToFirst());
        assertTrue(newTsCursor.getLong(newTsCursor.getColumnIndex(
                BusStopContract.DatabaseInformation.LAST_UPDATE_TIMESTAMP)) > assetDbVersion);
        newTsCursor.close();
        db.close();

        db = helper.getReadableDatabase();
        final Cursor correctVersionCursor = db.query(BusStopContract.DatabaseInformation.TABLE_NAME,
                new String[] { BusStopContract.DatabaseInformation.LAST_UPDATE_TIMESTAMP },
                null, null, null, null, null);
        assertTrue(correctVersionCursor.moveToFirst());
        assertTrue(correctVersionCursor.getLong(correctVersionCursor.getColumnIndex(
                BusStopContract.DatabaseInformation.LAST_UPDATE_TIMESTAMP)) > assetDbVersion);
        correctVersionCursor.close();
    }

    /**
     * Copy a test database instance in to the database location.
     *
     * @param databaseName The location of the test database within the test package assets.
     * @throws IOException When there was a problem copying the database.
     */
    private void copyTestDatabaseInToPlace(@NonNull final String databaseName) throws IOException {
        final AssetFileFetcher fetcher = new AssetFileFetcher(InstrumentationRegistry.getContext(),
                databaseName);
        final FileWriterFetcherStreamReader reader = new FileWriterFetcherStreamReader(
                context.getDatabasePath(BusStopContract.DB_NAME), false);
        fetcher.executeFetcher(reader);
    }
}
