/*
 * Copyright (C) 2023 - 2025 Niall 'Rivernile' Scott
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

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.transformLatest
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopSearchResult
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.utils.SingleLiveEvent
import javax.inject.Inject

/**
 * This is the [ViewModel] for [SearchFragment].
 *
 * @param savedState Saved instance state.
 * @param busStopsRepository Used to query the search term to obtain results.
 * @param defaultDispatcher The default [CoroutineDispatcher].
 * @author Niall Scott
 */
@HiltViewModel
class SearchFragmentViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val busStopsRepository: BusStopsRepository,
    @param:ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    companion object {

        private const val STATE_SEARCH_TERM = "searchTerm"

        private const val SEARCH_TERM_MIN_LENGTH = 3
        private const val SEARCH_TERM_DEBOUNCE_PERIOD_MILLIS = 250L
        private const val SEARCH_PROGRESS_DELAY_MILLIS = 250L
    }

    /**
     * The user's search term.
     */
    var searchTerm: String?
        get() = searchTermFlow.value
        set(value) {
            savedState[STATE_SEARCH_TERM] = value
        }

    private val searchTermFlow = savedState.getStateFlow<String?>(STATE_SEARCH_TERM, null)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private val uiStateFlow = searchTermFlow
        .debounce { searchTerm ->
            // If the search term is empty, we don't want to debounce - we want to go straight back
            // to the empty search term state.
            searchTerm
                ?.takeIf(this::isSearchTermNotEmpty)
                ?.let { SEARCH_TERM_DEBOUNCE_PERIOD_MILLIS }
                ?: 0L
        }
        .flatMapLatest(this::loadSearchResults)
        .transformLatest {
            // If the progress layout is to be shown, we watch to delay the dispatch of this so that
            // the UI doesn't appear to flicker.
            if (it is UiState.InProgress) {
                delay(SEARCH_PROGRESS_DELAY_MILLIS)
            }

            emit(it)
        }
        .flowOn(defaultDispatcher)
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 1)

    /**
     * This [LiveData] emits the current [List] of [UiSearchResult]s. `null` will be emitted when
     * there are no items to show.
     */
    val searchResultsLiveData = uiStateFlow
        .map { if (it is UiState.Content) it.results else null }
        .distinctUntilChanged()
        .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits the current [UiState].
     */
    val uiStateLiveData = uiStateFlow.asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits when the stop details should be shown.
     */
    val showStopLiveData: LiveData<String> get() = showStop
    private val showStop = SingleLiveEvent<String>()

    /**
     * This is called when a search result item has been clicked.
     *
     * @param item The search result which has been clicked.
     */
    fun onItemClicked(item: UiSearchResult) {
        showStop.value = item.stopCode
    }

    /**
     * Given a search term, load the results.
     *
     * If the search term is `null` or less than [SEARCH_TERM_MIN_LENGTH] in length, the returned
     * [kotlinx.coroutines.flow.Flow] will only emit a [UiState.EmptySearchTerm] item.
     *
     * @param searchTerm The user's search term.
     * @return A [kotlinx.coroutines.flow.Flow] with the [UiState] of loading the search results.
     */
    private fun loadSearchResults(searchTerm: String?) = searchTerm
        ?.takeIf(this::isSearchTermNotEmpty)
        ?.let {
            busStopsRepository.getStopSearchResultsFlow(it)
                .map(this::mapToSearchResults)
                .onStart { emit(UiState.InProgress) }
        } ?: flowOf(UiState.EmptySearchTerm)

    /**
     * Is the search term considered not empty?
     *
     * @param searchTerm The search term.
     * @return `true` if the search term is considered not empty, otherwise `false`.
     */
    private fun isSearchTermNotEmpty(searchTerm: String) =
        searchTerm.length >= SEARCH_TERM_MIN_LENGTH

    /**
     * Given a [List] of [StopSearchResult]s, either map it to a [UiState.Content] if the [List]
     * is populated, otherwise a [UiState.NoResults] is returned.
     *
     * @param results The [List] of [StopSearchResult]s.
     * @return The input [List] either mapped to a [UiState.Content] if results exist, or a
     * [UiState.NoResults] if not.
     */
    private fun mapToSearchResults(results: List<StopSearchResult>?) = results
        ?.ifEmpty { null }
        ?.map(this::mapToSearchResult)
        ?.let { UiState.Content(it) }
        ?: UiState.NoResults

    /**
     * Given a [StopSearchResult], map it to a [UiSearchResult].
     *
     * @param result The result to map.
     * @return The mapped result.
     */
    private fun mapToSearchResult(result: StopSearchResult) =
        UiSearchResult(
            result.stopCode,
            result.stopName,
            result.orientation,
            result.serviceListing)
}