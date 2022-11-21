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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.MapInfoWindowBinding

/**
 * This [MapInfoWindow] supplies a custom [View] for an `InfoWindow` when the bus stop marker
 * `InfoWindow` is being shown. This allows the text for services not to ellipsise (i.e. be
 * multi-line).
 *
 * @param context The [android.app.Activity] [Context].
 * @param inflater The [LayoutInflater] instance to inflate the layout with.
 * @param rootView The root [ViewGroup] to inflate against.
 * @author Niall Scott
 */
class MapInfoWindow(
        private val context: Context,
        inflater: LayoutInflater,
        private val rootView: ViewGroup) : GoogleMap.InfoWindowAdapter {

    private val viewBinding by lazy {
        MapInfoWindowBinding.inflate(inflater, rootView, false)
    }

    // Since we don't want to modify the window decoration, return null here so that Google Maps
    // uses its own implementation.
    override fun getInfoWindow(marker: Marker): View? = null

    override fun getInfoContents(marker: Marker): View {
        return viewBinding.apply {
            txtTitle.text = marker.title

            txtSnippet.text = (marker.tag as? UiStopMarker)
                    ?.serviceListing
                    ?.let {
                when (it) {
                    is UiServiceListing.InProgress ->
                        context.getString(R.string.busstopmapfragment_info_window_services_loading)
                    is UiServiceListing.Empty ->
                        context.getString(R.string.busstopmapfragment_info_window_services_empty)
                    is UiServiceListing.Success -> it.services.joinToString()
                }
            }
        }.root
    }
}