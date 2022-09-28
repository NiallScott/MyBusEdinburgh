/*
 * Copyright (C) 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.search

import uk.org.rivernile.android.bustracker.core.database.search.daos.SearchHistoryDao
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This repository is used to access data and perform actions on search history.
 *
 * @param searchHistoryDao Used to access the search history data.
 * @author Niall Scott
 */
@Singleton
class SearchHistoryRepository @Inject internal constructor(
        private val searchHistoryDao: SearchHistoryDao) {

    /**
     * Add a new search term to the search history collection of previous search terms.
     *
     * @param searchTerm The search term to add.
     */
    suspend fun addSearchTerm(searchTerm: String) {
        searchHistoryDao.addSearchTerm(searchTerm)
    }

    /**
     * Clear the search history, so that all previously entered user search terms are removed.
     */
    suspend fun clearSearchHistory() {
        searchHistoryDao.clearSearchHistory()
    }
}