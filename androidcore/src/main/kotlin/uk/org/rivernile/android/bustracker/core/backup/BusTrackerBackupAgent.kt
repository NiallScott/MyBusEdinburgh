/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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
 *
 */

package uk.org.rivernile.android.bustracker.core.backup

import android.app.backup.BackupAgentHelper
import android.app.backup.BackupDataInput
import android.app.backup.BackupDataOutput
import android.app.backup.FileBackupHelper
import android.app.backup.SharedPreferencesBackupHelper
import android.os.ParcelFileDescriptor
import uk.org.rivernile.android.bustracker.core.dagger.inject
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceManager
import java.io.File
import javax.inject.Inject

/**
 * This extends [BackupAgentHelper] which defines what is backed up and restored when the system
 * invokes this class.
 *
 * This class will only be used up until [android.os.Build.VERSION_CODES.LOLLIPOP_MR1] - after
 * this, the auto backup functionality is used instead.
 *
 * @author Niall Scott
 */
class BusTrackerBackupAgent : BackupAgentHelper() {

    companion object {

        private const val KEY_PREFERENCES_BACKUP = "prefs"
        private const val KEY_FAVOURITES_BACKUP = "favs"

        private const val FAVOURITES_FILE_NAME = "favourites.backup"
    }

    // This could be `null` if this instance lives in the restricted mode, as documented in
    // https://developer.android.com/guide/topics/data/autobackup
    @Inject
    @JvmField
    internal var backupRestoreTask: BackupRestoreTask? = null

    override fun onCreate() {
        inject(this)

        addHelper(
                KEY_PREFERENCES_BACKUP,
                SharedPreferencesBackupHelper(this, PreferenceManager.PREF_FILE))
        addHelper(
                KEY_FAVOURITES_BACKUP,
                FileBackupHelper(this, FAVOURITES_FILE_NAME))
    }

    override fun onBackup(oldState: ParcelFileDescriptor, data: BackupDataOutput,
            newState: ParcelFileDescriptor) {
        backupRestoreTask?.let {
            val output = getSettingsBackupFile()
            it.serialiseFavouriteStops(output)
            super.onBackup(oldState, data, newState)
            output.delete()
        } ?: super.onBackup(oldState, data, newState)
    }

    override fun onRestore(data: BackupDataInput, appVersionCode: Int,
            newState: ParcelFileDescriptor) {
        super.onRestore(data, appVersionCode, newState)

        backupRestoreTask?.let {
            val input = getSettingsBackupFile()
            it.restoreFavouriteStops(input)
            input.delete()
        }
    }

    /**
     * Get the [File] where settings are backed up to or restored from.
     *
     * @return The [File] where settings are backed up to or restored from.
     */
    private fun getSettingsBackupFile() = File(filesDir, FAVOURITES_FILE_NAME)
}