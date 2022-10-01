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
import android.app.Activity
import android.app.PendingIntent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import dagger.android.support.AndroidSupportInjection
import uk.org.rivernile.android.bustracker.repositories.busstopmap.SelectedStop
import uk.org.rivernile.android.bustracker.repositories.busstopmap.Stop
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowBusTimesListener
import uk.org.rivernile.android.bustracker.ui.search.SearchActivity
import uk.org.rivernile.android.bustracker.ui.serviceschooser.ServicesChooserDialogFragment
import uk.org.rivernile.android.utils.LocationUtils
import uk.org.rivernile.edinburghbustracker.android.R
import javax.inject.Inject

/**
 * This is a [Fragment] for display a map with stop marker icons and route lines.
 *
 * @author Niall Scott
 */
class BusStopMapFragment : Fragment(), OnMapReadyCallback,
        ServicesChooserDialogFragment.Callbacks,
        MapTypeBottomSheetDialogFragment.OnMapTypeSelectedListener,
        ClusterManager.OnClusterClickListener<Stop>,
        ClusterManager.OnClusterItemClickListener<Stop>,
        ClusterManager.OnClusterItemInfoWindowClickListener<Stop>,
        GoogleMap.OnInfoWindowCloseListener,
        StopClusterRenderer.OnItemRenderedListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var googleApiAvailability: GoogleApiAvailability

    private lateinit var callbacks: Callbacks
    private lateinit var viewModel: BusStopMapViewModel
    private var map: GoogleMap? = null
    private var clusterManager: ClusterManager<Stop>? = null
    private var stopClusterRenderer: StopClusterRenderer? = null
    private var routeLines: Map<String, List<Polyline>>? = null

    private var layoutError: View? = null
    private var txtError: TextView? = null
    private var btnErrorResolve: Button? = null
    private var mapView: MapView? = null

    private var menuItemServices: MenuItem? = null
    private var menuItemTrafficView: MenuItem? = null

    companion object {

        private const val ARG_STOPCODE = "stopCode"
        private const val ARG_LATITUDE = "latitude"
        private const val ARG_LONGITUDE = "longitude"

        private const val STATE_SELECTED_SERVICES = "selectedServices"
        private const val STATE_SELECTED_STOP_CODE = "selectedStopCode"

        private const val REQUEST_CODE_SEARCH = 100

        private const val DIALOG_SERVICES_CHOOSER = "dialogServicesChooser"
        private const val DIALOG_MAP_TYPE_BOTTOM_SHEET = "bottomSheetMapType"

        private const val PERMISSION_REQUEST_LOCATION = 1

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
        @JvmStatic
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
        @JvmStatic
        fun newInstance(latitude: Double, longitude: Double) = BusStopMapFragment().apply {
            arguments = Bundle().apply {
                putDouble(ARG_LATITUDE, latitude)
                putDouble(ARG_LONGITUDE, longitude)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        AndroidSupportInjection.inject(this)

        try {
            callbacks = context as Callbacks
        } catch (e: ClassCastException) {
            throw IllegalStateException("${context.javaClass.name} does not implement " +
                    Callbacks::class.java.name)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, viewModelFactory)
                .get(BusStopMapViewModel::class.java)

        viewModel.serviceNames.observe(this, Observer {
            configureServicesMenuItem()
        })
        viewModel.showStopDetails.observe(this, Observer(callbacks::onShowBusTimes))
        viewModel.showSearch.observe(this, Observer {
            showSearch()
        })
        viewModel.showServicesChooser.observe(this, Observer {
            showServicesChooser()
        })
        viewModel.showMapTypeSelection.observe(this, Observer {
            showMapTypeSelection()
        })
        viewModel.updateTrafficView.observe(this, Observer {
            handleTrafficViewMenuItemSelected()
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.busstopmap_fragment, container, false).apply {
            layoutError = findViewById(R.id.layoutError)
            txtError = findViewById(R.id.txtError)
            btnErrorResolve = findViewById(R.id.btnErrorResolve)
            mapView = findViewById(R.id.mapView)

            btnErrorResolve?.setText(R.string.busstopmapfragment_button_resolve)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        requireActivity().setTitle(R.string.map_title)

        savedInstanceState?.let(this::restoreState) ?: doFirstCreate()

        mapView?.let {
            it.onCreate(savedInstanceState)
            it.getMapAsync(this)
        }

        if (savedInstanceState == null) {
            if (!LocationUtils.checkLocationPermission(requireContext())) {
                requestLocationPermission()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        checkGooglePlayServices()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()

        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()

        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()

        mapView?.onStop()

        map?.let {
            val position = it.cameraPosition
            val latLng = position.target

            viewModel.onPersistMapParameters(latLng.latitude, latLng.longitude, position.zoom,
                    toMapType())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        map = null
        mapView?.onDestroy()
        mapView = null

        layoutError = null
        txtError = null
        btnErrorResolve = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        mapView?.onSaveInstanceState(outState)
        outState.putStringArray(STATE_SELECTED_SERVICES, viewModel.selectedServices)
        outState.putString(STATE_SELECTED_STOP_CODE, viewModel.showMapMarkerBubble.value?.stopCode)
    }

    override fun onLowMemory() {
        super.onLowMemory()

        mapView?.onLowMemory()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.busstopmap_option_menu, menu)

        menuItemServices = menu.findItem(R.id.busstopmap_option_menu_services)
        menuItemTrafficView = menu.findItem(R.id.busstopmap_option_menu_trafficview)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        configureServicesMenuItem()
        configureTrafficViewMenuItem()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
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
        else -> super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_SEARCH -> handleSearchActivityResult(resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            updateMyLocationFeature()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        this.map = map
        setHasOptionsMenu(true)

        map.uiSettings.apply {
            isMyLocationButtonEnabled = true
            isMapToolbarEnabled = false
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
        map.isMyLocationEnabled = LocationUtils.checkLocationPermission(context)
        this.clusterManager = clusterManager
        this.stopClusterRenderer = clusterRenderer

        viewModel.mapType.observe(this, Observer(this::handleMapTypeChanged))
        viewModel.cameraLocation.observe(this, Observer(this::handleCameraPositionChanged))
        viewModel.busStops.observe(this, Observer(this::handleStopsChanged))
        viewModel.routeLines.observe(this, Observer(this::handleRouteLinesChanged))
        viewModel.showMapMarkerBubble.observe(this, Observer(this::handleShowMapMarkerBubble))
    }

    override fun onServicesChosen(chosenServices: Array<String>?) {
        viewModel.onServicesChosen(chosenServices)
    }

    override fun onMapTypeSelected(@MapType mapType: Int) {
        viewModel.onMapTypeSelected(mapType)
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
        viewModel.onRestoreState(savedInstanceState.getStringArray(STATE_SELECTED_SERVICES),
                savedInstanceState.getString(STATE_SELECTED_STOP_CODE))
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
    private fun handleMapTypeChanged(mapType: Int?) {
        if (mapType != null) {
            map?.mapType = toGoogleMapType(mapType)
        }
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
        startActivityForResult(Intent(requireContext(), SearchActivity::class.java),
                REQUEST_CODE_SEARCH)
    }

    /**
     * Show the services chooser UI.
     */
    private fun showServicesChooser() {
        ServicesChooserDialogFragment.newInstance(viewModel.serviceNames.value,
                viewModel.selectedServices,
                getString(R.string.busstopmapfragment_service_chooser_title)).also {
            it.setTargetFragment(this, 0)
            it.show(parentFragmentManager, DIALOG_SERVICES_CHOOSER)
        }
    }

    /**
     * Handle the map type menu item being selected.
     */
    private fun showMapTypeSelection() {
        MapTypeBottomSheetDialogFragment.newInstance(toMapType()).also {
            it.setTargetFragment(this, 0)
            it.show(parentFragmentManager, DIALOG_MAP_TYPE_BOTTOM_SHEET)
        }
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
     * Handle a result being returned from a previous request to show the search UI.
     *
     * @param resultCode The result code of the operation.
     * @param intent The returned [Intent].
     */
    private fun handleSearchActivityResult(resultCode: Int, intent: Intent?) {
        if (resultCode == Activity.RESULT_OK && intent != null) {
            viewModel.onStopSearchResult(
                    intent.getStringExtra(SearchActivity.EXTRA_STOP_CODE)
                            ?: throw IllegalStateException())
        }
    }

    /**
     * Request the location permission from the user.
     */
    private fun requestLocationPermission() {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSION_REQUEST_LOCATION)
    }

    /**
     * Enable the 'My Location' layer if the user has enabled it and if the permission has been
     * granted.
     */
    private fun updateMyLocationFeature() {
        map?.isMyLocationEnabled = LocationUtils.checkLocationPermission(requireContext())
    }

    /**
     * Update the filter [MenuItem] to be enabled/disabled depending on the current state.
     */
    private fun configureServicesMenuItem() {
        menuItemServices?.apply {
            val serviceNames = viewModel.serviceNames.value
            isEnabled = serviceNames?.isNotEmpty() ?: false
        }
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
    @MapType
    private fun toMapType() = when (map?.mapType) {
        GoogleMap.MAP_TYPE_SATELLITE -> MapTypeBottomSheetDialogFragment.MAP_TYPE_SATELLITE
        GoogleMap.MAP_TYPE_HYBRID -> MapTypeBottomSheetDialogFragment.MAP_TYPE_HYBRID
        else -> MapTypeBottomSheetDialogFragment.MAP_TYPE_NORMAL
    }

    /**
     * Convert the [MapTypeBottomSheetDialogFragment] map type in to the type understood by
     * [GoogleMap].
     *
     * @param mapType The map type returned by [MapTypeBottomSheetDialogFragment].
     * @return The [GoogleMap] version of the map type.
     */
    private fun toGoogleMapType(@MapType mapType: Int) = when (mapType) {
        MapTypeBottomSheetDialogFragment.MAP_TYPE_NORMAL -> GoogleMap.MAP_TYPE_NORMAL
        MapTypeBottomSheetDialogFragment.MAP_TYPE_SATELLITE -> GoogleMap.MAP_TYPE_SATELLITE
        MapTypeBottomSheetDialogFragment.MAP_TYPE_HYBRID -> GoogleMap.MAP_TYPE_HYBRID
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

        if (result == ConnectionResult.SUCCESS) {
            layoutError?.visibility = View.GONE
            mapView?.visibility = View.VISIBLE
            btnErrorResolve?.setOnClickListener(null)
        } else {
            mapView?.visibility = View.GONE
            layoutError?.visibility = View.VISIBLE
            txtError?.setText(getPlayServicesErrorString(result))

            btnErrorResolve?.apply {
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

    /**
     * Any [Activities][Activity] which host this [androidx.fragment.app.Fragment] must
     * implement this interface to handle navigation events.
     */
    interface Callbacks : OnShowBusTimesListener
}