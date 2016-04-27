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

package uk.org.rivernile.android.bustracker.ui.main;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import uk.org.rivernile.android.bustracker.ui.bustimes.DisplayStopDataActivity;
import uk.org.rivernile.android.bustracker.ui.main.sections.FavouritesSection;
import uk.org.rivernile.android.bustracker.ui.main.sections.Section;
import uk.org.rivernile.edinburghbustracker.android.AddEditFavouriteStopActivity;
import uk.org.rivernile.edinburghbustracker.android.AddProximityAlertActivity;
import uk.org.rivernile.edinburghbustracker.android.AddTimeAlertActivity;
import uk.org.rivernile.edinburghbustracker.android.BusStopMapActivity;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs.DeleteFavouriteDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .DeleteProximityAlertDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs.DeleteTimeAlertDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .InstallBarcodeScannerDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs.ServicesChooserDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs.TurnOnGpsDialogFragment;
import uk.org.rivernile.android.bustracker.ui.alerts.AlertManagerFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.general.EnterStopCodeFragment;
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
        EnterStopCodeFragment.Callbacks, FavouriteStopsFragment.Callbacks,
        NearestStopsFragment.Callbacks, ServicesChooserDialogFragment.Callbacks,
        InstallBarcodeScannerDialogFragment.Callbacks, TurnOnGpsDialogFragment.Callbacks {
    
    private static final String BARCODE_APP_PACKAGE =
            "market://details?id=com.google.zxing.client.android";

    private static final String DIALOG_DELETE_PROX_ALERT = "deleteProxAlertDialog";
    private static final String DIALOG_DELETE_TIME_ALERT = "deleteTimeAlertDialog";
    private static final String DIALOG_SERVICES_CHOOSER = "servicesChooserDialog";
    private static final String DIALOG_INSTALL_BARCODE_SCANNER = "installBarcodeScannerDialog";
    private static final String DIALOG_CONFIRM_DELETE_FAVOURITE = "deleteFavouriteDialog";
    private static final String DIALOG_TURN_ON_GPS = "turnOnGpsDialog";
    
    private ActionBar actionBar;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle drawerToggle;
    private CharSequence drawerOpenTitle, drawerClosedTitle;
    private int statusBarColour;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main_activity);
        actionBar = getSupportActionBar();
        drawerOpenTitle = getTitle();
        
        drawer = (DrawerLayout) findViewById(R.id.drawer);
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
            showSection(FavouritesSection.getInstance());
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
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
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
    public void onShowConfirmDeleteProximityAlert() {
        new DeleteProximityAlertDialogFragment()
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
    public void onAskInstallBarcodeScanner() {
        new InstallBarcodeScannerDialogFragment()
                .show(getSupportFragmentManager(), DIALOG_INSTALL_BARCODE_SCANNER);
    }

    @Override
    public void onShowBusTimes(final String stopCode) {
        final Intent intent = new Intent(this, DisplayStopDataActivity.class);
        intent.putExtra(DisplayStopDataActivity.ARG_STOPCODE, stopCode);
        startActivity(intent);
    }

    @Override
    public void onShowEditFavouriteStop(final String stopCode) {
        final Intent intent = new Intent(this, AddEditFavouriteStopActivity.class);
        intent.putExtra(AddEditFavouriteStopActivity.ARG_STOPCODE, stopCode);
        startActivity(intent);
    }

    @Override
    public void onShowConfirmFavouriteDeletion(final String stopCode) {
        DeleteFavouriteDialogFragment.newInstance(stopCode)
                .show(getSupportFragmentManager(), DIALOG_CONFIRM_DELETE_FAVOURITE);
    }

    @Override
    public void onShowAddProximityAlert(final String stopCode) {
        final Intent intent = new Intent(this, AddProximityAlertActivity.class);
        intent.putExtra(AddProximityAlertActivity.ARG_STOPCODE, stopCode);
        startActivity(intent);
    }

    @Override
    public void onShowAddTimeAlert(final String stopCode,
            final String[] defaultServices) {
        final Intent intent = new Intent(this, AddTimeAlertActivity.class);
        intent.putExtra(AddTimeAlertActivity.ARG_STOPCODE, stopCode);
        intent.putExtra(AddTimeAlertActivity.ARG_DEFAULT_SERVICES, defaultServices);
        startActivity(intent);
    }

    @Override
    public void onShowBusStopMapWithStopCode(final String stopCode) {
        final Intent intent = new Intent(this, BusStopMapActivity.class);
        intent.putExtra(BusStopMapActivity.ARG_STOPCODE, stopCode);
        startActivity(intent);
    }

    @Override
    public void onAskTurnOnGps() {
        new TurnOnGpsDialogFragment().show(getSupportFragmentManager(), DIALOG_TURN_ON_GPS);
    }

    @Override
    public void onShowAddFavouriteStop(final String stopCode,
            final String stopName) {
        final Intent intent = new Intent(this, AddEditFavouriteStopActivity.class);
        intent.putExtra(AddEditFavouriteStopActivity.ARG_STOPCODE, stopCode);
        intent.putExtra(AddEditFavouriteStopActivity.ARG_STOPNAME, stopName);
        startActivity(intent);
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
        final Fragment f = getSupportFragmentManager().findFragmentById(R.id.layoutContainer);

        if (f != null) {
            f.setMenuVisibility(!isDrawerOpen);
        }
    }
}
