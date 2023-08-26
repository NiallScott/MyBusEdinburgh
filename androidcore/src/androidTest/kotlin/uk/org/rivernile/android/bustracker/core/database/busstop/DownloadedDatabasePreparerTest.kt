/*
 * Copyright (C) 2023 Niall 'Rivernile' Scott
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
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import okio.IOException
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import uk.org.rivernile.android.bustracker.core.database.busstop.migrations.Migration1To2
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule

/**
 * Tests for [DownloadedDatabasePreparer].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class DownloadedDatabasePreparerTest {

    companion object {

        private const val DB_NAME = "downloaded-database-preparer-test"
    }

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var exceptionLogger: ExceptionLogger

    private lateinit var preparer: DownloadedDatabasePreparer

    @Before
    fun setUp() {
        // This is done at the starting of the test to ensure we start with a clean slate.
        deleteExistingDatabase()

        val frameworkSQLiteOpenHelperFactory = FrameworkSQLiteOpenHelperFactory()

        val databaseOpener = DatabaseOpener(
            context,
            frameworkSQLiteOpenHelperFactory)

        val bundledDatabaseOpenHelperFactory = BundledDatabaseOpenHelperFactory(
            context,
            frameworkSQLiteOpenHelperFactory,
            databaseOpener,
            exceptionLogger)

        val databaseFactory = RoomBusStopDatabaseFactory(
            context,
            Migration1To2(),
            bundledDatabaseOpenHelperFactory)

        preparer = DownloadedDatabasePreparer(
            databaseOpener,
            databaseFactory,
            exceptionLogger,
            coroutineRule.testDispatcher)
    }

    @After
    fun tearDown() {
        // This is done again at the end of the test to clean up.
        deleteExistingDatabase()
    }

    @Test
    fun prepareDownloadedDatabaseReturnsFalseWhenDatabaseFileDoesNotExist() = runTest {
        val databaseFile = context.getDatabasePath(DB_NAME)

        assertFalse(databaseFile.exists())
        val result = preparer.prepareDownloadedDatabase(databaseFile)

        assertFalse(result)
        verify(exceptionLogger)
            .log(any<IOException>())
    }

    @Test
    fun prepareDownloadedDatabaseReturnsFalseWhenDatabaseFileIsEmpty() = runTest {
        val databaseFile = context.getDatabasePath(DB_NAME).apply {
            createNewFile()
        }

        val result = preparer.prepareDownloadedDatabase(databaseFile)

        assertFalse(result)
        verify(exceptionLogger)
            .log(any<IOException>())
    }

    @Test
    fun prepareDownloadedDatabaseReturnsFalseWhenDatabaseFileIsCorrupt() = runTest {
        val databaseFile = context.getDatabasePath(DB_NAME).apply {
            writeText("This is a corrupt file")
        }

        val result = preparer.prepareDownloadedDatabase(databaseFile)

        assertFalse(result)
        verify(exceptionLogger)
            .log(any<IOException>())
    }

    @Test
    fun prepareDownloadedDatabaseReturnsFalseWhenDatabaseDoesNotHaveExpectedSchema() = runTest {
        val databaseFile = context.getDatabasePath(DB_NAME)
        SQLiteDatabase.openDatabase(
            databaseFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY)
            .close()

        val result = preparer.prepareDownloadedDatabase(databaseFile)

        assertFalse(result)
        verify(exceptionLogger)
            .log(any<SQLException>())
    }

    @Test
    fun prepareDownloadedDatabaseReturnsFalseWhenDatabaseVersionIsTooHigh() = runTest {
        val databaseFile = context.getDatabasePath(DB_NAME)
        SQLiteDatabase.openDatabase(
            databaseFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY)
            .apply {
                version = 3
            }
            .close()

        val result = preparer.prepareDownloadedDatabase(databaseFile)

        assertFalse(result)
        verify(exceptionLogger)
            .log(any<IllegalStateException>())
    }

    @Test
    fun prepareDownloadedDatabaseReturnsFalseWhenDatabaseVersionIs2ButBadSchema() = runTest {
        val databaseFile = context.getDatabasePath(DB_NAME)
        SQLiteDatabase.openDatabase(
            databaseFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY)
            .apply {
                version = 2
            }
            .close()

        val result = preparer.prepareDownloadedDatabase(databaseFile)

        assertFalse(result)
        verify(exceptionLogger)
            .log(any<IllegalStateException>())
    }

    @Test
    fun prepareDownloadedDatabaseReturnsFalseWhenDatabaseVersionIs2WithOldSchema() = runTest {
        val databaseFile = context.getDatabasePath(DB_NAME)
        SQLiteDatabase.openDatabase(
            databaseFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY)
            .apply {
                version = 2
                createOldSchema()
            }
            .close()

        val result = preparer.prepareDownloadedDatabase(databaseFile)

        assertFalse(result)
        verify(exceptionLogger)
            .log(any<IllegalStateException>())
    }

    @Test
    fun prepareDownloadedDatabaseReturnsTrueWhenDatabaseVersionIsMissingButGoodSchema() = runTest {
        val databaseFile = context.getDatabasePath(DB_NAME)
        SQLiteDatabase.openDatabase(
            databaseFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY)
            .apply {
                createOldSchema()
            }
            .close()

        val result = preparer.prepareDownloadedDatabase(databaseFile)

        assertTrue(result)
    }

    @Test
    fun prepareDownloadedDatabaseReturnsTrueWhenDatabaseVersionIs0WithGoodSchema() = runTest {
        val databaseFile = context.getDatabasePath(DB_NAME)
        SQLiteDatabase.openDatabase(
            databaseFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY)
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
        val databaseFile = context.getDatabasePath(DB_NAME)
        SQLiteDatabase.openDatabase(
            databaseFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY)
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

    private fun deleteExistingDatabase() {
        context.deleteDatabase(DB_NAME)
    }
}