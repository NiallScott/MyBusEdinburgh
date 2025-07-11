/*
 * Copyright (C) 2023 - 2025 Niall 'Rivernile' Scott
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

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.rules.DeleteFilesRule
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.IOException
import org.junit.Rule
import uk.org.rivernile.android.bustracker.core.database.busstop.migrations.Migration1To2
import uk.org.rivernile.android.bustracker.core.log.FakeExceptionLogger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for [DownloadedDatabasePreparer].
 *
 * @author Niall Scott
 */
class RealDownloadedDatabasePreparerTest {

    companion object {

        private const val DB_NAME = "downloaded-database-preparer-test"
    }

    @get:Rule
    val deleteFilesRule = DeleteFilesRule()

    private val exceptionLogger = FakeExceptionLogger()

    @Test
    fun prepareDownloadedDatabaseReturnsFalseWhenDatabaseFileDoesNotExist() = runTest {
        val preparer = createDownloadedDatabasePreparer()
        val databaseFile = context.getDatabasePath(DB_NAME).toBusStopDatabaseFile()

        assertFalse(databaseFile.exists())
        val result = preparer.prepareDownloadedDatabase(databaseFile)

        assertFalse(result)
        with(exceptionLogger.loggedThrowables) {
            assertEquals(1, size)
            assertIs<IOException>(last())
        }
    }

    @Test
    fun prepareDownloadedDatabaseReturnsFalseWhenDatabaseFileIsEmpty() = runTest {
        val preparer = createDownloadedDatabasePreparer()
        val databaseFile = context
            .getDatabasePath(DB_NAME).apply {
                createNewFile()
            }
            .toBusStopDatabaseFile()

        val result = preparer.prepareDownloadedDatabase(databaseFile)

        assertFalse(result)
        with(exceptionLogger.loggedThrowables) {
            assertEquals(1, size)
            assertIs<IOException>(last())
        }
    }

    @Test
    fun prepareDownloadedDatabaseReturnsFalseWhenDatabaseFileIsCorrupt() = runTest {
        val preparer = createDownloadedDatabasePreparer()
        val databaseFile = context
            .getDatabasePath(DB_NAME)
            .apply {
                writeText("This is a corrupt file")
            }
            .toBusStopDatabaseFile()

        val result = preparer.prepareDownloadedDatabase(databaseFile)

        assertFalse(result)
        with(exceptionLogger.loggedThrowables) {
            assertEquals(1, size)
            assertIs<IOException>(last())
        }
    }

    @Test
    fun prepareDownloadedDatabaseReturnsFalseWhenDatabaseDoesNotHaveExpectedSchema() = runTest {
        val preparer = createDownloadedDatabasePreparer()
        val databaseFile = context.getDatabasePath(DB_NAME).toBusStopDatabaseFile()
        SQLiteDatabase
            .openDatabase(
                databaseFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY
            )
            .close()

        val result = preparer.prepareDownloadedDatabase(databaseFile)

        assertFalse(result)
        with(exceptionLogger.loggedThrowables) {
            assertEquals(1, size)
            assertIs<SQLException>(last())
        }
    }

    @Test
    fun prepareDownloadedDatabaseReturnsFalseWhenDatabaseVersionIsTooHigh() = runTest {
        val preparer = createDownloadedDatabasePreparer()
        val databaseFile = context.getDatabasePath(DB_NAME).toBusStopDatabaseFile()
        SQLiteDatabase
            .openDatabase(
                databaseFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY
            )
            .apply {
                version = 3
            }
            .close()

        val result = preparer.prepareDownloadedDatabase(databaseFile)

        assertFalse(result)
        with(exceptionLogger.loggedThrowables) {
            assertEquals(1, size)
            assertIs<IllegalStateException>(last())
        }
    }

    @Test
    fun prepareDownloadedDatabaseReturnsFalseWhenDatabaseVersionIs2ButBadSchema() = runTest {
        val preparer = createDownloadedDatabasePreparer()
        val databaseFile = context.getDatabasePath(DB_NAME).toBusStopDatabaseFile()
        SQLiteDatabase
            .openDatabase(
                databaseFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY
            )
            .apply {
                version = 2
            }
            .close()

        val result = preparer.prepareDownloadedDatabase(databaseFile)

        assertFalse(result)
        with(exceptionLogger.loggedThrowables) {
            assertEquals(1, size)
            assertIs<IllegalStateException>(last())
        }
    }

    @Test
    fun prepareDownloadedDatabaseReturnsFalseWhenDatabaseVersionIs2WithOldSchema() = runTest {
        val preparer = createDownloadedDatabasePreparer()
        val databaseFile = context.getDatabasePath(DB_NAME).toBusStopDatabaseFile()
        SQLiteDatabase
            .openDatabase(
                databaseFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY
            )
            .apply {
                version = 2
                createOldSchema()
            }
            .close()

        val result = preparer.prepareDownloadedDatabase(databaseFile)

        assertFalse(result)
        with(exceptionLogger.loggedThrowables) {
            assertEquals(1, size)
            assertIs<IllegalStateException>(last())
        }
    }

    @Test
    fun prepareDownloadedDatabaseReturnsTrueWhenDatabaseVersionIsMissingButGoodSchema() = runTest {
        val preparer = createDownloadedDatabasePreparer()
        val databaseFile = context.getDatabasePath(DB_NAME).toBusStopDatabaseFile()
        SQLiteDatabase
            .openDatabase(
                databaseFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY
            )
            .apply {
                createOldSchema()
            }
            .close()

        val result = preparer.prepareDownloadedDatabase(databaseFile)

        assertTrue(result)
    }

    @Test
    fun prepareDownloadedDatabaseReturnsTrueWhenDatabaseVersionIs0WithGoodSchema() = runTest {
        val preparer = createDownloadedDatabasePreparer()
        val databaseFile = context.getDatabasePath(DB_NAME).toBusStopDatabaseFile()
        SQLiteDatabase
            .openDatabase(
                databaseFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY
            )
            .apply {
                version = 0
                createOldSchema()
            }
            .close()

        val result = preparer.prepareDownloadedDatabase(databaseFile)

        assertTrue(result)
    }

    @Test
    fun prepareDownloadedDatabaseReturnsTrueWhenDatabaseVersionIs1WithGoodSchema() = runTest {
        val preparer = createDownloadedDatabasePreparer()
        val databaseFile = context.getDatabasePath(DB_NAME).toBusStopDatabaseFile()
        SQLiteDatabase
            .openDatabase(
                databaseFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY
            )
            .apply {
                version = 1
                createOldSchema()
            }
            .close()

        val result = preparer.prepareDownloadedDatabase(databaseFile)

        assertTrue(result)
    }

    private fun SQLiteDatabase.createOldSchema() {
        execSQL("""
            CREATE TABLE bus_stops (
                _id INTEGER PRIMARY KEY,
                stopCode TEXT,
                stopName TEXT,
                x REAL,
                y REAL,
                orientation INTEGER,
                locality TEXT)
        """.trimIndent())

        execSQL("""
            CREATE TABLE database_info (
                _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                current_topo_id TEXT,
                updateTS LONG)
        """.trimIndent())

        execSQL("""
            CREATE TABLE service (
                _id INTEGER,
                name TEXT,
                `desc` TEXT)
        """.trimIndent())

        execSQL("""
            CREATE TABLE service_colour (
                _id INTEGER,
                hex_colour TEXT)
        """.trimIndent())

        execSQL("""
            CREATE TABLE service_point (
                _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                service_id INTEGER,
                stop_id INTEGER,
                order_value INTEGER,
                chainage INTEGER,
                latitude REAL,
                longitude REAL)
        """.trimIndent())

        execSQL("""
            CREATE TABLE service_stops (
                _id INTEGER PRIMARY KEY AUTOINCREMENT,
                stopCode TEXT,
                serviceName TEXT)
        """.trimIndent())
    }

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.createDownloadedDatabasePreparer(): RealDownloadedDatabasePreparer {
        val context = context
        val frameworkSQLiteOpenHelperFactory = FrameworkSQLiteOpenHelperFactory()
        val databaseOpener = DatabaseOpener(
            context = context,
            frameworkSQLiteOpenHelperFactory = frameworkSQLiteOpenHelperFactory
        )

        return RealDownloadedDatabasePreparer(
            databaseOpener = databaseOpener,
            databaseFactory = RealRoomBusStopDatabaseFactory(
                context = context,
                migration1To2 = Migration1To2(),
                bundledDatabaseOpenHelperFactory = BundledDatabaseOpenHelperFactory(
                    context = context,
                    frameworkSQLiteOpenHelperFactory = frameworkSQLiteOpenHelperFactory,
                    databaseOpener = databaseOpener,
                    exceptionLogger = exceptionLogger
                )
            ),
            exceptionLogger = exceptionLogger,
            ioDispatcher = UnconfinedTestDispatcher(testScheduler)
        )
    }
}