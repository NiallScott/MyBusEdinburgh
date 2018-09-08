/*
 * Copyright (C) 2018 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.busstopmap

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import uk.org.rivernile.android.bustracker.ui.widgets.CheckableLinearLayout
import uk.org.rivernile.edinburghbustracker.android.R

/**
 * This shows a bottom sheet which allows the user to select a map type.
 *
 * @author Niall Scott
 */
class MapTypeBottomSheetDialogFragment : BottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var layoutNormal: CheckableLinearLayout
    private lateinit var layoutSatellite: CheckableLinearLayout
    private lateinit var layoutHybrid: CheckableLinearLayout

    companion object {

        /** The map type is a normal map. */
        const val MAP_TYPE_NORMAL = 1
        /** The map type is satellite, i.e. space imagery. */
        const val MAP_TYPE_SATELLITE = 2
        /** The map type is hybrid. That is, street names are imposed on top of satellite imagery. */
        const val MAP_TYPE_HYBRID = 3

        private const val ARG_MAP_TYPE = "mapType"

        /**
         * Create a new instance of a `MapTypeBottomSheetDialogFragment`.
         *
         * @param mapType The type of the map.
         * @return A new instance of `MapTypeBottomSheetDialogFragment`.
         */
        @JvmStatic
        fun newInstance(@MapType mapType: Int) = MapTypeBottomSheetDialogFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_MAP_TYPE, mapType)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.maptype_bottomsheet, container, false).also {
            layoutNormal = it.findViewById(R.id.layoutNormal)
            layoutSatellite = it.findViewById(R.id.layoutSatellite)
            layoutHybrid = it.findViewById(R.id.layoutHybrid)

            layoutNormal.setOnClickListener(this)
            layoutSatellite.setOnClickListener(this)
            layoutHybrid.setOnClickListener(this)

            setupStates()
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            layoutNormal -> handleItemClicked(MAP_TYPE_NORMAL)
            layoutSatellite -> handleItemClicked(MAP_TYPE_SATELLITE)
            layoutHybrid -> handleItemClicked(MAP_TYPE_HYBRID)
        }
    }

    /**
     * Setup the row states depending on what the selected item is.
     */
    private fun setupStates() {
        val mapType = arguments?.getInt(ARG_MAP_TYPE) ?: -1

        layoutNormal.isChecked = mapType == MAP_TYPE_NORMAL
        layoutSatellite.isChecked = mapType == MAP_TYPE_SATELLITE
        layoutHybrid.isChecked = mapType == MAP_TYPE_HYBRID
    }

    /**
     * Handle an item being clicked.
     *
     * @param mapType The type of the item that was clicked.
     */
    private fun handleItemClicked(@MapType mapType: Int) {
        (targetFragment as? OnMapTypeSelectedListener)?.onMapTypeSelected(mapType)
        dismiss()
    }

    /**
     * Classes which wish to receive callbacks when the user makes a map type choice should
     * implement this interface.
     */
    interface OnMapTypeSelectedListener {

        /**
         * This is called when the user chooses a map type.
         *
         * @param mapType The map type the user has chosen.
         */
        fun onMapTypeSelected(@MapType mapType: Int)
    }
}