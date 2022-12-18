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

package uk.org.rivernile.android.bustracker.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopSearchResult
import uk.org.rivernile.android.bustracker.core.di.ForApplicationCoroutineScope
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import uk.org.rivernile.android.bustracker.core.search.SearchHistoryRepository
import uk.org.rivernile.android.bustracker.utils.SingleLiveEvent
import javax.inject.Inject

/**
 * This is the [ViewModel] for [SearchActivity].
 *
 * @param featureRepository Used to access the [FeatureRepository].
 * @param busStopsRepository Used to access the [BusStopsRepository].
 * @param searchHistoryRepository Used to store search history results.
 * @param defaultDispatcher The default [CoroutineDispatcher].
 * @param applicationCoroutineScope The application [CoroutineScope].
 * @author Niall Scott
 */
@HiltViewModel
class SearchActivityViewModel @Inject constructor(
        featureRepository: FeatureRepository,
        private val busStopsRepository: BusStopsRepository,
        private val searchHistoryRepository: SearchHistoryRepository,
        @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
        @ForApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope)
    : ViewModel() {

    companion object {

        private const val SEARCH_TERM_MIN_LENGTH = 3
    }

    /**
     * The current search term.
     */
    var searchTerm: String?
        get() = searchTermFlow.value
        set(value) {
            searchTermFlow.value = value
        }

    private val searchTermFlow = MutableStateFlow<String?>(null)

    /**
     * This [LiveData] emits the current visibility status of the scan menu item.
     */
    val isScanMenuItemVisibleLiveData: LiveData<Boolean> =
            MutableLiveData(featureRepository.hasCameraFeature)

    /**
     * This [LiveData] emits when the stop details should be shown.
     */
    val showStopLiveData: LiveData<String> get() = showStop
    private val showStop = SingleLiveEvent<String>()

    /**
     * This [LiveData] emits when the QR code scanner should be shown.
     */
    val showQrCodeScannerLiveData: LiveData<Unit> get() = showQrCodeScanner
    private val showQrCodeScanner = SingleLiveEvent<Unit>()

    /**
     * This [LiveData] emits when the install QR scanner dialog should be shown.
     */
    val showInstallQrScannerDialogLiveData: LiveData<Unit> get() = showInstallQrScannerDialog
    private val showInstallQrScannerDialog = SingleLiveEvent<Unit>()

    /**
     * This [LiveData] emits when the invalid QR code error should be shown.
     */
    val showInvalidQrCodeErrorLiveData: LiveData<Unit> get() = showInvalidQrCodeError
    private val showInvalidQrCodeError = SingleLiveEvent<Unit>()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val uiStateFlow = searchTermFlow
            .flatMapLatest(this::loadSearchResults)
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
     * This is called when the user submits a new search term.
     *
     * This is similar to setting the value of [searchTerm], except instead of just changing the
     * text, the user has explicitly submitted the term. In this case, the term should be persisted
     * in the search history for later presentation as a search suggestion.
     *
     * @param searchTerm The search term which has been submitted.
     */
    fun submitSearchTerm(searchTerm: String?) {
        this.searchTerm = searchTerm

        searchTerm?.takeIf { it.length >= SEARCH_TERM_MIN_LENGTH }?.let {
            applicationCoroutineScope.launch(defaultDispatcher) {
                searchHistoryRepository.addSearchTerm(it)
            }
        }
    }

    /**
     * This is called when a search result item has been clicked.
     *
     * @param item The search result which has been clicked.
     */
    fun onItemClicked(item: UiSearchResult) {
        showStop.value = item.stopCode
    }

    /**
     * This is called when the scan menu item has been clicked.
     */
    fun onScanMenuItemClicked() {
        showQrCodeScanner.call()
    }

    /**
     * This is called when the QR scanner application was not found.
     */
    fun onQrScannerNotFound() {
        showInstallQrScannerDialog.call()
    }

    /**
     * This is called when the QR code has been scanned, with the resulting stop code.
     *
     * @param result The result from scanning the QR code.
     */
    fun onQrScanned(result: ScanQrCodeResult) {
        if (result is ScanQrCodeResult.Success) {
            result.stopCode
                    ?.ifBlank { null }
                    ?.let {
                        showStop.value = it
                    }
                    ?: showInvalidQrCodeError.call()
        }
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
            ?.takeIf { it.length >= SEARCH_TERM_MIN_LENGTH }
            ?.let {
                busStopsRepository.getStopSearchResultsFlow(it)
                        .map(this::mapToSearchResults)
                        .onStart { emit(UiState.InProgress) }
            } ?: flowOf(UiState.EmptySearchTerm)

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