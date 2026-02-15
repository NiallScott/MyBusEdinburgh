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

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [AndroidBusStopDatabase].
 *
 * @author Niall Scott
 */
class AndroidBusStopDatabaseTest {

    companion object {

        private const val DATABASE_NAME = "busstops23.db"
    }

    @Test
    fun replaceDatabaseWhenNewDatabaseCouldNotBePreparedReturnsFalse() = runTest {
        var deleteCount = 0
        val testDbFile = FakeBusStopDatabaseFile(
            name = "test",
            onDelete = {
                deleteCount++
                true
            }
        )
        val existingDb = createRoomBusStopDatabase()
        val database = createDatabase(
            databaseFactory = FakeRoomBusStopDatabaseFactory(
                onCreateRoomBusStopDatabase = { databaseName, allowAssetExtraction ->
                    assertEquals(DATABASE_NAME, databaseName)
                    assertTrue(allowAssetExtraction)
                    existingDb
                }
            ),
            downloadedDatabasePreparer = FakeDownloadedDatabasePreparer(
                onPrepareDownloadedDatabase = {
                    assertEquals(testDbFile, it)
                    false
                }
            )
        )

        try {
            database.isDatabaseOpenFlow.test {
                val result = database.replaceDatabase(testDbFile)

                assertFalse(result)
                assertTrue(awaitItem())
                ensureAllEventsConsumed()
            }
            assertEquals(1, deleteCount)
        } finally {
            existingDb.close()
        }
    }

    @Test
    fun replaceDatabaseWhenNewDatabasePassesPreparationReturnsTrue() = runTest {
        var deleteCount = 0
        val renameToOperations = mutableListOf<File>()
        val testDb = FakeBusStopDatabaseFile(
            name = "test",
            onDelete = {
                deleteCount++
                true
            },
            onRenameTo = {
                renameToOperations += it
                true
            }
        )
        val existingDb = createRoomBusStopDatabase()
        val newDb = createRoomBusStopDatabase()
        val databases = ArrayDeque(listOf(existingDb, newDb))
        val database = createDatabase(
            databaseFactory = FakeRoomBusStopDatabaseFactory(
                onCreateRoomBusStopDatabase = { databaseName, allowAssetExtraction ->
                    assertEquals(DATABASE_NAME, databaseName)
                    assertTrue(allowAssetExtraction)
                    databases.removeFirst()
                }
            ),
            downloadedDatabasePreparer = FakeDownloadedDatabasePreparer(
                onPrepareDownloadedDatabase = {
                    assertEquals(testDb, it)
                    true
                }
            )
        )

        try {
            database.isDatabaseOpenFlow.test {
                val result = database.replaceDatabase(testDb)

                assertTrue(result)
                assertTrue(awaitItem())
                assertFalse(awaitItem())
                assertTrue(awaitItem())
                ensureAllEventsConsumed()
            }
            assertFalse(existingDb.isOpen)
            assertEquals(0, deleteCount)
            assertEquals(
                listOf(context.getDatabasePath(DATABASE_NAME)),
                renameToOperations
            )
            assertTrue(databases.isEmpty())
        } finally {
            existingDb.close()
            newDb.close()
        }
    }

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.createDatabase(
        databaseFactory: RoomBusStopDatabaseFactory = FakeRoomBusStopDatabaseFactory(),
        downloadedDatabasePreparer: DownloadedDatabasePreparer = FakeDownloadedDatabasePreparer()
    ): AndroidBusStopDatabase {
        return AndroidBusStopDatabase(
            context,
            databaseFactory,
            downloadedDatabasePreparer,
            UnconfinedTestDispatcher(testScheduler)
        )
    }

    private fun createRoomBusStopDatabase(): RoomBusStopDatabase {
        return Room.inMemoryDatabaseBuilder<RoomBusStopDatabase>(context).build()
    }
}
