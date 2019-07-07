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
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceManager
import uk.org.rivernile.android.bustracker.core.utils.TimeUtils
import javax.inject.Inject
import javax.net.SocketFactory

/**
 * This class will create new [DatabaseUpdateCheckerSession] objects which allow the database to
 * be updated.
 *
 * @param apiEndpoint The endpoint to get the database metadata from, which describes the latest
 * database.
 * @param databaseInformationDao A DAO for accessing the current topology metadata.
 * @param databaseUpdater The implementation to download and update the database.
 * @param preferenceManager The [PreferenceManager].
 * @param timeUtils Utility class for obtaining a timestamp.
 * @author Niall Scott
 */
class DatabaseUpdateChecker @Inject constructor(
        private val apiEndpoint: ApiEndpoint,
        private val databaseInformationDao: DatabaseInformationDao,
        private val databaseUpdater: DatabaseUpdater,
        private val preferenceManager: PreferenceManager,
        private val timeUtils: TimeUtils) {

    /**
     * Create a new database update check session.
     *
     * @param socketFactory The [SocketFactory] to use to connect to the API service.
     * @return A [DatabaseUpdateCheckerSession] object.
     */
    fun createNewSession(socketFactory: SocketFactory? = null): DatabaseUpdateCheckerSession {
        val apiRequest = apiEndpoint.createDatabaseVersionRequest(socketFactory)

        return DatabaseUpdateCheckerSession(apiRequest, databaseInformationDao, databaseUpdater,
                preferenceManager, timeUtils, socketFactory)
    }
}