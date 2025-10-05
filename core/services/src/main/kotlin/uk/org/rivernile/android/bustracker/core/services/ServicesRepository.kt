/*
 * Copyright (C) 2020 - 2025 Niall 'Rivernile' Scott
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
import uk.org.rivernile.android.bustracker.core.database.busstop.service.ServiceDetails as StoredServiceDetails
import uk.org.rivernile.android.bustracker.core.database.busstop.service.ServiceWithColour as StoredServiceWithColour
import javax.inject.Inject

/**
 * This repository is used to access services data.
 */
interface ServicesRepository {

    /**
     * Get a [Flow] which returns a [Map] of service names to [ServiceColours] for the service, and
     * will emit further items if the backing store changes.
     *
     * @param services The services to get colours for. If `null` or empty, colours for all known
     * services will be returned.
     * @return The [Flow] which emits the service-colour mapping.
     */
    fun getColoursForServicesFlow(
        services: Set<String>? = null
    ): Flow<Map<String, ServiceColours>?>

    /**
     * Get a [Flow] which emits a [List] of [ServiceDetails] for services which stop at the given
     * [stopCode]. This [List] will return items already sorted.
     *
     * @param stopCode The stop code to get a [List] of [ServiceDetails] for.
     * @return A [Flow] which emits the service details, or emits `null` when no services could be
     * found or some other error occurred obtaining the services.
     */
    fun getServiceDetailsFlow(stopCode: String): Flow<List<ServiceDetails>?>

    /**
     * A [Flow] which emits ordered [List]s of [ServiceWithColour] for all known services.
     */
    val allServiceNamesWithColourFlow: Flow<List<ServiceWithColour>?>

    /**
     * Get a [Flow] which emits ordered [List]s of [ServiceWithColour] for services which stop at
     * the given [stopCode].
     *
     * @param stopCode The stop code to get services for.
     * @return A [Flow] which emits ordered [List]s of [ServiceWithColour] for services which stop
     * at the given [stopCode].
     */
    fun getServiceNamesWithColourFlow(stopCode: String): Flow<List<ServiceWithColour>?>

    /**
     * This [Flow] emits whether there are known services.
     */
    val hasServicesFlow: Flow<Boolean>
}

/**
 * This repository is used to access services data.
 *
 * @param serviceDao The DAO to access the services data store.
 * @param serviceColourOverride An implementation which may override the loaded service colours with
 * a hard-wired implementation. The actual implementation will most likely be defined per product
 * flavour.
 * @param serviceColoursGenerator An implementation which generates colours for a service.
 * @author Niall Scott
 */
internal class RealServicesRepository @Inject constructor(
    private val serviceDao: ServiceDao,
    private val serviceColourOverride: ServiceColourOverride?,
    private val serviceColoursGenerator: ServiceColoursGenerator
) : ServicesRepository {

    override fun getColoursForServicesFlow(
        services: Set<String>?
    ): Flow<Map<String, ServiceColours>?> {
        val initialResults = services
            ?.ifEmpty { null }
            ?.associateWith<String, Int?> { null }
            ?: emptyMap()

        return serviceDao.getColoursForServicesFlow(services)
            .map { loadedResults ->
                val combinedResult = loadedResults?.let {
                    initialResults + it
                } ?: initialResults

                mapToColoursForServices(combinedResult)
            }
    }

    override fun getServiceDetailsFlow(stopCode: String): Flow<List<ServiceDetails>?> {
        return serviceDao.getServiceDetailsFlow(stopCode)
            .map(this::mapToServiceDetailsList)
    }

    override val allServiceNamesWithColourFlow: Flow<List<ServiceWithColour>?> get() =
        serviceDao.allServiceNamesWithColourFlow
            .map(this::mapToServicesWithColour)

    override fun getServiceNamesWithColourFlow(stopCode: String): Flow<List<ServiceWithColour>?> {
        return serviceDao.getServiceNamesWithColourFlow(stopCode)
            .map(this::mapToServicesWithColour)
    }

    override val hasServicesFlow: Flow<Boolean> get() =
        serviceDao.serviceCountFlow
            .map { count ->
                count?.let { it > 0 } ?: false
            }
            .distinctUntilChanged()

    /**
     * Given a [loadedResults] [Map], apply the [ServiceColourOverride] implementation on each entry
     * if available. Once this is done, produce a [Map] which contains only non-`null` values.
     *
     * @param loadedResults The loaded results [Map] containing mappings between service names and
     * colour integers.
     * @return The [loadedResults] [Map] with the [ServiceColourOverride] implementation applied
     * on all entries (if available) and then all `null` values removed. Will be `null` when the
     * resulting [Map] is empty.
     */
    private fun mapToColoursForServices(
        loadedResults: Map<String, Int?>
    ): Map<String, ServiceColours>? {
        return buildMap {
            loadedResults.forEach { result ->
                produceColoursForService(result.key, result.value)?.let {
                    this[result.key] = it
                }
            }
        }.ifEmpty { null }
    }

    /**
     * Given a [List] of [StoredServiceDetails], map this to a [List] of [ServiceDetails].
     *
     * @param details The [List] to map.
     * @return The mapped [List].
     */
    private fun mapToServiceDetailsList(
        details: List<StoredServiceDetails>?
    ): List<ServiceDetails>? {
        return details
            ?.ifEmpty { null }
            ?.map { it.toServiceDetails() }
    }

    /**
     * Map a [StoredServiceDetails] to [ServiceDetails].
     *
     * @return The mapped result.
     */
    private fun StoredServiceDetails.toServiceDetails(): ServiceDetails {
        return ServiceDetails(
            name = name,
            description = description,
            colours = produceColoursForService(name, colour)
        )
    }

    /**
     * Given a [List] of [StoredServiceWithColour]s, map this to a [List] of [ServiceWithColour].
     *
     * @param servicesWithColour The [List] to map.
     * @return The mapped [List].
     */
    private fun mapToServicesWithColour(
        servicesWithColour: List<StoredServiceWithColour>?
    ): List<ServiceWithColour>? {
        return servicesWithColour
            ?.ifEmpty { null }
            ?.map { it.toServiceWithColour() }
    }

    /**
     * Given a [StoredServiceWithColour], map this to a [ServiceWithColour].
     *
     * @return The mapped item.
     */
    private fun StoredServiceWithColour.toServiceWithColour(): ServiceWithColour {
        return ServiceWithColour(
            name = name,
            colours = produceColoursForService(name, colour)
        )
    }

    /**
     * Given a [serviceName] and a possible primary [serviceColour] for the service, generate the
     * [ServiceColours] for this service.
     *
     * @param serviceName Name of the service.
     * @param serviceColour The service's primary colour, if available.
     * @return The [ServiceColours] for this service, or `null` if this could not be determined.
     */
    private fun produceColoursForService(
        serviceName: String,
        serviceColour: Int?
    ): ServiceColours? {
        return serviceColourOverride?.overrideServiceColour(serviceName, serviceColour)
            ?: serviceColoursGenerator.generateServiceColours(serviceColour)
    }
}
