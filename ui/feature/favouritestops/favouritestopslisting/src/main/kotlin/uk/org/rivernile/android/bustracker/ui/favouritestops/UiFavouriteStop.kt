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

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName

/**
 * A favourite stop which is displayed on the UI.
 *
 * @property stopIdentifier The stop identifier of the favourite stop.
 * @property savedName The user's saved name for this favourite stop.
 * @property services An [ImmutableList] of [UiServiceName]s which stop at this stop.
 * @property dropdownMenu Data for any [UiFavouriteDropdownMenu] which could be shown. If this is
 * `null` then the dropdown menu is not available for this item.
 * @author Niall Scott
 */
internal data class UiFavouriteStop(
    val stopIdentifier: StopIdentifier,
    val savedName: String,
    val services: ImmutableList<UiServiceName>?,
    val dropdownMenu: UiFavouriteDropdownMenu?
)

/**
 * Map this [List] of [FavouriteStopWithServices] to a [List] of [UiFavouriteStop].
 *
 * @param serviceColours A [Map] of services to [ServiceColours].
 * @param dropdownMenus A mapping of stop identifiers to [UiFavouriteDropdownMenu]s, used to
 * populate the dropdown menu for each [UiFavouriteStop].
 * @return This [List] of [FavouriteStopWithServices] mapped to a [List] of [UiFavouriteStop]s.
 */
internal fun List<FavouriteStopWithServices>.toUiFavouriteStops(
    serviceColours: Map<ServiceDescriptor, ServiceColours>?,
    dropdownMenus: Map<StopIdentifier, UiFavouriteDropdownMenu>?
) = map { favouriteStop ->
    favouriteStop
        .toUiFavouriteStop(
            serviceColours = serviceColours,
            dropdownMenu = dropdownMenus?.get(favouriteStop.stopIdentifier)
        )
}

private fun FavouriteStopWithServices.toUiFavouriteStop(
    serviceColours: Map<ServiceDescriptor, ServiceColours>?,
    dropdownMenu: UiFavouriteDropdownMenu?
): UiFavouriteStop {
    return UiFavouriteStop(
        stopIdentifier = stopIdentifier,
        savedName = savedName,
        services = services
            ?.ifEmpty { null }
            ?.map {
                toUiServiceName(it, serviceColours?.get(it))
            }
            ?.toImmutableList(),
        dropdownMenu = dropdownMenu
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
