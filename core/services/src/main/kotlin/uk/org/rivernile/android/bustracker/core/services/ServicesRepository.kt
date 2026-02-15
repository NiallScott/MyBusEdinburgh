/*
 * Copyright (C) 2020 - 2026 Niall 'Rivernile' Scott
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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import uk.org.rivernile.android.bustracker.core.database.busstop.service.ServiceDao
import uk.org.rivernile.android.bustracker.core.domain.NaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import javax.inject.Inject

/**
 * This repository is used to access services data.
 */
public interface ServicesRepository {

    /**
     * Get a [Flow] which returns a [Map] of service to [ServiceColours] for the service, and will
     * emit further items if the backing store changes.
     *
     * @param serviceDescriptors The services to get colours for. If `null` or empty, colours for
     * all known services will be returned.
     * @return The [Flow] which emits the service-colour mapping.
     */
    public fun getColoursForServicesFlow(
        serviceDescriptors: Set<ServiceDescriptor>? = null
    ): Flow<Map<ServiceDescriptor, ServiceColours>?>

    /**
     * Get a [Flow] which emits a [List] of [ServiceDetails] for services which stop at the given
     * [stopIdentifier]. This [List] will return items already sorted.
     *
     * @param stopIdentifier The stop code to get a [List] of [ServiceDetails] for.
     * @return A [Flow] which emits the service details, or emits `null` when no services could be
     * found or some other error occurred obtaining the services.
     */
    public fun getServiceDetailsFlow(stopIdentifier: StopIdentifier): Flow<List<ServiceDetails>?>

    /**
     * A [Flow] which emits ordered [List]s of [ServiceWithColour] for all known services.
     */
    public val allServiceNamesWithColourFlow: Flow<List<ServiceWithColour>?>

    /**
     * Get a [Flow] which emits ordered [List]s of [ServiceWithColour] for services which stop at
     * the given [stopIdentifier].
     *
     * @param stopIdentifier The stop code to get services for.
     * @return A [Flow] which emits ordered [List]s of [ServiceWithColour] for services which stop
     * at the given [stopIdentifier].
     */
    public fun getServiceNamesWithColourFlow(
        stopIdentifier: StopIdentifier
    ): Flow<List<ServiceWithColour>?>

    /**
     * This [Flow] emits whether there are known services.
     */
    public val hasServicesFlow: Flow<Boolean>
}

/**
 * This repository is used to access services data.
 *
 * @param serviceDao The DAO to access the services data store.
 * @param serviceColoursGenerator An implementation which generates colours for a service.
 * @author Niall Scott
 */
internal class RealServicesRepository @Inject constructor(
    private val serviceDao: ServiceDao,
    private val serviceColoursGenerator: ServiceColoursGenerator
) : ServicesRepository {

    override fun getColoursForServicesFlow(
        serviceDescriptors: Set<ServiceDescriptor>?
    ): Flow<Map<ServiceDescriptor, ServiceColours>?> {
        return serviceDao
            .getColoursForServicesFlow(serviceDescriptors)
            .map { result ->
                result
                    ?.mapNotNull { (key, value) ->
                        val newColours = value
                            .toServiceColours(serviceColoursGenerator::generateColourOnPrimary)

                        newColours?.let {
                            key to newColours
                        }
                    }
                    ?.ifEmpty { null }
                    ?.toMap()
            }
    }

    override fun getServiceDetailsFlow(
        stopIdentifier: StopIdentifier
    ): Flow<List<ServiceDetails>?> {
        return serviceDao
            .getServiceDetailsFlow(stopIdentifier.getNaptanCodeOrThrow())
            .map {
                it?.toServiceDetailsList(serviceColoursGenerator::generateColourOnPrimary)
                    ?.ifEmpty { null }
            }
    }

    override val allServiceNamesWithColourFlow: Flow<List<ServiceWithColour>?> get() =
        serviceDao
            .allServiceNamesWithColourFlow
            .map {
                it?.toServiceWithColourList(serviceColoursGenerator::generateColourOnPrimary)
                    ?.ifEmpty { null }
            }

    override fun getServiceNamesWithColourFlow(
        stopIdentifier: StopIdentifier
    ): Flow<List<ServiceWithColour>?> {
        return serviceDao
            .getServiceNamesWithColourFlow(stopIdentifier.getNaptanCodeOrThrow())
            .map {
                it?.toServiceWithColourList(serviceColoursGenerator::generateColourOnPrimary)
                    ?.ifEmpty { null }
            }
    }

    override val hasServicesFlow: Flow<Boolean> get() =
        serviceDao.serviceCountFlow
            .map { count ->
                count?.let { it > 0 } ?: false
            }
            .distinctUntilChanged()

    private fun StopIdentifier.getNaptanCodeOrThrow(): String {
        return if (this is NaptanStopIdentifier) {
            naptanStopCode
        } else {
            throw UnsupportedOperationException("Only Naptan stop identifiers are supported for " +
                "now.")
        }
    }
}
