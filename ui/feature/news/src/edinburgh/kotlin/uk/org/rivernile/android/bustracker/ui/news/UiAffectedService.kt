/*
 * Copyright (C) 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.news

import uk.org.rivernile.android.bustracker.core.services.ServiceColours

/**
 * Service details regarding an affected service.
 *
 * @property serviceName The display name of the service.
 * @property backgroundColour An optional background colour to use for the service.
 * @property textColour An optional text colour to use for the service.
 * @author Niall Scott
 */
internal data class UiAffectedService(
    val serviceName: String,
    val backgroundColour: Int?,
    val textColour: Int?
)

/**
 * Map a given [Collection] of [String] service names to a [List] of [UiAffectedService]s.
 *
 * @param serviceNames A [Collection] containing the service names of affected services.
 * @param serviceColours A [Map] of [ServiceColours] used to populate the colours for the services.
 * May be `null` if not available.
 * @param serviceNamesComparator A [Comparator] used to sort the services by name.
 * @return The given [serviceNames] mapped to a [List] of [UiAffectedService]s, in the order defined
 * by [serviceNamesComparator], with colours provided by [serviceColours].
 */
internal fun toUiAffectedServicesOrNull(
    serviceNames: Collection<String>?,
    serviceColours: Map<String, ServiceColours>?,
    serviceNamesComparator: Comparator<String>
): List<UiAffectedService>? {
    return serviceNames
        ?.ifEmpty { null }
        ?.sortedWith(serviceNamesComparator)
        ?.map {
            toUiAffectedService(it, serviceColours?.get(it))
        }
}

/**
 * Map a given [serviceName] and [serviceColours] to a [UiAffectedService].
 *
 * @param serviceName The name of the service.
 * @param serviceColours The colours pertaining to this service. May be `null` if there are no
 * known colours for this service.
 * @return The service as a [UiAffectedService].
 */
private fun toUiAffectedService(
    serviceName: String,
    serviceColours: ServiceColours?
): UiAffectedService {
    return serviceColours?.let {
        UiAffectedService(
            serviceName = serviceName,
            backgroundColour = it.primaryColour,
            textColour = it.colourOnPrimary
        )
    } ?: UiAffectedService(
        serviceName = serviceName,
        backgroundColour = null,
        textColour = null
    )
}