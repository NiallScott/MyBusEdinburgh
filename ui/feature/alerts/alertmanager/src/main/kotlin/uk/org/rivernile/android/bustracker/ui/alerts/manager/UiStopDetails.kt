/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.alerts.manager

import androidx.compose.runtime.Immutable
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.text.UiStopName

/**
 * The stop details, to be rendered on the UI.
 *
 * @property stopName The stop name details.
 * @property latLon The latitude/longitude pair of the stop.
 * @property orientation The orientation of the stop.
 * @author Niall Scott
 */
@Immutable
internal data class UiStopDetails(
    val stopName: UiStopName,
    val latLon: UiLatLon,
    val orientation: StopOrientation
)

/**
 * A latitude/longitude pair.
 *
 * @property latitude The latitude.
 * @property longitude The longitude.
 * @author Niall Scott
 */
@Immutable
internal data class UiLatLon(
    val latitude: Double,
    val longitude: Double
)

/**
 * Maps this [UiLatLon] to a Google Maps [LatLng] object.
 *
 * @return This [UiLatLon] as a [LatLng].
 */
internal fun UiLatLon.toGoogleMapsLatLng() = LatLng(latitude, longitude)

internal const val GOOGLE_MAP_ZOOM_LEVEL = 16f

/**
 * Maps this [UiLatLon] to a [CameraPosition].
 *
 * @return This [UiLatLon] as a [CameraPosition].
 */
internal fun UiLatLon.toCameraPosition() = CameraPosition
    .fromLatLngZoom(
        toGoogleMapsLatLng(),
        GOOGLE_MAP_ZOOM_LEVEL
    )
