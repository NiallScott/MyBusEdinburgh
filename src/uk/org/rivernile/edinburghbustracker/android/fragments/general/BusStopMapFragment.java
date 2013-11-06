/*
 * Copyright (C) 2012 - 2013 Niall 'Rivernile' Scott
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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;
import uk.org.rivernile.edinburghbustracker.android
        .MapSearchSuggestionsProvider;
import uk.org.rivernile.edinburghbustracker.android.PreferencesActivity;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .IndeterminateProgressDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .MapTypeChooserDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .ServicesChooserDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.maps.BusStopMarkerLoader;
import uk.org.rivernile.edinburghbustracker.android.maps.GeoSearchLoader;
import uk.org.rivernile.edinburghbustracker.android.maps.MapInfoWindow;
import uk.org.rivernile.edinburghbustracker.android.maps.RouteLineLoader;

/**
 * The BusStopMapFragment shows a Google Maps v2 MapView and depending on the
 * location of the camera, the zoom level and service filter, it shows bus stop
 * icons on the map. The user can tap on a bus stop icon to show the info
 * window (bubble). If the user taps on the info window, then the
 * BusStopDetailsFragment is shown.
 * 
 * The user can also select the type of map these wish to see and search for bus
 * stops and places.
 * 
 * @author Niall Scott
 */
public class BusStopMapFragment extends SupportMapFragment
        implements GoogleMap.OnCameraChangeListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener,
        LoaderManager.LoaderCallbacks,
        ServicesChooserDialogFragment.Callbacks,
        MapTypeChooserDialogFragment.Callbacks,
        IndeterminateProgressDialogFragment.Callbacks {
    
    /** The stopCode argument. */
    public static final String ARG_STOPCODE = "stopCode";
    /** The latitude argument. */
    public static final String ARG_LATITUDE = "latitude";
    /** The longitude argument. */
    public static final String ARG_LONGITUDE = "longitude";
    /** The search argument. */
    public static final String ARG_SEARCH = "searchTerm";
    
    private static final String ARG_CHOSEN_SERVICES = "chosenServices";
    
    /** The default latitude. */
    public static final double DEFAULT_LAT = 55.948611;
    /** The default longitude. */
    public static final double DEFAULT_LONG = -3.199811;
    /** The default zoom. */
    public static final float DEFAULT_ZOOM = 11f;
    /** The default search zoom. */
    public static final float DEFAULT_SEARCH_ZOOM =  16f;
    
    private static final Pattern STOP_CODE_PATTERN =
            Pattern.compile("(\\d{8})\\)$");
    private static final Pattern STOP_CODE_SEARCH_PATTERN =
            Pattern.compile("^\\d{8}$");
    
    private static final String LOADER_ARG_MIN_X = "minX";
    private static final String LOADER_ARG_MIN_Y = "minY";
    private static final String LOADER_ARG_MAX_X = "maxX";
    private static final String LOADER_ARG_MAX_Y = "maxY";
    private static final String LOADER_ARG_ZOOM = "zoom";
    private static final String LOADER_ARG_FILTERED_SERVICES =
            "filteredServices";
    private static final String LOADER_ARG_QUERY = "query";
    
    private static final int LOADER_ID_BUS_STOPS = 0;
    private static final int LOADER_ID_GEO_SEARCH = 1;
    private static final int LOADER_ID_ROUTE_LINES = 2;
    
    private Callbacks callbacks;
    private BusStopDatabase bsd;
    private GoogleMap map;
    private SharedPreferences sp;
    
    private final HashMap<String, Marker> busStopMarkers =
            new HashMap<String, Marker>();
    private final HashMap<String, LinkedList<Polyline>> routeLines =
            new HashMap<String, LinkedList<Polyline>>();
    private HashSet<Marker> geoSearchMarkers = new HashSet<Marker>();
    private String searchedBusStop = null;
    private String[] services;
    private String[] chosenServices;
    
    /**
     * Create a new instance of the BusStopMapFragment, setting the initial
     * location to that of the stopCode provided.
     * 
     * @param stopCode The stopCode to go to.
     * @return A new instance of this Fragment.
     */
    public static BusStopMapFragment newInstance(final String stopCode) {
        final BusStopMapFragment f = new BusStopMapFragment();
        final Bundle b = new Bundle();
        b.putString(ARG_STOPCODE, stopCode);
        f.setArguments(b);
        
        return f;
    }
    
    /**
     * Create a new instance of the BusStopMapFragment, setting the initial
     * location specified by latitude and longitude.
     * 
     * @param latitude The latitude to go to.
     * @param longitude The longitude to go to.
     * @return A new instance of this Fragment.
     */
    public static BusStopMapFragment newInstance(final double latitude,
            final double longitude) {
        final BusStopMapFragment f = new BusStopMapFragment();
        final Bundle b = new Bundle();
        b.putDouble(ARG_LATITUDE, latitude);
        b.putDouble(ARG_LONGITUDE, longitude);
        f.setArguments(b);
        
        return f;
    }
    
    /**
     * Create a new instance of the BusStopMapFragment, specifying a search
     * term. The item will be searched as soon as the Fragment is ready.
     * 
     * @param searchTerm The search term.
     * @return A new instance of this Fragment.
     */
    public static BusStopMapFragment newInstanceWithSearch(
            final String searchTerm) {
        final BusStopMapFragment f = new BusStopMapFragment();
        final Bundle b = new Bundle();
        b.putString(ARG_SEARCH, searchTerm);
        f.setArguments(b);
        
        return f;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        
        try {
            callbacks = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new IllegalStateException(activity.getClass().getName() +
                    " does not implement " + Callbacks.class.getName());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Retain the instance to this Fragment.
        setRetainInstance(true);
        
        final Context context = getActivity();
        bsd = BusStopDatabase.getInstance(context.getApplicationContext());
        sp = context.getSharedPreferences(PreferencesActivity.PREF_FILE, 0);

        services = bsd.getBusServiceList();
        
        if (savedInstanceState != null) {
            chosenServices = savedInstanceState
                    .getStringArray(ARG_CHOSEN_SERVICES);
        }
        
        // This Fragment shows an options menu.
        setHasOptionsMenu(true);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        if(map == null) {
            map = getMap();
            
            if(map != null) {
                getActivity().supportInvalidateOptionsMenu();
                
                final UiSettings uiSettings = map.getUiSettings();
                uiSettings.setRotateGesturesEnabled(false);
                uiSettings.setCompassEnabled(false);
                uiSettings.setMyLocationButtonEnabled(false);
                
                map.setInfoWindowAdapter(new MapInfoWindow(getActivity()));
                map.setOnCameraChangeListener(this);
                map.setOnMarkerClickListener(this);
                map.setOnInfoWindowClickListener(this);
                map.setMapType(sp.getInt(
                        PreferencesActivity.PREF_MAP_LAST_MAP_TYPE,
                        GoogleMap.MAP_TYPE_NORMAL));
                moveCameraToInitialLocation();
                
                refreshBusStops(null);
                
                // Check to see if a search is to be done.
                final Bundle args = getArguments();
                if(args != null && args.containsKey(ARG_SEARCH)) {
                    onSearch(args.getString(ARG_SEARCH));
                    args.remove(ARG_SEARCH);
                }
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        
        if(map != null) {
            final SharedPreferences sharedPrefs = getActivity()
                    .getSharedPreferences(PreferencesActivity.PREF_FILE, 0);
            map.setMyLocationEnabled(sharedPrefs
                    .getBoolean(PreferencesActivity.PREF_AUTO_LOCATION, true));
            map.getUiSettings().setZoomControlsEnabled(sharedPrefs
                    .getBoolean(PreferencesActivity.PREF_ZOOM_BUTTONS, true));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();
        
        if(map != null) {
            // Save the camera location to SharedPreferences, so the user is
            // shown this location when they load the map again.
            final SharedPreferences.Editor edit = sp.edit();
            final CameraPosition position = map.getCameraPosition();
            final LatLng latLng = position.target;
            
            edit.putString(PreferencesActivity.PREF_MAP_LAST_LATITUDE,
                    String.valueOf(latLng.latitude));
            edit.putString(PreferencesActivity.PREF_MAP_LAST_LONGITUDE,
                    String.valueOf(latLng.longitude));
            edit.putFloat(PreferencesActivity.PREF_MAP_LAST_ZOOM,
                    position.zoom);
            edit.putInt(PreferencesActivity.PREF_MAP_LAST_MAP_TYPE,
                    map.getMapType());
            edit.commit();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putStringArray(ARG_CHOSEN_SERVICES, chosenServices);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateOptionsMenu(final Menu menu,
            final MenuInflater inflater) {
        inflater.inflate(R.menu.busstopmap_option_menu, menu);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        
        MenuItem item =
                menu.findItem(R.id.busstopmap_option_menu_trafficview);
        if(map != null && map.isTrafficEnabled()) {
            item.setTitle(R.string.map_menu_mapoverlay_trafficviewoff);
        } else {
            item.setTitle(R.string.map_menu_mapoverlay_trafficviewon);
        }
        
        item.setEnabled(map != null);
        
        item = menu.findItem(R.id.busstopmap_option_menu_services);
        item.setEnabled(services != null && services.length > 0);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch(item.getItemId()) {
            case R.id.busstopmap_option_menu_mylocation:
                // Move the camera to the user's location and zoom in.
                moveCameraToMyLocation(true);
                return true;
            case R.id.busstopmap_option_menu_search:
                // Tell the underlying Activity to initiate a search.
                getActivity().onSearchRequested();
                return true;
            case R.id.busstopmap_option_menu_services:
                callbacks.onShowServicesChooser(services, chosenServices,
                        getString(R.string
                                .busstopmapfragment_service_chooser_title));
                return true;
            case R.id.busstopmap_option_menu_maptype:
                callbacks.onShowMapTypeSelection();
                return true;
            case R.id.busstopmap_option_menu_trafficview:
                // Toggle the traffic view.
                if(map != null) {
                    map.setTrafficEnabled(!map.isTrafficEnabled());
                    getActivity().supportInvalidateOptionsMenu();
                }
                
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCameraChange(final CameraPosition position) {
        // If the camera has changed, force a refresh of the bus stop markers.
        refreshBusStops(position);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        final String snippet = marker.getSnippet();
        
        if(busStopMarkers.containsValue(marker) &&
                (snippet == null || snippet.length() == 0)) {
            final Matcher matcher = STOP_CODE_PATTERN
                    .matcher(marker.getTitle());
            if(matcher.find()) {
                final String stopCode = matcher.group(1);
                marker.setSnippet(bsd.getBusServicesForStopAsString(stopCode));
            }
        }
        
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onInfoWindowClick(final Marker marker) {
        if(busStopMarkers.containsValue(marker)) {
            final Matcher matcher = STOP_CODE_PATTERN.matcher(
                    marker.getTitle());
            if(matcher.find()) {
                callbacks.onShowBusStopDetails(matcher.group(1));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Loader onCreateLoader(final int i, final Bundle bundle) {
        switch(i) {
            case LOADER_ID_BUS_STOPS:
                if(bundle.containsKey(LOADER_ARG_FILTERED_SERVICES)) {
                    return new BusStopMarkerLoader(
                            getActivity(),
                            bundle.getDouble(LOADER_ARG_MIN_X),
                            bundle.getDouble(LOADER_ARG_MIN_Y),
                            bundle.getDouble(LOADER_ARG_MAX_X),
                            bundle.getDouble(LOADER_ARG_MAX_Y),
                            bundle.getFloat(LOADER_ARG_ZOOM),
                            bundle.getStringArray(LOADER_ARG_FILTERED_SERVICES)
                        );
                } else {
                    return new BusStopMarkerLoader(
                            getActivity(),
                            bundle.getDouble(LOADER_ARG_MIN_X),
                            bundle.getDouble(LOADER_ARG_MIN_Y),
                            bundle.getDouble(LOADER_ARG_MAX_X),
                            bundle.getDouble(LOADER_ARG_MAX_Y),
                            bundle.getFloat(LOADER_ARG_ZOOM)
                        );
                }
            case LOADER_ID_GEO_SEARCH:
                String query = bundle.getString(LOADER_ARG_QUERY);
                // Make sure the query arg is not null.
                if(query == null) {
                   query = "";
                }
                
                return new GeoSearchLoader(getActivity(), query);
            case LOADER_ID_ROUTE_LINES:
                return new RouteLineLoader(getActivity(),
                        bundle.getStringArray(LOADER_ARG_FILTERED_SERVICES));
            default:
                return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoadFinished(final Loader loader, final Object d) {
        if (isAdded()) {
            switch (loader.getId()) {
                case LOADER_ID_BUS_STOPS:
                    addBusStopMarkers((HashMap<String, MarkerOptions>)d);
                    break;
                case LOADER_ID_GEO_SEARCH:
                    addGeoSearchResults((HashSet<MarkerOptions>)d);
                    break;
                case LOADER_ID_ROUTE_LINES:
                    addRouteLines((HashMap<String,
                            LinkedList<PolylineOptions>>)d);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoaderReset(final Loader loader) {
        // Nothing to do.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onServicesChosen(final String[] chosenServices) {
        this.chosenServices = chosenServices;
        
        // If the user has chosen services in the services filter, force a
        // refresh of the marker icons.
        refreshBusStops(null);
        
        final LinkedList<String> tempList = new LinkedList<String>();
        boolean found;
        
        // Loop through the existing route lines. If a service doesn't exist in
        // the chosen services list, add it to the to-be-removed list.
        for(String key : routeLines.keySet()) {
            found = false;
            
            for(String fs : chosenServices) {
                if(key.equals(fs)) {
                    found = true;
                    break;
                }
            }
            
            if(!found) {
                tempList.add(key);
            }
        }
        
        LinkedList<Polyline> polyLines;
        // Loop through the to-be-removed list and remove the Polylines and the
        // entry from the routeLines HashMap.
        for(String toRemove : tempList) {
            polyLines = routeLines.get(toRemove);
            routeLines.remove(toRemove);
            
            for(Polyline pl : polyLines) {
                pl.remove();
            }
        }
        
        // The tempList is going to be reused, so clear it out.
        tempList.clear();
        
        // Loop through the filteredServices array. If the element does not
        // appear in the existing route lines, then add it to the to-be-added
        // list.
        for(String fs : chosenServices) {
            if(!routeLines.containsKey(fs)) {
                tempList.add(fs);
            }
        }
        
        final int size = tempList.size();
        // Execute the load if there are routes to be loaded.
        if(size > 0) {
            final String[] servicesToLoad = new String[size];
            tempList.toArray(servicesToLoad);

            final Bundle b = new Bundle();
            b.putStringArray(LOADER_ARG_FILTERED_SERVICES, servicesToLoad);
            getLoaderManager().restartLoader(LOADER_ID_ROUTE_LINES, b, this);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onMapTypeChosen(final int mapType) {
        // When the user selects a new map type, change the map type.
        if(map != null) {
            map.setMapType(mapType);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onProgressCancel() {
        // If the user cancels the search progress dialog, then cancel the
        // search loader.
        getLoaderManager().destroyLoader(LOADER_ID_GEO_SEARCH);
    }
    
    /**
     * This is called when the back button is pressed. It is called by the
     * underlying Activity.
     * 
     * @return true if the back event was handled here, false if not.
     */
    public boolean onBackPressed() {
        if(!geoSearchMarkers.isEmpty()) {
            for(Marker m : geoSearchMarkers) {
                if(m.isInfoWindowShown()) {
                    m.hideInfoWindow();
                    return true;
                }
            }

            for(Marker m : geoSearchMarkers) {
                m.remove();
            }

            geoSearchMarkers.clear();
            return true;
        }
        
        // Loop through all the bus stop markers, and if any have an info
        // window shown, hide it then prevent the default back button
        // behaviour.
        for(Marker m : busStopMarkers.values()) {
            if(m.isInfoWindowShown()) {
                m.hideInfoWindow();
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * This method is called by the underlying Activity when a search has been
     * initiated.
     * 
     * @param searchTerm What to search for.
     */
    public void onSearch(final String searchTerm) {
        if(map == null) {
            return;
        }
        
        final Matcher m = STOP_CODE_SEARCH_PATTERN.matcher(searchTerm);
        
        if(m.matches()) {
            // If the searchTerm is a stop code, then move the camera to the bus
            // stop.
            moveCameraToBusStop(searchTerm);
        } else {
            // If it's not a stop code, then do a geo search.
            final Bundle b = new Bundle();
            b.putString(LOADER_ARG_QUERY, searchTerm);
            
            // Save the search term as a search suggestion.
            final SearchRecentSuggestions suggestions =
                    new SearchRecentSuggestions(getActivity(),
                        MapSearchSuggestionsProvider.AUTHORITY,
                        MapSearchSuggestionsProvider.MODE);
            suggestions.saveRecentQuery(searchTerm, null);
            
            // Start the search loader.
            getLoaderManager().restartLoader(LOADER_ID_GEO_SEARCH, b, this);

            callbacks.onShowSearchProgress(
                    getString(R.string.busstopmapfragment_progress_message,
                            searchTerm));
        }
    }
    
    /**
     * Move the camera to a given stopCode, and show the info window for that
     * stopCode when the camera gets there.
     * 
     * @param stopCode The stopCode to move to.
     */
    public void moveCameraToBusStop(final String stopCode) {
        if(stopCode == null || stopCode.length() == 0) {
            return;
        }
        
        searchedBusStop = stopCode;
        moveCameraToLocation(bsd.getLatLngForStopCode(stopCode),
                DEFAULT_SEARCH_ZOOM, true);
    }
    
    /**
     * Move the camera to a given LatLng location.
     * 
     * @param location Where to move the camera to.
     * @param zoomLevel The zoom level of the camera.
     * @param animate Whether the transition should be animated or not.
     */
    public void moveCameraToLocation(final LatLng location,
            final float zoomLevel, final boolean animate) {
        if(location == null) {
            return;
        }
        
        final CameraUpdate update = CameraUpdateFactory.newLatLngZoom(location,
                zoomLevel);
        
        if(animate) {
            map.animateCamera(update);
        } else {
            map.moveCamera(update);
        }
    }
    
    /**
     * Refresh the bus stop marker icons on the map. This may be because the
     * camera has moved, a configuration change has happened or the user has
     * selected services to filter by.
     * 
     * @param position If a CameraPosition is available, send it in so that it
     * doesn't need to be looked up again. If it's not available, use null.
     */
    private void refreshBusStops(CameraPosition position) {
        if(map == null || !isAdded()) {
            return;
        }
        
        // Populate the CameraPosition if it wasn't given.
        if(position == null) {
            position = map.getCameraPosition();
        }
        
        // Get the visible bounds.
        final LatLngBounds lastVisibleBounds =
                map.getProjection().getVisibleRegion().latLngBounds;
        final Bundle b = new Bundle();
        
        // Populate the Bundle of arguments for the bus stops Loader.
        b.putDouble(LOADER_ARG_MIN_X, lastVisibleBounds.southwest.latitude);
        b.putDouble(LOADER_ARG_MIN_Y, lastVisibleBounds.southwest.longitude);
        b.putDouble(LOADER_ARG_MAX_X, lastVisibleBounds.northeast.latitude);
        b.putDouble(LOADER_ARG_MAX_Y, lastVisibleBounds.northeast.longitude);
        b.putFloat(LOADER_ARG_ZOOM, position.zoom);

        // If there are chosen services, then set the filtered services
        // argument.
        if(chosenServices != null && chosenServices.length > 0) {
            b.putStringArray(LOADER_ARG_FILTERED_SERVICES, chosenServices);
        }
        
        // Start the bus stops Loader.
        getLoaderManager().restartLoader(LOADER_ID_BUS_STOPS, b, this);
    }
    
    /**
     * This method is called when the bus stops Loader has finished loading bus
     * stops and has data ready to be populated on the map.
     * 
     * @param result The data to be populated on the map.
     */
    private void addBusStopMarkers(
            final HashMap<String, MarkerOptions> result) {
        if(map == null) {
            return;
        }
        
        // Get an array of the stopCodes that are currently on the map. This is
        // given to us as an array of Objects, which cannot be cast to an array
        // of Strings.
        final Object[] currentStops = busStopMarkers.keySet()
                .toArray();
        Marker marker;
        for(Object existingStop : currentStops) {
            marker = busStopMarkers.get((String)existingStop);
            
            // If the new data does not contain the given stopCode, and the
            // marker for that bus stop doesn't have an info window shown, then
            // remove it.
            if(!result.containsKey((String)existingStop) &&
                    !marker.isInfoWindowShown()) {
                marker.remove();
                busStopMarkers.remove((String)existingStop);
            } else {
                // Otherwise, remove the bus stop from the new data as it is
                // already populated on the map and doesn't need to be
                // re-populated. This is a performance enhancement.
                result.remove((String)existingStop);
            }
        }
        
        // Loop through all the new bus stops, and add them to the map. Bus
        // stops common to the existing collection and the new collection will
        // not be touched.
        for(String newStop : result.keySet()) {
            busStopMarkers.put(newStop, map.addMarker(result.get(newStop)));
        }
        
        // If map has been moved to this location because the user searched for
        // a specific bus stop...
        if(searchedBusStop != null) {
            marker = busStopMarkers.get(searchedBusStop);
            
            // If the marker has been found...
            if(marker != null) {
                // Get the snippet text for the marker and if it does not exist,
                // populate it with the bus services list.
                final String snippet = marker.getSnippet();
                if(snippet == null || snippet.length() == 0) {
                    marker.setSnippet(bsd.getBusServicesForStopAsString(
                            searchedBusStop));
                }
                
                // Show the info window of the marker to highlight it.
                marker.showInfoWindow();
                // Set this to null to make sure the stop isn't highlighted
                // again, until the user initiates another search.
                searchedBusStop = null;
            }
        }
    }
    
    /**
     * This method is called when the search Loader has finished loading and
     * data is to be populated on the map.
     * 
     * @param result The data to be populated on the map.
     */
    private void addGeoSearchResults(final HashSet<MarkerOptions> result) {
        // If there is a progress Dialog, get rid of it.
        callbacks.onDismissSearchProgress();
        
        if(map == null) {
            return;
        }
        
        // Remove all of the existing search markers from the map.
        for(Marker m : geoSearchMarkers) {
            m.remove();
        }
        
        // ...and because they've been cleared from the map, remove them all
        // from the collection.
        geoSearchMarkers.clear();
        
        // If there are no results, show a Toast notification to the user.
        if(result == null || result.isEmpty()) {
            Toast.makeText(getActivity(),
                    R.string.busstopmapfragment_nosearchresults,
                    Toast.LENGTH_LONG).show();
            return;
        }
        
        Marker marker;
        boolean isFirst = true;
        
        for(MarkerOptions mo : result) {
            // Add the new marker to the map.
            marker = map.addMarker(mo);
            
            // Make sure the item does not already exist in the marker list. If
            // it does, remove it from the map again.
            if(!geoSearchMarkers.add(marker)) {
                marker.remove();
            } else if(isFirst) {
                // If it's the first icon to be added, move the camera to that
                // bus stop marker.
                isFirst = false;
                
                moveCameraToLocation(marker.getPosition(), DEFAULT_SEARCH_ZOOM,
                        true);
                marker.showInfoWindow();
            }
        }
    }
    
    /**
     * Add route lines to the Map. This is called when the route lines loader
     * has finished loading the route lines.
     * 
     * @param result A HashMap, mapping the service name to a LinkedList of
     * PolylineOptions objects. This is a LinkedList because a service may have
     * more than one Polyline.
     */
    private void addRouteLines(
            final HashMap<String, LinkedList<PolylineOptions>> result) {
        if(map == null) {
            return;
        }
        
        LinkedList<PolylineOptions> polyLineOptions;
        LinkedList<Polyline> newPolyLines;
        
        // Loop through all services in the HashMap.
        for(String service : result.keySet()) {
            polyLineOptions = result.get(service);
            // Create the LinkedList that the Polylines will be stored in.
            newPolyLines = new LinkedList<Polyline>();
            // Add the LinkedList to the routeLines HashMap.
            routeLines.put(service, newPolyLines);
            
            // Loop through all the PolylineOptions for this service, and add
            // them to the map and the Polyline LinkedList.
            for(PolylineOptions plo : polyLineOptions) {
                newPolyLines.add(map.addPolyline(plo));
            }
        }
    }
    
    /**
     * Move the camera to the device's location, as provided by the Google Maps
     * API.
     * 
     * @param verbose true if it should display a Toast notification if there is
     * no location, false if not.
     */
    private void moveCameraToMyLocation(final boolean verbose) {
        if (map != null && map.isMyLocationEnabled()) {
            final Location myLocation = map.getMyLocation();
            
            if(myLocation != null) {
                moveCameraToLocation(new LatLng(myLocation.getLatitude(),
                        myLocation.getLongitude()), DEFAULT_SEARCH_ZOOM, true);
                return;
            }
        }

        Toast.makeText(getActivity(),
                R.string.busstopmapfragment_location_unknown,
                Toast.LENGTH_LONG).show();
    }
    
    /**
     * Move the camera to the initial location. The initial location is
     * determined by the following order;
     * 
     * - If the args contains a stopCode, go there.
     * - If the args contains a latitude AND a longitude, go there.
     * - If the SharedPreferences have mappings for a previous location, then
     *   go there.
     * - Otherwise, go to the default map location, as defined by
     *   {@link #DEFAULT_LAT} and {@link #DEFAULT_LONG) at
     *   {@link #DEFAULT_ZOOM}.
     */
    private void moveCameraToInitialLocation() {
        final Bundle args = getArguments();
        
        if(args != null && args.containsKey(ARG_STOPCODE)) {
            moveCameraToBusStop(args.getString(ARG_STOPCODE));
            args.remove(ARG_STOPCODE);
        } else if(args != null && args.containsKey(ARG_LATITUDE) &&
                args.containsKey(ARG_LONGITUDE)) {
            moveCameraToLocation(new LatLng(args.getDouble(ARG_LATITUDE),
                    args.getDouble(ARG_LONGITUDE)), DEFAULT_SEARCH_ZOOM, false);
            args.remove(ARG_LATITUDE);
            args.remove(ARG_LONGITUDE);
        } else if(map != null) {
            // The Lat/Lons have to be treated as Strings because
            // SharedPreferences has no support for doubles.
            final String latitude = sp.getString(
                    PreferencesActivity.PREF_MAP_LAST_LATITUDE,
                    String.valueOf(DEFAULT_LAT));
            final String longitude = sp.getString(
                    PreferencesActivity.PREF_MAP_LAST_LONGITUDE,
                    String.valueOf(DEFAULT_LONG));
            final float zoom = sp.getFloat(
                    PreferencesActivity.PREF_MAP_LAST_ZOOM, DEFAULT_ZOOM);
            
            try {
                moveCameraToLocation(new LatLng(Double.parseDouble(latitude),
                        Double.parseDouble(longitude)), zoom, false);
            } catch(NumberFormatException e) {
                moveCameraToLocation(new LatLng(DEFAULT_LAT, DEFAULT_LONG),
                        DEFAULT_ZOOM, false);
            }
        }
    }
    
    /**
     * Any Activities which host this Fragment must implement this interface to
     * handle navigation events.
     */
    public static interface Callbacks {
        
        /**
         * This is called when the user wishes to select their preferred map
         * type.
         */
        public void onShowMapTypeSelection();
        
        /**
         * This is called when the user wishes to select services, for example,
         * for filtering.
         * 
         * @param services The services to choose from.
         * @param selectedServices Any services that should be selected by
         * default.
         * @param title A title to show on the chooser.
         */
        public void onShowServicesChooser(String[] services,
                String[] selectedServices, String title);
        
        /**
         * This is called when the user has initiated a search and progress
         * should be shown.
         * 
         * @param message The message to show the user.
         */
        public void onShowSearchProgress(String message);
        
        /**
         * This is called when the search progress should be dimissed.
         */
        public void onDismissSearchProgress();
        
        /**
         * This is called when the user wants to see details about a bus stop.
         * 
         * @param stopCode The bus stop code the user wants to see details for.
         */
        public void onShowBusStopDetails(String stopCode);
    }
}