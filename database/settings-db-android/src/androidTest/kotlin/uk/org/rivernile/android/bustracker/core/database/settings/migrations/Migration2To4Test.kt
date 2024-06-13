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
import uk.org.rivernile.android.bustracker.core.database.settings.assertAlertTriggersExist

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

        helper.runMigrationsAndValidate(TEST_DB, 4, true, Migration2To4()).apply {
            assertAlertTriggersExist(this)

            query("SELECT * FROM active_alerts").apply {
                assertEquals(0, count)
                close()
            }

            query("SELECT * FROM favourite_stops").apply {
                assertEquals(0, count)
                close()
            }
        }
    }

    @Test
    fun migrate2To4WithData() {
        oldDatabaseCreator.openDatabase(TEST_DB, openHelperCallback).apply {
            execSQL("""
                INSERT INTO favourite_stops (_id, stopName)
                VALUES ('111111', 'Stop 1')
            """.trimIndent())

            execSQL("""
                INSERT INTO favourite_stops (_id, stopName)
                VALUES ('222222', 'Stop 2')
            """.trimIndent())

            execSQL("""
                INSERT INTO favourite_stops (_id, stopName)
                VALUES ('333333', 'Stop 3')
            """.trimIndent())

            // Stop name is the same as '333333' to test there is no uniqueness on stop name.
            execSQL("""
                INSERT INTO favourite_stops (_id, stopName) 
                VALUES ('444444', 'Stop 3')
            """.trimIndent())

            execSQL("""
                INSERT INTO active_alerts (
                    type, timeAdded, stopCode, distanceFrom, serviceNames, timeTrigger)
                VALUES (
                    1, 100, '111111', 1, NULL, NULL)
            """.trimIndent())

            execSQL("""
                INSERT INTO active_alerts (
                    type, timeAdded, stopCode, distanceFrom, serviceNames, timeTrigger)
                VALUES (
                    1, 101, '222222', 2, NULL, NULL)
            """.trimIndent())

            execSQL("""
                INSERT INTO active_alerts (
                    type, timeAdded, stopCode, distanceFrom, serviceNames, timeTrigger)
                VALUES (
                    2, 102, '333333', NULL, '1,2,3', 1)
            """.trimIndent())

            execSQL("""
                INSERT INTO active_alerts (
                    type, timeAdded, stopCode, distanceFrom, serviceNames, timeTrigger)
                VALUES (
                    2, 103, '444444', NULL, '4', 2)
            """.trimIndent())
        }

        helper.runMigrationsAndValidate(TEST_DB, 4, true, Migration2To4()).apply {
            assertAlertTriggersExist(this)

            query("SELECT * FROM favourite_stops ORDER BY stopCode ASC").apply {
                assertEquals(4, count)
                val stopCodeColumn = getColumnIndexOrThrow("stopCode")
                val stopNameColumn = getColumnIndexOrThrow("stopName")

                moveToFirst()
                assertEquals("111111", getString(stopCodeColumn))
                assertEquals("Stop 1", getString(stopNameColumn))

                moveToNext()
                assertEquals("222222", getString(stopCodeColumn))
                assertEquals("Stop 2", getString(stopNameColumn))

                moveToNext()
                assertEquals("333333", getString(stopCodeColumn))
                assertEquals("Stop 3", getString(stopNameColumn))

                moveToNext()
                assertEquals("444444", getString(stopCodeColumn))
                assertEquals("Stop 3", getString(stopNameColumn))

                close()
            }

            query("SELECT * FROM active_alerts ORDER BY id ASC").apply {
                assertEquals(4, count)
                val typeColumn = getColumnIndexOrThrow("type")
                val timeAddedColumn = getColumnIndexOrThrow("timeAdded")
                val stopCodeColumn = getColumnIndexOrThrow("stopCode")
                val distanceFromColumn = getColumnIndexOrThrow("distanceFrom")
                val serviceNamesColumn = getColumnIndexOrThrow("serviceNames")
                val timeTriggerColumn = getColumnIndexOrThrow("timeTrigger")

                moveToFirst()
                assertEquals(1, getInt(typeColumn))
                assertEquals(100L, getLong(timeAddedColumn))
                assertEquals("111111", getString(stopCodeColumn))
                assertEquals(1, getInt(distanceFromColumn))
                assertTrue(isNull(serviceNamesColumn))
                assertTrue(isNull(timeTriggerColumn))

                moveToNext()
                assertEquals(1, getInt(typeColumn))
                assertEquals(101L, getLong(timeAddedColumn))
                assertEquals("222222", getString(stopCodeColumn))
                assertEquals(2, getInt(distanceFromColumn))
                assertTrue(isNull(serviceNamesColumn))
                assertTrue(isNull(timeTriggerColumn))

                moveToNext()
                assertEquals(2, getInt(typeColumn))
                assertEquals(102L, getLong(timeAddedColumn))
                assertEquals("333333", getString(stopCodeColumn))
                assertTrue(isNull(distanceFromColumn))
                assertEquals("1,2,3", getString(serviceNamesColumn))
                assertEquals(1, getInt(timeTriggerColumn))

                moveToNext()
                assertEquals(2, getInt(typeColumn))
                assertEquals(103L, getLong(timeAddedColumn))
                assertEquals("444444", getString(stopCodeColumn))
                assertTrue(isNull(distanceFromColumn))
                assertEquals("4", getString(serviceNamesColumn))
                assertEquals(2, getInt(timeTriggerColumn))

                close()
            }
        }
    }

    @Test
    fun migrate2To4WithNullStopCodeDoesNotGetMigrated() {
        oldDatabaseCreator.openDatabase(TEST_DB, openHelperCallback).apply {
            execSQL("""
                INSERT INTO favourite_stops (_id, stopName) 
                VALUES (NULL, 'Stop name 1')
            """.trimIndent())

            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 4, true, Migration2To4()).apply {
            query("SELECT * FROM favourite_stops").apply {
                assertEquals(0, count)
                close()
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