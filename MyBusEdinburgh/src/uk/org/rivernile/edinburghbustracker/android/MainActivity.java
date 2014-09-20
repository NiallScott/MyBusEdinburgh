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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import uk.org.rivernile.android.bustracker.preferences.PreferenceConstants;
import uk.org.rivernile.android.bustracker.ui.news.NewsUpdatesActivity;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .AboutDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .OpenSourceLicenseDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.general
        .MainDashboardFragment;

/**
 * This Activity loads the MainDashboardFragment.
 *
 * @author Niall Scott
 * @see MainDashboardFragment
 */
public class MainActivity extends ActionBarActivity
        implements MainDashboardFragment.Callbacks,
        AboutDialogFragment.Callbacks {
    
    private static final String DIALOG_ABOUT = "aboutDialog";
    private static final String DIALOG_LICENCE = "licenseDialog";

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.home_activity);
        
        if(getSharedPreferences(PreferenceConstants.PREF_FILE, 0)
                .getBoolean(PreferenceConstants.PREF_STARTUP_SHOW_FAVS,
                        false)) {
            onShowFavourites();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowFavourites() {
        startActivity(new Intent(this, FavouriteStopsActivity.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowStopCodeEntry() {
        startActivity(new Intent(this, EnterStopCodeActivity.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowBusStopMap() {
        startActivity(new Intent(this, BusStopMapActivity.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowNearestStops() {
        startActivity(new Intent(this, NearestStopsActivity.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowNewsUpdates() {
        startActivity(new Intent(this, NewsUpdatesActivity.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowAlertManager() {
        startActivity(new Intent(this, AlertManagerActivity.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowAppPreferences() {
        startActivity(new Intent(this, PreferencesActivity.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowAboutInformation() {
        new AboutDialogFragment()
                .show(getSupportFragmentManager(),DIALOG_ABOUT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowLicences() {
        new OpenSourceLicenseDialogFragment()
                .show(getSupportFragmentManager(), DIALOG_LICENCE);
    }
}
