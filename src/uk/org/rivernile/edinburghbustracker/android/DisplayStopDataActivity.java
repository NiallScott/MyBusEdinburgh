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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListAdapter;
import android.widget.SimpleExpandableListAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DisplayStopDataActivity extends ExpandableListActivity {

    public final static int ERROR_SERVER = 0;
    public final static int ERROR_NOCONNECTION = 1;
    public final static int ERROR_CANNOTRESOLVE = 2;
    public final static int ERROR_NOCODE = 3;
    public final static int ERROR_PARSEERR = 4;
    public final static int ERROR_NODATA = 5;

    private final static int FAVOURITE_ID = Menu.FIRST;
    private final static int AUTO_REFRESH_ID = Menu.FIRST + 1;
    private final static int REFRESH_ID = Menu.FIRST + 2;

    private final static int PROGRESS_DIALOG = 0;
    private final static int CONFIRM_DELETE = 1;

    private final static String SERVICE_NAME_KEY = "SERVICE_NAME";
    private final static String DESTINATION_KEY = "DESTINATION";
    private final static String ARRIVAL_TIME_KEY = "ARRIVAL_TIME";

    /** The ACTION_VIEW_STOP_DATA intent action name. */
    public final static String ACTION_VIEW_STOP_DATA =
            "uk.org.rivernile.edinburghbustracker.android."
            + "ACTION_VIEW_STOP_DATA";

    private String stopCode;
    private String stopName;
    private String remoteHost;
    private String jsonString;
    private int remotePort;
    private boolean autoRefresh;
    private boolean favouriteExists;
    private boolean showNightBuses;
    private boolean progressDialogShown = false;
    private SharedPreferences sp;
    private FetchLiveTimesTask fetchTask;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.displaystopdata);
        setTitle(R.string.displaystopdata_title);

        fetchTask = FetchLiveTimesTask.getInstance(mHandler);
        sp = getSharedPreferences(PreferencesActivity.PREF_FILE, 0);

        remoteHost = sp.getString(PreferencesActivity.KEY_HOSTNAME,
                "bustracker.selfip.org");
        try {
            remotePort = Integer.parseInt(sp.getString(
                    PreferencesActivity.KEY_PORT, "4876"));
        } catch(NumberFormatException e) {
            remotePort = 4876;
        }

        stopCode = getIntent().getStringExtra("stopCode");
        if(stopCode == null || stopCode.length() == 0)
            handleError(ERROR_NOCODE);

        showNightBuses = sp.getBoolean("pref_nightservices_state", true);
        autoRefresh = sp.getBoolean(PreferencesActivity.KEY_AUTOREFRESH_STATE,
                false);

        if(savedInstanceState != null) {
            jsonString = savedInstanceState.getString("jsonString");
            autoRefresh = savedInstanceState.getBoolean("autoRefresh", false);
        } else {
            autoRefresh = sp.getBoolean(
                    PreferencesActivity.KEY_AUTOREFRESH_STATE, false);
        }

        if(jsonString != null && jsonString.length() > 0) {
            handleJSONString(jsonString);
        } else if(!fetchTask.isExecuting()) {
                showDialog(PROGRESS_DIALOG);
                fetchTask.doTask(stopCode, remoteHost, remotePort);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        favouriteExists = SettingsDatabase.getInstance(getApplicationContext())
                .getFavouriteStopExists(stopCode);
    }

    @Override
    public void onPause() {
        super.onPause();

        autoRefresh = false;
        mHandler.removeMessages(99);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("autoRefresh", autoRefresh);
        outState.putString("jsonString", jsonString);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fetchTask.setHandler(null);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);

        if(favouriteExists) {
            menu.add(0, FAVOURITE_ID, 1, R.string.displaystopdata_menu_remfav);
        } else {
             menu.add(0, FAVOURITE_ID, 1, R.string.displaystopdata_menu_addfav);
        }

        if(autoRefresh) {
            menu.add(0, AUTO_REFRESH_ID, 2,
                    R.string.displaystopdata_menu_turnautorefreshoff)
                    .setIcon(R.drawable.ic_menu_auto_refresh);
        } else {
            menu.add(0, AUTO_REFRESH_ID, 2,
                    R.string.displaystopdata_menu_turnautorefreshon)
                    .setIcon(R.drawable.ic_menu_auto_refresh);
        }

        menu.add(0, REFRESH_ID, 3, R.string.displaystopdata_menu_refresh)
                .setIcon(R.drawable.ic_menu_refresh);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem item = menu.findItem(FAVOURITE_ID);
        if(favouriteExists) {
            item.setTitle(R.string.displaystopdata_menu_remfav)
                    .setIcon(R.drawable.ic_menu_delete);
        } else {
            item.setTitle(R.string.displaystopdata_menu_addfav)
                    .setIcon(R.drawable.ic_menu_add);

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
                if(favouriteExists) {
                    showDialog(CONFIRM_DELETE);
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
            case AUTO_REFRESH_ID:
                if(autoRefresh) {
                    autoRefresh = false;
                    mHandler.removeMessages(99);
                } else {
                    autoRefresh = true;
                    setUpAutoRefresh();
                }
                break;
            case REFRESH_ID:
                mHandler.removeMessages(99);
                showDialog(PROGRESS_DIALOG);
                fetchTask.doTask(stopCode, remoteHost, remotePort);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        switch(id) {
            case PROGRESS_DIALOG:
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
            case CONFIRM_DELETE:
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
                        favouriteExists = false;
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

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            if(msg.getData().containsKey("errorCode")) {
                handleError(msg.getData().getInt("errorCode"));
            } else if(msg.getData().containsKey("jsonString")) {
                handleJSONString(msg.getData().getString("jsonString"));
            } else if(msg.getData().containsKey("refresh")) {
                showDialog(PROGRESS_DIALOG);
                fetchTask.doTask(stopCode, remoteHost, remotePort);
            }
        }
    };

    private void handleError(final int errorCode) {
        if(progressDialogShown) dismissDialog(PROGRESS_DIALOG);
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
                showDialog(PROGRESS_DIALOG);
                fetchTask.doTask(stopCode, remoteHost, remotePort);
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

    private void handleJSONString(final String jsonString) {
        this.jsonString = jsonString;
        JSONObject jo;
        try {
            jo = new JSONObject(jsonString);

            String sc = jo.getString("stopCode");
            if(sc.length() == 0) {
                handleError(ERROR_NODATA);
                return;
            }

            stopName = jo.getString("stopName");
            setTitle(getString(R.string.displaystopdata_title2) + " " +
                    sc + " " + stopName);

            JSONArray services = jo.getJSONArray("services");
            if(services.length() == 0) return;

            ArrayList<HashMap<String, String>> groupData =
                    new ArrayList<HashMap<String, String>>();
            ArrayList<ArrayList<HashMap<String, String>>> childData =
                    new ArrayList<ArrayList<HashMap<String, String>>>();
            JSONObject currService, currBus;
            HashMap<String, String> curGroupMap;
            JSONArray buses;
            ArrayList<HashMap<String, String>> children;
            HashMap<String, String> curChildMap;
            int a = services.length();
            int b;
            String serviceName;

            for(int i = 0; i < a; i++) {
                currService = services.getJSONObject(i);
                serviceName = currService.getString("serviceName");
                if(!showNightBuses && serviceName.startsWith("N")) continue;
                curGroupMap = new HashMap<String, String>();
                groupData.add(curGroupMap);
                curGroupMap.put(SERVICE_NAME_KEY, serviceName + " " +
                        currService.getString("route"));
                buses = currService.getJSONArray("buses");
                children = new ArrayList<HashMap<String, String>>();
                b = buses.length();
                for(int j = 0; j < b; j++) {
                    currBus = buses.getJSONObject(j);
                    curChildMap = new HashMap<String, String>();
                    children.add(curChildMap);
                    curChildMap.put(DESTINATION_KEY,
                            currBus.getString("destination"));
                    curChildMap.put(ARRIVAL_TIME_KEY,
                            currBus.getString("arrivalTime"));
                }
                childData.add(children);
            }

            ExpandableListAdapter listAdapter = new SimpleExpandableListAdapter(
                    this, groupData,
                    android.R.layout.simple_expandable_list_item_1,
                    new String[] { SERVICE_NAME_KEY, ARRIVAL_TIME_KEY },
                    new int[] { android.R.id.text1, android.R.id.text2 },
                    childData,android.R.layout.simple_expandable_list_item_2,
                    new String[] { ARRIVAL_TIME_KEY, DESTINATION_KEY },
                    new int[] { android.R.id.text1, android.R.id.text2 });
            setListAdapter(listAdapter);
            
            if(progressDialogShown) dismissDialog(PROGRESS_DIALOG);
            if(autoRefresh) setUpAutoRefresh();
        } catch(JSONException e) {
            handleError(ERROR_PARSEERR);
        }
    }

    private void setUpAutoRefresh() {
        Message msg = mHandler.obtainMessage(99);
        Bundle bundle = new Bundle();
        bundle.putBoolean("refresh", true);
        msg.setData(bundle);
        mHandler.sendMessageDelayed(msg, 60000);
    }
}