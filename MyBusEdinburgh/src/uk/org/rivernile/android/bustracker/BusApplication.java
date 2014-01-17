/*
 * Copyright (C) 2009 - 2014 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker;

import android.app.Application;
import android.app.backup.BackupManager;
import static uk.org.rivernile.edinburghbustracker.android.PreferencesActivity
        .PREF_DATABASE_AUTO_UPDATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Looper;
import android.widget.Toast;
import com.bugsense.trace.BugSenseHandler;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;
import uk.org.rivernile.android.utils.FileUtils;
import uk.org.rivernile.edinburghbustracker.android.ApiKey;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;
import uk.org.rivernile.edinburghbustracker.android.PreferencesActivity;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.edinburghbustracker.android.SettingsDatabase;
import uk.org.rivernile.edinburghbustracker.android.endpoints
        .BusTrackerEndpoint;
import uk.org.rivernile.edinburghbustracker.android.utils.UrlBuilder;

/**
 * This code is the very first code that will be executed when the application
 * is started. It is used to register the BugSense handler, put a listener on
 * the SharedPreferences for Google Backup, and check for bus stop database
 * updates.
 * 
 * The Android developer documentation discourages the usage of this class, but
 * as it is unpredictable where the user will enter the application the code is
 * put here as this class is always instantiated when this application's process
 * is created.
 * 
 * @author Niall Scott
 */
public abstract class BusApplication extends Application
        implements OnSharedPreferenceChangeListener {
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // Register the BugSense handler.
        BugSenseHandler.initAndStartSession(this, ApiKey.BUGSENSE_KEY);
        getSharedPreferences(PreferencesActivity.PREF_FILE, 0)
                .registerOnSharedPreferenceChangeListener(this);
        
        // Cause the bus stop database to be extracted straight away.
        BusStopDatabase.getInstance(this);
        // Start the thread to check for bus stop database updates.
        new Thread(stopDBTasks).start();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sp,
            final String key) {
        BackupManager.dataChanged(getPackageName());
    }
    
    /**
     * Get the bus tracker endpoint.
     * 
     * @return The BusTrackerEndpoint instance for this application.
     */
    public abstract BusTrackerEndpoint getBusTrackerEndpoint();
    
    /**
     * Get an instance of the BusStopDatabase.
     * 
     * @return An instance of the BusStopDatabase.
     */
    public abstract BusStopDatabase getBusStopDatabase();
    
    /**
     * Get an instance of the SettingsDatabase.
     * 
     * @return An instance of the SettingsDatabase.
     */
    public abstract SettingsDatabase getSettingsDatabase();
    
    private Runnable stopDBTasks = new Runnable() {
        @Override
        public void run() {
            // Start update task.
            checkForDBUpdates(false);
        }
    };
    
    /**
     * Check for updates to the bus stop database. This may happen automatically
     * if 24 hours have elapsed since the last check, or if the user has forced
     * the action. If a database update is found, then the new database is
     * downloaded and placed in the correct location.
     * 
     * @param context The context.
     * @param force True if the user forced the check, false if not.
     */
    public void checkForDBUpdates(final boolean force) {
        // Check to see if the user wants their database automatically updated.
        final SharedPreferences sp = getSharedPreferences(
                PreferencesActivity.PREF_FILE, 0);
        final boolean autoUpdate = sp.getBoolean(PREF_DATABASE_AUTO_UPDATE,
                true);
        final SharedPreferences.Editor edit = sp.edit();
        
        // Continue to check if the user has enabled it, or a check has been
        // forced (from the Preferences).
        if(autoUpdate || force) {
            if(!force) {
                // If it has not been forced, check the last update time. It is
                // only checked once per day. Abort if it is too soon.
                long lastCheck = sp.getLong("lastUpdateCheck", 0);
                if((System.currentTimeMillis() - lastCheck) < 86400000) return;
            }
            
            final StringBuilder sb = new StringBuilder();
            try {
                // Do connection stuff.
                final URL url = new URL(UrlBuilder.getTopologyUrl().toString());
                final HttpURLConnection conn = (HttpURLConnection)url
                        .openConnection();
                try {
                    final BufferedInputStream is = new BufferedInputStream(
                            conn.getInputStream());
                    
                    if(!url.getHost().equals(conn.getURL().getHost())) {
                        is.close();
                        conn.disconnect();
                        return;
                    }
                    
                    // Read the incoming data.
                    int data;
                    while((data = is.read()) != -1) {
                        sb.append((char)data);
                    }
                } finally {
                    // Whether there's an error or not, disconnect.
                    conn.disconnect();
                }
            } catch(MalformedURLException e) {
                return;
            } catch(IOException e) {
                return;
            }
            
            String topoId;
            try {
                // Parse the JSON and get the topoId from it.
                final JSONObject jo = new JSONObject(sb.toString());
                topoId = jo.getString("topoId");
            } catch(JSONException e) {
                return;
            }
            
            // If there's topoId then it cannot continue.
            if(topoId == null || topoId.length() == 0) return;
            
            // Get the current topoId from the database.
            final BusStopDatabase bsd = BusStopDatabase.getInstance(this);
            final String dbTopoId = bsd.getTopoId();
            
            // If the topoIds match, write our check time to SharedPreferences.
            if(topoId.equals(dbTopoId)) {
                edit.putLong("lastUpdateCheck", System.currentTimeMillis());
                edit.commit();
                if(force) {
                    // It was forced, alert the user there is no update
                    // available.
                    Looper.prepare();
                    Toast.makeText(this, R.string.bus_stop_db_no_updates,
                            Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
                return;
            }
            
            // There is an update available.
            try {
                // Connection stuff.
                final URL url = new URL(UrlBuilder.getDbVersionCheckUrl(
                        BusStopDatabase.SCHEMA_NAME).toString());
                sb.setLength(0);
                final HttpURLConnection conn = (HttpURLConnection)url
                        .openConnection();
                try {
                    final BufferedInputStream is = new BufferedInputStream(
                            conn.getInputStream());
                    
                    if(!url.getHost().equals(conn.getURL().getHost())) {
                        is.close();
                        conn.disconnect();
                        return;
                    }
                    
                    int data;
                    // Read the incoming data.
                    while((data = is.read()) != -1) {
                        sb.append((char)data);
                    }
                } finally {
                    // Whether there's an error or not, disconnect.
                    conn.disconnect();
                }
            } catch(MalformedURLException e) {
                return;
            } catch(IOException e) {
                return;
            }
            
            String dbUrl, schemaVersion, checksum;
            try {
                // Get the data from tje returned JSON.
                final JSONObject jo = new JSONObject(sb.toString());
                dbUrl = jo.getString("db_url");
                schemaVersion = jo.getString("db_schema_version");
                topoId = jo.getString("topo_id");
                checksum = jo.getString("checksum");
            } catch(JSONException e) {
                // There was an error parsing the JSON, it cannot continue.
                return;
            }
            
            // Make sure the returned schema name is compatible with the one
            // the app uses.
            if(!BusStopDatabase.SCHEMA_NAME.equals(schemaVersion)) return;
            // Some basic sanity checking on the parameters.
            if(topoId == null || topoId.length() == 0) return;
            if(dbUrl == null || dbUrl.length() == 0) return;
            if(checksum == null || checksum.length() == 0) return;
            
            // Make sure an update really is available.
            if(!topoId.equals(dbTopoId)) {
                // Update the database.
                updateStopsDB(this, dbUrl, checksum);
            } else if(force) {
                // Tell the user there is no update available.
                Looper.prepare();
                Toast.makeText(this, R.string.bus_stop_db_no_updates,
                        Toast.LENGTH_LONG).show();
                Looper.loop();
            }
            
            // Write to the SharedPreferences the last update time.
            edit.putLong("lastUpdateCheck", System.currentTimeMillis());
            edit.commit();
        }
    }
    
    /**
     * Download the stop database from the server and put it in the
     * application's working data directory.
     *
     * @param context The context to use this method with.
     * @param url The URL of the bus stop database to download.
     */
    private static void updateStopsDB(final Context context,
            final String url, final String checksum) {
        if(context == null || url == null || url.length() == 0 ||
                checksum == null || checksum.length() == 0) return;
        try {
            // Connect to the server.
            final URL u = new URL(url);
            final HttpURLConnection con = (HttpURLConnection)u.openConnection();
            final InputStream in = con.getInputStream();
            
            // Make sure the URL is what we expect.
            if(!u.getHost().equals(con.getURL().getHost())) {
                in.close();
                con.disconnect();
                return;
            }
            
            // The location the file should be downloaded to.
            final File temp = context
                    .getDatabasePath(BusStopDatabase.STOP_DB_NAME + "_temp");
            // The eventual destination of the file.
            final File dest = context
                    .getDatabasePath(BusStopDatabase.STOP_DB_NAME);
            final FileOutputStream out = new FileOutputStream(temp);
            
            // Get the file from the server.
            byte[] buf = new byte[1024];
            int len;
            while((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            
            // Make sure the stream is flushed then close resources and
            // disconnect.
            out.flush();
            out.close();
            in.close();
            con.disconnect();
            
            // Do a MD5 checksum on the downloaded file. Make sure it matches
            // what the server reported.
            if(!FileUtils.md5Checksum(temp).equalsIgnoreCase(checksum)) {
                // If it doesn't match, delete the downloaded file.
                temp.delete();
                return;
            }
            
            try {
                // Open the temp database and execute the index operation on it.
                final SQLiteDatabase db = SQLiteDatabase.openDatabase(
                        temp.getAbsolutePath(), null,
                        SQLiteDatabase.OPEN_READWRITE);
                BusStopDatabase.setUpIndexes(db);
                db.close();
            } catch(SQLiteException e) {
                // If we couldn't create the index, continue anyway. The user
                // will still be able to use the database, it will just run
                // slowly if they want route lines.
            }
            
            // Close a currently open database. Delete the old database then
            // move the downloaded file in to its place. Do this while
            // synchronized to make sure noting else uses the database in this
            // time.
            final BusStopDatabase bsd = BusStopDatabase
                    .getInstance(context.getApplicationContext());
            synchronized(bsd) {
                try {
                    bsd.getReadableDatabase().close();
                } catch (SQLiteException e) {
                    // Nothing to do here. Assume it's already closed.
                }
                
                dest.delete();
                temp.renameTo(dest);
            }
            
            // Delete the associated journal file because we no longer need it.
            final File journalFile = context
                    .getDatabasePath(BusStopDatabase.STOP_DB_NAME +
                    "_temp-journal");
            if(journalFile.exists()) journalFile.delete();
            
            // Alert the user that the database has been updated.
            Looper.prepare();
            Toast.makeText(context, R.string.bus_stop_db_updated,
                    Toast.LENGTH_LONG).show();
            Looper.loop();
        } catch(MalformedURLException e) {
        } catch(IOException e) { }
    }
}