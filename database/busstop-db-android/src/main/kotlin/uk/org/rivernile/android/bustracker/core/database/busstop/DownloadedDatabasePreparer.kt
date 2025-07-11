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

import android.database.SQLException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okio.IOException
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForIoDispatcher
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import java.io.File
import javax.inject.Inject

/**
 * This prepares a downloaded database file prior to it replacing the existing database. The
 * candidate database will be migrated to the expected schema before replacement.
 *
 * @author Niall Scott
 */
internal interface DownloadedDatabasePreparer {

    /**
     * Prepare a downloaded database by migrating it to the expected schema prior to it replacing
     * the current database.
     *
     * @param databaseFile The candidate [BusStopDatabaseFile] to replace the existing database.
     * @return `true` if preparation was successful and this database should replace the existing
     * database, otherwise `false` if preparation was not successful and this candidate should be
     * scrapped.
     */
    suspend fun prepareDownloadedDatabase(databaseFile: BusStopDatabaseFile): Boolean
}

internal class RealDownloadedDatabasePreparer @Inject constructor(
    private val databaseOpener: DatabaseOpener,
    private val databaseFactory: RoomBusStopDatabaseFactory,
    private val exceptionLogger: ExceptionLogger,
    @param:ForIoDispatcher private val ioDispatcher: CoroutineDispatcher
) : DownloadedDatabasePreparer {

    override suspend fun prepareDownloadedDatabase(databaseFile: BusStopDatabaseFile): Boolean {
        return if (ensureDatabaseHasVersionCode(databaseFile)) {
            var result: Boolean

            databaseFactory.createRoomBusStopDatabase(databaseFile.absolutePath, false)
                .apply {
                    result = tryTestQuery()
                }
                .close()

            result
        } else {
            false
        }
    }

    /**
     * This method will open and then immediately close the database to ensure it has a version
     * code.
     *
     * Downloaded databases will most likely not have a version code because the server does not
     * give them one. Without a version code, Room will not run migrations for us, so we will not
     * be able to have the database on the correct schema without this.
     *
     * During the brief open of the database, a version code of at least 1 will be assigned to the
     * database (and if higher, it will be left as-is).
     *
     * @param databaseFile The [File] which contains the database.
     * @return `true` when we were able to open the database file and it now has a valid version
     * code, otherwise `false`.
     */
    private suspend fun ensureDatabaseHasVersionCode(databaseFile: BusStopDatabaseFile): Boolean {
        return withContext(ioDispatcher) {
            try {
                // This will open the database and force the version code to be at least 1. Prior to
                // handing the database over to Room, the version code needs to be at least 1 before
                // it will run migrations.
                databaseOpener.createOpenHelper(databaseFile).writableDatabase.close()
                true
            } catch (e: IOException) {
                exceptionLogger.log(e)
                false
            } catch (e: SQLException) {
                exceptionLogger.log(e)
                false
            }
        }
    }

    /**
     * Try a test query out on the database. If this query runs without encountering any exceptions
     * then it is deemed to pass and `true` will be returned. Conversely, if an exception is thrown
     * during the test, the test fails and `false` is returned.
     *
     * @return `true` if the test passed, otherwise `false`.
     */
    private suspend fun RoomBusStopDatabase.tryTestQuery(): Boolean {
        return withContext(ioDispatcher) {
            try {
                query("SELECT 1 FROM database_info", null).close()
                true
            } catch (e: SQLException) {
                exceptionLogger.log(e)
                false
            } catch (e: IllegalStateException) {
                // This is thrown when Room verifies the schema and verification failed.
                exceptionLogger.log(e)
                false
            }
        }
    }
}