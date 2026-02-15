/*
 * Copyright (C) 2020 - 2026 Niall 'Rivernile' Scott
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commitNow
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.ui.bustimes.details.StopDetailsFragment
import uk.org.rivernile.android.bustracker.ui.bustimes.times.BusTimesFragment
import uk.org.rivernile.edinburghbustracker.android.R

/**
 * This [FragmentStateAdapter] provides the pages and tab titles for [DisplayStopDataActivity].
 *
 * @param activity The hosting [FragmentActivity].
 * @param stopIdentifier The stop to show pages for. As the [android.content.Intent] of
 * [DisplayStopDataActivity] cannot be updated after it has been started, the stop identifier is
 * locked in. Therefore, there is no mechanism to update the stop code later.
 * @author Niall Scott
 */
class StopDataPagerAdapter(
    activity: FragmentActivity,
    private val stopIdentifier: StopIdentifier?
) : FragmentStateAdapter(activity) {

    companion object {

        private const val PAGE_COUNT = 2

        private const val PAGE_TIMES = 0
        private const val PAGE_DETAILS = 1
    }

    init {
        registerFragmentTransactionCallback(fragmentTransactionCallback)
    }

    override fun getItemCount() = PAGE_COUNT

    override fun createFragment(position: Int) = when (position) {
        PAGE_TIMES -> BusTimesFragment.newInstance(stopIdentifier)
        PAGE_DETAILS -> StopDetailsFragment.newInstance(stopIdentifier)
        else -> throw IllegalArgumentException()
    }

    @StringRes
    fun getPageTitleRes(position: Int) = when (position) {
        PAGE_TIMES -> R.string.displaystopdata_tab_times
        PAGE_DETAILS -> R.string.displaystopdata_tab_details
        else -> throw IllegalArgumentException()
    }

    /**
     * When swiping to [StopDetailsFragment] and swiping back to [BusTimesFragment], the location
     * access symbol will perpetually stay in the status bar. This is because the details Fragment,
     * despite no longer being visible, is still requesting locations. Why?
     *
     * Because [FragmentStateAdapter] merely puts the details Fragment back to a
     * [Lifecycle.State.STARTED] state. In this state, LiveData instances in [StopDetailsFragment]
     * are still active, and up the chain this still causes locations to be collected. This is
     * consistent with normal Android practice, because a STARTED element normally means the item
     * is still visible.
     *
     * Except it's not in this case, because it's been scrolled off-screen. We don't want to be
     * collecting locations when the user can't see [StopDetailsFragment] as this will hammer the
     * battery.
     *
     * This is controlled under the hood by [FragmentStateAdapter] by controlling the maximum
     * lifecycle of a Fragment via the FragmentManager. When a Fragment is the primary item, its max
     * lifecycle is [Lifecycle.State.RESUMED], and when not the primary item but cached, it is
     * [Lifecycle.State.STARTED]. It's because of this that the details Fragment is still started
     * and its LiveDatas are still active.
     *
     * So instead, we register this callback and implement
     * [FragmentStateAdapter.FragmentTransactionCallback] so we know when the maximum lifecycle for
     * a Fragment is changing. In the case the Fragment is [StopDetailsFragment], AND its maximum
     * lifecycle is [Lifecycle.State.STARTED], we then perform another transaction to forcefully
     * place its max lifecycle down to [Lifecycle.State.CREATED]. This means the Fragment still
     * exists, but all of its LiveDatas will be inactive.
     */
    private val fragmentTransactionCallback get() = object : FragmentTransactionCallback() {
        override fun onFragmentMaxLifecyclePreUpdated(
            fragment: Fragment,
            maxLifecycleState: Lifecycle.State): OnPostEventListener {
            return if (fragment is StopDetailsFragment &&
                maxLifecycleState == Lifecycle.State.STARTED) {
                OnPostEventListener {
                    fragment.parentFragmentManager.commitNow {
                        setMaxLifecycle(fragment, Lifecycle.State.CREATED)
                    }
                }
            } else {
                super.onFragmentMaxLifecyclePreUpdated(fragment, maxLifecycleState)
            }
        }
    }
}
