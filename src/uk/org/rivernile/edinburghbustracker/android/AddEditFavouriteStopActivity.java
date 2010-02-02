/*
 * Copyright (C) 2009 Niall 'Rivernile' Scott
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

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddEditFavouriteStopActivity extends Activity
        implements View.OnClickListener {

    public final static String ACTION_ADD_EDIT_FAVOURITE_STOP =
            "uk.org.rivernile.edinburghbustracker.android." +
            "ACTION_ADD_EDIT_FAVOURITE_STOP";

    private String stopCode;
    private String stopName;
    private EditText edit;
    private Button okay;
    private Button cancel;
    private boolean editing;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.addeditstop_title);
        setContentView(R.layout.addeditfavouritestop);

        stopCode = getIntent().getStringExtra("stopCode");
        stopName = getIntent().getStringExtra("stopName");
        if(stopName == null || stopName.length() == 0) {
            stopName = SettingsDatabase.getNameForStop(this, stopCode);
        }
        edit = (EditText)findViewById(R.id.addeditstop_edit_stopname);
        okay = (Button)findViewById(R.id.addeditstop_button_ok);
        cancel = (Button)findViewById(R.id.addeditstop_button_cancel);
        okay.setOnClickListener(this);
        cancel.setOnClickListener(this);
        edit.setText(stopName);

        editing = SettingsDatabase.getFavouriteStopExists(this, stopCode);
        if(editing) {
            setTitle(getString(R.string.addeditstop_title_edit) + " " +
                    stopCode);
        } else {
            setTitle(getString(R.string.addeditstop_title_add) + " " +
                    stopCode);
        }
    }

    @Override
    public void onClick(final View v) {
        if(v == okay) {
            String s = edit.getText().toString().trim();
            if(s.length() == 0) {
                Toast.makeText(this, R.string.addeditstop_error_blankstopname,
                        Toast.LENGTH_LONG).show();
                return;
            }
            if(editing) {
                SettingsDatabase.modifyFavouriteStop(this, stopCode, s);
            } else {
                SettingsDatabase.insertFavouriteStop(this, stopCode, s);
            }
        }
        finish();
    }
}