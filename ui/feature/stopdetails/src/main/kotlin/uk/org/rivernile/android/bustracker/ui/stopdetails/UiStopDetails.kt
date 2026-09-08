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

package uk.org.rivernile.android.bustracker.ui.stopdetails

import androidx.compose.runtime.Immutable
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import uk.org.rivernile.android.bustracker.core.busstops.StopDetails
import uk.org.rivernile.android.bustracker.core.busstops.StopLocation
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.AtcoStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.NaptanStopIdentifier

/**
 * This represents the stop details item.
 *
 * @property naptanCode The Naptan code of the stop.
 * @property atcoCode The ATCO code of the stop.
 * @property latLon The latitude/longitude coordinate of the stop.
 * @property orientation The orientation of the stop.
 * @property stopDistance The distance state of the stop. If this is `null`, this means the
 * device does not have location features.
 * @property isMapShown Will the map be shown?
 */
@Immutable
internal data class UiStopDetails(
    val naptanCode: NaptanStopIdentifier,
    val atcoCode: AtcoStopIdentifier,
    val latLon: UiLatLon,
    val orientation: StopOrientation,
    val stopDistance: UiStopDistance?,
    val isMapShown: Boolean
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
 * This represents the distance item shown to the user.
 *
 * @author Niall Scott
 */
@Immutable
internal sealed interface UiStopDistance {

    /**
     * The distance is unable to be shown due to insufficient location permissions.
     */
    data object InsufficientLocationPermissions : UiStopDistance

    /**
     * The distance is unable to be shown due to location services being off.
     */
    data object LocationOff : UiStopDistance

    /**
     * The device location is currently being obtained.
     */
    data object ObtainingLocation : UiStopDistance

    /**
     * The current location is unknown.
     */
    data object LocationUnknown : UiStopDistance

    /**
     * The distance is known.
     */
    sealed interface Distance : UiStopDistance {

        /**
         * The distance is to be represented in meters.
         *
         * @property distance The number of meters between the device and stop.
         */
        @JvmInline
        value class Meters(
            val distance: Int
        ) : Distance

        /**
         * The distance is to be represented in kilometers.
         *
         * @param distance The number of kilometers between the device and stop.
         */
        @JvmInline
        value class Kilometers(
            val distance: Float
        ) : Distance
    }
}

/**
 * Maps this [StopDetails] to a [UiStopDetails].
 *
 * @param stopDistance The stop distance details. This will be `null` when the device does not
 * support location services.
 * @param isMapShown Is the map to be shown?
 * @return This [StopDetails] as a [UiStopDetails].
 */
internal fun StopDetails.toUiStopDetails(
    stopDistance: UiStopDistance?,
    isMapShown: Boolean
): UiStopDetails {
    return UiStopDetails(
        naptanCode = naptanStopIdentifier,
        atcoCode = atcoStopIdentifier,
        latLon = location.toUiLatLon(),
        orientation = orientation,
        stopDistance = stopDistance,
        isMapShown = isMapShown
    )
}

/**
 * Maps this [UiLatLon] to a Google Maps [LatLng] object.
 *
 * @return This [UiLatLon] as a [LatLng].
 */
internal fun UiLatLon.toGoogleMapsLatLng() = LatLng(latitude, longitude)

internal const val GOOGLE_MAP_ZOOM_LEVEL = 17f

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

private fun StopLocation.toUiLatLon(): UiLatLon {
    return UiLatLon(
        latitude = latitude,
        longitude = longitude
    )
}
