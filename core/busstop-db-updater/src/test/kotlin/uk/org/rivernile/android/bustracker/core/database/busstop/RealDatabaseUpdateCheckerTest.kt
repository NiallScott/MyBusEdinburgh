/*
 * Copyright (C) 2022 - 2025 Niall 'Rivernile' Scott
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
import uk.org.rivernile.android.bustracker.core.endpoints.api.ApiEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.api.DatabaseVersion
import uk.org.rivernile.android.bustracker.core.endpoints.api.DatabaseVersionResponse
import uk.org.rivernile.android.bustracker.core.endpoints.api.FakeApiEndpoint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Tests for [DatabaseUpdateChecker].
 *
 * @author Niall Scott
 */
class RealDatabaseUpdateCheckerTest {

    @Test
    fun returnFalseWhenPerformingTheApiRequestFails() = runTest {
        val checker = createDatabaseUpdaterChecker(
            apiEndpoint = FakeApiEndpoint(
                onGetDatabaseVersion = { DatabaseVersionResponse.Error.ServerError }
            )
        )

        val result = checker.checkForDatabaseUpdates()

        assertFalse(result)
    }

    @Test
    fun returnTrueButDontUpdateDatabaseWhenTopologyIdsAreSame() = runTest {
        val databaseVersion = createDatabaseVersion()
        val checker = createDatabaseUpdaterChecker(
            apiEndpoint = FakeApiEndpoint(
                onGetDatabaseVersion = { DatabaseVersionResponse.Success(databaseVersion) }
            ),
            databaseRepository = FakeBusStopDatabaseRepository(
                onGetTopologyVersionId = { "abc123" }
            ),
            databaseUpdater = FakeDatabaseUpdater(
                onUpdateDatabase = { _, _ -> fail("Database should not be updated.") }
            )
        )

        val result = checker.checkForDatabaseUpdates()

        assertTrue(result)
    }

    @Test
    fun returnTrueWhenTopologyIdsAreDifferentAndDatabaseUpdateIsSuccessful() = runTest {
        val databaseVersion = createDatabaseVersion()
        var updateDatabaseInvocationCount = 0
        val checker = createDatabaseUpdaterChecker(
            apiEndpoint = FakeApiEndpoint(
                onGetDatabaseVersion = { DatabaseVersionResponse.Success(databaseVersion) }
            ),
            databaseRepository = FakeBusStopDatabaseRepository(
                onGetTopologyVersionId = { "xyz789" }
            ),
            databaseUpdater = FakeDatabaseUpdater(
                onUpdateDatabase = { version, _ ->
                    assertEquals(databaseVersion, version)
                    updateDatabaseInvocationCount++
                    true
                }
            )
        )

        val result = checker.checkForDatabaseUpdates()

        assertTrue(result)
        assertEquals(1, updateDatabaseInvocationCount)
    }

    @Test
    fun returnFalseWhenTopologyIdsAreDifferentAndDatabaseUpdateFails() = runTest {
        val databaseVersion = createDatabaseVersion()
        var updateDatabaseInvocationCount = 0
        val checker = createDatabaseUpdaterChecker(
            apiEndpoint = FakeApiEndpoint(
                onGetDatabaseVersion = { DatabaseVersionResponse.Success(databaseVersion) }
            ),
            databaseRepository = FakeBusStopDatabaseRepository(
                onGetTopologyVersionId = { "xyz789" }
            ),
            databaseUpdater = FakeDatabaseUpdater(
                onUpdateDatabase = { version, _ ->
                    assertEquals(databaseVersion, version)
                    updateDatabaseInvocationCount++
                    false
                }
            )
        )

        val result = checker.checkForDatabaseUpdates()

        assertFalse(result)
        assertEquals(1, updateDatabaseInvocationCount)
    }

    private fun createDatabaseUpdaterChecker(
        apiEndpoint: ApiEndpoint = FakeApiEndpoint(),
        databaseRepository: BusStopDatabaseRepository = FakeBusStopDatabaseRepository(),
        databaseUpdater: DatabaseUpdater = FakeDatabaseUpdater()
    ): DatabaseUpdateChecker {
        return RealDatabaseUpdateChecker(
            apiEndpoint,
            databaseRepository,
            databaseUpdater
        )
    }

    private fun createDatabaseVersion() =
        DatabaseVersion("MBE", "abc123", "http://host/db.db", "abcdef1234567890")
}