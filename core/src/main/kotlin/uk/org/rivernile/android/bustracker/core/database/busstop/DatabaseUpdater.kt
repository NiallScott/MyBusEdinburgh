/*
 * Copyright (C) 2019 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.busstop

import uk.org.rivernile.android.bustracker.core.database.DatabaseUtils
import uk.org.rivernile.android.bustracker.core.endpoints.api.DatabaseVersion
import uk.org.rivernile.android.bustracker.core.http.FileDownloadException
import uk.org.rivernile.android.bustracker.core.http.FileDownloadSession
import uk.org.rivernile.android.bustracker.core.http.FileDownloader
import uk.org.rivernile.android.bustracker.core.utils.FileConsistencyChecker
import java.io.IOException
import javax.inject.Inject

/**
 * The purpose of this class is to update the bus stop database.
 *
 * The new database is downloaded to a temporary path. It is then consistency checked with the
 * expected file hash. It is then moved in to place, replacing the existing database file.
 *
 * The temporary path will be deleted if any of the steps in the process fail.
 *
 * @param databaseUtils Utilities for dealing with databases.
 * @param fileDownloader An implementation for downloading files to a path.
 * @param fileConsistencyChecker An implementation for checking the consistency of files.
 * @param databaseRepository The repository which represents this database.
 * @author Niall Scott
 */
class DatabaseUpdater @Inject constructor(
        private val databaseUtils: DatabaseUtils,
        private val fileDownloader: FileDownloader,
        private val fileConsistencyChecker: FileConsistencyChecker,
        private val databaseRepository: BusStopDatabaseRepository) {

    private var downloadSession: FileDownloadSession? = null

    /**
     * Given a [DatabaseVersion] descriptor object, which supplies the database URL and expected
     * hash checksum, update the database.
     *
     * @param databaseVersion An object which describes the database to download.
     * @return `true` when the update succeeded, otherwise `false`.
     */
    fun updateDatabase(databaseVersion: DatabaseVersion): Boolean {
        databaseUtils.ensureDatabasePathExists()
        val downloadFile = databaseUtils.getDatabasePath("busstops.db_temp")
        val downloadSession = fileDownloader.createFileDownloadSession(databaseVersion.databaseUrl,
                downloadFile)

        try {
            downloadSession.downloadFile()
        } catch (ignored: FileDownloadException) {
            downloadFile.delete()

            return false
        }

        try {
            if (!fileConsistencyChecker.checkFileMatchesHash(downloadFile,
                            databaseVersion.checksum)) {
                downloadFile.delete()

                return false
            }
        } catch (ignored: IOException) {
            downloadFile.delete()

            return false
        }

        databaseRepository.replaceDatabase(downloadFile)

        return true
    }

    /**
     * Cancel any existing database downloads currently in-flight.
     */
    fun cancel() {
        val downloadSession = this.downloadSession
        this.downloadSession = null
        downloadSession?.cancel()
    }
}