/*
 * Copyright (C) 2009 - 2013 Niall 'Rivernile' Scott
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

import static uk.org.rivernile.edinburghbustracker.android.PreferencesActivity
        .PREF_DATABASE_AUTO_UPDATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.os.Looper;
import android.widget.Toast;
import com.bugsense.trace.BugSenseHandler;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import org.json.JSONException;
import org.json.JSONObject;
import uk.org.rivernile.android.utils.BackupCompat;

/**
 * This code is the very first code that will be executed when the application
 * is started. It is used to register the BugSense handler, put a listener on
 * the SharedPreferences for Google Backup on Froyo upwards, and check for bus
 * stop database updates.
 * 
 * The Android developer documentation discourages the usage of this class, but
 * as it is unpredictable where the user will enter the application the code is
 * put here as this class is always instantiated when this application's process
 * is created.
 * 
 * @author Niall Scott
 */
public class Application extends android.app.Application {
    
    private static final String DB_API_CHECK_URL =
            "http://www.mybustracker.co.uk/ws.php?module=json&function=" +
            "getTopoId&key=";
    private static final String DB_UPDATE_CHECK_URL =
            "http://edinb.us/api/DatabaseVersion?schemaType=" +
            BusStopDatabase.SCHEMA_NAME + "&random=";
    
    private static final Random random = new Random(System.currentTimeMillis());
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // Register the BugSense handler.
        BugSenseHandler.initAndStartSession(this, ApiKey.BUGSENSE_KEY);
        // Cause the bus stop database to be extracted straight away.
        BusStopDatabase.getInstance(this);
        
        // If the API level is Froyo or greater, then register the
        // SharedPreference listener.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
            getSharedPreferences(PreferencesActivity.PREF_FILE, 0)
                    .registerOnSharedPreferenceChangeListener(
                    new SharedPreferencesListener(this));
        
        // Start the thread to check for bus stop database updates.
        new Thread(stopDBTasks).start();
    }
    
    private Runnable stopDBTasks = new Runnable() {
        @Override
        public void run() {
            // Delete old database files if they exist.
            File toDelete = getDatabasePath("busstops.db");
            if(toDelete.exists()) toDelete.delete();
            
            toDelete = getDatabasePath("busstops.db-journal");
            if(toDelete.exists()) toDelete.delete();
            
            toDelete = getDatabasePath("busstops2.db");
            if(toDelete.exists()) toDelete.delete();
            
            toDelete = getDatabasePath("busstops2.db-journal");
            if(toDelete.exists()) toDelete.delete();
            
            toDelete = getDatabasePath("busstops8.db");
            if(toDelete.exists()) toDelete.delete();
            
            toDelete = getDatabasePath("busstops8.db-journal");
            if(toDelete.exists()) toDelete.delete();
            
            // Start update task.
            checkForDBUpdates(getApplicationContext(), false);
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
    public static void checkForDBUpdates(final Context context,
            final boolean force) {
        // Check to see if the user wants their database automatically updated.
        final SharedPreferences sp = context.getSharedPreferences(
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
            
            // Construct the checking URL.
            final StringBuilder sb = new StringBuilder();
            sb.append(DB_API_CHECK_URL);
            sb.append(ApiKey.getHashedKey());
            sb.append("&random=");
            // A random number is used so networks don't cache the HTTP
            // response.
            sb.append(random.nextInt());
            try {
                // Do connection stuff.
                final URL url = new URL(sb.toString());
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
            final BusStopDatabase bsd = BusStopDatabase.getInstance(context);
            final String dbTopoId = bsd.getTopoId();
            
            // If the topoIds match, write our check time to SharedPreferences.
            if(topoId.equals(dbTopoId)) {
                edit.putLong("lastUpdateCheck", System.currentTimeMillis());
                edit.commit();
                if(force) {
                    // It was forced, alert the user there is no update
                    // available.
                    Looper.prepare();
                    Toast.makeText(context, R.string.bus_stop_db_no_updates,
                            Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
                return;
            }
            
            // There is an update available. Empty the StringBuilder then create
            // the URL to get the new database information.
            sb.setLength(0);
            sb.append(DB_UPDATE_CHECK_URL);
            sb.append(random.nextInt());
            sb.append("&key=");
            sb.append(ApiKey.getHashedKey());
            
            try {
                // Connection stuff.
                final URL url = new URL(sb.toString());
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
                updateStopsDB(context, dbUrl, checksum);
            } else if(force) {
                // Tell the user there is no update available.
                Looper.prepare();
                Toast.makeText(context, R.string.bus_stop_db_no_updates,
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
            if(!md5Checksum(temp).equalsIgnoreCase(checksum)) {
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
            final BusStopDatabase bsd = BusStopDatabase.getInstance(context);
            synchronized(bsd) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    try {
                        bsd.getReadableDatabase().close();
                    } catch (SQLiteCantOpenDatabaseException e) {
                        // Nothing to do here. Assume it's already closed.
                    }
                } else {
                    bsd.getReadableDatabase().close();
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
    
    /**
     * Create a checksum for a File. This is used to ensure that a downloaded
     * database has not been corrupted or incomplete.
     * 
     * See: http://vyshemirsky.blogspot.com/2007/08/computing-md5-digest-checksum-in-java.html
     * This has been slightly modified.
     * 
     * @param file The file to run the MD5 checksum against.
     * @return The MD5 checksum string.
     */
    public static String md5Checksum(final File file) {
        try {
            final InputStream fin = new FileInputStream(file);
            final MessageDigest md5er = MessageDigest.getInstance("MD5");
            final byte[] buffer = new byte[1024];
            int read;
            
            while((read = fin.read(buffer)) != -1) {
                if(read > 0) md5er.update(buffer, 0, read);
            }
            fin.close();
            
            final byte[] digest = md5er.digest();
            if(digest == null) return null;
            final StringBuilder builder = new StringBuilder();
            for(byte a : digest) {
                builder.append(Integer.toString((a & 0xff) 
                + 0x100, 16).substring(1));
            }
            
            return builder.toString();
        } catch(FileNotFoundException e) {
            return "";
        } catch(NoSuchAlgorithmException e) {
            return "";
        } catch(IOException e) {
            return "";
        }
    }
    
    /**
     * The SharedPreferencesListener will look out for changes to the shared
     * preferences and schedule updates with Google Backup if there is, if the
     * device is running Android 2.2 (Froyo) or greater.
     */
    public static class SharedPreferencesListener
            implements OnSharedPreferenceChangeListener {
        
        final Context context;
        
        /**
         * Constructor, supplying a Context instance.
         * 
         * @param context The application Context.
         */
        public SharedPreferencesListener(final Context context) {
            this.context = context;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onSharedPreferenceChanged(final SharedPreferences sp,
                final String key) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
                BackupCompat.dataChanged(context.getPackageName());
        }
    }
}