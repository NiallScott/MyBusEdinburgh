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

import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.rules.DeleteFilesRule
import androidx.test.platform.app.InstrumentationRegistry
import okio.IOException
import org.junit.Rule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Tests for [DatabaseOpener].
 *
 * @author Niall Scott
 */
class DatabaseOpenerTest {

    companion object {

        private const val DB_NAME = "database-opener-test"
    }

    @get:Rule
    val deleteFilesRule = DeleteFilesRule()

    private lateinit var opener: DatabaseOpener

    @BeforeTest
    fun setUp() {
        opener = DatabaseOpener(
            context,
            FrameworkSQLiteOpenHelperFactory()
        )
    }

    @Test(expected = IOException::class)
    fun createOpenHelperThrowsIoExceptionWhenDatabaseFileDoesNotExist() {
        val databaseFile = context.getDatabasePath(DB_NAME).toBusStopDatabaseFile()

        assertFalse(databaseFile.exists())
        opener.createOpenHelper(databaseFile).readableDatabase.close()
    }

    @Test(expected = IOException::class)
    fun createOpenHelperThrowsIoExceptionWhenDatabaseFileIsEmpty() {
        val databaseFile = context
            .getDatabasePath(DB_NAME)
            .apply {
                createNewFile()
            }
            .toBusStopDatabaseFile()

        opener.createOpenHelper(databaseFile).readableDatabase.close()
    }

    @Test(expected = IOException::class)
    fun createOpenHelperThrowsIoExceptionWhenDatabaseFileIsCorrupt() {
        val databaseFile = context
            .getDatabasePath(DB_NAME)
            .apply {
                writeText("This is not a valid file")
            }
            .toBusStopDatabaseFile()

        opener.createOpenHelper(databaseFile).readableDatabase.close()
    }

    @Test
    fun createOpenHelperSetsTheDatabaseVersionTo1WhenNoVersionWasFound() {
        val databaseFile = context.getDatabasePath(DB_NAME).toBusStopDatabaseFile()
        SQLiteDatabase
            .openDatabase(
                databaseFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY
            )
            .close()

        opener.createOpenHelper(databaseFile)
            .readableDatabase
            .use {
                assertEquals(1, it.version)
            }
    }

    @Test
    fun createOpenHelperSetsTheDatabaseVersionTo1WhenVersionIs0() {
        val databaseFile = context.getDatabasePath(DB_NAME).toBusStopDatabaseFile()
        SQLiteDatabase
            .openDatabase(
                databaseFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY
            )
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
        val databaseFile = context.getDatabasePath(DB_NAME).toBusStopDatabaseFile()
        SQLiteDatabase
            .openDatabase(
                databaseFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY
            )
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
        val databaseFile = context.getDatabasePath(DB_NAME).toBusStopDatabaseFile()
        SQLiteDatabase
            .openDatabase(
                databaseFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY
            )
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
}