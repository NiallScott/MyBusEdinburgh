/*
 * Copyright (C) 2015 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.settings;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.provider.SearchRecentSuggestions;
import android.widget.Toast;

import uk.org.rivernile.android.bustracker.BusApplication;
import uk.org.rivernile.android.bustracker.preferences.PreferenceConstants;
import uk.org.rivernile.android.utils.GenericDialogPreference;
import uk.org.rivernile.edinburghbustracker.android.MapSearchSuggestionsProvider;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.edinburghbustracker.android.SettingsDatabase;

/**
 * This {@link PreferenceFragment} allows the user to change app-wide preferences. As the minimum
 * target is at least API level 14, then it is safe to use the platform provided
 * {@link PreferenceFragment}. It should be noted that on earlier versions of Android the achieved
 * design is good enough, but isn't entirely compliant with Material Design.
 *
 * @author Niall Scott
 */
public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SettingsDatabase sd;
    private SharedPreferences sp;
    private ListPreference numberOfDeparturesPref;
    private String[] numberOfDeparturesStrings;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final BusApplication app = (BusApplication) getActivity().getApplication();
        sd = SettingsDatabase.getInstance(app);

        getPreferenceManager().setSharedPreferencesName(PreferenceConstants.PREF_FILE);
        addPreferencesFromResource(R.xml.preferences);
        sp = getPreferenceScreen().getSharedPreferences();
        numberOfDeparturesStrings = getResources()
                .getStringArray(R.array.preferences_num_departures_entries);

        final GenericDialogPreference backupDialog = (GenericDialogPreference)
                findPreference(PreferenceConstants.PREF_BACKUP_FAVOURITES);
        final GenericDialogPreference restoreDialog = (GenericDialogPreference)
                findPreference(PreferenceConstants.PREF_RESTORE_FAVOURITES);
        final GenericDialogPreference clearSearchHistoryDialog = (GenericDialogPreference)
                findPreference(PreferenceConstants.PREF_CLEAR_MAP_SEARCH_HISTORY);
        numberOfDeparturesPref = (ListPreference)
                findPreference(PreferenceConstants.PREF_NUMBER_OF_SHOWN_DEPARTURES_PER_SERVICE);

        backupDialog.setOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    final String message = sd.backupDatabase();

                    if (message.equals("success")) {
                        Toast.makeText(getActivity(), R.string.preference_backup_success,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        restoreDialog.setOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    final String message = sd.restoreDatabase();

                    if (message.equals("success")) {
                        Toast.makeText(app, R.string.preference_restore_success, Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        Toast.makeText(app, message, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        clearSearchHistoryDialog.setOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    final SearchRecentSuggestions suggestions =
                            new SearchRecentSuggestions(getActivity(),
                                    MapSearchSuggestionsProvider.AUTHORITY,
                                    MapSearchSuggestionsProvider.MODE);
                    suggestions.clearHistory();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        sp.registerOnSharedPreferenceChangeListener(this);
        populateNumberOfDeparturesSummary();
    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sp, final String key) {
        if (PreferenceConstants.PREF_NUMBER_OF_SHOWN_DEPARTURES_PER_SERVICE.equals(key)) {
            populateNumberOfDeparturesSummary();
        }
    }

    /**
     * Populate the summary text for number of departures.
     */
    private void populateNumberOfDeparturesSummary() {
        final String s = sp.getString(PreferenceConstants
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
