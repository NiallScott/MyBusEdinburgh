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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import uk.org.rivernile.android.bustracker.core.database.busstop.database.DatabaseDao
import uk.org.rivernile.android.bustracker.core.database.busstop.database.DatabaseMetadata
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class represents an Android-specific implementation of the bus stop database repository.
 *
 * @param database The [AndroidBusStopDatabase].
 * @param databaseDao The DAO for accessing database information.
 * @author Niall Scott
 */
@Singleton
internal class AndroidBusStopDatabaseRepository @Inject constructor(
        private val database: AndroidBusStopDatabase,
        private val databaseDao: DatabaseDao)
    : BusStopDatabaseRepository {

    override suspend fun replaceDatabase(newDatabase: File) =
        database.replaceDatabase(newDatabase)

    override val databaseMetadataFlow: Flow<DatabaseMetadata?> get() =
        databaseDao.databaseMetadataFlow

    override suspend fun getTopologyVersionId() =
        databaseDao.topologyIdFlow.first()
}