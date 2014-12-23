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

package uk.org.rivernile.android.bustracker.database;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.v4.net.ConnectivityManagerCompat;
import java.io.File;
import java.io.IOException;
import uk.org.rivernile.android.bustracker.BusApplication;
import uk.org.rivernile.android.bustracker.endpoints.DatabaseEndpoint;
import uk.org.rivernile.android.bustracker.parser.database
        .DatabaseEndpointException;
import uk.org.rivernile.android.bustracker.parser.database.DatabaseVersion;
import uk.org.rivernile.android.bustracker.preferences.PreferenceConstants;
import uk.org.rivernile.android.fetchers.Fetcher;
import uk.org.rivernile.android.fetchers.FetcherFactory;
import uk.org.rivernile.android.fetchers.readers.FileWriterFetcherStreamReader;
import uk.org.rivernile.android.utils.FileUtils;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;

/**
 * The job of this {@link IntentService} is to check for bus stop database
 * updates and apply them if there is. As it is an {@link IntentService}, only
 * one request is sent off at a time and the checks are done in a non-UI thread.
 * Successful checks can only happen as often as once per 12 hours, but this
 * could be longer.
 * 
 * @author Niall Scott
 */
public class DatabaseUpdateService extends IntentService {
    
    private static final int CHECK_PERIOD = 43200000; // 12 hours
    
    private ConnectivityManager connMan;
    private BusStopDatabase bsd;
    private SharedPreferences sp;
    private DatabaseEndpoint databaseEndpoint;

    /**
     * Create a new {@code DatabaseUpdateService}. This should only be invoked
     * by the platform.
     */
    public DatabaseUpdateService() {
        super(DatabaseUpdateService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        final BusApplication app = (BusApplication) getApplication();
        connMan = (ConnectivityManager) getSystemService(Context
                .CONNECTIVITY_SERVICE);
        bsd = app.getBusStopDatabase();
        sp = getSharedPreferences(PreferenceConstants.PREF_FILE, 0);
        databaseEndpoint = app.getDatabaseEndpoint();
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        doDatabaseUpdateTask();
    }
    
    /**
     * Check with the remote server to see if there are any pending database
     * updates to apply. The following happens;
     * 
     * <ol>
     * <li>A check is made to see if the user has enabled database updates over
     * Wi-Fi only, and if so, it checks the connection status.</li>
     * <li>If the last check was within the last 12 hours, then the process is
     * abandoned.</li>
     * <li>The version information is retrieved from the remote server.</li>
     * <li>If there was a failure retrieving the version information, or it
     * relates to a schema not supported by this app, the process is
     * abandoned.</li>
     * <li>If there is an update available, then it initiates the download of
     * the update. If there's no update available, the check time is recorded
     * so a check is not made again for another 12 hours.</li>
     * </ol>
     */
    private void doDatabaseUpdateTask() {
        if (shouldStopCheck()) {
            return;
        }
        
        final long lastCheck = sp.getLong(
                PreferenceConstants.PREF_DATABASE_UPDATE_LAST_CHECK, 0);
        
        if ((System.currentTimeMillis() - lastCheck) < CHECK_PERIOD) {
            // If it has been less than the check period since the last check,
            // then do not proceed.
            return;
        }
        
        final DatabaseVersion version;
        
        try {
            version = databaseEndpoint
                    .getDatabaseVersion(BusStopDatabase.SCHEMA_NAME);
        } catch (DatabaseEndpointException e) {
            return;
        }
        
        if (version == null ||
                !BusStopDatabase.SCHEMA_NAME.equals(version.getSchemaName())) {
            return;
        }
        
        final String dbTopoId = bsd.getTopoId();
        if (!version.getTopologyId().equals(dbTopoId)) {
            updateDatabase(version);
        } else {
            writeUpdatedCheckTime();
        }
    }
    
    /**
     * Update the database if there's an update available.
     * 
     * @param version The database version information.
     */
    private void updateDatabase(final DatabaseVersion version) {
        // This is called again incase the connectivity has changed.
        if (shouldStopCheck()) {
            return;
        }
        
        // The new database is put in to a temporary file until it's ready to
        // be swapped in.
        final File tempFile = getDatabasePath(
                BusStopDatabase.STOP_DB_NAME + "_temp");
        final File destFile = getDatabasePath(BusStopDatabase.STOP_DB_NAME);
        
        final Fetcher fetcher = FetcherFactory.getFetcher(this,
                Uri.parse(version.getUrl()), false);
        final FileWriterFetcherStreamReader reader =
                new FileWriterFetcherStreamReader(tempFile, false);
        
        try {
            // This single call will fetch the database from the server and pipe
            // it out on to a file on disk.
            fetcher.executeFetcher(reader);
            
            // This checks the consistency of the downloaded file. If the file
            // is corrupt, delete it and stop the process.
            if (!FileUtils.md5Checksum(tempFile)
                    .equalsIgnoreCase(version.getChecksum())) {
                tempFile.delete();
                return;
            }
            
            // Open the temp database and execute the index operation on it.
            final SQLiteDatabase db = SQLiteDatabase.openDatabase(
                    tempFile.getAbsolutePath(), null,
                    SQLiteDatabase.OPEN_READWRITE);
            BusStopDatabase.setUpIndexes(db);
            db.close();
        } catch (IOException e) {
            tempFile.delete();
            return;
        } catch (SQLiteException e) {
            // No need to do anything. Indexing probably failed due to lack of
            // disk space. If this is the case, the database is still usable,
            // although map route lines may be quite slow.
        }
        
        // Synchronize the access to the database. This is done because we can't
        // take the database away underneath any other operations. We'll wait
        // our turn and do the swaperoo.
        synchronized(bsd) {
            try {
                // Make sure the database is closed.
                bsd.getReadableDatabase().close();
            } catch (SQLiteException e) {
                // Nothing to do here. Assume it's already closed.
            }
            
            // Swaperoo!
            destFile.delete();
            tempFile.renameTo(destFile);
        }
        
        // Delete the journal file if it was created.
        getDatabasePath(BusStopDatabase.STOP_DB_NAME + "_temp-journal")
                .delete();
        
        // Finally, if we reached this point, it means that everything was
        // probably okay, so write the new check time.
        writeUpdatedCheckTime();
    }
    
    /**
     * Should the check for new updates be halted?
     * 
     * @return {@code true} if the check should be halted, {@code false} if not.
     */
    private boolean shouldStopCheck() {
        return sp.getBoolean(
                PreferenceConstants.PREF_BUS_STOP_DATABASE_WIFI_ONLY, false) &&
                ConnectivityManagerCompat.isActiveNetworkMetered(connMan);
    }
    
    /**
     * Write a new check time to the preferences. This should only be done in
     * success cases.
     */
    private void writeUpdatedCheckTime() {
        final SharedPreferences.Editor edit = sp.edit();
        edit.putLong(PreferenceConstants.PREF_DATABASE_UPDATE_LAST_CHECK,
                System.currentTimeMillis());
        edit.commit();
    }
}