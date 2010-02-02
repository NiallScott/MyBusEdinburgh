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
public class SettingsDatabase {

    protected final static String SETTINGS_DB_NAME = "settings.db";
    protected final static int SETTINGS_DB_VERSION = 1;

    protected final static String FAVOURITE_STOPS_TABLE = "favourite_stops";
    public final static String FAVOURITE_STOPS_STOPCODE = "_id";
    public final static String FAVOURITE_STOPS_STOPNAME = "stopName";

    private static class OpenHelper extends SQLiteOpenHelper {

        public OpenHelper(final Context context) {
            super(context, SETTINGS_DB_NAME, null, SETTINGS_DB_VERSION);
        }

        @Override
        public void onCreate(final SQLiteDatabase db) {
            // This is what happens when the application cannot find a database
            // with the database name.
            db.execSQL("CREATE TABLE " + FAVOURITE_STOPS_TABLE + " (" +
                    FAVOURITE_STOPS_STOPCODE + " TEXT PRIMARY KEY," +
                    FAVOURITE_STOPS_STOPNAME + " TEXT NOT NULL);");
        }

        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
                final int newVersion)
        {
            // Nothing to upgrade yet.
        }
    }

    /**
     * Get a Cursor to all the favourite stop items.
     *
     * @param context The activity context.
     * @return A Cursor object to all the favourite stop items.
     */
    public static Cursor getAllFavouriteStops(final Context context) {
        SQLiteDatabase db = new OpenHelper(context).getReadableDatabase();
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
    public static boolean getFavouriteStopExists(final Context context,
            final String stopCode)
    {
        if(stopCode == null || stopCode.length() == 0) return false;
        SQLiteDatabase db = new OpenHelper(context).getReadableDatabase();
        Cursor c = db.query(FAVOURITE_STOPS_TABLE,
                new String[] { FAVOURITE_STOPS_STOPCODE },
                FAVOURITE_STOPS_STOPCODE + " = " + stopCode,
                null, null, null, null);
        if(c.getCount() > 0) {
            db.close();
            return true;
        }
        db.close();
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
    public static void insertFavouriteStop(final Context context,
            final String stopCode, final String stopName) throws SQLException
    {
        if(stopCode == null || stopName == null || stopCode.length() == 0
                || stopName.length() == 0) return;
        if(getFavouriteStopExists(context, stopCode)) return;
        ContentValues cv = new ContentValues();
        cv.put(FAVOURITE_STOPS_STOPCODE, stopCode);
        cv.put(FAVOURITE_STOPS_STOPNAME, stopName);

        SQLiteDatabase db = new OpenHelper(context).getWritableDatabase();
        db.insertOrThrow(FAVOURITE_STOPS_TABLE, FAVOURITE_STOPS_STOPNAME, cv);
        db.close();
    }

    /**
     * Delete a favourite stop from the database.
     *
     * @param context The activity context.
     * @param stopCode The stop code to delete from the database.
     */
    public static void deleteFavouriteStop(final Context context,
            final String stopCode)
    {
        if(stopCode == null || stopCode.length() == 0) return;
        if(!getFavouriteStopExists(context, stopCode)) return;

        SQLiteDatabase db = new OpenHelper(context).getWritableDatabase();
        db.delete(FAVOURITE_STOPS_TABLE,
                FAVOURITE_STOPS_STOPCODE + " = " + stopCode, null);
        db.close();
    }

    /**
     * Modify the stopName string of an item in the database.
     *
     * @param context The activity context.
     * @param stopCode The stop code of the item to modify.
     * @param stopName The new stop name.
     */
    public static void modifyFavouriteStop(final Context context,
            final String stopCode, final String stopName)
    {
        if(stopCode == null || stopName == null || stopCode.length() == 0
                || stopName.length() == 0) return;
        if(!getFavouriteStopExists(context, stopCode)) return;
        ContentValues cv = new ContentValues();
        cv.put(FAVOURITE_STOPS_STOPNAME, stopName);

        SQLiteDatabase db = new OpenHelper(context).getWritableDatabase();
        db.update(FAVOURITE_STOPS_TABLE, cv,
                FAVOURITE_STOPS_STOPCODE + " = " + stopCode, null);
        db.close();
    }

    public static String getNameForStop(final Context context,
            final String stopCode)
    {
        if(stopCode == null || stopCode.length() == 0
                || !getFavouriteStopExists(context, stopCode)) return null;

        SQLiteDatabase db = new OpenHelper(context).getReadableDatabase();
        Cursor c = db.query(FAVOURITE_STOPS_TABLE,
                new String[] { FAVOURITE_STOPS_STOPNAME },
                FAVOURITE_STOPS_STOPCODE + " = " + stopCode,
                null, null, null, null);
        c.moveToFirst();
        String s = c.getString(0);
        db.close();
        return s;
    }
}