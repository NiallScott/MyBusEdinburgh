/*
 * Copyright (C) 2009 - 2020 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.main;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import uk.org.rivernile.android.bustracker.ui.alerts.proximity.AddProximityAlertDialogFragment;
import uk.org.rivernile.android.bustracker.ui.alerts.time.AddTimeAlertDialogFragment;
import uk.org.rivernile.android.bustracker.ui.busstopmap.BusStopMapActivity;
import uk.org.rivernile.android.bustracker.ui.busstopmap.BusStopMapFragment;
import uk.org.rivernile.android.bustracker.ui.bustimes.DisplayStopDataActivity;
import uk.org.rivernile.android.bustracker.ui.favourites.AddEditFavouriteStopDialogFragment;
import uk.org.rivernile.android.bustracker.ui.main.sections.AlertManagerSection;
import uk.org.rivernile.android.bustracker.ui.main.sections.FavouritesSection;
import uk.org.rivernile.android.bustracker.ui.main.sections.Section;
import uk.org.rivernile.android.bustracker.ui.search.SearchActivity;
import uk.org.rivernile.edinburghbustracker.android.BuildConfig;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.android.bustracker.ui.favourites.DeleteFavouriteDialogFragment;
import uk.org.rivernile.android.bustracker.ui.alerts.proximity.DeleteProximityAlertDialogFragment;
import uk.org.rivernile.android.bustracker.ui.alerts.time.DeleteTimeAlertDialogFragment;
import uk.org.rivernile.android.bustracker.ui.search.InstallBarcodeScannerDialogFragment;
import uk.org.rivernile.android.bustracker.ui.serviceschooser.ServicesChooserDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs.TurnOnGpsDialogFragment;
import uk.org.rivernile.android.bustracker.ui.alerts.AlertManagerFragment;
import uk.org.rivernile.android.bustracker.ui.favourites.FavouriteStopsFragment;
import uk.org.rivernile.android.bustracker.ui.neareststops.NearestStopsFragment;

/**
 * This {@link android.app.Activity} hosts the root UI elements.
 *
 * @author Niall Scott
 * @see SectionListFragment
 */
public class MainActivity extends AppCompatActivity
        implements SectionListFragment.Callbacks, AlertManagerFragment.Callbacks,
        FavouriteStopsFragment.Callbacks, NearestStopsFragment.Callbacks,
        ServicesChooserDialogFragment.Callbacks, InstallBarcodeScannerDialogFragment.Callbacks,
        TurnOnGpsDialogFragment.Callbacks, BusStopMapFragment.Callbacks,
        HasAndroidInjector {

    public static final String ACTION_MANAGE_ALERTS = BuildConfig.APPLICATION_ID +
            ".ACTION_MANAGE_ALERTS";
    
    private static final String BARCODE_APP_PACKAGE =
            "market://details?id=com.google.zxing.client.android";

    private static final String DIALOG_ADD_FAVOURITE = "addFavouriteDialog";
    private static final String DIALOG_ADD_PROX_ALERT = "addProxAlertDialog";
    private static final String DIALOG_ADD_TIME_ALERT = "addTimeAlertDialog";
    private static final String DIALOG_DELETE_PROX_ALERT = "deleteProxAlertDialog";
    private static final String DIALOG_DELETE_TIME_ALERT = "deleteTimeAlertDialog";
    private static final String DIALOG_SERVICES_CHOOSER = "servicesChooserDialog";
    private static final String DIALOG_CONFIRM_DELETE_FAVOURITE = "deleteFavouriteDialog";
    private static final String DIALOG_TURN_ON_GPS = "turnOnGpsDialog";

    @Inject
    DispatchingAndroidInjector<Object> dispatchingAndroidInjector;
    
    private ActionBar actionBar;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle drawerToggle;
    private CharSequence drawerOpenTitle, drawerClosedTitle;
    private int statusBarColour;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        AndroidInjection.inject(this);

        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main_activity);
        actionBar = getSupportActionBar();
        drawerOpenTitle = getTitle();
        
        drawer = findViewById(R.id.drawer);
        drawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerToggle = new ActionBarDrawerToggle(this, drawer, R.string.main_drawer_open,
                R.string.main_drawer_close) {
            @Override
            public void onDrawerClosed(final View drawerView) {
                super.onDrawerClosed(drawerView);
                
                actionBar.setTitle(drawerClosedTitle);
                setFragmentOptionsMenuVisibility(false);
            }

            @Override
            public void onDrawerOpened(final View drawerView) {
                super.onDrawerOpened(drawerView);
                
                actionBar.setTitle(drawerOpenTitle);
                setFragmentOptionsMenuVisibility(true);
            }
        };
        
        drawer.addDrawerListener(drawerToggle);
        
        if (savedInstanceState == null) {
            if (!handleIntent(getIntent())) {
                showSection(FavouritesSection.getInstance());
            }
        }
    }

    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        
        drawerToggle.syncState();
        
        final boolean isDrawerOpen = drawer.isDrawerOpen(GravityCompat.START);
        
        if (!isDrawerOpen) {
            actionBar.setTitle(drawerClosedTitle);
        }
        
        setFragmentOptionsMenuVisibility(isDrawerOpen);
    }

    @Override
    public void onConfigurationChanged(@NonNull final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // FIXME: this code exists because of a bug in the compatibility library. Remove it when the
        // bug has been fixed in the library.
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void setTitle(final CharSequence title) {
        drawerClosedTitle = title;
    }

    @Override
    public void setTitle(final int titleId) {
        setTitle(getString(titleId));
    }

    @Override
    public void onSupportActionModeStarted(@NonNull final ActionMode mode) {
        super.onSupportActionModeStarted(mode);

        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Window window = getWindow();
            statusBarColour = window.getStatusBarColor();
            window.setStatusBarColor(ContextCompat.getColor(this,
                    R.color.colorContextualStatusBarBackground));
        }
    }

    @Override
    public void onSupportActionModeFinished(@NonNull final ActionMode mode) {
        super.onSupportActionModeFinished(mode);

        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(statusBarColour);
        }
    }

    @Override
    public void onSectionChosen(@NonNull final Section section) {
        showSection(section);
    }

    @Override
    public void onShowSearch() {
        startActivity(new Intent(this, SearchActivity.class));
    }

    @Override
    public void onShowConfirmDeleteProximityAlert(@NonNull final String stopCode) {
        DeleteProximityAlertDialogFragment.newInstance(stopCode)
                .show(getSupportFragmentManager(), DIALOG_DELETE_PROX_ALERT);
    }

    @Override
    public void onShowConfirmDeleteTimeAlert() {
        new DeleteTimeAlertDialogFragment()
                .show(getSupportFragmentManager(), DIALOG_DELETE_TIME_ALERT);
    }

    @Override
    public void onShowServicesChooser(final String[] services,
            final String[] selectedServices, final String title) {
        ServicesChooserDialogFragment
                .newInstance(services, selectedServices, title)
                .show(getSupportFragmentManager(), DIALOG_SERVICES_CHOOSER);
    }

    @Override
    public void onShowBusTimes(final String stopCode) {
        final Intent intent = new Intent(this, DisplayStopDataActivity.class);
        intent.putExtra(DisplayStopDataActivity.EXTRA_STOP_CODE, stopCode);
        startActivity(intent);
    }

    @Override
    public void onShowConfirmFavouriteDeletion(final String stopCode) {
        DeleteFavouriteDialogFragment.newInstance(stopCode)
                .show(getSupportFragmentManager(), DIALOG_CONFIRM_DELETE_FAVOURITE);
    }

    @Override
    public void onShowAddProximityAlert(final String stopCode) {
        AddProximityAlertDialogFragment.newInstance(stopCode)
                .show(getSupportFragmentManager(), DIALOG_ADD_PROX_ALERT);
    }

    @Override
    public void onShowAddTimeAlert(final String stopCode, final String[] defaultServices) {
        AddTimeAlertDialogFragment.newInstance(stopCode, defaultServices)
                .show(getSupportFragmentManager(), DIALOG_ADD_TIME_ALERT);
    }

    @Override
    public void onShowBusStopMapWithStopCode(final String stopCode) {
        final Intent intent = new Intent(this, BusStopMapActivity.class);
        intent.putExtra(BusStopMapActivity.EXTRA_STOP_CODE, stopCode);
        startActivity(intent);
    }

    @Override
    public void onAskTurnOnGps() {
        new TurnOnGpsDialogFragment().show(getSupportFragmentManager(), DIALOG_TURN_ON_GPS);
    }

    @Override
    public void onShowAddEditFavouriteStop(@NonNull final String stopCode) {
        AddEditFavouriteStopDialogFragment.newInstance(stopCode)
                .show(getSupportFragmentManager(), DIALOG_ADD_FAVOURITE);
    }

    @Override
    public void onServicesChosen(final String[] chosenServices) {
        try {
            final ServicesChooserDialogFragment.Callbacks child =
                    (ServicesChooserDialogFragment.Callbacks) getSupportFragmentManager()
                            .findFragmentById(R.id.layoutContainer);
            if (child != null) {
                child.onServicesChosen(chosenServices);
            }
        } catch (ClassCastException e) {
            // Unable to pass the callback on. Silently fail.
        }
    }

    @Override
    public void onShowInstallBarcodeScanner() {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(BARCODE_APP_PACKAGE));

        try {
            startActivity(intent);
        } catch(ActivityNotFoundException e) {
            Toast.makeText(this, R.string.barcodescannerdialog_noplaystore, Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onShowSystemLocationPreferences() {
        try {
            startActivity(TurnOnGpsDialogFragment.TURN_ON_GPS_INTENT);
        } catch (ActivityNotFoundException e) {
            // Do nothing.
        }
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return dispatchingAndroidInjector;
    }

    /**
     * Show a new {@link Section} to the user.
     * 
     * @param section The {@link Section} to show. Must not be {@code null}.
     */
    private void showSection(final Section section) {
        if (section == null) {
            return;
        }
        
        if (TextUtils.isEmpty(section.getFragmentTag())) {
            drawer.closeDrawer(GravityCompat.START);
            section.doAlternativeAction(this);
            return;
        }
        
        final FragmentManager fragMan = getSupportFragmentManager();
        final Fragment currFragment = fragMan.findFragmentById(R.id.layoutContainer);
        final Fragment newFragment = fragMan.findFragmentByTag(section.getFragmentTag());
        
        if (currFragment != null && currFragment == newFragment) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        
        final FragmentTransaction fragTrans = fragMan.beginTransaction();
        
        if (currFragment != null) {
            fragTrans.detach(currFragment);
        }
        
        if (newFragment == null) {
            fragTrans.add(R.id.layoutContainer, section.getFragment(), section.getFragmentTag());
        } else {
            // Fragments are attached/detached so that their state can be persisted in memory for
            // the lifetime of MainActivity.
            fragTrans.attach(newFragment);
        }
        
        fragTrans.commit();
        drawer.closeDrawer(GravityCompat.START);
    }
    
    /**
     * Set the correct visibility on the options menu of the current {@link Fragment} based on
     * whether the {@link DrawerLayout} is open or closed.
     * 
     * @param isDrawerOpen {@code true} if the drawer is open, {@code false} if it is closed.
     */
    private void setFragmentOptionsMenuVisibility(final boolean isDrawerOpen) {
        final Fragment container = getSupportFragmentManager()
                .findFragmentById(R.id.layoutContainer);

        if (container != null) {
            container.setMenuVisibility(!isDrawerOpen);
        }

        final Fragment drawer = getSupportFragmentManager()
                .findFragmentById(R.id.fragmentSectionList);

        if (drawer != null) {
            drawer.setMenuVisibility(isDrawerOpen);
        }
    }

    /**
     * Handle an {@link Intent} sent in to this {@link android.app.Activity}.
     *
     * @param intent The {@link Intent} to handle.
     * @return {@code true} if the {@link Intent} was handled here, otherwise {@code false}.
     */
    private boolean handleIntent(@NonNull final Intent intent) {
        final String action = intent.getAction();

        if (ACTION_MANAGE_ALERTS.equals(action)) {
            showSection(AlertManagerSection.getInstance());

            return true;
        }

        return false;
    }
}
