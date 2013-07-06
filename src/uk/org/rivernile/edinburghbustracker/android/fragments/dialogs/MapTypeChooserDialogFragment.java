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
import com.google.android.gms.maps.GoogleMap;
import java.lang.ref.WeakReference;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This DialogFragment allows users to select what type of map to display. The
 * map may be a normal vector map, a satellite map or a terrain map.
 * 
 * @author Niall Scott
 */
public class MapTypeChooserDialogFragment extends DialogFragment {
    
    private WeakReference<EventListener> listener;
    
    /**
     * Create a new instance of this DialogFragment, specifying the listener
     * object.
     * 
     * @param listener Where the event callbacks should go back to.
     * @return A new instance of this DialogFragment.
     */
    public static MapTypeChooserDialogFragment newInstance(
            final EventListener listener) {
        final MapTypeChooserDialogFragment f =
                new MapTypeChooserDialogFragment();
        f.setListener(listener);
        
        return f;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());
        builder.setCancelable(true)
                .setTitle(R.string.maptypechooserdialog_title)
                .setItems(R.array.maptypechooserdialog_items,
                    new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                // If there's no listener to call back to, no point in
                // continuing.
                if(listener == null) return;
                final EventListener listenerRef = listener.get();
                if (listenerRef == null) return;
                
                switch(which) {
                    case 0:
                        listenerRef.onMapTypeChosen(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case 1:
                        listenerRef.onMapTypeChosen(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                    case 2:
                        listenerRef.onMapTypeChosen(GoogleMap.MAP_TYPE_TERRAIN);
                        break;
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
        if (listener == null) {
            this.listener = null;
        } else {
            this.listener = new WeakReference<EventListener>(listener);
        }
    }
    
    /**
     * The EventListener is an interface that any class which wants callbacks
     * from this Fragment should implement.
     */
    public interface EventListener {
        
        /**
         * When the user makes a selection, this event is called on the event
         * listener.
         * 
         * @param mapType The type of map the user chose.
         */
        public void onMapTypeChosen(int mapType);
    }
}