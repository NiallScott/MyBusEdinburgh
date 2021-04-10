/*
 * Copyright (C) 2021 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.alerts

import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopDetails
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopName
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ArrivalAlert
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ProximityAlert
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [AlertsRetriever].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AlertsRetrieverTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var alertsRepository: AlertsRepository
    @Mock
    private lateinit var busStopsRepository: BusStopsRepository

    private lateinit var alertsRetriever: AlertsRetriever

    @Before
    fun setUp() {
        alertsRetriever = AlertsRetriever(alertsRepository, busStopsRepository)
    }

    @Test
    fun allAlertsFlowEmitsEmptyListWhenUpstreamEmitsNull() = runBlockingTest {
        whenever(alertsRepository.getAllAlertsFlow())
                .thenReturn(flowOf(null))

        val observer = alertsRetriever.allAlertsFlow.test(this)
        observer.finish()

        observer.assertValues(null, emptyList())
    }

    @Test
    fun allAlertsFlowEmitsEmptyListWhenUpstreamEmitsEmptyList() = runBlockingTest {
        whenever(alertsRepository.getAllAlertsFlow())
                .thenReturn(flowOf(emptyList()))

        val observer = alertsRetriever.allAlertsFlow.test(this)
        observer.finish()

        observer.assertValues(null, emptyList())
    }

    @Test
    fun allAlertsFlowWithArrivalAlertAndNullStopDetailsEmitsAlertWithoutStopDetails() =
            runBlockingTest {
        val arrivalAlert = ArrivalAlert(1, 123L, "123456", listOf("1"), 5)
        whenever(alertsRepository.getAllAlertsFlow())
                .thenReturn(flowOf(listOf(arrivalAlert)))
        whenever(busStopsRepository.getBusStopDetailsFlow(setOf("123456")))
                .thenReturn(flowOf(null))
        val expected = listOf(
                UiAlert.ArrivalAlert(1, "123456", null, listOf("1"), 5))

        val observer = alertsRetriever.allAlertsFlow.test(this)
        observer.finish()

        observer.assertValues(null, expected)
    }

    @Test
    fun allAlertsFlowWithArrivalAlertAndEmptyStopDetailsEmitsAlertWithoutStopDetails() =
            runBlockingTest {
        val arrivalAlert = ArrivalAlert(1, 123L, "123456", listOf("1"), 5)
        whenever(alertsRepository.getAllAlertsFlow())
                .thenReturn(flowOf(listOf(arrivalAlert)))
        whenever(busStopsRepository.getBusStopDetailsFlow(setOf("123456")))
                .thenReturn(flowOf(emptyMap()))
        val expected = listOf(
                UiAlert.ArrivalAlert(1, "123456", null, listOf("1"), 5))

        val observer = alertsRetriever.allAlertsFlow.test(this)
        observer.finish()

        observer.assertValues(null, expected)
    }

    @Test
    fun allAlertsWithArrivalAlertAndStopDetailsEmitsFullObject() = runBlockingTest {
        val arrivalAlert = ArrivalAlert(1, 123L, "123456", listOf("1"), 5)
        val stopDetails = StopDetails(
                "123456",
                StopName(
                        "Stop name",
                        "Locality"),
                1.2,
                3.4,
                5)
        whenever(alertsRepository.getAllAlertsFlow())
                .thenReturn(flowOf(listOf(arrivalAlert)))
        whenever(busStopsRepository.getBusStopDetailsFlow(setOf("123456")))
                .thenReturn(flowOf(mapOf("123456" to stopDetails)))
        val expected = listOf(
                UiAlert.ArrivalAlert(1, "123456", stopDetails, listOf("1"), 5))

        val observer = alertsRetriever.allAlertsFlow.test(this)
        observer.finish()

        observer.assertValues(null, expected)
    }

    @Test
    fun allAlertsWithArrivalAlertsEmitsUpdatedArrivalAlertDetails() = runBlockingTest {
        val arrivalAlert1 = ArrivalAlert(1, 123L, "123456", listOf("1"), 5)
        val arrivalAlert2 = ArrivalAlert(1, 123L, "123456", listOf("1"), 10)
        val stopDetails = StopDetails(
                "123456",
                StopName(
                        "Stop name",
                        "Locality"),
                1.2,
                3.4,
                5)
        whenever(alertsRepository.getAllAlertsFlow())
                .thenReturn(flowOf(listOf(arrivalAlert1), listOf(arrivalAlert2)))
        whenever(busStopsRepository.getBusStopDetailsFlow(setOf("123456")))
                .thenReturn(flowOf(mapOf("123456" to stopDetails)))
        val expected1 = listOf(
                UiAlert.ArrivalAlert(1, "123456", stopDetails, listOf("1"), 5))
        val expected2 = listOf(
                UiAlert.ArrivalAlert(1, "123456", stopDetails, listOf("1"), 10))


        val observer = alertsRetriever.allAlertsFlow.test(this)
        observer.finish()

        observer.assertValues(null, expected1, expected2)
    }

    @Test
    fun allAlertsWithArrivalAlertEmitsUpdatedStopDetails() = runBlockingTest {
        val arrivalAlert = ArrivalAlert(1, 123L, "123456", listOf("1"), 5)
        val stopDetails1 = StopDetails(
                "123456",
                StopName(
                        "Stop name",
                        "Locality"),
                1.2,
                3.4,
                5)
        val stopDetails2 = StopDetails(
                "123456",
                StopName(
                        "New stop name",
                        "New locality"),
                1.2,
                3.4,
                5)
        whenever(alertsRepository.getAllAlertsFlow())
                .thenReturn(flowOf(listOf(arrivalAlert)))
        whenever(busStopsRepository.getBusStopDetailsFlow(setOf("123456")))
                .thenReturn(
                        flowOf(mapOf("123456" to stopDetails1), mapOf("123456" to stopDetails2)))
        val expected1 = listOf(
                UiAlert.ArrivalAlert(1, "123456", stopDetails1, listOf("1"), 5))
        val expected2 = listOf(
                UiAlert.ArrivalAlert(1, "123456", stopDetails2, listOf("1"), 5))

        val observer = alertsRetriever.allAlertsFlow.test(this)
        observer.finish()

        observer.assertValues(null, expected1, expected2)
    }

    @Test
    fun allAlertsFlowWithProximityAlertAndNullStopDetailsEmitsAlertWithoutStopDetails() =
            runBlockingTest {
        val proximityAlert = ProximityAlert(1, 123L, "123456", 250)
        whenever(alertsRepository.getAllAlertsFlow())
                .thenReturn(flowOf(listOf(proximityAlert)))
        whenever(busStopsRepository.getBusStopDetailsFlow(setOf("123456")))
                .thenReturn(flowOf(null))
        val expected = listOf(
                UiAlert.ProximityAlert(1, "123456", null, 250))

        val observer = alertsRetriever.allAlertsFlow.test(this)
        observer.finish()

        observer.assertValues(null, expected)
    }

    @Test
    fun allAlertsFlowWithProximityAlertAndEmptyStopDetailsEmitsAlertWithoutStopDetails() =
            runBlockingTest {
        val proximityAlert = ProximityAlert(1, 123L, "123456", 250)
        whenever(alertsRepository.getAllAlertsFlow())
                .thenReturn(flowOf(listOf(proximityAlert)))
        whenever(busStopsRepository.getBusStopDetailsFlow(setOf("123456")))
                .thenReturn(flowOf(emptyMap()))
        val expected = listOf(
                UiAlert.ProximityAlert(1, "123456", null, 250))

        val observer = alertsRetriever.allAlertsFlow.test(this)
        observer.finish()

        observer.assertValues(null, expected)
    }

    @Test
    fun allAlertsWithProximityAlertAndStopDetailsEmitsFullObject() = runBlockingTest {
        val proximityAlert = ProximityAlert(1, 123L, "123456", 250)
        val stopDetails = StopDetails(
                "123456",
                StopName(
                        "Stop name",
                        "Locality"),
                1.2,
                3.4,
                5)
        whenever(alertsRepository.getAllAlertsFlow())
                .thenReturn(flowOf(listOf(proximityAlert)))
        whenever(busStopsRepository.getBusStopDetailsFlow(setOf("123456")))
                .thenReturn(flowOf(mapOf("123456" to stopDetails)))
        val expected = listOf(
                UiAlert.ProximityAlert(1, "123456", stopDetails, 250))

        val observer = alertsRetriever.allAlertsFlow.test(this)
        observer.finish()

        observer.assertValues(null, expected)
    }

    @Test
    fun allAlertsWithProximityAlertsEmitsUpdatedProximityAlertDetails() = runBlockingTest {
        val proximityAlert1 = ProximityAlert(1, 123L, "123456", 250)
        val proximityAlert2 = ProximityAlert(1, 123L, "123456", 500)
        val stopDetails = StopDetails(
                "123456",
                StopName(
                        "Stop name",
                        "Locality"),
                1.2,
                3.4,
                5)
        whenever(alertsRepository.getAllAlertsFlow())
                .thenReturn(flowOf(listOf(proximityAlert1), listOf(proximityAlert2)))
        whenever(busStopsRepository.getBusStopDetailsFlow(setOf("123456")))
                .thenReturn(flowOf(mapOf("123456" to stopDetails)))
        val expected1 = listOf(
                UiAlert.ProximityAlert(1, "123456", stopDetails, 250))
        val expected2 = listOf(
                UiAlert.ProximityAlert(1, "123456", stopDetails, 500))


        val observer = alertsRetriever.allAlertsFlow.test(this)
        observer.finish()

        observer.assertValues(null, expected1, expected2)
    }

    @Test
    fun allAlertsWithProximityAlertEmitsUpdatedStopDetails() = runBlockingTest {
        val proximityAlert = ProximityAlert(1, 123L, "123456", 250)
        val stopDetails1 = StopDetails(
                "123456",
                StopName(
                        "Stop name",
                        "Locality"),
                1.2,
                3.4,
                5)
        val stopDetails2 = StopDetails(
                "123456",
                StopName(
                        "New stop name",
                        "New locality"),
                1.2,
                3.4,
                5)
        whenever(alertsRepository.getAllAlertsFlow())
                .thenReturn(flowOf(listOf(proximityAlert)))
        whenever(busStopsRepository.getBusStopDetailsFlow(setOf("123456")))
                .thenReturn(
                        flowOf(mapOf("123456" to stopDetails1), mapOf("123456" to stopDetails2)))
        val expected1 = listOf(
                UiAlert.ProximityAlert(1, "123456", stopDetails1, 250))
        val expected2 = listOf(
                UiAlert.ProximityAlert(1, "123456", stopDetails2, 250))

        val observer = alertsRetriever.allAlertsFlow.test(this)
        observer.finish()

        observer.assertValues(null, expected1, expected2)
    }

    @Test
    fun allAlertsPropagatesStopDetailsUpdateToAllRelevantStops() = runBlockingTest {
        val arrivalAlert = ArrivalAlert(1, 123L, "123456", listOf("1"), 5)
        val proximityAlert = ProximityAlert(2, 124L, "123456", 250)
        val stopDetails1 = StopDetails(
                "123456",
                StopName(
                        "Stop name",
                        "Locality"),
                1.2,
                3.4,
                5)
        val stopDetails2 = StopDetails(
                "123456",
                StopName(
                        "New stop name",
                        "New locality"),
                1.2,
                3.4,
                5)
        whenever(alertsRepository.getAllAlertsFlow())
                .thenReturn(flowOf(listOf(arrivalAlert, proximityAlert)))
        whenever(busStopsRepository.getBusStopDetailsFlow(setOf("123456")))
                .thenReturn(
                        flowOf(mapOf("123456" to stopDetails1), mapOf("123456" to stopDetails2)))
        val expected1 = listOf(
                UiAlert.ArrivalAlert(1, "123456", stopDetails1, listOf("1"), 5),
                UiAlert.ProximityAlert(2, "123456", stopDetails1, 250))
        val expected2 = listOf(
                UiAlert.ArrivalAlert(1, "123456", stopDetails2, listOf("1"), 5),
                UiAlert.ProximityAlert(2, "123456", stopDetails2, 250))

        val observer = alertsRetriever.allAlertsFlow.test(this)
        observer.finish()

        observer.assertValues(null, expected1, expected2)
    }

    @Test
    fun allAlertsPropagatesStopDetailsUpdateOnlyToRelevantStops() = runBlockingTest {
        val arrivalAlert = ArrivalAlert(1, 123L, "123456", listOf("1"), 5)
        val proximityAlert = ProximityAlert(2, 124L, "987654", 250)
        val stopDetails1 = StopDetails(
                "123456",
                StopName(
                        "Stop name",
                        "Locality"),
                1.2,
                3.4,
                5)
        val stopDetails2 = StopDetails(
                "987654",
                StopName(
                        "Stop name 2",
                        "Locality 2"),
                9.8,
                7.6,
                0)
        val stopDetails3 = StopDetails(
                "123456",
                StopName(
                        "New stop name",
                        "New locality"),
                1.2,
                3.4,
                5)
        whenever(alertsRepository.getAllAlertsFlow())
                .thenReturn(flowOf(listOf(arrivalAlert, proximityAlert)))
        whenever(busStopsRepository.getBusStopDetailsFlow(setOf("123456", "987654")))
                .thenReturn(
                        flowOf(
                                mapOf(
                                        "123456" to stopDetails1,
                                        "987654" to stopDetails2),
                                mapOf(
                                        "123456" to stopDetails3,
                                        "987654" to stopDetails2)))
        val expected1 = listOf(
                UiAlert.ArrivalAlert(1, "123456", stopDetails1, listOf("1"), 5),
                UiAlert.ProximityAlert(2, "987654", stopDetails2, 250))
        val expected2 = listOf(
                UiAlert.ArrivalAlert(1, "123456", stopDetails3, listOf("1"), 5),
                UiAlert.ProximityAlert(2, "987654", stopDetails2, 250))

        val observer = alertsRetriever.allAlertsFlow.test(this)
        observer.finish()

        observer.assertValues(null, expected1, expected2)
    }

    private val runBlockingTest = coroutineRule::runBlockingTest
}