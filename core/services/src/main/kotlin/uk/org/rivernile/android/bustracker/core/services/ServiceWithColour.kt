/*
 * Copyright (C) 2023 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.services

import uk.org.rivernile.android.bustracker.core.database.busstop.service.ServiceWithColour
    as DatabaseServiceWithColour
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor

/**
 * A tuple of a service name and optional colours.
 *
 * @property serviceDescriptor The descriptor of this service.
 * @property colours The display colours of the service. This may be `null` if no colours are
 * attributed.
 * @author Niall Scott
 */
public data class ServiceWithColour(
    val serviceDescriptor: ServiceDescriptor,
    val colours: ServiceColours?
)

internal inline fun List<DatabaseServiceWithColour>.toServiceWithColourList(
    colourProducer: (Int) -> Int?
): List<ServiceWithColour> {
    return map {
        it.toServiceWithColour(colourProducer)
    }
}

private inline fun DatabaseServiceWithColour.toServiceWithColour(
    colourProducer: (Int) -> Int?
): ServiceWithColour {
    return ServiceWithColour(
        serviceDescriptor = descriptor,
        colours = colours.toServiceColours(colourProducer)
    )
}
