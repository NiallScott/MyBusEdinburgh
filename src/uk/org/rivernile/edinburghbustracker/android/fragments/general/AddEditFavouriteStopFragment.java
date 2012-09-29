/*
 * Copyright (C) 2009 - 2012 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android.fragments.general;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.edinburghbustracker.android.SettingsDatabase;

/**
 * This Fragment allows the user to add or edit a currently saved favourite
 * bus stop. It will change its title and action depending on whether the bus
 * stop is already saved in the database.
 * 
 * @author Niall Scott
 */
public class AddEditFavouriteStopFragment extends Fragment
        implements View.OnClickListener {
    
    /** The stopCode argument key. */
    private static final String ARG_STOPCODE = "stopCode";
    /** The stopName argument key. */
    private static final String ARG_STOPNAME = "stopName";
    
    private String stopCode;
    private String stopName;
    private EditText edit;
    private Button btnOkay;
    private boolean editing;
    private SettingsDatabase sd;
    
    /**
     * Create a new instance of AddEditFavouriteStopFragment.
     * 
     * @param stopCode The bus stop code to add or edit as a favourite.
     * @param stopName The name for this bus stop which is pre-populated in the
     * EditText field.
     * @return A new instance of this Fragment.
     */
    public static AddEditFavouriteStopFragment newInstance(
            final String stopCode, final String stopName) {
        final AddEditFavouriteStopFragment f =
                new AddEditFavouriteStopFragment();
        final Bundle b = new Bundle();
        b.putString(ARG_STOPCODE, stopCode);
        b.putString(ARG_STOPNAME, stopName);
        f.setArguments(b);
        
        return f;
    }
    
    /**
     * {@inheritDoc} 
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final Bundle args = getArguments();
        // Make sure that arguments exist. If they don't, throw an exception.
        if(args == null) throw new IllegalStateException("There were no " +
                "arguments supplied to AddEditFavouriteStopsFragment.");
        
        final Activity activity = getActivity();
        sd = SettingsDatabase.getInstance(activity.getApplicationContext());
        
        // Get the arguments.
        stopCode = args.getString(ARG_STOPCODE);
        stopName = args.getString(ARG_STOPNAME);
        
        // Check that the stopCode exists.
        if(stopCode == null || stopCode.length() == 0)
            throw new IllegalArgumentException("The stopCode must not be " +
                    "null or blank.");
        
        // If the stopName doesn't exist, try to get the existing name from
        // the settings database.
        if(stopName == null || stopName.length() == 0)
            stopName = sd.getNameForStop(stopCode);
        
        // Check to see if it already exists.
        editing = sd.getFavouriteStopExists(stopCode);
        // Set the appropriate title.
        if(editing) {
            activity.setTitle(getString(R.string.addeditstop_title_edit) + " " +
                    stopCode);
        } else {
            activity.setTitle(getString(R.string.addeditstop_title_add) + " " +
                    stopCode);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {
        // Inflate the View from the XML resource.
        final View v = inflater.inflate(R.layout.addeditfavouritestop,
                container, false);
        
        // Get the UI elements.
        edit = (EditText)v.findViewById(R.id.addeditstop_edit_stopname);
        btnOkay = (Button)v.findViewById(R.id.addeditstop_button_ok);
        final Button btnCancel = (Button)v.findViewById(
                R.id.addeditstop_button_cancel);
        btnOkay.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        
        return v;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        // Set the default text in the text box to the known name and set the
        // cursor to the end of the string.
        edit.setText(stopName);
        edit.setSelection(stopName.length());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(final View v) {
        final Activity activity = getActivity();
        
        if(v == btnOkay) {
            final String s = edit.getText().toString().trim();
            // The stop name is an empty string. Alert the user that this is
            // not allowed.
            if(s.length() == 0) {
                Toast.makeText(activity,
                        R.string.addeditstop_error_blankstopname,
                        Toast.LENGTH_LONG).show();
                return;
            }

            // Whether we add or edit depends on whether the favourite stop
            // already exists.
            if(editing) {
                sd.modifyFavouriteStop(stopCode, s);
            } else {
                sd.insertFavouriteStop(stopCode, s);
            }
        }
        
        // For both confirming and cancelling, the Activity needs to finish.
        activity.finish();
    }
}