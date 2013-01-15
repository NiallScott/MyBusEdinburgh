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

package uk.org.rivernile.edinburghbustracker.android.alerts;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.SystemClock;
import com.google.android.gms.maps.model.LatLng;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.edinburghbustracker.android.SettingsDatabase;

/**
 * The AlertManager takes care of handling the addition and removal of proximity
 * and time based alerts.
 * 
 * @author Niall Scott
 */
public class AlertManager {
    
    private static AlertManager instance;
    
    private Context context;
    private LocationManager locMan;
    private AlarmManager alMan;
    private BusStopDatabase bsd;
    private SettingsDatabase sd;
    
    /**
     * Create a new AlertManager instance.
     * 
     * @param context The Application context.
     */
    private AlertManager(final Context context) {
        locMan = (LocationManager)context.getSystemService(
                Context.LOCATION_SERVICE);
        alMan = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        bsd = BusStopDatabase.getInstance(context);
        sd = SettingsDatabase.getInstance(context);
    }
    
    /**
     * Get an instance of this class. It is a singleton.
     * 
     * @param context The Application context.
     * @return An AlertManager instance.
     */
    public static AlertManager getInstance(final Context context) {
        if(context == null)
            throw new IllegalArgumentException("The context should not be " +
                    "null.");
        if(instance == null) instance = new AlertManager(context);
        instance.context = context;
        return instance;
    }
    
    /**
     * Add a new proximity alert. The criteria is the bus stop code and the
     * maximum distance from that particular bus stop.
     * 
     * @param stopCode The bus stop to alert when in proximity of.
     * @param distance The maximum distance from the bus stop.
     * @see #removeProximityAlert() 
     */
    public void addProximityAlert(final String stopCode, final int distance) {
        if(stopCode == null || stopCode.length() == 0)
            throw new IllegalArgumentException("The stopCode cannot be null " +
                    "or blank.");
        
        // Remove any other existing proximity alerts.
        removeProximityAlert();
        // Get the coordinates of the bus stop.
        final LatLng point = bsd.getLatLngForStopCode(stopCode);
        double latitude = 0;
        double longitude = 0;
        if(point != null) {
            latitude = point.latitude;
            longitude = point.longitude;
        }
        
        // The intent to send to the BroadcastReceiver when the distance
        // criteria has been met.
        final Intent intent = new Intent(context, ProximityAlertReceiver.class);
        intent.putExtra("stopCode", stopCode);
        intent.putExtra("distance", distance);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        
        final PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        // Make sure the LocationManager is not looking out for any other
        // locations for the alerts.
        locMan.removeProximityAlert(pi);
        // Add the new alert to the database.
        sd.insertNewProximityAlert(stopCode, distance);
        // Ask LocationManager to look out for the given location.
        locMan.addProximityAlert(latitude, longitude, (float)distance,
                System.currentTimeMillis() + 3600000, pi);
    }
    
    /**
     * Remove any current proximity alerts. Only 1 can be set at a time, hence
     * why this method does not need any sort of id argument.
     * 
     * @see #addProximityAlert(java.lang.String, int) 
     */
    public void removeProximityAlert() {
        // Remove the alert from the database.
        sd.deleteAllAlertsOfType(SettingsDatabase.ALERTS_TYPE_PROXIMITY);
        final Intent intent = new Intent(context, ProximityAlertReceiver.class);
        final PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        // Remove the proximity alert from LocationManager.
        locMan.removeProximityAlert(pi);
        // Make sure the PendingIntent does not remain active.
        pi.cancel();
    }
    
    /**
     * Add a new time alert. Only a single stopCode can be monitored, but any
     * number of services can be monitored. A time trigger is specified which
     * denotes the maximum time the bus is from the stop when the alert is
     * triggered.
     * 
     * @param stopCode The bus stop code to monitor.
     * @param services A String array of bus service names to monitor for.
     * @param timeTrigger The maximum amount of time the bus should be from the
     * bus stop before an alert is triggered.
     * @see #removeTimeAlert() 
     */
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
        
        // Make sure any other time alerts do not exist.
        removeTimeAlert();
        
        // The intent to send to the service which monitors the bus times.
        final Intent intent = new Intent(context, TimeAlertService.class);
        intent.putExtra("stopCode", stopCode);
        intent.putExtra("services", services);
        intent.putExtra("timeTrigger", timeTrigger);
        intent.putExtra("timeSet", SystemClock.elapsedRealtime());
        
        final PendingIntent pi = PendingIntent.getService(context, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        // Make sure existing alarms are cancelled.
        alMan.cancel(pi);
        // Add a new time alert to the database.
        sd.insertNewTimeAlert(stopCode, services, timeTrigger);
        // Set the alarm.
        alMan.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 60000, pi);
    }
    
    /**
     * Remove any current time alerts. Only 1 can be set at a time, hence why
     * this method does not need any sort of id argument.
     * 
     * @see #addTimeAlert(java.lang.String, java.lang.String[], int) 
     */
    public void removeTimeAlert() {
        // Remove all time alerts from the database.
        sd.deleteAllAlertsOfType(SettingsDatabase.ALERTS_TYPE_TIME);
        final Intent intent = new Intent(context, TimeAlertService.class);
        final PendingIntent pi = PendingIntent.getService(context, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        // Cancel any pending checks with the AlarmManager.
        alMan.cancel(pi);
        // Make sure the PendingIntent is cancelled and invalid too.
        pi.cancel();
    }
    
    /**
     * Get an instance of the Dialog which confirms a proximity alert deletion.
     * 
     * @param context A Context object.
     * @return An AlertDialog instance.
     */
    public AlertDialog getConfirmDeleteProxAlertDialog(final Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true)
            .setTitle(R.string.alert_prox_rem_confirm)
            .setPositiveButton(R.string.okay,
            new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int id) {
                removeProximityAlert();
            }
        }).setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
             public void onClick(final DialogInterface dialog, final int id) {
                dialog.dismiss();
             }
        });
        
        return builder.create();
    }
    
    /**
     * Get an instance of the Dialog which confirms a time alert deletion.
     * 
     * @param context A Context object.
     * @return An AlertDialog instance.
     */
    public AlertDialog getConfirmDeleteTimeAlertDialog(final Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true)
            .setTitle(R.string.alert_time_rem_confirm)
            .setPositiveButton(R.string.okay,
            new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int id) {
                removeTimeAlert();
            }
        }).setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
             public void onClick(final DialogInterface dialog, final int id) {
                dialog.dismiss();
             }
        });
        
        return builder.create();
    }
}