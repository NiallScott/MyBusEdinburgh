/*
 * Copyright (C) 2021 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.map

import androidx.annotation.DrawableRes
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopOrientation
import uk.org.rivernile.edinburghbustracker.android.R
import javax.inject.Inject

/**
 * This class contains methods used to decorate map marker icons for stops, to indicate the compass
 * direction the stop faces. This isn't just necessarily used exclusively for maps - it can be be
 * used wherever an icon is used to indicate the stop direction.
 *
 * @author Niall Scott
 */
class StopMapMarkerDecorator @Inject constructor() {

    /**
     * Apply the correct direction icon to the [MarkerOptions] object based on the supplied
     * orientation.
     *
     * @param markerOptions The [MarkerOptions] to apply the direction icon to.
     * @param orientation The orientation of the stop.
     */
    fun applyStopDirectionToMarker(markerOptions: MarkerOptions, orientation: StopOrientation) {
        markerOptions.icon(BitmapDescriptorFactory.fromResource(
                getStopDirectionDrawableResourceId(orientation)))
    }

    /**
     * Apply the correct direction icon to the [Marker] object based on the supplied orientation.
     *
     * @param marker The [Marker] to apply the direction icon to.
     * @param orientation The orientation of the stop.
     */
    fun applyStopDirectionToMarker(marker: Marker, orientation: StopOrientation) {
        marker.setIcon(BitmapDescriptorFactory.fromResource(
                getStopDirectionDrawableResourceId(orientation)))
    }

    /**
     * Get a drawable resource ID for a given orientation.
     *
     * @param orientation The orientation.
     * @return A drawable resource ID for a given orientation.
     */
    @DrawableRes
    fun getStopDirectionDrawableResourceId(orientation: StopOrientation) = when (orientation) {
        StopOrientation.NORTH -> R.drawable.mapmarker_n
        StopOrientation.NORTH_EAST -> R.drawable.mapmarker_ne
        StopOrientation.EAST -> R.drawable.mapmarker_e
        StopOrientation.SOUTH_EAST -> R.drawable.mapmarker_se
        StopOrientation.SOUTH -> R.drawable.mapmarker_s
        StopOrientation.SOUTH_WEST -> R.drawable.mapmarker_sw
        StopOrientation.WEST -> R.drawable.mapmarker_w
        StopOrientation.NORTH_WEST -> R.drawable.mapmarker_nw
        else -> R.drawable.mapmarker
    }
}