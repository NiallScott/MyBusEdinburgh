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

package uk.org.rivernile.edinburghbustracker.android;

import java.io.File;
import uk.org.rivernile.android.bustracker.BusApplication;
import uk.org.rivernile.edinburghbustracker.android.endpoints
        .BusTrackerEndpoint;
import uk.org.rivernile.edinburghbustracker.android.endpoints
        .HttpBusTrackerEndpoint;
import uk.org.rivernile.edinburghbustracker.android.livetimes.parser
        .EdinburghParser;

/**
 * This class is the main entry point to the My Bus Edinburgh application. Some
 * app resources, such as the BusStopDatabase, can be retrieved from here.
 * 
 * @author Niall Scott
 */
public class MyBusEdinburghApplication extends BusApplication {
    
    private BusTrackerEndpoint busTrackerEndpoint;
    private BusStopDatabase busStopDatabase;
    private SettingsDatabase settingsDatabase;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        super.onCreate();
        
        new Thread(cleanupTasks).start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized BusTrackerEndpoint getBusTrackerEndpoint() {
        if (busTrackerEndpoint == null) {
            busTrackerEndpoint = new HttpBusTrackerEndpoint(
                    new EdinburghParser());
        }
        
        return busTrackerEndpoint;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized BusStopDatabase getBusStopDatabase() {
        if (busStopDatabase == null) {
            busStopDatabase = BusStopDatabase.getInstance(this);
        }
        
        return busStopDatabase;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized SettingsDatabase getSettingsDatabase() {
        if (settingsDatabase == null) {
            settingsDatabase = SettingsDatabase.getInstance(this);
        }
        
        return settingsDatabase;
    }
    
    private final Runnable cleanupTasks = new Runnable() {
        @Override
        public void run() {
            // Delete old database files if they exist.
            final File[] filesToDelete = new File[] {
                getDatabasePath("busstops.db"),
                getDatabasePath("busstops.db-journal"),
                getDatabasePath("busstops2.db"),
                getDatabasePath("busstops2.db-journal"),
                getDatabasePath("busstops8.db"),
                getDatabasePath("busstops8.db-journal")
            };
            
            for (File f : filesToDelete) {
                f.delete();
            }
        }
    };
}