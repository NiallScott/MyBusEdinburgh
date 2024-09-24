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

package uk.org.rivernile.android.bustracker.core.endpoints.updates.service.lothian

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdate

/**
 * This class describes the root JSON object for obtaining service updates.
 *
 * @property events A [List] of [JsonEvent]s.
 * @author Niall Scott
 */
@Serializable
internal data class JsonServiceUpdateEvents(
    @SerialName("events") val events: List<JsonEvent>? = null
)

/**
 * Map this [JsonServiceUpdateEvents] to a [List] of [ServiceUpdate]s. `null` will be returned if
 * there are no events or none of the events could be mapped to valid data.
 *
 * @return This [JsonServiceUpdateEvents] as a [List] of [ServiceUpdate]s. `null` will be returned
 * if there are no events or none of the events could be mapped to valid data.
 */
internal fun JsonServiceUpdateEvents.toServiceUpdatesOrNull(): List<ServiceUpdate>? {
    return events
        ?.mapNotNull { it.toServiceUpdateOrNull() }
        ?.ifEmpty { null }
}