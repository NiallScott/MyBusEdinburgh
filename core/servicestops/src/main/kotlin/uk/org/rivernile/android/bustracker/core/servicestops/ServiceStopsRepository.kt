/*
 * Copyright (C) 2021 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.servicestops

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.org.rivernile.android.bustracker.core.database.busstop.servicestop.ServiceStopDao
import uk.org.rivernile.android.bustracker.core.domain.NaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import javax.inject.Inject

/**
 * This repository is used to access service stops data.
 *
 * @author Niall Scott
 */
public interface ServiceStopsRepository {

    /**
     * Get a [Flow] which emits the [List] of services which serve the given [stopIdentifier]. If
     * the services for the stop is updated later, these will be emitted as updates.
     *
     * @param stopIdentifier The stop to get the services for.
     * @return The [Flow] which emits the [List] of services for the given stop.
     */
    public fun getServicesForStopFlow(
        stopIdentifier: StopIdentifier
    ): Flow<List<ServiceDescriptor>?>

    /**
     * Get a [Flow] which emits a [Map] of [StopIdentifier]s to a [List] of [ServiceDescriptor]
     * which service that stop. If there is a change to the backing data, there will be a new
     * emission with the latest data.
     *
     * @param stopIdentifiers A [Set] of the stops to get services for.
     * @return The [Flow] which emits the [Map] of stop codes to a [List] of services which service
     * those stops.
     */
    public fun getServicesForStopsFlow(
        stopIdentifiers: Set<StopIdentifier>
    ): Flow<Map<StopIdentifier, List<ServiceDescriptor>>?>
}

internal class DefaultServiceStopsRepository @Inject constructor(
    private val serviceStopDao: ServiceStopDao
) : ServiceStopsRepository {

    override fun getServicesForStopFlow(
        stopIdentifier: StopIdentifier
    ): Flow<List<ServiceDescriptor>?> {
        return serviceStopDao.getServicesForStopFlow(stopIdentifier.getNaptanCodeOrThrow())
    }

    override fun getServicesForStopsFlow(
        stopIdentifiers: Set<StopIdentifier>
    ): Flow<Map<StopIdentifier, List<ServiceDescriptor>>?> {
        val stopCodes = stopIdentifiers
            .map { it.getNaptanCodeOrThrow() }
            .toSet()

        return serviceStopDao
            .getServicesForStopsFlow(stopCodes)
            .map { servicesForStopEntry ->
                servicesForStopEntry
                    ?.mapKeys { it.key.toNaptanStopIdentifier() }
            }
    }

    private fun StopIdentifier.getNaptanCodeOrThrow(): String {
        return if (this is NaptanStopIdentifier) {
            naptanStopCode
        } else {
            throw UnsupportedOperationException("Only Naptan stop identifiers are supported for " +
                "now.")
        }
    }
}
