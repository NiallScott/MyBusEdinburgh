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
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
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
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.List;

import uk.org.rivernile.android.bustracker.BusApplication;
import uk.org.rivernile.android.bustracker.alerts.AlertManager;
import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract;
import uk.org.rivernile.android.bustracker.database.busstop.loaders.BusStopLoader;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowSystemLocationPreferencesListener;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link Fragment} allows the user to add a new proximity alert. This alerts the user when
 * they are within a user-selected range of a particular bus stop.
 * 
 * @author Niall Scott
 */
public class AddProximityAlertFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener,
        AdapterView.OnItemSelectedListener {
    
    /** The stopCode argument. */
    public static final String ARG_STOPCODE = "stopCode";
    
    /** The Intent used to show users device location settings. */
    public static final Intent LOCATION_SETTINGS_INTENT;

    private static final int LOADER_BUS_STOP = 1;
    
    private Callbacks callbacks;
    private AlertManager alertMan;
    private LocationManager locMan;
    private String stopCode;
    private int meters = 100;

    private Button btnLimitations, btnOkay, btnCancel;
    private CheckBox checkProxGps;
    private Spinner spinnerDistance;
    private TextView textProxDialogStop;
    
    static {
        // Set up the location settings intent statically. It doesn't need to be constantly
        // repeated.
        LOCATION_SETTINGS_INTENT = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        LOCATION_SETTINGS_INTENT.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    }
    
    /**
     * Create a new instance of the {@code AddProximityAlertFragment}.
     * 
     * @param stopCode The stop code this alert setting should be for.
     * @return A new instance of this {@link Fragment}.
     */
    @NonNull
    public static AddProximityAlertFragment newInstance(@NonNull final String stopCode) {
        final AddProximityAlertFragment f = new AddProximityAlertFragment();
        final Bundle b = new Bundle();
        b.putString(ARG_STOPCODE, stopCode);
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

        stopCode = getArguments().getString(ARG_STOPCODE);
        final BusApplication app = (BusApplication) getActivity().getApplication();
        
        // Get the various resources.
        alertMan = app.getAlertManager();
        locMan = (LocationManager) app.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.addproxalert, container, false);

        btnLimitations = (Button) v.findViewById(R.id.btnLimitations);
        btnOkay = (Button) v.findViewById(R.id.btnOkay);
        btnCancel = (Button) v.findViewById(R.id.btnCancel);
        checkProxGps = (CheckBox) v.findViewById(R.id.checkProxGPS);
        spinnerDistance = (Spinner) v.findViewById(R.id.prox_distance_select);
        textProxDialogStop = (TextView) v.findViewById(R.id.textProxDialogStop);

        btnLimitations.setOnClickListener(this);
        btnOkay.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        final ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(getActivity(), R.array.addproxalert_distance_array,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistance.setAdapter(adapter);
        spinnerDistance.setOnItemSelectedListener(this);
        
        return v;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        getLoaderManager().initLoader(LOADER_BUS_STOP, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        
        // Make sure the system preferences Activity which lets the user change GPS settings is
        // available.
        final List<ResolveInfo> packages = getActivity().getPackageManager()
                .queryIntentActivities(LOCATION_SETTINGS_INTENT, 0);
        if (packages != null && !packages.isEmpty() &&
                !locMan.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            checkProxGps.setVisibility(View.VISIBLE);
        } else {
            // Don't display if settings Activity is not present, or GPS is enabled.
            checkProxGps.setVisibility(View.GONE);
        }
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
        // Nothing to do here.
    }

    @Override
    public void onClick(final View v) {
        if (v == btnLimitations) {
            doLimitationsClicked();
        } else if (v == btnOkay) {
            doOkayClicked();
        } else if (v == btnCancel) {
            doCancelClicked();
        }
    }

    @Override
    public void onItemSelected(final AdapterView<?> parent, final View view, final int position,
            final long id) {
        if (parent == spinnerDistance) {
            switch (position) {
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
    }

    @Override
    public void onNothingSelected(final AdapterView<?> parent) {
        if (parent == spinnerDistance) {
            meters = 100;
        }
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

            textProxDialogStop.setText(Html.fromHtml(getString(R.string.addtimealert_busstop,
                    nameToDisplay)));
        } else {
            textProxDialogStop.setText(null);
        }
    }

    /**
     * This is called when the user clicks the limitations button.
     */
    private void doLimitationsClicked() {
        callbacks.onShowProximityAlertLimitations();
    }

    /**
     * This is called when the user clicks the okay button.
     */
    private void doOkayClicked() {
        // Add a new proximity alert.
        alertMan.addProximityAlert(stopCode, meters);

        // Start the GPS preference Activity if the user has checked the box.
        if (checkProxGps.getVisibility() == View.VISIBLE && checkProxGps.isChecked()) {
            callbacks.onShowSystemLocationPreferences();
        }

        // Tell the hosting Activity that a new alert has been added.
        callbacks.onProximityAlertAdded();
    }

    /**
     * This is called when the user clicks the cancel button.
     */
    private void doCancelClicked() {
        // Tell the hosting Activity that the user has cancelled.
        callbacks.onCancelAddProximityAlert();
    }

    /**
     * Any {@link android.app.Activity Activities} which host this {@link Fragment} must
     * implement this interface to handle navigation events.
     */
    public interface Callbacks extends OnShowSystemLocationPreferencesListener {
        
        /**
         * This is called when the user wants to read the text about the proximity alert
         * limitations.
         */
        void onShowProximityAlertLimitations();
        
        /**
         * This is called when the user has added a new proximity alert.
         */
        void onProximityAlertAdded();
        
        /**
         * This is called when the user wants to cancel adding a new proximity alert.
         */
        void onCancelAddProximityAlert();
    }
}