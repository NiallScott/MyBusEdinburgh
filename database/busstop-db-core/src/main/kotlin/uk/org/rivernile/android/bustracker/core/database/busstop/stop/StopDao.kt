/*
 * Copyright (C) 2023 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.busstop.stop

import kotlinx.coroutines.flow.Flow
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor

/**
 * This DAO is used to access stop data in the bus stop database.
 *
 * @author Niall Scott
 */
public interface StopDao {

    /**
     * Given a stop code, return a [Flow] which emits the name for this stop.
     *
     * @param naptanStopCode The stop to get the name for.
     * @return A [Flow] which emits the name of the stop, or will emit `null` when the stop is not
     * known or the stop cannot be found.
     */
    public fun getNameForStopFlow(naptanStopCode: String): Flow<StopName?>

    /**
     * Given a stop code, return a [Flow] which emits the location for this stop.
     *
     * @param naptanStopCode The stop to get the location for.
     * @return A [Flow] which emits the location of the stop, or emits `null` when the stop is not
     * found.
     */
    public fun getLocationForStopFlow(naptanStopCode: String): Flow<StopLocation?>

    /**
     * Given a stop code, return a [Flow] which emits the stop details for this stop.
     *
     * @param naptanStopCode The stop to get the location for.
     * @return A [Flow] which emits the stop details, or emits `null` when the stop is not found.
     */
    public fun getStopDetailsFlow(naptanStopCode: String): Flow<StopDetails?>

    /**
     * Given a [Set] of stop codes, get a [Flow] which emits the details for each stop.
     *
     * @param naptanStopCodes The stop codes to get details for.
     * @return A [Flow] which emits the details for the given stop codes, or emits `null` if no
     * stops were found or if an error occurred.
     */
    public fun getStopDetailsFlow(naptanStopCodes: Set<String>): Flow<Map<String, StopDetails>?>

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
     * Return a [Flow] which emits [List]s of [StopDetailsWithServices] objects which are within
     * the bounding box created by the min/max latitude/longitudes.
     *
     * @param minLatitude The minimum latitude of the included stops.
     * @param minLongitude The minimum longitude of the included stops.
     * @param maxLatitude The maximum latitude of the included stops.
     * @param maxLongitude The maximum longitude of the included stops.
     * @return A [Flow] which emits [List]s of [StopDetailsWithServices] objects which match the
     * filter parameters.
     */
    public fun getStopDetailsWithinSpanFlow(
        minLatitude: Double,
        minLongitude: Double,
        maxLatitude: Double,
        maxLongitude: Double
    ): Flow<List<StopDetailsWithServices>?>

    /**
     * Return a [Flow] which emits [List]s of [StopDetailsWithServices] objects which are within
     * the bounding box created by the min/max latitude/longitudes. Additionally, a [serviceFilter]
     * must be specified where these stops are further filtered to only include stops with these
     * services.
     *
     * @param minLatitude The minimum latitude of the included stops.
     * @param minLongitude The minimum longitude of the included stops.
     * @param maxLatitude The maximum latitude of the included stops.
     * @param maxLongitude The maximum longitude of the included stops.
     * @param serviceFilter A service filter. Only stops which include these services will be
     * emitted.
     * @return A [Flow] which emits [List]s of [StopDetailsWithServices] objects which match the
     * filter parameters.
     */
    public fun getStopDetailsWithinSpanFlow(
        minLatitude: Double,
        minLongitude: Double,
        maxLatitude: Double,
        maxLongitude: Double,
        serviceFilter: Set<ServiceDescriptor>
    ): Flow<List<StopDetailsWithServices>?>

    /**
     * Return a [Flow] which emits [List]s of [StopSearchResult] objects, based upon the supplied
     * [searchTerm].
     *
     * @param searchTerm The search term to use to search for stops.
     * @return A [Flow] which emits [List]s of [StopSearchResult] objects, based upon the supplied
     * [searchTerm].
     */
    public fun getStopSearchResultsFlow(searchTerm: String): Flow<List<StopSearchResult>?>
}
