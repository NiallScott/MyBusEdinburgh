/*
 * Copyright (C) 2011 - 2014 Niall 'Rivernile' Scott
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
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
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
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.List;
import uk.org.rivernile.android.bustracker.ui.callbacks
        .OnShowSystemLocationPreferencesListener;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.edinburghbustracker.android.alerts.AlertManager;

/**
 * This fragment allows the user to add a new proximity alert. This alerts the
 * user when they are within a user-selected range of a particular bus stop.
 * 
 * @author Niall Scott
 */
public class AddProximityAlertFragment extends Fragment {
    
    /** The stopCode argument. */
    public static final String ARG_STOPCODE = "stopCode";
    
    /** The Intent used to show users device location settings. */
    public static final Intent LOCATION_SETTINGS_INTENT;
    
    private Callbacks callbacks;
    private AlertManager alertMan;
    private LocationManager locMan;
    private BusStopDatabase bsd;
    private String stopCode;
    private CheckBox checkProxGps;
    private Spinner spinner;
    private TextView textProxDialogStop;
    private int meters = 100;
    
    static {
        // Set up the location settings intent statically. It doesn't need to
        // be constantly repeated.
        LOCATION_SETTINGS_INTENT = new Intent(
                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        LOCATION_SETTINGS_INTENT.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    }
    
    /**
     * Create a new instance of the AddProximityAlertFragment.
     * 
     * @param stopCode The stopCode this alert setting should be for.
     * @return A new instance of this Fragment.
     */
    public static AddProximityAlertFragment newInstance(final String stopCode) {
        final AddProximityAlertFragment f = new AddProximityAlertFragment();
        final Bundle b = new Bundle();
        b.putString(ARG_STOPCODE, stopCode);
        f.setArguments(b);
        
        return f;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        
        try {
            callbacks = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new IllegalStateException(activity.getClass().getName() +
                    " does not implement " + Callbacks.class.getName());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final Bundle args = getArguments();
        // Make sure that an args Bundle exists.
        if(args == null)
            throw new IllegalStateException("Arguments must be supplied to " +
                    "the AddProximityAlertFragment.");
        
        // Get the stopCode argument and make sure it is valid.
        stopCode = args.getString(ARG_STOPCODE);
        if(stopCode == null || stopCode.length() == 0)
            throw new IllegalArgumentException("The stopCode argument must " +
                    "not be null or empty.");
        
        final Activity activity = getActivity();
        
        // Get the various resources.
        alertMan = AlertManager.getInstance(activity.getApplicationContext());
        locMan = (LocationManager)activity
                .getSystemService(Context.LOCATION_SERVICE);
        bsd = BusStopDatabase.getInstance(activity.getApplicationContext());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.addproxalert, container,
                false);
        
        checkProxGps = (CheckBox)v.findViewById(R.id.checkProxGPS);
        spinner = (Spinner)v.findViewById(R.id.prox_distance_select);
        textProxDialogStop = (TextView)v.findViewById(R.id.textProxDialogStop);
        
        Button btn = (Button)v.findViewById(R.id.btnOkay);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                // Add a new proximity alert.
                alertMan.addProximityAlert(stopCode, meters);
                // Start the GPS preference Activity if the user has checked
                // the box.
                if(checkProxGps.isChecked()) {
                    callbacks.onShowSystemLocationPreferences();
                }
                // Tell the hosting Activity that a new alert has been added.
                callbacks.onProximityAlertAdded();
            }
        });
        
        btn = (Button)v.findViewById(R.id.btnCancel);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                // Tell the hosting Activity that the user has cancelled.
                callbacks.onCancelAddProximityAlert();
            }
        });
        
        btn = (Button)v.findViewById(R.id.btnLimitations);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                callbacks.onShowProximityAlertLimitations();
            }
        });
        
        return v;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        // Sort the distance spinner.
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(getActivity(),
                    R.array.addproxalert_distance_array,
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
                        meters = 100;
                        break;
                    case 1:
                        meters = 250;
                        break;
                    case 2:
                        meters = 500;
                        break;
                    case 3:
                        meters = 750;
                        break;
                    case 4:
                        meters = 1000;
                        break;
                    default:
                        meters = 100;
                        break;
                }
            }

            @Override
            public void onNothingSelected(final AdapterView parent) {
                meters = 100;
            }
        });
        
        // Get locality information, if there is any.
        final String locality = bsd.getLocalityForStopCode(stopCode);
        final String name;
        
        if(locality != null) {
            name = getString(R.string.busstop_locality_coloured,
                    bsd.getNameForBusStop(stopCode), locality, stopCode);
        } else {
            name = getString(R.string.busstop_coloured,
                    bsd.getNameForBusStop(stopCode), stopCode);
        }
        
        // Set the information text.
        textProxDialogStop.setText(Html.fromHtml(
                getString(R.string.addproxalert_second, name)));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        
        // Make sure the system preferences Activity which lets the user change
        // GPS settings is available.
        final List<ResolveInfo> packages = getActivity().getPackageManager()
                .queryIntentActivities(LOCATION_SETTINGS_INTENT, 0);
        if(packages != null && !packages.isEmpty() &&
                !locMan.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            checkProxGps.setVisibility(View.VISIBLE);
        } else {
            // Don't display if settings Activity is not present, or GPS is
            // enabled.
            checkProxGps.setVisibility(View.GONE);
        }
    }
    
    /**
     * Any Activities which host this Fragment must implement this interface to
     * handle navigation events.
     */
    public static interface Callbacks
            extends OnShowSystemLocationPreferencesListener {
        
        /**
         * This is called when the user wants to read the text about the
         * proximity alert limitations.
         */
        public void onShowProximityAlertLimitations();
        
        /**
         * This is called when the user has added a new proximity alert.
         */
        public void onProximityAlertAdded();
        
        /**
         * This is called when the user wants to cancel adding a new proximity
         * alert.
         */
        public void onCancelAddProximityAlert();
    }
}