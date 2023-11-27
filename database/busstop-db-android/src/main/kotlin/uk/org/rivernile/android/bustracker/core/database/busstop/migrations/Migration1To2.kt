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

package uk.org.rivernile.android.bustracker.core.database.busstop.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

/**
 * Perform a migration from version 1 to version 2 of the stop database. Version 1 is pre-Room.
 *
 * The version 1 schema is as follows;
 *
 * ```
 * CREATE TABLE bus_stops (
 *     _id INTEGER PRIMARY KEY,
 *     stopCode TEXT,
 *     stopName TEXT,
 *     x REAL,
 *     y REAL,
 *     orientation INTEGER,
 *     locality TEXT)
 *
 * CREATE TABLE "database_info" (
 *     "_id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
 *     "current_topo_id" TEXT,
 *     "updateTS" LONG)
 *
 * CREATE TABLE "service" (
 *     "_id" INTEGER,
 *     "name" TEXT,
 *     "desc" TEXT)
 *
 * CREATE TABLE "service_colour" (
 *     "_id" INTEGER,
 *     "hex_colour" TEXT)
 *
 * CREATE TABLE "service_point" (
 *     "_id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
 *     "service_id" INTEGER,
 *     "stop_id" INTEGER,
 *     "order_value" INTEGER,
 *     "chainage" INTEGER,
 *     "latitude" REAL,
 *     "longitude" REAL)
 *
 * CREATE TABLE service_stops (
 *     _id INTEGER PRIMARY KEY AUTOINCREMENT,
 *     stopCode TEXT,
 *     serviceName TEXT)
 * ```
 *
 * @author Niall Scott
 */
internal class Migration1To2 @Inject constructor() : Migration(1, 2) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.apply {
            removeOldIndices()
            removeOldViews()
            migrateDatabaseInfo()
            migrateBusStops()
            migrateServices()
            migrateServiceStops()
            migrateServicePoints()
        }
    }

    /**
     * Remove any old indices from the database.
     */
    private fun SupportSQLiteDatabase.removeOldIndices() {
        execSQL("DROP INDEX IF EXISTS service_point_index")
    }

    /**
     * Remove any old views from the database.
     */
    private fun SupportSQLiteDatabase.removeOldViews() {
        execSQL("DROP VIEW IF EXISTS view_services")
        execSQL("DROP VIEW IF EXISTS view_bus_stops")
        execSQL("DROP VIEW IF EXISTS view_service_points")
    }

    /**
     * Perform a migration of the `database_info` table.
     */
    private fun SupportSQLiteDatabase.migrateDatabaseInfo() {
        execSQL("""
            CREATE TABLE IF NOT EXISTS `temp_database_info` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `topologyId` TEXT, 
                `updateTimestamp` INTEGER NOT NULL)
        """.trimIndent())

        execSQL("""
            INSERT INTO temp_database_info (topologyId, updateTimestamp) 
            SELECT current_topo_id, updateTS 
            FROM database_info 
            WHERE updateTS NOT NULL 
            ORDER BY updateTS DESC 
            LIMIT 1
        """.trimIndent())

        execSQL("DROP TABLE database_info")
        execSQL("ALTER TABLE temp_database_info RENAME TO database_info")
    }

    /**
     * Perform a migration of the `bus_stop` table.
     */
    private fun SupportSQLiteDatabase.migrateBusStops() {
        execSQL("""
            CREATE TABLE IF NOT EXISTS `temp_bus_stop` (
                `id` INTEGER NOT NULL, 
                `stopCode` TEXT NOT NULL, 
                `stopName` TEXT NOT NULL, 
                `latitude` REAL, 
                `longitude` REAL, 
                `orientation` INTEGER, 
                `locality` TEXT, 
                PRIMARY KEY(`id`))
        """.trimIndent())

        execSQL("""
            INSERT INTO temp_bus_stop (
                id, stopCode, stopName, latitude, longitude, orientation, locality)
            SELECT DISTINCT _id, stopCode, stopName, x, y, orientation, locality 
            FROM bus_stops 
            WHERE _id NOT NULL 
            AND stopCode NOT NULL 
            AND stopName NOT NULL
        """.trimIndent())

        execSQL("DROP TABLE bus_stops")
        execSQL("ALTER TABLE temp_bus_stop RENAME TO bus_stop")
        execSQL("CREATE INDEX IF NOT EXISTS `bus_stop_index` ON `bus_stop` (`stopCode`)")
    }

    /**
     * Perform a migration of the `service` table.
     */
    private fun SupportSQLiteDatabase.migrateServices() {
        execSQL("""
            CREATE TABLE IF NOT EXISTS `temp_service` (
                `id` INTEGER NOT NULL, 
                `name` TEXT NOT NULL, 
                `description` TEXT, 
                `hexColour` TEXT, 
                PRIMARY KEY(`id`))
        """.trimIndent())

        execSQL("""
            INSERT INTO temp_service (id, name, description, hexColour) 
            SELECT service._id, name, desc, service_colour.hex_colour 
            FROM service 
            LEFT JOIN service_colour ON service._id = service_colour._id 
            WHERE service._id NOT NULL 
            AND name NOT NULL 
            GROUP BY service._id
        """.trimIndent())

        execSQL("DROP TABLE service")
        execSQL("DROP TABLE service_colour")
        execSQL("ALTER TABLE temp_service RENAME TO service")
        execSQL("CREATE INDEX IF NOT EXISTS `service_index` ON `service` (`name`)")
    }

    /**
     * Perform a migration of the `service_stop` table.
     */
    private fun SupportSQLiteDatabase.migrateServiceStops() {
        execSQL("""
            CREATE TABLE IF NOT EXISTS `temp_service_stop` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `stopCode` TEXT NOT NULL,
                `serviceName` TEXT NOT NULL)
        """.trimIndent())

        execSQL("""
            INSERT INTO temp_service_stop (stopCode, serviceName) 
            SELECT stopCode, serviceName 
            FROM service_stops 
            WHERE stopCode NOT NULL 
            AND serviceName NOT NULL
        """.trimIndent())

        execSQL("DROP TABLE service_stops")
        execSQL("ALTER TABLE temp_service_stop RENAME TO service_stop")
    }

    /**
     * Perform a migration of the `service_point` table.
     */
    private fun SupportSQLiteDatabase.migrateServicePoints() {
        execSQL("""
            CREATE TABLE IF NOT EXISTS `temp_service_point` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `serviceId` INTEGER NOT NULL, 
                `stopId` INTEGER, 
                `orderValue` INTEGER NOT NULL, 
                `chainage` INTEGER NOT NULL, 
                `latitude` REAL NOT NULL, 
                `longitude` REAL NOT NULL)
        """.trimIndent())

        execSQL("""
            INSERT INTO temp_service_point (
                serviceId, stopId, orderValue, chainage, latitude, longitude) 
            SELECT service_id, stop_id, order_value, chainage, latitude, longitude 
            FROM service_point 
            WHERE service_id NOT NULL 
            AND order_value NOT NULL 
            AND chainage NOT NULL 
            AND latitude NOT NULL 
            AND longitude NOT NULL
        """.trimIndent())

        execSQL("DROP TABLE service_point")
        execSQL("ALTER TABLE temp_service_point RENAME TO service_point")
        execSQL("""
            CREATE INDEX IF NOT EXISTS `service_point_index` 
            ON `service_point` (`serviceId`, `chainage`, `orderValue`)
        """.trimIndent())
    }
}