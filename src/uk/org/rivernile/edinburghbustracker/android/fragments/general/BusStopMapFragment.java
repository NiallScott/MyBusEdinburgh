/*
 * Copyright (C) 2012 Niall 'Rivernile' Scott
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

import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;
import uk.org.rivernile.edinburghbustracker.android.BusStopDetailsActivity;
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
        ServicesChooserDialogFragment.EventListener,
        MapTypeChooserDialogFragment.EventListener,
        IndeterminateProgressDialogFragment.EventListener {
    
    private static final double DEFAULT_LAT = 55.948611;
    private static final double DEFAULT_LONG = -3.199811;
    private static final float DEFAULT_ZOOM = 11f;
    private static final float DEFAULT_SEARCH_ZOOM =  16f;
    
    private static final Pattern STOP_CODE_PATTERN =
            Pattern.compile("(\\d{8})\\)$");
    private static final Pattern STOP_CODE_SEARCH_PATTERN =
            Pattern.compile("^\\d{8}$");
    
    private static final String SERVICES_CHOOSER_DIALOG_TAG =
            "servicesChooserDialogTag";
    private static final String MAP_TYPE_CHOOSER_DIALOG_TAG =
            "mapTypeChooserDialogTag";
    private static final String PROGRESS_DIALOG_TAG = "progressDialog";
    
    private static final String LOADER_ARG_MIN_X = "minX";
    private static final String LOADER_ARG_MIN_Y = "minY";
    private static final String LOADER_ARG_MAX_X = "maxX";
    private static final String LOADER_ARG_MAX_Y = "maxY";
    private static final String LOADER_ARG_BEARING = "bearing";
    private static final String LOADER_ARG_ZOOM = "zoom";
    private static final String LOADER_ARG_FILTERED_SERVICES =
            "filteredServices";
    private static final String LOADER_ARG_QUERY = "query";
    
    private static final int LOADER_ID_BUS_STOPS = 0;
    private static final int LOADER_ID_GEO_SEARCH = 1;
    
    private BusStopDatabase bsd;
    private GoogleMap map;
    
    private ServicesChooserDialogFragment servicesChooser;
    private IndeterminateProgressDialogFragment progressDialog;
    private final HashMap<String, Marker> busStopMarkers =
            new HashMap<String, Marker>();
    private HashSet<Marker> geoSearchMarkers = new HashSet<Marker>();
    private String searchedBusStop = null;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Retain the instance to this Fragment.
        setRetainInstance(true);
        // This Fragment shows an options menu.
        setHasOptionsMenu(true);
        
        bsd = BusStopDatabase.getInstance(getActivity()
                .getApplicationContext());
        
        // The reference to the ServicesChooserDialogFragment should be held
        // throughout the lifecycle of this Fragment so that the user's choices
        // are remembered.
        servicesChooser = ServicesChooserDialogFragment.newInstance(
                bsd.getBusServiceList(),
                    getString(R.string.servicefilter_title), this);
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
                uiSettings.setZoomControlsEnabled(false);
                uiSettings.setRotateGesturesEnabled(false);
                uiSettings.setCompassEnabled(false);
                uiSettings.setMyLocationButtonEnabled(
                        Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB);
                
                map.setOnCameraChangeListener(this);
                map.setOnMarkerClickListener(this);
                map.setOnInfoWindowClickListener(this);
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(DEFAULT_LAT, DEFAULT_LONG), DEFAULT_ZOOM));
            }
        }
        
        refreshBusStops(null);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        
        map.setMyLocationEnabled(
                getActivity().getSharedPreferences(
                        PreferencesActivity.PREF_FILE, 0)
                    .getBoolean(PreferencesActivity.PREF_AUTO_LOCATION, true));
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
        
        final MenuItem item =
                menu.findItem(R.id.busstopmap_option_menu_trafficview);
        if(map != null && map.isTrafficEnabled()) {
            item.setTitle(R.string.map_menu_mapoverlay_trafficviewoff);
        } else {
            item.setTitle(R.string.map_menu_mapoverlay_trafficviewon);
        }
        
        if(map == null) {
            item.setEnabled(false);
        }
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
            case R.id.busstopmap_option_menu_filter:
                // Show the services chooser Dialog.
                servicesChooser.show(getFragmentManager(),
                        SERVICES_CHOOSER_DIALOG_TAG);
                return true;
            case R.id.busstopmap_option_menu_maptype:
                MapTypeChooserDialogFragment.newInstance(this)
                        .show(getFragmentManager(),
                            MAP_TYPE_CHOOSER_DIALOG_TAG);
                return true;
            case R.id.busstopmap_option_menu_trafficview:
                // Toggle the traffic view.
                map.setTrafficEnabled(!map.isTrafficEnabled());
                getActivity().supportInvalidateOptionsMenu();
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
                final Intent intent = new Intent(getActivity(),
                        BusStopDetailsActivity.class);
                intent.putExtra(BusStopDetailsActivity.ARG_STOPCODE,
                        matcher.group(1));
                startActivity(intent);
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
                            bundle.getFloat(LOADER_ARG_BEARING),
                            bundle.getStringArray(LOADER_ARG_FILTERED_SERVICES)
                        );
                } else {
                    return new BusStopMarkerLoader(
                            getActivity(),
                            bundle.getDouble(LOADER_ARG_MIN_X),
                            bundle.getDouble(LOADER_ARG_MIN_Y),
                            bundle.getDouble(LOADER_ARG_MAX_X),
                            bundle.getDouble(LOADER_ARG_MAX_Y),
                            bundle.getFloat(LOADER_ARG_ZOOM),
                            bundle.getFloat(LOADER_ARG_BEARING)
                        );
                }
            case LOADER_ID_GEO_SEARCH:
                String query = bundle.getString(LOADER_ARG_QUERY);
                // Make sure the query arg is not null.
                if(query == null) {
                   query = "";
                }
                
                return new GeoSearchLoader(getActivity(), query);
            default:
                return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoadFinished(final Loader loader, final Object d) {
        switch(loader.getId()) {
            case LOADER_ID_BUS_STOPS:
                addBusStopMarkers((HashMap<String, MarkerOptions>)d);
                break;
            case LOADER_ID_GEO_SEARCH:
                addGeoSearchResults((HashSet<MarkerOptions>)d);
                break;
            default:
                break;
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
    public void onServicesChosen() {
        // If the user has chosen services in the services filter, force a
        // refresh of the marker icons.
        refreshBusStops(null);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onMapTypeChosen(final int mapType) {
        // When the user selects a new map type, change the map type.
        map.setMapType(mapType);
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
     * This methid is called by the underlying Activity when a search has been
     * initiated.
     * 
     * @param searchTerm What to search for.
     */
    public void onSearch(final String searchTerm) {
        final Matcher m = STOP_CODE_SEARCH_PATTERN.matcher(searchTerm);
        
        if(m.matches()) {
            // If the searchTerm is a stop code, then move the camera to the bus
            // stop.
            searchedBusStop = searchTerm;
            final LatLng location = bsd.getLatLngForStopCode(searchTerm);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(location,
                    DEFAULT_SEARCH_ZOOM));
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
            // Show the progress Dialog.
            progressDialog = IndeterminateProgressDialogFragment
                    .newInstance(this,
                        getString(R.string.busstopmapfragment_progress_message,
                        new Object[] { searchTerm }));
            progressDialog.show(getFragmentManager(), PROGRESS_DIALOG_TAG);
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
        b.putFloat(LOADER_ARG_BEARING, position.bearing);
        b.putFloat(LOADER_ARG_ZOOM, position.zoom);

        final String[] chosenServices = servicesChooser.getChosenServices();
        // If there are chosen services, then set the filtered services
        // argument.
        if(chosenServices.length > 0) {
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
        if(progressDialog != null) {
            progressDialog.dismissAllowingStateLoss();
            progressDialog = null;
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
                
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        marker.getPosition(), DEFAULT_SEARCH_ZOOM));
                marker.showInfoWindow();
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
        final Location myLocation = map.getMyLocation();
        
        if(myLocation != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(myLocation.getLatitude(),
                            myLocation.getLongitude()), DEFAULT_SEARCH_ZOOM));
        } else if(verbose) {
            Toast.makeText(getActivity(),
                    R.string.busstopmapfragment_location_unknown,
                    Toast.LENGTH_LONG).show();
        }
    }
}