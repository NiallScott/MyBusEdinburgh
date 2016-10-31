/*
 * Copyright (C) 2016 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.bustimes.details;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * The purpose of this {@link Fragment} is to show users details for a given bus stop code.
 *
 * @author Niall Scott
 */
public class StopDetailsFragment extends Fragment {

    private static final String ARG_STOP_CODE = "stopCode";

    /**
     * Create a new instance of this {@link Fragment}.
     *
     * @param stopCode The bus stop code to show details for.
     * @return A new instance of this {@link Fragment}.
     */
    @NonNull
    public static StopDetailsFragment newInstance(@NonNull final String stopCode) {
        final StopDetailsFragment fragment = new StopDetailsFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_STOP_CODE, stopCode);
        fragment.setArguments(args);

        return fragment;
    }
}
