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

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

/**
 * This callback is called by the [RoomDatabase] throughout various parts of its lifecycle. We use
 * this callback to perform actions that we otherwise are unable to through Room's usual API.
 *
 * @author Niall Scott
 */
internal class SettingsDatabaseCallback @Inject constructor() : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        db.createAlertTablesTriggers()
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
