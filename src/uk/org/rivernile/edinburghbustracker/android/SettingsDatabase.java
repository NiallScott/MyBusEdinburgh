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
import uk.org.rivernile.android.utils.BackupCompat;

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
    
    private static final String BACKUP_DB_VERSION = "dbVersion";
    private static final String BACKUP_SCHEMA_VERSION = "jsonSchemaVersion";
    private static final String BACKUP_CREATE_TIME = "createTime";
    private static final String BACKUP_FAVOURITE_STOPS = "favouriteStops";
    private static final String BACKUP_STOPCODE = "stopCode";
    private static final String BACKUP_STOPNAME = "stopName";
    private static final String BACKUP_DIRECTORY = "/mybusedinburgh/";
    private static final String BACKUP_FILE_NAME = "settings.backup";
    
    /** A proximity alert type. */
    public static final byte ALERTS_TYPE_PROXIMITY = 1;
    /** A time alert type. */
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
        SQLiteDatabase db = getReadableDatabase();
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
    public boolean getFavouriteStopExists(final String stopCode)
    {
        if(stopCode == null || stopCode.length() == 0) return false;
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(FAVOURITE_STOPS_TABLE,
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
     * Insert a new favourite stop in to the database.
     *
     * @param stopCode The stop code to insert.
     * @param stopName The stop name to insert.
     * @throws SQLException If an error occurs whilst writing to the database.
     */
    public void insertFavouriteStop(final String stopCode,
            final String stopName) throws SQLException {
        if(stopCode == null || stopName == null || stopCode.length() == 0
                || stopName.length() == 0) return;
        if(getFavouriteStopExists(stopCode)) return;
        ContentValues cv = new ContentValues();
        cv.put(FAVOURITE_STOPS_STOPCODE, stopCode);
        cv.put(FAVOURITE_STOPS_STOPNAME, stopName);

        SQLiteDatabase db = getWritableDatabase();
        db.insertOrThrow(FAVOURITE_STOPS_TABLE, FAVOURITE_STOPS_STOPNAME, cv);
        
        if(isFroyoOrGreater) BackupCompat.dataChanged(context.getPackageName());
    }

    /**
     * Delete a favourite stop from the database.
     *
     * @param stopCode The stop code to delete from the database.
     */
    public void deleteFavouriteStop(final String stopCode) {
        if(stopCode == null || stopCode.length() == 0) return;
        if(!getFavouriteStopExists(stopCode)) return;

        SQLiteDatabase db = getWritableDatabase();
        db.delete(FAVOURITE_STOPS_TABLE, FAVOURITE_STOPS_STOPCODE + " = ?",
                new String[] { stopCode });
        
        if(isFroyoOrGreater) BackupCompat.dataChanged(context.getPackageName());
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
        db.update(FAVOURITE_STOPS_TABLE, cv, FAVOURITE_STOPS_STOPCODE + " = ?",
                new String[] { stopCode });
        
        if(isFroyoOrGreater) BackupCompat.dataChanged(context.getPackageName());
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
     * Insert a new proximity alert in to the database. We have to store this
     * because it's not possible to get information on proximity alerts we have
     * set back from the platform.
     * 
     * @param stopCode The stopCode for which the proximity alert is set.
     * @param distance The distance at which to trigger the proximity alert.
     */
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
    
    /**
     * Insert a new time alert in to the database. We have to store this because
     * it's not possible to get alarm information from the platform after we
     * have set up an alarm.
     * 
     * @param stopCode The stopCode for which the time alert is set.
     * @param serviceNames The names of the services which should trigger this
     * alert.
     * @param timeTrigger The time at which to trigger the time alert.
     */
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
    
    /**
     * Delete an alert of a particular type. This can be either
     * {@link SettingsDatabase#ALERTS_TYPE_PROXIMITY} or
     * {@link SettingsDatabase#ALERTS_TYPE_TIME}.
     * 
     * @param type The type of alert to delete.
     * @see SettingsDatabase#ALERTS_TYPE_PROXIMITY
     * @see SettingsDatabase#ALERTS_TYPE_TIME
     */
    public void deleteAllAlertsOfType(final byte type) {
        SQLiteDatabase db = getWritableDatabase();
        cleanupAlerts(db);
        db.delete(ALERTS_TABLE, ALERTS_TYPE + " = ?",
                new String[] { String.valueOf(type) });
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
        SQLiteDatabase db = getWritableDatabase();
        cleanupAlerts(db);
        Cursor c = db.query(ALERTS_TABLE, new String[] { ALERTS_STOPCODE },
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
     * Return a Cursor object which contains all fields of an alert, for all
     * alerts in the database. It is perfectly possible that no alerts exist,
     * therefore an empty Cursor is returned.
     * 
     * @return A Cursor containing all alerts known in the database.
     */
    public Cursor getAllAlerts() {
        SQLiteDatabase db = getWritableDatabase();
        
        cleanupAlerts(db);
        return db.query(ALERTS_TABLE, null, null, null, null, null, null);
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
    
    /**
     * Return a dump of the list of favourite bus stops as a JSONObject, to be
     * later output to file.
     * 
     * @return A JSON object describing the favourite bus stops.
     * @throws JSONException When an error occurs whilst dealing with JSON.
     */
    public JSONObject backupDatabaseAsJSON() throws JSONException {
        final JSONObject root = new JSONObject();
        final JSONArray favStops = new JSONArray();
        JSONObject stop;
        
        root.put(BACKUP_DB_VERSION, SETTINGS_DB_VERSION);
        root.put(BACKUP_SCHEMA_VERSION, 1);
        root.put(BACKUP_CREATE_TIME, System.currentTimeMillis());
        root.put(BACKUP_FAVOURITE_STOPS, favStops);

        Cursor c = getAllFavouriteStops();
        while(c.moveToNext()) {
            stop = new JSONObject();
            stop.put(BACKUP_STOPCODE, c.getString(0));
            stop.put(BACKUP_STOPNAME, c.getString(1));
            favStops.put(stop);
        }
        
        return root;
    }
    
    /**
     * Restore a previous backup from JSON input and insert it in to the
     * database.
     * 
     * @param jsonString The JSON to be restored.
     * @throws JSONException When an error occurs whilst parsing the JSON text.
     */
    public void restoreDatabaseFromJSON(final String jsonString)
            throws JSONException {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(FAVOURITE_STOPS_TABLE, null, null);
        
        final JSONObject root = new JSONObject(jsonString);
        final JSONArray favStops = root.getJSONArray(BACKUP_FAVOURITE_STOPS);
        JSONObject stop;
        
        int len = favStops.length();
        for(int i = 0; i < len; i++) {
            stop = favStops.getJSONObject(i);
            insertFavouriteStop(stop.getString(BACKUP_STOPCODE),
                    stop.getString(BACKUP_STOPNAME));
        }
    }

    /**
     * Backup the JSON dump of the database to a file on external storage.
     * 
     * @return A success or failure string.
     */
    public String backupDatabase() {
        if(!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return context.getString(
                    R.string.preferences_backup_error_mediaerror);
        }
        
        File out = new File(Environment.getExternalStorageDirectory(),
                BACKUP_DIRECTORY);
        out.mkdirs();
        out = new File(out, BACKUP_FILE_NAME);
        
        try {
            final JSONObject root = backupDatabaseAsJSON();
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
    
    /**
     * Restore a previous JSON dump of the database from a file on external
     * storage.
     * 
     * @return A success or failure string.
     */
    public String restoreDatabase() {
        if(!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return context.getString(
                    R.string.preferences_backup_error_mediaerror);
        }
        
        final File in = new File(Environment.getExternalStorageDirectory(),
                BACKUP_DIRECTORY + BACKUP_FILE_NAME);
        if(!in.exists() || !in.canRead()) {
            return context.getString(R.string.preferences_backup_error_nofile);
        }
        
        final StringBuilder jsonString = new StringBuilder();
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