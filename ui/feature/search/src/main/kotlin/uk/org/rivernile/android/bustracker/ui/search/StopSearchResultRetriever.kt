/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.search

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import javax.inject.Inject
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * This is used to perform the search and emits its results with [StopSearchResultState].
 *
 * @author Niall Scott
 */
internal interface StopSearchResultRetriever {

    /**
     * A [Flow] which emits the current state of the search, with [StopSearchResultState].
     */
    val stopSearchResultStateFlow: Flow<StopSearchResultState>
}

private const val SEARCH_TERM_MIN_LENGTH = 3

internal class RealStopSearchResultRetriever @Inject constructor(
    private val state: State,
    private val busStopsRepository: BusStopsRepository
) : StopSearchResultRetriever {

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override val stopSearchResultStateFlow get() = state
        .searchTermFlow
        .map { it?.trim() }
        .debounce { searchTerm ->
            if (searchTerm.isSearchTermValid()) SEARCH_TERM_DEBOUNCE_PERIOD_MILLIS else 0L
        }
        .flatMapLatest(::getStopSearchResultStateFlowWithSearchTerm)

    private fun getStopSearchResultStateFlowWithSearchTerm(
        searchTerm: String?
    ): Flow<StopSearchResultState> {
        return if (searchTerm.isSearchTermValid()) {
            busStopsRepository
                .getStopSearchResultsFlow(searchTerm)
                .map { searchResults ->
                    StopSearchResultState.Results(
                        results = searchResults
                    )
                }
                .onStart<StopSearchResultState> { emit(StopSearchResultState.InProgress) }
        } else {
            flowOf(StopSearchResultState.EmptySearchTerm)
        }
    }

    @OptIn(ExperimentalContracts::class)
    private fun String?.isSearchTermValid(): Boolean {
        contract {
            returns(true) implies (this@isSearchTermValid != null)
        }

        return !isNullOrBlank() && length >= SEARCH_TERM_MIN_LENGTH
    }
}
