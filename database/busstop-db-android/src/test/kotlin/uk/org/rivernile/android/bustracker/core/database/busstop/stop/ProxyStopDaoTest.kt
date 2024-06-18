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

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.database.busstop.AndroidBusStopDatabase
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [ProxyStopDao].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class ProxyStopDaoTest {

    @Mock
    private lateinit var database: AndroidBusStopDatabase

    @Mock
    private lateinit var roomStopDao: RoomStopDao

    private lateinit var dao: ProxyStopDao

    @BeforeTest
    fun setUp() {
        dao = ProxyStopDao(database)

        whenever(database.roomStopDao)
            .thenReturn(roomStopDao)
    }

    @Test
    fun getNameForStopFlowRespondsToDatabaseOpenStatus() = runTest {
        val first = mock<RoomStopName>()
        val second = mock<RoomStopName>()
        whenever(database.isDatabaseOpenFlow)
            .thenReturn(intervalFlowOf(0L, 10L, true, false, true))
        whenever(roomStopDao.getNameForStopFlow("123456"))
            .thenReturn(
                flowOf(first),
                flowOf(second)
            )

        dao.getNameForStopFlow("123456").test {
            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getLocationForStopFlowRespondsToDatabaseOpenStatus() = runTest {
        val first = mock<RoomStopLocation>()
        val second = mock<RoomStopLocation>()
        whenever(database.isDatabaseOpenFlow)
            .thenReturn(intervalFlowOf(0L, 10L, true, false, true))
        whenever(roomStopDao.getLocationForStopFlow("123456"))
            .thenReturn(
                flowOf(first),
                flowOf(second)
            )

        dao.getLocationForStopFlow("123456").test {
            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getStopDetailsFlowSingleRespondsToDatabaseOpenStatus() = runTest {
        val first = mock<RoomStopDetails>()
        val second = mock<RoomStopDetails>()
        whenever(database.isDatabaseOpenFlow)
            .thenReturn(intervalFlowOf(0L, 10L, true, false, true))
        whenever(roomStopDao.getStopDetailsFlow("123456"))
            .thenReturn(
                flowOf(first),
                flowOf(second)
            )

        dao.getStopDetailsFlow("123456").test {
            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getStopDetailsFlowMultipleRespondsToDatabaseOpenStatus() = runTest {
        val first = mock<Map<String, RoomStopDetails>>()
        val second = mock<Map<String, RoomStopDetails>>()
        whenever(database.isDatabaseOpenFlow)
            .thenReturn(intervalFlowOf(0L, 10L, true, false, true))
        whenever(roomStopDao.getStopDetailsFlow(setOf("123456")))
            .thenReturn(
                flowOf(first),
                flowOf(second)
            )

        dao.getStopDetailsFlow(setOf("123456")).test {
            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getStopDetailsWithServiceFilterFlowRespondsToDatabaseOpenStatus() = runTest {
        val first = mock<List<StopDetails>>()
        val second = mock<List<StopDetails>>()
        whenever(database.isDatabaseOpenFlow)
            .thenReturn(intervalFlowOf(0L, 10L, true, false, true))
        whenever(roomStopDao.getStopDetailsWithServiceFilterFlow(setOf("1")))
            .thenReturn(
                flowOf(first),
                flowOf(second)
            )

        dao.getStopDetailsWithServiceFilterFlow(setOf("1")).test {
            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getStopDetailsWithinSpanFlowRespondsToDatabaseOpenStatus() = runTest {
        val first = mock<List<RoomStopDetailsWithServices>>()
        val second = mock<List<RoomStopDetailsWithServices>>()
        whenever(database.isDatabaseOpenFlow)
            .thenReturn(intervalFlowOf(0L, 10L, true, false, true))
        whenever(roomStopDao.getStopDetailsWithinSpanFlow(
            any(),
            any(),
            any(),
            any()))
            .thenReturn(
                flowOf(first),
                flowOf(second)
            )

        dao.getStopDetailsWithinSpanFlow(1.1, 2.2, 3.3, 4.4).test {
            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getStopDetailsWithinSpanFlowWithServiceFilterRespondsToDatabaseOpenStatus() = runTest {
        val first = mock<List<RoomStopDetailsWithServices>>()
        val second = mock<List<RoomStopDetailsWithServices>>()
        whenever(database.isDatabaseOpenFlow)
            .thenReturn(intervalFlowOf(0L, 10L, true, false, true))
        whenever(roomStopDao.getStopDetailsWithinSpanFlow(
            any(),
            any(),
            any(),
            any(),
            any()))
            .thenReturn(
                flowOf(first),
                flowOf(second)
            )

        dao.getStopDetailsWithinSpanFlow(1.1, 2.2, 3.3, 4.4, setOf("1")).test {
            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getStopSearchResultsFlowRespondsToDatabaseOpenStatus() = runTest {
        val first = mock<List<RoomStopSearchResult>>()
        val second = mock<List<RoomStopSearchResult>>()
        whenever(database.isDatabaseOpenFlow)
            .thenReturn(intervalFlowOf(0L, 10L, true, false, true))
        whenever(roomStopDao.getStopSearchResultsFlow("abc123"))
            .thenReturn(
                flowOf(first),
                flowOf(second)
            )

        dao.getStopSearchResultsFlow("abc123").test {
            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
            awaitComplete()
        }
    }
}