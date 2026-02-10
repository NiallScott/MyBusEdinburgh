/*
 * Copyright (C) 2022 - 2026 Niall 'Rivernile' Scott
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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.IOException
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.endpoints.api.DatabaseVersion
import uk.org.rivernile.android.bustracker.core.http.FileDownloadResponse
import uk.org.rivernile.android.bustracker.core.http.FileDownloader
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import uk.org.rivernile.android.bustracker.core.log.FakeExceptionLogger
import uk.org.rivernile.android.bustracker.core.utils.FileConsistencyChecker
import uk.org.rivernile.android.bustracker.core.utils.TemporaryFileCreator
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Tests for [DatabaseUpdater].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class RealDatabaseUpdaterTest {

    companion object {

        private const val TEMP_FILE_PREFIX = "mybus-database-download"
    }

    @Mock
    private lateinit var temporaryFileCreator: TemporaryFileCreator
    @Mock
    private lateinit var fileDownloader: FileDownloader
    @Mock
    private lateinit var fileConsistencyChecker: FileConsistencyChecker

    @Mock
    private lateinit var downloadFile: File

    private val databaseVersion = DatabaseVersion(
        timestampInSeconds = 123L,
        databaseUrl = "http://host/db.db",
        sha256Checksum = "xyz789"
    )

    @Test
    fun returnsFalseWhenTheTemporaryFileCouldNotBeCreated() = runTest {
        val updater = createDatabaseUpdater(
            databaseRepository = FakeBusStopDatabaseRepository(
                onReplaceDatabase = { fail("Database should not be replaced.") }
            )
        )
        whenever(temporaryFileCreator.createTemporaryFile(TEMP_FILE_PREFIX))
            .thenThrow(IOException::class.java)

        val result = updater.updateDatabase(databaseVersion, null)

        assertFalse(result)
        verify(fileDownloader, never())
            .downloadFile(any(), any(), anyOrNull())
    }

    @Test
    fun returnsFalseAndDeletesDownloadFileWhenDownloadFails() = runTest {
        val updater = createDatabaseUpdater(
            databaseRepository = FakeBusStopDatabaseRepository(
                onReplaceDatabase = { fail("Database should not be replaced.") }
            )
        )
        givenCreatesTemporaryFile()
        whenever(fileDownloader.downloadFile(any(), any(), anyOrNull()))
            .thenReturn(FileDownloadResponse.Error.ServerError)

        val result = updater.updateDatabase(databaseVersion, null)

        assertFalse(result)
        verify(fileDownloader)
            .downloadFile(any(), any(), anyOrNull())
        verify(downloadFile)
            .delete()
    }

    @Test
    fun returnsFalseAndDeletesDownloadFileWhenFileConsistencyFailsToReadFile() = runTest {
        val logger = FakeExceptionLogger()
        val updater = createDatabaseUpdater(
            databaseRepository = FakeBusStopDatabaseRepository(
                onReplaceDatabase = { fail("Database should not be replaced.") }
            ),
            exceptionLogger = logger
        )
        givenCreatesTemporaryFile()
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
        assertEquals(listOf(exception), logger.loggedThrowables)
    }

    @Test
    fun returnsFalseAndDeletesDownloadWhenFileConsistencyDoesNotPass() = runTest {
        val updater = createDatabaseUpdater(
            databaseRepository = FakeBusStopDatabaseRepository(
                onReplaceDatabase = { fail("Database should not be replaced.") }
            )
        )
        givenCreatesTemporaryFile()
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
    }

    @Test
    fun returnsFalseAndDeletesDownloadFileWhenReplaceDatabaseReturnsFalse() = runTest {
        val updater = createDatabaseUpdater(
            databaseRepository = FakeBusStopDatabaseRepository(
                onReplaceDatabase = { file ->
                    assertEquals(downloadFile, file)
                    false
                }
            )
        )
        givenCreatesTemporaryFile()
        whenever(fileDownloader.downloadFile(any(), any(), anyOrNull()))
            .thenReturn(FileDownloadResponse.Success)
        whenever(fileConsistencyChecker.checkFileMatchesHash(downloadFile, "xyz789"))
            .thenReturn(true)

        val result = updater.updateDatabase(databaseVersion, null)

        assertFalse(result)
        verify(fileConsistencyChecker)
            .checkFileMatchesHash(downloadFile, "xyz789")
        verify(downloadFile)
            .delete()
    }

    @Test
    fun returnsTrueAndReplacesDatabaseWhenDownloadIsSuccessfulAndFileConsistencyPasses() = runTest {
        val updater = createDatabaseUpdater(
            databaseRepository = FakeBusStopDatabaseRepository(
                onReplaceDatabase = { file ->
                    assertEquals(downloadFile, file)
                    true
                }
            )
        )
        givenCreatesTemporaryFile()
        whenever(fileDownloader.downloadFile(any(), any(), anyOrNull()))
            .thenReturn(FileDownloadResponse.Success)
        whenever(fileConsistencyChecker.checkFileMatchesHash(downloadFile, "xyz789"))
            .thenReturn(true)

        val result = updater.updateDatabase(databaseVersion, null)

        assertTrue(result)
        verify(downloadFile, never())
            .delete()
    }

    private suspend fun givenCreatesTemporaryFile() {
        whenever(temporaryFileCreator.createTemporaryFile(TEMP_FILE_PREFIX))
            .thenReturn(downloadFile)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.createDatabaseUpdater(
        exceptionLogger: ExceptionLogger = FakeExceptionLogger(),
        databaseRepository: BusStopDatabaseRepository = FakeBusStopDatabaseRepository()
    ): DatabaseUpdater {
        return RealDatabaseUpdater(
            temporaryFileCreator,
            fileDownloader,
            fileConsistencyChecker,
            databaseRepository,
            exceptionLogger,
            UnconfinedTestDispatcher(testScheduler)
        )
    }
}
