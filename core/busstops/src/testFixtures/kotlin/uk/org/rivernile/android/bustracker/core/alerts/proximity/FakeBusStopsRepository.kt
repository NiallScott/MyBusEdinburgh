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

package uk.org.rivernile.android.bustracker.core.alerts.proximity

import kotlinx.coroutines.flow.Flow
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopDetails
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopDetailsWithServices
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopLocation
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopName
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopSearchResult

/**
 * A fake [BusStopsRepository] for testing.
 *
 * @author Niall Scott
 */
class FakeBusStopsRepository(
    private val onGetStopLocation: (String) -> StopLocation? = { throw NotImplementedError() }
) : BusStopsRepository {

    override fun getNameForStopFlow(stopCode: String): Flow<StopName?> {
        throw NotImplementedError()
    }

    override fun getBusStopDetailsFlow(stopCode: String): Flow<StopDetails?> {
        throw NotImplementedError()
    }

    override fun getBusStopDetailsFlow(stopCodes: Set<String>): Flow<Map<String, StopDetails>?> {
        throw NotImplementedError()
    }

    override fun getStopDetailsWithinSpanFlow(
        minLatitude: Double,
        minLongitude: Double,
        maxLatitude: Double,
        maxLongitude: Double,
        serviceFilter: Set<String>?
    ): Flow<List<StopDetailsWithServices>?> {
        throw NotImplementedError()
    }

    override fun getStopDetailsWithServiceFilterFlow(serviceFilter: Set<String>?): Flow<List<StopDetails>?> {
        throw NotImplementedError()
    }

    override fun getStopSearchResultsFlow(searchTerm: String): Flow<List<StopSearchResult>?> {
        throw NotImplementedError()
    }

    override suspend fun getStopLocation(stopCode: String) = onGetStopLocation(stopCode)

    override suspend fun getNameForStop(stopCode: String): StopName? {
        throw NotImplementedError()
    }
}