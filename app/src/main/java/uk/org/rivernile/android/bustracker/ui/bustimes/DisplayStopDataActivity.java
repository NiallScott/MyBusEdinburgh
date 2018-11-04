/*
 * Copyright (C) 2009 - 2018 Niall 'Rivernile' Scott
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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;

import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract;
import uk.org.rivernile.android.bustracker.database.busstop.loaders.BusStopLoader;
import uk.org.rivernile.android.bustracker.database.settings.loaders.FavouriteStopsLoader;
import uk.org.rivernile.android.bustracker.database.settings.loaders.HasProximityAlertLoader;
import uk.org.rivernile.android.bustracker.database.settings.loaders.HasTimeAlertLoader;
import uk.org.rivernile.android.bustracker.ui.alerts.proximity.AddProximityAlertDialogFragment;
import uk.org.rivernile.android.bustracker.ui.alerts.proximity.DeleteProximityAlertDialogFragment;
import uk.org.rivernile.android.bustracker.ui.alerts.time.AddTimeAlertDialogFragment;
import uk.org.rivernile.android.bustracker.ui.alerts.time.DeleteTimeAlertDialogFragment;
import uk.org.rivernile.android.bustracker.ui.busstopmap.BusStopMapActivity;
import uk.org.rivernile.android.bustracker.ui.bustimes.details.StopDetailsFragment;
import uk.org.rivernile.android.bustracker.ui.favourites.AddEditFavouriteStopDialogFragment;
import uk.org.rivernile.android.bustracker.ui.favourites.DeleteFavouriteDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.BuildConfig;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * The purpose of this {@link android.app.Activity} it to display to the user live times and details
 * for the given bus stop code.
 *
 * @author Niall Scott
 */
public class DisplayStopDataActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, AppBarLayout.OnOffsetChangedListener,
        StopDetailsFragment.Callbacks {

    public static final String ACTION_VIEW_STOP_DATA =
            BuildConfig.APPLICATION_ID + ".ACTION_VIEW_STOP_DATA";

    public static final String EXTRA_STOP_CODE = "stopCode";

    private static final int LOADER_BUS_STOP = 1;
    private static final int LOADER_FAVOURITE_STOP = 2;
    private static final int LOADER_HAS_PROX_ALERT = 3;
    private static final int LOADER_HAS_TIME_ALERT = 4;

    private static final String DIALOG_ADD_FAVOURITE = "addFavouriteDialog";
    private static final String DIALOG_ADD_PROX_ALERT = "addProxAlertDialog";
    private static final String DIALOG_ADD_TIME_ALERT = "addTimeAlertDialog";
    private static final String DIALOG_REMOVE_FAVOURITE = "removeFavourite";
    private static final String DIALOG_REMOVE_PROX_ALERT = "removeProxAlert";
    private static final String DIALOG_REMOVE_TIME_ALERT = "removeTimeAlert";

    private Cursor favouriteCursor;
    private Cursor proxCursor;
    private Cursor timeCursor;
    private Intent streetViewIntent;

    private CollapsingToolbarLayout collapsingLayout;
    private TextView txtStopName;
    private TextView txtStopCode;

    private MenuItem favouriteMenuItem;
    private MenuItem proxMenuItem;
    private MenuItem timeMenuItem;
    private MenuItem streetViewMenuItem;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.displaystopdata);
        setSupportActionBar(findViewById(R.id.toolbar));
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        final AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        collapsingLayout = findViewById(R.id.collapsingLayout);
        txtStopName = findViewById(R.id.txtStopName);
        txtStopCode = findViewById(R.id.txtStopCode);
        final ViewPager viewPager = findViewById(R.id.viewPager);
        final TabLayout tabLayout = findViewById(R.id.tabLayout);

        appBarLayout.addOnOffsetChangedListener(this);

        final Intent intent = getIntent();
        final String stopCode;

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            stopCode = intent.getData().getQueryParameter("busStopCode");
        } else {
            stopCode = intent.getStringExtra(EXTRA_STOP_CODE);
        }

        viewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.padding_default));
        viewPager.setAdapter(new StopDataPagerAdapter(this, getSupportFragmentManager(), stopCode));
        tabLayout.setupWithViewPager(viewPager);

        final LoaderManager loaderManager = getSupportLoaderManager();
        loaderManager.initLoader(LOADER_BUS_STOP, null, this);
        loaderManager.initLoader(LOADER_FAVOURITE_STOP, null, this);
        loaderManager.initLoader(LOADER_HAS_PROX_ALERT, null, this);
        loaderManager.initLoader(LOADER_HAS_TIME_ALERT, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.displaystopdata_option_menu, menu);
        favouriteMenuItem = menu.findItem(R.id.displaystopdata_option_menu_favourite);
        proxMenuItem = menu.findItem(R.id.displaystopdata_option_menu_prox);
        timeMenuItem = menu.findItem(R.id.displaystopdata_option_menu_time);
        streetViewMenuItem = menu.findItem(R.id.displaystopdata_option_menu_street_view);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);

        configureFavouriteMenuItem(favouriteCursor);
        configureProximityAlertMenuItem(proxCursor);
        configureTimeAlertMenuItem(timeCursor);
        configureStreetViewMenuItem();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.displaystopdata_option_menu_favourite:
                doFavouriteSelected();
                return true;
            case R.id.displaystopdata_option_menu_prox:
                doProximityAlertSelected();
                return true;
            case R.id.displaystopdata_option_menu_time:
                doTimeAlertSelected();
                return true;
            case R.id.displaystopdata_option_menu_street_view:
                doStreetViewSelected();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        switch (id) {
            case LOADER_BUS_STOP:
                return new BusStopLoader(this, getIntent().getStringExtra(EXTRA_STOP_CODE),
                        new String[] {
                                BusStopContract.BusStops.STOP_NAME,
                                BusStopContract.BusStops.LOCALITY,
                                BusStopContract.BusStops.LATITUDE,
                                BusStopContract.BusStops.LONGITUDE
                        });
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
            case LOADER_BUS_STOP:
                handleBusStopLoad(data);
                break;
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
            case LOADER_BUS_STOP:
                handleBusStopLoad(null);
                break;
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

    @Override
    public void onOffsetChanged(final AppBarLayout appBarLayout, final int verticalOffset) {
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(
                    Math.abs(verticalOffset) >= collapsingLayout.getScrimVisibleHeightTrigger());
        }
    }

    @Override
    public void showMapForStop(@NonNull final String stopCode) {
        final Intent intent = new Intent(this, BusStopMapActivity.class);
        intent.putExtra(BusStopMapActivity.EXTRA_STOP_CODE, stopCode);
        startActivity(intent);
    }

    /**
     * Handle the load of the bus stop details {@link Cursor}.
     *
     * @param cursor The {@link Cursor} containing bus stop details, or {@code null}.
     */
    private void handleBusStopLoad(@Nullable final Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            final String stopCode = getIntent().getStringExtra(EXTRA_STOP_CODE);
            final String stopName = cursor.getString(
                    cursor.getColumnIndex(BusStopContract.BusStops.STOP_NAME));
            final String locality = cursor.getString(
                    cursor.getColumnIndex(BusStopContract.BusStops.LOCALITY));
            final String nameToDisplay = !TextUtils.isEmpty(locality)
                    ? getString(R.string.bustimes_title_locality_format, stopName, locality)
                    : stopName;

            txtStopName.setText(nameToDisplay);
            txtStopCode.setText(stopCode);
            final ActionBar actionBar = getSupportActionBar();

            if (actionBar != null) {
                actionBar.setTitle(nameToDisplay);
                actionBar.setSubtitle(stopCode);
            }

            final double latitude = cursor.getDouble(
                    cursor.getColumnIndex(BusStopContract.BusStops.LATITUDE));
            final double longitude = cursor.getDouble(
                    cursor.getColumnIndex(BusStopContract.BusStops.LONGITUDE));
            final String uri = "google.streetview:cbll=" + latitude + ',' + longitude;
            streetViewIntent = new Intent(Intent.ACTION_VIEW);
            streetViewIntent.setData(Uri.parse(uri));
            streetViewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            streetViewIntent = null;
        }

        configureStreetViewMenuItem();
    }

    /**
     * Configure the favourite menu item to correctly show the favourite situation for this bus
     * stop.
     *
     * @param cursor The {@link Cursor} containing the favourite status of this bus stop.
     */
    private void configureFavouriteMenuItem(@Nullable final Cursor cursor) {
        favouriteCursor = cursor;

        if (cursor != null) {
            if (favouriteMenuItem != null) {
                favouriteMenuItem.setEnabled(true);

                if (cursor.getCount() > 0) {
                    favouriteMenuItem.setTitle(R.string.displaystopdata_menu_favourite_rem)
                            .setIcon(R.drawable.ic_action_star);
                } else {
                    favouriteMenuItem.setTitle(R.string.displaystopdata_menu_favourite_add)
                            .setIcon(R.drawable.ic_action_star_border);
                }
            }
        } else {
            if (favouriteMenuItem != null) {
                favouriteMenuItem.setEnabled(false);
            }
        }
    }

    /**
     * Configure the proximity alert menu item to correctly show if a proximity alert has been set
     * for this bus stop.
     *
     * @param cursor The {@link Cursor} containing the proximity alert status of this bus stop.
     */
    private void configureProximityAlertMenuItem(@Nullable final Cursor cursor) {
        proxCursor = cursor;

        if (cursor != null) {
            if (proxMenuItem != null) {
                proxMenuItem.setEnabled(true);

                if (cursor.getCount() > 0) {
                    proxMenuItem.setTitle(R.string.displaystopdata_menu_prox_rem)
                            .setIcon(R.drawable.ic_action_location_off);
                } else {
                    proxMenuItem.setTitle(R.string.displaystopdata_menu_prox_add)
                            .setIcon(R.drawable.ic_action_location_on);
                }
            }
        } else {
            if (proxMenuItem != null) {
                proxMenuItem.setEnabled(false);
            }
        }
    }

    /**
     * Configure the time alert menu item to correctly show if a time alert has been set for this
     * bus stop.
     *
     * @param cursor The {@link Cursor} containing the time alert status of this bus stop.
     */
    private void configureTimeAlertMenuItem(@Nullable final Cursor cursor) {
        timeCursor = cursor;

        if (cursor != null) {
            if (timeMenuItem != null) {
                timeMenuItem.setEnabled(true);

                if (cursor.getCount() > 0) {
                    timeMenuItem.setTitle(R.string.displaystopdata_menu_time_rem)
                            .setIcon(R.drawable.ic_action_alarm_off);
                } else {
                    timeMenuItem.setTitle(R.string.displaystopdata_menu_time_add)
                            .setIcon(R.drawable.ic_action_alarm_add);
                }
            }
        } else {
            if (timeMenuItem != null) {
                timeMenuItem.setEnabled(false);
            }
        }
    }

    /**
     * Configure the Street View menu item visibility depending on if there's an {@link Intent}
     * available, and if another {@link android.app.Activity} on the system responds to that
     * {@link Intent}.
     */
    private void configureStreetViewMenuItem() {
        if (streetViewMenuItem != null) {
            streetViewMenuItem.setVisible(streetViewIntent != null &&
                    streetViewIntent.resolveActivity(getPackageManager()) != null);
        }
    }

    /**
     * This is called when the favourite alert ActionItem is selected.
     */
    private void doFavouriteSelected() {
        if (favouriteCursor != null) {
            if (favouriteCursor.getCount() > 0) {
                showRemoveFavourite();
            } else {
                showAddFavourite();
            }
        }
    }

    /**
     * This is called when the proximity alert ActionItem is selected.
     */
    private void doProximityAlertSelected() {
        if (proxCursor != null) {
            if (proxCursor.getCount() > 0) {
                showRemoveProximityAlert();
            } else {
                // Show the Activity for adding a new proximity alert.
                showAddProximityAlert();
            }
        }
    }

    /**
     * This is called when the time alert ActionItem is selected.
     */
    private void doTimeAlertSelected() {
        if (timeCursor != null) {
            if (timeCursor.getCount() > 0) {
                showRemoveTimeAlert();
            } else {
                // Show the Activity for adding a new time alert.
                showAddTimeAlert();
            }
        }
    }

    /**
     * This is called when the Street View ActionItem is selected.
     */
    private void doStreetViewSelected() {
        if (streetViewIntent != null) {
            try {
                startActivity(streetViewIntent);
            } catch (ActivityNotFoundException ignored) {
                // This should never happen as the Intent has been checked before for a respondent
                // Activity.
            }
        }
    }

    /**
     * Show the UI for adding a new favourite bus stop.
     */
    private void showAddFavourite() {
        AddEditFavouriteStopDialogFragment.newInstance(getIntent().getStringExtra(EXTRA_STOP_CODE))
                .show(getSupportFragmentManager(), DIALOG_ADD_FAVOURITE);
    }

    /**
     * Show the UI for removing a favourite bus stop.
     */
    private void showRemoveFavourite() {
        DeleteFavouriteDialogFragment.newInstance(getIntent().getStringExtra(EXTRA_STOP_CODE))
                .show(getSupportFragmentManager(), DIALOG_REMOVE_FAVOURITE);
    }

    /**
     * Show the UI for adding a new proximity alert.
     */
    private void showAddProximityAlert() {
        AddProximityAlertDialogFragment.newInstance(getIntent().getStringExtra(EXTRA_STOP_CODE))
                .show(getSupportFragmentManager(), DIALOG_ADD_PROX_ALERT);
    }

    /**
     * Show the UI for removing a proximity alert.
     */
    private void showRemoveProximityAlert() {
        new DeleteProximityAlertDialogFragment()
                .show(getSupportFragmentManager(), DIALOG_REMOVE_PROX_ALERT);
    }

    /**
     * Show the UI for adding a new time alert.
     */
    private void showAddTimeAlert() {
        AddTimeAlertDialogFragment.newInstance(getIntent().getStringExtra(EXTRA_STOP_CODE))
                .show(getSupportFragmentManager(), DIALOG_ADD_TIME_ALERT);
    }

    /**
     * Show the UI for removing a time alert.
     */
    private void showRemoveTimeAlert() {
        new DeleteTimeAlertDialogFragment()
                .show(getSupportFragmentManager(), DIALOG_REMOVE_TIME_ALERT);
    }
}
