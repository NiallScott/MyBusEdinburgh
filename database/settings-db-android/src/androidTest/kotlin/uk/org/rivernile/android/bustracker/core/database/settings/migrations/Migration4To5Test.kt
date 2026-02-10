/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
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
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import uk.org.rivernile.android.bustracker.core.database.settings.RoomSettingsDatabase
import uk.org.rivernile.android.bustracker.core.database.settings.assertAlertTablesTriggersExist
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

/**
 * Tests for [Migration4To5].
 *
 * @author Niall Scott
 */
class Migration4To5Test {

    companion object {

        private const val TEST_DB = "migration-test"
    }

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation = InstrumentationRegistry.getInstrumentation(),
        databaseClass = RoomSettingsDatabase::class.java
    )

    @Test
    fun migrate4To5Empty() {
        helper.createDatabase(name = TEST_DB, version = 4).close()

        helper.runMigrationsAndValidate(
            name = TEST_DB,
            version = 5,
            validateDroppedTables = true,
            migrations = arrayOf(Migration4To5())
        ).use { database ->
            database.assertAlertTablesTriggersExist()

            database.query("SELECT * FROM arrival_alert").use { result ->
                assertEquals(0, result.count)
            }

            database.query("SELECT * FROM arrival_alert_service").use { result ->
                assertEquals(0, result.count)
            }

            database.query("SELECT * FROM proximity_alert").use { result ->
                assertEquals(0, result.count)
            }

            database.query("SELECT * FROM favourite_stop").use { result ->
                assertEquals(0, result.count)
            }
        }
    }

    @Test
    fun migrate4To5WithData() {
        val currentTime = Clock.System.now().toEpochMilliseconds()

        helper.createDatabase(name = TEST_DB, version = 4).use { database ->
            database.execSQL("""
                INSERT INTO favourite_stops (stopCode, stopName)
                VALUES ('111111', 'Stop 1')
            """.trimIndent())

            database.execSQL("""
                INSERT INTO favourite_stops (stopCode, stopName)
                VALUES ('222222', 'Stop 2')
            """.trimIndent())

            database.execSQL("""
                INSERT INTO favourite_stops (stopCode, stopName)
                VALUES ('333333', 'Stop 3')
            """.trimIndent())

            // Stop name is the same as '333333' to test there is no uniqueness on stop name.
            database.execSQL("""
                INSERT INTO favourite_stops (stopCode, stopName)
                VALUES ('444444', 'Stop 3')
            """.trimIndent())

            // Alerts do not get migrated between these versions. This is because service names
            // need to have an operator ID paired with them. The impact will be minimal as this
            // is a one-time only operation.
            database.execSQL("""
                INSERT INTO active_alerts (
                    type, timeAdded, stopCode, distanceFrom, serviceNames, timeTrigger)
                VALUES (
                    1, $currentTime, '111111', 1, NULL, NULL)
            """.trimIndent())

            database.execSQL("""
                INSERT INTO active_alerts (
                    type, timeAdded, stopCode, distanceFrom, serviceNames, timeTrigger)
                VALUES (
                    1, ${currentTime + 1L}, '222222', 2, NULL, NULL)
            """.trimIndent())

            database.execSQL("""
                INSERT INTO active_alerts (
                    type, timeAdded, stopCode, distanceFrom, serviceNames, timeTrigger)
                VALUES (
                    2, ${currentTime + 2L}, '333333', NULL, '1,2,3', 1)
            """.trimIndent())

            database.execSQL("""
                INSERT INTO active_alerts (
                    type, timeAdded, stopCode, distanceFrom, serviceNames, timeTrigger)
                VALUES (
                    2, ${currentTime + 3L}, '444444', NULL, '4', 2)
            """.trimIndent())
        }

        helper.runMigrationsAndValidate(
            name = TEST_DB,
            version = 5,
            validateDroppedTables = true,
            migrations = arrayOf(Migration4To5())
        ).use { database ->
            database.assertAlertTablesTriggersExist()

            database.query("SELECT * FROM arrival_alert").use { result ->
                assertEquals(0, result.count)
            }

            database.query("SELECT * FROM arrival_alert_service").use { result ->
                assertEquals(0, result.count)
            }

            database.query("SELECT * FROM proximity_alert").use { result ->
                assertEquals(0, result.count)
            }

            database.query("SELECT * FROM favourite_stop ORDER BY stop_code ASC").use { result ->
                assertEquals(4, result.count)
                val stopCodeColumn = result.getColumnIndexOrThrow("stop_code")
                val stopNameColumn = result.getColumnIndexOrThrow("stop_name")

                result.moveToFirst()
                assertEquals("111111", result.getString(stopCodeColumn))
                assertEquals("Stop 1", result.getString(stopNameColumn))

                result.moveToNext()
                assertEquals("222222", result.getString(stopCodeColumn))
                assertEquals("Stop 2", result.getString(stopNameColumn))

                result.moveToNext()
                assertEquals("333333", result.getString(stopCodeColumn))
                assertEquals("Stop 3", result.getString(stopNameColumn))

                result.moveToNext()
                assertEquals("444444", result.getString(stopCodeColumn))
                assertEquals("Stop 3", result.getString(stopNameColumn))
            }
        }
    }
}
