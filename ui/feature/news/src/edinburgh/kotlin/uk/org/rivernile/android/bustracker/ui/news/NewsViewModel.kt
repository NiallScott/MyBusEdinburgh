/*
 * Copyright (C) 2024 - 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForViewModelCoroutineScope
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.IncidentsViewModelState
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.UiIncident
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.UiIncidentAction
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.UiIncidentsState
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.UiContent
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.UiContentFetcher
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.UiServiceUpdate
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions.DiversionsViewModelState
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions.UiDiversion
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions.UiDiversionAction
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions.UiDiversionsState
import javax.inject.Inject

/**
 * The News [ViewModel].
 *
 * @param incidentsViewModelState Used to hold state for incidents.
 * @param diversionsViewModelState Used to hold state for diversions.
 * @param contentFetcher Used to fetch the content.
 * @param defaultCoroutineDispatcher The default [CoroutineDispatcher] to run coroutines on.
 * @param viewModelCoroutineScope The [CoroutineScope] to launch coroutines on.
 * @author Niall Scott
 */
@HiltViewModel
internal class NewsViewModel @Inject constructor(
    private val incidentsViewModelState: IncidentsViewModelState,
    private val diversionsViewModelState: DiversionsViewModelState,
    private val contentFetcher: UiContentFetcher,
    @ForDefaultDispatcher defaultCoroutineDispatcher: CoroutineDispatcher,
    @ForViewModelCoroutineScope viewModelCoroutineScope: CoroutineScope
) : ViewModel(viewModelCoroutineScope, contentFetcher) {

    /**
     * A [kotlinx.coroutines.flow.Flow] which emits the latest [UiState].
     */
    val uiStateFlow = uiIncidentsState
        .combine(uiDiversionsState, this::createUiState)
        .flowOn(defaultCoroutineDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState()
        )

    /**
     * Reload data from their data sources.
     */
    fun onRefresh() {
        contentFetcher.refresh()
    }

    /**
     * This is called when the "More details" item has been clicked on an incident item.
     *
     * @param item The incident item on which the "More details" item was clicked.
     */
    fun onIncidentMoreDetailsClicked(item: UiIncident) {
        item.moreDetails?.let {
            incidentsViewModelState.action = UiIncidentAction.ShowUrl(it.url)
        }
    }

    /**
     * This is called when the "More details" item has been clicked on a diversion item.
     *
     * @param item The diversion item on which the "More details" item was clicked.
     */
    fun onDiversionMoreDetailsClicked(item: UiDiversion) {
        item.moreDetails?.let {
            diversionsViewModelState.action = UiDiversionAction.ShowUrl(it.url)
        }
    }

    /**
     * This is called when an [UiIncidentAction] has been launched.
     */
    fun onIncidentActionLaunched() {
        incidentsViewModelState.action = null
    }

    /**
     * This is called when an [UiDiversionAction] has been launched.
     */
    fun onDiversionActionLaunched() {
        diversionsViewModelState.action = null
    }

    private val uiIncidentsState get() = contentFetcher
        .incidentsContentFlow
        .combine(incidentsViewModelState.actionFlow, ::UiIncidentsState)

    private val uiDiversionsState get() = contentFetcher
        .diversionsContentFlow
        .combine(diversionsViewModelState.actionFlow, ::UiDiversionsState)

    /**
     * Given [UiIncidentsState] and [UiDiversionsState], create a new [UiState] object.
     *
     * @param incidentsState The incidents state.
     * @param diversionsState The diversions state.
     * @return The resulting [UiState].
     */
    private fun createUiState(
        incidentsState: UiIncidentsState,
        diversionsState: UiDiversionsState
    ): UiState {
        val isRefreshing = incidentsState.content.isRefreshing ||
                diversionsState.content.isRefreshing
        val incidentCount = (incidentsState.content as? UiContent.Populated)?.size
        val diversionCount = (diversionsState.content as? UiContent.Populated)?.size

        return UiState(
            incidentsState = incidentsState,
            diversionsState = diversionsState,
            actionButtons = UiActionButtons(
                refresh = UiActionButton.Refresh(
                    isEnabled = !isRefreshing,
                    isRefreshing = isRefreshing
                )
            ),
            tabBadges = UiTabBadges(
                incidentsCount = incidentCount,
                diversionsCount = diversionCount
            )
        )
    }

    private val UiContent.Populated<UiServiceUpdate>.size get() = items.size
}