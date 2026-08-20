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

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForViewModelCoroutineScope
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import javax.inject.Inject

/**
 * The [ViewModel] for the search screen.
 *
 * @param state The state held for this [ViewModel].
 * @param uiContentRetriever Used to retrieve [UiContent] to be displayed.
 * @param defaultCoroutineDispatcher The default [CoroutineDispatcher].
 * @param viewModelCoroutineScope The [ViewModel] [CoroutineScope].
 * @author Niall Scott
 */
@HiltViewModel
internal class SearchViewModel @Inject constructor(
    private val state: State,
    private val uiContentRetriever: UiContentRetriever,
    @ForDefaultDispatcher defaultCoroutineDispatcher: CoroutineDispatcher,
    @ForViewModelCoroutineScope viewModelCoroutineScope: CoroutineScope
) : ViewModel(viewModelCoroutineScope) {

    /**
     * This emits the current [UiState].
     */
    val uiStateFlow: StateFlow<UiState> = _uiStateFlow
        .flowOn(defaultCoroutineDispatcher)
        .stateIn(
            scope = viewModelCoroutineScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState()
        )

    /**
     * The user's search term.
     */
    var searchTerm: String?
        get() = state.searchTerm
        set(value) {
            state.searchTerm = value
        }

    /**
     * This is called when a stop search result has been clicked.
     *
     * @param stopIdentifier The identifier of the clicked stop.
     */
    fun onItemClicked(stopIdentifier: StopIdentifier) {
        state.action = UiAction.ShowStopData(
            stopIdentifier = stopIdentifier
        )
    }

    /**
     * This is called when the add favourite stop item has been clicked in the dropdown menu.
     *
     * @param stopIdentifier The identifier of the stop to add a favourite stop for.
     */
    fun onAddFavouriteStopClicked(stopIdentifier: StopIdentifier) {
        state.action = UiAction.ShowAddFavouriteStop(
            stopIdentifier = stopIdentifier
        )
    }

    /**
     * This is called when the remove favourite stop item has been clicked in the dropdown menu.
     *
     * @param stopIdentifier The identifier of the stop to remove the favourite stop for.
     */
    fun onRemoveFavouriteStopClicked(stopIdentifier: StopIdentifier) {
        state.action = UiAction.ShowRemoveFavouriteStop(
            stopIdentifier = stopIdentifier
        )
    }

    /**
     * This is called when the add arrival alert item has been clicked in the dropdown menu.
     *
     * @param stopIdentifier The identifier of the stop to add an arrival alert for.
     */
    fun onAddArrivalAlertClicked(stopIdentifier: StopIdentifier) {
        state.action = UiAction.ShowAddArrivalAlert(
            stopIdentifier = stopIdentifier
        )
    }

    /**
     * This is called when the remove arrival alert item has been clicked in the dropdown menu.
     *
     * @param stopIdentifier The identifier of the stop to remove an arrival alert for.
     */
    fun onRemoveArrivalAlertClicked(stopIdentifier: StopIdentifier) {
        state.action = UiAction.ShowRemoveArrivalAlert(
            stopIdentifier = stopIdentifier
        )
    }

    /**
     * This is called when the add proximity alert item has been clicked in the dropdown menu.
     *
     * @param stopIdentifier The identifier of the stop to add a proximity alert for.
     */
    fun onAddProximityAlertClicked(stopIdentifier: StopIdentifier) {
        state.action = UiAction.ShowAddProximityAlert(
            stopIdentifier = stopIdentifier
        )
    }

    /**
     * This is called when the remove proximity alert item has been clicked in the dropdown menu.
     *
     * @param stopIdentifier The identifier of the stop to remove a proximity alert for.
     */
    fun onRemoveProximityAlertClicked(stopIdentifier: StopIdentifier) {
        state.action = UiAction.ShowRemoveProximityAlert(
            stopIdentifier = stopIdentifier
        )
    }

    /**
     * This is called when the map item has been clicked in the dropdown menu.
     *
     * @param stopIdentifier The identifier of the stop to show the map for.
     */
    fun onShowOnMapClicked(stopIdentifier: StopIdentifier) {
        state.action = UiAction.ShowOnMap(
            stopIdentifier = stopIdentifier
        )
    }

    /**
     * This is called when an action has been launched.
     */
    fun onActionLaunched() {
        state.action = null
    }

    private val _uiStateFlow get() = combine(
        uiContentRetriever.uiContentFlow,
        state.actionFlow,
        ::UiState
    )
}
