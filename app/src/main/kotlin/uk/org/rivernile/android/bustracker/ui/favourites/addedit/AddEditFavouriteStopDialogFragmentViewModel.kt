/*
 * Copyright (C) 2021 - 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.favourites.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.org.rivernile.android.bustracker.core.database.settings.entities.FavouriteStop
import uk.org.rivernile.android.bustracker.core.di.ForApplicationCoroutineScope
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.favourites.FavouritesRepository
import uk.org.rivernile.android.bustracker.core.text.TextFormattingUtils
import uk.org.rivernile.android.bustracker.utils.Event
import javax.inject.Inject

/**
 * This is the [ViewModel] for [AddEditFavouriteStopDialogFragment].
 *
 * @param savedState Used to access the Android saved state data.
 * @param favouritesRepository Used to add and edit favourite stops.
 * @param fetcher Used to fetch the favourite stop data for display on the UI.
 * @param textFormattingUtils Used to format the stop names when pre-populating the editable name.
 * @param defaultDispatcher The default [CoroutineDispatcher].
 * @param applicationCoroutineScope The application [CoroutineScope].
 * @author Niall Scott
 */
@HiltViewModel
class AddEditFavouriteStopDialogFragmentViewModel @Inject constructor(
        private val savedState: SavedStateHandle,
        private val favouritesRepository: FavouritesRepository,
        private val fetcher: FavouriteStopFetcher,
        private val textFormattingUtils: TextFormattingUtils,
        @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
        @ForApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope)
    : ViewModel() {

    companion object {

        private const val STATE_PRE_POPULATED_STOP_CODE = "prePopulatedStopCode"
    }

    /**
     * This property is used to get and set the stop code for this favourite.
     */
    var stopCode: String?
        get() = stopCodeFlow.value
        set(value) {
            stopCodeFlow.value = value
        }

    private val stopCodeFlow = MutableStateFlow<String?>(null)

    private val stopNameFlow = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val uiStateFlow = stopCodeFlow
            .flatMapLatest(fetcher::loadFavouriteStopAndDetails)
            .flowOn(defaultDispatcher)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), UiState.InProgress)

    /**
     * Emits the current [UiState].
     */
    val uiStateLiveData = uiStateFlow.asLiveData(viewModelScope.coroutineContext)

    /**
     * Should the Dialog's positive button be enabled or not?
     */
    val isPositiveButtonEnabledLiveData = stopNameFlow.map { it?.isNotEmpty() == true }
            .asLiveData(viewModelScope.coroutineContext)

    /**
     * Emits an [Event] containing the [String] to pre-populate the editable stop name field with.
     * This is an [Event] because it should only be consumed once. The platform deals with the
     * saving of state of the current editable name contents.
     */
    val prePopulateNameLiveData = uiStateFlow
            .filterIsInstance<UiState.Mode>()
            .filter(this::shouldPrePopulateName)
            .map(this::getPrePopulatedName)
            .asLiveData(viewModelScope.coroutineContext)

    /**
     * Update the internal state of what the current contents of the editable stop name field are.
     *
     * @param stopName The current contents of the stop name field.
     */
    fun updateStopName(stopName: String?) {
        stopNameFlow.value = stopName
    }

    /**
     * The submit button has been clicked.
     */
    fun onSubmitClicked() {
        when (val uiState = uiStateFlow.value) {
            is UiState.Mode.Add -> addFavouriteStop(uiState)
            is UiState.Mode.Edit -> editFavouriteStop(uiState)
            else -> { /* Nothing to do - removes warning. */ }
        }
    }

    /**
     * Should the stop name field be pre-populated?
     *
     * @return `true` if the stop name field should be pre-populated, otherwise `false`.
     */
    private fun shouldPrePopulateName(state: UiState.Mode) =
            state.stopCode != savedState[STATE_PRE_POPULATED_STOP_CODE]

    /**
     * Get the stop name to use for the purpose of pre-populating the stop name field.
     *
     * @param state The current [UiState.Mode]. The type of this determines where the pre-populated
     * name should come from, and what its contents should be.
     */
    private fun getPrePopulatedName(state: UiState.Mode): Event<String> {
        savedState[STATE_PRE_POPULATED_STOP_CODE] = state.stopCode

        return when (state) {
            is UiState.Mode.Add -> state.stopName?.let(textFormattingUtils::formatBusStopName)
                    ?: state.stopCode
            is UiState.Mode.Edit -> state.favouriteStop.stopName
        }.let(::Event)
    }

    /**
     * Given an existing [mode], add the stop represented as a favourite, using the name state
     * tracked inside this instance as its user-supplied name.
     *
     * @param mode The [UiState.Mode.Add] mode.
     */
    private fun addFavouriteStop(mode: UiState.Mode.Add) {
        stopNameFlow.value?.ifEmpty { null }?.let { stopName ->
            val favouriteStop = FavouriteStop(
                    stopCode = mode.stopCode,
                    stopName = stopName)

            applicationCoroutineScope.launch(defaultDispatcher) {
                // This is launched in application scope as the Dialog is dismissed right away when
                // the user clicks the 'positive' button. If we were to use the ViewModel scope, the
                // task would be immediately cancelled. Besides, once the Dialog is dismissed, the
                // user doesn't have the opportunity to cancel the operation anyway.
                favouritesRepository.addFavouriteStop(favouriteStop)
            }
        }
    }

    /**
     * Given an existing [mode], update the name of stop represented with the name state tracked
     * inside this instance.
     *
     * @param mode The [UiState.Mode.Edit] mode.
     */
    private fun editFavouriteStop(mode: UiState.Mode.Edit) {
        stopNameFlow.value?.ifEmpty { null }?.let { stopName ->
            val favouriteStop = mode.favouriteStop.copy(stopName = stopName)

            applicationCoroutineScope.launch(defaultDispatcher) {
                // This is launched in application scope as the Dialog is dismissed right away when
                // the user clicks the 'positive' button. If we were to use the ViewModel scope, the
                // task would be immediately cancelled. Besides, once the Dialog is dismissed, the
                // user doesn't have the opportunity to cancel the operation anyway.
                favouritesRepository.updateFavouriteStop(favouriteStop)
            }
        }
    }
}