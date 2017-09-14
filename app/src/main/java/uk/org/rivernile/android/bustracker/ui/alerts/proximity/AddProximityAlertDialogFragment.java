/*
 * Copyright (C) 2017 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.alerts.proximity;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import uk.org.rivernile.android.bustracker.BusApplication;
import uk.org.rivernile.android.bustracker.alerts.AlertManager;
import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract;
import uk.org.rivernile.android.bustracker.database.busstop.loaders.BusStopLoader;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link DialogFragment} allows a user to add a proximity alert for a supplied stop. It is
 * presented as a dialog.
 *
 * @author Niall Scott
 */
public class AddProximityAlertDialogFragment extends DialogFragment
        implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private static final String ARG_STOPCODE = "stopCode";

    private static final int LOADER_BUS_STOP = 1;

    private static final String DIALOG_PROX_ALERT_LIMITATIONS = "proxLimitationsDialog";

    private static final int PERMISSION_REQUEST_LOCATION = 1;

    private AlertManager alertManager;
    private LocationManager locManager;

    private String stopCode;
    private boolean hasLocationFeature;
    private boolean hasLocationPermission;
    private boolean isLoadingBusStop;
    private Intent locationSettingsIntent;

    private ProgressBar progress;
    private View layoutContent;
    private View txtErrorNoLocationFeature;
    private View layoutLocationPermission;
    private Button btnGrantPermission;
    private TextView txtBlurb;
    private Spinner spinnerDistance;
    private Button btnLimitations;
    private View layoutLocationDisabled;
    private Button btnLocationSettings;

    /**
     * Create a new {@code AddProximityAlertDialogFragment}.
     *
     * @param stopCode The stop code to add a proximity alert for.
     * @return A new {@code AddProximityAlertDialogFragment}.
     */
    @NonNull
    public static AddProximityAlertDialogFragment newInstance(@NonNull final String stopCode) {
        final AddProximityAlertDialogFragment fragment = new AddProximityAlertDialogFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_STOPCODE, stopCode);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(true);
        stopCode = getArguments().getString(ARG_STOPCODE);

        final Context context = getContext();
        final BusApplication app = (BusApplication) context.getApplicationContext();
        alertManager = app.getAlertManager();
        locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        hasLocationFeature = context.getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_LOCATION);

        locationSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Context context = getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View v = inflater.inflate(R.layout.addproxalert, null, false);

        progress = (ProgressBar) v.findViewById(R.id.progress);
        layoutContent = v.findViewById(R.id.layoutContent);
        txtErrorNoLocationFeature = v.findViewById(R.id.txtErrorNoLocationFeature);
        layoutLocationPermission = v.findViewById(R.id.layoutLocationPermission);
        btnGrantPermission = (Button) v.findViewById(R.id.btnGrantPermission);
        txtBlurb = (TextView) v.findViewById(R.id.txtBlurb);
        spinnerDistance = (Spinner) v.findViewById(R.id.spinnerDistance);
        btnLimitations = (Button) v.findViewById(R.id.btnLimitations);
        layoutLocationDisabled = v.findViewById(R.id.layoutLocationDisabled);
        btnLocationSettings = (Button) v.findViewById(R.id.btnLocationSettings);

        btnLimitations.setOnClickListener(this);
        btnGrantPermission.setOnClickListener(this);
        btnLocationSettings.setOnClickListener(this);

        return new AlertDialog.Builder(context)
                .setTitle(R.string.addproxalertdialog_title)
                .setPositiveButton(R.string.addproxalertdialog_button_add,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                handlePositiveButtonClick();
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .setView(v)
                .create();
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (hasLocationFeature) {
            checkLocationPermission();
            showProgress();
            loadBusStopDetails();

            if (!hasLocationPermission && savedInstanceState == null) {
                requestLocationPermission();
            }
        } else {
            showNoLocationFeatureLayout();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (hasLocationFeature) {
            checkLocationPermission();
            getContext().registerReceiver(locationProviderChangedReceiver,
                    new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
            handleLocationProvidersChange();
        }

        updatePositiveButtonEnabledState();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (hasLocationFeature) {
            getContext().unregisterReceiver(locationProviderChangedReceiver);
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
            @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        checkLocationPermission();
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        switch (id) {
            case LOADER_BUS_STOP:
                return new BusStopLoader(getContext(), stopCode,
                        new String[] {
                                BusStopContract.BusStops.STOP_NAME,
                                BusStopContract.BusStops.LOCALITY
                        });
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
        switch (loader.getId()) {
            case LOADER_BUS_STOP:
                handleBusStopLoaded(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_BUS_STOP:
                handleBusStopLoaded(null);
                break;
        }
    }

    @Override
    public void onClick(final View v) {
        if (v == btnLimitations) {
            handleLimitationsButtonClick();
        } else if (v == btnGrantPermission) {
            requestLocationPermission();
        } else if (v == btnLocationSettings) {
            handleLocationSettingsButtonClick();
        }
    }

    /**
     * Check to see if the app has been granted location permissions by the user.
     */
    private void checkLocationPermission() {
        hasLocationPermission = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        updatePositiveButtonEnabledState();

        if (hasLocationPermission) {
            if (isLoadingBusStop) {
                showProgress();
            } else {
                showContent();
            }
        } else {
            showLocationPermissionLayout();
        }
    }

    /**
     * Request location permissions from the user.
     */
    private void requestLocationPermission() {
        requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                PERMISSION_REQUEST_LOCATION);
    }

    /**
     * Load details for the bus stop from the database.
     */
    private void loadBusStopDetails() {
        isLoadingBusStop = true;
        getLoaderManager().initLoader(LOADER_BUS_STOP, null, this);
    }

    /**
     * Handle the load of the bus stop data {@link Cursor}.
     *
     * @param cursor The bus stop data.
     */
    private void handleBusStopLoaded(@Nullable final Cursor cursor) {
        isLoadingBusStop = false;
        populateStopName(cursor);
        updatePositiveButtonEnabledState();
        showContent();
    }

    /**
     * Populate the bus stop name in the blurb text.
     */
    private void populateStopName(@Nullable final Cursor cursor) {
        final String text;

        if (cursor != null && cursor.moveToFirst()) {
            final int nameColumnIndex = cursor.getColumnIndex(BusStopContract.BusStops.STOP_NAME);
            final int localityColumnIndex =
                    cursor.getColumnIndex(BusStopContract.BusStops.LOCALITY);

            final String name = cursor.getString(nameColumnIndex);
            final String locality = cursor.getString(localityColumnIndex);
            final String nameToDisplay;

            if (!TextUtils.isEmpty(locality)) {
                nameToDisplay = getString(R.string.busstop_locality, name, locality, stopCode);
            } else {
                nameToDisplay = getString(R.string.busstop, name, stopCode);
            }

            text = getString(R.string.addproxalertdialog_blurb, nameToDisplay);
        } else {
            text = getString(R.string.addproxalertdialog_blurb, stopCode);
        }

        txtBlurb.setText(text);
    }

    /**
     * Handle the status of the location providers changing. This will show an advisory layout to
     * the user if no location providers are enabled and an {@link android.app.Activity} exists on
     * the system which can show location settings.
     */
    private void handleLocationProvidersChange() {
        final boolean hasLocationSettings =
                locationSettingsIntent.resolveActivity(getContext().getPackageManager()) != null;
        final boolean gpsEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        final boolean networkEnabled = locManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER);
        final boolean showLocationDisabled = hasLocationSettings && !gpsEnabled && !networkEnabled;

        layoutLocationDisabled.setVisibility(showLocationDisabled ? View.VISIBLE : View.GONE);
    }

    /**
     * Handle the dialog positive button being clicked.
     */
    private void handlePositiveButtonClick() {
        alertManager.addProximityAlert(stopCode, getSelectedMeters());
    }

    /**
     * Handle the limitations button being clicked.
     */
    private void handleLimitationsButtonClick() {
        ProximityLimitationsDialogFragment.newInstance()
                .show(getFragmentManager(), DIALOG_PROX_ALERT_LIMITATIONS);
    }

    /**
     * Handle the location settings button being clicked.
     */
    private void handleLocationSettingsButtonClick() {
        try {
            startActivity(locationSettingsIntent);
        } catch (ActivityNotFoundException ignored) {
            // The button should never be shown if nothing responds to this Intent, as this is
            // checked elsewhere. However, this call has an exception handler check around it to
            // prevent any crashes incase the button is shown due to any logic holes.
        }
    }

    /**
     * Show the progress layout to the user.
     */
    private void showProgress() {
        if (hasLocationPermission) {
            layoutLocationPermission.setVisibility(View.GONE);
            layoutContent.setVisibility(View.GONE);
            txtErrorNoLocationFeature.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Show the content layout to the user.
     */
    private void showContent() {
        if (hasLocationPermission) {
            layoutLocationPermission.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);
            txtErrorNoLocationFeature.setVisibility(View.GONE);
            layoutContent.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Show the layout to ask the user for location permissions.
     */
    private void showLocationPermissionLayout() {
        progress.setVisibility(View.GONE);
        layoutContent.setVisibility(View.GONE);
        txtErrorNoLocationFeature.setVisibility(View.GONE);
        layoutLocationPermission.setVisibility(View.VISIBLE);
    }

    /**
     * Show an error to the user when the device is not capable of receiving location updates.
     */
    private void showNoLocationFeatureLayout() {
        progress.setVisibility(View.GONE);
        layoutContent.setVisibility(View.GONE);
        layoutLocationPermission.setVisibility(View.GONE);
        txtErrorNoLocationFeature.setVisibility(View.VISIBLE);
    }

    /**
     * Update the enabled state of the positive button on the {@link AlertDialog}.
     */
    private void updatePositiveButtonEnabledState() {
        final AlertDialog dialog = (AlertDialog) getDialog();

        if (dialog != null) {
            final Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);

            if (button != null) {
                button.setEnabled(hasLocationFeature && hasLocationPermission && !isLoadingBusStop);
            }
        }
    }

    /**
     * Convert the chosen distance in the {@link Spinner} to an actual distance.
     *
     * @return The distance selected in the {@link Spinner}.
     */
    private int getSelectedMeters() {
        final int value;

        switch (spinnerDistance.getSelectedItemPosition()) {
            case 1:
                value = 250;
                break;
            case 2:
                value = 500;
                break;
            case 3:
                value = 750;
                break;
            case 4:
                value = 1000;
                break;
            default:
                value = 100;
                break;
        }

        return value;
    }

    private BroadcastReceiver locationProviderChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            handleLocationProvidersChange();
        }
    };
}
