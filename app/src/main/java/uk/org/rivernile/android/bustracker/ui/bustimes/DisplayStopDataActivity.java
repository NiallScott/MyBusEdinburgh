/*
 * Copyright (C) 2009 - 2017 Niall 'Rivernile' Scott
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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import uk.org.rivernile.android.bustracker.BusApplication;
import uk.org.rivernile.android.bustracker.ui.alerts.proximity.AddProximityAlertDialogFragment;
import uk.org.rivernile.android.utils.NavigationUtils;
import uk.org.rivernile.edinburghbustracker.android
        .AddEditFavouriteStopActivity;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .DeleteFavouriteDialogFragment;
import uk.org.rivernile.android.bustracker.ui.alerts.proximity.DeleteProximityAlertDialogFragment;
import uk.org.rivernile.android.bustracker.ui.alerts.time.DeleteTimeAlertDialogFragment;

/**
 * This Activity hosts a DisplayStopDataFragment which shows the user live bus
 * times.
 * 
 * This Activity can be started by any other application with the following
 * Intent action;
 * 
 * uk.org.rivernile.edinburghbustracker.android.ACTION_VIEW_STOP_DATA
 * 
 * A String parameter called "stopCode" must be added as an extra to the Intent
 * which contains the bus stop code to be loaded.
 * 
 * @author Niall Scott
 * @see DisplayStopDataFragment
 */
public class DisplayStopDataActivity extends AppCompatActivity
        implements DisplayStopDataFragment.Callbacks {
    
    /** The ACTION_VIEW_STOP_DATA intent action name. */
    public static final String ACTION_VIEW_STOP_DATA =
            "uk.org.rivernile.edinburghbustracker.android."
            + "ACTION_VIEW_STOP_DATA";
    
    /** The Intent argument for the stop code. */
    public static final String ARG_STOPCODE =
            DisplayStopDataFragment.ARG_STOPCODE;
    
    private static final String DIALOG_CONFIRM_DELETE_FAVOURITE =
            "deleteFavDialog";
    private static final String DIALOG_DELETE_PROX_ALERT = "delProxAlertDialog";
    private static final String DIALOG_DELETE_TIME_ALERT = "delTimeAlertDialog";
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.single_fragment_container);
        
        if (savedInstanceState == null) {
            final Intent intent = getIntent();
            final String stopCode;
            
            if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                stopCode = intent.getData().getQueryParameter("busStopCode");
            } else {
                stopCode = intent.getStringExtra(ARG_STOPCODE);
            }
            
            final BusApplication app = (BusApplication) getApplication();
            final DisplayStopDataFragment f = app.getFragmentFactory()
                    .getDisplayStopDataFragment(stopCode);
            
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, f).commit();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                NavigationUtils
                        .navigateUpOnActivityWithMultipleEntryPoints(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
        AddProximityAlertDialogFragment.newInstance(stopCode)
                .show(getSupportFragmentManager(), "this will be removed soon");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowAddTimeAlert(final String stopCode,
            final String[] defaultServices) {
        // This will soon be removed.
    }
}