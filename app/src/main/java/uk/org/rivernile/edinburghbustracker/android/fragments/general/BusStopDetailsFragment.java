/*
 * Copyright (C) 2012 - 2016 Niall 'Rivernile' Scott
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
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.text.NumberFormat;

import uk.org.rivernile.android.bustracker.database.settings.loaders.HasFavouriteStopLoader;
import uk.org.rivernile.android.bustracker.database.settings.loaders.HasProximityAlertLoader;
import uk.org.rivernile.android.bustracker.database.settings.loaders.HasTimeAlertLoader;
import uk.org.rivernile.android.bustracker.preferences.PreferenceConstants;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowAddFavouriteStopListener;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowAddProximityAlertListener;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowAddTimeAlertListener;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowBusStopMapWithStopCodeListener;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowBusTimesListener;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowConfirmDeleteProximityAlertListener;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowConfirmDeleteTimeAlertListener;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowConfirmFavouriteDeletionListener;
import uk.org.rivernile.android.utils.LocationUtils;
import uk.org.rivernile.android.utils.SimpleCursorLoader;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link Fragment} shows details for a bus stop. The bus stop code is passed in as an
 * argument to this {@link Fragment}. A Map is shown at the top of the {@link Fragment}
 * if the Google Play Services are available, otherwise it is removed.
 * 
 * @author Niall Scott
 */
public class BusStopDetailsFragment extends Fragment implements LocationListener,
        SensorEventListener, LoaderManager.LoaderCallbacks<Cursor> {
    
    /** This is the stopCode argument. */
    public static final String ARG_STOPCODE = "stopCode";

    private static final int LOADER_BUS_STOP_DATA = 1;
    private static final int LOADER_HAS_FAVOURITE_STOP = 2;
    private static final int LOADER_HAS_PROX_ALERT = 3;
    private static final int LOADER_HAS_TIME_ALERT = 4;

    private static final NumberFormat DISTANCE_FORMAT = NumberFormat.getInstance();
    
    private static final int REQUEST_PERIOD = 10000;
    private static final float MIN_DISTANCE = 3.0f;
    private static final float MAP_ZOOM = 15f;
    
    private Callbacks callbacks;
    private BusStopDatabase bsd;
    private LocationManager locMan;
    private SensorManager sensMan;
    private Sensor accelerometer;
    private Sensor magnetometer;
    
    private MapView mapView;
    private GoogleMap map;
    private ImageButton favouriteBtn;
    private TextView txtName, txtServices, txtDistance, txtEmpty, txtProxAlert, txtTimeAlert;
    private View layoutContent, progress;
    
    private Location lastLocation;
    private String stopCode;
    private String stopName;
    private double latitude;
    private double longitude;
    private int orientation;
    private String locality;

    private Cursor cursorFavourite;
    private Cursor cursorProxAlert;
    private Cursor cursorTimeAlert;
    
    private final float[] distance = new float[2];
    private final float[] rotationMatrix = new float[9];
    private final float[] headings = new float[3];
    private float[] accelerometerValues;
    private float[] magnetometerValues;
    private GeomagneticField geoField;
    private int screenRotation;
    private Bitmap needleBitmap;
    
    /**
     * Get a new instance of this {@link Fragment}. A bus stop code must be supplied.
     * 
     * @param stopCode The bus stop code to show details for.
     * @return A new instance of this {@link Fragment}.
     */
    @NonNull
    public static BusStopDetailsFragment newInstance(@NonNull final String stopCode) {
        final BusStopDetailsFragment f = new BusStopDetailsFragment();
        final Bundle b = new Bundle();
        b.putString(ARG_STOPCODE, stopCode);
        f.setArguments(b);
        
        return f;
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
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final Context context = getActivity().getApplicationContext();
        
        // Get the various resources and services.
        bsd = BusStopDatabase.getInstance(context);
        locMan = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        sensMan = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensMan.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        stopCode = getArguments().getString(ARG_STOPCODE);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        // This is the View that contains the root view.
        final View v = inflater.inflate(R.layout.busstopdetails, container, false);
        
        // Get the MapView and send it the onCreate event.
        mapView = (MapView)v.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        
        layoutContent = v.findViewById(R.id.layoutContent);
        progress = v.findViewById(R.id.progress);
        txtEmpty = (TextView) v.findViewById(android.R.id.empty);
        txtName = (TextView) v.findViewById(R.id.txtName);
        txtServices = (TextView) v.findViewById(R.id.txtServices);
        txtDistance = (TextView) v.findViewById(R.id.txtDistance);
        txtProxAlert = (TextView) v.findViewById(R.id.txtProxAlert);
        txtTimeAlert = (TextView) v.findViewById(R.id.txtTimeAlert);
        
        Button b = (Button) v.findViewById(R.id.btnBusTimes);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                // Show the bus times.
                callbacks.onShowBusTimes(stopCode);
            }
        });
        
        b = (Button) v.findViewById(R.id.btnStreetView);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                // Show StreetView.
                callbacks.onShowStreetView(latitude, longitude);
            }
        });
        
        favouriteBtn = (ImageButton) v.findViewById(R.id.imgbtnFavourite);
        favouriteBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                // Add/remove as favourite.
                if (cursorFavourite != null) {
                    if (cursorFavourite.getCount() > 0) {
                        callbacks.onShowConfirmFavouriteDeletion(stopCode);
                    } else {
                        callbacks.onShowAddFavouriteStop(stopCode,
                                locality != null ? stopName + ", " + locality : stopName);
                    }
                }
            }
        });
        
        txtProxAlert.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (cursorProxAlert != null) {
                    if (cursorProxAlert.getCount() > 0) {
                        callbacks.onShowConfirmDeleteProximityAlert();
                    } else {
                        callbacks.onShowAddProximityAlert(stopCode);
                    }
                }
            }
        });
        
        txtTimeAlert.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (cursorTimeAlert != null) {
                    if (cursorTimeAlert.getCount() > 0) {
                        callbacks.onShowConfirmDeleteTimeAlert();
                    } else {
                        callbacks.onShowAddTimeAlert(stopCode, null);
                    }
                }
            }
        });
        
        return v;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(LOADER_HAS_FAVOURITE_STOP, null, this);
        loaderManager.initLoader(LOADER_HAS_PROX_ALERT, null, this);
        loaderManager.initLoader(LOADER_HAS_TIME_ALERT, null, this);
        
        // Get the screen rotation. This will be needed later to remap the coordinate system.
        screenRotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        
        // Initialise the lastLocation to the best known location.
        if (hasLocationPermission()) {
            lastLocation = LocationUtils.getBestInitialLocation(locMan);
        }

        updateLocation();
        
        map = mapView.getMap();
        // The Map can be null if Google Play Services is not available.
        if (map != null) {
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.setOnMapClickListener(new OnMapClickListener() {
                @Override
                public void onMapClick(final LatLng point) {
                    callbacks.onShowBusStopMapWithStopCode(stopCode);
                }
            });
        } else {
            // If the MapView is null, then hide it.
            mapView.setVisibility(View.GONE);
        }
        
        // The loader is restarted each time incase a new bus stop database has become available.
        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        
        // Feed back life cycle events back to the MapView.
        mapView.onResume();

        if (hasLocationPermission()) {
            // Start the location providers if they are enabled.
            if (locMan.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, REQUEST_PERIOD,
                        MIN_DISTANCE, this);
            }

            if (locMan.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, REQUEST_PERIOD,
                        MIN_DISTANCE, this);
            }

            // Show the user's location on the map if we can.
            if (map != null) {
                map.setMyLocationEnabled(getActivity().getSharedPreferences(
                        PreferenceConstants.PREF_FILE, 0)
                        .getBoolean(PreferenceConstants.PREF_AUTO_LOCATION, true));
            }

            // Start the accelerometer and magnetometer.
            startOrientationSensors();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        
        // Feed back life cycle events back to the MapView.
        mapView.onPause();
        
        // When the Activity is being paused, cancel location updates.
        locMan.removeUpdates(this);
        // ...and Sensor updates.
        sensMan.unregisterListener(this);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        
        // Feed back life cycle events back to the MapView.
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // Feed back life cycle events back to the MapView.
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        
        // Feed back life cycle events back to the MapView.
        mapView.onLowMemory();
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle bundle) {
        switch (id) {
            case LOADER_BUS_STOP_DATA:
                return new BusStopDetailsLoader(getActivity(), stopCode);
            case LOADER_HAS_FAVOURITE_STOP:
                return new HasFavouriteStopLoader(getActivity(), stopCode);
            case LOADER_HAS_PROX_ALERT:
                return new HasProximityAlertLoader(getActivity(), stopCode);
            case LOADER_HAS_TIME_ALERT:
                return new HasTimeAlertLoader(getActivity(), stopCode);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor c) {
        switch (loader.getId()) {
            case LOADER_BUS_STOP_DATA:
                populateData(c);
                break;
            case LOADER_HAS_FAVOURITE_STOP:
                updateFavouritesItem(c);
                break;
            case LOADER_HAS_PROX_ALERT:
                updateProximityItem(c);
                break;
            case LOADER_HAS_TIME_ALERT:
                updateTimeAlert(c);
                break;
        }
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_HAS_FAVOURITE_STOP:
                updateFavouritesItem(null);
                break;
            case LOADER_HAS_PROX_ALERT:
                updateProximityItem(null);
                break;
            case LOADER_HAS_TIME_ALERT:
                updateTimeAlert(null);
                break;
        }
    }

    @Override
    public void onLocationChanged(final Location location) {
        if (LocationUtils.isBetterLocation(location, lastLocation)) {
            lastLocation = location;

            // Calculate the GeomagneticField of the current location. This is
            // used by the direction indicator.
            if (location != null) {
                geoField = new GeomagneticField(
                        Double.valueOf(location.getLatitude()).floatValue(),
                        Double.valueOf(location.getLongitude()).floatValue(),
                        Double.valueOf(location.getAltitude()).floatValue(),
                        System.currentTimeMillis());
            }

            updateLocation();
        }
    }

    @Override
    public void onStatusChanged(final String provider, final int status,
            final Bundle extras) {
        // Nothing to do here.
    }

    @Override
    public void onProviderEnabled(final String provider) {
        if (hasLocationPermission() && (LocationManager.GPS_PROVIDER.equals(provider) ||
                LocationManager.NETWORK_PROVIDER.equals(provider))) {
            locMan.requestLocationUpdates(provider, REQUEST_PERIOD, MIN_DISTANCE, this);
        }
    }

    @Override
    public void onProviderDisabled(final String provider) {
        // Nothing to do here.
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        final float[] values = event.values;
        
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                // If there's no accelerometer values yet, create the values
                // array.
                if (accelerometerValues == null) {
                    accelerometerValues = new float[3];
                    accelerometerValues[0] = values[0];
                    accelerometerValues[1] = values[1];
                    accelerometerValues[2] = values[2];
                } else {
                    smoothValues(accelerometerValues, values);
                }
                
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                // If there's no magnetometer values yet, create the values
                // array.
                if (magnetometerValues == null) {
                    magnetometerValues = new float[3];
                    magnetometerValues[0] = values[0];
                    magnetometerValues[1] = values[1];
                    magnetometerValues[2] = values[2];
                } else {
                    smoothValues(magnetometerValues, values);
                }
                
                break;
            default:
                return;
        }
        
        // Make sure the needle is pointing the correct direction.
        updateDirectionNeedle();
    }

    @Override
    public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
        // Nothing to do here.
    }

    /**
     * Load the bus stop data.
     */
    private void loadData() {
        progress.setVisibility(View.VISIBLE);
        txtEmpty.setVisibility(View.GONE);
        layoutContent.setVisibility(View.GONE);
        getLoaderManager().restartLoader(LOADER_BUS_STOP_DATA, null, this);
    }
    
    /**
     * This is called when the {@link Loader} has finished. It moves the data out of the
     * {@link Cursor} and in to local fields.
     * 
     * @param c The returned Cursor.
     */
    private void populateData(final Cursor c) {
        if (c == null) {
            showLoadFailedError();
            
            return;
        }

        if (c.moveToNext()) {
            stopName = c.getString(2);
            latitude = c.getDouble(3);
            longitude = c.getDouble(4);
            orientation = c.getInt(5);
            locality = c.getString(6);
            
            c.close();
            populateView();
        } else {
            c.close();
            showLoadFailedError();
        }
    }
    
    /**
     * Using the data in the local fields of this object, populate the various views with data.
     */
    private void populateView() {
        layoutContent.setVisibility(View.VISIBLE);
        txtEmpty.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        
        setMapLocation();
        
        final String name;
        
        if (locality != null) {
            name = getString(R.string.busstop_locality_coloured, stopName, locality, stopCode);
        } else {
            name = getString(R.string.busstop_coloured, stopName, stopCode);
        }
        
        txtName.setText(Html.fromHtml(name));
        
        // Set the services list text.
        final String services = bsd.getBusServicesForStopAsString(stopCode);

        if (services == null || services.length() == 0) {
            txtServices.setText(R.string.busstopdetails_noservices);
        } else {
            txtServices.setText(BusStopDatabase.getColouredServiceListString(services));
        }
        
        updateLocation();
    }

    /**
     * Update the favourites item.
     *
     * @param c The favourites {@link Cursor}.
     */
    private void updateFavouritesItem(@Nullable final Cursor c) {
        cursorFavourite = c;

        if (cursorFavourite != null) {
            favouriteBtn.setEnabled(true);

            if (cursorFavourite.getCount() > 0) {
                favouriteBtn.setImageResource(R.drawable.ic_list_favourite);
                favouriteBtn.setContentDescription(getString(R.string.favourite_rem));
            } else {
                favouriteBtn.setImageResource(R.drawable.ic_list_unfavourite);
                favouriteBtn.setContentDescription(getString(R.string.favourite_add));
            }
        } else {
            favouriteBtn.setEnabled(false);
        }
    }

    /**
     * Update the proximity item.
     *
     * @param c The proximity {@link Cursor}.
     */
    private void updateProximityItem(@Nullable final Cursor c) {
        cursorProxAlert = c;

        if (cursorProxAlert != null) {
            txtProxAlert.setEnabled(true);

            if (cursorProxAlert.getCount() > 0) {
                txtProxAlert.setText(R.string.busstopdetails_prox_rem);
            } else {
                txtProxAlert.setText(R.string.busstopdetails_prox_add);
            }
        } else {
            txtProxAlert.setEnabled(false);
        }
    }

    /**
     * Update the time item.
     *
     * @param c The time {@link Cursor}.
     */
    private void updateTimeAlert(@Nullable final Cursor c) {
        cursorTimeAlert = c;

        if (cursorTimeAlert != null) {
            txtTimeAlert.setEnabled(true);

            if (cursorTimeAlert.getCount() > 0) {
                txtTimeAlert.setText(R.string.busstopdetails_time_rem);
            } else {
                txtTimeAlert.setText(R.string.busstopdetails_time_add);
            }
        } else {
            txtTimeAlert.setEnabled(false);
        }
    }
    
    /**
     * Set the location of the map.
     */
    private void setMapLocation() {
        if (map != null) {
            // This is a bit of a hack to stop the map crashing the app. The CameraUpdateFactory
            // depends on being initialised before use. It seems to work well when the Fragment
            // is first run, but if the app is killed then later resumed with this Fragment being
            // what is returned to, the app crashes. This code forces initialisation to happen.
            if (MapsInitializer.initialize(getActivity()) != ConnectionResult.SUCCESS) {
                return;
            }
            
            // Move the camera to the position of the bus stop marker.
            final LatLng position = new LatLng(latitude, longitude);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, MAP_ZOOM));
            // Create new MarkerOptions. Not draggable.
            final MarkerOptions mo = new MarkerOptions();
            mo.draggable(false);
            mo.position(position);
            
            // Select the marker icon to use on the map.
            switch (orientation) {
                case 0:
                    mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarker_n));
                    break;
                case 1:
                    mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarker_ne));
                    break;
                case 2:
                    mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarker_e));
                    break;
                case 3:
                    mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarker_se));
                    break;
                case 4:
                    mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarker_s));
                    break;
                case 5:
                    mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarker_sw));
                    break;
                case 6:
                    mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarker_w));
                    break;
                case 7:
                    mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarker_nw));
                    break;
                default:
                    mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarker));
                    break;
            }
            
            // Add the marker to the map.
            map.addMarker(mo);
        }
    }
    
    /**
     * This method is called when there is a problem loading the bus stop or the bus stop does
     * not exist in the database.
     */
    private void showLoadFailedError() {
        layoutContent.setVisibility(View.GONE);
        txtEmpty.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
    }
    
    /**
     * This method is called when the device detects a new location.
     */
    private void updateLocation() {
        if (lastLocation == null || stopCode == null) {
            // Make sure there's no distance text or direction needle.
            txtDistance.setText("");
            txtDistance.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            recycleNeedleBitmapIfNotNull(null);
            
            return;
        }
        
        // Get the distance between the device's location and the bus stop.
        Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(),
                latitude, longitude, distance);
        // Units depends on distance.
        if (distance[0] > 1000f) {
            DISTANCE_FORMAT.setMaximumFractionDigits(1);
            DISTANCE_FORMAT.setParseIntegerOnly(false);
            txtDistance.setText(DISTANCE_FORMAT.format(distance[0] / 1000) + " km");
        } else {
            DISTANCE_FORMAT.setMaximumFractionDigits(0);
            DISTANCE_FORMAT.setParseIntegerOnly(true);
            txtDistance.setText(DISTANCE_FORMAT.format(distance[0]) + " m");
        }
        
        // There's a new location, the direction needle needs to be updated.
        updateDirectionNeedle();
    }
    
    /**
     * Update the direction needle so that it is pointing towards the bus stop, based on the
     * device location and the direction it is facing.
     */
    private void updateDirectionNeedle() {
        // We need values for location, the accelerometer and magnetometer to continue.
        if (lastLocation == null || accelerometerValues == null || magnetometerValues == null) {
            // Make sure the needle isn't showing.
            txtDistance.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            recycleNeedleBitmapIfNotNull(null);
            
            return;
        }
        
        // Calculating the rotation matrix may fail, for example, if the device is in freefall.
        // In that case we cannot continue as the values will be unreliable.
        if (!SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerValues,
                magnetometerValues)) {
            return;
        }
        
        // The screen rotation was obtained earlier.
        switch (screenRotation) {
            // There's lots of information about this elsewhere, but briefly;
            // The values from the sensors are in the device's coordinate system which may be
            // correct if the device is in its natural orientation, but it needs to be remapped
            // if the device is rotated.
            case Surface.ROTATION_0:
                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X,
                        SensorManager.AXIS_Z, rotationMatrix);
                break;
            case Surface.ROTATION_90:
                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_Z,
                        SensorManager.AXIS_MINUS_X, rotationMatrix);
                break;
            case Surface.ROTATION_180:
                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_MINUS_X,
                        SensorManager.AXIS_MINUS_Z, rotationMatrix);
                break;
            case Surface.ROTATION_270:
                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_MINUS_Z,
                        SensorManager.AXIS_X, rotationMatrix);
                break;
        }
        
        // Get the X, Y and Z orientations, which are in radians. Covert this in to degrees East
        // of North.
        SensorManager.getOrientation(rotationMatrix, headings);
        double heading = Math.toDegrees(headings[0]);
        
        // If there's a GeomagneticField value, then adjust the heading to take this in to account.
        if (geoField != null) {
            heading -= geoField.getDeclination();
        }
        
        // The orientation is in the range of -180 to +180. Convert this in to a range of 0 to 360.
        final float bearingTo = distance[1] < 0 ? distance[1] + 360 : distance[1];
        
        // This is the heading to the bus stop.
        heading = bearingTo - heading;
        
        // The above calculation may come out as a negative number again. Put this back in to the
        // range of 0 to 360.
        if (heading < 0) {
            heading += 360;
        }
        
        // This 'if' statement is required to prevent a crash during device rotation. It ensured
        // that the Fragment is still part of the Activity.
        if (isAdded()) {
            // Get the arrow bitmap from the resources.
            final Bitmap needleIn = BitmapFactory.decodeResource(getResources(),
                    R.drawable.heading_arrow);
            // Get an identity matrix and rotate it by the required amount.
            final Matrix m = new Matrix();
            m.setRotate((float) heading % 360, (float) needleIn.getWidth() / 2,
                    (float) needleIn.getHeight() / 2);
            // Apply the rotation matrix to the Bitmap, to create a new Bitmap.
            final Bitmap needleOut = Bitmap.createBitmap(needleIn, 0, 0, needleIn.getWidth(),
                    needleIn.getHeight(), m, true);
            
            // Recycle the needle read in if it's not the same as the rotated needle.
            if (needleIn != needleOut) {
                needleIn.recycle();
            }
            
            // This Bitmap needs to be converted to a Drawable type.
            final BitmapDrawable drawable = new BitmapDrawable(getResources(), needleOut);
            // Set the new needle to be on the right hand side of the TextView.
            txtDistance.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
            recycleNeedleBitmapIfNotNull(needleOut);
        } else {
            // If the Fragment is not added to the Activity, then make sure there's no needle.
            txtDistance.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            recycleNeedleBitmapIfNotNull(null);
        }
    }
    
    /**
     * Start the sensors used to measure device magnetic orientation.
     */
    private void startOrientationSensors() {
        // Only use the sensors when both the magnetometer and the accelerometer are available.
        if (magnetometer != null && accelerometer != null) {
            sensMan.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
            sensMan.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
    
    /**
     * This method is used to smooth the values coming from the accelerometer and the
     * magnetometer. This stops the needle from jumping around.
     * 
     * @param oldValues The existing values.
     * @param newValues The new values from the sensor.
     */
    private static void smoothValues(final float[] oldValues, final float[] newValues) {
        // If there's no old or new values, then return.
        if (oldValues == null || newValues == null) {
            return;
        }
        
        // Loop through the arrays, smoothing the values.
        final int len = oldValues.length;
        for (int i = 0; i < len; i++) {
            oldValues[i] += 0.2f * (newValues[i] - oldValues[i]);
        }
    }
    
    /**
     * Recycle the needle {@link Bitmap} if it's no longer in use.
     * 
     * @param newNeedle The new needle {@link Bitmap}.
     */
    private void recycleNeedleBitmapIfNotNull(@Nullable final Bitmap newNeedle) {
        if (needleBitmap != null) {
            needleBitmap.recycle();
        }
        
        needleBitmap = newNeedle;
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
     * This static class is what loads the bus stop details.
     */
    private static class BusStopDetailsLoader extends SimpleCursorLoader {
        
        private final BusStopDatabase bsd;
        private final String stopCode;
        
        /**
         * Create a new {@link BusStopDetailsLoader}, specifying the {@code stopCode}.
         * 
         * @param context A {@link Context} instance.
         * @param stopCode The {@code stopCode} to load.
         */
        public BusStopDetailsLoader(@NonNull final Context context,
                @NonNull final String stopCode) {
            super(context);
            
            bsd = BusStopDatabase.getInstance(context.getApplicationContext());
            this.stopCode = stopCode;
        }

        @Override
        public Cursor loadInBackground() {
            final Cursor c = bsd.getBusStopByCode(stopCode);
            
            // This ensures the Cursor window is set properly.
            if (c != null) {
                c.getCount();
            }
            
            return c;
        }
    }
    
    /**
     * Any {@link Activity Activities} which host this {@link Fragment} must implement this
     * interface to handle navigation events.
     */
    public interface Callbacks extends OnShowConfirmFavouriteDeletionListener,
            OnShowConfirmDeleteProximityAlertListener, OnShowConfirmDeleteTimeAlertListener,
            OnShowBusTimesListener, OnShowAddProximityAlertListener, OnShowAddTimeAlertListener,
            OnShowBusStopMapWithStopCodeListener, OnShowAddFavouriteStopListener {
        
        /**
         * This is called when the user wishes to see a location on Street View.
         * 
         * @param latitude The latitude of the location to show.
         * @param longitude The longitude of the location to show.
         */
        void onShowStreetView(double latitude, double longitude);
    }
}