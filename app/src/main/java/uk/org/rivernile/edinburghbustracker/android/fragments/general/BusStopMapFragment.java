/*
 * Copyright (C) 2012 - 2017 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android.fragments.general;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.org.rivernile.android.bustracker.BusApplication;
import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract;
import uk.org.rivernile.android.bustracker.database.busstop.loaders.AllServiceNamesLoader;
import uk.org.rivernile.android.bustracker.database.busstop.loaders.BusStopLoader;
import uk.org.rivernile.android.bustracker.database.busstop.loaders.BusStopServicesLoader;
import uk.org.rivernile.android.bustracker.preferences.PreferenceManager;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowServicesChooserListener;
import uk.org.rivernile.android.utils.ProcessedCursorLoader;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs.MapTypeChooserDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs.ServicesChooserDialogFragment;
import uk.org.rivernile.android.bustracker.database.busstop.loaders.BusStopMarkerLoader;
import uk.org.rivernile.edinburghbustracker.android.maps.GeoSearchLoader;
import uk.org.rivernile.edinburghbustracker.android.maps.MapInfoWindow;
import uk.org.rivernile.android.bustracker.database.busstop.loaders.RouteLineLoader;

/**
 * The {@code BusStopMapFragment} shows a Google Maps v2 {@link com.google.android.gms.maps.MapView}
 * and depending on the location of the camera, the zoom level and service filter, it shows bus stop
 * icons on the map. The user can tap on a bus stop icon to show the info window (bubble). If the
 * user taps on the info window, then the BusStopDetailsFragment is shown.
 *
 * <p>
 *     The user can also select the type of map they wish to see and search for bus stops and
 *     places.
 * </p>
 * 
 * @author Niall Scott
 */
public class BusStopMapFragment extends SupportMapFragment
        implements OnMapReadyCallback, GoogleMap.OnCameraChangeListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnInfoWindowCloseListener, LoaderManager.LoaderCallbacks,
        ServicesChooserDialogFragment.Callbacks, MapTypeChooserDialogFragment.Callbacks {
    
    /** The stopCode argument. */
    public static final String ARG_STOPCODE = "stopCode";
    /** The latitude argument. */
    public static final String ARG_LATITUDE = "latitude";
    /** The longitude argument. */
    public static final String ARG_LONGITUDE = "longitude";
    /** The search argument. */
    public static final String ARG_SEARCH = "searchTerm";
    
    private static final String STATE_CHOSEN_SERVICES = "chosenServices";
    private static final String STATE_CHOSEN_STOP = "chosenStop";

    /** The default search zoom. */
    public static final float DEFAULT_SEARCH_ZOOM =  16f;

    private static final Pattern STOP_CODE_SEARCH_PATTERN = Pattern.compile("^\\d{8}$");
    
    private static final String LOADER_ARG_MIN_X = "minX";
    private static final String LOADER_ARG_MIN_Y = "minY";
    private static final String LOADER_ARG_MAX_X = "maxX";
    private static final String LOADER_ARG_MAX_Y = "maxY";
    private static final String LOADER_ARG_ZOOM = "zoom";
    private static final String LOADER_ARG_FILTERED_SERVICES = "filteredServices";
    private static final String LOADER_ARG_QUERY = "query";
    
    private static final int LOADER_ID_BUS_STOPS = 0;
    private static final int LOADER_ID_GEO_SEARCH = 1;
    private static final int LOADER_ID_ROUTE_LINES = 2;
    private static final int LOADER_ID_SERVICES = 3;
    private static final int LOADER_ID_BUS_STOP_COORDS = 4;
    private static final int LOADER_ID_BUS_STOP_SERVICES = 5;

    private static final int PERMISSION_REQUEST_LOCATION = 1;
    
    private Callbacks callbacks;
    private GoogleMap map;
    private PreferenceManager preferenceManager;
    private SearchManager searchMan;
    
    private final HashMap<String, Marker> busStopMarkers = new HashMap<>();
    private final HashMap<String, LinkedList<Polyline>> routeLines = new HashMap<>();
    private final HashSet<Marker> geoSearchMarkers = new HashSet<>();
    private String searchedBusStop = null;
    private String[] services;
    private String[] chosenServices;
    private int actionBarHeight;

    private SearchView searchView;
    private MenuItem menuItemServices;
    private MenuItem menuItemSearch;
    private MenuItem menuItemTrafficView;
    
    /**
     * Create a new instance of the {@code BusStopMapFragment}, setting the initial location to that
     * of the {@code stopCode} provided.
     * 
     * @param stopCode The stopCode to go to.
     * @return A new instance of this {@link android.support.v4.app.Fragment}.
     */
    public static BusStopMapFragment newInstance(final String stopCode) {
        final BusStopMapFragment f = new BusStopMapFragment();
        final Bundle b = new Bundle();
        b.putString(ARG_STOPCODE, stopCode);
        f.setArguments(b);
        
        return f;
    }
    
    /**
     * Create a new instance of the {@code BusStopMapFragment}, setting the initial location
     * specified by {@code latitude} and {@code longitude}.
     * 
     * @param latitude The latitude to go to.
     * @param longitude The longitude to go to.
     * @return A new instance of this {@link android.support.v4.app.Fragment}.
     */
    public static BusStopMapFragment newInstance(final double latitude, final double longitude) {
        final BusStopMapFragment f = new BusStopMapFragment();
        final Bundle b = new Bundle();
        b.putDouble(ARG_LATITUDE, latitude);
        b.putDouble(ARG_LONGITUDE, longitude);
        f.setArguments(b);
        
        return f;
    }
    
    /**
     * Create a new instance of the {@code BusStopMapFragment}, specifying a search term. The item
     * will be searched as soon as the {@link android.support.v4.app.Fragment} is ready.
     * 
     * @param searchTerm The search term.
     * @return A new instance of this {@link android.support.v4.app.Fragment}.
     */
    public static BusStopMapFragment newInstanceWithSearch(final String searchTerm) {
        final BusStopMapFragment f = new BusStopMapFragment();
        final Bundle b = new Bundle();
        b.putString(ARG_SEARCH, searchTerm);
        f.setArguments(b);
        
        return f;
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        
        try {
            callbacks = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new IllegalStateException(activity.getClass().getName() + " does not implement " +
                    Callbacks.class.getName());
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final Context context = getActivity();
        preferenceManager = ((BusApplication) context.getApplicationContext())
                .getPreferenceManager();
        searchMan = (SearchManager) context.getSystemService(Context.SEARCH_SERVICE);
        
        if (savedInstanceState != null) {
            chosenServices = savedInstanceState.getStringArray(STATE_CHOSEN_SERVICES);
            searchedBusStop = savedInstanceState.getString(STATE_CHOSEN_STOP);
        }
        
        // Get the height of the ActionBar from the assigned attribute in the appcompat project
        // theme.
        final TypedValue value = new TypedValue();
        getActivity().getTheme()
                .resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, value, true);
        actionBarHeight = getResources().getDimensionPixelSize(value.resourceId);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        getActivity().setTitle(R.string.map_title);
        getMapAsync(this);
        getLoaderManager().initLoader(LOADER_ID_SERVICES, null, this);

        if (savedInstanceState == null) {
            if (preferenceManager.isMapLocationShownAutomatically() && !hasLocationPermission()) {
                requestPermissions(
                        new String[] {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        }, PERMISSION_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        
        if (map != null) {
            updateMyLocationFeature();
            map.getUiSettings().setZoomControlsEnabled(preferenceManager.isMapZoomButtonsShown());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        
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
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putStringArray(STATE_CHOSEN_SERVICES, chosenServices);

        // Save the currently selected stop, if there is one.
        for (String busStop : busStopMarkers.keySet()) {
            final Marker marker = busStopMarkers.get(busStop);

            if (marker.isInfoWindowShown()) {
                outState.putString(STATE_CHOSEN_STOP, busStop);
                break;
            }
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.map = googleMap;

        // This Fragment shows an options menu.
        setHasOptionsMenu(true);

        final UiSettings uiSettings = map.getUiSettings();
        uiSettings.setRotateGesturesEnabled(false);
        uiSettings.setCompassEnabled(false);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setMapToolbarEnabled(false);
        uiSettings.setZoomControlsEnabled(preferenceManager.isMapZoomButtonsShown());

        map.setInfoWindowAdapter(new MapInfoWindow(getActivity()));
        map.setOnCameraChangeListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnInfoWindowClickListener(this);
        map.setOnInfoWindowCloseListener(this);
        map.setMapType(preferenceManager.getLastMapType());
        map.setPadding(0, actionBarHeight, 0, 0);
        updateMyLocationFeature();

        // This causes an initial load of bus stops to happen too.
        moveCameraToInitialLocation();

        if (chosenServices != null && chosenServices.length > 0) {
            onServicesChosen(chosenServices);
        }

        // Check to see if a search is to be done.
        final Bundle args = getArguments();

        if (args != null && args.containsKey(ARG_SEARCH)) {
            onSearch(args.getString(ARG_SEARCH));
            args.remove(ARG_SEARCH);
        } else if (getLoaderManager().getLoader(LOADER_ID_GEO_SEARCH) != null) {
            getLoaderManager().initLoader(LOADER_ID_GEO_SEARCH, null, this);
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.busstopmap_option_menu, menu);

        menuItemTrafficView = menu.findItem(R.id.busstopmap_option_menu_trafficview);
        menuItemServices = menu.findItem(R.id.busstopmap_option_menu_services);
        menuItemSearch = menu.findItem(R.id.busstopmap_option_menu_search);
        searchView = (SearchView) MenuItemCompat.getActionView(menuItemSearch);
        searchView.setSearchableInfo(searchMan.getSearchableInfo(getActivity().getComponentName()));
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
            case R.id.busstopmap_option_menu_services:
                callbacks.onShowServicesChooser(services, chosenServices,
                        getString(R.string.busstopmapfragment_service_chooser_title));
                return true;
            case R.id.busstopmap_option_menu_maptype:
                callbacks.onShowMapTypeSelection();
                return true;
            case R.id.busstopmap_option_menu_trafficview:
                // Toggle the traffic view.
                if (map != null) {
                    map.setTrafficEnabled(!map.isTrafficEnabled());
                    configureTrafficViewMenuItem();
                }
                
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
    public void onCameraChange(final CameraPosition position) {
        // If the camera has changed, force a refresh of the bus stop markers.
        refreshBusStops(position);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        final Object tag = marker.getTag();

        if (tag instanceof String) {
            searchedBusStop = (String) tag;
            getLoaderManager().restartLoader(LOADER_ID_BUS_STOP_SERVICES, null, this);
        }

        return false;
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        if (busStopMarkers.containsValue(marker)) {
            callbacks.onShowBusStopDetails((String) marker.getTag());
        }
    }

    @Override
    public void onInfoWindowClose(final Marker marker) {
        searchedBusStop = null;

        if (marker.getTag() != null) {
            getLoaderManager().destroyLoader(LOADER_ID_BUS_STOP_SERVICES);
        }
    }

    @Override
    public Loader onCreateLoader(final int i, final Bundle bundle) {
        switch (i) {
            case LOADER_ID_BUS_STOPS:
                return new BusStopMarkerLoader(getContext(),
                        bundle.getDouble(LOADER_ARG_MIN_X),
                        bundle.getDouble(LOADER_ARG_MAX_X),
                        bundle.getDouble(LOADER_ARG_MIN_Y),
                        bundle.getDouble(LOADER_ARG_MAX_Y),
                        bundle.getStringArray(LOADER_ARG_FILTERED_SERVICES));
            case LOADER_ID_GEO_SEARCH:
                String query = bundle.getString(LOADER_ARG_QUERY);
                // Make sure the query arg is not null.
                if (query == null) {
                   query = "";
                }
                
                return new GeoSearchLoader(getActivity(), query);
            case LOADER_ID_ROUTE_LINES:
                return new RouteLineLoader(getActivity(),
                        bundle.getStringArray(LOADER_ARG_FILTERED_SERVICES));
            case LOADER_ID_SERVICES:
                return new AllServiceNamesLoader(getContext());
            case LOADER_ID_BUS_STOP_COORDS:
                return new BusStopLoader(getContext(), searchedBusStop,
                        new String[] {
                                BusStopContract.BusStops.LATITUDE,
                                BusStopContract.BusStops.LONGITUDE
                        });
            case LOADER_ID_BUS_STOP_SERVICES:
                return new BusStopServicesLoader(getContext(), new String[] { searchedBusStop });
            default:
                return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onLoadFinished(final Loader loader, final Object d) {
        if (isAdded()) {
            switch (loader.getId()) {
                case LOADER_ID_BUS_STOPS:
                    addBusStopMarkers(((ProcessedCursorLoader.ResultWrapper<Map<String,
                            MarkerOptions>>) d).getResult());
                    break;
                case LOADER_ID_GEO_SEARCH:
                    addGeoSearchResults((HashSet<MarkerOptions>) d);
                    break;
                case LOADER_ID_ROUTE_LINES:
                    final Map<String, List<PolylineOptions>> result = ((ProcessedCursorLoader
                            .ResultWrapper<Map<String, List<PolylineOptions>>>) d).getResult();

                    if (result != null) {
                        addRouteLines(result);
                    }

                    break;
                case LOADER_ID_SERVICES:
                    services = ((ProcessedCursorLoader.ResultWrapper<String[]>) d).getResult();
                    configureServicesMenuItem();
                    break;
                case LOADER_ID_BUS_STOP_COORDS:
                    handleLoadBusStopCoords((Cursor) d);
                    break;
                case LOADER_ID_BUS_STOP_SERVICES:
                    handleBusStopServices(((ProcessedCursorLoader
                            .ResultWrapper<Map<String, String>>) d).getResult());
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(final Loader loader) {
        // Nothing to do.
    }

    @Override
    public void onServicesChosen(final String[] chosenServices) {
        this.chosenServices = chosenServices;
        
        // If the user has chosen services in the services filter, force a refresh of the marker
        // icons.
        refreshBusStops(null);
        
        final Iterator<String> itRouteLines = routeLines.keySet().iterator();

        // Loop through all existing route lines on the map...
        while (itRouteLines.hasNext()) {
            final String key = itRouteLines.next();
            boolean found = false;

            // ...and then loop through the chosen services list from the user...
            for (String fs : chosenServices) {
                if (key.equals(fs)) {
                    found = true;
                    break;
                }
            }

            // ...and if the service is not found in the user's list, remove it from the map.
            if (!found) {
                final LinkedList<Polyline> polyLines = routeLines.get(key);

                for (Polyline pl : polyLines) {
                    pl.remove();
                }

                itRouteLines.remove();
            }
        }

        final LinkedList<String> tempList = new LinkedList<>();
        
        // Loop through the filteredServices array. If the element does not appear in the existing
        // route lines, then add it to the to-be-added list.
        for (String fs : chosenServices) {
            if (!routeLines.containsKey(fs)) {
                tempList.add(fs);
            }
        }
        
        final int size = tempList.size();
        // Execute the load if there are routes to be loaded.
        if (size > 0) {
            final String[] servicesToLoad = new String[size];
            tempList.toArray(servicesToLoad);

            final Bundle b = new Bundle();
            b.putStringArray(LOADER_ARG_FILTERED_SERVICES, servicesToLoad);
            getLoaderManager().restartLoader(LOADER_ID_ROUTE_LINES, b, this);
        }
    }

    @Override
    public void onMapTypeChosen(final int mapType) {
        // When the user selects a new map type, change the map type.
        if(map != null) {
            map.setMapType(mapType);
        }
    }
    
    /**
     * This is called when the back button is pressed. It is called by the underlying
     * {@link Activity}.
     * 
     * @return {@code true} if the back event was handled here, {@code false} if not.
     */
    public boolean onBackPressed() {
        if (!geoSearchMarkers.isEmpty()) {
            for (Marker m : geoSearchMarkers) {
                if (m.isInfoWindowShown()) {
                    m.hideInfoWindow();
                    return true;
                }
            }

            for (Marker m : geoSearchMarkers) {
                m.remove();
            }

            geoSearchMarkers.clear();
            getLoaderManager().destroyLoader(LOADER_ID_GEO_SEARCH);

            return true;
        }
        
        // Loop through all the bus stop markers, and if any have an info window shown, hide it then
        // prevent the default back button behaviour.
        for (Marker m : busStopMarkers.values()) {
            if (m.isInfoWindowShown()) {
                m.hideInfoWindow();
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * This method is called by the underlying {@link Activity} when a search has been initiated.
     * 
     * @param searchTerm What to search for.
     */
    public void onSearch(final String searchTerm) {
        if (map == null) {
            return;
        }
        
        final Matcher m = STOP_CODE_SEARCH_PATTERN.matcher(searchTerm);
        
        if (m.matches()) {
            // If the searchTerm is a stop code, then move the camera to the bus stop.
            moveCameraToBusStop(searchTerm);
        } else {
            // If it's not a stop code, then do a geo search.
            final Bundle b = new Bundle();
            b.putString(LOADER_ARG_QUERY, searchTerm);
            
            // Start the search loader.
            getLoaderManager().restartLoader(LOADER_ID_GEO_SEARCH, b, this);

            configureSearchMenuItem(true);
        }
    }
    
    /**
     * Move the camera to a given {@code stopCode}, and show the info window for that
     * {@code stopCode} when the camera gets there.
     * 
     * @param stopCode The stopCode to move to.
     */
    public void moveCameraToBusStop(final String stopCode) {
        if (TextUtils.isEmpty(stopCode)) {
            return;
        }
        
        searchedBusStop = stopCode;
        getLoaderManager().restartLoader(LOADER_ID_BUS_STOP_COORDS, null, this);
    }
    
    /**
     * Move the camera to a given LatLng location.
     * 
     * @param location Where to move the camera to.
     * @param zoomLevel The zoom level of the camera.
     * @param animate Whether the transition should be animated or not.
     */
    public void moveCameraToLocation(final LatLng location, final float zoomLevel,
                                     final boolean animate) {
        if (location == null) {
            return;
        }
        
        final CameraUpdate update = CameraUpdateFactory.newLatLngZoom(location, zoomLevel);
        
        if (animate) {
            map.animateCamera(update);
        } else {
            map.moveCamera(update);
        }
    }

    /**
     * Enable the 'My Location' layer if the user has enabled it and if the permission has been
     * granted.
     */
    private void updateMyLocationFeature() {
        if (map != null) {
            map.setMyLocationEnabled(preferenceManager.isMapLocationShownAutomatically() &&
                    hasLocationPermission());
        }
    }
    
    /**
     * Refresh the bus stop marker icons on the map. This may be because the camera has moved, a
     * configuration change has happened or the user has selected services to filter by.
     * 
     * @param position If a {@link CameraPosition} is available, send it in so that it doesn't need
     *                 to be looked up again. If it's not available, use {@code null}.
     */
    private void refreshBusStops(CameraPosition position) {
        // Populate the CameraPosition if it wasn't given.
        if (position == null) {
            position = map.getCameraPosition();
        }
        
        // Get the visible bounds.
        final LatLngBounds lastVisibleBounds = map.getProjection().getVisibleRegion().latLngBounds;
        final Bundle b = new Bundle();
        
        // Populate the Bundle of arguments for the bus stops Loader.
        b.putDouble(LOADER_ARG_MIN_X, lastVisibleBounds.southwest.latitude);
        b.putDouble(LOADER_ARG_MIN_Y, lastVisibleBounds.southwest.longitude);
        b.putDouble(LOADER_ARG_MAX_X, lastVisibleBounds.northeast.latitude);
        b.putDouble(LOADER_ARG_MAX_Y, lastVisibleBounds.northeast.longitude);
        b.putFloat(LOADER_ARG_ZOOM, position.zoom);

        // If there are chosen services, then set the filtered services argument.
        if (chosenServices != null && chosenServices.length > 0) {
            b.putStringArray(LOADER_ARG_FILTERED_SERVICES, chosenServices);
        }
        
        // Start the bus stops Loader.
        getLoaderManager().restartLoader(LOADER_ID_BUS_STOPS, b, this);
    }
    
    /**
     * This method is called when the bus stops {@link Loader} has finished loading bus stops and
     * has data ready to be populated on the map.
     * 
     * @param result The data to be populated on the map.
     */
    private void addBusStopMarkers(final Map<String, MarkerOptions> result) {
        if (map == null) {
            return;
        }
        
        final Iterator<String> itCurrentStops = busStopMarkers.keySet().iterator();

        while (itCurrentStops.hasNext()) {
            final String existingStopCode = itCurrentStops.next();
            final Marker marker = busStopMarkers.get(existingStopCode);

            // If the new data does not contain the given stopCode, and the marker for that bus stop
            // doesn't have an info window shown, then remove it.
            if (!result.containsKey(existingStopCode) && !marker.isInfoWindowShown()) {
                marker.remove();
                itCurrentStops.remove();
            } else {
                // Otherwise, remove the bus stop from the new data as it is already populated on
                // the map and doesn't need to be re-populated. This is a performance enhancement.
                result.remove(existingStopCode);
            }
        }
        
        // Loop through all the new bus stops, and add them to the map. Bus stops common to the
        // existing collection and the new collection will not be touched.
        for (String newStop : result.keySet()) {
            final Marker marker = map.addMarker(result.get(newStop));
            marker.setTag(newStop);
            busStopMarkers.put(newStop, marker);
        }
        
        // If map has been moved to this location because the user searched for a specific bus
        // stop...
        if (searchedBusStop != null) {
            final Marker marker = busStopMarkers.get(searchedBusStop);
            
            // If the marker has been found...
            if (marker != null) {
                // Show the info window of the marker to highlight it.
                marker.showInfoWindow();
                // Set this to null to make sure the stop isn't highlighted again, until the user
                // initiates another search.
                searchedBusStop = null;
            }
        }
    }
    
    /**
     * This method is called when the search Loader has finished loading and data is to be populated
     * on the map.
     * 
     * @param result The data to be populated on the map.
     */
    private void addGeoSearchResults(final HashSet<MarkerOptions> result) {
        // Stop showing progress.
        configureSearchMenuItem(false);
        
        if (map == null) {
            return;
        }
        
        // Remove all of the existing search markers from the map.
        for (Marker m : geoSearchMarkers) {
            m.remove();
        }
        
        // ...and because they've been cleared from the map, remove them all from the collection.
        geoSearchMarkers.clear();
        
        // If there are no results, show a Toast notification to the user.
        if (result == null || result.isEmpty()) {
            Toast.makeText(getActivity(), R.string.busstopmapfragment_nosearchresults,
                    Toast.LENGTH_LONG).show();
            return;
        }
        
        Marker marker;
        boolean isFirst = true;
        
        for (MarkerOptions mo : result) {
            // Add the new marker to the map.
            marker = map.addMarker(mo);
            
            // Make sure the item does not already exist in the marker list. If it does, remove it
            // from the map again.
            if (!geoSearchMarkers.add(marker)) {
                marker.remove();
            } else if (isFirst) {
                // If it's the first icon to be added, move the camera to that bus stop marker.
                isFirst = false;
                
                moveCameraToLocation(marker.getPosition(), DEFAULT_SEARCH_ZOOM, true);
                marker.showInfoWindow();
            }
        }
    }
    
    /**
     * Add route lines to the map. This is called when the route lines loader has finished loading
     * the route lines.
     * 
     * @param result A {@link HashMap}, mapping the service name to a {@link LinkedList} of
     * {@link PolylineOptions} objects. This is a {@link LinkedList} because a service may have
     * more than one {@link Polyline}.
     */
    private void addRouteLines(@NonNull final Map<String, List<PolylineOptions>> result) {
        if (map == null) {
            return;
        }
        
        List<PolylineOptions> polyLineOptions;
        LinkedList<Polyline> newPolyLines;
        
        // Loop through all services in the HashMap.
        for (String service : result.keySet()) {
            polyLineOptions = result.get(service);
            // Create the LinkedList that the Polylines will be stored in.
            newPolyLines = new LinkedList<>();
            // Add the LinkedList to the routeLines HashMap.
            routeLines.put(service, newPolyLines);
            
            // Loop through all the PolylineOptions for this service, and add them to the map and
            // the Polyline LinkedList.
            for (PolylineOptions plo : polyLineOptions) {
                newPolyLines.add(map.addPolyline(plo));
            }
        }
    }
    
    /**
     * Move the camera to the initial location. The initial location is determined by the following
     * order;
     *
     * <ol>
     *     <li>If the args contains a stopCode, go there.</li>
     *     <li>If the args contains a latitude AND a longitude, go there.</li>
     *     <li>If the SharedPreferences have mappings for a previous location, then go there.</li>
     *     <li>Otherwise, go to the default map location, as defined by DEFAULT_LAT and
     *         DEFAULT_LONG at DEFAULT_ZOOM.</li>
     * </ol>
     */
    private void moveCameraToInitialLocation() {
        final Bundle args = getArguments();
        
        if (args != null && args.containsKey(ARG_STOPCODE)) {
            moveCameraToBusStop(args.getString(ARG_STOPCODE));
            args.remove(ARG_STOPCODE);
        } else if (args != null && args.containsKey(ARG_LATITUDE) &&
                args.containsKey(ARG_LONGITUDE)) {
            moveCameraToLocation(new LatLng(args.getDouble(ARG_LATITUDE),
                    args.getDouble(ARG_LONGITUDE)), DEFAULT_SEARCH_ZOOM, false);
            args.remove(ARG_LATITUDE);
            args.remove(ARG_LONGITUDE);
        } else if (map != null) {
            final double latitude = preferenceManager.getLastMapLatitude();
            final double longitude = preferenceManager.getLastMapLongitude();
            final float zoom = preferenceManager.getLastMapZoomLevel();

            moveCameraToLocation(new LatLng(latitude, longitude), zoom, false);
        }
    }

    /**
     * Handle bus stop coordinates being loaded. This is done when a bus stop is deep linked in to
     * and the camera should pan to the selected bus stop.
     *
     * @param cursor The {@link Cursor} containing the latitude and longitude of the selected bus
     * stop.
     */
    private void handleLoadBusStopCoords(@Nullable final Cursor cursor) {
        if (cursor != null) {
            final int latitudeColumn = cursor.getColumnIndex(BusStopContract.BusStops.LATITUDE);
            final int longitudeColumn = cursor.getColumnIndex(BusStopContract.BusStops.LONGITUDE);

            if (cursor.moveToFirst()) {
                final LatLng location =
                        new LatLng(cursor.getDouble(latitudeColumn),
                                cursor.getDouble(longitudeColumn));
                moveCameraToLocation(location, DEFAULT_SEARCH_ZOOM, true);
            }
        }

        getLoaderManager().destroyLoader(LOADER_ID_BUS_STOP_COORDS);
    }

    /**
     * Handle loading of the services listing for a bus stop marker.
     *
     * @param services A {@link Map} of bus stop code to the services {@link String}.
     */
    private void handleBusStopServices(@Nullable final Map<String, String> services) {
        if (services != null && searchedBusStop != null) {
            final Marker marker = busStopMarkers.get(searchedBusStop);

            if (marker != null) {
                final String servicesStr = services.get(searchedBusStop);

                if (!TextUtils.isEmpty(servicesStr)) {
                    marker.setSnippet(servicesStr);

                    // Reshow the window to update the snippet text.
                    if (marker.isInfoWindowShown()) {
                        marker.showInfoWindow();
                    }
                }
            }
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
     * Update the filter {@link MenuItem} to be enabled/disabled depending on the current state.
     */
    private void configureServicesMenuItem() {
        if (menuItemServices != null) {
            menuItemServices.setEnabled(services != null && services.length > 0 &&
                    hasLocationPermission());
        }
    }

    /**
     * Configure the search menu item.
     *
     * @param isLoading Is a search in progress?
     */
    private void configureSearchMenuItem(final boolean isLoading) {
        if (menuItemSearch != null) {
            menuItemSearch.setEnabled(map != null);

            if (isLoading) {
                MenuItemCompat.setActionView(menuItemSearch,
                        R.layout.actionbar_indeterminate_progress);
            } else {
                MenuItemCompat.setActionView(menuItemSearch, searchView);
            }
        }
    }

    /**
     * Does the application have access to the necessary location permissions?
     *
     * @return {@code true} if the application has access to the necessary location permissions,
     * {@code false} if not.
     */
    private boolean hasLocationPermission() {
        final boolean hasFineLocation = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        final boolean hasCoarseLocation = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        return hasFineLocation && hasCoarseLocation;
    }
    
    /**
     * Any {@link Activity Activities} which host this {@link android.support.v4.app.Fragment} must
     * implement this interface to handle navigation events.
     */
    public interface Callbacks extends OnShowServicesChooserListener {
        
        /**
         * This is called when the user wishes to select their preferred map type.
         */
        void onShowMapTypeSelection();
        
        /**
         * This is called when the user wants to see details about a bus stop.
         * 
         * @param stopCode The bus stop code the user wants to see details for.
         */
        void onShowBusStopDetails(String stopCode);
    }
}