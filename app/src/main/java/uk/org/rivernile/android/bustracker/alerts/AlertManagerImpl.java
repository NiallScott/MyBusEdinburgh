/*
 * Copyright (C) 2011 - 2020 Niall 'Rivernile' Scott
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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import uk.org.rivernile.android.bustracker.core.alerts.arrivals.ArrivalAlertRunnerService;
import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract;
import uk.org.rivernile.android.bustracker.database.settings.loaders.AddProximityAlertTask;
import uk.org.rivernile.android.bustracker.database.settings.loaders.AddTimeAlertTask;
import uk.org.rivernile.android.bustracker.database.settings.loaders.DeleteAllProximityAlertsTask;
import uk.org.rivernile.android.bustracker.database.settings.loaders.DeleteAllTimeAlertsTask;

/**
 * This is a concrete implementation of {@link AlertManager}.
 * 
 * @author Niall Scott
 */
@Singleton
public class AlertManagerImpl implements AlertManager {
    
    private final Context context;
    private final LocationManager locMan;
    
    /**
     * Create a new instance of {@link AlertManagerImpl}.
     * 
     * @param context The {@link android.app.Application} {@link Context}.
     * @param locationManager The Android {@link LocationManager}.
     */
    @Inject
    AlertManagerImpl(
            @NonNull final Context context,
            @NonNull final LocationManager locationManager) {
        this.context = context;
        locMan = locationManager;
    }

    @Override
    public void addProximityAlert(@NonNull final String stopCode,
            @IntRange(from = 1) final int distance) {
        // Remove any other existing proximity alerts.
        removeProximityAlert();
        // This is executed in an AsyncTask because it needs to fetch the bus stop coordinates from
        // the database first.
        new ProximityAlertTask(stopCode, distance).execute();
    }

    @Override
    public void removeProximityAlert() {
        // Remove the alert from the database.
        DeleteAllProximityAlertsTask.start(context);
        final Intent intent = new Intent(context, ProximityAlertService.class);
        final PendingIntent pi = PendingIntent.getService(context, 0, intent,
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

        // Add a new time alert to the database.
        AddTimeAlertTask.start(context, stopCode, services, timeTrigger);
        final Intent intent = new Intent(context, ArrivalAlertRunnerService.class);

        // TODO: this style shouldn't make it to the re-implementation of this class.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }
    
    @Override
    public void removeTimeAlert() {
        // Remove all time alerts from the database.
        DeleteAllTimeAlertsTask.start(context);
    }

    /**
     * This {@link AsyncTask} adds a new proximity alert. This exists inside an {@link AsyncTask} as
     * it needs to fetch the latitude and longitude of the bus stop from the database before it can
     * add the alert. Database access should be done on a background thread so this class is used to
     * facilitate this.
     */
    private class ProximityAlertTask extends AsyncTask<Void, Void, Cursor> {

        private final String stopCode;
        private final int distance;

        /**
         * Create a new {@code ProximityAlertTask}.
         *
         * @param stopCode See {@link #addProximityAlert(String, int)}.
         * @param distance See {@link #addProximityAlert(String, int)}.
         */
        ProximityAlertTask(@NonNull final String stopCode, @IntRange(from = 1) final int distance) {
            this.stopCode = stopCode;
            this.distance = distance;
        }

        @Override
        protected Cursor doInBackground(final Void... params) {
            return context.getContentResolver().query(BusStopContract.BusStops.CONTENT_URI,
                    new String[] {
                            BusStopContract.BusStops.LATITUDE,
                            BusStopContract.BusStops.LONGITUDE
                    },
                    BusStopContract.BusStops.STOP_CODE + " = ?", new String[] { stopCode }, null);
        }

        @Override
        protected void onPostExecute(final Cursor cursor) {
            final double latitude;
            final double longitude;

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    latitude = cursor.getDouble(cursor.getColumnIndex(
                            BusStopContract.BusStops.LATITUDE));
                    longitude = cursor.getDouble(cursor.getColumnIndex(
                            BusStopContract.BusStops.LONGITUDE));
                } else {
                    latitude = longitude = 0f;
                }

                cursor.close();
            } else {
                latitude = longitude = 0f;
            }

            // The intent to send to the BroadcastReceiver when the distance criteria has been met.
            final Intent intent = new Intent(context, ProximityAlertService.class);
            intent.putExtra(ProximityAlertService.EXTRA_STOPCODE, stopCode);
            intent.putExtra(ProximityAlertService.EXTRA_DISTANCE, distance);

            final PendingIntent pi = PendingIntent.getService(context, 0, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            // Make sure the LocationManager is not looking out for any other locations for the
            // alerts.
            locMan.removeProximityAlert(pi);
            // Add the new alert to the database.
            AddProximityAlertTask.start(context, stopCode, distance);
            // Ask LocationManager to look out for the given location.
            locMan.addProximityAlert(latitude, longitude, (float) distance,
                    System.currentTimeMillis() + 3600000, pi);
        }
    }
}