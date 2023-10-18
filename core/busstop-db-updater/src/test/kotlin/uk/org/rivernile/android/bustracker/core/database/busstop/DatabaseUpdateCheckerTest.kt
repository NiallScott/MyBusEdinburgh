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
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.endpoints.api.ApiEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.api.DatabaseVersion
import uk.org.rivernile.android.bustracker.core.endpoints.api.DatabaseVersionResponse
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule

/**
 * Tests for [DatabaseUpdateChecker].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class DatabaseUpdateCheckerTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var apiEndpoint: ApiEndpoint
    @Mock
    private lateinit var databaseRepository: BusStopDatabaseRepository
    @Mock
    private lateinit var databaseUpdater: DatabaseUpdater

    private lateinit var checker: DatabaseUpdateChecker

    @Before
    fun setUp() {
        checker = DatabaseUpdateChecker(
            apiEndpoint,
            databaseRepository,
            databaseUpdater)
    }

    @Test
    fun returnFalseWhenPerformingTheApiRequestFails() = runTest {
        whenever(apiEndpoint.getDatabaseVersion(anyOrNull()))
            .thenReturn(DatabaseVersionResponse.Error.ServerError)

        val result = checker.checkForDatabaseUpdates()

        assertFalse(result)
    }

    @Test
    fun returnTrueButDontUpdateDatabaseWhenTopologyIdsAreSame() = runTest {
        val databaseVersion = createDatabaseVersion()
        whenever(apiEndpoint.getDatabaseVersion(anyOrNull()))
            .thenReturn(DatabaseVersionResponse.Success(databaseVersion))
        whenever(databaseRepository.getTopologyVersionId())
            .thenReturn("abc123")

        val result = checker.checkForDatabaseUpdates()

        assertTrue(result)
        verify(databaseUpdater, never())
            .updateDatabase(any(), anyOrNull())
    }

    @Test
    fun returnTrueWhenTopologyIdsAreDifferentAndDatabaseUpdateIsSuccessful() = runTest {
        val databaseVersion = createDatabaseVersion()
        whenever(apiEndpoint.getDatabaseVersion(anyOrNull()))
            .thenReturn(DatabaseVersionResponse.Success(databaseVersion))
        whenever(databaseRepository.getTopologyVersionId())
            .thenReturn("xyz789")
        whenever(databaseUpdater.updateDatabase(eq(databaseVersion), anyOrNull()))
            .thenReturn(true)

        val result = checker.checkForDatabaseUpdates()

        assertTrue(result)
        verify(databaseUpdater)
            .updateDatabase(eq(databaseVersion), anyOrNull())
    }

    @Test
    fun returnFalseWhenTopologyIdsAreDifferentAndDatabaseUpdateFails() = runTest {
        val databaseVersion = createDatabaseVersion()
        whenever(apiEndpoint.getDatabaseVersion(anyOrNull()))
            .thenReturn(DatabaseVersionResponse.Success(databaseVersion))
        whenever(databaseRepository.getTopologyVersionId())
            .thenReturn("xyz789")
        whenever(databaseUpdater.updateDatabase(eq(databaseVersion), anyOrNull()))
            .thenReturn(false)

        val result = checker.checkForDatabaseUpdates()

        assertFalse(result)
        verify(databaseUpdater)
            .updateDatabase(eq(databaseVersion), anyOrNull())
    }

    private fun createDatabaseVersion() =
            DatabaseVersion("MBE", "abc123", "http://host/db.db", "abcdef1234567890")
}