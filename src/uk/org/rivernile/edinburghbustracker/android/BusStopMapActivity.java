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

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class BusStopMapActivity extends MapActivity
        implements OnItemClickListener {

    private static final int MENU_MYLOCATION = 0;
    private static final int MENU_MAPTYPE = 1;
    private static final int MENU_OVERLAY_TRAFFICVIEW = 2;

    private MapView mapView;
    private MyLocationOverlay myLocation;
    private BusStopMapOverlay stopOverlay;
    private boolean createdDialog = false;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.busstopmap);
        mapView = (MapView)findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.getController().setCenter(new GeoPoint(55948611, -3199811));
        mapView.getController().setZoom(12);
        myLocation = new MyLocationOverlay(this, mapView);
        Toast.makeText(this, R.string.map_finding_location, Toast.LENGTH_LONG)
                .show();
        myLocation.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                mapView.getController().setCenter(
                        BusStopMapActivity.this.myLocation.getMyLocation());
                mapView.getController().setZoom(17);
                if(createdDialog) dismissDialog(0);
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

        menu.add(0, MENU_MYLOCATION, 1, R.string.map_menu_mylocation).setIcon(
                android.R.drawable.ic_menu_mylocation);
        if(mapView.isSatellite()) {
            menu.add(0, MENU_MAPTYPE, 2, R.string.map_menu_maptype_mapview);
        } else {
            menu.add(0, MENU_MAPTYPE, 2, R.string.map_menu_maptype_satellite);
        }
        if(mapView.isTraffic()) {
            menu.add(0, MENU_OVERLAY_TRAFFICVIEW, 3,
                R.string.map_menu_mapoverlay_trafficviewoff);
        } else {
            menu.add(0, MENU_OVERLAY_TRAFFICVIEW, 3,
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
            case MENU_MYLOCATION:
                GeoPoint ml = myLocation.getMyLocation();
                if(ml != null) {
                    mapView.getController().setCenter(ml);
                    mapView.getController().setZoom(17);
                } else {
                    Toast.makeText(this, R.string.map_location_unknown,
                            Toast.LENGTH_LONG).show();
                }
                break;
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

    @Override
    protected Dialog onCreateDialog(final int id) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.mapdialog);
        dialog.setCancelable(true);
        ListView list =
                (ListView)dialog.findViewById(R.id.mapdialog_list_options);
        ArrayAdapter ad = new ArrayAdapter<String>(dialog.getContext(),
                android.R.layout.simple_list_item_1);
        ad.add(getString(R.string.map_dialog_showtimes));
        ad.add(getString(R.string.map_dialog_close));
        list.setAdapter(ad);
        list.setOnItemClickListener(this);
        createdDialog = true;
        return dialog;
    }

    @Override
    protected void onPrepareDialog(final int id, final Dialog dialog) {
        BusStopOverlayItem oi = stopOverlay.getSelectedItem();
        String[] services = BusStopDatabase.getInstance(this)
                .getBusServicesForStop(oi.getStopCode());
        TextView tv = (TextView)dialog.findViewById(
                R.id.mapdialog_text_services);
        dialog.setTitle(oi.getStopCode() + " " + oi.getStopName());
        if(services == null) {
            tv.setText(R.string.map_dialog_noservices);
        } else {
            String s = "";
            for(int i = 0; i < services.length; i++) {
                if(i == (services.length - 1)) {
                    s = s + services[i];
                } else {
                    s = s + services[i] + ", ";
                }
            }
            tv.setText(s);
        }
    }

    @Override
    public void onItemClick(final AdapterView<?> l, final View view,
            final int position, final long id)
    {
        //String stopName = stopOverlay.getSeletedItem().getStopName();
        switch(position) {
            case 0:
                Intent intent = new Intent(this, DisplayStopDataActivity.class);
                intent.setAction(DisplayStopDataActivity.ACTION_VIEW_STOP_DATA);
                intent.putExtra("stopCode",
                        stopOverlay.getSelectedItem().getStopCode());
                startActivity(intent);
                break;
            case 1:
                dismissDialog(0);
                break;
        }
    }
}