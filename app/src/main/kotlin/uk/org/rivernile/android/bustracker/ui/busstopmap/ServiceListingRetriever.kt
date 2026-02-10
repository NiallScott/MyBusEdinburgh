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

package uk.org.rivernile.android.bustracker.ui.busstopmap

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.sortByServiceName
import uk.org.rivernile.android.bustracker.core.servicestops.ServiceStopsRepository
import javax.inject.Inject

/**
 * This class is used to retrieve the service listing for stops for the stop map.
 *
 * @param serviceStopsRepository Used to get the service listing.
 * @param serviceNameComparator Used to sort the services by name.
 * @author Niall Scott
 */
class ServiceListingRetriever @Inject constructor(
    private val serviceStopsRepository: ServiceStopsRepository,
    private val serviceNameComparator: Comparator<String>
) {

    /**
     * Gets a [kotlinx.coroutines.flow.Flow] of [UiServiceListing] which is the service listing
     * for a given [stopIdentifier].
     *
     * @param stopIdentifier The stop to get the service listing for.
     * @return A [kotlinx.coroutines.flow.Flow] of [UiServiceListing] which is the service listing
     * for a given [stopIdentifier].
     */
    fun getServiceListingFlow(stopIdentifier: StopIdentifier?) = stopIdentifier?.let { sc ->
        serviceStopsRepository.getServicesForStopFlow(sc)
            .map {
                mapToUiServiceListing(sc, it)
            }
            .onStart { emit(UiServiceListing.InProgress(sc)) }
    } ?: flowOf(null)

    /**
     * Map the loaded [services] to the appropriate [UiServiceListing].
     *
     * @param stopIdentifier The stop the service listing is for.
     * @param services The loaded service listing.
     * @return The appropriate [UiServiceListing]. This will be [UiServiceListing.Success] when
     * [services] is not `null` and not empty. Otherwise it will be [UiServiceListing.Empty].
     */
    private fun mapToUiServiceListing(
        stopIdentifier: StopIdentifier,
        services: List<ServiceDescriptor>?
    ) = services
        ?.ifEmpty { null }
        ?.sortByServiceName(serviceNameComparator)
        ?.let {
            UiServiceListing.Success(stopIdentifier, it)
        } ?: UiServiceListing.Empty(stopIdentifier)
}
