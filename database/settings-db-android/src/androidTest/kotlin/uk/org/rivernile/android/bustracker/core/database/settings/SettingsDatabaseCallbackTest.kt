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

package uk.org.rivernile.android.bustracker.core.database.settings

import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.test.AfterTest

/**
 * Tests for [SettingsDatabaseCallback].
 *
 * @author Niall Scott
 */
class SettingsDatabaseCallbackTest {

    private lateinit var roomDatabase: RoomSettingsDatabase
    private lateinit var database: SupportSQLiteDatabase

    @Before
    fun setUp() {
        roomDatabase = Room
            .inMemoryDatabaseBuilder<RoomSettingsDatabase>(
                context = InstrumentationRegistry.getInstrumentation().targetContext,
            )
            .addCallback(SettingsDatabaseCallback())
            .build()
        database = roomDatabase
            .openHelper
            .writableDatabase
    }

    @AfterTest
    fun tearDown() {
        database.close()
        roomDatabase.close()
    }

    @Test
    fun callbackCreateAlertsTriggersOnCreation() {
        database.apply {
            assertAlertTablesTriggersExist()
        }
    }

    @Test
    fun insertArrivalAlertFiresTrigger() {
        database.apply {
            execSQL("""
                INSERT INTO arrival_alert (
                    time_added_millis, stop_code, time_trigger_minutes)
                VALUES (
                    0, '123456', 5)
            """.trimIndent())
            assertExpiredArrivalAlertExists()

            execSQL("""
                INSERT INTO arrival_alert (
                    time_added_millis, stop_code, time_trigger_minutes)
                VALUES (
                    ${System.currentTimeMillis()}, '987654', 10)
            """.trimIndent())

            query("SELECT COUNT(*) FROM arrival_alert").use {
                assertTrue(it.moveToNext())
                assertEquals(1, it.getInt(0))
            }

            close()
        }
    }

    @Test
    fun insertProximityAlertFiresTrigger() {
        database.apply {
            execSQL("""
                INSERT INTO proximity_alert (
                    time_added_millis, stop_code, radius_trigger_meters)
                VALUES (
                    0, '123456', 100)
            """.trimIndent())
            assertExpiredProximityAlertExists()

            execSQL("""
                INSERT INTO proximity_alert (
                    time_added_millis, stop_code, radius_trigger_meters)
                VALUES (
                    ${System.currentTimeMillis()}, '987654', 250)
            """.trimIndent())

            query("SELECT COUNT(*) FROM proximity_alert").use {
                assertTrue(it.moveToNext())
                assertEquals(1, it.getInt(0))
                close()
            }

            close()
        }
    }

    @Test
    fun updateArrivalAlertFiresTrigger() {
        database.apply {
            execSQL("""
                INSERT INTO arrival_alert (
                    time_added_millis, stop_code, time_trigger_minutes)
                VALUES (
                    ${System.currentTimeMillis()}, '123456', 5)
            """.trimIndent())

            execSQL("""
                INSERT INTO arrival_alert (
                    time_added_millis, stop_code, time_trigger_minutes)
                VALUES (
                    0, '24680', 10)
            """.trimIndent())

            assertExpiredArrivalAlertExists()

            execSQL("""
                UPDATE arrival_alert
                SET stop_code = '987654'
                WHERE stop_code = '123456'
            """.trimIndent())

            query("SELECT COUNT(*) FROM arrival_alert").use {
                assertTrue(it.moveToNext())
                assertEquals(1, it.getInt(0))
                close()
            }

            close()
        }
    }

    @Test
    fun updateProximityAlertFiresTrigger() {
        database.apply {
            execSQL("""
                INSERT INTO proximity_alert (
                    time_added_millis, stop_code, radius_trigger_meters)
                VALUES (
                    ${System.currentTimeMillis()}, '123456', 100)
            """.trimIndent())

            execSQL("""
                INSERT INTO proximity_alert (
                    time_added_millis, stop_code, radius_trigger_meters)
                VALUES (
                    0, '24680', 250)
            """.trimIndent())

            assertExpiredProximityAlertExists()

            execSQL("""
                UPDATE proximity_alert
                SET stop_code = '987654'
                WHERE stop_code = '123456'
            """.trimIndent())

            query("SELECT COUNT(*) FROM proximity_alert").use {
                assertTrue(it.moveToNext())
                assertEquals(1, it.getInt(0))
                close()
            }

            close()
        }
    }

    @Test
    fun deleteArrivalAlertFiresTrigger() {
        database.apply {
            execSQL("""
                INSERT INTO arrival_alert (
                    time_added_millis, stop_code, time_trigger_minutes)
                VALUES (
                    ${System.currentTimeMillis()}, '123456', 5)
            """.trimIndent())

            execSQL("""
                INSERT INTO arrival_alert (
                    time_added_millis, stop_code, time_trigger_minutes)
                VALUES (
                    0, '24680', 10)
            """.trimIndent())

            assertExpiredArrivalAlertExists()

            execSQL("DELETE FROM arrival_alert WHERE stop_code = '123456'")

            query("SELECT * FROM arrival_alert").use {
                assertEquals(0, it.count)
            }

            close()
        }
    }

    @Test
    fun deleteProximityAlertFiresTrigger() {
        database.apply {
            execSQL("""
                INSERT INTO proximity_alert (
                    time_added_millis, stop_code, radius_trigger_meters)
                VALUES (
                    ${System.currentTimeMillis()}, '123456', 100)
            """.trimIndent())

            execSQL("""
                INSERT INTO proximity_alert (
                    time_added_millis, stop_code, radius_trigger_meters)
                VALUES (
                    0, '24680', 250)
            """.trimIndent())

            assertExpiredProximityAlertExists()

            execSQL("DELETE FROM proximity_alert WHERE stop_code = '123456'")

            query("SELECT * FROM proximity_alert").use {
                assertEquals(0, it.count)
            }

            close()
        }
    }

    private fun SupportSQLiteDatabase.assertExpiredArrivalAlertExists() {
        query("SELECT time_added_millis FROM arrival_alert WHERE time_added_millis = 0").use {
            assertEquals(1, it.count)
            assertTrue(it.moveToNext())
            assertEquals(0L, it.getLong(it.getColumnIndexOrThrow("time_added_millis")))
        }
    }

    private fun SupportSQLiteDatabase.assertExpiredProximityAlertExists() {
        query("SELECT time_added_millis FROM proximity_alert WHERE time_added_millis = 0").use {
            assertEquals(1, it.count)
            assertTrue(it.moveToNext())
            assertEquals(0L, it.getLong(it.getColumnIndexOrThrow("time_added_millis")))
        }
    }
}
