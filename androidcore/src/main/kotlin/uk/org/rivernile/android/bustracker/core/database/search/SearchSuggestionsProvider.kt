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

import android.content.SearchRecentSuggestionsProvider
import android.database.Cursor
import android.net.Uri
import dagger.android.AndroidInjection
import javax.inject.Inject

/**
 * This [android.content.ContentProvider] provides suggested search items when the user begins
 * typing a search term in to the search box.
 *
 * @author Niall Scott
 */
class SearchSuggestionsProvider : SearchRecentSuggestionsProvider() {

    @Inject
    internal lateinit var databaseContract: SearchDatabaseContract
    @Inject
    internal lateinit var suggestionsFetcher: SuggestionsFetcher

    override fun onCreate(): Boolean {
        AndroidInjection.inject(this)

        setupSuggestions(databaseContract.authority, SearchDatabaseContract.MODE)

        return super.onCreate()
    }

    override fun query(
            uri: Uri,
            projection: Array<out String>?,
            selection: String?,
            selectionArgs: Array<out String>?,
            sortOrder: String?): Cursor? {
        val recentCursor = super.query(uri, projection, selection, selectionArgs, sortOrder)

        // If a search term exists, fetch and merge the suggestions Cursor. Otherwise, just return
        // return the recent Cursor.
        return selectionArgs
                ?.firstOrNull()
                ?.ifEmpty { null }
                ?.let {
                    suggestionsFetcher.fetchAndMergeSuggestions(recentCursor, it)
                } ?: recentCursor
    }
}