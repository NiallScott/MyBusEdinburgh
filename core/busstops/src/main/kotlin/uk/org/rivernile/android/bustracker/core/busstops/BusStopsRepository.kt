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

package uk.org.rivernile.android.bustracker.core.busstops

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopDao
import uk.org.rivernile.android.bustracker.core.domain.NaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This repository is used to access bus stop data.
 *
 * @author Niall Scott
 */
public interface BusStopsRepository {

    /**
     * Get a [Flow] which returns [StopName] for the given [stopIdentifier]. If the stop name is
     * updated later, these will be emitted.
     *
     * @param stopIdentifier The stop to get details for.
     * @return The [Flow] which emits the [StopName] for the given stop code.
     */
    public fun getNameForStopFlow(stopIdentifier: StopIdentifier): Flow<StopName?>

    /**
     * Get a [Flow] which returns [StopDetails] for the given [StopIdentifier]. If stop details are
     * updated later, these will be emitted.
     *
     * @param stopIdentifier The stop to get details for.
     * @return The [Flow] which emits [StopDetails] for the given stop code.
     */
    public fun getBusStopDetailsFlow(stopIdentifier: StopIdentifier): Flow<StopDetails?>

    /**
     * Get a [Flow] which returns [StopDetails] for the given [stopIdentifiers]. If stop details are
     * updated later, these will be emitted.
     *
     * @param stopIdentifiers The stops to get details for.
     * @return The [Flow] which emits [StopDetails] for the given stop codes.
     */
    public fun getBusStopDetailsFlow(
        stopIdentifiers: Set<StopIdentifier>
    ): Flow<Map<StopIdentifier, StopDetails>?>

    /**
     * Return a [Flow] which emits [List]s of [StopDetailsWithServices] objects for stops which
     * match the parameters supplied to this method.
     *
     * @param minLatitude The minimum latitude of stops.
     * @param minLongitude The minimum longitude of stops.
     * @param maxLatitude The maximum latitude of stops.
     * @param maxLongitude The maximum longitude of stops.
     * @param serviceFilter The listing of services to filter by.
     */
    public fun getStopDetailsWithinSpanFlow(
        minLatitude: Double,
        minLongitude: Double,
        maxLatitude: Double,
        maxLongitude: Double,
        serviceFilter: Set<ServiceDescriptor>?
    ): Flow<List<StopDetailsWithServices>?>

    /**
     * Return a [Flow] which emits a [List] of [StopDetails], which only contains items which
     * satisfy the supplied [serviceFilter].
     *
     * @param serviceFilter An optional [Set] which contains the service filter.
     * @return A [Flow] which emits [List]s of [StopDetails] which satisfy the supplied
     * [serviceFilter].
     */
    public fun getStopDetailsWithServiceFilterFlow(
        serviceFilter: Set<ServiceDescriptor>?
    ): Flow<List<StopDetails>?>

    /**
     * Return a [Flow] which emits [List]s of [StopSearchResult] objects for stops which match the
     * given search term.
     *
     * @param searchTerm The search term to use to search for stops.
     * @return A [Flow] which emits [List]s of [StopSearchResult] objects for stops which match the
     * given search term.
     */
    public fun getStopSearchResultsFlow(searchTerm: String): Flow<List<StopSearchResult>?>

    /**
     * Get a [StopLocation] for a given [stopIdentifier].
     *
     * @param stopIdentifier The stop to get a location for.
     * @return The [StopLocation] for the given stop code, or `null` if there is no location for
     * this stop.
     */
    public suspend fun getStopLocation(stopIdentifier: StopIdentifier): StopLocation?

    /**
     * Get the [StopName] for the given [stopIdentifier].
     *
     * @param stopIdentifier The stop to get the name for.
     * @return The [StopName] for the stop, or `null` if it's not available.
     */
    public suspend fun getNameForStop(stopIdentifier: StopIdentifier): StopName?
}

@Singleton
internal class RealBusStopsRepository @Inject constructor(
    private val stopDao: StopDao
) : BusStopsRepository {

    override fun getNameForStopFlow(stopIdentifier: StopIdentifier): Flow<StopName?> {
        return stopDao
            .getNameForStopFlow(stopIdentifier.getNaptanCodeOrThrow())
            .map { it?.toStopName() }
    }

    override fun getBusStopDetailsFlow(stopIdentifier: StopIdentifier): Flow<StopDetails?> {
        return stopDao
            .getStopDetailsFlow(stopIdentifier.getNaptanCodeOrThrow())
            .map { it?.toStopDetails() }
    }

    override fun getBusStopDetailsFlow(
        stopIdentifiers: Set<StopIdentifier>
    ): Flow<Map<StopIdentifier, StopDetails>?> {
        val stopCodes = stopIdentifiers
            .map { it.getNaptanCodeOrThrow() }
            .toSet()

        return stopDao
            .getStopDetailsFlow(stopCodes)
            .map { stopDetails ->
                stopDetails
                    ?.map { (key, value) ->
                        key.toNaptanStopIdentifier() to value.toStopDetails()
                    }
                    ?.toMap()
            }
    }

    override fun getStopDetailsWithinSpanFlow(
        minLatitude: Double,
        minLongitude: Double,
        maxLatitude: Double,
        maxLongitude: Double,
        serviceFilter: Set<ServiceDescriptor>?
    ): Flow<List<StopDetailsWithServices>?> {
        val flow = serviceFilter?.ifEmpty { null }?.let {
            stopDao.getStopDetailsWithinSpanFlow(
                minLatitude,
                minLongitude,
                maxLatitude,
                maxLongitude,
                it
            )
        } ?: stopDao.getStopDetailsWithinSpanFlow(
            minLatitude,
            minLongitude,
            maxLatitude,
            maxLongitude
        )

        return flow
            .map {
                it?.toStopDetailsWithServicesList()
            }
    }

    override fun getStopDetailsWithServiceFilterFlow(
        serviceFilter: Set<ServiceDescriptor>?
    ): Flow<List<StopDetails>?> {
        return stopDao
            .getStopDetailsWithServiceFilterFlow(serviceFilter)
            .map { it?.toStopDetailsList() }
    }

    override fun getStopSearchResultsFlow(searchTerm: String) =
        stopDao
            .getStopSearchResultsFlow(searchTerm)
            .map { it?.toStopSearchResults() }

    override suspend fun getStopLocation(stopIdentifier: StopIdentifier): StopLocation? {
        return stopDao
            .getLocationForStopFlow(stopIdentifier.getNaptanCodeOrThrow())
            .first()
            ?.toStopLocation()
    }

    override suspend fun getNameForStop(stopIdentifier: StopIdentifier): StopName? {
        return stopDao
            .getNameForStopFlow(stopIdentifier.getNaptanCodeOrThrow())
            .first()
            ?.toStopName()
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
