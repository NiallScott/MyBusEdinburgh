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

package uk.org.rivernile.android.bustracker.core.database.settings.migrations

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.org.rivernile.android.bustracker.core.database.OldDatabaseCreator
import uk.org.rivernile.android.bustracker.core.database.settings.RoomSettingsDatabase
import uk.org.rivernile.android.bustracker.core.database.settings.assertAlertTriggersExistUpToVersion4

/**
 * Tests for [Migration2To4].
 *
 * @author Niall Scott
 */
class Migration2To4Test {

    companion object {

        private const val TEST_DB = "migration-test"
    }

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        RoomSettingsDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    private lateinit var oldDatabaseCreator: OldDatabaseCreator

    @Before
    fun setUp() {
        oldDatabaseCreator = OldDatabaseCreator()
    }

    @Test
    fun migrate2To4Empty() {
        oldDatabaseCreator.openDatabase(TEST_DB, openHelperCallback).close()

        helper.runMigrationsAndValidate(TEST_DB, 4, true, Migration2To4()).use { database ->
            database.assertAlertTriggersExistUpToVersion4()

            database.query("SELECT * FROM active_alerts").use {
                assertEquals(0, it.count)
            }

            database.query("SELECT * FROM favourite_stops").use {
                assertEquals(0, it.count)
            }
        }
    }

    @Test
    fun migrate2To4WithData() {
        oldDatabaseCreator.openDatabase(TEST_DB, openHelperCallback).use { database ->
            database.execSQL("""
                INSERT INTO favourite_stops (_id, stopName)
                VALUES ('111111', 'Stop 1')
            """.trimIndent())

            database.execSQL("""
                INSERT INTO favourite_stops (_id, stopName)
                VALUES ('222222', 'Stop 2')
            """.trimIndent())

            database.execSQL("""
                INSERT INTO favourite_stops (_id, stopName)
                VALUES ('333333', 'Stop 3')
            """.trimIndent())

            // Stop name is the same as '333333' to test there is no uniqueness on stop name.
            database.execSQL("""
                INSERT INTO favourite_stops (_id, stopName)
                VALUES ('444444', 'Stop 3')
            """.trimIndent())

            database.execSQL("""
                INSERT INTO active_alerts (
                    type, timeAdded, stopCode, distanceFrom, serviceNames, timeTrigger)
                VALUES (
                    1, 100, '111111', 1, NULL, NULL)
            """.trimIndent())

            database.execSQL("""
                INSERT INTO active_alerts (
                    type, timeAdded, stopCode, distanceFrom, serviceNames, timeTrigger)
                VALUES (
                    1, 101, '222222', 2, NULL, NULL)
            """.trimIndent())

            database.execSQL("""
                INSERT INTO active_alerts (
                    type, timeAdded, stopCode, distanceFrom, serviceNames, timeTrigger)
                VALUES (
                    2, 102, '333333', NULL, '1,2,3', 1)
            """.trimIndent())

            database.execSQL("""
                INSERT INTO active_alerts (
                    type, timeAdded, stopCode, distanceFrom, serviceNames, timeTrigger)
                VALUES (
                    2, 103, '444444', NULL, '4', 2)
            """.trimIndent())
        }

        helper.runMigrationsAndValidate(TEST_DB, 4, true, Migration2To4()).use { database ->
            database.assertAlertTriggersExistUpToVersion4()

            database.query("SELECT * FROM favourite_stops ORDER BY stopCode ASC").use {
                assertEquals(4, it.count)
                val stopCodeColumn = it.getColumnIndexOrThrow("stopCode")
                val stopNameColumn = it.getColumnIndexOrThrow("stopName")

                it.moveToFirst()
                assertEquals("111111", it.getString(stopCodeColumn))
                assertEquals("Stop 1", it.getString(stopNameColumn))

                it.moveToNext()
                assertEquals("222222", it.getString(stopCodeColumn))
                assertEquals("Stop 2", it.getString(stopNameColumn))

                it.moveToNext()
                assertEquals("333333", it.getString(stopCodeColumn))
                assertEquals("Stop 3", it.getString(stopNameColumn))

                it.moveToNext()
                assertEquals("444444", it.getString(stopCodeColumn))
                assertEquals("Stop 3", it.getString(stopNameColumn))
            }

            database.query("SELECT * FROM active_alerts ORDER BY id ASC").use {
                assertEquals(4, it.count)
                val typeColumn = it.getColumnIndexOrThrow("type")
                val timeAddedColumn = it.getColumnIndexOrThrow("timeAdded")
                val stopCodeColumn = it.getColumnIndexOrThrow("stopCode")
                val distanceFromColumn = it.getColumnIndexOrThrow("distanceFrom")
                val serviceNamesColumn = it.getColumnIndexOrThrow("serviceNames")
                val timeTriggerColumn = it.getColumnIndexOrThrow("timeTrigger")

                it.moveToFirst()
                assertEquals(1, it.getInt(typeColumn))
                assertEquals(100L, it.getLong(timeAddedColumn))
                assertEquals("111111", it.getString(stopCodeColumn))
                assertEquals(1, it.getInt(distanceFromColumn))
                assertTrue(it.isNull(serviceNamesColumn))
                assertTrue(it.isNull(timeTriggerColumn))

                it.moveToNext()
                assertEquals(1, it.getInt(typeColumn))
                assertEquals(101L, it.getLong(timeAddedColumn))
                assertEquals("222222", it.getString(stopCodeColumn))
                assertEquals(2, it.getInt(distanceFromColumn))
                assertTrue(it.isNull(serviceNamesColumn))
                assertTrue(it.isNull(timeTriggerColumn))

                it.moveToNext()
                assertEquals(2, it.getInt(typeColumn))
                assertEquals(102L, it.getLong(timeAddedColumn))
                assertEquals("333333", it.getString(stopCodeColumn))
                assertTrue(it.isNull(distanceFromColumn))
                assertEquals("1,2,3", it.getString(serviceNamesColumn))
                assertEquals(1, it.getInt(timeTriggerColumn))

                it.moveToNext()
                assertEquals(2, it.getInt(typeColumn))
                assertEquals(103L, it.getLong(timeAddedColumn))
                assertEquals("444444", it.getString(stopCodeColumn))
                assertTrue(it.isNull(distanceFromColumn))
                assertEquals("4", it.getString(serviceNamesColumn))
                assertEquals(2, it.getInt(timeTriggerColumn))
            }
        }
    }

    @Test
    fun migrate2To4WithNullStopCodeDoesNotGetMigrated() {
        oldDatabaseCreator.openDatabase(TEST_DB, openHelperCallback).use { database ->
            database.execSQL("""
                INSERT INTO favourite_stops (_id, stopName)
                VALUES (NULL, 'Stop name 1')
            """.trimIndent())
        }

        helper.runMigrationsAndValidate(TEST_DB, 4, true, Migration2To4()).use { database ->
            database.query("SELECT * FROM favourite_stops").use {
                assertEquals(0, it.count)
            }
        }
    }

    private val openHelperCallback get() = object : SupportSQLiteOpenHelper.Callback(2) {
        override fun onCreate(db: SupportSQLiteDatabase) {
            db.apply {
                execSQL("""
                        CREATE TABLE favourite_stops (
                            _id TEXT PRIMARY KEY,
                            stopName TEXT NOT NULL)
                    """.trimIndent())

                execSQL("""
                        CREATE TABLE active_alerts (
                            _id INTEGER PRIMARY KEY AUTOINCREMENT,
                            type NUMERIC NOT NULL,
                            timeAdded INTEGER NOT NULL,
                            stopCode TEXT NOT NULL,
                            distanceFrom INTEGER,
                            serviceNames TEXT,
                            timeTrigger INTEGER)
                    """.trimIndent())
            }
        }

        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {

        }
    }
}
