/*
 * Copyright (C) 2009 - 2013 Niall 'Rivernile' Scott
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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.Html;
import android.text.Spanned;
import android.text.SpannedString;
import com.google.android.gms.maps.model.LatLng;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * This class deals with the handling of the Bus Stop Database. It deals with
 * the initialisation as well as instance creation. Get an instance of this
 * class from the getInstance() method.
 * 
 * @author Niall Scott
 */
public final class BusStopDatabase extends SQLiteOpenHelper {

    /** The name of the database. */
    public static final String STOP_DB_NAME = "busstops9.db";
    /** This is the schema name of the database. */
    public static final String SCHEMA_NAME = "MBE_9";
    /** The version of the database. For internal use only. */
    protected static final int STOP_DB_VERSION = 1;
    
    private static final String BUS_STOPS_TABLE = "bus_stops";
    private static final String BUS_STOPS_STOPCODE = "stopCode";
    private static final String BUS_STOPS_STOPNAME = "stopName";
    private static final String BUS_STOPS_X = "x";
    private static final String BUS_STOPS_Y = "y";
    private static final String BUS_STOPS_ORIENTATION = "orientation";
    private static final String BUS_STOPS_LOCALITY = "locality";
    
    private static final String DATABASE_INFO_TABLE = "database_info";
    private static final String DATABASE_INFO_UPDATE_TIME = "updateTS";
    private static final String DATABASE_INFO_TOPOLOGY = "current_topo_id";
    
    private static final String SERVICE_STOPS_TABLE = "service_stops";
    private static final String SERVICE_STOPS_STOPCODE = "stopCode";
    private static final String SERVICE_STOPS_SERVICE_NAME = "serviceName";
    
    private static final String SERVICE_TABLE = "service";
    private static final String SERVICE_ID = "_id";
    private static final String SERVICE_SERVICE_NAME = "name";
    
    private static final String SERVICE_COLOUR_TABLE = "service_colour";
    private static final String SERVICE_COLOUR_ID = "_id";
    private static final String SERVICE_COLOUR_HEX_COLOUR = "hex_colour";
    
    private static final String SERVICE_POINT_TABLE = "service_point";
    private static final String SERVICE_POINT_SERVICE_ID = "service_id";
    private static final String SERVICE_POINT_ORDER_VALUE = "order_value";
    private static final String SERVICE_POINT_CHAINAGE = "chainage";
    private static final String SERVICE_POINT_LATITUDE = "latitude";
    private static final String SERVICE_POINT_LONGITUDE = "longitude";

    private static BusStopDatabase instance = null;
    
    private Context context;
    private final File f;

    /**
     * Create a new instance of this class. This constructor will move the
     * assets version of the database in to place first if it does not exist.
     * 
     * @param context An application context.
     */
    private BusStopDatabase(final Context context) {
        super(context, STOP_DB_NAME, null, STOP_DB_VERSION);
        this.context = context;
        
        f = context.getDatabasePath(STOP_DB_NAME);
        if(!f.exists()) {
            new Thread(new RestoreDBFromAssetsTask(this)).start();
        } else {
            final long assetVersion = Long.parseLong(context.getString(
                    R.string.asset_db_version));
            long currentVersion;
            try {
                currentVersion = getLastDBModTime();
            } catch(SQLiteException e) {
                f.delete();
                new Thread(new RestoreDBFromAssetsTask(this)).start();
                return;
            }

            if(assetVersion > currentVersion) {
                f.delete();
                new Thread(new RestoreDBFromAssetsTask(this)).start();
            }
        }
    }

    /**
     * Get a new instance of the database object. If the physical database does
     * not exist, it will be created from assets. This class uses the singleton
     * design pattern, meaning that no more than 1 instance of this class will
     * exist.
     * 
     * @param context Provide an application context.
     * @return The singleton instance of this class.
     */
    public static BusStopDatabase getInstance(final Context context) {
        if(instance == null) instance = new BusStopDatabase(context);
        return instance;
    }
    
    /**
     * Move a copy of the database from the application assets in to the
     * application's database path.
     * 
     * @return True if the operation was successful, otherwise return false.
     */
    private synchronized boolean restoreDBFromAssets() {
        try {
            getWritableDatabase().close();
            final InputStream in = context.getAssets().open(STOP_DB_NAME);
            final FileOutputStream out = new FileOutputStream(f);
            final byte[] buf = new byte[1024];
            int len;
            
            while((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            
            out.flush();
            out.close();
            in.close();
            
            setUpIndexes(getWritableDatabase());
            
            return true;
        } catch(IOException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public void onCreate(final SQLiteDatabase db) {
        // The database should already exist, do nothing if it doesn't.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
            final int newVersion) {
        // Do nothing.
    }
    
    /**
     * Set up the indexes on the database to increase performance. Creating
     * index will cause the database to increase in size.
     * 
     * @param db The database to create the indexes on.
     */
    public static void setUpIndexes(final SQLiteDatabase db) {
        try {
            db.execSQL("CREATE INDEX service_point_index ON " +
                    "service_point(service_id, chainage, order_value)");
        } catch(SQLiteException e) {
            // Nothing to do, except now when route lines are drawn, the app
            // run like shit. This is most likely caused because we're out of
            // disk space.
        }
    }

    /**
     * Get information for a bus stop based on a boxed area. This is used to
     * return results based on location.
     * 
     * @param minX The minimum longitude to return results for.
     * @param minY The minimum latitude to return results for.
     * @param maxX The maximum longitude to return results for.
     * @param maxY The maximum latitude to return results for.
     * @return A database Cursor object with the result set.
     */
    public synchronized Cursor getBusStopsByCoords(final double minX,
            final double minY, final double maxX, final double maxY) {
        try {
            final String[] projection = new String[] {
                BUS_STOPS_STOPCODE,
                BUS_STOPS_STOPNAME,
                BUS_STOPS_X,
                BUS_STOPS_Y,
                BUS_STOPS_ORIENTATION,
                BUS_STOPS_LOCALITY
            };
            
            final SQLiteDatabase db = getReadableDatabase();
            return db.query(BUS_STOPS_TABLE, projection,
                    '(' + BUS_STOPS_X + " BETWEEN ? AND ?) AND " +
                    '(' + BUS_STOPS_Y + " BETWEEN ? AND ?)",
                    new String[] { String.valueOf(minX), String.valueOf(maxX),
                        String.valueOf(minY), String.valueOf(maxY)},
                    null, null, null);
        } catch(SQLiteException e) {
            return null;
        }
    }
    
    /**
     * Get information for a bus stop based on a boxed area. This is used to
     * return results based on location. Additionally, only return results
     * related to bus services as specified in the filter parameter.
     * 
     * @param minX The minimum longitude to return results for.
     * @param minY The minimum latitude to return results for.
     * @param maxX The maximum longitude to return results for.
     * @param maxY The maximum latitude to return results for.
     * @param filter Bus services to filter by, as a comma separated list as
     * defined by the SQL 'IN' parameter.
     * @return A database Cursor object with the result set.
     */
    public synchronized Cursor getFilteredStopsByCoords(final double minX,
            final double minY, final double maxX, final double maxY,
            final String filter) {
        try {
            final SQLiteDatabase db = getReadableDatabase();
            return db.rawQuery("SELECT " +
                    BUS_STOPS_TABLE + '.' + BUS_STOPS_STOPCODE + ", " +
                    BUS_STOPS_TABLE + '.' + BUS_STOPS_STOPNAME + ", " +
                    BUS_STOPS_TABLE + '.' + BUS_STOPS_X + ", " +
                    BUS_STOPS_TABLE + '.' + BUS_STOPS_Y + ", " +
                    BUS_STOPS_TABLE + '.' + BUS_STOPS_ORIENTATION + ", " +
                    BUS_STOPS_TABLE + '.' + BUS_STOPS_LOCALITY +
                    " FROM " + SERVICE_STOPS_TABLE +
                    " LEFT JOIN " + BUS_STOPS_TABLE + " ON " +
                    SERVICE_STOPS_TABLE + '.' + SERVICE_STOPS_STOPCODE + " = " +
                    BUS_STOPS_TABLE + '.' + BUS_STOPS_STOPCODE + " WHERE " +
                    SERVICE_STOPS_TABLE + '.' + SERVICE_STOPS_SERVICE_NAME +
                    " IN (" + filter + ") AND " +
                    '(' + BUS_STOPS_X + " BETWEEN ? AND ?) AND " +
                    '(' + BUS_STOPS_Y + " BETWEEN ? AND ?) " +
                    "GROUP BY " + BUS_STOPS_TABLE + '.' + BUS_STOPS_STOPCODE,
                    new String[] { String.valueOf(minX), String.valueOf(maxX),
                        String.valueOf(minY), String.valueOf(maxY)});
        } catch(SQLiteException e) {
            return null;
        }
    }

    /**
     * Return a result set for a bus stop based on it's stop code.
     * 
     * @param stopCode The bus stop code to query for.
     * @return A Cursor result set.
     */
    public synchronized Cursor getBusStopByCode(final String stopCode) {
        try {
            final SQLiteDatabase db = getReadableDatabase();
            return db.query(BUS_STOPS_TABLE, null, BUS_STOPS_STOPCODE + " = ?",
                    new String[] { stopCode }, null, null, null);
        } catch(SQLiteException e) {
            return null;
        }
    }

    /**
     * Get a String array of bus services which serve a particular bus stop.
     * 
     * @param stopCode The bus stop code to search for.
     * @return A String array of bus services.
     */
    public synchronized String[] getBusServicesForStop(final String stopCode) {
        String[] result;
        
        try {
            final SQLiteDatabase db = getReadableDatabase();
            final Cursor c = db.query(true, SERVICE_STOPS_TABLE,
                    new String[] { SERVICE_STOPS_SERVICE_NAME },
                    SERVICE_STOPS_STOPCODE + " = ?", new String[] { stopCode },
                    null, null,
                    "CASE WHEN " + SERVICE_STOPS_SERVICE_NAME +
                    " GLOB '[^0-9.]*' THEN " + SERVICE_STOPS_SERVICE_NAME +
                    " ELSE cast(" + SERVICE_STOPS_SERVICE_NAME + " AS int) END",
                    null);
            final int count = c.getCount();
            int i = 0;
            if(count > 0) {
                result = new String[count];
                while(c.moveToNext()) {
                    result[i] = c.getString(0);
                    i++;
                }
            } else {
                result = new String[] { };
            }
            
            c.close();
        } catch(SQLiteException e) {
            result = new String[] { };
        }
        
        return result;
    }
    
    /**
     * A convenience method for getBusServicesForStop() which returns a String
     * formatted as a comma separated list of bus services, for example;
     * 1, 2, 3, 3A, 4, 100, X48
     * 
     * @param stopCode The bus stop code to search for.
     * @return A comma separated list bus services.
     */
    public synchronized String getBusServicesForStopAsString(
            final String stopCode) {
        final String[] services = getBusServicesForStop(stopCode);
        if(services == null) return "";
        
        final StringBuilder builder = new StringBuilder();
        final int len = services.length;
        
        for(int i = 0; i < len; i++) {
            builder.append(services[i]);
            if(i != (len - 1)) builder.append(", ");
        }
        
        return builder.toString();
    }

    /**
     * Get the timestamp for when the bus stop database was last updated. If
     * the value was invalid, 0 is returned.
     * 
     * @return The timestamp for when the bus stop database was last updated. If
     * the value was invalid, 0 is returned.
     */
    public synchronized long getLastDBModTime() {
        long result = 0;
        
        try {
            final SQLiteDatabase db = getReadableDatabase();
            final Cursor c = db.query(true, DATABASE_INFO_TABLE,
                    new String[] { DATABASE_INFO_UPDATE_TIME }, null, null,
                    null, null, null, null);

            if(c.moveToNext()) {
                try {
                    result = Long.parseLong(c.getString(0));
                } catch(NumberFormatException e) { }
            }
            
            c.close();
        } catch(SQLiteException e) {
            
        }
        
        return result;
    }
    
    /**
     * Get the current topology ID. This is a string which uniquely identifies
     * the version of the bus stop data the database is using.
     * 
     * @return The current topology ID.
     */
    public synchronized String getTopoId() {
        String result = "";
        
        try {
            final SQLiteDatabase db = getReadableDatabase();
            final Cursor c = db.query(true, DATABASE_INFO_TABLE,
                    new String[] { DATABASE_INFO_TOPOLOGY }, null, null, null,
                    null, null, null);
            
            if(c.moveToNext()) {
                result = c.getString(0);
            }
            
            c.close();
        } catch(SQLiteException e) {
            
        }
        
        return result;
    }

    /**
     * Perform a search on the database. This looks at the stop code and stop
     * name.
     * 
     * @param term The search term.
     * @return A Cursor object as a result set.
     */
    public synchronized Cursor searchDatabase(String term) {
        term = '%' + term + '%';
        try {
            final SQLiteDatabase db = getReadableDatabase();
            final Cursor c = db.query(BUS_STOPS_TABLE, null,
                    BUS_STOPS_STOPCODE + " LIKE ? OR " + BUS_STOPS_STOPNAME +
                    " LIKE ? OR " + BUS_STOPS_LOCALITY + " LIKE ?",
                    new String[] { term, term, term }, BUS_STOPS_STOPCODE,
                    null, null);

            return c;
        } catch(SQLiteException e) {
            return null;
        }
    }
    
    /**
     * Get a listing of all known bus services in the database, as a String
     * array.
     * 
     * @return A listing of all known bus services in the database, as a String
     * array.
     */
    public synchronized String[] getBusServiceList() {
        String[] result;
        
        try {
            final SQLiteDatabase db = getReadableDatabase();
            final Cursor c = db.query(true, SERVICE_TABLE,
                    new String[] { SERVICE_SERVICE_NAME },
                    null, null, null, null,
                    "CASE WHEN " + SERVICE_SERVICE_NAME +
                    " GLOB '[^0-9.]*' THEN " + SERVICE_SERVICE_NAME +
                    " ELSE cast(" + SERVICE_SERVICE_NAME + " AS int) END",
                    null);
            final int count = c.getCount();
            if(count > 0) {
                int i = 0;
                result = new String[count];
                while(c.moveToNext()) {
                    result[i] = c.getString(0);
                    i++;
                }
            } else {
                result = new String[] { };
            }
            
            c.close();
        } catch(SQLiteException e) {
            result = new String[] { };
        }
        
        return result;
    }
    
    /**
     * Get the GeoPoint for a bus stop. The GeoPoint class can be found in
     * the Google Maps for Android API and is essentially an object which
     * encapsulates latitude and longitude for a single point.
     * 
     * @param stopCode The bus stop code to get the GeoPoint for.
     * @return A GeoPoint which specifies a latitude and longitude.
     */
    public synchronized LatLng getLatLngForStopCode(final String stopCode) {
        LatLng point = null;
        
        try {
            final SQLiteDatabase db = getReadableDatabase();
            final Cursor c = db.query(BUS_STOPS_TABLE,
                    new String[] { BUS_STOPS_X, BUS_STOPS_Y },
                    BUS_STOPS_STOPCODE + " = ?", new String[] { stopCode },
                    null, null, null);

            if(c.moveToNext()) {
                point = new LatLng(c.getDouble(0), c.getDouble(1));
            }
            
            c.close();
        } catch(SQLiteException e) {
            
        }
        
        return point;
    }
    
    /**
     * Get the locality for a given bus stop.
     * 
     * @param stopCode The bus stop code to get the locality for.
     * @return The locality of the given bus stop.
     */
    public synchronized String getLocalityForStopCode(final String stopCode) {
        String result = null;
        
        try {
            final SQLiteDatabase db = getReadableDatabase();
            final Cursor c = db.query(BUS_STOPS_TABLE,
                    new String[] { BUS_STOPS_LOCALITY },
                    BUS_STOPS_STOPCODE + " = ?", new String[] { stopCode },
                    null, null, null);
            
            if(c.moveToNext()) {
                result = c.getString(0);
            }
            
            c.close();
        } catch(SQLiteException e) {
            
        }
        
        return result;
    }
    
    /**
     * Get the name for a given bus stop.
     * 
     * @param stopCode The bus stop code to get the name for.
     * @return The name of the given bus stop.
     */
    public synchronized String getNameForBusStop(final String stopCode) {
        String result = "";
        try {
            final SQLiteDatabase db = getReadableDatabase();
            final Cursor c = db.query(BUS_STOPS_TABLE,
                    new String[] { BUS_STOPS_STOPNAME },
                    BUS_STOPS_STOPCODE + " = ?", new String[] { stopCode },
                    null, null, null);
            
            if(c.moveToNext()) {
                result = c.getString(0);
            }
            
            c.close();
        } catch(SQLiteException e) {
            
        }
        
        return result;
    }
    
    /**
     * Get a HashMap which contains the mapping of service names to service
     * colours.
     * 
     * @param serviceList A String array of services. A null or empty list will
     * mean that all services are returned.
     * @return A mapping of service names to colours.
     */
    public synchronized HashMap<String, String> getServiceColours(
            final String[] serviceList) {
        // Create the HashMap now. We may need to return an empty version later
        // if there's an error.
        final HashMap<String, String> result = new HashMap<String, String>();
        
        try {
            final SQLiteDatabase db = getReadableDatabase();
            Cursor c;
            
            if(serviceList == null || serviceList.length == 0) {
                // If the serviceList is null or empty, get all services.
                c = db.rawQuery("SELECT " +
                        SERVICE_TABLE + '.' + SERVICE_SERVICE_NAME + ", " +
                        SERVICE_COLOUR_TABLE + '.' + SERVICE_COLOUR_HEX_COLOUR +
                        " FROM " + SERVICE_COLOUR_TABLE + " LEFT JOIN " +
                        SERVICE_TABLE + " ON " +
                        SERVICE_TABLE + '.' + SERVICE_ID + " = " +
                        SERVICE_COLOUR_TABLE + '.' + SERVICE_COLOUR_ID, null);
            } else {
                // If we've been given a list, we need to construct the parts
                // of the 'IN' clause.
                // Format: 'A','B','C'
                final StringBuilder sb = new StringBuilder();
                final int len = serviceList.length;
                
                for(int i = 0; i < len; i++) {
                    if(i > 0) {
                        sb.append(',');
                    }
                    
                    sb.append('\'').append(serviceList[i]).append('\'');
                }
                
                // Android selectArgs doesn't work with IN because the arguments
                // inside the IN require their own single quotes, and Android
                // comes along and puts its own single quotes in and bad things
                // happen.
                c = db.rawQuery("SELECT " +
                        SERVICE_TABLE + '.' + SERVICE_SERVICE_NAME + ", " +
                        SERVICE_COLOUR_TABLE + '.' + SERVICE_COLOUR_HEX_COLOUR +
                        " FROM " + SERVICE_COLOUR_TABLE + " LEFT JOIN " +
                        SERVICE_TABLE + " ON " +
                        SERVICE_TABLE + '.' + SERVICE_ID + " = " +
                        SERVICE_COLOUR_TABLE + '.' + SERVICE_COLOUR_ID +
                        " WHERE " + SERVICE_TABLE + '.' + SERVICE_SERVICE_NAME +
                        " IN (" + sb.toString() + ')', null);
            }
            
            if(c != null) {
                // Loop through the Cursor, putting each element in the HashMap.
                while(c.moveToNext()) {
                    result.put(c.getString(0), c.getString(1));
                }
                
                c.close();
            }
        } catch(SQLiteException e) {
            
        }
        
        return result;
    }
    
    /**
     * Get a Cursor which contains all the known route points for a given
     * service.
     * 
     * @param serviceName The serviceName to get the route points for.
     * @return A Cursor containing the route points for the service.
     */
    public synchronized Cursor getServicePointsForService(
            final String serviceName) {
        if(serviceName == null || serviceName.length() == 0) {
            throw new IllegalArgumentException("The serviceName cannot be " +
                    "null or blank.");
        }
        
        try {
            final SQLiteDatabase db = getReadableDatabase();
            return db.rawQuery("SELECT " + SERVICE_POINT_CHAINAGE + ", " + 
                    SERVICE_POINT_LATITUDE + ", " + SERVICE_POINT_LONGITUDE +
                    " FROM " + SERVICE_POINT_TABLE + " WHERE " +
                    SERVICE_POINT_SERVICE_ID + " = (SELECT " + SERVICE_ID +
                    " FROM " + SERVICE_TABLE + " WHERE " +
                    SERVICE_SERVICE_NAME + " = ?) ORDER BY " +
                    SERVICE_POINT_CHAINAGE + " ASC, " +
                    SERVICE_POINT_ORDER_VALUE + " ASC",
                    new String[] { serviceName });
        } catch(SQLiteException e) {
            return null;
        }
    }
    
    /**
     * Return Spanned text which takes a String of service names (e.g. "1, 3, 34
     * X25, N25, N26") and adds colour where appropriate.
     * 
     * Current rules;
     * 
     * - Wrap the character 'N' with formatting to colour the character red.
     *   This is for night bus services.
     * 
     * @param serviceList A String containing a list of bus services.
     * @return Spanned text, with added formatting.
     */
    public static Spanned getColouredServiceListString(
            final String serviceList) {
        if(serviceList == null) return new SpannedString("");
        
        return Html.fromHtml(serviceList.replace("N",
                "<font color=\"red\">N</font>"));
    }
    
    /**
     * This task is run when a database needs to be restored from the assets
     * directory.
     */
    private static class RestoreDBFromAssetsTask implements Runnable {
        
        private final BusStopDatabase bsd;
        
        /**
         * Create a new task.
         * 
         * @param bsd A reference to the BusStopDatabase.
         */
        public RestoreDBFromAssetsTask(final BusStopDatabase bsd) {
            if(bsd == null) {
                throw new IllegalArgumentException("A reference to the " +
                        "BusStopDatabase must be provided.");
            }
            
            this.bsd = bsd;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            bsd.restoreDBFromAssets();
        }
    }
}