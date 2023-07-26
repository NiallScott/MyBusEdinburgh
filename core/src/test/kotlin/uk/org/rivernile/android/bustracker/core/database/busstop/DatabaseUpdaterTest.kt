/*
 * Copyright (C) 2022 - 2023 Niall 'Rivernile' Scott
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

import kotlinx.coroutines.test.runTest
import okio.IOException
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.database.DatabaseUtils
import uk.org.rivernile.android.bustracker.core.endpoints.api.DatabaseVersion
import uk.org.rivernile.android.bustracker.core.http.FileDownloadResponse
import uk.org.rivernile.android.bustracker.core.http.FileDownloader
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import uk.org.rivernile.android.bustracker.core.utils.FileConsistencyChecker
import uk.org.rivernile.android.bustracker.core.utils.TimeUtils
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import java.io.File

/**
 * Tests for [DatabaseUpdater].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class DatabaseUpdaterTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var databaseUtils: DatabaseUtils
    @Mock
    private lateinit var fileDownloader: FileDownloader
    @Mock
    private lateinit var fileConsistencyChecker: FileConsistencyChecker
    @Mock
    private lateinit var databaseRepository: BusStopDatabaseRepository
    @Mock
    private lateinit var timeUtils: TimeUtils
    @Mock
    private lateinit var exceptionLogger: ExceptionLogger

    @Mock
    private lateinit var downloadFile: File

    private val databaseVersion = DatabaseVersion("MBE", "abc123", "http://host/db.db", "xyz789")

    private lateinit var updater: DatabaseUpdater

    @Before
    fun setUp() {
        updater = DatabaseUpdater(
                databaseUtils,
                fileDownloader,
                fileConsistencyChecker,
                databaseRepository,
                timeUtils,
                exceptionLogger,
                coroutineRule.testDispatcher)

        whenever(databaseUtils.getDatabasePath(any()))
                .thenReturn(downloadFile)
    }

    @Test
    fun usesTemporaryFilenameForDownloadFileLocation() = runTest {
        whenever(timeUtils.currentTimeMills)
                .thenReturn(123L)

        updater.updateDatabase(databaseVersion, null)

        verify(databaseUtils)
                .getDatabasePath("busstops.123.db_temp")
    }

    @Test
    fun ensuresDatabasePathExistsBeforeCommencingDownload() = runTest {
        updater.updateDatabase(databaseVersion, null)

        inOrder(databaseUtils, fileDownloader) {
            verify(databaseUtils)
                    .ensureDatabasePathExists()
            verify(fileDownloader)
                    .downloadFile(any(), any(), anyOrNull())
        }
    }

    @Test
    fun returnsFalseAndDeletesDownloadFileWhenDownloadFails() = runTest {
        whenever(fileDownloader.downloadFile(any(), any(), anyOrNull()))
                .thenReturn(FileDownloadResponse.Error.ServerError)

        val result = updater.updateDatabase(databaseVersion, null)

        assertFalse(result)
        verify(fileDownloader)
                .downloadFile(any(), any(), anyOrNull())
        verify(downloadFile)
                .delete()
        verify(databaseRepository, never())
                .replaceDatabase(any())
    }

    @Test
    fun returnsFalseAndDeletesDownloadFileWhenFileConsistencyFailsToReadFile() = runTest {
        whenever(fileDownloader.downloadFile(any(), any(), anyOrNull()))
                .thenReturn(FileDownloadResponse.Success)
        val exception = IOException()
        whenever(fileConsistencyChecker.checkFileMatchesHash(downloadFile, "xyz789"))
                .thenThrow(exception)

        val result = updater.updateDatabase(databaseVersion, null)

        assertFalse(result)
        verify(fileConsistencyChecker)
                .checkFileMatchesHash(downloadFile, "xyz789")
        verify(downloadFile)
                .delete()
        verify(databaseRepository, never())
                .replaceDatabase(any())
        verify(exceptionLogger)
            .log(exception)
    }

    @Test
    fun returnsFalseAndDeletesDownloadWhenFileConsistencyDoesNotPass() = runTest {
        whenever(fileDownloader.downloadFile(any(), any(), anyOrNull()))
                .thenReturn(FileDownloadResponse.Success)
        whenever(fileConsistencyChecker.checkFileMatchesHash(downloadFile, "xyz789"))
                .thenReturn(false)

        val result = updater.updateDatabase(databaseVersion, null)

        assertFalse(result)
        verify(fileConsistencyChecker)
                .checkFileMatchesHash(downloadFile, "xyz789")
        verify(downloadFile)
                .delete()
        verify(databaseRepository, never())
                .replaceDatabase(any())
    }

    @Test
    fun returnsFalseAndDeletesDownloadFileWhenReplaceDatabaseReturnsFalse() = runTest {
        whenever(fileDownloader.downloadFile(any(), any(), anyOrNull()))
            .thenReturn(FileDownloadResponse.Success)
        whenever(fileConsistencyChecker.checkFileMatchesHash(downloadFile, "xyz789"))
            .thenReturn(true)
        whenever(databaseRepository.replaceDatabase(downloadFile))
            .thenReturn(false)

        val result = updater.updateDatabase(databaseVersion, null)

        assertFalse(result)
        verify(fileConsistencyChecker)
            .checkFileMatchesHash(downloadFile, "xyz789")
        verify(downloadFile)
            .delete()
    }

    @Test
    fun returnsTrueAndReplacesDatabaseWhenDownloadIsSuccessfulAndFileConsistencyPasses() = runTest {
        whenever(fileDownloader.downloadFile(any(), any(), anyOrNull()))
                .thenReturn(FileDownloadResponse.Success)
        whenever(fileConsistencyChecker.checkFileMatchesHash(downloadFile, "xyz789"))
                .thenReturn(true)
        whenever(databaseRepository.replaceDatabase(downloadFile))
            .thenReturn(true)

        val result = updater.updateDatabase(databaseVersion, null)

        assertTrue(result)
        verify(downloadFile, never())
                .delete()
    }
}