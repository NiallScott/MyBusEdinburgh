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

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import java.lang.ref.WeakReference;

/**
 * This DialogFragment contains an indeterminate progress Dialog. It is up to
 * whatever uses this Dialog to specify what message is display to the user.
 * 
 * @author Niall Scott
 */
public class IndeterminateProgressDialogFragment extends DialogFragment {
    
    private WeakReference<EventListener> listener;
    private String message;
    
    /**
     * Create a new instance of this DialogFragment, specifying the listener
     * callback and the message to display to the user.
     * 
     * @param listener Where to send events back to.
     * @param message The message to display to the user.
     * @return A new instance of this DialogFragment.
     */
    public static IndeterminateProgressDialogFragment newInstance(
            final EventListener listener, final String message) {
        final IndeterminateProgressDialogFragment f =
                new IndeterminateProgressDialogFragment();
        f.setListener(listener);
        f.setMessage(message);
        
        return f;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setRetainInstance(true);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final ProgressDialog d = new ProgressDialog(getActivity());
        d.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        d.setCancelable(true);
        d.setMessage(message);
        
        return d;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCancel(final DialogInterface di) {
        super.onCancel(di);
        
        if(listener != null) {
            final EventListener listenerRef = listener.get();
            if (listenerRef != null) {
                listenerRef.onProgressCancel();
            }
        }
    }
    
    /**
     * Set the listener which listens out for dialog events.
     * 
     * @param listener Where to call back to.
     */
    public void setListener(final EventListener listener) {
        if (listener == null) {
            this.listener = null;
        } else {
            this.listener = new WeakReference<EventListener>(listener);
        }
    }
    
    /**
     * Set the message to be displayed to the user.
     * 
     * @param message The message to be displayed to the user.
     */
    public void setMessage(final String message) {
        this.message = message;
    }
    
    /**
     * The EventListener is an interface that any class which wants callbacks
     * from this Fragment should implement.
     */
    public interface EventListener {
        
        /**
         * When the user cancels this DialogFragment, this method is called upon
         * the event listener.
         */
        public void onProgressCancel();
    }
}