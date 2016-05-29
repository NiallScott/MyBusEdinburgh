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

package uk.org.rivernile.android.bustracker.alerts;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.SystemClock;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import uk.org.rivernile.android.bustracker.database.settings.loaders.AddProximityAlertTask;
import uk.org.rivernile.android.bustracker.database.settings.loaders.AddTimeAlertTask;
import uk.org.rivernile.android.bustracker.database.settings.loaders.DeleteAllProximityAlertsTask;
import uk.org.rivernile.android.bustracker.database.settings.loaders.DeleteAllTimeAlertsTask;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;

/**
 * This is a concrete implementation of {@link AlertManager}.
 * 
 * @author Niall Scott
 */
public class AlertManagerImpl implements AlertManager {
    
    private final Context context;
    private final LocationManager locMan;
    private final AlarmManager alMan;
    private final BusStopDatabase bsd;
    
    /**
     * Create a new instance of {@link AlertManagerImpl}.
     * 
     * @param context The {@link android.app.Application} {@link Context}.
     */
    public AlertManagerImpl(@NonNull final Context context) {
        this.context = context;
        locMan = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        alMan = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        bsd = BusStopDatabase.getInstance(context.getApplicationContext());
    }

    @Override
    public void addProximityAlert(@NonNull final String stopCode,
            @IntRange(from = 1) final int distance) {
        // Remove any other existing proximity alerts.
        removeProximityAlert();
        // Get the coordinates of the bus stop.
        final LatLng point = bsd.getLatLngForStopCode(stopCode);
        final double latitude;
        final double longitude;

        if (point != null) {
            latitude = point.latitude;
            longitude = point.longitude;
        } else {
            latitude = longitude = 0;
        }
        
        // The intent to send to the BroadcastReceiver when the distance criteria has been met.
        final Intent intent = new Intent(context, ProximityAlertReceiver.class);
        intent.putExtra(ProximityAlertReceiver.ARG_STOPCODE, stopCode);
        intent.putExtra(ProximityAlertReceiver.ARG_DISTANCE, distance);
        
        final PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        // Make sure the LocationManager is not looking out for any other locations for the alerts.
        locMan.removeProximityAlert(pi);
        // Add the new alert to the database.
        AddProximityAlertTask.start(context, stopCode, distance);
        // Ask LocationManager to look out for the given location.
        locMan.addProximityAlert(latitude, longitude, (float) distance,
                System.currentTimeMillis() + 3600000, pi);
    }

    @Override
    public void removeProximityAlert() {
        // Remove the alert from the database.
        DeleteAllProximityAlertsTask.start(context);
        final Intent intent = new Intent(context, ProximityAlertReceiver.class);
        final PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        // Remove the proximity alert from LocationManager.
        locMan.removeProximityAlert(pi);
        // Make sure the PendingIntent does not remain active.
        pi.cancel();
    }

    @Override
    public void addTimeAlert(@NonNull final String stopCode, @NonNull final String[] services,
            @IntRange(from = 0) final int timeTrigger) {
        // Make sure any other time alerts do not exist.
        removeTimeAlert();
        
        // The intent to send to the service which monitors the bus times.
        final Intent intent = new Intent(context, TimeAlertService.class);
        intent.putExtra(TimeAlertService.ARG_STOPCODE, stopCode);
        intent.putExtra(TimeAlertService.ARG_SERVICES, services);
        intent.putExtra(TimeAlertService.ARG_TIME_TRIGGER, timeTrigger);
        intent.putExtra(TimeAlertService.ARG_TIME_SET, SystemClock.elapsedRealtime());
        
        final PendingIntent pi = PendingIntent.getService(context, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        // Make sure existing alarms are cancelled.
        alMan.cancel(pi);
        // Add a new time alert to the database.
        AddTimeAlertTask.start(context, stopCode, services, timeTrigger);
        // Set the alarm.
        alMan.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 60000, pi);
    }
    
    @Override
    public void removeTimeAlert() {
        // Remove all time alerts from the database.
        DeleteAllTimeAlertsTask.start(context);
        final Intent intent = new Intent(context, TimeAlertService.class);
        final PendingIntent pi = PendingIntent.getService(context, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        // Cancel any pending checks with the AlarmManager.
        alMan.cancel(pi);
        // Make sure the PendingIntent is cancelled and invalid too.
        pi.cancel();
    }
}