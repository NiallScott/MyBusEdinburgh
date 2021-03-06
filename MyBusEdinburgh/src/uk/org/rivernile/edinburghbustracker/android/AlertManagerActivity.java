/*
 * Copyright (C) 2011 - 2013 Niall 'Rivernile' Scott
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

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import uk.org.rivernile.android.utils.NavigationUtils;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .DeleteAllAlertsDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .DeleteProximityAlertDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .DeleteTimeAlertDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.general
        .AlertManagerFragment;

/**
 * This Activity hosts the AlertManagerFragment which shows the user which
 * alerts they have set and the user is able to cancel alerts from here.
 * 
 * @author Niall Scott
 * @see AlertManagerFragment
 */
public class AlertManagerActivity extends ActionBarActivity
        implements AlertManagerFragment.Callbacks,
        DeleteAllAlertsDialogFragment.Callbacks,
        DeleteProximityAlertDialogFragment.Callbacks,
        DeleteTimeAlertDialogFragment.Callbacks {
    
    private static final String DIALOG_DELETE_ALL_ALERTS_TAG =
            "delAllAlertsDialog";
    private static final String DIALOG_DELETE_PROX_ALERT =
            "delProxAlertDialog";
    private static final String DIALOG_DELETE_TIME_ALERT =
            "delTimeAlertDialog";

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.alertmanager_activity);
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
    public void onShowConfirmDeleteAllAlerts() {
        new DeleteAllAlertsDialogFragment().show(getSupportFragmentManager(),
                DIALOG_DELETE_ALL_ALERTS_TAG);
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
    public void onConfirmAllAlertsDeletion() {
        try {
            final DeleteAllAlertsDialogFragment.Callbacks child =
                    (DeleteAllAlertsDialogFragment.Callbacks)
                            getSupportFragmentManager()
                                    .findFragmentById(R.id
                                            .fragmentAlertManager);
            if (child != null) {
                child.onConfirmAllAlertsDeletion();
            }
        } catch (ClassCastException e) {
            // Unable to pass the callback on. Silently fail.
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onCancelAllAlertsDeletion() {
        try {
            final DeleteAllAlertsDialogFragment.Callbacks child =
                    (DeleteAllAlertsDialogFragment.Callbacks)
                            getSupportFragmentManager()
                                    .findFragmentById(R.id
                                            .fragmentAlertManager);
            if (child != null) {
                child.onCancelAllAlertsDeletion();
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
                                            .fragmentAlertManager);
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
                                            .fragmentAlertManager);
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
                                            .fragmentAlertManager);
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
                                            .fragmentAlertManager);
            if (child != null) {
                child.onCancelTimeAlertDeletion();
            }
        } catch (ClassCastException e) {
            // Unable to pass the callback on. Silently fail.
        }
    }
}