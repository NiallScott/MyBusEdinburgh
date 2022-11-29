/*
 * Copyright (C) 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.explore

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import com.google.android.material.tabs.TabLayout
import uk.org.rivernile.android.bustracker.ui.busstopmap.BusStopMapFragment
import uk.org.rivernile.android.bustracker.ui.neareststops.NearestStopsFragment
import uk.org.rivernile.android.bustracker.ui.scroll.HasScrollableContent
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.FragmentExploreBinding

/**
 * This [Fragment] shows various UI views which allow them to discover stops, such as a map and a
 * nearest stop listing.
 *
 * @author Niall Scott
 */
class ExploreFragment : Fragment(), HasScrollableContent {

    companion object {

        private const val FRAGMENT_TAG_MAP = "tagMap"
        private const val FRAGMENT_TAG_NEAREST_STOPS = "tagNearestStops"
    }

    private lateinit var callbacks: Callbacks

    private val viewBinding get() = _viewBinding!!
    private var _viewBinding: FragmentExploreBinding? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        callbacks = try {
            context as Callbacks
        } catch (ignored: ClassCastException) {
            throw IllegalStateException("${context.javaClass.name} does not implement " +
                    Callbacks::class.java.name)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        _viewBinding = FragmentExploreBinding.inflate(inflater, container, false)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.tabLayout.addOnTabSelectedListener(tabSelectedListener)

        when (childFragmentManager.findFragmentById(R.id.fragmentContainer)) {
            is NearestStopsFragment -> showItem(1)
            else -> showItem(0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _viewBinding = null
    }

    override val scrollableContentIdRes: Int get() {
        val fragment = childFragmentManager.findFragmentById(R.id.fragmentContainer)

        return (fragment as? HasScrollableContent)?.scrollableContentIdRes ?: View.NO_ID
    }

    /**
     * Set whether the tab bar is visible or not.
     *
     * @param isVisible The tab bar visibility.
     */
    fun setTabBarVisible(isVisible: Boolean) {
        viewBinding.tabLayout.isVisible = isVisible
    }

    /**
     * Show the item at the given position.
     *
     * @param position The position of the item to show.
     */
    private fun showItem(position: Int) {
        childFragmentManager.commit {
            childFragmentManager.findFragmentById(R.id.fragmentContainer)?.let(this::detach)

            when (position) {
                0 -> {
                    childFragmentManager.findFragmentByTag(FRAGMENT_TAG_MAP)
                            ?.let(this::attach)
                            ?: add(
                                    R.id.fragmentContainer,
                                    BusStopMapFragment.newInstance(),
                                    FRAGMENT_TAG_MAP)
                }
                1 -> {
                    childFragmentManager.findFragmentByTag(FRAGMENT_TAG_NEAREST_STOPS)
                            ?.let(this::attach)
                            ?: add(
                                    R.id.fragmentContainer,
                                    NearestStopsFragment(),
                                    FRAGMENT_TAG_NEAREST_STOPS)
                }
                else -> return@showItem
            }

            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        }

        viewBinding.tabLayout.apply {
            selectTab(getTabAt(position))
        }

        callbacks.onExploreTabSwitched()
    }

    private val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            showItem(tab.position)
        }

        override fun onTabReselected(tab: TabLayout.Tab) {
            // Nothing to do here.
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {
            // Nothing to do here.
        }
    }

    interface Callbacks {

        fun onExploreTabSwitched()
    }
}