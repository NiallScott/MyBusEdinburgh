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

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import uk.org.rivernile.android.bustracker.core.busstops.StopName
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.text.UiStopName
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName

/**
 * A nearest stop which is displayed on the UI.
 *
 * @property stopIdentifier The stop identifier of the nearest stop.
 * @property stopName The stop name details.
 * @property services An immutable list of services which serve this stop.
 * @property orientation The orientation of the stop.
 * @property distanceMeters The distance between this device and the stop in meters.
 * @property dropdownMenu The dropdown menu for this nearest stop.
 * @author Niall Scott
 */
@Immutable
internal data class UiNearestStop(
    val stopIdentifier: StopIdentifier,
    val stopName: UiStopName,
    val services: ImmutableList<UiServiceName>?,
    val orientation: StopOrientation,
    val distanceMeters: Int,
    val dropdownMenu: UiNearestStopDropdownMenu
)

/**
 * Map this [List] of [NearestStop]s to a [List] of [UiNearestStop].
 *
 * @param serviceColours A [Map] of services to [ServiceColours].
 * @param dropdownMenus A mapping of stop identifiers to [UiNearestStopDropdownMenu]s, used to
 * populate the dropdown menu for each [UiNearestStop].
 * @param serviceNameComparator A [Comparator] used to sort service names.
 * @return This [List] of [NearestStop]s mapped to a [List] of [UiNearestStop]s.
 */
internal fun List<NearestStop>.toUiNearestStops(
    serviceColours: Map<ServiceDescriptor, ServiceColours>?,
    dropdownMenus: Map<StopIdentifier, UiNearestStopDropdownMenu>?,
    serviceNameComparator: Comparator<String>
) = map { nearestStop ->
    nearestStop
        .toUiNearestStop(
            serviceColours = serviceColours,
            dropdownMenu = dropdownMenus?.get(nearestStop.stopIdentifier)
                ?: UiNearestStopDropdownMenu(),
            serviceNameComparator = serviceNameComparator
        )
}

private fun NearestStop.toUiNearestStop(
    serviceColours: Map<ServiceDescriptor, ServiceColours>?,
    dropdownMenu: UiNearestStopDropdownMenu,
    serviceNameComparator: Comparator<String>
): UiNearestStop {
    return UiNearestStop(
        stopIdentifier = stopIdentifier,
        stopName = stopName.toUiStopName(),
        services = serviceListing
            ?.ifEmpty { null }
            ?.map {
                toUiServiceName(it, serviceColours?.get(it))
            }
            ?.sortedWith(
                compareBy(serviceNameComparator) {
                    it.serviceName
                }
            )
            ?.toImmutableList(),
        orientation = orientation,
        distanceMeters = distanceMeters,
        dropdownMenu = dropdownMenu
    )
}

private fun StopName.toUiStopName(): UiStopName {
    return UiStopName(
        name = name,
        locality = locality
    )
}

private fun toUiServiceName(
    serviceDescriptor: ServiceDescriptor,
    serviceColours: ServiceColours?
): UiServiceName {
    return UiServiceName(
        serviceName = serviceDescriptor.serviceName,
        colours = serviceColours?.toUiServiceColours()
    )
}

private fun ServiceColours.toUiServiceColours(): UiServiceColours {
    return UiServiceColours(
        backgroundColour = colourPrimary,
        textColour = colourOnPrimary
    )
}
