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

import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import okio.IOException
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

/**
 * Tests for [DatabaseOpener].
 *
 * @author Niall Scott
 */
class DatabaseOpenerTest {

    companion object {

        private const val DB_NAME = "database-opener-test"
    }

    private lateinit var opener: DatabaseOpener

    @Before
    fun setUp() {
        // This is done at the starting of the test to ensure we start with a clean slate.
        deleteExistingDatabase()

        opener = DatabaseOpener(
            context,
            FrameworkSQLiteOpenHelperFactory())
    }

    @After
    fun tearDown() {
        // This is done again at the end of the test to clean up.
        deleteExistingDatabase()
    }

    @Test(expected = IOException::class)
    fun createOpenHelperThrowsIoExceptionWhenDatabaseFileDoesNotExist() {
        val databaseFile = context.getDatabasePath(DB_NAME)

        assertFalse(databaseFile.exists())
        opener.createOpenHelper(databaseFile).readableDatabase.close()
    }

    @Test(expected = IOException::class)
    fun createOpenHelperThrowsIoExceptionWhenDatabaseFileIsEmpty() {
        val databaseFile = context.getDatabasePath(DB_NAME)
        databaseFile.createNewFile()

        opener.createOpenHelper(databaseFile).readableDatabase.close()
    }

    @Test(expected = IOException::class)
    fun createOpenHelperThrowsIoExceptionWhenDatabaseFileIsCorrupt() {
        val databaseFile = context.getDatabasePath(DB_NAME)
        databaseFile.writeText("This is not a valid file")

        opener.createOpenHelper(databaseFile).readableDatabase.close()
    }

    @Test
    fun createOpenHelperSetsTheDatabaseVersionTo1WhenNoVersionWasFound() {
        val databaseFile = context.getDatabasePath(DB_NAME)
        SQLiteDatabase.openDatabase(
            databaseFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY)
            .close()

        opener.createOpenHelper(databaseFile)
            .readableDatabase
            .use {
                assertEquals(1, it.version)
            }
    }

    @Test
    fun createOpenHelperSetsTheDatabaseVersionTo1WhenVersionIs0() {
        val databaseFile = context.getDatabasePath(DB_NAME)
        SQLiteDatabase.openDatabase(
            databaseFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY)
            .use {
                it.version = 0
            }

        opener.createOpenHelper(databaseFile)
            .readableDatabase
            .use {
                assertEquals(1, it.version)
            }
    }

    @Test
    fun createOpenHelperLeavesTheDatabaseVersionAt1WhenTheVersionIsAlready1() {
        val databaseFile = context.getDatabasePath(DB_NAME)
        SQLiteDatabase.openDatabase(
            databaseFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY)
            .use {
                it.version = 1
            }

        opener.createOpenHelper(databaseFile)
            .readableDatabase
            .use {
                assertEquals(1, it.version)
            }
    }

    @Test
    fun createOpenHelperLeavesTheDatabaseVersionAt2WhenTheVersionIs2() {
        val databaseFile = context.getDatabasePath(DB_NAME)
        SQLiteDatabase.openDatabase(
            databaseFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY)
            .use {
                it.version = 2
            }

        opener.createOpenHelper(databaseFile)
            .readableDatabase
            .use {
                assertEquals(2, it.version)
            }
    }

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun deleteExistingDatabase() {
        context.deleteDatabase(DB_NAME)
    }
}