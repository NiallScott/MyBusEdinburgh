/*
 * Copyright (C) 2009 - 2014 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker;

import android.app.Application;
import android.app.backup.BackupManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import com.bugsense.trace.BugSenseHandler;
import uk.org.rivernile.android.bustracker.database.DatabaseUpdateService;
import uk.org.rivernile.android.bustracker.endpoints.BusTrackerEndpoint;
import uk.org.rivernile.android.bustracker.endpoints.DatabaseEndpoint;
import uk.org.rivernile.android.bustracker.endpoints.TwitterEndpoint;
import uk.org.rivernile.android.bustracker.preferences.PreferenceConstants;
import uk.org.rivernile.edinburghbustracker.android.ApiKey;
import uk.org.rivernile.edinburghbustracker.android.BuildConfig;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;
import uk.org.rivernile.edinburghbustracker.android.SettingsDatabase;

/**
 * This code is the very first code that will be executed when the application
 * is started. It is used to register the BugSense handler, put a listener on
 * the SharedPreferences for Google Backup, and check for bus stop database
 * updates.
 * 
 * The Android developer documentation discourages the usage of this class, but
 * as it is unpredictable where the user will enter the application the code is
 * put here as this class is always instantiated when this application's process
 * is created.
 * 
 * @author Niall Scott
 */
public abstract class BusApplication extends Application
        implements OnSharedPreferenceChangeListener {
    
    @Override
    public void onCreate() {
        super.onCreate();

        // Register the BugSense handler.
        if (BuildConfig.BUGSENSE_ENABLED) {
            BugSenseHandler.initAndStartSession(this, ApiKey.BUGSENSE_KEY);
        }

        getSharedPreferences(PreferenceConstants.PREF_FILE, 0)
                .registerOnSharedPreferenceChangeListener(this);
        
        // Cause the bus stop database to be extracted straight away.
        BusStopDatabase.getInstance(this);
        // Start the database update service.
        startService(new Intent(this, DatabaseUpdateService.class));
    }
    
    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sp,
            final String key) {
        BackupManager.dataChanged(getPackageName());
    }
    
    /**
     * Get the bus tracker endpoint.
     * 
     * @return The BusTrackerEndpoint instance for this application.
     */
    public abstract BusTrackerEndpoint getBusTrackerEndpoint();
    
    /**
     * Get the database endpoint, used for checking for bus stop database
     * updates.
     * 
     * @return The DatabaseEndpoint instance for this application.
     */
    public abstract DatabaseEndpoint getDatabaseEndpoint();
    
    /**
     * Get the Twitter endpoint, used for loading a list of Tweets to show the
     * user updates.
     * 
     * @return The TwitterEndpoint instance for this application.
     */
    public abstract TwitterEndpoint getTwitterEndpoint();
    
    /**
     * Get an instance of the BusStopDatabase.
     * 
     * @return An instance of the BusStopDatabase.
     */
    public abstract BusStopDatabase getBusStopDatabase();
    
    /**
     * Get an instance of the SettingsDatabase.
     * 
     * @return An instance of the SettingsDatabase.
     */
    public abstract SettingsDatabase getSettingsDatabase();
    
    /**
     * Get an instance of the FragmentFactory.
     * 
     * @return An instance of the FragmentFactory.
     */
    public abstract FragmentFactory getFragmentFactory();
}