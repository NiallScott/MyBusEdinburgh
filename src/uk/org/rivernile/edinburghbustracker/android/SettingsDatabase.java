/*
 * Copyright (C) 2009 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This class deals with the database interaction with the settings database.
 * Currently the settings database only holds information for favourite bus
 * stops.
 *
 * @author Niall Scott
 */
public class SettingsDatabase extends SQLiteOpenHelper {

    protected final static String SETTINGS_DB_NAME = "settings.db";
    protected final static int SETTINGS_DB_VERSION = 1;

    protected final static String FAVOURITE_STOPS_TABLE = "favourite_stops";
    protected final static String FAVOURITE_STOPS_STOPCODE = "_id";
    protected final static String FAVOURITE_STOPS_STOPNAME = "stopName";

    private static SettingsDatabase instance;

    /**
     * Creates a new SettingsDatabase object. If the DB does not already exist,
     * it will be created.
     *
     * @param context The activity context.
     */
    private SettingsDatabase(final Context context) {
        super(context, SETTINGS_DB_NAME, null, SETTINGS_DB_VERSION);
    }

    public static SettingsDatabase getInstance(final Context context) {
        if(instance == null) instance = new SettingsDatabase(context);
        return instance;
    }

    @Override
    protected void finalize() {
        getReadableDatabase().close();
    }

    /**
     * This is called when the DB does not exist.
     *
     * @param db The database object to interface with.
     */
    @Override
    public void onCreate(final SQLiteDatabase db) {
        // This is what happens when the application cannot find a database
        // with the database name.
        db.execSQL("CREATE TABLE " + FAVOURITE_STOPS_TABLE + " (" +
                FAVOURITE_STOPS_STOPCODE + " TEXT PRIMARY KEY," +
                FAVOURITE_STOPS_STOPNAME + " TEXT NOT NULL);");
    }

    /**
     * An upgrade of the database, an abstract method in the super class. This
     * method, as of yet, does nothing.
     *
     * @param db The database object to interface with.
     * @param oldVersion The version of the old database.
     * @param newVersion The version of the new database.
     */
    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
            final int newVersion)
    {
        // Nothing to upgrade yet.
    }

    /**
     * Get a Cursor to all the favourite stop items.
     *
     * @param context The activity context.
     * @return A Cursor object to all the favourite stop items.
     */
    public Cursor getAllFavouriteStops() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(true,
                FAVOURITE_STOPS_TABLE,
                new String[] { FAVOURITE_STOPS_STOPCODE,
                FAVOURITE_STOPS_STOPNAME },
                null, null, null, null, FAVOURITE_STOPS_STOPNAME + " ASC",
                null);
        return c;
    }

    /**
     * This method checks to see if a favourite stop already exists in the
     * database.
     *
     * @param context The activity context.
     * @param stopCode The stopCode to check for.
     * @return True if it already exists, false if it doesn't.
     */
    public boolean getFavouriteStopExists(final String stopCode)
    {
        if(stopCode == null || stopCode.length() == 0) return false;
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(FAVOURITE_STOPS_TABLE,
                new String[] { FAVOURITE_STOPS_STOPCODE },
                FAVOURITE_STOPS_STOPCODE + " = " + stopCode,
                null, null, null, null);
        if(c.getCount() > 0) {
            c.close();
            return true;
        }
        c.close();
        return false;
    }

    /**
     * Insert a new favourite stop in to the database.
     *
     * @param context The activity context.
     * @param stopCode The stop code to insert.
     * @param stopName The stop name to insert.
     * @throws SQLException If an error occurs whilst writing to the database.
     */
    public void insertFavouriteStop(final String stopCode,
            final String stopName) throws SQLException
    {
        if(stopCode == null || stopName == null || stopCode.length() == 0
                || stopName.length() == 0) return;
        if(getFavouriteStopExists(stopCode)) return;
        ContentValues cv = new ContentValues();
        cv.put(FAVOURITE_STOPS_STOPCODE, stopCode);
        cv.put(FAVOURITE_STOPS_STOPNAME, stopName);

        SQLiteDatabase db = getWritableDatabase();
        db.insertOrThrow(FAVOURITE_STOPS_TABLE, FAVOURITE_STOPS_STOPNAME, cv);
    }

    /**
     * Delete a favourite stop from the database.
     *
     * @param context The activity context.
     * @param stopCode The stop code to delete from the database.
     */
    public void deleteFavouriteStop(final String stopCode) {
        if(stopCode == null || stopCode.length() == 0) return;
        if(!getFavouriteStopExists(stopCode)) return;

        SQLiteDatabase db = getWritableDatabase();
        db.delete(FAVOURITE_STOPS_TABLE,
                FAVOURITE_STOPS_STOPCODE + " = " + stopCode, null);
    }

    /**
     * Modify the stopName string of an item in the database.
     *
     * @param stopCode The stop code of the item to modify.
     * @param stopName The new stop name.
     */
    public void modifyFavouriteStop(final String stopCode,
            final String stopName) {
        if(stopCode == null || stopName == null || stopCode.length() == 0
                || stopName.length() == 0) return;
        if(!getFavouriteStopExists(stopCode)) return;
        ContentValues cv = new ContentValues();
        cv.put(FAVOURITE_STOPS_STOPNAME, stopName);

        SQLiteDatabase db = getWritableDatabase();
        db.update(FAVOURITE_STOPS_TABLE, cv,
                FAVOURITE_STOPS_STOPCODE + " = " + stopCode, null);
    }

    /**
     * Get the name for a bus stop.
     *
     * @param stopCode The bus stop code.
     * @return The name for the bus stop.
     */
    public String getNameForStop(final String stopCode) {
        if(stopCode == null || stopCode.length() == 0
                || !getFavouriteStopExists(stopCode)) return null;

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(FAVOURITE_STOPS_TABLE,
                new String[] { FAVOURITE_STOPS_STOPNAME },
                FAVOURITE_STOPS_STOPCODE + " = " + stopCode,
                null, null, null, null);
        c.moveToFirst();
        String s = c.getString(0);
        c.close();
        return s;
    }
}