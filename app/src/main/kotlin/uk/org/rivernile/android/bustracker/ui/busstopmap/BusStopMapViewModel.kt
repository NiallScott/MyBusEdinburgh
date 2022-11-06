/*
 * Copyright (C) 2018 - 2022 Niall 'Rivernile' Scott
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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopDetails
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.core.preferences.LastMapCameraLocation
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceManager
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceRepository
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import uk.org.rivernile.android.bustracker.utils.SingleLiveEvent

/**
 * This is a [ViewModel] for presenting the stop map.
 *
 * @param preferenceManager The [PreferenceManager].
 * @author Niall Scott
 */
class BusStopMapViewModel(
        private val savedState: SavedStateHandle,
        private val locationRepository: LocationRepository,
        servicesRepository: ServicesRepository,
        private val busStopsRepository: BusStopsRepository,
        private val serviceListingRetriever: ServiceListingRetriever,
        private val routeLineRetriever: RouteLineRetriever,
        isMyLocationEnabledDetector: IsMyLocationEnabledDetector,
        private val preferenceRepository: PreferenceRepository,
        private val preferenceManager: PreferenceManager,
        private val defaultDispatcher: CoroutineDispatcher)
    : ViewModel() {

    companion object {

        private const val STATE_REQUESTED_LOCATION_PERMISSIONS = "requestedLocationPermissions"
        private const val STATE_SELECTED_SERVICES = "selectedServices"
        private const val STATE_SELECTED_STOP_CODE = "selectedStopCode"

        private const val DEFAULT_ZOOM = 14f
        private const val STOP_ZOOM = 20f
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

    private val permissionsStateFlow = MutableStateFlow<PermissionsState?>(null)

    /**
     * This [LiveData] emits when the user should be asked to grant location permissions.
     */
    val requestLocationPermissionsLiveData: LiveData<Unit> get() = requestLocationPermissions
    private val requestLocationPermissions = SingleLiveEvent<Unit>()

    /**
     * This [LiveData] emits whether the My Location feature is enabled or not.
     */
    val isMyLocationFeatureEnabledLiveData = isMyLocationEnabledDetector
            .getIsMyLocationFeatureEnabledFlow(permissionsStateFlow.filterNotNull())
            .asLiveData(viewModelScope.coroutineContext)

    private val allServiceNamesFlow = servicesRepository.allServiceNamesFlow
            .flowOn(defaultDispatcher)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    /**
     * A [LiveData] which emits whether the filter menu item is enabled.
     */
    val isFilterEnabledLiveData = allServiceNamesFlow
            .map { !it.isNullOrEmpty() }
            .distinctUntilChanged()
            .asLiveData(viewModelScope.coroutineContext)

    /**
     * When this [LiveData] emits a new item, the services chooser should be shown. The data that is
     * emitted is the parameters which should be passed to the chooser UI.
     */
    val showServicesChooserLiveData: LiveData<ServicesChooserParams> get() = showServicesChooser
    private val showServicesChooser = SingleLiveEvent<ServicesChooserParams>()

    private val selectedServicesFlow =
            savedState.getStateFlow<Array<String>?>(STATE_SELECTED_SERVICES, null)

    private val selectedStopCodeFlow =
            savedState.getStateFlow<String?>(STATE_SELECTED_STOP_CODE, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val serviceListingFlow get() = selectedStopCodeFlow
            .flatMapLatest(serviceListingRetriever::getServiceListingFlow)

    val showStopMarkerInfoWindowLiveData =
            selectedStopCodeFlow.asLiveData(viewModelScope.coroutineContext)

    @OptIn(ExperimentalCoroutinesApi::class)
    val stopMarkersLiveData = selectedServicesFlow
            .flatMapLatest(this::loadBusStops)
            .combine(serviceListingFlow, this::mapToUiStopMarkers)
            .flowOn(defaultDispatcher)
            .asLiveData(viewModelScope.coroutineContext)

    @OptIn(ExperimentalCoroutinesApi::class)
    val routeLinesLiveData = selectedServicesFlow
            .flatMapLatest(this::loadRouteLines)
            .flowOn(defaultDispatcher)
            .asLiveData(viewModelScope.coroutineContext)

    val cameraLocationLiveData: LiveData<UiCameraLocation> get() = cameraLocation
    private val cameraLocation = SingleLiveEvent<UiCameraLocation>()

    val showStopDetailsLiveData: LiveData<String> get() = showStopDetails
    private val showStopDetails = SingleLiveEvent<String>()

    val showSearchLiveData: LiveData<Unit> get() = showSearch
    private val showSearch = SingleLiveEvent<Unit>()

    var lastCameraLocation: UiCameraLocation
        get() = mapToUiCameraLocation(preferenceRepository.lastMapCameraLocation)
        set(value) {
            preferenceRepository.lastMapCameraLocation = mapToLastMapCameraLocation(value)
        }

    /**
     * A [LiveData] representing the type of map to be displayed.
     */
    val mapType: LiveData<MapType>
        get() = _mapType
    /**
     * A [LiveData] representing a request to show the map type selector.
     */
    val showMapTypeSelection: LiveData<Void>
        get() = _showMapTypeSelection
    /**
     * A [LiveData] representing a request to update the traffic view.
     */
    val updateTrafficView: LiveData<Void>
        get() = _updateTrafficView

    /**
     * A [LiveData] representing a request to show the zoom controls.
     */
    val shouldShowZoomControls: Boolean
        get() = preferenceManager.isMapZoomButtonsShown()

    private val _mapType = MutableLiveData<MapType>()
    private val _showMapTypeSelection = SingleLiveEvent<Void>()
    private val _updateTrafficView = SingleLiveEvent<Void>()

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
     * This is called when the marker bubble is clicked.
     *
     * @param stopMarker The stop marker that was clicked on.
     */
    fun onMarkerBubbleClicked(stopMarker: UiStopMarker) {
        showStopDetails.value = stopMarker.stopCode
    }

    /**
     * This is called when the search menu item is clicked.
     */
    fun onSearchMenuItemClicked() {
        showSearch.call()
    }

    /**
     * This is called when the services menu item is clicked.
     */
    fun onServicesMenuItemClicked() {
        allServiceNamesFlow.value?.ifEmpty { null }?.let {
            showServicesChooser.value = ServicesChooserParams(
                    it,
                    selectedServicesFlow.value?.toList())
        }
    }

    /**
     * This is called when the map type menu item is clicked.
     */
    fun onMapTypeMenuItemClicked() {
        _showMapTypeSelection.call()
    }

    /**
     * This is called when the traffic view menu item is clicked.
     */
    fun onTrafficViewMenuItemClicked() {
        _updateTrafficView.call()
    }

    /**
     * This is called when a new map type has been selected.
     *
     * @param mapType The selected map type.
     */
    fun onMapTypeSelected(mapType: MapType) {
        _mapType.value = mapType
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
        savedState[STATE_SELECTED_STOP_CODE] = stopCode
        moveCameraToStopLocation(stopCode)
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
     * This is called when the map marker bubble has been closed.
     */
    fun onInfoWindowClosed() {
        savedState[STATE_SELECTED_STOP_CODE] = null
    }

    /**
     * Handle the permissions being updated. The logic in here determines if the user should be
     * asked to grant permission(s).
     *
     * @param permissionsState The newly-set [PermissionsState].
     */
    private fun handlePermissionsSet(permissionsState: PermissionsState) {
        val requestedPermissions: Boolean? = savedState[STATE_REQUESTED_LOCATION_PERMISSIONS]

        if (requestedPermissions != true) {
            savedState[STATE_REQUESTED_LOCATION_PERMISSIONS] = true

            if (locationRepository.hasLocationFeature &&
                    permissionsState.fineLocationPermission == PermissionState.UNGRANTED &&
                    permissionsState.coarseLocationPermission == PermissionState.UNGRANTED) {
                requestLocationPermissions.call()
            }
        }
    }

    /**
     * This is called when stops should be loaded.
     *
     * @param filteredServices Filtered services, if any.
     */
    private fun loadBusStops(filteredServices: Array<String>?) =
            busStopsRepository.getStopDetailsWithServiceFilterFlow(filteredServices?.toSet())

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
        lastCameraLocation = location
        cameraLocation.value = location
    }

    /**
     * Given an optional [List] of [StopDetails] and an optional [UiServiceListing], map this to a
     * [List] of [UiStopMarker]. This will be `null` if [stopDetails] is `null` or empty.
     *
     * @param stopDetails The [List] of [StopDetails].
     * @param serviceListing An optional [UiServiceListing] if a stop is currently selected.
     * @return The mapped [List] of [UiStopMarker], or `null`.
     */
    private fun mapToUiStopMarkers(
            stopDetails: List<StopDetails>?,
            serviceListing: UiServiceListing?) =
            stopDetails?.map { sd ->
                val sl = serviceListing?.takeIf { it.stopCode == sd.stopCode }

                mapToUiStopMarker(sd, sl)
            }?.ifEmpty { null }

    /**
     * Given a [StopDetails], map this to a [UiStopMarker]. If [serviceListing] is not `null`, this
     * means this stop is currently selected.
     *
     * @param stopDetails The [StopDetails] to map from.
     * @param serviceListing The [UiServiceListing] if this stop is currently selected.
     * @return The mapped [UiStopMarker].
     */
    private fun mapToUiStopMarker(
            stopDetails: StopDetails,
            serviceListing: UiServiceListing?) =
            UiStopMarker(
                    stopDetails.stopCode,
                    stopDetails.stopName,
                    LatLng(stopDetails.latitude, stopDetails.longitude),
                    stopDetails.orientation,
                    serviceListing)

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
}