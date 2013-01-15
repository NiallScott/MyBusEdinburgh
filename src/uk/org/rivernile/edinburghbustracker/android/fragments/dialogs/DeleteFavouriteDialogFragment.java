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
import uk.org.rivernile.edinburghbustracker.android.SettingsDatabase;

/**
 * This Fragment will show a Dialog which asks the user to confirm if they wish
 * to delete the favourite bus stop or not. Objects can ask for callbacks by
 * implementing {@link DeleteFavouriteDialogFragment.EventListener} and
 * registering a callback with
 * {@link #setListener(uk.org.rivernile.edinburghbustracker.android.fragments
 * .dialogs.DeleteFavouriteDialogFragment.EventListener)}
 * 
 * @author Niall Scott
 */
public class DeleteFavouriteDialogFragment extends DialogFragment {
    
    /** The argument that is sent to this Fragment to denote the stop code. */
    private static final String ARG_STOPCODE = "stopCode";
    
    private SettingsDatabase sd;
    private EventListener listener;
    private String stopCode;
    
    /**
     * Create a new instance of the DeleteFavouriteDialogFragment, giving the
     * stopCode as the argument.
     * 
     * @param stopCode The stopCode to potentially delete.
     * @param listener Where events should be called back to.
     * @return A new instance of this DialogFragment.
     */
    public static DeleteFavouriteDialogFragment newInstance(
            final String stopCode, final EventListener listener) {
        final DeleteFavouriteDialogFragment f =
                new DeleteFavouriteDialogFragment();
        final Bundle b = new Bundle();
        b.putString(ARG_STOPCODE, stopCode);
        f.setArguments(b);
        f.setListener(listener);
        
        return f;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sd = SettingsDatabase.getInstance(
                getActivity().getApplicationContext());
        
        final Bundle args = getArguments();
        if(args == null) throw new IllegalStateException("There were no " +
                "arguments supplied to DeleteFavouriteDialogFragment.");
        
        stopCode = args.getString(ARG_STOPCODE);
        if(stopCode == null || stopCode.length() == 0)
            throw new IllegalArgumentException("The stopCode argument cannot " +
                    "be null or empty.");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());
        builder.setCancelable(true)
                .setTitle(R.string.deletefavouritedialog_title)
                .setPositiveButton(R.string.okay,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                            final int id) {
                        sd.deleteFavouriteStop(stopCode);
                        
                        if(listener != null) {
                            listener.onConfirmFavouriteDeletion();
                        }
                    }
                }).setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(final DialogInterface dialog,
                             final int id) {
                        dismiss();
                        
                        if(listener != null) {
                            listener.onCancelFavouriteDeletion();
                        }
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
         * favourite bus stop to be deleted.
         */
        public void onConfirmFavouriteDeletion();
        
        /**
         * This is called when the user has cancelled the deletion of the
         * favourite bus stop.
         */
        public void onCancelFavouriteDeletion();
    }
}