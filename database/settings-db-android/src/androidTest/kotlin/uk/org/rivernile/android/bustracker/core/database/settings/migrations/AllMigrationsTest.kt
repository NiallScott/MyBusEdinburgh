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

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import uk.org.rivernile.android.bustracker.core.database.OldDatabaseCreator
import uk.org.rivernile.android.bustracker.core.database.settings.RoomSettingsDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Test all database migrations, from version 1 up to the current database version.
 *
 * @author Niall Scott
 */
class AllMigrationsTest {

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

    @BeforeTest
    fun setUp() {
        oldDatabaseCreator = OldDatabaseCreator()
    }

    @Test
    fun migrateAll() {
        // Test from version 4, which is the first version which introduced Room. Attempting to test
        // earlier than this will fail the test because Room isn't aware of the schemas prior to
        // version 4.
        helper.createDatabase(TEST_DB, 4).close()

        // Using Room to open the database in effect performs the test, because Room will throw an
        // Exception when the database schema is not in the expected state. This is evaluated after
        // all the migrations have been performed - so the post-migration state must match the state
        // of a freshly created database.
        Room
            .databaseBuilder(
                InstrumentationRegistry.getInstrumentation().targetContext,
                RoomSettingsDatabase::class.java,
                TEST_DB
            )
            .addMigrations(*allMigrations)
            .build()
            .openHelper
            .writableDatabase
            .close()
    }

    @Test
    fun migrateAllFromVersion1() {
        val callback = object : SupportSQLiteOpenHelper.Callback(1) {
            override fun onCreate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE favourite_stops (
                        _id TEXT PRIMARY KEY,
                        stopName TEXT NOT NULL)
                """.trimIndent())
            }

            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {

            }
        }
        oldDatabaseCreator.openDatabase(TEST_DB, callback).close()

        // Using Room to open the database in effect performs the test, because Room will throw an
        // Exception when the database schema is not in the expected state. This is evaluated after
        // all the migrations have been performed - so the post-migration state must match the state
        // of a freshly created database.
        Room
            .databaseBuilder(
                InstrumentationRegistry.getInstrumentation().targetContext,
                RoomSettingsDatabase::class.java,
                TEST_DB
            )
            .addMigrations(*allMigrations)
            .build()
            .openHelper
            .writableDatabase
            .close()
    }

    @Test
    fun migrateAllFromVersion2() {
        val callback = object : SupportSQLiteOpenHelper.Callback(2) {
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
        oldDatabaseCreator.openDatabase(TEST_DB, callback).close()

        // Using Room to open the database in effect performs the test, because Room will throw an
        // Exception when the database schema is not in the expected state. This is evaluated after
        // all the migrations have been performed - so the post-migration state must match the state
        // of a freshly created database.
        Room
            .databaseBuilder(
                InstrumentationRegistry.getInstrumentation().targetContext,
                RoomSettingsDatabase::class.java,
                TEST_DB
            )
            .addMigrations(*allMigrations)
            .build()
            .openHelper
            .writableDatabase
            .close()
    }

    @Test
    fun migrateAllFromVersion3() {
        val callback = object : SupportSQLiteOpenHelper.Callback(3) {
            override fun onCreate(db: SupportSQLiteDatabase) {
                db.apply {
                    execSQL("""
                        CREATE TABLE IF NOT EXISTS active_alerts (
                            _id INTEGER PRIMARY KEY AUTOINCREMENT,
                            type NUMERIC NOT NULL,
                            timeAdded INTEGER NOT NULL,
                            stopCode TEXT NOT NULL,
                            distanceFrom INTEGER,
                            serviceNames TEXT,
                            timeTrigger INTEGER)
                    """.trimIndent())

                    execSQL("""
                        CREATE TABLE IF NOT EXISTS favourite_stops (
                            _id INTEGER PRIMARY KEY AUTOINCREMENT,
                            stopCode TEXT NOT NULL UNIQUE,
                            stopName TEXT NOT NULL)
                    """.trimIndent())

                    execSQL("""
                        CREATE TRIGGER IF NOT EXISTS insert_alert
                        BEFORE INSERT ON active_alerts
                        FOR EACH ROW BEGIN
                            DELETE FROM active_alerts
                            WHERE timeAdded < ((SELECT strftime('%s','now') * 1000) - 3600000);
                        END
                    """.trimIndent())

                    execSQL("""
                        CREATE TRIGGER IF NOT EXISTS delete_alert
                        AFTER DELETE ON active_alerts
                        FOR EACH ROW BEGIN
                            DELETE FROM active_alerts
                            WHERE timeAdded < ((SELECT strftime('%s','now') * 1000) - 3600000);
                        END
                    """.trimIndent())

                    execSQL("""
                        CREATE TRIGGER IF NOT EXISTS update_alert
                        AFTER UPDATE ON active_alerts
                        FOR EACH ROW BEGIN
                            DELETE FROM active_alerts
                            WHERE timeAdded < ((SELECT strftime('%s','now') * 1000) - 3600000);
                        END
                    """.trimIndent())
                }
            }

            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {

            }
        }
        oldDatabaseCreator.openDatabase(TEST_DB, callback).close()

        // Using Room to open the database in effect performs the test, because Room will throw an
        // Exception when the database schema is not in the expected state. This is evaluated after
        // all the migrations have been performed - so the post-migration state must match the state
        // of a freshly created database.
        Room
            .databaseBuilder(
                InstrumentationRegistry.getInstrumentation().targetContext,
                RoomSettingsDatabase::class.java,
                TEST_DB
            )
            .addMigrations(*allMigrations)
            .build()
            .openHelper
            .writableDatabase
            .close()
    }

    private val allMigrations get() =
        arrayOf(
            Migration1To4(),
            Migration2To4(),
            Migration3To4()
        )
}