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
import android.os.AsyncTask;
import android.provider.SearchRecentSuggestions;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MapSearchHelper {

    private static MapSearchHelper instance = null;

    private BusStopMapActivity mContext;
    private SearchRecentSuggestions suggestions;
    private BusStopDatabase bsd;
    private DoSearchTask task;
    protected SearchResultsArrayAdapter searchResults;

    private MapSearchHelper(final Context context) {
        searchResults = new SearchResultsArrayAdapter(context
                .getApplicationContext());
        suggestions = new SearchRecentSuggestions(context
                .getApplicationContext(),
                MapSearchHistoryProvider.AUTHORITY,
                MapSearchHistoryProvider.MODE);
        bsd = BusStopDatabase.getInstance(context);
    }

    public static MapSearchHelper getInstance(
            final BusStopMapActivity context) {
        if(instance == null) instance = new MapSearchHelper(context);
        instance.mContext = context;
        return instance;
    }

    public void doSearch(final String searchTerm) {
        searchResults.clear();
        suggestions.saveRecentQuery(searchTerm, null);

        mContext.showDialog(BusStopMapActivity.DIALOG_PROGRESS);
        task = new DoSearchTask();
        task.execute(new String[] { searchTerm });
    }
    
    public void cancel() {
        if(task != null) {
            task.cancel(true);
        }
    }
    
    private class DoSearchTask extends AsyncTask<String, Void,
            ArrayList<SearchResult>> {
        
        @Override
        protected ArrayList<SearchResult> doInBackground(final String... args) {
            if(args.length != 1)
                throw new IllegalArgumentException("There must only be one " +
                        "search term.");
            
            ArrayList<SearchResult> res = new ArrayList<SearchResult>();
            GeoPoint myLocation = mContext.myLocation.getMyLocation();
            GeoPoint pointLocation;
            SearchResult result;
            
            Cursor c = bsd.searchDatabase(args[0]);
            String stopCode;
            while(c.moveToNext()) {
                pointLocation = new GeoPoint(c.getInt(2), c.getInt(3));
                stopCode = c.getString(0);
                result = new SearchResult(pointLocation,
                        SearchResult.TYPE_STOP, c.getString(1) + " (" +
                        stopCode + ")");
                result.stopCode = stopCode;
                result.services = bsd.getBusServicesForStopAsString(stopCode);
                if(myLocation != null)
                    result.distance = calculateGeographicalDistance(
                            myLocation, pointLocation);
                res.add(result);
            }
            c.close();

            Geocoder geo = new Geocoder(mContext);
            try {
                List<Address> addrs = geo.getFromLocationName(args[0], 50,
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
                    res.add(result);
                }
            } catch(IOException e) { }
            
            return res;
        }
        
        @Override
        protected void onPostExecute(final ArrayList<SearchResult> result) {
            mContext.dismissDialog(BusStopMapActivity.DIALOG_PROGRESS);
            
            if(result.isEmpty()) {
                Toast.makeText(mContext, R.string.map_search_dialog_noresults,
                        Toast.LENGTH_LONG).show();
            } else {
                for(SearchResult sr : result) {
                    searchResults.add(sr);
                }
                
                searchResults.sort(mDistanceCompare);
                mContext.showDialog(BusStopMapActivity.DIALOG_SEARCH_RESULTS);
            }
        }
        
        @Override
        protected void onCancelled() {
            task = null;
        }
    }

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
        public String stopCode;
        public String services;

        public SearchResult(final GeoPoint geoPoint, final int type,
                final String description)
        {
            this.geoPoint = geoPoint;
            this.type = type;
            this.description = description;
        }
    }

    public class SearchResultsArrayAdapter extends ArrayAdapter<SearchResult> {

        private LayoutInflater vi;

        public SearchResultsArrayAdapter(final Context context) {
            super(context, R.layout.map_search_results_list_item1,
                    android.R.id.text1);

            vi = (LayoutInflater)context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, final View convertView,
                final ViewGroup parent) {
            SearchResult sr = getItem(position);
            
            TextView text1, text2 = null;
            
            if(convertView != null) {
                text2 = (TextView)convertView.findViewById(android.R.id.text2);
            }
            
            View row = null;
            
            switch(sr.type) {
                case SearchResult.TYPE_ADDRESS:
                    if(text2 == null) {
                        if(convertView != null) row = convertView;
                    } else {
                        row = vi.inflate(R.layout.map_search_results_list_item1,
                                parent, false);
                    }
                    
                    text1 = (TextView)row.findViewById(android.R.id.text1);
                    text1.setText(sr.description);
                    break;
                case SearchResult.TYPE_STOP:
                    if(text2 == null) {
                        row = vi.inflate(R.layout.map_search_results_list_item2,
                                parent, false);
                        text2 = (TextView)row.findViewById(android.R.id.text2);
                    } else {
                        row = convertView;
                    }
                    
                    text2.setText(sr.services);
                    
                    text1 = (TextView)row.findViewById(android.R.id.text1);
                    text1.setText(sr.description);
                    break;
                default:
                    break;
            }
            
            return row;
        }
    }
}