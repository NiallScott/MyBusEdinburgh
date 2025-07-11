/*
 * Copyright (C) 2023 - 2025 Niall 'Rivernile' Scott
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

import uk.org.rivernile.android.bustracker.core.database.busstop.BusStopDatabase
import uk.org.rivernile.android.bustracker.core.database.busstop.withFlowIfDatabaseIsOpenOrEmptyFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The proxy implementation of [StopDao] which responds to the database opening/closing and
 * switches [kotlinx.coroutines.flow.Flow]s when this happens.
 *
 * @param database A reference to the database.
 * @author Niall Scott
 */
@Singleton
internal class ProxyStopDao @Inject constructor(
    private val database: BusStopDatabase
) : StopDao {

    override fun getNameForStopFlow(stopCode: String) = database
        .withFlowIfDatabaseIsOpenOrEmptyFlow {
            stopDao.getNameForStopFlow(stopCode)
        }

    override fun getLocationForStopFlow(stopCode: String) = database
        .withFlowIfDatabaseIsOpenOrEmptyFlow {
            stopDao.getLocationForStopFlow(stopCode)
        }

    override fun getStopDetailsFlow(stopCode: String) = database
        .withFlowIfDatabaseIsOpenOrEmptyFlow {
            stopDao.getStopDetailsFlow(stopCode)
        }

    override fun getStopDetailsFlow(stopCodes: Set<String>) = database
        .withFlowIfDatabaseIsOpenOrEmptyFlow {
            stopDao.getStopDetailsFlow(stopCodes)
        }

    override fun getStopDetailsWithServiceFilterFlow(serviceFilter: Set<String>?) = database
        .withFlowIfDatabaseIsOpenOrEmptyFlow {
            stopDao.getStopDetailsWithServiceFilterFlow(serviceFilter)
        }

    override fun getStopDetailsWithinSpanFlow(
        minLatitude: Double,
        minLongitude: Double,
        maxLatitude: Double,
        maxLongitude: Double
    ) = database.withFlowIfDatabaseIsOpenOrEmptyFlow {
        stopDao.getStopDetailsWithinSpanFlow(
            minLatitude = minLatitude,
            minLongitude = minLongitude,
            maxLatitude = maxLatitude,
            maxLongitude = maxLongitude
        )
    }

    override fun getStopDetailsWithinSpanFlow(
        minLatitude: Double,
        minLongitude: Double,
        maxLatitude: Double,
        maxLongitude: Double,
        serviceFilter: Set<String>
    ) = database.withFlowIfDatabaseIsOpenOrEmptyFlow {
        stopDao.getStopDetailsWithinSpanFlow(
            minLatitude = minLatitude,
            minLongitude = minLongitude,
            maxLatitude = maxLatitude,
            maxLongitude = maxLongitude,
            serviceFilter = serviceFilter
        )
    }

    override fun getStopSearchResultsFlow(searchTerm: String) = database
        .withFlowIfDatabaseIsOpenOrEmptyFlow {
            stopDao.getStopSearchResultsFlow(searchTerm)
        }
}