/*
 * Copyright (C) 2009 - 2011 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import uk.org.rivernile.edinburghbustracker.android.mapoverlays
        .BusStopMapOverlay;

public class BusStopMapActivity extends MapActivity implements
        OnItemClickListener{

    public static final int SHOW_STOPS = 1;

    public static final int DIALOG_PROGRESS = 0;
    public static final int DIALOG_SEARCH_RESULTS = 1;
    public static final int DIALOG_FILTER = 2;

    private static final int MENU_MYLOCATION = 0;
    private static final int MENU_SEARCH = 1;
    private static final int MENU_MAPTYPE = 2;
    private static final int MENU_OVERLAY_TRAFFICVIEW = 3;
    private static final int MENU_FILTER = 4;

    private static final int DEFAULT_LAT = 55948611;
    private static final int DEFAULT_LONG = -3199811;
    private static final int DEFAULT_ZOOM = 12;

    private MapView mapView;
    protected MyLocationOverlayFix myLocation;

    private BusStopMapOverlay stopOverlay;

    private String searchTerm;
    private MapSearchHelper searcher;
    private ServiceFilter serviceFilter = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.busstopmap);

        mapView = (MapView)findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        myLocation = new MyLocationOverlayFix(this, mapView);
        mapView.getOverlays().add(myLocation);
        Intent intent = getIntent();

        if(savedInstanceState != null &&
                savedInstanceState.containsKey("currentLat") &&
                savedInstanceState.containsKey("currentLong") &&
                savedInstanceState.containsKey("currentZoom"))
        {
            mapView.getController().setCenter(new GeoPoint(savedInstanceState
                    .getInt("currentLat", DEFAULT_LAT), savedInstanceState
                    .getInt("currentLong", DEFAULT_LONG)));
            mapView.getController().setZoom(savedInstanceState
                    .getInt("currentZoom", DEFAULT_ZOOM));
        } else {
            if(intent.hasExtra("lat") && intent.hasExtra("long") &&
                    intent.hasExtra("zoom"))
            {
                mapView.getController().setCenter(new GeoPoint(
                        intent.getIntExtra("lat", DEFAULT_LAT),
                        intent.getIntExtra("long", DEFAULT_LONG)));
                mapView.getController().setZoom(intent.getIntExtra("zoom", 12));
            } else {
                mapView.getController().setCenter(new GeoPoint(DEFAULT_LAT,
                    DEFAULT_LONG));
                mapView.getController().setZoom(12);
                myLocation.runOnFirstFix(new Runnable() {
                    @Override
                    public void run() {
                        mapView.getController().setCenter(myLocation
                                .getMyLocation());
                        mapView.getController().setZoom(17);
                    }
                });
            }
        }

        stopOverlay = new BusStopMapOverlay(getResources().getDrawable(
                R.drawable.mapmarker), this, mapView);
        mapView.getOverlays().add(stopOverlay);
        if(savedInstanceState != null &&
                savedInstanceState.containsKey("currentSelectedStopCode"))
            stopOverlay.setCurrentStopCodeAndShowDialog(savedInstanceState
                        .getString("currentSelectedStopCode"));

        searcher = MapSearchHelper.getInstance(this);
        if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
            searchTerm = intent.getStringExtra(SearchManager.QUERY);
            searcher.doSearch(searchTerm);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();

        if(getSharedPreferences(PreferencesActivity.PREF_FILE, 0)
                .getBoolean("pref_autolocation_state", true)) {
            myLocation.enableMyLocation();
        }
        if(stopOverlay != null) stopOverlay.mapResumed();
        
        serviceFilter = ServiceFilter.getInstance(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();
        myLocation.disableMyLocation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);

        if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
            searchTerm = intent.getStringExtra(SearchManager.QUERY);
            searcher.doSearch(searchTerm);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        GeoPoint g = mapView.getMapCenter();

        outState.putInt("currentLat", g.getLatitudeE6());
        outState.putInt("currentLong", g.getLongitudeE6());
        outState.putInt("currentZoom", mapView.getZoomLevel());

        if(stopOverlay != null) {
            String stopCode = stopOverlay.getCurrentStopCode();
            if(stopCode != null) outState.putString("currentSelectedStopCode",
                    stopCode);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRouteDisplayed() { return false; }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, MENU_MYLOCATION, 1, R.string.map_menu_mylocation).setIcon(
                R.drawable.ic_menu_mylocation);
        menu.add(0, MENU_SEARCH, 2, R.string.search)
                .setIcon(R.drawable.ic_menu_search);
        menu.add(0, MENU_MAPTYPE, 3, R.string.map_menu_maptype_mapview)
                    .setIcon(R.drawable.ic_menu_mapmode);
        menu.add(0, MENU_OVERLAY_TRAFFICVIEW, 4,
                R.string.map_menu_mapoverlay_trafficviewoff)
                .setIcon(R.drawable.ic_menu_trafficview);
        menu.add(0, MENU_FILTER, 5, R.string.map_menu_filter)
                .setIcon(R.drawable.ic_menu_filter);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem item = menu.getItem(MENU_MYLOCATION);
        item.setEnabled(getSharedPreferences(PreferencesActivity.PREF_FILE, 0)
                .getBoolean("pref_autolocation_state", true));
        item = menu.getItem(MENU_MAPTYPE);
        if(mapView.isSatellite()) {
            item.setTitle(R.string.map_menu_maptype_mapview)
                    .setIcon(R.drawable.ic_menu_mapmode);
        } else {
            item.setTitle(R.string.map_menu_maptype_satellite)
                    .setIcon(R.drawable.ic_menu_satview);
        }
        item = menu.getItem(MENU_OVERLAY_TRAFFICVIEW);
        if(mapView.isTraffic()) {
            item.setTitle(R.string.map_menu_mapoverlay_trafficviewoff);
        } else {
            item.setTitle(R.string.map_menu_mapoverlay_trafficviewon);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch(item.getItemId()) {
            case MENU_MYLOCATION:
                GeoPoint ml = myLocation.getMyLocation();
                if(ml != null) {
                    mapView.getController().setCenter(ml);
                    mapView.getController().setZoom(17);
                } else {
                    Toast.makeText(this, R.string.map_location_unknown,
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case MENU_SEARCH:
                onSearchRequested();
                break;
            case MENU_MAPTYPE:
                mapView.setSatellite(!mapView.isSatellite());
                break;
            case MENU_OVERLAY_TRAFFICVIEW:
                mapView.setTraffic(!mapView.isTraffic());
                break;
            case MENU_FILTER:
                showDialog(DIALOG_FILTER);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Dialog onCreateDialog(final int id) {
        switch(id) {
            case DIALOG_PROGRESS:
                ProgressDialog prog = new ProgressDialog(this);
                prog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                prog.setCancelable(true);
                prog.setMessage(getString(R.string.map_search_progress_dialog));
                prog.setOnCancelListener(new DialogInterface
                        .OnCancelListener() {
                    public void onCancel(DialogInterface di) {
                        searcher.searchThread.interrupt();
                    }
                });
                return prog;
            case DIALOG_SEARCH_RESULTS:
                AlertDialog.Builder ad;
                LayoutInflater inflater = (LayoutInflater)
                        getSystemService(LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(
                        R.layout.map_search_results_list, null);
                ListView lv = (ListView)layout.findViewById(
                        R.id.listMapSearchResults);
                lv.setAdapter(searcher.searchResults);
                lv.setOnItemClickListener(this);
                ad = new AlertDialog.Builder(this);
                ad.setTitle(R.string.map_search_dialog_title);
                ad.setView(layout);
                return ad.create();
            case DIALOG_FILTER:
                if(serviceFilter == null)
                    serviceFilter = ServiceFilter.getInstance(this);
                return serviceFilter.getFilterDialog();
            default:
                return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onItemClick(final AdapterView<?> l, final View view,
            final int position, final long id) {
        MapSearchHelper.SearchResult sr = searcher.searchResults
                .getItem(position);
        mapView.getController().setCenter(sr.geoPoint);
        mapView.getController().setZoom(19);
        dismissDialog(DIALOG_SEARCH_RESULTS);
    }

    /**
     * This private class here is a hacky fix to get around the Google Maps API
     * crashing when drawing the user's location on the map. This crash is
     * caused by a class missing internally within Google Maps, therefore the
     * code cannot be found and then the API just fails. At least with this,
     * the user can continue, albeit with a missing location dot on the map!
     */
    protected class MyLocationOverlayFix extends MyLocationOverlay {

        public MyLocationOverlayFix(final Context context,
                final MapView mapView) {
            super(context, mapView);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void drawMyLocation(final Canvas canvas,
                final MapView mapView, final Location lastFix,
                final GeoPoint myLocation, final long when) {
            try {
                super.drawMyLocation(canvas, mapView, lastFix, myLocation,
                        when);
            } catch(Exception e) {
                /*
                 * I really really really hate catching Exception, but I have
                 * to make an exception (lol) in this case because it appears a
                 * few things cause the crash to happen. It is safe to catch the
                 * exception and let the application continue.
                 */
            }
        }
    }
}