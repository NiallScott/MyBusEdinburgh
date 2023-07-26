/*
 * Copyright (C) 2022 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.bustimes.details

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import uk.org.rivernile.android.bustracker.core.services.ServiceDetails
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import uk.org.rivernile.android.bustracker.core.servicestops.ServiceStopsRepository
import javax.inject.Inject

/**
 * This class is used to retrieve [UiItem.Service]s for a stop, for display on the UI. It loads the
 * services which stop at the stop, then populates the details for these services in to a
 * [UiItem.Service] object.
 *
 * @param serviceStopsRepository Used to retrieve the services which are known to stop at the stop.
 * @param servicesRepository Used to retrieve the details for the services.
 * @author Niall Scott
 */
class ServicesRetriever @Inject constructor(
    private val serviceStopsRepository: ServiceStopsRepository,
    private val servicesRepository: ServicesRepository) {

    /**
     * Get a [Flow] of the [UiItem.Service] for each known service for the given stop. If there are
     * no services for the given [stopCode] then `null` will be emitted.
     *
     * @param stopCode The stop code to get services for.
     * @return A [Flow] which emits a [List] of [UiItem.Service] for all known services at this
     * stop or `null` is emitted when there are no services for this stop.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getServicesFlow(stopCode: String): Flow<List<UiItem.Service>?> =
            serviceStopsRepository.getServicesForStopFlow(stopCode)
                    .flatMapLatest(this::loadServiceDetails)

    /**
     * Given a [List] of service names, load the [UiItem.Service] for these services. If there are
     * no services, `null` will be emitted.
     *
     * @param services The services to get [ServiceDetails] for.
     * @return A [Flow] of the [UiItem.Service] for the given [services], or a [Flow] of `null`
     * if there are no services.
     */
    private fun loadServiceDetails(services: List<String>?): Flow<List<UiItem.Service>?> {
        return services?.takeIf(List<String>::isNotEmpty)?.let { s ->
            servicesRepository.getServiceDetailsFlow(s.toSet())
                .map { assembleServiceDetails(s, it) }
        } ?: flowOf(null)
    }

    /**
     * Assemble the [List] of services in to a [List] of [UiItem.Service]s we've loaded. If
     * [ServiceDetails] aren't available for a service, then we synthesise a [UiItem.Service] object
     * for that service with default values.
     *
     * @param services The [List] of services for stop.
     * @param serviceDetails The loaded [ServiceDetails] for the services.
     * @return The services as a [List] of [UiItem.Service].
     */
    private fun assembleServiceDetails(
        services: List<String>,
        serviceDetails: Map<String, ServiceDetails>?) = services.map { service ->
        serviceDetails?.get(service)?.let {
            UiItem.Service(it.name.hashCode().toLong(), it.name, it.description, it.colour)
        } ?: UiItem.Service(service.hashCode().toLong(), service, null, null)
    }
}