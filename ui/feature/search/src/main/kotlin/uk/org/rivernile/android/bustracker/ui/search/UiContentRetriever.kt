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

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.plus
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.busstops.StopSearchResult
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForViewModelCoroutineScope
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import javax.inject.Inject
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.time.Duration.Companion.milliseconds

/**
 * This is used to retrieve the [UiContent] state.
 *
 * @author Niall Scott
 */
internal interface UiContentRetriever {

    /**
     * A [Flow] which emits the current [UiContent] state.
     */
    val uiContentFlow: Flow<UiContent>
}

internal const val SEARCH_TERM_DEBOUNCE_PERIOD_MILLIS = 250L
internal const val SEARCH_PROGRESS_DELAY_MILLIS = 250L
private const val SEARCH_TERM_MIN_LENGTH = 3

internal class RealUiContentRetriever @Inject constructor(
    private val state: State,
    servicesRepository: ServicesRepository,
    private val busStopsRepository: BusStopsRepository,
    private val stopSearchResultDropdownMenuGenerator: UiStopSearchResultDropdownMenuGenerator,
    private val alphanumericComparator: Comparator<String>,
    @ForDefaultDispatcher defaultCoroutineDispatcher: CoroutineDispatcher,
    @ForViewModelCoroutineScope viewModelCoroutineScope: CoroutineScope
) : UiContentRetriever {

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override val uiContentFlow get() = state
        .searchTermFlow
        .debounce { searchTerm ->
            if (searchTerm.isSearchTermValid()) SEARCH_TERM_DEBOUNCE_PERIOD_MILLIS else 0L
        }
        .flatMapLatest(::getUiContentFlowWithSearchTerm)
        .transformLatest {
            // If the progress layout is to be shown, we watch to delay the dispatch of this so that
            // the UI doesn't appear to flicker.
            if (it is UiContent.InProgress) {
                delay(SEARCH_PROGRESS_DELAY_MILLIS.milliseconds)
            }

            emit(it)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getUiContentFlowWithSearchTerm(searchTerm: String?): Flow<UiContent> {
        return if (searchTerm.isSearchTermValid()) {
            busStopsRepository
                .getStopSearchResultsFlow(searchTerm.trim())
                .flatMapLatest(::getUiContentFlowWithSearchResults)
                .onStart { emit(UiContent.InProgress) }
        } else {
            flowOf(UiContent.EmptySearchTerm)
        }
    }

    private fun getUiContentFlowWithSearchResults(
        searchResults: List<StopSearchResult>?
    ): Flow<UiContent> {
        return if (!searchResults.isNullOrEmpty()) {
            val stopIdentifiers = searchResults.map { it.stopIdentifier }.toSet()

            combine(
                serviceColoursFlow,
                stopSearchResultDropdownMenuGenerator
                    .getDropdownMenuItemsForStopsFlow(stopIdentifiers)
            ) { serviceColours, dropdownMenus ->
                UiContent.Content(
                    results = searchResults
                        .toUiStopSearchResults(
                            serviceColours = serviceColours,
                            dropdownMenus = dropdownMenus,
                            stopNameComparator = alphanumericComparator,
                            serviceNameComparator = alphanumericComparator
                        )
                        .toImmutableList()
                )
            }
        } else {
            flowOf(UiContent.NoResults)
        }
    }

    private val serviceColoursFlow = servicesRepository
        .getColoursForServicesFlow()
        .shareIn(
            scope = viewModelCoroutineScope + defaultCoroutineDispatcher,
            started = SharingStarted.WhileSubscribed(5000L),
            replay = 1
        )

    @OptIn(ExperimentalContracts::class)
    private fun String?.isSearchTermValid(): Boolean {
        contract {
            returns(true) implies (this@isSearchTermValid != null)
        }

        return !isNullOrBlank() && length >= SEARCH_TERM_MIN_LENGTH
    }
}
