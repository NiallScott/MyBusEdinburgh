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
            BusStopDatabase bsd = BusStopDatabase.getInstance(mContext);
            Cursor c = bsd.searchDatabase(searchTerm);
            if(c.getCount() > 0) {
                while(!c.isLast()) {
                    c.moveToNext();
                    searchResults.add(new SearchResult(
                            new GeoPoint(c.getInt(2), c.getInt(3)),
                            SearchResult.TYPE_STOP, c.getString(1)));
                }
            }
            c.close();

            Geocoder geo = new Geocoder(mContext);
            try {
                List<Address> addrs = geo.getFromLocationName(searchTerm, 50,
                    55.480057, 3.271144, 56.014467, 2.533221);
                for(Address a : addrs) {
                    searchResults.add(new SearchResult(new GeoPoint(
                            (int)(a.getLatitude() * 1000000),
                            (int)(a.getLongitude() * 1000000)),
                            SearchResult.TYPE_ADDRESS, a.getAddressLine(0) +
                            ", " + a.getLocality() + ", " + a.getPostalCode()));
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

    public class SearchResult {

        public static final int TYPE_STOP = 0;
        public static final int TYPE_ADDRESS = 1;

        public GeoPoint geoPoint;
        public int type;
        public String description;

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
            if(convertView == null) {
                LayoutInflater vi = (LayoutInflater)mContext.getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.map_search_results_list_item,
                        null);
            }

            ImageView iv = (ImageView)convertView.findViewById(
                    R.id.imgMapSearchIcon);
            SearchResult sr = getItem(position);
            if(sr.type == SearchResult.TYPE_STOP) {
                iv.setImageResource(R.drawable.mapmarker);
            } else {
                // TODO: sort image
            }
            TextView tv = (TextView)convertView.findViewById(
                    R.id.txtMapSearchResult);
            tv.setText(sr.description);

            return convertView;
        }
    }
}