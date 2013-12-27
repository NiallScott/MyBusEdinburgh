/*
 * Copyright (C) 2012 Niall 'Rivernile' Scott
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

import android.annotation.TargetApi;
import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import org.json.JSONException;

/**
 * This is the backup helper that will be called if the device implements the
 * Google Backup agent. This stores the user's preferences and saved bus stops
 * to the Google Backup service, for restoration at a later date.
 * 
 * @author Niall Scott
 */
@TargetApi(8)
public class SettingsBackupAgent extends BackupAgentHelper {
    
    private static final String PREFS_BACKUP_KEY = "prefs";
    private static final String FAVS_BACKUP_KEY = "favs";
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        // We can use an inbuilt helper to deal with the SharedPreferences for
        // us.
        SharedPreferencesBackupHelper prefsHelper =
                new SharedPreferencesBackupHelper(this,
                        PreferencesActivity.PREF_FILE);
        addHelper(PREFS_BACKUP_KEY, prefsHelper);
        
        // Use the FileBackupHelper to deal with the favourite stops.
        FileBackupHelper favsHelper = new FileBackupHelper(this,
                "settings.backup");
        addHelper(FAVS_BACKUP_KEY, favsHelper);
    }
    
    /**
     * Backup the SharedPreferences and favourite bus stops to the Google Backup
     * service. We don't need to do anything special for the SharedPreferences
     * here, but we need to write the favourite bus stops out to file first to
     * back it up.
     * 
     * @param oldState The previous backup state.
     * @param data Where to write the data to.
     * @param newState The new backup state.
     * @throws IOException When the file could not be written to.
     */
    @Override
    public void onBackup(final ParcelFileDescriptor oldState,
            final BackupDataOutput data, final ParcelFileDescriptor newState)
            throws IOException {
        final SettingsDatabase sd = SettingsDatabase.getInstance(
                getApplicationContext());
        // Write out favourite stops to a temporary file in JSON format.
        final File out = new File(getFilesDir(), "settings.backup");
        final PrintWriter pw = new PrintWriter(new FileWriter(out));
        try {
            pw.println(sd.backupDatabaseAsJSON().toString());
        } catch(JSONException e) { }
        pw.flush();
        pw.close();
        
        // Backup the file.
        super.onBackup(oldState, data, newState);
        
        // Delete the temporary file.
        out.delete();
    }

    /**
     * Restore the SharedPreferences and favourite bus stops from the Google
     * Backup service. The SharedPreferences is handled automatically, but the
     * favourite bus stops file must be handled in a special way.
     * 
     * @param data The backup data to be restored.
     * @param appVersionCode The version code of the app this data is for.
     * @param newState A copy of the new state.
     * @throws IOException When an IOException occurs.
     */
    @Override
    public void onRestore(final BackupDataInput data, final int appVersionCode,
            final ParcelFileDescriptor newState) throws IOException {
        // Restore normally first.
        super.onRestore(data, appVersionCode, newState);
        
        final SettingsDatabase sd = SettingsDatabase.getInstance(
                getApplicationContext());
        
        // Read the favourite stops file.
        StringBuilder sb = new StringBuilder();
        String str;
        final File in = new File(getFilesDir(), "settings.backup");
        final BufferedReader reader = new BufferedReader(new FileReader(in));
        while((str = reader.readLine()) != null) {
            sb.append(str);
        }
        reader.close();
        
        // Restore the favourite stops.
        try {
            sd.restoreDatabaseFromJSON(sb.toString());
        } catch(JSONException e) { }
        
        // Delete the favourite stops file as it was temporary.
        in.delete();
    }
}