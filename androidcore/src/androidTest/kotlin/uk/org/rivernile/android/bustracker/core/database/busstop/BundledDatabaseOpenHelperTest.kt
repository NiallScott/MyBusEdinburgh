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
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import okio.IOException
import okio.use
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import uk.org.rivernile.android.bustracker.androidcore.R
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger

/**
 * Tests for [BundledDatabaseOpenHelper].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class BundledDatabaseOpenHelperTest {

    companion object {

        private const val DB_NAME = "testing-busstop-database"
        private const val ASSET_PREPACKAGED_DATABASE_PATH = "busstops10.db"
    }

    @Mock
    private lateinit var exceptionLogger: ExceptionLogger

    private lateinit var openHelper: BundledDatabaseOpenHelper

    @Before
    fun setUp() {
        // This is done at the starting of the test to ensure we start with a clean slate.
        deleteExistingDatabase()

        val dbConfiguration = SupportSQLiteOpenHelper.Configuration
            .builder(context)
            .name(DB_NAME)
            .callback(object : SupportSQLiteOpenHelper.Callback(2) {
                override fun onCreate(db: SupportSQLiteDatabase) { }

                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int) { }
            })
            .build()

        openHelper = BundledDatabaseOpenHelper(
            context,
            FrameworkSQLiteOpenHelperFactory().create(dbConfiguration),
            context.getString(R.string.asset_db_version).toLong(),
            ASSET_PREPACKAGED_DATABASE_PATH,
            exceptionLogger)
    }

    @After
    fun tearDown() {
        // This is done again at the end of the test to clean up.
        deleteExistingDatabase()
    }

    @Test
    fun ensureBundledDatabaseIsPackaged() {
        val assets = checkNotNull(context.assets.list(""))

        assertTrue(assets.contains(ASSET_PREPACKAGED_DATABASE_PATH))
    }

    @Test
    fun extractsDatabaseFromAssetsWhenDatabaseDoesNotYetExist() {
        val expectedUpdateTimestamp = context
            .getString(R.string.asset_db_version)
            .toLong()

        openHelper.readableDatabase.use { db ->
            db.query("""
                SELECT updateTS 
                FROM database_info 
                ORDER BY updateTS DESC 
                LIMIT 1
            """.trimIndent())
                .use {
                    assertEquals(1, it.count)
                    assertTrue(it.moveToFirst())
                    assertEquals(expectedUpdateTimestamp, it.getLong(0))
                }

            db.ensureExtractedDatabaseLooksSane()
            assertEquals(2, db.version)
        }

        verify(exceptionLogger, never())
            .log(any())
    }

    @Test
    fun extractsDatabaseFromAssetsWhenExistingFileIsEmpty() {
        context.getDatabasePath(DB_NAME).createNewFile()
        val expectedUpdateTimestamp = context
            .getString(R.string.asset_db_version)
            .toLong()

        openHelper.readableDatabase.use { db ->
            db.query("""
                SELECT updateTS 
                FROM database_info 
                ORDER BY updateTS DESC 
                LIMIT 1
            """.trimIndent())
                .use {
                    assertEquals(1, it.count)
                    assertTrue(it.moveToFirst())
                    assertEquals(expectedUpdateTimestamp, it.getLong(0))
                }

            db.ensureExtractedDatabaseLooksSane()
            assertEquals(2, db.version)
        }

        verify(exceptionLogger)
            .log(any<IOException>())
    }

    @Test
    fun extractsDatabaseFromAssetsWhenExistingDatabaseIsCorrupt() {
        context.getDatabasePath(DB_NAME).writeText("This is not a valid file")
        val expectedUpdateTimestamp = context
            .getString(R.string.asset_db_version)
            .toLong()

        openHelper.readableDatabase.use { db ->
            db.query("""
                SELECT updateTS 
                FROM database_info 
                ORDER BY updateTS DESC 
                LIMIT 1
            """.trimIndent())
                .use {
                    assertEquals(1, it.count)
                    assertTrue(it.moveToFirst())
                    assertEquals(expectedUpdateTimestamp, it.getLong(0))
                }

            db.ensureExtractedDatabaseLooksSane()
            assertEquals(2, db.version)
        }

        verify(exceptionLogger)
            .log(any<IOException>())
    }

    @Test
    fun extractsDatabaseFromAssetsWhenDatabaseDoesNotHaveInfoTable() {
        val expectedUpdateTimestamp = context
            .getString(R.string.asset_db_version)
            .toLong()
        val dbConfiguration = SupportSQLiteOpenHelper.Configuration
            .builder(context)
            .name(DB_NAME)
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) { }

                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int) { }
            })
            .build()
        FrameworkSQLiteOpenHelperFactory().create(dbConfiguration).writableDatabase.close()

        openHelper.readableDatabase.use { db ->
            db.query("""
                SELECT updateTS 
                FROM database_info 
                ORDER BY updateTS DESC 
                LIMIT 1
            """.trimIndent())
                .use {
                    assertEquals(1, it.count)
                    assertTrue(it.moveToFirst())
                    assertEquals(expectedUpdateTimestamp, it.getLong(0))
                }

            db.ensureExtractedDatabaseLooksSane()
            assertEquals(2, db.version)
        }

        verify(exceptionLogger)
            .log(any<SQLException>())
    }

    @Test
    fun extractsDatabaseFromAssetsWhenDatabaseHasEmptyInfoTableV1() {
        val expectedUpdateTimestamp = context
            .getString(R.string.asset_db_version)
            .toLong()
        val dbConfiguration = SupportSQLiteOpenHelper.Configuration
            .builder(context)
            .name(DB_NAME)
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("""
                        CREATE TABLE database_info (
                            _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                            current_topo_id TEXT, 
                            updateTS LONG)
                    """.trimIndent())
                }

                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int) { }
            })
            .build()
        FrameworkSQLiteOpenHelperFactory().create(dbConfiguration).writableDatabase.close()

        openHelper.readableDatabase.use { db ->
            db.query("""
                SELECT updateTS 
                FROM database_info 
                ORDER BY updateTS DESC 
                LIMIT 1
            """.trimIndent())
                .use {
                    assertEquals(1, it.count)
                    assertTrue(it.moveToFirst())
                    assertEquals(expectedUpdateTimestamp, it.getLong(0))
                }

            db.ensureExtractedDatabaseLooksSane()
            assertEquals(2, db.version)
        }
    }

    @Test
    fun extractsDatabaseFromAssetsWhenDatabaseHasEmptyInfoTableV2() {
        val expectedUpdateTimestamp = context
            .getString(R.string.asset_db_version)
            .toLong()
        val dbConfiguration = SupportSQLiteOpenHelper.Configuration
            .builder(context)
            .name(DB_NAME)
            .callback(object : SupportSQLiteOpenHelper.Callback(2) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS database_info (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                            topologyId TEXT, 
                            updateTimestamp INTEGER NOT NULL)
                    """.trimIndent())
                }

                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int) { }
            })
            .build()
        FrameworkSQLiteOpenHelperFactory().create(dbConfiguration).writableDatabase.close()

        openHelper.readableDatabase.use { db ->
            db.query("""
                SELECT updateTS 
                FROM database_info 
                ORDER BY updateTS DESC 
                LIMIT 1
            """.trimIndent())
                .use {
                    assertEquals(1, it.count)
                    assertTrue(it.moveToFirst())
                    assertEquals(expectedUpdateTimestamp, it.getLong(0))
                }

            db.ensureExtractedDatabaseLooksSane()
            assertEquals(2, db.version)
        }
    }

    @Test
    fun doesNotExtractDatabaseFromAssetsWhenNewerDatabaseIsPresentV1() {
        val expectedUpdateTimestamp = context
            .getString(R.string.asset_db_version)
            .toLong() + 1L
        val dbConfiguration = SupportSQLiteOpenHelper.Configuration
            .builder(context)
            .name(DB_NAME)
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("""
                        CREATE TABLE database_info (
                            _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                            current_topo_id TEXT, 
                            updateTS LONG)
                    """.trimIndent())
                    db.execSQL("""
                        INSERT INTO database_info (
                            current_topo_id, updateTS)
                        VALUES (
                            'topoId', ?)
                    """.trimIndent(), arrayOf(expectedUpdateTimestamp))
                }

                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int) { }
            })
            .build()
        FrameworkSQLiteOpenHelperFactory().create(dbConfiguration).writableDatabase.close()

        openHelper.readableDatabase.use { db ->
            db.query("""
                SELECT updateTS 
                FROM database_info 
                ORDER BY updateTS DESC 
                LIMIT 1
            """.trimIndent())
                .use {
                    assertEquals(1, it.count)
                    assertTrue(it.moveToFirst())
                    assertEquals(expectedUpdateTimestamp, it.getLong(0))
                }

            assertEquals(2, db.version)
        }

        verify(exceptionLogger, never())
            .log(any())
    }

    @Test
    fun doesNotExtractDatabaseFromAssetsWhenNewerDatabaseIsPresentV2() {
        val expectedUpdateTimestamp = context
            .getString(R.string.asset_db_version)
            .toLong() + 1L
        val dbConfiguration = SupportSQLiteOpenHelper.Configuration
            .builder(context)
            .name(DB_NAME)
            .callback(object : SupportSQLiteOpenHelper.Callback(2) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS database_info (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                            topologyId TEXT, 
                            updateTimestamp INTEGER NOT NULL)
                    """.trimIndent())
                    db.execSQL("""
                        INSERT INTO database_info (
                            topologyId, updateTimestamp)
                        VALUES (
                            'topoId', ?)
                    """.trimIndent(), arrayOf(expectedUpdateTimestamp))
                }

                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int) { }
            })
            .build()
        FrameworkSQLiteOpenHelperFactory().create(dbConfiguration).writableDatabase.close()

        openHelper.readableDatabase.use { db ->
            db.query("""
                SELECT updateTimestamp 
                FROM database_info 
                ORDER BY updateTimestamp DESC 
                LIMIT 1
            """.trimIndent())
                .use {
                    assertEquals(1, it.count)
                    assertTrue(it.moveToFirst())
                    assertEquals(expectedUpdateTimestamp, it.getLong(0))
                }

            assertEquals(2, db.version)
        }

        verify(exceptionLogger, never())
            .log(any())
    }

    private fun deleteExistingDatabase() {
        context.deleteDatabase(DB_NAME)
    }

    private fun SupportSQLiteDatabase.ensureExtractedDatabaseLooksSane() {
        query("SELECT COUNT(*) FROM bus_stops").use {
            assertTrue(it.moveToFirst())
            assertTrue(it.getInt(0) > 1)
        }

        query("SELECT COUNT(*) FROM service").use {
            assertTrue(it.moveToFirst())
            assertTrue(it.getInt(0) > 1)
        }

        query("SELECT COUNT(*) FROM service_colour").use {
            assertTrue(it.moveToFirst())
            assertTrue(it.getInt(0) > 1)
        }

        query("SELECT COUNT(*) FROM service_point").use {
            assertTrue(it.moveToFirst())
            assertTrue(it.getInt(0) > 1)
        }

        query("SELECT COUNT(*) FROM service_stops").use {
            assertTrue(it.moveToFirst())
            assertTrue(it.getInt(0) > 1)
        }
    }

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
}