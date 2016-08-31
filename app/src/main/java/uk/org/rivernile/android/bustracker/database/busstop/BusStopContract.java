/*
 * Copyright (C) 2016 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.database.busstop;

import android.net.Uri;
import android.provider.BaseColumns;

import uk.org.rivernile.edinburghbustracker.android.BuildConfig;

/**
 * The contract between the bus stop database and the rest of the application. Contains definitions
 * for the supported URIs and data columns. All data is read-only.
 *
 * <h3>Overview</h3>
 *
 * <ul>
 *     <li>The {@link DatabaseInformation} table contains information on this instance of the
 *     database, such as identifying codes and the date of creation.</li>
 *     <li>The {@link Services} table contains data on all known services in the system.</li>
 *     <li>The {@link BusStops} table contains data on all known bus stops in the system.</li>
 *     <li>The {@link ServiceStops} table contains a mapping between bus stops and bus services, so
 *     it can be looked up at what services stop at which bus stops, and vice-versa.</li>
 *     <li>The {@link ServicePoints} table contains all the service points in the system. These
 *     points are linked together to form a route line for a service, necessary for drawing service
 *     routes on a map.</li>
 * </ul>
 *
 * @author Niall Scott
 */
public final class BusStopContract {

    /**
     * The authority of the {@link android.content.ContentProvider}.
     */
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.busstop";

    /**
     * The content {@link Uri} used to reference the {@link android.content.ContentProvider}.
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    /**
     * This is the name of the schema for the database.
     */
    public static final String SCHEMA_NAME = "MBE_10";

    /**
     * The name of the database file.
     */
    static final String DB_NAME = "busstops10.db";
    /**
     * The version of the schema.
     */
    static final int DB_VERSION = 1;

    /**
     * The method to "call" on the {@link android.content.ContentProvider} to replace the database,
     * for example when a new version of the database has been downloaded.
     */
    static final String METHOD_REPLACE_DATABASE = "replaceDatabase";

    private static final String SUBTYPE_MULTIPLE = "vnd.android.cursor.dir/";

    /**
     * The constructor is private to prevent instantiation.
     */
    private BusStopContract() { }

    /**
     * Columns from the database information table.
     */
    protected interface DatabaseInformationColumns extends BaseColumns {

        /**
         * The current topology ID. Column name.
         *
         * <p>
         *     Type: STRING
         * </p>
         */
        String CURRENT_TOPOLOGY_ID = "current_topo_id";

        /**
         * The timestamp in milliseconds when the database was last updated. Column name.
         *
         * <p>
         *     Type: LONG
         * </p>
         */
        String LAST_UPDATE_TIMESTAMP = "updateTS";
    }

    /**
     * Fields for interacting with datbase information. This table will only contain 1 row, and is
     * read-only.
     */
    public static final class DatabaseInformation implements DatabaseInformationColumns {

        /**
         * The name of the table. To be used within the package.
         */
        static final String TABLE_NAME = "database_info";

        private static final String TYPE_DATABASE_INFO = "vnd." + AUTHORITY + '.' + TABLE_NAME;

        /**
         * The content type of single item results.
         */
        static final String CONTENT_TYPE = SUBTYPE_MULTIPLE + TYPE_DATABASE_INFO;

        /**
         * The content {@link Uri} used to access the favourites table.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + '/' +
                TABLE_NAME);

        /**
         * The constructor is private to prevent instantiation.
         */
        private DatabaseInformation() { }
    }

    /**
     * Columns from the service table.
     */
    protected interface ServicesColumns extends BaseColumns {

        /**
         * The name of the service. Column name.
         *
         * <p>
         *     Type: STRING
         * </p>
         */
        String NAME = "name";

        /**
         * The description of the service. Column name.
         *
         * <p>
         *     Type: STRING
         * </p>
         */
        String DESCRIPTION = "desc";

        /**
         * The colour of the service, expressed in hex, with a preceding {@code #}.
         *
         * <p>
         *     Type: STRING
         * </p>
         */
        String COLOUR = "hex_colour";
    }

    /**
     * Fields for interacting with services. Each row of this table represents a single service.
     * This table is read-only.
     */
    public static final class Services implements ServicesColumns {

        /**
         * The name of the table. To be used within the package.
         */
        static final String TABLE_NAME = "view_services";

        private static final String TYPE_SERVICES = "vnd." + AUTHORITY + '.' + TABLE_NAME;

        /**
         * The content type of multi-item results.
         */
        static final String CONTENT_TYPE = SUBTYPE_MULTIPLE + TYPE_SERVICES;

        /**
         * The content {@link Uri} used to access the favourites table.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + '/' +
                TABLE_NAME);

        /**
         * The constructor is private to prevent instantiation.
         */
        private Services() { }
    }

    /**
     * Columns from the bus stop table.
     */
    protected interface BusStopsColumns extends BaseColumns {

        /**
         * The unique code of the bus stop. Column name.
         *
         * <p>
         *     Type: STRING
         * </p>
         */
        String STOP_CODE = "stopCode";

        /**
         * The name of the bus stop. Column name.
         *
         * <p>
         *     Type: STRING
         * </p>
         */
        String STOP_NAME = "stopName";

        /**
         * The latitude of the bus stop. Column name.
         *
         * <p>
         *     Type: DOUBLE
         * </p>
         */
        String LATITUDE = "x";

        /**
         * The longitude of the bus stop. Column name.
         *
         * <p>
         *     Type: DOUBLE
         * </p>
         */
        String LONGITUDE = "y";

        /**
         * The orientation of the bus stop, expressed between 0 and 7, where 0 is north and 7 is
         * north-west, and the rest of the numbers are filled in clockwise. Column name.
         *
         * <p>
         *     Type: INTEGER
         * </p>
         */
        String ORIENTATION = "orientation";

        /**
         * The locality of the bus stop - for example, the name of the local area in the city.
         * Column name.
         *
         * <p>
         *     Type: STRING
         * </p>
         */
        String LOCALITY = "locality";

        /**
         * The listing of services as a comma separated list, suitable for displaying to users.
         * Column name.
         *
         * <p>
         *     Type: STRING
         * </p>
         */
        String SERVICE_LISTING = "serviceListing";
    }

    /**
     * Fields for interacting with bus stops. Each row of this table represents a single bus stop.
     * This table is read-only.
     */
    public static final class BusStops implements BusStopsColumns {

        /**
         * The name of the table. To be used within the package.
         */
        static final String TABLE_NAME = "view_bus_stops";

        private static final String TYPE_BUS_STOPS = "vnd." + AUTHORITY + '.' + TABLE_NAME;

        /**
         * The content type of multi-item results.
         */
        static final String CONTENT_TYPE = SUBTYPE_MULTIPLE + TYPE_BUS_STOPS;

        /**
         * The content {@link Uri} used to access the favourites table.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + '/' +
                TABLE_NAME);

        /**
         * The constructor is private to prevent instantiation.
         */
        private BusStops() { }
    }

    /**
     * Columns from the service stops table.
     */
    protected interface ServiceStopsColumns extends BaseColumns {

        /**
         * The stop code. Column name.
         *
         * <p>
         *     Type: STRING
         * </p>
         */
        String STOP_CODE = "stopCode";

        /**
         * The service name. Column name.
         *
         * <p>
         *     Type: STRING
         * </p>
         */
        String SERVICE_NAME = "serviceName";
    }

    /**
     * Fields for interacting with service stops. Each row of this table represents a mapping
     * between a bus stop and a bus service. This table is read-only.
     */
    public static final class ServiceStops implements ServiceStopsColumns {

        /**
         * The name of the table. To be used within the package.
         */
        static final String TABLE_NAME = "service_stops";

        private static final String TYPE_SERVICE_STOPS = "vnd." + AUTHORITY + '.' + TABLE_NAME;

        /**
         * The content type of multi-item results.
         */
        static final String CONTENT_TYPE = SUBTYPE_MULTIPLE + TYPE_SERVICE_STOPS;

        /**
         * The content {@link Uri} used to access the favourites table.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + '/' +
                TABLE_NAME);

        /**
         * The constructor is private to prevent instantiation.
         */
        private ServiceStops() { }
    }

    /**
     * Columns from the service points table.
     */
    protected interface ServicePointsColumns extends BaseColumns {

        /**
         * The name of the service. Column name.
         *
         * <p>
         *     Type: STRING
         * </p>
         */
        String SERVICE_NAME = "serviceName";

        /**
         * The stop code associated with the point. Column name.
         *
         * <p>
         *     Type: STRING
         * </p>
         */
        String STOP_CODE = "stopCode";

        /**
         * A value denoting the order of the point within the chainage. Column name.
         *
         * <p>
         *     Type: INTEGER
         * </p>
         */
        String ORDER_VALUE = "order_value";

        /**
         * A value denoting a chainage for the point (a chainage is a grouping of points). Column
         * name.
         *
         * <p>
         *     Type: INTEGER
         * </p>
         */
        String CHAINAGE = "chainage";

        /**
         * The latitude of the point.
         *
         * <p>
         *     Type: DOUBLE
         * </p>
         */
        String LATITUDE = "latitude";

        /**
         * The longitude of the point.
         *
         * <p>
         *     Type: DOUBLE
         * </p>
         */
        String LONGITUDE = "longitude";
    }

    /**
     * Fields for interacting with service points. Each row in this table represents a point on a
     * route for a service. This table is read-only.
     */
    public static final class ServicePoints implements ServicePointsColumns {

        /**
         * The name of the table. To be used within the package.
         */
        static final String TABLE_NAME = "service_point";

        private static final String TYPE_SERVICE_POINTS = "vnd." + AUTHORITY + '.' + TABLE_NAME;

        /**
         * The content type of multi-item results.
         */
        static final String CONTENT_TYPE = SUBTYPE_MULTIPLE + TYPE_SERVICE_POINTS;

        /**
         * The content {@link Uri} used to access the favourites table.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + '/' +
                TABLE_NAME);

        /**
         * The constructor is private to prevent instantiation.
         */
        private ServicePoints() { }
    }
}
