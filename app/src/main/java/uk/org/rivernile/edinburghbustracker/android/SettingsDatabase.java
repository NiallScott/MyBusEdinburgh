/*
 * Copyright (C) 2009 - 2016 Niall 'Rivernile' Scott
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

import android.content.Context;
import android.database.Cursor;
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

    /** The name of the file on disk for this database. */
    protected static final String SETTINGS_DB_NAME = "settings.db";
    private static final int SETTINGS_DB_VERSION = 2;

    private static final String FAVOURITE_STOPS_TABLE = "favourite_stops";
    private static final String FAVOURITE_STOPS_STOPCODE = "_id";
    public static final String FAVOURITE_STOPS_STOPNAME = "stopName";
    
    private static final String ALERTS_TABLE = "active_alerts";
    private static final String ALERTS_ID = "_id";
    private static final String ALERTS_TYPE = "type";
    private static final String ALERTS_TIME_ADDED = "timeAdded";
    private static final String ALERTS_STOPCODE = "stopCode";
    private static final String ALERTS_DISTANCE_FROM = "distanceFrom";
    private static final String ALERTS_SERVICE_NAMES = "serviceNames";
    private static final String ALERTS_TIME_TRIGGER = "timeTrigger";
    
    /** A proximity alert type. */
    public static final byte ALERTS_TYPE_PROXIMITY = 1;
    /** A time alert type. */
    public static final byte ALERTS_TYPE_TIME = 2;

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

    /**
     * Get the singleton instance of this class.
     * 
     * @param context The application context.
     * @return A reference to the singleton instance of this class.
     */
    public static SettingsDatabase getInstance(final Context context) {
        if(instance == null) instance = new SettingsDatabase(context);
        return instance;
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
        db.execSQL("CREATE TABLE " + ALERTS_TABLE + " (" +
                ALERTS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ALERTS_TYPE + " NUMERIC NOT NULL," +
                ALERTS_TIME_ADDED + " INTEGER NOT NULL," +
                ALERTS_STOPCODE + " TEXT NOT NULL," +
                ALERTS_DISTANCE_FROM + " INTEGER," +
                ALERTS_SERVICE_NAMES + " TEXT," +
                ALERTS_TIME_TRIGGER + " INTEGER);");
    }

    /**
     * An upgrade of the database, an abstract method in the super class.
     *
     * @param db The database object to interface with.
     * @param oldVersion The version of the old database.
     * @param newVersion The version of the new database.
     */
    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
            final int newVersion) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + ALERTS_TABLE + " (" +
                ALERTS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ALERTS_TYPE + " NUMERIC NOT NULL," +
                ALERTS_TIME_ADDED + " INTEGER NOT NULL," +
                ALERTS_STOPCODE + " TEXT NOT NULL," +
                ALERTS_DISTANCE_FROM + " INTEGER," +
                ALERTS_SERVICE_NAMES + " TEXT," +
                ALERTS_TIME_TRIGGER + " INTEGER);");
    }
    
    /**
     * {@inheritDoc} 
     */
    @Override
    public void onOpen(final SQLiteDatabase db) {
        cleanupAlerts(db);
    }

    /**
     * Get a Cursor to all the favourite stop items.
     *
     * @return A Cursor object to all the favourite stop items.
     */
    public Cursor getAllFavouriteStops() {
        final SQLiteDatabase db = getReadableDatabase();
        return db.query(true, FAVOURITE_STOPS_TABLE,
                new String[] { FAVOURITE_STOPS_STOPCODE,
                FAVOURITE_STOPS_STOPNAME },
                null, null, null, null, FAVOURITE_STOPS_STOPNAME + " ASC",
                null);
    }

    /**
     * This method checks to see if a favourite stop already exists in the
     * database.
     *
     * @param stopCode The stopCode to check for.
     * @return True if it already exists, false if it doesn't.
     */
    public boolean getFavouriteStopExists(final String stopCode) {
        if(stopCode == null || stopCode.length() == 0) return false;
        
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor c = db.query(FAVOURITE_STOPS_TABLE,
                new String[] { FAVOURITE_STOPS_STOPCODE },
                FAVOURITE_STOPS_STOPCODE + " = ?", new String[] { stopCode },
                null, null, null);
        if(c.getCount() > 0) {
            c.close();
            return true;
        }
        
        c.close();
        return false;
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

        final SQLiteDatabase db = getReadableDatabase();
        final Cursor c = db.query(FAVOURITE_STOPS_TABLE,
                new String[] { FAVOURITE_STOPS_STOPNAME },
                FAVOURITE_STOPS_STOPCODE + " = ?", new String[] { stopCode },
                null, null, null);
        String result = null;
        if(c.moveToFirst()) {
            result = c.getString(0);
        }
        
        c.close();
        return result;
    }
    
    /**
     * Check to see if a proximity alert already exists for the given stopCode.
     * 
     * @param stopCode The stopCode to check against.
     * @return True if an alert exists, false if it doesn't.
     */
    public boolean isActiveProximityAlert(final String stopCode) {
        return isActiveAlertOfType(stopCode, ALERTS_TYPE_PROXIMITY);
    }
    
    /**
     * Check to see if a time alert already exists for the given stopCode.
     * 
     * @param stopCode The stopCode to check against.
     * @return True if the alert exists, false if it doesn't.
     */
    public boolean isActiveTimeAlert(final String stopCode) {
        return isActiveAlertOfType(stopCode, ALERTS_TYPE_TIME);
    }
    
    /**
     * Check to see if an alert of a particular type exists at the given
     * stopCode. The type is either
     * {@link SettingsDatabase#ALERTS_TYPE_PROXIMITY} or
     * {@link SettingsDatabase#ALERTS_TYPE_TIME}
     * 
     * @param stopCode The stopCode to check against.
     * @param type The type of alert. See this method's description.
     * @return True if the alert exists, false if it doesn't.
     * @see SettingsDatabase#ALERTS_TYPE_PROXIMITY
     * @see SettingsDatabase#ALERTS_TYPE_TIME
     */
    public boolean isActiveAlertOfType(final String stopCode, final int type) {
        if(stopCode == null || stopCode.length() == 0) return false;
        final SQLiteDatabase db = getWritableDatabase();
        cleanupAlerts(db);
        final Cursor c = db.query(ALERTS_TABLE,
                new String[] { ALERTS_STOPCODE },
                ALERTS_STOPCODE + " = ? AND " + ALERTS_TYPE + " = ?",
                new String[] { stopCode, String.valueOf(type)}, null, null,
                null);
        if(c.getCount() > 0) {
            c.close();
            return true;
        }
        
        c.close();
        return false;
    }
    
    /**
     * Clean up alerts. This removes any alerts which are older than 1 hour.
     * 
     * @param db The SQLiteDatabase on which we are operating.
     */
    public void cleanupAlerts(final SQLiteDatabase db) {
        if(!db.isReadOnly()) {
            db.delete(ALERTS_TABLE, ALERTS_TIME_ADDED + " < ?",
                    new String[] {
                        String.valueOf(System.currentTimeMillis() - 3600000)
                    });
        }
    }
}