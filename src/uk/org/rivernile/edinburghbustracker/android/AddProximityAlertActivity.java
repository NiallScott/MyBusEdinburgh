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
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.List;
import uk.org.rivernile.edinburghbustracker.android.alerts.AlertManager;

public class AddProximityAlertActivity extends Activity {
    
    private static final byte DIALOG_LIMITATIONS = 1;
    
    private int meters = 100;
    private CheckBox check;
    private AlertManager alertMan;
    private LocationManager locMan;
    private BusStopDatabase bsd;
    private String stopCode;
    private Intent locationSettingsIntent;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        if(!intent.hasExtra("stopCode")) {
            finish();
            return;
        }
        
        stopCode = intent.getStringExtra("stopCode");
        
        locationSettingsIntent = new Intent(
                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        locationSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        setTitle(R.string.alert_dialog_prox_title);
        setContentView(R.layout.addproxalert);
        
        alertMan = AlertManager.getInstance(this);
        locMan = (LocationManager)getSystemService(LOCATION_SERVICE);
        bsd = BusStopDatabase.getInstance(this);
        
        check = (CheckBox)findViewById(R.id.checkProxGPS);
        final Spinner spinner = (Spinner)findViewById(R.id
                .prox_distance_select);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(this,
                    R.array.alert_dialog_prox_distance_array,
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
        
        Button btn = (Button)findViewById(R.id.btnOkay);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                alertMan.addProximityAlert(stopCode, meters);
                if(check.isChecked()) {
                    try {
                        startActivity(locationSettingsIntent);
                    } catch(ActivityNotFoundException e) { }
                }
                finish();
            }
        });
        
        btn = (Button)findViewById(R.id.btnCancel);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                finish();
            }
        });
        
        btn = (Button)findViewById(R.id.btnLimitations);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                showDialog(DIALOG_LIMITATIONS);
            }
        });
        
        String locality = bsd.getLocalityForStopCode(stopCode);
        String stopNameCode;
        if(locality == null) {
            stopNameCode = bsd.getNameForBusStop(stopCode) + " (" + stopCode +
                    ")";
        } else {
            stopNameCode = bsd.getNameForBusStop(stopCode) + ", " + locality +
                    " (" + stopCode + ")";
        }
        
        final TextView second = (TextView)findViewById(R.id.textProxDialogStop);
        second.setText(getString(R.string.alert_dialog_prox_second)
                .replace("%s", stopNameCode));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        
        List<ResolveInfo> packages = getPackageManager()
                .queryIntentActivities(locationSettingsIntent, 0);
        if(packages != null && !packages.isEmpty() &&
                !locMan.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            check.setVisibility(View.VISIBLE);
        } else {
            check.setVisibility(View.GONE);
        }
    }
    
    @Override
    public Dialog onCreateDialog(final int id) {
        switch(id) {
            case DIALOG_LIMITATIONS:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.alert_dialog_prox_limitations_title)
                        .setCancelable(true)
                        .setView(getLayoutInflater()
                                .inflate(R.layout.addproxalert_dialog, null))
                        .setNegativeButton(R.string.close,
                                new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int which) {
                                dialog.dismiss();
                            }
                        })
                        .setInverseBackgroundForced(true);
                
                return builder.create();
            default:
                return null;
        }
    }
}