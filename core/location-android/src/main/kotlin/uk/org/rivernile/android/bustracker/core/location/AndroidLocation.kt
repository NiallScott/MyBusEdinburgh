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

package uk.org.rivernile.android.bustracker.core.location

import android.location.Location as AndroidLocation

/**
 * Maps an [AndroidLocation] to a [Location].
 *
 * @return The [AndroidLocation] as a [Location].
 * @author Niall Scott
 */
internal fun AndroidLocation.toLocation(): Location {
    return Location(
        latLon = toLatLon(),
        horizontalAccuracy = if (hasAccuracy()) accuracy else null
    )
}

/**
 * Maps an [AndroidLocation] to a [LatLon].
 *
 * @return The [AndroidLocation] as a [LatLon].
 * @author Niall Scott
 */
internal fun AndroidLocation.toLatLon(): LatLon {
    return LatLon(
        latitude = latitude,
        longitude = longitude
    )
}
