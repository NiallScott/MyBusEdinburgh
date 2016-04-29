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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import uk.org.rivernile.android.bustracker.ui.callbacks
        .OnShowServicesChooserListener;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.edinburghbustracker.android.alerts.AlertManager;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .ServicesChooserDialogFragment;

/**
 * This fragment allows the user to add a new time alert. This alerts the user
 * when a service that they have selected is within a certain time of a chosen
 * bus stop.
 * 
 * @author Niall Scott
 */
public class AddTimeAlertFragment extends Fragment
        implements ServicesChooserDialogFragment.Callbacks {
    
    /** The argument for the stopCode. */
    public static final String ARG_STOPCODE = "stopCode";
    /** The argument for the default services. */
    public static final String ARG_DEFAULT_SERVICES = "defaultServices";
    /** The argument used in saving the instance state.*/
    private static final String ARG_SELECTED_SERVICES = "selectedServices";
    
    private Callbacks callbacks;
    private BusStopDatabase bsd;
    private AlertManager alertMan;
    private String stopCode;
    private String[] services;
    private String[] selectedServices;
    private int timeTrigger = 0;
    
    private Button btnOkay;
    private TextView txtServices, txtTimeDialogStop;
    
    /**
     * Create a new instance of the AddTimeAlertFragment.
     * 
     * @param stopCode The stopCode this alert setting should be for.
     * @return A new instance of this Fragment.
     */
    public static AddTimeAlertFragment newInstance(final String stopCode) {
        final AddTimeAlertFragment f = new AddTimeAlertFragment();
        final Bundle b = new Bundle();
        b.putString(ARG_STOPCODE, stopCode);
        f.setArguments(b);
        
        return f;
    }
    
    /**
     * Create a new instance of the AddTimeAlertFragment.
     * 
     * @param stopCode The stopCode this alert setting should be for.
     * @param defaultServices The default services to show.
     * @return A new instance of this Fragment.
     */
    public static AddTimeAlertFragment newInstance(final String stopCode,
            final String[] defaultServices) {
        final AddTimeAlertFragment f = new AddTimeAlertFragment();
        final Bundle b = new Bundle();
        b.putString(ARG_STOPCODE, stopCode);
        b.putStringArray(ARG_DEFAULT_SERVICES, defaultServices);
        f.setArguments(b);
        
        return f;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        
        try {
            callbacks = (Callbacks) context;
        } catch (ClassCastException e) {
            throw new IllegalStateException(context.getClass().getName() +
                    " does not implement " + Callbacks.class.getName());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get the various resources.
        final Context context = getActivity().getApplicationContext();
        bsd = BusStopDatabase.getInstance(context);
        alertMan = AlertManager.getInstance(context);
        
        final Bundle args = getArguments();
        // Get the stop code from the arguments.
        stopCode = args.getString(ARG_STOPCODE);

        // Make sure a stopcode exists.
        if(stopCode == null || stopCode.length() == 0)
            throw new IllegalArgumentException("A stop code must be " +
                    "supplied.");
        
        services = bsd.getBusServicesForStop(stopCode);
        
        if (savedInstanceState != null) {
            selectedServices = savedInstanceState
                    .getStringArray(ARG_SELECTED_SERVICES);
        } else {
            selectedServices = args.getStringArray(ARG_DEFAULT_SERVICES);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.addtimealert, container,
                false);
        
        btnOkay = (Button)v.findViewById(R.id.btnOkay);
        txtServices = (TextView)v.findViewById(R.id.txtTimeAlertServices);
        txtTimeDialogStop = (TextView)v.findViewById(R.id.txtTimeDialogStop);
        
        btnOkay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                // Add the alert.
                alertMan.addTimeAlert(stopCode, selectedServices, timeTrigger);
                // Tell the underlying Activity that a new alert has been added.
                callbacks.onTimeAlertAdded();
            }
        });
        
        // Set up the spinner.
        final Spinner spinner = (Spinner)v.findViewById(R.id.time_time_select);
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(getActivity(),
                    R.array.addtimealert_array,
                    android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent,
                    final View view, final int pos, final long id) {
                switch(pos) {
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

            @Override
            public void onNothingSelected(final AdapterView parent) {
                timeTrigger = 0;
            }
        });
        
        Button btn = (Button)v.findViewById(R.id.btnCancel);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                // Tell the underlying Activity that the user has cancelled.
                callbacks.onCancelAddTimeAlert();
            }
        });
        
        btn = (Button)v.findViewById(R.id.btnAlertTimeServices);
        if (services != null && services.length > 0) {
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    callbacks.onShowServicesChooser(services, selectedServices,
                            getString(R.string.addtimealert_services_title));
                }
            });
        } else {
            btn.setEnabled(false);
        }
        
        btn = (Button)v.findViewById(R.id.btnLimitations);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                callbacks.onShowTimeAlertLimitations();
            }
        });
        
        // Set a piece of informative text with the stopCode, stopName and
        // locality (if available).
        final String locality = bsd.getLocalityForStopCode(stopCode);
        final String name;
        
        if(locality != null) {
            name = getString(R.string.busstop_locality_coloured,
                    bsd.getNameForBusStop(stopCode), locality, stopCode);
        } else {
            name = getString(R.string.busstop_coloured,
                    bsd.getNameForBusStop(stopCode), stopCode);
        }
        
        txtTimeDialogStop.setText(Html.fromHtml(
                getString(R.string.addtimealert_busstop, name)));
        
        // Force a refresh of the TextView that shows the services that have
        // been chosen.
        onServicesChosen(selectedServices);
        
        return v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putStringArray(ARG_SELECTED_SERVICES, selectedServices);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onServicesChosen(final String[] chosenServices) {
        selectedServices = chosenServices;
        
        if (chosenServices != null && chosenServices.length > 0) {
            // If the services list is not empty, put the services list in the
            // view and enable the okay button.
            txtServices.setText(
                    BusStopDatabase.getColouredServiceListString(
                            ServicesChooserDialogFragment
                                    .getChosenServicesAsString(
                                            chosenServices)));
            btnOkay.setEnabled(true);
        } else {
            // If the services list is empty, put the default text in the view
            // and disable the okay button.
            txtServices.setText(R.string.addtimealert_noservices);
            btnOkay.setEnabled(false);
        }
    }
    
    /**
     * Any Activities which host this Fragment must implement this interface to
     * handle navigation events.
     */
    public static interface Callbacks extends OnShowServicesChooserListener {
        
        /**
         * This is called when the user wants to read the text about the time
         * alert limitations.
         */
        public void onShowTimeAlertLimitations();
        
        /**
         * This is called when the user has added a new time alert.
         */
        public void onTimeAlertAdded();
        
        /**
         * This is called when the user wants to cancel adding a new time alert.
         */
        public void onCancelAddTimeAlert();
    }
}