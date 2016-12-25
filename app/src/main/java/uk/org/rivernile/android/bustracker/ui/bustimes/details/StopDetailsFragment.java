/*
 * Copyright (C) 2016 Niall 'Rivernile' Scott
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

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.List;

import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract;
import uk.org.rivernile.android.bustracker.database.busstop.loaders.BusStopLoader;
import uk.org.rivernile.android.utils.ProcessedCursorLoader;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * The purpose of this {@link Fragment} is to show users details for a given bus stop code.
 *
 * @author Niall Scott
 */
public class StopDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks,
        StopDetailsAdapter.OnItemClickListener {

    private static final String ARG_STOP_CODE = "stopCode";

    private static final int LOADER_BUS_STOP = 1;
    private static final int LOADER_SERVICES = 2;

    private Callbacks callbacks;
    private StopDetailsAdapter adapter;
    private String stopCode;

    private RecyclerView recyclerView;
    private ProgressBar progress;

    /**
     * Create a new instance of this {@link Fragment}.
     *
     * @param stopCode The bus stop code to show details for.
     * @return A new instance of this {@link Fragment}.
     */
    @NonNull
    public static StopDetailsFragment newInstance(@NonNull final String stopCode) {
        final StopDetailsFragment fragment = new StopDetailsFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_STOP_CODE, stopCode);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(final Context context) {
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
        super.onCreate(savedInstanceState);

        stopCode = getArguments().getString(ARG_STOP_CODE);
        adapter = new StopDetailsAdapter(getContext());
        adapter.setOnItemClickedListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.stopdetails_fragment, container, false);
        recyclerView = (RecyclerView) v.findViewById(android.R.id.list);
        progress = (ProgressBar) v.findViewById(R.id.progress);

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
    public Loader onCreateLoader(final int id, final Bundle args) {
        switch (id) {
            case LOADER_BUS_STOP:
                return new BusStopLoader(getContext(), stopCode,
                        new String[] {
                                BusStopContract.BusStops.LATITUDE,
                                BusStopContract.BusStops.LONGITUDE,
                                BusStopContract.BusStops.ORIENTATION
                        });
            case LOADER_SERVICES:
                return new BusStopServiceDetailsLoader(getContext(), stopCode);
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
        switch (loader.getId()) {
            case LOADER_BUS_STOP:
                handleBusStopLoaded(null);
                break;
        }
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
