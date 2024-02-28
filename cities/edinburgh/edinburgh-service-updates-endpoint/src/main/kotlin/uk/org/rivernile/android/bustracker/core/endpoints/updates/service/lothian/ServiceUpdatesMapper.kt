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

import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdate
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdateType
import javax.inject.Inject

private const val SEVERITY_INCIDENT = "incident"
private const val SEVERITY_PLANNED = "planned"

/**
 * This class maps [JsonServiceUpdateEvents] to a [List] of [ServiceUpdate]s.
 *
 * @author Niall Scott
 */
internal class ServiceUpdatesMapper @Inject constructor() {

    /**
     * Given [events], map this to a [List] of [ServiceUpdate]s. `null` will be returned if
     * [events] is `null`, contains no events, or none of its events could be mapped to valid data.
     *
     * @param events The items to be mapped.
     * @return [events] mapped to a [List] of [ServiceUpdate]s. `null` will be returned if [events]
     * is `null`, contains no events, or none of its events could be mapped to valid data.
     */
    fun mapToServiceUpdates(events: JsonServiceUpdateEvents?): List<ServiceUpdate>? {
        return events
            ?.events
            ?.mapNotNull(this::mapToServiceUpdate)
            ?.ifEmpty { null }
    }

    /**
     * Given a [JsonEvent], map this to a [ServiceUpdate]. This will return `null` if the
     * [JsonEvent] could not be mapped, e.g. required data is missing.
     *
     * @param event The data to map.
     * @return The [event] mapped to a [ServiceUpdate], or `null` if this could not be mapped, e.g.
     * required data is missing.
     */
    private fun mapToServiceUpdate(event: JsonEvent): ServiceUpdate? {
        val id = event.id?.ifBlank { null } ?: return null
        val lastUpdated = event.lastUpdatedTime ?: event.createdTime ?: return null
        val serviceUpdateType = mapToServiceUpdateType(event.severity) ?: return null
        val summary = event.descriptions?.get("en")?.ifBlank { null } ?: return null

        return ServiceUpdate(
            id,
            lastUpdated,
            serviceUpdateType,
            summary,
            mapToAffectedServices(event.routesAffected),
            event.url?.ifBlank { null }
        )
    }

    /**
     * Given a [severity] string, map this to [ServiceUpdateType]. `null` will be returned when
     * there is no match.
     *
     * @param severity The severity string to map to a [ServiceUpdateType].
     * @return The [severity] mapped to a [ServiceUpdateType], or `null` if no mapping exists.
     */
    private fun mapToServiceUpdateType(severity: String?): ServiceUpdateType? {
        return when (severity?.lowercase()) {
            SEVERITY_INCIDENT -> ServiceUpdateType.INCIDENT
            SEVERITY_PLANNED -> ServiceUpdateType.PLANNED
            else -> null
        }
    }

    /**
     * Given a [List] of [JsonRouteAffected], map this to a [Set] of service names.
     *
     * @param routesAffected A [List] of [JsonRouteAffected] to be mapped.
     * @return A [Set] of service names, or `null` if [routesAffected] is `null` or empty, or if
     * after mapping an empty collection is yielded.
     */
    private fun mapToAffectedServices(routesAffected: List<JsonRouteAffected>?): Set<String>? {
        return routesAffected
            ?.mapNotNull { it.name?.ifBlank { null } }
            ?.ifEmpty { null }
            ?.toSet()
    }
}