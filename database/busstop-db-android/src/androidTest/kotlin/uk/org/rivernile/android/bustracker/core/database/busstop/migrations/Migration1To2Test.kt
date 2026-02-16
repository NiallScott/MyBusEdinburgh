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

package uk.org.rivernile.android.bustracker.core.database.busstop.migrations

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import uk.org.rivernile.android.bustracker.core.database.OldDatabaseCreator
import uk.org.rivernile.android.bustracker.core.database.busstop.RoomBusStopDatabase
import uk.org.rivernile.android.bustracker.core.database.busstop.preProcessedSchemaCreateStatements
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [Migration1To2].
 *
 * @author Niall Scott
 */
class Migration1To2Test {

    companion object {

        private const val TEST_DB = "stopdb-migration-test"
    }

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        RoomBusStopDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    private lateinit var oldDatabaseCreator: OldDatabaseCreator

    @BeforeTest
    fun setUp() {
        oldDatabaseCreator = OldDatabaseCreator()
    }

    @Test
    fun ensureIndicesCreatedWhenIndicesDoNotAlreadyExist() {
        openOldDatabase().close()

        runMigrationsAndValidate().apply {
            assertIndicesExist()
        }
    }

    @Test
    fun ensureViewsCreatedWhenViewsDoNotAlreadyExist() {
        openOldDatabase().close()

        runMigrationsAndValidate().apply {
            assertViewsExist()
        }
    }

    private fun openOldDatabase(
        name: String = TEST_DB,
        callbacks: SupportSQLiteOpenHelper.Callback = openHelperCallback
    ) = oldDatabaseCreator.openDatabase(name, callbacks)

    private fun runMigrationsAndValidate(
        name: String = TEST_DB,
        validateDroppedTables: Boolean = true
    ) = helper.runMigrationsAndValidate(name, 2, validateDroppedTables, Migration1To2())

    private fun SupportSQLiteDatabase.assertIndicesExist() {
        query("PRAGMA index_info(service_index)").use {
            assertTrue(it.count > 0)
        }

        query("PRAGMA index_info(service_point_index)").use {
            assertTrue(it.count > 0)
        }

        query("PRAGMA index_info(service_stop_service_id_index)").use {
            assertTrue(it.count > 0)
        }

        query("PRAGMA index_info(service_stop_stop_id_index)").use {
            assertTrue(it.count > 0)
        }

        query("PRAGMA index_info(stop_naptan_code_index)").use {
            assertTrue(it.count > 0)
        }

        query("PRAGMA index_info(stop_atco_code_index)").use {
            assertTrue(it.count > 0)
        }
    }

    private fun SupportSQLiteDatabase.assertViewsExist() {
        query("""
            SELECT name
            FROM sqlite_master
            WHERE type = 'view'
            ORDER BY name ASC
        """.trimIndent()
        ).use {
            assertEquals(1, it.count)
            assertTrue(it.moveToNext())
            assertEquals("service_view", it.getString(0))
        }
    }

    private fun SupportSQLiteDatabase.createBaseSchema() {
        preProcessedSchemaCreateStatements()
            .forEach(::execSQL)
    }

    private val openHelperCallback get() = object : SupportSQLiteOpenHelper.Callback(1) {
        override fun onCreate(db: SupportSQLiteDatabase) {
            db.createBaseSchema()
        }

        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {

        }
    }
}
