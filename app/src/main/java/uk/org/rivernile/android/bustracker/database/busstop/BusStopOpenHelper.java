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

package uk.org.rivernile.android.bustracker.database.busstop;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;

import uk.org.rivernile.android.fetchutils.fetchers.AssetFileFetcher;
import uk.org.rivernile.android.fetchutils.fetchers.readers.FileWriterFetcherStreamReader;

/**
 * This class is used to open up the bus stop database. If the database does not already exist, or
 * the existing database appears to be corrupted, then this class will attempt to place the bundled
 * database in to the database location.
 *
 * <p>
 *     This class can also be used to replace the bus stop database with an updated version.
 *     However, this is only intended to be used from {@link BusStopProvider}.
 * </p>
 *
 * <p>
 *     Also of note is that {@link #getWritableDatabase()} is a no-op and will throw an
 *     {@link UnsupportedOperationException}. The bus stop database cannot be written to.
 * </p>
 *
 * @author Niall Scott
 */
class BusStopOpenHelper extends SQLiteOpenHelper {

    private final Context context;
    private final long minimumLastModTime;
    private final File databaseFile;
    private boolean ensureDatabaseCalled;

    /**
     * Create the {@code BusStopOpenHelper} to open the bus stop database.
     *
     * @param context The {@link Context} object for the application.
     * @param minimumLastModTime The minimum age the database must be to be suitable for this
     * version of the application.
     */
    BusStopOpenHelper(@NonNull final Context context, final long minimumLastModTime) {
        super(context, BusStopContract.DB_NAME, null, BusStopContract.DB_VERSION);

        this.context = context;
        this.minimumLastModTime = minimumLastModTime;
        databaseFile = context.getDatabasePath(BusStopContract.DB_NAME);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        // This database is always created off-device, and as such, no schema set-up is required
        // here.
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        // This database is always created off-device, and as such, no upgrades will ever happen
        // here.
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        ensureDatabase();
        return super.getReadableDatabase();
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        throw new UnsupportedOperationException("The bus stop database is read-only.");
    }

    /**
     * Ensure that the bus stop database exists, and perform a few tests on it to make sure it's not
     * corrupt. If necessary, the bus stop database bundled with the application will be extracted
     * from the application assets in to a temporary location.
     */
    private synchronized void ensureDatabase() {
        if (!ensureDatabaseCalled) {
            ensureDatabaseCalled = true;

            if (!databaseFile.exists()) {
                // The database does not exist yet, so extract the bundled database to the database
                // location.
                extractDatabaseFromAssets();
            } else {
                Cursor c = null;
                boolean shouldExtractDatabase = false;

                try {
                    c = getReadableDatabase().query(BusStopContract.DatabaseInformation.TABLE_NAME,
                            new String[] {
                                    BusStopContract.DatabaseInformation.LAST_UPDATE_TIMESTAMP
                            }, null, null, null, null, null);
                } catch (SQLiteException ignored) {
                    // If an Exception occurs, the Cursor will be null so a database extraction will
                    // happen.
                }

                // What follows is some extremely defensive programming. If any of it fails, it is
                // assumed the database is corrupt and needs to be re-extracted.
                if (c != null) {
                    if (c.moveToFirst()) {
                        final int tsColumnIndex = c.getColumnIndex(
                                BusStopContract.DatabaseInformation.LAST_UPDATE_TIMESTAMP);

                        if (tsColumnIndex >= 0) {
                            // Make sure we're not running a database from before this release of
                            // the app.
                            if (c.getLong(tsColumnIndex) < minimumLastModTime) {
                                shouldExtractDatabase = true;
                            }
                        } else {
                            shouldExtractDatabase = true;
                        }
                    } else {
                        shouldExtractDatabase = true;
                    }

                    c.close();
                } else {
                    shouldExtractDatabase = true;
                }

                if (shouldExtractDatabase) {
                    extractDatabaseFromAssets();
                }
            }
        }
    }

    /**
     * Extract a copy of the bus stop database from the application assets in to a temporary file in
     * the database directory. Once this extraction has been done and has been deemed successful,
     * the database will be swapped out with the current one (if it exists).
     */
    private void extractDatabaseFromAssets() {
        final File outFile = context.getDatabasePath(BusStopContract.DB_NAME + "_temp");

        try {
            final AssetFileFetcher fetcher = new AssetFileFetcher(context, BusStopContract.DB_NAME);
            final FileWriterFetcherStreamReader reader = new FileWriterFetcherStreamReader(outFile,
                    false);
            fetcher.executeFetcher(reader);
        } catch (IOException e) {
            throw new IllegalStateException("Bus stop database could not be extracted.", e);
        }

        replaceDatabase(outFile);
    }

    /**
     * Replace the bus stop database with the database pointed to by the specified {@link File}.
     * This method may be used by {@link BusStopProvider} to update the database to a new version.
     *
     * @param newDatabaseFile The {@link File} to replace the bus stop database with.
     */
    synchronized void replaceDatabase(@NonNull final File newDatabaseFile) {
        SQLiteDatabase db = null;

        try {
            // Attempt to set up the indexes on the new database file. If we can't (for example, no
            // disk space) then that's just too bad. The user will need to put up with slow
            // performance when loading route lines until the next time we replace the database and
            // at that point we try again.
            db = SQLiteDatabase.openDatabase(newDatabaseFile.getAbsolutePath(), null,
                    SQLiteDatabase.OPEN_READWRITE);
            setUpIndexes(db);
            setUpViews(db);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        close();
        // We should always clean up after ourselves.
        context.deleteDatabase(BusStopContract.DB_NAME);
        new File(newDatabaseFile.getAbsolutePath() + "-journal").delete();

        if (!newDatabaseFile.renameTo(databaseFile)) {
            throw new IllegalStateException("Unable to rename temporary database in to permanent " +
                    "position.");
        }
    }

    /**
     * Set up the indexes on the database to increase performance. Creating an index will cause the
     * database to increase in size.
     *
     * @param db The database to create the indexes on.
     */
    private static void setUpIndexes(@NonNull final SQLiteDatabase db) {
        db.beginTransaction();

        try {
            db.execSQL("CREATE INDEX IF NOT EXISTS service_point_index ON service_point " +
                    "(service_id, " + BusStopContract.ServicePoints.CHAINAGE + ", " +
                    BusStopContract.ServicePoints.ORDER_VALUE + ")");
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Set up the views on the database to make interfacing with the database from the provider
     * much easier.
     *
     * @param db The database to create the views on.
     */
    private static void setUpViews(@NonNull final SQLiteDatabase db) {
        setUpServicesView(db);
        setUpBusStopsView(db);
        setUpServicePointsView(db);
    }

    /**
     * Set up a view on the database to make interacting with the services table easier.
     *
     * @param db The database to create the view on.
     */
    private static void setUpServicesView(@NonNull final SQLiteDatabase db) {
        db.execSQL("CREATE VIEW IF NOT EXISTS " + BusStopContract.Services.TABLE_NAME +
                " AS SELECT service._id AS " + BusStopContract.Services._ID + ", " +
                BusStopContract.Services.NAME + ", " + BusStopContract.Services.DESCRIPTION + ", " +
                BusStopContract.Services.COLOUR + " FROM service LEFT JOIN service_colour ON " +
                "service._id = service_colour._id");
    }

    /**
     * Set up a view on the database to make interacting with the bus stops table easier.
     *
     * @param db The database to create the view on.
     */
    private static void setUpBusStopsView(@NonNull final SQLiteDatabase db) {
        db.execSQL("CREATE VIEW IF NOT EXISTS " + BusStopContract.BusStops.TABLE_NAME +
                " AS SELECT " + BusStopContract.BusStops._ID + ", " +
                BusStopContract.BusStops.STOP_CODE + ", " + BusStopContract.BusStops.STOP_NAME +
                ", " + BusStopContract.BusStops.LATITUDE + ", " +
                BusStopContract.BusStops.LONGITUDE + ", " + BusStopContract.BusStops.ORIENTATION +
                ", " + BusStopContract.BusStops.LOCALITY + ", " +
                "(SELECT group_concat(" + BusStopContract.ServiceStops.SERVICE_NAME + ", ', ') " +
                "FROM (SELECT " + BusStopContract.ServiceStops.STOP_CODE + ", " +
                BusStopContract.ServiceStops.SERVICE_NAME + " FROM " +
                BusStopContract.ServiceStops.TABLE_NAME + " WHERE bus_stops.stopCode = " +
                BusStopContract.ServiceStops.TABLE_NAME + '.' +
                BusStopContract.ServiceStops.STOP_CODE + " ORDER BY CASE WHEN " +
                BusStopContract.ServiceStops.SERVICE_NAME + " GLOB '[^0-9.]*' THEN " +
                BusStopContract.ServiceStops.SERVICE_NAME + " ELSE cast(" +
                BusStopContract.ServiceStops.SERVICE_NAME + " AS int) END) GROUP BY " +
                BusStopContract.ServiceStops.STOP_CODE + ") AS " +
                BusStopContract.BusStops.SERVICE_LISTING + " FROM bus_stops");
    }

    /**
     * Set up a view on the database to make interacting with the service points table easier.
     *
     * @param db The database to create the view on.
     */
    private static void setUpServicePointsView(@NonNull final SQLiteDatabase db) {
        db.execSQL("CREATE VIEW IF NOT EXISTS " + BusStopContract.ServicePoints.TABLE_NAME +
                " AS SELECT service_point._id AS " + BusStopContract.ServicePoints._ID +
                ", service.name AS " + BusStopContract.ServicePoints.SERVICE_NAME + ", " +
                "bus_stops.stopCode AS " + BusStopContract.ServicePoints.STOP_CODE +
                ", " + BusStopContract.ServicePoints.ORDER_VALUE + ", " +
                BusStopContract.ServicePoints.CHAINAGE + ", " +
                BusStopContract.ServicePoints.LATITUDE + ", " +
                BusStopContract.ServicePoints.LONGITUDE +
                " FROM service_point LEFT JOIN service ON service_id = service._id LEFT JOIN " +
                "bus_stops ON stop_id = bus_stops._id");
    }
}
