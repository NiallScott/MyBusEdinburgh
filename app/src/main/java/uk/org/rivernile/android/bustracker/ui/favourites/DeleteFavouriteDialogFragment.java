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

package uk.org.rivernile.android.bustracker.ui.favourites;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import uk.org.rivernile.android.bustracker.database.settings.loaders.DeleteFavouriteStopTask;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link DialogFragment} will show an {@link AlertDialog} which asks the user to confirm if
 * they wish to delete the favourite bus stop or not.
 * 
 * @author Niall Scott
 */
public class DeleteFavouriteDialogFragment extends DialogFragment {
    
    /** The argument that is sent to this Fragment to denote the stop code. */
    private static final String ARG_STOPCODE = "stopCode";

    private String stopCode;
    
    /**
     * Create a new instance of the {@code DeleteFavouriteDialogFragment}, giving the
     * {@code stopCode} as the argument.
     * 
     * @param stopCode The stopCode to potentially delete.
     * @return A new instance of this {@link DialogFragment}.
     */
    @NonNull
    public static DeleteFavouriteDialogFragment newInstance(@NonNull final String stopCode) {
        final DeleteFavouriteDialogFragment f = new DeleteFavouriteDialogFragment();
        final Bundle b = new Bundle();
        b.putString(ARG_STOPCODE, stopCode);
        f.setArguments(b);
        
        return f;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final Bundle args = getArguments();

        if (args == null) {
            throw new IllegalStateException("There were no arguments supplied to " +
                    "DeleteFavouriteDialogFragment.");
        }
        
        stopCode = args.getString(ARG_STOPCODE);
        setCancelable(true);

        if (TextUtils.isEmpty(stopCode)) {
            throw new IllegalArgumentException("The stopCode argument cannot be null or empty.");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.deletefavouritedialog_title)
                .setPositiveButton(R.string.okay,
                        (dialog, id) -> DeleteFavouriteStopTask.start(getContext(), stopCode))
                .setNegativeButton(R.string.cancel, null)
                .create();
    }
}