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

import app.cash.turbine.test
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.FakeStopDao
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.FakeStopDetails
    as DatabaseFakeStopDetails
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.FakeStopDetailsWithServices
    as DatabaseFakeStopDetailsWithServices
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.FakeStopLocation
    as DatabaseFakeStopLocation
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.FakeStopName
    as DatabaseFakeStopName
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.FakeStopSearchResult
    as DatabaseFakeStopSearchResult
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopDao
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopDetails
    as DatabaseStopDetails
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopDetailsWithServices
    as DatabaseStopDetailsWithServices
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopLocation
    as DatabaseStopLocation
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopName as DatabaseStopName
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopOrientation
    as DatabaseStopOrientation
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopSearchResult
    as DatabaseStopSearchResult
import uk.org.rivernile.android.bustracker.core.domain.toAtcoStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
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

    @Test(expected = UnsupportedOperationException::class)
    fun getNameForStopFlowWithNonNaptanStopIdentifierThrowsException() = runTest {
        val repository = createBusStopsRepository(
            stopsDao = FakeStopDao()
        )

        repository.getNameForStopFlow("123456".toAtcoStopIdentifier()).single()
    }

    @Test
    fun getNameForStopFlowEmitsItems() = runTest {
        val databaseStopName1 = createDatabaseStopName(1)
        val databaseStopName2 = createDatabaseStopName(2)
        val repository = createBusStopsRepository(
            stopsDao = FakeStopDao(
                onGetNameForStopFlow = {
                    assertEquals("123456", it)
                    intervalFlowOf(
                        0L,
                        10L,
                        null,
                        databaseStopName1,
                        databaseStopName2
                    )
                }
            )
        )

        repository.getNameForStopFlow("123456".toNaptanStopIdentifier()).test {
            assertNull(awaitItem())
            assertEquals(databaseStopName1.toStopName(), awaitItem())
            assertEquals(databaseStopName2.toStopName(), awaitItem())
            awaitComplete()
        }
    }

    @Test(expected = UnsupportedOperationException::class)
    fun getBusStopDetailsFlowWithNonNaptanStopIdentifierThrowsException() = runTest {
        val repository = createBusStopsRepository(
            stopsDao = FakeStopDao()
        )

        repository.getBusStopDetailsFlow("123456".toAtcoStopIdentifier()).single()
    }

    @Test
    fun getBusStopDetailsFlowEmitsItems() = runTest {
        val databaseStopDetails1 = createDatabaseStopDetails(1)
        val databaseStopDetails2 = createDatabaseStopDetails(2)
        val repository = createBusStopsRepository(
            stopsDao = FakeStopDao(
                onGetStopDetailsFlowForSingleStop = {
                    assertEquals("123456", it)
                    intervalFlowOf(
                        0L,
                        10L,
                        null,
                        databaseStopDetails1,
                        databaseStopDetails2
                    )
                }
            )
        )

        repository.getBusStopDetailsFlow("123456".toNaptanStopIdentifier()).test {
            assertNull(awaitItem())
            assertEquals(databaseStopDetails1.toStopDetails(), awaitItem())
            assertEquals(databaseStopDetails2.toStopDetails(), awaitItem())
            awaitComplete()
        }
    }

    @Test(expected = UnsupportedOperationException::class)
    fun getBusStopDetailsFlowWithNonNaptanStopIdentifiersThrowsException() = runTest {
        val repository = createBusStopsRepository(
            stopsDao = FakeStopDao()
        )

        repository.getBusStopDetailsFlow(
            setOf(
                "123456".toAtcoStopIdentifier(),
                "987654".toAtcoStopIdentifier()
            )
        ).single()
    }

    @Test
    fun getBusStopDetailsFlowWithStopCodeSetEmitsItems() = runTest {
        val repository = createBusStopsRepository(
            stopsDao = FakeStopDao(
                onGetStopDetailsFlowForMultipleStops = {
                    assertEquals(setOf("123456", "987654"), it)
                    intervalFlowOf(
                        0L,
                        10L,
                        null,
                        mapOf(),
                        mapOf()
                    )
                }
            )
        )

        repository.getBusStopDetailsFlow(
            setOf(
                "123456".toNaptanStopIdentifier(),
                "987654".toNaptanStopIdentifier()
            )
        ).test {
            assertNull(awaitItem())
            assertEquals(mapOf(), awaitItem())
            assertEquals(mapOf(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getStopDetailsWithinSpanFlowWithNullServiceFilterEmitsItems() = runTest {
        val databaseStopDetails1 = createDatabaseStopDetailsWithServices(1)
        val databaseStopDetails2 = createDatabaseStopDetailsWithServices(2)
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
                        listOf(databaseStopDetails1),
                        listOf(databaseStopDetails1, databaseStopDetails2)
                    )
                },
                onGetStopDetailsWithinSpanFlowWithServiceFilter = { _, _, _, _, _ ->
                    fail("Not expecting to filter by services")
                }
            )
        )

        repository.getStopDetailsWithinSpanFlow(1.1, 2.2, 3.3, 4.4, null).test {
            assertNull(awaitItem())
            assertEquals(listOf(databaseStopDetails1).toStopDetailsWithServicesList(), awaitItem())
            assertEquals(
                listOf(
                    databaseStopDetails1,
                    databaseStopDetails2
                ).toStopDetailsWithServicesList(),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getStopDetailsWithinSpanFlowWithEmptyServiceFilterEmitsItems() = runTest {
        val databaseStopDetails1 = createDatabaseStopDetailsWithServices(1)
        val databaseStopDetails2 = createDatabaseStopDetailsWithServices(2)
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
                        listOf(databaseStopDetails1),
                        listOf(databaseStopDetails1, databaseStopDetails2)
                    )
                },
                onGetStopDetailsWithinSpanFlowWithServiceFilter = { _, _, _, _, _ ->
                    fail("Not expecting to filter by services")
                }
            )
        )

        repository.getStopDetailsWithinSpanFlow(1.1, 2.2, 3.3, 4.4, emptySet()).test {
            assertNull(awaitItem())
            assertEquals(listOf(databaseStopDetails1).toStopDetailsWithServicesList(), awaitItem())
            assertEquals(
                listOf(
                    databaseStopDetails1,
                    databaseStopDetails2
                ).toStopDetailsWithServicesList(),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getStopDetailsWithinSpanFlowWithServiceFilterEmitsItems() = runTest {
        val databaseStopDetails1 = createDatabaseStopDetailsWithServices(1)
        val databaseStopDetails2 = createDatabaseStopDetailsWithServices(2)
        val services = setOf(
            FakeServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            FakeServiceDescriptor(
                serviceName = "2",
                operatorCode = "TEST2"
            ),
            FakeServiceDescriptor(
                serviceName = "3",
                operatorCode = "TEST3"
            )
        )
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
                    assertEquals(services, serviceFilter)
                    intervalFlowOf(
                        0L,
                        10L,
                        null,
                        listOf(databaseStopDetails1),
                        listOf(databaseStopDetails1, databaseStopDetails2)
                    )
                }
            )
        )

        repository.getStopDetailsWithinSpanFlow(
            1.1,
            2.2,
            3.3,
            4.4,
            services
        ).test {
            assertNull(awaitItem())
            assertEquals(listOf(databaseStopDetails1).toStopDetailsWithServicesList(), awaitItem())
            assertEquals(
                listOf(
                    databaseStopDetails1,
                    databaseStopDetails2
                ).toStopDetailsWithServicesList(),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getStopDetailsWithServiceFilterFlowEmitsItems() = runTest {
        val databaseStopDetails1 = createDatabaseStopDetails(1)
        val databaseStopDetails2 = createDatabaseStopDetails(2)
        val services = setOf(
            FakeServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            FakeServiceDescriptor(
                serviceName = "2",
                operatorCode = "TEST2"
            ),
            FakeServiceDescriptor(
                serviceName = "3",
                operatorCode = "TEST3"
            )
        )
        val repository = createBusStopsRepository(
            stopsDao = FakeStopDao(
                onGetStopDetailsWithServiceFilterFlow = {
                    assertEquals(services, it)
                    intervalFlowOf(
                        0L,
                        10L,
                        null,
                        listOf(databaseStopDetails1),
                        listOf(databaseStopDetails1, databaseStopDetails2)
                    )
                }
            )
        )

        repository.getStopDetailsWithServiceFilterFlow(services).test {
            assertNull(awaitItem())
            assertEquals(listOf(databaseStopDetails1).toStopDetailsList(), awaitItem())
            assertEquals(
                listOf(
                    databaseStopDetails1,
                    databaseStopDetails2
                ).toStopDetailsList(),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getStopSearchResultsFlowEmitsItems() = runTest {
        val databaseSearchResult1 = createDatabaseStopSearchResult(1)
        val databaseSearchResult2 = createDatabaseStopSearchResult(2)
        val repository = createBusStopsRepository(
            stopsDao = FakeStopDao(
                onGetStopSearchResultsFlow = {
                    assertEquals("search term", it)
                    intervalFlowOf(
                        0L,
                        10L,
                        null,
                        listOf(databaseSearchResult1),
                        listOf(databaseSearchResult1, databaseSearchResult2)
                    )
                }
            )
        )

        repository.getStopSearchResultsFlow("search term").test {
            assertNull(awaitItem())
            assertEquals(listOf(databaseSearchResult1).toStopSearchResults(), awaitItem())
            assertEquals(
                listOf(
                    databaseSearchResult1,
                    databaseSearchResult2
                ).toStopSearchResults(),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test(expected = UnsupportedOperationException::class)
    fun getStopLocationWithNonNaptanStopIdentifierThrowsException() = runTest {
        val repository = createBusStopsRepository(
            stopsDao = FakeStopDao()
        )

        repository.getStopLocation("123456".toAtcoStopIdentifier())
    }

    @Test
    fun getStopLocationReturnsStopLocation() = runTest {
        val databaseStopLocation = createDatabaseStopLocation()
        val repository = createBusStopsRepository(
            stopsDao = FakeStopDao(
                onGetLocationForStopFlow = {
                    assertEquals("123456", it)
                    intervalFlowOf(10L, 10L, databaseStopLocation)
                }
            )
        )

        val result = repository.getStopLocation("123456".toNaptanStopIdentifier())

        assertEquals(databaseStopLocation.toStopLocation(), result)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun getNameForStopWithNonNaptanStopIdentifierThrowsException() = runTest {
        val repository = createBusStopsRepository(
            stopsDao = FakeStopDao()
        )

        repository.getNameForStop("123456".toAtcoStopIdentifier())
    }

    @Test
    fun getNameForStopReturnsStopName() = runTest {
        val databaseStopName = createDatabaseStopName()
        val repository = createBusStopsRepository(
            stopsDao = FakeStopDao(
                onGetNameForStopFlow = {
                    assertEquals("123456", it)
                    intervalFlowOf(10L, 10L, databaseStopName)
                }
            )
        )

        val result = repository.getNameForStop("123456".toNaptanStopIdentifier())

        assertEquals(databaseStopName.toStopName(), result)
    }

    private fun createBusStopsRepository(
        stopsDao: StopDao = FakeStopDao()
    ): BusStopsRepository {
        return RealBusStopsRepository(stopsDao)
    }

    private fun createDatabaseStopName(index: Int = 1): DatabaseStopName {
        return DatabaseFakeStopName(
            name = "Name$index",
            locality = "Locality$index"
        )
    }

    private fun createDatabaseStopLocation(index: Int = 1): DatabaseStopLocation {
        return DatabaseFakeStopLocation(
            latitude = 1.1 * index,
            longitude = 2.2 * index
        )
    }

    private fun createDatabaseStopDetails(index: Int = 1): DatabaseStopDetails {
        return DatabaseFakeStopDetails(
            naptanStopIdentifier = "stopCode$index".toNaptanStopIdentifier(),
            stopName = createDatabaseStopName(index),
            location = createDatabaseStopLocation(index),
            orientation = DatabaseStopOrientation.NORTH
        )
    }

    private fun createDatabaseStopDetailsWithServices(
        index: Int = 1
    ): DatabaseStopDetailsWithServices {
        return DatabaseFakeStopDetailsWithServices(
            naptanStopIdentifier = "stopCode$index".toNaptanStopIdentifier(),
            stopName = createDatabaseStopName(index),
            location = createDatabaseStopLocation(index),
            orientation = DatabaseStopOrientation.NORTH,
            serviceListing = null
        )
    }

    private fun createDatabaseStopSearchResult(index: Int = 1): DatabaseStopSearchResult {
        return DatabaseFakeStopSearchResult(
            naptanStopIdentifier = "stopCode$index".toNaptanStopIdentifier(),
            stopName = createDatabaseStopName(index),
            orientation = DatabaseStopOrientation.NORTH,
            serviceListing = null
        )
    }
}
