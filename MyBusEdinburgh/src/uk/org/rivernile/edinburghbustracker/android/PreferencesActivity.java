/*
 * Copyright (C) 2009 - 2014 Niall 'Rivernile' Scott
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

import uk.org.rivernile.android.bustracker.BusApplication;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.provider.SearchRecentSuggestions;
import android.view.MenuItem;
import android.widget.Toast;
import uk.org.rivernile.android.bustracker.preferences.PreferenceConstants;
import uk.org.rivernile.android.utils.GenericDialogPreference;
import uk.org.rivernile.android.utils.NavigationUtils;

/**
 * The preferences {@link Activity} of the application. There is not much code
 * here, it is mostly defined in res/xml/preferences.xml and dealt with by the
 * platform.
 * 
 * TODO: convert this in to a {@link PreferenceFragment} when Google add it to
 * the compatibility package.
 *
 * @author Niall Scott
 */
public class PreferencesActivity extends PreferenceActivity
        implements OnSharedPreferenceChangeListener {
    
    private SettingsDatabase sd;
    private ListPreference numberOfDeparturesPref;
    private String[] numberOfDeparturesStrings;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getPreferenceManager().setSharedPreferencesName(
                PreferenceConstants.PREF_FILE);
        addPreferencesFromResource(R.xml.preferences);
        sd = SettingsDatabase.getInstance(getApplication());
        numberOfDeparturesStrings = getResources()
                .getStringArray(R.array.preferences_num_departures_entries);

        final GenericDialogPreference backupDialog = (GenericDialogPreference)
                findPreference(PreferenceConstants.PREF_BACKUP_FAVOURITES);
        final GenericDialogPreference restoreDialog = (GenericDialogPreference)
                findPreference(PreferenceConstants.PREF_RESTORE_FAVOURITES);
        final GenericDialogPreference clearSearchHistoryDialog =
                (GenericDialogPreference)findPreference(
                PreferenceConstants.PREF_CLEAR_MAP_SEARCH_HISTORY);
        final GenericDialogPreference checkStopDBUpdates =
                (GenericDialogPreference)findPreference(
                PreferenceConstants.PREF_DATABASE_FORCE_UPDATE);
        numberOfDeparturesPref = (ListPreference)findPreference(
                PreferenceConstants
                        .PREF_NUMBER_OF_SHOWN_DEPARTURES_PER_SERVICE);

        backupDialog.setOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                if (which != DialogInterface.BUTTON_POSITIVE) {
                    dialog.dismiss();
                    return;
                }
                
                final String message = sd.backupDatabase();
                if (message.equals("success")) {
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
                if (which != DialogInterface.BUTTON_POSITIVE) {
                    dialog.dismiss();
                    return;
                }
                
                String message = sd.restoreDatabase();
                if (message.equals("success")) {
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
                final SearchRecentSuggestions suggestions =
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
                if (which != DialogInterface.BUTTON_POSITIVE) {
                    dialog.dismiss();
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final BusApplication app = (BusApplication)
                                getApplicationContext();
                        app.checkForDBUpdates(true);
                    }
                }).start();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        
        final SharedPreferences sp = getPreferenceScreen()
                .getSharedPreferences();
        sp.registerOnSharedPreferenceChangeListener(this);
        setNumberOfDeparturesSummary(sp);
    }

    @Override
    public void onPause() {
        super.onPause();
        
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sp,
            final String key) {
        if (PreferenceConstants.PREF_NUMBER_OF_SHOWN_DEPARTURES_PER_SERVICE
                .equals(key)) {
            setNumberOfDeparturesSummary(sp);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
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
                sp.getString(PreferenceConstants
                        .PREF_NUMBER_OF_SHOWN_DEPARTURES_PER_SERVICE, "4");
        int val;
        
        try {
            val = Integer.parseInt(s);
        } catch(NumberFormatException e) {
            val = 4;
        }
        
        numberOfDeparturesPref.setSummary(numberOfDeparturesStrings[val - 1]);
    }
}