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
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.SystemClock;
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
        double latitude = 0;
        double longitude = 0;
        if(g != null) {
            latitude = (double)(g.getLatitudeE6() / 1E6);
            longitude = (double)(g.getLongitudeE6() / 1E6);
        }
        
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