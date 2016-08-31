/*
 * Copyright (C) 2012 - 2016 Niall 'Rivernile' Scott
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

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.SearchRecentSuggestionsProvider;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;

import uk.org.rivernile.android.bustracker.database.busstop.*;
import uk.org.rivernile.android.utils.LocationUtils;

/**
 * This {@link android.content.ContentProvider} extends {@link SearchRecentSuggestionsProvider} and
 * quite frankly, abuses it. The purpose of this is to return recent search items and search
 * suggestions based on bus stops and location to the search controls.
 * 
 * @author Niall Scott
 */
public class MapSearchSuggestionsProvider extends SearchRecentSuggestionsProvider {
    
    /** The authority to use. */
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID +
            ".MapSearchSuggestionsProvider";
    
    /** The database modes. */
    public static final int MODE = DATABASE_MODE_QUERIES | DATABASE_MODE_2LINES;
    
    private static final String[] COLUMNS = new String[] {
            SearchManager.SUGGEST_COLUMN_FORMAT,
            SearchManager.SUGGEST_COLUMN_ICON_1,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_QUERY,
            BaseColumns._ID
    };
    
    private LocationManager locMan;
    
    /**
     * Create a new {@code MapSearchSuggestionsProvider}. As per the API documentation, this sets
     * up the suggestions.
     */
    public MapSearchSuggestionsProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }

    @Override
    public boolean onCreate() {
        // Get an instance of the LocationManager.
        locMan = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        
        return super.onCreate();
    }

    @Override
    public Cursor query(final Uri uri, final String[] projection, final String selection,
            final String[] selectionArgs, final String sortOrder) {
        // Get the recent search terms first, then merge later.
        final Cursor recentCursor = super.query(uri, projection, selection, selectionArgs,
                sortOrder);
        
        // If there's no selection args, then just return the recent searches cursor.
        if (selectionArgs == null || selectionArgs.length == 0 ||
                TextUtils.isEmpty(selectionArgs[0])) {
            return recentCursor;
        }

        // This is so that the _id column is unique.
        final int recentLastIndex = recentCursor != null ? recentCursor.getCount() : 0;
        
        // Merge the recent suggestions and suggestions together to form a single Cursor.
        return new MergeCursor(new Cursor[] {
                recentCursor,
                createSearchResultsCursor(getSearchedBusStops(selectionArgs[0]), recentLastIndex)
        });
    }

    @Nullable
    private ArrayList<SearchResult> getSearchedBusStops(@NonNull final String query) {
        final Location location;
        final float[] distance;

        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location = LocationUtils.getBestInitialLocation(locMan);
            distance = new float[1];
        } else {
            location = null;
            distance = null;
        }

        final Cursor c = uk.org.rivernile.android.bustracker.database.busstop.BusStopDatabase
                .searchBusStops(getContext(), query);

        if (c != null) {
            final int stopCodeColumn = c.getColumnIndex(BusStopContract.BusStops.STOP_CODE);
            final int stopNameColumn = c.getColumnIndex(BusStopContract.BusStops.STOP_NAME);
            final int latitudeColumn = c.getColumnIndex(BusStopContract.BusStops.LATITUDE);
            final int longitudeColumn = c.getColumnIndex(BusStopContract.BusStops.LONGITUDE);
            final int orientationColumn = c.getColumnIndex(BusStopContract.BusStops.ORIENTATION);
            final int localityColumn = c.getColumnIndex(BusStopContract.BusStops.LOCALITY);
            final int servicesColumn = c.getColumnIndex(BusStopContract.BusStops.SERVICE_LISTING);
            c.moveToPosition(-1);
            final ArrayList<SearchResult> results = new ArrayList<>();

            while (c.moveToNext()) {
                final SearchResult sr = new SearchResult();
                sr.stopCode = c.getString(stopCodeColumn);
                sr.stopName = c.getString(stopNameColumn);
                sr.latitude = c.getDouble(latitudeColumn);
                sr.longitude = c.getDouble(longitudeColumn);
                sr.orientation = c.getInt(orientationColumn);
                sr.locality = c.getString(localityColumn);
                sr.services = c.getString(servicesColumn);

                if (location != null) {
                    Location.distanceBetween(sr.latitude, sr.longitude, location.getLatitude(),
                            location.getLongitude(), distance);
                    sr.distance = distance[0];
                }

                results.add(sr);
            }

            c.close();
            Collections.sort(results);

            return results;
        } else {
            return null;
        }
    }

    @NonNull
    private Cursor createSearchResultsCursor(@Nullable final ArrayList<SearchResult> results,
            final int startIndex) {
        final MatrixCursor cursor = new MatrixCursor(COLUMNS);

        if (results != null && !results.isEmpty()) {
            final int size = results.size();

            for (int i = 0; i < size; i++) {
                final SearchResult sr = results.get(i);
                final String displayName = !TextUtils.isEmpty(sr.locality)
                        ? getContext().getString(R.string.busstop_locality, sr.stopName,
                                sr.locality, sr.stopCode)
                        : getContext().getString(R.string.busstop, sr.stopName, sr.stopCode);

                final int drawable;

                switch (sr.orientation) {
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

                cursor.addRow(new Object[] {
                        null,
                        "android.resource://" + getContext().getPackageName() + '/' + drawable,
                        displayName,
                        sr.services,
                        sr.stopCode,
                        (startIndex + i)
                });
            }
        }

        return cursor;
    }
    
    /**
     * A {@code SearchResult} object holds data on a bus stop or location result.
     */
    private static class SearchResult implements Comparable<SearchResult> {

        public double latitude;
        public double longitude;
        public float distance = Float.MAX_VALUE;
        public String stopCode;
        public String stopName;
        public String services;
        public int orientation;
        public String locality;

        @Override
        public int compareTo(@NonNull final SearchResult another) {
            return distance != another.distance ?
                    (distance > another.distance ? 1 : -1) : 0;
        }
    }
}