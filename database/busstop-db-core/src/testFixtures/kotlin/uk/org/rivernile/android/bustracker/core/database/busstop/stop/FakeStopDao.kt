/*
 * Copyright (C) 2025 Niall 'Rivernile' Scott
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

/**
 * A fake [StopDao] for testing.
 *
 * @author Niall Scott
 */
class FakeStopDao(
    private val onGetNameForStopFlow: (String) -> Flow<StopName?> = { throw NotImplementedError() },
    private val onGetLocationForStopFlow: (String) -> Flow<StopLocation?> =
        { throw NotImplementedError() },
    private val onGetStopDetailsFlowForSingleStop: (String) -> Flow<StopDetails?> =
        { throw NotImplementedError() },
    private val onGetStopDetailsFlowForMultipleStops:
        (Set<String>) -> Flow<Map<String, StopDetails>?> = { throw NotImplementedError() },
    private val onGetStopDetailsWithServiceFilterFlow: (Set<String>?) -> Flow<List<StopDetails>?> =
        { throw NotImplementedError() },
    private val onGetStopDetailsWithinSpanFlow:
        (Double, Double, Double, Double) -> Flow<List<StopDetailsWithServices>?> =
        { _, _, _, _ -> throw NotImplementedError() },
    private val onGetStopDetailsWithinSpanFlowWithServiceFilter:
        (Double, Double, Double, Double, Set<String>) -> Flow<List<StopDetailsWithServices>?> =
        { _, _, _, _, _ -> throw NotImplementedError() },
    private val onGetStopSearchResultsFlow: (String) -> Flow<List<StopSearchResult>?> =
        { throw NotImplementedError() }
) : StopDao {

    override fun getNameForStopFlow(stopCode: String) = onGetNameForStopFlow(stopCode)

    override fun getLocationForStopFlow(stopCode: String) = onGetLocationForStopFlow(stopCode)

    override fun getStopDetailsFlow(stopCode: String) = onGetStopDetailsFlowForSingleStop(stopCode)

    override fun getStopDetailsFlow(stopCodes: Set<String>) =
        onGetStopDetailsFlowForMultipleStops(stopCodes)

    override fun getStopDetailsWithServiceFilterFlow(serviceFilter: Set<String>?) =
        onGetStopDetailsWithServiceFilterFlow(serviceFilter)

    override fun getStopDetailsWithinSpanFlow(
        minLatitude: Double,
        minLongitude: Double,
        maxLatitude: Double,
        maxLongitude: Double
    ) = onGetStopDetailsWithinSpanFlow(minLatitude, minLongitude, maxLatitude, maxLongitude)

    override fun getStopDetailsWithinSpanFlow(
        minLatitude: Double,
        minLongitude: Double,
        maxLatitude: Double,
        maxLongitude: Double,
        serviceFilter: Set<String>
    ) = onGetStopDetailsWithinSpanFlowWithServiceFilter(
        minLatitude,
        minLongitude,
        maxLatitude,
        maxLongitude,
        serviceFilter
    )

    override fun getStopSearchResultsFlow(searchTerm: String) =
        onGetStopSearchResultsFlow(searchTerm)
}