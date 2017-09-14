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

package uk.org.rivernile.android.bustracker.ui.alerts.time;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
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
import uk.org.rivernile.android.bustracker.database.busstop.loaders.BusStopServiceNamesLoader;
import uk.org.rivernile.android.utils.ProcessedCursorLoader;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs.ServicesChooserDialogFragment;

/**
 * This {@link DialogFragment} allows a user to add a time alert for a supplied stop. It is
 * presented as a dialog.
 *
 * @author Niall Scott
 */
public class AddTimeAlertDialogFragment extends DialogFragment
        implements LoaderManager.LoaderCallbacks, ServicesChooserDialogFragment.Callbacks,
        View.OnClickListener {

    private static final String ARG_STOPCODE = "stopCode";
    private static final String ARG_DEFAULT_SERVICES = "defaultServices";

    private static final String STATE_SELECTED_SERVICES = "selectedServices";

    private static final int LOADER_BUS_STOP = 1;
    private static final int LOADER_SERVICES = 2;

    private static final String DIALOG_SELECT_SERVICES = "selectServicesDialog";
    private static final String DIALOG_TIME_ALERT_LIMITATIONS = "timeLimitationsDialog";

    private AlertManager alertManager;

    private String stopCode;
    private String[] services;
    private String[] selectedServices;
    private boolean isLoadingBusStop;
    private boolean isLoadingServices;

    private ProgressBar progress;
    private View layoutContent;
    private TextView txtBlurb;
    private TextView txtSelectedServices;
    private Button btnSelectServices;
    private Spinner spinnerTime;
    private Button btnLimitations;

    /**
     * Create a new {@code AddTimeAlertDialogFragment}, supplying only the stop code.
     *
     * @param stopCode The stop code to add a time alert for.
     * @return A new {@link AddTimeAlertDialogFragment}.
     * @see #newInstance(String, String[])
     */
    @NonNull
    public static AddTimeAlertDialogFragment newInstance(@NonNull final String stopCode) {
        final AddTimeAlertDialogFragment fragment = new AddTimeAlertDialogFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_STOPCODE, stopCode);
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Create a new {@code AddTimeAlertDialogFragment}, supplying the stop code and services that
     * are to be selected by default.
     *
     * @param stopCode The stop code to add a time alert for.
     * @param defaultServices Services that are to be selected by default.
     * @return A new {@link AddTimeAlertDialogFragment}.
     * @see #newInstance(String)
     */
    @NonNull
    public static AddTimeAlertDialogFragment newInstance(@NonNull final String stopCode,
            @Nullable final String[] defaultServices) {
        final AddTimeAlertDialogFragment fragment = new AddTimeAlertDialogFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_STOPCODE, stopCode);
        args.putStringArray(ARG_DEFAULT_SERVICES, defaultServices);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(true);
        final Bundle args = getArguments();
        stopCode = args.getString(ARG_STOPCODE);
        selectedServices = savedInstanceState != null
                ? savedInstanceState.getStringArray(STATE_SELECTED_SERVICES)
                : args.getStringArray(ARG_DEFAULT_SERVICES);

        alertManager = ((BusApplication) getContext().getApplicationContext()).getAlertManager();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Context context = getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View v = inflater.inflate(R.layout.addtimealert2, null, false);

        progress = (ProgressBar) v.findViewById(R.id.progress);
        layoutContent = v.findViewById(R.id.layoutContent);
        txtBlurb = (TextView) v.findViewById(R.id.txtBlurb);
        txtSelectedServices = (TextView) v.findViewById(R.id.txtSelectedServices);
        btnSelectServices = (Button) v.findViewById(R.id.btnSelectServices);
        spinnerTime = (Spinner) v.findViewById(R.id.spinnerTime);
        btnLimitations = (Button) v.findViewById(R.id.btnLimitations);

        btnSelectServices.setOnClickListener(this);
        btnLimitations.setOnClickListener(this);

        populateServices();

        return new AlertDialog.Builder(context)
                .setTitle(R.string.addtimealertdialog_title)
                .setPositiveButton(R.string.addtimealertdialog_button_add,
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
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        showProgress();
        loadBusStopDetails();
        loadServices();
    }

    @Override
    public void onStart() {
        super.onStart();

        updatePositiveButtonEnabledState();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putStringArray(STATE_SELECTED_SERVICES, selectedServices);
    }

    @Override
    public Loader onCreateLoader(final int id, final Bundle args) {
        switch (id) {
            case LOADER_BUS_STOP:
                return new BusStopLoader(getContext(), stopCode,
                        new String[] {
                                BusStopContract.BusStops.STOP_NAME,
                                BusStopContract.BusStops.LOCALITY
                        });
            case LOADER_SERVICES:
                return new BusStopServiceNamesLoader(getContext(), stopCode);
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
                handleServicesLoaded(((ProcessedCursorLoader.ResultWrapper<String[]>) data)
                        .getResult());
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
    public void onServicesChosen(final String[] chosenServices) {
        selectedServices = chosenServices;
        populateServices();
        updatePositiveButtonEnabledState();
    }

    @Override
    public void onClick(final View v) {
        if (v == btnSelectServices) {
            handleSelectServicesButtonClick();
        } else if (v == btnLimitations) {
            handleLimitationsButtonClick();
        }
    }

    /**
     * Begin loading bus stop details.
     */
    private void loadBusStopDetails() {
        isLoadingBusStop = true;
        getLoaderManager().initLoader(LOADER_BUS_STOP, null, this);
    }

    /**
     * Begin loading the services list for the stop.
     */
    private void loadServices() {
        isLoadingServices = true;
        getLoaderManager().initLoader(LOADER_SERVICES, null, this);
    }

    /**
     * Handle the bus stop details loading completed.
     *
     * @param cursor A {@link Cursor} containing the bus stop details.
     */
    private void handleBusStopLoaded(@Nullable final Cursor cursor) {
        isLoadingBusStop = false;
        populateStopName(cursor);
        updatePositiveButtonEnabledState();

        if (!isLoadingServices) {
            showContent();
        }
    }

    /**
     * Handle the services loading completed.
     *
     * @param services The services for the stop.
     */
    private void handleServicesLoaded(final String[] services) {
        isLoadingServices = false;
        this.services = services;
        updatePositiveButtonEnabledState();

        if (!isLoadingBusStop) {
            showContent();
        }
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

            text = getString(R.string.addtimealertdialog_blurb, nameToDisplay);
        } else {
            text = getString(R.string.addtimealertdialog_blurb, stopCode);
        }

        txtBlurb.setText(text);
    }

    /**
     * Handle a click on the dialog positive button.
     */
    private void handlePositiveButtonClick() {
        alertManager.addTimeAlert(stopCode, selectedServices, getSelectedTimeTrigger());
    }

    /**
     * Handle the select services button being clicked.
     */
    private void handleSelectServicesButtonClick() {
        final ServicesChooserDialogFragment fragment =
                ServicesChooserDialogFragment.newInstance(services,selectedServices,
                        getString(R.string.addtimealertdialog_services_chooser_dialog_title));
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), DIALOG_SELECT_SERVICES);
    }

    /**
     * Handle the limitations button being clicked.
     */
    private void handleLimitationsButtonClick() {
        TimeLimitationsDialogFragment.newInstance()
                .show(getFragmentManager(), DIALOG_TIME_ALERT_LIMITATIONS);
    }

    /**
     * Populate the selected services text.
     */
    private void populateServices() {
        if (selectedServices != null && selectedServices.length > 0) {
            txtSelectedServices.setText(
                    ServicesChooserDialogFragment.getChosenServicesAsString(selectedServices));
        } else {
            txtSelectedServices.setText(R.string.addtimealertdialog_no_services_selected);
        }
    }

    /**
     * Show the progress layout.
     */
    private void showProgress() {
        layoutContent.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
    }

    /**
     * Show the content layout.
     */
    private void showContent() {
        progress.setVisibility(View.GONE);
        layoutContent.setVisibility(View.VISIBLE);
    }

    /**
     * Update the enabled state of the positive button on the {@link AlertDialog}.
     */
    private void updatePositiveButtonEnabledState() {
        final AlertDialog dialog = (AlertDialog) getDialog();

        if (dialog != null) {
            final Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);

            if (button != null) {
                button.setEnabled(!isLoadingBusStop &&
                        !isLoadingServices &&
                        selectedServices != null &&
                        selectedServices.length > 0);
            }
        }
    }

    /**
     * Get the selected number of minutes to use as the time trigger.
     *
     * @return The selected number of minutes to use as the time trigger.
     */
    private int getSelectedTimeTrigger() {
        final int value;

        switch (spinnerTime.getSelectedItemPosition()) {
            case 0:
                value = 1;
                break;
            case 1:
                value = 2;
                break;
            case 2:
                value = 5;
                break;
            case 3:
                value = 10;
                break;
            case 4:
                value = 15;
                break;
            case 5:
                value = 20;
                break;
            case 6:
                value = 25;
                break;
            case 7:
                value = 30;
                break;
            default:
                value = 0;
                break;
        }

        return value;
    }
}
