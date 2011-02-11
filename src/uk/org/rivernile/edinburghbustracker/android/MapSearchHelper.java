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
package uk.org.rivernile.edinburghbustracker.android;

import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Message;
import android.provider.SearchRecentSuggestions;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class MapSearchHelper {

    private static final int EVENT_SEARCH_READY = 1;
    private static final int EVENT_NO_RESULTS = 2;

    private static MapSearchHelper instance = null;

    public Thread searchThread;

    private BusStopMapActivity mContext;
    private String searchTerm;
    protected SearchResultsArrayAdapter searchResults;

    private MapSearchHelper() {

    }

    public static MapSearchHelper getInstance(
            final BusStopMapActivity context) {
        if(instance == null) instance = new MapSearchHelper();
        instance.mContext = context;
        return instance;
    }

    public void doSearch(final String searchTerm) {
        this.searchTerm = searchTerm;
        if(searchResults == null)
            searchResults = new SearchResultsArrayAdapter();
        searchResults.clear();

        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
                mContext, MapSearchHistoryProvider.AUTHORITY,
                MapSearchHistoryProvider.MODE);
        suggestions.saveRecentQuery(searchTerm, null);

        mContext.showDialog(BusStopMapActivity.DIALOG_PROGRESS);
        searchThread = new Thread(searchTask);
        searchThread.start();
    }

    private Runnable searchTask = new Runnable() {
        @Override
        public void run() {
            GeoPoint myLocation = mContext.myLocation.getMyLocation();
            GeoPoint pointLocation;
            SearchResult result;
            BusStopDatabase bsd = BusStopDatabase.getInstance(mContext);
            Cursor c = bsd.searchDatabase(searchTerm);
            if(c.getCount() > 0) {
                while(!c.isLast()) {
                    c.moveToNext();
                    pointLocation = new GeoPoint(c.getInt(2), c.getInt(3));
                    result = new SearchResult(pointLocation,
                            SearchResult.TYPE_STOP, c.getString(0) + " " +
                            c.getString(1));
                    if(myLocation != null)
                        result.distance = calculateGeographicalDistance(
                                myLocation, pointLocation);
                    searchResults.add(result);
                }
            }
            c.close();

            Geocoder geo = new Geocoder(mContext);
            try {
                List<Address> addrs = geo.getFromLocationName(searchTerm, 50,
                    55.819447, -3.403363, 56.000000, -2.864143);
                String locLine;
                for(Address a : addrs) {
                    locLine = a.getFeatureName();
                    if(a.getLocality() != null) locLine += ", " +
                            a.getLocality();
                    if(a.getPostalCode() != null) locLine += ", " +
                            a.getPostalCode();
                    pointLocation = new GeoPoint((int)(a.getLatitude() * 1E6),
                            (int)(a.getLongitude() * 1E6));
                    result = new SearchResult(pointLocation,
                            SearchResult.TYPE_ADDRESS, locLine);
                    if(myLocation != null)
                        result.distance = calculateGeographicalDistance(
                                myLocation, pointLocation);
                    searchResults.add(result);
                    searchResults.sort(mDistanceCompare);
                }
            } catch(IOException e) {
            } finally {
                mContext.dismissDialog(BusStopMapActivity.DIALOG_PROGRESS);
                if(!searchThread.isInterrupted()) {
                    if(searchResults.getCount() > 0) {
                        mHandler.sendEmptyMessage(EVENT_SEARCH_READY);
                    } else {
                        mHandler.sendEmptyMessage(EVENT_NO_RESULTS);
                    }
                }
            }
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            switch(msg.what) {
                case EVENT_SEARCH_READY:
                    mContext.showDialog(BusStopMapActivity
                            .DIALOG_SEARCH_RESULTS);
                    break;
                case EVENT_NO_RESULTS:
                    Toast.makeText(mContext,
                            R.string.map_search_dialog_noresults,
                            Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };

    private Comparator<SearchResult> mDistanceCompare =
            new Comparator<SearchResult>() {
        @Override
        public int compare(final SearchResult a, final SearchResult b) {
            // Because the type of distance is a double and the granularity
            // does matter to us, we cannot just do the trick of
            // return a.distance - b.distance;
            // We return 0 if they are equal, positive if
            // a.distance > b.distance and negative if a.distance < b.distance
            if(a.distance == b.distance) return 0;

            if(a.distance > b.distance) {
                return 1;
            } else {
                return -1;
            }
        }
    };

    /**
     * This is a slightly modified implementation of the Haversine formula to
     * calculate the distance between two points on the globe.
     *
     * @param start The starting point.
     * @param end The end point.
     * @return The distance, in km, between two points on the Earth.
     */
    public static double calculateGeographicalDistance(final GeoPoint start,
            final GeoPoint end) {
        if(start == null || end == null) return 0;

        double startLat = start.getLatitudeE6() / 1E6;
        double endLat = end.getLatitudeE6() / 1E6;
        double startLong = start.getLongitudeE6() / 1E6;
        double endLong = end.getLongitudeE6() / 1E6;
        double deltaLat = Math.toRadians(endLat - startLat);
        double deltaLong = Math.toRadians(endLong - startLong);

        // Here comes a nice looking piece of code.
        return 6371 * (2 * Math.asin(Math.sqrt(Math.sin(deltaLat / 2) *
                Math.sin(deltaLat / 2) + Math.cos(Math.toRadians(startLat)) *
                Math.cos(Math.toRadians(endLat)) * Math.sin(deltaLong / 2) *
                Math.sin(deltaLong / 2))));
    }

    public class SearchResult {

        public static final int TYPE_STOP = 0;
        public static final int TYPE_ADDRESS = 1;

        public GeoPoint geoPoint;
        public int type;
        public String description;
        public double distance = 0;

        public SearchResult(final GeoPoint geoPoint, final int type,
                final String description)
        {
            this.geoPoint = geoPoint;
            this.type = type;
            this.description = description;
        }
    }

    public class SearchResultsArrayAdapter extends ArrayAdapter<SearchResult> {

        public SearchResultsArrayAdapter() {
            super(mContext, R.layout.map_search_results_list_item,
                    R.id.txtMapSearchResult);
        }

        @Override
        public View getView(final int position, View convertView,
                final ViewGroup parent) {
            LayoutInflater vi = (LayoutInflater)mContext.getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
            View row = vi.inflate(R.layout.map_search_results_list_item, null);

            TextView tv = (TextView)row.findViewById(R.id.txtMapSearchResult);
            SearchResult sr = getItem(position);
            tv.setText(sr.description);

            ImageView iv = (ImageView)row.findViewById(R.id.imgMapSearchIcon);
            if(sr.type == SearchResult.TYPE_STOP) {
                iv.setImageResource(R.drawable.mapmarker);
            } else {
                iv.setImageResource(R.drawable.house);
            }

            return row;
        }
    }
}