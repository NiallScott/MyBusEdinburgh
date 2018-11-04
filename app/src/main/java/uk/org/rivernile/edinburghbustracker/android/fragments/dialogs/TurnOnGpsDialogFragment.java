/*
 * Copyright (C) 2012 - 2018 Niall 'Rivernile' Scott
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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import uk.org.rivernile.android.bustracker.BusApplication;
import uk.org.rivernile.android.bustracker.preferences.PreferenceManager;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowSystemLocationPreferencesListener;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link DialogFragment} asks the user if they wish to turn on the GPS receiver on their
 * device. It additionally asks the user if they wish to not be asked again. If the user confirms
 * the {@link Dialog}, they are taken to the system settings where they can turn GPS on.
 * 
 * @author Niall Scott
 */
public class TurnOnGpsDialogFragment extends DialogFragment {
    
    /** The Intent to use to show the GPS settings Activity. */
    public static final Intent TURN_ON_GPS_INTENT;
    
    private Callbacks callbacks;
    private PreferenceManager preferenceManager;
    
    static {
        // TODO: sort deprecation.
        TURN_ON_GPS_INTENT = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        TURN_ON_GPS_INTENT.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        
        try {
            callbacks = (Callbacks) context;
        } catch (ClassCastException e) {
            throw new IllegalStateException(context.getClass().getName() + " does not implement " +
                    Callbacks.class.getName());
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        preferenceManager = ((BusApplication) getContext().getApplicationContext())
                .getPreferenceManager();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Activity activity = getActivity();
        final LayoutInflater inflater = LayoutInflater.from(activity);
        
        final View v = inflater.inflate(R.layout.turn_on_gps, null);
        final CheckBox cb = v.findViewById(R.id.chkTurnongps);
        cb.setOnCheckedChangeListener(
                (v1, isChecked) -> preferenceManager.setGpsPromptDisabled(isChecked));

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(true)
                .setTitle(R.string.turnongpsdialog_title)
                .setView(v)
                .setPositiveButton(R.string.yes,
                        (dialog, id) -> callbacks.onShowSystemLocationPreferences())
                .setNegativeButton(R.string.no, null);
        
        return builder.create();
    }
    
    /**
     * Any {@link Activity Activities} which host this {@link DialogFragment} must implement this
     * interface to handle navigation events.
     */
    public interface Callbacks extends OnShowSystemLocationPreferencesListener {
        
        // Nothing to put here - the interfaces are defined elsewhere.
    }
}