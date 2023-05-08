/*
 * Copyright (C) 2018 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.busstopmap

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.di.ForApplicationCoroutineScope
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.preferences.LastMapCameraLocation
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceRepository
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import uk.org.rivernile.android.bustracker.utils.Event
import uk.org.rivernile.android.bustracker.utils.SingleLiveEvent
import javax.inject.Inject

/**
 * This is a [ViewModel] for presenting the stop map.
 *
 * @param savedState The saved instance state.
 * @param permissionHandler Used to handle the permission state changing.
 * @param playServicesAvailabilityChecker Used to check the availability of Play Services.
 * @param servicesRepository Used to access services data.
 * @param busStopsRepository Used to access bus stop data.
 * @param stopMarkersRetriever Retrieves stop markers to be shown on the map.
 * @param routeLineRetriever Used to retrieve route lines for display on the map.
 * @param isMyLocationEnabledDetector Used to detect whether the My Location feature is enabled or
 * not.
 * @param preferenceRepository A repository for storing user preferences.
 * @param applicationCoroutineScope The application [CoroutineScope].
 * @param defaultDispatcher The default [CoroutineDispatcher].
 * @author Niall Scott
 */
@HiltViewModel
class BusStopMapViewModel @Inject constructor(
        private val savedState: SavedStateHandle,
        private val permissionHandler: PermissionHandler,
        playServicesAvailabilityChecker: PlayServicesAvailabilityChecker,
        servicesRepository: ServicesRepository,
        private val busStopsRepository: BusStopsRepository,
        stopMarkersRetriever: StopMarkersRetriever,
        private val routeLineRetriever: RouteLineRetriever,
        isMyLocationEnabledDetector: IsMyLocationEnabledDetector,
        private val preferenceRepository: PreferenceRepository,
        @ForApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
        @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher)
    : ViewModel() {

    companion object {

        private const val STATE_SELECTED_SERVICES = "selectedServices"
        private const val STATE_SELECTED_STOP_CODE = "selectedStopCode"
        private const val STATE_TRAFFIC_VIEW_ENABLED = "trafficViewEnabled"

        private const val DEFAULT_ZOOM = 14f
        private const val STOP_ZOOM = 20f
    }

    /**
     * The current state of permissions pertaining to this view.
     */
    var permissionsState: PermissionsState
        get() = permissionHandler.permissionsState
        set(value) {
            permissionHandler.permissionsState = value
        }

    private val selectedServicesFlow =
            savedState.getStateFlow<Array<String>?>(STATE_SELECTED_SERVICES, null)

    private val selectedStopCodeFlow =
            savedState.getStateFlow<String?>(STATE_SELECTED_STOP_CODE, null)

    private val playServicesAvailabilityFlow = playServicesAvailabilityChecker
            .apiAvailabilityFlow
            .flowOn(defaultDispatcher)
            .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(replayExpirationMillis = 0L),
                    PlayServicesAvailabilityResult.InProgress)

    private val allServiceNamesFlow = servicesRepository.allServiceNamesFlow
            .flowOn(defaultDispatcher)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    /**
     * This [LiveData] emits when the user should be asked to grant location permissions.
     */
    val requestLocationPermissionsLiveData = permissionHandler
            .requestLocationPermissionsFlow
            .map { Event(Unit) }
            .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits the current [UiState].
     */
    val uiStateLiveData = playServicesAvailabilityFlow
            .map(this::mapPlayServicesAvailabilityToUiState)
            .distinctUntilChanged()
            .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits the Play Services error code as an `Int`.
     */
    val playServicesErrorLiveData = playServicesAvailabilityFlow
            .map(this::mapPlayServicesAvailabilityToError)
            .distinctUntilChanged()
            .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits whether the Play Services resolve error button is visible.
     */
    val isErrorResolveButtonVisibleLiveData = playServicesAvailabilityFlow
            .map { it is PlayServicesAvailabilityResult.Unavailable.Resolvable }
            .distinctUntilChanged()
            .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits [List]s of [UiStopMarker]s to be shown on the map.
     */
    val stopMarkersLiveData = stopMarkersRetriever
            .stopMarkersFlow
            .flowOn(defaultDispatcher)
            .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits [List]s of [UiServiceRoute]s to be show on the map.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val routeLinesLiveData = selectedServicesFlow
            .flatMapLatest(this::loadRouteLines)
            .flowOn(defaultDispatcher)
            .asLiveData(viewModelScope.coroutineContext)

    /**
     * A [LiveData] which emits whether the filter menu item is enabled.
     */
    val isFilterMenuItemEnabledLiveData = combine(
            allServiceNamesFlow,
            playServicesAvailabilityFlow,
            this::calculateFilterMenuItemEnabled)
            .distinctUntilChanged()
            .asLiveData(viewModelScope.coroutineContext)

    /**
     * A [LiveData] which emits whether the map type item is enabled.
     */
    val isMapTypeMenuItemEnabledLiveData = playServicesAvailabilityFlow
            .map(this::calculateMenuItemEnabled)
            .distinctUntilChanged()
            .asLiveData(viewModelScope.coroutineContext)

    /**
     * A [LiveData] which emits whether the traffic view item is enabled.
     */
    val isTrafficViewMenuItemEnabledLiveData = playServicesAvailabilityFlow
            .map(this::calculateMenuItemEnabled)
            .distinctUntilChanged()
            .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits [UiCameraLocation]s when the map camera should be moved.
     */
    val cameraLocationLiveData: LiveData<UiCameraLocation> get() = cameraLocation
    private val cameraLocation = SingleLiveEvent<UiCameraLocation>()

    /**
     * This [LiveData] emits whether the My Location feature is enabled or not.
     */
    val isMyLocationFeatureEnabledLiveData by lazy {
        isMyLocationEnabledDetector
                .getIsMyLocationFeatureEnabledFlow(
                        permissionHandler.permissionsStateFlow.filterNotNull())
                .asLiveData(viewModelScope.coroutineContext)
    }

    /**
     * This [LiveData] emits whether the traffic view feature is enabled.
     */
    val isTrafficViewEnabledLiveData = savedState
            .getStateFlow(STATE_TRAFFIC_VIEW_ENABLED, false)
            .asLiveData(viewModelScope.coroutineContext)
            .distinctUntilChanged()

    /**
     * This [LiveData] emits whether the map zoom controls should be shown.
     */
    val isZoomControlsVisibleLiveData = preferenceRepository
            .isMapZoomControlsVisibleFlow
            .flowOn(defaultDispatcher)
            .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits the map type.
     */
    val mapTypeLiveData = preferenceRepository
            .mapTypeFlow
            .map(MapType::fromValue)
            .flowOn(defaultDispatcher)
            .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits the last camera location.
     */
    val lastCameraLocationLiveData = preferenceRepository
        .lastMapCameraLocationFlow
        .map(this::mapToUiCameraLocation)
        .flowOn(defaultDispatcher)
        .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits an `Int` error code when the Play Services error resolution UI should
     * be shown.
     */
    val showPlayServicesErrorResolutionLiveData: LiveData<Int> get() =
        showPlayServicesErrorResolution
    private val showPlayServicesErrorResolution = SingleLiveEvent<Int>()

    /**
     * This [LiveData] emits when the map type selection UI should be shown.
     */
    val showMapTypeSelectionLiveData: LiveData<Unit> get() = showMapTypeSelection
    private val showMapTypeSelection = SingleLiveEvent<Unit>()

    /**
     * This [LiveData] emits a stop code when the stop marker info window should be shown.
     */
    val showStopMarkerInfoWindowLiveData = selectedStopCodeFlow
        .asLiveData(viewModelScope.coroutineContext)
        .distinctUntilChanged()

    /**
     * This [LiveData] emits a stop code when the stop details UI should be shown.
     */
    val showStopDetailsLiveData: LiveData<String> get() = showStopDetails
    private val showStopDetails = SingleLiveEvent<String>()

    /**
     * When this [LiveData] emits a new item, the services chooser should be shown. The data that is
     * emitted is the parameters which should be passed to the chooser UI.
     */
    val showServicesChooserLiveData: LiveData<List<String>?> get() = showServicesChooser
    private val showServicesChooser = SingleLiveEvent<List<String>?>()

    /**
     * This is called when the error resolution button has been clicked.
     */
    fun onErrorResolveButtonClicked() {
        val result = playServicesAvailabilityFlow.value

        if (result is PlayServicesAvailabilityResult.Unavailable.Resolvable) {
            showPlayServicesErrorResolution.value = result.errorCode
        }
    }

    /**
     * This is called when a request has been made to move the map camera to the location of the
     * given [stopCode].
     *
     * @param stopCode The stop to move the camera map location to.
     */
    fun showStop(stopCode: String) {
        savedState[STATE_SELECTED_STOP_CODE] = stopCode
        moveCameraToStopLocation(stopCode)
    }

    /**
     * This is called when a request has been made to move the map camera to the given location.
     *
     * @param latLon The latitude/longitude of where the map camera should be moved to.
     */
    fun showLocation(latLon: UiLatLon) {
        moveCameraToLocation(UiCameraLocation(latLon, DEFAULT_ZOOM))
    }

    /**
     * This is called when the services menu item is clicked.
     */
    fun onServicesMenuItemClicked() {
        if (!allServiceNamesFlow.value.isNullOrEmpty()) {
            showServicesChooser.value = selectedServicesFlow.value?.toList()
        }
    }

    /**
     * This is called when the map type menu item is clicked.
     */
    fun onMapTypeMenuItemClicked() {
        showMapTypeSelection.call()
    }

    /**
     * This is called when the traffic view menu item is clicked.
     */
    fun onTrafficViewMenuItemClicked() {
        savedState[STATE_TRAFFIC_VIEW_ENABLED] =
                !(savedState[STATE_TRAFFIC_VIEW_ENABLED] ?: false)
    }

    /**
     * This is called when a new map type has been selected.
     *
     * @param mapType The selected map type.
     */
    fun onMapTypeSelected(mapType: MapType) {
        applicationCoroutineScope.launch(defaultDispatcher) {
            preferenceRepository.setMapType(mapType.value)
        }
    }

    /**
     * This is called when services have been selected.
     *
     * @param selectedServices The selected services.
     */
    fun onServicesSelected(selectedServices: List<String>?) {
        savedState[STATE_SELECTED_SERVICES] = selectedServices?.toTypedArray()
    }

    /**
     * This is called when the user has selected a stop code search result.
     *
     * @param stopCode The selected stop code.
     */
    fun onStopSearchResult(stopCode: String) {
        showStop(stopCode)
    }

    /**
     * This is called when a map marker has been clicked.
     *
     * @param stopMarker The stop marker.
     */
    fun onMapMarkerClicked(stopMarker: UiStopMarker) {
        savedState[STATE_SELECTED_STOP_CODE] = stopMarker.stopCode
    }

    /**
     * This is called when the marker bubble is clicked.
     *
     * @param stopMarker The stop marker that was clicked on.
     */
    fun onMarkerBubbleClicked(stopMarker: UiStopMarker) {
        showStopDetails.value = stopMarker.stopCode
    }

    /**
     * This is called when the map marker bubble has been closed.
     */
    fun onInfoWindowClosed() {
        savedState[STATE_SELECTED_STOP_CODE] = null
    }

    /**
     * Update the camera location to the new value.
     *
     * @param cameraLocation The new camera location.
     */
    fun updateCameraLocation(cameraLocation: UiCameraLocation) {
        applicationCoroutineScope.launch(defaultDispatcher) {
            preferenceRepository.setLastMapCameraLocation(
                mapToLastMapCameraLocation(cameraLocation))
        }
    }

    /**
     * Given a [PlayServicesAvailabilityResult], map this to the relevant [UiState].
     *
     * @param result The result from determining Play Services availability.
     * @return The [UiState].
     */
    private fun mapPlayServicesAvailabilityToUiState(
            result: PlayServicesAvailabilityResult): UiState {
        return when (result) {
            is PlayServicesAvailabilityResult.InProgress -> UiState.PROGRESS
            is PlayServicesAvailabilityResult.Available -> UiState.CONTENT
            else -> UiState.ERROR
        }
    }

    /**
     * Given a [PlayServicesAvailabilityResult], map this to an error code if available.
     *
     * @param result The result from determining Play Services availability.
     * @return The error code for the result, or `null` if this does not apply.
     */
    private fun mapPlayServicesAvailabilityToError(
            result: PlayServicesAvailabilityResult): Int? {
        return (result as? PlayServicesAvailabilityResult.Unavailable)?.errorCode
    }

    /**
     * This is called when route lines should be loaded.
     *
     * @param services A service filter for the route lines.
     */
    private fun loadRouteLines(services: Array<String>?) =
            routeLineRetriever.getRouteLinesFlow(services?.toSet())

    /**
     * Move the map camera to the location of the given [stopCode]. If this stop code is invalid
     * or there is no location for this stop, no action will be performed.
     *
     * @param stopCode The stop code to move the map camera to.
     */
    private fun moveCameraToStopLocation(stopCode: String) {
        viewModelScope.launch {
            withContext(defaultDispatcher) {
                busStopsRepository.getStopLocation(stopCode)
            }?.let {
                moveCameraToLocation(UiCameraLocation(
                        UiLatLon(
                                it.latitude,
                                it.longitude),
                        STOP_ZOOM))
            }
        }
    }

    /**
     * Move the camera to the given [UiCameraLocation].
     *
     * @param location The new location for the camera.
     */
    private fun moveCameraToLocation(location: UiCameraLocation) {
        updateCameraLocation(location)
        cameraLocation.value = location
    }

    /**
     * Given a [LastMapCameraLocation], map it to a [UiCameraLocation].
     *
     * @param cameraLocation The last location of the map camera.
     * @return The [LastMapCameraLocation] as a [UiCameraLocation].
     */
    private fun mapToUiCameraLocation(cameraLocation: LastMapCameraLocation) =
            UiCameraLocation(
                    UiLatLon(
                            cameraLocation.latitude,
                            cameraLocation.longitude),
                    cameraLocation.zoomLevel)

    /**
     * Given a [UiCameraLocation], map it to a [LastMapCameraLocation].
     *
     * @param cameraLocation The camera location.
     * @return The [UiCameraLocation] as a [LastMapCameraLocation].
     */
    private fun mapToLastMapCameraLocation(cameraLocation: UiCameraLocation) =
            LastMapCameraLocation(
                    cameraLocation.latLon.latitude,
                    cameraLocation.latLon.longitude,
                    cameraLocation.zoomLevel)

    /**
     * Calculate whether the filter menu item should be enabled.
     *
     * @param serviceNames The loaded service names used for filtering.
     * @param playServicesAvailabilityResult The Play Services availability result.
     * @return `true` if the filter menu item should be enabled, otherwise `false`.
     */
    private fun calculateFilterMenuItemEnabled(
            serviceNames: List<String>?,
            playServicesAvailabilityResult: PlayServicesAvailabilityResult) =
            !serviceNames.isNullOrEmpty() &&
                    playServicesAvailabilityResult is PlayServicesAvailabilityResult.Available

    /**
     * Calculate whether an ordinary menu item should be enabled. This is based on the Play Services
     * availability result.
     *
     * @param playServicesAvailabilityResult The Play Services availability result.
     * @return `true` if the menu item should be enabled, otherwise `false`.
     */
    private fun calculateMenuItemEnabled(
            playServicesAvailabilityResult: PlayServicesAvailabilityResult) =
            playServicesAvailabilityResult is PlayServicesAvailabilityResult.Available
}