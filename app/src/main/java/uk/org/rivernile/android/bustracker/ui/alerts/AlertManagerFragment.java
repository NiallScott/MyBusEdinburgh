/*
 * Copyright (C) 2011 - 2016 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.alerts;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Settings;
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
import android.widget.TextView;

import java.util.Map;

import uk.org.rivernile.android.bustracker.database.settings.SettingsContract;
import uk.org.rivernile.android.bustracker.database.settings.loaders.AlertsLoader;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowConfirmDeleteProximityAlertListener;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowConfirmDeleteTimeAlertListener;
import uk.org.rivernile.android.utils.ProcessedCursorLoader;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link Fragment} allows the users to view what proximity and time alerts they have set and
 * allow them to delete single alerts or all alerts.
 * 
 * @author Niall Scott
 */
public class AlertManagerFragment extends Fragment
        implements LoaderManager.LoaderCallbacks, AlertsAdapter.OnItemClickListener {

    private static final int LOADER_ALERTS = 1;
    private static final int LOADER_BUS_STOPS = 2;
    
    private Callbacks callbacks;
    private AlertsAdapter adapter;

    private RecyclerView recyclerView;
    private ProgressBar progress;
    private TextView txtEmpty;

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
        
        // Create the adapter.
        adapter = new AlertsAdapter(getActivity());
        adapter.setOnItemClickListener(this);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.alertmanager, container, false);
        recyclerView = (RecyclerView) v.findViewById(android.R.id.list);
        progress = (ProgressBar) v.findViewById(R.id.progress);
        txtEmpty = (TextView) v.findViewById(android.R.id.empty);

        recyclerView.setAdapter(adapter);

        return v;
    }

    @Override
    public void onActivityCreated(final Bundle bundle) {
        super.onActivityCreated(bundle);
        
        getActivity().setTitle(R.string.alertmanager_title);
        loadAlerts();
    }

    @Override
    public Loader onCreateLoader(final int id, final Bundle args) {
        switch (id) {
            case LOADER_ALERTS:
                return new AlertsLoader(getActivity());
            case LOADER_BUS_STOPS:
                final String[] stopCodes = getStopCodes();
                return stopCodes != null ? new AlertsBusStopLoader(getContext(), stopCodes) : null;
            default:
                return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onLoadFinished(final Loader loader, final Object result) {
        switch (loader.getId()) {
            case LOADER_ALERTS:
                handleAlertsLoaded((Cursor) result);
                break;
            case LOADER_BUS_STOPS:
                handleBusStopsLoaded(
                        ((ProcessedCursorLoader.ResultWrapper<Map<String, BusStop>>) result)
                                .getResult());
                break;
        }
    }

    @Override
    public void onLoaderReset(final Loader loader) {
        switch (loader.getId()) {
            case LOADER_ALERTS:
                // If the Loader has been reset, empty the adapter.
                handleAlertsLoaded(null);
                break;
        }
    }

    @Override
    public void onLocationSettingsClicked() {
        final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onRemoveProximityAlertClicked(@NonNull final Cursor cursor) {
        callbacks.onShowConfirmDeleteProximityAlert();
    }

    @Override
    public void onRemoveTimeAlertClicked(@NonNull final Cursor cursor) {
        callbacks.onShowConfirmDeleteTimeAlert();
    }

    /**
     * Begin loading active alerts.
     */
    private void loadAlerts() {
        showProgress();
        getLoaderManager().initLoader(LOADER_ALERTS, null, this);
    }

    /**
     * Load bus stop information for the loaded alerts.
     */
    private void loadAlertBusStops() {
        getLoaderManager().restartLoader(LOADER_BUS_STOPS, null, this);
    }

    /**
     * Handle the resulting {@link Cursor} from loading the active alerts.
     *
     * @param cursor The active alerts.
     */
    private void handleAlertsLoaded(@Nullable final Cursor cursor) {
        swapCursor(cursor);
    }

    /**
     * Handle the resulting {@link Map} of bus stops loaded from the database.
     *
     * @param busStops The {@link Map} of bus stop data.
     */
    private void handleBusStopsLoaded(@Nullable final Map<String, BusStop> busStops) {
        adapter.setBusStops(busStops);
    }

    /**
     * Swap the active alerts {@link Cursor}.
     *
     * @param cursor The new {@link Cursor} to use to represent the active alerts.
     */
    private void swapCursor(@Nullable final Cursor cursor) {
        adapter.swapCursor(cursor);

        if (cursor != null && cursor.getCount() > 0) {
            showContent();
            loadAlertBusStops();
        } else {
            showEmpty();
        }
    }

    /**
     * Show loading progress.
     */
    private void showProgress() {
        recyclerView.setVisibility(View.GONE);
        txtEmpty.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
    }

    /**
     * Show content - that is, no loading is happening and there are alerts active.
     */
    private void showContent() {
        txtEmpty.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * Show the empty state.
     */
    private void showEmpty() {
        recyclerView.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        txtEmpty.setVisibility(View.VISIBLE);
    }

    /**
     * Get the stop codes for the currently loaded alerts {@link Cursor}.
     *
     * @return The stop codes for the currently loaded alerts {@link Cursor}, or {@code null} if
     * there is no currently loaded {@link Cursor} or it is empty.
     */
    @Nullable
    private String[] getStopCodes() {
        final Cursor cursor = adapter.getCursor();

        if (cursor != null) {
            final int count = cursor.getCount();

            if (count == 0) {
                return null;
            }

            final int stopCodeColumn = cursor.getColumnIndex(SettingsContract.Alerts.STOP_CODE);
            final String[] stopCodes = new String[count];

            for (int i = 0; i < count; i++) {
                cursor.moveToPosition(i);
                stopCodes[i] = cursor.getString(stopCodeColumn);
            }

            return stopCodes;
        } else {
            return null;
        }
    }

    /**
     * Any {@link android.app.Activity Activities} which host this {@link Fragment} must implement
     * this interface to handle navigation events.
     */
    public interface Callbacks extends OnShowConfirmDeleteProximityAlertListener,
            OnShowConfirmDeleteTimeAlertListener {
        // Nothing else defined here.
    }
}