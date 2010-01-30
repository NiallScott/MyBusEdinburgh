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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

/**
 * The preferences dialog of the application. There is not much code here, it is
 * mostly defined in res/xml/preferences.xml.
 *
 * @author Niall Scott
 */
public class PreferencesActivity extends PreferenceActivity
        implements OnSharedPreferenceChangeListener {

    /** The name of the preferences file. */
    public final static String PREF_FILE = "preferences";
    /** The AUTOREFRESH_STATE key in the preferences. */
    public final static String KEY_AUTOREFRESH_STATE = "pref_autorefresh_state";
    /** The HOSTNAME key in the preferences */
    public final static String KEY_HOSTNAME = "pref_server_hostname";
    /** The PORT key in the preferences */
    public final static String KEY_PORT = "pref_server_port";

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(PREF_FILE);
        addPreferencesFromResource(R.xml.preferences);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * {@inheritDoc}
     */
    public void onSharedPreferenceChanged(final SharedPreferences sp,
            final String key)
    {
        String value;
        if(key.equals("pref_server_hostname")) {
            value = sp.getString(key, "");
            if(value.length() == 0) {
                EditTextPreference t = (EditTextPreference)getPreferenceScreen()
                        .findPreference(key);
                t.setText("bustracker.selfip.org");
                Toast.makeText(this, R.string.preferences_invalidhostname,
                        Toast.LENGTH_LONG).show();
            }
        } else if(key.equals("pref_server_port")) {
            value = sp.getString(key, "4876");
            try {
                int i = Integer.parseInt(value);
                if(i < 1 || i > 65535) {
                    EditTextPreference t =
                            (EditTextPreference)getPreferenceScreen()
                            .findPreference(key);
                    t.setText("4876");
                    Toast.makeText(this, R.string.preferences_invalidport,
                            Toast.LENGTH_LONG).show();
                }
            } catch(NumberFormatException e) {
                EditTextPreference t = (EditTextPreference)getPreferenceScreen()
                        .findPreference(key);
                t.setText("4876");
                Toast.makeText(this, R.string.preferences_invalidport,
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}