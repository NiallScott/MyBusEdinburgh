/*
 * Copyright (C) 2017 - 2018 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.bustimes;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import uk.org.rivernile.android.bustracker.ui.bustimes.details.StopDetailsFragment;
import uk.org.rivernile.android.bustracker.ui.bustimes.times.BusTimesFragment;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link FragmentPagerAdapter} provides the pages and tabs for
 * {@link DisplayStopDataActivity}.
 *
 * @author Niall Scott
 */
class StopDataPagerAdapter extends FragmentPagerAdapter {

    private final Context context;
    private final String stopCode;

    /**
     * Create a new {@code StopDataPagerAdapter}.
     *
     * @param context A {@link Context} instance.
     * @param fragmentManager The {@link FragmentManager}.
     * @param stopCode The stop code for this bus stop.
     */
    StopDataPagerAdapter(@NonNull final Context context,
            @NonNull final FragmentManager fragmentManager, @NonNull final String stopCode) {
        super(fragmentManager);

        this.context = context;
        this.stopCode = stopCode;
    }

    @Override
    public Fragment getItem(final int position) {
        switch (position) {
            case 0:
                return BusTimesFragment.newInstance(stopCode);
            case 1:
                return StopDetailsFragment.newInstance(stopCode);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.displaystopdata_tab_times);
            case 1:
                return context.getString(R.string.displaystopdata_tab_details);
            default:
                return null;
        }
    }
}