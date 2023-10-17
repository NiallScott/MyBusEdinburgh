/*
 * Copyright (C) 2023 Niall 'Rivernile' Scott
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.database.busstop.AndroidBusStopDatabase
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [ProxyStopDao].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class ProxyStopDaoTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var database: AndroidBusStopDatabase

    @Mock
    private lateinit var roomStopDao: RoomStopDao

    private lateinit var dao: ProxyStopDao

    @Before
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
                flowOf(second))

        val observer = dao.getNameForStopFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(first, second)
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
                flowOf(second))

        val observer = dao.getLocationForStopFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(first, second)
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
                flowOf(second))

        val observer = dao.getStopDetailsFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(first, second)
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
                flowOf(second))

        val observer = dao.getStopDetailsFlow(setOf("123456")).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(first, second)
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
                flowOf(second))

        val observer = dao.getStopDetailsWithServiceFilterFlow(setOf("1")).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(first, second)
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
                flowOf(second))

        val observer = dao.getStopDetailsWithinSpanFlow(1.1, 2.2, 3.3, 4.4).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(first, second)
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
                flowOf(second))

        val observer = dao.getStopDetailsWithinSpanFlow(1.1, 2.2, 3.3, 4.4, setOf("1")).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(first, second)
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
                flowOf(second))

        val observer = dao.getStopSearchResultsFlow("abc123").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(first, second)
    }
}