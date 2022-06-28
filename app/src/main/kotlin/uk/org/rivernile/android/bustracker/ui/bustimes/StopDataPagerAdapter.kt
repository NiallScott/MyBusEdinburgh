/*
 * Copyright (C) 2020 - 2022 Niall 'Rivernile' Scott
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

import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import uk.org.rivernile.android.bustracker.ui.bustimes.details.StopDetailsFragment
import uk.org.rivernile.android.bustracker.ui.bustimes.times.BusTimesFragment
import uk.org.rivernile.edinburghbustracker.android.R

/**
 * This [FragmentStateAdapter] provides the pages and tab titles for [DisplayStopDataActivity].
 *
 * @param activity The hosting [FragmentActivity].
 * @param stopCode The stop code to show pages for. As the [android.content.Intent] of
 * [DisplayStopDataActivity] cannot be updated after it has been started, the stop code is locked
 * in. Therefore, there is no mechanism to update the stop code later.
 * @author Niall Scott
 */
class StopDataPagerAdapter(
        activity: FragmentActivity,
        private val stopCode: String?)
    : FragmentStateAdapter(activity) {

    companion object {

        private const val PAGE_COUNT = 2

        private const val PAGE_TIMES = 0
        private const val PAGE_DETAILS = 1
    }

    override fun getItemCount() = PAGE_COUNT

    override fun createFragment(position: Int) = when (position) {
        PAGE_TIMES -> BusTimesFragment.newInstance(stopCode)
        PAGE_DETAILS -> StopDetailsFragment.newInstance(stopCode)
        else -> throw IllegalArgumentException()
    }

    @StringRes
    fun getPageTitleRes(position: Int) = when (position) {
        PAGE_TIMES -> R.string.displaystopdata_tab_times
        PAGE_DETAILS -> R.string.displaystopdata_tab_details
        else -> throw IllegalArgumentException()
    }
}