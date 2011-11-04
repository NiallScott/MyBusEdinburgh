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
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;
import java.util.ArrayList;
import uk.org.rivernile.edinburghbustracker.android.alerts.AlertManager;

public class BusStopMapActivity extends MapActivity implements
        OnItemClickListener {

    public static final int SHOW_STOPS = 1;

    public static final int DIALOG_PROGRESS = 0;
    public static final int DIALOG_SEARCH_RESULTS = 1;
    private static final int DIALOG_FILTER = 2;
    private static final int DIALOG_BUSSTOP = 3;
    private static final int DIALOG_CONFIRM_REM_FAV = 4;
    private static final int DIALOG_INSTALL_STREET_VIEW = 5;
    private static final int DIALOG_ADD_PROX_ALERT = 6;
    private static final int DIALOG_REM_PROX_ALERT = 7;
    private static final int DIALOG_ADD_TIME_ALERT = 8;
    private static final int DIALOG_REM_TIME_ALERT = 9;

    private static final int DEFAULT_LAT = 55948611;
    private static final int DEFAULT_LONG = -3199811;
    private static final int DEFAULT_ZOOM = 12;
    
    private final static int ZOOM_LEVEL_STANDARD = 15;
    private final static int ZOOM_LEVEL_FILTERED = 9;

    private MapView mapView;
    protected MyLocationOverlayFix myLocation;
    private BusStopMapOverlay stopOverlay;
    private String searchTerm;
    private MapSearchHelper searcher;
    private ServiceFilter serviceFilter = null;
    private View stopDialogView;
    private ArrayAdapter<String> stopDialogAdapter;
    private boolean showStopDialog = false;
    
    private BusStopDatabase bsd;
    private SettingsDatabase sd;
    private AlertManager alertMan;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.busstopmap);
        
        bsd = BusStopDatabase.getInstance(getApplicationContext());
        sd = SettingsDatabase.getInstance(getApplicationContext());
        alertMan = AlertManager.getInstance(getApplicationContext());
        serviceFilter = ServiceFilter.getInstance(this);

        mapView = (MapView)findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        myLocation = new MyLocationOverlayFix(this, mapView);
        mapView.getOverlays().add(myLocation);
        
        stopOverlay = new BusStopMapOverlay(getResources().getDrawable(
                R.drawable.mapmarker));
        mapView.getOverlays().add(stopOverlay);
        
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
            } else if(intent.hasExtra("stopCode") && intent.hasExtra("zoom")) {
                String stopCode = intent.getStringExtra("stopCode");
                GeoPoint gp = bsd.getGeoPointForStopCode(stopCode);
                
                if(gp != null) {
                    mapView.getController().setCenter(gp);
                    mapView.getController().setZoom(intent.getIntExtra("zoom",
                            12));
                    stopOverlay.setCurrentStopCode(stopCode);
                    showDialog(DIALOG_BUSSTOP);
                }
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
        
        if(savedInstanceState != null &&
                savedInstanceState.containsKey("currentSelectedStopCode"))
            stopOverlay.setCurrentStopCode(savedInstanceState
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
        
        serviceFilter = ServiceFilter.getInstance(this);
        serviceFilter.setCallback(stopOverlay);
        
        if(showStopDialog) {
            showDialog(DIALOG_BUSSTOP);
            showStopDialog = false;
        }
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

        if(stopOverlay.currentItem != null) {
            String stopCode = stopOverlay.currentItem.getStopCode();
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.busstopmap_option_menu, menu);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem item = menu.findItem(R.id.busstopmap_option_menu_mylocation);
        item.setEnabled(getSharedPreferences(PreferencesActivity.PREF_FILE, 0)
                .getBoolean("pref_autolocation_state", true));
        
        item = menu.findItem(R.id.busstopmap_option_menu_maptype);
        if(mapView.isSatellite()) {
            item.setTitle(R.string.map_menu_maptype_mapview)
                    .setIcon(R.drawable.ic_menu_mapmode);
        } else {
            item.setTitle(R.string.map_menu_maptype_satellite)
                    .setIcon(R.drawable.ic_menu_satview);
        }
        
        item = menu.findItem(R.id.busstopmap_option_menu_trafficview);
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
            case R.id.busstopmap_option_menu_mylocation:
                GeoPoint ml = myLocation.getMyLocation();
                if(ml != null) {
                    mapView.getController().setCenter(ml);
                    mapView.getController().setZoom(17);
                } else {
                    Toast.makeText(this, R.string.map_location_unknown,
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.busstopmap_option_menu_search:
                onSearchRequested();
                break;
            case R.id.busstopmap_option_menu_maptype:
                mapView.setSatellite(!mapView.isSatellite());
                break;
            case R.id.busstopmap_option_menu_trafficview:
                mapView.setTraffic(!mapView.isTraffic());
                break;
            case R.id.busstopmap_option_menu_filter:
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
        Dialog d;
        
        switch(id) {
            case DIALOG_PROGRESS:
                ProgressDialog prog = new ProgressDialog(this);
                prog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                prog.setCancelable(true);
                prog.setMessage(getString(R.string.map_search_progress_dialog));
                prog.setOnCancelListener(new DialogInterface
                        .OnCancelListener() {
                    public void onCancel(DialogInterface di) {
                        searcher.cancel();
                    }
                });
                return prog;
            case DIALOG_SEARCH_RESULTS:
                AlertDialog.Builder ad = new AlertDialog.Builder(this);
                LayoutInflater inflater = (LayoutInflater)
                        getSystemService(LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(
                        R.layout.map_search_results_list, null);
                ListView lv = (ListView)layout.findViewById(
                        R.id.listMapSearchResults);
                lv.setAdapter(searcher.searchResults);
                lv.setOnItemClickListener(searchClickListener);
                ad.setTitle(R.string.map_search_dialog_title);
                ad.setView(layout);
                ad.setNegativeButton(R.string.close,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                            final int id) {
                        dialog.dismiss();
                    }
                });
                return ad.create();
            case DIALOG_FILTER:
                return serviceFilter.getFilterDialog();
            case DIALOG_BUSSTOP:
                LayoutInflater vi = (LayoutInflater)getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                stopDialogView = vi.inflate(R.layout.mapdialog, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(true).setTitle("!");
                builder.setNegativeButton(R.string.close,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                            final int id) {
                        dialog.dismiss();
                    }
                });
                builder.setView(stopDialogView);
                d = builder.create();

                ListView list = (ListView)stopDialogView.findViewById(
                        R.id.mapdialog_list_options);
                stopDialogAdapter = new ArrayAdapter<String>(d.getContext(),
                        android.R.layout.simple_list_item_1);
                list.setAdapter(stopDialogAdapter);
                list.setOnItemClickListener(this);
                
                return d;
            case DIALOG_CONFIRM_REM_FAV:
                return createConfirmDeleteFavouriteDialog();
            case DIALOG_INSTALL_STREET_VIEW:
                return createInstallStreetViewDialog();
            case DIALOG_ADD_PROX_ALERT:
                d = alertMan.getAddProxAlertDialog(this);
                setDialogToDismissAndShowBusStopDialog(d);
                return d;
            case DIALOG_REM_PROX_ALERT:
                d = alertMan.getConfirmDeleteProxAlertDialog(this);
                setDialogToDismissAndShowBusStopDialog(d);
                return d;
            case DIALOG_ADD_TIME_ALERT:
                d = alertMan.getAddTimeAlertDialog(this);
                setDialogToDismissAndShowBusStopDialog(d);
                return d;
            case DIALOG_REM_TIME_ALERT:
                d = alertMan.getConfirmDeleteTimeAlertDialog(this);
                setDialogToDismissAndShowBusStopDialog(d);
                return d;
            default:
                return null;
        }
    }
    
    @Override
    protected void onPrepareDialog(final int id, final Dialog d) {
        switch(id) {
            case DIALOG_BUSSTOP:
                if(stopOverlay.currentItem == null) return;

                String stopCode = stopOverlay.currentItem.getStopCode();

                boolean isFavourite = sd.getFavouriteStopExists(stopCode);

                String services = bsd.getBusServicesForStopAsString(stopCode);
                TextView tv = (TextView)stopDialogView.findViewById(
                        R.id.mapdialog_text_services);
                d.setTitle(stopOverlay.currentItem.getStopName() + " (" +
                        stopCode + ")");
                if(services == null) {
                    tv.setText(R.string.map_dialog_noservices);
                } else {
                    tv.setText(services);
                }

                stopDialogAdapter.clear();
                stopDialogAdapter.add(getString(R.string.map_dialog_showtimes));

                if(isFavourite) {
                    stopDialogAdapter.add(getString(
                            R.string.displaystopdata_menu_remfav));
                } else {
                    stopDialogAdapter.add(
                            getString(R.string.displaystopdata_menu_addfav));
                }

                if(sd.isActiveProximityAlert(stopCode)) {
                    stopDialogAdapter.add(getString(R.string.alert_prox_rem));
                } else {
                    stopDialogAdapter.add(getString(R.string.alert_prox_add));
                }
                
                if(sd.isActiveTimeAlert(stopCode)) {
                    stopDialogAdapter.add(getString(R.string.alert_time_rem));
                } else {
                    stopDialogAdapter.add(getString(R.string.alert_time_add));
                }

                stopDialogAdapter.add(getString(
                        R.string.map_dialog_streetview));
                break;
            case DIALOG_ADD_PROX_ALERT:
                alertMan.editAddProxAlertDialog(stopOverlay.currentItem
                        .stopCode, (AlertDialog)d);
                break;
            case DIALOG_ADD_TIME_ALERT:
                alertMan.editAddTimeAlertDialog(stopOverlay.currentItem
                        .stopCode, (AlertDialog)d, null);
                break;
            default:
                break;
        }
    }
    
    private void setDialogToDismissAndShowBusStopDialog(final Dialog d) {
        if(d == null)
            throw new IllegalArgumentException("The dialog cannot be null.");
        
        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(final DialogInterface dialog) {
                dialog.dismiss();
                showDialog(DIALOG_BUSSTOP);
            }
        });
    }
    
    private AlertDialog createInstallStreetViewDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true).setInverseBackgroundForced(true)
                .setTitle(R.string.map_streetview_dialog_title)
                .setMessage(R.string.map_streetview_dialog_message)
                .setPositiveButton(R.string.yes,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog,
                                final int id) {
                            try {
                                startActivity(new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id=com." +
                                        "google.android.street")));
                            } catch(ActivityNotFoundException e) {
                                Toast.makeText(BusStopMapActivity.this,
                                        R.string.map_streetview_error_nomarket,
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                }).setNegativeButton(R.string.no,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog,
                                final int id) {
                            dialog.dismiss();
                        }
                });
        return builder.create();
    }
    
    private AlertDialog createConfirmDeleteFavouriteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true)
            .setTitle(R.string.favouritestops_dialog_confirm_title)
            .setPositiveButton(R.string.okay,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int id)
                {
                    sd.deleteFavouriteStop(stopOverlay.currentItem
                            .getStopCode());
                }
            }).setNegativeButton(R.string.cancel,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int id)
                {
                    dialog.dismiss();
                }
            }
        );
        
        AlertDialog d = builder.create();
        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(final DialogInterface dialog) {
                dialog.dismiss();
                showDialog(DIALOG_BUSSTOP);
            }
        });
        return d;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onItemClick(final AdapterView<?> l, final View view,
            final int position, final long id)
    {
        Intent intent;
        String stopCode = stopOverlay.currentItem.getStopCode();
        
        switch(position) {
            case 0:
                intent = new Intent(this, DisplayStopDataActivity.class);
                intent.setAction(DisplayStopDataActivity.ACTION_VIEW_STOP_DATA);
                intent.putExtra("stopCode", stopCode);
                startActivity(intent);
                showStopDialog = true;
                dismissDialog(DIALOG_BUSSTOP);
                break;
            case 1:
                if(sd.getFavouriteStopExists(stopCode)) {
                    showDialog(DIALOG_CONFIRM_REM_FAV);
                } else {
                    intent = new Intent(this,
                            AddEditFavouriteStopActivity.class);
                    intent.setAction(AddEditFavouriteStopActivity
                            .ACTION_ADD_EDIT_FAVOURITE_STOP);
                    intent.putExtra("stopCode", stopCode);
                    intent.putExtra("stopName", stopOverlay.currentItem
                            .getStopName());
                    startActivity(intent);
                }
                showStopDialog = true;
                dismissDialog(DIALOG_BUSSTOP);
                break;
            case 2:
                if(sd.isActiveProximityAlert(stopCode)) {
                    showDialog(DIALOG_REM_PROX_ALERT);
                } else {
                    showDialog(DIALOG_ADD_PROX_ALERT);
                }
                break;
            case 3:
                if(sd.isActiveTimeAlert(stopCode)) {
                    showDialog(DIALOG_REM_TIME_ALERT);
                } else {
                    showDialog(DIALOG_ADD_TIME_ALERT);
                }
                break;
            case 4:
                GeoPoint g = bsd.getGeoPointForStopCode(stopCode);
                if(g != null) {
                    try {
                        StringBuilder sb = new StringBuilder();
                        sb.append("google.streetview:cbll=");
                        sb.append((double)(g.getLatitudeE6() / 1E6));
                        sb.append(',');
                        sb.append((double)(g.getLongitudeE6() / 1E6));
                        sb.append("&cbp=1,0,,0,1.0&mz=19");
                    
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse(sb.toString())));
                    } catch(ActivityNotFoundException e) {
                        showDialog(DIALOG_INSTALL_STREET_VIEW);
                    }
                }
                break;
        }
    }
    
    private OnItemClickListener searchClickListener =
            new OnItemClickListener() {
        @Override
        public void onItemClick(final AdapterView<?> l, final View view,
                final int position, final long id) {
            MapSearchHelper.SearchResult sr = searcher.searchResults
                    .getItem(position);
            mapView.getController().setCenter(sr.geoPoint);
            mapView.getController().setZoom(19);
            dismissDialog(DIALOG_SEARCH_RESULTS);
        }
    };
    
    protected class BusStopMapOverlay
            extends ItemizedOverlay<BusStopOverlayItem>
            implements Filterable {
        
        public BusStopOverlayItem currentItem;
        
        private int lastZoom;
        private GeoPoint lastCenter;
        
        private ArrayList<BusStopOverlayItem> items =
                new ArrayList<BusStopOverlayItem>();
        
        public BusStopMapOverlay(final Drawable defaultMarker) {
            super(boundCenterBottom(defaultMarker));
        }
        
        @Override
        protected BusStopOverlayItem createItem(final int i) {
            return items.get(i);
        }
        
        @Override
        public int size() {
            return items.size();
        }
        
        @Override
        protected boolean onTap(final int index) {
            currentItem = items.get(index);
            mapView.getController().animateTo(currentItem.getPoint());
            showDialog(DIALOG_BUSSTOP);
            return true;
        }
        
        private void doPopulateBusStops() {
            setLastFocusedIndex(-1);
            items.clear();
            populate();
            final boolean filtered = serviceFilter.isFiltered();
            int zoomLevel = filtered ? ZOOM_LEVEL_FILTERED :
                    ZOOM_LEVEL_STANDARD;
            if(mapView.getZoomLevel() >= zoomLevel) {
                int minX, minY, maxX, maxY, latSpan, longSpan;
                GeoPoint g = mapView.getMapCenter();
                latSpan = (mapView.getLatitudeSpan() / 2) + 1;
                longSpan = (mapView.getLongitudeSpan() / 2) + 1;
                minX = g.getLatitudeE6() + latSpan;
                minY = g.getLongitudeE6() - longSpan;
                maxX = g.getLatitudeE6() - latSpan;
                maxY = g.getLongitudeE6() + longSpan;
                Cursor c;
                
                if(filtered) {
                    c = bsd.getFilteredStopsByCoords(minX, minY, maxX, maxY,
                            serviceFilter.toString());
                } else {
                    c = bsd.getBusStopsByCoords(minX, minY, maxX, maxY);
                }
                
                while(c.moveToNext()) {
                    items.add(new BusStopOverlayItem(new GeoPoint(
                            c.getInt(2), c.getInt(3)), c.getString(0),
                            c.getString(1)));
                }
                c.close();
                populate();
            }
        }
        
        @Override
        public void draw(final Canvas canvas, final MapView mapView,
                final boolean shadow) {
            super.draw(canvas, mapView, shadow);
            int zoom = mapView.getZoomLevel();
            GeoPoint g = mapView.getMapCenter();

            if(zoom != lastZoom || !g.equals(lastCenter)) {
                lastZoom = zoom;
                lastCenter = g;
                doPopulateBusStops();
            }
        }
        
        public void setCurrentStopCode(final String stopCode) {
            if(currentItem != null) return;

            Cursor c = bsd.getBusStopByCode(stopCode);
            if(c.getCount() != 1) return;
            c.moveToFirst();
            currentItem = new BusStopOverlayItem(new GeoPoint(c.getInt(2),
                    c.getInt(3)), c.getString(0), c.getString(1));
            c.close();
        }
        
        @Override
        public void onFilterSet() {
            doPopulateBusStops();
            mapView.invalidate();
        }
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
    
    public class BusStopOverlayItem extends OverlayItem {

        private String stopCode;
        private String stopName;

        public BusStopOverlayItem(final GeoPoint point, final String stopCode,
                final String stopName)
        {
            super(point, stopCode, stopName);
            this.stopCode = stopCode;
            this.stopName = stopName;
        }

        public String getStopCode() {
            return stopCode;
        }

        public String getStopName() {
            return stopName;
        }
    }
}