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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import uk.org.rivernile.edinburghbustracker.android.fragments.general
        .MainDashboardFragment;

/**
 * This Activity loads the MainDashboardFragment.
 *
 * @author Niall Scott
 * @see MainDashboardFragment
 */
public class MainActivity extends FragmentActivity {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_fragment_container);
        setTitle(R.string.app_name);
        
        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, new MainDashboardFragment())
                    .commit();
        }
        
        if(getSharedPreferences(PreferencesActivity.PREF_FILE, 0)
                .getBoolean("pref_startupshowfavs_state", false)) {
            startActivity(new Intent(this, FavouriteStopsActivity.class));
        }
    }
}
