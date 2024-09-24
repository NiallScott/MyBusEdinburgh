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

/**
 * This class contains the JSON data structure for an affected route.
 *
 * @property name The name of the affected route.
 * @author Niall Scott
 */
@Serializable
internal data class JsonRouteAffected(
    @SerialName("name") val name: String? = null
)

/**
 * Map this [Collection] of [JsonRouteAffected] in to a [Set] of service names. If this yields an
 * empty [Set] then `null` will be returned.
 *
 * @return This [Collection] of [JsonRouteAffected] as a [Set] of service names or `null` if the
 * yielded [Set] is empty.
 */
internal fun Collection<JsonRouteAffected>.toAffectedServicesOrNull(): Set<String>? {
    return mapNotNull { it.name?.ifBlank { null } }
        .ifEmpty { null }
        ?.toSet()
}