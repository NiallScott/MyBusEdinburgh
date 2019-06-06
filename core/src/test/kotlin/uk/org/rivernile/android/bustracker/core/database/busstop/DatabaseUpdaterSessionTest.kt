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

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.database.DatabaseUtils
import uk.org.rivernile.android.bustracker.core.endpoints.api.DatabaseVersion
import uk.org.rivernile.android.bustracker.core.http.FileDownloadException
import uk.org.rivernile.android.bustracker.core.http.FileDownloadSession
import uk.org.rivernile.android.bustracker.core.http.FileDownloader
import uk.org.rivernile.android.bustracker.core.utils.FileConsistencyChecker
import uk.org.rivernile.android.bustracker.core.utils.TimeUtils
import java.io.File
import java.io.IOException

/**
 * Unit tests for [DatabaseUpdaterSession].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class DatabaseUpdaterSessionTest {

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
    private lateinit var downloadSession: FileDownloadSession
    @Mock
    private lateinit var downloadFile: File

    private val databaseVersion = DatabaseVersion("MBE", "abc123", "http://host/db.db", "xyz789")

    @Before
    fun setUp() {
        whenever(databaseUtils.getDatabasePath(any()))
                .thenReturn(downloadFile)
        whenever(fileDownloader.createFileDownloadSession(any(), any()))
                .thenReturn(downloadSession)
    }

    @Test
    fun usesTemporaryFilenameForDownloadFileLocation() {
        val session = createSession(databaseVersion)
        whenever(timeUtils.getCurrentTimeMillis())
                .thenReturn(123L)

        session.updateDatabase()

        verify(databaseUtils)
                .getDatabasePath("busstops.123.db_temp")
    }

    @Test
    fun ensuresDatabasePathExistsBeforeCommencingDownload() {
        val session = createSession(databaseVersion)

        session.updateDatabase()

        val inOrder = inOrder(databaseUtils, downloadSession)
        inOrder.verify(databaseUtils)
                .ensureDatabasePathExists()
        inOrder.verify(downloadSession)
                .downloadFile()
    }

    @Test
    fun returnsFalseAndDeletesDownloadFileWhenDownloadFails() {
        val session = createSession(databaseVersion)
        whenever(downloadSession.downloadFile())
                .thenThrow(FileDownloadException::class.java)

        val result = session.updateDatabase()

        assertFalse(result)
        verify(downloadSession)
                .downloadFile()
        verify(downloadFile)
                .delete()
        verify(databaseRepository, never())
                .replaceDatabase(any())
    }

    @Test
    fun returnsFalseAndDeletesDownloadFileWhenFileConsistencyFailsToReadFile() {
        val session = createSession(databaseVersion)
        whenever(fileConsistencyChecker.checkFileMatchesHash(downloadFile, "xyz789"))
                .thenThrow(IOException::class.java)

        val result = session.updateDatabase()

        assertFalse(result)
        verify(fileConsistencyChecker)
                .checkFileMatchesHash(downloadFile, "xyz789")
        verify(downloadFile)
                .delete()
        verify(databaseRepository, never())
                .replaceDatabase(any())
    }

    @Test
    fun returnsFalseAndDeletesDownloadWhenFileConsistencyDoesNotPass() {
        val session = createSession(databaseVersion)
        whenever(fileConsistencyChecker.checkFileMatchesHash(downloadFile, "xyz789"))
                .thenReturn(false)

        val result = session.updateDatabase()

        assertFalse(result)
        verify(fileConsistencyChecker)
                .checkFileMatchesHash(downloadFile, "xyz789")
        verify(downloadFile)
                .delete()
        verify(databaseRepository, never())
                .replaceDatabase(any())
    }

    @Test
    fun returnsTrueAndReplacesDatabaseWhenDownloadIsSuccessfulAndFileConsistencyPasses() {
        val session = createSession(databaseVersion)
        whenever(fileConsistencyChecker.checkFileMatchesHash(downloadFile, "xyz789"))
                .thenReturn(true)

        val result = session.updateDatabase()

        assertTrue(result)
        verify(downloadFile, never())
                .delete()
        verify(databaseRepository)
                .replaceDatabase(downloadFile)
    }

    @Test
    fun cancellingASessionAttemptToCancelFileDownload() {
        val session = createSession(databaseVersion)
        whenever(fileConsistencyChecker.checkFileMatchesHash(downloadFile, "xyz789"))
                .thenReturn(true)

        session.updateDatabase()
        session.cancel()

        verify(downloadSession)
                .cancel()
    }

    @Test(expected = IllegalStateException::class)
    fun throwsIoExceptionWhenAttemptingToStartAnAlreadyRunSession() {
        val session = createSession(databaseVersion)
        whenever(fileConsistencyChecker.checkFileMatchesHash(downloadFile, "xyz789"))
                .thenReturn(true)

        session.updateDatabase()
        session.updateDatabase()
    }

    private fun createSession(databaseVersion: DatabaseVersion) =
            DatabaseUpdaterSession(databaseUtils, fileDownloader, fileConsistencyChecker,
                    databaseRepository, timeUtils, databaseVersion)
}