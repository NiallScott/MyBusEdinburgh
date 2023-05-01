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
        db.createNewAlertTriggers()
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
            FOR EACH ROW BEGIN
                DELETE FROM active_alerts 
                WHERE timeAdded < ((SELECT strftime('%s','now') * 1000) - 3600000);
            END
        """.trimIndent())
    }
}