/*
 * Copyright (C) 2019 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.alerts.arrivals

import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.alerts.AlertNotificationDispatcher
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.alerts.ArrivalAlert
import uk.org.rivernile.android.bustracker.core.alerts.FakeAlertNotificationDispatcher
import uk.org.rivernile.android.bustracker.core.alerts.FakeAlertsRepository
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.FakeTrackerEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.TrackerEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimes
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimesResponse
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.Service
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.Stop
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.Vehicle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Unit tests for [RealCheckTimesTask].
 *
 * @author Niall Scott
 */
class RealCheckTimesTaskTest {

    @Test
    fun doesNotCreateRequestWhenArrivalAlertStopCodesIsNull() = runTest {
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onGetAllArrivalAlertStopCodes = { null }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { _, _ ->
                    fail("Not expecting to get any times from the tracker endpoint.")
                }
            )
        )

        checkTimesTask.checkTimes()
    }

    @Test
    fun doesNotCreateRequestWhenArrivalAlertStopCodesIsEmpty() = runTest {
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onGetAllArrivalAlertStopCodes = { emptySet() }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { _, _ ->
                    fail("Not expecting to get any times from the tracker endpoint.")
                }
            )
        )

        checkTimesTask.checkTimes()
    }

    @Test
    fun createsRequestWithExpectedStopCodeWhenSingleStopCodeIsSupplied() = runTest {
        val stopCodes = setOf("123456")
        var getLiveTimesInvocationCount = 0
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onGetAllArrivalAlertStopCodes = { stopCodes }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { stops, numberOfDepartures ->
                    assertEquals(stopCodes.toList(), stops)
                    assertEquals(1, numberOfDepartures)
                    getLiveTimesInvocationCount++
                    LiveTimesResponse.Error.NoConnectivity
                }
            )
        )

        checkTimesTask.checkTimes()

        assertEquals(1, getLiveTimesInvocationCount)
    }

    @Test
    fun createsRequestWithExpectedStopCodesWhenMultipleStopCodesAreSupplied() = runTest {
        val stopCodes = setOf("123456", "987654", "246802")
        var getLiveTimesInvocationCount = 0
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onGetAllArrivalAlertStopCodes = { stopCodes }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { stops, numberOfDepartures ->
                    assertEquals(stopCodes.toList(), stops)
                    assertEquals(1, numberOfDepartures)
                    getLiveTimesInvocationCount++
                    LiveTimesResponse.Error.NoConnectivity
                }
            )
        )

        checkTimesTask.checkTimes()

        assertEquals(1, getLiveTimesInvocationCount)
    }

    @Test
    fun failingRequestFailsSilently() = runTest {
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onGetAllArrivalAlertStopCodes = { setOf("123456") }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { stops, numberOfDepartures ->
                    assertEquals(listOf("123456"), stops)
                    assertEquals(1, numberOfDepartures)
                    LiveTimesResponse.Error.ServerError.Other()
                }
            )
        )

        checkTimesTask.checkTimes()
    }

    @Test
    fun nullArrivalAlertsDoesNotCauseNotifications() = runTest {
        val vehicle = createVehicle(6)
        val service = createService("1", listOf(vehicle))
        val stop = createStop("123456", listOf(service))
        val liveTimes = createLiveTimes(mapOf("123456" to stop))
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onRemoveArrivalAlertWithId = {
                    fail("Not expecting to remove any arrival alerts.")
                },
                onGetAllArrivalAlerts = { null },
                onGetAllArrivalAlertStopCodes = { setOf("123456") }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { stops, numberOfDepartures ->
                    assertEquals(listOf("123456"), stops)
                    assertEquals(1, numberOfDepartures)
                    LiveTimesResponse.Success(liveTimes)
                }
            ),
            alertNotificationDispatcher = FakeAlertNotificationDispatcher(
                onDispatchTimeAlertNotification = { _, _ ->
                    fail("Not expecting to dispatch any time alert notifications.")
                }
            )
        )

        checkTimesTask.checkTimes()
    }

    @Test
    fun emptyArrivalAlertsDoesNotCauseNotifications() = runTest {
        val vehicle = createVehicle(6)
        val service = createService("1", listOf(vehicle))
        val stop = createStop("123456", listOf(service))
        val liveTimes = createLiveTimes(mapOf("123456" to stop))
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onRemoveArrivalAlertWithId = {
                    fail("Not expecting to remove any arrival alerts.")
                },
                onGetAllArrivalAlerts = { emptyList() },
                onGetAllArrivalAlertStopCodes = { setOf("123456") }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { stops, numberOfDepartures ->
                    assertEquals(listOf("123456"), stops)
                    assertEquals(1, numberOfDepartures)
                    LiveTimesResponse.Success(liveTimes)
                }
            ),
            alertNotificationDispatcher = FakeAlertNotificationDispatcher(
                onDispatchTimeAlertNotification = { _, _ ->
                    fail("Not expecting to dispatch any time alert notifications.")
                }
            )
        )

        checkTimesTask.checkTimes()
    }

    @Test
    fun singleServiceForSingleStopThatDoesNotMeetTimeTriggerDoesNotCauseNotification() = runTest {
        val arrivalAlerts = listOf(ArrivalAlert(1, 123L, "123456", listOf("1"), 5))
        val vehicle = createVehicle(6)
        val service = createService("1", listOf(vehicle))
        val stop = createStop("123456", listOf(service))
        val liveTimes = createLiveTimes(mapOf("123456" to stop))
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onRemoveArrivalAlertWithId = {
                    fail("Not expecting to remove any arrival alerts.")
                },
                onGetAllArrivalAlerts = { arrivalAlerts },
                onGetAllArrivalAlertStopCodes = { setOf("123456") }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { stops, numberOfDepartures ->
                    assertEquals(listOf("123456"), stops)
                    assertEquals(1, numberOfDepartures)
                    LiveTimesResponse.Success(liveTimes)
                }
            ),
            alertNotificationDispatcher = FakeAlertNotificationDispatcher(
                onDispatchTimeAlertNotification = { _, _ ->
                    fail("Not expecting to dispatch any time alert notifications.")
                }
            )
        )

        checkTimesTask.checkTimes()
    }

    @Test
    fun singleServiceForSingleStopThatEqualsTimeTriggerCausesNotification() = runTest {
        val arrivalAlert = ArrivalAlert(1, 123L, "123456", listOf("1"), 5)
        val vehicle = createVehicle(5)
        val service = createService("1", listOf(vehicle))
        val stop = createStop("123456", listOf(service))
        val liveTimes = createLiveTimes(mapOf("123456" to stop))
        var removeArrivalAlertWithIdInvocationCount = 0
        var dispatchTimeAlertNotificationInvocationCount = 0
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onRemoveArrivalAlertWithId = {
                    assertEquals(1, it)
                    removeArrivalAlertWithIdInvocationCount++
                },
                onGetAllArrivalAlerts = { listOf(arrivalAlert) },
                onGetAllArrivalAlertStopCodes = { setOf("123456") }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { stops, numberOfDepartures ->
                    assertEquals(listOf("123456"), stops)
                    assertEquals(1, numberOfDepartures)
                    LiveTimesResponse.Success(liveTimes)
                }
            ),
            alertNotificationDispatcher = FakeAlertNotificationDispatcher(
                onDispatchTimeAlertNotification = { a, s ->
                    assertEquals(arrivalAlert, a)
                    assertEquals(listOf(service), s)
                    dispatchTimeAlertNotificationInvocationCount++
                }
            )
        )

        checkTimesTask.checkTimes()

        assertEquals(1, dispatchTimeAlertNotificationInvocationCount)
        assertEquals(1, removeArrivalAlertWithIdInvocationCount)
    }

    @Test
    fun singleServiceForSingleStopThatIsLessThanTimeTriggerCausesNotification() = runTest {
        val arrivalAlert = ArrivalAlert(1, 123L, "123456", listOf("1"), 5)
        val vehicle = createVehicle(4)
        val service = createService("1", listOf(vehicle))
        val stop = createStop("123456", listOf(service))
        val liveTimes = createLiveTimes(mapOf("123456" to stop))
        var removeArrivalAlertWithIdInvocationCount = 0
        var dispatchTimeAlertNotificationInvocationCount = 0
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onRemoveArrivalAlertWithId = {
                    assertEquals(1, it)
                    removeArrivalAlertWithIdInvocationCount++
                },
                onGetAllArrivalAlerts = { listOf(arrivalAlert) },
                onGetAllArrivalAlertStopCodes = { setOf("123456") }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { stops, numberOfDepartures ->
                    assertEquals(listOf("123456"), stops)
                    assertEquals(1, numberOfDepartures)
                    LiveTimesResponse.Success(liveTimes)
                }
            ),
            alertNotificationDispatcher = FakeAlertNotificationDispatcher(
                onDispatchTimeAlertNotification = { a, s ->
                    assertEquals(arrivalAlert, a)
                    assertEquals(listOf(service), s)
                    dispatchTimeAlertNotificationInvocationCount++
                }
            )
        )

        checkTimesTask.checkTimes()

        assertEquals(1, dispatchTimeAlertNotificationInvocationCount)
        assertEquals(1, removeArrivalAlertWithIdInvocationCount)
    }

    @Test
    fun stopNotFoundInArrivalAlertsDoesNotCauseNotification() = runTest {
        val arrivalAlerts = listOf(ArrivalAlert(1, 123L, "987654", listOf("1"), 5))
        val vehicle = createVehicle(4)
        val service = createService("1", listOf(vehicle))
        val stop = createStop("123456", listOf(service))
        val liveTimes = createLiveTimes(mapOf("123456" to stop))
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onRemoveArrivalAlertWithId = {
                    fail("Not expecting to remove any arrival alerts.")
                },
                onGetAllArrivalAlerts = { arrivalAlerts },
                onGetAllArrivalAlertStopCodes = { setOf("987654") }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { stops, numberOfDepartures ->
                    assertEquals(listOf("987654"), stops)
                    assertEquals(1, numberOfDepartures)
                    LiveTimesResponse.Success(liveTimes)
                }
            ),
            alertNotificationDispatcher = FakeAlertNotificationDispatcher(
                onDispatchTimeAlertNotification = { _, _ ->
                    fail("Not expecting to dispatch any time alert notifications.")
                }
            )
        )

        checkTimesTask.checkTimes()
    }

    @Test
    fun multipleServicesForSingleStopThatDoesNotMeetTimeTriggerDoesNotCauseNotification() =
            runTest {
        val arrivalAlerts = listOf(ArrivalAlert(1, 123L, "123456", listOf("1", "2", "3"), 5))
        val vehicle = createVehicle(6)
        val service1 = createService("1", listOf(vehicle))
        val service2 = createService("2", listOf(vehicle))
        val service3 = createService("3", listOf(vehicle))
        val stop = createStop("123456", listOf(service1, service2, service3))
        val liveTimes = createLiveTimes(mapOf("123456" to stop))
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onRemoveArrivalAlertWithId = {
                    fail("Not expecting to remove any arrival alerts.")
                },
                onGetAllArrivalAlerts = { arrivalAlerts },
                onGetAllArrivalAlertStopCodes = { setOf("123456") }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { stops, numberOfDepartures ->
                    assertEquals(listOf("123456"), stops)
                    assertEquals(1, numberOfDepartures)
                    LiveTimesResponse.Success(liveTimes)
                }
            ),
            alertNotificationDispatcher = FakeAlertNotificationDispatcher(
                onDispatchTimeAlertNotification = { _, _ ->
                    fail("Not expecting to dispatch any time alert notifications.")
                }
            )
        )

        checkTimesTask.checkTimes()
    }

    @Test
    fun singleServiceForSingleStopThatSatisfiesTimeTriggerCausesNotification() = runTest {
        val arrivalAlert = ArrivalAlert(1, 123L, "123456", listOf("1", "2", "3"), 5)
        val vehicle1 = createVehicle(6)
        val vehicle2 = createVehicle(4)
        val vehicle3 = createVehicle(7)
        val service1 = createService("1", listOf(vehicle1))
        val service2 = createService("2", listOf(vehicle2))
        val service3 = createService("3", listOf(vehicle3))
        val stop = createStop("123456", listOf(service1, service2, service3))
        val liveTimes = createLiveTimes(mapOf("123456" to stop))
        var removeArrivalAlertWithIdInvocationCount = 0
        var dispatchTimeAlertNotificationInvocationCount = 0
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onRemoveArrivalAlertWithId = {
                    assertEquals(1, it)
                    removeArrivalAlertWithIdInvocationCount++
                },
                onGetAllArrivalAlerts = { listOf(arrivalAlert) },
                onGetAllArrivalAlertStopCodes = { setOf("123456") }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { stops, numberOfDepartures ->
                    assertEquals(listOf("123456"), stops)
                    assertEquals(1, numberOfDepartures)
                    LiveTimesResponse.Success(liveTimes)
                }
            ),
            alertNotificationDispatcher = FakeAlertNotificationDispatcher(
                onDispatchTimeAlertNotification = { a, s ->
                    assertEquals(arrivalAlert, a)
                    assertEquals(listOf(service2), s)
                    dispatchTimeAlertNotificationInvocationCount++
                }
            )
        )

        checkTimesTask.checkTimes()

        assertEquals(1, dispatchTimeAlertNotificationInvocationCount)
        assertEquals(1, removeArrivalAlertWithIdInvocationCount)
    }

    @Test
    fun multipleServicesForSingleStopThatSatisfiesTimeTriggerCausesNotifications() = runTest {
        val arrivalAlert = ArrivalAlert(1, 123L, "123456", listOf("1", "2", "3"), 5)
        val vehicle1 = createVehicle(6)
        val vehicle2 = createVehicle(4)
        val vehicle3 = createVehicle(0)
        val service1 = createService("1", listOf(vehicle1))
        val service2 = createService("2", listOf(vehicle2))
        val service3 = createService("3", listOf(vehicle3))
        val stop = createStop("123456", listOf(service1, service2, service3))
        val liveTimes = createLiveTimes(mapOf("123456" to stop))
        var removeArrivalAlertWithIdInvocationCount = 0
        var dispatchTimeAlertNotificationInvocationCount = 0
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onRemoveArrivalAlertWithId = {
                    assertEquals(1, it)
                    removeArrivalAlertWithIdInvocationCount++
                },
                onGetAllArrivalAlerts = { listOf(arrivalAlert) },
                onGetAllArrivalAlertStopCodes = { setOf("123456") }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { stops, numberOfDepartures ->
                    assertEquals(listOf("123456"), stops)
                    assertEquals(1, numberOfDepartures)
                    LiveTimesResponse.Success(liveTimes)
                }
            ),
            alertNotificationDispatcher = FakeAlertNotificationDispatcher(
                onDispatchTimeAlertNotification = { a, s ->
                    assertEquals(arrivalAlert, a)
                    assertEquals(listOf(service2, service3), s)
                    dispatchTimeAlertNotificationInvocationCount++
                }
            )
        )

        checkTimesTask.checkTimes()

        assertEquals(1, dispatchTimeAlertNotificationInvocationCount)
        assertEquals(1, removeArrivalAlertWithIdInvocationCount)
    }

    @Test
    fun multipleStopsWithMultipleCombinationsYieldsExpectedResults() = runTest {
        val arrivalAlert1 = ArrivalAlert(1, 123L, "123", listOf("1", "2", "3"), 10)
        val arrivalAlert2 = ArrivalAlert(2, 123L, "456", listOf("4", "5", "6"), 5)
        val arrivalAlert3 = ArrivalAlert(3, 123L, "789", listOf("7", "8", "9"), 3)
        // Services for stop "123"
        val service1 = createService("1", listOf(createVehicle(1)))
        val service2 = createService("2", listOf(createVehicle(0)))
        val service3 = createService("3", listOf(createVehicle(2)))
        // Services for stop "456"
        val service4 = createService("4", listOf(createVehicle(10)))
        val service5 = createService("5", listOf(createVehicle(6)))
        val service6 = createService("6", listOf(createVehicle(10)))
        // Services for stop "789"
        val service7 = createService("7", listOf(createVehicle(5)))
        val service8 = createService("8", listOf(createVehicle(6)))
        val service9 = createService("9", listOf(createVehicle(3)))
        // Stops
        val stop1 = createStop("123", listOf(service1, service2, service3))
        val stop2 = createStop("456", listOf(service4, service5, service6))
        val stop3 = createStop("789", listOf(service7, service8, service9))
        val liveTimes = createLiveTimes(
            mapOf(
                "123" to stop1,
                "456" to stop2,
                "789" to stop3
            )
        )
        val ids = ArrayDeque(listOf(1, 3)) // Item '2' should never be removed.
        val notifiedArrivalAlerts = ArrayDeque(listOf(arrivalAlert1, arrivalAlert3))
        val notifiedServiceListings = ArrayDeque(
            listOf(
                listOf(service1, service2, service3),
                listOf(service9)
            )
        )
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onRemoveArrivalAlertWithId = {
                    assertEquals(ids.removeFirst(), it)
                },
                onGetAllArrivalAlerts = { listOf(arrivalAlert1, arrivalAlert2, arrivalAlert3) },
                onGetAllArrivalAlertStopCodes = { setOf("123", "456", "789") }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { stops, numberOfDepartures ->
                    assertEquals(listOf("123", "456", "789"), stops)
                    assertEquals(1, numberOfDepartures)
                    LiveTimesResponse.Success(liveTimes)
                }
            ),
            alertNotificationDispatcher = FakeAlertNotificationDispatcher(
                onDispatchTimeAlertNotification = { a, s ->
                    assertEquals(notifiedArrivalAlerts.removeFirst(), a)
                    assertEquals(notifiedServiceListings.removeFirst(), s)
                }
            )
        )

        checkTimesTask.checkTimes()
        assertTrue(ids.isEmpty())
        assertTrue(notifiedArrivalAlerts.isEmpty())
        assertTrue(notifiedArrivalAlerts.isEmpty())
    }

    private fun createCheckTimesTask(
        alertsRepository: AlertsRepository = FakeAlertsRepository(),
        trackerEndpoint: TrackerEndpoint = FakeTrackerEndpoint(),
        alertNotificationDispatcher: AlertNotificationDispatcher = FakeAlertNotificationDispatcher()
    ): RealCheckTimesTask {
        return RealCheckTimesTask(
            alertsRepository,
            trackerEndpoint,
            alertNotificationDispatcher
        )
    }

    private fun createVehicle(departureMinutes: Int) =
        Vehicle(
            destination = "a",
            departureTime = Clock.System.now(),
            departureMinutes = departureMinutes,
            isEstimatedTime = false,
            isDiverted = false
        )

    private fun createService(serviceName: String, vehicles: List<Vehicle>) =
        Service(
            serviceName = serviceName,
            vehicles = vehicles
        )

    private fun createStop(stopCode: String, services: List<Service>) =
        Stop(
            stopCode = stopCode,
            services = services
        )

    private fun createLiveTimes(stops: Map<String, Stop>) =
        LiveTimes(
            stops = stops,
            receiveTime = Instant.fromEpochMilliseconds(123L),
        )
}
