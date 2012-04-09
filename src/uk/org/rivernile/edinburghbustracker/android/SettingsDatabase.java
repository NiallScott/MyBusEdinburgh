/*
 * Copyright (C) 2009 - 2012 Niall 'Rivernile' Scott
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
import android.os.Build;
import android.os.Environment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class deals with the database interaction with the settings database.
 * Currently the settings database only holds information for favourite bus
 * stops.
 *
 * @author Niall Scott
 */
public class SettingsDatabase extends SQLiteOpenHelper {

    protected final static String SETTINGS_DB_NAME = "settings.db";
    private final static int SETTINGS_DB_VERSION = 2;

    protected final static String FAVOURITE_STOPS_TABLE = "favourite_stops";
    protected final static String FAVOURITE_STOPS_STOPCODE = "_id";
    protected final static String FAVOURITE_STOPS_STOPNAME = "stopName";
    
    protected final static String ALERTS_TABLE = "active_alerts";
    protected final static String ALERTS_ID = "_id";
    protected final static String ALERTS_TYPE = "type";
    protected final static String ALERTS_TIME_ADDED = "timeAdded";
    protected final static String ALERTS_STOPCODE = "stopCode";
    protected final static String ALERTS_DISTANCE_FROM = "distanceFrom";
    protected final static String ALERTS_SERVICE_NAMES = "serviceNames";
    protected final static String ALERTS_TIME_TRIGGER = "timeTrigger";
    
    public static final byte ALERTS_TYPE_PROXIMITY = 1;
    public static final byte ALERTS_TYPE_TIME = 2;
    
    private static final boolean isFroyoOrGreater =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;

    private static SettingsDatabase instance;

    private Context context;

    /**
     * Creates a new SettingsDatabase object. If the DB does not already exist,
     * it will be created.
     *
     * @param context The activity context.
     */
    private SettingsDatabase(final Context context) {
        super(context, SETTINGS_DB_NAME, null, SETTINGS_DB_VERSION);
        this.context = context;
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
            final int newVersion)
    {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + ALERTS_TABLE + " (" +
                ALERTS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ALERTS_TYPE + " NUMERIC NOT NULL," +
                ALERTS_TIME_ADDED + " INTEGER NOT NULL," +
                ALERTS_STOPCODE + " TEXT NOT NULL," +
                ALERTS_DISTANCE_FROM + " INTEGER," +
                ALERTS_SERVICE_NAMES + " TEXT," +
                ALERTS_TIME_TRIGGER + " INTEGER);");
    }
    
    @Override
    public void onOpen(final SQLiteDatabase db) {
        cleanupAlerts(db);
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
        
        if(isFroyoOrGreater) MainActivity.BackupSupport
                .dataChanged(context.getPackageName());
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
        
        if(isFroyoOrGreater) MainActivity.BackupSupport
                .dataChanged(context.getPackageName());
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
        
        if(isFroyoOrGreater) MainActivity.BackupSupport
                .dataChanged(context.getPackageName());
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
    
    public void insertNewProximityAlert(final String stopCode,
            final int distance) {
        SQLiteDatabase db = getWritableDatabase();
        cleanupAlerts(db);
        
        ContentValues cv = new ContentValues();
        cv.put(ALERTS_TYPE, ALERTS_TYPE_PROXIMITY);
        cv.put(ALERTS_TIME_ADDED, System.currentTimeMillis());
        cv.put(ALERTS_STOPCODE, stopCode);
        cv.put(ALERTS_DISTANCE_FROM, distance);
        
        db.insertOrThrow(ALERTS_TABLE, ALERTS_DISTANCE_FROM, cv);
    }
    
    public void insertNewTimeAlert(final String stopCode,
            final String[] serviceNames, final int timeTrigger) {
        SQLiteDatabase db = getWritableDatabase();
        cleanupAlerts(db);
        
        int len = serviceNames.length;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < len; i++) {
            sb.append(serviceNames[i]);
            if(i != len-1) {
                sb.append(',');
            }
        }
        
        ContentValues cv = new ContentValues();
        cv.put(ALERTS_TYPE, ALERTS_TYPE_TIME);
        cv.put(ALERTS_TIME_ADDED, System.currentTimeMillis());
        cv.put(ALERTS_STOPCODE, stopCode);
        cv.put(ALERTS_SERVICE_NAMES, sb.toString());
        cv.put(ALERTS_TIME_TRIGGER, timeTrigger);
        
        db.insertOrThrow(ALERTS_TABLE, ALERTS_TIME_TRIGGER, cv);
    }
    
    public void deleteAllAlertsOfType(final byte type) {
        SQLiteDatabase db = getWritableDatabase();
        cleanupAlerts(db);
        db.delete(ALERTS_TABLE, ALERTS_TYPE + " = " + type, null);
    }
    
    public boolean isActiveProximityAlert(final String stopCode) {
        return isActiveAlertOfType(stopCode, ALERTS_TYPE_PROXIMITY);
    }
    
    public boolean isActiveTimeAlert(final String stopCode) {
        return isActiveAlertOfType(stopCode, ALERTS_TYPE_TIME);
    }
    
    public boolean isActiveAlertOfType(final String stopCode, final int type) {
        if(stopCode == null || stopCode.length() == 0) return false;
        SQLiteDatabase db = getWritableDatabase();
        cleanupAlerts(db);
        Cursor c = db.query(ALERTS_TABLE,
                new String[] { ALERTS_STOPCODE },
                ALERTS_STOPCODE + " = " + stopCode + " AND " + ALERTS_TYPE +
                " = " + type, null, null, null, null);
        if(c.getCount() > 0) {
            c.close();
            return true;
        }
        c.close();
        return false;
    }
    
    public Cursor getAllAlerts() {
        SQLiteDatabase db = getWritableDatabase();
        
        cleanupAlerts(db);
        return db.query(ALERTS_TABLE, null, null, null, null, null, null);
    }
    
    public void cleanupAlerts(SQLiteDatabase db) {
        if(!db.isReadOnly()) {
            long time = System.currentTimeMillis() - 3600000;
            db.delete(ALERTS_TABLE, ALERTS_TIME_ADDED + " < " +
                    String.valueOf(System.currentTimeMillis() - 3600000), null);
        }
    }
    
    public JSONObject backupDatabaseAsJSON() throws JSONException {
        JSONObject root = new JSONObject();
        JSONArray favStops = new JSONArray();
        JSONObject stop;
        
        root.put("dbVersion", SETTINGS_DB_VERSION);
        root.put("jsonSchemaVersion", 1);
        root.put("createTime", System.currentTimeMillis());
        root.put("favouriteStops", favStops);

        Cursor c = getAllFavouriteStops();
        while(c.moveToNext()) {
            stop = new JSONObject();
            stop.put("stopCode", c.getString(0));
            stop.put("stopName", c.getString(1));
            favStops.put(stop);
        }
        
        return root;
    }
    
    public void restoreDatabaseFromJSON(final String jsonString)
            throws JSONException {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(FAVOURITE_STOPS_TABLE, null, null);
        
        JSONObject root, stop;
        JSONArray favStops;
        
        root = new JSONObject(jsonString);
        favStops = root.getJSONArray("favouriteStops");
        int len = favStops.length();
        for(int i = 0; i < len; i++) {
            stop = favStops.getJSONObject(i);
            insertFavouriteStop(stop.getString("stopCode"),
                    stop.getString("stopName"));
        }
    }

    public String backupDatabase() {
        if(!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return context.getString(
                    R.string.preferences_backup_error_mediaerror);
        }
        
        File out = new File(Environment.getExternalStorageDirectory(),
                "/mybusedinburgh/");
        out.mkdirs();
        out = new File(out, "settings.backup");
        
        JSONObject root;
        try {
            root = backupDatabaseAsJSON();
            PrintWriter pw = new PrintWriter(new FileWriter(out));
            pw.println(root.toString());
            pw.flush();
            pw.close();
        } catch(JSONException e) {
            return context.getString(
                    R.string.preferences_backup_error_json_write);
        } catch(IOException e) {
            return context.getString(R.string.preferences_backup_error_ioerror);
        }
        
        return "success";
    }
    
    public String restoreDatabase() {
        if(!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return context.getString(
                    R.string.preferences_backup_error_mediaerror);
        }
        
        final File in = new File(Environment.getExternalStorageDirectory(),
                "/mybusedinburgh/settings.backup");
        if(!in.exists() || !in.canRead()) {
            return context.getString(R.string.preferences_backup_error_nofile);
        }
        
        StringBuilder jsonString = new StringBuilder();
        try {
            String str;
            BufferedReader reader = new BufferedReader(new FileReader(in));
            while((str = reader.readLine()) != null) {
                jsonString.append(str);
            }
            reader.close();
            
            restoreDatabaseFromJSON(jsonString.toString());
        } catch(IOException e) {
            return context.getString(R.string.preferences_backup_error_ioerror);
        } catch(JSONException e) {
            return context.getString(
                    R.string.preferences_backup_error_json_read);
        }
        
        return "success";
    }
}