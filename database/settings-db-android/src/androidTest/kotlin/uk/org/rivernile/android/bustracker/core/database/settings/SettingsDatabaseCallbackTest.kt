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

package uk.org.rivernile.android.bustracker.core.database.settings

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tests for [SettingsDatabaseCallback].
 *
 * @author Niall Scott
 */
class SettingsDatabaseCallbackTest {

    companion object {

        private const val TEST_DB = "callback-test"
    }

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        RoomSettingsDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    private lateinit var callback: SettingsDatabaseCallback

    @Before
    fun setUp() {
        callback = SettingsDatabaseCallback()
    }

    @After
    fun tearDown() {
        InstrumentationRegistry.getInstrumentation().targetContext.deleteDatabase(TEST_DB)
    }

    @Test
    fun callbackCreateAlertsTriggersOnCreation() {
        database.apply {
            assertAlertTriggersExist(this)
            close()
        }
    }

    @Test
    fun insertAlertFiresTrigger() {
        database.apply {
            execSQL("""
                INSERT INTO active_alerts (
                    type, timeAdded, stopCode, distanceFrom, serviceNames, timeTrigger)
                VALUES (
                    1, 0, '123456', 1, NULL, NULL)
            """.trimIndent())
            assertExpiredAlertExists()

            execSQL("""
                INSERT INTO active_alerts (
                    type, timeAdded, stopCode, distanceFrom, serviceNames, timeTrigger)
                VALUES (
                    2, ${System.currentTimeMillis()}, '123456', NULL, '1,2,3', 1)
            """.trimIndent())

            query("SELECT type FROM active_alerts").apply {
                assertEquals(1, count)
                assertTrue(moveToNext())
                assertEquals(2, getInt(getColumnIndexOrThrow("type")))
                close()
            }

            close()
        }
    }

    @Test
    fun updateAlertFiresTrigger() {
        database.apply {
            execSQL("""
                INSERT INTO active_alerts (
                    type, timeAdded, stopCode, distanceFrom, serviceNames, timeTrigger)
                VALUES (
                    2, ${System.currentTimeMillis()}, '123456', NULL, '1,2,3', 1)
            """.trimIndent())

            execSQL("""
                INSERT INTO active_alerts (
                    type, timeAdded, stopCode, distanceFrom, serviceNames, timeTrigger)
                VALUES (
                    1, 0, '24680', 1, NULL, NULL)
            """.trimIndent())

            assertExpiredAlertExists()

            execSQL("""
                UPDATE active_alerts 
                SET stopCode = '987654' 
                WHERE stopCode = '123456'
            """.trimIndent())

            query("SELECT type FROM active_alerts").apply {
                assertEquals(1, count)
                assertTrue(moveToNext())
                assertEquals(2, getInt(getColumnIndexOrThrow("type")))
                close()
            }

            close()
        }
    }

    @Test
    fun deleteAlertFiresTrigger() {
        database.apply {
            execSQL("""
                INSERT INTO active_alerts (
                    type, timeAdded, stopCode, distanceFrom, serviceNames, timeTrigger)
                VALUES (
                    2, ${System.currentTimeMillis()}, '123456', NULL, '1,2,3', 1)
            """.trimIndent())

            execSQL("""
                INSERT INTO active_alerts (
                    type, timeAdded, stopCode, distanceFrom, serviceNames, timeTrigger)
                VALUES (
                    1, 0, '24680', 1, NULL, NULL)
            """.trimIndent())

            assertExpiredAlertExists()

            execSQL("DELETE FROM active_alerts WHERE stopCode = '123456'")

            query("SELECT * FROM active_alerts").apply {
                assertEquals(0, count)
                close()
            }

            close()
        }
    }

    private val database get() =
        Room
            .databaseBuilder(
                InstrumentationRegistry.getInstrumentation().targetContext,
                RoomSettingsDatabase::class.java,
                TEST_DB
            )
            .addCallback(callback)
            .build()
            .openHelper
            .writableDatabase

    private fun SupportSQLiteDatabase.assertExpiredAlertExists() {
        query("SELECT timeAdded FROM active_alerts WHERE timeAdded = 0").apply {
            assertEquals(1, count)
            assertTrue(moveToNext())
            assertEquals(0L, getLong(getColumnIndexOrThrow("timeAdded")))
            close()
        }
    }
}