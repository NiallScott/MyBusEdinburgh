/*
 * Copyright (C) 2019 - 2020 Niall 'Rivernile' Scott
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

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.alerts.AlertNotificationDispatcher
import uk.org.rivernile.android.bustracker.core.database.settings.daos.AlertsDao
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ArrivalAlert
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.TrackerEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.TrackerRequest
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.UnrecognisedServerErrorException
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimes
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.Service
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.Stop
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.Vehicle
import java.util.Date

/**
 * Unit tests for [CheckTimesTask].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class CheckTimesTaskTest {

    @Mock
    lateinit var alertsDao: AlertsDao
    @Mock
    lateinit var trackerEndpoint: TrackerEndpoint
    @Mock
    lateinit var alertNotificationDispatcher: AlertNotificationDispatcher

    @Mock
    lateinit var trackerRequest: TrackerRequest<LiveTimes>

    private lateinit var checkTimesTask: CheckTimesTask

    @Before
    fun setUp() {
        checkTimesTask = CheckTimesTask(alertsDao, trackerEndpoint, alertNotificationDispatcher)
    }

    @Test
    fun doesNotCreateRequestWhenArrivalAlertStopCodesIsNull() {
        whenever(alertsDao.getAllArrivalAlertStopCodes())
                .thenReturn(null)

        checkTimesTask.checkTimes()

        verify(trackerEndpoint, never())
                .createLiveTimesRequest(any<Array<String>>(), any())
    }

    @Test
    fun doesNotCreateRequestWhenArrivalAlertStopCodesIsEmpty() {
        whenever(alertsDao.getAllArrivalAlertStopCodes())
                .thenReturn(emptyList())

        checkTimesTask.checkTimes()

        verify(trackerEndpoint, never())
                .createLiveTimesRequest(any<Array<String>>(), any())
    }

    @Test
    fun createsRequestWithExpectedStopCodeWhenSingleStopCodeIsSupplied() {
        givenTrackerEndpointCreatesRequest()
        val stopCodes = listOf("123456")
        whenever(alertsDao.getAllArrivalAlertStopCodes())
                .thenReturn(stopCodes)

        checkTimesTask.checkTimes()

        verify(trackerEndpoint)
                .createLiveTimesRequest(stopCodes.toTypedArray(), 1)
    }

    @Test
    fun createsRequestWithExpectedStopCodesWhenMultipleStopCodesAreSupplied() {
        givenTrackerEndpointCreatesRequest()
        val stopCodes = listOf("123456", "987654", "246802")
        whenever(alertsDao.getAllArrivalAlertStopCodes())
                .thenReturn(stopCodes)

        checkTimesTask.checkTimes()

        verify(trackerEndpoint)
                .createLiveTimesRequest(stopCodes.toTypedArray(), 1)
    }

    @Test
    fun failingRequestFailsSilently() {
        givenTrackerEndpointCreatesRequest()
        givenArrivalAlertStopCodes(listOf("123456"))
        whenever(trackerRequest.performRequest())
                .thenThrow(UnrecognisedServerErrorException::class.java)

        checkTimesTask.checkTimes()
    }

    @Test
    fun nullArrivalAlertsDoesNotCauseNotifications() {
        givenTrackerEndpointCreatesRequest()
        givenArrivalAlertStopCodes(listOf("123456"))
        givenArrivalAlerts(null)

        checkTimesTask.checkTimes()

        verify(alertNotificationDispatcher, never())
                .dispatchTimeAlertNotification(any(), any())
        verify(alertsDao, never())
                .removeArrivalAlert(any())
    }

    @Test
    fun emptyArrivalAlertsDoesNotCauseNotifications() {
        givenTrackerEndpointCreatesRequest()
        givenArrivalAlertStopCodes(listOf("123456"))
        givenArrivalAlerts(emptyList())

        checkTimesTask.checkTimes()

        verify(alertNotificationDispatcher, never())
                .dispatchTimeAlertNotification(any(), any())
        verify(alertsDao, never())
                .removeArrivalAlert(any())
    }

    @Test
    fun singleServiceForSingleStopThatDoesNotMeetTimeTriggerDoesNotCauseNotification() {
        givenTrackerEndpointCreatesRequest()
        givenArrivalAlertStopCodes(listOf("123456"))
        val arrivalAlerts = listOf(ArrivalAlert(1, 123L, "123456", listOf("1"), 5))
        givenArrivalAlerts(arrivalAlerts)
        val vehicle = createVehicle(6)
        val service = createService("1", listOf(vehicle))
        val stop = createStop("123456", listOf(service))
        val liveTimes = createLiveTimes(mapOf("123456" to stop))
        whenever(trackerRequest.performRequest())
                .thenReturn(liveTimes)

        checkTimesTask.checkTimes()

        verify(alertNotificationDispatcher, never())
                .dispatchTimeAlertNotification(any(), any())
        verify(alertsDao, never())
                .removeArrivalAlert(any())
    }

    @Test
    fun singleServiceForSingleStopThatEqualsTimeTriggerCausesNotification() {
        givenTrackerEndpointCreatesRequest()
        givenArrivalAlertStopCodes(listOf("123456"))
        val arrivalAlert = ArrivalAlert(1, 123L, "123456", listOf("1"), 5)
        givenArrivalAlerts(listOf(arrivalAlert))
        val vehicle = createVehicle(5)
        val service = createService("1", listOf(vehicle))
        val stop = createStop("123456", listOf(service))
        val liveTimes = createLiveTimes(mapOf("123456" to stop))
        whenever(trackerRequest.performRequest())
                .thenReturn(liveTimes)

        checkTimesTask.checkTimes()

        verify(alertNotificationDispatcher)
                .dispatchTimeAlertNotification(arrivalAlert, listOf(service))
        verify(alertsDao)
                .removeArrivalAlert(1)
    }

    @Test
    fun singleServiceForSingleStopThatIsLessThanTimeTriggerCausesNotification() {
        givenTrackerEndpointCreatesRequest()
        givenArrivalAlertStopCodes(listOf("123456"))
        val arrivalAlert = ArrivalAlert(1, 123L, "123456", listOf("1"), 5)
        givenArrivalAlerts(listOf(arrivalAlert))
        val vehicle = createVehicle(4)
        val service = createService("1", listOf(vehicle))
        val stop = createStop("123456", listOf(service))
        val liveTimes = createLiveTimes(mapOf("123456" to stop))
        whenever(trackerRequest.performRequest())
                .thenReturn(liveTimes)

        checkTimesTask.checkTimes()

        verify(alertNotificationDispatcher)
                .dispatchTimeAlertNotification(arrivalAlert, listOf(service))
        verify(alertsDao)
                .removeArrivalAlert(1)
    }

    @Test
    fun stopNotFoundInArrivalAlertsDoesNotCauseNotification() {
        givenTrackerEndpointCreatesRequest()
        givenArrivalAlertStopCodes(listOf("987654"))
        val arrivalAlerts = listOf(ArrivalAlert(1, 123L, "987654", listOf("1"), 5))
        givenArrivalAlerts(arrivalAlerts)
        val vehicle = createVehicle(4)
        val service = createService("1", listOf(vehicle))
        val stop = createStop("123456", listOf(service))
        val liveTimes = createLiveTimes(mapOf("123456" to stop))
        whenever(trackerRequest.performRequest())
                .thenReturn(liveTimes)

        checkTimesTask.checkTimes()

        verify(alertNotificationDispatcher, never())
                .dispatchTimeAlertNotification(any(), any())
        verify(alertsDao, never())
                .removeArrivalAlert(any())
    }

    @Test
    fun multipleServicesForSingleStopThatDoesNotMeetTimeTriggerDoesNotCauseNotification() {
        givenTrackerEndpointCreatesRequest()
        givenArrivalAlertStopCodes(listOf("123456"))
        val arrivalAlerts = listOf(ArrivalAlert(1, 123L, "123456", listOf("1", "2", "3"), 5))
        givenArrivalAlerts(arrivalAlerts)
        val vehicle = createVehicle(6)
        val service1 = createService("1", listOf(vehicle))
        val service2 = createService("2", listOf(vehicle))
        val service3 = createService("3", listOf(vehicle))
        val stop = createStop("123456", listOf(service1, service2, service3))
        val liveTimes = createLiveTimes(mapOf("123456" to stop))
        whenever(trackerRequest.performRequest())
                .thenReturn(liveTimes)

        checkTimesTask.checkTimes()

        verify(alertNotificationDispatcher, never())
                .dispatchTimeAlertNotification(any(), any())
        verify(alertsDao, never())
                .removeArrivalAlert(any())
    }

    @Test
    fun singleServiceForSingleStopThatSatisfiesTimeTriggerCausesNotification() {
        givenTrackerEndpointCreatesRequest()
        givenArrivalAlertStopCodes(listOf("123456"))
        val arrivalAlert = ArrivalAlert(1, 123L, "123456", listOf("1", "2", "3"), 5)
        givenArrivalAlerts(listOf(arrivalAlert))
        val vehicle1 = createVehicle(6)
        val vehicle2 = createVehicle(4)
        val vehicle3 = createVehicle(7)
        val service1 = createService("1", listOf(vehicle1))
        val service2 = createService("2", listOf(vehicle2))
        val service3 = createService("3", listOf(vehicle3))
        val stop = createStop("123456", listOf(service1, service2, service3))
        val liveTimes = createLiveTimes(mapOf("123456" to stop))
        whenever(trackerRequest.performRequest())
                .thenReturn(liveTimes)

        checkTimesTask.checkTimes()

        verify(alertNotificationDispatcher)
                .dispatchTimeAlertNotification(arrivalAlert, listOf(service2))
        verify(alertsDao)
                .removeArrivalAlert(1)
    }

    @Test
    fun multipleServicesForSingleStopThatSatisfiesTimeTriggerCausesNotifications() {
        givenTrackerEndpointCreatesRequest()
        givenArrivalAlertStopCodes(listOf("123456"))
        val arrivalAlert = ArrivalAlert(1, 123L, "123456", listOf("1", "2", "3"), 5)
        givenArrivalAlerts(listOf(arrivalAlert))
        val vehicle1 = createVehicle(6)
        val vehicle2 = createVehicle(4)
        val vehicle3 = createVehicle(0)
        val service1 = createService("1", listOf(vehicle1))
        val service2 = createService("2", listOf(vehicle2))
        val service3 = createService("3", listOf(vehicle3))
        val stop = createStop("123456", listOf(service1, service2, service3))
        val liveTimes = createLiveTimes(mapOf("123456" to stop))
        whenever(trackerRequest.performRequest())
                .thenReturn(liveTimes)

        checkTimesTask.checkTimes()

        verify(alertNotificationDispatcher)
                .dispatchTimeAlertNotification(arrivalAlert, listOf(service2, service3))
        verify(alertsDao)
                .removeArrivalAlert(1)
    }

    @Test
    fun multipleStopsWithMultipleCombinationsYieldsExpectedResults() {
        givenTrackerEndpointCreatesRequest()
        givenArrivalAlertStopCodes(listOf("123", "456", "789"))
        val arrivalAlert1 = ArrivalAlert(1, 123L, "123", listOf("1", "2", "3"), 10)
        val arrivalAlert2 = ArrivalAlert(2, 123L, "456", listOf("4", "5", "6"), 5)
        val arrivalAlert3 = ArrivalAlert(3, 123L, "789", listOf("7", "8", "9"), 3)
        givenArrivalAlerts(listOf(arrivalAlert1, arrivalAlert2, arrivalAlert3))
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
        val liveTimes = createLiveTimes(mapOf(
                "123" to stop1,
                "456" to stop2,
                "789" to stop3))
        whenever(trackerRequest.performRequest())
                .thenReturn(liveTimes)

        checkTimesTask.checkTimes()

        verify(alertNotificationDispatcher)
                .dispatchTimeAlertNotification(arrivalAlert1, listOf(service1, service2, service3))
        verify(alertNotificationDispatcher, never())
                .dispatchTimeAlertNotification(eq(arrivalAlert2), any())
        verify(alertNotificationDispatcher)
                .dispatchTimeAlertNotification(arrivalAlert3, listOf(service9))
        verify(alertsDao)
                .removeArrivalAlert(1)
        verify(alertsDao, never())
                .removeArrivalAlert(2)
        verify(alertsDao)
                .removeArrivalAlert(3)
    }

    private fun givenTrackerEndpointCreatesRequest() {
        whenever(trackerEndpoint.createLiveTimesRequest(any<Array<String>>(), any()))
                .thenReturn(trackerRequest)
    }

    private fun givenArrivalAlertStopCodes(stopCodes: List<String>) {
        whenever(alertsDao.getAllArrivalAlertStopCodes())
                .thenReturn(stopCodes)
    }

    private fun givenArrivalAlerts(arrivalAlerts: List<ArrivalAlert>?) {
        whenever(alertsDao.getAllArrivalAlerts())
                .thenReturn(arrivalAlerts)
    }

    private fun createVehicle(departureMinutes: Int) =
            Vehicle(
                    "a",
                    Date(),
                    departureMinutes,
                    null,
                    null,
                    isEstimatedTime = false,
                    isDelayed = false,
                    isDiverted = false,
                    isTerminus = false,
                    isPartRoute = false)

    private fun createService(serviceName: String, vehicles: List<Vehicle>) =
            Service(
                    serviceName,
                    vehicles,
                    null,
                    null,
                    isDisrupted = false,
                    isDiverted = false)

    private fun createStop(stopCode: String, services: List<Service>) =
            Stop(
                    stopCode,
                    null,
                    services,
                    false)

    private fun createLiveTimes(stops: Map<String, Stop>) =
            LiveTimes(
                    stops,
                    123L,
                    false)
}