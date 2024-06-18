/*
 * Copyright (C) 2020 - 2024 Niall 'Rivernile' Scott
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
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopDao
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopDetails
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopDetailsWithServices
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopLocation
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopName
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopSearchResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This repository is used to access bus stop data.
 *
 * @param stopDao The DAO to access the bus stop data store.
 * @author Niall Scott
 */
@Singleton
class BusStopsRepository @Inject internal constructor(
    private val stopDao: StopDao
) {

    /**
     * Get a [Flow] which returns [StopName] for the given `stopCode`. If the stop name is updated
     * later, these will be emitted.
     *
     * @param stopCode The stop code to get details for.
     * @return The [Flow] which emits the [StopName] for the given stop code.
     */
    fun getNameForStopFlow(stopCode: String): Flow<StopName?> =
        stopDao.getNameForStopFlow(stopCode)

    /**
     * Get a [Flow] which returns [StopDetails] for the given `stopCode`. If stop details are
     * updated later, these will be emitted.
     *
     * @param stopCode The stop code to get details for.
     * @return The [Flow] which emits [StopDetails] for the given stop code.
     */
    fun getBusStopDetailsFlow(stopCode: String): Flow<StopDetails?> =
        stopDao.getStopDetailsFlow(stopCode)

    /**
     * Get a [Flow] which returns [StopDetails] for the given `stopCodes`. If stop details are
     * updated later, these will be emitted.
     *
     * @param stopCodes The stop codes to get details for.
     * @return The [Flow] which emits [StopDetails] for the given stop codes.
     */
    fun getBusStopDetailsFlow(stopCodes: Set<String>): Flow<Map<String, StopDetails>?> =
        stopDao.getStopDetailsFlow(stopCodes)

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
    fun getStopDetailsWithinSpanFlow(
        minLatitude: Double,
        minLongitude: Double,
        maxLatitude: Double,
        maxLongitude: Double,
        serviceFilter: Set<String>?
    ): Flow<List<StopDetailsWithServices>?> =
        serviceFilter?.ifEmpty { null }?.let {
            stopDao.getStopDetailsWithinSpanFlow(
                minLatitude,
                minLongitude,
                maxLatitude,
                maxLongitude,
                it)
        } ?: stopDao.getStopDetailsWithinSpanFlow(
            minLatitude,
            minLongitude,
            maxLatitude,
            maxLongitude)

    /**
     * Return a [Flow] which emits a [List] of [StopDetails], which only contains items which
     * satisfy the supplied [serviceFilter].
     *
     * @param serviceFilter An optional [Set] which contains the service filter.
     * @return A [Flow] which emits [List]s pf [StopDetails] which satisfy the supplied
     * [serviceFilter].
     */
    fun getStopDetailsWithServiceFilterFlow(serviceFilter: Set<String>?): Flow<List<StopDetails>?> =
        stopDao.getStopDetailsWithServiceFilterFlow(serviceFilter)

    /**
     * Return a [Flow] which emits [List]s of [StopSearchResult] objects for stops which match the
     * given search term.
     *
     * @param searchTerm The search term to use to search for stops.
     * @return A [Flow] which emits [List]s of [StopSearchResult] objects for stops which match the
     * given search term.
     */
    fun getStopSearchResultsFlow(searchTerm: String): Flow<List<StopSearchResult>?> =
        stopDao.getStopSearchResultsFlow(searchTerm)

    /**
     * Get a [StopLocation] for a given [stopCode].
     *
     * @param stopCode The stop code to get a location for.
     * @return The [StopLocation] for the given stop code, or `null` if there is no location for
     * this stop.
     */
    suspend fun getStopLocation(stopCode: String): StopLocation? =
        stopDao.getLocationForStopFlow(stopCode).first()

    /**
     * Get the [StopName] for the given [stopCode].
     *
     * @param stopCode The stop code to get the name for.
     * @return The [StopName] for the stop, or `null` if it's not available.
     */
    suspend fun getNameForStop(stopCode: String): StopName? =
        stopDao.getNameForStopFlow(stopCode).first()
}