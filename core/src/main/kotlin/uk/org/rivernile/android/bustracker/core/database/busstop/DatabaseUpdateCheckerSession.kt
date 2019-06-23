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
import java.util.concurrent.atomic.AtomicBoolean

/**
 * This class will check to see if a new bus stop database is available, and if so, perform the
 * update of the database.
 *
 * Each instance of this class represents a single session. Each session can only be attempted
 * once. A new instance will need to be acquired to attempt a new session.
 *
 * @param apiEndpoint The endpoint to get the database metadata from, which describes the latest
 * database.
 * @param databaseInformationDao A DAO for accessing the current topology metadata.
 * @param databaseUpdater The implementation to download and update the database.
 * @author Niall Scott
 */
class DatabaseUpdateCheckerSession internal constructor(
        private val apiEndpoint: ApiEndpoint,
        private val databaseInformationDao: DatabaseInformationDao,
        private val databaseUpdater: DatabaseUpdater) {

    private val hasRun = AtomicBoolean(false)
    private var request: ApiRequest<DatabaseVersion>? = null
    private var databaseUpdaterSession: DatabaseUpdaterSession? = null

    /**
     * Check for any new database updates, and if an update is available, update the database.
     *
     * This method can only be run once per session object. If a further attempt is tried on the
     * same instance, an [IllegalStateException] will be thrown.
     *
     * @return `true` if the check was successful, otherwise `false`.
     * @throws IllegalStateException When this session object is attempted more than once.
     */
    fun checkForDatabaseUpdates(): Boolean {
        if (!hasRun.compareAndSet(false, true)) {
            throw IllegalStateException("Each session can only be run once.")
        }

        val request = apiEndpoint.createDatabaseVersionRequest()
        this.request = request

        val databaseVersion = try {
            request.performRequest()
        } catch (ignored: ApiException) {
            return false
        }

        val currentTopologyId = databaseInformationDao.getTopologyId()

        return if (databaseVersion.topologyId != currentTopologyId) {
            updateDatabase(databaseVersion)
        } else {
            // The check was successful but there was no change in topology ID.
            true
        }
    }

    /**
     * Cancel any current in-flight database checks or updates. If this session has not yet been
     * run, calling this method will have no effect.
     */
    fun cancel() {
        request?.cancel()
        databaseUpdaterSession?.cancel()
    }

    /**
     * Given the conditions to update the database have been matched, perform the update of the
     * database.
     *
     * @param databaseVersion The metadata of the newly available database.
     * @return `true` if updating the database was successful, `false` if not.
     */
    private fun updateDatabase(databaseVersion: DatabaseVersion): Boolean {
        val databaseUpdaterSession = databaseUpdater.createNewSession(databaseVersion)
        this.databaseUpdaterSession = databaseUpdaterSession

        return databaseUpdaterSession.updateDatabase()
    }
}