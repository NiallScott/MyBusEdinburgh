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
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceManager
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import uk.org.rivernile.android.bustracker.repositories.busstopmap.BusStopMapRepository
import uk.org.rivernile.android.bustracker.repositories.busstopmap.SelectedStop
import uk.org.rivernile.android.bustracker.repositories.busstopmap.Stop
import uk.org.rivernile.android.bustracker.utils.ClearableLiveData
import uk.org.rivernile.android.bustracker.utils.SingleLiveEvent

/**
 * This is a [ViewModel] for presenting the stop map.
 *
 * @author Niall Scott
 * @param repository The [BusStopMapRepository].
 * @param preferenceManager The [PreferenceManager].
 */
class BusStopMapViewModel(
        private val savedState: SavedStateHandle,
        private val locationRepository: LocationRepository,
        servicesRepository: ServicesRepository,
        isMyLocationEnabledDetector: IsMyLocationEnabledDetector,
        private val repository: BusStopMapRepository,
        private val preferenceManager: PreferenceManager,
        defaultDispatcher: CoroutineDispatcher)
    : ViewModel() {

    companion object {

        private const val STATE_REQUESTED_LOCATION_PERMISSIONS = "requestedLocationPermissions"
        private const val STATE_SELECTED_SERVICES = "selectedServices"

        private const val DEFAULT_ZOOM = 14f
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
    // TODO: this is a temporary measure.
    private val selectedServicesLiveData =
            selectedServicesFlow.asLiveData(viewModelScope.coroutineContext)

    private var _busStops: ClearableLiveData<Map<String, Stop>>? = null
    /**
     * A [LiveData] which represents all stops to be shown on the map.
     */
    val busStops: LiveData<Map<String, Stop>> =
            Transformations.switchMap(selectedServicesLiveData, this::loadBusStops)

    /**
     * A [LiveData] which represents route lines shown on the map.
     */
    val routeLines: LiveData<Map<String, List<PolylineOptions>>> =
            Transformations.switchMap(selectedServicesLiveData, this::loadRouteLines)
    private var _routeLines: ClearableLiveData<Map<String, List<PolylineOptions>>>? = null

    /**
     * A [LiveData] representing the type of map to be displayed.
     */
    val mapType: LiveData<MapType>
        get() = _mapType
    /**
     * A [LiveData] representing a requested camera location.
     */
    val cameraLocation: LiveData<CameraLocation>
        get() = _cameraLocation
    /**
     * A [LiveData] representing a request to show the map marker bubble.
     */
    val showMapMarkerBubble: LiveData<SelectedStop>
        get() = _showMapMarkerBubble
    /**
     * A [LiveData] representing a request to show details for a stop.
     */
    val showStopDetails: LiveData<String>
        get() = _showStopDetails
    /**
     * A [LiveData] representing a request to show the search UI.
     */
    val showSearch: LiveData<Void>
        get() = _showSearch
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

    private val _selectedStopCode = MutableLiveData<String?>()

    private val _mapType = MutableLiveData<MapType>()
    private val _cameraLocation = MutableLiveData<CameraLocation>()
    private val _showStopDetails = SingleLiveEvent<String>()
    private val _showSearch = SingleLiveEvent<Void>()
    private val _showMapTypeSelection = SingleLiveEvent<Void>()
    private val _updateTrafficView = SingleLiveEvent<Void>()

    private val _selectedStopLiveData =
            Transformations.switchMap(_selectedStopCode, this::loadBusStop)
    private var _selectedStop: ClearableLiveData<SelectedStop>? = null
    private val _showMapMarkerBubble = MediatorLiveData<SelectedStop>().also {
        it.addSource(_selectedStopLiveData, this::handleSelectedStopLoaded)
    }

    private var searchedBusStop: String? = null

    /**
     * This should be called when state is being restored from the UI.
     *
     * @param selectedStopCode The selected stop code.
     */
    fun onRestoreState(selectedStopCode: String?) {
        _cameraLocation.value = CameraLocation(
                preferenceManager.getLastMapLatitude(),
                preferenceManager.getLastMapLongitude(),
                preferenceManager.getLastMapZoomLevel(), false)
        _mapType.value = MapType.fromValue(preferenceManager.getLastMapType())

        if (_selectedStopCode.value != selectedStopCode) {
            _selectedStopCode.value = selectedStopCode
        }
    }

    /**
     * This is called when the hosting UI is first created and has no arguments.
     */
    fun onFirstCreate() {
        _cameraLocation.value = CameraLocation(
                preferenceManager.getLastMapLatitude(),
                preferenceManager.getLastMapLongitude(),
                preferenceManager.getLastMapZoomLevel(),
                false)
        _mapType.value = MapType.fromValue(preferenceManager.getLastMapType())
    }

    /**
     * This is called when the hosting UI is first created and has a stop code argument.
     *
     * @param stopCode The supplied stop code.
     */
    fun onFirstCreate(stopCode: String) {
        searchedBusStop = stopCode
        _selectedStopCode.value = stopCode
        _mapType.value = MapType.fromValue(preferenceManager.getLastMapType())
    }

    /**
     * This is called when the hosting UI is first created and has a latitude/longitude pair as
     * arguments.
     *
     * @param latitude The supplied latitude.
     * @param longitude The supplied longitude.
     */
    fun onFirstCreate(latitude: Double, longitude: Double) {
        _cameraLocation.value = CameraLocation(latitude, longitude, DEFAULT_ZOOM, false)
        _mapType.value = MapType.fromValue(preferenceManager.getLastMapType())
    }

    override fun onCleared() {
        _busStops?.onCleared()
        _selectedStop?.onCleared()
        _routeLines?.onCleared()
    }

    /**
     * This is called when the marker bubble is clicked.
     *
     * @param stop The stop that was clicked on.
     */
    fun onMarkerBubbleClicked(stop: Stop) {
        _showStopDetails.value = stop.stopCode
    }

    /**
     * This is called when the search menu item is clicked.
     */
    fun onSearchMenuItemClicked() {
        _showSearch.call()
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
     * This is called when map parameters should be persisted.
     *
     * @param latitude The latitude to be persisted.
     * @param longitude The longitude to be persisted.
     * @param zoomLevel The zoom level to be persisted.
     * @param mapType The map type to be persisted.
     */
    fun onPersistMapParameters(latitude: Double, longitude: Double, zoomLevel: Float,
                               mapType: MapType) {
        preferenceManager.apply {
            setLastMapLatitude(latitude)
            setLastMapLongitude(longitude)
            setLastMapZoomLevel(zoomLevel)
            setLastMapType(mapType.value)
        }
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
        searchedBusStop = stopCode
        _selectedStopCode.value = stopCode
    }

    /**
     * This is called when the user has clicked a stop marker cluster.
     *
     * @param latitude The latitude of the cluster.
     * @param longitude The longitude of the cluster.
     * @param currentZoom The current camera zoom.
     */
    fun onClusterMarkerClicked(latitude: Double, longitude: Double, currentZoom: Float) {
        _cameraLocation.value = CameraLocation(latitude, longitude, currentZoom + 1f, true)
    }

    /**
     * This is called when a map marker has been clicked.
     *
     * @param stop The stop representing the map marker.
     */
    fun onMapMarkerClicked(stop: Stop) {
        _selectedStopCode.value = stop.stopCode
        _cameraLocation.value = CameraLocation(stop.latitude, stop.longitude, null, true)
    }

    /**
     * This is called when the map marker bubble has been closed.
     */
    fun onMapMarkerBubbleClosed(stopCode: String) {
        if (stopCode == _selectedStopCode.value) {
            _selectedStopCode.value = null
            _showMapMarkerBubble.value = null
        }
    }

    /**
     * This is called when a new camera location is requested.
     *
     * @param latitude The requested latitude.
     * @param longitude The requested longitude.
     */
    fun onRequestCameraLocation(latitude: Double, longitude: Double) {
        _cameraLocation.value = CameraLocation(latitude, longitude, DEFAULT_ZOOM, false)
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
            repository.getBusStops(filteredServices).also {
                _busStops?.onCleared()
                _busStops = it
            }

    /**
     * This is called when a stop should be loaded.
     *
     * @param stopCode The stop code of the stop to load. `null` will mean no loading will occur.
     */
    private fun loadBusStop(stopCode: String?) = stopCode?.let {
        repository.getBusStop(it).also { liveData ->
            _selectedStop?.onCleared()
            _selectedStop = liveData
        }
    }

    /**
     * This is called when route lines should be loaded.
     *
     * @param services A service filter for the route lines.
     */
    private fun loadRouteLines(services: Array<String>?) =
            repository.getRouteLines(services).also {
                _routeLines?.onCleared()
                _routeLines = it
            }

    /**
     * Handle a selected stop load result.
     *
     * @param selectedStop The selected stop.
     */
    private fun handleSelectedStopLoaded(selectedStop: SelectedStop?) {
        _showMapMarkerBubble.value = selectedStop

        if (searchedBusStop != null && searchedBusStop == selectedStop?.stopCode) {
            selectedStop?.let {
                _cameraLocation.value = CameraLocation(it.latitude, it.longitude, 99f, false)
            }
        }
    }
}