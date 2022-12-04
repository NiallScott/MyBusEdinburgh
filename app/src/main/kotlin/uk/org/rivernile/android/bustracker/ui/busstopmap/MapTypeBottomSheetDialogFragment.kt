/*
 * Copyright (C) 2018 - 2022 Niall 'Rivernile' Scott
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import uk.org.rivernile.android.bustracker.core.bundle.getSerializableCompat
import uk.org.rivernile.edinburghbustracker.android.databinding.MaptypeBottomsheetBinding

/**
 * This shows a bottom sheet which allows the user to select a map type.
 *
 * @author Niall Scott
 */
class MapTypeBottomSheetDialogFragment : BottomSheetDialogFragment() {

    companion object {

        const val REQUEST_KEY = "requestChooseMapType"

        const val RESULT_CHOSEN_MAP_TYPE = "chosenMapType"

        private const val ARG_MAP_TYPE = "mapType"

        /**
         * Create a new instance of a `MapTypeBottomSheetDialogFragment`.
         *
         * @param mapType The type of the map.
         * @return A new instance of `MapTypeBottomSheetDialogFragment`.
         */
        fun newInstance(mapType: MapType) = MapTypeBottomSheetDialogFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ARG_MAP_TYPE, mapType)
            }
        }
    }

    private val viewBinding get() = _viewBinding!!
    private var _viewBinding: MaptypeBottomsheetBinding? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        _viewBinding = MaptypeBottomsheetBinding.inflate(layoutInflater, container, false)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.apply {
            txtMapTypeNormal.setOnClickListener {
                handleItemClicked(MapType.NORMAL)
            }

            txtMapTypeSatellite.setOnClickListener {
                handleItemClicked(MapType.SATELLITE)
            }

            txtMapTypeHybrid.setOnClickListener {
                handleItemClicked(MapType.HYBRID)
            }
        }

        setupStates()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _viewBinding = null
    }
    /**
     * Setup the row states depending on what the selected item is.
     */
    private fun setupStates() {
        val mapType = arguments?.getSerializableCompat(ARG_MAP_TYPE) ?: MapType.NORMAL

        viewBinding.apply {
            txtMapTypeNormal.isChecked = mapType == MapType.NORMAL
            txtMapTypeSatellite.isChecked = mapType == MapType.SATELLITE
            txtMapTypeHybrid.isChecked = mapType == MapType.HYBRID
        }
    }

    /**
     * Handle an item being clicked.
     *
     * @param mapType The type of the item that was clicked.
     */
    private fun handleItemClicked(mapType: MapType) {
        val bundle = Bundle()
        bundle.putSerializable(RESULT_CHOSEN_MAP_TYPE, mapType)
        parentFragmentManager.setFragmentResult(REQUEST_KEY, bundle)
        dismiss()
    }
}