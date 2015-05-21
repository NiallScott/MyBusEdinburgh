/*
 * Copyright (C) 2012 - 2015 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android.fragments.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.edinburghbustracker.android.alerts.AlertManager;

/**
 * This {@link DialogFragment} will show an {@link AlertDialog} which asks the user to confirm if
 * they wish to delete the proximity alert or not.
 * 
 * @author Niall Scott
 */
public class DeleteProximityAlertDialogFragment extends DialogFragment {
    
    private AlertManager alertMan;
    private Callbacks callbacks;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        
        try {
            callbacks = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new IllegalStateException(activity.getClass().getName() + " does not implement " +
                    Callbacks.class.getName());
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Make sure to get the AlertManager.
        alertMan = AlertManager.getInstance(getActivity().getApplicationContext());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true)
                .setTitle(R.string.deleteproxdialog_title)
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int id) {
                        alertMan.removeProximityAlert();
                        callbacks.onConfirmProximityAlertDeletion();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int id) {
                        callbacks.onCancelProximityAlertDeletion();
                    }
                });
        
        return builder.create();
    }
    
    /**
     * Any {@link Activity Activities} which host this {@link DialogFragment} must implement this
     * interface to handle navigation events.
     */
    public interface Callbacks {
        
        /**
         * This is called when the user has confirmed that they wish for the proximity alert to be
         * deleted.
         */
        void onConfirmProximityAlertDeletion();
        
        /**
         * This is called when the user has cancelled the deletion of the proximity alert.
         */
        void onCancelProximityAlertDeletion();
    }
}