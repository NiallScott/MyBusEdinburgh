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

import androidx.annotation.WorkerThread
import uk.org.rivernile.android.bustracker.core.database.settings.SettingsDatabaseRepository
import uk.org.rivernile.android.bustracker.core.database.settings.daos.FavouritesDao
import uk.org.rivernile.android.bustracker.core.database.settings.entities.FavouriteStop
import uk.org.rivernile.android.bustracker.core.utils.TimeUtils
import java.io.File
import javax.inject.Inject

/**
 * This task is executed to perform backup and restore of user data.
 *
 * @param favouritesDao Used to access favourites data.
 * @param settingsDatabaseRepository Used to access properties about the settings database.
 * @param backupPersistence The implementation of storing and reading of backup data to and from
 * disk.
 * @param timeUtils Used to obtain the current time.
 * @author Niall Scott
 */
internal class BackupRestoreTask @Inject constructor(
        private val favouritesDao: FavouritesDao,
        private val settingsDatabaseRepository: SettingsDatabaseRepository,
        private val backupPersistence: BackupPersistence,
        private val timeUtils: TimeUtils) {

    /**
     * Serialise the current favourite stops to disk.
     *
     * This must happen on a background thread.
     *
     * @param outFile The [File] to write the backup data to.
     */
    @WorkerThread
    fun serialiseFavouriteStops(outFile: File) {
        val favouriteStops = favouritesDao.getAllFavouriteStops()
                ?.map(this::mapToBackupFavouriteStop)
        Backup(
                dbVersion = settingsDatabaseRepository.getDatabaseVersion(),
                createTime = timeUtils.getCurrentTimeMillis(),
                favouriteStops = favouriteStops)
                .let {
                    backupPersistence.persistBackup(outFile, it)
                }
    }

    /**
     * Read backup data from the given [File] and apply the state to the user persistence.
     *
     * @param inputFile The [File] containing the backup data.
     */
    @WorkerThread
    fun restoreFavouriteStops(inputFile: File) {
        backupPersistence.readBackup(inputFile)
                ?.favouriteStops
                ?.map(this::mapToFavouriteStop)
                ?.also {
                    if (it.isNotEmpty()) {
                        favouritesDao.removeAllFavouriteStops()
                        favouritesDao.addFavouriteStops(it)
                    }
                }
    }

    /**
     * Given a [FavouriteStop], map it to a [BackupFavouriteStop].
     *
     * @param favouriteStop The [FavouriteStop] to map.
     * @return The [BackupFavouriteStop].
     */
    private fun mapToBackupFavouriteStop(favouriteStop: FavouriteStop) =
            BackupFavouriteStop(
                    favouriteStop.stopCode,
                    favouriteStop.stopName)

    /**
     * Given a [BackupFavouriteStop], map it to a [FavouriteStop].
     *
     * @param backupFavouriteStop The [BackupFavouriteStop] to map.
     * @return The [FavouriteStop].
     */
    private fun mapToFavouriteStop(backupFavouriteStop: BackupFavouriteStop) =
            FavouriteStop(
                    stopCode = backupFavouriteStop.stopCode,
                    stopName = backupFavouriteStop.stopName)
}