/*
 * Copyright (C) 2011 - 2012 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import uk.org.rivernile.edinburghbustracker.android.alerts.AlertManager;

/**
 * Add a new time alert. This allows the user to specify a list of services to
 * watch and the time trigger in which to alert them. If this activity is
 * started with no stopCode in the Intent, then the activity will exit
 * immediately.
 * 
 * @author Niall Scott
 */
public class AddTimeAlertActivity extends Activity {
    
    private static final int DIALOG_SELECT_SERVICES = 1;
    private static final byte DIALOG_LIMITATIONS = 2;
    
    private String stopCode;
    private String defaultService;
    private int timeTrigger = 0;
    private AlertManager alertMan;
    private BusStopDatabase bsd;
    private String[] servicesList;
    private boolean[] checkBoxes;
    private Button btnOkay;
    private TextView txtServices;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // If the intent does not contain "stopCode" in its extra data, then we
        // finish up and return.
        final Intent intent = getIntent();
        if(!intent.hasExtra("stopCode")) {
            finish();
            return;
        }
        
        // Set the title and content view.
        setTitle(R.string.alert_dialog_time_title);
        setContentView(R.layout.addtimealert);
        
        // Initialise things.
        stopCode = intent.getStringExtra("stopCode");
        defaultService = intent.getStringExtra("defaultService");
        alertMan = AlertManager.getInstance(this);
        bsd = BusStopDatabase.getInstance(this);
        btnOkay = (Button)findViewById(R.id.btnOkay);
        txtServices = (TextView)findViewById(R.id.txtTimeAlertServices);

        final String locality = bsd.getLocalityForStopCode(stopCode);
        String stopNameCode;
        if(locality == null) {
            // Format the string for when we do not have locality.
            stopNameCode = bsd.getNameForBusStop(stopCode) + " (" + stopCode +
                    ")";
        } else {
            // Format the string for when we do have locality.
            stopNameCode = bsd.getNameForBusStop(stopCode) + ", " + locality +
                    " (" + stopCode + ")";
        }
        
        if(savedInstanceState != null) {
            // If there was a cofiguration change, such as screen rotation,
            // use the servicesList and checkBoxes from the previous state.
            servicesList = savedInstanceState.getStringArray("servicesList");
            checkBoxes = savedInstanceState.getBooleanArray("checkBoxes");
        } else {
            // ...otherwise create a fresh state as it looks like we've been
            // started fresh.
            servicesList = bsd.getBusServicesForStop(stopCode);
            int len = servicesList.length;
            checkBoxes = new boolean[len];
            
            // If we have a fresh start and there's a defaultService available,
            // populate that in the checkboxes.
            if(defaultService != null && defaultService.length() > 0) {
                for(int i = 0; i < len; i++) {
                    if(servicesList[i].equals(defaultService)) {
                        checkBoxes[i] = true;
                        break;
                    }
                }
            }
        }
        
        populateServicesText();
        
        btnOkay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                int count = 0;
                int len = checkBoxes.length;

                // Get a count so we know how long our services array will be.
                for(boolean b : checkBoxes) {
                    if(b) count++;
                }

                String[] services = new String[count];
                int j = 0;
                
                // Populate the services array in preparation to send it to the
                // AlertManager.
                for(int i = 0; i < len; i++) {
                    if(checkBoxes[i]) {
                        services[j] = servicesList[i];
                        j++;
                    }
                }

                // Add the alert then finish up.
                alertMan.addTimeAlert(stopCode, services, timeTrigger);
                finish();
            }
        });
        
        Button btn = (Button)findViewById(R.id.btnCancel);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                finish();
            }
        });
        
        btn = (Button)findViewById(R.id.btnAlertTimeServices);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                showDialog(DIALOG_SELECT_SERVICES);
            }
        });
        
        btn = (Button)findViewById(R.id.btnLimitations);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                showDialog(DIALOG_LIMITATIONS);
            }
        });
        
        // Populate the bus stop information TextView.
        TextView tv = (TextView)findViewById(R.id.txtTimeDialogStop);
        tv.setText(getString(R.string.alert_dialog_time_busstop).replace("%s",
                stopNameCode));
        
        // Prepare our Spinner.
        final Spinner spinner = (Spinner)findViewById(R.id.time_time_select);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(this,
                    R.array.alert_dialog_time_array,
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
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        
        // Put the services list and checkboxes state in to the outState.
        outState.putStringArray("servicesList", servicesList);
        outState.putBooleanArray("checkBoxes", checkBoxes);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Dialog onCreateDialog(final int id) {
        switch(id) {
            case DIALOG_SELECT_SERVICES:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(true)
                        .setTitle(R.string.alert_dialog_time_services_title);
                builder.setMultiChoiceItems(servicesList, checkBoxes,
                        new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                            final int which, boolean isChecked) {
                        checkBoxes[which] = isChecked;
                    }
                });

                builder.setPositiveButton(R.string.close,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                            final int id) {
                        dialog.dismiss();
                    }
                });

                AlertDialog d = builder.create();
                d.setOnDismissListener(new Dialog.OnDismissListener() {
                    @Override
                    public void onDismiss(final DialogInterface dialog) {
                        populateServicesText();
                    }
                });

                return d;
            case DIALOG_LIMITATIONS:
                AlertDialog.Builder ad = new AlertDialog.Builder(this);
                ad.setTitle(R.string.alert_dialog_time_limitations_title)
                        .setCancelable(true)
                        .setView(getLayoutInflater()
                                .inflate(R.layout.addtimealert_dialog, null))
                        .setNegativeButton(R.string.close,
                                new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int which) {
                                dialog.dismiss();
                            }
                        })
                        .setInverseBackgroundForced(true);
                
                return ad.create();
            default:
                return null;
        }
    }
    
    /**
     * Populate the services list text and set the state of the okay button.
     * The okay button should only be enabled if there are services selected.
     */
    private void populateServicesText() {
        final StringBuilder sb = new StringBuilder();

        int len = servicesList.length;
        for(int i = 0; i < len; i++) {
            if(checkBoxes[i]) {
                if(sb.length() > 0) sb.append(", ");

                sb.append(servicesList[i]);
            }
        }

        if(sb.length() == 0) {
            // If the services list is empty, put the default text in the view
            // and disable the okay button.
            txtServices.setText(getString(R.string
                    .alert_dialog_time_noservices));
            btnOkay.setEnabled(false);
        } else {
            // If the services list is not empty, put the services list in the
            // view and enable the okay button.
            txtServices.setText(sb.toString());
            btnOkay.setEnabled(true);
        }
    }
}