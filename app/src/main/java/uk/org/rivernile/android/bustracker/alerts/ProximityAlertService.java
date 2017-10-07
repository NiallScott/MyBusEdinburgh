/*
 * Copyright (C) 2016 - 2017 Niall 'Rivernile' Scott
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

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import uk.org.rivernile.android.bustracker.BusApplication;
import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract;
import uk.org.rivernile.android.bustracker.database.settings.SettingsDatabase;
import uk.org.rivernile.android.bustracker.preferences.PreferenceManager;
import uk.org.rivernile.android.bustracker.ui.bustimes.DisplayStopDataActivity;
import uk.org.rivernile.android.utils.MapsUtils;
import uk.org.rivernile.edinburghbustracker.android.BusStopMapActivity;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link android.app.Service} is started when the device has entered the proximity of a
 * specified bus stop. The job of this service is to do some tidy up of the alert and then to
 * display a notification to the user that they have entered the bus stop radius.
 *
 * @author Niall Scott
 */
public class ProximityAlertService extends IntentService {

    /** Argument for the stop code. */
    public static final String EXTRA_STOPCODE = "stopCode";
    /** Argument for the distance. */
    public static final String EXTRA_DISTANCE = "distance";

    private static final int ALERT_ID = 1;

    private NotificationManager notifMan;
    private PreferenceManager preferenceManager;
    private AlertManager alertMan;

    /**
     * Providing a default constructor is required for {@link IntentService}.
     */
    public ProximityAlertService() {
        super(ProximityAlertService.class.getName());
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notifMan = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        final BusApplication app = (BusApplication) getApplication();
        preferenceManager = app.getPreferenceManager();
        alertMan = app.getAlertManager();
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        final String stopCode = intent.getStringExtra(EXTRA_STOPCODE);

        if (!SettingsDatabase.isActiveProximityAlert(this, stopCode)) {
            return;
        }

        alertMan.removeProximityAlert();
        String stopName = getNameForBusStop(stopCode);

        if (TextUtils.isEmpty(stopName)) {
            stopName = stopCode;
        }

        showNotification(stopCode, stopName, intent.getIntExtra(EXTRA_DISTANCE, 0));
    }

    /**
     * Get the name of the bus stop.
     *
     * @param stopCode The stop code to get the name for.
     * @return The name of the bus stop, or {@code null} if it was not found.
     */
    @Nullable
    private String getNameForBusStop(@NonNull final String stopCode) {
        final Cursor c = getContentResolver().query(BusStopContract.BusStops.CONTENT_URI,
                new String[] { BusStopContract.BusStops.STOP_NAME },
                BusStopContract.BusStops.STOP_CODE + " = ?",
                new String[] { stopCode }, null);

        final String result;

        if (c != null) {
            c.moveToFirst();
            result = c.getString(c.getColumnIndex(BusStopContract.BusStops.STOP_NAME));
            c.close();
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Show the notification to the user.
     *
     * @param stopCode The stop code the notification relates to.
     * @param stopName The name of the bus stop.
     * @param distance The distance the user set to be alerted within.
     */
    private void showNotification(final String stopCode, final String stopName,
            final int distance) {
        final String title = getString(R.string.proxreceiver_notification_title, stopName);
        final String summary = getString(R.string.proxreceiver_notification_summary, distance,
                stopName);
        final String ticker = getString(R.string.proxreceiver_notification_ticker, stopName);

        // Create the notification.
        final NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this);
        notifBuilder.setAutoCancel(true);
        notifBuilder.setSmallIcon(R.drawable.ic_status_bus);
        notifBuilder.setTicker(ticker);
        notifBuilder.setContentTitle(title);
        notifBuilder.setContentText(summary);
        // Support for Jelly Bean notifications.
        notifBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(summary));

        final Intent launchIntent;

        if (MapsUtils.isGoogleMapsAvailable(this)) {
            // The Intent which launches the bus stop map at the selected stop.
            launchIntent = new Intent(this, BusStopMapActivity.class);
            launchIntent.putExtra(BusStopMapActivity.ARG_STOPCODE, stopCode);
        } else {
            launchIntent = new Intent(this, DisplayStopDataActivity.class);
            launchIntent.putExtra(DisplayStopDataActivity.EXTRA_STOP_CODE, stopCode);
        }

        notifBuilder.setContentIntent(
                PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_ONE_SHOT));

        final Notification n = notifBuilder.build();

        if (preferenceManager.isNotificationWithSound()) {
            n.defaults |= Notification.DEFAULT_SOUND;
        }

        if (preferenceManager.isNotificationWithVibration()) {
            n.defaults |= Notification.DEFAULT_VIBRATE;
        }

        if (preferenceManager.isNotificationWithLed()) {
            n.defaults |= Notification.DEFAULT_LIGHTS;
            n.flags |= Notification.FLAG_SHOW_LIGHTS;
        }

        // Send the notification to the UI.
        notifMan.notify(ALERT_ID, n);
    }
}
