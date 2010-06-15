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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
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
 * perform. This class also deals with the initialisation and updating of the
 * bus stop database.
 *
 * @author Niall Scott
 */
public class MainActivity extends Activity {

    private ImageButton favouriteButton;
    private ImageButton stopCodeButton;
    private ImageButton stopMapButton;
    private ImageButton preferencesButton;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        favouriteButton = (ImageButton) findViewById(R.id.favouriteButton);
        stopCodeButton = (ImageButton) findViewById(R.id.stopCodeButton);
        stopMapButton = (ImageButton) findViewById(R.id.stopMapButton);
        preferencesButton = (ImageButton) findViewById(R.id.stopSettingsButton);

        favouriteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(MainActivity.this,
                        FavouriteStopsActivity.class));
            }
        });

        stopCodeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(MainActivity.this,
                        EnterStopCodeActivity.class));
            }
        });

        stopMapButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(MainActivity.this,
                        BusStopMapActivity.class));
            }
        });

        preferencesButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(MainActivity.this,
                        PreferencesActivity.class));
            }
        });

        new Thread(stopDBTasks).start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, 0, 2, R.string.about_title)
                .setIcon(R.drawable.ic_menu_info_details);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        startActivity(new Intent(this, AboutActivity.class));
        return super.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    private Runnable stopDBTasks = new Runnable() {
        @Override
        public void run() {
            initStopDB();
            try {
                BusStopDatabase.getInstance(getApplicationContext())
                        .getLastDBModTime();
            } catch(SQLiteException e) {
                File f = getDatabasePath(BusStopDatabase.STOP_DB_NAME);
                if(f.exists()) f.delete();
                initStopDB();
            }
            checkForDBUpdates();
        }
    };

    /**
     * Initialise the bus stop database. This is only normally run the first
     * time a user runs the application so that the default map database is
     * moved from the assets directory to the working data directory.
     */
    private void initStopDB() {
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
            } catch(IOException e) { }
        }
    }

    /**
     * Check with the remote server to see if any database updates exist for the
     * database. This gets checked upon app startup if it hasn't been checked
     * for more than 24 hours. If a database update does exist, its downloaded.
     */
    private void checkForDBUpdates() {
        SharedPreferences sp = getSharedPreferences(
                PreferencesActivity.PREF_FILE, 0);
        boolean autoUpdate = sp.getBoolean("pref_database_autoupdate", true);

        if(autoUpdate) {
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
                writer.println("getDBLastModTime");
                dbLastModStr = reader.readLine();
                writer.println("getDBURL");
                dbURL = reader.readLine();
                writer.println("exit");
                writer.close();
                reader.close();
                sock.close();
            } catch(UnknownHostException e) {
                return;
            } catch(IOException e) {
                return;
            }

            if(dbLastModStr == null || dbLastModStr.length() == 0) return;
            long dbLastMod;
            try {
                dbLastMod = Long.parseLong(dbLastModStr);
            } catch(NumberFormatException e) {
                return;
            }
            if(dbLastMod > BusStopDatabase.getInstance(getApplicationContext())
                        .getLastDBModTime())
            {
                updateStopsDB(getApplicationContext(), dbURL);
            }
            SharedPreferences.Editor edit = sp.edit();
            edit.putLong("lastUpdateCheck", System.currentTimeMillis());
            edit.commit();
        }
    }

    /**
     * Download the stop database from the server and put it in the
     * application's working data directory.
     *
     * @param context The context to use this method with.
     * @param url The URL of the bus stop database to download.
     */
    private static void updateStopsDB(final Context context,
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
