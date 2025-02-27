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

package uk.org.rivernile.android.bustracker.core.busstops

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.FakeStopDao
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.FakeStopDetails
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.FakeStopDetailsWithServices
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.FakeStopLocation
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.FakeStopName
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.FakeStopSearchResult
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopDao
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopDetails
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopDetailsWithServices
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopLocation
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopName
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopOrientation
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopSearchResult
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.fail

/**
 * Tests for [RealBusStopsRepository].
 *
 * @author Niall Scott
 */
class RealBusStopsRepositoryTest {

    @Test
    fun getNameForStopFlowEmitsItems() = runTest {
        val stopName1 = createStopName(1)
        val stopName2 = createStopName(2)
        val repository = createBusStopsRepository(
            stopsDao = FakeStopDao(
                onGetNameForStopFlow = {
                    assertEquals("123456", it)
                    intervalFlowOf(
                        0L,
                        10L,
                        null,
                        stopName1,
                        stopName2
                    )
                }
            )
        )

        repository.getNameForStopFlow("123456").test {
            assertNull(awaitItem())
            assertEquals(stopName1, awaitItem())
            assertEquals(stopName2, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getBusStopDetailsFlowEmitsItems() = runTest {
        val stopDetails1 = createStopDetails(1)
        val stopDetails2 = createStopDetails(2)
        val repository = createBusStopsRepository(
            stopsDao = FakeStopDao(
                onGetStopDetailsFlowForSingleStop = {
                    assertEquals("123456", it)
                    intervalFlowOf(
                        0L,
                        10L,
                        null,
                        stopDetails1,
                        stopDetails2
                    )
                }
            )
        )

        repository.getBusStopDetailsFlow("123456").test {
            assertNull(awaitItem())
            assertEquals(stopDetails1, awaitItem())
            assertEquals(stopDetails2, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getBusStopDetailsFlowWithStopCodeSetEmitsItems() = runTest {
        val stopDetails1 = mapOf<String, StopDetails>()
        val stopDetails2 = mapOf<String, StopDetails>()
        val repository = createBusStopsRepository(
            stopsDao = FakeStopDao(
                onGetStopDetailsFlowForMultipleStops = {
                    assertEquals(setOf("123456", "987654"), it)
                    intervalFlowOf(
                        0L,
                        10L,
                        null,
                        stopDetails1,
                        stopDetails2
                    )
                }
            )
        )

        repository.getBusStopDetailsFlow(setOf("123456", "987654")).test {
            assertNull(awaitItem())
            assertEquals(stopDetails1, awaitItem())
            assertEquals(stopDetails2, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getStopDetailsWithinSpanFlowWithNullServiceFilterEmitsItems() = runTest {
        val stopDetails1 = createStopDetailsWithServices(1)
        val stopDetails2 = createStopDetailsWithServices(2)
        val repository = createBusStopsRepository(
            stopsDao = FakeStopDao(
                onGetStopDetailsWithinSpanFlow = { minLat, minLon, maxLat, maxLon ->
                    assertEquals(1.1, minLat)
                    assertEquals(2.2, minLon)
                    assertEquals(3.3, maxLat)
                    assertEquals(4.4, maxLon)
                    intervalFlowOf(
                        0L,
                        10L,
                        null,
                        listOf(stopDetails1),
                        listOf(stopDetails1, stopDetails2)
                    )
                },
                onGetStopDetailsWithinSpanFlowWithServiceFilter = { _, _, _, _, _ ->
                    fail("Not expecting to filter by services")
                }
            )
        )

        repository.getStopDetailsWithinSpanFlow(1.1, 2.2, 3.3, 4.4, null).test {
            assertNull(awaitItem())
            assertEquals(listOf(stopDetails1), awaitItem())
            assertEquals(listOf(stopDetails1, stopDetails2), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getStopDetailsWithinSpanFlowWithEmptyServiceFilterEmitsItems() = runTest {
        val stopDetails1 = createStopDetailsWithServices(1)
        val stopDetails2 = createStopDetailsWithServices(2)
        val repository = createBusStopsRepository(
            stopsDao = FakeStopDao(
                onGetStopDetailsWithinSpanFlow = { minLat, minLon, maxLat, maxLon ->
                    assertEquals(1.1, minLat)
                    assertEquals(2.2, minLon)
                    assertEquals(3.3, maxLat)
                    assertEquals(4.4, maxLon)
                    intervalFlowOf(
                        0L,
                        10L,
                        null,
                        listOf(stopDetails1),
                        listOf(stopDetails1, stopDetails2)
                    )
                },
                onGetStopDetailsWithinSpanFlowWithServiceFilter = { _, _, _, _, _ ->
                    fail("Not expecting to filter by services")
                }
            )
        )

        repository.getStopDetailsWithinSpanFlow(1.1, 2.2, 3.3, 4.4, emptySet()).test {
            assertNull(awaitItem())
            assertEquals(listOf(stopDetails1), awaitItem())
            assertEquals(listOf(stopDetails1, stopDetails2), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getStopDetailsWithinSpanFlowWithServiceFilterEmitsItems() = runTest {
        val stopDetails1 = createStopDetailsWithServices(1)
        val stopDetails2 = createStopDetailsWithServices(2)
        val repository = createBusStopsRepository(
            stopsDao = FakeStopDao(
                onGetStopDetailsWithinSpanFlow = { _, _, _, _ ->
                    fail("Expecting the service filter to be used.")
                },
                onGetStopDetailsWithinSpanFlowWithServiceFilter = { minLat, minLon, maxLat, maxLon,
                                                                    serviceFilter ->
                    assertEquals(1.1, minLat)
                    assertEquals(2.2, minLon)
                    assertEquals(3.3, maxLat)
                    assertEquals(4.4, maxLon)
                    assertEquals(setOf("1", "2", "3"), serviceFilter)
                    intervalFlowOf(
                        0L,
                        10L,
                        null,
                        listOf(stopDetails1),
                        listOf(stopDetails1, stopDetails2)
                    )
                }
            )
        )

        repository.getStopDetailsWithinSpanFlow(
            1.1,
            2.2,
            3.3,
            4.4,
            setOf("1", "2", "3")
        ).test {
            assertNull(awaitItem())
            assertEquals(listOf(stopDetails1), awaitItem())
            assertEquals(listOf(stopDetails1, stopDetails2), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getStopDetailsWithServiceFilterFlowEmitsItems() = runTest {
        val stopDetails1 = createStopDetails(1)
        val stopDetails2 = createStopDetails(2)
        val repository = createBusStopsRepository(
            stopsDao = FakeStopDao(
                onGetStopDetailsWithServiceFilterFlow = {
                    assertEquals(setOf("1", "2", "3"), it)
                    intervalFlowOf(
                        0L,
                        10L,
                        null,
                        listOf(stopDetails1),
                        listOf(stopDetails1, stopDetails2)
                    )
                }
            )
        )

        repository.getStopDetailsWithServiceFilterFlow(setOf("1", "2", "3")).test {
            assertNull(awaitItem())
            assertEquals(listOf(stopDetails1), awaitItem())
            assertEquals(listOf(stopDetails1, stopDetails2), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getStopSearchResultsFlowEmitsItems() = runTest {
        val searchResult1 = createStopSearchResult(1)
        val searchResult2 = createStopSearchResult(2)
        val repository = createBusStopsRepository(
            stopsDao = FakeStopDao(
                onGetStopSearchResultsFlow = {
                    assertEquals("search term", it)
                    intervalFlowOf(
                        0L,
                        10L,
                        null,
                        listOf(searchResult1),
                        listOf(searchResult1, searchResult2)
                    )
                }
            )
        )

        repository.getStopSearchResultsFlow("search term").test {
            assertNull(awaitItem())
            assertEquals(listOf(searchResult1), awaitItem())
            assertEquals(listOf(searchResult1, searchResult2), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getStopLocationReturnsStopLocation() = runTest {
        val stopLocation = createStopLocation()
        val repository = createBusStopsRepository(
            stopsDao = FakeStopDao(
                onGetLocationForStopFlow = {
                    assertEquals("123456", it)
                    intervalFlowOf(10L, 10L, stopLocation)
                }
            )
        )

        val result = repository.getStopLocation("123456")

        assertEquals(stopLocation, result)
    }

    @Test
    fun getNameForStopReturnsStopName() = runTest {
        val stopName = createStopName()
        val repository = createBusStopsRepository(
            stopsDao = FakeStopDao(
                onGetNameForStopFlow = {
                    assertEquals("123456", it)
                    intervalFlowOf(10L, 10L, stopName)
                }
            )
        )

        val result = repository.getNameForStop("123456")

        assertEquals(stopName, result)
    }

    private fun createBusStopsRepository(
        stopsDao: StopDao = FakeStopDao()
    ): BusStopsRepository {
        return RealBusStopsRepository(stopsDao)
    }

    private fun createStopName(index: Int = 1): StopName {
        return FakeStopName(
            name = "Name$index",
            locality = "Locality$index"
        )
    }

    private fun createStopLocation(index: Int = 1): StopLocation {
        return FakeStopLocation(
            latitude = 1.1 * index,
            longitude = 2.2 * index
        )
    }

    private fun createStopDetails(index: Int = 1): StopDetails {
        return FakeStopDetails(
            stopCode = "stopCode$index",
            stopName = createStopName(index),
            location = createStopLocation(index),
            orientation = StopOrientation.NORTH
        )
    }

    private fun createStopDetailsWithServices(index: Int = 1): StopDetailsWithServices {
        return FakeStopDetailsWithServices(
            stopCode = "stopCode$index",
            stopName = createStopName(index),
            location = createStopLocation(index),
            orientation = StopOrientation.NORTH,
            serviceListing = null
        )
    }

    private fun createStopSearchResult(index: Int = 1): StopSearchResult {
        return FakeStopSearchResult(
            stopCode = "stopCode$index",
            stopName = createStopName(index),
            orientation = StopOrientation.NORTH,
            serviceListing = null
        )
    }
}