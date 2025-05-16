/*
 * Copyright (C) 2024 - 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.news.serviceupdates

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName

/**
 * Map a given [Collection] of [String] service names to an [ImmutableList] of [UiServiceName]s.
 *
 * @param serviceNames A [Collection] containing the service names of affected services.
 * @param serviceColours A [Map] of [ServiceColours] used to populate the colours for the services.
 * May be `null` if not available.
 * @param serviceNamesComparator A [Comparator] used to sort the services by name.
 * @return The given [serviceNames] mapped to an [ImmutableList] of [UiServiceName]s, in the order
 * defined by [serviceNamesComparator], with colours provided by [serviceColours].
 */
internal fun toUiServiceNamesOrNull(
    serviceNames: Collection<String>?,
    serviceColours: Map<String, ServiceColours>?,
    serviceNamesComparator: Comparator<String>
): ImmutableList<UiServiceName>? {
    return serviceNames
        ?.ifEmpty { null }
        ?.sortedWith(serviceNamesComparator)
        ?.map {
            toUiServiceName(it, serviceColours?.get(it))
        }
        ?.toImmutableList()
}

/**
 * Map a given [serviceName] and [serviceColours] to a [UiServiceName].
 *
 * @param serviceName The name of the service.
 * @param serviceColours The colours pertaining to this service. May be `null` if there are no
 * known colours for this service.
 * @return The service as a [UiServiceName].
 */
private fun toUiServiceName(
    serviceName: String,
    serviceColours: ServiceColours?
): UiServiceName {
    return serviceColours?.let {
        UiServiceName(
            serviceName = serviceName,
            colours = UiServiceColours(
                backgroundColour = it.primaryColour,
                textColour = it.colourOnPrimary
            )
        )
    } ?: UiServiceName(
        serviceName = serviceName
    )
}