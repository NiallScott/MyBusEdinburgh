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

package uk.org.rivernile.edinburghbustracker.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import uk.org.rivernile.android.bustracker.parser.livetimes.Bus;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusService;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusStop;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusTimes;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusTimesEvent;
import uk.org.rivernile.edinburghbustracker.android.alerts.AlertManager;
import uk.org.rivernile.edinburghbustracker.android.livetimes.parser.EdinburghBus;
import uk.org.rivernile.edinburghbustracker.android.livetimes.parser.EdinburghBusStop;
import uk.org.rivernile.edinburghbustracker.android.livetimes.parser
        .EdinburghParser;

public class DisplayStopDataActivity extends ExpandableListActivity
        implements BusTimesEvent {

    private final static int EVENT_REFRESH = 1;
    private final static int EVENT_UPDATE_TIME = 2;

    private final static int DIALOG_PROGRESS = 0;
    private final static int DIALOG_CONFIRM_DELETE = 1;
    private final static int DIALOG_PROX_REM = 2;
    private final static int DIALOG_TIME_REM = 3;

    private final static String SERVICE_NAME_KEY = "SERVICE_NAME";
    private final static String DESTINATION_KEY = "DESTINATION";
    private final static String ARRIVAL_TIME_KEY = "ARRIVAL_TIME";

    /** The ACTION_VIEW_STOP_DATA intent action name. */
    public final static String ACTION_VIEW_STOP_DATA =
            "uk.org.rivernile.edinburghbustracker.android."
            + "ACTION_VIEW_STOP_DATA";
    
    private static final boolean isHoneycombOrGreater =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;

    private String stopCode;
    private String stopName;
    private boolean autoRefresh;
    private boolean progressDialogShown = false;
    private SharedPreferences sp;
    private BusTimes busTimes;
    private TextView textLastRefreshed;
    private SettingsDatabase sd;
    private AlertManager alertMan;
    private SimpleExpandableListAdapter listAdapter;
    private BusStopDatabase bsd;
    private int numDepartures = 4;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.displaystopdata);
        setTitle(R.string.displaystopdata_title);
        
        bsd = BusStopDatabase.getInstance(this);
        sd = SettingsDatabase.getInstance(this);
        alertMan = AlertManager.getInstance(getApplicationContext());
        Intent intent = getIntent();
        sp = getSharedPreferences(PreferencesActivity.PREF_FILE, 0);
        busTimes = BusTimes.getInstance(this, EdinburghParser.getInstance());
        
        textLastRefreshed = (TextView)findViewById(R.id.displayLastUpdated);

        try {
            numDepartures = Integer.parseInt(
                    sp.getString("pref_numberOfShownDeparturesPerService",
                    "4"));
        } catch(NumberFormatException e) {
            numDepartures = 4;
        }
        
        if(Intent.ACTION_VIEW.equals(intent.getAction())) {
            stopCode = intent.getData().getQueryParameter("busStopCode");
        } else {
            stopCode = getIntent().getStringExtra("stopCode");
        }

        if(stopCode == null || stopCode.length() == 0)
            handleError(BusTimes.ERROR_NOCODE);

        if(savedInstanceState != null) {
            autoRefresh = savedInstanceState.getBoolean("autoRefresh", false);
        } else {
            autoRefresh = sp.getBoolean(
                    PreferencesActivity.KEY_AUTOREFRESH_STATE, false);
        }
        
        registerForContextMenu(getExpandableListView());
    }

    @Override
    public void onResume() {
        super.onResume();
        
        mHandler.removeMessages(EVENT_UPDATE_TIME);
        updateLastRefreshed();
        setUpLastUpdated();
        
        if(!busTimes.isExecuting()) {
            HashMap<String, BusStop> busStops = busTimes.getBusStops();
            if(busStops == null || !busStops.containsKey(stopCode)) {
                busTimes.doRequest(new String[] { stopCode }, numDepartures);
            } else {
                displayData();
            }
        } else {
            showDialog(DIALOG_PROGRESS);
        }
        
        if(isHoneycombOrGreater) {
            invalidateOptionsMenuSupport();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        autoRefresh = false;
        mHandler.removeMessages(EVENT_REFRESH);
        mHandler.removeMessages(EVENT_UPDATE_TIME);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("autoRefresh", autoRefresh);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        busTimes.setHandler(null);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.displaystopdata_option_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem item = menu.findItem(
                R.id.displaystopdata_option_menu_favourite);
        if(sd.getFavouriteStopExists(stopCode)) {
            item.setTitle(R.string.displaystopdata_menu_remfav)
                    .setIcon(R.drawable.ic_menu_delete);
        } else {
            item.setTitle(R.string.displaystopdata_menu_addfav)
                    .setIcon(R.drawable.ic_menu_add);
        }

        item = menu.findItem(R.id.displaystopdata_option_menu_sort);
        if(sp.getBoolean("pref_servicessorting_state", false)) {
            item.setTitle(R.string.displaystopdata_menu_sort_service);
        } else {
            item.setTitle(R.string.displaystopdata_menu_sort_times);
        }

        item = menu.findItem(R.id.displaystopdata_option_menu_autorefresh);
        if(autoRefresh) {
            item.setTitle(R.string.displaystopdata_menu_turnautorefreshoff);
        } else {
            item.setTitle(R.string.displaystopdata_menu_turnautorefreshon);
        }
        
        item = menu.findItem(R.id.displaystopdata_option_menu_prox);
        if(sd.isActiveProximityAlert(stopCode)) {
            item.setTitle(R.string.alert_prox_rem)
                    .setIcon(R.drawable.ic_menu_proximityremove);
        } else {
            item.setTitle(R.string.alert_prox_add)
                    .setIcon(R.drawable.ic_menu_proximityadd);
        }
        
        item = menu.findItem(R.id.displaystopdata_option_menu_time);
        if(sd.isActiveTimeAlert(stopCode)) {
            item.setTitle(R.string.alert_time_rem)
                    .setIcon(R.drawable.ic_menu_arrivalremove);
        } else {
            item.setTitle(R.string.alert_time_add)
                    .setIcon(R.drawable.ic_menu_arrivaladd);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        Intent intent;
        switch(item.getItemId()) {
            case R.id.displaystopdata_option_menu_favourite:
                if(sd.getFavouriteStopExists(stopCode)) {
                    showDialog(DIALOG_CONFIRM_DELETE);
                } else {
                    intent = new Intent(this,
                            AddEditFavouriteStopActivity.class);
                    intent.setAction(AddEditFavouriteStopActivity
                            .ACTION_ADD_EDIT_FAVOURITE_STOP);
                    intent.putExtra("stopCode", stopCode);
                    intent.putExtra("stopName", stopName);
                    startActivity(intent);
                }
                break;
            case R.id.displaystopdata_option_menu_sort:
                boolean sortByTime = sp.getBoolean("pref_servicessorting_state",
                        false);
                sortByTime = !sortByTime;
                SharedPreferences.Editor edit = sp.edit();
                edit.putBoolean("pref_servicessorting_state", sortByTime);
                edit.commit();
                displayData();
                if(isHoneycombOrGreater) {
                    invalidateOptionsMenuSupport();
                }
                break;
            case R.id.displaystopdata_option_menu_autorefresh:
                if(autoRefresh) {
                    autoRefresh = false;
                    mHandler.removeMessages(EVENT_REFRESH);
                } else {
                    autoRefresh = true;
                    setUpAutoRefresh();
                }
                break;
            case R.id.displaystopdata_option_menu_refresh:
                mHandler.removeMessages(EVENT_REFRESH);
                showDialog(DIALOG_PROGRESS);
                busTimes.doRequest(new String[] { stopCode }, numDepartures);
                break;
            case R.id.displaystopdata_option_menu_prox:
                if(sd.isActiveProximityAlert(stopCode)) {
                    showDialog(DIALOG_PROX_REM);
                } else {
                    intent = new Intent(this,
                            AddProximityAlertActivity.class);
                    intent.putExtra("stopCode", stopCode);
                    startActivity(intent);
                }
                break;
            case R.id.displaystopdata_option_menu_time:
                if(sd.isActiveTimeAlert(stopCode)) {
                    showDialog(DIALOG_TIME_REM);
                } else {
                    intent = new Intent(this, AddTimeAlertActivity.class);
                    intent.putExtra("stopCode", stopCode);
                    startActivity(intent);
                }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
            final ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        
        MenuInflater inflater = getMenuInflater();
        menu.setHeaderTitle(getString(R.string.displaystopdata_context_title));
        inflater.inflate(R.menu.displaystopdata_context_menu, menu);
    }
    
    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        ExpandableListContextMenuInfo info =
                (ExpandableListContextMenuInfo)item.getMenuInfo();
        
        switch(item.getItemId()) {
            case R.id.displaystopdata_context_menu_addarrivalalert:
                int position = ExpandableListView
                        .getPackedPositionGroup(info.packedPosition);
                if(listAdapter != null) {
                    HashMap<String, String> groupData =
                            (HashMap<String, String>)listAdapter
                            .getGroup(position);
                    Intent intent = new Intent(this,
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

    @Override
    protected Dialog onCreateDialog(final int id) {
        switch(id) {
            case DIALOG_PROGRESS:
                ProgressDialog prog = new ProgressDialog(this);
                prog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                prog.setCancelable(true);
                prog.setMessage(getString(
                        R.string.displaystopdata_gettingdata));
                prog.setOnCancelListener(new DialogInterface
                        .OnCancelListener() {
                    public void onCancel(DialogInterface di) {
                        finish();
                    }
                });
                progressDialogShown = true;
                return prog;
            case DIALOG_CONFIRM_DELETE:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setCancelable(true)
                    .setTitle(R.string.favouritestops_dialog_confirm_title)
                    .setPositiveButton(R.string.okay,
                    new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                            final int id)
                    {
                        sd.deleteFavouriteStop(stopCode);
                        if(isHoneycombOrGreater) {
                            invalidateOptionsMenuSupport();
                        }
                    }
                }).setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                     public void onClick(final DialogInterface dialog,
                             final int id)
                     {
                        dialog.dismiss();
                     }
                });
                return builder2.create();
            case DIALOG_PROX_REM:
                return alertMan.getConfirmDeleteProxAlertDialog(this);
            case DIALOG_TIME_REM:
                return alertMan.getConfirmDeleteTimeAlertDialog(this);
            default:
                return null;
        }
    }
    
    @Override
    public void onPreExecute() {
        showDialog(DIALOG_PROGRESS);
    }
    
    @Override
    public void onBusTimesError(final int errorCode) {
        handleError(errorCode);
    }
    
    @Override
    public void onBusTimesReady(final HashMap<String, BusStop> result) {
        if(progressDialogShown) dismissDialog(DIALOG_PROGRESS);
        displayData();
    }
    
    @Override
    public void onCancel() {
        if(progressDialogShown) dismissDialog(DIALOG_PROGRESS);
        finish();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            switch(msg.what) {
                case EVENT_REFRESH:
                    showDialog(DIALOG_PROGRESS);
                    busTimes.doRequest(new String[] { stopCode },
                            numDepartures);
                    break;
                case EVENT_UPDATE_TIME:
                    updateLastRefreshed();
                    setUpLastUpdated();
                    break;
                default:
                    break;
            }
        }
    };

    private void handleError(final int errorCode) {
        if(progressDialogShown) dismissDialog(DIALOG_PROGRESS);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch(errorCode) {
            case BusTimes.ERROR_NOCONNECTION:
                builder.setMessage(R.string.displaystopdata_err_noconn);
                break;
            case BusTimes.ERROR_CANNOTRESOLVE:
                builder.setMessage(R.string.displaystopdata_err_noresolv);
                break;
            case BusTimes.ERROR_NOCODE:
                builder.setMessage(R.string.displaystopdata_err_nocode);
                break;
            case BusTimes.ERROR_PARSEERR:
                builder.setMessage(R.string.displaystopdata_err_parseerr);
                break;
            case BusTimes.ERROR_NODATA:
                builder.setMessage(R.string.displaystopdata_err_nodata);
                break;
            case BusTimes.ERROR_INVALID_APP_KEY:
                builder.setMessage(R.string
                        .displaystopdata_err_api_invalid_key);
                break;
            case BusTimes.ERROR_INVALID_PARAMETER:
                builder.setMessage(R.string
                        .displaystopdata_err_api_invalid_parameter);
                break;
            case BusTimes.ERROR_PROCESSING_ERROR:
                builder.setMessage(R.string
                        .displaystopdata_err_api_processing_error);
                break;
            case BusTimes.ERROR_SYSTEM_MAINTENANCE:
                builder.setMessage(R.string
                        .displaystopdata_err_api_system_maintenance);
                break;
            case BusTimes.ERROR_SYSTEM_OVERLOADED:
                builder.setMessage(R.string
                        .displaystopdata_err_api_system_overloaded);
                break;
            default:
                builder.setMessage(R.string.displaystopdata_err_unknown);
                break;
        }
        
        builder.setCancelable(false).setTitle(R.string.error)
                .setPositiveButton(R.string.retry,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface di, final int i) {
                showDialog(DIALOG_PROGRESS);
                busTimes.doRequest(new String[] { stopCode }, numDepartures);
            }
        }).setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface di, final int i) {
                finish();
            }
        });
        builder.create().show();
    }
    
    private void displayData() {
        HashMap<String, BusStop> data = busTimes.getBusStops();
        if(data == null) {
            handleError(BusTimes.ERROR_NODATA);
            return;
        }
        
        EdinburghBusStop busStop = (EdinburghBusStop)data.get(stopCode);
        if(busStop == null) {
            handleError(BusTimes.ERROR_NODATA);
            return;
        }
        
        stopName = busStop.getStopName();
        
        StringBuilder sb = new StringBuilder();
        sb.append(busStop.getStopName());
        sb.append(" (");
        sb.append(busStop.getStopCode());
        sb.append(')');
        
        LinearLayout ll = (LinearLayout)findViewById(R.id.stopInfo);
        ll.setVisibility(View.VISIBLE);
        TextView tv = (TextView)findViewById(R.id.txtStopName);
        tv.setText(sb.toString());
        
        tv = (TextView)findViewById(R.id.txtServices);
        tv.setText(bsd.getBusServicesForStopAsString(stopCode));
        
        ArrayList<BusService> services;
        if(sp.getBoolean("pref_servicessorting_state", false)) {
            services = busStop.getSortedByTimeBusServices();
        } else {
            services = busStop.getBusServices();
        }
        
        ArrayList<HashMap<String, String>> groupData =
                new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> childData =
                new ArrayList<ArrayList<HashMap<String, String>>>();
        HashMap<String, String> curGroupMap;
        ArrayList<HashMap<String, String>> children;
        HashMap<String, String> curChildMap;
        EdinburghBus bus;
        String timeToDisplay;
        int mins;
        boolean first;
        
        for(BusService busService : services) {
            if(!sp.getBoolean("pref_nightservices_state", true) &&
                    busService.getServiceName().startsWith("N")) continue;
            
            curGroupMap = new HashMap<String, String>();
            groupData.add(curGroupMap);
            curGroupMap.put(SERVICE_NAME_KEY, busService.getServiceName());
            
            children = new ArrayList<HashMap<String, String>>();
            first = true;
            for(Bus lBus : busService.getBuses()) {
                bus = (EdinburghBus)lBus;
                mins = bus.getArrivalMinutes();
                if(mins > 59) {
                    timeToDisplay = bus.getArrivalTime();
                } else if(mins < 2) {
                    timeToDisplay = "DUE";
                } else {
                    timeToDisplay = String.valueOf(mins);
                }
                
                if(bus.isEstimated()) {
                    timeToDisplay = "*" + timeToDisplay;
                }
                
                if(first) {
                    curGroupMap.put(DESTINATION_KEY, bus.getDestination());
                    curGroupMap.put(ARRIVAL_TIME_KEY, timeToDisplay);
                    first = false;
                } else {
                    curChildMap = new HashMap<String, String>();
                    children.add(curChildMap);
                    curChildMap.put(DESTINATION_KEY, bus.getDestination());
                    curChildMap.put(ARRIVAL_TIME_KEY, timeToDisplay);
                }
            }
            childData.add(children);
        }
        
        listAdapter = new SimpleExpandableListAdapter(
                this, groupData, R.layout.expandable_list_group,
                new String[] { SERVICE_NAME_KEY, DESTINATION_KEY,
                    ARRIVAL_TIME_KEY },
                new int[] { R.id.buslist_service, R.id.buslist_destination,
                    R.id.buslist_time },
                childData, R.layout.expandable_list_child,
                new String[] { DESTINATION_KEY, ARRIVAL_TIME_KEY },
                new int[] { R.id.buschild_destination, R.id.buschild_time });
        setListAdapter(listAdapter);

        if(progressDialogShown) dismissDialog(DIALOG_PROGRESS);
        if(autoRefresh) setUpAutoRefresh();
        updateLastRefreshed();
    }
    
    private void updateLastRefreshed() {
        long lastRefreshed = busTimes.getLastDataRefresh();
        long timeSinceRefresh = System.currentTimeMillis() - lastRefreshed;
        
        StringBuilder sb = new StringBuilder();
        
        sb.append(getString(R.string.displaystopdata_lastupdated)).append(' ');
        
        if(lastRefreshed == 0) {
            sb.append(getString(R.string.times_never));
        } else if(timeSinceRefresh < 60000) {
            sb.append(getString(R.string.times_lessthanoneminago));
        } else if(timeSinceRefresh < 120000) {
            sb.append(getString(R.string.times_oneminago));
        } else if(timeSinceRefresh > 3600000) {
            sb.append(getString(R.string.times_greaterthanhour));
        } else {
            byte mins = (byte)(timeSinceRefresh / 60000);
            sb.append(getString(R.string.times_xminsago).replace("%t",
                    String.valueOf(mins)));
        }
        
        textLastRefreshed.setText(sb.toString());
    }

    private void setUpAutoRefresh() {
        mHandler.sendEmptyMessageDelayed(EVENT_REFRESH, 60000);
    }
    
    private void setUpLastUpdated() {
        mHandler.sendEmptyMessageDelayed(EVENT_UPDATE_TIME, 10000);
    }
    
    private void invalidateOptionsMenuSupport() {
        try {
            Method mtd = BusStopMapActivity.class
                    .getMethod("invalidateOptionsMenu", (Class<?>[]) null);
            if(mtd != null) mtd.invoke(this, (Object[]) null);
        } catch(NoSuchMethodException e) {
            
        } catch(IllegalAccessException e) {
            
        } catch(InvocationTargetException e) {
            
        }
    }
}