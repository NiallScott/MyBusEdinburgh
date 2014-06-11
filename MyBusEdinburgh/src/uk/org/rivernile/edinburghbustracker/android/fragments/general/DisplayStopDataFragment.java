/*
 * Copyright (C) 2009 - 2014 Niall 'Rivernile' Scott
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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import org.json.JSONException;
import uk.org.rivernile.android.bustracker.parser.livetimes
        .AuthenticationException;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusService;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusStop;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusTimes;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusTimesLoader;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveTimesException;
import uk.org.rivernile.android.bustracker.parser.livetimes
        .MaintenanceException;
import uk.org.rivernile.android.bustracker.parser.livetimes
        .SystemOverloadedException;
import uk.org.rivernile.android.bustracker.ui.bustimes
        .BusTimesExpandableListAdapter;
import uk.org.rivernile.android.fetchers.UrlMismatchException;
import uk.org.rivernile.android.utils.LoaderResult;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;
import uk.org.rivernile.edinburghbustracker.android.PreferencesActivity;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.edinburghbustracker.android.SettingsDatabase;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .DeleteFavouriteDialogFragment;

/**
 * This fragment shows live bus times. It is perhaps the most important part of
 * the application. There are a few things to note;
 * 
 * - This fragment communicates with the BusTimes loader. It is a singleton
 * instance which holds the result between rotation changes.
 * - There is a progress view, bus times view and error view. This simply
 * enables and disables layouts as required.
 * - The menu item enabled states change depending on whether bus times are
 * being displayed or not.
 * - The bus stop name shown is taken from the favourite stops list or the bus
 * stop database or finally from the bus tracker service.
 * 
 * @author Niall Scott
 */
public class DisplayStopDataFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<
                LoaderResult<LiveBusTimes, LiveTimesException>>,
        DeleteFavouriteDialogFragment.Callbacks {
    
    private static final int EVENT_REFRESH = 1;
    private static final int EVENT_UPDATE_TIME = 2;
    
    /** This is the stop code argument. */
    public static final String ARG_STOPCODE = "stopCode";
    /** This is the argument required to force a reload of data. */
    public static final String ARG_FORCELOAD = "forceLoad";
    
    private static final String LOADER_ARG_STOPCODES = "stopCodes";
    private static final String LOADER_ARG_NUMBER_OF_DEPARTURES =
            "numberOfDepartures";
    
    private static final String STATE_KEY_AUTOREFRESH = "autoRefresh";
    private static final String STATE_KEY_LAST_REFRESH = "lastRefresh";
    private static final String STATE_KEY_EXPANDED_ITEMS = "expandedItems";
    
    private static final int AUTO_REFRESH_PERIOD = 60000;
    private static final int LAST_REFRESH_PERIOD = 10000;
    
    private Callbacks callbacks;
    private BusStopDatabase bsd;
    private SettingsDatabase sd;
    private SharedPreferences sp;
    
    private BusTimesExpandableListAdapter adapter;
    private ExpandableListView listView;
    private TextView txtLastRefreshed, txtStopName, txtServices, txtError;
    private View layoutTopBar;
    private ProgressBar progressSmall, progressBig;
    private ImageButton imgbtnFavourite;
    
    private int numDepartures = 4;
    private String stopCode;
    private String stopName;
    private String stopLocality;
    private boolean autoRefresh;
    private long lastRefresh = 0;
    private final ArrayList<String> expandedServices = new ArrayList<String>();
    private boolean busTimesLoading = false;
    
    /**
     * Create a new instance of this Fragment, specifying the bus stop code.
     * 
     * @param stopCode The stopCode to load times for.
     * @return A new instance of this Fragment.
     */
    public static DisplayStopDataFragment newInstance(final String stopCode) {
        final DisplayStopDataFragment f = new DisplayStopDataFragment();
        final Bundle b = new Bundle();
        b.putString(ARG_STOPCODE, stopCode);
        f.setArguments(b);
        
        return f;
    }
    
    /**
     * Create a new instance of this Fragment, specifying the bus stop code and
     * if a load of the data should be forced.
     * 
     * @param stopCode The stopCode to load times for.
     * @param forceLoad true if data is to be refreshed, false if not.
     * @return A new instance of this Fragment.
     */
    public static DisplayStopDataFragment newInstance(final String stopCode,
            final boolean forceLoad) {
        final DisplayStopDataFragment f = new DisplayStopDataFragment();
        final Bundle b = new Bundle();
        b.putString(ARG_STOPCODE, stopCode);
        b.putBoolean(ARG_FORCELOAD, forceLoad);
        f.setArguments(b);
        
        return f;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        
        try {
            callbacks = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new IllegalStateException(activity.getClass().getName() +
                    " does not implement " + Callbacks.class.getName());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get the various resources we need.
        final Context context = getActivity().getApplicationContext();
        bsd = BusStopDatabase.getInstance(context);
        sd = SettingsDatabase.getInstance(context);
        sp = context.getSharedPreferences(PreferencesActivity.PREF_FILE, 0);
        
        // Get the stop code from the arguments bundle.
        stopCode = getArguments().getString(ARG_STOPCODE);
        
        adapter = createAdapter();
        adapter.setShowNightServices(
                sp.getBoolean(PreferencesActivity.PREF_SHOW_NIGHT_BUSES, true));
        final boolean sortByTime = sp
                .getBoolean(PreferencesActivity.PREF_SERVICE_SORTING, false);
        
        if (sortByTime) {
            adapter.setOrder(BusTimesExpandableListAdapter.Order.ARRIVAL_TIME);
        } else {
            adapter.setOrder(BusTimesExpandableListAdapter.Order.SERVICE_NAME);
        }
        
        // Get preferences.
        try {
            numDepartures = Integer.parseInt(
                    sp.getString(PreferencesActivity
                        .PREF_NUMBER_OF_SHOWN_DEPARTURES_PER_SERVICE, "4"));
        } catch(NumberFormatException e) {
            numDepartures = 4;
        }
        
        if(savedInstanceState != null) {
            lastRefresh = savedInstanceState.getLong(STATE_KEY_LAST_REFRESH, 0);
            autoRefresh = savedInstanceState.getBoolean(STATE_KEY_AUTOREFRESH,
                    false);
            
            if(savedInstanceState.containsKey(STATE_KEY_EXPANDED_ITEMS)) {
                expandedServices.clear();
                Collections.addAll(expandedServices,
                        savedInstanceState.getStringArray(
                        STATE_KEY_EXPANDED_ITEMS));
            }
        } else {
            autoRefresh = sp.getBoolean(PreferencesActivity.PREF_AUTO_REFRESH,
                    false);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.displaystopdata, container,
                false);
        
        // Get the UI components we need.
        listView = (ExpandableListView)v.findViewById(android.R.id.list);
        txtLastRefreshed = (TextView)v.findViewById(R.id.txtLastUpdated);
        layoutTopBar = v.findViewById(R.id.layoutTopBar);
        txtStopName = (TextView)v.findViewById(R.id.txtStopName);
        txtServices = (TextView)v.findViewById(R.id.txtServices);
        txtError = (TextView)v.findViewById(R.id.txtError);
        progressSmall = (ProgressBar)v.findViewById(R.id.progressSmall);
        progressBig = (ProgressBar)v.findViewById(R.id.progressBig);
        imgbtnFavourite = (ImageButton)v.findViewById(R.id.imgbtnFavourite);
        
        imgbtnFavourite.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                // Add/remove as favourite.
                if(sd.getFavouriteStopExists(stopCode)) {
                    callbacks.onShowConfirmFavouriteDeletion(stopCode);
                } else {
                    callbacks.onShowAddFavouriteStop(stopCode,
                            stopLocality != null ?
                                    stopName + ", " + stopLocality : stopName);
                }
            }
        });
        
        // The ListView has a context menu.
        registerForContextMenu(listView);
        listView.setAdapter(adapter);
        
        return v;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        // Tell the fragment that there is an options menu.
        setHasOptionsMenu(true);
        
        if(stopCode != null && stopCode.length() != 0) {
            setStopName();
            // Since there is a stop code, there is no reason the bus service
            // list cannot be populated.
            txtServices.setText(BusStopDatabase.getColouredServiceListString(
                    bsd.getBusServicesForStopAsString(stopCode)));
            
            if(getArguments().getBoolean(ARG_FORCELOAD, false)) {
                loadBusTimes(true);
            } else {
                loadBusTimes(false);
            }
        } else {
            showError(getString(R.string.displaystopdata_err_nocode));
        }
        
        getArguments().remove(ARG_FORCELOAD);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        
        // Make sure there are no EVENT_UPDATE_TIME messages in the queue.
        mHandler.removeMessages(EVENT_UPDATE_TIME);
        // Set it up again.
        updateLastRefreshed();
        setUpLastUpdated();
        
        if (autoRefresh && !busTimesLoading) {
            setUpAutoRefresh();
        }
        
        // Refresh the menu.
        getActivity().supportInvalidateOptionsMenu();
        
        // Set the favourite ImageButton.
        if(sd.getFavouriteStopExists(stopCode)) {
            imgbtnFavourite.setImageResource(R.drawable.ic_list_favourite);
            imgbtnFavourite.setContentDescription(
                    getString(R.string.favourite_rem));
        } else {
            imgbtnFavourite.setImageResource(
                    R.drawable.ic_list_unfavourite_light);
            imgbtnFavourite.setContentDescription(
                    getString(R.string.favourite_add));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();

        // Stop the background tasks when we're pasued.
        mHandler.removeMessages(EVENT_REFRESH);
        mHandler.removeMessages(EVENT_UPDATE_TIME);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_KEY_AUTOREFRESH, autoRefresh);
        outState.putLong(STATE_KEY_LAST_REFRESH, lastRefresh);
        
        populateExpandedItemsList();
        if(!expandedServices.isEmpty()) {
            final String[] items = new String[expandedServices.size()];
            outState.putStringArray(STATE_KEY_EXPANDED_ITEMS,
                    expandedServices.toArray(items));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateOptionsMenu(final Menu menu,
            final MenuInflater inflater) {
        // Inflate the menu.
        inflater.inflate(R.menu.displaystopdata_option_menu, menu);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        
        // Get the menu items.
        final MenuItem sortItem = menu.findItem(
                R.id.displaystopdata_option_menu_sort);
        final MenuItem autoRefreshItem = menu.findItem(
                R.id.displaystopdata_option_menu_autorefresh);
        final MenuItem proxItem = menu.findItem(
                R.id.displaystopdata_option_menu_prox);
        final MenuItem timeItem = menu.findItem(
                R.id.displaystopdata_option_menu_time);
        final MenuItem refreshItem = menu.findItem(
                R.id.displaystopdata_option_menu_refresh);
        
        // If progress is being shown, disable the refresh button.
        if(progressBig.getVisibility() == View.VISIBLE ||
                progressSmall.getVisibility() == View.VISIBLE) {
            refreshItem.setEnabled(false);
        } else {
            refreshItem.setEnabled(true);
        }
        
        // If there's no bus times, disable all other menu items.
        if(listView.getVisibility() == View.VISIBLE) {
            sortItem.setEnabled(true);
            proxItem.setEnabled(true);
            timeItem.setEnabled(true);
        } else {
            sortItem.setEnabled(false);
            proxItem.setEnabled(false);
            timeItem.setEnabled(false);
        }

        // Sort by time or service?
        if(sp.getBoolean(PreferencesActivity.PREF_SERVICE_SORTING, false)) {
            sortItem.setTitle(R.string.displaystopdata_menu_sort_service);
        } else {
            sortItem.setTitle(R.string.displaystopdata_menu_sort_times);
        }

        // Auto-refresh on or off?
        if(autoRefresh) {
            autoRefreshItem.setTitle(
                    R.string.displaystopdata_menu_turnautorefreshoff);
        } else {
            autoRefreshItem.setTitle(
                    R.string.displaystopdata_menu_turnautorefreshon);
        }
        
        // Proximity alert active or not?
        if(sd.isActiveProximityAlert(stopCode)) {
            proxItem.setTitle(R.string.displaystopdata_menu_prox_rem)
                    .setIcon(R.drawable.ic_menu_proximityremove);
        } else {
            proxItem.setTitle(R.string.displaystopdata_menu_prox_add)
                    .setIcon(R.drawable.ic_menu_proximityadd);
        }
        
        // Time alert active or not?
        if(sd.isActiveTimeAlert(stopCode)) {
            timeItem.setTitle(R.string.displaystopdata_menu_time_rem)
                    .setIcon(R.drawable.ic_menu_arrivalremove);
        } else {
            timeItem.setTitle(R.string.displaystopdata_menu_time_add)
                    .setIcon(R.drawable.ic_menu_arrivaladd);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch(item.getItemId()) {
            case R.id.displaystopdata_option_menu_sort:
                // Change the sort preference and ask for a data redisplay.
                final boolean sortByTime = !sp.getBoolean(
                        PreferencesActivity.PREF_SERVICE_SORTING, false);
                final SharedPreferences.Editor edit = sp.edit();
                edit.putBoolean(PreferencesActivity.PREF_SERVICE_SORTING,
                        sortByTime);
                edit.commit();
                
                if (sortByTime) {
                    adapter.setOrder(
                            BusTimesExpandableListAdapter.Order.ARRIVAL_TIME);
                } else {
                    adapter.setOrder(
                            BusTimesExpandableListAdapter.Order.SERVICE_NAME);
                }
                
                getActivity().supportInvalidateOptionsMenu();
                
                return true;
            case R.id.displaystopdata_option_menu_autorefresh:
                // Turn auto-refresh on or off.
                if(autoRefresh) {
                    autoRefresh = false;
                    mHandler.removeMessages(EVENT_REFRESH);
                } else {
                    autoRefresh = true;
                    setUpAutoRefresh();
                }
                
                getActivity().supportInvalidateOptionsMenu();
                
                return true;
            case R.id.displaystopdata_option_menu_refresh:
                // Ask for a refresh.
                mHandler.removeMessages(EVENT_REFRESH);
                loadBusTimes(true);
                
                return true;
            case R.id.displaystopdata_option_menu_prox:
                if(sd.isActiveProximityAlert(stopCode)) {
                    callbacks.onShowConfirmDeleteProximityAlert();
                } else {
                    // Show the Activity for adding a new proximity alert.
                    callbacks.onShowAddProximityAlert(stopCode);
                }
                
                return true;
            case R.id.displaystopdata_option_menu_time:
                if(sd.isActiveTimeAlert(stopCode)) {
                    callbacks.onShowConfirmDeleteTimeAlert();
                } else {
                    // Show the Activity for adding a new time alert.
                    callbacks.onShowAddTimeAlert(stopCode, null);
                }
                
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
        
        // Create the ListView context menu.
        final MenuInflater inflater = getActivity().getMenuInflater();
        menu.setHeaderTitle(getString(R.string.displaystopdata_context_title));
        inflater.inflate(R.menu.displaystopdata_context_menu, menu);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        // Cast the information parameter.
        final ExpandableListContextMenuInfo info =
                (ExpandableListContextMenuInfo) item.getMenuInfo();
        
        switch (item.getItemId()) {
            case R.id.displaystopdata_context_menu_addarrivalalert:
                // Get the position where this data lives.
                final int position = ExpandableListView
                        .getPackedPositionGroup(info.packedPosition);
                if(position < adapter.getGroupCount()) {
                    final LiveBusService busService = adapter
                            .getGroup(position);
                    // Fire off the Activity.
                    callbacks.onShowAddTimeAlert(stopCode,
                            new String[] { busService.getServiceName() });
                }
                
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Loader<LoaderResult<LiveBusTimes, LiveTimesException>>
            onCreateLoader(final int id, final Bundle args) {
        if(args == null) {
            return null;
        }
        
        showProgress();
        busTimesLoading = true;

        return new LiveBusTimesLoader(getActivity(),
                args.getStringArray(LOADER_ARG_STOPCODES),
                args.getInt(LOADER_ARG_NUMBER_OF_DEPARTURES, 4));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoadFinished(
            final Loader<LoaderResult<LiveBusTimes, LiveTimesException>> loader,
            final LoaderResult<LiveBusTimes, LiveTimesException> result) {
        busTimesLoading = false;
        
        if(result != null && isAdded()) {
            lastRefresh = result.getLoadTime();
            
            if(result.hasException()) {
                handleError(result.getException());
            } else {
                displayData(result.getResult());
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoaderReset(final Loader<LoaderResult<LiveBusTimes,
            LiveTimesException>> loader) {
        // Nothing to do here.
    }
    
    /**
     * Create a new Adapter for the {@link ExpandableListView}. This method
     * exists so that subclasses can return another Adapter.
     * 
     * @return A new {@link BusTimesExpandableListAdapter}.
     */
    protected BusTimesExpandableListAdapter createAdapter() {
        return new BusTimesExpandableListAdapter(getActivity());
    }
    
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            if (!isAdded()) {
                return;
            }
            
            switch(msg.what) {
                case EVENT_REFRESH:
                    // Do a refresh.
                    loadBusTimes(true);
                    break;
                case EVENT_UPDATE_TIME:
                    // Update the last update time.
                    updateLastRefreshed();
                    setUpLastUpdated();
                    break;
                default:
                    break;
            }
        }
    };
    
    /**
     * Request new bus times.
     */
    private void loadBusTimes(final boolean reload) {
        mHandler.removeMessages(EVENT_REFRESH);
        
        final Bundle args = new Bundle();
        args.putStringArray(LOADER_ARG_STOPCODES, new String[] { stopCode });
        args.putInt(LOADER_ARG_NUMBER_OF_DEPARTURES, numDepartures);
        
        if(reload) {
            getLoaderManager().restartLoader(0, args, this);
        } else {
            getLoaderManager().initLoader(0, args, this);
        }
    }
    
    /**
     * Handle errors.
     * 
     * @param errorCode A number attributed to the error.
     */
    private void handleError(final LiveTimesException exception) {
        if (exception == null) {
            showError(getString(R.string.displaystopdata_err_unknown));
            return;
        }
        
        final Throwable cause = exception.getCause();
        final Throwable e = cause != null ? cause : exception;
        final String errorMessage;
        
        if (e instanceof UrlMismatchException) {
            errorMessage = getString(R.string.displaystopdata_err_urlmismatch);
        } else if (e instanceof JSONException) {
            errorMessage = getString(R.string.displaystopdata_err_parseerr);
        } else if (e instanceof AuthenticationException) {
            errorMessage = getString(R.string
                    .displaystopdata_err_api_invalid_key);
        } else if (e instanceof MaintenanceException) {
            errorMessage = getString(R.string
                        .displaystopdata_err_api_system_maintenance);
        } else if (e instanceof SystemOverloadedException) {
            errorMessage = getString(R.string
                        .displaystopdata_err_api_system_overloaded);
        } else {
            errorMessage = getString(R.string.displaystopdata_err_unknown);
        }
        
        showError(errorMessage);
    }
    
    /**
     * Show progress indicators. If the ListView is not shown, then replace the
     * huge white space with a progress indicator. If the ListView is shown,
     * replace the last updated text with new text and a small progress
     * indicator.
     */
    private void showProgress() {
        txtError.setVisibility(View.GONE);
        
        if(listView.getVisibility() == View.GONE) {
            layoutTopBar.setVisibility(View.GONE);
            progressBig.setVisibility(View.VISIBLE);
        } else {
            layoutTopBar.setVisibility(View.VISIBLE);
            progressBig.setVisibility(View.GONE);
            progressSmall.setVisibility(View.VISIBLE);
        }
        
        getActivity().supportInvalidateOptionsMenu();
    }
    
    /**
     * Show the bus times. Ensure progress and error layouts are removed and
     * show the top bar and ListView.
     */
    private void showTimes() {
        progressBig.setVisibility(View.GONE);
        txtError.setVisibility(View.GONE);
        progressSmall.setVisibility(View.INVISIBLE);
        
        layoutTopBar.setVisibility(View.VISIBLE);
        listView.setVisibility(View.VISIBLE);
        
        getActivity().supportInvalidateOptionsMenu();
    }
    
    /**
     * Show errors. Ensure progress and bus times layouts are removed and show
     * the error layout.
     */
    private void showError(final String errorMessage) {
        layoutTopBar.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
        progressBig.setVisibility(View.GONE);
        progressSmall.setVisibility(View.GONE);
        
        txtError.setText(errorMessage);
        txtError.setVisibility(View.VISIBLE);
        
        getActivity().supportInvalidateOptionsMenu();
        
        if (autoRefresh) {
            setUpAutoRefresh();
        }
    }
    
    /**
     * Set the stop name. Firstly, it checks to see if there is a favourite stop
     * for this stop code and uses the user-set name. If not, it checks the bus
     * stop database and uses that name. Otherwise, it will use empty String
     * for it to be replaced later when the times are loaded with the name from
     * the bus tracker web service.
     */
    private void setStopName() {
        if(sd.getFavouriteStopExists(stopCode)) {
            stopName = sd.getNameForStop(stopCode);
        } else {
            stopName = bsd.getNameForBusStop(stopCode);
            stopLocality = bsd.getLocalityForStopCode(stopCode);
        }
        
        if(stopName == null || stopName.length() == 0) {
            txtStopName.setText(stopCode);
            stopName = "";
        } else {
            final String name;
            
            if(stopLocality != null) {
                name = getString(R.string.busstop_locality_coloured,
                        stopName, stopLocality, stopCode);
            } else {
                name = getString(R.string.busstop_coloured, stopName, stopCode);
            }
            
            txtStopName.setText(Html.fromHtml(name));
        }
    }
    
    /**
     * Display the data that was loaded from the real time service.
     * 
     * @param busTimes The loaded LiveBusTimes object.
     */
    private void displayData(final LiveBusTimes busTimes) {
        if (busTimes == null) {
            showError(getString(R.string.displaystopdata_err_nodata));
            adapter.setBusStop(null);
            return;
        }
        
        final LiveBusStop busStop = busTimes.getBusStop(stopCode);
        if (busStop == null) {
            showError(getString(R.string.displaystopdata_err_nodata));
            adapter.setBusStop(null);
            return;
        }
        
        populateExpandedItemsList();
        
        if (TextUtils.isEmpty(stopName)) {
            stopName = busStop.getStopName();
            txtStopName.setText(Html.fromHtml(
                    getString(R.string.busstop_coloured, stopName, stopCode)));
        }
        
        adapter.setBusStop(busStop);
        
        final int count = adapter.getGroupCount();
        LiveBusService busService;
        for(int i = 0; i < count; i++) {
            busService = adapter.getGroup(i);
            // Re-expand previously expanded items.
            if(expandedServices.contains(busService.getServiceName())) {
                listView.expandGroup(i);
            }
        }

        showTimes();
        updateLastRefreshed();
        
        if(autoRefresh) {
            setUpAutoRefresh();
        }
    }
    
    /**
     * Update the text that informs the user how long it has been since the bus
     * data was last refreshed. This normally gets called about every 10
     * seconds.
     */
    private void updateLastRefreshed() {
        final long timeSinceRefresh = SystemClock.elapsedRealtime() -
                lastRefresh;
        final int mins = (int)(timeSinceRefresh / 60000);
        final String text;
        
        if(lastRefresh <= 0) {
            // The data has never been refreshed.
            text = getString(R.string.times_never);
        } else if(mins > 59) {
            // The data was refreshed more than 1 hour ago.
            text = getString(R.string.times_greaterthanhour);
        } else if(mins == 0) {
            // The data was refreshed less than 1 minute ago.
            text = getString(R.string.times_lessthanoneminago);
        } else {
            text = getResources()
                    .getQuantityString(R.plurals.times_minsago, mins, mins);
        }
        
        txtLastRefreshed.setText(getString(R.string.displaystopdata_lastupdated,
                text));
    }
    
    /**
     * Schedule the auto-refresh to execute again 60 seconds after the data was
     * last refreshed.
     */
    private void setUpAutoRefresh() {
        mHandler.removeMessages(EVENT_REFRESH);
        final long time = (lastRefresh + AUTO_REFRESH_PERIOD) -
                SystemClock.elapsedRealtime();
        
        if(time > 0) {
            mHandler.sendEmptyMessageDelayed(EVENT_REFRESH, time);
        } else {
            mHandler.sendEmptyMessage(EVENT_REFRESH);
        }
    }
    
    /**
     * Schedule the text which denotes the last update time to update in 10
     * seconds.
     */
    private void setUpLastUpdated() {
        mHandler.sendEmptyMessageDelayed(EVENT_UPDATE_TIME,
                LAST_REFRESH_PERIOD);
    }
    
    /**
     * This method populates the ArrayList of expanded list items. It will clear
     * the list and loop through the group items in the expanded items to see
     * if that item is expanded or not. If the item is expanded, the service
     * name will be added to the list.
     */
    private void populateExpandedItemsList() {
        // Firstly, flush the previous items from the list.
        expandedServices.clear();
        
        // Cache the count.
        final int count = adapter.getGroupCount();

        // Loop through all group items.
        for(int i = 0; i < count; i++) {
            // If the group is expanded, get the service name and add it to
            // the list.
            if(listView.isGroupExpanded(i)) {
                expandedServices.add(adapter.getGroup(i)
                        .getServiceName());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfirmFavouriteDeletion() {
        imgbtnFavourite.setImageResource(
                R.drawable.ic_list_unfavourite_light);
        imgbtnFavourite.setContentDescription(
                getString(R.string.favourite_add));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCancelFavouriteDeletion() {
        // Nothing to do here.
    }
    
    /**
     * Any Activities which host this Fragment must implement this interface to
     * handle navigation events.
     */
    public static interface Callbacks {
        
        /**
         * This is called when it should be confirmed with the user that they
         * want to delete a favourite bus stop.
         * 
         * @param stopCode The bus stop that the user may want to delete.
         */
        public void onShowConfirmFavouriteDeletion(String stopCode);
        
        /**
         * This is called when it should be confirmed with the user that they
         * want to delete the proximity alert.
         */
        public void onShowConfirmDeleteProximityAlert();
        
        /**
         * This is called when it should be confirmed with the user that they
         * want to delete the time alert.
         */
        public void onShowConfirmDeleteTimeAlert();
        
        /**
         * This is called when the user wants to add a new favourite bus stop.
         * 
         * @param stopCode The stop code of the bus stop to add.
         * @param stopName The default name to use for the bus stop.
         */
        public void onShowAddFavouriteStop(String stopCode, String stopName);
        
        /**
         * This is called when the user wants to view the interface to add a new
         * proximity alert.
         * 
         * @param stopCode The stopCode the proximity alert should be added for.
         */
        public void onShowAddProximityAlert(String stopCode);
        
        /**
         * This is called when the user wants to view the interface to add a new
         * time alert.
         * 
         * @param stopCode The stopCode the time alert should be added for.
         * @param defaultServices The services that should be selected by
         * default. Set to null if no services should be selected.
         */
        public void onShowAddTimeAlert(String stopCode,
                String[] defaultServices);
    }
}