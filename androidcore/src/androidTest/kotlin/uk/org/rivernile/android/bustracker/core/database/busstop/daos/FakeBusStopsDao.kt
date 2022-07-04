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

import kotlinx.coroutines.flow.flowOf
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopDetails
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopLocation
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopName

/**
 * A fake implementation of [BusStopsDao].
 *
 * @author Niall Scott
 */
class FakeBusStopsDao : BusStopsDao {

    override fun addOnBusStopsChangedListener(listener: BusStopsDao.OnBusStopsChangedListener) {

    }

    override fun removeOnBusStopsChangedListener(listener: BusStopsDao.OnBusStopsChangedListener) {

    }

    override suspend fun getNameForStop(stopCode: String): StopName? = null

    override suspend fun getLocationForStop(stopCode: String): StopLocation? = null

    override suspend fun getStopDetails(stopCode: String): StopDetails? = null

    override suspend fun getStopDetails(stopCodes: Set<String>): Map<String, StopDetails>? = null

    override suspend fun getServicesForStops(stopCodes: Set<String>): Map<String, List<String>>? =
            null

    override fun getStopDetailsWithinSpanFlow(
            minLatitude: Double,
            minLongitude: Double,
            maxLatitude: Double,
            maxLongitude: Double) = flowOf(null)

    override fun getStopDetailsWithinSpanFlow(
            minLatitude: Double,
            minLongitude: Double,
            maxLatitude: Double,
            maxLongitude: Double,
            serviceFilter: List<String>) = flowOf(null)

    override fun getStopSearchResultsFlow(searchTerm: String) = flowOf(null)
}