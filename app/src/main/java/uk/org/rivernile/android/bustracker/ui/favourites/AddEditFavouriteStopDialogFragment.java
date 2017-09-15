/*
 * Copyright (C) 2017 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.favourites;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract;
import uk.org.rivernile.android.bustracker.database.busstop.loaders.BusStopLoader;
import uk.org.rivernile.android.bustracker.database.settings.SettingsContract;
import uk.org.rivernile.android.bustracker.database.settings.loaders.AddFavouriteStopTask;
import uk.org.rivernile.android.bustracker.database.settings.loaders.EditFavouriteStopTask;
import uk.org.rivernile.android.bustracker.database.settings.loaders.FavouriteStopsLoader;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * Show a {@link DialogFragment} which allows the user to add a new favourite stop, or edit the name
 * of an existing one. This {@link DialogFragment} will determine if the given stop code is already
 * a favourite stop and present the correct UI.
 *
 * @author Niall Scott
 */
public class AddEditFavouriteStopDialogFragment extends DialogFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String ARG_STOPCODE = "stopCode";

    private static final int LOADER_FAVOURITE_STOP = 1;
    private static final int LOADER_BUS_STOP = 2;

    private static final String STATE_IS_NAME_POPULATED = "isNamePopulated";

    private String stopCode;
    private boolean isNamePopulated;
    private boolean isEditMode;
    private boolean isFavouriteStopLoading;
    private boolean isBusStopDetailsLoading;
    private Cursor favouriteStopCursor;
    private Cursor stopDetailsCursor;

    private ProgressBar progress;
    private View layoutContent;
    private TextView txtBlurb;
    private EditText editName;

    /**
     * Create a new {@code AddEditFavouriteStopDialogFragment}, supplying the stop code to be added
     * or edited.
     *
     * @param stopCode The stop code to be added or edited.
     * @return A new instance of {@code AddEditFavouriteStopDialogFragment}.
     */
    @NonNull
    public static AddEditFavouriteStopDialogFragment newInstance(@NonNull final String stopCode) {
        final AddEditFavouriteStopDialogFragment fragment =
                new AddEditFavouriteStopDialogFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_STOPCODE, stopCode);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(true);
        stopCode = getArguments().getString(ARG_STOPCODE);

        if (savedInstanceState != null) {
            isNamePopulated = savedInstanceState.getBoolean(STATE_IS_NAME_POPULATED);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Context context = getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View v = inflater.inflate(R.layout.addeditfavouritestop2, null, false);
        progress = (ProgressBar) v.findViewById(R.id.progress);
        layoutContent = v.findViewById(R.id.layoutContent);
        txtBlurb = (TextView) v.findViewById(R.id.txtBlurb);
        editName = (EditText) v.findViewById(R.id.editName);

        editName.addTextChangedListener(new NameWatcher());

        return new AlertDialog.Builder(context)
                .setTitle(R.string.addeditfavouritestopdialog_title_add)
                .setPositiveButton(R.string.addeditfavouritestopdialog_button_add,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                handlePositiveButtonClick();
                            }
                        })
                .setNegativeButton(R.string.cancel, null)
                .setView(v)
                .create();
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        showProgress();
        loadFavouriteBusStop();
        loadBusStopDetails();
    }

    @Override
    public void onStart() {
        super.onStart();

        updateDialogTitle();
        updatePositiveButtonEnabledState();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_IS_NAME_POPULATED, isNamePopulated);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        switch (id) {
            case LOADER_FAVOURITE_STOP:
                return new FavouriteStopsLoader(getContext(), stopCode);
            case LOADER_BUS_STOP:
                return new BusStopLoader(getContext(), stopCode,
                        new String[] {
                                BusStopContract.BusStops.STOP_NAME,
                                BusStopContract.BusStops.LOCALITY
                        });
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
        switch (loader.getId()) {
            case LOADER_FAVOURITE_STOP:
                handleFavouriteBusStopLoadComplete(data);
                break;
            case LOADER_BUS_STOP:
                handleBusStopDetailsLoadComplete(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_FAVOURITE_STOP:
                handleFavouriteBusStopLoadComplete(null);
                break;
            case LOADER_BUS_STOP:
                handleBusStopDetailsLoadComplete(null);
                break;
        }
    }

    @Override
    public AlertDialog getDialog() {
        return (AlertDialog) super.getDialog();
    }

    /**
     * Begin loading the favourite stop.
     */
    private void loadFavouriteBusStop() {
        isFavouriteStopLoading = true;
        getLoaderManager().initLoader(LOADER_FAVOURITE_STOP, null, this);
    }

    /**
     * Begin loading the bus stop details.
     */
    private void loadBusStopDetails() {
        isBusStopDetailsLoading = true;
        getLoaderManager().initLoader(LOADER_BUS_STOP, null, this);
    }

    /**
     * Handle the favourite bus stop loading complete.
     *
     * @param cursor The {@link Cursor} containing favourite stop information.
     */
    private void handleFavouriteBusStopLoadComplete(@Nullable final Cursor cursor) {
        isFavouriteStopLoading = false;
        favouriteStopCursor = cursor;
        processCursors();

        if (!isBusStopDetailsLoading) {
            showContent();
        }
    }

    /**
     * Handle the bus stop details loading complete.
     *
     * @param cursor The {@link Cursor} containing bus stop details.
     */
    private void handleBusStopDetailsLoadComplete(@Nullable final Cursor cursor) {
        isBusStopDetailsLoading = false;
        stopDetailsCursor = cursor;
        processCursors();

        if (!isFavouriteStopLoading) {
            showContent();
        }
    }

    /**
     * Process the data in the {@link Cursor}s to present the correct UI.
     */
    private void processCursors() {
        final String blurb;

        if (favouriteStopCursor != null && stopDetailsCursor != null) {
            final String stopName = getLongStopName(stopDetailsCursor);
            isEditMode = favouriteStopCursor.moveToFirst();

            if (isEditMode) {
                blurb = getString(R.string.addeditfavouritestopdialog_blurb_edit, stopName);

                if (!isNamePopulated) {
                    final int nameColumn = favouriteStopCursor
                            .getColumnIndex(SettingsContract.Favourites.STOP_NAME);
                    setStopName(favouriteStopCursor.getString(nameColumn));
                }
            } else {
                blurb = getString(R.string.addeditfavouritestopdialog_blurb_add, stopName);

                if (!isNamePopulated) {
                    setStopName(getShortStopName(stopDetailsCursor));
                }
            }
        } else {
            blurb = null;
        }

        txtBlurb.setText(blurb);
        updateDialogTitle();
    }

    /**
     * Set the stop name in the stop name {@link EditText}.
     *
     * @param stopName The stop name to set.
     */
    private void setStopName(final String stopName) {
        editName.setText(stopName);
        isNamePopulated = true;
    }

    /**
     * Get the long stop name, for showing in the blurb.
     *
     * @param cursor The {@link Cursor} containing stop data.
     * @return The long version of the stop name.
     */
    @NonNull
    private String getLongStopName(@NonNull final Cursor cursor) {
        if (cursor.moveToFirst()) {
            final String name = getStopName(cursor);
            final String locality = getStopLocality(cursor);

            if (!TextUtils.isEmpty(locality)) {
                return getString(R.string.busstop_locality, name, locality, stopCode);
            } else {
                return getString(R.string.busstop, name, stopCode);
            }
        } else {
            return stopCode;
        }
    }

    /**
     * Get a short stop name, for pre-populating the {@link EditText} in add mode.
     *
     * @param cursor The {@link Cursor} containing stop data.
     * @return The short version of the stop name.
     */
    @NonNull
    private String getShortStopName(@NonNull final Cursor cursor) {
        if (cursor.moveToFirst()) {
            String result = getStopName(cursor);
            final String locality = getStopLocality(cursor);

            if (!TextUtils.isEmpty(locality)) {
                result += ", " + locality;
            }

            return result;
        } else {
            return stopCode;
        }
    }

    /**
     * Get merely the name of the stop from the database.
     *
     * @param cursor The {@link Cursor} containing the stop data.
     * @return The stop name.
     */
    @NonNull
    private String getStopName(@NonNull final Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(BusStopContract.BusStops.STOP_NAME));
    }

    /**
     * Get the stop locality.
     *
     * @param cursor The {@link Cursor} containing the stop data.
     * @return The stop locality.
     */
    @Nullable
    private String getStopLocality(@NonNull final Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(BusStopContract.BusStops.LOCALITY));
    }

    /**
     * Handle the user clicking on the positive button.
     */
    private void handlePositiveButtonClick() {
        if (favouriteStopCursor != null) {
            final String name = editName.getText().toString();

            if (favouriteStopCursor.moveToFirst()) {
                final int idColumn = favouriteStopCursor.getColumnIndex(
                        SettingsContract.Favourites._ID);
                final long id = favouriteStopCursor.getLong(idColumn);
                EditFavouriteStopTask.start(getContext(), id, name);
            } else {
                AddFavouriteStopTask.start(getContext(), stopCode, name);
            }
        }
    }

    /**
     * Show the progress layout.
     */
    private void showProgress() {
        layoutContent.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
    }

    /**
     * Show the content layout.
     */
    private void showContent() {
        progress.setVisibility(View.GONE);
        layoutContent.setVisibility(View.VISIBLE);
    }

    /**
     * Update the dialog title. The title changes depending if the dialog is in add or edit mode.
     */
    private void updateDialogTitle() {
        final AlertDialog dialog = getDialog();

        if (dialog != null) {
            final int titleRes = isEditMode
                    ? R.string.addeditfavouritestopdialog_title_edit
                    : R.string.addeditfavouritestopdialog_title_add;
            dialog.setTitle(titleRes);
        }
    }

    /**
     * Update the dialog positive button enabled state. The button is only enabled when the name
     * {@link EditText} is populated.
     */
    private void updatePositiveButtonEnabledState() {
        final AlertDialog dialog = getDialog();

        if (dialog != null) {
            final Button btnPositive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);

            if (btnPositive != null) {
                final String text = editName.getText().toString();
                btnPositive.setEnabled(!TextUtils.isEmpty(text));
            }
        }
    }

    /**
     * This listens for text updates to the {@link EditText} and updates the dialog positive button
     * enabled state.
     */
    private class NameWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(final CharSequence s, final int start, final int count,
                final int after) {
            // Nothing to do here.
        }

        @Override
        public void onTextChanged(final CharSequence s, final int start, final int before,
                final int count) {
            updatePositiveButtonEnabledState();
        }

        @Override
        public void afterTextChanged(final Editable s) {
            // Nothing to do here.
        }
    }
}
