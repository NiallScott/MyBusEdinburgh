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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.SQLException;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.bugsense.trace.BugSenseHandler;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Random;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The main activity in the application. This activity displays a the main menu
 * of the application to the user where they select the action they want to
 * perform. This class also deals with the initialisation and updating of the
 * bus stop database.
 *
 * @author Niall Scott
 */
public class MainActivity extends Activity {
    
    private static final int DIALOG_ABOUT = 0;
    
    private static final String DB_API_CHECK_URL =
            "http://www.mybustracker.co.uk/ws.php?module=json&function=" +
            "getTopoId&key=";
    private static final String DB_UPDATE_CHECK_URL =
            "http://edinb.us/api/DatabaseVersion?schemaType=" +
            BusStopDatabase.SCHEMA_NAME + "&random=";
    private static final Random random = new Random();
    private static final boolean isHoneycombOrGreater =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;

    private Button favouriteButton;
    private Button stopCodeButton;
    private Button stopMapButton;
    private Button nearestButton;
    private Button newsButton;
    private Button alertButton;

    private TextView txtDBVersion, txtTopoVersion;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugSenseHandler.setup(this, ApiKey.BUGSENSE_KEY);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
            this.getSharedPreferences(PreferencesActivity.PREF_FILE, 0)
                    .registerOnSharedPreferenceChangeListener(
                    new SharedPreferencesListener(this));
        setContentView(R.layout.home);
        
        favouriteButton = (Button)findViewById(R.id.home_btn_favourites);
        stopCodeButton = (Button)findViewById(R.id.home_btn_entercode);
        stopMapButton = (Button)findViewById(R.id.home_btn_map);
        nearestButton = (Button)findViewById(R.id.home_btn_nearest);
        newsButton = (Button)findViewById(R.id.home_btn_news);
        alertButton = (Button)findViewById(R.id.home_btn_alerts);

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

        nearestButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(MainActivity.this,
                        NearestStopsActivity.class));
            }
        });

        newsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(MainActivity.this,
                        NewsUpdatesActivity.class));
            }
        });
        
        alertButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(MainActivity.this,
                        AlertManagerActivity.class));
            }
        });
        
        new Thread(stopDBTasks).start();
        
        if(getSharedPreferences(PreferencesActivity.PREF_FILE, 0)
                .getBoolean("pref_startupshowfavs_state", false)) {
            startActivity(new Intent(this, FavouriteStopsActivity.class));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_option_menu, menu);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch(item.getItemId()) {
            case R.id.main_option_menu_preferences:
                startActivity(new Intent(this, PreferencesActivity.class));
                break;
            case R.id.main_option_menu_about:
                showDialog(DIALOG_ABOUT);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Dialog onCreateDialog(final int id) {
        switch(id) {
            case DIALOG_ABOUT:
                LayoutInflater inflater = (LayoutInflater)getSystemService(
                        LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.about,
                        (ViewGroup)findViewById(R.id.aboutRoot));

                TextView temp = (TextView)layout.findViewById(
                        R.id.aboutVersion);
                try {
                    temp.setText(getText(R.string.version) + " " +
                        getPackageManager().getPackageInfo(getPackageName(),
                        0).versionName + " (#" + getPackageManager()
                        .getPackageInfo(getPackageName(), 0).versionCode +
                        ")");
                } catch(NameNotFoundException e) {
                    // This should never occur.
                    temp.setText("Unknown");
                }

                txtDBVersion = (TextView)layout.findViewById(R.id
                        .aboutDBVersion);
                txtTopoVersion = (TextView)layout.findViewById(
                        R.id.aboutTopoVersion);
                
                AlertDialog.Builder builder;
                if(isHoneycombOrGreater) {
                    //builder = new AlertDialog.Builder(this,
                            //AlertDialog.THEME_HOLO_DARK);
                    builder = getHoneycombDialog(this);
                } else {
                    builder = new AlertDialog.Builder(this);
                }
                builder.setView(layout)
                        .setNegativeButton(R.string.close,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int id) {
                                dialog.dismiss();
                            }
                        });
                
                return builder.create();
            default:
                return null;
        }
    }

    @Override
    protected void onPrepareDialog(final int id, final Dialog d) {
        switch(id) {
            case DIALOG_ABOUT:
                long dbtime;
                Calendar date = Calendar.getInstance();
                BusStopDatabase bsd = BusStopDatabase.getInstance(this);
                try {
                    dbtime = bsd.getLastDBModTime();
                } catch(SQLException e) {
                    dbtime = 0;
                }
                date.setTimeInMillis(dbtime);

                txtDBVersion.setText(getText(R.string
                        .main_aboutdialog_dbversion) + ": " + dbtime + " (" +
                        date.getTime().toLocaleString() + ")");
                txtTopoVersion.setText(
                        getText(R.string.main_aboutdialog_topology) + ": " +
                        bsd.getTopoId());
                break;
            default:
                break;
        }
    }

    private Runnable stopDBTasks = new Runnable() {
        @Override
        public void run() {
            File toDelete = getDatabasePath("busstops.db");
            if(toDelete.exists()) toDelete.delete();
            
            toDelete = getDatabasePath("busstops2.db");
            if(toDelete.exists()) toDelete.delete();
            
            checkForDBUpdates(getApplicationContext(), false);
        }
    };
    
    public static AlertDialog.Builder getHoneycombDialog(
            final Context context) {
        try {
            Class cls = AlertDialog.Builder.class;
            Class[] partypes = new Class[2];
            partypes[0] = Context.class;
            partypes[1] = Integer.TYPE;
            Constructor ct = cls.getConstructor(partypes);
            Object[] arglist = new Object[2];
            arglist[0] = context;
            arglist[1] = new Integer(AlertDialog.THEME_HOLO_DARK);
            return (AlertDialog.Builder)ct.newInstance(arglist);
        } catch(NoSuchMethodException e) {
            
        } catch(InstantiationException e) {
            
        } catch(IllegalAccessException e) {
            
        } catch(InvocationTargetException e) {
            
        }
        
        return null;
    }
    
    /**
     * Check for updates to the bus stop database. This may happen automatically
     * if 24 hours have elapsed since the last check, or if the user has forced
     * the action. If a database update is found, then the new database is
     * downloaded and placed in the correct location.
     * 
     * @param context The context.
     * @param force True if the user forced the check, false if not.
     */
    public static void checkForDBUpdates(final Context context,
            final boolean force) {
        SharedPreferences sp = context.getSharedPreferences(
                PreferencesActivity.PREF_FILE, 0);
        boolean autoUpdate = sp.getBoolean("pref_database_autoupdate", true);
        SharedPreferences.Editor edit = sp.edit();
        
        if(autoUpdate || force) {
            if(!force) {
                long lastCheck = sp.getLong("lastUpdateCheck", 0);
                if((System.currentTimeMillis() - lastCheck) < 86400000) return;
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append(DB_API_CHECK_URL);
            sb.append(ApiKey.getHashedKey());
            sb.append("&random=");
            sb.append(random.nextInt());
            try {
                URL url = new URL(sb.toString());
                sb.setLength(0);
                HttpURLConnection conn = (HttpURLConnection)url
                        .openConnection();
                try {
                    BufferedInputStream is = new BufferedInputStream(
                            conn.getInputStream());
                    int data;
                    while((data = is.read()) != -1) {
                        sb.append((char)data);
                    }
                } finally {
                    conn.disconnect();
                }
            } catch(MalformedURLException e) {
                return;
            } catch(IOException e) {
                return;
            }
            
            String topoId;
            try {
                JSONObject jo = new JSONObject(sb.toString());
                topoId = jo.getString("topoId");
            } catch(JSONException e) {
                return;
            }
            
            if(topoId == null || topoId.length() == 0) return;
            
            BusStopDatabase bsd = BusStopDatabase.getInstance(context);
            final String dbTopoId = bsd.getTopoId();
            
            if(topoId.equals(dbTopoId)) {
                edit.putLong("lastUpdateCheck", System.currentTimeMillis());
                edit.commit();
                if(force) {
                    Looper.prepare();
                    Toast.makeText(context, R.string.main_db_no_updates,
                            Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
                return;
            }
            
            sb.setLength(0);
            sb.append(DB_UPDATE_CHECK_URL);
            sb.append(random.nextInt());
            sb.append("&key=");
            sb.append(ApiKey.getHashedKey());
            
            try {
                URL url = new URL(sb.toString());
                sb.setLength(0);
                HttpURLConnection conn = (HttpURLConnection)url
                        .openConnection();
                try {
                    BufferedInputStream is = new BufferedInputStream(
                            conn.getInputStream());
                    int data;
                    while((data = is.read()) != -1) {
                        sb.append((char)data);
                    }
                } finally {
                    conn.disconnect();
                }
            } catch(MalformedURLException e) {
                return;
            } catch(IOException e) {
                return;
            }
            
            String dbUrl, schemaVersion, checksum;
            try {
                JSONObject jo = new JSONObject(sb.toString());
                dbUrl = jo.getString("db_url");
                schemaVersion = jo.getString("db_schema_version");
                topoId = jo.getString("topo_id");
                checksum = jo.getString("checksum");
            } catch(JSONException e) {
                return;
            }
            
            if(!BusStopDatabase.SCHEMA_NAME.equals(schemaVersion)) return;
            if(topoId == null || topoId.length() == 0) return;
            if(dbUrl == null || dbUrl.length() == 0) return;
            if(checksum == null || checksum.length() == 0) return;
            
            if(!topoId.equals(dbTopoId)) {
                updateStopsDB(context, dbUrl, checksum);
            } else if(force) {
                Looper.prepare();
                Toast.makeText(context, R.string.main_db_no_updates,
                        Toast.LENGTH_LONG).show();
                Looper.loop();
            }
            
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
            final String url, final String checksum) {
        if(context == null || url == null || url.length() == 0 ||
                checksum == null || checksum.length() == 0) return;
        try {
            URL u = new URL(url);
            HttpURLConnection con = (HttpURLConnection)u.openConnection();
            InputStream in = con.getInputStream();
            File temp = context.getDatabasePath(BusStopDatabase.STOP_DB_NAME +
                    "_temp");
            File dest = context.getDatabasePath(BusStopDatabase.STOP_DB_NAME);
            FileOutputStream out = new FileOutputStream(temp);
            byte[] buf = new byte[1024];
            int len;
            while((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();
            out.close();
            in.close();
            con.disconnect();
            
            if(!md5Checksum(temp).equalsIgnoreCase(checksum)) {
                temp.delete();
                return;
            }
            
            BusStopDatabase bsd = BusStopDatabase.getInstance(context);
            synchronized(bsd) {
                bsd.getReadableDatabase().close();
                dest.delete();
                temp.renameTo(dest);
            }
            
            Looper.prepare();
            Toast.makeText(context, R.string.main_db_updated, Toast.LENGTH_LONG)
                    .show();
            Looper.loop();
        } catch(MalformedURLException e) {
        } catch(IOException e) { }
    }
    
    /**
     * Create a checksum for a File. This is used to ensure that a downloaded
     * database has not been corrupted or incomplete.
     * 
     * See: http://vyshemirsky.blogspot.com/2007/08/computing-md5-digest-checksum-in-java.html
     * This has been slightly modified.
     * 
     * @param file The file to run the MD5 checksum against.
     * @return The MD5 checksum string.
     */
    public static String md5Checksum(final File file) {
        try {
            InputStream fin = new FileInputStream(file);
            MessageDigest md5er = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024];
            int read;
            
            while((read = fin.read(buffer)) != -1) {
                if(read > 0) md5er.update(buffer, 0, read);
            }
            fin.close();
            
            byte[] digest = md5er.digest();
            if(digest == null) return null;
            StringBuilder builder = new StringBuilder();
            for(byte a : digest) {
                builder.append(Integer.toString((a & 0xff) 
                + 0x100, 16).substring(1));
            }
            
            return builder.toString();
        } catch(FileNotFoundException e) {
            return "";
        } catch(NoSuchAlgorithmException e) {
            return "";
        } catch(IOException e) {
            return "";
        }
    }
    
    public static class BackupSupport {
        
        public static void dataChanged(final String packageName) {
            BackupManager.dataChanged(packageName);
        }
    }
    
    public static class SharedPreferencesListener
            implements OnSharedPreferenceChangeListener {
        
        final Context context;
        
        public SharedPreferencesListener(final Context context) {
            this.context = context;
        }

        @Override
        public void onSharedPreferenceChanged(final SharedPreferences sp,
                final String key) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
                BackupSupport.dataChanged(context.getPackageName());
        }
    }
}
