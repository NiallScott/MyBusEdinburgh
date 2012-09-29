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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import uk.org.rivernile.edinburghbustracker.android.PreferencesActivity;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This DialogFragment asks the user if they wish to turn on the GPS receiver on
 * their device. It additionally asks the user if they wish to not be asked
 * again. If the user confirms the Dialog, they are taken to the system settings
 * where they can turn GPS on.
 * 
 * @author Niall Scott
 */
public class TurnOnGpsDialogFragment extends DialogFragment {
    
    public static final Intent TURN_ON_GPS_INTENT =
            new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    
    private SharedPreferences sp;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sp = getActivity().getSharedPreferences(PreferencesActivity.PREF_FILE,
                0);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Activity activity = getActivity();
        final LayoutInflater inflater = LayoutInflater.from(activity);
        
        final View v = inflater.inflate(R.layout.neareststops_gpsdialog, null);
        final CheckBox cb = (CheckBox)v.findViewById(R.id.chkTurnongps);
        cb.setOnCheckedChangeListener(new CompoundButton
                .OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton v,
                    boolean isChecked) {
                SharedPreferences.Editor edit = sp.edit();
                edit.putBoolean("neareststops_gps_prompt_disable",
                        isChecked);
                edit.commit();
            }
        });

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(true)
                .setTitle(R.string.neareststops_turnongps_title)
                .setView(v)
                .setInverseBackgroundForced(true)
                .setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog,
                    final int id) {
                try {
                    startActivity(TURN_ON_GPS_INTENT);
                } catch(ActivityNotFoundException e) { }
            }
        }).setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog,
                    final int id) {
                dismiss();
            }
        });
        
        return builder.create();
    }
}