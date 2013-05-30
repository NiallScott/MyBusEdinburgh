/*
 * Copyright (C) 2011 - 2013 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android.fragments.general;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.Spanned;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.org.rivernile.android.utils.LocationUtils;
import uk.org.rivernile.android.utils.SimpleResultLoader;
import uk.org.rivernile.edinburghbustracker.android
        .AddEditFavouriteStopActivity;
import uk.org.rivernile.edinburghbustracker.android.AddProximityAlertActivity;
import uk.org.rivernile.edinburghbustracker.android.AddTimeAlertActivity;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;
import uk.org.rivernile.edinburghbustracker.android.BusStopMapActivity;
import uk.org.rivernile.edinburghbustracker.android.DisplayStopDataActivity;
import uk.org.rivernile.edinburghbustracker.android.PreferencesActivity;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.edinburghbustracker.android.SettingsDatabase;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .DeleteFavouriteDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .DeleteProximityAlertDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .DeleteTimeAlertDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .ServicesChooserDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .TurnOnGpsDialogFragment;

/**
 * Show a list of the nearest bus stops to the handset. If a location could not
 * be found or the user is too far away, an error message will be shown.
 * The user is able to filter the shown bus stops by what bus services stop
 * there. Long pressing on a bus stop shows a context menu where the user can
 * perform various actions on that stop. Tapping the stop shows bus times for
 * that stop.
 * 
 * @author Niall Scott
 */
public class NearestStopsFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<
        ArrayList<NearestStopsFragment.SearchResult>>,
        LocationListener, ServicesChooserDialogFragment.EventListener {
    
    private static final String DELETE_FAV_DIALOG_TAG = "deleteFavDialog";
    private static final String DELETE_TIME_ALERT_DIALOG_TAG =
            "delTimeAlertDialog";
    private static final String DELETE_PROX_ALERT_DIALOG_TAG =
            "delProxAlertDialog";
    private static final String TURN_ON_GPS_DIALOG_TAG = "turnOnGpsDialog";
    private static final String SERVICES_CHOOSER_DIALOG_TAG =
            "servicesChooserDialogTag";
    
    private static final int REQUEST_PERIOD = 10000;
    private static final float MIN_DISTANCE = 3.0f;
    
    private LocationManager locMan;
    private SharedPreferences sp;
    private BusStopDatabase bsd;
    private SettingsDatabase sd;
    private Location lastLocation;
    private SearchResult selectedStop;
    
    private ServicesChooserDialogFragment servicesChooser;
    private NearestStopsArrayAdapter ad;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Tell the underlying Activity to retain the instance during
        // configuration changes.
        setRetainInstance(true);
        // Tell the underlying Activity that this Fragment contains an options
        // menu.
        setHasOptionsMenu(true);
        
        final Activity activity = getActivity();
        // Get references to required resources.
        locMan = (LocationManager)activity
                .getSystemService(Context.LOCATION_SERVICE);
        sp = activity.getSharedPreferences(PreferencesActivity.PREF_FILE, 0);
        bsd = BusStopDatabase.getInstance(activity.getApplicationContext());
        sd = SettingsDatabase.getInstance(activity.getApplicationContext());
        // Create the ArrayAdapter for the ListView.
        ad = new NearestStopsArrayAdapter(activity);
        
        // Initialise the services chooser Dialog.
        servicesChooser = ServicesChooserDialogFragment.newInstance(
                bsd.getBusServiceList(),
                getString(R.string.neareststops_service_chooser_title), this);
        
        // Check to see if GPS is enabled then check to see if the GPS prompt
        // dialog has been disabled.
        if(!locMan.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                !sp.getBoolean(PreferencesActivity.PREF_DISABLE_GPS_PROMPT,
                false)) {
            // Get the list of Activities which can handle the enabling of
            // location services.
            final List<ResolveInfo> packages = activity.getPackageManager()
                    .queryIntentActivities(
                    TurnOnGpsDialogFragment.TURN_ON_GPS_INTENT, 0);
            // If the list is not empty, this means Activities do exist. Show
            // Dialog asking users if they want to turn on GPS.
            if(packages != null && !packages.isEmpty()) {
                new TurnOnGpsDialogFragment().show(getFragmentManager(),
                    TURN_ON_GPS_DIALOG_TAG);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.neareststops, container, false);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        // The ListView items can show a context menu.
        registerForContextMenu(getListView());
        // Set the ListView adapter.
        setListAdapter(ad);
        
        // Initialise the lastLocation to the best known location.
        lastLocation = LocationUtils.getBestInitialLocation(locMan);
        
        // Force an update to initially show data.
        doUpdate();
        
        // If the services chooser Dialog is showing, make sure its listener is
        // updated.
        final ServicesChooserDialogFragment servicesDialog =
                (ServicesChooserDialogFragment)getFragmentManager()
                        .findFragmentByTag(SERVICES_CHOOSER_DIALOG_TAG);
        if(servicesDialog != null) {
            servicesDialog.setListener(this);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        
        // Start the location providers if they are enabled.
        if(locMan.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    REQUEST_PERIOD, MIN_DISTANCE, this);
        }
        
        if(locMan.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    REQUEST_PERIOD, MIN_DISTANCE, this);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();
        
        // When the Activity is being paused, cancel location updates.
        locMan.removeUpdates(this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateOptionsMenu(final Menu menu,
            final MenuInflater inflater) {
        // Inflate the menu.
        inflater.inflate(R.menu.neareststops_option_menu, menu);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch(item.getItemId()) {
            case R.id.neareststops_option_menu_filter:
                // Show the services chooser Dialog.
                servicesChooser.show(getFragmentManager(),
                        SERVICES_CHOOSER_DIALOG_TAG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
            final ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        
        // Get the MenuInflater.
        final MenuInflater inflater = getActivity().getMenuInflater();
        // Cast the menuInfo object to something we understand.
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
        // Get the item relating to the selected item.
        selectedStop = ad.getItem(info.position);

        final String name;
        
        if(selectedStop.locality != null) {
            name = getString(R.string.busstop_locality,
                    selectedStop.stopName, selectedStop.locality,
                        selectedStop.stopCode);
        } else {
            name = getString(R.string.busstop, selectedStop.stopName,
                        selectedStop.stopCode);
        }
        
        // Set the title of the context menu.
        menu.setHeaderTitle(name);
        
        // Inflate the menu from XML.
        inflater.inflate(R.menu.neareststops_context_menu, menu);
        
        // Title depends on whether it's already a favourite or not.
        MenuItem item = menu.findItem(R.id.neareststops_context_menu_favourite);
        if(sd.getFavouriteStopExists(selectedStop.stopCode)) {
            item.setTitle(R.string.neareststops_context_remasfav);
        } else {
            item.setTitle(R.string.neareststops_context_addasfav);
        }
        
        // Title depends on whether a proximity alert has already been added or
        // not.
        item = menu.findItem(R.id.neareststops_context_menu_prox_alert);
        if(sd.isActiveProximityAlert(selectedStop.stopCode)) {
            item.setTitle(R.string.neareststops_menu_prox_rem);
        } else {
            item.setTitle(R.string.neareststops_menu_prox_add);
        }
        
        // Title depends on whether a time alert has already been added or not.
        item = menu.findItem(R.id.neareststops_context_menu_time_alert);
        if(sd.isActiveTimeAlert(selectedStop.stopCode)) {
            item.setTitle(R.string.neareststops_menu_time_rem);
        } else {
            item.setTitle(R.string.neareststops_menu_time_add);
        }
        
        // If the Google Play Services is not available, then don't show the
        // option to show the stop on the map.
        item = menu.findItem(R.id.neareststops_context_menu_showonmap);
        
        if(GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getActivity()) !=
                    ConnectionResult.SUCCESS) {
            item.setVisible(false);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        // Make sure that the selectedStop exists.
        if(selectedStop == null) return false;
        Intent intent;
        
        switch(item.getItemId()) {
            case R.id.neareststops_context_menu_favourite:
                // See if this stop exists as a favourite already.
                if(sd.getFavouriteStopExists(selectedStop.stopCode)) {
                    // If exists, show the DeleteFavouriteDialogFragment.
                    DeleteFavouriteDialogFragment.newInstance(
                            selectedStop.stopCode, null)
                            .show(getFragmentManager(), DELETE_FAV_DIALOG_TAG);
                } else {
                    // If it doesn't exist, show the
                    // AddEditFavouriteStopAcitivity.
                    intent = new Intent(getActivity(),
                            AddEditFavouriteStopActivity.class);
                    intent.putExtra(AddEditFavouriteStopActivity.ARG_STOPCODE,
                            selectedStop.stopCode);
                    if(selectedStop.locality != null) {
                        intent.putExtra(
                                AddEditFavouriteStopActivity.ARG_STOPNAME,
                                selectedStop.stopName + ", " +
                                    selectedStop.locality);
                    } else {
                        intent.putExtra(
                                AddEditFavouriteStopActivity.ARG_STOPNAME,
                                selectedStop.stopName);
                    }
                    startActivity(intent);
                }
                
                return true;
            case R.id.neareststops_context_menu_prox_alert:
                // See if this stop exists as a proximity alert.
                if(sd.isActiveProximityAlert(selectedStop.stopCode)) {
                    // If exists, show the DeleteProximityAlertDialogFragment.
                    DeleteProximityAlertDialogFragment.newInstance(null)
                            .show(getFragmentManager(),
                            DELETE_PROX_ALERT_DIALOG_TAG);
                } else {
                    // If it doesn't exist, show the AddProximityAlertActivity.
                    intent = new Intent(getActivity(),
                            AddProximityAlertActivity.class);
                    intent.putExtra(AddProximityAlertActivity.ARG_STOPCODE,
                            selectedStop.stopCode);
                    startActivity(intent);
                }
                
                return true;
            case R.id.neareststops_context_menu_time_alert:
                // See if this stop exists as a time alert.
                if(sd.isActiveTimeAlert(selectedStop.stopCode)) {
                    // If exists, show the DeleteTimeAlertDialogFragment.
                    DeleteTimeAlertDialogFragment.newInstance(null)
                            .show(getFragmentManager(), 
                            DELETE_TIME_ALERT_DIALOG_TAG);
                } else {
                    // If it doesn't exist, show the AddTimeAlertActivity.
                    intent = new Intent(getActivity(),
                            AddTimeAlertActivity.class);
                    intent.putExtra(AddTimeAlertActivity.ARG_STOPCODE,
                            selectedStop.stopCode);
                    startActivity(intent);
                }
                
                return true;
            case R.id.neareststops_context_menu_showonmap:
                // Start the BusStopMapActivity, giving it a stopCode and zoom
                // level to center upon.
                intent = new Intent(getActivity(), BusStopMapActivity.class);
                intent.putExtra(BusStopMapActivity.ARG_STOPCODE,
                        selectedStop.stopCode);
                startActivity(intent);
                
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Loader<ArrayList<SearchResult>> onCreateLoader(final int id,
            final Bundle args) {
        // Create a new Loader.
        return new NearestStopsLoader(getActivity(), args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoadFinished(final Loader<ArrayList<SearchResult>> loader,
            final ArrayList<SearchResult> results) {
        final ListView lv = getListView();
        // Get the first visible position so the scroll position is restored
        // later.
        int currentIndex = lv.getFirstVisiblePosition();
        final View v = lv.getChildAt(0);
        
        // When loading has finished, clear the ArrayAdapter and add in all the
        // new results.
        ad.clear();
        ad.addAll(results);
        
        final int lastIndex = results.size() - 1;
        
        if(lastIndex < currentIndex) {
            // If the final index is less than the index before, then scroll to
            // the new final index.
            lv.setSelectionFromTop(lastIndex, 0);
        } else {
            // Otherwise, go to the previous exact scroll position.
            lv.setSelectionFromTop(currentIndex,
                            (v == null) ? 0 : v.getTop());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoaderReset(final Loader<ArrayList<SearchResult>> loader) {
        // If the Loader has been reset, clear the SearchResults.
        ad.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLocationChanged(final Location location) {
        // When the location has changed, cache the new location and force an
        // update.
        if(LocationUtils.isBetterLocation(location, lastLocation)) {
            lastLocation = location;
            doUpdate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusChanged(final String provider, final int status,
            final Bundle extras) {
        // Nothing to do here.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onProviderEnabled(final String provider) {
        if (LocationManager.GPS_PROVIDER.equals(provider) ||
                LocationManager.NETWORK_PROVIDER.equals(provider)) {
            locMan.requestLocationUpdates(provider, REQUEST_PERIOD,
                    MIN_DISTANCE, this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onProviderDisabled(final String provider) {
        // Nothing to do here.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onServicesChosen() {
        // The user has been in the services chooser Dialog, so force an update
        // incase anything has changed.
        doUpdate();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onListItemClick(final ListView l, final View v,
            final int position, final long id) {
        // Ensure that the position is within range.
        if(position < ad.getCount()) {
            // Show the DisplayStopDataActivity.
            final Intent intent = new Intent(getActivity(),
                    DisplayStopDataActivity.class);
            intent.setAction(DisplayStopDataActivity.ACTION_VIEW_STOP_DATA);
            intent.putExtra(DisplayStopDataActivity.ARG_STOPCODE,
                    ad.getItem(position).stopCode);
            startActivity(intent);
        }
    }
    
    /**
     * Cause the data to refresh. The refresh happens asynchronously in another
     * thread.
     */
    private void doUpdate() {
        if(lastLocation == null) return;
        
        // Stuff the arguments Bundle.
        final Bundle args = new Bundle();
        args.putDouble(NearestStopsLoader.ARG_LATITUDE,
                lastLocation.getLatitude());
        args.putDouble(NearestStopsLoader.ARG_LONGITUDE,
                lastLocation.getLongitude());
        
        // Only put this argument in if chosen services exist.
        final String[] chosenServices = servicesChooser.getChosenServices();
        if(chosenServices.length > 0)
            args.putStringArray(NearestStopsLoader.ARG_FILTERED_SERVICES,
                    chosenServices);
        
        // Restart the Loader.
        getLoaderManager().restartLoader(0, args, this);
    }
    
    /**
     * A SearchResult is essentially bean object to hold the values returned for
     * the database for a particular bus stop. The fields are intentially public
     * for quick execution speed within this class.
     */
    public static class SearchResult implements Comparable<SearchResult> {

        public String stopCode;
        public String stopName;
        public Spanned services;
        public float distance;
        public int orientation;
        public String locality;

        /**
         * Create a new SearchResult instance, specifying default values.
         * 
         * @param stopCode The bus stop code.
         * @param stopName The bus stop name.
         * @param services A String denoting which services stop at this bus
         * stop.
         * @param distance The distance from the handset to this bus stop.
         * @param point The location of this bus stop.
         * @param orientation The direction the bus stop faces.
         * @param locality The locality for this bus stop.
         */
        public SearchResult(final String stopCode, final String stopName,
                final Spanned services, final float distance,
                final int orientation, final String locality) {
            this.stopCode = stopCode;
            this.stopName = stopName;
            this.services = services;
            this.distance = distance;
            this.orientation = orientation;
            this.locality = locality;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(final SearchResult item) {
            if(distance == item.distance) return 0;
            
            // Sort the distance in ascending order.
            return distance > item.distance ? 1 : -1;
        }
    }
    
    /**
     * The NearestStopsLoader accepts an argument Bundle which contains the
     * handset\'s current latitude and longitude and optionally a list of
     * filtered bus services. It will then query the bus stop database to result
     * a list of matching results.
     */
    private static class NearestStopsLoader
            extends SimpleResultLoader<ArrayList<SearchResult>> {
        
        /** The latitude argument. */
        public static final String ARG_LATITUDE = "latitude";
        /** The longitude argument. */
        public static final String ARG_LONGITUDE = "longitude";
        /** The filtered services argument. */
        public static final String ARG_FILTERED_SERVICES = "filteredServices";
        
        /** If modifying for another city, check that this value is correct. */
        private static final double LATITUDE_SPAN = 0.004499;
        /** If modifying for another city, check that this value is correct. */
        private static final double LONGITUDE_SPAN = 0.008001;
        
        private final BusStopDatabase bsd;
        private final Bundle args;
        
        /**
         * Create a new instance of the NearestStopsLoader. An argument Bundle
         * MUST be supplied.
         * 
         * @param context A Context object.
         * @param args The argument Bundle.
         */
        public NearestStopsLoader(final Context context, final Bundle args) {
            super(context);
            
            // Make sure the argument Bundle exists.
            if(args == null)
                throw new IllegalArgumentException("The args cannot be null.");
            
            // Set the class fields.
            bsd = BusStopDatabase.getInstance(context.getApplicationContext());
            this.args = args;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ArrayList<SearchResult> loadInBackground() {
            // Create the List where the results will be placed. If no results
            // have been found, this list will be empty.
            final ArrayList<SearchResult> result =
                    new ArrayList<SearchResult>();
            
            // Cannot continue without coordinates. Return the empty list.
            if(!args.containsKey(ARG_LATITUDE) ||
                    !args.containsKey(ARG_LONGITUDE)) {
                return result;
            }
            
            // Get the latitude and longitude arguments.
            final double latitude = args.getDouble(ARG_LATITUDE);
            final double longitude = args.getDouble(ARG_LONGITUDE);
            
            // Calculate the bounds.
            final double minX = latitude - LATITUDE_SPAN;
            final double minY = longitude - LONGITUDE_SPAN;
            final double maxX = latitude + LATITUDE_SPAN;
            final double maxY = longitude + LONGITUDE_SPAN;
            
            // Do not let anything else touch the stop database while we are
            // querying it.
            synchronized(bsd) {
                Cursor c;
                // What query is executed depends on whether services are being
                // filtered or not.
                if(args.containsKey(ARG_FILTERED_SERVICES)) {
                    c = bsd.getFilteredStopsByCoords(minX, minY, maxX, maxY,
                            ServicesChooserDialogFragment
                            .getChosenServicesForSql(
                            args.getStringArray(ARG_FILTERED_SERVICES)));
                } else {
                    c = bsd.getBusStopsByCoords(minX, minY, maxX, maxY);
                }
                
                // Defensive programming!
                if(c != null) {
                    // We don't care about the bearings so a float array of only
                    // 1 in size is required.
                    final float[] distance = new float[1];
                    distance[0] = 0f;
                    
                    // Loop through all results.
                    while(c.moveToNext()) {
                        // Use the Location class in the Android framework to
                        // compute the distance between the handset and the bus
                        // stop.
                        Location.distanceBetween(latitude, longitude,
                                c.getDouble(2), c.getDouble(3), distance);
                        final String stopCode = c.getString(0);
                        // Create a new SearchResult and add it to the results
                        // list.
                        result.add(new SearchResult(stopCode, c.getString(1),
                                BusStopDatabase.getColouredServiceListString(
                                bsd.getBusServicesForStopAsString(stopCode)),
                                distance[0], c.getInt(4), c.getString(5)));
                    }
                    
                    // Cursor is no longer needed, free the resource.
                    c.close();
                }
            }
            
            // Sort the bus stop results in order of distance from the handset.
            Collections.sort(result);
            
            return result;
        }
    }
    
    /**
     * This ArrayAdapter holds a collection of SearchResult objects which
     * describe each bus stop to show. This class is necessary because a custom
     * layout is required for each row item.
     */
    private static class NearestStopsArrayAdapter
            extends ArrayAdapter<SearchResult> {
        
        private LayoutInflater vi;

        /**
         * Create a new NearestStopsArrayAdapter.
         * 
         * @param context A Context object.
         */
        public NearestStopsArrayAdapter(final Context context) {
            super(context, R.layout.neareststops_list_item,
                    android.R.id.text1);
            
            // Get a LayoutInflater.
            vi = LayoutInflater.from(context);
        }
        
        /**
         * This is a convenience method which adds all the items from an
         * ArrayList to this adapter. It calls through to the add() method in
         * the parent class.
         * 
         * @param collection The collection to add to the end of this adapter.
         */
        public void addAll(final ArrayList<SearchResult> collection) {
            for(SearchResult sr : collection) {
                add(sr);
            }
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public View getView(final int position, final View convertView,
                final ViewGroup parent) {
            final View row;
            // If a previous row exists, use that so that XML doesn't need to
            // be inflated.
            if(convertView != null) {
                row = convertView;
            } else {
                row = vi.inflate(R.layout.neareststops_list_item, null);
            }

            // Get the TextView objects.
            final TextView distance = (TextView)row.findViewById(
                    R.id.txtNearestDistance);
            final TextView stopDetails = (TextView)row.findViewById(
                    android.R.id.text1);
            final TextView buses = (TextView)row.findViewById(
                    android.R.id.text2);
            // Get the SearchResult at position.
            final SearchResult sr = getItem(position);
            // Set the distance text.
            distance.setText((int)sr.distance + " m");
            
            final String name;
            
            if(sr.locality != null) {
                name = getContext().getString(
                        R.string.busstop_locality_coloured, sr.stopName,
                        sr.locality, sr.stopCode);
            } else {
                name = getContext().getString(
                        R.string.busstop_coloured, sr.stopName, sr.stopCode);
            }
            
            stopDetails.setText(Html.fromHtml(name));

            buses.setText(sr.services);
            
            // Set the bus stop marker icon.
            switch(sr.orientation) {
                case 0:
                    distance.setCompoundDrawablesWithIntrinsicBounds(0,
                            R.drawable.mapmarker_n, 0, 0);
                    break;
                case 1:
                    distance.setCompoundDrawablesWithIntrinsicBounds(0,
                            R.drawable.mapmarker_ne, 0, 0);
                    break;
                case 2:
                    distance.setCompoundDrawablesWithIntrinsicBounds(0,
                            R.drawable.mapmarker_e, 0, 0);
                    break;
                case 3:
                    distance.setCompoundDrawablesWithIntrinsicBounds(0,
                            R.drawable.mapmarker_se, 0, 0);
                    break;
                case 4:
                    distance.setCompoundDrawablesWithIntrinsicBounds(0,
                            R.drawable.mapmarker_s, 0, 0);
                    break;
                case 5:
                    distance.setCompoundDrawablesWithIntrinsicBounds(0,
                            R.drawable.mapmarker_sw, 0, 0);
                    break;
                case 6:
                    distance.setCompoundDrawablesWithIntrinsicBounds(0,
                            R.drawable.mapmarker_w, 0, 0);
                    break;
                case 7:
                    distance.setCompoundDrawablesWithIntrinsicBounds(0,
                            R.drawable.mapmarker_nw, 0, 0);
                    break;
                default:
                    distance.setCompoundDrawablesWithIntrinsicBounds(0,
                            R.drawable.mapmarker, 0, 0);
                    break;
            }

            return row;
        }
    }
}