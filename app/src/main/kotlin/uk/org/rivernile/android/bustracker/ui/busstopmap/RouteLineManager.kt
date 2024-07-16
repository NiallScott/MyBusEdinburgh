/*
 * Copyright (C) 2022 - 2024 Niall 'Rivernile' Scott
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
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.material.color.MaterialColors
import com.google.maps.android.ktx.model.polylineOptions

/**
 * This class manages the route lines shown on a [GoogleMap].
 *
 * @param context The [android.app.Activity] [Context].
 * @param map The [GoogleMap] to manage the route lines upon.
 * @author Niall Scott
 */
class RouteLineManager(
        context: Context,
        private val map: GoogleMap) {

    private val defaultColour = MaterialColors.getColor(
        context,
        com.google.android.material.R.attr.colorTertiary,
        null
    )

    private var polylines: List<Polyline>? = null

    /**
     * Submit the route lines to be shown on the map. This removes any existing lines and replaces
     * it with the contents of the given [List].
     *
     * @param routeLines The route lines to be shown on the map. `null` or empty means no lines will
     * be shown.
     */
    fun submitRouteLines(routeLines: List<UiServiceRoute>?) {
        polylines?.forEach(Polyline::remove)
        polylines = null

        routeLines?.ifEmpty { null }?.apply {
            val newPolylines = mutableListOf<Polyline>()

            forEach { route ->
                route.lines.forEach { line ->
                    polylineOptions {
                        color(route.serviceColour ?: defaultColour)

                        line.points
                                .map(this@RouteLineManager::mapToLatLng)
                                .let(this::addAll)
                    }.let {
                        newPolylines += map.addPolyline(it)
                    }
                }
            }

            polylines = newPolylines
        }
    }

    /**
     * Given a [UiLatLon], map it to a [LatLng].
     *
     * @param latLon The [UiLatLon] to map.
     * @return The mapped [LatLng].
     */
    private fun mapToLatLng(latLon: UiLatLon) = LatLng(latLon.latitude, latLon.longitude)
}