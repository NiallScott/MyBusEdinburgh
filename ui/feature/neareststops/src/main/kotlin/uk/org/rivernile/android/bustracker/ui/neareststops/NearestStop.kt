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

package uk.org.rivernile.android.bustracker.ui.neareststops

import uk.org.rivernile.android.bustracker.core.busstops.StopDetailsWithServices
import uk.org.rivernile.android.bustracker.core.busstops.StopLocation
import uk.org.rivernile.android.bustracker.core.busstops.StopName
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier

/**
 * This describes a nearest stop.
 *
 * @property stopIdentifier The identifier for the stop.
 * @property stopName The name details of the stop.
 * @property distanceMeters The distance in meters between the device and the stop.
 * @property orientation The orientation of the stop.
 * @property serviceListing The [List]ing of the services for this stop.
 * @author Niall Scott
 */
internal data class NearestStop(
    val stopIdentifier: StopIdentifier,
    val stopName: StopName,
    val distanceMeters: Int,
    val orientation: StopOrientation,
    val serviceListing: List<ServiceDescriptor>?
)

/**
 * Map a [List] of [StopDetailsWithServices] to a [List] of [NearestStop]s.
 *
 * @param distanceCalculator A lambda which provides an implementation for calculating the distance
 * between the device location and the stop's location.
 */
internal fun List<StopDetailsWithServices>.toNearestStops(
    distanceCalculator: (StopLocation) -> Int
): List<NearestStop> {
    return map { stopDetails ->
        stopDetails
            .toNearestStop(distanceMeters = distanceCalculator(stopDetails.location))
    }
}

private fun StopDetailsWithServices.toNearestStop(distanceMeters: Int): NearestStop {
    return NearestStop(
        stopIdentifier = stopIdentifier,
        stopName = stopName,
        distanceMeters = distanceMeters,
        orientation = orientation,
        serviceListing = serviceListing
    )
}
