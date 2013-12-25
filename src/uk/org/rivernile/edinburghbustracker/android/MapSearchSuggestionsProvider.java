/*
 * Copyright (C) 2012 - 2013 Niall 'Rivernile' Scott
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

import android.app.SearchManager;
import android.content.Context;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This ContentProvider extends the SearchRecentSuggestionsProvider and quite
 * frankly, abuses it. The purpose of this is to return recent search items
 * and search suggestions based on bus stops and location to the search
 * controls.
 * 
 * @author Niall Scott
 */
public class MapSearchSuggestionsProvider extends
        SearchRecentSuggestionsProvider {
    
    /** The authority to use. */
    public static final String AUTHORITY = "uk.org.rivernile." +
            "edinburghbustracker.android.MapSearchSuggestionsProvider";
    
    /** The database modes. */
    public static final int MODE = DATABASE_MODE_QUERIES | DATABASE_MODE_2LINES;
    
    private static final String[] COLUMNS;
    private static final boolean SUPPORTS_ICON =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    
    private LocationManager locMan;
    
    static {
        if (SUPPORTS_ICON) {
            COLUMNS = new String[] {
                SearchManager.SUGGEST_COLUMN_FORMAT,
                SearchManager.SUGGEST_COLUMN_ICON_1,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2,
                SearchManager.SUGGEST_COLUMN_QUERY,
                BaseColumns._ID
            };
        } else {
            COLUMNS = new String[] {
                SearchManager.SUGGEST_COLUMN_FORMAT,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2,
                SearchManager.SUGGEST_COLUMN_QUERY,
                BaseColumns._ID
            };
        }
    }
    
    /**
     * Create a new MapSearchSuggestionsProvider. As per the API documentation,
     * this sets up the suggestions.
     */
    public MapSearchSuggestionsProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreate() {
        // Get an instance of the LocationManager.
        locMan = (LocationManager)getContext().getSystemService(
                Context.LOCATION_SERVICE);
        
        return super.onCreate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor query(final Uri uri, final String[] projection,
            final String selection, final String[] selectionArgs,
            final String sortOrder) {
        // Get the recent search terms first, then merge later.
        final Cursor recentCursor = super.query(uri, projection, selection,
                selectionArgs, sortOrder);
        
        // If there's no selection args, then just return the recent searches
        // cursor.
        if(selectionArgs == null || selectionArgs.length == 0 ||
                selectionArgs[0] == null || selectionArgs[0].length() == 0) {
            return recentCursor;
        }
        
        final MatrixCursor cursor = new MatrixCursor(COLUMNS);
        final String query = selectionArgs[0];
        // Get the last known device location.
        final Location location = getLastLocation();
        final BusStopDatabase bsd = BusStopDatabase.getInstance(getContext());
        final ArrayList<SearchResult> results = new ArrayList<SearchResult>();
        final StringBuilder sb = new StringBuilder();
        // The float array is for getting distances.
        float[] distance = new float[1];
        // This is so that the _id column is unique.
        final int recentLastIndex = recentCursor.getCount();
        SearchResult result;
        String locality;
        
        // Synchronize so that requests aren't made during database updates.
        synchronized(bsd) {
            final Cursor c = bsd.searchDatabase(query);
            if(c != null) {
                while(c.moveToNext()) {
                    // Populate bus stop records.
                    result = new SearchResult();
                    
                    result.stopCode = c.getString(1);
                    result.latitude = c.getDouble(3);
                    result.longitude = c.getDouble(4);
                    result.orientation = c.getInt(5);
                    locality = c.getString(6);
                    result.services = bsd.getBusServicesForStopAsString(
                            result.stopCode);
                    if(location != null) {
                        // If the location is known, get the distance to the bus
                        // stop.
                        Location.distanceBetween(result.latitude,
                                result.longitude, location.getLatitude(),
                                location.getLongitude(), distance);
                        result.distance = distance[0];
                    }
                    
                    // If there's locality information, append it.
                    if(locality != null) {
                        result.stopName = c.getString(2) + ", " + locality;
                    } else {
                        result.stopName = c.getString(2);
                    }
                    
                    results.add(result);
                }
                
                c.close();
            }
        }
        
        // Sort the list by distance ascending.
        Collections.sort(results);
        
        final int count = results.size();
        Object[] row;
        int drawable;
        for(int i = 0; i < count; i++) {
            // Loop though all of the results and add them to the MatrixCursor.
            result = results.get(i);

            sb.append(result.stopName).append(' ').append('(')
                    .append(result.stopCode).append(')');
            
            if (SUPPORTS_ICON) {
                switch(result.orientation) {
                    case 1:
                        drawable = R.drawable.ic_map_busstopne;
                        break;
                    case 2:
                        drawable = R.drawable.ic_map_busstope;
                        break;
                    case 3:
                        drawable = R.drawable.ic_map_busstopse;
                        break;
                    case 4:
                        drawable = R.drawable.ic_map_busstops;
                        break;
                    case 5:
                        drawable = R.drawable.ic_map_busstopsw;
                        break;
                    case 6:
                        drawable = R.drawable.ic_map_busstopw;
                        break;
                    case 7:
                        drawable = R.drawable.ic_map_busstopnw;
                        break;
                    case 0:
                    default:
                        drawable = R.drawable.ic_map_busstopn;
                        break;
                }

                row = new Object[] {
                    null,
                    "android.resource://" + getContext().getPackageName() +
                        '/' + drawable,
                    sb.toString(),
                    result.services,
                    result.stopCode,
                    (recentLastIndex + i)
                };
            } else {
                row = new Object[] {
                    null,
                    sb.toString(),
                    result.services,
                    result.stopCode,
                    (recentLastIndex + i)
                };
            }

            cursor.addRow(row);
            sb.setLength(0);
        }
        
        // Merge the recent suggestions and suggestions together to form a
        // single Cursor.
        return new MergeCursor(new Cursor[] { recentCursor, cursor });
    }
    
    /**
     * Get the last known location of the device.
     * 
     * @return The last known location of the device.
     */
    private Location getLastLocation() {
        final List<String> providers = locMan.getAllProviders();
        Location temp, bestLocation = null;
        
        // Loop through all location providers.
        for(String provider : providers) {
            temp = locMan.getLastKnownLocation(provider);
            
            // If there's no best location, set the best location to be that of
            // the current provider.
            if(bestLocation == null) {
                bestLocation = temp;
            } else {
                if(temp != null) {
                    // If this provider's location is newer than the best
                    // provider's, then go with that.
                    if(temp.getTime() > bestLocation.getTime()) {
                        bestLocation = temp;
                    }
                }
            }
        }
        
        return bestLocation;
    }
    
    /**
     * A SearchResult object holds data on a bus stop or location result.
     */
    private static class SearchResult implements Comparable<SearchResult> {
        
        public byte type;
        public double latitude;
        public double longitude;
        public float distance = Float.MAX_VALUE;
        public String stopCode;
        public String stopName;
        public String services;
        public int orientation;
        
        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(final SearchResult another) {
            // Order by distance ascending.
            if(distance == another.distance) return 0;
            
            if(distance > another.distance) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}