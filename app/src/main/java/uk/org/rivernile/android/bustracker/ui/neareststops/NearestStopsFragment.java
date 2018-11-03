/*
 * Copyright (C) 2011 - 2018 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.neareststops;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import uk.org.rivernile.android.bustracker.BusApplication;
import uk.org.rivernile.android.bustracker.database.busstop.loaders.AllServiceNamesLoader;
import uk.org.rivernile.android.bustracker.database.settings.loaders.HasFavouriteStopLoader;
import uk.org.rivernile.android.bustracker.database.settings.loaders.HasProximityAlertLoader;
import uk.org.rivernile.android.bustracker.database.settings.loaders.HasTimeAlertLoader;
import uk.org.rivernile.android.bustracker.preferences.PreferenceManager;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowAddEditFavouriteStopListener;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowAddProximityAlertListener;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowAddTimeAlertListener;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowBusStopMapWithStopCodeListener;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowBusTimesListener;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowConfirmDeleteProximityAlertListener;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowConfirmDeleteTimeAlertListener;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowConfirmFavouriteDeletionListener;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowServicesChooserListener;
import uk.org.rivernile.android.utils.MapsUtils;
import uk.org.rivernile.android.utils.LocationUtils;
import uk.org.rivernile.android.utils.ProcessedCursorLoader;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.android.bustracker.ui.serviceschooser.ServicesChooserDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs.TurnOnGpsDialogFragment;

/**
 * Show a list of the nearest bus stops to the device. If a location could not be found or the
 * user is too far away, an error message will be shown. The user is able to filter the shown bus
 * stops by what bus services stop there. Long pressing on a bus stop shows a contextual action bar
 * where the user can perform various actions on that stop. Tapping the stop shows bus times for
 * that stop.
 * 
 * @author Niall Scott
 */
public class NearestStopsFragment extends Fragment
        implements LoaderManager.LoaderCallbacks, LocationListener,
        ServicesChooserDialogFragment.Callbacks, NearestStopsAdapter.OnItemClickedListener {

    private static final String STATE_CHOSEN_SERVICES = "chosenServices";
    private static final String STATE_SELECTED_STOP = "chosenStop";

    private static final int PERMISSION_REQUEST_LOCATION = 1;
    
    private static final int REQUEST_PERIOD = 10000;
    private static final float MIN_DISTANCE = 3.0f;

    private static final int LOADER_NEAREST_STOPS = 1;
    private static final int LOADER_SERVICES = 2;
    private static final int LOADER_HAS_FAVOURITE_STOP = 3;
    private static final int LOADER_HAS_PROXIMITY_ALERT = 4;
    private static final int LOADER_HAS_TIME_ALERT = 5;
    
    private Callbacks callbacks;
    private LocationManager locMan;
    private PreferenceManager preferenceManager;
    
    private NearestStopsAdapter adapter;
    private ActionMode actionMode;
    private String[] services;
    private String[] chosenServices;
    private Location lastLocation;
    private SearchResult selectedStop;

    private Cursor cursorFavourite;
    private Cursor cursorProxAlert;
    private Cursor cursorTimeAlert;

    private RecyclerView recyclerView;
    private ProgressBar progress;
    private View layoutError;
    private TextView txtError;
    private Button btnErrorResolve;

    private MenuItem menuItemFilter;
    private MenuItem amMenuItemFavourite;
    private MenuItem amMenuItemProxAlert;
    private MenuItem amMenuItemTimeAlert;
    private MenuItem amMenuItemShowOnMap;

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
            chosenServices = savedInstanceState.getStringArray(STATE_CHOSEN_SERVICES);
            selectedStop = savedInstanceState.getParcelable(STATE_SELECTED_STOP);
        }
        
        final Activity activity = getActivity();
        // Get references to required resources.
        locMan = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        preferenceManager = ((BusApplication) getContext().getApplicationContext())
                .getPreferenceManager();
        adapter = new NearestStopsAdapter(activity);
        adapter.setOnItemClickedListener(this);

        if (LocationUtils.checkLocationPermission(getContext())) {
            // Initialise the lastLocation to the best known location.
            lastLocation = LocationUtils.getBestInitialLocation(locMan);
        }
        
        // Tell the underlying Activity that this Fragment contains an options menu.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.neareststops, container, false);
        recyclerView = (RecyclerView) v.findViewById(android.R.id.list);
        progress = (ProgressBar) v.findViewById(R.id.progress);
        layoutError = v.findViewById(R.id.layoutError);
        txtError = (TextView) v.findViewById(R.id.txtError);
        btnErrorResolve = (Button) v.findViewById(R.id.btnErrorResolve);

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        return v;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        getActivity().setTitle(R.string.neareststops_title);

        // Force an update to initially show data.
        doUpdate(true);
        getLoaderManager().initLoader(LOADER_SERVICES, null, this);

        if (selectedStop != null) {
            actionMode = ((AppCompatActivity) getActivity())
                    .startSupportActionMode(actionModeCallback);
        }

        if (savedInstanceState == null) {
            // Show a dialog asking the user to turn on GPS if required. This will be shown when
            // the user has not asked for this dialog to not be shown, when the system has the GPS
            // feature, the GPS provider is disabled and there is a GPS resolution Activity.
            if (!preferenceManager.isGpsPromptDisabled() &&
                    getActivity().getPackageManager()
                            .hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS) &&
                    !locMan.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                    hasGpsSettingActivty()) {
                // Show Dialog asking users if they want to turn on GPS.
                callbacks.onAskTurnOnGps();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (LocationUtils.checkLocationPermission(getContext())) {
            startLocationUpdates();
        } else {
            showPermissionRequiredError();
        }

        updateFilterMenuItem();
    }

    @Override
    public void onPause() {
        super.onPause();
        
        // When the Activity is being paused, cancel location updates.
        locMan.removeUpdates(this);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putStringArray(STATE_CHOSEN_SERVICES, chosenServices);
        outState.putParcelable(STATE_SELECTED_STOP, selectedStop);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        // Inflate the menu.
        inflater.inflate(R.menu.neareststops_option_menu, menu);
        menuItemFilter = menu.findItem(R.id.neareststops_option_menu_filter);
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);

        updateFilterMenuItem();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch(item.getItemId()) {
            case R.id.neareststops_option_menu_filter:
                if (services != null && services.length > 0) {
                    callbacks.onShowServicesChooser(services, chosenServices,
                            getString(R.string.neareststops_service_chooser_title));
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader onCreateLoader(final int id, final Bundle args) {
        switch (id) {
            case LOADER_NEAREST_STOPS:
                return new NearestStopsLoader(getActivity(), lastLocation.getLatitude(),
                        lastLocation.getLongitude(), chosenServices);
            case LOADER_SERVICES:
                return new AllServiceNamesLoader(getContext());
            case LOADER_HAS_FAVOURITE_STOP:
                return selectedStop != null
                        ? new HasFavouriteStopLoader(getActivity(), selectedStop.getStopCode())
                        : null;
            case LOADER_HAS_PROXIMITY_ALERT:
                return selectedStop != null
                        ? new HasProximityAlertLoader(getActivity(), selectedStop.getStopCode())
                        : null;
            case LOADER_HAS_TIME_ALERT:
                return selectedStop != null
                        ? new HasTimeAlertLoader(getActivity(), selectedStop.getStopCode())
                        : null;
            default:
                return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onLoadFinished(final Loader loader, final Object result) {
        switch (loader.getId()) {
            case LOADER_NEAREST_STOPS:
                populateResults(
                        ((ProcessedCursorLoader.ResultWrapper<List<SearchResult>>) result)
                                .getResult());
                break;
            case LOADER_SERVICES:
                services = ((ProcessedCursorLoader.ResultWrapper<String[]>) result).getResult();
                updateFilterMenuItem();
                break;
            case LOADER_HAS_FAVOURITE_STOP:
                cursorFavourite = (Cursor) result;
                updateActionModeItemFavourite();
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
            case LOADER_HAS_FAVOURITE_STOP:
                cursorFavourite = null;
                break;
            case LOADER_HAS_PROXIMITY_ALERT:
                cursorProxAlert = null;
                break;
            case LOADER_HAS_TIME_ALERT:
                cursorTimeAlert = null;
                break;
        }
    }

    @Override
    public void onLocationChanged(final Location location) {
        // When the location has changed and is better, cache the new location and force an update.
        if (LocationUtils.isBetterLocation(location, lastLocation)) {
            lastLocation = location;
            doUpdate(false);
        }
    }

    @Override
    public void onStatusChanged(final String provider, final int status, final Bundle extras) {
        // Nothing to do here.
    }

    @Override
    public void onProviderEnabled(final String provider) {
        if (hasSuitableEnabledLocationProvider() && layoutError.getVisibility() == View.VISIBLE) {
            showProgress();
        }
    }

    @Override
    public void onProviderDisabled(final String provider) {
        if (!hasSuitableEnabledLocationProvider()) {
            showLocationServicesDisabledError();
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
            @NonNull final String[] permissions,
            @NonNull final int[] grantResults) {
        if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            if (isResumed()) {
                startLocationUpdates();
            }
        } else {
            showPermissionRequiredError();
        }

        updateFilterMenuItem();
    }

    @Override
    public void onServicesChosen(final String[] chosenServices) {
        this.chosenServices = chosenServices;
        
        // The user has been in the services chooser Dialog, so force an update incase anything
        // has changed.
        doUpdate(false);
    }

    @Override
    public void onItemClicked(@NonNull final SearchResult item) {
        if (actionMode == null) {
            callbacks.onShowBusTimes(item.getStopCode());
        }
    }

    @Override
    public boolean onItemLongClicked(@NonNull final SearchResult item) {
        if (actionMode != null) {
            return false;
        }

        selectedStop = item;
        actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(actionModeCallback);
        return true;
    }

    /**
     * Cause the data to refresh. The refresh happens asynchronously in another thread.
     * 
     * @param isFirst Is this the first load?
     */
    private void doUpdate(final boolean isFirst) {
        if (lastLocation == null) {
            return;
        }

        if (isFirst) {
            getLoaderManager().initLoader(LOADER_NEAREST_STOPS, null, this);
        } else {
            getLoaderManager().restartLoader(LOADER_NEAREST_STOPS, null, this);
        }
    }

    /**
     * Populate the {@link List} of results within the {@link RecyclerView.Adapter}. If no results
     * are available, the empty state error is shown.
     *
     * @param results The loaded {@link List} of results.
     */
    private void populateResults(@Nullable final List<SearchResult> results) {
        if (results != null && !results.isEmpty()) {
            adapter.setSearchResults(results);
            showContent();
        } else {
            adapter.setSearchResults(null);

            if (layoutError.getVisibility() != View.VISIBLE) {
                showEmptyError();
            }
        }
    }

    /**
     * Show the content view.
     */
    private void showContent() {
        layoutError.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * Show indeterminate progress.
     */
    private void showProgress() {
        layoutError.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
    }

    /**
     * Show the empty state error.
     */
    private void showEmptyError() {
        showError(R.string.neareststops_empty, 0);
    }

    /**
     * Show an error which asks the user to grant permission to the application to access the
     * device's current location.
     */
    private void showPermissionRequiredError() {
        btnErrorResolve.setOnClickListener(locationClickListener);
        showError(R.string.neareststops_error_permission_required,
                R.string.neareststops_error_permission_required_button);
    }

    /**
     * Show an error which informs the user that location services have not been enabled on their
     * device.
     */
    private void showLocationServicesDisabledError() {
        final int btnTextRes;

        if (hasGpsSettingActivty()) {
            btnErrorResolve.setOnClickListener(noProvidersClickListener);
            btnTextRes = R.string.neareststops_error_location_sources_button;
        } else {
            btnTextRes = 0;
        }

        showError(R.string.neareststops_error_location_sources, btnTextRes);
    }

    /**
     * Show an error.
     *
     * @param errorTextRes The {@link String} resource to display for the error.
     * @param errorButtonRes The {@link String} resource to display for the resolve {@link Button}.
     * If no {@link Button} is to be shown, set to {@code 0}.
     */
    private void showError(@StringRes final int errorTextRes, @StringRes final int errorButtonRes) {
        txtError.setText(errorTextRes);

        if (errorButtonRes != 0) {
            btnErrorResolve.setText(errorButtonRes);
            btnErrorResolve.setVisibility(View.VISIBLE);
        } else {
            btnErrorResolve.setVisibility(View.GONE);
        }

        recyclerView.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);

        if (actionMode != null) {
            actionMode.finish();
        }
    }

    /**
     * Update the filter {@link MenuItem} to be enabled/disabled depending on the current state.
     */
    private void updateFilterMenuItem() {
        if (menuItemFilter != null) {
            menuItemFilter.setEnabled(services != null && services.length > 0 &&
                    LocationUtils.checkLocationPermission(getContext()));
        }
    }

    /**
     * Update the favourite action mode item depending on the current state.
     */
    private void updateActionModeItemFavourite() {
        if (amMenuItemFavourite != null) {
            if (cursorFavourite != null) {
                amMenuItemFavourite.setEnabled(true);

                if (cursorFavourite.getCount() > 0) {
                    amMenuItemFavourite.setTitle(R.string.neareststops_context_remasfav);
                    amMenuItemFavourite.setIcon(R.drawable.ic_action_star);
                } else {
                    amMenuItemFavourite.setTitle(R.string.neareststops_context_addasfav);
                    amMenuItemFavourite.setIcon(R.drawable.ic_action_star_border);
                }
            } else {
                amMenuItemFavourite.setEnabled(false);
            }
        }
    }

    /**
     * Update the proximity action mode item depending on the current state.
     */
    private void updateActionModeItemProximity() {
        if (amMenuItemProxAlert != null) {
            if (cursorProxAlert != null) {
                amMenuItemProxAlert.setEnabled(true);

                if (cursorProxAlert.getCount() > 0) {
                    amMenuItemProxAlert.setTitle(R.string.neareststops_menu_prox_rem);
                    amMenuItemProxAlert.setIcon(R.drawable.ic_action_location_off);
                } else {
                    amMenuItemProxAlert.setTitle(R.string.neareststops_menu_prox_add);
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
                    amMenuItemTimeAlert.setTitle(R.string.neareststops_menu_time_rem);
                    amMenuItemTimeAlert.setIcon(R.drawable.ic_action_alarm_off);
                } else {
                    amMenuItemTimeAlert.setTitle(R.string.neareststops_menu_time_add);
                    amMenuItemTimeAlert.setIcon(R.drawable.ic_action_alarm_add);
                }
            } else {
                amMenuItemTimeAlert.setEnabled(false);
            }
        }
    }

    /**
     * Starts a location provider in the {@link LocationManager}.
     *
     * @param provider The provider to start.
     */
    private void startLocationProvider(@NonNull final String provider) {
        locMan.requestLocationUpdates(provider, REQUEST_PERIOD, MIN_DISTANCE, this);
    }

    /**
     * Start listening for location updates. Access to the necessary location permissions should be
     * checked before calling this method.
     */
    private void startLocationUpdates() {
        final List<String> providers = locMan.getAllProviders();

        if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            startLocationProvider(LocationManager.NETWORK_PROVIDER);
        }

        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            startLocationProvider(LocationManager.GPS_PROVIDER);
        }

        if (!hasSuitableEnabledLocationProvider()) {
            showLocationServicesDisabledError();
        } else {
            if (layoutError.getVisibility() == View.VISIBLE) {
                showProgress();
            }

            final Location bestLocation = LocationUtils.getBestInitialLocation(locMan);

            if (bestLocation != null &&
                    LocationUtils.isBetterLocation(bestLocation, lastLocation)) {
                lastLocation = bestLocation;
                doUpdate(false);
            }
        }
    }

    /**
     * Is there a suitable enabled location provider?
     *
     * @return {@code true} if there is a suitable enabled location provider, {@code false} if not.
     */
    private boolean hasSuitableEnabledLocationProvider() {
        return locMan.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                locMan.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Is there an {@link Activity} on the system that allows the user to turn on the GPS location
     * provider?
     *
     * @return {@code true} if there is an {@link Activity} on the system that allows the user to
     * turn on the GPS location provider, {@code false} if not.
     */
    private boolean hasGpsSettingActivty() {
        final List<ResolveInfo> packages = getActivity().getPackageManager()
                .queryIntentActivities(TurnOnGpsDialogFragment.TURN_ON_GPS_INTENT, 0);
        return packages != null && !packages.isEmpty();
    }

    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
            if (selectedStop == null) {
                return false;
            }

            mode.getMenuInflater().inflate(R.menu.neareststops_context_menu, menu);
            amMenuItemFavourite = menu.findItem(R.id.neareststops_context_menu_favourite);
            amMenuItemProxAlert = menu.findItem(R.id.neareststops_context_menu_prox_alert);
            amMenuItemTimeAlert = menu.findItem(R.id.neareststops_context_menu_time_alert);
            amMenuItemShowOnMap = menu.findItem(R.id.neareststops_context_menu_showonmap);

            final LoaderManager loaderManager = getLoaderManager();
            loaderManager.restartLoader(LOADER_HAS_FAVOURITE_STOP, null, NearestStopsFragment.this);
            loaderManager.restartLoader(LOADER_HAS_PROXIMITY_ALERT, null,
                    NearestStopsFragment.this);
            loaderManager.restartLoader(LOADER_HAS_TIME_ALERT, null, NearestStopsFragment.this);

            final String stopCode = selectedStop.getStopCode();
            final String locality = selectedStop.getLocality();
            final String name;

            if (locality != null) {
                name = getString(R.string.busstop_locality, selectedStop.getStopName(), locality,
                        stopCode);
            } else {
                name = getString(R.string.busstop, selectedStop.getStopName(), stopCode);
            }

            mode.setTitle(name);
            onPrepareActionMode(mode, menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
            updateActionModeItemFavourite();
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
            // Make sure that the selectedStop exists.
            if (selectedStop == null) {
                mode.finish();
                return false;
            }

            final String stopCode = selectedStop.getStopCode();
            final boolean result;

            switch (item.getItemId()) {
                case R.id.neareststops_context_menu_favourite:
                    // See if this stop exists as a favourite already.
                    if (cursorFavourite != null) {
                        if (cursorFavourite.getCount() > 0) {
                            callbacks.onShowConfirmFavouriteDeletion(stopCode);
                        } else {
                            // If it doesn't exist, show the Add Favourite Stop interface.
                            callbacks.onShowAddEditFavouriteStop(stopCode);
                        }
                    }

                    result = true;
                    break;
                case R.id.neareststops_context_menu_prox_alert:
                    // See if this stop exists as a proximity alert.
                    if (cursorProxAlert != null) {
                        if (cursorProxAlert.getCount() > 0) {
                            callbacks.onShowConfirmDeleteProximityAlert();
                        } else {
                            callbacks.onShowAddProximityAlert(stopCode);
                        }
                    }

                    result = true;
                    break;
                case R.id.neareststops_context_menu_time_alert:
                    // See if this stop exists as a time alert.
                    if (cursorTimeAlert != null) {
                        if (cursorTimeAlert.getCount() > 0) {
                            callbacks.onShowConfirmDeleteTimeAlert();
                        } else {
                            callbacks.onShowAddTimeAlert(stopCode, null);
                        }
                    }

                    result = true;
                    break;
                case R.id.neareststops_context_menu_showonmap:
                    // Start the BusStopMapActivity, giving it a stopCode.
                    callbacks.onShowBusStopMapWithStopCode(stopCode);

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
            loaderManager.destroyLoader(LOADER_HAS_FAVOURITE_STOP);
            loaderManager.destroyLoader(LOADER_HAS_PROXIMITY_ALERT);
            loaderManager.destroyLoader(LOADER_HAS_TIME_ALERT);

            actionMode = null;
            amMenuItemFavourite = amMenuItemProxAlert = amMenuItemTimeAlert = amMenuItemShowOnMap
                    = null;
            selectedStop = null;
        }
    };

    private final View.OnClickListener locationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            requestPermissions(
                    new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, PERMISSION_REQUEST_LOCATION);
        }
    };

    private final View.OnClickListener noProvidersClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            startActivity(TurnOnGpsDialogFragment.TURN_ON_GPS_INTENT);
        }
    };
    
    /**
     * Any {@link Activity Activities} which host this {@link Fragment} must implement this
     * interface to handle navigation events.
     */
    public interface Callbacks extends OnShowConfirmFavouriteDeletionListener,
            OnShowConfirmDeleteProximityAlertListener, OnShowConfirmDeleteTimeAlertListener,
            OnShowAddEditFavouriteStopListener, OnShowAddProximityAlertListener,
            OnShowAddTimeAlertListener, OnShowServicesChooserListener,
            OnShowBusTimesListener, OnShowBusStopMapWithStopCodeListener {
        
        /**
         * This is called when the user should be asked if they want to turn on GPS or not.
         */
        void onAskTurnOnGps();
    }
}