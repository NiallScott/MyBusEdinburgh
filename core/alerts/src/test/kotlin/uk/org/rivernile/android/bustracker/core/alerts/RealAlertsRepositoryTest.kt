/*
 * Copyright (C) 2020 - 2025 Niall 'Rivernile' Scott
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
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.ArrivalAlertRequest
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.ArrivalAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.FakeArrivalAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.alerts.proximity.FakeProximityAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.alerts.proximity.ProximityAlertRequest
import uk.org.rivernile.android.bustracker.core.alerts.proximity.ProximityAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.AlertsDao
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.ArrivalAlertEntity
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.FakeAlertsDao
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.ProximityAlertEntity
import uk.org.rivernile.android.bustracker.core.time.FakeTimeUtils
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Tests for [AlertsRepository].
 *
 * @author Niall Scott
 */
class RealAlertsRepositoryTest {

    @Test
    fun addArrivalAlertAddsAlertToDaoAndLaunchesArrivalAlertTask() = runTest {
        val launchArrivalAlertTaskInvocationTracker = InvocationTracker()
        val addArrivalAlertTracker = AddArrivalAlertTracker()
        val repository = createAlertsRepository(
            arrivalAlertTaskLauncher = FakeArrivalAlertTaskLauncher(
                onLaunchArrivalAlertTask = launchArrivalAlertTaskInvocationTracker
            ),
            alertsDao = FakeAlertsDao(
                onAddArrivalAlert = addArrivalAlertTracker
            )
        )
        val request = ArrivalAlertRequest("123456", listOf("1", "2", "3"), 5)
        val expected = ArrivalAlertEntity(
            0,
            123L,
            "123456",
            listOf("1", "2", "3"),
            5
        )

        repository.addArrivalAlert(request)

        assertEquals(listOf(expected), addArrivalAlertTracker.addedArrivalAlerts)
        assertEquals(1, launchArrivalAlertTaskInvocationTracker.invocationCount)
    }

    @Test
    fun addProximityAlertAddsAlertToDaoAndLaunchesArrivalAlertTask() = runTest {
        val launchProximityAlertTaskInvocationTracker = InvocationTracker()
        val addedProximityAlertTracker = AddProximityAlertTracker()
        val repository = createAlertsRepository(
            proximityAlertTaskLauncher = FakeProximityAlertTaskLauncher(
                onLaunchProximityAlertTask = launchProximityAlertTaskInvocationTracker
            ),
            alertsDao = FakeAlertsDao(
                onAddProximityAlert = addedProximityAlertTracker
            )
        )
        val request = ProximityAlertRequest("123456", 50)
        val expected = ProximityAlertEntity(
            0,
            123L,
            "123456",
            50
        )

        repository.addProximityAlert(request)

        assertEquals(listOf(expected), addedProximityAlertTracker.addedProximityAlerts)
        assertEquals(1, launchProximityAlertTaskInvocationTracker.invocationCount)
    }

    @Test
    fun removeArrivalAlertWithStopCodeRemovesArrivalAlert() = runTest {
        val removeArrivalAlertTracker = RemoveAlertByStopCodeTracker()
        val repository = createAlertsRepository(
            alertsDao = FakeAlertsDao(
                onRemoveArrivalAlertByStopCode = removeArrivalAlertTracker
            )
        )

        repository.removeArrivalAlert("123456")

        assertEquals(listOf("123456"), removeArrivalAlertTracker.stopCodes)
    }

    @Test
    fun removeArrivalAlertWithIdRemovesArrivalAlert() = runTest {
        val removeArrivalAlertTracker = RemoveAlertByIdTracker()
        val repository = createAlertsRepository(
            alertsDao = FakeAlertsDao(
                onRemoveArrivalAlertById = removeArrivalAlertTracker
            )
        )

        repository.removeArrivalAlert(42)

        assertEquals(listOf(42), removeArrivalAlertTracker.ids)
    }

    @Test
    fun removeAllArrivalAlertsRemovesAllArrivalAlertsInDao() = runTest {
        val removeAllArrivalAlertsTracker = InvocationTracker()
        val repository = createAlertsRepository(
            alertsDao = FakeAlertsDao(
                onRemoveAllArrivalAlerts = removeAllArrivalAlertsTracker
            )
        )

        repository.removeAllArrivalAlerts()

        assertEquals(1, removeAllArrivalAlertsTracker.invocationCount)
    }

    @Test
    fun removeProximityAlertWithStopCodeRemovesProximityAlert() = runTest {
        val removeProximityAlertTracker = RemoveAlertByStopCodeTracker()
        val repository = createAlertsRepository(
            alertsDao = FakeAlertsDao(
                onRemoveProximityAlertByStopCode = removeProximityAlertTracker
            )
        )

        repository.removeProximityAlert("123456")

        assertEquals(listOf("123456"), removeProximityAlertTracker.stopCodes)
    }

    @Test
    fun removeProximityAlertWithIdRemovesProximityAlert() = runTest {
        val removeProximityAlertTracker = RemoveAlertByIdTracker()
        val repository = createAlertsRepository(
            alertsDao = FakeAlertsDao(
                onRemoveProximityAlertById = removeProximityAlertTracker
            )
        )

        repository.removeProximityAlert(42)

        assertEquals(listOf(42), removeProximityAlertTracker.ids)
    }

    @Test
    fun removeAllProximityAlertsRemovesAllProximityAlertsInDao() = runTest {
        val removeAllProximityAlertsTracker = InvocationTracker()
        val repository = createAlertsRepository(
            alertsDao = FakeAlertsDao(
                onRemoveAllProximityAlerts = removeAllProximityAlertsTracker
            )
        )

        repository.removeAllProximityAlerts()

        assertEquals(1, removeAllProximityAlertsTracker.invocationCount)
    }

    @Test
    fun getAllArrivalAlertsMapsNullResult() = runTest {
        val repository = createAlertsRepository(
            alertsDao = FakeAlertsDao(
                onGetAllArrivalAlerts = { null }
            )
        )

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
        val repository = createAlertsRepository(
            alertsDao = FakeAlertsDao(
                onGetAllArrivalAlerts = { items }
            )
        )

        val result = repository.getAllArrivalAlerts()

        assertEquals(expected, result)
    }

    @Test
    fun getAllArrivalAlertStopCodesMapsNullResult() = runTest {
        val repository = createAlertsRepository(
            alertsDao = FakeAlertsDao(
                onGetAllArrivalAlertStopCodes = { null }
            )
        )

        val result = repository.getAllArrivalAlertStopCodes()

        assertNull(result)
    }

    @Test
    fun getAllArrivalAlertStopCodesMapsNonNullResult() = runTest {
        val items = listOf("1", "2", "3")
        val expected = setOf("1", "2", "3")
        val repository = createAlertsRepository(
            alertsDao = FakeAlertsDao(
                onGetAllArrivalAlertStopCodes = { items }
            )
        )

        val result = repository.getAllArrivalAlertStopCodes()

        assertEquals(expected, result)
    }

    @Test
    fun getProximityAlertMapsNullResult() = runTest {
        val repository = createAlertsRepository(
            alertsDao = FakeAlertsDao(
                onGetProximityAlert = { null }
            )
        )

        val result = repository.getProximityAlert(42)

        assertNull(result)
    }

    @Test
    fun hasArrivalAlertFlowEmitsDistinctValues() = runTest {
        val repository = createAlertsRepository(
            alertsDao = FakeAlertsDao(
                onGetHasArrivalAlertFlow = {
                    assertEquals("123456", it)
                    intervalFlowOf(0L, 10L, false, false, true, true, false)
                }
            )
        )

        repository.hasArrivalAlertFlow("123456").test {
            assertFalse(awaitItem())
            assertTrue(awaitItem())
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun hasProximityAlertFlowEmitsDistinctValues() = runTest {
        val repository = createAlertsRepository(
            alertsDao = FakeAlertsDao(
                onGetHasProximityAlertFlow = {
                    intervalFlowOf(0L, 10L, false, false, true, true, false)
                }
            )
        )

        repository.hasProximityAlertFlow("123456").test {
            assertFalse(awaitItem())
            assertTrue(awaitItem())
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun arrivalAlertCountFlowEmitsDistinctValues() = runTest {
        val repository = createAlertsRepository(
            alertsDao = FakeAlertsDao(
                onArrivalAlertCountFlow = { intervalFlowOf(0L, 10L, 0, 0, 1, 1, 2, 2, 3) }
            )
        )

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
        val repository = createAlertsRepository(
            alertsDao = FakeAlertsDao(
                onAllProximityAlertsFlow = { flow }
            )
        )

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
        val repository = createAlertsRepository(
            alertsDao = FakeAlertsDao(
                onAllAlertsFlow = { flow }
            )
        )

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
        val repository = createAlertsRepository(
            arrivalAlertTaskLauncher = FakeArrivalAlertTaskLauncher(
                onLaunchArrivalAlertTask = { fail("Should not launch the arrival alert task.") }
            ),
            proximityAlertTaskLauncher = FakeProximityAlertTaskLauncher(
                onLaunchProximityAlertTask = { fail("Should not launch the proximity alert task") }
            ),
            alertsDao = FakeAlertsDao(
                onGetArrivalAlertCount = { 0 },
                onGetProximityAlertCount = { 0 }
            )
        )

        repository.ensureTasksRunningIfAlertsExists()
    }

    @Test
    fun ensureTasksRunningLaunchesTasks() {
        val launchArrivalAlertTaskInvocationTracker = InvocationTracker()
        val launchProximityAlertTaskInvocationTracker = InvocationTracker()
        val repository = createAlertsRepository(
            arrivalAlertTaskLauncher = FakeArrivalAlertTaskLauncher(
                onLaunchArrivalAlertTask = launchArrivalAlertTaskInvocationTracker
            ),
            proximityAlertTaskLauncher = FakeProximityAlertTaskLauncher(
                onLaunchProximityAlertTask = launchProximityAlertTaskInvocationTracker
            )
        )

        repository.ensureTasksRunning()

        assertEquals(1, launchArrivalAlertTaskInvocationTracker.invocationCount)
        assertEquals(1, launchProximityAlertTaskInvocationTracker.invocationCount)
    }

    @Test
    fun ensureTasksRunningIfAlertsExistStartsArrivalTaskWhenCountIsGreaterThanZero() = runTest {
        val launchArrivalAlertTaskInvocationTracker = InvocationTracker()
        val repository = createAlertsRepository(
            arrivalAlertTaskLauncher = FakeArrivalAlertTaskLauncher(
                onLaunchArrivalAlertTask = launchArrivalAlertTaskInvocationTracker
            ),
            proximityAlertTaskLauncher = FakeProximityAlertTaskLauncher(
                onLaunchProximityAlertTask = { fail("Should not launch the proximity alert task") }
            ),
            alertsDao = FakeAlertsDao(
                onGetArrivalAlertCount = { 1 },
                onGetProximityAlertCount = { 0 }
            )
        )

        repository.ensureTasksRunningIfAlertsExists()

        assertEquals(1, launchArrivalAlertTaskInvocationTracker.invocationCount)
    }

    @Test
    fun ensureTasksRunningIfAlertsExistDoesNotStartProximityTaskWhenCountIsZero() = runTest {
        val repository = createAlertsRepository(
            arrivalAlertTaskLauncher = FakeArrivalAlertTaskLauncher(
                onLaunchArrivalAlertTask = { fail("Should not launch the arrival alert task.") }
            ),
            proximityAlertTaskLauncher = FakeProximityAlertTaskLauncher(
                onLaunchProximityAlertTask = { fail("Should not launch the proximity alert task") }
            ),
            alertsDao = FakeAlertsDao(
                onGetArrivalAlertCount = { 0 },
                onGetProximityAlertCount = { 0 }
            )
        )

        repository.ensureTasksRunningIfAlertsExists()
    }

    @Test
    fun ensureTasksRunningIfAlertsExistStartsProximityTaskWhenCountIsGreaterThanZero() = runTest {
        val launchProximityAlertTaskInvocationTracker = InvocationTracker()
        val repository = createAlertsRepository(
            arrivalAlertTaskLauncher = FakeArrivalAlertTaskLauncher(
                onLaunchArrivalAlertTask = { fail("Should not launch the arrival alert task.") }
            ),
            proximityAlertTaskLauncher = FakeProximityAlertTaskLauncher(
                onLaunchProximityAlertTask = launchProximityAlertTaskInvocationTracker
            ),
            alertsDao = FakeAlertsDao(
                onGetArrivalAlertCount = { 0 },
                onGetProximityAlertCount = { 1 }
            )
        )

        repository.ensureTasksRunningIfAlertsExists()

        assertEquals(1, launchProximityAlertTaskInvocationTracker.invocationCount)
    }

    private fun createAlertsRepository(
        arrivalAlertTaskLauncher: ArrivalAlertTaskLauncher = FakeArrivalAlertTaskLauncher(),
        proximityAlertTaskLauncher: ProximityAlertTaskLauncher = FakeProximityAlertTaskLauncher(),
        alertsDao: AlertsDao = FakeAlertsDao()
    ): RealAlertsRepository {
        return RealAlertsRepository(
            arrivalAlertTaskLauncher = arrivalAlertTaskLauncher,
            proximityAlertTaskLauncher = proximityAlertTaskLauncher,
            alertsDao = alertsDao,
            timeUtils = FakeTimeUtils(
                onGetCurrentTimeMillis = { 123L }
            )
        )
    }

    private class InvocationTracker : () -> Unit {

        var invocationCount = 0
            private set

        override fun invoke() {
            invocationCount++
        }
    }

    private class AddArrivalAlertTracker : (ArrivalAlertEntity) -> Unit {

        val addedArrivalAlerts get() = _addedArrivalAlerts.toList()
        private val _addedArrivalAlerts = mutableListOf<ArrivalAlertEntity>()

        override fun invoke(p1: ArrivalAlertEntity) {
            _addedArrivalAlerts += p1
        }
    }

    private class AddProximityAlertTracker : (ProximityAlertEntity) -> Unit {

        val addedProximityAlerts get() = _addedProximityAlerts.toList()
        private val _addedProximityAlerts = mutableListOf<ProximityAlertEntity>()

        override fun invoke(p1: ProximityAlertEntity) {
            _addedProximityAlerts += p1
        }
    }

    private class RemoveAlertByStopCodeTracker : (String) -> Unit {

        val stopCodes get() = _stopCodes.toList()
        private val _stopCodes = mutableListOf<String>()

        override fun invoke(p1: String) {
            _stopCodes += p1
        }
    }

    private class RemoveAlertByIdTracker : (Int) -> Unit {

        val ids get() = _ids.toList()
        private val _ids = mutableListOf<Int>()

        override fun invoke(p1: Int) {
            _ids += p1
        }
    }
}