/*
 * Copyright (C) 2023 - 2026 Niall 'Rivernile' Scott
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

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.database.busstop.database.DatabaseDao
import uk.org.rivernile.android.bustracker.core.database.busstop.database.FakeDatabaseDao
import uk.org.rivernile.android.bustracker.core.database.busstop.database.FakeDatabaseMetadata
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

/**
 * Tests for [AndroidBusStopDatabaseRepository].
 *
 * @author Niall Scott
 */
class AndroidBusStopDatabaseRepositoryTest {

    @Test
    fun replaceDatabaseReturnsFalseWhenReplaceDatabaseReturnsFalse() = runTest {
        val fakeFile = File("/fake/file/path.db")
        val repository = createRepository(
            databaseReplacer = FakeDatabaseReplacer(
                onReplaceDatabase = {
                    assertEquals(fakeFile.toBusStopDatabaseFile(), it)
                    false
                }
            )
        )

        val result = repository.replaceDatabase(fakeFile)

        assertFalse(result)
    }

    @Test
    fun replaceDatabaseReturnsTrueWhenReplaceDatabaseReturnsTrue() = runTest {
        val fakeFile = File("/fake/file/path.db")
        val repository = createRepository(
            databaseReplacer = FakeDatabaseReplacer(
                onReplaceDatabase = {
                    assertEquals(fakeFile.toBusStopDatabaseFile(), it)
                    true
                }
            )
        )

        val result = repository.replaceDatabase(fakeFile)

        assertTrue(result)
    }

    @Test
    fun databaseMetadataFlowReturnsFlowFromDatabaseInformationDao() = runTest {
        val databaseMetadata = FakeDatabaseMetadata(
            updateTimestamp = Instant.fromEpochMilliseconds(123L)
        )
        val repository = createRepository(
            databaseDao = FakeDatabaseDao(
                onDatabaseMetadataFlow = { flowOf(databaseMetadata) }
            )
        )

        repository.databaseMetadataFlow.test {
            assertEquals(databaseMetadata, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getDatabaseUpdateTimestampReturnsValueFromDatabaseDao() = runTest {
        val repository = createRepository(
            databaseDao = FakeDatabaseDao(
                onGetDatabaseUpdateTimestamp = { Instant.fromEpochMilliseconds(123L) }
            )
        )

        val result = repository.getDatabaseUpdateTimestamp()

        assertEquals(
            Instant.fromEpochMilliseconds(123L),
            result
        )
    }

    private fun createRepository(
       databaseReplacer: DatabaseReplacer = FakeDatabaseReplacer(),
        databaseDao: DatabaseDao = FakeDatabaseDao()
    ): AndroidBusStopDatabaseRepository {
        return AndroidBusStopDatabaseRepository(
            databaseReplacer = databaseReplacer,
            databaseDao = databaseDao
        )
    }
}
