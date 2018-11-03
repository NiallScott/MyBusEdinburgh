/*
 * Copyright (C) 2011 - 2018 Niall 'Rivernile' Scott
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
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import java.util.List;

import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract;
import uk.org.rivernile.android.bustracker.database.settings.SettingsDatabase;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveTimesException;
import uk.org.rivernile.android.bustracker.BusApplication;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBus;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusService;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusStop;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusTimes;
import uk.org.rivernile.android.bustracker.preferences.PreferenceManager;
import uk.org.rivernile.android.bustracker.ui.bustimes.DisplayStopDataActivity;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * The purpose of the {@code TimeAlertService} is to be run on a once-per-minute
 * basis to load bus times from the server to see if any of the services the
 * user has filtered on have arrived at the bus stop within the time trigger
 * time, also set by the user. If the criteria is not met then it schedules to
 * run again in the next minute. If the criteria is met, the user is greeted
 * with a notification.
 * 
 * <p>
 * As this is an {@link IntentService}, it runs in a separate thread and does
 * not block the UI thread.
 * </p>
 * 
 * @author Niall Scott
 */
public class TimeAlertService extends IntentService {
    
    /** Argument for the stopCode. */
    public static final String ARG_STOPCODE = "stopCode";
    /** Argument for the list of services to check against. */
    public static final String ARG_SERVICES = "services";
    /** Argument for the time threshold to trigger an alert. */
    public static final String ARG_TIME_TRIGGER = "timeTrigger";
    /** Argument for the time that the alert was set at. */
    public static final String ARG_TIME_SET = "timeSet";
    
    private static final int ALERT_ID = 2;
    
    private BusApplication app;
    private NotificationManager notifMan;
    private AlertManager alertMan;
    private AlarmManager alarmMan;
    private PreferenceManager preferenceManager;
    
    /**
     * Create a new instance of the {@code TimeAlertService}. This simply calls
     * its super constructor.
     */
    public TimeAlertService() {
        super(TimeAlertService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        app = (BusApplication) getApplication();
        notifMan = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        alertMan = app.getAlertManager();
        alarmMan = (AlarmManager) getSystemService(ALARM_SERVICE);
        preferenceManager = app.getPreferenceManager();
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        final String stopCode = intent.getStringExtra(ARG_STOPCODE);
        final String[] services = intent.getStringArrayExtra(ARG_SERVICES);
        final int timeTrigger = intent.getIntExtra(ARG_TIME_TRIGGER, 5);
        
        LiveBusTimes result;
        try {
            // Get the bus times. Only get 1 bus per service.
            result = app.getBusTrackerEndpoint().getBusTimes(
                    new String[] { stopCode }, 1);
        } catch(LiveTimesException e) {
            // There was an error. No point continuing. Reschedule.
            reschedule(intent);
            return;
        }
        
        // Get the bus stop we are interested in. It should be the only one in
        // the result.
        final LiveBusStop busStop = result.getBusStop(stopCode);
        if (busStop == null) {
            reschedule(intent);
            return;
        }
        
        final List<LiveBusService> busServices = busStop.getServices();
        final int servicesLen = busServices.size();
        int time;
        LiveBusService busService;
        LiveBus bus;
        List<LiveBus> buses;
        
        // Loop through all the bus services at this stop.
        for(int i = 0; i < servicesLen; i++) {
            busService = busServices.get(i);
            buses = busService.getLiveBuses();
            
            // We are only interested in the next departure. Also get the time.
            bus = !buses.isEmpty() ? buses.get(0) : null;
            if (bus == null) {
                continue;
            }
            
            time = bus.getDepartureMinutes();
            
            // Loop through all of the services we are interested in.
            for(String service : services) {
                // The service matches and meets the time criteria.
                if(service.equals(busService.getServiceName()) &&
                        time <= timeTrigger) {
                    // The alert may have been cancelled by the user recently,
                    // check it's still active to stay relevant. Cancel the
                    // alert if we're continuing.
                    if (!SettingsDatabase.isActiveTimeAlert(this, stopCode)) {
                        return;
                    }
                    
                    alertMan.removeTimeAlert();
                    displayNotification(stopCode, service, time);
                    return;
                }
            }
        }
        
        // All the services have been looped through and the criteria didn't
        // match. This means a reschedule should be attempted.
        reschedule(intent);
    }
    
    /**
     * Display a notification to the user to alert them to the arrival.
     * 
     * @param stopCode The {@code stopCode} of the bus stop the arrival will
     * take place at.
     * @param serviceName The name of the service which is about to arrive.
     * @param time The number of minutes until the service will arrive at the
     * stop.
     */
    private void displayNotification(final String stopCode,
            final String serviceName, final int time) {
        // Create the intent that's fired when the notification is
        // tapped. It shows the bus times view for that stop.
        final Intent launchIntent = new Intent(this,
                DisplayStopDataActivity.class);
        launchIntent.setAction(DisplayStopDataActivity
                .ACTION_VIEW_STOP_DATA);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        launchIntent.putExtra(DisplayStopDataActivity.EXTRA_STOP_CODE,
                stopCode);

        final String stopName = getBusStopName(stopCode);
        final String title = getString(R.string
                .timeservice_notification_title);
        final String summary = getResources().getQuantityString(
                R.plurals.timeservice_notification_summary,
                time == 0 ? 1 : time, serviceName, time, stopName);

        // Build the notification.
        final NotificationCompat.Builder notifBuilder =
                new NotificationCompat.Builder(this);
        notifBuilder.setAutoCancel(true);
        notifBuilder.setSmallIcon(R.drawable.ic_status_bus);
        notifBuilder.setTicker(summary);
        notifBuilder.setContentTitle(title);
        notifBuilder.setContentText(summary);
        // Support for Jelly Bean notifications.
        notifBuilder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(summary));
        notifBuilder.setContentIntent(
                PendingIntent.getActivity(this, 0, launchIntent,
                    PendingIntent.FLAG_ONE_SHOT));

        final Notification n = notifBuilder.build();
        if (preferenceManager.isNotificationWithSound())
            n.defaults |= Notification.DEFAULT_SOUND;

        if (preferenceManager.isNotificationWithVibration())
            n.defaults |= Notification.DEFAULT_VIBRATE;

        if (preferenceManager.isNotificationWithLed()) {
            n.defaults |= Notification.DEFAULT_LIGHTS;
            n.flags |= Notification.FLAG_SHOW_LIGHTS;
        }

        // Send the notification.
        notifMan.notify(ALERT_ID, n);
    }
    
    /**
     * Reschedule the retrieval of bus times from the server because there was
     * an error loading them or the service/time criteria has not been met.
     * 
     * If the rescheduling goes on for an hour, then cancel the checking and
     * remove the alert otherwise the user's battery will be drained and data
     * used.
     * 
     * @param intent The {@link Intent} that started this service. This is to be
     * reused to start the next service at the appropriate time.
     */
    private void reschedule(final Intent intent) {
        final long timeSet = intent.getLongExtra(ARG_TIME_SET, 0);
        
        // Checks to see if the alert has been active for the last hour or more.
        // If so, it gets cancelled.
        if((SystemClock.elapsedRealtime() - timeSet) >= 3600000) {
            alertMan.removeTimeAlert();
            return;
        }
        
        final PendingIntent pi = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        // Reschedule ourself to run again in 60 seconds.
        alarmMan.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 60000, pi);
    }

    /**
     * Get the name of a bus stop.
     *
     * @param stopCode The code of the stop to get a name for.
     * @return The name of the bus stop, or {@code null} if getting this failed.
     */
    @Nullable
    private String getBusStopName(@NonNull final String stopCode) {
        final Cursor c = getContentResolver().query(BusStopContract.BusStops.CONTENT_URI,
                new String[] { BusStopContract.BusStops.STOP_NAME },
                BusStopContract.BusStops.STOP_CODE + " = ?", new String[] { stopCode }, null);
        final String result;

        if (c != null) {
            if (c.moveToFirst()) {
                result = c.getString(c.getColumnIndex(BusStopContract.BusStops.STOP_NAME));
            } else {
                result = null;
            }

            c.close();
        } else {
            result = null;
        }

        return result;
    }
}