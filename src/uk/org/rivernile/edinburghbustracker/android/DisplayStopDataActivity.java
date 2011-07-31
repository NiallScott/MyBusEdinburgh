/*
 * Copyright (C) 2009 - 2010 Niall 'Rivernile' Scott
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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import uk.org.rivernile.android.bustracker.parser.livetimes.Bus;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusService;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusStop;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusTimes;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusTimesEvent;
import uk.org.rivernile.edinburghbustracker.android.livetimes.parser.EdinburghBus;
import uk.org.rivernile.edinburghbustracker.android.livetimes.parser.EdinburghBusStop;
import uk.org.rivernile.edinburghbustracker.android.livetimes.parser
        .EdinburghParser;

public class DisplayStopDataActivity extends ExpandableListActivity
        implements BusTimesEvent {

    public final static int ERROR_SERVER = 0;
    public final static int ERROR_NOCONNECTION = 1;
    public final static int ERROR_CANNOTRESOLVE = 2;
    public final static int ERROR_NOCODE = 3;
    public final static int ERROR_PARSEERR = 4;
    public final static int ERROR_NODATA = 5;
        
    private final static int EVENT_REFRESH = 1;
    private final static int EVENT_UPDATE_TIME = 2;

    private final static int FAVOURITE_ID = Menu.FIRST;
    private final static int SORT_ID = Menu.FIRST + 1;
    private final static int AUTO_REFRESH_ID = Menu.FIRST + 2;
    private final static int REFRESH_ID = Menu.FIRST + 3;

    private final static int DIALOG_PROGRESS = 0;
    private final static int DIALOG_CONFIRM_DELETE = 1;

    private final static String SERVICE_NAME_KEY = "SERVICE_NAME";
    private final static String DESTINATION_KEY = "DESTINATION";
    private final static String ARRIVAL_TIME_KEY = "ARRIVAL_TIME";

    /** The ACTION_VIEW_STOP_DATA intent action name. */
    public final static String ACTION_VIEW_STOP_DATA =
            "uk.org.rivernile.edinburghbustracker.android."
            + "ACTION_VIEW_STOP_DATA";

    private String stopCode;
    private String stopName;
    private boolean autoRefresh;
    private boolean progressDialogShown = false;
    private SharedPreferences sp;
    private BusTimes busTimes;
    private TextView textLastRefreshed;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.displaystopdata);
        textLastRefreshed = (TextView)findViewById(R.id.displayLastUpdated);
        setTitle(R.string.displaystopdata_title);

        busTimes = BusTimes.getInstance(this,
                EdinburghParser.getInstance());
        sp = getSharedPreferences(PreferencesActivity.PREF_FILE, 0);

        stopCode = getIntent().getStringExtra("stopCode");
        if(stopCode == null || stopCode.length() == 0)
            handleError(ERROR_NOCODE);

        autoRefresh = sp.getBoolean(PreferencesActivity.KEY_AUTOREFRESH_STATE,
                false);

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
                busTimes.doRequest(new String[] { stopCode });
            } else {
                displayData();
            }
        } else {
            showDialog(DIALOG_PROGRESS);
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
        super.onCreateOptionsMenu(menu);

        menu.add(0, FAVOURITE_ID, 1, R.string.displaystopdata_menu_remfav);

        menu.add(0, SORT_ID, 2, R.string.displaystopdata_menu_sort_service)
                .setIcon(R.drawable.ic_menu_sort);

        menu.add(0, AUTO_REFRESH_ID, 3,
                R.string.displaystopdata_menu_turnautorefreshoff)
                .setIcon(R.drawable.ic_menu_auto_refresh);

        menu.add(0, REFRESH_ID, 4, R.string.displaystopdata_menu_refresh)
                .setIcon(R.drawable.ic_menu_refresh);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem item = menu.findItem(FAVOURITE_ID);
        if(SettingsDatabase.getInstance(getApplicationContext())
                .getFavouriteStopExists(stopCode)) {
            item.setTitle(R.string.displaystopdata_menu_remfav)
                    .setIcon(R.drawable.ic_menu_delete);
        } else {
            item.setTitle(R.string.displaystopdata_menu_addfav)
                    .setIcon(R.drawable.ic_menu_add);
        }

        item = menu.findItem(SORT_ID);
        if(sp.getBoolean("pref_servicessorting_state", false)) {
            item.setTitle(R.string.displaystopdata_menu_sort_service);
        } else {
            item.setTitle(R.string.displaystopdata_menu_sort_times);
        }

        item = menu.findItem(AUTO_REFRESH_ID);
        if(autoRefresh) {
            item.setTitle(R.string.displaystopdata_menu_turnautorefreshoff);
        } else {
            item.setTitle(R.string.displaystopdata_menu_turnautorefreshon);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch(item.getItemId()) {
            case FAVOURITE_ID:
                if(SettingsDatabase.getInstance(getApplicationContext())
                        .getFavouriteStopExists(stopCode)) {
                    showDialog(DIALOG_CONFIRM_DELETE);
                } else {
                    Intent intent = new Intent(this,
                            AddEditFavouriteStopActivity.class);
                    intent.setAction(AddEditFavouriteStopActivity
                            .ACTION_ADD_EDIT_FAVOURITE_STOP);
                    intent.putExtra("stopCode", stopCode);
                    intent.putExtra("stopName", stopName);
                    startActivity(intent);
                }
                break;
            case SORT_ID:
                boolean sortByTime = sp.getBoolean("pref_servicessorting_state",
                        false);
                sortByTime = !sortByTime;
                SharedPreferences.Editor edit = sp.edit();
                edit.putBoolean("pref_servicessorting_state", sortByTime);
                edit.commit();
                displayData();
                break;
            case AUTO_REFRESH_ID:
                if(autoRefresh) {
                    autoRefresh = false;
                    mHandler.removeMessages(EVENT_REFRESH);
                } else {
                    autoRefresh = true;
                    setUpAutoRefresh();
                }
                break;
            case REFRESH_ID:
                mHandler.removeMessages(EVENT_REFRESH);
                showDialog(DIALOG_PROGRESS);
                busTimes.doRequest(new String[] { stopCode });
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
            final ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        
        menu.setHeaderTitle("Test context menu");
        menu.add(0, ContextMenu.FIRST, 2, "Test context menu item");
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
                        SettingsDatabase.getInstance(getApplicationContext())
                                .deleteFavouriteStop(stopCode);
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
                    busTimes.doRequest(new String[] { stopCode });
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
            case ERROR_SERVER:
                builder.setMessage(R.string.displaystopdata_err_serverr);
                break;
            case ERROR_NOCONNECTION:
                builder.setMessage(R.string.displaystopdata_err_noconn);
                break;
            case ERROR_CANNOTRESOLVE:
                builder.setMessage(R.string.displaystopdata_err_noresolv);
                break;
            case ERROR_NOCODE:
                builder.setMessage(R.string.displaystopdata_err_nocode);
                break;
            case ERROR_PARSEERR:
                builder.setMessage(R.string.displaystopdata_err_parseerr);
                break;
            case ERROR_NODATA:
                builder.setMessage(R.string.displaystopdata_err_nodata);
                break;
            default:
                // Default error message
                break;
        }
        builder.setCancelable(false).setTitle(R.string.error)
                .setPositiveButton(R.string.retry,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface di, final int i) {
                showDialog(DIALOG_PROGRESS);
                busTimes.doRequest(new String[] { stopCode });
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
        sb.append(getString(R.string.displaystopdata_title2));
        sb.append(' ');
        sb.append(busStop.getStopName());
        sb.append(" (");
        sb.append(busStop.getStopCode());
        sb.append(')');
        setTitle(sb.toString());
        
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
                EdinburghBus bus = (EdinburghBus)lBus;
                
                if(first) {
                    curGroupMap.put(DESTINATION_KEY, bus.getDestination());
                    curGroupMap.put(ARRIVAL_TIME_KEY, bus.getArrivalTime());
                    first = false;
                } else {
                    curChildMap = new HashMap<String, String>();
                    children.add(curChildMap);
                    curChildMap.put(DESTINATION_KEY, bus.getDestination());
                    curChildMap.put(ARRIVAL_TIME_KEY, bus.getArrivalTime());
                }
            }
            childData.add(children);
        }
        
        ExpandableListAdapter listAdapter = new SimpleExpandableListAdapter(
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
            byte secs = (byte)(timeSinceRefresh / 60000);
            sb.append(getString(R.string.times_xminsago).replace("%t",
                    String.valueOf(secs)));
        }
        
        textLastRefreshed.setText(sb.toString());
    }

    private void setUpAutoRefresh() {
        mHandler.sendEmptyMessageDelayed(EVENT_REFRESH, 60000);
    }
    
    private void setUpLastUpdated() {
        mHandler.sendEmptyMessageDelayed(EVENT_UPDATE_TIME, 10000);
    }
}