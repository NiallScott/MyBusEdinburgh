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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.text.NumberFormat;
import java.util.List;
import uk.org.rivernile.android.utils.SimpleCursorLoader;
import uk.org.rivernile.edinburghbustracker.android
        .AddEditFavouriteStopActivity;
import uk.org.rivernile.edinburghbustracker.android.AddProximityAlertActivity;
import uk.org.rivernile.edinburghbustracker.android.AddTimeAlertActivity;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;
import uk.org.rivernile.edinburghbustracker.android.DisplayStopDataActivity;
import uk.org.rivernile.edinburghbustracker.android.PreferencesActivity;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.edinburghbustracker.android.SettingsDatabase;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .DeleteFavouriteDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .DeleteProximityAlertDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .DeleteTimeAlertDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .InstallStreetViewDialogFragment;

/**
 * This Fragment shows details for a bus stop. The bus stop code is passed in
 * as an argument to this Fragment. A Map is shown at the top of the Fragment
 * if the Google Play Services are available, otherwise it is removed.
 * 
 * @author Niall Scott
 */
public class BusStopDetailsFragment extends ListFragment
        implements LocationListener, LoaderManager.LoaderCallbacks<Cursor>,
        DeleteProximityAlertDialogFragment.EventListener,
        DeleteTimeAlertDialogFragment.EventListener,
        DeleteFavouriteDialogFragment.EventListener {
    
    private static final NumberFormat distanceFormat =
            NumberFormat.getInstance();
    
    /** This is the stopCode argument. */
    public static final String ARG_STOPCODE = "stopCode";
    
    private static final int REQUEST_PERIOD = 10000;
    private static final float MIN_DISTANCE = 3.0f;
    private static final float MAP_ZOOM = 15f;
    
    private static final String STATE_KEY_STOPCODE = "stopCode";
    private static final String STATE_KEY_STOPNAME = "stopName";
    private static final String STATE_KEY_LATITUDE = "latitude";
    private static final String STATE_KEY_LONGITUDE = "longitude";
    private static final String STATE_KEY_ORIENTATION = "orientation";
    private static final String STATE_KEY_LOCALITY = "locality";
    
    private static final String DELETE_TIME_ALERT_DIALOG_TAG =
            "delTimeAlertDialog";
    private static final String DELETE_PROX_ALERT_DIALOG_TAG =
            "delProxAlertDialog";
    private static final String DELETE_FAVOURITE_DIALOG_TAG = "delFavDialog";
    private static final String INSTALL_DIALOG_TAG = "installStreetViewDialog";
    
    private BusStopDatabase bsd;
    private SettingsDatabase sd;
    private LocationManager locMan;
    
    private MapView mapView;
    private GoogleMap map;
    private ImageButton favouriteBtn;
    private TextView txtName, txtServices, txtDistance;
    private ArrayAdapter<String> listAdapter;
    
    private Location lastLocation;
    private String stopCode;
    private String stopName;
    private double latitude;
    private double longitude;
    private int orientation;
    private String locality;
    
    /**
     * Get a new instance of this Fragment. A bus stop code must be supplied.
     * 
     * @param stopCode The bus stop code to show details for.
     * @return A new instance of this Fragment.
     */
    public static BusStopDetailsFragment newInstance(final String stopCode) {
        final BusStopDetailsFragment f = new BusStopDetailsFragment();
        final Bundle b = new Bundle();
        b.putString(ARG_STOPCODE, stopCode);
        f.setArguments(b);
        
        return f;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final Context context = getActivity().getApplicationContext();
        
        // Get the various resources and services.
        bsd = BusStopDatabase.getInstance(context);
        sd = SettingsDatabase.getInstance(context);
        locMan = (LocationManager)context
                .getSystemService(Context.LOCATION_SERVICE);
        
        if(savedInstanceState != null) {
            stopCode = savedInstanceState.getString(STATE_KEY_STOPCODE);
            stopName = savedInstanceState.getString(STATE_KEY_STOPNAME);
            latitude = savedInstanceState.getDouble(STATE_KEY_LATITUDE);
            longitude = savedInstanceState.getDouble(STATE_KEY_LONGITUDE);
            orientation = savedInstanceState.getInt(STATE_KEY_ORIENTATION);
            locality = savedInstanceState.getString(STATE_KEY_LOCALITY);
        }
        
        listAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {
        // This is the View that contains the ListView.
        final View v = inflater.inflate(R.layout.busstopdetails, container,
                false);
        // We need the ListView to add the headers.
        final ListView lv = (ListView)v.findViewById(android.R.id.list);
        // Inflate the headers.
        final View header1 = inflater.inflate(R.layout.busstopdetails_header_1,
                lv, false);
        final View header2 = inflater.inflate(R.layout.busstopdetails_header_2,
                lv, false);
        
        // Get the MapView and send it the onCreate event.
        mapView = (MapView)header1.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        
        txtName = (TextView)header1.findViewById(R.id.txtName);
        txtServices = (TextView)header1.findViewById(R.id.txtServices);
        txtDistance = (TextView)header1.findViewById(R.id.txtDistance);
        
        Button b = (Button)header2.findViewById(R.id.btnBusTimes);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                // Show the bus times.
                final Intent intent = new Intent(getActivity(),
                        DisplayStopDataActivity.class);
                intent.putExtra(DisplayStopDataActivity.ARG_STOPCODE,
                        getArguments().getString(ARG_STOPCODE));
                startActivity(intent);
            }
        });
        
        b = (Button)header2.findViewById(R.id.btnStreetView);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                // Show StreetView.
                final StringBuilder sb = new StringBuilder();
                sb.append("google.streetview:cbll=");
                sb.append(latitude);
                sb.append(',');
                sb.append(longitude);
                sb.append("&cbp=1,0,,0,1.0&mz=19");
                
                try {
                    // Attempt to show StreetView.
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(sb.toString())));
                } catch(ActivityNotFoundException e) {
                    // Ask the user if they want to install StreetView when it
                    // isn't already installed.
                    new InstallStreetViewDialogFragment().show(
                            getFragmentManager(), INSTALL_DIALOG_TAG);
                }
            }
        });
        
        favouriteBtn = (ImageButton)header1
                .findViewById(R.id.imgbtnFavourite);
        favouriteBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                // Add/remove as favourite.
                if(sd.getFavouriteStopExists(stopCode)) {
                    DeleteFavouriteDialogFragment.newInstance(stopCode,
                                BusStopDetailsFragment.this)
                            .show(getFragmentManager(),
                                DELETE_FAVOURITE_DIALOG_TAG);
                } else {
                    final Intent intent = new Intent(getActivity(),
                            AddEditFavouriteStopActivity.class);
                    intent.putExtra(AddEditFavouriteStopActivity.ARG_STOPCODE,
                            stopCode);
                    intent.putExtra(AddEditFavouriteStopActivity.ARG_STOPNAME,
                            stopName);
                    startActivity(intent);
                }
            }
        });
        
        // Add the headers to the ListView.
        lv.addHeaderView(header1);
        lv.addHeaderView(header2);
        
        return v;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        setListAdapter(listAdapter);
        
        // See http://android-developers.blogspot.com/2011/06/deep-dive-into-location.html
        // Get the most recent, most accurate cached Location.
        final List<String> matchingProviders = locMan.getAllProviders();
        float accuracy, bestAccuracy = Float.MAX_VALUE;
        long time, bestTime = Long.MIN_VALUE;
        Location location;
        
        // Loop through all providers.
        for(String provider : matchingProviders) {
            // Get the cached Location for this provider.
            location = locMan.getLastKnownLocation(provider);
            if(location != null) {
                // Get the accuracy and time of this Location.
                accuracy = location.getAccuracy();
                time = location.getTime();
        
                if(time > REQUEST_PERIOD && accuracy < bestAccuracy) {
                    lastLocation = location;
                    bestAccuracy = accuracy;
                    bestTime = time;
                } else if(time < REQUEST_PERIOD && 
                    bestAccuracy == Float.MAX_VALUE && time > bestTime) {
                    lastLocation = location;
                    bestTime = time;
                }
            }
        }
        
        updateLocation();
        
        map = mapView.getMap();
        // The Map can be null if Google Play Services is not available.
        if(map != null) {
            final UiSettings mapUi = map.getUiSettings();
            mapUi.setAllGesturesEnabled(false);
            mapUi.setCompassEnabled(false);
            mapUi.setMyLocationButtonEnabled(false);
            mapUi.setZoomControlsEnabled(false);
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else {
            // If the MapView is null, then hide it.
            mapView.setVisibility(View.GONE);
        }
        
        if(savedInstanceState == null) {
            // Start the bus stop information Loader.
            getLoaderManager().restartLoader(0, null, this);
        } else {
            // If there is already information, then just jump straight to
            // populateView()
            populateView();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        
        // Feed back life cycle events back to the MapView.
        mapView.onResume();
        
        // Get the best Location provider.
        final String provider = getBestLocationProvider();
        if(provider != null) {
            // Request location updates.
            locMan.requestLocationUpdates(provider, REQUEST_PERIOD,
                    MIN_DISTANCE, this);
        }
        
        if(map != null) {
            map.setMyLocationEnabled(
                    getActivity().getSharedPreferences(
                            PreferencesActivity.PREF_FILE, 0)
                        .getBoolean(PreferencesActivity.PREF_AUTO_LOCATION,
                                true));
        }
        
        if(sd.getFavouriteStopExists(getArguments().getString(ARG_STOPCODE))) {
            favouriteBtn.setBackgroundResource(R.drawable.ic_list_favourite);
        } else {
            favouriteBtn.setBackgroundResource(R.drawable.ic_list_unfavourite);
        }
        
        // Repopulate the list. Things might have changed since we were last
        // shown.
        populateList();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();
        
        // Feed back life cycle events back to the MapView.
        mapView.onPause();
        
        // When the Activity is being paused, cancel location updates.
        locMan.removeUpdates(this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        
        // Feed back life cycle events back to the MapView.
        mapView.onSaveInstanceState(outState);
        
        outState.putString(STATE_KEY_STOPCODE, stopCode);
        outState.putString(STATE_KEY_STOPNAME, stopName);
        outState.putDouble(STATE_KEY_LATITUDE, latitude);
        outState.putDouble(STATE_KEY_LONGITUDE, longitude);
        outState.putInt(STATE_KEY_ORIENTATION, orientation);
        outState.putString(STATE_KEY_LOCALITY, locality);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // Feed back life cycle events back to the MapView.
        mapView.onDestroy();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        
        // Feed back life cycle events back to the MapView.
        mapView.onLowMemory();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Loader<Cursor> onCreateLoader(final int i, final Bundle bundle) {
        return new BusStopDetailsLoader(getActivity(),
                getArguments().getString(ARG_STOPCODE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor c) {
        populateData(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        // Nothing to do here.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLocationChanged(final Location location) {
        lastLocation = location;
        updateLocation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusChanged(final String provider, final int status,
            final Bundle extras) {
        // Nothing to do here.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onProviderEnabled(final String provider) {
        // If GPS has been enabled, make sure we're using that.
        if(LocationManager.GPS_PROVIDER.equals(provider)) {
            locMan.removeUpdates(this);
            locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    REQUEST_PERIOD, MIN_DISTANCE, this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onProviderDisabled(final String provider) {
        // If GPS has been disabled, revert back to network location.
        if(LocationManager.GPS_PROVIDER.equals(provider)) {
            locMan.removeUpdates(this);
            locMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    REQUEST_PERIOD, MIN_DISTANCE, this);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onListItemClick(final ListView l, final View v,
            final int position, final long id) {
        Intent intent;
        
        switch(position) {
            case 2:
                if(sd.isActiveProximityAlert(stopCode)) {
                    DeleteProximityAlertDialogFragment.newInstance(this)
                            .show(getFragmentManager(),
                            DELETE_PROX_ALERT_DIALOG_TAG);
                } else {
                    intent = new Intent(getActivity(),
                            AddProximityAlertActivity.class);
                    intent.putExtra(AddProximityAlertActivity.ARG_STOPCODE,
                            stopCode);
                    startActivity(intent);
                }
                
                break;
            case 3:
                if(sd.isActiveTimeAlert(stopCode)) {
                    DeleteTimeAlertDialogFragment.newInstance(this)
                            .show(getFragmentManager(), 
                            DELETE_TIME_ALERT_DIALOG_TAG);
                } else {
                    intent = new Intent(getActivity(),
                            AddTimeAlertActivity.class);
                    intent.putExtra(AddTimeAlertActivity.ARG_STOPCODE,
                            stopCode);
                    startActivity(intent);
                }
                
                break;
            default:
                break;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfirmFavouriteDeletion() {
        favouriteBtn.setBackgroundResource(R.drawable.ic_list_unfavourite);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCancelFavouriteDeletion() {
        // Nothing to do.
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfirmProximityAlertDeletion() {
        populateList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCancelProximityAlertDeletion() {
        // Nothing to do.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfirmTimeAlertDeletion() {
        populateList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCancelTimeAlertDeletion() {
        // Nothing to do.
    }
    
    /**
     * Get the best location provider based on what is enabled. The following
     * are tried;
     * 
     * GPS -> Network -> None
     * 
     * @return The best location provider available.
     */
    public String getBestLocationProvider() {
        if(locMan.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER;
        } else if(locMan.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return LocationManager.NETWORK_PROVIDER;
        } else {
            return null;
        }
    }
    
    /**
     * Populate the ListView items with other actions that can be taken on the
     * bus stop.
     */
    private void populateList() {
        final String code = getArguments().getString(ARG_STOPCODE);
        listAdapter.clear();
        
        if(sd.isActiveProximityAlert(code)) {
            listAdapter.add(getString(R.string.alert_prox_rem));
        } else {
            listAdapter.add(getString(R.string.alert_prox_add));
        }
        
        if(sd.isActiveTimeAlert(code)) {
            listAdapter.add(getString(R.string.alert_time_rem));
        } else {
            listAdapter.add(getString(R.string.alert_time_add));
        }
    }
    
    /**
     * This is called when the Loader has finished. It moves the data out of the
     * Cursor and in to local fields.
     * 
     * @param c The returned Cursor.
     */
    private void populateData(final Cursor c) {
        if(c == null || !c.moveToNext()) {
            // TODO: error
            
            return;
        }
        
        stopCode = c.getString(1);
        stopName = c.getString(2);
        latitude = c.getDouble(3);
        longitude = c.getDouble(4);
        orientation = c.getInt(5);
        locality = c.getString(6);
        
        c.close();
        
        populateView();
    }
    
    /**
     * Using the data in the local fields of this object, populate the various
     * views with data.
     */
    private void populateView() {
        setMapLocation();
        
        final StringBuilder sb = new StringBuilder();
        sb.append(stopName);
        
        if(locality != null) {
            sb.append(',').append(' ').append(locality);
        }
        
        sb.append(' ').append("<font color=\"#989898\">(")
                .append(stopCode).append(")</font>");
        
        // Set the bus stop name text.
        txtName.setText(Html.fromHtml(sb.toString()));
        
        // Set the services list text.
        final String services = bsd.getBusServicesForStopAsString(stopCode);
        if(services == null || services.length() == 0) {
            txtServices.setText(R.string.busstopdetails_noservices);
        } else {
            txtServices.setText(BusStopDatabase.getColouredServiceListString(
                    services));
        }
    }
    
    /**
     * Set the location of the map.
     */
    private void setMapLocation() {
        if(map != null) {
            // Move the camera to the position of the bus stop marker.
            final LatLng position = new LatLng(latitude, longitude);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(position,
                    MAP_ZOOM));
            // Create new MarkerOptions. Not draggable.
            final MarkerOptions mo = new MarkerOptions();
            mo.draggable(false);
            mo.position(position);
            
            // Select the marker icon to use on the map.
            switch(orientation) {
                case 0:
                    mo.icon(BitmapDescriptorFactory.fromResource(
                            R.drawable.mapmarker_n));
                    break;
                case 1:
                    mo.icon(BitmapDescriptorFactory.fromResource(
                            R.drawable.mapmarker_ne));
                    break;
                case 2:
                    mo.icon(BitmapDescriptorFactory.fromResource(
                            R.drawable.mapmarker_e));
                    break;
                case 3:
                    mo.icon(BitmapDescriptorFactory.fromResource(
                            R.drawable.mapmarker_se));
                    break;
                case 4:
                    mo.icon(BitmapDescriptorFactory.fromResource(
                            R.drawable.mapmarker_s));
                    break;
                case 5:
                    mo.icon(BitmapDescriptorFactory.fromResource(
                            R.drawable.mapmarker_sw));
                    break;
                case 6:
                    mo.icon(BitmapDescriptorFactory.fromResource(
                            R.drawable.mapmarker_w));
                    break;
                case 7:
                    mo.icon(BitmapDescriptorFactory.fromResource(
                            R.drawable.mapmarker_nw));
                    break;
                default:
                    mo.icon(BitmapDescriptorFactory.fromResource(
                            R.drawable.mapmarker));
                    break;
            }
            
            // Add the marker to the map.
            map.addMarker(mo);
        }
    }
    
    /**
     * This method is called when the device detects a new location.
     */
    private void updateLocation() {
        if(lastLocation == null || stopCode == null) {
            txtDistance.setText("");
            return;
        }
        
        // Get the distance between the device's location and the bus stop.
        final float[] distance = new float[1];
        Location.distanceBetween(lastLocation.getLatitude(),
                lastLocation.getLongitude(), latitude, longitude, distance);
        // Units depends on distance.
        if(distance[0] > 1000f) {
            distanceFormat.setMaximumFractionDigits(1);
            txtDistance.setText(distanceFormat.format(distance[0] / 1000) +
                    " km");
        } else {
            distanceFormat.setMaximumFractionDigits(0);
            distanceFormat.setParseIntegerOnly(true);
            txtDistance.setText(distanceFormat.format(distance[0]) + " m");
        }
    }
    
    /**
     * This static class is what loads the bus stop details.
     */
    private static class BusStopDetailsLoader extends SimpleCursorLoader {
        
        private final BusStopDatabase bsd;
        private final String stopCode;
        
        /**
         * Create a new BusStopDetailsLoader, specifying the stopCode.
         * 
         * @param context A Context instance.
         * @param stopCode The stopCode to load.
         */
        public BusStopDetailsLoader(final Context context,
                final String stopCode) {
            super(context);
            
            bsd = BusStopDatabase.getInstance(context.getApplicationContext());
            this.stopCode = stopCode;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Cursor loadInBackground() {
            return bsd.getBusStopByCode(stopCode);
        }
    }
}