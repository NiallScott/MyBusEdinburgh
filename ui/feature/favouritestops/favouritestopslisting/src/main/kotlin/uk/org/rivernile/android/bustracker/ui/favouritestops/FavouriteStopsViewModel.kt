/*
 * Copyright (C) 2025 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.favouritestops

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForViewModelCoroutineScope
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.shortcuts.FavouriteStopShortcut
import uk.org.rivernile.android.bustracker.core.shortcuts.ShortcutsRepository
import javax.inject.Inject

/**
 * This is the [ViewModel] for the favourite stops screen.
 *
 * @param arguments The arguments the UI was invoked with.
 * @param state Where any transient state is held.
 * @param uiFavouriteStopsRetriever Used to retrieve [UiFavouriteStop]s.
 * @param shortcutsRepository Used to manage application shortcuts.
 * @param defaultCoroutineDispatcher The default [CoroutineDispatcher].
 * @param viewModelCoroutineScope The [CoroutineScope] that the view model should use.
 * @author Niall Scott
 */
@HiltViewModel
internal class FavouriteStopsViewModel @Inject constructor(
    private val arguments: Arguments,
    private val state: State,
    private val uiFavouriteStopsRetriever: UiFavouriteStopsRetriever,
    private val shortcutsRepository: ShortcutsRepository,
    @ForDefaultDispatcher defaultCoroutineDispatcher: CoroutineDispatcher,
    @ForViewModelCoroutineScope viewModelCoroutineScope: CoroutineScope
) : ViewModel(viewModelCoroutineScope) {

    /**
     * This emits [UiState] objects which reflects the state of the favourite stops screen.
     */
    val uiStateFlow = _uiStateFlow
        .flowOn(defaultCoroutineDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState()
        )

    /**
     * This is called when a favourite stop has been clicked.
     *
     * @param stopIdentifier The identifier of the clicked favourite stop.
     * @param savedName The name the stop is saved as.
     */
    fun onItemClicked(stopIdentifier: StopIdentifier, savedName: String) {
        if (arguments.isShortcutMode) {
            state.action = UiAction.AddShortcut(
                stopIdentifier = stopIdentifier,
                savedName = savedName
            )
        } else {
            state.action = UiAction.ShowStopData(stopIdentifier = stopIdentifier)
        }
    }

    /**
     * This is called when the dropdown has been clicked for a favourite stop.
     *
     * @param stopIdentifier The identifier of the favourite stop for which a dropdown should be
     * shown.
     */
    fun onItemOpenDropdownClicked(stopIdentifier: StopIdentifier) {
        if (!arguments.isShortcutMode) {
            state.selectedStopIdentifier = stopIdentifier
        }
    }

    /**
     * This is called when the dropdown menu has been dismissed.
     */
    fun onDropdownMenuDismissed() {
        if (!arguments.isShortcutMode) {
            dismissDropdownMenu()
        }
    }

    /**
     * This is called when the edit favourite dropdown item has been clicked.
     *
     * @param stopIdentifier The identifier of the stop which is to be edited.
     */
    fun onEditFavouriteNameClicked(stopIdentifier: StopIdentifier) {
        if (!arguments.isShortcutMode) {
            state.action = UiAction.ShowEditFavouriteStop(stopIdentifier = stopIdentifier)
            dismissDropdownMenu()
        }
    }

    /**
     * This is called when the remove favourite dropdown item has been clicked.
     *
     * @param stopIdentifier The identifier of the stop which is to removed.
     */
    fun onRemoveFavouriteClicked(stopIdentifier: StopIdentifier) {
        if (!arguments.isShortcutMode) {
            state.action = UiAction.ShowConfirmRemoveFavourite(stopIdentifier = stopIdentifier)
            dismissDropdownMenu()
        }
    }

    /**
     * This is called when the add shortcut favourite dropdown item has been clicked.
     *
     * @param stopIdentifier The stop identifier of the clicked favourite.
     * @param savedName The name the stop is saved as.
     */
    fun onAddShortcutClicked(stopIdentifier: StopIdentifier, savedName: String) {
        if (!arguments.isShortcutMode) {
            // Just to clear up the confusion: this method is called when we are not in shortcut
            // mode as this is the way to create shortcuts when we are not in shortcut mode.
            shortcutsRepository.pinFavouriteStopShortcut(
                shortcut = FavouriteStopShortcut(
                    stopIdentifier = stopIdentifier,
                    displayName = savedName
                )
            )
            dismissDropdownMenu()
        }
    }

    /**
     * This is called when the add arrival alert dropdown item has been clicked.
     *
     * @param stopIdentifier The identifier of the stop for which an arrival alert is to be added.
     */
    fun onAddArrivalAlertClicked(stopIdentifier: StopIdentifier) {
        if (!arguments.isShortcutMode) {
            state.action = UiAction.ShowAddArrivalAlert(stopIdentifier = stopIdentifier)
            dismissDropdownMenu()
        }
    }

    /**
     * This is called when the remove arrival alert dropdown item has been clicked.
     *
     * @param stopIdentifier The identifier of the stop for which an arrival alert is to be removed.
     */
    fun onRemoveArrivalAlertClicked(stopIdentifier: StopIdentifier) {
        if (!arguments.isShortcutMode) {
            state.action = UiAction.ShowConfirmRemoveArrivalAlert(stopIdentifier = stopIdentifier)
            dismissDropdownMenu()
        }
    }

    /**
     * This is called when the add proximity alert dropdown item has been clicked.
     *
     * @param stopIdentifier The identifier of the stop for which a proximity alert is to be added.
     */
    fun onAddProximityAlertClicked(stopIdentifier: StopIdentifier) {
        if (!arguments.isShortcutMode) {
            state.action = UiAction.ShowAddProximityAlert(stopIdentifier = stopIdentifier)
            dismissDropdownMenu()
        }
    }

    /**
     * This is called when the remove proximity alert dropdown item has been clicked.
     *
     * @param stopIdentifier The identifier of the stop for which a proximity alert is to be
     * removed.
     */
    fun onRemoveProximityAlertClicked(stopIdentifier: StopIdentifier) {
        if (!arguments.isShortcutMode) {
            state.action = UiAction.ShowConfirmRemoveProximityAlert(stopIdentifier = stopIdentifier)
            dismissDropdownMenu()
        }
    }

    /**
     * This is called when the 'Show on map' dropdown item has been clicked.
     *
     * @param stopIdentifier The stop identifier for which the map is to be shown.
     */
    fun onShowOnMapClicked(stopIdentifier: StopIdentifier) {
        if (!arguments.isShortcutMode) {
            state.action = UiAction.ShowOnMap(stopIdentifier = stopIdentifier)
            dismissDropdownMenu()
        }
    }

    /**
     * This is called when an action has been launched.
     */
    fun onActionLaunched() {
        state.action = null
    }

    private val uiContentFlow get() = uiFavouriteStopsRetriever
        .allFavouriteStopsFlow
        .map(::toUiContent)

    private val _uiStateFlow get() =
        combine(
            uiContentFlow,
            state.actionFlow,
            ::UiState
        )

    private fun toUiContent(
        favouriteStops: List<UiFavouriteStop>?
    ): UiContent {
        return if (favouriteStops?.isNotEmpty() == true) {
            UiContent.Content(
                favouriteStops = favouriteStops.toImmutableList()
            )
        } else {
            UiContent.Empty
        }
    }

    private fun dismissDropdownMenu() {
        state.selectedStopIdentifier = null
    }
}
