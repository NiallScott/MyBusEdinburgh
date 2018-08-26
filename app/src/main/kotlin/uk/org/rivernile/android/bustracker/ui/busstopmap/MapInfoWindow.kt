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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import uk.org.rivernile.edinburghbustracker.android.R

/**
 * The `MapInfoWindow` supplies a custom [View] for an `InfoWindow` when the bus stop marker
 * `InfoWindow` is being shown. This allows the text for services not to ellipsise (i.e. be
 * multi-line).
 *
 * @author Niall Scott
 * @param context A [Context] instance.
 * @param rootView The [View] to inflate against.
 */
class MapInfoWindow(context: Context, private val rootView: ViewGroup)
    : GoogleMap.InfoWindowAdapter {

    private val inflater = LayoutInflater.from(context)
    private var cachedView: View? = null
    private lateinit var txtTitle: TextView
    private lateinit var txtSnippet: TextView

    override fun getInfoWindow(marker: Marker): View? {
        // Since we don't want to modify the window decoration, return null here so that Google Maps
        // uses its own implementation.
        return null
    }

    override fun getInfoContents(marker: Marker): View {
        if (cachedView == null) {
            cachedView = inflater.inflate(R.layout.map_info_window, rootView, false)
                    .also {
                        txtTitle = it.findViewById(R.id.txtTitle) as TextView
                        txtSnippet = it.findViewById(R.id.txtSnippet) as TextView
                    }
        }

        txtTitle.text = marker.title
        txtSnippet.text = marker.snippet

        return cachedView!!
    }
}