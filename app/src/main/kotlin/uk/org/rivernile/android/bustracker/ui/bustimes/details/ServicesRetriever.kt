/*
 * Copyright (C) 2022 - 2026 Niall 'Rivernile' Scott
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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.services.ServiceDetails
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import javax.inject.Inject

/**
 * This class is used to retrieve [UiItem.Service]s for a stop, for display on the UI. It loads the
 * services which stop at the stop, then populates the details for these services in to a
 * [UiItem.Service] object.
 *
 * @param servicesRepository Used to retrieve the details for the services.
 * @author Niall Scott
 */
class ServicesRetriever @Inject constructor(
    private val servicesRepository: ServicesRepository
) {

    /**
     * Get a [Flow] of the [UiItem.Service] for each known service for the given stop. If there are
     * no services for the given [stopIdentifier] then `null` will be emitted.
     *
     * @param stopIdentifier The stop code to get services for.
     * @return A [Flow] which emits a [List] of [UiItem.Service] for all known services at this
     * stop or `null` is emitted when there are no services for this stop.
     */
    fun getServicesFlow(stopIdentifier: StopIdentifier): Flow<List<UiItem.Service>?> =
        servicesRepository.getServiceDetailsFlow(stopIdentifier)
            .map(this::mapToUiItemServices)

    /**
     * Given a [List] of [ServiceDetails], map this to a [List] of [UiItem.Service].
     *
     * @param serviceDetails The [List] of [ServiceDetails] to map.
     * @return The [List] of [UiItem.Service] which has been mapped, or `null` if [serviceDetails]
     * is `null` or empty.
     */
    private fun mapToUiItemServices(serviceDetails: List<ServiceDetails>?): List<UiItem.Service>? {
        return serviceDetails
            ?.ifEmpty { null }
            ?.map(this::mapToUiItemService)
    }

    /**
     * Given a [ServiceDetails] object, map this to a [UiItem.Service].
     *
     * @param serviceDetails The [ServiceDetails] to map.
     * @return The [serviceDetails] as a [UiItem.Service].
     */
    private fun mapToUiItemService(serviceDetails: ServiceDetails): UiItem.Service {
        return UiItem.Service(
            serviceDetails.serviceDescriptor.hashCode().toLong(),
            serviceDetails.serviceDescriptor.serviceName,
            serviceDetails.description,
            serviceDetails.colours
        )
    }
}
