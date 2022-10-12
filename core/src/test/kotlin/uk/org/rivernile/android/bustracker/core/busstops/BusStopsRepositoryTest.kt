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

package uk.org.rivernile.android.bustracker.core.busstops

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.database.busstop.daos.BusStopsDao
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopDetails
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopDetailsWithServices
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopName
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopSearchResult
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [BusStopsRepository].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class BusStopsRepositoryTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var busStopsDao: BusStopsDao

    private lateinit var repository: BusStopsRepository

    @Before
    fun setUp() {
        repository = BusStopsRepository(busStopsDao)
    }

    @Test
    fun getNameForStopFlowGetsInitialValue() = runTest {
        val expected = createStopName()
        whenever(busStopsDao.getNameForStop("123456"))
                .thenReturn(expected)

        val observer = repository.getNameForStopFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()
        advanceUntilIdle()

        observer.assertValues(expected)
        verify(busStopsDao)
                .removeOnBusStopsChangedListener(any())
    }

    @Test
    fun getNameForStopFlowRespondsToBusStopsChanged() = runTest {
        doAnswer {
            val listener = it.getArgument<BusStopsDao.OnBusStopsChangedListener>(0)
            listener.onBusStopsChanged()
            listener.onBusStopsChanged()
        }.whenever(busStopsDao).addOnBusStopsChangedListener(any())
        val expected1 = createStopName()
        val expected3 = expected1.copy(locality = null)
        whenever(busStopsDao.getNameForStop("123456"))
                .thenReturn(expected1, null, expected3)

        val observer = repository.getNameForStopFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()
        advanceUntilIdle()

        observer.assertValues(expected1, null, expected3)
        verify(busStopsDao)
                .removeOnBusStopsChangedListener(any())
    }

    @Test
    fun getBusStopDetailsFlowGetsInitialValue() = runTest {
        val expected = createStopDetails()
        whenever(busStopsDao.getStopDetails("123456"))
                .thenReturn(expected)

        val observer = repository.getBusStopDetailsFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()
        advanceUntilIdle()

        observer.assertValues(expected)
        verify(busStopsDao)
                .removeOnBusStopsChangedListener(any())
    }

    @Test
    fun getBusStopDetailsFlowRespondsToBusStopsChanged() = runTest {
        doAnswer {
            val listener = it.getArgument<BusStopsDao.OnBusStopsChangedListener>(0)
            listener.onBusStopsChanged()
            listener.onBusStopsChanged()
        }.whenever(busStopsDao).addOnBusStopsChangedListener(any())
        val expected1 = createStopDetails()
        val expected3 = expected1.copy(stopName = StopName("Stop name", null))
        whenever(busStopsDao.getStopDetails("123456"))
                .thenReturn(expected1, null, expected3)

        val observer = repository.getBusStopDetailsFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()
        advanceUntilIdle()

        observer.assertValues(expected1, null, expected3)
        verify(busStopsDao)
                .removeOnBusStopsChangedListener(any())
    }

    @Test
    fun getBusStopDetailsFlowMultiGetsInitialValue() = runTest {
        val expected = mapOf("123456" to StopDetails(
                "123456",
                StopName(
                        "Stop name 1",
                        "Locality 1"),
                1.2,
                3.4,
                4))
        whenever(busStopsDao.getStopDetails(setOf("123456", "123457")))
                .thenReturn(expected)

        val observer = repository.getBusStopDetailsFlow(setOf("123456", "123457")).test(this)
        advanceUntilIdle()
        observer.finish()
        advanceUntilIdle()

        observer.assertValues(expected)
        verify(busStopsDao)
                .removeOnBusStopsChangedListener(any())
    }

    @Test
    fun getBusStopDetailsFlowMultiRespondsToBusStopsChanged() = runTest {
        doAnswer {
            val listener = it.getArgument<BusStopsDao.OnBusStopsChangedListener>(0)
            listener.onBusStopsChanged()
            listener.onBusStopsChanged()
        }.whenever(busStopsDao).addOnBusStopsChangedListener(any())
        val expected1 = mapOf("123456" to StopDetails(
                "123456",
                StopName(
                        "Stop name 1",
                        "Locality 1"),
                1.2,
                3.4,
                4))
        val expected3 = mapOf(
                "123456" to StopDetails(
                        "123456",
                        StopName(
                                "Stop name 1",
                                "Locality 1"),
                        1.2,
                        3.4,
                        4),
                "123457" to StopDetails(
                        "123457",
                        StopName(
                                "Stop name 2",
                                "Locality 2"),
                        1.3,
                        3.5,
                        5))
        whenever(busStopsDao.getStopDetails(setOf("123456", "123457")))
                .thenReturn(expected1, null, expected3)

        val observer = repository.getBusStopDetailsFlow(setOf("123456", "123457")).test(this)
        advanceUntilIdle()
        observer.finish()
        advanceUntilIdle()

        observer.assertValues(expected1, null, expected3)
        verify(busStopsDao)
                .removeOnBusStopsChangedListener(any())
    }

    @Test
    fun getStopDetailsWithinSpanFlowDoesNotUseServiceFilterWhenItIsNull() {
        val stopDetailsFlow = mock<Flow<List<StopDetailsWithServices>?>>()
        whenever(busStopsDao.getStopDetailsWithinSpanFlow(
                any(),
                any(),
                any(),
                any()))
                .thenReturn(stopDetailsFlow)

        val result = repository.getStopDetailsWithinSpanFlow(1.0, 2.0, 3.0, 4.0, null)

        assertNotNull(result)
        verify(busStopsDao, never())
                .getStopDetailsWithinSpanFlow(any(), any(), any(), any(), any())
    }

    @Test
    fun getStopDetailsWithinSpanFlowDoesNotUseServiceFilterWhenItIsEmpty() {
        val stopDetailsFlow = mock<Flow<List<StopDetailsWithServices>?>>()
        whenever(busStopsDao.getStopDetailsWithinSpanFlow(
                any(),
                any(),
                any(),
                any()))
                .thenReturn(stopDetailsFlow)

        val result = repository.getStopDetailsWithinSpanFlow(1.0, 2.0, 3.0, 4.0, emptyList())

        assertNotNull(result)
        verify(busStopsDao, never())
                .getStopDetailsWithinSpanFlow(any(), any(), any(), any(), any())
    }

    @Test
    fun getStopDetailsWithinSpanFlowUsesServiceFilterWithItIsPopulated() {
        val stopDetailsFlow = mock<Flow<List<StopDetailsWithServices>?>>()
        whenever(busStopsDao.getStopDetailsWithinSpanFlow(
                any(),
                any(),
                any(),
                any(),
                any()))
                .thenReturn(stopDetailsFlow)

        val result = repository.getStopDetailsWithinSpanFlow(1.0, 2.0, 3.0, 4.0, listOf("1", "2"))

        assertNotNull(result)
        verify(busStopsDao, never())
                .getStopDetailsWithinSpanFlow(any(), any(), any(), any())
    }

    @Test
    fun getStopDetailsWithServiceFilterFlowUsesFlowFromDao() {
        val stopDetailsFlow = mock<Flow<List<StopDetails>?>>()
        whenever(busStopsDao.getStopDetailsWithServiceFilterFlow(setOf("1", "2", "3")))
                .thenReturn(stopDetailsFlow)

        val result = repository.getStopDetailsWithServiceFilterFlow(setOf("1", "2", "3"))
        assertNotNull(result)
    }

    @Test
    fun getStopSearchResultsFlowReturnsFlowFromBusStopsDao() {
        val stopSearchResultsFlow = mock<Flow<List<StopSearchResult>?>>()
        whenever(busStopsDao.getStopSearchResultsFlow("123456"))
                .thenReturn(stopSearchResultsFlow)

        val result = repository.getStopSearchResultsFlow("123456")

        assertSame(stopSearchResultsFlow, result)
    }

    private fun createStopName() = StopName("Name", "Locality")

    private fun createStopDetails() =
            StopDetails(
                    "123456",
                    StopName(
                            "Stop name",
                            "Locality"),
                    1.2,
                    3.4,
                    5)
}