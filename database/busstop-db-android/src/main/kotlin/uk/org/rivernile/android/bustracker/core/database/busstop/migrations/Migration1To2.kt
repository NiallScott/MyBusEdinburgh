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

package uk.org.rivernile.android.bustracker.core.database.busstop.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import uk.org.rivernile.android.bustracker.core.database.busstop.service.CREATE_SERVICE_VIEW_STATEMENT
import javax.inject.Inject

/**
 * Perform a migration from version 1 to version 2 of the stop database. Version 1 is pre-Room.
 *
 * The version 1 schema is as follows;
 *
 * ```
 * CREATE TABLE database_info (
 *     id INTEGER PRIMARY KEY NOT NULL,
 *     update_timestamp INTEGER NOT NULL)
 *
 * CREATE TABLE operator (
 *     id INTEGER PRIMARY KEY NOT NULL,
 *     reference TEXT NOT NULL,
 *     national_code TEXT,
 *     name TEXT NOT NULL)
 *
 * CREATE TABLE service (
 *     id INTEGER PRIMARY KEY NOT NULL,
 *     name TEXT NOT NULL,
 *     operator_id INTEGER NOT NULL,
 *     description TEXT NOT NULL,
 *     colour_primary TEXT,
 *     colour_on_primary TEXT)
 *
 * CREATE TABLE service_stop (
 *     id INTEGER PRIMARY KEY NOT NULL,
 *     service_id INTEGER NOT NULL,
 *     stop_id INTEGER NOT NULL)
 *
 * CREATE TABLE service_point (
 *     id INTEGER PRIMARY KEY NOT NULL,
 *     service_id INTEGER NOT NULL,
 *     stop_id INTEGER,
 *     route_section INTEGER NOT NULL,
 *     order_value INTEGER NOT NULL,
 *     latitude REAL NOT NULL,
 *     longitude REAL NOT NULL)
 *
 * CREATE TABLE stop (
 *     id INTEGER PRIMARY KEY NOT NULL,
 *     naptan_code TEXT NOT NULL,
 *     atco_code TEXT NOT NULL,
 *     name TEXT NOT NULL,
 *     locality TEXT,
 *     latitude REAL NOT NULL,
 *     longitude REAL NOT NULL,
 *     bearing TEXT)
 * ```
 *
 * @author Niall Scott
 */
internal class Migration1To2 @Inject constructor() : Migration(1, 2) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.apply {
            createIndices()
            createViews()
        }
    }

    private fun SupportSQLiteDatabase.createIndices() {
        execSQL("CREATE INDEX IF NOT EXISTS `service_index` ON `service` (`name`, `operator_id`)")

        execSQL("""
            CREATE INDEX IF NOT EXISTS `service_point_index`
            ON `service_point` (`service_id`, `route_section`, `order_value`)
        """.trimIndent())

        execSQL("CREATE INDEX IF NOT EXISTS `stop_naptan_code_index` ON `stop` (`naptan_code`)")
        execSQL("CREATE INDEX IF NOT EXISTS `stop_atco_code_index` ON `stop` (`atco_code`)")
    }

    private fun SupportSQLiteDatabase.createViews() {
        execSQL("""
            CREATE VIEW IF NOT EXISTS `service_view` AS $CREATE_SERVICE_VIEW_STATEMENT
        """.trimIndent())
    }
}
