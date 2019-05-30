/*
 * Copyright (C) 2019 Niall 'Rivernile' Scott
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

import uk.org.rivernile.android.bustracker.core.database.busstop.daos.DatabaseInformationDao
import uk.org.rivernile.android.bustracker.core.endpoints.api.ApiEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.api.ApiException
import uk.org.rivernile.android.bustracker.core.endpoints.api.ApiRequest
import uk.org.rivernile.android.bustracker.core.endpoints.api.DatabaseVersion
import javax.inject.Inject

/**
 * This class will check to see if a new bus stop database is available, and if so, perform the
 * update of the database.
 *
 * @param apiEndpoint The endpoint to get the database metadata from, which describes the latest
 * database.
 * @param databaseInformationDao A DAO for accessing the current topology metadata.
 * @param databaseUpdater The implementation to download and update the database.
 * @author Niall Scott
 */
class DatabaseUpdateChecker @Inject constructor(
        private val apiEndpoint: ApiEndpoint,
        private val databaseInformationDao: DatabaseInformationDao,
        private val databaseUpdater: DatabaseUpdater) {

    private var currentRequest: ApiRequest<DatabaseVersion>? = null

    /**
     * Check for any new database updates, and if an update is available, update the database.
     *
     * @return `true` if the check was successful, otherwise `false`.
     */
    fun checkForDatabaseUpdates(): Boolean {
        // Make sure any previous invocations have been cancelled.
        cancel()

        val currentRequest = apiEndpoint.createDatabaseVersionRequest()
        this.currentRequest = currentRequest

        val databaseVersion = try {
            currentRequest.performRequest()
        } catch (ignored: ApiException) {
            return false
        }

        val currentTopologyId = databaseInformationDao.getTopologyId()

        return if (databaseVersion.topologyId != currentTopologyId) {
            databaseUpdater.updateDatabase(databaseVersion)
        } else {
            // The check was successful but there was no change.
            true
        }
    }

    /**
     * Cancel any current in-flight database checks or updates.
     */
    fun cancel() {
        val currentRequest = this.currentRequest
        this.currentRequest = null
        currentRequest?.cancel()
        databaseUpdater.cancel()
    }
}