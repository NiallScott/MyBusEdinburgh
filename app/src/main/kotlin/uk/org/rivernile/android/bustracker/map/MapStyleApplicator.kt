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

package uk.org.rivernile.android.bustracker.map

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MapStyleOptions
import uk.org.rivernile.android.bustracker.utils.NightModeDetector
import uk.org.rivernile.edinburghbustracker.android.R
import javax.inject.Inject

/**
 * This class is used to apply the correct styling to a supplied [GoogleMap].
 *
 * @param nightModeDetector Used to detect whether night mode is enabled or not.
 * @author Niall Scott
 */
class MapStyleApplicator @Inject constructor(
        private val nightModeDetector: NightModeDetector) {

    /**
     * Apply the correct map styling to the supplied [map].
     *
     * @param context The [Context] of the [android.app.Activity] which hosts the [GoogleMap]
     * instance.
     * @param map The [GoogleMap] to apply the styling to.
     */
    fun applyMapStyle(context: Context, map: GoogleMap) {
        val mapStyle = if (nightModeDetector.isNightMode(context)) {
            MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_night)
        } else {
            null
        }

        map.setMapStyle(mapStyle)
    }
}