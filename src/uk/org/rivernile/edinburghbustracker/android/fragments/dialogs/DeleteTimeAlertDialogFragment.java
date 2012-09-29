/*
 * Copyright (C) 2012 Niall 'Rivernile' Scott
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.edinburghbustracker.android.alerts.AlertManager;

/**
 * This Fragment will show a Dialog which asks the user to confirm if they wish
 * to delete the time alert or not. Objects can ask for callbacks by
 * implementing {@link DeleteTimeAlertDialogFragment.EventListener} and
 * registering a callback with
 * {@link #setListener(uk.org.rivernile.edinburghbustracker.android.fragments
 * .dialogs.DeleteTimeAlertDialogFragment.EventListener)}
 * 
 * @author Niall Scott
 */
public class DeleteTimeAlertDialogFragment extends DialogFragment {
    
    private AlertManager alertMan;
    private EventListener listener;
    
    /**
     * Create a new instance of the DeleteTimeAlertDialogFragment.
     * 
     * @param listener Where events should be called back to.
     * @return A new instance of this DialogFragment.
     */
    public static DeleteTimeAlertDialogFragment newInstance(
            final EventListener listener) {
        final DeleteTimeAlertDialogFragment f =
                new DeleteTimeAlertDialogFragment();
        f.setListener(listener);
        
        return f;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Make sure to get the AlertManager.
        alertMan = AlertManager.getInstance(
                getActivity().getApplicationContext());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());
        builder.setCancelable(true)
            .setTitle(R.string.alert_time_rem_confirm)
            .setPositiveButton(R.string.okay,
            new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int id) {
                // The user has confirmed they want to delete the alert.
                alertMan.removeTimeAlert();
                
                if(listener != null) listener.onConfirmTimeAlertDeletion();
            }
        }).setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
             @Override
             public void onClick(final DialogInterface dialog, final int id) {
                dismiss();
                
                if(listener != null) listener.onCancelTimeAlertDeletion();
             }
        });
        
        return builder.create();
    }
    
    /**
     * Set the listener which listens out for dialog events.
     * 
     * @param listener Where to call back to.
     */
    public void setListener(final EventListener listener) {
        this.listener = listener;
    }
    
    /**
     * The EventListener is an interface that any class which wants callbacks
     * from this Fragment should implement.
     */
    public interface EventListener {
        
        /**
         * This is called when the user has confirmed that they wish for the
         * time alert to be deleted.
         */
        public void onConfirmTimeAlertDeletion();
        
        /**
         * This is called when the user has cancelled the deletion of the
         * time alert.
         */
        public void onCancelTimeAlertDeletion();
    }
}