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

package uk.org.rivernile.android.bustracker.ui.addoreditfavouritestop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForApplicationCoroutineScope
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForViewModelCoroutineScope
import uk.org.rivernile.android.bustracker.core.favourites.FavouriteStop
import uk.org.rivernile.android.bustracker.core.favourites.FavouritesRepository
import javax.inject.Inject

/**
 * This is the [ViewModel] for [AddOrEditFavouriteStopDialogFragment].
 *
 * @param arguments The [Arguments] for this UI.
 * @param state Any [State] to be held for this UI.
 * @param uiContentFetcher Used to fetch the [UiContent] to show.
 * @param favouritesRepository Used to add or save changes to favourite stops.
 * @param defaultCoroutineDispatcher The default [CoroutineDispatcher].
 * @param applicationCoroutineScope The application [CoroutineScope].
 * @param viewModelCoroutineScope The [ViewModel] [CoroutineScope].
 * @author Niall Scott
 */
@HiltViewModel
internal class AddOrEditFavouriteStopViewModel @Inject constructor(
    private val arguments: Arguments,
    private val state: State,
    private val uiContentFetcher: UiContentFetcher,
    private val favouritesRepository: FavouritesRepository,
    @param:ForDefaultDispatcher private val defaultCoroutineDispatcher: CoroutineDispatcher,
    @param:ForApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
    @ForViewModelCoroutineScope viewModelCoroutineScope: CoroutineScope
) : ViewModel(viewModelCoroutineScope) {

    /**
     * A [kotlinx.coroutines.flow.Flow] which emits the current [UiState].
     */
    val uiStateFlow = _uiStateFlow
        .flowOn(defaultCoroutineDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState()
        )

    /**
     * The currently set stop name text, which is used as the saved name of the favourite stop.
     */
    var stopNameText: String?
        get() = state.stopNameText
        set(value) {
            state.stopNameText = value
        }

    /**
     * This is called when the positive button has been clicked.
     */
    fun onAddButtonClicked() {
        val stopNameText = stopNameText?.trim()

        if (isStopNameValid(stopNameText)) {
            addOrUpdateFavouriteStop(stopNameText)
        }
    }

    /**
     * This is called when the keyboard action button has been pressed.
     */
    fun onKeyboardActionButtonPressed() {
        val stopNameText = stopNameText?.trim()

        if (isStopNameValid(stopNameText)) {
            addOrUpdateFavouriteStop(stopNameText)
            state.action = UiAction.DismissDialog
        }
    }

    /**
     * This is called when the current [UiAction] has been launched.
     */
    fun onActionLaunched() {
        state.action = null
    }

    private val _uiStateFlow get() = combine(
        uiContentFetcher.uiContentFlow,
        state.actionFlow,
        ::UiState
    )

    private fun addOrUpdateFavouriteStop(stopNameText: String) {
        val stopCode = arguments.stopCode?.ifBlank { null } ?: return

        val favouriteStop = FavouriteStop(
            stopCode = stopCode,
            stopName = stopNameText
        )

        applicationCoroutineScope.launch(defaultCoroutineDispatcher) {
            // This is launched in application scope as the Dialog is dismissed right away when the
            // user clicks the 'positive' button. If we were to use the ViewModel scope, the task
            // would be immediately cancelled. Besides, once the Dialog is dismissed, the user
            // doesn't have the opportunity to cancel the operation anyway.
            favouritesRepository.addOrUpdateFavouriteStop(favouriteStop = favouriteStop)
        }
    }
}
