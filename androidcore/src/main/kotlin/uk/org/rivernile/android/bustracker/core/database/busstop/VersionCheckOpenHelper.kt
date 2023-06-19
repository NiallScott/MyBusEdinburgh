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

package uk.org.rivernile.android.bustracker.core.database.busstop

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import java.io.File

/**
 * A [SupportSQLiteOpenHelper] which intercepts opening actions on the database and uses this to
 * check the state of the database prior to opening. If the database is not in a minimally expected
 * state, the database is removed. In this case, Room will extract the database in shared
 * preferences.
 *
 * Actions will be delegated to [delegate].
 *
 * @param context The application [Context].
 * @param delegate Where actions are delegated to after we handle them, or by default.
 * @param minimumUpdateTimestamp The minimal update timestamp the database should have. If the
 * database's value is less than this, the database will be removed.
 * @param exceptionLogger Used to log handled exceptions.
 * @author Niall Scott
 */
internal class VersionCheckOpenHelper(
    private val context: Context,
    private val delegate: SupportSQLiteOpenHelper,
    private val minimumUpdateTimestamp: Long,
    private val exceptionLogger: ExceptionLogger)
    : SupportSQLiteOpenHelper by delegate {

    companion object {

        private const val TABLE_DATABASE_INFO = "database_info"
        private const val COLUMN_UPDATE_TIMESTAMP_V1 = "updateTS"
        private const val COLUMN_UPDATE_TIMESTAMP_V2 = "updateTimestamp"
    }

    private var isOpen = false

    override val writableDatabase: SupportSQLiteDatabase get() {
        performDatabaseCheckIfClosed()

        return delegate.writableDatabase
    }

    override val readableDatabase: SupportSQLiteDatabase get() {
        performDatabaseCheckIfClosed()

        return delegate.readableDatabase
    }

    @Synchronized
    override fun close() {
        delegate.close()
        isOpen = false
    }

    /**
     * Perform the database check, but only if it is currently closed. If the database is open then
     * this method immediately returns. This method is thread-safe.
     */
    @Synchronized
    private fun performDatabaseCheckIfClosed() {
        if (!isOpen) {
            checkDatabaseVersion()
            isOpen = true
        }
    }

    /**
     * Check the database version (the update timestamp). If the timestamp is less than
     * [minimumUpdateTimestamp] or there was an error while trying to retrieve the current update
     * timestamp, the database will be deleted.
     *
     * In the case the database didn't already exist or this method deleted it, Room will extract
     * the pre-packaged database from the application assets for us downstream.
     */
    private fun checkDatabaseVersion() {
        val databaseFile = context.getDatabasePath(databaseName)

        if (!databaseFile.exists()) {
            // If the database does not exist, shortcut the check as there is nothing to check. The
            // database will be extracted from assets by Room when it attempts to open the database.
            return
        }

        var deleteDatabase = false

        try {
            // We only want to open the database then perform a single query upon it before closing
            // it again. The new SupportSQLiteDatabase way of opening the database is quite
            // cumbersome to do this, and may attempt to update the database.
            //
            // So we use the old SQLiteDatabase class which has been in Android since API level 1.
            // This allows us to simply open the database. We open it in read-only mode, and a
            // database file will not be created if it does not already exist (although by reaching
            // this code, a database file must already exist). When this happens, a SQLiteException
            // will be thrown - which is okay, because we handle it and mark the database for
            // deletion (which is also okay if it doesn't exist).
            //
            // If any error at all occurs while opening the database and performing the query, the
            // database will be marked for deletion. Downstream, after the database has been
            // deleted, Room will extract a database from the application assets for us.
            SQLiteDatabase.openDatabase(
                databaseFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY)
                .use { db ->
                    val updateTimestamp = db.updateTimestamp

                    if (updateTimestamp == null || updateTimestamp < minimumUpdateTimestamp) {
                        deleteDatabase = true
                    }
                }
        } catch (e: SQLiteException) {
            exceptionLogger.log(e)
            deleteDatabase = true
        }

        if (deleteDatabase) {
            context.deleteDatabase(databaseName)
            File("${databaseFile.absolutePath}-journal").delete()
        }
    }

    /**
     * The update timestamp contained within the database. This will be `null` when the timestamp
     * could not be found. A [SQLiteException] will be thrown when there was an error while trying
     * to obtain the timestamp.
     */
    private val SQLiteDatabase.updateTimestamp: Long? get() {
        // The name of the update timestamp column was changed in version 2 of the database. So we
        // get the version of the database and adjust the name of the column we query for
        // appropriately.
        val updateTimestampColumn =
            if (version == 1) COLUMN_UPDATE_TIMESTAMP_V1 else COLUMN_UPDATE_TIMESTAMP_V2

        return query(
            TABLE_DATABASE_INFO, // Table name
            arrayOf(updateTimestampColumn), // Projection columns
            null, // Selection
            null, // Selection args
            null, // Group by
            null, // Having
            "$updateTimestampColumn DESC", // Order by
            "1") // Limit
            .use {
                if (it.moveToFirst() && !it.isNull(0)) {
                    it.getLong(0)
                } else {
                    null
                }
            }
    }
}