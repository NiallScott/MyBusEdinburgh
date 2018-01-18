/*
 * Copyright (C) 2017 - 2018 Niall 'Rivernile' Scott
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
 */

package uk.org.rivernile.android.bustracker.ui.busstopmap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;

import uk.org.rivernile.android.bustracker.BusApplication;
import uk.org.rivernile.android.bustracker.database.busstop.loaders.AllServiceNamesLoader;
import uk.org.rivernile.android.bustracker.preferences.PreferenceManager;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowBusTimesListener;
import uk.org.rivernile.android.bustracker.ui.search.SearchActivity;
import uk.org.rivernile.android.bustracker.ui.serviceschooser.ServicesChooserDialogFragment;
import uk.org.rivernile.android.utils.LocationUtils;
import uk.org.rivernile.android.utils.ProcessedCursorLoader;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * The purpose of this {@link Fragment} is to show a map with bus stops and bus routes on it.
 *
 * @author Niall Scott
 */
public class BusStopMapFragment extends Fragment implements LoaderManager.LoaderCallbacks,
        OnMapReadyCallback, ServicesChooserDialogFragment.Callbacks,
        MapTypeBottomSheetDialogFragment.OnMapTypeSelectedListener,
        ClusterManager.OnClusterClickListener<Stop>,
        ClusterManager.OnClusterItemClickListener<Stop>,
        ClusterManager.OnClusterItemInfoWindowClickListener<Stop>,
        GetStopServicesTask.OnStopServicesLoadedListener {

    private static final String ARG_STOPCODE = "stopCode";
    private static final String ARG_LATITUDE = "latitude";
    private static final String ARG_LONGITUDE = "longitude";

    private static final String STATE_SELECTED_SERVICES = "selectedServices";

    private static final String DIALOG_SERVICES_CHOOSER = "dialogServicesChooser";
    private static final String DIALOG_MAP_TYPE_BOTTOM_SHEET = "bottomSheetMapType";

    private static final int PERMISSION_REQUEST_LOCATION = 1;

    private static final int REQUEST_CODE_SEARCH = 100;

    private static final int LOADER_SERVICES = 1;
    private static final int LOADER_STOPS = 2;

    private Callbacks callbacks;
    private PreferenceManager preferenceManager;

    private GoogleMap map;
    private ClusterManager<Stop> clusterManager;
    private StopClusterRenderer clusterRenderer;
    private GetStopServicesTask getStopServicesTask;

    private String[] services;
    private String[] selectedServices;
    private Marker currentSelectedMarker;

    private MapView mapView;

    private MenuItem menuItemServices;
    private MenuItem menuItemTrafficView;

    /**
     * Create a new instance of {@code BusStopMapFragment}.
     *
     * @return A new instance of {@code BusStopMapFragment}.
     */
    @NonNull
    public static BusStopMapFragment newInstance() {
        return new BusStopMapFragment();
    }

    /**
     * Create a new instance of {@code BusStopMapFragment}, setting the initial location to that
     * of the {@code stopCode} provided.
     *
     * @param stopCode The stop code to go to.
     * @return A new instance of {@code BusStopMapFragment}.
     */
    @NonNull
    public static BusStopMapFragment newInstance(@NonNull final String stopCode) {
        final BusStopMapFragment fragment = new BusStopMapFragment();
        final Bundle b = new Bundle();
        b.putString(ARG_STOPCODE, stopCode);
        fragment.setArguments(b);

        return fragment;
    }

    /**
     * Create a new instance of {@code BusStopMapFragment}, setting the initial location
     * specified by {@code latitude} and {@code longitude}.
     *
     * @param latitude The latitude to go to.
     * @param longitude The longitude to go to.
     * @return A new instance of {@code BusStopMapFragment}.
     */
    @NonNull
    public static BusStopMapFragment newInstance(final double latitude, final double longitude) {
        final BusStopMapFragment fragment = new BusStopMapFragment();
        final Bundle b = new Bundle();
        b.putDouble(ARG_LATITUDE, latitude);
        b.putDouble(ARG_LONGITUDE, longitude);
        fragment.setArguments(b);

        return fragment;
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);

        try {
            callbacks = (Callbacks) context;
        } catch (ClassCastException e) {
            throw new IllegalStateException(context.getClass().getName() + " does not implement " +
                    Callbacks.class.getName());
        }
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferenceManager = ((BusApplication) getContext().getApplicationContext())
                .getPreferenceManager();

        if (savedInstanceState != null) {
            selectedServices = savedInstanceState.getStringArray(STATE_SELECTED_SERVICES);
        }
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.busstopmap_fragment, container, false);
        mapView = (MapView) v.findViewById(R.id.mapView);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        startServicesLoader();

        if (savedInstanceState == null) {
            if (preferenceManager.isMapLocationShownAutomatically() &&
                    !LocationUtils.checkLocationPermission(getContext())) {
                requestLocationPermission();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mapView.onStart();

        if (map != null) {
            updateMyLocationFeature();
            map.getUiSettings().setZoomControlsEnabled(preferenceManager.isMapZoomButtonsShown());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        mapView.onStop();

        if (map != null) {
            // Save the camera location to SharedPreferences, so the user is shown this location
            // when they load the map again.
            final CameraPosition position = map.getCameraPosition();
            final LatLng latLng = position.target;

            preferenceManager.setLastMapLatitude(latLng.latitude);
            preferenceManager.setLastMapLongitude(latLng.longitude);
            preferenceManager.setLastMapZoomLevel(position.zoom);
            preferenceManager.setLastMapType(map.getMapType());
        }

        if (getStopServicesTask != null) {
            getStopServicesTask.cancel(false);
            getStopServicesTask = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        mapView.onSaveInstanceState(outState);
        outState.putStringArray(STATE_SELECTED_SERVICES, selectedServices);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        mapView.onLowMemory();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.busstopmap2_option_menu, menu);

        menuItemServices = menu.findItem(R.id.busstopmap_option_menu_services);
        menuItemTrafficView = menu.findItem(R.id.busstopmap_option_menu_trafficview);
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);

        configureServicesMenuItem();
        configureTrafficViewMenuItem();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.busstopmap_option_menu_search:
                handleSearchMenuItemSelected();
                return true;
            case R.id.busstopmap_option_menu_services:
                handleServicesSearchMenuItemSelected();
                return true;
            case R.id.busstopmap_option_menu_maptype:
                handleMapTypeMenuItemSelected();
                return true;
            case R.id.busstopmap_option_menu_trafficview:
                handleTrafficViewMenuItemSelected();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_SEARCH:
                handleSearchActivityResult(resultCode, data);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
            @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            updateMyLocationFeature();
        }
    }

    @Override
    public Loader onCreateLoader(final int id, final Bundle args) {
        switch (id) {
            case LOADER_SERVICES:
                return new AllServiceNamesLoader(getContext());
            case LOADER_STOPS:
                return new StopMarkerLoader(getContext(), selectedServices);
            default:
                return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onLoadFinished(final Loader loader, final Object data) {
        switch (loader.getId()) {
            case LOADER_SERVICES:
                handleServicesLoaded(
                        ((ProcessedCursorLoader.ResultWrapper<String[]>) data).getResult());
                break;
            case LOADER_STOPS:
                handleBusStopsLoaded(((ProcessedCursorLoader.ResultWrapper<List<Stop>>)
                        data).getResult());
                break;
        }
    }

    @Override
    public void onLoaderReset(final Loader loader) {
        // Nothing to do here.
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        map = googleMap;
        setUpMap();
    }

    @Override
    public void onServicesChosen(final String[] chosenServices) {
        selectedServices = chosenServices;
        startStopsLoader(true);
    }

    @Override
    public void onMapTypeSelected(@MapTypeBottomSheetDialogFragment.MapType final int mapType) {
        setMapType(toGoogleMapType(mapType));
    }

    @Override
    public boolean onClusterClick(final Cluster<Stop> cluster) {
        final float currentZoom = map.getCameraPosition().zoom;
        moveCameraToLocation(cluster.getPosition(), currentZoom + 1f, true);

        return true;
    }

    @Override
    public boolean onClusterItemClick(final Stop stop) {
        if (getStopServicesTask != null) {
            getStopServicesTask.cancel(false);
            getStopServicesTask = null;
        }

        final Marker marker = clusterRenderer.getMarker(stop);

        if (marker != null) {
            currentSelectedMarker = marker;
            final String stopCode = (String) marker.getTag();

            if (stopCode != null) {
                getStopServicesTask = new GetStopServicesTask(getContext(), this);
                getStopServicesTask.execute(stopCode);
            }
        }

        return true;
    }

    @Override
    public void onClusterItemInfoWindowClick(final Stop stop) {
        callbacks.onShowBusTimes(stop.getStopCode());
    }

    @Override
    public void onStopServicesLoaded(@Nullable final String services) {
        if (currentSelectedMarker != null) {
            currentSelectedMarker.setSnippet(services);
            currentSelectedMarker.showInfoWindow();
            final float zoomLevel = map.getCameraPosition().zoom;
            moveCameraToLocation(currentSelectedMarker.getPosition(), zoomLevel, true);
        }

        getStopServicesTask = null;
    }

    /**
     * Start the services loader to get all known services. This is used for service filtering.
     */
    private void startServicesLoader() {
        getLoaderManager().initLoader(LOADER_SERVICES, null, this);
    }

    /**
     * Start the stops loader to populate the stops on the map.
     *
     * @param force {@code true} if the loader should be re-created, {@code false} if not.
     */
    private void startStopsLoader(final boolean force) {
        if (force) {
            getLoaderManager().restartLoader(LOADER_STOPS, null, this);
        } else {
            getLoaderManager().initLoader(LOADER_STOPS, null, this);
        }
    }

    /**
     * Perform setup on the map.
     */
    private void setUpMap() {
        setHasOptionsMenu(true);

        final UiSettings uiSettings = map.getUiSettings();
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setMapToolbarEnabled(false);
        uiSettings.setZoomControlsEnabled(preferenceManager.isMapZoomButtonsShown());

        map.setMapType(preferenceManager.getLastMapType());
        updateMyLocationFeature();

        final Context context = getContext();
        clusterManager = new ClusterManager<>(context, map);
        clusterRenderer = new StopClusterRenderer(context, map, clusterManager);
        clusterManager.setRenderer(clusterRenderer);
        clusterManager.setOnClusterClickListener(this);
        clusterManager.setOnClusterItemClickListener(this);
        clusterManager.setOnClusterItemInfoWindowClickListener(this);
        map.setOnCameraIdleListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);
        map.setOnInfoWindowClickListener(clusterManager);
        map.setInfoWindowAdapter(new MapInfoWindow(getActivity(), (ViewGroup) getView()));
        startStopsLoader(false);
        moveCameraToInitialLocation();
    }

    /**
     * Handle a load of the service listing.
     *
     * @param services All known services in the stop database.
     */
    private void handleServicesLoaded(@Nullable final String[] services) {
        this.services = services;
        configureServicesMenuItem();
    }

    /**
     * Handle the stops load completing.
     *
     * @param stops The {@link List} of stops to show on the map.
     */
    private void handleBusStopsLoaded(@Nullable final List<Stop> stops) {
        clusterManager.clearItems();
        clusterManager.addItems(stops);
        clusterManager.cluster();
    }

    /**
     * Move the camera of the map to its initial location.
     */
    private void moveCameraToInitialLocation() {
        final double latitude = preferenceManager.getLastMapLatitude();
        final double longitude = preferenceManager.getLastMapLongitude();
        final float zoom = preferenceManager.getLastMapZoomLevel();

        moveCameraToLocation(latitude, longitude, zoom, false);
    }

    /**
     * Move the camera to a given latitude and longitude.
     *
     * @param latitude The latitude to move to.
     * @param longitude The longitude to move to.
     * @param zoomLevel The zoom level of the camera.
     * @param animate Whether the transition should be animated or not.
     */
    private void moveCameraToLocation(final double latitude, final double longitude,
            final float zoomLevel, final boolean animate) {
        moveCameraToLocation(new LatLng(latitude, longitude), zoomLevel, animate);
    }

    /**
     * Move the camera to a given {@link LatLng} location.
     *
     * @param location Where to move the camera to.
     * @param zoomLevel The zoom level of the camera.
     * @param animate Whether the transition should be animated or not.
     */
    private void moveCameraToLocation(@NonNull final LatLng location, final float zoomLevel,
            final boolean animate) {
        final CameraUpdate update = CameraUpdateFactory.newLatLngZoom(location, zoomLevel);

        if (animate) {
            map.animateCamera(update);
        } else {
            map.moveCamera(update);
        }
    }

    /**
     * Set the {@link GoogleMap} type.
     *
     * @param mapType The map type to set.
     */
    private void setMapType(final int mapType) {
        if (map != null) {
            map.setMapType(mapType);
        }
    }

    /**
     * Handle the search menu item being selected.
     */
    private void handleSearchMenuItemSelected() {
        startActivityForResult(new Intent(getContext(), SearchActivity.class), REQUEST_CODE_SEARCH);
    }

    /**
     * Handle the services menu item being selected.
     */
    private void handleServicesSearchMenuItemSelected() {
        final ServicesChooserDialogFragment dialog =
                ServicesChooserDialogFragment.newInstance(services, selectedServices,
                        getString(R.string.busstopmapfragment_service_chooser_title));
        dialog.setTargetFragment(this, 0);
        dialog.show(getFragmentManager(), DIALOG_SERVICES_CHOOSER);
    }

    /**
     * Handle the map type menu item being selected.
     */
    private void handleMapTypeMenuItemSelected() {
        final MapTypeBottomSheetDialogFragment bottomSheet =
                MapTypeBottomSheetDialogFragment.newInstance(toMapType());
        bottomSheet.setTargetFragment(this, 0);
        bottomSheet.show(getFragmentManager(), DIALOG_MAP_TYPE_BOTTOM_SHEET);
    }

    /**
     * Handle the traffic view menu item being selected.
     */
    private void handleTrafficViewMenuItemSelected() {
        if (map != null) {
            map.setTrafficEnabled(!map.isTrafficEnabled());
            configureTrafficViewMenuItem();
        }
    }

    /**
     * Handle the result coming back from the {@link SearchActivity}.
     *
     * @param resultCode The result code of the operation.
     */
    private void handleSearchActivityResult(final int resultCode, @NonNull final Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            // TODO: pan to the chosen bus stop.
        }
    }

    /**
     * Request the location permission from the user.
     */
    private void requestLocationPermission() {
        requestPermissions(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, PERMISSION_REQUEST_LOCATION);
    }

    /**
     * Enable the 'My Location' layer if the user has enabled it and if the permission has been
     * granted.
     */
    private void updateMyLocationFeature() {
        if (map != null & LocationUtils.checkLocationPermission(getContext())) {
            map.setMyLocationEnabled(preferenceManager.isMapLocationShownAutomatically());
        }
    }

    /**
     * Update the filter {@link MenuItem} to be enabled/disabled depending on the current state.
     */
    private void configureServicesMenuItem() {
        if (menuItemServices != null) {
            menuItemServices.setEnabled(services != null && services.length > 0);
        }
    }

    /**
     * Configure the traffic view menu item.
     */
    private void configureTrafficViewMenuItem() {
        if (menuItemTrafficView != null) {
            if (map != null) {
                menuItemTrafficView.setEnabled(true);

                if (map.isTrafficEnabled()) {
                    menuItemTrafficView.setTitle(R.string.map_menu_mapoverlay_trafficviewoff);
                } else {
                    menuItemTrafficView.setTitle(R.string.map_menu_mapoverlay_trafficviewon);
                }
            } else {
                menuItemTrafficView.setEnabled(false);
            }
        }
    }

    /**
     * Convert the current {@link GoogleMap#getMapType()} in to the type understood by
     * {@link MapTypeBottomSheetDialogFragment}.
     *
     * @return The current map type as understood by {@link MapTypeBottomSheetDialogFragment}.
     */
    @MapTypeBottomSheetDialogFragment.MapType
    private int toMapType() {
        if (map != null) {
            switch (map.getMapType()) {
                case GoogleMap.MAP_TYPE_SATELLITE:
                    return MapTypeBottomSheetDialogFragment.MAP_TYPE_SATELLITE;
                case GoogleMap.MAP_TYPE_HYBRID:
                    return MapTypeBottomSheetDialogFragment.MAP_TYPE_HYBRID;
                default:
                    break;
            }
        }

        return MapTypeBottomSheetDialogFragment.MAP_TYPE_NORMAL;
    }

    /**
     * Convert the {@link MapTypeBottomSheetDialogFragment} map type in to the type understood by
     * {@link GoogleMap}.
     *
     * @param mapType The map type returned by {@link MapTypeBottomSheetDialogFragment}.
     * @return The {@link GoogleMap} version of the map type.
     */
    private int toGoogleMapType(@MapTypeBottomSheetDialogFragment.MapType final int mapType) {
        switch (mapType) {
            case MapTypeBottomSheetDialogFragment.MAP_TYPE_NORMAL:
                return GoogleMap.MAP_TYPE_NORMAL;
            case MapTypeBottomSheetDialogFragment.MAP_TYPE_SATELLITE:
                return GoogleMap.MAP_TYPE_SATELLITE;
            case MapTypeBottomSheetDialogFragment.MAP_TYPE_HYBRID:
                return GoogleMap.MAP_TYPE_HYBRID;
            default:
                return GoogleMap.MAP_TYPE_NORMAL;
        }
    }

    /**
     * Any {@link Activity Activities} which host this {@link android.support.v4.app.Fragment} must
     * implement this interface to handle navigation events.
     */
    public interface Callbacks extends OnShowBusTimesListener {

        // No other methods to define.
    }
}
