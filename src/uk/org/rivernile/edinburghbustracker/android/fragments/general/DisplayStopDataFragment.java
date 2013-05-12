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

package uk.org.rivernile.edinburghbustracker.android.fragments.general;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.TouchDelegate;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import uk.org.rivernile.android.bustracker.parser.livetimes.Bus;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusParser;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusService;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusStop;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusTimesLoader;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusTimesResult;
import uk.org.rivernile.edinburghbustracker.android
        .AddEditFavouriteStopActivity;
import uk.org.rivernile.edinburghbustracker.android.AddProximityAlertActivity;
import uk.org.rivernile.edinburghbustracker.android.AddTimeAlertActivity;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;
import uk.org.rivernile.edinburghbustracker.android.PreferencesActivity;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.edinburghbustracker.android.SettingsDatabase;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .DeleteFavouriteDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .DeleteProximityAlertDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .DeleteTimeAlertDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.livetimes.parser
        .EdinburghBus;
import uk.org.rivernile.edinburghbustracker.android.livetimes.parser
        .EdinburghBusStop;
import uk.org.rivernile.edinburghbustracker.android.livetimes.parser
        .EdinburghParser;

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
        implements LoaderManager.LoaderCallbacks<BusTimesResult>,
        DeleteFavouriteDialogFragment.EventListener {
    
    private static final int EVENT_REFRESH = 1;
    private static final int EVENT_UPDATE_TIME = 2;
    
    private static final String SERVICE_NAME_KEY = "SERVICE_NAME";
    private static final String DESTINATION_KEY = "DESTINATION";
    private static final String ARRIVAL_TIME_KEY = "ARRIVAL_TIME";
    
    /** This is the stop code argument. */
    public static final String ARG_STOPCODE = "stopCode";
    /** This is the argument required to force a reload of data. */
    public static final String ARG_FORCELOAD = "forceLoad";
    
    private static final String DELETE_FAV_DIALOG_TAG = "deleteFav";
    private static final String DELETE_TIME_DIALOG_TAG = "delTimeAlert";
    private static final String DELETE_PROX_DIALOG_TAG = "delProxAlert";
    
    private static final String LOADER_ARG_STOPCODES = "stopCodes";
    private static final String LOADER_ARG_NUMBER_OF_DEPARTURES =
            "numberOfDepartures";
    
    private static final String STATE_KEY_AUTOREFRESH = "autoRefresh";
    private static final String STATE_KEY_LAST_REFRESH = "lastRefresh";
    private static final String STATE_KEY_EXPANDED_ITEMS = "expandedItems";
    
    private static final int AUTO_REFRESH_PERIOD = 60000;
    private static final int LAST_REFRESH_PERIOD = 10000;
    
    private BusStopDatabase bsd;
    private SettingsDatabase sd;
    private SharedPreferences sp;
    
    private ExpandableListView listView;
    private TextView txtLastRefreshed, txtStopName, txtServices, txtError;
    private BusTimesExpandableListAdapter listAdapter;
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
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get the various resources we need.
        final Context context = getActivity().getApplicationContext();
        bsd = BusStopDatabase.getInstance(context);
        sd = SettingsDatabase.getInstance(context);
        sp = context.getSharedPreferences(PreferencesActivity.PREF_FILE, 0);
        
        // Get the stop code from the arguments bundle.
        stopCode = getArguments().getString(ARG_STOPCODE);
        
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
                    DeleteFavouriteDialogFragment.newInstance(stopCode,
                                DisplayStopDataFragment.this)
                            .show(getFragmentManager(), DELETE_FAV_DIALOG_TAG);
                } else {
                    final Intent intent = new Intent(getActivity(),
                            AddEditFavouriteStopActivity.class);
                    intent.putExtra(AddEditFavouriteStopActivity.ARG_STOPCODE,
                            stopCode);
                    if(stopLocality != null) {
                        intent.putExtra(
                                AddEditFavouriteStopActivity.ARG_STOPNAME,
                                stopName + ", " + stopLocality);
                    } else {
                        intent.putExtra(
                                AddEditFavouriteStopActivity.ARG_STOPNAME,
                                stopName);
                    }
                    
                    startActivity(intent);
                }
            }
        });
        
        // The ListView has a context menu.
        registerForContextMenu(listView);
        
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
            handleError(BusParser.ERROR_NOCODE);
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
            imgbtnFavourite.setBackgroundResource(R.drawable.ic_list_favourite);
            imgbtnFavourite.setContentDescription(
                    getString(R.string.favourite_rem));
        } else {
            imgbtnFavourite.setBackgroundResource(
                    R.drawable.ic_list_unfavourite);
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
        Intent intent;
        switch(item.getItemId()) {
            case R.id.displaystopdata_option_menu_sort:
                // Change the sort preference and ask for a data redisplay.
                boolean sortByTime = sp.getBoolean(
                        PreferencesActivity.PREF_SERVICE_SORTING, false);
                sortByTime = !sortByTime;
                final SharedPreferences.Editor edit = sp.edit();
                edit.putBoolean(PreferencesActivity.PREF_SERVICE_SORTING,
                        sortByTime);
                edit.commit();
                loadBusTimes(false);
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
                
                return true;
            case R.id.displaystopdata_option_menu_refresh:
                // Ask for a refresh.
                mHandler.removeMessages(EVENT_REFRESH);
                loadBusTimes(true);
                
                return true;
            case R.id.displaystopdata_option_menu_prox:
                if(sd.isActiveProximityAlert(stopCode)) {
                    // Show the DialogFragment for deleting a proximity alert.
                    new DeleteProximityAlertDialogFragment()
                            .show(getFragmentManager(), DELETE_PROX_DIALOG_TAG);
                } else {
                    // Show the Activity for adding a new proximity alert.
                    intent = new Intent(getActivity(),
                            AddProximityAlertActivity.class);
                    intent.putExtra(AddProximityAlertActivity.ARG_STOPCODE,
                            stopCode);
                    startActivity(intent);
                }
                
                return true;
            case R.id.displaystopdata_option_menu_time:
                if(sd.isActiveTimeAlert(stopCode)) {
                    // Show the DialogFragment for deleting a time alert,
                    new DeleteTimeAlertDialogFragment()
                            .show(getFragmentManager(), DELETE_TIME_DIALOG_TAG);
                } else {
                    // Show the Activity for adding a new time alert.
                    intent = new Intent(getActivity(),
                            AddTimeAlertActivity.class);
                    intent.putExtra(AddTimeAlertActivity.ARG_STOPCODE,
                            stopCode);
                    startActivity(intent);
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
                (ExpandableListContextMenuInfo)item.getMenuInfo();
        
        switch(item.getItemId()) {
            case R.id.displaystopdata_context_menu_addarrivalalert:
                // Get the position where this data lives.
                final int position = ExpandableListView
                        .getPackedPositionGroup(info.packedPosition);
                if(listAdapter != null) {
                    final HashMap<String, String> groupData =
                            (HashMap<String, String>)listAdapter
                            .getGroup(position);
                    // Fire off the Activity.
                    final Intent intent = new Intent(getActivity(),
                            AddTimeAlertActivity.class);
                    intent.putExtra("stopCode", stopCode);
                    intent.putExtra("defaultService",
                            groupData.get(SERVICE_NAME_KEY));
                    startActivity(intent);
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
    public Loader<BusTimesResult> onCreateLoader(final int id,
            final Bundle args) {
        if(args == null) return null;
        
        showProgress();
        busTimesLoading = true;
        
        return new BusTimesLoader(getActivity(), new EdinburghParser(),
                args.getStringArray(LOADER_ARG_STOPCODES),
                args.getInt(LOADER_ARG_NUMBER_OF_DEPARTURES, 4));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoadFinished(final Loader<BusTimesResult> loader,
            final BusTimesResult result) {
        busTimesLoading = false;
        
        if(result != null) {
            lastRefresh = result.getLastRefresh();
            if(result.hasError()) {
                handleError(result.getError());
            } else {
                displayData(result.getResult());
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoaderReset(final Loader<BusTimesResult> loader) {
        // Nothing to do here.
    }
    
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
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
    private void handleError(final int errorCode) {
        switch(errorCode) {
            case BusParser.ERROR_NOCONNECTION:
                txtError.setText(R.string.displaystopdata_err_noconn);
                break;
            case BusParser.ERROR_CANNOTRESOLVE:
                txtError.setText(R.string.displaystopdata_err_noresolv);
                break;
            case BusParser.ERROR_NOCODE:
                txtError.setText(R.string.displaystopdata_err_nocode);
                break;
            case BusParser.ERROR_PARSEERR:
                txtError.setText(R.string.displaystopdata_err_parseerr);
                break;
            case BusParser.ERROR_NODATA:
                txtError.setText(R.string.displaystopdata_err_nodata);
                break;
            case BusParser.ERROR_URLMISMATCH:
                txtError.setText(R.string.displaystopdata_err_urlmismatch);
                break;
            case EdinburghParser.ERROR_INVALID_APP_KEY:
                txtError.setText(R.string
                        .displaystopdata_err_api_invalid_key);
                break;
            case EdinburghParser.ERROR_INVALID_PARAMETER:
                txtError.setText(R.string
                        .displaystopdata_err_api_invalid_parameter);
                break;
            case EdinburghParser.ERROR_PROCESSING_ERROR:
                txtError.setText(R.string
                        .displaystopdata_err_api_processing_error);
                break;
            case EdinburghParser.ERROR_SYSTEM_MAINTENANCE:
                txtError.setText(R.string
                        .displaystopdata_err_api_system_maintenance);
                break;
            case EdinburghParser.ERROR_SYSTEM_OVERLOADED:
                txtError.setText(R.string
                        .displaystopdata_err_api_system_overloaded);
                break;
            default:
                txtError.setText(R.string.displaystopdata_err_unknown);
                break;
        }
        
        showError();
        
        if(autoRefresh) {
            setUpAutoRefresh();
        }
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
        
        layoutTopBar.post(new Runnable() {
            @Override
            public void run() {
                final Rect rect = new Rect();
                imgbtnFavourite.getHitRect(rect);
                // Assume it's a square
                final int newSize = (int)(48 * getActivity().getResources()
                        .getDisplayMetrics().density);
                final int adjustBy = (int)
                        ((newSize - (rect.bottom - rect.top)) / 2);

                if(adjustBy > 0) {
                    rect.top -= adjustBy;
                    rect.bottom += adjustBy;
                    rect.left -= adjustBy;
                    rect.right += adjustBy;
                }

                layoutTopBar.setTouchDelegate(new TouchDelegate(rect,
                        imgbtnFavourite));
            }
        });
    }
    
    /**
     * Show errors. Ensure progress and bus times layouts are removed and show
     * the error layout.
     */
    private void showError() {
        layoutTopBar.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
        progressBig.setVisibility(View.GONE);
        progressSmall.setVisibility(View.GONE);
        
        txtError.setVisibility(View.VISIBLE);
        
        getActivity().supportInvalidateOptionsMenu();
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
     * Display the data once loaded in the ListView.
     */
    private void displayData(final HashMap<String, BusStop> data) {
        if(data == null) {
            // There must be no data.
            handleError(BusParser.ERROR_NODATA);
            return;
        }
        
        // Get the data for this stop code.
        final EdinburghBusStop busStop = (EdinburghBusStop)data.get(stopCode);
        if(busStop == null) {
            // There must be no data for this stop code.
            handleError(BusParser.ERROR_NODATA);
            return;
        }
        
        // If this is just a refresh, populate the expanded items list.
        if(listAdapter != null) {
            populateExpandedItemsList();
        }
        
        // If the stopName could not be set earlier, get it now from the web
        // service.
        if(stopName == null || stopName.length() == 0) {
            stopName = busStop.getStopName();
        
            final String name = getString(R.string.busstop_coloured, stopName,
                    stopCode);

            // Show the user the stop name and stop code.
            txtStopName.setText(Html.fromHtml(name));
        }
        
        // Get the list of services in the user's preferred order.
        final ArrayList<BusService> services;
        if(sp.getBoolean(PreferencesActivity.PREF_SERVICE_SORTING, false)) {
            services = busStop.getSortedByTimeBusServices();
        } else {
            services = busStop.getBusServices();
        }
        
        // Does the user want to show night services?
        final boolean showNightServices =
                sp.getBoolean(PreferencesActivity.PREF_SHOW_NIGHT_BUSES, true);
        
        // Declare variables before going in to the loop.
        final ArrayList<HashMap<String, String>> groupData =
                new ArrayList<HashMap<String, String>>();
        final ArrayList<ArrayList<HashMap<String, String>>> childData =
                new ArrayList<ArrayList<HashMap<String, String>>>();
        HashMap<String, String> curGroupMap;
        ArrayList<HashMap<String, String>> children;
        HashMap<String, String> curChildMap;
        EdinburghBus bus;
        String timeToDisplay, destination;
        int mins;
        boolean first;
        
        // Loop through the list of services.
        for(BusService busService : services) {
            if(!showNightServices &&
                    busService.getServiceName().startsWith("N")) continue;
            
            curGroupMap = new HashMap<String, String>();
            groupData.add(curGroupMap);
            // Add the service name.
            curGroupMap.put(SERVICE_NAME_KEY, busService.getServiceName());
            
            children = new ArrayList<HashMap<String, String>>();
            first = true;
            // Loop through the buses inside a service.
            for(Bus lBus : busService.getBuses()) {
                bus = (EdinburghBus)lBus;
                destination = bus.getDestination();
                
                if(bus.isDiverted()) {
                    // Special case if diverted.
                    timeToDisplay = "";
                    
                    // Destination may be null when it comes back from the web
                    // service. Display diverted notice accordingly.
                    if(destination != null) {
                        destination += " (" +
                                getString(R.string.displaystopdata_diverted) +
                                ')';
                    } else {
                        destination = getString(R.string
                                .displaystopdata_diverted);
                    }
                } else {
                    // Get the number of minutes until arrival.
                    mins = bus.getArrivalMinutes();
                    if(mins > 59) {
                        // If more than 59 minutes, display the full time.
                        timeToDisplay = bus.getArrivalTime();
                    } else if(mins < 2) {
                        // If the bus is due in less than 2 mins, show as due.
                        timeToDisplay = "DUE";
                    } else {
                        // Otherwise, display the number of minutes until
                        // arrival.
                        timeToDisplay = String.valueOf(mins);
                    }

                    // If the time is estimated, prefix this to the time shown.
                    if(bus.isEstimated()) {
                        timeToDisplay = '*' + timeToDisplay;
                    }
                    
                    // If the destination is null, make it the empty string to
                    // prevent future problems.
                    if(destination == null) {
                        destination = "";
                    }
                }
                
                if(first) {
                    // If this is the first bus for this service, put this entry
                    // in the group map.
                    curGroupMap.put(DESTINATION_KEY, destination);
                    curGroupMap.put(ARRIVAL_TIME_KEY, timeToDisplay);
                    first = false;
                } else {
                    // Otherwise, put it in the expanded child map.
                    curChildMap = new HashMap<String, String>();
                    children.add(curChildMap);
                    curChildMap.put(DESTINATION_KEY, destination);
                    curChildMap.put(ARRIVAL_TIME_KEY, timeToDisplay);
                }
            }
            childData.add(children);
        }
        
        // Create the adatper. This is ugly.
        listAdapter = new BusTimesExpandableListAdapter(
                getActivity(), groupData, R.layout.expandable_list_group,
                new String[] { SERVICE_NAME_KEY, DESTINATION_KEY,
                    ARRIVAL_TIME_KEY },
                new int[] { R.id.buslist_service, R.id.buslist_destination,
                    R.id.buslist_time },
                childData, R.layout.expandable_list_child,
                new String[] { DESTINATION_KEY, ARRIVAL_TIME_KEY },
                new int[] { R.id.buschild_destination, R.id.buschild_time });
        listView.setAdapter(listAdapter);
        
        final int count = groupData.size();
        for(int i = 0; i < count; i++) {
            curGroupMap = groupData.get(i);
            // Re-expand previously expanded items.
            if(expandedServices.contains(curGroupMap.get(SERVICE_NAME_KEY))) {
                listView.expandGroup(i);
            }
        }

        showTimes();
        if(autoRefresh) setUpAutoRefresh();
        updateLastRefreshed();
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
        
        // The ListAdapter could be null.
        if(listAdapter != null) {
            // Cache the count.
            final int count = listAdapter.getGroupCount();
            
            HashMap<String, String> groupData;
            // Loop through all group items.
            for(int i = 0; i < count; i++) {
                // If the group is expanded, get the service name and add it to
                // the list.
                if(listView.isGroupExpanded(i)) {
                    groupData = (HashMap<String, String>)listAdapter
                            .getGroup(i);
                    expandedServices.add(groupData.get(SERVICE_NAME_KEY));
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfirmFavouriteDeletion() {
        imgbtnFavourite.setBackgroundResource(R.drawable.ic_list_unfavourite);
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
     * This custom ExpandableListAdapter attributes colours to service names
     * in the ExpandableListView.
     */
    private static class BusTimesExpandableListAdapter
            extends SimpleExpandableListAdapter {
        
        private final Context context;
        private final int defaultColour;
        private final HashMap<String, String> colours;
        
        /**
         * Create a new BusTimesExpandableListAdapter.
         * 
         * @param context A Context instance.
         * @param groupData The group data.
         * @param groupLayout The layout to use for the group View.
         * @param groupFrom An array of keys to use for the group items.
         * @param groupTo The TextViews to load the keys in to.
         * @param childData The child data.
         * @param childLayout The layout to use for the child View.
         * @param childFrom An array of keys to use for the child View.
         * @param childTo The TextViews to load the keys in to.
         */
        public BusTimesExpandableListAdapter(final Context context,
                final ArrayList<HashMap<String, String>> groupData,
                final int groupLayout, final String[] groupFrom,
                final int[] groupTo,
                final ArrayList<ArrayList<HashMap<String, String>>> childData,
                final int childLayout,
                final String[] childFrom, final int[] childTo) {
            super(context, groupData, groupLayout, groupFrom, groupTo,
                    childData, childLayout, childFrom, childTo);
            
            // The superclass has no way to get the context again, so cache it
            // here.
            this.context = context;
            
            final BusStopDatabase bsd = BusStopDatabase.getInstance(
                    context.getApplicationContext());
            defaultColour = context.getResources().getColor(R.color
                    .defaultBusColour);
            final int size = groupData.size();
            // Create an array of String to hold the loaded services.
            final String[] services = new String[size];
            int i = 0;
            
            // Get the service list from the group data and put it in the
            // service array.
            for(HashMap<String, String> map : groupData) {
                services[i] = map.get(SERVICE_NAME_KEY);
                i++;
            }
            
            if(size > 0) {
                colours = bsd.getServiceColours(services);
            } else {
                colours = null;
            }
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressLint({"NewAPI"})
        public View getGroupView(final int groupPosition,
                final boolean isExpanded, final View convertView,
                final ViewGroup parent) {
            final View v = super.getGroupView(groupPosition, isExpanded,
                    convertView, parent);
            final TextView txtService = (TextView)v.findViewById(
                    R.id.buslist_service);
            // Get the HashMap for the groupPosition.
            final HashMap<String, String> group =
                    (HashMap<String, String>)getGroup(groupPosition);
            // Get the name of the service.
            final String service = group.get(SERVICE_NAME_KEY);
            // Get the Drawable which makes up the retangle in the background
            // with the rounded corners. Make it mutable so it doesn't affect
            // other instances of the same Drawable.
            final GradientDrawable background;
            try {
                background = (GradientDrawable)context.getResources()
                        .getDrawable(R.drawable.bus_service_rounded_background)
                        .mutate();
            } catch(ClassCastException e) {
                txtService.setTextColor(Color.BLACK);
                return v;
            }
            
            // Night services are treated differently to the rest.
            if(service.startsWith("N")) {
                // Give it a black background.
                background.setColor(Color.BLACK);
                // We need to replace the text in the TextView because HTML
                // formatting has been applied to it, to make the 'N' red.
                txtService.setText(
                        BusStopDatabase.getColouredServiceListString(service));
            } else if(colours != null && colours.containsKey(service)) {
                try {
                    // If the colour for the service can be parsed, set the
                    // background here.
                    background.setColor(Color.parseColor(
                            colours.get(service)));
                } catch(IllegalArgumentException e) {
                    // If it cannot be parsed, use the default background
                    // colour.
                    background.setColor(defaultColour);
                }
            } else {
                // If not a night service, and a colour doesn't exist for the
                // service, use the default colour.
                background.setColor(defaultColour);
            }
            
            // Set the background and return the View for the group.
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                txtService.setBackground(background);
            } else {
                txtService.setBackgroundDrawable(background);
            }
            
            return v;
        }
    }
}