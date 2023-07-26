/*
 * Copyright (C) 2021 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.favourites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.favourites.FavouriteStop
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import uk.org.rivernile.android.bustracker.utils.SingleLiveEvent
import javax.inject.Inject

/**
 * This is the [ViewModel] for [FavouriteStopsFragment].
 *
 * @param savedState This is used to access the [SavedStateHandle], to store state over instances.
 * @param favouriteStopsRetriever Used to retrieve favourite stops.
 * @param alertsRepository Used to determine if stops have alerts set against them.
 * @param featureRepository Used to determine what features are available.
 * @param defaultDispatcher The default [CoroutineDispatcher].
 * @author Niall Scott
 */
@HiltViewModel
class FavouriteStopsFragmentViewModel @Inject constructor(
        private val savedState: SavedStateHandle,
        favouriteStopsRetriever: FavouriteStopsRetriever,
        private val alertsRepository: AlertsRepository,
        featureRepository: FeatureRepository,
        @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher) : ViewModel() {

    companion object {

        private const val STATE_SELECTED_STOP_CODE = "selectedStopCode"
    }

    private val selectedStopCodeFlow =
            savedState.getStateFlow<String?>(STATE_SELECTED_STOP_CODE, null)
    private var selectedStopCode
        get() = selectedStopCodeFlow.value?.ifEmpty { null }
        set(value) {
            savedState[STATE_SELECTED_STOP_CODE] = value
        }

    /**
     * Are we in the create shortcut mode? If we are, then the user selecting a favourite stop
     * causes a shortcut to be created (normally it shows live times). Also, when in the create
     * shortcut mode, we don't allow the context menu to be shown for a favourite stop.
     */
    var isCreateShortcutMode = false

    private val favouritesFlow = favouriteStopsRetriever
        .allFavouriteStopsFlow
        .combine(selectedStopCodeFlow, this::applySelectedState)
        .flowOn(defaultDispatcher)
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 1)

    /**
     * This [LiveData] emits the current [List] of [UiFavouriteStop]s, for display on the UI.
     */
    val favouritesLiveData = favouritesFlow.asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits the current [UiState].
     */
    val uiStateLiveData = favouritesFlow
            .map(this::calculateUiState)
            .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits the current visibility state of the context menu.
     */
    val showContextMenuLiveData = selectedStopCodeFlow.map {
        !isCreateShortcutMode && it?.ifEmpty { null } != null
    }.distinctUntilChanged().asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits the name data for the currently selected stop.
     */
    val selectedStopNameLiveData = selectedStopCodeFlow
            .combine(favouritesFlow, this::getStopName)
            .distinctUntilChanged()
            .asLiveData(viewModelScope.coroutineContext + defaultDispatcher)

    /**
     * This [LiveData] emits the visibility state of UI allowing the user to show the selected stop
     * on a map.
     */
    val isStopMapVisibleLiveData: LiveData<Boolean> =
            MutableLiveData(featureRepository.hasStopMapUiFeature)
    /**
     * This [LiveData] emits the visibility state of UI allowing the user to see or manipulate
     * arrival alerts.
     */
    val isArrivalAlertVisibleLiveData: LiveData<Boolean> =
            MutableLiveData(featureRepository.hasArrivalAlertFeature)
    /**
     * This [LiveData] emits the visibility state of UI allowing the user to see or manipulate
     * proximity alerts.
     */
    val isProximityAlertVisibleLiveData: LiveData<Boolean> =
            MutableLiveData(featureRepository.hasProximityAlertFeature)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val hasArrivalAlertFlow = selectedStopCodeFlow
            .flatMapLatest(this::loadHasArrivalAlert)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val hasProximityAlertFlow = selectedStopCodeFlow
            .flatMapLatest(this::loadHasProximityAlert)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    /**
     * This [LiveData] emits the enabled state of UI allowing the user to see or manipulate arrival
     * alerts.
     */
    val isArrivalAlertEnabledLiveData = hasArrivalAlertFlow.map { it != null }
            .distinctUntilChanged()
            .asLiveData(viewModelScope.coroutineContext)
    /**
     * This [LiveData] emits the enabled state of UI allowing the user to see or manipulate
     * proximity alerts.
     */
    val isProximityAlertEnabledLiveData = hasProximityAlertFlow.map { it != null }
            .distinctUntilChanged()
            .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits whether there is an arrival alert set on the selected stop or not.
     */
    val hasArrivalAlertLiveData = hasArrivalAlertFlow.map { it ?: false }
            .distinctUntilChanged()
            .asLiveData(viewModelScope.coroutineContext)
    /**
     * This [LiveData] emits whether there is a proximity alert set on the selected stop or not.
     */
    val hasProximityAlertLiveData = hasProximityAlertFlow.map { it ?: false }
            .distinctUntilChanged()
            .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits a stop code whenever stop data should be shown (e.g. live times). This
     * will not emit when [isCreateShortcutMode] is `true`.
     */
    val showStopDataLiveData: LiveData<String> get() = showStopData
    private val showStopData = SingleLiveEvent<String>()

    /**
     * This [LiveData] emits a [FavouriteStop] whenever a shortcut should be created for a stop.
     * This will not emit when [isCreateShortcutMode] is `false`.
     */
    val createShortcutLiveData: LiveData<FavouriteStop> get() = createShortcut
    private val createShortcut = SingleLiveEvent<FavouriteStop>()

    /**
     * This [LiveData] emits a stop code whenever a favourite stop should be edited.
     */
    val showEditFavouriteStopLiveData: LiveData<String> get() = showEditFavouriteStop
    private val showEditFavouriteStop = SingleLiveEvent<String>()

    /**
     * This [LiveData] emits a stop code whenever the deletion of a favourite stop should be
     * confirmed with the user.
     */
    val showConfirmDeleteFavouriteLiveData: LiveData<String> get() = showConfirmDeleteFavourite
    private val showConfirmDeleteFavourite = SingleLiveEvent<String>()

    /**
     * This [LiveData] emits a stop code whenever the stop should be shown on a map.
     */
    val showOnMapLiveData: LiveData<String> get() = showOnMap
    private val showOnMap = SingleLiveEvent<String>()

    /**
     * This [LiveData] emits a stop code whenever UI should be presented to allow the user to add
     * an arrival alert.
     */
    val showAddArrivalAlertLiveData: LiveData<String> get() = showAddArrivalAlert
    private val showAddArrivalAlert = SingleLiveEvent<String>()

    /**
     * This [LiveData] emits a stop code whenever UI should be presented to allow the user to
     * confirm that they wish to remove an arrival alert.
     */
    val showConfirmDeleteArrivalAlertLiveData: LiveData<String> get() =
        showConfirmDeleteArrivalAlert
    private val showConfirmDeleteArrivalAlert = SingleLiveEvent<String>()

    /**
     * This [LiveData] emits a stop code whenever UI should be presented to allow the user to add
     * a proximity alert.
     */
    val showAddProximityAlertLiveData: LiveData<String> get() = showAddProximityAlert
    private val showAddProximityAlert = SingleLiveEvent<String>()

    /**
     * This [LiveData] emits a stop code whenever UI should be presented to allow the user to
     * confirm that they wish to remove a proximity alert.
     */
    val showConfirmDeleteProximityAlertLiveData: LiveData<String> get() =
        showConfirmDeleteProximityAlert
    private val showConfirmDeleteProximityAlert = SingleLiveEvent<String>()

    /**
     * This is called when the user has clicked on a favourite stop.
     *
     * If [isCreateShortcutMode] is `true`, it will cause a shortcut to be created. Otherwise, it
     * will show stop data.
     *
     * @param favourite The favourite item which has been clicked.
     */
    fun onFavouriteStopClicked(favourite: FavouriteStop) {
        if (isCreateShortcutMode) {
            createShortcut.value = favourite
        } else {
            if (selectedStopCode == null) {
                showStopData.value = favourite.stopCode
            }
        }
    }

    /**
     * This is called when the user has long clicked on a favourite stop.
     *
     * When [isCreateShortcutMode] is `false`, this will cause a context menu to be requested for
     * the selected stop code. Otherwise, nothing will happen.
     *
     * @param stopCode The stop which was long clicked.
     * @return `true` if we've consumed the event and requested that the context menu be shown.
     * Otherwise, `false`.
     */
    fun onFavouriteStopLongClicked(stopCode: String): Boolean {
        return if (!isCreateShortcutMode && stopCode.isNotEmpty()) {
            selectedStopCode = stopCode

            true
        } else {
            false
        }
    }

    /**
     * This is called from the UI when the context menu for the selected stop is closed, even when
     * we didn't request its closure (e.g. user back button press).
     */
    fun onFavouriteStopUnselected() {
        closeContextMenu()
    }

    /**
     * This is called when the edit button is clicked. If there is a selected stop, it causes the
     * edit UI to be requested.
     *
     * @return `true` if we handled the event. That is, there is a selected stop. Otherwise `false`.
     */
    fun onEditFavouriteClicked() = selectedStopCode?.let {
        showEditFavouriteStop.value = it
        closeContextMenu()
        true
    } ?: false

    /**
     * This is called when the delete button is clicked. If there is a selected stop, it causes the
     * delete confirmation UI to be requested.
     *
     * @return `true` if we handled the event. That is, there is a selected stop. Otherwise `false`.
     */
    fun onDeleteFavouriteClicked() = selectedStopCode?.let {
        showConfirmDeleteFavourite.value = it
        closeContextMenu()
        true
    } ?: false

    /**
     * This is called when the show on map button is clicked. If there is a selected stop, it
     * requests that the favourite stop is shown on the map.
     *
     * @return `true` if we handled the event. That is, there is a selected stop. Otherwise `false`.
     */
    fun onShowOnMapClicked() = selectedStopCode?.let {
        showOnMap.value = it
        closeContextMenu()
        true
    } ?: false

    /**
     * This is called when the proximity alert button has been clicked. If no stop is selected,
     * calling this method does nothing.
     *
     * If there is an active proximity alert for the selected stop, this method will request UI
     * asking the user to confirm the removal of the proximity alert.
     *
     * If there is not an active proximity alert for the selected stop, this method will request UI
     * which allows the user to add a new proximity alert.
     *
     * @return `true` if we handled the event. That is, there is a selected stop. Otherwise `false`.
     */
    fun onProximityAlertClicked() = selectedStopCode?.let {
        when (hasProximityAlertFlow.value) {
            true -> showConfirmDeleteProximityAlert.value = it
            false -> showAddProximityAlert.value = it
            else -> { }
        }

        closeContextMenu()
        true
    } ?: false

    /**
     * This is called when the arrival alert button has been clicked. If no stop is selected,
     * calling this method does nothing.
     *
     * If there is an active arrival alert for the selected stop, this method will request UI
     * asking the user to confirm the removal of the arrival alert.
     *
     * If there is not an active arrival alert for the selected stop, this method will request UI
     * which allows the user to add a new arrival alert.
     *
     * @return `true` if we handled the event. That is, there is a selected stop. Otherwise `false`.
     */
    fun onArrivalAlertClicked() = selectedStopCode?.let {
        when (hasArrivalAlertFlow.value) {
            true -> showConfirmDeleteArrivalAlert.value = it
            false -> showAddArrivalAlert.value = it
            else -> { }
        }

        closeContextMenu()
        true
    } ?: false

    /**
     * Given a [List] of [UiFavouriteStop]s, apply the correct selected state to all items in the
     * [List] based upon the [selectedStopCode] value.
     *
     * @param favouriteStops The [List] of favourite stops.
     * @param selectedStopCode The currently selected stop code.
     * @return [favouriteStops] mapped with the correct selected state for all items.
     */
    private fun applySelectedState(
            favouriteStops: List<UiFavouriteStop>?,
            selectedStopCode: String?): List<UiFavouriteStop>? {
        return favouriteStops?.map {
            it.copy(isSelected = it.favouriteStop.stopCode == selectedStopCode)
        }
    }

    /**
     * Based on the loaded [List] of [UiFavouriteStop]s, calculate the high-level state of the UI.
     *
     * @param favouriteStops The currently loaded [List] of [UiFavouriteStop]s.
     * @return The calculated [UiState].
     */
    private fun calculateUiState(favouriteStops: List<UiFavouriteStop>?) = when {
        favouriteStops == null -> UiState.PROGRESS
        favouriteStops.isEmpty() -> UiState.ERROR
        else -> UiState.CONTENT
    }

    /**
     * Load whether the given [stopCode] has an arrival alert set against it or not. If the
     * [stopCode] is `null` or empty, the returned [kotlinx.coroutines.flow.Flow] emits `null`.
     * `null` will also be emitted in lieu of a value while the status is loading.
     *
     * @param stopCode The stop code to get the arrival alert status for.
     * @return A [kotlinx.coroutines.flow.Flow] which emits whether the given stop code has an
     * arrival alert set against it or not.
     */
    private fun loadHasArrivalAlert(stopCode: String?) = stopCode?.ifEmpty { null }?.let {
        alertsRepository.hasArrivalAlertFlow(it)
                .flowOn(defaultDispatcher)
                .onStart<Boolean?> { emit(null) }
    } ?: flowOf(null)

    /**
     * Load whether the given [stopCode] has a proximity alert set against it or not. If the
     * [stopCode] is `null` or empty, the returned [kotlinx.coroutines.flow.Flow] emits `null`.
     * `null` will also be emitted in lieu of a value while the status is loading.
     *
     * @param stopCode The stop code to get the proximity alert status for.
     * @return A [kotlinx.coroutines.flow.Flow] which emits whether the given stop code has a
     * proximity alert set against it or not.
     */
    private fun loadHasProximityAlert(stopCode: String?) = stopCode?.ifEmpty { null }?.let {
        alertsRepository.hasProximityAlertFlow(it)
                .flowOn(defaultDispatcher)
                .onStart<Boolean?> { emit(null) }
    }?: flowOf(null)

    /**
     * Given a [stopCode] and a [List] of [UiFavouriteStop]s, get the currently set name for the
     * favourite identified by the stop code. If this isn't found, `null` will be returned.
     *
     * @param stopCode The stop code to search for. If this is `null` or empty, this method will
     * return `null`.
     * @param favourites The current [List] of [UiFavouriteStop]s to search within.
     */
    private fun getStopName(stopCode: String?, favourites: List<UiFavouriteStop>?):
            UiFavouriteName? {
        return stopCode?.ifEmpty { null }?.let { sc ->
            favourites?.firstOrNull { it.favouriteStop.stopCode == sc }
                ?.let {
                    UiFavouriteName(
                        sc,
                        FavouriteStopName(
                            it.favouriteStop.stopName,
                            null)
                    )
            }
        }
    }

    /**
     * Close the context menu by scrubbing out the currently set selected stop code.
     */
    private fun closeContextMenu() {
        selectedStopCode = null
    }
}