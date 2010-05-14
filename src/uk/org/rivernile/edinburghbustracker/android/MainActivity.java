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

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * The main activity in the application. This activity displays a the main menu
 * of the application to the user where they select the action they want to
 * perform.
 *
 * @author Niall Scott
 */
public class MainActivity extends ListActivity {

    private Thread initStopDBThread;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayAdapter ad = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1);
        ad.add(getString(R.string.main_favourite_stops));
        ad.add(getString(R.string.main_enter_stop_code));
        ad.add(getString(R.string.main_bus_stop_map));
        ad.add(getString(R.string.preferences));
        ad.add(getString(R.string.about_title));
        setListAdapter(ad);
        initStopDBThread = new Thread(initStopDBTask);
        initStopDBThread.start();
        new Thread(updateTask).start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onListItemClick(final ListView l, final View v,
            final int position, final long id) {
        switch(position) {
            case 0:
                startActivity(new Intent(this, FavouriteStopsActivity.class));
                break;
            case 1:
                startActivity(new Intent(this, EnterStopCodeActivity.class));
                break;
            case 2:
                startActivity(new Intent(this, BusStopMapActivity.class));
                break;
            case 3:
                startActivity(new Intent(this, PreferencesActivity.class));
                break;
            case 4:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
    }

    private Runnable initStopDBTask = new Runnable() {
        @Override
        public void run() {
            File f = getDatabasePath(BusStopDatabase.STOP_DB_NAME);
            if(!f.exists()) {
                try {
                    // Start of horrible hack to create database directory and
                    // set permissions if it doesn't already exist.
                    SQLiteDatabase db = MainActivity.this.openOrCreateDatabase(
                            BusStopDatabase.STOP_DB_NAME, 0, null);
                    db.close();
                    // End of horrible hack.
                    InputStream in = getAssets().open(
                            BusStopDatabase.STOP_DB_NAME);
                    FileOutputStream out = new FileOutputStream(f);
                    byte[] buf = new byte[1024];
                    int len;
                    while((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.flush();
                    out.close();
                    in.close();
                } catch(IOException e) {
                }
            }
        }
    };

    private Runnable updateTask = new Runnable() {
        @Override
        public void run() {
            SharedPreferences sp = getSharedPreferences(
                    PreferencesActivity.PREF_FILE, 0);
            boolean autoUpdate = sp.getBoolean("pref_database_autoupdate",
                    true);
            boolean updateCheck = sp.getBoolean("pref_appupdatescheck_state",
                    true);
            if(!autoUpdate && !updateCheck) return;
            long lastCheck = sp.getLong("lastUpdateCheck", 0);

            if((System.currentTimeMillis() - lastCheck) < 86400000) return;
            String remoteHost = sp.getString(PreferencesActivity.KEY_HOSTNAME,
                    "bustracker.selfip.org");
            int remotePort;
            try {
                remotePort = Integer.parseInt(sp.getString(
                        PreferencesActivity.KEY_PORT, "4876"));
            } catch(NumberFormatException e) {
                remotePort = 4876;
            }

            String latestClientVersionStr = "";
            String dbLastModStr = "";
            String dbURL = "";
            try {
                Socket sock = new Socket();
                sock.setSoTimeout(20000);
                sock.connect(new InetSocketAddress(remoteHost, remotePort),
                        20000);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(sock.getInputStream()));
                PrintWriter writer = new PrintWriter(sock.getOutputStream(),
                        true);
                if(updateCheck) {
                    writer.println("getLatestAndroidClientVersion");
                    latestClientVersionStr = reader.readLine();
                }
                if(autoUpdate) {
                    writer.println("getDBLastModTime");
                    dbLastModStr = reader.readLine();
                    writer.println("getDBURL");
                    dbURL = reader.readLine();
                }
                writer.println("exit");
                writer.close();
                reader.close();
                sock.close();
            } catch(UnknownHostException e) {
                return;
            } catch(IOException e) {
                return;
            }

            if(updateCheck && latestClientVersionStr != null &&
                    latestClientVersionStr.length() > 0 &&
                    !latestClientVersionStr.equals("Unknown"))
            {
                try {
                    int latestClientVersion;
                    latestClientVersion = Integer.parseInt(
                            latestClientVersionStr);
                    int currentVersion = getPackageManager().getPackageInfo(
                            getPackageName(), 0).versionCode;
                    if(latestClientVersion > currentVersion) {
                        // What to do if there's a new client version
                    }
                } catch(NumberFormatException e) {
                } catch(PackageManager.NameNotFoundException e) { }
            }

            if(dbLastModStr == null || dbLastModStr.length() == 0 ||
                    !autoUpdate) return;
            long dbLastMod;
            try {
                dbLastMod = Long.parseLong(dbLastModStr);
            } catch(NumberFormatException e) {
                return;
            }
            if(dbLastMod > BusStopDatabase.getInstance(getApplicationContext())
                    .getLastDBModTime())
            {
                try {
                    initStopDBThread.join();
                } catch(InterruptedException e) { }
                updateStopsDB(getApplicationContext(), dbURL);
            }
            SharedPreferences.Editor edit = sp.edit();
            edit.putLong("lastUpdateCheck", System.currentTimeMillis());
            edit.commit();
        }
    };

    public static synchronized void updateStopsDB(final Context context,
            final String url)
    {
        if(context == null || url == null || url.length() == 0) return;
        try {
            URL u = new URL(url);
            HttpURLConnection con = (HttpURLConnection)u.openConnection();
            InputStream in = con.getInputStream();
            File temp = context.getDatabasePath(BusStopDatabase.STOP_DB_NAME +
                    "_temp");
            File dest = context.getDatabasePath(BusStopDatabase.STOP_DB_NAME);
            FileOutputStream out = new FileOutputStream(temp);
            BusStopDatabase.getInstance(context).finalize();
            byte[] buf = new byte[1024];
            int len;
            while((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();
            out.close();
            in.close();
            con.disconnect();
            dest.delete();
            temp.renameTo(dest);
            Looper.prepare();
            Toast.makeText(context, R.string.main_db_updated, Toast.LENGTH_LONG)
                    .show();
            Looper.loop();
        } catch(MalformedURLException e) {
        } catch(IOException e) { }
    }
}
