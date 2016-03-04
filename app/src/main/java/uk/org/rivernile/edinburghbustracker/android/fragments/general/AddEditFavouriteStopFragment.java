/*
 * Copyright (C) 2009 - 2016 Niall 'Rivernile' Scott
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
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import uk.org.rivernile.android.bustracker.database.settings.SettingsContract;
import uk.org.rivernile.android.bustracker.database.settings.loaders.AddFavouriteStopTask;
import uk.org.rivernile.android.bustracker.database.settings.loaders.EditFavouriteStopTask;
import uk.org.rivernile.android.bustracker.database.settings.loaders.FavouriteStopsLoader;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link Fragment} allows the user to add or edit a currently saved favourite bus stop. It
 * will change its title and action depending on whether the bus stop is already saved in the
 * database.
 * 
 * @author Niall Scott
 */
public class AddEditFavouriteStopFragment extends Fragment implements View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    
    /** The stopCode argument key. */
    public static final String ARG_STOPCODE = "stopCode";
    /** The stopName argument key. */
    public static final String ARG_STOPNAME = "stopName";

    private static final String STATE_NAME_REQUIRES_POPULATION = "nameRequiresPopulation";
    
    private String stopCode;
    private String stopName;
    private Cursor cursor;
    private boolean nameRequiresPopulation;

    private EditText edit;
    private Button btnOkay;
    
    /**
     * Create a new instance of {@code AddEditFavouriteStopFragment}.
     * 
     * @param stopCode The bus stop code to add or edit as a favourite.
     * @param stopName The name for this bus stop which is pre-populated in the {@link EditText}
     * field.
     * @return A new instance of this {@link Fragment}.
     */
    public static AddEditFavouriteStopFragment newInstance(@NonNull final String stopCode,
            @Nullable final String stopName) {
        final AddEditFavouriteStopFragment f = new AddEditFavouriteStopFragment();
        final Bundle b = new Bundle();
        b.putString(ARG_STOPCODE, stopCode);
        b.putString(ARG_STOPNAME, stopName);
        f.setArguments(b);
        
        return f;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nameRequiresPopulation = savedInstanceState == null ||
                savedInstanceState.getBoolean(STATE_NAME_REQUIRES_POPULATION);
        
        final Bundle args = getArguments();
        // Make sure that arguments exist. If they don't, throw an exception.
        if (args == null) {
            throw new IllegalStateException("There were no arguments supplied to " +
                    "AddEditFavouriteStopsFragment.");
        }
        
        // Get the arguments.
        stopCode = args.getString(ARG_STOPCODE);
        stopName = args.getString(ARG_STOPNAME);
        
        // Check that the stopCode exists.
        if (TextUtils.isEmpty(stopCode)) {
            throw new IllegalArgumentException("The stopCode must not be null or blank.");
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        // Inflate the View from the XML resource.
        final View v = inflater.inflate(R.layout.addeditfavouritestop, container, false);
        
        // Get the UI elements.
        edit = (EditText) v.findViewById(R.id.addeditstop_edit_stopname);
        btnOkay = (Button) v.findViewById(R.id.addeditstop_button_ok);
        final Button btnCancel = (Button) v.findViewById(R.id.addeditstop_button_cancel);

        btnOkay.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_NAME_REQUIRES_POPULATION, nameRequiresPopulation);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        return new FavouriteStopsLoader(getActivity(), stopCode);
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
        handleCursorLoaded(data);
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        handleCursorLoaded(null);
    }

    @Override
    public void onClick(final View v) {
        final Activity activity = getActivity();
        
        if (v == btnOkay) {
            final String s = edit.getText().toString().trim();
            // The stop name is an empty string. Alert the user that this is not allowed.
            if (TextUtils.isEmpty(s)) {
                Toast.makeText(activity, R.string.addeditstop_error_blankstopname,
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (cursor != null) {
                // Whether we add or edit depends on whether the favourite stop already exists.
                if (cursor.getCount() > 0) {
                    EditFavouriteStopTask.start(getActivity(),
                            cursor.getLong(cursor.getColumnIndex(SettingsContract.Favourites._ID)),
                            s);
                } else {
                    AddFavouriteStopTask.start(getActivity(), stopCode, s);
                }
            } else {
                // Shouldn't be allowed to happen - don't progress.
                return;
            }
        }
        
        // For both confirming and cancelling, the Activity needs to finish.
        activity.finish();
    }

    /**
     * Handle the loading of the favourites {@link Cursor}.
     *
     * @param cursor The loaded favourites {@link Cursor}.
     */
    private void handleCursorLoaded(@Nullable final Cursor cursor) {
        this.cursor = cursor;

        if (cursor != null) {
            btnOkay.setEnabled(true);

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                getActivity().setTitle(getString(R.string.addeditstop_title_edit, stopCode));

                if (nameRequiresPopulation) {
                    nameRequiresPopulation = false;
                    stopName = cursor.getString(
                            cursor.getColumnIndex(SettingsContract.Favourites.STOP_NAME));
                    edit.setText(stopName);
                    edit.setSelection(stopName.length());
                }
            } else {
                getActivity().setTitle(getString(R.string.addeditstop_title_add, stopCode));

                if (nameRequiresPopulation) {
                    nameRequiresPopulation = false;
                    edit.setText(stopName);
                    edit.setSelection(stopName.length());
                }
            }
        } else {
            btnOkay.setEnabled(false);
        }
    }
}