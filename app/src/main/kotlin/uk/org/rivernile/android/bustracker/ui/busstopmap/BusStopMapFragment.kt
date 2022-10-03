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
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import dagger.android.support.AndroidSupportInjection
import uk.org.rivernile.android.bustracker.core.bundle.getSerializableCompat
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.repositories.busstopmap.SelectedStop
import uk.org.rivernile.android.bustracker.repositories.busstopmap.Stop
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
class BusStopMapFragment : Fragment(), OnMapReadyCallback,
        ClusterManager.OnClusterClickListener<Stop>,
        ClusterManager.OnClusterItemClickListener<Stop>,
        ClusterManager.OnClusterItemInfoWindowClickListener<Stop>,
        GoogleMap.OnInfoWindowCloseListener,
        StopClusterRenderer.OnItemRenderedListener {

    companion object {

        private const val ARG_STOPCODE = "stopCode"
        private const val ARG_LATITUDE = "latitude"
        private const val ARG_LONGITUDE = "longitude"

        private const val STATE_SELECTED_STOP_CODE = "selectedStopCode"

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
            arguments = Bundle().apply {
                putString(ARG_STOPCODE, stopCode)
            }
        }

        /**
         * Create a new instance of [BusStopMapFragment], specifying the initial latitude/longitude
         * camera location.
         *
         * @param latitude The initial camera latitude.
         * @param longitude The initial camera longitude.
         * @return A new instance of [BusStopMapFragment].
         */
        fun newInstance(latitude: Double, longitude: Double) = BusStopMapFragment().apply {
            arguments = Bundle().apply {
                putDouble(ARG_LATITUDE, latitude)
                putDouble(ARG_LONGITUDE, longitude)
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: BusStopMapFragmentViewModelFactory
    @Inject
    lateinit var googleApiAvailability: GoogleApiAvailability

    private val viewModel: BusStopMapViewModel by viewModels {
        GenericSavedStateViewModelFactory(viewModelFactory, this)
    }

    private lateinit var callbacks: Callbacks
    private var map: GoogleMap? = null
    private var clusterManager: ClusterManager<Stop>? = null
    private var stopClusterRenderer: StopClusterRenderer? = null
    private var routeLines: Map<String, List<Polyline>>? = null

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

        requireActivity().setTitle(R.string.map_title)

        savedInstanceState?.let(this::restoreState) ?: doFirstCreate()

        viewBinding.mapView.apply {
            onCreate(savedInstanceState)
            getMapAsync(this@BusStopMapFragment)
        }

        viewBinding.layoutError.btnErrorResolve.setText(R.string.busstopmapfragment_button_resolve)

        val viewLifecycleOwner = viewLifecycleOwner

        viewModel.requestLocationPermissionsLiveData.observe(viewLifecycleOwner) {
            handleRequestLocationPermissions()
        }
        viewModel.isMyLocationFeatureEnabledLiveData.observe(viewLifecycleOwner,
                this::handleMyLocationEnabledChanged)
        viewModel.isFilterEnabledLiveData.observe(viewLifecycleOwner,
                this::handleServicesMenuItemEnabledChanged)
        viewModel.showServicesChooserLiveData.observe(viewLifecycleOwner, this::showServicesChooser)

        viewModel.showStopDetails.observe(viewLifecycleOwner, callbacks::onShowBusTimes)
        viewModel.showSearch.observe(viewLifecycleOwner) {
            showSearch()
        }
        viewModel.showMapTypeSelection.observe(viewLifecycleOwner) {
            showMapTypeSelection()
        }
        viewModel.updateTrafficView.observe(viewLifecycleOwner) {
            handleTrafficViewMenuItemSelected()
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

        map?.let {
            val position = it.cameraPosition
            val latLng = position.target

            viewModel.onPersistMapParameters(latLng.latitude, latLng.longitude, position.zoom,
                    toMapType())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewBinding.mapView.onDestroy()
        map = null
        _viewBinding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        viewBinding.mapView.onSaveInstanceState(outState)
        outState.putString(STATE_SELECTED_STOP_CODE, viewModel.showMapMarkerBubble.value?.stopCode)
    }

    override fun onLowMemory() {
        super.onLowMemory()

        viewBinding.mapView.onLowMemory()
    }

    override fun onMapReady(map: GoogleMap) {
        this.map = map
        val viewLifecycleOwner = viewLifecycleOwner
        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner)

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

        map.uiSettings.apply {
            isMyLocationButtonEnabled = true
            isZoomControlsEnabled = viewModel.shouldShowZoomControls
        }

        val context = requireContext()
        val clusterManager = ClusterManager<Stop>(context, map)
        val clusterRenderer = StopClusterRenderer(context, map, clusterManager, this, viewModel)
        clusterManager.renderer = clusterRenderer

        clusterManager.setOnClusterClickListener(this)
        clusterManager.setOnClusterItemClickListener(this)
        clusterManager.setOnClusterItemInfoWindowClickListener(this)

        map.setOnCameraIdleListener(clusterManager)
        map.setOnMarkerClickListener(clusterManager)
        map.setOnInfoWindowClickListener(clusterManager)
        map.setOnInfoWindowCloseListener(this)
        map.setInfoWindowAdapter(MapInfoWindow(requireActivity(), view as ViewGroup))
        handleMyLocationEnabledChanged(viewModel.isMyLocationFeatureEnabledLiveData.value ?: false)
        this.clusterManager = clusterManager
        this.stopClusterRenderer = clusterRenderer

        viewModel.mapType.observe(viewLifecycleOwner, this::handleMapTypeChanged)
        viewModel.cameraLocation.observe(viewLifecycleOwner, this::handleCameraPositionChanged)
        viewModel.busStops.observe(viewLifecycleOwner, this::handleStopsChanged)
        viewModel.routeLines.observe(viewLifecycleOwner, this::handleRouteLinesChanged)
        viewModel.showMapMarkerBubble.observe(viewLifecycleOwner, this::handleShowMapMarkerBubble)
    }

    override fun onClusterClick(cluster: Cluster<Stop>): Boolean {
        map?.let {
            val position = cluster.position
            val latitude = position.latitude
            val longitude = position.longitude
            val currentZoom = it.cameraPosition.zoom
            viewModel.onClusterMarkerClicked(latitude, longitude, currentZoom)
        }

        return true
    }

    override fun onClusterItemClick(stop: Stop): Boolean {
        viewModel.onMapMarkerClicked(stop)

        return true
    }

    override fun onClusterItemInfoWindowClick(stop: Stop) {
        viewModel.onMarkerBubbleClicked(stop)
    }

    override fun onInfoWindowClose(marker: Marker) {
        (marker.tag as? String)?.let {
            viewModel.onMapMarkerBubbleClosed(it)
        }
    }

    override fun onItemRendered(marker: Marker) {
        val stopCode = marker.tag as? String
        val selectedStop = viewModel.showMapMarkerBubble.value

        if (stopCode != null && stopCode == selectedStop?.stopCode) {
            marker.snippet = selectedStop.serviceListing
            marker.showInfoWindow()
        }
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
     * @param latitude The new latitude.
     * @param longitude The new longitude.
     */
    fun onRequestCameraLocation(latitude: Double, longitude: Double) {
        viewModel.onRequestCameraLocation(latitude, longitude)
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
     * Do what is required on the first create of this [Fragment]. This occurs when the saved
     * instance state is `null`.
     */
    private fun doFirstCreate() {
        arguments?.let { args ->
            when {
                args.containsKey(ARG_STOPCODE) -> {
                    args.getString(ARG_STOPCODE)?.let {
                        viewModel.onFirstCreate(it)
                    }
                }
                args.containsKey(ARG_LATITUDE) && args.containsKey(ARG_LONGITUDE) -> {
                    val latitude = args.getDouble(ARG_LATITUDE, 0.0)
                    val longitude = args.getDouble(ARG_LONGITUDE, 0.0)
                    viewModel.onFirstCreate(latitude, longitude)
                }
                else -> viewModel.onFirstCreate()
            }
        } ?: viewModel.onFirstCreate()
    }

    /**
     * Restore saved instance state from a given [Bundle].
     *
     * @param savedInstanceState Previously saved state.
     */
    private fun restoreState(savedInstanceState: Bundle) {
        viewModel.onRestoreState(savedInstanceState.getString(STATE_SELECTED_STOP_CODE))
    }

    /**
     * Handle the collection of stop markers changing.
     *
     * @param stops The new stops collection to display.
     */
    private fun handleStopsChanged(stops: Map<String, Stop>?) {
        clusterManager?.apply {
            clearItems()

            stops?.let {
                addItems(it.values)
            }

            cluster()
        }
    }

    /**
     * Handle the collection of route lines changing.
     *
     * @param routeLines The new route lines collection to display.
     */
    private fun handleRouteLinesChanged(routeLines: Map<String, List<PolylineOptions>>?) {
        this.routeLines?.forEach { (_, polyLines) ->
            polyLines.forEach(Polyline::remove)
        }

        map?.let { map ->
            this.routeLines = routeLines?.mapValues {
                it.value.map(map::addPolyline)
            }
        } ?: run {
            this.routeLines = null
        }
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
     * @param cameraLocation The new [CameraLocation].
     */
    private fun handleCameraPositionChanged(cameraLocation: CameraLocation?) {
        if (cameraLocation != null) {
            map?.let {
                val latLng = LatLng(cameraLocation.latitude, cameraLocation.longitude)
                val zoom = cameraLocation.zoomLevel

                val cameraUpdate = if (zoom != null) {
                    CameraUpdateFactory.newLatLngZoom(latLng, zoom)
                } else {
                    CameraUpdateFactory.newLatLng(latLng)
                }

                if (cameraLocation.animate) {
                    it.animateCamera(cameraUpdate)
                } else {
                    it.moveCamera(cameraUpdate)
                }
            }
        }
    }

    /**
     * Handle a request to show the map marker bubble.
     *
     * @param stop The stop to show the map marker bubble for.
     */
    private fun handleShowMapMarkerBubble(stop: SelectedStop?) {
        stop?.let {
            val stopData = viewModel.busStops.value?.get(it.stopCode)

            if (stopData != null) {
                val marker = stopClusterRenderer?.getMarker(stopData)

                if (marker != null) {
                    marker.snippet = stop.serviceListing
                    marker.showInfoWindow()
                }
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
    private fun handleTrafficViewMenuItemSelected() {
        map?.apply {
            isTrafficEnabled = !isTrafficEnabled
            configureTrafficViewMenuItem()
        }
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
    private fun configureTrafficViewMenuItem() {
        menuItemTrafficView?.apply {
            map?.let {
                isEnabled = true

                val titleRes = if (it.isTrafficEnabled) {
                    R.string.map_menu_mapoverlay_trafficviewoff
                } else {
                    R.string.map_menu_mapoverlay_trafficviewon
                }

                setTitle(titleRes)
            } ?: run {
                isEnabled = false
            }
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
            configureTrafficViewMenuItem()
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