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

package uk.org.rivernile.edinburghbustracker.android.fragments.general;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import uk.org.rivernile.android.bustracker.BusApplication;
import uk.org.rivernile.android.bustracker.alerts.AlertManager;
import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract;
import uk.org.rivernile.android.bustracker.database.busstop.loaders.BusStopLoader;
import uk.org.rivernile.android.bustracker.database.busstop.loaders.BusStopServiceNamesLoader;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowServicesChooserListener;
import uk.org.rivernile.android.utils.ProcessedCursorLoader;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs.ServicesChooserDialogFragment;

/**
 * This fragment allows the user to add a new time alert. This alerts the user when a service
 * that they have selected is within a certain time of a chosen bus stop.
 * 
 * @author Niall Scott
 */
public class AddTimeAlertFragment extends Fragment
        implements LoaderManager.LoaderCallbacks, ServicesChooserDialogFragment.Callbacks,
        View.OnClickListener, AdapterView.OnItemSelectedListener {
    
    /** The argument for the stopCode. */
    public static final String ARG_STOPCODE = "stopCode";
    /** The argument for the default services. */
    public static final String ARG_DEFAULT_SERVICES = "defaultServices";
    /** The argument used in saving the instance state.*/
    private static final String ARG_SELECTED_SERVICES = "selectedServices";

    private static final int LOADER_BUS_STOP = 1;
    private static final int LOADER_SERVICES = 2;
    
    private Callbacks callbacks;
    private AlertManager alertMan;
    private String stopCode;
    private String[] services;
    private String[] selectedServices;
    private int timeTrigger;
    
    private Button btnOkay, btnCancel, btnServices, btnLimitations;
    private TextView txtServices, txtTimeDialogStop;
    private Spinner spinnerTimeSelect;
    
    /**
     * Create a new instance of the {@code AddTimeAlertFragment}.
     * 
     * @param stopCode The stop code this alert setting should be for.
     * @return A new instance of this {@link Fragment}.
     */
    @NonNull
    public static AddTimeAlertFragment newInstance(@NonNull final String stopCode) {
        final AddTimeAlertFragment f = new AddTimeAlertFragment();
        final Bundle b = new Bundle();
        b.putString(ARG_STOPCODE, stopCode);
        f.setArguments(b);
        
        return f;
    }
    
    /**
     * Create a new instance of the {@code AddTimeAlertFragment}.
     * 
     * @param stopCode The stop code this alert setting should be for.
     * @param defaultServices The default services to show.
     * @return A new instance of this {@link Fragment}.
     */
    @NonNull
    public static AddTimeAlertFragment newInstance(@NonNull final String stopCode,
            @Nullable final String[] defaultServices) {
        final AddTimeAlertFragment f = new AddTimeAlertFragment();
        final Bundle b = new Bundle();
        b.putString(ARG_STOPCODE, stopCode);
        b.putStringArray(ARG_DEFAULT_SERVICES, defaultServices);
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

        alertMan = ((BusApplication) getActivity().getApplicationContext()).getAlertManager();
        final Bundle args = getArguments();
        // Get the stop code from the arguments.
        stopCode = args.getString(ARG_STOPCODE);
        selectedServices = savedInstanceState != null
                ? savedInstanceState.getStringArray(ARG_SELECTED_SERVICES)
                : args.getStringArray(ARG_DEFAULT_SERVICES);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.addtimealert, container, false);
        
        btnOkay = (Button) v.findViewById(R.id.btnOkay);
        btnCancel = (Button) v.findViewById(R.id.btnCancel);
        btnServices = (Button) v.findViewById(R.id.btnAlertTimeServices);
        btnLimitations =  (Button) v.findViewById(R.id.btnLimitations);
        txtServices = (TextView) v.findViewById(R.id.txtTimeAlertServices);
        txtTimeDialogStop = (TextView) v.findViewById(R.id.txtTimeDialogStop);
        spinnerTimeSelect = (Spinner) v.findViewById(R.id.time_time_select);

        btnOkay.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnServices.setOnClickListener(this);
        btnLimitations.setOnClickListener(this);
        spinnerTimeSelect.setOnItemSelectedListener(this);
        
        // Set up the spinner.
        final ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(getActivity(), R.array.addtimealert_array,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeSelect.setAdapter(adapter);
        
        // Force a refresh of the TextView that shows the services that have been chosen.
        onServicesChosen(selectedServices);
        
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(LOADER_BUS_STOP, null, this);
        loaderManager.initLoader(LOADER_SERVICES, null, this);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putStringArray(ARG_SELECTED_SERVICES, selectedServices);
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
        // Nothing to do here.
    }

    @Override
    public void onClick(final View v) {
        if (v == btnOkay) {
            doOkayClicked();
        } else if (v == btnCancel) {
            doCancelClicked();
        } else if (v == btnServices) {
            doServicesClicked();
        } else if (v == btnLimitations) {
            doLimitationsClicked();
        }
    }

    @Override
    public void onItemSelected(final AdapterView<?> parent, final View view, final int position,
            final long id) {
        if (parent == spinnerTimeSelect) {
            switch (position) {
                case 0:
                    timeTrigger = 1;
                    break;
                case 1:
                    timeTrigger = 2;
                    break;
                case 2:
                    timeTrigger = 5;
                    break;
                case 3:
                    timeTrigger = 10;
                    break;
                case 4:
                    timeTrigger = 15;
                    break;
                case 5:
                    timeTrigger = 20;
                    break;
                case 6:
                    timeTrigger = 25;
                    break;
                case 7:
                    timeTrigger = 30;
                    break;
                default:
                    timeTrigger = 0;
                    break;
            }
        }
    }

    @Override
    public void onNothingSelected(final AdapterView<?> parent) {
        if (parent == spinnerTimeSelect) {
            timeTrigger = 0;
        }
    }

    @Override
    public void onServicesChosen(final String[] chosenServices) {
        selectedServices = chosenServices;
        
        if (chosenServices != null && chosenServices.length > 0) {
            // If the services list is not empty, put the services list in the TextView.
            txtServices.setText(BusStopDatabase.getColouredServiceListString(
                    ServicesChooserDialogFragment.getChosenServicesAsString(chosenServices)));
        } else {
            // If the services list is empty, put the default text in the TextView.
            txtServices.setText(R.string.addtimealert_noservices);
        }

        updateOkayButton();
    }

    /**
     * Handle the bus stop data being loaded.
     *
     * @param cursor The {@link Cursor} containing the data for the bus stop.
     */
    private void handleBusStopLoaded(@Nullable final Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            final String name = cursor.getString(cursor.getColumnIndex(
                    BusStopContract.BusStops.STOP_NAME));
            final String locality = cursor.getString(cursor.getColumnIndex(
                    BusStopContract.BusStops.LOCALITY));
            final String nameToDisplay;

            if (locality != null) {
                nameToDisplay = getString(R.string.busstop_locality_coloured, name, locality,
                        stopCode);
            } else {
                nameToDisplay = getString(R.string.busstop_coloured, name, stopCode);
            }

            txtTimeDialogStop.setText(Html.fromHtml(getString(R.string.addtimealert_busstop,
                    nameToDisplay)));
        } else {
            txtTimeDialogStop.setText(null);
        }
    }

    /**
     * Handle the array of services for the bus stop being loaded.
     *
     * @param services The array of services that stop at this bus stop.
     */
    private void handleServicesLoaded(@Nullable final String[] services) {
        this.services = services;
        updateServicesButton();
    }

    /**
     * This is called when the user clicks the okay button.
     */
    private void doOkayClicked() {
        // Add the alert.
        alertMan.addTimeAlert(stopCode, selectedServices, timeTrigger);
        // Tell the underlying Activity that a new alert has been added.
        callbacks.onTimeAlertAdded();
    }

    /**
     * This is called when the user clicks the cancel button.
     */
    private void doCancelClicked() {
        // Tell the underlying Activity that the user has cancelled.
        callbacks.onCancelAddTimeAlert();
    }

    /**
     * This is called when the user clicks the services chooser button.
     */
    private void doServicesClicked() {
        if (services != null && services.length > 0) {
            callbacks.onShowServicesChooser(services, selectedServices,
                    getString(R.string.addtimealert_services_title));
        }
    }

    /**
     * This is called when the user clicks the limitations button.
     */
    private void doLimitationsClicked() {
        callbacks.onShowTimeAlertLimitations();
    }

    /**
     * Update the status of the okay button. This will make sure the button is only enabled when the
     * selected services array has 1 or more items.
     */
    private void updateOkayButton() {
        btnOkay.setEnabled(selectedServices != null && selectedServices.length > 0);
    }

    /**
     * Update the status of the services chooser button. This will make sure the button is only
     * enabled when the services array has 1 or more items.
     */
    private void updateServicesButton() {
        btnServices.setEnabled(services != null && services.length > 0);
    }
    
    /**
     * Any {@link android.app.Activity Activities} which hosts this {@link Fragment} must implement
     * this interface to handle navigation events.
     */
    public interface Callbacks extends OnShowServicesChooserListener {
        
        /**
         * This is called when the user wants to read the text about the time alert limitations.
         */
        void onShowTimeAlertLimitations();
        
        /**
         * This is called when the user has added a new time alert.
         */
        void onTimeAlertAdded();
        
        /**
         * This is called when the user wants to cancel adding a new time alert.
         */
        void onCancelAddTimeAlert();
    }
}