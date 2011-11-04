/*
 * Copyright (C) 2011 Niall 'Rivernile' Scott
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
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;
import uk.org.rivernile.edinburghbustracker.android.PreferencesActivity;
import uk.org.rivernile.edinburghbustracker.android.SettingsDatabase;

public class TimeAlertReceiver extends BroadcastReceiver {
    
    private final static int ALERT_ID = 2;
    
    @Override
    public void onReceive(final Context context, final Intent intent) {
        SettingsDatabase db = SettingsDatabase.getInstance(context);
        String stopCode = intent.getStringExtra("stopCode");
        if(!db.isActiveTimeAlert(stopCode)) return;
        
        String stopName = BusStopDatabase.getInstance(context)
                .getNameForBusStop(stopCode);
        
        NotificationManager notMan = (NotificationManager)context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        AlarmManager alMan = (AlarmManager)context
                .getSystemService(Context.ALARM_SERVICE);
        
        SharedPreferences sp = context
                .getSharedPreferences(PreferencesActivity.PREF_FILE, 0);
        
        Notification n = new Notification();
        
        if(sp.getBoolean("pref_alertsound_state", true))
            n.defaults |= Notification.DEFAULT_SOUND;
        
        if(sp.getBoolean("pref_alertvibrate_state", true))
            n.defaults |= Notification.DEFAULT_VIBRATE;
        
        if(sp.getBoolean("pref_alertled_state", true)) {
            n.defaults |= Notification.DEFAULT_LIGHTS;
            n.flags |= Notification.FLAG_SHOW_LIGHTS;
        }
    }
}