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
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
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
    fun doesNotCreateRequestWhenArrivalAlertStopsIsNull() = runTest {
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onGetAllArrivalAlertStops = { null }
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
    fun doesNotCreateRequestWhenArrivalAlertStopsIsEmpty() = runTest {
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onGetAllArrivalAlertStops = { emptySet() }
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
    fun createsRequestWithExpectedStopsWhenSingleStopIdentifierIsSupplied() = runTest {
        val stops = setOf("123456".toNaptanStopIdentifier())
        var getLiveTimesInvocationCount = 0
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onGetAllArrivalAlertStops = { stops }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { stops, numberOfDepartures ->
                    assertEquals(stops.toList(), stops)
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
    fun createsRequestWithExpectedStopsWhenMultipleStopIdentifiersAreSupplied() = runTest {
        val stops = setOf(
            "123456".toNaptanStopIdentifier(),
            "987654".toNaptanStopIdentifier(),
            "246802".toNaptanStopIdentifier()
        )
        var getLiveTimesInvocationCount = 0
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onGetAllArrivalAlertStops = { stops }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { stops, numberOfDepartures ->
                    assertEquals(stops.toList(), stops)
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
                onGetAllArrivalAlertStops = { setOf("123456".toNaptanStopIdentifier()) }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { stops, numberOfDepartures ->
                    assertEquals(listOf("123456".toNaptanStopIdentifier()), stops)
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
        val service = createService(
            serviceDescriptor = FakeServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            listOf(vehicle)
        )
        val stop = createStop(
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            listOf(service)
        )
        val liveTimes = createLiveTimes(
            mapOf("123456".toNaptanStopIdentifier() to stop)
        )
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onRemoveArrivalAlertWithId = {
                    fail("Not expecting to remove any arrival alerts.")
                },
                onGetAllArrivalAlerts = { null },
                onGetAllArrivalAlertStops = { setOf("123456".toNaptanStopIdentifier()) }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { stops, numberOfDepartures ->
                    assertEquals(listOf("123456".toNaptanStopIdentifier()), stops)
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
        val service = createService(
            FakeServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            listOf(vehicle)
        )
        val stop = createStop(
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            services = listOf(service)
        )
        val liveTimes = createLiveTimes(
            mapOf("123456".toNaptanStopIdentifier() to stop)
        )
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onRemoveArrivalAlertWithId = {
                    fail("Not expecting to remove any arrival alerts.")
                },
                onGetAllArrivalAlerts = { emptyList() },
                onGetAllArrivalAlertStops = { setOf("123456".toNaptanStopIdentifier()) }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { stops, numberOfDepartures ->
                    assertEquals(listOf("123456".toNaptanStopIdentifier()), stops)
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
        val arrivalAlerts = listOf(
            ArrivalAlert(
                id = 1,
                timeAdded = Instant.fromEpochMilliseconds(123L),
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                services = setOf(
                    FakeServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    )
                ),
                timeTriggerMinutes = 5
            )
        )
        val vehicle = createVehicle(6)
        val service = createService(
            serviceDescriptor = FakeServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            vehicles = listOf(vehicle)
        )
        val stop = createStop("123456".toNaptanStopIdentifier(), listOf(service))
        val liveTimes = createLiveTimes(
            mapOf("123456".toNaptanStopIdentifier() to stop)
        )
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onRemoveArrivalAlertWithId = {
                    fail("Not expecting to remove any arrival alerts.")
                },
                onGetAllArrivalAlerts = { arrivalAlerts },
                onGetAllArrivalAlertStops = { setOf("123456".toNaptanStopIdentifier()) }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { stops, numberOfDepartures ->
                    assertEquals(listOf("123456".toNaptanStopIdentifier()), stops)
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
        val arrivalAlert = ArrivalAlert(
            id = 1,
            timeAdded = Instant.fromEpochMilliseconds(123L),
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            services = setOf(
                FakeServiceDescriptor(
                    serviceName = "1",
                    operatorCode = "TEST1"
                )
            ),
            timeTriggerMinutes = 5
        )
        val vehicle = createVehicle(5)
        val service = createService(
            serviceDescriptor = FakeServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            vehicles = listOf(vehicle)
        )
        val stop = createStop("123456".toNaptanStopIdentifier(), listOf(service))
        val liveTimes = createLiveTimes(mapOf("123456".toNaptanStopIdentifier() to stop))
        var removeArrivalAlertWithIdInvocationCount = 0
        var dispatchTimeAlertNotificationInvocationCount = 0
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onRemoveArrivalAlertWithId = {
                    assertEquals(1, it)
                    removeArrivalAlertWithIdInvocationCount++
                },
                onGetAllArrivalAlerts = { listOf(arrivalAlert) },
                onGetAllArrivalAlertStops = { setOf("123456".toNaptanStopIdentifier()) }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { stops, numberOfDepartures ->
                    assertEquals(listOf("123456".toNaptanStopIdentifier()), stops)
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
        val arrivalAlert = ArrivalAlert(
            id = 1,
            timeAdded = Instant.fromEpochMilliseconds(123L),
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            services = setOf(
                FakeServiceDescriptor(
                    serviceName = "1",
                    operatorCode = "TEST1"
                )
            ),
            timeTriggerMinutes = 5
        )
        val vehicle = createVehicle(4)
        val service = createService(
            serviceDescriptor = FakeServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            vehicles = listOf(vehicle)
        )
        val stop = createStop("123456".toNaptanStopIdentifier(), listOf(service))
        val liveTimes = createLiveTimes(mapOf("123456".toNaptanStopIdentifier() to stop))
        var removeArrivalAlertWithIdInvocationCount = 0
        var dispatchTimeAlertNotificationInvocationCount = 0
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onRemoveArrivalAlertWithId = {
                    assertEquals(1, it)
                    removeArrivalAlertWithIdInvocationCount++
                },
                onGetAllArrivalAlerts = { listOf(arrivalAlert) },
                onGetAllArrivalAlertStops = { setOf("123456".toNaptanStopIdentifier()) }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { stops, numberOfDepartures ->
                    assertEquals(listOf("123456".toNaptanStopIdentifier()), stops)
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
        val arrivalAlerts = listOf(
            ArrivalAlert(
                id = 1,
                timeAdded = Instant.fromEpochMilliseconds(123L),
                stopIdentifier = "987654".toNaptanStopIdentifier(),
                services = setOf(
                    FakeServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    )
                ),
                timeTriggerMinutes = 5
            )
        )
        val vehicle = createVehicle(4)
        val service = createService(
            serviceDescriptor = FakeServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            vehicles = listOf(vehicle)
        )
        val stop = createStop("123456".toNaptanStopIdentifier(), listOf(service))
        val liveTimes = createLiveTimes(mapOf("123456".toNaptanStopIdentifier() to stop))
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onRemoveArrivalAlertWithId = {
                    fail("Not expecting to remove any arrival alerts.")
                },
                onGetAllArrivalAlerts = { arrivalAlerts },
                onGetAllArrivalAlertStops = { setOf("987654".toNaptanStopIdentifier()) }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { stops, numberOfDepartures ->
                    assertEquals(listOf("987654".toNaptanStopIdentifier()), stops)
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
        val arrivalAlerts = listOf(
            ArrivalAlert(
                id = 1,
                Instant.fromEpochMilliseconds(123L),
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                services = setOf(
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
                ),
                timeTriggerMinutes = 5
            )
        )
        val vehicle = createVehicle(6)
        val service1 = createService(
            serviceDescriptor = FakeServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            vehicles = listOf(vehicle)
        )
        val service2 = createService(
            serviceDescriptor = FakeServiceDescriptor(
                serviceName = "2",
                operatorCode = "TEST2"
            ),
            vehicles = listOf(vehicle)
        )
        val service3 = createService(
            serviceDescriptor = FakeServiceDescriptor(
                serviceName = "3",
                operatorCode = "TEST3"
            ),
            vehicles = listOf(vehicle)
        )
        val stop = createStop(
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            services = listOf(service1, service2, service3)
        )
        val liveTimes = createLiveTimes(mapOf("123456".toNaptanStopIdentifier() to stop))
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onRemoveArrivalAlertWithId = {
                    fail("Not expecting to remove any arrival alerts.")
                },
                onGetAllArrivalAlerts = { arrivalAlerts },
                onGetAllArrivalAlertStops = { setOf("123456".toNaptanStopIdentifier()) }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { stops, numberOfDepartures ->
                    assertEquals(listOf("123456".toNaptanStopIdentifier()), stops)
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
        val arrivalAlert = ArrivalAlert(
            id = 1,
            timeAdded = Instant.fromEpochMilliseconds(123L),
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            services = setOf(
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
            ),
            timeTriggerMinutes = 5
        )
        val vehicle1 = createVehicle(6)
        val vehicle2 = createVehicle(4)
        val vehicle3 = createVehicle(7)
        val service1 = createService(
            serviceDescriptor = FakeServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            vehicles = listOf(vehicle1)
        )
        val service2 = createService(
            serviceDescriptor = FakeServiceDescriptor(
                serviceName = "2",
                operatorCode = "TEST2"
            ),
            vehicles = listOf(vehicle2)
        )
        val service3 = createService(
            serviceDescriptor = FakeServiceDescriptor(
                serviceName = "3",
                operatorCode = "TEST3"
            ),
            vehicles = listOf(vehicle3)
        )
        val stop = createStop(
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            services = listOf(service1, service2, service3)
        )
        val liveTimes = createLiveTimes(mapOf("123456".toNaptanStopIdentifier() to stop))
        var removeArrivalAlertWithIdInvocationCount = 0
        var dispatchTimeAlertNotificationInvocationCount = 0
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onRemoveArrivalAlertWithId = {
                    assertEquals(1, it)
                    removeArrivalAlertWithIdInvocationCount++
                },
                onGetAllArrivalAlerts = { listOf(arrivalAlert) },
                onGetAllArrivalAlertStops = { setOf("123456".toNaptanStopIdentifier()) }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { stops, numberOfDepartures ->
                    assertEquals(listOf("123456".toNaptanStopIdentifier()), stops)
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
        val arrivalAlert = ArrivalAlert(
            id = 1,
            timeAdded = Instant.fromEpochMilliseconds(123L),
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            services = setOf(
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
            ),
            timeTriggerMinutes = 5
        )
        val vehicle1 = createVehicle(6)
        val vehicle2 = createVehicle(4)
        val vehicle3 = createVehicle(0)
        val service1 = createService(
            serviceDescriptor = FakeServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            vehicles = listOf(vehicle1)
        )
        val service2 = createService(
            serviceDescriptor = FakeServiceDescriptor(
                serviceName = "2",
                operatorCode = "TEST2"
            ),
            vehicles = listOf(vehicle2)
        )
        val service3 = createService(
            serviceDescriptor = FakeServiceDescriptor(
                serviceName = "3",
                operatorCode = "TEST3"
            ),
            vehicles = listOf(vehicle3)
        )
        val stop = createStop(
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            services = listOf(service1, service2, service3)
        )
        val liveTimes = createLiveTimes(mapOf("123456".toNaptanStopIdentifier() to stop))
        var removeArrivalAlertWithIdInvocationCount = 0
        var dispatchTimeAlertNotificationInvocationCount = 0
        val checkTimesTask = createCheckTimesTask(
            alertsRepository = FakeAlertsRepository(
                onRemoveArrivalAlertWithId = {
                    assertEquals(1, it)
                    removeArrivalAlertWithIdInvocationCount++
                },
                onGetAllArrivalAlerts = { listOf(arrivalAlert) },
                onGetAllArrivalAlertStops = { setOf("123456".toNaptanStopIdentifier()) }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { stops, numberOfDepartures ->
                    assertEquals(listOf("123456".toNaptanStopIdentifier()), stops)
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
        val arrivalAlert1 = ArrivalAlert(
            id = 1,
            timeAdded = Instant.fromEpochMilliseconds(123L),
            stopIdentifier = "123".toNaptanStopIdentifier(),
            services = setOf(
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
            ),
            timeTriggerMinutes = 10
        )
        val arrivalAlert2 = ArrivalAlert(
            id = 2,
            timeAdded = Instant.fromEpochMilliseconds(123L),
            stopIdentifier = "456".toNaptanStopIdentifier(),
            services = setOf(
                FakeServiceDescriptor(
                    serviceName = "4",
                    operatorCode = "TEST4"
                ),
                FakeServiceDescriptor(
                    serviceName = "5",
                    operatorCode = "TEST5"
                ),
                FakeServiceDescriptor(
                    serviceName = "6",
                    operatorCode = "TEST6"
                )
            ),
            timeTriggerMinutes = 5
        )
        val arrivalAlert3 = ArrivalAlert(
            id = 3,
            timeAdded = Instant.fromEpochMilliseconds(123L),
            stopIdentifier = "789".toNaptanStopIdentifier(),
            services = setOf(
                FakeServiceDescriptor(
                    serviceName = "7",
                    operatorCode = "TEST7"
                ),
                FakeServiceDescriptor(
                    serviceName = "8",
                    operatorCode = "TEST8"
                ),
                FakeServiceDescriptor(
                    serviceName = "9",
                    operatorCode = "TEST9"
                )
            ),
            timeTriggerMinutes = 3
        )
        // Services for stop "123"
        val service1 = createService(
            serviceDescriptor = FakeServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            vehicles = listOf(createVehicle(1))
        )
        val service2 = createService(
            serviceDescriptor = FakeServiceDescriptor(
                serviceName = "2",
                operatorCode = "TEST2"
            ),
            vehicles = listOf(createVehicle(0))
        )
        val service3 = createService(
            serviceDescriptor = FakeServiceDescriptor(
                serviceName = "3",
                operatorCode = "TEST3"
            ),
            vehicles = listOf(createVehicle(2))
        )
        // Services for stop "456"
        val service4 = createService(
            FakeServiceDescriptor(
                serviceName = "4",
                operatorCode = "TEST4"
            ),
            vehicles = listOf(createVehicle(10))
        )
        val service5 = createService(
            serviceDescriptor = FakeServiceDescriptor(
                serviceName = "5",
                operatorCode = "TEST5"
            ),
            vehicles = listOf(createVehicle(6))
        )
        val service6 = createService(
            serviceDescriptor = FakeServiceDescriptor(
                serviceName = "6",
                operatorCode = "TEST6"
            ),
            vehicles = listOf(createVehicle(10))
        )
        // Services for stop "789"
        val service7 = createService(
            serviceDescriptor = FakeServiceDescriptor(
                serviceName = "7",
                operatorCode = "TEST7"
            ),
            listOf(createVehicle(5))
        )
        val service8 = createService(
            serviceDescriptor = FakeServiceDescriptor(
                serviceName = "8",
                operatorCode = "TEST8"
            ),
            vehicles = listOf(createVehicle(6))
        )
        val service9 = createService(
            serviceDescriptor = FakeServiceDescriptor(
                serviceName = "9",
                operatorCode = "TEST9"
            ),
            vehicles = listOf(createVehicle(3))
        )
        // Stops
        val stop1 = createStop("123".toNaptanStopIdentifier(), listOf(service1, service2, service3))
        val stop2 = createStop("456".toNaptanStopIdentifier(), listOf(service4, service5, service6))
        val stop3 = createStop("789".toNaptanStopIdentifier(), listOf(service7, service8, service9))
        val liveTimes = createLiveTimes(
            mapOf(
                "123".toNaptanStopIdentifier() to stop1,
                "456".toNaptanStopIdentifier() to stop2,
                "789".toNaptanStopIdentifier() to stop3
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
                onGetAllArrivalAlertStops = {
                    setOf(
                        "123".toNaptanStopIdentifier(),
                        "456".toNaptanStopIdentifier(),
                        "789".toNaptanStopIdentifier()
                    )
                }
            ),
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithMultipleStops = { stops, numberOfDepartures ->
                    assertEquals(
                        listOf(
                            "123".toNaptanStopIdentifier(),
                            "456".toNaptanStopIdentifier(),
                            "789".toNaptanStopIdentifier()
                        ),
                        stops
                    )
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

    private fun createService(serviceDescriptor: ServiceDescriptor, vehicles: List<Vehicle>) =
        Service(
            serviceDescriptor = serviceDescriptor,
            vehicles = vehicles
        )

    private fun createStop(stopIdentifier: StopIdentifier, services: List<Service>) =
        Stop(
            stopIdentifier = stopIdentifier,
            services = services
        )

    private fun createLiveTimes(stops: Map<StopIdentifier, Stop>) =
        LiveTimes(
            stops = stops,
            receiveTime = Instant.fromEpochMilliseconds(123L),
        )
}
