/*
 * Copyright (C) 2021 - 2026 Niall 'Rivernile' Scott
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

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.alerts.ArrivalAlert
import uk.org.rivernile.android.bustracker.core.alerts.ProximityAlert
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopDetails
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopLocation
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopName
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

/**
 * Tests for [AlertsRetriever].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class AlertsRetrieverTest {

    @Mock
    private lateinit var alertsRepository: AlertsRepository
    @Mock
    private lateinit var busStopsRepository: BusStopsRepository

    private lateinit var alertsRetriever: AlertsRetriever

    @BeforeTest
    fun setUp() {
        alertsRetriever = AlertsRetriever(
            alertsRepository,
            busStopsRepository,
            naturalOrder()
        )
    }

    @Test
    fun allAlertsFlowEmitsEmptyListWhenUpstreamEmitsNull() = runTest {
        whenever(alertsRepository.allAlertsFlow)
            .thenReturn(flowOf(null))

        alertsRetriever.allAlertsFlow.test {
            assertNull(awaitItem())
            assertEquals(emptyList(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allAlertsFlowEmitsEmptyListWhenUpstreamEmitsEmptyList() = runTest {
        whenever(alertsRepository.allAlertsFlow)
            .thenReturn(flowOf(emptyList()))

        alertsRetriever.allAlertsFlow.test {
            assertNull(awaitItem())
            assertEquals(emptyList(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allAlertsFlowWithArrivalAlertAndNullStopDetailsEmitsAlertWithoutStopDetails() = runTest {
        val arrivalAlert = ArrivalAlert(
            id = 1,
            timeAdded = Instant.fromEpochMilliseconds(123L),
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            services = setOf(service(1)),
            timeTriggerMinutes = 5
        )
        whenever(alertsRepository.allAlertsFlow)
            .thenReturn(flowOf(listOf(arrivalAlert)))
        whenever(busStopsRepository.getBusStopDetailsFlow(setOf("123456".toNaptanStopIdentifier())))
            .thenReturn(flowOf(null))
        val expected = listOf(
            UiAlert.ArrivalAlert(
                id = 1,
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopDetails = null,
                services = listOf(service(1)),
                5
            )
        )

        alertsRetriever.allAlertsFlow.test {
            assertNull(awaitItem())
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allAlertsFlowWithArrivalAlertAndEmptyStopDetailsEmitsAlertWithoutStopDetails() = runTest {
        val arrivalAlert = ArrivalAlert(
            id = 1,
            timeAdded = Instant.fromEpochMilliseconds(123L),
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            services = setOf(service(1)),
            timeTriggerMinutes = 5
        )
        whenever(alertsRepository.allAlertsFlow)
            .thenReturn(flowOf(listOf(arrivalAlert)))
        whenever(busStopsRepository.getBusStopDetailsFlow(setOf("123456".toNaptanStopIdentifier())))
            .thenReturn(flowOf(emptyMap()))
        val expected = listOf(
            UiAlert.ArrivalAlert(
                id = 1,
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopDetails = null,
                services = listOf(service(1)),
                timeTrigger = 5
            )
        )

        alertsRetriever.allAlertsFlow.test {
            assertNull(awaitItem())
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allAlertsWithArrivalAlertAndStopDetailsEmitsFullObject() = runTest {
        val arrivalAlert = ArrivalAlert(
            id = 1,
            timeAdded = Instant.fromEpochMilliseconds(123L),
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            services = setOf(service(3), service(1), service(2)),
            timeTriggerMinutes = 5
        )
        val stopDetails = FakeStopDetails(
            "123456".toNaptanStopIdentifier(),
            FakeStopName(
                "Stop name",
                "Locality"
            ),
            FakeStopLocation(
                1.2,
                3.4
            ),
            StopOrientation.SOUTH
        )
        whenever(alertsRepository.allAlertsFlow)
            .thenReturn(flowOf(listOf(arrivalAlert)))
        whenever(busStopsRepository.getBusStopDetailsFlow(setOf("123456".toNaptanStopIdentifier())))
            .thenReturn(
                flowOf(
                    mapOf("123456".toNaptanStopIdentifier() to stopDetails)
                )
            )
        val expected = listOf(
            UiAlert.ArrivalAlert(
                id = 1,
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopDetails = stopDetails,
                services = listOf(service(1), service(2), service(3)),
                timeTrigger = 5
            )
        )

        alertsRetriever.allAlertsFlow.test {
            assertNull(awaitItem())
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allAlertsWithArrivalAlertsEmitsUpdatedArrivalAlertDetails() = runTest {
        val arrivalAlert1 = ArrivalAlert(
            id = 1,
            timeAdded = Instant.fromEpochMilliseconds(123L),
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            services = setOf(service(1)),
            timeTriggerMinutes = 5
        )
        val arrivalAlert2 = ArrivalAlert(
            id = 1,
            timeAdded = Instant.fromEpochMilliseconds(123L),
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            services = setOf(service(1)),
            timeTriggerMinutes = 10
        )
        val stopDetails = FakeStopDetails(
            "123456".toNaptanStopIdentifier(),
            FakeStopName(
                "Stop name",
                "Locality"
            ),
            FakeStopLocation(
                1.2,
                3.4
            ),
            StopOrientation.SOUTH
        )
        whenever(alertsRepository.allAlertsFlow)
            .thenReturn(flowOf(listOf(arrivalAlert1), listOf(arrivalAlert2)))
        whenever(busStopsRepository.getBusStopDetailsFlow(setOf("123456".toNaptanStopIdentifier())))
            .thenReturn(flowOf(mapOf("123456".toNaptanStopIdentifier() to stopDetails)))
        val expected1 = listOf(
            UiAlert.ArrivalAlert(
                id = 1,
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopDetails = stopDetails,
                services = listOf(service(1)),
                timeTrigger = 5
            )
        )
        val expected2 = listOf(
            UiAlert.ArrivalAlert(
                id = 1,
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopDetails = stopDetails,
                services = listOf(service(1)),
                timeTrigger = 10
            )
        )

        alertsRetriever.allAlertsFlow.test {
            assertNull(awaitItem())
            assertEquals(expected1, awaitItem())
            assertEquals(expected2, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allAlertsWithArrivalAlertEmitsUpdatedStopDetails() = runTest {
        val arrivalAlert = ArrivalAlert(
            id = 1,
            timeAdded = Instant.fromEpochMilliseconds(123L),
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            services = setOf(service(1)),
            timeTriggerMinutes = 5
        )
        val stopDetails1 = FakeStopDetails(
            "123456".toNaptanStopIdentifier(),
            FakeStopName(
                "Stop name",
                "Locality"
            ),
            FakeStopLocation(
                1.2,
                3.4
            ),
            StopOrientation.SOUTH
        )
        val stopDetails2 = FakeStopDetails(
            "123456".toNaptanStopIdentifier(),
            FakeStopName(
                "New stop name",
                "New locality"
            ),
            FakeStopLocation(
                1.2,
                3.4
            ),
            StopOrientation.SOUTH
        )
        whenever(alertsRepository.allAlertsFlow)
            .thenReturn(flowOf(listOf(arrivalAlert)))
        whenever(busStopsRepository.getBusStopDetailsFlow(setOf("123456".toNaptanStopIdentifier())))
            .thenReturn(
                flowOf(
                    mapOf("123456".toNaptanStopIdentifier() to stopDetails1),
                    mapOf("123456".toNaptanStopIdentifier() to stopDetails2)
                )
            )
        val expected1 = listOf(
            UiAlert.ArrivalAlert(
                id = 1,
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopDetails = stopDetails1,
                services = listOf(service(1)),
                timeTrigger = 5
            )
        )
        val expected2 = listOf(
            UiAlert.ArrivalAlert(
                id = 1,
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopDetails = stopDetails2,
                services = listOf(service(1)),
                timeTrigger = 5
            )
        )

        alertsRetriever.allAlertsFlow.test {
            assertNull(awaitItem())
            assertEquals(expected1, awaitItem())
            assertEquals(expected2, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allAlertsFlowWithProximityAlertAndNullStopDetailsEmitsAlertWithoutStopDetails() = runTest {
        val proximityAlert = ProximityAlert(
            id = 1,
            timeAdded = Instant.fromEpochMilliseconds(123L),
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            distanceFromMeters = 250
        )
        whenever(alertsRepository.allAlertsFlow)
            .thenReturn(flowOf(listOf(proximityAlert)))
        whenever(busStopsRepository.getBusStopDetailsFlow(setOf("123456".toNaptanStopIdentifier())))
            .thenReturn(flowOf(null))
        val expected = listOf(
            UiAlert.ProximityAlert(
                id = 1,
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopDetails = null,
                distanceFrom = 250
            )
        )

        alertsRetriever.allAlertsFlow.test {
            assertNull(awaitItem())
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allAlertsFlowWithProximityAlertAndEmptyStopDetailsEmitsAlertWithoutStopDetails() = runTest {
        val proximityAlert = ProximityAlert(
            id = 1,
            timeAdded = Instant.fromEpochMilliseconds(123L),
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            distanceFromMeters = 250
        )
        whenever(alertsRepository.allAlertsFlow)
            .thenReturn(flowOf(listOf(proximityAlert)))
        whenever(busStopsRepository.getBusStopDetailsFlow(setOf("123456".toNaptanStopIdentifier())))
            .thenReturn(flowOf(emptyMap()))
        val expected = listOf(
            UiAlert.ProximityAlert(
                id = 1,
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopDetails = null,
                distanceFrom = 250
            )
        )

        alertsRetriever.allAlertsFlow.test {
            assertNull(awaitItem())
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allAlertsWithProximityAlertAndStopDetailsEmitsFullObject() = runTest {
        val proximityAlert = ProximityAlert(
            id = 1,
            timeAdded = Instant.fromEpochMilliseconds(123L),
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            distanceFromMeters = 250
        )
        val stopDetails = FakeStopDetails(
            "123456".toNaptanStopIdentifier(),
            FakeStopName(
                "Stop name",
                "Locality"
            ),
            FakeStopLocation(
                1.2,
                3.4
            ),
            StopOrientation.SOUTH
        )
        whenever(alertsRepository.allAlertsFlow)
            .thenReturn(flowOf(listOf(proximityAlert)))
        whenever(busStopsRepository.getBusStopDetailsFlow(setOf("123456".toNaptanStopIdentifier())))
            .thenReturn(flowOf(mapOf("123456".toNaptanStopIdentifier() to stopDetails)))
        val expected = listOf(
            UiAlert.ProximityAlert(
                id = 1,
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopDetails = stopDetails,
                distanceFrom = 250
            )
        )

        alertsRetriever.allAlertsFlow.test {
            assertNull(awaitItem())
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allAlertsWithProximityAlertsEmitsUpdatedProximityAlertDetails() = runTest {
        val proximityAlert1 = ProximityAlert(
            id = 1,
            timeAdded = Instant.fromEpochMilliseconds(123L),
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            distanceFromMeters = 250
        )
        val proximityAlert2 = ProximityAlert(
            id = 1,
            timeAdded = Instant.fromEpochMilliseconds(123L),
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            distanceFromMeters = 500
        )
        val stopDetails = FakeStopDetails(
            "123456".toNaptanStopIdentifier(),
            FakeStopName(
                "Stop name",
                "Locality"
            ),
            FakeStopLocation(
                1.2,
                3.4
            ),
            StopOrientation.SOUTH
        )
        whenever(alertsRepository.allAlertsFlow)
            .thenReturn(flowOf(listOf(proximityAlert1), listOf(proximityAlert2)))
        whenever(busStopsRepository.getBusStopDetailsFlow(setOf("123456".toNaptanStopIdentifier())))
            .thenReturn(flowOf(mapOf("123456".toNaptanStopIdentifier() to stopDetails)))
        val expected1 = listOf(
            UiAlert.ProximityAlert(
                id = 1,
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopDetails = stopDetails,
                distanceFrom = 250
            )
        )
        val expected2 = listOf(
            UiAlert.ProximityAlert(
                id = 1,
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopDetails = stopDetails,
                distanceFrom = 500
            )
        )

        alertsRetriever.allAlertsFlow.test {
            assertNull(awaitItem())
            assertEquals(expected1, awaitItem())
            assertEquals(expected2, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allAlertsWithProximityAlertEmitsUpdatedStopDetails() = runTest {
        val proximityAlert = ProximityAlert(
            id = 1,
            timeAdded = Instant.fromEpochMilliseconds(123L),
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            distanceFromMeters = 250
        )
        val stopDetails1 = FakeStopDetails(
            "123456".toNaptanStopIdentifier(),
            FakeStopName(
                "Stop name",
                "Locality"
            ),
            FakeStopLocation(
                1.2,
                3.4
            ),
            StopOrientation.SOUTH
        )
        val stopDetails2 = FakeStopDetails(
            "123456".toNaptanStopIdentifier(),
            FakeStopName(
                "New stop name",
                "New locality"
            ),
            FakeStopLocation(
                1.2,
                3.4
            ),
            StopOrientation.SOUTH
        )
        whenever(alertsRepository.allAlertsFlow)
            .thenReturn(flowOf(listOf(proximityAlert)))
        whenever(busStopsRepository.getBusStopDetailsFlow(setOf("123456".toNaptanStopIdentifier())))
            .thenReturn(
                flowOf(
                    mapOf("123456".toNaptanStopIdentifier() to stopDetails1),
                    mapOf("123456".toNaptanStopIdentifier() to stopDetails2)
                )
            )
        val expected1 = listOf(
            UiAlert.ProximityAlert(
                id = 1,
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopDetails = stopDetails1,
                distanceFrom = 250
            )
        )
        val expected2 = listOf(
            UiAlert.ProximityAlert(
                id = 1,
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopDetails = stopDetails2,
                distanceFrom = 250
            )
        )

        alertsRetriever.allAlertsFlow.test {
            assertNull(awaitItem())
            assertEquals(expected1, awaitItem())
            assertEquals(expected2, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allAlertsPropagatesStopDetailsUpdateToAllRelevantStops() = runTest {
        val arrivalAlert = ArrivalAlert(
            id = 1,
            timeAdded = Instant.fromEpochMilliseconds(123L),
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            services = setOf(service(1)),
            timeTriggerMinutes = 5
        )
        val proximityAlert = ProximityAlert(
            id = 2,
            timeAdded = Instant.fromEpochMilliseconds(124L),
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            distanceFromMeters = 250
        )
        val stopDetails1 = FakeStopDetails(
            "123456".toNaptanStopIdentifier(),
            FakeStopName(
                "Stop name",
                "Locality"
            ),
            FakeStopLocation(
                1.2,
                3.4
            ),
            StopOrientation.SOUTH
        )
        val stopDetails2 = FakeStopDetails(
            "123456".toNaptanStopIdentifier(),
            FakeStopName(
                "New stop name",
                "New locality"
            ),
            FakeStopLocation(
                1.2,
                3.4
            ),
            StopOrientation.SOUTH
        )
        whenever(alertsRepository.allAlertsFlow)
            .thenReturn(flowOf(listOf(arrivalAlert, proximityAlert)))
        whenever(busStopsRepository.getBusStopDetailsFlow(setOf("123456".toNaptanStopIdentifier())))
            .thenReturn(
                flowOf(
                    mapOf("123456".toNaptanStopIdentifier() to stopDetails1),
                    mapOf("123456".toNaptanStopIdentifier() to stopDetails2)
                )
            )
        val expected1 = listOf(
            UiAlert.ArrivalAlert(
                id = 1,
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopDetails = stopDetails1,
                services = listOf(service(1)),
                timeTrigger = 5
            ),
            UiAlert.ProximityAlert(
                id = 2,
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopDetails = stopDetails1,
                distanceFrom = 250
            )
        )
        val expected2 = listOf(
            UiAlert.ArrivalAlert(
                id = 1,
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopDetails = stopDetails2,
                services = listOf(service(1)),
                timeTrigger = 5
            ),
            UiAlert.ProximityAlert(
                id = 2,
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopDetails = stopDetails2,
                distanceFrom = 250
            )
        )

        alertsRetriever.allAlertsFlow.test {
            assertNull(awaitItem())
            assertEquals(expected1, awaitItem())
            assertEquals(expected2, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allAlertsPropagatesStopDetailsUpdateOnlyToRelevantStops() = runTest {
        val arrivalAlert = ArrivalAlert(
            id = 1,
            timeAdded = Instant.fromEpochMilliseconds(123L),
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            services = setOf(service(1)),
            timeTriggerMinutes = 5
        )
        val proximityAlert = ProximityAlert(
            id = 2,
            timeAdded = Instant.fromEpochMilliseconds(124L),
            stopIdentifier = "987654".toNaptanStopIdentifier(),
            distanceFromMeters = 250
        )
        val stopDetails1 = FakeStopDetails(
            "123456".toNaptanStopIdentifier(),
            FakeStopName(
                "Stop name",
                "Locality"
            ),
            FakeStopLocation(
                1.2,
                3.4
            ),
            StopOrientation.SOUTH
        )
        val stopDetails2 = FakeStopDetails(
            "987654".toNaptanStopIdentifier(),
            FakeStopName(
                "Stop name 2",
                "Locality 2"
            ),
            FakeStopLocation(
                9.8,
                7.6
            ),
            StopOrientation.UNKNOWN
        )
        val stopDetails3 = FakeStopDetails(
            "123456".toNaptanStopIdentifier(),
            FakeStopName(
                "New stop name",
                "New locality"
            ),
            FakeStopLocation(
                1.2,
                3.4
            ),
            StopOrientation.SOUTH
        )
        whenever(alertsRepository.allAlertsFlow)
            .thenReturn(flowOf(listOf(arrivalAlert, proximityAlert)))
        whenever(busStopsRepository.getBusStopDetailsFlow(
            setOf("123456".toNaptanStopIdentifier(), "987654".toNaptanStopIdentifier())))
            .thenReturn(
                flowOf(
                    mapOf(
                        "123456".toNaptanStopIdentifier() to stopDetails1,
                        "987654".toNaptanStopIdentifier() to stopDetails2
                    ),
                    mapOf(
                        "123456".toNaptanStopIdentifier() to stopDetails3,
                        "987654".toNaptanStopIdentifier() to stopDetails2)
                )
            )
        val expected1 = listOf(
            UiAlert.ArrivalAlert(
                id = 1,
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopDetails = stopDetails1,
                services = listOf(service(1)),
                timeTrigger = 5
            ),
            UiAlert.ProximityAlert(
                id = 2,
                stopIdentifier = "987654".toNaptanStopIdentifier(),
                stopDetails = stopDetails2,
                distanceFrom = 250
            )
        )
        val expected2 = listOf(
            UiAlert.ArrivalAlert(
                id = 1,
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopDetails = stopDetails3,
                services = listOf(service(1)),
                timeTrigger = 5
            ),
            UiAlert.ProximityAlert(
                id = 2,
                stopIdentifier = "987654".toNaptanStopIdentifier(),
                stopDetails = stopDetails2,
                distanceFrom = 250
            )
        )

        alertsRetriever.allAlertsFlow.test {
            assertNull(awaitItem())
            assertEquals(expected1, awaitItem())
            assertEquals(expected2, awaitItem())
            awaitComplete()
        }
    }

    private fun service(id: Int): ServiceDescriptor {
        return FakeServiceDescriptor(
            serviceName = id.toString(),
            operatorCode = "TEST$id"
        )
    }
}
