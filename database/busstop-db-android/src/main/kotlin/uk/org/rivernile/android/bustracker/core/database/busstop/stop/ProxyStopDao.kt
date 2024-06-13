/*
 * Copyright (C) 2023 - 2024 Niall 'Rivernile' Scott
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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import uk.org.rivernile.android.bustracker.core.database.busstop.AndroidBusStopDatabase

/**
 * The proxy implementation of [StopDao] which responds to the database opening/closing and
 * switches [kotlinx.coroutines.flow.Flow]s when this happens.
 *
 * @param database A reference to the database.
 * @author Niall Scott
 */
internal class ProxyStopDao(
    private val database: AndroidBusStopDatabase
) : StopDao {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getNameForStopFlow(stopCode: String) =
        database.isDatabaseOpenFlow
            .flatMapLatest {
                if (it) {
                    database.roomStopDao.getNameForStopFlow(stopCode)
                } else {
                    emptyFlow()
                }
            }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getLocationForStopFlow(stopCode: String) =
        database.isDatabaseOpenFlow
            .flatMapLatest {
                if (it) {
                    database.roomStopDao.getLocationForStopFlow(stopCode)
                } else {
                    emptyFlow()
                }
            }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getStopDetailsFlow(stopCode: String) =
        database.isDatabaseOpenFlow
            .flatMapLatest {
                if (it) {
                    database.roomStopDao.getStopDetailsFlow(stopCode)
                } else {
                    emptyFlow()
                }
            }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getStopDetailsFlow(stopCodes: Set<String>) =
        database.isDatabaseOpenFlow
            .flatMapLatest {
                if (it) {
                    database.roomStopDao.getStopDetailsFlow(stopCodes)
                } else {
                    emptyFlow()
                }
            }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getStopDetailsWithServiceFilterFlow(serviceFilter: Set<String>?) =
        database.isDatabaseOpenFlow
            .flatMapLatest {
                if (it) {
                    database.roomStopDao.getStopDetailsWithServiceFilterFlow(serviceFilter)
                } else {
                    emptyFlow()
                }
            }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getStopDetailsWithinSpanFlow(
        minLatitude: Double,
        minLongitude: Double,
        maxLatitude: Double,
        maxLongitude: Double
    ) =
        database.isDatabaseOpenFlow
            .flatMapLatest {
                if (it) {
                    database.roomStopDao.getStopDetailsWithinSpanFlow(
                        minLatitude,
                        minLongitude, maxLatitude, maxLongitude)
                } else {
                    emptyFlow()
                }
            }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getStopDetailsWithinSpanFlow(
        minLatitude: Double,
        minLongitude: Double,
        maxLatitude: Double,
        maxLongitude: Double,
        serviceFilter: Set<String>
    ) =
        database.isDatabaseOpenFlow
            .flatMapLatest {
                if (it) {
                    database.roomStopDao.getStopDetailsWithinSpanFlow(
                        minLatitude,
                        minLongitude,
                        maxLatitude,
                        maxLongitude,
                        serviceFilter)
                } else {
                    emptyFlow()
                }
            }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getStopSearchResultsFlow(searchTerm: String) =
        database.isDatabaseOpenFlow
            .flatMapLatest {
                if (it) {
                    database.roomStopDao.getStopSearchResultsFlow(searchTerm)
                } else {
                    emptyFlow()
                }
            }
}