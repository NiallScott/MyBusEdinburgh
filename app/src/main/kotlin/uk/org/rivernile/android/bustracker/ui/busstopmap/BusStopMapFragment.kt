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

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import dagger.android.support.AndroidSupportInjection
import uk.org.rivernile.android.bustracker.core.bundle.getParcelableCompat
import uk.org.rivernile.android.bustracker.core.bundle.getSerializableCompat
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowBusTimesListener
import uk.org.rivernile.android.bustracker.ui.serviceschooser.ServicesChooserDialogFragment
import uk.org.rivernile.android.bustracker.viewmodel.GenericSavedStateViewModelFactory
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.BusstopmapFragmentBinding
import javax.inject.Inject

/**
 * This is a [Fragment] for display a map with stop marker icons and route lines.
 *
 * @author Niall Scott
 */
class BusStopMapFragment : Fragment() {

    companion object {

        private const val ARG_STOPCODE = "stopCode"
        private const val ARG_LOCATION = "location"

        private const val DIALOG_SERVICES_CHOOSER = "dialogServicesChooser"
        private const val DIALOG_MAP_TYPE_BOTTOM_SHEET = "bottomSheetMapType"

        /**
         * Create a new instance of [BusStopMapFragment] with no parameters.
         *
         * @return A new instance of [BusStopMapFragment].
         */
        @JvmStatic
        fun newInstance() = BusStopMapFragment()

        /**
         * Create a new instance of [BusStopMapFragment], specifying the initially selected stop
         * code.
         *
         * @param stopCode The initially selected stop code.
         * @return A new instance of [BusStopMapFragment].
         */
        fun newInstance(stopCode: String?) = BusStopMapFragment().apply {
            stopCode?.let {
                arguments = Bundle().apply {
                    putString(ARG_STOPCODE, it)
                }
            }
        }

        /**
         * Create a new instance of [BusStopMapFragment], specifying the initial latitude/longitude
         * camera location.
         *
         * @param location The initial camera location.
         * @return A new instance of [BusStopMapFragment].
         */
        fun newInstance(location: UiLatLon) = BusStopMapFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_LOCATION, location)
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: BusStopMapFragmentViewModelFactory
    @Inject
    lateinit var googleApiAvailability: GoogleApiAvailability
    @Inject
    lateinit var stopClusterRendererFactory: StopClusterRendererFactory

    private val viewModel: BusStopMapViewModel by viewModels {
        GenericSavedStateViewModelFactory(viewModelFactory, this)
    }

    private lateinit var callbacks: Callbacks
    private var map: GoogleMap? = null
    private var clusterManager: ClusterManager<UiStopMarker>? = null
    private var routeLineManager: RouteLineManager? = null

    private val viewBinding get() = _viewBinding!!
    private var _viewBinding: BusstopmapFragmentBinding? = null

    private var menuItemServices: MenuItem? = null
    private var menuItemTrafficView: MenuItem? = null

    private val requestLocationPermissionsLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
            this::handleLocationPermissionsResult)

    private val searchStopLauncher = registerForActivityResult(SearchStop()) { stopCode ->
        stopCode?.let {
            viewModel.onStopSearchResult(it)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            callbacks = context as Callbacks
        } catch (e: ClassCastException) {
            throw IllegalStateException("${context.javaClass.name} does not implement " +
                    Callbacks::class.java.name)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)

        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            arguments?.let { args ->
                when {
                    args.containsKey(ARG_STOPCODE) ->
                        args.getString(ARG_STOPCODE)?.let(viewModel::showStop)
                    args.containsKey(ARG_LOCATION) ->
                        args.getParcelableCompat<UiLatLon>(ARG_LOCATION)
                                ?.let(viewModel::showLocation)
                    else -> { }
                }
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        _viewBinding = BusstopmapFragmentBinding.inflate(inflater, container, false)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewLifecycleOwner = viewLifecycleOwner

        childFragmentManager.setFragmentResultListener(
                MapTypeBottomSheetDialogFragment.REQUEST_KEY,
                viewLifecycleOwner) { _, result ->
            val mapType = result.getSerializableCompat(
                    MapTypeBottomSheetDialogFragment.RESULT_CHOSEN_MAP_TYPE)
                    ?: MapType.NORMAL
            viewModel.onMapTypeSelected(mapType)
        }

        childFragmentManager.setFragmentResultListener(
                ServicesChooserDialogFragment.REQUEST_KEY,
                viewLifecycleOwner) { _, result ->
            val selectedServices = result.getStringArray(
                    ServicesChooserDialogFragment.RESULT_CHOSEN_SERVICES)
            viewModel.onServicesSelected(selectedServices?.toList())
        }

        viewBinding.mapView.apply {
            onCreate(savedInstanceState)
            getMapAsync(this@BusStopMapFragment::handleOnMapReady)
        }

        viewBinding.layoutError.btnErrorResolve.setText(R.string.busstopmapfragment_button_resolve)

        viewModel.requestLocationPermissionsLiveData.observe(viewLifecycleOwner) {
            handleRequestLocationPermissions()
        }
        viewModel.isMyLocationFeatureEnabledLiveData.observe(viewLifecycleOwner,
                this::handleMyLocationEnabledChanged)
        viewModel.isFilterEnabledLiveData.observe(viewLifecycleOwner,
                this::handleServicesMenuItemEnabledChanged)
        viewModel.showServicesChooserLiveData.observe(viewLifecycleOwner, this::showServicesChooser)
        viewModel.stopMarkersLiveData.observe(viewLifecycleOwner, this::handleStopMarkersChanged)
        viewModel.routeLinesLiveData.observe(viewLifecycleOwner, this::handleRouteLinesChanged)
        viewModel.showStopMarkerInfoWindowLiveData.observe(viewLifecycleOwner,
                this::handleShowMapMarkerInfoWindow)
        viewModel.showStopDetailsLiveData.observe(viewLifecycleOwner, callbacks::onShowBusTimes)
        viewModel.showSearchLiveData.observe(viewLifecycleOwner) {
            showSearch()
        }
        viewModel.cameraLocationLiveData.observe(viewLifecycleOwner,
                this::handleCameraPositionChanged)

        viewModel.showMapTypeSelectionLiveData.observe(viewLifecycleOwner) {
            showMapTypeSelection()
        }
        viewModel.isTrafficViewEnabledLiveData.observe(viewLifecycleOwner) {
            handleTrafficViewEnabledChanged(it)
            handleTrafficViewMenuItemChanged(it)
        }
        viewModel.isZoomControlsVisibleLiveData.observe(viewLifecycleOwner,
                this::handleZoomControlsVisibilityChanged)
        viewModel.mapTypeFlow.observe(viewLifecycleOwner, this::handleMapTypeChanged)

        requireActivity().apply {
            setTitle(R.string.map_title)
            addMenuProvider(menuProvider, viewLifecycleOwner)
        }
    }

    override fun onStart() {
        super.onStart()

        checkGooglePlayServices()
        viewBinding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()

        viewBinding.mapView.onResume()
        updatePermissions()
    }

    override fun onPause() {
        super.onPause()

        viewBinding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()

        viewBinding.mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        clusterManager = null
        routeLineManager = null
        map = null
        viewBinding.mapView.onDestroy()
        _viewBinding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        _viewBinding?.mapView?.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()

        viewBinding.mapView.onLowMemory()
    }

    /**
     * This should be called by the hosting [android.app.Activity] when a new stop code has been
     * selected. For example, the hosting [android.app.Activity] may receive a call to
     * [android.app.Activity.onNewIntent]. Set the newly supplied stop code here in this case.
     *
     * @param stopCode The new stop code.
     */
    fun onNewStopCode(stopCode: String) {
        viewModel.onStopSearchResult(stopCode)
    }

    /**
     * This should be called by the hosting [android.app.Activity] when a new latitude/longitude
     * pair has bee selected. For example, the hosting [android.app.Activity] may receive a call to
     * [android.app.Activity.onNewIntent]. Set the newly supplied latitude/longitude pair here in
     * this case.
     *
     * @param location The location to move the camera to.
     */
    fun onRequestCameraLocation(location: UiLatLon) {
        viewModel.showLocation(location)
    }

    /**
     * Update [BusStopMapViewModel] with the current state of permissions.
     */
    private fun updatePermissions() {
        viewModel.permissionsState = PermissionsState(
                getPermissionState(Manifest.permission.ACCESS_FINE_LOCATION),
                getPermissionState(Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    /**
     * Handle asking the user to grant permissions.
     */
    private fun handleRequestLocationPermissions() {
        requestLocationPermissionsLauncher.launch(
                arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    /**
     * Handle the result of asking for location permission access.
     *
     * @param states A [Map] of the permission to a boolean which informs us if the permission was
     * granted or not.
     */
    private fun handleLocationPermissionsResult(states: Map<String, Boolean>) {
        val fineLocationState = states[Manifest.permission.ACCESS_FINE_LOCATION]
                ?.let(this::getPermissionState)
                ?: getPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationState = states[Manifest.permission.ACCESS_COARSE_LOCATION]
                ?.let(this::getPermissionState)
                ?: getPermissionState(Manifest.permission.ACCESS_COARSE_LOCATION)

        viewModel.permissionsState = PermissionsState(fineLocationState, coarseLocationState)
    }

    /**
     * For a given [permission], determine the [PermissionState].
     *
     * @param permission The permission to obtain the [PermissionState] for.
     * @return The determined [PermissionState].
     */
    private fun getPermissionState(permission: String) =
            getPermissionState(
                    ContextCompat.checkSelfPermission(requireContext(), permission) ==
                            PackageManager.PERMISSION_GRANTED)

    /**
     * Maps the permission granted status in to a [PermissionState].
     *
     * @param isGranted Has the permission been granted? `true` implies [PermissionState.GRANTED],
     * otherwise [PermissionState.UNGRANTED].
     * @return The determined [PermissionState].
     */
    private fun getPermissionState(isGranted: Boolean) = if (isGranted) {
        PermissionState.GRANTED
    } else {
        PermissionState.UNGRANTED
    }

    /**
     * Handle the map being ready.
     *
     * @param map Used to control the map.
     */
    private fun handleOnMapReady(map: GoogleMap) {
        this.map = map.apply {
            // This is just whether the my location button should be enabled if the my location
            // feature is enabled. The button disappears when the feature is not enabled regardless
            // of this setting.
            uiSettings.isMyLocationButtonEnabled = true

            setOnInfoWindowCloseListener {
                viewModel.onInfoWindowClosed()
            }
        }

        val context = requireContext()
        clusterManager = ClusterManager<UiStopMarker>(context, map).apply {
            renderer = stopClusterRendererFactory.createStopClusterRenderer(context, map, this)

            setOnClusterClickListener(this@BusStopMapFragment::handleOnClusterClicked)
            setOnClusterItemClickListener(this@BusStopMapFragment::handleOnClusterItemClick)
            setOnClusterItemInfoWindowClickListener(viewModel::onMarkerBubbleClicked)

            markerCollection.setInfoWindowAdapter(
                    MapInfoWindow(context, layoutInflater, view as ViewGroup))
        }

        routeLineManager = RouteLineManager(context, map)

        map.setOnCameraIdleListener {
            handleCameraIdle()
        }

        handleMyLocationEnabledChanged(viewModel.isMyLocationFeatureEnabledLiveData.value ?: false)
        handleStopMarkersChanged(viewModel.stopMarkersLiveData.value)
        handleRouteLinesChanged(viewModel.routeLinesLiveData.value)
        handleShowMapMarkerInfoWindow(viewModel.showStopMarkerInfoWindowLiveData.value)
        handleTrafficViewEnabledChanged(viewModel.isTrafficViewEnabledLiveData.value ?: false)
        handleZoomControlsVisibilityChanged(viewModel.isZoomControlsVisibleLiveData.value ?: false)
        handleMapTypeChanged(viewModel.mapTypeFlow.value ?: MapType.NORMAL)

        val lastMapCameraLocation = viewModel.lastCameraLocation
        CameraUpdateFactory.newLatLngZoom(
                LatLng(
                        lastMapCameraLocation.latLon.latitude,
                        lastMapCameraLocation.latLon.longitude),
                lastMapCameraLocation.zoomLevel)
                .let(map::moveCamera)
    }

    /**
     * Handle the map camera becoming idle.
     */
    private fun handleCameraIdle() {
        // We need to pass the camera idle event through to the ClusterManager so that clustering
        // occurs.
        clusterManager?.onCameraIdle()

        map?.cameraPosition?.let {
            viewModel.lastCameraLocation = UiCameraLocation(
                    UiLatLon(
                            it.target.latitude,
                            it.target.longitude),
                    it.zoom)
        }
    }

    /**
     * Handle a cluster marker item being clicked.
     *
     * @param cluster The cluster item which was clicked.
     * @return `true` to tell the callback invoker that the event was handled here.
     */
    private fun handleOnClusterClicked(cluster: Cluster<UiStopMarker>): Boolean {
        // Cheat here by invoking the camera animation directly. We avoid lots of needless
        // complication by doing this.
        map?.apply {
            // Move the camera to the cluster's location, and also zoom in by 1 unit.
            animateCamera(CameraUpdateFactory.newLatLngZoom(
                    cluster.position,
                    cameraPosition.zoom + 1f))
        }

        return true
    }

    /**
     * Handle a stop marker being clicked.
     *
     * @param stopMarker The stop marker which was clicked.
     * @return `true` to tell the callback invoker that the event was handled here.
     */
    private fun handleOnClusterItemClick(stopMarker: UiStopMarker): Boolean {
        // Cheat here by invoking the camera animation directly. We avoid lots of needless
        // complication by doing this.
        map?.animateCamera(CameraUpdateFactory.newLatLng(stopMarker.latLng))
        viewModel.onMapMarkerClicked(stopMarker)

        return true
    }

    /**
     * Handle the stop markers being changed, so that the map is updated with the new state.
     *
     * @param stopMarkers The new stop marker state.
     */
    private fun handleStopMarkersChanged(stopMarkers: List<UiStopMarker>?) {
        clusterManager?.apply {
            clearItems()

            stopMarkers?.let {
                addItems(stopMarkers)
            }

            cluster()

            handleShowMapMarkerInfoWindow(viewModel.showStopMarkerInfoWindowLiveData.value)
        }
    }

    /**
     * Handle the route lines being changed, so that the map is updated with the new state.
     *
     * @param routeLines The new route lines state.
     */
    private fun handleRouteLinesChanged(routeLines: List<UiServiceRoute>?) {
        routeLineManager?.submitRouteLines(routeLines)
    }

    /**
     * Handle whether the My Location layer should be shown on the map. This consists of the
     * device's location being drawn on the map, and the display of location UI controls.
     *
     * @param isEnabled `true` if the My Location layer has been enabled, otherwise `false`.
     */
    @SuppressLint("MissingPermission")
    private fun handleMyLocationEnabledChanged(isEnabled: Boolean) {
        map?.isMyLocationEnabled = isEnabled
    }

    /**
     * Handle the map type being changed.
     *
     * @param mapType The new map type.
     */
    private fun handleMapTypeChanged(mapType: MapType) {
        map?.mapType = toGoogleMapType(mapType)
    }

    /**
     * Handle the camera position being changed.
     *
     * @param cameraLocation The new [UiCameraLocation].
     */
    private fun handleCameraPositionChanged(cameraLocation: UiCameraLocation?) {
        map?.apply {
            cameraLocation?.let {
                CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                                it.latLon.latitude,
                                it.latLon.longitude),
                        it.zoomLevel)
                        .let(this::moveCamera)
            }
        }
    }

    /**
     * Handle a request to show the map marker bubble.
     *
     * @param stopCode The stop code to show the marker bubble for.
     */
    private fun handleShowMapMarkerInfoWindow(stopCode: String?) {
        val markers = clusterManager?.markerCollection?.markers ?: return

        stopCode?.let { sc ->
            markers.firstOrNull {
                (it.tag as? UiStopMarker)?.stopCode == sc
            }?.showInfoWindow()
        } ?: run {
            markers.forEach {
                if (it.isInfoWindowShown) it.hideInfoWindow()
            }
        }
    }

    /**
     * Show the search UI.
     */
    private fun showSearch() {
        searchStopLauncher.launch()
    }

    /**
     * Show the services chooser UI.
     *
     * @param params The parameters to start [ServicesChooserDialogFragment] with.
     */
    private fun showServicesChooser(params: ServicesChooserParams) {
        ServicesChooserDialogFragment.newInstance(
                params.services.toTypedArray(),
                params.selectedServices?.toTypedArray(),
                getString(R.string.busstopmapfragment_service_chooser_title))
                .show(childFragmentManager, DIALOG_SERVICES_CHOOSER)
    }

    /**
     * Handle the map type menu item being selected.
     */
    private fun showMapTypeSelection() {
        MapTypeBottomSheetDialogFragment.newInstance(toMapType())
                .show(childFragmentManager, DIALOG_MAP_TYPE_BOTTOM_SHEET)
    }

    /**
     * Handle the traffic view menu item being selected.
     */
    private fun handleTrafficViewEnabledChanged(isEnabled: Boolean) {
        map?.isTrafficEnabled = isEnabled
    }

    /**
     * Handle the zoom controls visibility changing.
     *
     * @param isVisible Should the zoom controls be visible?
     */
    private fun handleZoomControlsVisibilityChanged(isVisible: Boolean) {
        map?.uiSettings?.isZoomControlsEnabled = isVisible
    }

    /**
     * Handle a change in the enabled state of the services menu item.
     *
     * @param isEnabled Is the services menu item enabled?
     */
    private fun handleServicesMenuItemEnabledChanged(isEnabled: Boolean) {
        menuItemServices?.isEnabled = isEnabled
    }

    /**
     * Configure the traffic view menu item.
     */
    private fun handleTrafficViewMenuItemChanged(isTrafficViewEnabled: Boolean) {
        menuItemTrafficView?.apply {
            if (isTrafficViewEnabled) {
                R.string.map_menu_mapoverlay_trafficviewoff
            } else {
                R.string.map_menu_mapoverlay_trafficviewon
            }.let(this::setTitle)
        }
    }

    /**
     * Convert the current [GoogleMap.getMapType] in to the type understood by
     * [MapTypeBottomSheetDialogFragment].
     *
     * @return The current map type as understood by [MapTypeBottomSheetDialogFragment].
     */
    private fun toMapType() = when (map?.mapType) {
        GoogleMap.MAP_TYPE_SATELLITE -> MapType.SATELLITE
        GoogleMap.MAP_TYPE_HYBRID -> MapType.HYBRID
        else -> MapType.NORMAL
    }

    /**
     * Convert a [MapType] in to the type understood by [GoogleMap].
     *
     * @param mapType The [MapType].
     * @return The [GoogleMap] version of the map type.
     */
    private fun toGoogleMapType(mapType: MapType) = when (mapType) {
        MapType.SATELLITE -> GoogleMap.MAP_TYPE_SATELLITE
        MapType.HYBRID -> GoogleMap.MAP_TYPE_HYBRID
        else -> GoogleMap.MAP_TYPE_NORMAL
    }

    /**
     * Check Google Play Services to ensure it is working correctly on this device. If an error is
     * returned, then present error UI to the user.
     *
     * The error UI may contain a button which helps the user resolve the issue, if Googe Play
     * Services tells us the error can be resolved.
     */
    private fun checkGooglePlayServices() {
        val context = requireContext()
        val result = googleApiAvailability.isGooglePlayServicesAvailable(context)

        viewBinding.apply {
            if (result == ConnectionResult.SUCCESS) {
                layoutError.layoutError.visibility = View.GONE
                mapView.visibility = View.VISIBLE
                layoutError.btnErrorResolve.setOnClickListener(null)
            } else {
                mapView.visibility = View.GONE
                layoutError.layoutError.visibility = View.VISIBLE
                layoutError.txtError.txtError.setText(getPlayServicesErrorString(result))

                layoutError.btnErrorResolve.apply {
                    if (googleApiAvailability.isUserResolvableError(result)) {
                        visibility = View.VISIBLE
                        setOnClickListener {
                            val pendingIntent = googleApiAvailability
                                    .getErrorResolutionPendingIntent(context, result, 0)

                            try {
                                pendingIntent?.send() ?: showFailedToResolvePlayServicesToast()
                            } catch (ignored: PendingIntent.CanceledException) {
                                showFailedToResolvePlayServicesToast()
                            }
                        }
                    } else {
                        visibility = View.GONE
                        setOnClickListener(null)
                    }
                }
            }
        }
    }

    /**
     * When [GoogleApiAvailability.isGooglePlayServicesAvailable] returns a non-success code,
     * then pass the error code in to this method to obtain the error string resource to display to
     * the user.
     *
     * @param errorCode The error code returned from
     * [GoogleApiAvailability.isGooglePlayServicesAvailable]. Must not be
     * [ConnectionResult.SUCCESS].
     * @return A string resource ID for the error string to display to the user.
     */
    private fun getPlayServicesErrorString(errorCode: Int) = when (errorCode) {
        ConnectionResult.SERVICE_MISSING -> R.string.busstopmapfragment_error_play_services_missing
        ConnectionResult.SERVICE_UPDATING ->
            R.string.busstopmapfragment_error_play_services_updating
        ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED ->
            R.string.busstopmapfragment_error_play_services_update_required
        ConnectionResult.SERVICE_DISABLED ->
            R.string.busstopmapfragment_error_play_services_disabled
        ConnectionResult.SERVICE_INVALID -> R.string.busstopmapfragment_error_play_services_invalid
        else -> R.string.busstopmapfragment_error_play_services_unknown
    }

    /**
     * Show the user a [Toast] message which informs them that the resolution action could not be
     * performed to make Google Play Services work.
     */
    private fun showFailedToResolvePlayServicesToast() {
        Toast.makeText(requireContext(),
                R.string.busstopmapfragment_error_play_services_resolve_failed,
                Toast.LENGTH_SHORT)
                .show()
    }

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.busstopmap_option_menu, menu)

            menuItemServices = menu.findItem(R.id.busstopmap_option_menu_services)
            menuItemTrafficView = menu.findItem(R.id.busstopmap_option_menu_trafficview)
        }

        override fun onPrepareMenu(menu: Menu) {
            handleServicesMenuItemEnabledChanged(viewModel.isFilterEnabledLiveData.value ?: false)
            handleTrafficViewMenuItemChanged(viewModel.isTrafficViewEnabledLiveData.value ?: false)
        }

        override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
            R.id.busstopmap_option_menu_search -> {
                viewModel.onSearchMenuItemClicked()
                true
            }
            R.id.busstopmap_option_menu_services -> {
                viewModel.onServicesMenuItemClicked()
                true
            }
            R.id.busstopmap_option_menu_maptype -> {
                viewModel.onMapTypeMenuItemClicked()
                true
            }
            R.id.busstopmap_option_menu_trafficview -> {
                viewModel.onTrafficViewMenuItemClicked()
                true
            }
            else -> false
        }
    }

    /**
     * Any [Activities][Activity] which host this [androidx.fragment.app.Fragment] must
     * implement this interface to handle navigation events.
     */
    interface Callbacks : OnShowBusTimesListener
}