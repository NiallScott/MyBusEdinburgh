/*
 * Copyright (C) 2001 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android.alerts;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.android.maps.GeoPoint;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.edinburghbustracker.android.SettingsDatabase;

public class AlertManager {
    
    private static AlertManager instance;
    
    private Context context;
    private LocationManager locMan;
    private AlarmManager alMan;
    private BusStopDatabase bsd;
    private SettingsDatabase sd;
    private int meters = 100;
    private int timeTrigger = 0;
    private String currentStop;
    private String[] servicesList;
    private boolean[] checkBoxes;
    
    private AlertManager(final Context context) {
        locMan = (LocationManager)context.getSystemService(
                Context.LOCATION_SERVICE);
        alMan = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        bsd = BusStopDatabase.getInstance(context);
        sd = SettingsDatabase.getInstance(context);
    }
    
    public static AlertManager getInstance(final Context context) {
        if(context == null)
            throw new IllegalArgumentException("The context should not be " +
                    "null.");
        if(instance == null) instance = new AlertManager(context);
        instance.context = context;
        return instance;
    }
    
    public void addProximityAlert(final String stopCode, final int distance) {
        if(stopCode == null || stopCode.length() == 0)
            throw new IllegalArgumentException("The stopCode cannot be null " +
                    "or blank.");
        
        removeProximityAlert();
        GeoPoint g = bsd.getGeoPointForStopCode(stopCode);
        double latitude = (double)(g.getLatitudeE6() / 1E6);
        double longitude = (double)(g.getLongitudeE6() / 1E6);
        
        Intent intent = new Intent(context, ProximityAlertReceiver.class);
        intent.putExtra("stopCode", stopCode);
        intent.putExtra("distance", distance);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        locMan.removeProximityAlert(pi);
        sd.insertNewProximityAlert(stopCode, distance);
        locMan.addProximityAlert(latitude, longitude, (float)distance,
                System.currentTimeMillis() + 3600000, pi);
    }
    
    public void removeProximityAlert() {
        sd.deleteAllAlertsOfType(SettingsDatabase.ALERTS_TYPE_PROXIMITY);
        Intent intent = new Intent(context, ProximityAlertReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        locMan.removeProximityAlert(pi);
        pi.cancel();
    }
    
    public void addTimeAlert(final String stopCode, final String[] services,
            final int timeTrigger) {
        if(stopCode == null || stopCode.length() == 0)
            throw new IllegalArgumentException("The stopCode cannot be null " +
                    "or blank.");
        if(services == null || services.length == 0)
            throw new IllegalArgumentException("The services list cannot be " +
                    "null or empty.");
        if(timeTrigger < 0)
            throw new IllegalArgumentException("The timeTrigger cannot be " +
                    "less than 0.");
        
        removeTimeAlert();
        
        Intent intent = new Intent(context, TimeAlertService.class);
        intent.putExtra("stopCode", stopCode);
        intent.putExtra("services", services);
        intent.putExtra("timeTrigger", timeTrigger);
        intent.putExtra("timeSet", SystemClock.elapsedRealtime());
        
        PendingIntent pi = PendingIntent.getService(context, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        alMan.cancel(pi);
        sd.insertNewTimeAlert(stopCode, services, timeTrigger);
        alMan.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 60000, pi);
    }
    
    public void removeTimeAlert() {
        sd.deleteAllAlertsOfType(SettingsDatabase.ALERTS_TYPE_TIME);
        Intent intent = new Intent(context, TimeAlertService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        alMan.cancel(pi);
        pi.cancel();
    }
    
    public AlertDialog getAddProxAlertDialog(final Context context) {
        LayoutInflater vi = (LayoutInflater)context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.proxalertdialog, null);
        
        final Spinner spinner = (Spinner)v.findViewById(
                R.id.prox_distance_select);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(context,
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
        
        final CheckBox check = (CheckBox)v.findViewById(R.id.checkProxGPS);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true).setInverseBackgroundForced(true)
                .setTitle(R.string.alert_dialog_prox_title)
                .setView(v).setPositiveButton(R.string.okay,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog,
                                final int id) {
                            addProximityAlert(currentStop, meters);
                            
                            if(check.isChecked()) {
                                Intent intent = new Intent(
                                        Settings.ACTION_SECURITY_SETTINGS);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            }
                            
                            check.setChecked(false);
                        }
                    })
                .setNegativeButton(R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog,
                                final int id) {
                            dialog.dismiss();
                        }
        });
        
        return builder.create();
    }
    
    public void editAddProxAlertDialog(final String stopCode,
            final AlertDialog dialog) {
        if(stopCode == null)
            throw new IllegalArgumentException("stopCode must not be null");
        if(dialog == null)
            throw new IllegalArgumentException("dialog must not be null");
        
        currentStop = stopCode;
        
        String stopNameCode = bsd.getNameForBusStop(stopCode) + " (" +
                stopCode + ")";
        final TextView second = (TextView)dialog.findViewById(
                R.id.textProxDialogStop);
        second.setText(context.getString(R.string.alert_dialog_prox_second)
                .replace("%s", stopNameCode));
        
        final CheckBox check = (CheckBox)dialog.findViewById(R.id.checkProxGPS);
        
        if(locMan.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            check.setVisibility(View.GONE);
        } else {
            check.setVisibility(View.VISIBLE);
        }
    }
    
    public AlertDialog getConfirmDeleteProxAlertDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true)
            .setTitle(R.string.alert_prox_rem_confirm)
            .setPositiveButton(R.string.okay,
            new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog,
                    final int id)
            {
                removeProximityAlert();
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
    }
    
    public AlertDialog getAddTimeAlertDialog(final Context context) {
        LayoutInflater vi = (LayoutInflater)context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.timealertdialog, null);
        
        final Spinner spinner = (Spinner)v.findViewById(
                R.id.time_time_select);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(context,
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
                        timeTrigger = 0;
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
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true).setInverseBackgroundForced(true)
                .setTitle(R.string.alert_dialog_time_title)
                .setView(v).setPositiveButton(R.string.okay,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog,
                                final int id) {
                            int count = 0;
                            int len = checkBoxes.length;
                            
                            for(boolean b : checkBoxes) {
                                if(b) count++;
                            }
                            
                            String[] services = new String[count];
                            int j = 0;
                            
                            for(int i = 0; i < len; i++) {
                                if(checkBoxes[i]) {
                                    services[j] = servicesList[i];
                                    j++;
                                }
                            }
                            
                            addTimeAlert(currentStop, services,
                                    timeTrigger);
                        }
                    })
                .setNegativeButton(R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog,
                                final int id) {
                            dialog.dismiss();
                        }
        });
        
        final AlertDialog d = builder.create();
        
        final Button b = (Button)v.findViewById(R.id.btnAlertTimeServices);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                getServiceChoiceDialog(context, d).show();
            }
        });
        
        return d;
    }
    
    public void editAddTimeAlertDialog(final String stopCode,
            final AlertDialog dialog, final String defaultService) {
        if(stopCode == null)
            throw new IllegalArgumentException("stopCode must not be null");
        if(dialog == null)
            throw new IllegalArgumentException("dialog must not be null");
        
        currentStop = stopCode;
        
        servicesList = bsd.getBusServicesForStop(stopCode);
        int len = servicesList.length;
        checkBoxes = new boolean[len];
        
        String stopNameCode = bsd.getNameForBusStop(stopCode) + " (" +
                stopCode + ")";
        TextView tv = (TextView)dialog.findViewById(R.id.txtTimeDialogStop);
        tv.setText(context.getString(R.string.alert_dialog_time_busstop)
                .replace("%s", stopNameCode));
        
        if(defaultService != null && defaultService.length() > 0) {
            for(int i = 0; i < len; i++) {
                if(servicesList[i].equals(defaultService)) {
                    checkBoxes[i] = true;
                    break;
                }
            }
        } else {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }
    }
    
    private AlertDialog getServiceChoiceDialog(final Context context,
            final AlertDialog timeDialog) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true)
                .setTitle(R.string.alert_dialog_time_services_title);
        builder.setMultiChoiceItems(servicesList, checkBoxes,
                new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which,
                    boolean isChecked) {
                checkBoxes[which] = isChecked;
            }
        });
        
        builder.setPositiveButton(R.string.close,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.dismiss();
            }
        });
        
        AlertDialog d = builder.create();
        d.setOnDismissListener(new Dialog.OnDismissListener() {
            @Override
            public void onDismiss(final DialogInterface dialog) {
                StringBuilder sb = new StringBuilder();
                
                int len = servicesList.length;
                for(int i = 0; i < len; i++) {
                    if(checkBoxes[i]) {
                        if(sb.length() > 0) sb.append(", ");
                        
                        sb.append(servicesList[i]);
                    }
                }
                
                TextView tv = (TextView)timeDialog.findViewById(
                        R.id.txtTimeAlertServices);
                if(sb.length() == 0) {
                    tv.setText(context.getString(R.string
                            .alert_dialog_time_noservices));
                    timeDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setEnabled(false);
                } else {
                    tv.setText(sb.toString());
                    timeDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setEnabled(true);
                }
            }
        });
        
        return d;
    }
    
    public AlertDialog getConfirmDeleteTimeAlertDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true)
            .setTitle(R.string.alert_time_rem_confirm)
            .setPositiveButton(R.string.okay,
            new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog,
                    final int id)
            {
                removeTimeAlert();
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
    }
}