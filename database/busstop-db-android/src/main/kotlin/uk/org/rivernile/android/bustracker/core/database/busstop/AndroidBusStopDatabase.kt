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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForIoDispatcher
import uk.org.rivernile.android.bustracker.core.database.busstop.database.DatabaseDao
import uk.org.rivernile.android.bustracker.core.database.busstop.database.ProxyDatabaseDao
import uk.org.rivernile.android.bustracker.core.database.busstop.service.ProxyServiceDao
import uk.org.rivernile.android.bustracker.core.database.busstop.service.ServiceDao
import uk.org.rivernile.android.bustracker.core.database.busstop.servicepoint.ProxyServicePointDao
import uk.org.rivernile.android.bustracker.core.database.busstop.servicepoint.ServicePointDao
import uk.org.rivernile.android.bustracker.core.database.busstop.servicestop.ProxyServiceStopDao
import uk.org.rivernile.android.bustracker.core.database.busstop.servicestop.ServiceStopDao
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.ProxyStopDao
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopDao
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This is the Android-specific implementation of the bus stop database.
 *
 * @param context The application [Context].
 * @param databaseFactory Used to construct [RoomBusStopDatabase] instances.
 * @param downloadedDatabasePreparer Used to prepare downloaded databases by migrating them to the
 * expected schema.
 * @param ioDispatcher The IO [CoroutineDispatcher].
 * @author Niall Scott
 */
@Singleton
internal class AndroidBusStopDatabase @Inject constructor(
    private val context: Context,
    private val databaseFactory: RoomBusStopDatabaseFactory,
    private val downloadedDatabasePreparer: DownloadedDatabasePreparer,
    @param:ForIoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    companion object {

        private const val DATABASE_NAME = "busstops10.db"
    }

    // Creates the RoomBusStopDatabase. Calling createRoomDatabase does not do IO blocking as the
    // database is lazily opened on the first query. This just creates a reference to allow us to
    // talk to the database.
    private var database = databaseFactory.createRoomBusStopDatabase(DATABASE_NAME, true)
    private val databaseMutex = Mutex()
    private val _isDatabaseOpenFlow = MutableStateFlow(true)

    /**
     * A [Flow] which emits whether the database is open or not.
     */
    val isDatabaseOpenFlow: Flow<Boolean> get() = _isDatabaseOpenFlow

    /**
     * The [DatabaseDao].
     */
    val databaseDao: DatabaseDao = ProxyDatabaseDao(this)

    /**
     * The [ServiceDao].
     */
    val serviceDao: ServiceDao = ProxyServiceDao(this)

    /**
     * The [ServicePointDao].
     */
    val servicePointDao: ServicePointDao = ProxyServicePointDao(this)

    /**
     * The [ServiceStopDao].
     */
    val serviceStopDao: ServiceStopDao = ProxyServiceStopDao(this)

    /**
     * The [StopDao].
     */
    val stopDao: StopDao = ProxyStopDao(this)

    val roomDatabaseDao get() = database.databaseDao
    val roomServiceDao get() = database.serviceDao
    val roomServicePointDao get() = database.servicePointDao
    val roomServiceStopDao get() = database.serviceStopDao
    val roomStopDao get() = database.stopDao

    /**
     * Given a [newDatabaseFile], attempt to replace the existing database with this file. If the
     * new database does not pass some internal checks, the operation will fail and the existing
     * database will continue to be used.
     *
     * @param newDatabaseFile The new database file. This is assumed to already be in the database
     * directory.
     * @return `true` if the database was replaced, `false` if not.
     */
    suspend fun replaceDatabase(newDatabaseFile: File): Boolean {
        return databaseMutex.withLock {
            if (downloadedDatabasePreparer.prepareDownloadedDatabase(newDatabaseFile)) {
                database.close()
                _isDatabaseOpenFlow.value = false
                replaceDatabaseFile(newDatabaseFile)

                database = databaseFactory.createRoomBusStopDatabase(DATABASE_NAME, true)
                _isDatabaseOpenFlow.value = true

                true
            } else {
                withContext(ioDispatcher) {
                    newDatabaseFile.delete()
                }

                false
            }
        }
    }

    /**
     * Replace the existing database file by removing it and renaming the [newDatabaseFile] to the
     * same name as the expected database file.
     *
     * @param newDatabaseFile The new database file.
     */
    private suspend fun replaceDatabaseFile(newDatabaseFile: File) {
        withContext(ioDispatcher) {
            context.deleteDatabase(DATABASE_NAME)
            newDatabaseFile.renameTo(context.getDatabasePath(DATABASE_NAME))
        }
    }
}