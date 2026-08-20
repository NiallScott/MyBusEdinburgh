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

package uk.org.rivernile.android.bustracker.ui.search

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import uk.org.rivernile.android.bustracker.core.busstops.StopName
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.busstops.StopSearchResult
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.sortByServiceName
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.text.UiStopName
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName

/**
 * A stop search result item displayed on the UI.
 *
 * @property stopIdentifier The identifier of the stop search result item.
 * @property stopName The stop name details.
 * @property orientation The orientation of the stop.
 * @property services An immutable list of services which serve this stop.
 * @property dropdownMenu The dropdown meny for this stop search result.
 * @author Niall Scott
 */
internal data class UiStopSearchResult(
    val stopIdentifier: StopIdentifier,
    val stopName: UiStopName,
    val orientation: StopOrientation,
    val services: ImmutableList<UiServiceName>?,
    val dropdownMenu: UiStopSearchResultDropdownMenu
)

/**
 * Map this collection of [StopSearchResult] items in to a [List] of [UiStopSearchResult] items,
 * suitable for displaying on the UI.
 *
 * @param serviceColours The service colours mapping.
 * @param dropdownMenus The mapping of produced dropdown menus.
 * @param stopNameComparator Used to order stops.
 * @param serviceNameComparator Used to sort service names.
 * @return This collection of [StopSearchResult]s as a [List] of [UiStopSearchResult]s.
 */
internal fun Collection<StopSearchResult>.toUiStopSearchResults(
    serviceColours: Map<ServiceDescriptor, ServiceColours>?,
    dropdownMenus: Map<StopIdentifier, UiStopSearchResultDropdownMenu>?,
    stopNameComparator: Comparator<String>,
    serviceNameComparator: Comparator<String>
) = map { searchResult ->
    searchResult
        .toUiStopSearchResult(
            serviceColours = serviceColours,
            dropdownMenu = dropdownMenus
                ?.get(searchResult.stopIdentifier)
                ?: UiStopSearchResultDropdownMenu(),
            serviceNameComparator = serviceNameComparator
        )
}.sortedWith(
    compareBy(stopNameComparator) {
        it.stopName.name
    }
)

private fun StopSearchResult.toUiStopSearchResult(
    serviceColours: Map<ServiceDescriptor, ServiceColours>?,
    dropdownMenu: UiStopSearchResultDropdownMenu,
    serviceNameComparator: Comparator<String>
): UiStopSearchResult {
    return UiStopSearchResult(
        stopIdentifier = stopIdentifier,
        stopName = stopName.toUiStopName(),
        services = serviceListing
            ?.ifEmpty { null }
            ?.sortByServiceName(serviceNameComparator)
            ?.map {
                toUiServiceName(it, serviceColours?.get(it))
            }
            ?.toImmutableList(),
        orientation = orientation,
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
