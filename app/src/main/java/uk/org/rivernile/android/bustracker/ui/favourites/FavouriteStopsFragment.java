/*
 * Copyright (C) 2009 - 2017 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.favourites;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Map;

import uk.org.rivernile.android.bustracker.database.busstop.loaders.BusStopServicesLoader;
import uk.org.rivernile.android.bustracker.database.settings.SettingsContract;
import uk.org.rivernile.android.bustracker.database.settings.loaders.FavouriteStopsLoader;
import uk.org.rivernile.android.bustracker.database.settings.loaders.HasProximityAlertLoader;
import uk.org.rivernile.android.bustracker.database.settings.loaders.HasTimeAlertLoader;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowAddEditFavouriteStopListener;
import uk.org.rivernile.android.utils.MapsUtils;
import uk.org.rivernile.android.bustracker.ui.bustimes.DisplayStopDataActivity;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowAddProximityAlertListener;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowAddTimeAlertListener;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowBusStopMapWithStopCodeListener;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowBusTimesListener;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowConfirmDeleteProximityAlertListener;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowConfirmDeleteTimeAlertListener;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowConfirmFavouriteDeletionListener;
import uk.org.rivernile.android.utils.ProcessedCursorLoader;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link Fragment} shows the user a list of their favourite bus stops. What this
 * {@link Fragment} does depends on the {@link #ARG_CREATE_SHORTCUT} argument.
 *
 * <p>
 *     If {@link #ARG_CREATE_SHORTCUT} is set to {@code true};
 *
 *     <ul>
 *         <li>Do not show the context menu when the user long presses on a list item.</li>
 *         <li>When the user selects a list item, set the {@link Activity} result with an
 *             {@link Intent} which sets the shortcut icon.</li>
 *     </ul>
 * </p>
 *
 * <p>
 *     If {@link #ARG_CREATE_SHORTCUT} is set to {@code false};
 *
 *     <ul>
 *         <li>Allow the user to bring up a context menu when they long press on a list item.</li>
 *         <li>When the user selects a list item, show the bus times for that bus stop.</li>
 *     </ul>
 * </p>
 * 
 * @author Niall Scott
 */
public class FavouriteStopsFragment extends Fragment implements LoaderManager.LoaderCallbacks,
        FavouriteStopsAdapter.OnItemClickedListener {

    /** The argument to signify create shortcut mode. */
    public static final String ARG_CREATE_SHORTCUT = "createShortcut";

    private static final String STATE_SELECTED_STOP_CODE = "selectedStopCode";

    private static final int LOADER_FAVOURITE_STOPS = 1;
    private static final int LOADER_BUS_STOP_SERVICES = 2;
    private static final int LOADER_HAS_PROXIMITY_ALERT = 3;
    private static final int LOADER_HAS_TIME_ALERT = 4;
    
    private Callbacks callbacks;
    private FavouriteStopsAdapter adapter;
    private ActionMode actionMode;
    private boolean isCreateShortcut;
    private String selectedStopCode;
    private int columnStopCode;
    private int columnStopName;

    private Cursor cursorProxAlert;
    private Cursor cursorTimeAlert;

    private RecyclerView recyclerView;
    private ProgressBar progress;
    private TextView txtError;

    private MenuItem amMenuItemProxAlert;
    private MenuItem amMenuItemTimeAlert;
    private MenuItem amMenuItemShowOnMap;
    
    /**
     * Create a new instance of this {@link Fragment}, specifying whether it should be in
     * shortcuts mode, or favourites mode.
     * 
     * @param isCreateShortcut {@code true} if the user wants to add a shortcut, {@code false} if
     * not.
     * @return A new instance of this {@link Fragment}.
     */
    @NonNull
    public static FavouriteStopsFragment newInstance(final boolean isCreateShortcut) {
        final FavouriteStopsFragment f = new FavouriteStopsFragment();
        final Bundle b = new Bundle();
        b.putBoolean(ARG_CREATE_SHORTCUT, isCreateShortcut);
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

        if (savedInstanceState != null) {
            selectedStopCode = savedInstanceState.getString(STATE_SELECTED_STOP_CODE);
        }
        
        // Cache the Activity instance.
        final Activity activity = getActivity();

        // Determine the mode this Fragment should be in.
        isCreateShortcut = getArguments().getBoolean(ARG_CREATE_SHORTCUT);
        adapter = new FavouriteStopsAdapter(activity);
        adapter.setOnItemClickedListener(this);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.favouritestops, container, false);
        recyclerView = (RecyclerView) v.findViewById(android.R.id.list);
        progress = (ProgressBar) v.findViewById(R.id.progress);
        txtError = (TextView) v.findViewById(R.id.txtError);

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        
        return v;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(LOADER_FAVOURITE_STOPS, null, this);
        
        // What title is set depends on the mode.
        if (isCreateShortcut) {
            getActivity().setTitle(R.string.favouriteshortcut_title);
        } else {
            getActivity().setTitle(R.string.favouritestops_title);

            if (selectedStopCode != null) {
                actionMode = ((AppCompatActivity) getActivity())
                        .startSupportActionMode(actionModeCallback);
            }
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(STATE_SELECTED_STOP_CODE, selectedStopCode);
    }

    @Override
    public void onItemClicked(final Cursor cursor) {
        if (actionMode == null) {
            final String stopCode = cursor.getString(columnStopCode);

            if (isCreateShortcut) {
                final Activity activity = getActivity();
                // Set the Intent which is used when the shortcut is tapped.
                final Intent intent = new Intent(DisplayStopDataActivity.ACTION_VIEW_STOP_DATA);
                intent.setClass(activity, DisplayStopDataActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(DisplayStopDataActivity.ARG_STOPCODE, stopCode);

                // Set the Activity result to send back to the launcher, which contains a name,
                // Intent and icon.
                final Intent result = new Intent();
                result.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
                result.putExtra(Intent.EXTRA_SHORTCUT_NAME, cursor.getString(columnStopName));
                result.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource
                        .fromContext(activity, R.drawable.appicon_favourite));

                // Set the Activity result and exit.
                activity.setResult(Activity.RESULT_OK, result);
                activity.finish();
            } else {
                // View bus stop times.
                callbacks.onShowBusTimes(stopCode);
            }
        }
    }

    @Override
    public boolean onItemLongClicked(final Cursor cursor) {
        if (isCreateShortcut || actionMode != null) {
            return false;
        }

        selectedStopCode = cursor.getString(columnStopCode);
        actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(actionModeCallback);

        return true;
    }

    @Override
    public Loader onCreateLoader(final int i, final Bundle bundle) {
        switch (i) {
            case LOADER_FAVOURITE_STOPS:
                return new FavouriteStopsLoader(getActivity());
            case LOADER_BUS_STOP_SERVICES:
                final Cursor cursor = adapter.getCursor();

                if (cursor != null) {
                    final String[] stopCodes = getAllStopCodes(cursor);

                    if (stopCodes.length > 0) {
                        return new BusStopServicesLoader(getActivity(), stopCodes);
                    }
                }

                break;
            case LOADER_HAS_PROXIMITY_ALERT:
                return selectedStopCode != null
                        ? new HasProximityAlertLoader(getActivity(), selectedStopCode) : null;
            case LOADER_HAS_TIME_ALERT:
                return selectedStopCode != null
                        ? new HasTimeAlertLoader(getActivity(), selectedStopCode) : null;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onLoadFinished(final Loader loader, final Object result) {
        switch (loader.getId()) {
            case LOADER_FAVOURITE_STOPS:
                handleFavouriteStopsLoaded((Cursor) result);
                break;
            case LOADER_BUS_STOP_SERVICES:
                handleBusStopServicesLoaded(
                        ((ProcessedCursorLoader.ResultWrapper<Map<String, String>>) result)
                                .getResult());
                break;
            case LOADER_HAS_PROXIMITY_ALERT:
                cursorProxAlert = (Cursor) result;
                updateActionModeItemProximity();
                break;
            case LOADER_HAS_TIME_ALERT:
                cursorTimeAlert = (Cursor) result;
                updateActionModeItemTime();
                break;
        }
    }

    @Override
    public void onLoaderReset(final Loader loader) {
        switch (loader.getId()) {
            case LOADER_FAVOURITE_STOPS:
                // Give the adapter a null Cursor when the Loader is reset.
                handleFavouriteStopsLoaded(null);
                break;
            case LOADER_BUS_STOP_SERVICES:
                handleBusStopServicesLoaded(null);
                break;
            case LOADER_HAS_PROXIMITY_ALERT:
                cursorProxAlert = null;
                break;
            case LOADER_HAS_TIME_ALERT:
                cursorTimeAlert = null;
                break;
        }
    }

    /**
     * Load the user's saved favourite stops.
     */
    private void loadBusStopServices() {
        getLoaderManager().restartLoader(LOADER_BUS_STOP_SERVICES, null, this);
    }

    /**
     * Handle the result of loading the user's saved favourite stops.
     *
     * @param c The {@link Cursor} containing the user's saved favourite stops.
     */
    private void handleFavouriteStopsLoaded(@Nullable final Cursor c) {
        if (c != null) {
            columnStopCode = c.getColumnIndex(SettingsContract.Favourites.STOP_CODE);
            columnStopName = c.getColumnIndex(SettingsContract.Favourites.STOP_NAME);
        }

        // When loading is complete, swap the Cursor. The old Cursor is automatically closed.
        adapter.swapCursor(c);

        if (c == null || c.getCount() == 0) {
            showError();
        } else {
            showContent();
            loadBusStopServices();
        }
    }

    /**
     * Handle the result of loading the mapping of bus stop codes to a listing of services for those
     * bus stops.
     *
     * @param busStopServices The result of the load.
     */
    private void handleBusStopServicesLoaded(@Nullable final Map<String, String> busStopServices) {
        adapter.setBusStopServices(busStopServices);
    }

    /**
     * Show the content view.
     */
    private void showContent() {
        progress.setVisibility(View.GONE);
        txtError.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * Show the error view.
     */
    private void showError() {
        txtError.setText(R.string.favouritestops_nosavedstops);
        progress.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        txtError.setVisibility(View.VISIBLE);

        if (actionMode != null) {
            actionMode.finish();
        }
    }

    /**
     * Return the {@link Cursor} representing the list of the user's favourite bus stops, set at the
     * row of the given {@code stopCode}, or {@code null} if the {@link Cursor} is {@code null} or
     * the bus stop code is not found.
     *
     * @param stopCode The bus stop code to look for.
     * @return The {@link Cursor} at the position of the given {@code stopCode}, or {@code null} if
     * there is no {@link Cursor} or the {@code stopCode} could not be found.
     */
    @Nullable
    private Cursor getCursorRow(@NonNull final String stopCode) {
        final Cursor cursor = adapter.getCursor();

        if (cursor != null) {
            cursor.moveToPosition(-1);

            while (cursor.moveToNext()) {
                if (stopCode.equals(cursor.getString(columnStopCode))) {
                    return cursor;
                }
            }
        }

        return null;
    }

    /**
     * Get all stop codes contained within the favourite stops {@link Cursor}.
     *
     * @param cursor The favourite stops {@link Cursor}.
     * @return A {@link String} array of all the favourite stop codes.
     */
    @NonNull
    private String[] getAllStopCodes(@NonNull final Cursor cursor) {
        final int len = cursor.getCount();
        final String[] stopCodes = new String[len];

        for (int i = 0; i < len; i++) {
            cursor.moveToPosition(i);
            stopCodes[i] = cursor.getString(columnStopCode);
        }

        return stopCodes;
    }

    /**
     * Update the proximity action mode item depending on the current state.
     */
    private void updateActionModeItemProximity() {
        if (amMenuItemProxAlert != null) {
            if (cursorProxAlert != null) {
                amMenuItemProxAlert.setEnabled(true);

                if (cursorProxAlert.getCount() > 0) {
                    amMenuItemProxAlert.setTitle(R.string.favouritestops_menu_prox_rem);
                    amMenuItemProxAlert.setIcon(R.drawable.ic_action_location_off);
                } else {
                    amMenuItemProxAlert.setTitle(R.string.favouritestops_menu_prox_add);
                    amMenuItemProxAlert.setIcon(R.drawable.ic_action_location_on);
                }
            } else {
                amMenuItemProxAlert.setEnabled(false);
            }
        }
    }

    /**
     * Update the time action mode item depending on the current state.
     */
    private void updateActionModeItemTime() {
        if (amMenuItemTimeAlert != null) {
            if (cursorTimeAlert != null) {
                amMenuItemTimeAlert.setEnabled(true);

                if (cursorTimeAlert.getCount() > 0) {
                    amMenuItemTimeAlert.setTitle(R.string.favouritestops_menu_time_rem);
                    amMenuItemTimeAlert.setIcon(R.drawable.ic_action_alarm_off);
                } else {
                    amMenuItemTimeAlert.setTitle(R.string.favouritestops_menu_time_add);
                    amMenuItemTimeAlert.setIcon(R.drawable.ic_action_alarm_add);
                }
            } else {
                amMenuItemTimeAlert.setEnabled(false);
            }
        }
    }

    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
            if (selectedStopCode == null) {
                return false;
            }

            final Cursor cursor = getCursorRow(selectedStopCode);

            if (cursor == null) {
                return false;
            }

            mode.getMenuInflater().inflate(R.menu.favouritestops_context_menu, menu);
            amMenuItemProxAlert = menu.findItem(R.id.favouritestops_context_menu_prox_alert);
            amMenuItemTimeAlert = menu.findItem(R.id.favouritestops_context_menu_time_alert);
            amMenuItemShowOnMap = menu.findItem(R.id.favouritestops_context_menu_showonmap);

            final LoaderManager loaderManager = getLoaderManager();
            loaderManager.restartLoader(LOADER_HAS_PROXIMITY_ALERT, null,
                    FavouriteStopsFragment.this);
            loaderManager.restartLoader(LOADER_HAS_TIME_ALERT, null, FavouriteStopsFragment.this);

            mode.setTitle(getString(R.string.busstop, cursor.getString(columnStopName),
                    selectedStopCode));
            onPrepareActionMode(mode, menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
            if (selectedStopCode == null) {
                mode.finish();
                return false;
            }

            updateActionModeItemProximity();
            updateActionModeItemTime();

            // If the Google Play Services is not available, then don't show the option to show the
            // stop on the map.
            if (!MapsUtils.isGoogleMapsAvailable(getActivity())) {
                amMenuItemShowOnMap.setVisible(false);
            }

            return true;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
            if (selectedStopCode == null) {
                mode.finish();
                return false;
            }

            final boolean result;

            switch (item.getItemId()) {
                case R.id.favouritestops_context_menu_modify:
                    // Allow the user to edit the name of the favourite stop.
                    callbacks.onShowAddEditFavouriteStop(selectedStopCode);
                    result = true;
                    break;
                case R.id.favouritestops_context_menu_delete:
                    callbacks.onShowConfirmFavouriteDeletion(selectedStopCode);
                    result = true;
                    break;
                case R.id.favouritestops_context_menu_showonmap:
                    // Show the selected bus stop on the map.
                    callbacks.onShowBusStopMapWithStopCode(selectedStopCode);
                    result = true;
                    break;
                case R.id.favouritestops_context_menu_prox_alert:
                    // See if this stop exists as a proximity alert.
                    if (cursorProxAlert != null) {
                        if (cursorProxAlert.getCount() > 0) {
                            callbacks.onShowConfirmDeleteProximityAlert();
                        } else {
                            callbacks.onShowAddProximityAlert(selectedStopCode);
                        }
                    }

                    result = true;
                    break;
                case R.id.favouritestops_context_menu_time_alert:
                    // See if this stop exists as a time alert.
                    if (cursorTimeAlert != null) {
                        if (cursorTimeAlert.getCount() > 0) {
                            callbacks.onShowConfirmDeleteTimeAlert();
                        } else {
                            callbacks.onShowAddTimeAlert(selectedStopCode, null);
                        }
                    }

                    result = true;
                    break;
                default:
                    result = false;
                    break;
            }

            mode.finish();
            return result;
        }

        @Override
        public void onDestroyActionMode(final ActionMode mode) {
            final LoaderManager loaderManager = getLoaderManager();
            loaderManager.destroyLoader(LOADER_HAS_PROXIMITY_ALERT);
            loaderManager.destroyLoader(LOADER_HAS_TIME_ALERT);

            actionMode = null;
            amMenuItemProxAlert = amMenuItemTimeAlert = amMenuItemShowOnMap = null;
            selectedStopCode = null;
        }
    };

    /**
     * Any {@link Activity Activities} which host this {@link Fragment} must implement this
     * interface to handle navigation events.
     */
    public interface Callbacks extends OnShowAddEditFavouriteStopListener,
            OnShowConfirmFavouriteDeletionListener, OnShowConfirmDeleteProximityAlertListener,
            OnShowConfirmDeleteTimeAlertListener, OnShowAddProximityAlertListener,
            OnShowAddTimeAlertListener, OnShowBusStopMapWithStopCodeListener,
            OnShowBusTimesListener {
        
        // Nothing new to add here.
    }
}