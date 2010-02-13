/*
 * Copyright (C) 2009 Niall 'Rivernile' Scott
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

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class BusStopMapActivity extends MapActivity {

    private static final int MENU_MAPTYPE = 0;
    private static final int MENU_OVERLAY_TRAFFICVIEW = 1;

    private MapView mapView;
    private MyLocationOverlay myLocation;
    private BusStopMapOverlay stopOverlay;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.busstopmap);
        mapView = (MapView)findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.getController().setCenter(new GeoPoint(55948611, -3199811));
        mapView.getController().setZoom(12);
        myLocation = new MyLocationOverlay(this, mapView);
        myLocation.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                mapView.getController().setCenter(
                        BusStopMapActivity.this.myLocation.getMyLocation());
                mapView.getController().setZoom(17);
            }
        });
        mapView.getOverlays().add(myLocation);
        stopOverlay = new BusStopMapOverlay(
                getResources().getDrawable(R.drawable.mapmarker), this,
                mapView);
        stopOverlay.doPopulateBusStops();
        mapView.getOverlays().add(stopOverlay);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        myLocation.enableMyLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        myLocation.disableMyLocation();
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);

        if(mapView.isSatellite()) {
            menu.add(0, MENU_MAPTYPE, 1, R.string.map_menu_maptype_mapview);
        } else {
            menu.add(0, MENU_MAPTYPE, 1, R.string.map_menu_maptype_satellite);
        }
        if(mapView.isTraffic()) {
            menu.add(0, MENU_OVERLAY_TRAFFICVIEW, 2,
                R.string.map_menu_mapoverlay_trafficviewoff);
        } else {
            menu.add(0, MENU_OVERLAY_TRAFFICVIEW, 2,
                R.string.map_menu_mapoverlay_trafficviewon);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.getItem(MENU_MAPTYPE);
        if(mapView.isSatellite()) {
            item.setTitle(R.string.map_menu_maptype_mapview);
        } else {
            item.setTitle(R.string.map_menu_maptype_satellite);
        }
        item = menu.getItem(MENU_OVERLAY_TRAFFICVIEW);
        if(mapView.isTraffic()) {
            item.setTitle(R.string.map_menu_mapoverlay_trafficviewoff);
        } else {
            item.setTitle(R.string.map_menu_mapoverlay_trafficviewon);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case MENU_MAPTYPE:
                mapView.setSatellite(!mapView.isSatellite());
                break;
            case MENU_OVERLAY_TRAFFICVIEW:
                if(mapView.isTraffic()) {
                    mapView.setTraffic(false);
                } else {
                    mapView.setTraffic(true);
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}