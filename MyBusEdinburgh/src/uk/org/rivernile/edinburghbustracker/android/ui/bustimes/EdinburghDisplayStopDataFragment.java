/*
 * Copyright (C) 2014 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android.ui.bustimes;

import android.os.Bundle;
import uk.org.rivernile.android.bustracker.ui.bustimes.
        BusTimesExpandableListAdapter;
import uk.org.rivernile.android.bustracker.ui.bustimes
        .DisplayStopDataFragment;

/**
 * This is the Edinburgh-specific implementation of
 * {@link DisplayStopDataFragment}.
 * 
 * @author Niall Scott
 */
public class EdinburghDisplayStopDataFragment extends DisplayStopDataFragment {
    
    /**
     * Create a new instance of this Fragment.
     * 
     * @param stopCode The stopCode for the bus stop to show data for.
     * @return A new instance of this Fragment.
     */
    public static EdinburghDisplayStopDataFragment newInstance(
            final String stopCode) {
        final EdinburghDisplayStopDataFragment f =
                new EdinburghDisplayStopDataFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_STOPCODE, stopCode);
        f.setArguments(args);
        
        return f;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BusTimesExpandableListAdapter createAdapter() {
        return new EdinburghBusTimesExpandableListAdapter(getActivity());
    }
}