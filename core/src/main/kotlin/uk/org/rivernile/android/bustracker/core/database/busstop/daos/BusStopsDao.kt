/*
 * Copyright (C) 2020 - 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.busstop.daos

import kotlinx.coroutines.flow.Flow
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopDetails
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopDetailsWithServices
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopLocation
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopName
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopSearchResult

/**
 * This DAO is used to access bus stops.
 *
 * @author Niall Scott
 */
interface BusStopsDao {

    /**
     * Add a new [OnBusStopsChangedListener] to be informed when the bus stop data has changed.
     *
     * @param listener The listener to add.
     */
    fun addOnBusStopsChangedListener(listener: OnBusStopsChangedListener)

    /**
     * Remove a [OnBusStopsChangedListener] so it is no longer informed that bus stop data has
     * changed.
     *
     * @param listener The listener to remove.
     */
    fun removeOnBusStopsChangedListener(listener: OnBusStopsChangedListener)

    /**
     * Given a stop code, get the name for this stop.
     *
     * @param stopCode The stop to get the name for.
     * @return The name of the stop, or `null` if the name is not known or the stop cannot be found.
     */
    suspend fun getNameForStop(stopCode: String): StopName?

    /**
     * Given a stop code, get the latitude and longitude for this stop.
     *
     * @param stopCode The stop to get the location for.
     * @return The location of the stop, or `null` if the stop is not found.
     */
    suspend fun getLocationForStop(stopCode: String): StopLocation?

    /**
     * Given a stop code, get the details for the stop.
     *
     * @param stopCode The stop to get the details for.
     * @return The details for the given stop, or `null` if the stop is not found.
     */
    suspend fun getStopDetails(stopCode: String): StopDetails?

    /**
     * Given a [Set] of stop codes, get the details for each stop.
     *
     * @param stopCodes The stop codes to get the details for.
     * @return The details for the given stop codes, or `null` if no stops were found or if an error
     * occurred.
     */
    suspend fun getStopDetails(stopCodes: Set<String>): Map<String, StopDetails>?

    /**
     * Given a [Set] of stop codes, get the [List] of services which service this stop.
     *
     * @param stopCodes The stop codes to get the [List] of services for.
     * @return A [Map] of stop code to the [List] of services which service that stop. If `null`
     * is returned, this is either because of an error, or there is no data.
     */
    suspend fun getServicesForStops(stopCodes: Set<String>): Map<String, List<String>>?

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
    fun getStopDetailsWithinSpanFlow(
            minLatitude: Double,
            minLongitude: Double,
            maxLatitude: Double,
            maxLongitude: Double): Flow<List<StopDetailsWithServices>?>

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
    fun getStopDetailsWithinSpanFlow(
            minLatitude: Double,
            minLongitude: Double,
            maxLatitude: Double,
            maxLongitude: Double,
            serviceFilter: List<String>): Flow<List<StopDetailsWithServices>?>

    /**
     * Return a [Flow] which emits a [List] of [StopDetails], which only contains items which
     * satisfy the supplied [serviceFilter].
     *
     * @param serviceFilter An optional [Set] which contains the service filter.
     * @return A [Flow] which emits [List]s pf [StopDetails] which satify the supplied
     * [serviceFilter].
     */
    fun getStopDetailsWithServiceFilterFlow(serviceFilter: Set<String>?): Flow<List<StopDetails>?>

    /**
     * Return a [Flow] which emits [List]s of [StopSearchResult] objects, based upon the supplied
     * [searchTerm].
     *
     * @param searchTerm The search term to use to search for stops.
     * @return A [Flow] which emits [List]s of [StopSearchResult] objects, based upon the supplied
     * [searchTerm].
     */
    fun getStopSearchResultsFlow(searchTerm: String): Flow<List<StopSearchResult>?>

    /**
     * This interface should be implemented to listen for changes to bus stops. Call
     * [addOnBusStopsChangedListener] to register the listener.
     */
    interface OnBusStopsChangedListener {

        /**
         * This is called when bus stop data has changed.
         */
        fun onBusStopsChanged()
    }
}