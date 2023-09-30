/*
 * Copyright (C) 2022 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.neareststops

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceRepository
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import uk.org.rivernile.android.bustracker.utils.Event
import uk.org.rivernile.android.bustracker.utils.SingleLiveEvent
import javax.inject.Inject

/**
 * This [ViewModel] is used by [NearestStopsFragment].
 *
 * @param savedState The saved previous instance state.
 * @param servicesRepository The services repository.
 * @param busStopsRepository The bus stops repository.
 * @param favouritesStateRetriever Used to retrieve favourite stop data.
 * @param alertsStateRetriever Used to retrieve alert data.
 * @param featureRepository The feature repository.
 * @param locationRepository The location repository.
 * @param preferenceRepository The preferences repository.
 * @param uiStateRetriever Used to retrieve UI state.
 * @param defaultDispatcher The default [CoroutineDispatcher].
 * @author Niall Scott
 */
@HiltViewModel
class NearestStopsFragmentViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    servicesRepository: ServicesRepository,
    private val busStopsRepository: BusStopsRepository,
    favouritesStateRetriever: FavouritesStateRetriever,
    alertsStateRetriever: AlertsStateRetriever,
    featureRepository: FeatureRepository,
    private val locationRepository: LocationRepository,
    private val preferenceRepository: PreferenceRepository,
    uiStateRetriever: UiStateRetriever,
    @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher) : ViewModel() {

    companion object {

        private const val STATE_ASKED_TURN_ON_GPS = "askedTurnOnGps"
        private const val STATE_ASKED_FOR_PERMISSIONS = "askedForPermissions"
        private const val STATE_SELECTED_STOP_CODE = "selectedStopCode"
        private const val STATE_SELECTED_SERVICES = "selectedServices"

        private const val LIVE_DATA_TIMEOUT = 1000L
    }

    /**
     * The current state of permissions pertaining to this view.
     */
    var permissionsState: PermissionsState
        get() = permissionsStateFlow.value ?: PermissionsState()
        set(value) {
            permissionsStateFlow.value = value
            handlePermissionsSet(value)
        }

    /**
     * This property is used to get and set the current selected services.
     */
    var selectedServices: List<String>?
        get() = selectedServicesFlow.value?.ifEmpty { null }?.toList()
        set(value) {
            savedState[STATE_SELECTED_SERVICES] = value?.ifEmpty { null }?.toTypedArray()
        }

    private val selectedServicesFlow =
        savedState.getStateFlow<Array<String>?>(STATE_SELECTED_SERVICES, null)

    private val permissionsStateFlow = MutableStateFlow<PermissionsState?>(null)

    private val selectedStopCodeFlow =
            savedState.getStateFlow<String?>(STATE_SELECTED_STOP_CODE, null)
    private var selectedStopCode
        get() = selectedStopCodeFlow.value?.ifEmpty { null }
        set(value) {
            savedState[STATE_SELECTED_STOP_CODE] = value
        }

    /**
     * This [LiveData] emits the user's selected services.
     */
    private val selectedServicesLiveData = selectedServicesFlow
            .map { it?.ifEmpty { null }?.asList() }
            .asLiveData(viewModelScope.coroutineContext)

    private val allServiceNamesFlow = servicesRepository.allServiceNamesFlow
            .flowOn(defaultDispatcher)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    private val uiStateFlow = uiStateRetriever.getUiStateFlow(
            permissionsStateFlow.filterNotNull(),
            selectedServicesLiveData.asFlow())
            .distinctUntilChanged()
            .flowOn(defaultDispatcher)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), UiState.InProgress)

    /**
     * This [LiveData] emits the [UiNearestStop] items to display on the UI.
     */
    val itemsLiveData = uiStateFlow
            .combine(selectedStopCodeFlow, this::mapToItems)
            .distinctUntilChanged()
            .asLiveData(viewModelScope.coroutineContext, LIVE_DATA_TIMEOUT)

    /**
     * This [LiveData] emits the current [UiState].
     */
    val uiStateLiveData = uiStateFlow
        .asLiveData(viewModelScope.coroutineContext, LIVE_DATA_TIMEOUT)
        .distinctUntilChanged()

    private val hasLocationFeature by lazy { locationRepository.hasLocationFeature }

    /**
     * This [LiveData] emits the current enabled state of the filter button.
     */
    val isFilterEnabledLiveData = allServiceNamesFlow
            .map { hasLocationFeature && !it.isNullOrEmpty() }
            .distinctUntilChanged()
            .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits when the user should be asked to grant location permissions.
     */
    val askForLocationPermissionsLiveData: LiveData<Unit> get() = askForLocationPermissions
    private val askForLocationPermissions = SingleLiveEvent<Unit>()

    /**
     * This [LiveData] emits the current visibility state of the context menu.
     */
    val showContextMenuLiveData = selectedStopCodeFlow.map {
        it?.ifEmpty { null } != null
    }.distinctUntilChanged().asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits the name of the currently selected stop.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedStopNameLiveData = selectedStopCodeFlow
            .flatMapLatest(this::loadBusStopName)
            .distinctUntilChanged()
            .asLiveData(viewModelScope.coroutineContext)

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
    val isArrivalAlertVisibleLiveData by lazy {
        alertsStateRetriever.isArrivalAlertVisibleFlow
                .asLiveData(viewModelScope.coroutineContext)
    }
    /**
     * This [LiveData] emits the visibility state of UI allowing the user to see or manipulate
     * proximity alerts.
     */
    val isProximityAlertVisibleLiveData by lazy {
        alertsStateRetriever.isProximityAlertVisibleFlow
                .asLiveData(viewModelScope.coroutineContext)
    }

    private val isAddedAsFavouriteFlow = favouritesStateRetriever
            .getIsAddedAsFavouriteStopFlow(selectedStopCodeFlow)
            .flowOn(defaultDispatcher)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    private val hasArrivalAlertFlow = alertsStateRetriever
            .getHasArrivalAlertFlow(selectedStopCodeFlow)
            .flowOn(defaultDispatcher)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    private val hasProximityAlertFlow = alertsStateRetriever
            .getHasProximityAlertFlow(selectedStopCodeFlow)
            .flowOn(defaultDispatcher)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    /**
     * This [LiveData] emits the enabled state of UI allowing the user to see or manipulate
     * favourite stops.
     */
    val isFavouriteEnabledLiveData = isAddedAsFavouriteFlow.map { it != null }
            .distinctUntilChanged()
            .asLiveData(viewModelScope.coroutineContext)
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
     * This [LiveData] emits whether the selected stop is added as a favourite nor not.
     */
    val isAddedAsFavouriteStopLiveData = isAddedAsFavouriteFlow.map { it ?: false }
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
     * This [LiveData] emits a stop code whenever stop data should be shown (e.g. live times).
     */
    val showStopDataLiveData: LiveData<String> get() = showStopData
    private val showStopData = SingleLiveEvent<String>()

    /**
     * This [LiveData] emits a stop code whenever a favourite stop should be added.
     */
    val showAddFavouriteStopLiveData: LiveData<String> get() = showAddFavouriteStop
    private val showAddFavouriteStop = SingleLiveEvent<String>()

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
     * When this [LiveData] emits a new item, the services chooser should be shown. The data that is
     * emitted is the parameters which should be passed to the chooser UI.
     */
    val showServicesChooserLiveData: LiveData<List<String>?> get() = showServicesChooser
    private val showServicesChooser = SingleLiveEvent<List<String>?>()

    /**
     * When this [LiveData] emits a new item, the location settings should be shown.
     */
    val showLocationSettingsLiveData: LiveData<Unit> get() = showLocationSettings
    private val showLocationSettings = SingleLiveEvent<Unit>()

    /**
     * When this [LiveData] emits a new consumable event, the user should be asked to turn on the
     * GPS location provider.
     */
    val showTurnOnGpsLiveData = combine(
        permissionsStateFlow.filterNotNull(),
        preferenceRepository.isGpsPromptDisabledFlow,
        ::Pair)
        .filter {
            shouldAskToTurnOnGps(it.first, it.second)
        }
        .onEach { savedState[STATE_ASKED_TURN_ON_GPS] = true }
        .map { Event(Unit) }
        .asLiveData(viewModelScope.coroutineContext)

    /**
     * This is called when the user has clicked on a nearest stop.
     *
     * @param nearestStop The nearest stop that the user clicked on.
     */
    fun onNearestStopClicked(nearestStop: UiNearestStop) {
        if (selectedStopCode == null) {
            showStopData.value = nearestStop.stopCode
        }
    }

    /**
     * This is called when the user has long clicked on a nearest stop.
     *
     * @param stopCode The stop code of the long clicked nearest stop.
     * @return `true` if we've consumed the event and request that the context menu be shown.
     * Otherwise, `false`.
     */
    fun onNearestStopLongClicked(stopCode: String): Boolean {
        return if (stopCode.isNotEmpty()) {
            selectedStopCode = stopCode

            true
        } else {
            false
        }
    }

    /**
     * This is called when the user has clicked on the filter menu item.
     */
    fun onFilterMenuItemClicked() {
        if (!allServiceNamesFlow.value.isNullOrEmpty()) {
            showServicesChooser.value = selectedServices
        }
    }

    /**
     * This is called from the UI when the context menu for the selected stop is closed, even when
     * we didn't request its closure (e.g. user back button press).
     */
    fun onNearestStopUnselected() {
        closeContextMenu()
    }

    /**
     * This is called when the favourite button has been clicked. If no stop is selected, calling
     * this method does nothing.
     *
     * If this stop is added as a user favourite, this method will request UI asking the user to
     * confirm the removal of the favourite stop.
     *
     * If this stop does not exist as a user favourite, this method will request UI which allows
     * the user to add a new favourite stop.
     *
     * @return `true` if we handled the event. That is, there is a selected stop. Otherwise `false`.
     */
    fun onFavouriteMenuItemClicked() = selectedStopCode?.let {
        when (isAddedAsFavouriteFlow.value) {
            true -> showConfirmDeleteFavourite.value = it
            false -> showAddFavouriteStop.value = it
            else -> {
                // Do nothing.
            }
        }

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
    fun onProximityAlertMenuItemClicked() = selectedStopCode?.let {
        when (hasProximityAlertFlow.value) {
            true -> showConfirmDeleteProximityAlert.value = it
            false -> showAddProximityAlert.value = it
            else -> {
                // Do nothing.
            }
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
    fun onTimeAlertMenuItemClicked() = selectedStopCode?.let {
        when (hasArrivalAlertFlow.value) {
            true -> showConfirmDeleteArrivalAlert.value = it
            false -> showAddArrivalAlert.value = it
            else -> {
                // Do nothing.
            }
        }

        closeContextMenu()
        true
    } ?: false

    /**
     * This is called when the show on map button is clicked. If there is a selected stop, it
     * shows that stop on a map.
     *
     * @return `true` if we handled the event. That is, there is a selected stop. Otherwise `false`.
     */
    fun onShowOnMapMenuItemClicked() = selectedStopCode?.let {
        showOnMap.value = it
        closeContextMenu()
        true
    } ?: false

    /**
     * This is called when the 'resolve' button on the error layout is clicked.
     */
    fun onResolveErrorButtonClicked() {
        when (uiStateFlow.value) {
            is UiState.Error.InsufficientLocationPermissions -> askForLocationPermissions.call()
            is UiState.Error.LocationOff -> showLocationSettings.call()
            else -> { /* Nothing to do here - exists to be exhaustive. */ }
        }
    }

    /**
     * Handle the permissions being updated. The logic in here determines if the user should be
     * asked to grant permission(s).
     *
     * @param permissionsState The newly-set [PermissionsState].
     */
    private fun handlePermissionsSet(permissionsState: PermissionsState) {
        val askedForPermissions: Boolean? = savedState[STATE_ASKED_FOR_PERMISSIONS]

        if (askedForPermissions != true) {
            savedState[STATE_ASKED_FOR_PERMISSIONS] = true

            if (permissionsState.fineLocationPermission == PermissionState.UNGRANTED &&
                    permissionsState.coarseLocationPermission == PermissionState.UNGRANTED) {
                askForLocationPermissions.call()
            }
        }
    }

    /**
     * Load the name for the given stop code. If the [stopCode] is `null` or empty, the returned
     * [kotlinx.coroutines.flow.Flow] emits `null`. When the name first begins loading, a
     * [UiNearestStopName] will be emitted with a populated stop code, but missing the name element.
     *
     * @param stopCode The stop code to get the name for.
     * @return A [UiNearestStopName] with the stop name.
     */
    private fun loadBusStopName(stopCode: String?) = stopCode?.ifEmpty { null }?.let { sc ->
        busStopsRepository.getNameForStopFlow(sc)
                .map { UiNearestStopName(sc, it) }
                .flowOn(defaultDispatcher)
                .onStart { emit(UiNearestStopName(sc, null)) }
    } ?: flowOf(null)

    /**
     * Map the given [UiState] in to a [List] of [UiNearestStop]s.
     *
     * @param state The state to map.
     * @param selectedStopCode The currently selected stop code.
     * @return The [List] of [UiNearestStop]s. This will be non-`null` when the [UiState] is a
     * [UiState.Success], otherwise `null` is returned here.
     */
    private fun mapToItems(
            state: UiState,
            selectedStopCode: String?): List<UiNearestStop>? {
        return if (state is UiState.Success) {
            state.nearestStops.map {
                it.copy(isSelected = it.stopCode == selectedStopCode)
            }
        } else {
            null
        }
    }

    /**
     * Should we ask to turn on GPS?
     *
     * @param permissionsState The [PermissionsState].
     * @param isGpsPromptDisabled Is the GPS prompt disabled?
     * @return `true` if we should ask to turn on GPS, otherwise `false`.
     */
    private fun shouldAskToTurnOnGps(
        permissionsState: PermissionsState,
        isGpsPromptDisabled: Boolean): Boolean {
        if (permissionsState.coarseLocationPermission != PermissionState.GRANTED &&
                permissionsState.fineLocationPermission != PermissionState.GRANTED) {
            return false
        }

        if (!locationRepository.hasGpsLocationProvider) {
            return false
        }

        if (isGpsPromptDisabled) {
            return false
        }

        if (savedState.get<Boolean>(STATE_ASKED_TURN_ON_GPS) == true) {
            return false
        }

        return !locationRepository.isGpsLocationProviderEnabled
    }

    /**
     * Close the context menu by scrubbing out the currently set selected stop code.
     */
    private fun closeContextMenu() {
        selectedStopCode = null
    }
}