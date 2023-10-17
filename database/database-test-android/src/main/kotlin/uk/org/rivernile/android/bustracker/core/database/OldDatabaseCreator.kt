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

package uk.org.rivernile.android.bustracker.core.database

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry

/**
 * This utility class is used to open an old database for testing.
 *
 * @author Niall Scott
 */
class OldDatabaseCreator {

    /**
     * Open a database for testing.
     *
     * @param databaseName The name of the database.
     * @param callback Supply a [SupportSQLiteOpenHelper.Callback] to create the database.
     * @return A [SupportSQLiteDatabase] for use for testing.
     */
    fun openDatabase(
        databaseName: String,
        callback: SupportSQLiteOpenHelper.Callback): SupportSQLiteDatabase {
        ensureNoExistingDatabase(databaseName)

        val configuration = SupportSQLiteOpenHelper.Configuration
            .builder(InstrumentationRegistry.getInstrumentation().targetContext)
            .name(databaseName)
            .callback(callback)
            .build()

        return FrameworkSQLiteOpenHelperFactory()
            .create(configuration)
            .writableDatabase
    }

    /**
     * Ensure that no database exists at the database path with the given name. If it does, an
     * attempt will be made to delete it. If it cannot be deleted, an [IllegalStateException] will
     * be thrown.
     *
     * @param dbName The name of the database.
     * @throws IllegalStateException When there is an existing database and it cannot be deleted.
     */
    private fun ensureNoExistingDatabase(dbName: String) {
        val dbPath = InstrumentationRegistry
            .getInstrumentation()
            .targetContext
            .getDatabasePath(dbName)

        if (dbPath.exists()) {
            if (!dbPath.delete()) {
                throw IllegalStateException("Database file already exists.")
            }
        }
    }
}