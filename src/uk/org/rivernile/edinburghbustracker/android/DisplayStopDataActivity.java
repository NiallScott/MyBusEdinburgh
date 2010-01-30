/*
 * Copyright (C) 2009 Niall 'Rivernile' Scott
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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListAdapter;
import android.widget.SimpleExpandableListAdapter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The DisplayStopDataActivity displays the bus stop information to the user
 * once they have selected a bus stop to view data for in an ExpandableListView.
 *
 * @author Niall Scott
 */
public class DisplayStopDataActivity extends ExpandableListActivity
        implements Runnable {

    private final static int AUTO_REFRESH_ID = Menu.FIRST;
    private final static int REFRESH_ID = Menu.FIRST + 1;

    private final static int PROGRESS_DIALOG = 0;
    private final static int ERROR_DIALOG = 1;

    private final static String STOP_DATA_COMMAND = "getBusTimesByStopCode";
    
    private final static String SERVICE_NAME_KEY = "SERVICE_NAME";
    private final static String ROUTE_KEY = "ROUTE";
    private final static String DESTINATION_KEY = "DESTINATION";
    private final static String ARRIVAL_TIME_KEY = "ARRIVAL_TIME";
    private final static String ACCESSIBLE_KEY = "ACCESSIBLE";

    /** The ACTION_VIEW_STOP_DATA intent action name. */
    public final static String ACTION_VIEW_STOP_DATA =
            "uk.org.rivernile.edinburghbustracker.android."
            + "ACTION_VIEW_STOP_DATA";

    private boolean autoRefresh, cancel = false;
    private String remoteHost;
    private int remotePort;
    private String stopCode;
    private JSONObject jo;
    private String errorString;
    private Thread sockThread, afThread;
    private ExpandableListAdapter listAdapter;
    private SharedPreferences sp;
    private long lastRefresh;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.displaystopdata_title);
        setContentView(R.layout.displaystopdata);

        stopCode = getIntent().getStringExtra("stopCode");
        if (stopCode == null || stopCode.length() == 0) {
            doError(getString(R.string.displaystopdata_err_nocode));
            finish();
            return;
        }

        sp = getSharedPreferences(PreferencesActivity.PREF_FILE, 0);
        autoRefresh = sp.getBoolean(PreferencesActivity.KEY_AUTOREFRESH_STATE,
                false);
        remoteHost = sp.getString(PreferencesActivity.KEY_HOSTNAME,
                "bustracker.selfip.org");
        try {
            remotePort = Integer.parseInt(sp.getString(
                    PreferencesActivity.KEY_PORT, "4876"));
        } catch(NumberFormatException e) {
            remotePort = 4876;
        }

        doGetBusTimesTask();
        if(autoRefresh) {
            afThread = new Thread(afTask);
            afThread.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        autoRefresh = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        autoRefresh = sp.getBoolean(PreferencesActivity.KEY_AUTOREFRESH_STATE,
                false);
        if(autoRefresh) {
            afThread = new Thread(afTask);
            afThread.start();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);

        if(autoRefresh) {
            menu.add(0, AUTO_REFRESH_ID, 1,
                    R.string.displaystopdata_menu_turnautorefreshoff);
        } else {
            menu.add(0, AUTO_REFRESH_ID, 1,
                    R.string.displaystopdata_menu_turnautorefreshon);
        }
        menu.add(0, REFRESH_ID, 2, R.string.displaystopdata_menu_refresh);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case AUTO_REFRESH_ID:
                handleAutoRefreshMenuItem(item);
                break;
            case REFRESH_ID:
                doGetBusTimesTask();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch (id) {
            case PROGRESS_DIALOG:
                ProgressDialog d = new ProgressDialog(this);
                d.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                d.setCancelable(true);
                d.setMessage(getString(R.string.displaystopdata_gettingdata));
                d.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface di) {
                        cancel = true;
                        finish();
                    }
                });
                dialog = d;
                break;
            case ERROR_DIALOG:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(errorString).setCancelable(false)
                        .setTitle(R.string.error)
                        .setPositiveButton(R.string.retry,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int id) {
                                doGetBusTimesTask();
                            }
                        }).setNegativeButton(R.string.cancel,
                                new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int i) {
                                DisplayStopDataActivity.this.finish();
                            }
                });
                dialog = builder.create();
                break;
            default:
                dialog = null;
                break;
        }
        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, final Dialog dialog) {
        if(id == ERROR_DIALOG) {
            AlertDialog alert = (AlertDialog)dialog;
            alert.setMessage(errorString);
        }
    }

    /**
     * Handle the auto-refresh menu item click event. This alternates between
     * turning it on and off.
     *
     * @param item The menu item object of the auto-refresh menu item. Used to
     * edit its text content.
     */
    private void handleAutoRefreshMenuItem(final MenuItem item) {
        if (autoRefresh) {
            autoRefresh = false;
            item.setTitle(R.string.displaystopdata_menu_turnautorefreshon);
        } else {
            autoRefresh = true;
            item.setTitle(R.string.displaystopdata_menu_turnautorefreshoff);
            afThread = new Thread(afTask);
            afThread.start();
        }
    }

    @Override
    public void run() {
        try {
            // Set up socket stuff.
            Socket sock = new Socket();
            sock.setSoTimeout(20000);
            sock.connect(new InetSocketAddress(remoteHost, remotePort), 20000);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(sock.getInputStream()));
            PrintWriter writer = new PrintWriter(sock.getOutputStream(), true);
            writer.println(STOP_DATA_COMMAND + ":" + stopCode);
            String jsonString = "";
            String tmp = "";
            boolean readJson = false;
            while ((tmp = reader.readLine()) != null) {
                if(tmp.startsWith("Error:")) {
                    doError(getString(R.string.displaystopdata_err_serverr));
                    writer.println("exit");
                    reader.close();
                    writer.close();
                    sock.close();
                    return;
                }
                if (tmp.equals("+")) {
                    readJson = true;
                } else if (tmp.equals("-")) {
                    break;
                } else if (readJson) {
                    jsonString = jsonString + tmp;
                }
            }
            jo = new JSONObject(jsonString);
            writer.println("exit");
            reader.close();
            writer.close();
            sock.close();
            Message msg = handler.obtainMessage();
            Bundle b = new Bundle();
            b.putBoolean("finished", true);
            msg.setData(b);
            handler.sendMessage(msg);
        } catch (UnknownHostException e) {
            doError(getString(R.string.displaystopdata_err_noresolv));
        } catch (IOException e) {
            doError(getString(R.string.displaystopdata_err_noconn));
        } catch (JSONException e) {
            doError(getString(R.string.displaystopdata_err_parseerr));
        }
    }

    private void doGetBusTimesTask() {
        cancel = false;
        showDialog(PROGRESS_DIALOG);
        sockThread = new Thread(this);
        sockThread.start();
    }

    /**
     * A convienience method for when an error occurs. This method exists to
     * prevent repetition of code.
     *
     * @param error The error string to display.
     */
    private void doError(final String error) {
        Message msg = handler.obtainMessage();
        Bundle b = new Bundle();
        b.putBoolean("isError", true);
        b.putString("errorString", error);
        msg.setData(b);
        handler.sendMessage(msg);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            if (msg.getData().getBoolean("isError")) {
                errorString = msg.getData().getString("errorString");
                dismissDialog(PROGRESS_DIALOG);
                showDialog(ERROR_DIALOG);
            } else if(msg.getData().getBoolean("refresh")) {
                doGetBusTimesTask();
            } else if(msg.getData().getBoolean("finished")) {
                dismissDialog(PROGRESS_DIALOG);
                if(cancel) return;
                try {
                    String stopCode = jo.getString("stopCode");
                    if(stopCode.length() == 0) {
                        doError(getString(R.string.displaystopdata_err_nodata));
                        return;
                    }
                    String stopName = jo.getString("stopName");
                    setTitle(getString(R.string.displaystopdata_title2) + " " +
                            stopCode + " " + stopName);

                    JSONArray services = jo.getJSONArray("services");
                    if(services.length() == 0) return;

                    List<Map<String, String>> groupData =
                            new ArrayList<Map<String, String>>();
                    List<List<Map<String, String>>> childData =
                            new ArrayList<List<Map<String, String>>>();
                    JSONObject currService, currBus;
                    Map<String, String> curGroupMap;
                    JSONArray buses;
                    List<Map<String, String>> children;
                    Map<String, String> curChildMap;
                    
                    for(int i = 0; i < services.length(); i++) {
                        currService = services.getJSONObject(i);
                        curGroupMap = new HashMap<String, String>();
                        groupData.add(curGroupMap);
                        curGroupMap.put(SERVICE_NAME_KEY,
                                currService.getString("serviceName"));
                        curGroupMap.put(ROUTE_KEY,
                                currService.getString("route"));
                        buses = currService.getJSONArray("buses");
                        children = new ArrayList<Map<String, String>>();
                        for(int j = 0; j < buses.length(); j++) {
                            currBus = buses.getJSONObject(j);
                            if(j == 0) {
                                curGroupMap.put(ARRIVAL_TIME_KEY,
                                        currBus.getString("arrivalTime"));
                            }
                            curChildMap = new HashMap<String, String>();
                            children.add(curChildMap);
                            curChildMap.put(DESTINATION_KEY,
                                    currBus.getString("destination"));
                            curChildMap.put(ARRIVAL_TIME_KEY,
                                    currBus.getString("arrivalTime"));
                        }
                        childData.add(children);
                    }

                    listAdapter = new SimpleExpandableListAdapter(
                            DisplayStopDataActivity.this,
                            groupData,
                            android.R.layout.simple_expandable_list_item_1,
                            new String[] { SERVICE_NAME_KEY, ROUTE_KEY,
                    ARRIVAL_TIME_KEY },
                            new int[] { android.R.id.text1, android.R.id.text2 },
                            childData,
                            android.R.layout.simple_expandable_list_item_2,
                            new String[] { DESTINATION_KEY, ARRIVAL_TIME_KEY },
                            new int[] { android.R.id.text1, android.R.id.text2 }
                            );
                    setListAdapter(listAdapter);
                    setLastRefresh(System.currentTimeMillis());
                    if(autoRefresh) {
                        afThread = new Thread(afTask);
                        afThread.start();
                    }
                } catch(JSONException e) {
                    doError(getString(R.string.displaystopdata_err_parseerr));
                }
            }
        }
    };

    private synchronized long getLastRefresh() {
        return lastRefresh;
    }

    private synchronized void setLastRefresh(long lr) {
        lastRefresh = lr;
    }

    private Runnable afTask = new Runnable() {
        @Override
        public void run() {
            Message msg = handler.obtainMessage();
            Bundle b = new Bundle();
            b.putBoolean("refresh", true);
            while(autoRefresh) {
                if(System.currentTimeMillis() >= (getLastRefresh() + 10000)) {
                    msg.setData(b);
                    handler.sendMessage(msg);
                    return;
                }
                try {
                    Thread.sleep(20);
                } catch(InterruptedException e) {
                }
            }
        }
    };
}
