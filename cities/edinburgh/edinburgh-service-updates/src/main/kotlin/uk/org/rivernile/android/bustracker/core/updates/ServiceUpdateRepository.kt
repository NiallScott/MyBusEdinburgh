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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdate as EndpointServiceUpdate
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdatesEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdatesResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This repository is used to access service updates.
 *
 * @author Niall Scott
 */
interface ServiceUpdateRepository {

    /**
     * A [Flow] which emits [ServiceUpdatesResult]s for [IncidentServiceUpdate]s.
     */
    val incidentServiceUpdatesFlow: Flow<ServiceUpdatesResult<IncidentServiceUpdate>>

    /**
     * A [Flow] which emits [ServiceUpdatesResult]s for [PlannedServiceUpdate]s.
     */
    val plannedServiceUpdatesFlow: Flow<ServiceUpdatesResult<PlannedServiceUpdate>>
}

/**
 * This is the real implementation of [ServiceUpdateRepository].
 *
 * @param serviceUpdatesEndpoint The endpoint to receive service updates from.
 * @author Niall Scott
 */
@Singleton
internal class RealServiceUpdateRepository @Inject constructor(
    private val serviceUpdatesEndpoint: ServiceUpdatesEndpoint
) : ServiceUpdateRepository {

    override val incidentServiceUpdatesFlow get() = flow {
        emit(ServiceUpdatesResult.InProgress)
        emit(
            fetchServiceUpdates {
                it?.toIncidentsServiceUpdatesOrNull()
            }
        )
    }

    override val plannedServiceUpdatesFlow get() = flow {
        emit(ServiceUpdatesResult.InProgress)
        emit(
            fetchServiceUpdates {
                it?.toPlannedServiceUpdatesOrNull()
            }
        )
    }

    /**
     * This suspending function fetches the latest [ServiceUpdate]s and returns the appropriate
     * [ServiceUpdatesResult] object.
     *
     * @param mapper A function which maps the success data.
     * @return A [ServiceUpdatesResult] object encapsulating the result of the request.
     */
    private suspend inline fun <T : ServiceUpdate> fetchServiceUpdates(
        mapper: (List<EndpointServiceUpdate>?) -> List<T>?
    ): ServiceUpdatesResult<T> {
        return when (val response = serviceUpdatesEndpoint.getServiceUpdates()) {
            is ServiceUpdatesResponse.Success ->
                ServiceUpdatesResult.Success(mapper(response.serviceUpdates))
            is ServiceUpdatesResponse.Error.NoConnectivity ->
                ServiceUpdatesResult.Error.NoConnectivity
            is ServiceUpdatesResponse.Error.Io -> ServiceUpdatesResult.Error.Io(response.throwable)
            is ServiceUpdatesResponse.Error.ServerError -> ServiceUpdatesResult.Error.Server
        }
    }
}