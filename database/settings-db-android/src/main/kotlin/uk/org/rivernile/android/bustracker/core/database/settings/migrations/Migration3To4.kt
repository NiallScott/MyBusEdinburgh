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

package uk.org.rivernile.android.bustracker.core.database.settings.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Perform a migration from version 3 to version 4 of the settings database. Version 3 is pre-Room.
 *
 * The version 3 schema is as follows;
 *
 * ```
 * CREATE TABLE IF NOT EXISTS active_alerts (
 *     _id INTEGER PRIMARY KEY AUTOINCREMENT,
 *     type NUMERIC NOT NULL,
 *     timeAdded INTEGER NOT NULL,
 *     stopCode TEXT NOT NULL,
 *     distanceFrom INTEGER,
 *     serviceNames TEXT,
 *     timeTrigger INTEGER)
 *
 * CREATE TABLE IF NOT EXISTS favourite_stops (
 *     _id INTEGER PRIMARY KEY AUTOINCREMENT,
 *     stopCode TEXT NOT NULL UNIQUE,
 *     stopName TEXT NOT NULL)
 *
 * CREATE TRIGGER IF NOT EXISTS insert_alert
 * BEFORE INSERT ON active_alerts
 * FOR EACH ROW BEGIN
 *     DELETE FROM active_alerts
 *     WHERE timeAdded < ((SELECT strftime('%s','now') * 1000) - 3600000);
 * END
 *
 * CREATE TRIGGER IF NOT EXISTS delete_alert
 * AFTER DELETE ON active_alerts
 * FOR EACH ROW BEGIN
 *     DELETE FROM active_alerts
 *     WHERE timeAdded < ((SELECT strftime('%s','now') * 1000) - 3600000);
 * END
 *
 * CREATE TRIGGER IF NOT EXISTS update_alert
 * AFTER UPDATE ON active_alerts
 * FOR EACH ROW BEGIN
 *     DELETE FROM active_alerts
 *     WHERE timeAdded < ((SELECT strftime('%s','now') * 1000) - 3600000);
 * END
 * ```
 *
 * @author Niall Scott
 */
internal class Migration3To4 : Migration(3, 4) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.apply {
            dropExistingAlertTriggers()
            migrateAlerts()
            migrateFavouriteStops()
            createNewAlertTriggers()
        }
    }

    /**
     * Drop the existing triggers for the `active_alerts` table.
     */
    private fun SupportSQLiteDatabase.dropExistingAlertTriggers() {
        dropTrigger("insert_alert")
        dropTrigger("delete_alert")
        dropTrigger("update_alert")
    }

    /**
     * Perform a migration of the `active_alerts` table.
     */
    private fun SupportSQLiteDatabase.migrateAlerts() {
        execSQL("""
            CREATE TABLE IF NOT EXISTS `temp_active_alerts` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `type` INTEGER NOT NULL,
                `timeAdded` INTEGER NOT NULL,
                `stopCode` TEXT NOT NULL,
                `distanceFrom` INTEGER,
                `serviceNames` TEXT,
                `timeTrigger` INTEGER)
        """.trimIndent())

        execSQL("""
            INSERT INTO temp_active_alerts (
                id, type, timeAdded, stopCode, distanceFrom, serviceNames, timeTrigger)
            SELECT _id, type, timeAdded, stopCode, distanceFrom, serviceNames, timeTrigger 
            FROM active_alerts
        """.trimIndent())

        execSQL("DROP TABLE active_alerts")
        execSQL("ALTER TABLE temp_active_alerts RENAME TO active_alerts")
    }

    /**
     * Perform a migration of the `favourite_stops` table.
     */
    private fun SupportSQLiteDatabase.migrateFavouriteStops() {
        execSQL("""
            CREATE TABLE IF NOT EXISTS `temp_favourite_stops` (
                `stopCode` TEXT NOT NULL,
                `stopName` TEXT NOT NULL,
                PRIMARY KEY(`stopCode`))
        """.trimIndent())

        execSQL("""
            INSERT INTO temp_favourite_stops (stopCode, stopName)
            SELECT DISTINCT stopCode, stopName 
            FROM favourite_stops
        """.trimIndent())

        execSQL("DROP TABLE favourite_stops")
        execSQL("ALTER TABLE temp_favourite_stops RENAME TO favourite_stops")
    }

    /**
     * Drop a trigger with the given [triggerName].
     *
     * @param triggerName The name of the trigger to drop.
     */
    private fun SupportSQLiteDatabase.dropTrigger(triggerName: String) {
        execSQL("DROP TRIGGER IF EXISTS $triggerName")
    }

    /**
     * Create the new triggers on the `active_alerts` table.
     */
    private fun SupportSQLiteDatabase.createNewAlertTriggers() {
        createAlertsTrigger("insert_alert", "BEFORE INSERT")
        createAlertsTrigger("delete_alert", "AFTER DELETE")
        createAlertsTrigger("update_alert", "AFTER UPDATE")
    }

    /**
     * Create a trigger on the `active_alerts` table.
     *
     * @param triggerName The name the trigger should have in the database.
     * @param condition The condition on which the trigger should fire on, in SQL syntax.
     */
    private fun SupportSQLiteDatabase.createAlertsTrigger(triggerName: String, condition: String) {
        execSQL("""
            CREATE TRIGGER IF NOT EXISTS $triggerName 
            $condition ON active_alerts 
            BEGIN
                DELETE FROM active_alerts 
                WHERE timeAdded < ((SELECT strftime('%s','now') * 1000) - 3600000);
            END
        """.trimIndent())
    }
}