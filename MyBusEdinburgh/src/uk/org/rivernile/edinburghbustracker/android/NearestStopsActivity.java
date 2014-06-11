/*
 * Copyright (C) 2011 - 2014 Niall 'Rivernile' Scott
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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import uk.org.rivernile.android.bustracker.ui.bustimes.DisplayStopDataActivity;
import uk.org.rivernile.android.utils.NavigationUtils;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .DeleteFavouriteDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .DeleteProximityAlertDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .DeleteTimeAlertDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .ServicesChooserDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .TurnOnGpsDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.general
        .NearestStopsFragment;

/**
 * This Activity hosts the NearestStopsFragment which shows a list of the
 * nearest bus stops to the device.
 * 
 * @author Niall Scott
 * @see NearestStopsFragment
 */
public class NearestStopsActivity extends ActionBarActivity
        implements NearestStopsFragment.Callbacks,
        TurnOnGpsDialogFragment.Callbacks,
        DeleteFavouriteDialogFragment.Callbacks,
        DeleteProximityAlertDialogFragment.Callbacks,
        DeleteTimeAlertDialogFragment.Callbacks,
        ServicesChooserDialogFragment.Callbacks {
    
    private static final String DIALOG_TURN_ON_GPS = "turnOnGpsDialog";
    private static final String DIALOG_CONFIRM_DELETE_FAVOURITE =
            "deleteFavDialog";
    private static final String DIALOG_DELETE_PROX_ALERT = "delProxAlertDialog";
    private static final String DIALOG_DELETE_TIME_ALERT = "delTimeAlertDialog";
    private static final String DIALOG_SERVICES_CHOOSER =
            "servicesChooserDialog";

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.neareststops_activity);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                NavigationUtils.navigateUpOnActivityWithSingleEntryPoint(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAskTurnOnGps() {
        new TurnOnGpsDialogFragment().show(getSupportFragmentManager(),
                        DIALOG_TURN_ON_GPS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowGpsPreferences() {
        try {
            startActivity(TurnOnGpsDialogFragment.TURN_ON_GPS_INTENT);
        } catch (ActivityNotFoundException e) {
            // Do nothing.
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowConfirmFavouriteDeletion(final String stopCode) {
        DeleteFavouriteDialogFragment.newInstance(stopCode)
                .show(getSupportFragmentManager(),
                        DIALOG_CONFIRM_DELETE_FAVOURITE);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowConfirmDeleteProximityAlert() {
        new DeleteProximityAlertDialogFragment()
                .show(getSupportFragmentManager(), DIALOG_DELETE_PROX_ALERT);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowConfirmDeleteTimeAlert() {
        new DeleteTimeAlertDialogFragment()
                .show(getSupportFragmentManager(), DIALOG_DELETE_TIME_ALERT);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowServicesChooser(final String[] services,
                final String[] selectedServices, final String title) {
        ServicesChooserDialogFragment
                .newInstance(services, selectedServices, title)
                .show(getSupportFragmentManager(), DIALOG_SERVICES_CHOOSER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowAddFavouriteStop(final String stopCode,
            final String stopName) {
        final Intent intent = new Intent(this,
                AddEditFavouriteStopActivity.class);
        intent.putExtra(AddEditFavouriteStopActivity.ARG_STOPCODE, stopCode);
        intent.putExtra(AddEditFavouriteStopActivity.ARG_STOPNAME, stopName);
        
        startActivity(intent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowAddProximityAlert(final String stopCode) {
        final Intent intent = new Intent(this, AddProximityAlertActivity.class);
        intent.putExtra(AddProximityAlertActivity.ARG_STOPCODE, stopCode);
        startActivity(intent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowAddTimeAlert(final String stopCode) {
        final Intent intent = new Intent(this, AddTimeAlertActivity.class);
        intent.putExtra(AddTimeAlertActivity.ARG_STOPCODE, stopCode);
        startActivity(intent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowBusStopMapWithStopCode(final String stopCode) {
        final Intent intent = new Intent(this, BusStopMapActivity.class);
        intent.putExtra(BusStopMapActivity.ARG_STOPCODE, stopCode);
        startActivity(intent);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowBusTimes(final String stopCode) {
        final Intent intent = new Intent(this, DisplayStopDataActivity.class);
        intent.putExtra(DisplayStopDataActivity.ARG_STOPCODE, stopCode);
        startActivity(intent);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfirmFavouriteDeletion() {
        try {
            final DeleteFavouriteDialogFragment.Callbacks child =
                    (DeleteFavouriteDialogFragment.Callbacks)
                            getSupportFragmentManager()
                                    .findFragmentById(R.id
                                            .fragmentNearestStops);
            if (child != null) {
                child.onConfirmFavouriteDeletion();
            }
        } catch (ClassCastException e) {
            // Unable to pass the callback on. Silently fail.
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCancelFavouriteDeletion() {
        try {
            final DeleteFavouriteDialogFragment.Callbacks child =
                    (DeleteFavouriteDialogFragment.Callbacks)
                            getSupportFragmentManager()
                                    .findFragmentById(R.id
                                            .fragmentNearestStops);
            if (child != null) {
                child.onCancelFavouriteDeletion();
            }
        } catch (ClassCastException e) {
            // Unable to pass the callback on. Silently fail.
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfirmProximityAlertDeletion() {
        try {
            final DeleteProximityAlertDialogFragment.Callbacks child =
                    (DeleteProximityAlertDialogFragment.Callbacks)
                            getSupportFragmentManager()
                                    .findFragmentById(R.id
                                            .fragmentNearestStops);
            if (child != null) {
                child.onConfirmProximityAlertDeletion();
            }
        } catch (ClassCastException e) {
            // Unable to pass the callback on. Silently fail.
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCancelProximityAlertDeletion() {
        try {
            final DeleteProximityAlertDialogFragment.Callbacks child =
                    (DeleteProximityAlertDialogFragment.Callbacks)
                            getSupportFragmentManager()
                                    .findFragmentById(R.id
                                            .fragmentNearestStops);
            if (child != null) {
                child.onCancelProximityAlertDeletion();
            }
        } catch (ClassCastException e) {
            // Unable to pass the callback on. Silently fail.
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfirmTimeAlertDeletion() {
        try {
            final DeleteTimeAlertDialogFragment.Callbacks child =
                    (DeleteTimeAlertDialogFragment.Callbacks)
                            getSupportFragmentManager()
                                    .findFragmentById(R.id
                                            .fragmentNearestStops);
            if (child != null) {
                child.onConfirmTimeAlertDeletion();
            }
        } catch (ClassCastException e) {
            // Unable to pass the callback on. Silently fail.
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCancelTimeAlertDeletion() {
        try {
            final DeleteTimeAlertDialogFragment.Callbacks child =
                    (DeleteTimeAlertDialogFragment.Callbacks)
                            getSupportFragmentManager()
                                    .findFragmentById(R.id
                                            .fragmentNearestStops);
            if (child != null) {
                child.onCancelTimeAlertDeletion();
            }
        } catch (ClassCastException e) {
            // Unable to pass the callback on. Silently fail.
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onServicesChosen(final String[] chosenServices) {
        try {
            final ServicesChooserDialogFragment.Callbacks child =
                    (ServicesChooserDialogFragment.Callbacks)
                            getSupportFragmentManager()
                                    .findFragmentById(R.id
                                            .fragmentNearestStops);
            if (child != null) {
                child.onServicesChosen(chosenServices);
            }
        } catch (ClassCastException e) {
            // Unable to pass the callback on. Silently fail.
        }
    }
}