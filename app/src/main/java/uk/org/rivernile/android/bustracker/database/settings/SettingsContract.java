/*
 * Copyright (C) 2015 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.database.settings;

import android.net.Uri;
import android.provider.BaseColumns;

import uk.org.rivernile.edinburghbustracker.android.BuildConfig;

/**
 * <p>
 *     The contract between the settings provider and the rest of the application. Contains
 *     definitions for the supported URIs and data columns.
 * </p>
 *
 * <h3>Overview</h3>
 *
 * <p>
 *     {@code SettingsContract} defines the data model of favourites and alerts related information.
 *     This data is stored in the following tables;
 * </p>
 *
 * <ul>
 *     <li>The {@link Favourites} table holds the user's saved favourite stops. Each row in this
 *     table represents a single favourite stop.</li>
 *     <li>The {@link Alerts} table holds alerts the user has set. Each row in this table
 *     represents a single alert. Pay attention to the {@link Alerts#TYPE} field so that the type
 *     of alert can be detected.</li>
 * </ul>
 *
 * @author Niall Scott
 */
public final class SettingsContract {

    /**
     * The authority of the {@link android.content.ContentProvider}.
     */
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.settings";

    /**
     * The content {@link Uri} used to reference the {@link android.content.ContentProvider}.
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    /**
     * The name of the database file.
     */
    static final String DB_NAME = "settings.db";
    /**
     * The version of the schema.
     */
    static final int DB_VERSION = 3;

    private static final String SUBTYPE_SINGLE = "vnd.android.cursor.item/";
    private static final String SUBTYPE_MULTIPLE = "vnd.android.cursor.dir/";

    /**
     * The constructor is private to prevent instantiation.
     */
    private SettingsContract() { }

    /**
     * Columns from the favourites table.
     */
    protected interface FavouritesColumns extends BaseColumns {

        /**
         * The unique code of the favourite stop. Column name.
         *
         * <p>
         *     Type: STRING
         * </p>
         */
        String STOP_CODE = "stopCode";

        /**
         * The saved name of the favourite stop. Column name.
         *
         * <p>
         *     Type: STRING
         * </p>
         */
        String STOP_NAME = "stopName";
    }

    /**
     * Fields for interacting with favourites. Each row of this table represents a single favourite
     * stop saved by the user. Both {@link #STOP_CODE} and {@link #STOP_NAME} must be supplied when
     * inserting a new favourite.
     */
    public static final class Favourites implements FavouritesColumns {

        /**
         * The name of the table. To be used within the package.
         */
        static final String TABLE_NAME = "favourite_stops";

        private static final String TYPE_FAVOURITES = "vnd." + AUTHORITY + '.' + TABLE_NAME;

        /**
         * The content type of multi-item results.
         */
        static final String CONTENT_TYPE = SUBTYPE_MULTIPLE + TYPE_FAVOURITES;
        /**
         * The content type of single item results.
         */
        static final String CONTENT_ITEM_TYPE = SUBTYPE_SINGLE + TYPE_FAVOURITES;

        /**
         * The content {@link Uri} used to access the favourites table.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + '/' +
                TABLE_NAME);

        /**
         * The constructor is private to prevent instantiation.
         */
        private Favourites() { }
    }

    /**
     * Columns from the alerts table.
     */
    protected interface AlertsColumns extends BaseColumns {

        /**
         * The type of alert. Column name.
         *
         * <p>
         *     Type: INTEGER (one of {@link #ALERTS_TYPE_PROXIMITY} or {@link #ALERTS_TYPE_TIME})
         * </p>
         */
        String TYPE = "type";

        /**
         * A proximity alert type.
         */
        byte ALERTS_TYPE_PROXIMITY = 1;
        /**
         * A time alert type.
         */
        byte ALERTS_TYPE_TIME = 2;

        /**
         * The time the alert was added. Column name.
         *
         * <p>
         *     Type: INTEGER (long)
         * </p>
         */
        String TIME_ADDED = "timeAdded";

        /**
         * The stop code for this alert. Column name.
         *
         * <p>
         *     Type: STRING
         * </p>
         */
        String STOP_CODE = "stopCode";

        /**
         * The maximum distance from the stop the alert should activate. Column name.
         *
         * <p>
         *     Type: INTEGER
         * </p>
         */
        String DISTANCE_FROM = "distanceFrom";

        /**
         * The names of the services the alert concerns. Column name.
         *
         * <p>
         *     Type: STRING (comma separated list)
         * </p>
         */
        String SERVICE_NAMES = "serviceNames";

        /**
         * The maximum amount of time before the service is due to the alert should activate. Column
         * name.
         *
         * <p>
         *     Type: INTEGER
         * </p>
         */
        String TIME_TRIGGER = "timeTrigger";
    }

    /**
     * Fields for interacting with alerts. Each row of this table represents a single alert. The
     * following fields must be included when inserting a new alert;
     *
     * <ul>
     *     <li>{@link #_ID}</li>
     *     <li>{@link #TYPE}</li>
     *     <li>{@link #TIME_ADDED}</li>
     *     <li>{@link #STOP_CODE}</li>
     * </ul>
     *
     * <p>
     *     When the type is {@link #ALERTS_TYPE_TIME}, {@link #SERVICE_NAMES} and
     *     {@link #TIME_TRIGGER} must also be supplied.
     * </p>
     *
     * <p>
     *     When the type is {@link #ALERTS_TYPE_PROXIMITY}, {@link #DISTANCE_FROM} must be also be
     *     supplied.
     * </p>
     */
    public static final class Alerts implements AlertsColumns {

        /**
         * The name of the table. To be used within the package.
         */
        static final String TABLE_NAME = "active_alerts";

        private static final String TYPE_ALERTS = "vnd." + AUTHORITY + '.' + TABLE_NAME;

        /**
         * The content type of multi-item results.
         */
        static final String CONTENT_TYPE = SUBTYPE_MULTIPLE + TYPE_ALERTS;
        /**
         * The content type of single item results.
         */
        static final String CONTENT_ITEM_TYPE = SUBTYPE_SINGLE + TYPE_ALERTS;

        /**
         * The content {@link Uri} used to access the alerts table.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + '/' +
                TABLE_NAME);

        /**
         * The constructor is private to prevent instantiation.
         */
        private Alerts() { }
    }
}
