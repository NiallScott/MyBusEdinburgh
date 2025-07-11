/*
 * Copyright (C) 2023 - 2025 Niall 'Rivernile' Scott
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
import android.database.SQLException
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import okio.IOException
import okio.buffer
import okio.sink
import okio.source
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import java.io.File

/**
 * This [SupportSQLiteOpenHelper] performs the extraction of the bundled database in the following
 * circumstances;
 *
 * - The database does not yet exist.
 * - The on-device database is older than the bundled database.
 * - There was an issue opening the existing database.
 *
 * All other database operations are delegated to [delegate] to perform.
 *
 * This implementation borrows some code from Room.
 *
 * @param context The application [Context].
 * @param delegate Where database operations should be delegated to.
 * @param minimumUpdateTimestamp The minimum update time of the database. If the current database
 * has a timestamp less than this, it will be replaced.
 * @param bundledDatabaseAssetPath The path within the application assets where the bundled database
 * is located.
 * @param databaseOpener An implementation used to open the database to peek in to it before it is
 * handed off to Room.
 * @param exceptionLogger Used to log exceptions.
 * @author Niall Scott
 */
internal class BundledDatabaseOpenHelper(
    private val context: Context,
    private val delegate: SupportSQLiteOpenHelper,
    private val minimumUpdateTimestamp: Long,
    private val bundledDatabaseAssetPath: String,
    private val databaseOpener: DatabaseOpener,
    private val exceptionLogger: ExceptionLogger
) : SupportSQLiteOpenHelper by delegate {

    companion object {

        private const val TABLE_DATABASE_INFO = "database_info"
        private const val COLUMN_UPDATE_TIMESTAMP_V1 = "updateTS"
        private const val COLUMN_UPDATE_TIMESTAMP_V2 = "updateTimestamp"
    }

    private var verified = false

    override val writableDatabase: SupportSQLiteDatabase
        get() {
            performVerificationIfRequired()
            return delegate.writableDatabase
        }

    override val readableDatabase: SupportSQLiteDatabase
        get() {
            performVerificationIfRequired()
            return delegate.readableDatabase
        }

    @Synchronized
    override fun close() {
        delegate.close()
        verified = false
    }

    /**
     * Perform the verification of the database if it has not yet been verified.
     */
    @Synchronized
    private fun performVerificationIfRequired() {
        if (!verified) {
            verifyDatabaseFile()
            verified = true
        }
    }

    /**
     * Verify the database file. The following checks and operations are performed;
     *
     * - If the database does not exist, copy the bundled database from assets.
     * - If the database already exists, open it and obtain the last update timestamp.
     * - If the last update timestamp is the same as or newer than the bundled database, then
     *   verification is complete and database can then be opened properly.
     * - If the last update timestamp is older than the bundled database, the existing database is
     *   removed and the bundled database is put in its place.
     *
     * This method handles any exceptions which occur. Upon this method returning, the database can
     * be assumed to verified in one form or another.
     */
    private fun verifyDatabaseFile() {
        val name = checkNotNull(databaseName)
        val databaseFile = context.getDatabasePath(name)

        if (!databaseFile.exists()) {
            try {
                copyDatabaseFile(databaseFile)
            } catch (e: IOException) {
                exceptionLogger.log(e)
            } catch (e: SQLException) {
                exceptionLogger.log(e)
            }

            // Regardless if the database copy was successful or not, we return here. If it failed,
            // it was likely due to low disk space. If we haven't instated a database, then after
            // the database opens Room will create the database with the expected schema, albeit an
            // empty one. But that's better than crashing, right?
            return
        }

        var deleteDatabase = false

        try {
            databaseOpener
                .createOpenHelper(databaseFile.toBusStopDatabaseFile())
                .readableDatabase
                .use { db ->
                    val updateTimestamp = db.updateTimestamp

                    if (updateTimestamp == null || updateTimestamp < minimumUpdateTimestamp) {
                        deleteDatabase = true
                    }
                }
        } catch (e: IOException) {
            exceptionLogger.log(e)
            deleteDatabase = true
        } catch (e: SQLException) {
            exceptionLogger.log(e)
            deleteDatabase = true
        }

        if (deleteDatabase) {
            context.deleteDatabase(name)

            try {
                copyDatabaseFile(databaseFile)
            } catch (e: IOException) {
                exceptionLogger.log(e)
            } catch (e: SQLException) {
                exceptionLogger.log(e)
            }
        }
    }

    /**
     * Copy the database file from the application assets in to the location specified by
     * [destinationFile]. This can fail in multiple ways.
     *
     * @param destinationFile Where the database should be copied to.
     * @throws IOException When there was an issue copying the file to the destination.
     * @throws SQLException When the database could not be opened in the verification phase.
     */
    @Throws(IOException::class, SQLException::class)
    private fun copyDatabaseFile(destinationFile: File) {
        // Firstly, attempt to create the required directories. If this fails then there isn't much
        // point in continuing.
        createDirectories(destinationFile)

        // An intermediate file is used so that we never end up with a half-copied database file
        // in the internal directory. (From Room)
        val intermediateFile = File.createTempFile("mybus-database", ".tmp", context.cacheDir)
        intermediateFile.deleteOnExit()

        try {
            context.assets.open(bundledDatabaseAssetPath).source().buffer().use { source ->
                intermediateFile.sink().buffer().use { sink ->
                    sink.writeAll(source)
                }
            }

            // We open and immediately close the database so that its version code is set to 1. This
            // means that downstream Room will run the migration code for us.
            databaseOpener
                .createOpenHelper(intermediateFile.toBusStopDatabaseFile())
                .writableDatabase
                .close()
        } catch (e: IOException) {
            intermediateFile.delete()
            throw e
        } catch (e: SQLException) {
            intermediateFile.delete()
            throw e
        }

        if (!intermediateFile.renameTo(destinationFile)) {
            intermediateFile.delete()
            throw IOException("Failed to move intermediate file " +
                    "(${intermediateFile.absolutePath}) to destination " +
                    "(${destinationFile.absolutePath}).")
        }
    }

    /**
     * Given a [destinationFile], ensure its path exists and create the path of directories if
     * necessary.
     *
     * @param destinationFile The intended destination of the database file.
     * @throws IOException When there was a failure if and when the path tried to be created.
     */
    @Throws(IOException::class)
    private fun createDirectories(destinationFile: File) {
        val parent = destinationFile.parentFile

        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw IOException("Failed to create directories for ${destinationFile.path}")
        }
    }

    /**
     * The update timestamp contained within the database. This will be `null` when the timestamp
     * could not be found.
     *
     * @throws [SQLException] when there was an error while trying to obtain the timestamp.
     */
    @get:Throws(SQLException::class)
    private val SupportSQLiteDatabase.updateTimestamp: Long? get() {
        val updateTimestampColumn =
            if (version < 2) COLUMN_UPDATE_TIMESTAMP_V1 else COLUMN_UPDATE_TIMESTAMP_V2

        return query("""
            SELECT $updateTimestampColumn 
            FROM $TABLE_DATABASE_INFO 
            ORDER BY $updateTimestampColumn DESC 
            LIMIT 1
        """.trimIndent())
            .use {
                if (it.moveToFirst() && !it.isNull(0)) {
                    it.getLong(0)
                } else {
                    null
                }
            }
    }
}