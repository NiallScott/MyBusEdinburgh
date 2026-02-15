/*
 * Copyright (C) 2025 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.favouritestops

import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.favourites.FavouriteStop

/**
 * This describes a favourite stop with the attributed service listing for the stop.
 *
 * @property stopIdentifier The stop identifier of the favourite stop.
 * @property savedName The user's saved name for this favourite stop.
 * @property services A [List] of services which service this favourite stop. `null` if there are
 * no known services.
 * @author Niall Scott
 */
internal data class FavouriteStopWithServices(
    val stopIdentifier: StopIdentifier,
    val savedName: String,
    val services: List<ServiceDescriptor>?
)

/**
 * Map this [List] of [FavouriteStop]s in to a [List] of [FavouriteStopWithServices].
 *
 * @param stopServices A [Map] of stop identifiers to a [List] of services.
 * @return This [List] as a [List] of [FavouriteStopWithServices].
 */
internal fun List<FavouriteStop>.toFavouriteStopsWithServices(
    stopServices: Map<StopIdentifier, List<ServiceDescriptor>>?
) = map { favouriteStop ->
    favouriteStop
        .toFavouriteStopWithServices(
            services = stopServices
                ?.get(favouriteStop.stopIdentifier)
                ?.ifEmpty { null }
        )
}

private fun FavouriteStop.toFavouriteStopWithServices(
    services: List<ServiceDescriptor>?
): FavouriteStopWithServices {
    return FavouriteStopWithServices(
        stopIdentifier = stopIdentifier,
        savedName = stopName,
        services = services
    )
}
