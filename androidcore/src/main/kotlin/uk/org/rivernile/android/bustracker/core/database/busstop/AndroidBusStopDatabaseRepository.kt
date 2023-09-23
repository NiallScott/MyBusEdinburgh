/*
 * Copyright (C) 2019 - 2023 Niall 'Rivernile' Scott
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
import kotlinx.coroutines.withContext
import uk.org.rivernile.android.bustracker.core.database.busstop.daos.DatabaseInformationDao
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.DatabaseMetadata
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForIoDispatcher
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class represents an Android-specific implementation of the bus stop database repository.
 *
 * @param context The application [Context].
 * @param contract The contract for the bus stop database.
 * @param databaseInformationDao The DAO for accessing database information.
 * @author Niall Scott
 */
@Singleton
internal class AndroidBusStopDatabaseRepository @Inject constructor(
        private val context: Context,
        private val contract: BusStopDatabaseContract,
        private val databaseInformationDao: DatabaseInformationDao,
        @ForIoDispatcher private val ioDispatcher: CoroutineDispatcher)
    : BusStopDatabaseRepository {

    override suspend fun replaceDatabase(newDatabase: File) {
        withContext(ioDispatcher) {
            context.contentResolver.call(contract.getContentUri(),
                    BusStopDatabaseContract.METHOD_REPLACE_DATABASE, newDatabase.absolutePath, null)
        }
    }

    override val databaseMetadataFlow: Flow<DatabaseMetadata?> get() =
            databaseInformationDao.databaseMetadataFlow
}