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

import uk.org.rivernile.android.bustracker.core.database.DatabaseUtils
import uk.org.rivernile.android.bustracker.core.endpoints.api.DatabaseVersion
import uk.org.rivernile.android.bustracker.core.http.FileDownloader
import uk.org.rivernile.android.bustracker.core.utils.FileConsistencyChecker
import uk.org.rivernile.android.bustracker.core.utils.TimeUtils
import javax.inject.Inject

/**
 * This class will create new [DatabaseUpdaterSession] object which will perform the action of
 * updating the database.
 *
 * @param databaseUtils Utilities for dealing with databases.
 * @param fileDownloader An implementation for downloading files to a path.
 * @param fileConsistencyChecker An implementation for checking the consistency of files.
 * @param databaseRepository The repository which represents this database.
 * @param timeUtils Used to access timestamps.
 * @author Niall Scott
 */
class DatabaseUpdater @Inject constructor(
        private val databaseUtils: DatabaseUtils,
        private val fileDownloader: FileDownloader,
        private val fileConsistencyChecker: FileConsistencyChecker,
        private val databaseRepository: BusStopDatabaseRepository,
        private val timeUtils: TimeUtils) {

    /**
     * Create a new database update session.
     *
     * @param databaseVersion Metadata describing the database to update to.
     * @return A [DatabaseUpdaterSession] object.
     */
    fun createNewSession(databaseVersion: DatabaseVersion) =
            DatabaseUpdaterSession(databaseUtils, fileDownloader, fileConsistencyChecker,
                    databaseRepository, timeUtils, databaseVersion)
}