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

package uk.org.rivernile.android.bustracker.core.database.busstop.migrations

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import uk.org.rivernile.android.bustracker.core.database.OldDatabaseCreator
import uk.org.rivernile.android.bustracker.core.database.busstop.RoomBusStopDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test
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
    fun ensureIndicesStillExistAfterMigration() {
        openOldDatabase().use {
            it.createIndices()
        }

        runMigrationsAndValidate().apply {
            assertIndicesExist()
        }
    }

    @Test
    fun ensureViewsDoNotExistWhenTheyDidNotPreviouslyExist() {
        openOldDatabase().close()

        runMigrationsAndValidate().apply {
            assertViewsDoNotExist()
        }
    }

    @Test
    fun ensureViewsDoNotExistWhenTheyPreviouslyDidExist() {
        openOldDatabase().use {
            it.createViews()
        }

        runMigrationsAndValidate().apply {
            assertViewsDoNotExist()
        }
    }

    @Test
    fun ensureOldTablesAreDropped() {
        openOldDatabase().close()

        runMigrationsAndValidate().apply {
            query("""
                SELECT name 
                FROM sqlite_master 
                WHERE name IN ('bus_stops', 'service_colour', 'service_stops')
            """.trimIndent())
                .use {
                    assertEquals(0, it.count)
                }
        }
    }

    @Test
    fun databaseInfoMigrationDiscardsNullUpdateTimestamp() {
        openOldDatabase().use {
            it.execSQL("""
                INSERT INTO database_info (current_topo_id, updateTS) 
                VALUES (NULL, NULL)
            """.trimIndent())
        }

        runMigrationsAndValidate().apply {
            query("SELECT topologyId, updateTimestamp FROM database_info").use {
                assertEquals(0, it.count)
            }
        }
    }

    @Test
    fun databaseInfoMigrationOnlyTakesTheNewestItem() {
        openOldDatabase().use {
            it.execSQL("INSERT INTO database_info (current_topo_id, updateTS) VALUES ('test1', 1)")
            it.execSQL("INSERT INTO database_info (current_topo_id, updateTS) VALUES ('test2', 3)")
            it.execSQL("INSERT INTO database_info (current_topo_id, updateTS) VALUES ('test3', 2)")
        }

        runMigrationsAndValidate().apply {
            query("SELECT topologyId, updateTimestamp FROM database_info").use {
                assertEquals(1, it.count)
                assertTrue(it.moveToFirst())
                assertEquals("test2", it.getString(0))
                assertEquals(3, it.getLong(1))
            }
        }
    }

    @Test
    fun busStopMigrationDropsRowsWithNullStopCode() {
        openOldDatabase().use {
            it.execSQL("""
                INSERT INTO bus_stops (stopCode, stopName, x, y, orientation, locality)
                VALUES (NULL, 'Stop name', 1.1, 2.2, 3, 'Locality')
            """.trimIndent())
        }

        runMigrationsAndValidate().apply {
            query("""
                SELECT stopCode, stopName, latitude, longitude, orientation, locality 
                FROM bus_stop
            """.trimIndent())
                .use {
                    assertEquals(0, it.count)
                }
        }
    }

    @Test
    fun busStopMigrationDropsRowsWithNullStopName() {
        openOldDatabase().use {
            it.execSQL("""
                INSERT INTO bus_stops (stopCode, stopName, x, y, orientation, locality)
                VALUES ('123456', NULL, 1.1, 2.2, 3, 'Locality')
            """.trimIndent())
        }

        runMigrationsAndValidate().apply {
            query("""
                SELECT stopCode, stopName, latitude, longitude, orientation, locality 
                FROM bus_stop
            """.trimIndent())
                .use {
                    assertEquals(0, it.count)
                }
        }
    }

    @Test
    fun busStopMigrationCorrectlyMigratesValidRows() {
        openOldDatabase().use {
            it.execSQL("""
                INSERT INTO bus_stops (stopCode, stopName, x, y, orientation, locality)
                VALUES ('111111', 'Stop name 1', NULL, NULL, NULL, NULL)
            """.trimIndent())

            it.execSQL("""
                INSERT INTO bus_stops (stopCode, stopName, x, y, orientation, locality)
                VALUES ('111112', 'Stop name 2', 1.1, NULL, NULL, NULL)
            """.trimIndent())

            it.execSQL("""
                INSERT INTO bus_stops (stopCode, stopName, x, y, orientation, locality)
                VALUES ('111113', 'Stop name 3', NULL, 2.2, NULL, NULL)
            """.trimIndent())

            it.execSQL("""
                INSERT INTO bus_stops (stopCode, stopName, x, y, orientation, locality)
                VALUES ('111114', 'Stop name 4', NULL, NULL, 3, NULL)
            """.trimIndent())

            it.execSQL("""
                INSERT INTO bus_stops (stopCode, stopName, x, y, orientation, locality)
                VALUES ('111115', 'Stop name 5', NULL, NULL, NULL, 'Locality 5')
            """.trimIndent())

            it.execSQL("""
                INSERT INTO bus_stops (stopCode, stopName, x, y, orientation, locality)
                VALUES ('111116', 'Stop name 6', 1.1, 2.2, 3, 'Locality 6')
            """.trimIndent())
        }

        runMigrationsAndValidate().apply {
            query("""
                SELECT stopCode, stopName, latitude, longitude, orientation, locality 
                FROM bus_stop
                ORDER BY stopCode ASC
            """.trimIndent())
                .use {
                    assertEquals(6, it.count)

                    assertTrue(it.moveToFirst())
                    assertEquals("111111", it.getString(0))
                    assertEquals("Stop name 1", it.getString(1))
                    assertTrue(it.isNull(2))
                    assertTrue(it.isNull(3))
                    assertTrue(it.isNull(4))
                    assertTrue(it.isNull(5))

                    assertTrue(it.moveToNext())
                    assertEquals("111112", it.getString(0))
                    assertEquals("Stop name 2", it.getString(1))
                    assertEquals(1.1, it.getDouble(2), 0.000001)
                    assertTrue(it.isNull(3))
                    assertTrue(it.isNull(4))
                    assertTrue(it.isNull(5))

                    assertTrue(it.moveToNext())
                    assertEquals("111113", it.getString(0))
                    assertEquals("Stop name 3", it.getString(1))
                    assertTrue(it.isNull(2))
                    assertEquals(2.2, it.getDouble(3), 0.000001)
                    assertTrue(it.isNull(4))
                    assertTrue(it.isNull(5))

                    assertTrue(it.moveToNext())
                    assertEquals("111114", it.getString(0))
                    assertEquals("Stop name 4", it.getString(1))
                    assertTrue(it.isNull(2))
                    assertTrue(it.isNull(3))
                    assertEquals(3, it.getInt(4))
                    assertTrue(it.isNull(5))

                    assertTrue(it.moveToNext())
                    assertEquals("111115", it.getString(0))
                    assertEquals("Stop name 5", it.getString(1))
                    assertTrue(it.isNull(2))
                    assertTrue(it.isNull(3))
                    assertTrue(it.isNull(4))
                    assertEquals("Locality 5", it.getString(5))

                    assertTrue(it.moveToNext())
                    assertEquals("111116", it.getString(0))
                    assertEquals("Stop name 6", it.getString(1))
                    assertEquals(1.1, it.getDouble(2), 0.000001)
                    assertEquals(2.2, it.getDouble(3), 0.000001)
                    assertEquals(3, it.getInt(4))
                    assertEquals("Locality 6", it.getString(5))
                }
        }
    }

    @Test
    fun servicesMigrationOnlyTakesItemsWithDistinctIds() {
        openOldDatabase().use {
            it.execSQL("""
                INSERT INTO service (_id, name, desc) 
                VALUES (1, '1', NULL)
            """.trimIndent())

            it.execSQL("""
                INSERT INTO service (_id, name, desc) 
                VALUES (1, '2', NULL)
            """.trimIndent())
        }

        runMigrationsAndValidate().apply {
            query("SELECT id, name, description, hexColour FROM service").use {
                assertEquals(1, it.count)
            }
        }
    }

    @Test
    fun servicesMigrationDropsRowsWithNullId() {
        openOldDatabase().use {
            it.execSQL("""
                INSERT INTO service (_id, name, desc) 
                VALUES (NULL, '1', NULL)
            """.trimIndent())
        }

        runMigrationsAndValidate().apply {
            query("SELECT id, name, description, hexColour FROM service").use {
                assertEquals(0, it.count)
            }
        }
    }

    @Test
    fun servicesMigrationDropsRowsWithNullServiceName() {
        openOldDatabase().use {
            it.execSQL("""
                INSERT INTO service (_id, name, desc) 
                VALUES (1, NULL, NULL)
            """.trimIndent())
        }

        runMigrationsAndValidate().apply {
            query("SELECT id, name, description, hexColour FROM service").use {
                assertEquals(0, it.count)
            }
        }
    }

    @Test
    fun servicesMigrationCorrectlyMigratesValidRows() {
        openOldDatabase().use {
            it.execSQL("""
                INSERT INTO service (_id, name, desc) 
                VALUES (1, '1', NULL)
            """.trimIndent())

            it.execSQL("""
                INSERT INTO service (_id, name, desc) 
                VALUES (2, '2', 'Description 2')
            """.trimIndent())

            it.execSQL("""
                INSERT INTO service (_id, name, desc) 
                VALUES (3, '3', 'Description 3')
            """.trimIndent())

            it.execSQL("""
                INSERT INTO service (_id, name, desc) 
                VALUES (4, '4', 'Description 4')
            """.trimIndent())

            it.execSQL("""
                INSERT INTO service_colour (_id, hex_colour) 
                VALUES (1, '#111111')
            """.trimIndent())

            it.execSQL("""
                INSERT INTO service_colour (_id, hex_colour) 
                VALUES (3, NULL)
            """.trimIndent())

            it.execSQL("""
                INSERT INTO service_colour (_id, hex_colour) 
                VALUES (4, '#444444')
            """.trimIndent())
        }

        runMigrationsAndValidate().apply {
            query("""
                SELECT id, name, description, hexColour 
                FROM service 
                ORDER BY id ASC
            """.trimIndent())
                .use {
                    assertEquals(4, it.count)

                    assertTrue(it.moveToFirst())
                    assertEquals(1, it.getInt(0))
                    assertEquals("1", it.getString(1))
                    assertTrue(it.isNull(2))
                    assertEquals("#111111", it.getString(3))

                    assertTrue(it.moveToNext())
                    assertEquals(2, it.getInt(0))
                    assertEquals("2", it.getString(1))
                    assertEquals("Description 2", it.getString(2))
                    assertTrue(it.isNull(3))

                    assertTrue(it.moveToNext())
                    assertEquals(3, it.getInt(0))
                    assertEquals("3", it.getString(1))
                    assertEquals("Description 3", it.getString(2))
                    assertTrue(it.isNull(3))

                    assertTrue(it.moveToNext())
                    assertEquals(4, it.getInt(0))
                    assertEquals("4", it.getString(1))
                    assertEquals("Description 4", it.getString(2))
                    assertEquals("#444444", it.getString(3))
                }
        }
    }

    @Test
    fun serviceStopsMigrationDropsRowsWithNullStopCode() {
        openOldDatabase().use {
            it.execSQL("""
                INSERT INTO service_stops (stopCode, serviceName) 
                VALUES (NULL, '1')
            """.trimIndent())
        }

        runMigrationsAndValidate().apply {
            query("SELECT stopCode, serviceName FROM service_stop").use {
                assertEquals(0, it.count)
            }
        }
    }

    @Test
    fun serviceStopsMigrationDropsRowsWithNullServiceName() {
        openOldDatabase().use {
            it.execSQL("""
                INSERT INTO service_stops (stopCode, serviceName) 
                VALUES ('111111', NULL)
            """.trimIndent())
        }

        runMigrationsAndValidate().apply {
            query("SELECT stopCode, serviceName FROM service_stop").use {
                assertEquals(0, it.count)
            }
        }
    }

    @Test
    fun serviceStopsMigrationCorrectlyMigratesValidValues() {
        openOldDatabase().use {
            it.execSQL("""
                INSERT INTO service_stops (stopCode, serviceName) 
                VALUES ('111111', '1')
            """.trimIndent())

            it.execSQL("""
                INSERT INTO service_stops (stopCode, serviceName) 
                VALUES ('222222', '2')
            """.trimIndent())

            it.execSQL("""
                INSERT INTO service_stops (stopCode, serviceName) 
                VALUES ('333333', '2')
            """.trimIndent())

            it.execSQL("""
                INSERT INTO service_stops (stopCode, serviceName) 
                VALUES ('333333', '3')
            """.trimIndent())
        }

        runMigrationsAndValidate().apply {
            query("SELECT stopCode, serviceName FROM service_stop").use {
                assertEquals(4, it.count)

                assertTrue(it.moveToFirst())
                assertEquals("111111", it.getString(0))
                assertEquals("1", it.getString(1))

                assertTrue(it.moveToNext())
                assertEquals("222222", it.getString(0))
                assertEquals("2", it.getString(1))

                assertTrue(it.moveToNext())
                assertEquals("333333", it.getString(0))
                assertEquals("2", it.getString(1))

                assertTrue(it.moveToNext())
                assertEquals("333333", it.getString(0))
                assertEquals("3", it.getString(1))
            }
        }
    }

    @Test
    fun servicePointsMigrationDropsRowsWithNullServiceId() {
        openOldDatabase().use {
            it.execSQL("""
                INSERT INTO service_point (
                    service_id, stop_id, order_value, chainage, latitude, longitude)
                VALUES (
                    NULL, 2, 3, 4, 5.1, 6.2)
            """.trimIndent())
        }

        runMigrationsAndValidate().apply {
            query("""
                SELECT serviceId, stopId, orderValue, chainage, latitude, longitude 
                FROM service_point
            """.trimIndent())
                .use {
                    assertEquals(0, it.count)
                }
        }
    }

    @Test
    fun servicePointsMigrationDropsRowsWithNullOrderValue() {
        openOldDatabase().use {
            it.execSQL("""
                INSERT INTO service_point (
                    service_id, stop_id, order_value, chainage, latitude, longitude)
                VALUES (
                    1, 2, NULL, 4, 5.1, 6.2)
            """.trimIndent())
        }

        runMigrationsAndValidate().apply {
            query("""
                SELECT serviceId, stopId, orderValue, chainage, latitude, longitude 
                FROM service_point
            """.trimIndent())
                .use {
                    assertEquals(0, it.count)
                }
        }
    }

    @Test
    fun servicePointsMigrationDropsRowsWithNullChainage() {
        openOldDatabase().use {
            it.execSQL("""
                INSERT INTO service_point (
                    service_id, stop_id, order_value, chainage, latitude, longitude)
                VALUES (
                    1, 2, 3, NULL, 5.1, 6.2)
            """.trimIndent())
        }

        runMigrationsAndValidate().apply {
            query("""
                SELECT serviceId, stopId, orderValue, chainage, latitude, longitude 
                FROM service_point
            """.trimIndent())
                .use {
                    assertEquals(0, it.count)
                }
        }
    }

    @Test
    fun servicePointsMigrationDropsRowsWithNullLatitude() {
        openOldDatabase().use {
            it.execSQL("""
                INSERT INTO service_point (
                    service_id, stop_id, order_value, chainage, latitude, longitude)
                VALUES (
                    1, 2, 3, 4, NULL, 6.2)
            """.trimIndent())
        }

        runMigrationsAndValidate().apply {
            query("""
                SELECT serviceId, stopId, orderValue, chainage, latitude, longitude 
                FROM service_point
            """.trimIndent())
                .use {
                    assertEquals(0, it.count)
                }
        }
    }

    @Test
    fun servicePointsMigrationDropsRowsWithNullLongitude() {
        openOldDatabase().use {
            it.execSQL("""
                INSERT INTO service_point (
                    service_id, stop_id, order_value, chainage, latitude, longitude)
                VALUES (
                    1, 2, 3, 4, 5.1, NULL)
            """.trimIndent())
        }

        runMigrationsAndValidate().apply {
            query("""
                SELECT serviceId, stopId, orderValue, chainage, latitude, longitude 
                FROM service_point
            """.trimIndent())
                .use {
                    assertEquals(0, it.count)
                }
        }
    }

    @Test
    fun servicePointsMigrationCorrectlyMigratesValidValues() {
        openOldDatabase().use {
            it.execSQL("""
                INSERT INTO service_point (
                    service_id, stop_id, order_value, chainage, latitude, longitude)
                VALUES (
                    1, NULL, 3, 4, 5.1, 6.2)
            """.trimIndent())

            it.execSQL("""
                INSERT INTO service_point (
                    service_id, stop_id, order_value, chainage, latitude, longitude)
                VALUES (
                    10, 20, 30, 40, 50.1, 60.2)
            """.trimIndent())
        }

        runMigrationsAndValidate().apply {
            query("""
                SELECT serviceId, stopId, orderValue, chainage, latitude, longitude 
                FROM service_point
            """.trimIndent())
                .use {
                    assertEquals(2, it.count)

                    assertTrue(it.moveToFirst())
                    assertEquals(1, it.getInt(0))
                    assertTrue(it.isNull(1))
                    assertEquals(3, it.getInt(2))
                    assertEquals(4, it.getInt(3))
                    assertEquals(5.1, it.getDouble(4), 0.000001)
                    assertEquals(6.2, it.getDouble(5), 0.000001)

                    assertTrue(it.moveToNext())
                    assertEquals(10, it.getInt(0))
                    assertEquals(20, it.getInt(1))
                    assertEquals(30, it.getInt(2))
                    assertEquals(40, it.getInt(3))
                    assertEquals(50.1, it.getDouble(4), 0.000001)
                    assertEquals(60.2, it.getDouble(5), 0.000001)
                }
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
        query("PRAGMA index_info(service_point_index)").use {
            assertTrue(it.count > 0)
        }

        query("PRAGMA index_info(bus_stop_index)").use {
            assertTrue(it.count > 0)
        }

        query("PRAGMA index_info(service_index)").use {
            assertTrue(it.count > 0)
        }
    }

    private fun SupportSQLiteDatabase.assertViewsDoNotExist() {
        query("SELECT name FROM sqlite_master WHERE type = 'view' ORDER BY name ASC").use {
            assertEquals(0, it.count)
        }
    }

    private fun SupportSQLiteDatabase.createBaseSchema() {
        execSQL("""
            CREATE TABLE bus_stops (
                _id INTEGER PRIMARY KEY,
                stopCode TEXT,
                stopName TEXT,
                x REAL,
                y REAL,
                orientation INTEGER,
                locality TEXT)
        """.trimIndent())

        execSQL("""
            CREATE TABLE database_info (
                _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                current_topo_id TEXT,
                updateTS LONG)
        """.trimIndent())

        execSQL("""
            CREATE TABLE service (
                _id INTEGER,
                name TEXT,
                desc TEXT)
        """.trimIndent())

        execSQL("""
            CREATE TABLE service_colour (
                _id INTEGER,
                hex_colour TEXT)
        """.trimIndent())

        execSQL("""
            CREATE TABLE service_point (
                _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                service_id INTEGER,
                stop_id INTEGER,
                order_value INTEGER,
                chainage INTEGER,
                latitude REAL,
                longitude REAL)
        """.trimIndent())

        execSQL("""
            CREATE TABLE service_stops (
                _id INTEGER PRIMARY KEY AUTOINCREMENT,
                stopCode TEXT,
                serviceName TEXT)
        """.trimIndent())
    }

    private fun SupportSQLiteDatabase.createIndices() {
        execSQL("""
            CREATE INDEX IF NOT EXISTS service_point_index 
            ON service_point (service_id, chainage, order_value)
        """.trimIndent())
    }

    private fun SupportSQLiteDatabase.createViews() {
        execSQL("""
            CREATE VIEW IF NOT EXISTS view_services 
            AS SELECT service._id AS _id, name, desc, hex_colour 
            FROM service 
            LEFT JOIN service_colour 
                ON service._id = service_colour._id
        """.trimIndent())

        execSQL("""
            CREATE VIEW IF NOT EXISTS view_bus_stops
            AS SELECT _id, stopCode, stopName, x, y, orientation, locality, (
                SELECT group_concat(serviceName, ', ') 
                FROM (
                    SELECT stopCode, serviceName 
                    FROM service_stops 
                    WHERE bus_stops.stopCode = service_stops.stopCode
                    ORDER BY CASE WHEN serviceName GLOB '[^0-9.]*' 
                        THEN serviceName 
                        ELSE cast(serviceName AS int) 
                        END) 
                    GROUP BY stopCode) 
                AS serviceListing 
            FROM bus_stops
        """.trimIndent())

        execSQL("""
            CREATE VIEW IF NOT EXISTS view_service_points 
            AS SELECT service_point._id AS _id, service.name AS serviceName, 
                bus_stops.stopCode AS stopCode, order_value, chainage, latitude, longitude 
            FROM service_point 
            LEFT JOIN service 
                ON service_id = service._id 
            LEFT JOIN bus_stops 
                ON stop_id = bus_stops._id
        """.trimIndent())
    }

    private val openHelperCallback get() = object : SupportSQLiteOpenHelper.Callback(1) {
        override fun onCreate(db: SupportSQLiteDatabase) {
            db.createBaseSchema()
        }

        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {

        }
    }
}