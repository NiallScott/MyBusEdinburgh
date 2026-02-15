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

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Perform a migration from version 4 to version 5 of the settings database. As version 4 is
 * managed by Room, the schema is not documented here as the schema is available in the file
 * `4.json`.
 *
 * @author Niall Scott
 */
internal class Migration4To5 : Migration(4, 5) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.apply {
            dropExistingTriggers()
            createArrivalAlertTable()
            createArrivalAlertServiceTable()
            createProximityAlertTable()
            migrateFavouriteStop()
            dropOldActiveAlertsTable()
            createAlertTablesTriggers()
        }
    }

    private fun SupportSQLiteDatabase.dropExistingTriggers() {
        dropTrigger("insert_alert")
        dropTrigger("delete_alert")
        dropTrigger("update_alert")
    }

    private fun SupportSQLiteDatabase.createArrivalAlertTable() {
        execSQL("""
            CREATE TABLE IF NOT EXISTS `arrival_alert` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `time_added_millis` INTEGER NOT NULL,
                `stop_code` TEXT NOT NULL,
                `time_trigger_minutes` INTEGER NOT NULL
            )
        """.trimIndent())

        execSQL("""
            CREATE UNIQUE INDEX IF NOT EXISTS `arrival_alert_stop_code`
            ON `arrival_alert` (`stop_code`)
        """.trimIndent())
    }

    private fun SupportSQLiteDatabase.createArrivalAlertServiceTable() {
        execSQL("""
            CREATE TABLE IF NOT EXISTS `arrival_alert_service` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `arrival_alert_id` INTEGER NOT NULL,
                `service_name` TEXT NOT NULL,
                `operator_code` TEXT NOT NULL,
                FOREIGN KEY(`arrival_alert_id`)
                REFERENCES `arrival_alert`(`id`) ON UPDATE CASCADE ON DELETE CASCADE
            )
        """.trimIndent())

        execSQL("""
            CREATE INDEX IF NOT EXISTS `arrival_alert_service_arrival_alert_id`
            ON `arrival_alert_service` (`arrival_alert_id`)
        """.trimIndent())
    }

    private fun SupportSQLiteDatabase.createProximityAlertTable() {
        execSQL("""
            CREATE TABLE IF NOT EXISTS `proximity_alert` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `time_added_millis` INTEGER NOT NULL,
                `stop_code` TEXT NOT NULL,
                `radius_trigger_meters` INTEGER NOT NULL
            )
        """.trimIndent())

        execSQL("""
            CREATE UNIQUE INDEX IF NOT EXISTS `proximity_alert_stop_code`
            ON `proximity_alert` (`stop_code`)
        """.trimIndent())
    }

    private fun SupportSQLiteDatabase.migrateFavouriteStop() {
        execSQL("""
            CREATE TABLE IF NOT EXISTS `favourite_stop` (
                `stop_code` TEXT NOT NULL,
                `stop_name` TEXT NOT NULL,
                PRIMARY KEY(`stop_code`)
            )
        """.trimIndent())

        execSQL("""
            INSERT INTO `favourite_stop` (stop_code, stop_name)
            SELECT stopCode, stopName
            FROM favourite_stops
        """.trimIndent())

        execSQL("DROP TABLE `favourite_stops`")
    }

    private fun SupportSQLiteDatabase.dropOldActiveAlertsTable() {
        execSQL("DROP TABLE `active_alerts`")
    }

    private fun SupportSQLiteDatabase.dropTrigger(triggerName: String) {
        execSQL("DROP TRIGGER IF EXISTS $triggerName")
    }

    private fun SupportSQLiteDatabase.createAlertTablesTriggers() {
        createAlertTablesTrigger(
            tableName = "arrival_alert",
            triggerName = "insert_arrival_alert",
            condition = "BEFORE INSERT"
        )
        createAlertTablesTrigger(
            tableName = "arrival_alert",
            triggerName = "delete_arrival_alert",
            condition = "AFTER DELETE"
        )
        createAlertTablesTrigger(
            tableName = "arrival_alert",
            triggerName = "update_arrival_alert",
            condition = "AFTER UPDATE"
        )
        createAlertTablesTrigger(
            tableName = "proximity_alert",
            triggerName = "insert_proximity_alert",
            condition = "BEFORE INSERT"
        )
        createAlertTablesTrigger(
            tableName = "proximity_alert",
            triggerName = "delete_proximity_alert",
            condition = "AFTER DELETE"
        )
        createAlertTablesTrigger(
            tableName = "proximity_alert",
            triggerName = "update_proximity_alert",
            condition = "AFTER UPDATE"
        )
    }

    private fun SupportSQLiteDatabase.createAlertTablesTrigger(
        tableName: String,
        triggerName: String,
        condition: String
    ) {
        execSQL("""
            CREATE TRIGGER IF NOT EXISTS $triggerName
            $condition ON $tableName
            BEGIN
                DELETE FROM $tableName
                WHERE time_added_millis < ((SELECT strftime('%s','now') * 1000) - 3600000);
            END
        """.trimIndent())
    }
}
