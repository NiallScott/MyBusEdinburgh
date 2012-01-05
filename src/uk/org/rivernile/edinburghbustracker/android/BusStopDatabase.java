/*
 * Copyright (C) 2009 - 2011 Niall 'Rivernile' Scott
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
import com.google.android.maps.GeoPoint;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class deals with the handling of the Bus Stop Database. It deals with
 * the initialisation as well as instance creation. Get an instance of this
 * class from the getInstance() method.
 * 
 * @author Niall Scott
 */
public final class BusStopDatabase extends SQLiteOpenHelper {

    /** The name of the database. */
    public static final String STOP_DB_NAME = "busstops2.db";
    /** This is the schema name of the database. */
    public static final String SCHEMA_NAME = "MBE_5";
    /** The version of the database. For internal use only. */
    protected static final int STOP_DB_VERSION = 1;

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
            restoreDBFromAssets();
        } else {
            long assetVersion = Long.parseLong(context.getString(
                    R.string.asset_db_version));
            long currentVersion = 0;
            try {
                currentVersion = getLastDBModTime();
            } catch(SQLiteException e) {
                f.delete();
                restoreDBFromAssets();
                return;
            }

            if(assetVersion > currentVersion) {
                f.delete();
                restoreDBFromAssets();
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
     * {@inheritDoc}
     */
    @Override
    protected void finalize() {
        getReadableDatabase().close();
    }
    
    /**
     * Move a copy of the database from the application assets in to the
     * application's database path.
     * 
     * @return True if the operation was successful, otherwise return false.
     */
    private boolean restoreDBFromAssets() {
        try {
            // Start of horrible hack to create database directory and
            // set permissions if it doesn't already exist.
            SQLiteDatabase db = context.openOrCreateDatabase(STOP_DB_NAME, 0,
                    null);
            db.close();
            // End of horrible hack.
            InputStream in = context.getAssets().open(STOP_DB_NAME);
            FileOutputStream out = new FileOutputStream(f);
            byte[] buf = new byte[1024];
            int len;
            while((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();
            out.close();
            in.close();
            return true;
        } catch(IOException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // The database should already exist, do nothing if it doesn't.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
            final int newVersion)
    {
        // Do nothing.
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
    public Cursor getBusStopsByCoords(final int minX, final int minY,
            final int maxX, final int maxY)
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("bus_stops", null, "x <= " + minX + " AND y >= " +
                minY + " AND x >= " + maxX + " AND y <= " + maxY, null, null,
                null, null);
        return c;
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
    public Cursor getFilteredStopsByCoords(final int minX, final int minY,
            final int maxX, final int maxY, final String filter) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT bus_stops._id, bus_stops.stopName, " +
	    "bus_stops.x, bus_stops.y FROM service_stops LEFT JOIN bus_stops " +
            "ON service_stops.stopCode = bus_stops._id WHERE " +
            "service_stops.serviceName IN (" + filter + ") AND x <= " + minX +
            " AND y >= " + minY + " AND x >= " + maxX + " AND y <= " + maxY +
            " GROUP BY bus_stops._id", null);
        return c;
    }

    /**
     * Return a result set for a bus stop based on it's stop code.
     * 
     * @param stopCode The bus stop code to query for.
     * @return A Cursor result set.
     */
    public Cursor getBusStopByCode(final String stopCode) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.query("bus_stops", null, "_id = " + stopCode, null,
                null, null, null);
        return c;
    }

    /**
     * Get a String array of bus services which serve a particular bus stop.
     * 
     * @param stopCode The bus stop code to search for.
     * @return A String array of bus services.
     */
    public String[] getBusServicesForStop(final String stopCode) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(true, "service_stops",
                new String[] { "serviceName" }, "stopCode = " + stopCode, null,
                null, null, "CASE WHEN serviceName GLOB '[^0-9.]*' THEN " +
                "serviceName ELSE cast(serviceName AS int) END", null);
        int count = c.getCount();
        int i = 0;
        if(count > 0) {
            String[] result = new String[count];
            while(!c.isLast()) {
                c.moveToNext();
                result[i] = c.getString(0);
                i++;
            }
            c.close();
            return result;
        } else {
            c.close();
            return null;
        }
    }
    
    /**
     * A convenience method for getBusServicesForStop() which returns a String
     * formatted as a comma separated list of bus services, for example;
     * 1, 2, 3, 3A, 4, 100, X48
     * 
     * @param stopCode The bus stop code to search for.
     * @return A comma separated list bus services.
     */
    public String getBusServicesForStopAsString(final String stopCode) {
        String[] services = getBusServicesForStop(stopCode);
        if(services == null) return "";
        
        StringBuilder builder = new StringBuilder();
        int len = services.length;
        
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
    public long getLastDBModTime() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(true, "database_info",
                new String[] { "updateTS" }, null, null, null, null, null,
                null);
        
        if(c.moveToNext()) {
            String result = c.getString(0);
            c.close();
            try {
                return Long.parseLong(result);
            } catch(NumberFormatException e) {
                return 0;
            }
        } else {
            c.close();
            return 0;
        }
    }
    
    /**
     * Get the current topology ID. This is a string which uniquely identifies
     * the version of the bus stop data the database is using.
     * 
     * @return The current topology ID.
     */
    public String getTopoId() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(true, "database_info",
                new String[] { "current_topo_id" }, null, null, null, null,
                null, null);
        String result = "";
        if(c.moveToNext()) {
            result = c.getString(0);
        }
        c.close();
        
        return result;
    }

    /**
     * Perform a search on the database. This looks at the stop code and stop
     * name.
     * 
     * @param term The search term.
     * @return A Cursor object as a result set.
     */
    public Cursor searchDatabase(final String term) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("bus_stops", null, "_id LIKE \"%" + term + "%\"" +
                " OR stopName LIKE \"%" + term + "%\"", null, null, null, null);

        return c;
    }
    
    /**
     * Get a listing of all known bus services in the database, as a String
     * array.
     * 
     * @return A listing of all known bus services in the database, as a String
     * array.
     */
    public String[] getBusServiceList() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(true, "service_stops",
                new String[] { "serviceName" }, null, null, null, null,
                "CASE WHEN serviceName GLOB '[^0-9.]*' THEN serviceName " +
                "ELSE cast(serviceName AS int) END", null);
        int count = c.getCount();
        int i = 0;
        if(count > 0) {
            String[] result = new String[count];
            while(!c.isLast()) {
                c.moveToNext();
                result[i] = c.getString(0);
                i++;
            }
            c.close();
            return result;
        } else {
            c.close();
            return new String[] { };
        }
    }
    
    /**
     * Get the GeoPoint for a bus stop. The GeoPoint class can be found in
     * the Google Maps for Android API and is essentially an object which
     * encapsulates latitude and longitude for a single point.
     * 
     * @param stopCode The bus stop code to get the GeoPoint for.
     * @return A GeoPoint which specifies a latitude and longitude.
     */
    public GeoPoint getGeoPointForStopCode(final String stopCode) {
        GeoPoint gp = null;
        
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("bus_stops", new String[] { "x", "y" },
                "_id = " + stopCode, null, null, null, null);
        
        if(c.getCount() == 0) return null;
        c.moveToNext();
        gp = new GeoPoint(c.getInt(0), c.getInt(1));
        c.close();
        return gp;
    }
    
    /**
     * Get the name for a given bus stop.
     * 
     * @param stopCode The bus stop code to get the name for.
     * @return The name of the given bus stop.
     */
    public String getNameForBusStop(final String stopCode) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("bus_stops", new String[] { "stopName" },
                "_id = " + stopCode, null, null, null, null);
        if(c.getCount() == 0) return null;
        c.moveToNext();
        String result = c.getString(0);
        c.close();
        return result;
    }
}