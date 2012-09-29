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

import uk.org.rivernile.android.utils.GenericDialogPreference;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.Toast;
import uk.org.rivernile.android.utils.ActionBarCompat;

/**
 * The preferences dialog of the application. There is not much code here, it is
 * mostly defined in res/xml/preferences.xml.
 * 
 * TODO: convert this in to a PreferenceFragment when Google add it to the
 * compatibility package.
 *
 * @author Niall Scott
 */
public class PreferencesActivity extends PreferenceActivity
        implements OnSharedPreferenceChangeListener {

    /** The name of the preferences file. */
    public final static String PREF_FILE = "preferences";
    /** The AUTOREFRESH_STATE key in the preferences. */
    public final static String KEY_AUTOREFRESH_STATE = "pref_autorefresh_state";
    
    private final static boolean IS_HONEYCOMB_OR_GREATER =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    
    private SettingsDatabase sd;
    private ListPreference numberOfDeparturesPref;
    private String[] numberOfDeparturesStrings;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(IS_HONEYCOMB_OR_GREATER) {
            ActionBarCompat.setDisplayHomeAsUpEnabled(this, true);
        }
        
        getPreferenceManager().setSharedPreferencesName(PREF_FILE);
        addPreferencesFromResource(R.xml.preferences);
        sd = SettingsDatabase.getInstance(this);
        numberOfDeparturesStrings = getResources()
                .getStringArray(R.array.preferences_num_departures_entries);

        GenericDialogPreference backupDialog = (GenericDialogPreference)
                findPreference("pref_backup_favourites");
        GenericDialogPreference restoreDialog = (GenericDialogPreference)
                findPreference("pref_restore_favourites");
        GenericDialogPreference clearSearchHistoryDialog =
                (GenericDialogPreference)findPreference(
                "pref_clear_search_history");
        GenericDialogPreference checkStopDBUpdates =
                (GenericDialogPreference)findPreference("pref_update_stop_db");
        numberOfDeparturesPref = (ListPreference)findPreference(
                "pref_numberOfShownDeparturesPerService");

        backupDialog.setOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                if(which != DialogInterface.BUTTON_POSITIVE) {
                    dialog.dismiss();
                    return;
                }
                String message = sd.backupDatabase();
                if(message.equals("success")) {
                    Toast.makeText(getApplicationContext(),
                            R.string.preference_backup_success,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), message,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        restoreDialog.setOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                if(which != DialogInterface.BUTTON_POSITIVE) {
                    dialog.dismiss();
                    return;
                }
                String message = sd.restoreDatabase();
                if(message.equals("success")) {
                    Toast.makeText(getApplicationContext(),
                            R.string.preference_restore_success,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), message,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        clearSearchHistoryDialog.setOnClickListener(
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                if(which != DialogInterface.BUTTON_POSITIVE) {
                    dialog.dismiss();
                    return;
                }
                SearchRecentSuggestions suggestions =
                        new SearchRecentSuggestions(PreferencesActivity.this,
                        MapSearchHistoryProvider.AUTHORITY,
                        MapSearchHistoryProvider.MODE);
                suggestions.clearHistory();
            }
        });

        checkStopDBUpdates.setOnClickListener(
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                if(which != DialogInterface.BUTTON_POSITIVE) {
                    dialog.dismiss();
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Application.checkForDBUpdates(getApplicationContext(),
                                true);
                    }
                }).start();
            }
        });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        
        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        sp.registerOnSharedPreferenceChangeListener(this);
        setNumberOfDeparturesSummary(sp);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();
        
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sp,
            final String key) {
        if("pref_numberOfShownDeparturesPerService".equals(key)) {
            setNumberOfDeparturesSummary(sp);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            switch(item.getItemId()) {
                case android.R.id.home:
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
    
    /**
     * Set the summary text for number of departures.
     * 
     * @param sp An object which represents the SharedPreferences file in use.
     */
    private void setNumberOfDeparturesSummary(final SharedPreferences sp) {
        String s = sp.getString("pref_numberOfShownDeparturesPerService", "4");
        int val;
        
        try {
            val = Integer.parseInt(s);
        } catch(NumberFormatException e) {
            val = 4;
        }
        
        numberOfDeparturesPref.setSummary(numberOfDeparturesStrings[val - 1]);
    }
}