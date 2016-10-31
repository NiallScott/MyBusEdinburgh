/*
 * Copyright (C) 2009 - 2016 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.bustimes;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import uk.org.rivernile.android.bustracker.database.settings.loaders.FavouriteStopsLoader;
import uk.org.rivernile.android.bustracker.database.settings.loaders.HasProximityAlertLoader;
import uk.org.rivernile.android.bustracker.database.settings.loaders.HasTimeAlertLoader;
import uk.org.rivernile.android.bustracker.ui.bustimes.details.StopDetailsFragment;
import uk.org.rivernile.android.bustracker.ui.bustimes.times.BusTimesFragment;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * The purpose of this {@link android.app.Activity} it to display to the user live times and details
 * for the given bus stop code.
 *
 * @author Niall Scott
 */
public class DisplayStopDataActivity2 extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_STOP_CODE = "stopCode";

    private static final int LOADER_FAVOURITE_STOP = 1;
    private static final int LOADER_HAS_PROX_ALERT = 2;
    private static final int LOADER_HAS_TIME_ALERT = 3;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.displaystopdata2);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        viewPager.setAdapter(new StopDataPagerAdapter(this, getSupportFragmentManager(),
                getIntent().getStringExtra(EXTRA_STOP_CODE)));
        tabLayout.setupWithViewPager(viewPager);

        final LoaderManager loaderManager = getSupportLoaderManager();
        loaderManager.initLoader(LOADER_FAVOURITE_STOP, null, this);
        loaderManager.initLoader(LOADER_HAS_PROX_ALERT, null, this);
        loaderManager.initLoader(LOADER_HAS_TIME_ALERT, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        switch (id) {
            case LOADER_FAVOURITE_STOP:
                return new FavouriteStopsLoader(this, getIntent().getStringExtra(EXTRA_STOP_CODE));
            case LOADER_HAS_PROX_ALERT:
                return new HasProximityAlertLoader(this,
                        getIntent().getStringExtra(EXTRA_STOP_CODE));
            case LOADER_HAS_TIME_ALERT:
                return new HasTimeAlertLoader(this, getIntent().getStringExtra(EXTRA_STOP_CODE));
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
        switch (loader.getId()) {
            case LOADER_FAVOURITE_STOP:
                configureFavouriteMenuItem(data);
                break;
            case LOADER_HAS_PROX_ALERT:
                configureProximityAlertMenuItem(data);
                break;
            case LOADER_HAS_TIME_ALERT:
                configureTimeAlertMenuItem(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_FAVOURITE_STOP:
                configureFavouriteMenuItem(null);
                break;
            case LOADER_HAS_PROX_ALERT:
                configureProximityAlertMenuItem(null);
                break;
            case LOADER_HAS_TIME_ALERT:
                configureTimeAlertMenuItem(null);
                break;
        }
    }

    /**
     * Configure the favourite menu item to correctly show the favourite situation for this bus
     * stop.
     *
     * @param cursor The {@link Cursor} containing the favourite status of this bus stop.
     */
    private void configureFavouriteMenuItem(@Nullable final Cursor cursor) {

    }

    /**
     * Configure the proximity alert menu item to correctly show if a proximity alert has been set
     * for this bus stop.
     *
     * @param cursor The {@link Cursor} containing the proximity alert status of this bus stop.
     */
    private void configureProximityAlertMenuItem(@Nullable final Cursor cursor) {

    }

    /**
     * Configure the time alert menu item to correctly show if a time alert has been set for this
     * bus stop.
     *
     * @param cursor The {@link Cursor} containing the time alert status of this bus stop.
     */
    private void configureTimeAlertMenuItem(@Nullable final Cursor cursor) {

    }

    /**
     * This {@link FragmentPagerAdapter} provides the pages and tabs for this
     * {@link android.app.Activity}.
     */
    private static class StopDataPagerAdapter extends FragmentPagerAdapter {

        private final Context context;
        private final String stopCode;

        /**
         * Create a new {@code StopDataPagerAdapter}.
         *
         * @param context A {@link Context} instance.
         * @param fragmentManager The {@link FragmentManager}.
         * @param stopCode The stop code for this bus stop.
         */
        private StopDataPagerAdapter(@NonNull final Context context,
                @NonNull final FragmentManager fragmentManager, @NonNull final String stopCode) {
            super(fragmentManager);

            this.context = context;
            this.stopCode = stopCode;
        }

        @Override
        public Fragment getItem(final int position) {
            switch (position) {
                case 0:
                    return BusTimesFragment.newInstance(stopCode);
                case 1:
                    return StopDetailsFragment.newInstance(stopCode);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(final int position) {
            switch (position) {
                case 0:
                    return context.getString(R.string.displaystopdata_tab_times);
                case 1:
                    return context.getString(R.string.displaystopdata_tab_details);
                default:
                    return null;
            }
        }
    }
}
