/*
 * Copyright (C) 2009 - 2016 Niall 'Rivernile' Scott
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

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import uk.org.rivernile.android.bustracker.BusApplication;
import uk.org.rivernile.android.bustracker.FragmentFactory;
import uk.org.rivernile.android.bustracker.endpoints.BusTrackerEndpoint;
import uk.org.rivernile.android.bustracker.endpoints.DatabaseEndpoint;
import uk.org.rivernile.android.bustracker.endpoints.HttpBusTrackerEndpoint;
import uk.org.rivernile.android.bustracker.endpoints.HttpDatabaseEndpoint;
import uk.org.rivernile.android.bustracker.endpoints.HttpTwitterEndpoint;
import uk.org.rivernile.android.bustracker.endpoints.TwitterEndpoint;
import uk.org.rivernile.android.bustracker.endpoints.UrlBuilder;
import uk.org.rivernile.android.bustracker.parser.twitter.TwitterParserImpl;
import uk.org.rivernile.edinburghbustracker.android.parser.livetimes.EdinburghParser;
import uk.org.rivernile.edinburghbustracker.android.parser.database.EdinburghDatabaseVersionParser;
import uk.org.rivernile.edinburghbustracker.android.utils.EdinburghUrlBuilder;

/**
 * This class is the main entry point to the My Bus Edinburgh application. Some app resources,
 * such as the {@link BusStopDatabase}, can be retrieved from here.
 *
 * @author Niall Scott
 */
public class MyBusEdinburghApplication extends BusApplication {
    
    private EdinburghUrlBuilder urlBuilder;
    private BusTrackerEndpoint busTrackerEndpoint;
    private DatabaseEndpoint databaseEndpoint;
    private TwitterEndpoint twitterEndpoint;
    private BusStopDatabase busStopDatabase;
    private FragmentFactory fragmentFactory;
    private Picasso picasso;

    @Override
    public void onCreate() {
        super.onCreate();
        
        new Thread(cleanupTasks).start();
    }

    @Override
    public synchronized BusTrackerEndpoint getBusTrackerEndpoint() {
        if (busTrackerEndpoint == null) {
            busTrackerEndpoint = new HttpBusTrackerEndpoint(this, new EdinburghParser(),
                    getUrlBuilder());
        }
        
        return busTrackerEndpoint;
    }

    @Override
    public synchronized DatabaseEndpoint getDatabaseEndpoint() {
        if (databaseEndpoint == null) {
            databaseEndpoint = new HttpDatabaseEndpoint(this, new EdinburghDatabaseVersionParser(),
                    getUrlBuilder());
        }
        
        return databaseEndpoint;
    }

    @Override
    public synchronized TwitterEndpoint getTwitterEndpoint() {
        if (twitterEndpoint == null) {
            twitterEndpoint = new HttpTwitterEndpoint(this, new TwitterParserImpl(),
                    getUrlBuilder());
        }
        
        return twitterEndpoint;
    }

    @Override
    public synchronized BusStopDatabase getBusStopDatabase() {
        if (busStopDatabase == null) {
            busStopDatabase = BusStopDatabase.getInstance(this);
        }
        
        return busStopDatabase;
    }

    @Override
    public FragmentFactory getFragmentFactory() {
        if (fragmentFactory == null) {
            fragmentFactory = new EdinburghFragmentFactory();
        }
        
        return fragmentFactory;
    }

    @Override
    public Picasso getPicasso() {
        if (picasso == null) {
            picasso = new Picasso.Builder(this)
                    .downloader(new OkHttp3Downloader(this))
                    .build();
        }

        return picasso;
    }

    /**
     * Get an instance of the {@link UrlBuilder}.
     * 
     * @return An instance of the {@link UrlBuilder}.
     */
    private UrlBuilder getUrlBuilder() {
        if (urlBuilder == null) {
            urlBuilder = new EdinburghUrlBuilder();
        }
        
        return urlBuilder;
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