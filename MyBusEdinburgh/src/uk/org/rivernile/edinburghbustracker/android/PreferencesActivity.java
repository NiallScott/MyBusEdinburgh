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

package uk.org.rivernile.edinburghbustracker.android;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.provider.SearchRecentSuggestions;
import android.view.MenuItem;
import android.widget.Toast;
import uk.org.rivernile.android.utils.GenericDialogPreference;
import uk.org.rivernile.android.utils.NavigationUtils;

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
    public static final String PREF_FILE = "preferences";
    
    /** The Preference for showing the favourites list on app startup. */
    public static final String PREF_STARTUP_SHOW_FAVS =
            "pref_startupshowfavs_state";
    /** The Preference for automatically updating the bus stop database. */
    public static final String PREF_DATABASE_AUTO_UPDATE =
            "pref_database_autoupdate";
    /** The Preference for forcing a bus stop database update. */
    public static final String PREF_DATABASE_FORCE_UPDATE =
            "pref_update_stop_db";
    /** The Preference for backing up favourites. */
    public static final String PREF_BACKUP_FAVOURITES =
            "pref_backup_favourites";
    /** The Preference for restoring favourites. */
    public static final String PREF_RESTORE_FAVOURITES =
            "pref_restore_favourites";
    /** The Preference for alert sounds. */
    public static final String PREF_ALERT_SOUND = "pref_alertsound_state";
    /** The Preference for alert vibration. */
    public static final String PREF_ALERT_VIBRATE = "pref_alertvibrate_state";
    /** The Preference for alert lights (LED flash). */
    public static final String PREF_ALERT_LED = "pref_alertled_state";
    /** The Preference for auto refresh. */
    public static final String PREF_AUTO_REFRESH = "pref_autorefresh_state";
    /** The Preference for showing night bus services. */
    public static final String PREF_SHOW_NIGHT_BUSES =
            "pref_nightservices_state";
    /** The Preference for service sorting. */
    public static final String PREF_SERVICE_SORTING =
            "pref_servicessorting_state";
    /** The Preference for number of shown departures per service. */
    public static final String PREF_NUMBER_OF_SHOWN_DEPARTURES_PER_SERVICE =
            "pref_numberOfShownDeparturesPerService";
    /** The Preference for automatically showing device location. */
    public static final String PREF_AUTO_LOCATION = "pref_autolocation_state";
    /** The Preference for showing zoom buttons on the map. */
    public static final String PREF_ZOOM_BUTTONS =
            "pref_map_zoom_buttons_state";
    /** The Preference for clearing the map search history. */
    public static final String PREF_CLEAR_MAP_SEARCH_HISTORY =
            "pref_clear_search_history";
    /** The Preference for disabling the GPS prompt. */
    public static final String PREF_DISABLE_GPS_PROMPT =
            "neareststops_gps_prompt_disable";
    /** The Preference for the last known map latitude (not shown). */
    public static final String PREF_MAP_LAST_LATITUDE =
            "pref_map_last_latitude";
    /** The Preference for the last known map longitude (not shown). */
    public static final String PREF_MAP_LAST_LONGITUDE =
            "pref_map_last_longitude";
    /** The Preference for the last known map zoom level (not shown). */
    public static final String PREF_MAP_LAST_ZOOM = "pref_map_last_zoom";
    /** The Preference for the last known map type (not shown). */
    public static final String PREF_MAP_LAST_MAP_TYPE =
            "pref_map_last_map_type";
    
    private SettingsDatabase sd;
    private ListPreference numberOfDeparturesPref;
    private String[] numberOfDeparturesStrings;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getPreferenceManager().setSharedPreferencesName(PREF_FILE);
        addPreferencesFromResource(R.xml.preferences);
        sd = SettingsDatabase.getInstance(getApplication());
        numberOfDeparturesStrings = getResources()
                .getStringArray(R.array.preferences_num_departures_entries);

        GenericDialogPreference backupDialog = (GenericDialogPreference)
                findPreference(PREF_BACKUP_FAVOURITES);
        GenericDialogPreference restoreDialog = (GenericDialogPreference)
                findPreference(PREF_RESTORE_FAVOURITES);
        GenericDialogPreference clearSearchHistoryDialog =
                (GenericDialogPreference)findPreference(
                PREF_CLEAR_MAP_SEARCH_HISTORY);
        GenericDialogPreference checkStopDBUpdates =
                (GenericDialogPreference)findPreference(
                PREF_DATABASE_FORCE_UPDATE);
        numberOfDeparturesPref = (ListPreference)findPreference(
                PREF_NUMBER_OF_SHOWN_DEPARTURES_PER_SERVICE);

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
                        MapSearchSuggestionsProvider.AUTHORITY,
                        MapSearchSuggestionsProvider.MODE);
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
        if(PREF_NUMBER_OF_SHOWN_DEPARTURES_PER_SERVICE.equals(key)) {
            setNumberOfDeparturesSummary(sp);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                NavigationUtils.navigateUpOnActivityWithSingleEntryPoint(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    /**
     * Set the summary text for number of departures.
     * 
     * @param sp An object which represents the SharedPreferences file in use.
     */
    private void setNumberOfDeparturesSummary(final SharedPreferences sp) {
        final String s =
                sp.getString(PREF_NUMBER_OF_SHOWN_DEPARTURES_PER_SERVICE, "4");
        int val;
        
        try {
            val = Integer.parseInt(s);
        } catch(NumberFormatException e) {
            val = 4;
        }
        
        numberOfDeparturesPref.setSummary(numberOfDeparturesStrings[val - 1]);
    }
}