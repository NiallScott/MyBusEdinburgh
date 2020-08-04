/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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
 *
 */

package uk.org.rivernile.android.bustracker.ui.bustimes

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import uk.org.rivernile.android.bustracker.ui.bustimes.details.StopDetailsFragment
import uk.org.rivernile.android.bustracker.ui.bustimes.times.BusTimesFragment
import uk.org.rivernile.edinburghbustracker.android.R

/**
 * This [FragmentPagerAdapter] provides the pages and tab titles for [DisplayStopDataActivity].
 *
 * @param context The [Context] of [DisplayStopDataActivity].
 * @param fragmentManager The [FragmentManager] owned by [DisplayStopDataActivity].
 * @param stopCode The stop code to show pages for. As the [android.content.Intent] of
 * [DisplayStopDataActivity] cannot be updated after it has been started, the stop code is locked
 * in. Therefore, there is no mechanism to update the stop code later.
 * @author Niall Scott
 */
class StopDataPagerAdapter(
        private val context: Context,
        fragmentManager: FragmentManager,
        private val stopCode: String?)
    : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment = when(position) {
        0 -> BusTimesFragment.newInstance(stopCode)
        // We're not allowed to return null when the position is unknown as per the specified
        // interface of the parent class, so we stick StopDetailsFragment in the last position and
        // hope the value of getCount() method is respected properly.
        else -> StopDetailsFragment.newInstance(stopCode)
    }

    override fun getCount() = 2

    override fun getPageTitle(position: Int): CharSequence? = when (position) {
        0 -> context.getString(R.string.displaystopdata_tab_times)
        1 -> context.getString(R.string.displaystopdata_tab_details)
        else -> null
    }
}