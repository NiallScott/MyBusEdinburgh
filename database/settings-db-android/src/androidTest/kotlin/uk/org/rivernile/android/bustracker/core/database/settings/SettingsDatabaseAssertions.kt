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

import androidx.sqlite.db.SupportSQLiteDatabase
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Assert that after a migration, the `active_alerts` triggers still exist. This only applies up to
 * version 4 of the database.
 */
internal fun SupportSQLiteDatabase.assertAlertTriggersExistUpToVersion4() {
    query("""
        SELECT name, tbl_name
        FROM sqlite_master
        WHERE type = 'trigger'
        ORDER BY name ASC
        """)
        .use { result ->
            assertEquals(3, result.count)

            assertTrue(result.moveToNext())
            assertEquals("delete_alert", result.getString(0))
            assertEquals("active_alerts", result.getString(1))

            assertTrue(result.moveToNext())
            assertEquals("insert_alert", result.getString(0))
            assertEquals("active_alerts", result.getString(1))

            assertTrue(result.moveToNext())
            assertEquals("update_alert", result.getString(0))
            assertEquals("active_alerts", result.getString(1))
        }
}

/**
 * Assert that after a migration, the alert tables triggers exist. This applies after version 5 of
 * the database.
 */
internal fun SupportSQLiteDatabase.assertAlertTablesTriggersExist() {
    query("""
            SELECT name, tbl_name
            FROM sqlite_master
            WHERE type = 'trigger'
            ORDER BY name ASC
        """.trimIndent()).use { result ->
        assertEquals(6, result.count)

        assertTrue(result.moveToNext())
        assertEquals("delete_arrival_alert", result.getString(0))
        assertEquals("arrival_alert", result.getString(1))

        assertTrue(result.moveToNext())
        assertEquals("delete_proximity_alert", result.getString(0))
        assertEquals("proximity_alert", result.getString(1))

        assertTrue(result.moveToNext())
        assertEquals("insert_arrival_alert", result.getString(0))
        assertEquals("arrival_alert", result.getString(1))

        assertTrue(result.moveToNext())
        assertEquals("insert_proximity_alert", result.getString(0))
        assertEquals("proximity_alert", result.getString(1))

        assertTrue(result.moveToNext())
        assertEquals("update_arrival_alert", result.getString(0))
        assertEquals("arrival_alert", result.getString(1))

        assertTrue(result.moveToNext())
        assertEquals("update_proximity_alert", result.getString(0))
        assertEquals("proximity_alert", result.getString(1))
    }
}
