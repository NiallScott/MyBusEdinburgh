/*
 * Copyright (C) 2015 - 2020 Niall 'Rivernile' Scott
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

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import uk.org.rivernile.android.bustracker.database.search.SearchSuggestionsProvider;
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceManager;
import uk.org.rivernile.android.utils.GenericDialogPreference;
import uk.org.rivernile.edinburghbustracker.android.R;

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

    @Inject
    PreferenceManager preferenceManager;

    private SharedPreferences sp;
    private ListPreference numberOfDeparturesPref;
    private String[] numberOfDeparturesStrings;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        AndroidInjection.inject(this);

        super.onCreate(savedInstanceState);

        getPreferenceManager().setSharedPreferencesName(PreferenceManager.PREF_FILE);
        addPreferencesFromResource(R.xml.preferences);
        sp = getPreferenceScreen().getSharedPreferences();
        numberOfDeparturesStrings = getResources()
                .getStringArray(R.array.preferences_num_departures_entries);

        final GenericDialogPreference clearSearchHistoryDialog = (GenericDialogPreference)
                findPreference(PreferenceManager.PREF_CLEAR_MAP_SEARCH_HISTORY);
        numberOfDeparturesPref = (ListPreference)
                findPreference(PreferenceManager.PREF_NUMBER_OF_SHOWN_DEPARTURES_PER_SERVICE);

        clearSearchHistoryDialog.setOnClickListener((dialog, which) -> {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                final SearchRecentSuggestions suggestions =
                        new SearchRecentSuggestions(getActivity(),
                                SearchSuggestionsProvider.AUTHORITY,
                                SearchSuggestionsProvider.MODE);
                suggestions.clearHistory();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        sp.registerOnSharedPreferenceChangeListener(this);
        populateNumberOfDeparturesSummary();
    }

    @Override
    public void onStop() {
        super.onStop();

        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sp, final String key) {
        if (PreferenceManager.PREF_NUMBER_OF_SHOWN_DEPARTURES_PER_SERVICE.equals(key)) {
            populateNumberOfDeparturesSummary();
        }
    }

    /**
     * Populate the summary text for number of departures.
     */
    private void populateNumberOfDeparturesSummary() {
        final int val = preferenceManager.getBusTimesNumberOfDeparturesToShowPerService();
        numberOfDeparturesPref.setSummary(numberOfDeparturesStrings[val - 1]);
    }
}
