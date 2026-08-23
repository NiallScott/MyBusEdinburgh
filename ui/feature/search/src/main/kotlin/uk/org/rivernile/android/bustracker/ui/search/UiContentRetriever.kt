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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import javax.inject.Inject
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

internal class RealUiContentRetriever @Inject constructor(
    private val stopSearchResultRetriever: StopSearchResultRetriever,
    private val servicesRepository: ServicesRepository,
    private val stopSearchResultDropdownMenuGenerator: UiStopSearchResultDropdownMenuGenerator,
    private val alphanumericComparator: Comparator<String>
) : UiContentRetriever {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val uiContentFlow get() = combine(
        stopSearchResultsWithDropdownMenusFlow,
        servicesRepository.getColoursForServicesFlow()
    ) { stopSearchResultStateWithDropdownMenus, serviceColours ->
        createUiContent(
            stopSearchResultState = stopSearchResultStateWithDropdownMenus.stopSearchResultState,
            dropdownMenus = stopSearchResultStateWithDropdownMenus.dropdownMenus,
            serviceColours = serviceColours
        )
    }.transformLatest {
        // If the progress layout is to be shown, we watch to delay the dispatch of this so that
        // the UI doesn't appear to flicker.
        if (it is UiContent.InProgress) {
            delay(SEARCH_PROGRESS_DELAY_MILLIS.milliseconds)
        }

        emit(it)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val stopSearchResultsWithDropdownMenusFlow get() = stopSearchResultRetriever
        .stopSearchResultStateFlow
        .flatMapLatest(::getStopSearchResultStateWithDropdownMenusFlow)

    private fun createUiContent(
        stopSearchResultState: StopSearchResultState,
        dropdownMenus: Map<StopIdentifier, UiStopSearchResultDropdownMenu>?,
        serviceColours: Map<ServiceDescriptor, ServiceColours>?
    ): UiContent {
        return when (stopSearchResultState) {
            is StopSearchResultState.EmptySearchTerm -> UiContent.EmptySearchTerm
            is StopSearchResultState.InProgress -> UiContent.InProgress
            is StopSearchResultState.Results -> {
                val results = stopSearchResultState
                    .results
                    ?.toUiStopSearchResults(
                        serviceColours = serviceColours,
                        dropdownMenus = dropdownMenus,
                        stopNameComparator = alphanumericComparator,
                        serviceNameComparator = alphanumericComparator
                    )
                    ?.toImmutableList()

                if (!results.isNullOrEmpty()) {
                    UiContent.Content(
                        results = results
                    )
                } else {
                    UiContent.NoResults
                }
            }
        }
    }

    private fun getStopSearchResultStateWithDropdownMenusFlow(
        stopSearchResultState: StopSearchResultState
    ): Flow<StopSearchResultStateWithDropdownMenus> {
        return if (stopSearchResultState is StopSearchResultState.Results) {
            val stopIdentifiers = stopSearchResultState
                .results
                ?.map { it.stopIdentifier }
                ?.toSet()

            if (!stopIdentifiers.isNullOrEmpty()) {
                stopSearchResultDropdownMenuGenerator
                    .getDropdownMenuItemsForStopsFlow(stopIdentifiers)
                    .map {
                        StopSearchResultStateWithDropdownMenus(
                            stopSearchResultState = stopSearchResultState,
                            dropdownMenus = it
                        )
                    }
            } else {
                flowOf(
                    StopSearchResultStateWithDropdownMenus(
                        stopSearchResultState = stopSearchResultState
                    )
                )
            }
        } else {
            flowOf(
                StopSearchResultStateWithDropdownMenus(
                    stopSearchResultState = stopSearchResultState
                )
            )
        }
    }

    private data class StopSearchResultStateWithDropdownMenus(
        val stopSearchResultState: StopSearchResultState,
        val dropdownMenus: Map<StopIdentifier, UiStopSearchResultDropdownMenu>? = null
    )
}
