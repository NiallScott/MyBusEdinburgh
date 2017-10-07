/*
 * Copyright (C) 2012 - 2017 Niall 'Rivernile' Scott
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

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;
import java.io.File;
import java.io.IOException;

import uk.org.rivernile.android.bustracker.database.settings.SettingsDatabase;
import uk.org.rivernile.android.bustracker.preferences.PreferenceManager;

/**
 * This is the backup helper that will be called if the device implements the Google Backup agent.
 * This stores the user's preferences and saved bus stops to the Google Backup service, for
 * restoration at a later date.
 * 
 * @author Niall Scott
 */
public class SettingsBackupAgent extends BackupAgentHelper {
    
    private static final String PREFS_BACKUP_KEY = "prefs";
    private static final String FAVS_BACKUP_KEY = "favs";

    @Override
    public void onCreate() {
        // We can use an inbuilt helper to deal with the SharedPreferences for us.
        final SharedPreferencesBackupHelper prefsHelper =
                new SharedPreferencesBackupHelper(this, PreferenceManager.PREF_FILE);
        addHelper(PREFS_BACKUP_KEY, prefsHelper);
        
        // Use the FileBackupHelper to deal with the favourite stops.
        final FileBackupHelper favsHelper = new FileBackupHelper(this, "settings.backup");
        addHelper(FAVS_BACKUP_KEY, favsHelper);
    }
    
    /**
     * Backup the {@link android.content.SharedPreferences} and favourite bus stops to the Google
     * Backup service. We don't need to do anything special for the
     * {@link android.content.SharedPreferences} here, but we need to write the favourite bus
     * stops out to file first to back it up.
     * 
     * @param oldState The previous backup state.
     * @param data Where to write the data to.
     * @param newState The new backup state.
     * @throws IOException When the file could not be written to.
     */
    @Override
    public void onBackup(final ParcelFileDescriptor oldState, final BackupDataOutput data,
            final ParcelFileDescriptor newState) throws IOException {
        // Write out favourite stops to a temporary file in JSON format.
        final File out = new File(getFilesDir(), "settings.backup");
        SettingsDatabase.backupFavourites(getApplicationContext(), out);
        super.onBackup(oldState, data, newState);
        // Delete the temporary file.
        out.delete();
    }

    /**
     * Restore the {@link android.content.SharedPreferences} and favourite bus stops from the Google
     * Backup service. The {@link android.content.SharedPreferences} is handled automatically, but
     * the favourite bus stops file must be handled in a special way.
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
        final File in = new File(getFilesDir(), "settings.backup");
        SettingsDatabase.restoreFavourites(getApplicationContext(), in);
        in.delete();
    }
}