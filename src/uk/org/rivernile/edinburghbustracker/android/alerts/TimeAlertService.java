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
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import java.util.HashMap;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusParser;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusParserException;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusService;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusStop;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;
import uk.org.rivernile.edinburghbustracker.android.DisplayStopDataActivity;
import uk.org.rivernile.edinburghbustracker.android.PreferencesActivity;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.edinburghbustracker.android.SettingsDatabase;
import uk.org.rivernile.edinburghbustracker.android.livetimes.parser.EdinburghBus;
import uk.org.rivernile.edinburghbustracker.android.livetimes.parser
        .EdinburghParser;

public class TimeAlertService extends IntentService {
    
    private final static int ALERT_ID = 2;
    
    private SettingsDatabase sd;
    private BusStopDatabase bsd;
    private NotificationManager notifMan;
    private AlertManager alertMan;
    private AlarmManager alarmMan;
    private BusParser parser;
    private SharedPreferences sp;
    
    public TimeAlertService() {
        super(TimeAlertService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        sd = SettingsDatabase.getInstance(getApplicationContext());
        bsd = BusStopDatabase.getInstance(getApplicationContext());
        notifMan = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        alertMan = AlertManager.getInstance(this);
        alarmMan = (AlarmManager)getSystemService(ALARM_SERVICE);
        parser = EdinburghParser.getInstance();
        sp = getSharedPreferences(PreferencesActivity.PREF_FILE, 0);
    }
    
    @Override
    protected void onHandleIntent(final Intent intent) {
        final String stopCode = intent.getStringExtra("stopCode");
        final String[] services = intent.getStringArrayExtra("services");
        final int timeTrigger = intent.getIntExtra("timeTrigger", 5);
        //final long timeSet = intent.getLongExtra("timeSet", 0);
        
        HashMap<String, BusStop> result = null;
        try {
            result = parser.getBusStopData(new String[] { stopCode }, 1);
        } catch(BusParserException e) {
            reschedule(intent);
            return;
        }
        
        final BusStop busStop = result.get(stopCode);
        int time;
        EdinburghBus edinBs;
        
        for(BusService bs : busStop.getBusServices()) {
            edinBs = (EdinburghBus)bs.getFirstBus();
            time = edinBs.getArrivalMinutes();
            
            for(String service : services) {
                if(service.equals(bs.getServiceName()) && time <= timeTrigger) {
                    if(!sd.isActiveTimeAlert(stopCode)) return;
                    alertMan.removeTimeAlert();
                    
                    Intent launchIntent = new Intent(this,
                            DisplayStopDataActivity.class);
                    launchIntent.setAction(DisplayStopDataActivity
                            .ACTION_VIEW_STOP_DATA);
                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    launchIntent.putExtra("stopCode", stopCode);
                    launchIntent.putExtra("forceLoad", true);
                    
                    String stopName = bsd.getNameForBusStop(stopCode);
                    
                    String title = getString(R.string.alert_time_title)
                            .replace("%stopName", stopName);
                    String summary;
                    if(time >= 2) {
                        summary = getString(R.string.alert_time_summary_plural)
                                .replace("%service", service)
                                .replace("%mins", String.valueOf(time))
                                .replace("%stopName", stopName);
                    } else {
                        summary = getString(R.string.alert_time_summary_due)
                                .replace("%service", service)
                                .replace("%stopName", stopName);
                    }
                    
                    Notification n = new Notification();
                    
                    if(sp.getBoolean("pref_alertsound_state", true))
                        n.defaults |= Notification.DEFAULT_SOUND;

                    if(sp.getBoolean("pref_alertvibrate_state", true))
                        n.defaults |= Notification.DEFAULT_VIBRATE;

                    if(sp.getBoolean("pref_alertled_state", true)) {
                        n.defaults |= Notification.DEFAULT_LIGHTS;
                        n.flags |= Notification.FLAG_SHOW_LIGHTS;
                    }
                    
                    n.flags |= Notification.FLAG_AUTO_CANCEL;
                    n.icon = R.drawable.ic_status_bus;
                    n.when = System.currentTimeMillis();
                    n.tickerText = summary;
                    n.setLatestEventInfo(this, title, summary,
                            PendingIntent.getActivity(this, 0, launchIntent,
                            PendingIntent.FLAG_ONE_SHOT));
        
                    notifMan.notify(ALERT_ID, n);
                    return;
                }
            }
        }
        
        reschedule(intent);
    }
    
    private void reschedule(final Intent intent) {
        final long timeSet = intent.getLongExtra("timeSet", 0);
        
        if((SystemClock.elapsedRealtime() - timeSet) >= 3600000) {
            alertMan.removeTimeAlert();
            return;
        }
        
        PendingIntent pi = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMan.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 60000, pi);
    }
}