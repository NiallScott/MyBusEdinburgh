/*
 * Copyright (C) 2020 - 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.alerts

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.ArrivalAlertRequest
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.ArrivalAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.alerts.proximity.ProximityAlertRequest
import uk.org.rivernile.android.bustracker.core.alerts.proximity.ProximityAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.AlertsDao
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.ArrivalAlertEntity
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.ProximityAlertEntity
import uk.org.rivernile.android.bustracker.core.utils.TimeUtils
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [AlertsRepository].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class AlertsRepositoryTest {

    @Mock
    private lateinit var arrivalAlertTaskLauncher: ArrivalAlertTaskLauncher
    @Mock
    private lateinit var proximityAlertTaskLauncher: ProximityAlertTaskLauncher
    @Mock
    private lateinit var alertsDao: AlertsDao
    @Mock
    private lateinit var timeUtils: TimeUtils

    private lateinit var repository: AlertsRepository

    @BeforeTest
    fun setUp() {
        repository = AlertsRepository(
            arrivalAlertTaskLauncher,
            proximityAlertTaskLauncher,
            alertsDao,
            timeUtils
        )

        whenever(timeUtils.currentTimeMills)
            .thenReturn(123L)
    }

    @Test
    fun addArrivalAlertAddsAlertToDaoAndLaunchesArrivalAlertTask() = runTest {
        val request = ArrivalAlertRequest("123456", listOf("1", "2", "3"), 5)
        val expected = ArrivalAlertEntity(
            0,
            123L,
            "123456",
            listOf("1", "2", "3"),
            5
        )

        repository.addArrivalAlert(request)

        verify(alertsDao)
            .addArrivalAlert(expected)
        verify(arrivalAlertTaskLauncher)
            .launchArrivalAlertTask()
    }

    @Test
    fun addProximityAlertAddsAlertToDaoAndLaunchesArrivalAlertTask() = runTest {
        val request = ProximityAlertRequest("123456", 50)
        val expected = ProximityAlertEntity(
            0,
            123L,
            "123456",
            50
        )

        repository.addProximityAlert(request)

        verify(alertsDao)
            .addProximityAlert(expected)
        verify(proximityAlertTaskLauncher)
            .launchProximityAlertTask()
    }

    @Test
    fun removeArrivalAlertWithStopCodeRemovesArrivalAlert() = runTest {
        repository.removeArrivalAlert("123456")

        verify(alertsDao)
            .removeArrivalAlert("123456")
    }

    @Test
    fun removeArrivalAlertWithIdRemovesArrivalAlert() = runTest {
        repository.removeArrivalAlert(42)

        verify(alertsDao)
            .removeArrivalAlert(42)
    }

    @Test
    fun removeAllArrivalAlertsRemovesAllArrivalAlertsInDao() = runTest {
        repository.removeAllArrivalAlerts()

        verify(alertsDao)
            .removeAllArrivalAlerts()
    }

    @Test
    fun removeProximityAlertWithStopCodeRemovesProximityAlert() = runTest {
        repository.removeProximityAlert("123456")

        verify(alertsDao)
            .removeProximityAlert("123456")
    }

    @Test
    fun removeProximityAlertWithIdRemovesProximityAlert() = runTest {
        repository.removeProximityAlert(42)

        verify(alertsDao)
            .removeProximityAlert(42)
    }

    @Test
    fun removeAllProximityAlertsRemovesAllProximityAlertsInDao() = runTest {
        repository.removeAllProximityAlerts()

        verify(alertsDao)
            .removeAllProximityAlerts()
    }

    @Test
    fun getAllArrivalAlertsMapsNullResult() = runTest {
        whenever(alertsDao.getAllArrivalAlerts())
            .thenReturn(null)

        val result = repository.getAllArrivalAlerts()

        assertNull(result)
    }

    @Test
    fun getAllArrivalAlertsMapsNonNullResult() = runTest {
        val items = listOf(
            ArrivalAlertEntity(
                1,
                10L,
                "1",
                listOf("1"),
                1
            ),
            ArrivalAlertEntity(
                2,
                20L,
                "2",
                listOf("1", "2"),
                2
            ),
            ArrivalAlertEntity(
                3,
                30L,
                "3",
                listOf("1", "2", "3"),
                3
            )
        )
        val expected = listOf(
            ArrivalAlert(
                1,
                10L,
                "1",
                listOf("1"),
                1
            ),
            ArrivalAlert(
                2,
                20L,
                "2",
                listOf("1", "2"),
                2
            ),
            ArrivalAlert(
                3,
                30L,
                "3",
                listOf("1", "2", "3"),
                3
            )
        )
        whenever(alertsDao.getAllArrivalAlerts())
            .thenReturn(items)

        val result = repository.getAllArrivalAlerts()

        assertEquals(expected, result)
    }

    @Test
    fun getAllArrivalAlertStopCodesMapsNullResult() = runTest {
        whenever(alertsDao.getAllArrivalAlertStopCodes())
            .thenReturn(null)

        val result = repository.getAllArrivalAlertStopCodes()

        assertNull(result)
    }

    @Test
    fun getAllArrivalAlertStopCodesMapsNonNullResult() = runTest {
        val items = listOf("1", "2", "3")
        val expected = setOf("1", "2", "3")
        whenever(alertsDao.getAllArrivalAlertStopCodes())
            .thenReturn(items)

        val result = repository.getAllArrivalAlertStopCodes()

        assertEquals(expected, result)
    }

    @Test
    fun getProximityAlertMapsNullResult() = runTest {
        whenever(alertsDao.getProximityAlert(42))
            .thenReturn(null)

        val result = repository.getProximityAlert(42)

        assertNull(result)
    }

    @Test
    fun hasArrivalAlertFlowEmitsDistinctValues() = runTest {
        whenever(alertsDao.getHasArrivalAlertFlow("123456"))
            .thenReturn(intervalFlowOf(0L, 10L, false, false, true, true, false))

        repository.hasArrivalAlertFlow("123456").test {
            assertFalse(awaitItem())
            assertTrue(awaitItem())
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun hasProximityAlertFlowEmitsDistinctValues() = runTest {
        whenever(alertsDao.getHasProximityAlertFlow("123456"))
            .thenReturn(intervalFlowOf(0L, 10L, false, false, true, true, false))

        repository.hasProximityAlertFlow("123456").test {
            assertFalse(awaitItem())
            assertTrue(awaitItem())
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun arrivalAlertCountFlowEmitsDistinctValues() = runTest {
        whenever(alertsDao.arrivalAlertCountFlow)
            .thenReturn(intervalFlowOf(0L, 10L, 0, 0, 1, 1, 2, 2, 3))

        repository.arrivalAlertCountFlow.test {
            assertEquals(0, awaitItem())
            assertEquals(1, awaitItem())
            assertEquals(2, awaitItem())
            assertEquals(3, awaitItem())
        }
    }

    @Test
    fun allProximityAlertsFlowEmitsDistinctValues() = runTest {
        val alert1 = ProximityAlertEntity(1, 1L, "1", 1)
        val alert2 = ProximityAlertEntity(2, 2L, "2", 2)
        val alert3 = ProximityAlertEntity(3, 3L, "3", 3)
        val expected1 = ProximityAlert(1, 1L, "1", 1)
        val expected2 = ProximityAlert(2, 2L, "2", 2)
        val expected3 = ProximityAlert(3, 3L, "3", 3)
        val flow = intervalFlowOf(
            0L,
            10L,
            null,
            null,
            listOf(alert1),
            listOf(alert1),
            listOf(alert1, alert2, alert3),
            listOf(alert2)
        )
        whenever(alertsDao.allProximityAlertsFlow)
            .thenReturn(flow)

        repository.allProximityAlertsFlow.test {
            assertNull(awaitItem())
            assertEquals(listOf(expected1), awaitItem())
            assertEquals(listOf(expected1, expected2, expected3), awaitItem())
            assertEquals(listOf(expected2), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allAlertsFlowEmitsDistinctValues() = runTest {
        val arrivalAlert1 = ArrivalAlertEntity(1, 1L, "1", listOf("1", "2", "3"), 1)
        val arrivalAlert2 = ArrivalAlertEntity(2, 2L, "2", listOf("4", "5", "6"), 6)
        val proximityAlert1 = ProximityAlertEntity(3, 3L, "3", 3)
        val proximityAlert2 = ProximityAlertEntity(4, 4L, "4", 4)
        val expectedArrivalAlert1 = ArrivalAlert(1, 1L, "1", listOf("1", "2", "3"), 1)
        val expectedArrivalAlert2 = ArrivalAlert(2, 2L, "2", listOf("4", "5", "6"), 6)
        val expectedProximityAlert1 = ProximityAlert(3, 3L, "3", 3)
        val expectedProximityAlert2 = ProximityAlert(4, 4L, "4", 4)
        val flow = intervalFlowOf(
            0L,
            10L,
            null,
            null,
            listOf(arrivalAlert1),
            listOf(arrivalAlert1),
            listOf(proximityAlert1),
            listOf(proximityAlert1),
            listOf(arrivalAlert1, arrivalAlert2, proximityAlert1, proximityAlert2),
            listOf(arrivalAlert1, arrivalAlert2, proximityAlert1, proximityAlert2),
            listOf(arrivalAlert2, proximityAlert2)
        )
        whenever(alertsDao.allAlertsFlow)
            .thenReturn(flow)

        repository.allAlertsFlow.test {
            assertNull(awaitItem())
            assertEquals(listOf(expectedArrivalAlert1), awaitItem())
            assertEquals(listOf(expectedProximityAlert1), awaitItem())
            assertEquals(
                listOf(
                    expectedArrivalAlert1,
                    expectedArrivalAlert2,
                    expectedProximityAlert1,
                    expectedProximityAlert2
                ),
                awaitItem()
            )
            assertEquals(listOf(expectedArrivalAlert2, expectedProximityAlert2), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun ensureTasksRunningIfAlertsExistDoesNotStartArrivalTaskWhenCountIsZero() = runTest {
        whenever(alertsDao.getArrivalAlertCount())
            .thenReturn(0)
        whenever(alertsDao.getProximityAlertCount())
            .thenReturn(0)

        repository.ensureTasksRunningIfAlertsExists()

        verify(arrivalAlertTaskLauncher, never())
            .launchArrivalAlertTask()
    }

    @Test
    fun ensureTasksRunningLaunchesTasks() {
        repository.ensureTasksRunning()

        verify(arrivalAlertTaskLauncher)
            .launchArrivalAlertTask()
        verify(proximityAlertTaskLauncher)
            .launchProximityAlertTask()
    }

    @Test
    fun ensureTasksRunningIfAlertsExistStartsArrivalTaskWhenCountIsGreaterThanZero() = runTest {
        whenever(alertsDao.getArrivalAlertCount())
            .thenReturn(1)
        whenever(alertsDao.getProximityAlertCount())
            .thenReturn(0)

        repository.ensureTasksRunningIfAlertsExists()

        verify(arrivalAlertTaskLauncher)
            .launchArrivalAlertTask()
    }

    @Test
    fun ensureTasksRunningIfAlertsExistDoesNotStartProximityTaskWhenCountIsZero() = runTest {
        whenever(alertsDao.getProximityAlertCount())
            .thenReturn(0)
        whenever(alertsDao.getArrivalAlertCount())
            .thenReturn(0)

        repository.ensureTasksRunningIfAlertsExists()

        verify(proximityAlertTaskLauncher, never())
            .launchProximityAlertTask()
    }

    @Test
    fun ensureTasksRunningIfAlertsExistStartsProximityTaskWhenCountIsGreaterThanZero() = runTest {
        whenever(alertsDao.getProximityAlertCount())
            .thenReturn(1)
        whenever(alertsDao.getArrivalAlertCount())
            .thenReturn(0)

        repository.ensureTasksRunningIfAlertsExists()

        verify(proximityAlertTaskLauncher)
            .launchProximityAlertTask()
    }
}