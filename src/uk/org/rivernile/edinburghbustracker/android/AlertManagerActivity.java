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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import uk.org.rivernile.edinburghbustracker.android.alerts.AlertManager;

public class AlertManagerActivity extends ListActivity
        implements View.OnClickListener {
    
    private final static int DIALOG_DEL_PROX = 0;
    private final static int DIALOG_DEL_TIME = 1;
    private final static int DIALOG_DEL_ALL = 2;
    
    private SettingsDatabase sd;
    private AlertManager alertMan;
    private LinearLayout topBar;
    private Button btnRemoveAll;
    private Cursor c;
    private AlertCursorAdapter ad;
    
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alertmanager);
        setTitle(R.string.alertmanager_title);
        
        sd = SettingsDatabase.getInstance(this);
        alertMan = AlertManager.getInstance(getApplicationContext());
        
        topBar = (LinearLayout)findViewById(R.id.alertManTopBar);
        btnRemoveAll = (Button)findViewById(R.id.btnAlertRemoveAll);
        
        btnRemoveAll.setOnClickListener(this);
        
        c = sd.getAllAlerts();
        startManagingCursor(c);
        ad = new AlertCursorAdapter();
        setListAdapter(ad);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        updateCursor();
    }
    
    @Override
    protected Dialog onCreateDialog(final int id) {
        AlertDialog alertDialog;
        switch(id) {
            case DIALOG_DEL_PROX:
                alertDialog = alertMan.getConfirmDeleteProxAlertDialog(this);
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,
                        getString(R.string.okay),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int id) {
                                alertMan.removeProximityAlert();
                                updateCursor();
                            }
                });
                return alertDialog;
            case DIALOG_DEL_TIME:
                alertDialog = alertMan.getConfirmDeleteTimeAlertDialog(this);
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,
                        getString(R.string.okay),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int id) {
                                alertMan.removeTimeAlert();
                                updateCursor();
                            }
                });
                return alertDialog;
            case DIALOG_DEL_ALL:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(true)
                    .setTitle(R.string.alert_all_rem_confirm)
                    .setPositiveButton(R.string.okay,
                    new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                            final int id)
                    {
                        alertMan.removeTimeAlert();
                        alertMan.removeProximityAlert();
                        updateCursor();
                    }
                }).setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                     public void onClick(final DialogInterface dialog,
                             final int id)
                     {
                        dialog.dismiss();
                     }
                });
                return builder.create();
            default:
                return null;
        }
    }
    
    @Override
    public void onClick(final View v) {
        if(v == btnRemoveAll) {
            showDialog(DIALOG_DEL_ALL);
        }
    }
    
    private void updateCursor() {
        stopManagingCursor(c);
        c = sd.getAllAlerts();
        startManagingCursor(c);
        ad.changeCursor(c);
        if(c.getCount() > 0) {
            topBar.setVisibility(View.VISIBLE);
        } else {
            topBar.setVisibility(View.GONE);
        }
    }
    
    private class AlertCursorAdapter extends CursorAdapter {
        
        private BusStopDatabase bsd;
        private LayoutInflater vi;
        
        public AlertCursorAdapter() {
            super(AlertManagerActivity.this, c, false);
            
            bsd = BusStopDatabase.getInstance(getApplicationContext());
            vi = LayoutInflater.from(AlertManagerActivity.this);
        }
        
        @Override
        public View getView(final int position, final View convertView,
                final ViewGroup parent) {
            super.getView(position, convertView, parent);
            
            Cursor cursor = getCursor();
            int type = cursor.getInt(1);
            View v;
            TextView txt;
            Button b;
            CharSequence str;
            String stopCode = cursor.getString(3);
            String locality = bsd.getLocalityForStopCode(stopCode);
            String busStop;
            if(locality == null) {
                busStop = bsd.getNameForBusStop(stopCode) + " (" + stopCode +
                        ")";
            } else {
                busStop = bsd.getNameForBusStop(stopCode) + ", " + locality +
                        " (" + stopCode + ")";
            }
            
            switch(type) {
                case SettingsDatabase.ALERTS_TYPE_PROXIMITY:
                    v = vi.inflate(R.layout.alertmanager_list_proximity, parent,
                            false);
                    b = (Button)v.findViewById(R.id.btnRemoveProxAlert);
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            showDialog(DIALOG_DEL_PROX);
                        }
                    });
                    
                    txt = (TextView)v.findViewById(R.id.txtAlertManProx);
                    str = getString(R.string.alertmanager_prox_text)
                            .replace("%d", String.valueOf(cursor.getInt(4)))
                            .replace("%stop", busStop);
                    txt.setText(str);
                    return v;
                case SettingsDatabase.ALERTS_TYPE_TIME:
                    v = vi.inflate(R.layout.alertmanager_list_time, parent,
                            false);
                    b = (Button)v.findViewById(R.id.btnRemoveTimeAlert);
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            showDialog(DIALOG_DEL_TIME);
                        }
                    });
                    
                    txt = (TextView)v.findViewById(R.id.txtAlertManTime);
                    int timeTrigger = cursor.getInt(6);
                    String[] services = cursor.getString(5).split(",");
                    StringBuilder sb = new StringBuilder();
                    
                    for(String service : services) {
                        if(sb.length() > 0) sb.append(", ");
                        
                        sb.append(service);
                    }
                    
                    if(timeTrigger > 1) {
                        str = getString(R.string.alertmanager_time_text_plural)
                                .replace("%busStop", busStop)
                                .replace("%services", sb.toString())
                                .replace("%minutes", String.valueOf(
                                        timeTrigger));
                    } else {
                        str = getString(
                                R.string.alertmanager_time_text_singular)
                                .replace("%busStop", busStop)
                                .replace("%services", sb.toString());
                    }
                    txt.setText(str);
                    return v;
                default:
                    return null;
            }
        }
        
        @Override
        public void bindView(final View view, final Context context,
                final Cursor cursor) {
            // Do nothing
        }
        
        @Override
        public View newView(final Context context, final Cursor cursor,
                final ViewGroup parent) {
            return null;
        }
    }
}