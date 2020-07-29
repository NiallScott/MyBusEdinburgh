/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.search

import android.app.SearchManager
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.database.MergeCursor
import android.provider.BaseColumns
import uk.org.rivernile.android.bustracker.core.database.busstop.BusStopsContract
import javax.inject.Inject
import kotlin.math.max

/**
 * The purpose of this class is to fetch suggestions to display in the stop search functionality and
 * merge them with them with the user's recent search history.
 *
 * @param context The application [Context].
 * @param busStopsContract The contract for talking with the bus stops database table.
 * @author Niall Scott
 */
internal class SuggestionsFetcher @Inject constructor(
        private val context: Context,
        private val busStopsContract: BusStopsContract) {

    companion object {

        private const val SELECTION = "${BusStopsContract.LOCALITY} LIKE ?"

        private val COLUMNS = arrayOf(
                SearchManager.SUGGEST_COLUMN_FORMAT,
                SearchManager.SUGGEST_COLUMN_ICON_1,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2,
                SearchManager.SUGGEST_COLUMN_QUERY,
                BaseColumns._ID)
    }

    /**
     * Based on the search term, fetch suggestions based on this term and merge the result with the
     * recentCursor.
     *
     * @param recentCursor A [Cursor] containing the user's recent search history.
     * @param searchTerm The search term to base suggestions on.
     * @return A [Cursor] which is the merge of the recent searches and the search suggestions.
     */
    fun fetchAndMergeSuggestions(recentCursor: Cursor?, searchTerm: String): Cursor? {
        val recentLastIndex = findLargestId(recentCursor)
        val suggestionsCursor = getSuggestionsCursor(searchTerm, recentLastIndex + 1)

        return suggestionsCursor?.let {
            MergeCursor(arrayOf(recentCursor, it))
        } ?: recentCursor
    }

    /**
     * Get a [Cursor] which contains suggested items to include within the recent search items.
     *
     * @param searchTerm The user's search term.
     * @param startId The ID our [Cursor] items should start at.
     * @return A [Cursor] containing suggested items.
     */
    private fun getSuggestionsCursor(searchTerm: String, startId: Int) = context.contentResolver
            .query(
                    busStopsContract.getContentUri(),
                    arrayOf("DISTINCT " + BusStopsContract.LOCALITY),
                    SELECTION,
                    arrayOf("%$searchTerm%"),
                    "${BusStopsContract.LOCALITY} ASC"
            )?.use {
                val localityColumn = it.getColumnIndex(BusStopsContract.LOCALITY)
                val result = MatrixCursor(COLUMNS)
                val transparentResource = "android.resource://${context.packageName}/" +
                        "${android.R.color.transparent}"
                val count = it.count

                for (i in 0 until count) {
                    it.moveToPosition(i)
                    val locality = it.getString(localityColumn)

                    result.addRow(arrayOf(
                            null,
                            transparentResource,
                            locality,
                            null,
                            locality,
                            startId + i
                    ))
                }

                result
            }

    /**
     * Find the largest ID from the recent [Cursor]. The suggestions we generate mustn't have IDs
     * which clash with the recent [Cursor] so we start the suggestions [Cursor] IDs at the last
     * recent ID, plus 1.
     *
     * @param recentCursor The recent [Cursor].
     * @return The largest ID found in the recent [Cursor], or `0` if the [Cursor] is `null`.
     */
    private fun findLargestId(recentCursor: Cursor?) = recentCursor?.let {
        var maxId = 0
        it.moveToPosition(-1)
        val idColumn = it.getColumnIndex(BaseColumns._ID)

        while (it.moveToNext()) {
            val id = it.getInt(idColumn)
            maxId = max(id, maxId)
        }

        it.moveToPosition(-1)

        maxId
    } ?: 0
}