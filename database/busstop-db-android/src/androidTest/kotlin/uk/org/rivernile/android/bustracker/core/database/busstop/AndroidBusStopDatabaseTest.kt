/*
 * Copyright (C) 2023 - 2024 Niall 'Rivernile' Scott
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

import android.os.Build
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import uk.org.rivernile.android.bustracker.coroutines.test
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [AndroidBusStopDatabase].
 *
 * @author Niall Scott
 */
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.P) // Because Mockk.
@OptIn(ExperimentalCoroutinesApi::class)
class AndroidBusStopDatabaseTest {

    companion object {

        private const val DATABASE_NAME = "busstops10.db"
    }

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var databaseFactory: RoomBusStopDatabaseFactory
    @MockK
    private lateinit var downloadedDatabasePreparer: DownloadedDatabasePreparer

    @Test
    fun replaceDatabaseWhenNewDatabaseCouldNotBePreparedReturnsFalse() = runTest {
        val testDb = mockk<File>(relaxed = true)
        val existingDb = mockk<RoomBusStopDatabase>()
        every { databaseFactory.createRoomBusStopDatabase(DATABASE_NAME, true) } returns existingDb
        coEvery { downloadedDatabasePreparer.prepareDownloadedDatabase(testDb) } returns false
        val database = createDatabase()

        val isDatabaseOpenObserver = database.isDatabaseOpenFlow.test(this)
        val result = database.replaceDatabase(testDb)
        isDatabaseOpenObserver.finish()

        assertFalse(result)
        verify(exactly = 1) {
            testDb.delete()
        }
        isDatabaseOpenObserver.assertValues(true)
    }

    @Test
    fun replaceDatabaseWhenNewDatabasePassesPreparationReturnsTrue() = runTest {
        val testDb = mockk<File>(relaxed = true)
        val existingDb = mockk<RoomBusStopDatabase>(relaxed = true)
        val newDb = mockk<RoomBusStopDatabase>()
        every {
            databaseFactory.createRoomBusStopDatabase(DATABASE_NAME, true)
        } returnsMany listOf(existingDb, newDb)
        coEvery { downloadedDatabasePreparer.prepareDownloadedDatabase(testDb) } returns true
        val database = createDatabase()

        val isDatabaseOpenObserver = database.isDatabaseOpenFlow.test(this)
        advanceUntilIdle()
        val result = database.replaceDatabase(testDb)
        advanceUntilIdle()
        isDatabaseOpenObserver.finish()

        assertTrue(result)
        verify(exactly = 1) {
            existingDb.close()
        }
        verify(exactly = 0) {
            testDb.delete()
        }
        verify(exactly = 1) {
            testDb.renameTo(context.getDatabasePath(DATABASE_NAME))
        }
        isDatabaseOpenObserver.assertValues(true, false, true)
    }

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun TestScope.createDatabase(): AndroidBusStopDatabase {
        return AndroidBusStopDatabase(
            context,
            databaseFactory,
            downloadedDatabasePreparer,
            StandardTestDispatcher(testScheduler)
        )
    }
}