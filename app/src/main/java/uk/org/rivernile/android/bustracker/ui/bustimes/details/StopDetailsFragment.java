/*
 * Copyright (C) 2016 - 2020 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.bustimes.details;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract;
import uk.org.rivernile.android.bustracker.database.busstop.loaders.BusStopLoader;
import uk.org.rivernile.android.utils.LocationUtils;
import uk.org.rivernile.android.utils.ProcessedCursorLoader;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * The purpose of this {@link Fragment} is to show users details for a given bus stop code.
 *
 * @author Niall Scott
 */
public class StopDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks,
        LocationListener, StopDetailsAdapter.OnItemClickListener {

    private static final String ARG_STOP_CODE = "stopCode";

    private static final String STATE_ASKED_LOCATION_PERMISSION = "askedLocationPermission";

    private static final int LOADER_BUS_STOP = 1;
    private static final int LOADER_SERVICES = 2;

    private static final int PERMISSION_REQUEST_LOCATION = 1;

    private static final int LOCATION_REQUEST_PERIOD = 10000;
    private static final float LOCATION_MIN_DISTANCE = 10.0f;

    @Inject
    LocationManager locationManager;

    private Callbacks callbacks;
    private StopDetailsAdapter adapter;
    private String stopCode;
    private boolean isStarted;
    private boolean isListeningForLocationUpdates;
    private boolean hasAskedForLocationPermission;

    private RecyclerView recyclerView;
    private ProgressBar progress;

    /**
     * Create a new instance of this {@link Fragment}.
     *
     * @param stopCode The bus stop code to show details for.
     * @return A new instance of this {@link Fragment}.
     */
    @NonNull
    public static StopDetailsFragment newInstance(@Nullable final String stopCode) {
        final StopDetailsFragment fragment = new StopDetailsFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_STOP_CODE, stopCode);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(@NonNull final Context context) {
        super.onAttach(context);

        try {
            callbacks = (Callbacks) context;
        } catch (ClassCastException e) {
            throw new IllegalStateException(context.getClass().getName() + " must implement " +
                    Callbacks.class.getName());
        }
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            hasAskedForLocationPermission =
                    savedInstanceState.getBoolean(STATE_ASKED_LOCATION_PERMISSION);
        }

        stopCode = getArguments().getString(ARG_STOP_CODE);
        adapter = new StopDetailsAdapter(requireContext());
        adapter.setOnItemClickedListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.stopdetails_fragment, container, false);
        recyclerView = v.findViewById(android.R.id.list);
        progress = v.findViewById(R.id.progress);

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        showProgress();
        getLoaderManager().initLoader(LOADER_BUS_STOP, null, this);
        getLoaderManager().initLoader(LOADER_SERVICES, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();

        isStarted = true;

        if (getUserVisibleHint()) {
            startLocationUpdates();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        isStarted = false;
        stopLocationUpdates();
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_ASKED_LOCATION_PERMISSION, hasAskedForLocationPermission);
    }

    @Override
    public void setUserVisibleHint(final boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (getContext() != null) {
            if (isVisibleToUser && isStarted) {
                startLocationUpdates();
            } else {
                stopLocationUpdates();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
            @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                isStarted && getUserVisibleHint()) {
            startLocationUpdates();
        }

        adapter.onLocationPermissionChanged();
    }

    @NonNull
    @Override
    public Loader onCreateLoader(final int id, final Bundle args) {
        switch (id) {
            case LOADER_BUS_STOP:
                return new BusStopLoader(requireContext(), stopCode,
                        new String[] {
                                BusStopContract.BusStops.LATITUDE,
                                BusStopContract.BusStops.LONGITUDE,
                                BusStopContract.BusStops.ORIENTATION
                        });
            case LOADER_SERVICES:
                return new BusStopServiceDetailsLoader(requireContext(), stopCode);
            default:
                return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onLoadFinished(final Loader loader, final Object data) {
        switch (loader.getId()) {
            case LOADER_BUS_STOP:
                handleBusStopLoaded((Cursor) data);
                break;
            case LOADER_SERVICES:
                final ProcessedCursorLoader.ResultWrapper<List<Service>> result =
                        (ProcessedCursorLoader.ResultWrapper<List<Service>>) data;
                handleServicesLoaded(result.getResult());
                break;
        }
    }

    @Override
    public void onLoaderReset(final Loader loader) {
        if (loader.getId() == LOADER_BUS_STOP) {
            handleBusStopLoaded(null);
        }
    }

    @Override
    public void onLocationChanged(final Location location) {
        adapter.setDeviceLocation(location);
    }

    @Override
    public void onStatusChanged(final String provider, final int status, final Bundle extras) {
        // Nothing to do here.
    }

    @Override
    public void onProviderEnabled(final String provider) {
        // Nothing to do here.
    }

    @Override
    public void onProviderDisabled(final String provider) {
        // Nothing to do here.
    }

    @Override
    public void onMapClicked() {
        callbacks.showMapForStop(stopCode);
    }

    @Override
    public void onServiceClicked(@NonNull final String serviceName) {
        // TODO: need to implement focusing on a service in the bus stop map.
    }

    /**
     * Start listening for location updates.
     */
    private void startLocationUpdates() {
        if (LocationUtils.checkLocationPermission(requireContext())) {
            if (!isListeningForLocationUpdates) {
                isListeningForLocationUpdates = true;
                adapter.setDeviceLocation(LocationUtils.getBestInitialLocation(locationManager));
                final List<String> providers = locationManager.getAllProviders();

                if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
                    startLocationProvider(LocationManager.NETWORK_PROVIDER);
                }

                if (providers.contains(LocationManager.GPS_PROVIDER)) {
                    startLocationProvider(LocationManager.GPS_PROVIDER);
                }
            }
        } else {
            adapter.setDeviceLocation(null);

            if (!hasAskedForLocationPermission) {
                requestPermissions(
                        new String[] {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        }, PERMISSION_REQUEST_LOCATION);
            }
        }

        hasAskedForLocationPermission = true;
    }

    /**
     * Starts a location provider in the {@link LocationManager}.
     *
     * @param provider The provider to start.
     */
    private void startLocationProvider(@NonNull final String provider) {
        locationManager.requestLocationUpdates(provider, LOCATION_REQUEST_PERIOD,
                LOCATION_MIN_DISTANCE, this);
    }

    /**
     * Stops a location provider in the {@link LocationManager}.
     */
    private void stopLocationUpdates() {
        if (isListeningForLocationUpdates &&
                LocationUtils.checkLocationPermission(requireContext())) {
            locationManager.removeUpdates(this);
        }

        isListeningForLocationUpdates = false;
    }

    /**
     * Handle the bus stop data being loaded from the database.
     *
     * @param cursor The {@link Cursor} containing bus stop data.
     */
    private void handleBusStopLoaded(@Nullable final Cursor cursor) {
        final BusStopLocation location;

        if (cursor != null && cursor.moveToFirst()) {
            final double latitude = cursor.getDouble(cursor.getColumnIndex(
                    BusStopContract.BusStops.LATITUDE));
            final double longitude = cursor.getDouble(cursor.getColumnIndex(
                    BusStopContract.BusStops.LONGITUDE));
            final int orientation = cursor.getInt(cursor.getColumnIndex(
                    BusStopContract.BusStops.ORIENTATION));
            location = new BusStopLocation(latitude, longitude, orientation);
        } else {
            location = null;
        }

        adapter.setBusStopLocation(location);
        showContent();
    }

    /**
     * Handle the service listing for this stop being loaded from the database.
     *
     * @param services The {@link List} of {@link Service}s for this stop.
     */
    private void handleServicesLoaded(@Nullable final List<Service> services) {
        adapter.setServices(services);
    }

    /**
     * Show the progress view.
     */
    private void showProgress() {
        recyclerView.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
    }

    /**
     * Show the content view.
     */
    private void showContent() {
        progress.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This interface must be implemented by any {@link android.app.Activity Activites} which host
     * this {@link Fragment}.
     */
    public interface Callbacks {

        /**
         * This is called when the user wishes to see the stop map, with this stop being selected
         * by default.
         *
         * @param stopCode The stop code to view on the map.
         */
        void showMapForStop(@NonNull String stopCode);
    }
}
