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

package uk.org.rivernile.android.bustracker.ui.alerts;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
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
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .ProximityLimitationsDialogFragment;

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

    private String stopCode;
    private boolean hasLocationFeature;
    private boolean hasLocationPermission;
    private boolean isLoadingBusStop;
    private Cursor stopDetailsCursor;

    private ProgressBar progress;
    private View layoutContent;
    private View txtErrorNoLocationFeature;
    private View layoutLocationPermission;
    private Button btnGrantPermission;
    private TextView txtBlurb;
    private Spinner spinnerDistance;
    private Button btnLimitations;

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

        final BusApplication app = (BusApplication) getContext().getApplicationContext();
        alertManager = app.getAlertManager();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        final View v = inflater.inflate(R.layout.addproxalert2, null, false);
        progress = (ProgressBar) v.findViewById(R.id.progress);
        layoutContent = v.findViewById(R.id.layoutContent);
        txtErrorNoLocationFeature = v.findViewById(R.id.txtErrorNoLocationFeature);
        layoutLocationPermission = v.findViewById(R.id.layoutLocationPermission);
        btnGrantPermission = (Button) v.findViewById(R.id.btnGrantPermission);
        txtBlurb = (TextView) v.findViewById(R.id.txtBlurb);
        spinnerDistance = (Spinner) v.findViewById(R.id.spinnerDistance);
        btnLimitations = (Button) v.findViewById(R.id.btnLimitations);

        btnLimitations.setOnClickListener(this);
        btnGrantPermission.setOnClickListener(this);

        return new AlertDialog.Builder(getContext())
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

        checkLocationCapability();

        if (hasLocationFeature) {
            checkLocationPermission();
            showProgress();
            isLoadingBusStop = true;
            getLoaderManager().initLoader(LOADER_BUS_STOP, null, this);

            if (!hasLocationPermission && savedInstanceState == null) {
                requestLocationPermission();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        checkLocationPermission();
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
        handleBusStopLoaded(null);
    }

    @Override
    public void onClick(final View v) {
        if (v == btnLimitations) {
            handleLimitationsButtonClick();
        } else if (v == btnGrantPermission) {
            requestLocationPermission();
        }
    }

    /**
     * Check to see if the device is capable of receiving location updates. Note that this does not
     * check permissions - these are checked elsewhere.
     */
    private void checkLocationCapability() {
        hasLocationFeature = getActivity().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_LOCATION);

        if (!hasLocationFeature) {
            progress.setVisibility(View.GONE);
            layoutContent.setVisibility(View.GONE);
            layoutLocationPermission.setVisibility(View.GONE);
            txtErrorNoLocationFeature.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Check to see if the app has been granted location permissions by the user.
     */
    private void checkLocationPermission() {
        hasLocationPermission = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

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
     * Handle the load of the bus stop data {@link Cursor}.
     *
     * @param cursor The bus stop data.
     */
    private void handleBusStopLoaded(@Nullable final Cursor cursor) {
        stopDetailsCursor = cursor;
        populateStopName();
        showContent();
        isLoadingBusStop = false;
    }

    /**
     * Populate the bus stop name in the blurb text.
     */
    private void populateStopName() {
        final String text;

        if (stopDetailsCursor != null && stopDetailsCursor.moveToFirst()) {
            final String name = stopDetailsCursor.getString(
                    stopDetailsCursor.getColumnIndex(BusStopContract.BusStops.STOP_NAME));
            final String locality = stopDetailsCursor.getString(
                    stopDetailsCursor.getColumnIndex(BusStopContract.BusStops.LOCALITY));
            final String nameToDisplay;

            if (!TextUtils.isEmpty(locality)) {
                nameToDisplay = getString(R.string.busstop_locality, name, locality, stopCode);
            } else {
                nameToDisplay = getString(R.string.busstop, name, stopCode);
            }

            text = getString(R.string.addproxalertdialog_blurb, nameToDisplay);
        } else {
            text = stopCode;
        }

        txtBlurb.setText(text);
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
     * Show the progress layout to the user.
     */
    private void showProgress() {
        if (hasLocationPermission) {
            layoutLocationPermission.setVisibility(View.GONE);
            layoutContent.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
        } else {
            showLocationPermissionLayout();
        }
    }

    /**
     * Show the content layout to the user.
     */
    private void showContent() {
        if (hasLocationPermission) {
            layoutLocationPermission.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);
            layoutContent.setVisibility(View.VISIBLE);
            //getAlertDialog().getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
        } else {
            showLocationPermissionLayout();
        }
    }

    /**
     * Show the layout to ask the user for location permissions.
     */
    private void showLocationPermissionLayout() {
        progress.setVisibility(View.GONE);
        layoutContent.setVisibility(View.GONE);
        layoutLocationPermission.setVisibility(View.VISIBLE);
        //getAlertDialog().getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
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
}
