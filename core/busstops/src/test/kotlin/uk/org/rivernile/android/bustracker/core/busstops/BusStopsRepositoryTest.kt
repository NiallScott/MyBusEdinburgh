/*
 * Copyright (C) 2020 - 2023 Niall 'Rivernile' Scott
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
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopDao
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopDetails
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopDetailsWithServices
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopLocation
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopName
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopSearchResult
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
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
    private lateinit var stopsDao: StopDao

    private lateinit var repository: BusStopsRepository

    @Before
    fun setUp() {
        repository = BusStopsRepository(stopsDao)
    }

    @Test
    fun getNameForStopFlowEmitsItems() = runTest {
        val stopName1 = mock<StopName>()
        val stopName2 = mock<StopName>()
        whenever(stopsDao.getNameForStopFlow("123456"))
            .thenReturn(intervalFlowOf(
                0L,
                10L,
                null,
                stopName1,
                stopName2))

        val observer = repository.getNameForStopFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            null,
            stopName1,
            stopName2)
    }

    @Test
    fun getBusStopDetailsFlowEmitsItems() = runTest {
        val stopDetails1 = mock<StopDetails>()
        val stopDetails2 = mock<StopDetails>()
        whenever(stopsDao.getStopDetailsFlow("123456"))
            .thenReturn(intervalFlowOf(
                0L,
                10L,
                null,
                stopDetails1,
                stopDetails2))

        val observer = repository.getBusStopDetailsFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            null,
            stopDetails1,
            stopDetails2)
    }

    @Test
    fun getBusStopDetailsFlowWithStopCodeSetEmitsItems() = runTest {
        val stopDetails1 = mapOf<String, StopDetails>()
        val stopDetails2 = mapOf<String, StopDetails>()
        whenever(stopsDao.getStopDetailsFlow(setOf("123456", "987654")))
            .thenReturn(intervalFlowOf(
                0L,
                10L,
                null,
                stopDetails1,
                stopDetails2))

        val observer = repository.getBusStopDetailsFlow(setOf("123456", "987654")).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            null,
            stopDetails1,
            stopDetails2)
    }

    @Test
    fun getStopDetailsWithinSpanFlowWithNullServiceFilterEmitsItems() = runTest {
        val stopDetails1 = mock<StopDetailsWithServices>()
        val stopDetails2 = mock<StopDetailsWithServices>()
        whenever(stopsDao.getStopDetailsWithinSpanFlow(1.1, 2.2, 3.3, 4.4))
            .thenReturn(intervalFlowOf(
                0L,
                10L,
                null,
                listOf(stopDetails1),
                listOf(stopDetails1, stopDetails2)))

        val observer = repository.getStopDetailsWithinSpanFlow(1.1, 2.2, 3.3, 4.4, null).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            null,
            listOf(stopDetails1),
            listOf(stopDetails1, stopDetails2))
        verify(stopsDao, never())
            .getStopDetailsWithinSpanFlow(any(), any(), any(), any(), any())
    }

    @Test
    fun getStopDetailsWithinSpanFlowWithEmptyServiceFilterEmitsItems() = runTest {
        val stopDetails1 = mock<StopDetailsWithServices>()
        val stopDetails2 = mock<StopDetailsWithServices>()
        whenever(stopsDao.getStopDetailsWithinSpanFlow(1.1, 2.2, 3.3, 4.4))
            .thenReturn(intervalFlowOf(
                0L,
                10L,
                null,
                listOf(stopDetails1),
                listOf(stopDetails1, stopDetails2)))

        val observer = repository.getStopDetailsWithinSpanFlow(1.1, 2.2, 3.3, 4.4, emptySet())
            .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            null,
            listOf(stopDetails1),
            listOf(stopDetails1, stopDetails2))
        verify(stopsDao, never())
            .getStopDetailsWithinSpanFlow(any(), any(), any(), any(), any())
    }

    @Test
    fun getStopDetailsWithinSpanFlowWithServiceFilterEmitsItems() = runTest {
        val stopDetails1 = mock<StopDetailsWithServices>()
        val stopDetails2 = mock<StopDetailsWithServices>()
        whenever(stopsDao.getStopDetailsWithinSpanFlow(1.1, 2.2, 3.3, 4.4, setOf("1", "2", "3")))
            .thenReturn(intervalFlowOf(
                0L,
                10L,
                null,
                listOf(stopDetails1),
                listOf(stopDetails1, stopDetails2)))

        val observer = repository.getStopDetailsWithinSpanFlow(
            1.1,
            2.2,
            3.3,
            4.4,
            setOf("1", "2", "3"))
            .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            null,
            listOf(stopDetails1),
            listOf(stopDetails1, stopDetails2))
        verify(stopsDao, never())
            .getStopDetailsWithinSpanFlow(any(), any(), any(), any())
    }

    @Test
    fun getStopDetailsWithServiceFilterFlowEmitsItems() = runTest {
        val stopDetails1 = mock<StopDetails>()
        val stopDetails2 = mock<StopDetails>()
        whenever(stopsDao.getStopDetailsWithServiceFilterFlow(setOf("1", "2", "3")))
            .thenReturn(intervalFlowOf(
                0L,
                10L,
                null,
                listOf(stopDetails1),
                listOf(stopDetails1, stopDetails2)))

        val observer = repository.getStopDetailsWithServiceFilterFlow(setOf("1", "2", "3"))
            .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            null,
            listOf(stopDetails1),
            listOf(stopDetails1, stopDetails2))
    }

    @Test
    fun getStopSearchResultsFlowEmitsItems() = runTest {
        val searchResult1 = mock<StopSearchResult>()
        val searchResult2 = mock<StopSearchResult>()
        whenever(stopsDao.getStopSearchResultsFlow("search term"))
            .thenReturn(intervalFlowOf(
                0L,
                10L,
                null,
                listOf(searchResult1),
                listOf(searchResult1, searchResult2)))

        val observer = repository.getStopSearchResultsFlow("search term").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            null,
            listOf(searchResult1),
            listOf(searchResult1, searchResult2))
    }

    @Test
    fun getStopLocationReturnsStopLocation() = runTest {
        val stopLocation = mock<StopLocation>()
        whenever(stopsDao.getLocationForStopFlow("123456"))
            .thenReturn(intervalFlowOf(10L, 10L, stopLocation))

        val result = repository.getStopLocation("123456")

        assertEquals(stopLocation, result)
    }

    @Test
    fun getNameForStopReturnsStopName() = runTest {
        val stopName = mock<StopName>()
        whenever(stopsDao.getNameForStopFlow("123456"))
            .thenReturn(intervalFlowOf(10L, 10L, stopName))

        val result = repository.getNameForStop("123456")

        assertEquals(stopName, result)
    }
}