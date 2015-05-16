/*
 * Copyright (C) 2012 - 2014 Niall 'Rivernile' Scott
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
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;
import uk.org.rivernile.android.utils.NavigationUtils;
import uk.org.rivernile.android.bustracker.ui.bustimes.DisplayStopDataActivity;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .DeleteFavouriteDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .DeleteProximityAlertDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .DeleteTimeAlertDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .InstallStreetViewDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.general
        .BusStopDetailsFragment;

/**
 * This Activity hosts the BusStopDetailsFragment which shows details for a bus
 * stop.
 * 
 * @author Niall Scott
 */
public class BusStopDetailsActivity extends AppCompatActivity
        implements BusStopDetailsFragment.Callbacks,
        DeleteFavouriteDialogFragment.Callbacks,
        DeleteProximityAlertDialogFragment.Callbacks,
        DeleteTimeAlertDialogFragment.Callbacks,
        InstallStreetViewDialogFragment.Callbacks {
    
    /** The Intent argument for stopCode. */
    public static final String ARG_STOPCODE =
            BusStopDetailsFragment.ARG_STOPCODE;
    
    private static final String STREET_VIEW_APP_PACKAGE =
            "market://details?id=com.google.android.street";
    
    private static final String DIALOG_CONFIRM_DELETE_FAVOURITE =
            "deleteFavDialog";
    private static final String DIALOG_DELETE_PROX_ALERT = "delProxAlertDialog";
    private static final String DIALOG_DELETE_TIME_ALERT = "delTimeAlertDialog";
    private static final String DIALOG_INSTALL_STREET_VIEW =
            "installStreetViewDialog";
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.single_fragment_container);
        
        if(savedInstanceState == null) {
            final Intent intent = getIntent();
            
            if(!intent.hasExtra(ARG_STOPCODE)) {
                throw new IllegalArgumentException(
                        "A stopCode MUST be provided.");
            }
            
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer,
                        BusStopDetailsFragment.newInstance(
                            intent.getStringExtra(ARG_STOPCODE)))
                    .commit();
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
    public void onShowBusTimes(final String stopCode) {
        final Intent intent = new Intent(this, DisplayStopDataActivity.class);
        intent.putExtra(DisplayStopDataActivity.ARG_STOPCODE, stopCode);
        startActivity(intent);
    }
    
    @Override
    public void onShowStreetView(final double latitude,
            final double longitude) {
        final StringBuilder sb = new StringBuilder();
        sb.append("google.streetview:cbll=");
        sb.append(latitude);
        sb.append(',');
        sb.append(longitude);
        sb.append("&cbp=1,0,,0,1.0&mz=19");
        
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(sb.toString())));
        } catch (ActivityNotFoundException e) {
            new InstallStreetViewDialogFragment()
                .show(getSupportFragmentManager(), DIALOG_INSTALL_STREET_VIEW);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowInstallStreetView() {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(STREET_VIEW_APP_PACKAGE));

        try {
            startActivity(intent);
        } catch(ActivityNotFoundException e) {
            Toast.makeText(this, R.string.streetviewdialog_noplaystore,
                    Toast.LENGTH_LONG).show();
        }
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
    public void onShowAddTimeAlert(final String stopCode,
            final String[] defaultServices) {
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
    public void onConfirmFavouriteDeletion() {
        try {
            final DeleteFavouriteDialogFragment.Callbacks child =
                    (DeleteFavouriteDialogFragment.Callbacks)
                            getSupportFragmentManager()
                                    .findFragmentById(R.id.fragmentContainer);
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
                                    .findFragmentById(R.id.fragmentContainer);
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
                                    .findFragmentById(R.id.fragmentContainer);
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
                                    .findFragmentById(R.id.fragmentContainer);
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
                                    .findFragmentById(R.id.fragmentContainer);
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
                                    .findFragmentById(R.id.fragmentContainer);
            if (child != null) {
                child.onCancelTimeAlertDeletion();
            }
        } catch (ClassCastException e) {
            // Unable to pass the callback on. Silently fail.
        }
    }
}