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

package uk.org.rivernile.android.bustracker.core.updates

import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdateType
import javax.inject.Inject
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdate as EndpointServiceUpdate

/**
 * This class provides mapping functions for [ServiceUpdate].
 *
 * @author Niall Scott
 */
internal class ServiceUpdateMapper @Inject constructor() {

    /**
     * Map a given [List] of [EndpointServiceUpdate]s to a [List] of [ServiceUpdate]s.
     *
     * @param endpointServiceUpdates The [EndpointServiceUpdate]s to map.
     * @return The given [EndpointServiceUpdate]s mapped to a [List] of [ServiceUpdate]. Will return
     * `null` if [endpointServiceUpdates] is `null` or empty.
     */
    fun mapToServiceUpdates(
        endpointServiceUpdates: List<EndpointServiceUpdate>?
    ): List<ServiceUpdate>? {
        return endpointServiceUpdates
            ?.map(this::mapToServiceUpdate)
            ?.ifEmpty { null }
    }

    /**
     * Map a given [EndpointServiceUpdate] to a [ServiceUpdate].
     *
     * @param serviceUpdate The [EndpointServiceUpdate] to map.
     * @return The mapped values as a [ServiceUpdate].
     */
    private fun mapToServiceUpdate(serviceUpdate: EndpointServiceUpdate): ServiceUpdate {
        return when (serviceUpdate.serviceUpdateType) {
            ServiceUpdateType.INCIDENT -> mapToIncidentServiceUpdate(serviceUpdate)
            ServiceUpdateType.PLANNED -> mapToPlannedServiceUpdate(serviceUpdate)
        }
    }

    /**
     * Map a given [EndpointServiceUpdate] to a [IncidentServiceUpdate].
     *
     * @param serviceUpdate The [EndpointServiceUpdate] to map.
     * @return The mapped values as a [IncidentServiceUpdate].
     */
    private fun mapToIncidentServiceUpdate(
        serviceUpdate: EndpointServiceUpdate
    ): IncidentServiceUpdate {
        return IncidentServiceUpdate(
            serviceUpdate.id,
            serviceUpdate.lastUpdated,
            serviceUpdate.summary,
            serviceUpdate.affectedServices,
            serviceUpdate.url
        )
    }

    /**
     * Map a given [EndpointServiceUpdate] to a [PlannedServiceUpdate].
     *
     * @param serviceUpdate The [EndpointServiceUpdate] to map.
     * @return The mapped values as a [PlannedServiceUpdate].
     */
    private fun mapToPlannedServiceUpdate(
        serviceUpdate: EndpointServiceUpdate
    ): PlannedServiceUpdate {
        return PlannedServiceUpdate(
            serviceUpdate.id,
            serviceUpdate.lastUpdated,
            serviceUpdate.summary,
            serviceUpdate.affectedServices,
            serviceUpdate.url
        )
    }
}