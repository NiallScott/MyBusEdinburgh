/*
 * Copyright (C) 2009 - 2012 Niall 'Rivernile' Scott
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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ProgressBar;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import java.util.ArrayList;
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
    
    private final static int EVENT_REFRESH = 1;
    private final static int EVENT_UPDATE_TIME = 2;
    
    private final static String SERVICE_NAME_KEY = "SERVICE_NAME";
    private final static String DESTINATION_KEY = "DESTINATION";
    private final static String ARRIVAL_TIME_KEY = "ARRIVAL_TIME";
    
    /** This is the stop code argument. */
    public static final String ARG_STOP_CODE = "stopCode";
    
    private static final String DELETE_FAV_DIALOG_TAG = "deleteFav";
    private static final String DELETE_TIME_DIALOG_TAG = "delTimeAlert";
    private static final String DELETE_PROX_DIALOG_TAG = "delProxAlert";
    
    private static final String LOADER_ARG_STOPCODES = "stopCodes";
    private static final String LOADER_ARG_NUMBER_OF_DEPARTURES =
            "numberOfDepartures";
    
    private static final String STATE_KEY_AUTOREFRESH = "autoRefresh";
    private static final String STATE_KEY_LAST_REFRESH = "lastRefresh";
    
    private DisplayStopDataEvent eventCallback;
    private BusStopDatabase bsd;
    private SettingsDatabase sd;
    private SharedPreferences sp;
    
    private ExpandableListView listView;
    private TextView txtLastRefreshed, txtStopName, txtServices, txtError;
    private SimpleExpandableListAdapter listAdapter;
    private View layoutProgress, layoutError, layoutTopBar;
    private ProgressBar progress;
    
    private int numDepartures = 4;
    private String stopCode;
    private String stopName;
    private boolean autoRefresh;
    private long lastRefresh = 0;
    
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
        stopCode = getArguments().getString(ARG_STOP_CODE);
        
        if(savedInstanceState != null) {
            lastRefresh = savedInstanceState.getLong(STATE_KEY_LAST_REFRESH, 0);
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
        layoutProgress = v.findViewById(R.id.layoutProgress);
        layoutError = v.findViewById(R.id.layoutError);
        layoutTopBar = v.findViewById(R.id.layoutTopBar);
        txtStopName = (TextView)v.findViewById(R.id.txtStopName);
        txtServices = (TextView)v.findViewById(R.id.txtServices);
        txtError = (TextView)v.findViewById(R.id.txtError);
        progress = (ProgressBar)v.findViewById(R.id.progress);
        
        // If the cancel button is clicked, let the activity decide what to do.
        final Button btn = (Button)v.findViewById(R.id.btnCancel);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                eventCallback.onCancel();
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
        
        // Get the event callback and make sure it casts correctly (ie, the
        // underlying activity implements DisplayStopDataEvent).
        try {
            eventCallback = (DisplayStopDataEvent)getActivity();
        } catch(ClassCastException e) {
            throw new IllegalStateException("The Activity hosting this " +
                    "fragment must implement DisplayStopDataEvent.");
        }
        
        // Get preferences.
        try {
            numDepartures = Integer.parseInt(
                    sp.getString("pref_numberOfShownDeparturesPerService",
                    "4"));
        } catch(NumberFormatException e) {
            numDepartures = 4;
        }
        
        if(savedInstanceState != null) {
            autoRefresh = savedInstanceState.getBoolean(STATE_KEY_AUTOREFRESH,
                    false);
        } else {
            autoRefresh = sp.getBoolean(
                    PreferencesActivity.KEY_AUTOREFRESH_STATE, false);
        }
        
        if(stopCode == null || stopCode.length() == 0)
            handleError(BusParser.ERROR_NOCODE);
        
        // Tell the fragment that there is an options menu.
        setHasOptionsMenu(true);
        setStopName();
        // Since there is a stop code, there is no reason the bus service list
        // cannot be populated.
        txtServices.setText(BusStopDatabase.getColouredServiceListString(
                bsd.getBusServicesForStopAsString(stopCode)));
        
        if(lastRefresh > 0 && getArguments().getBoolean("forceLoad", false)) {
            getArguments().remove("forceLoad");
            loadBusTimes(true);
        } else {
            loadBusTimes(false);
        }
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
        
        // Refresh the menu.
        getActivity().supportInvalidateOptionsMenu();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();

        // Stop the background tasks when we're pasued.
        autoRefresh = false;
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
        final MenuItem favItem = menu.findItem(
                R.id.displaystopdata_option_menu_favourite);
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
        if(layoutProgress.getVisibility() == View.VISIBLE ||
                progress.getVisibility() == View.VISIBLE) {
            refreshItem.setEnabled(false);
        } else {
            refreshItem.setEnabled(true);
        }
        
        // If there's no bus times, disable all other menu items.
        if(listView.getVisibility() == View.VISIBLE) {
            favItem.setEnabled(true);
            sortItem.setEnabled(true);
            autoRefreshItem.setEnabled(true);
            proxItem.setEnabled(true);
            timeItem.setEnabled(true);
        } else {
            favItem.setEnabled(false);
            sortItem.setEnabled(false);
            autoRefreshItem.setEnabled(false);
            proxItem.setEnabled(false);
            timeItem.setEnabled(false);
        }

        // Add or remove favourite stops?
        if(sd.getFavouriteStopExists(stopCode)) {
            favItem.setTitle(R.string.displaystopdata_menu_remfav)
                    .setIcon(R.drawable.ic_menu_delete);
        } else {
            favItem.setTitle(R.string.displaystopdata_menu_addfav)
                    .setIcon(R.drawable.ic_menu_add);
        }

        // Sort by time or service?
        if(sp.getBoolean("pref_servicessorting_state", false)) {
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
            proxItem.setTitle(R.string.alert_prox_rem)
                    .setIcon(R.drawable.ic_menu_proximityremove);
        } else {
            proxItem.setTitle(R.string.alert_prox_add)
                    .setIcon(R.drawable.ic_menu_proximityadd);
        }
        
        // Time alert active or not?
        if(sd.isActiveTimeAlert(stopCode)) {
            timeItem.setTitle(R.string.alert_time_rem)
                    .setIcon(R.drawable.ic_menu_arrivalremove);
        } else {
            timeItem.setTitle(R.string.alert_time_add)
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
            case R.id.displaystopdata_option_menu_favourite:
                if(sd.getFavouriteStopExists(stopCode)) {
                    // Show the delete favourite stop DialogFragment.
                    final DeleteFavouriteDialogFragment deleteFavFrag =
                            DeleteFavouriteDialogFragment.newInstance(stopCode,
                            this);
                    deleteFavFrag.show(getFragmentManager(),
                            DELETE_FAV_DIALOG_TAG);
                } else {
                    // Show the add favourite stop Activity.
                    intent = new Intent(getActivity(),
                            AddEditFavouriteStopActivity.class);
                    intent.setAction(AddEditFavouriteStopActivity
                            .ACTION_ADD_EDIT_FAVOURITE_STOP);
                    intent.putExtra("stopCode", stopCode);
                    intent.putExtra("stopName", stopName);
                    startActivity(intent);
                }
                
                return true;
            case R.id.displaystopdata_option_menu_sort:
                // Change the sort preference and ask for a data redisplay.
                boolean sortByTime = sp.getBoolean("pref_servicessorting_state",
                        false);
                sortByTime = !sortByTime;
                final SharedPreferences.Editor edit = sp.edit();
                edit.putBoolean("pref_servicessorting_state", sortByTime);
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
                    intent.putExtra("stopCode", stopCode);
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
                    intent.putExtra("stopCode", stopCode);
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
    }
    
    /**
     * Show progress indicators. If the ListView is not shown, then replace the
     * huge white space with a progress indicator. If the ListView is shown,
     * replace the last updated text with new text and a small progress
     * indicator.
     */
    private void showProgress() {
        layoutError.setVisibility(View.GONE);
        
        if(listView.getVisibility() == View.GONE) {
            layoutTopBar.setVisibility(View.GONE);
            layoutProgress.setVisibility(View.VISIBLE);
        } else {
            layoutTopBar.setVisibility(View.VISIBLE);
            layoutProgress.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            txtLastRefreshed.setText(R.string.displaystopdata_gettingdata);
        }
        
        getActivity().supportInvalidateOptionsMenu();
    }
    
    /**
     * Show the bus times. Ensure progress and error layouts are removed and
     * show the top bar and ListView.
     */
    private void showTimes() {
        layoutProgress.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        progress.setVisibility(View.INVISIBLE);
        
        layoutTopBar.setVisibility(View.VISIBLE);
        listView.setVisibility(View.VISIBLE);
        
        getActivity().supportInvalidateOptionsMenu();
    }
    
    /**
     * Show errors. Ensure progress and bus times layouts are removed and show
     * the error layout.
     */
    private void showError() {
        layoutTopBar.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
        layoutProgress.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        
        layoutError.setVisibility(View.VISIBLE);
        
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
        }
        
        if(stopName == null || stopName.length() == 0) {
            txtStopName.setText(stopCode);
            stopName = "";
        } else {
            final StringBuilder sb = new StringBuilder();
            sb.append(stopName);
            sb.append(' ');
            sb.append('(');
            sb.append(stopCode);
            sb.append(')');
            txtStopName.setText(sb.toString());
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
        
        // If the stopName could not be set earlier, get it now from the web
        // service.
        if(stopName.length() == 0)
            stopName = busStop.getStopName();
        
        final StringBuilder sb = new StringBuilder();
        sb.append(stopName);
        sb.append(' ');
        sb.append('(');
        sb.append(stopCode);
        sb.append(')');
        
        // Show the user the stop name and stop code.
        txtStopName.setText(sb.toString());
        
        // Get the list of services in the user's preferred order.
        final ArrayList<BusService> services;
        if(sp.getBoolean("pref_servicessorting_state", false)) {
            services = busStop.getSortedByTimeBusServices();
        } else {
            services = busStop.getBusServices();
        }
        
        // Does the user want to show night services?
        final boolean showNightServices =
                sp.getBoolean("pref_nightservices_state", true);
        
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
                    destination += " (" +
                            getString(R.string.displaystopdata_diverted) + ')';
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
        listAdapter = new SimpleExpandableListAdapter(
                getActivity(), groupData, R.layout.expandable_list_group,
                new String[] { SERVICE_NAME_KEY, DESTINATION_KEY,
                    ARRIVAL_TIME_KEY },
                new int[] { R.id.buslist_service, R.id.buslist_destination,
                    R.id.buslist_time },
                childData, R.layout.expandable_list_child,
                new String[] { DESTINATION_KEY, ARRIVAL_TIME_KEY },
                new int[] { R.id.buschild_destination, R.id.buschild_time });
        listView.setAdapter(listAdapter);

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
        final long timeSinceRefresh = System.currentTimeMillis() -
                lastRefresh;
        final int mins = (int)(timeSinceRefresh / 60000);
        
        final StringBuilder sb = new StringBuilder();
        
        sb.append(getString(R.string.displaystopdata_lastupdated)).append(' ');
        
        if(lastRefresh == 0) {
            // The data has never been refreshed.
            sb.append(getString(R.string.times_never));
        } else if(mins > 59) {
            // The data was refreshed more than 1 hour ago.
            sb.append(getString(R.string.times_greaterthanhour));
        } else if(mins == 0) {
            // The data was refreshed less than 1 minute ago.
            sb.append(getString(R.string.times_lessthanoneminago));
        } else {
            sb.append(getResources()
                    .getQuantityString(R.plurals.times_minsago, mins, mins));
        }
        
        txtLastRefreshed.setText(sb.toString());
    }
    
    /**
     * Schedule the auto-refresh to execute again in 60 seconds.
     */
    private void setUpAutoRefresh() {
        mHandler.sendEmptyMessageDelayed(EVENT_REFRESH, 60000);
    }
    
    /**
     * Schedule the text which denotes the last update time to update in 10
     * seconds.
     */
    private void setUpLastUpdated() {
        mHandler.sendEmptyMessageDelayed(EVENT_UPDATE_TIME, 10000);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfirmFavouriteDeletion() {
        // Refresh the menu/Action items.
        getActivity().supportInvalidateOptionsMenu();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCancelFavouriteDeletion() {
        // Nothing to do here.
    }
    
    /**
     * This interface is used to send events back to the Activity hosting the
     * DisplayStopDataFragment. Generally the events in here are ones which may
     * be handled differently on a phone or a tablet.
     */
    public interface DisplayStopDataEvent {

        /**
         * This is called when the user cancels after an error.
         */
        public void onCancel();
    }
}