/*
 * Copyright (C) 2011 - 2013 Niall 'Rivernile' Scott
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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.support.v4.app.NotificationCompat;
import uk.org.rivernile.android.utils.GenericUtils;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;
import uk.org.rivernile.edinburghbustracker.android.BusStopDetailsActivity;
import uk.org.rivernile.edinburghbustracker.android.BusStopMapActivity;
import uk.org.rivernile.edinburghbustracker.android.PreferencesActivity;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.edinburghbustracker.android.SettingsDatabase;

/**
 * The ProximityAlertReceiver is only called when a previously set proximity
 * alert meets its criteria. This is handled by the Android platform in
 * LocationManager. This BroadcastReceiver assumes all it has to do is manage
 * the alert and send the user notification.
 * 
 * @author Niall Scott
 */
public class ProximityAlertReceiver extends BroadcastReceiver {
    
    /** Argument for the stopCode. */
    public static final String ARG_STOPCODE = "stopCode";
    /** Argument for the distance. */
    public static final String ARG_DISTANCE = "distance";
    
    private static final int ALERT_ID = 1;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {  
        final SettingsDatabase db = SettingsDatabase.getInstance(context);
        final String stopCode = intent.getStringExtra(ARG_STOPCODE);
        // Make sure the alert is still active to remain relevant.
        if(!db.isActiveProximityAlert(stopCode)) return;
        
        final String stopName = BusStopDatabase.getInstance(context)
                .getNameForBusStop(stopCode);
        
        final NotificationManager notMan = (NotificationManager)context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        final LocationManager locMan = (LocationManager)context
                .getSystemService(Context.LOCATION_SERVICE);
        
        // Delete the alert from the database.
        db.deleteAllAlertsOfType(SettingsDatabase.ALERTS_TYPE_PROXIMITY);
        
        // Make sure the LocationManager no longer checks for this proximity.
        final PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent,
                0);
        locMan.removeProximityAlert(pi);
        
        final String title = context.getString(
                R.string.proxreceiver_notification_title, stopName);
        final String summary = context.getString(
                R.string.proxreceiver_notification_summary,
                intent.getIntExtra(ARG_DISTANCE, 0), stopName);
        final String ticker = context.getString(
                R.string.proxreceiver_notification_ticker, stopName);
        
        final SharedPreferences sp = context
                .getSharedPreferences(PreferencesActivity.PREF_FILE, 0);
        
        // Create the notification.
        final NotificationCompat.Builder notifBuilder =
                new NotificationCompat.Builder(context);
        notifBuilder.setAutoCancel(true);
        notifBuilder.setSmallIcon(R.drawable.ic_status_bus);
        notifBuilder.setTicker(ticker);
        notifBuilder.setContentTitle(title);
        notifBuilder.setContentText(summary);
        // Support for Jelly Bean notifications.
        notifBuilder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(summary));
        
        final Intent launchIntent;
        if (GenericUtils.isGoogleMapsAvailable(context)) {
            // The Intent which launches the bus stop map at the selected stop.
            launchIntent = new Intent(context, BusStopMapActivity.class);
            launchIntent.putExtra(BusStopMapActivity.ARG_STOPCODE, stopCode);
        } else {
            launchIntent = new Intent(context, BusStopDetailsActivity.class);
            launchIntent.putExtra(BusStopDetailsActivity.ARG_STOPCODE,
                    stopCode);
        }
        
        notifBuilder.setContentIntent(
                    PendingIntent.getActivity(context, 0, launchIntent,
                        PendingIntent.FLAG_ONE_SHOT));
        
        final Notification n = notifBuilder.build();
        if(sp.getBoolean(PreferencesActivity.PREF_ALERT_SOUND, true))
            n.defaults |= Notification.DEFAULT_SOUND;
        
        if(sp.getBoolean(PreferencesActivity.PREF_ALERT_VIBRATE, true))
            n.defaults |= Notification.DEFAULT_VIBRATE;
        
        if(sp.getBoolean(PreferencesActivity.PREF_ALERT_LED, true)) {
            n.defaults |= Notification.DEFAULT_LIGHTS;
            n.flags |= Notification.FLAG_SHOW_LIGHTS;
        }
        
        // Send the notification to the UI.
        notMan.notify(ALERT_ID, n);
    }
}