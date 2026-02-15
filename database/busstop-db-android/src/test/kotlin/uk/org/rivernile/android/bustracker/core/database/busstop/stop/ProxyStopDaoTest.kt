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

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.database.busstop.BusStopDatabase
import uk.org.rivernile.android.bustracker.core.database.busstop.FakeBusStopDatabase
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [ProxyStopDao].
 *
 * @author Niall Scott
 */
class ProxyStopDaoTest {

    @Test
    fun getNameForStopFlowRespondsToDatabaseOpenStatus() = runTest {
        val first = FakeStopName(
            name = "Name 1",
            locality = "Locality 1"
        )
        val second = FakeStopName(
            name = "Name 2",
            locality = "Locality 2"
        )
        val flows = ArrayDeque(
            listOf(
                flowOf(first),
                flowOf(second)
            )
        )
        val dao = createProxyStopDao(
            database = FakeBusStopDatabase(
                onStopDao = {
                    FakeStopDao(
                        onGetNameForStopFlow = {
                            assertEquals("123456", it)
                            flows.removeFirst()
                        }
                    )
                },
                onIsDatabaseOpenFlow = { intervalFlowOf(0L, 10L, true, false, true) }
            )
        )

        dao.getNameForStopFlow("123456").test {
            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
            awaitComplete()
        }
        assertTrue(flows.isEmpty())
    }

    @Test
    fun getLocationForStopFlowRespondsToDatabaseOpenStatus() = runTest {
        val first = FakeStopLocation(latitude = 1.1, longitude = 2.2)
        val second = FakeStopLocation(latitude = 3.3, 4.4)
        val flows = ArrayDeque(
            listOf(
                flowOf(first),
                flowOf(second)
            )
        )
        val dao = createProxyStopDao(
            database = FakeBusStopDatabase(
                onStopDao = {
                    FakeStopDao(
                        onGetLocationForStopFlow = {
                            assertEquals("123456", it)
                            flows.removeFirst()
                        }
                    )
                },
                onIsDatabaseOpenFlow = { intervalFlowOf(0L, 10L, true, false, true) }
            )
        )

        dao.getLocationForStopFlow("123456").test {
            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
            awaitComplete()
        }
        assertTrue(flows.isEmpty())
    }

    @Test
    fun getStopDetailsFlowSingleRespondsToDatabaseOpenStatus() = runTest {
        val first = firstStopDetails
        val second = secondStopDetails
        val flows = ArrayDeque(
            listOf(
                flowOf(first),
                flowOf(second)
            )
        )
        val dao = createProxyStopDao(
            database = FakeBusStopDatabase(
                onStopDao = {
                    FakeStopDao(
                        onGetStopDetailsFlowForSingleStop = {
                            assertEquals("123456", it)
                            flows.removeFirst()
                        }
                    )
                },
                onIsDatabaseOpenFlow = { intervalFlowOf(0L, 10L, true, false, true) }
            )
        )

        dao.getStopDetailsFlow("123456").test {
            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
            awaitComplete()
        }
        assertTrue(flows.isEmpty())
    }

    @Test
    fun getStopDetailsFlowMultipleRespondsToDatabaseOpenStatus() = runTest {
        val first = mapOf("1" to firstStopDetails)
        val second = mapOf("2" to secondStopDetails)
        val flows = ArrayDeque(
            listOf(
                flowOf(first),
                flowOf(second)
            )
        )
        val dao = createProxyStopDao(
            database = FakeBusStopDatabase(
                onStopDao = {
                    FakeStopDao(
                        onGetStopDetailsFlowForMultipleStops = {
                            assertEquals(setOf("123456"), it)
                            flows.removeFirst()
                        }
                    )
                },
                onIsDatabaseOpenFlow = { intervalFlowOf(0L, 10L, true, false, true) }
            )
        )

        dao.getStopDetailsFlow(setOf("123456")).test {
            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
            awaitComplete()
        }
        assertTrue(flows.isEmpty())
    }

    @Test
    fun getStopDetailsWithServiceFilterFlowRespondsToDatabaseOpenStatus() = runTest {
        val first = listOf(firstStopDetails)
        val second = listOf(secondStopDetails)
        val flows = ArrayDeque(
            listOf(
                flowOf(first),
                flowOf(second)
            )
        )
        val dao = createProxyStopDao(
            database = FakeBusStopDatabase(
                onStopDao = {
                    FakeStopDao(
                        onGetStopDetailsWithServiceFilterFlow = {
                            assertEquals(
                                setOf(
                                    FakeServiceDescriptor(
                                        serviceName = "1",
                                        operatorCode = "TEST"
                                    )
                                ),
                                it
                            )
                            flows.removeFirst()
                        }
                    )
                },
                onIsDatabaseOpenFlow = { intervalFlowOf(0L, 10L, true, false, true) }
            )
        )

        dao.getStopDetailsWithServiceFilterFlow(
            setOf(
                FakeServiceDescriptor(
                    serviceName = "1",
                    operatorCode = "TEST"
                )
            )
        ).test {
            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
            awaitComplete()
        }
        assertTrue(flows.isEmpty())
    }

    @Test
    fun getStopDetailsWithinSpanFlowRespondsToDatabaseOpenStatus() = runTest {
        val first = listOf(firstStopDetailsWithServices)
        val second = listOf(secondStopDetailsWithServices)
        val flows = ArrayDeque(
            listOf(
                flowOf(first),
                flowOf(second)
            )
        )
        val dao = createProxyStopDao(
            database = FakeBusStopDatabase(
                onStopDao = {
                    FakeStopDao(
                        onGetStopDetailsWithinSpanFlow = { minLat, minLon, maxLat, maxLon ->
                            assertEquals(1.1, minLat)
                            assertEquals(2.2, minLon)
                            assertEquals(3.3, maxLat)
                            assertEquals(4.4, maxLon)
                            flows.removeFirst()
                        }
                    )
                },
                onIsDatabaseOpenFlow = { intervalFlowOf(0L, 10L, true, false, true) }
            )
        )

        dao.getStopDetailsWithinSpanFlow(1.1, 2.2, 3.3, 4.4).test {
            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
            awaitComplete()
        }
        assertTrue(flows.isEmpty())
    }

    @Test
    fun getStopDetailsWithinSpanFlowWithServiceFilterRespondsToDatabaseOpenStatus() = runTest {
        val first = listOf(firstStopDetailsWithServices)
        val second = listOf(secondStopDetailsWithServices)
        val flows = ArrayDeque(
            listOf(
                flowOf(first),
                flowOf(second)
            )
        )
        val dao = createProxyStopDao(
            database = FakeBusStopDatabase(
                onStopDao = {
                    FakeStopDao(
                        onGetStopDetailsWithinSpanFlowWithServiceFilter = { minLat, minLon, maxLat,
                                                                            maxLon, serviceFilter ->
                            assertEquals(1.1, minLat)
                            assertEquals(2.2, minLon)
                            assertEquals(3.3, maxLat)
                            assertEquals(4.4, maxLon)
                            assertEquals(
                                setOf(
                                    FakeServiceDescriptor(
                                        serviceName = "1",
                                        operatorCode = "TEST"
                                    )
                                ),
                                serviceFilter
                            )
                            flows.removeFirst()
                        }
                    )
                },
                onIsDatabaseOpenFlow = { intervalFlowOf(0L, 10L, true, false, true) }
            )
        )
        val services = setOf(
            FakeServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST"
            )
        )

        dao.getStopDetailsWithinSpanFlow(1.1, 2.2, 3.3, 4.4, services).test {
            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
            awaitComplete()
        }
        assertTrue(flows.isEmpty())
    }

    @Test
    fun getStopSearchResultsFlowRespondsToDatabaseOpenStatus() = runTest {
        val first = listOf(
            FakeStopSearchResult(
                naptanStopIdentifier = "1".toNaptanStopIdentifier(),
                stopName = FakeStopName(
                    name = "Name 1",
                    locality = "Locality 1"
                ),
                orientation = StopOrientation.NORTH,
                serviceListing = listOf(
                    FakeServiceDescriptor(
                        serviceName = "100",
                        operatorCode = "TEST"
                    ),
                    FakeServiceDescriptor(
                        serviceName = "200",
                        operatorCode = "TEST"
                    )
                )
            )
        )
        val second = listOf(
            FakeStopSearchResult(
                naptanStopIdentifier = "2".toNaptanStopIdentifier(),
                stopName = FakeStopName(
                    name = "Name 2",
                    locality = "Locality 2"
                ),
                orientation = StopOrientation.SOUTH,
                serviceListing = listOf(
                    FakeServiceDescriptor(
                        serviceName = "300",
                        operatorCode = "TEST"
                    ),
                    FakeServiceDescriptor(
                        serviceName = "400",
                        operatorCode = "TEST"
                    )
                )
            )
        )
        val flows = ArrayDeque(
            listOf(
                flowOf(first),
                flowOf(second)
            )
        )
        val dao = createProxyStopDao(
            database = FakeBusStopDatabase(
                onStopDao = {
                    FakeStopDao(
                        onGetStopSearchResultsFlow = {
                            assertEquals("abc123", it)
                            flows.removeFirst()
                        }
                    )
                },
                onIsDatabaseOpenFlow = { intervalFlowOf(0L, 10L, true, false, true) }
            )
        )

        dao.getStopSearchResultsFlow("abc123").test {
            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
            awaitComplete()
        }
        assertTrue(flows.isEmpty())
    }

    private fun createProxyStopDao(
        database: BusStopDatabase = FakeBusStopDatabase()
    ): ProxyStopDao {
        return ProxyStopDao(database = database)
    }

    private val firstStopDetails get() = FakeStopDetails(
        naptanStopIdentifier = "1".toNaptanStopIdentifier(),
        stopName = FakeStopName(
            name = "Name 1",
            locality = "Locality 1"
        ),
        location = FakeStopLocation(
            latitude = 1.1,
            longitude = 2.2
        ),
        orientation = StopOrientation.NORTH
    )

    private val secondStopDetails get() = FakeStopDetails(
        naptanStopIdentifier = "2".toNaptanStopIdentifier(),
        stopName = FakeStopName(
            name = "Name 2",
            locality = "Locality 2"
        ),
        location = FakeStopLocation(
            latitude = 3.3,
            longitude = 4.4
        ),
        orientation = StopOrientation.SOUTH
    )

    private val firstStopDetailsWithServices get() = FakeStopDetailsWithServices(
        naptanStopIdentifier = "1".toNaptanStopIdentifier(),
        stopName = FakeStopName(
            name = "Name 1",
            locality = "Locality 1"
        ),
        location = FakeStopLocation(
            latitude = 1.1,
            longitude = 2.2
        ),
        orientation = StopOrientation.NORTH,
        serviceListing = listOf(
            FakeServiceDescriptor(
                serviceName = "100",
                operatorCode = "TEST"
            ),
            FakeServiceDescriptor(
                serviceName = "200",
                operatorCode = "TEST"
            )
        )
    )

    private val secondStopDetailsWithServices get() = FakeStopDetailsWithServices(
        naptanStopIdentifier = "2".toNaptanStopIdentifier(),
        stopName = FakeStopName(
            name = "Name 2",
            locality = "Locality 2"
        ),
        location = FakeStopLocation(
            latitude = 3.3,
            longitude = 4.4
        ),
        orientation = StopOrientation.SOUTH,
        serviceListing = listOf(
            FakeServiceDescriptor(
                serviceName = "300",
                operatorCode = "TEST"
            ),
            FakeServiceDescriptor(
                serviceName = "400",
                operatorCode = "TEST"
            )
        )
    )
}
