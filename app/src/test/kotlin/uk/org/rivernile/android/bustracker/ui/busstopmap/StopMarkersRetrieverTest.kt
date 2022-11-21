/*
 * Copyright (C) 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.busstopmap

import androidx.lifecycle.SavedStateHandle
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopDetails
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopName
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [StopMarkersRetriever].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class StopMarkersRetrieverTest {

    companion object {

        private const val STATE_SELECTED_SERVICES = "selectedServices"
        private const val STATE_SELECTED_STOP_CODE = "selectedStopCode"
    }

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var busStopsRepository: BusStopsRepository
    @Mock
    private lateinit var serviceListingRetriever: ServiceListingRetriever

    private val stopDetails1 = StopDetails(
            "123456",
            StopName(
                    "Stop name 1",
                    "Locality 1"),
            1.1,
            2.1,
            1)
    private val stopDetails2 = StopDetails(
            "987654",
            StopName(
                    "Stop name 2",
                    "Locality 2"),
            1.2,
            2.2,
            2)
    private val stopDetails3 = StopDetails(
            "246802",
            StopName(
                    "Stop name 3",
                    "Locality 4"),
            1.3,
            2.3,
            3)

    @Test
    fun stopMarkersFlowEmitsNullWhenStopsAreNull() = runTest {
        whenever(busStopsRepository.getStopDetailsWithServiceFilterFlow(null))
                .thenReturn(flowOf(null))
        whenever(serviceListingRetriever.getServiceListingFlow(null))
                .thenReturn(flowOf(null))
        val retriever = createRetriever()

        val observer = retriever.stopMarkersFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(null)
    }

    @Test
    fun stopMarkersFlowEmitsNullWhenStopsAreEmpty() = runTest {
        whenever(busStopsRepository.getStopDetailsWithServiceFilterFlow(null))
                .thenReturn(flowOf(emptyList()))
        whenever(serviceListingRetriever.getServiceListingFlow(null))
                .thenReturn(flowOf(null))
        val retriever = createRetriever()

        val observer = retriever.stopMarkersFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(null)
    }

    @Test
    fun stopMarkersFlowEmitsStopsFromBusStopsRepository() = runTest {
        whenever(busStopsRepository.getStopDetailsWithServiceFilterFlow(null))
                .thenReturn(flowOf(
                        listOf(stopDetails1, stopDetails2, stopDetails3)))
        whenever(serviceListingRetriever.getServiceListingFlow(null))
                .thenReturn(flowOf(null))
        val retriever = createRetriever()
        val expected1 = UiStopMarker(
                "123456",
                StopName(
                        "Stop name 1",
                        "Locality 1"),
                LatLng(1.1, 2.1),
                1,
                null)
        val expected2 = UiStopMarker(
                "987654",
                StopName(
                        "Stop name 2",
                        "Locality 2"),
                LatLng(1.2, 2.2),
                2,
                null)
        val expected3 = UiStopMarker(
                "246802",
                StopName(
                        "Stop name 3",
                        "Locality 3"),
                LatLng(1.3, 2.3),
                3,
                null)

        val observer = retriever.stopMarkersFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                listOf(expected1, expected2, expected3))
    }

    @Test
    fun stopMarkersEmitsStopsWithFilteredServicesFromState() = runTest {
        whenever(busStopsRepository.getStopDetailsWithServiceFilterFlow(setOf("1", "2", "3")))
                .thenReturn(flowOf(
                        listOf(stopDetails1)))
        whenever(serviceListingRetriever.getServiceListingFlow(null))
                .thenReturn(flowOf(null))
        val retriever = createRetriever(
                SavedStateHandle(
                        mapOf(STATE_SELECTED_SERVICES to arrayOf("1", "2", "3"))))
        val expected1 = UiStopMarker(
                "123456",
                StopName(
                        "Stop name 1",
                        "Locality 1"),
                LatLng(1.1, 2.1),
                1,
                null)

        val observer = retriever.stopMarkersFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                listOf(expected1))
    }

    @Test
    fun stopMarkersFlowEmitsStopsWithFilteredServices() = runTest {
        whenever(busStopsRepository.getStopDetailsWithServiceFilterFlow(null))
                .thenReturn(flowOf(
                        listOf(stopDetails1, stopDetails2, stopDetails3)))
        whenever(busStopsRepository.getStopDetailsWithServiceFilterFlow(setOf("1", "2", "3")))
                .thenReturn(flowOf(
                        listOf(stopDetails1)))
        whenever(serviceListingRetriever.getServiceListingFlow(null))
                .thenReturn(flowOf(null))
        val savedState = SavedStateHandle()
        val retriever = createRetriever(savedState)
        val expected1 = UiStopMarker(
                "123456",
                StopName(
                        "Stop name 1",
                        "Locality 1"),
                LatLng(1.1, 2.1),
                1,
                null)
        val expected2 = UiStopMarker(
                "987654",
                StopName(
                        "Stop name 2",
                        "Locality 2"),
                LatLng(1.2, 2.2),
                2,
                null)
        val expected3 = UiStopMarker(
                "246802",
                StopName(
                        "Stop name 3",
                        "Locality 3"),
                LatLng(1.3, 2.3),
                3,
                null)

        val observer = retriever.stopMarkersFlow.test(this)
        advanceUntilIdle()
        savedState[STATE_SELECTED_SERVICES] = arrayOf("1", "2", "3")
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                listOf(expected1, expected2, expected3),
                listOf(expected1))
    }

    @Test
    fun stopMarkersFlowDoesNotApplyServiceListingToNonMatchingStopCodes() = runTest {
        whenever(busStopsRepository.getStopDetailsWithServiceFilterFlow(null))
                .thenReturn(flowOf(
                        listOf(stopDetails1)))
        whenever(serviceListingRetriever.getServiceListingFlow(null))
                .thenReturn(intervalFlowOf(
                        0L,
                        10L,
                        UiServiceListing.InProgress("192837"),
                        UiServiceListing.Empty("192837"),
                        UiServiceListing.Success("192837", listOf("1", "2", "3"))))
        val retriever = createRetriever()
        val expected = UiStopMarker(
                "123456",
                StopName(
                        "Stop name 1",
                        "Locality 1"),
                LatLng(1.1, 2.1),
                1,
                null)

        val observer = retriever.stopMarkersFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                listOf(expected),
                listOf(expected),
                listOf(expected))
    }

    @Test
    fun stopMarkersFlowAppliesServiceListingToMatchingStopCodesFromState() = runTest {
        whenever(busStopsRepository.getStopDetailsWithServiceFilterFlow(null))
                .thenReturn(flowOf(
                        listOf(stopDetails1)))
        whenever(serviceListingRetriever.getServiceListingFlow("123456"))
                .thenReturn(intervalFlowOf(
                        0L,
                        10L,
                        UiServiceListing.InProgress("123456"),
                        UiServiceListing.Empty("123456"),
                        UiServiceListing.Success("123456", listOf("1", "2", "3"))))
        val retriever = createRetriever(
                SavedStateHandle(
                        mapOf(STATE_SELECTED_STOP_CODE to "123456")))
        val expected1 = UiStopMarker(
                "123456",
                StopName(
                        "Stop name 1",
                        "Locality 1"),
                LatLng(1.1, 2.1),
                1,
                UiServiceListing.InProgress("123456"))
        val expected2 = UiStopMarker(
                "123456",
                StopName(
                        "Stop name 1",
                        "Locality 1"),
                LatLng(1.1, 2.1),
                1,
                UiServiceListing.Empty("123456"))
        val expected3 = UiStopMarker(
                "123456",
                StopName(
                        "Stop name 1",
                        "Locality 1"),
                LatLng(1.1, 2.1),
                1,
                UiServiceListing.Success("123456", listOf("1", "2", "3")))

        val observer = retriever.stopMarkersFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                listOf(expected1),
                listOf(expected2),
                listOf(expected3))
    }

    @Test
    fun stopMarkersFlowAppliesServiceListingToMatchingStopCodes() = runTest {
        whenever(busStopsRepository.getStopDetailsWithServiceFilterFlow(null))
                .thenReturn(flowOf(
                        listOf(stopDetails1)))
        whenever(serviceListingRetriever.getServiceListingFlow(null))
                .thenReturn(flowOf(null))
        whenever(serviceListingRetriever.getServiceListingFlow("123456"))
                .thenReturn(intervalFlowOf(
                        0L,
                        10L,
                        UiServiceListing.InProgress("123456"),
                        UiServiceListing.Empty("123456"),
                        UiServiceListing.Success("123456", listOf("1", "2", "3"))))
        val savedState = SavedStateHandle()
        val retriever = createRetriever(savedState)
        val expected1 = UiStopMarker(
                "123456",
                StopName(
                        "Stop name 1",
                        "Locality 1"),
                LatLng(1.1, 2.1),
                1,
                null)
        val expected2 = UiStopMarker(
                "123456",
                StopName(
                        "Stop name 1",
                        "Locality 1"),
                LatLng(1.1, 2.1),
                1,
                UiServiceListing.InProgress("123456"))
        val expected3 = UiStopMarker(
                "123456",
                StopName(
                        "Stop name 1",
                        "Locality 1"),
                LatLng(1.1, 2.1),
                1,
                UiServiceListing.Empty("123456"))
        val expected4 = UiStopMarker(
                "123456",
                StopName(
                        "Stop name 1",
                        "Locality 1"),
                LatLng(1.1, 2.1),
                1,
                UiServiceListing.Success("123456", listOf("1", "2", "3")))

        val observer = retriever.stopMarkersFlow.test(this)
        advanceUntilIdle()
        savedState[STATE_SELECTED_STOP_CODE] = "123456"
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                listOf(expected1),
                listOf(expected2),
                listOf(expected3),
                listOf(expected4))
    }

    private fun createRetriever(savedStateHandle: SavedStateHandle = SavedStateHandle()) =
            StopMarkersRetriever(
                    savedStateHandle,
                    busStopsRepository,
                    serviceListingRetriever)
}