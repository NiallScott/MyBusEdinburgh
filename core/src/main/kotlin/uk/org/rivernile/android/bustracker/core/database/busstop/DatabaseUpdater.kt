/*
 * Copyright (C) 2019 - 2023 Niall 'Rivernile' Scott
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

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okio.IOException
import uk.org.rivernile.android.bustracker.core.database.DatabaseUtils
import uk.org.rivernile.android.bustracker.core.di.ForIoDispatcher
import uk.org.rivernile.android.bustracker.core.endpoints.api.DatabaseVersion
import uk.org.rivernile.android.bustracker.core.http.FileDownloadResponse
import uk.org.rivernile.android.bustracker.core.http.FileDownloader
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import uk.org.rivernile.android.bustracker.core.utils.FileConsistencyChecker
import java.io.File
import javax.inject.Inject
import javax.net.SocketFactory

/**
 * This class updates the database with a new version which has been downloaded from the API server.
 *
 * @param databaseUtils Utilities for dealing with databases.
 * @param fileDownloader An implementation for downloading files to a path.
 * @param fileConsistencyChecker An implementation for checking the consistency of files.
 * @param databaseRepository The repository which represents this database.
 * @param exceptionLogger Used to log exceptions.
 * @param ioDispatcher The IO [CoroutineDispatcher].
 * @author Niall Scott
 */
class DatabaseUpdater @Inject internal constructor(
    private val databaseUtils: DatabaseUtils,
    private val fileDownloader: FileDownloader,
    private val fileConsistencyChecker: FileConsistencyChecker,
    private val databaseRepository: BusStopDatabaseRepository,
    private val exceptionLogger: ExceptionLogger,
    @ForIoDispatcher private val ioDispatcher: CoroutineDispatcher) {

    /**
     * Given a [DatabaseVersion] descriptor object, which supplies the database URL and expected
     * hash checksum, update the database.
     *
     * @return `true` when the update succeeded, otherwise `false`.
     */
    suspend fun updateDatabase(
        databaseVersion: DatabaseVersion,
        socketFactory: SocketFactory? = null): Boolean {
        val downloadFile = try {
            databaseUtils.createTemporaryFile("mybus-database-download")
        } catch (e: IOException) {
            exceptionLogger.log(e)
            return false
        }

        if (fileDownloader.downloadFile(
                databaseVersion.databaseUrl,
                downloadFile,
                socketFactory) !is FileDownloadResponse.Success) {
            downloadFile.deleteSuspend()

            return false
        }

        if (!doesPassConsistencyCheck(downloadFile, databaseVersion.checksum)) {
            downloadFile.deleteSuspend()

            return false
        }

        if (!databaseRepository.replaceDatabase(downloadFile)) {
            downloadFile.deleteSuspend()

            return false
        }

        return true
    }

    /**
     * Does the downloaded [File] pass the file consistency check?
     *
     * @param downloadFile A [File] object describing the downloaded file.
     * @param checksum The expected checksum to check for.
     * @return `true` if the file consistency check passes, otherwise `false`.
     */
    private suspend fun doesPassConsistencyCheck(downloadFile: File, checksum: String) = try {
        fileConsistencyChecker.checkFileMatchesHash(downloadFile, checksum)
    } catch (e: IOException) {
        exceptionLogger.log(e)
        false
    }

    /**
     * Deletes the [File] from the filesystem while being executed on the Coroutines IO
     * [CoroutineDispatcher].
     *
     * @see File.delete
     */
    private suspend fun File.deleteSuspend() {
        withContext(ioDispatcher) {
            delete()
        }
    }
}