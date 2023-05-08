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

package uk.org.rivernile.android.bustracker.core.alerts

import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.ArrivalAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.alerts.proximity.ProximityAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.database.settings.daos.AlertsDao
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ArrivalAlert
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ProximityAlert
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule

/**
 * Tests for [AlertManager].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class AlertManagerTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var alertsDao: AlertsDao
    @Mock
    private lateinit var arrivalAlertTaskLauncher: ArrivalAlertTaskLauncher
    @Mock
    private lateinit var proximityAlertTaskLauncher: ProximityAlertTaskLauncher

    private lateinit var alertManager: AlertManager

    @Before
    fun setUp() {
        alertManager = AlertManager(
                alertsDao,
                arrivalAlertTaskLauncher,
                proximityAlertTaskLauncher)
    }

    @Test
    fun addArrivalAlertAddsAlertToDaoThenLaunchesTask() = runTest {
        val arrivalAlert = ArrivalAlert(1, 123L, "123456", listOf("1", "2", "3"), 5)

        alertManager.addArrivalAlert(arrivalAlert)

        inOrder(alertsDao, arrivalAlertTaskLauncher) {
            verify(alertsDao)
                    .addArrivalAlert(arrivalAlert)
            verify(arrivalAlertTaskLauncher)
                    .launchArrivalAlertTask()
        }
    }

    @Test
    fun removeArrivalAlertRemovesArrivalAlert() = runTest {
        alertManager.removeArrivalAlert("123456")

        verify(alertsDao)
                .removeArrivalAlert("123456")
    }

    @Test
    fun removeAllArrivalAlertsRemovesAllArrivalAlerts() = runTest {
        alertManager.removeAllArrivalAlerts()

        verify(alertsDao)
                .removeAllArrivalAlerts()
    }

    @Test
    fun addProximityAlertAddsAlertToDaoThenLaunchesTask() = runTest {
        val proximityAlert = ProximityAlert(1, 123L, "123456", 250)

        alertManager.addProximityAlert(proximityAlert)

        inOrder(alertsDao, proximityAlertTaskLauncher) {
            verify(alertsDao)
                    .addProximityAlert(proximityAlert)
            verify(proximityAlertTaskLauncher)
                    .launchProximityAlertTask()
        }
    }

    @Test
    fun removeProximityAlertRemovesProximityAlert() = runTest {
        alertManager.removeProximityAlert("123456")

        verify(alertsDao)
                .removeProximityAlert("123456")
    }

    @Test
    fun removeAllProximityAlertsRemovesAllProximityAlerts() = runTest {
        alertManager.removeAllProximityAlerts()

        verify(alertsDao)
                .removeAllProximityAlerts()
    }

    @Test
    fun ensureTasksRunningIfAlertsExistDoesNotStartArrivalTaskWhenCountIsZero() = runTest {
        whenever(alertsDao.getArrivalAlertCount())
                .thenReturn(0)
        whenever(alertsDao.getProximityAlertCount())
                .thenReturn(0)

        alertManager.ensureTasksRunningIfAlertsExists()

        verify(arrivalAlertTaskLauncher, never())
                .launchArrivalAlertTask()
    }

    @Test
    fun ensureTasksRunningIfAlertsExistStartsArrivalTaskWhenCountIsGreaterThanZero() = runTest {
        whenever(alertsDao.getArrivalAlertCount())
                .thenReturn(1)
        whenever(alertsDao.getProximityAlertCount())
                .thenReturn(0)

        alertManager.ensureTasksRunningIfAlertsExists()

        verify(arrivalAlertTaskLauncher)
                .launchArrivalAlertTask()
    }

    @Test
    fun ensureTasksRunningIfAlertsExistDoesNotStartProximityTaskWhenCountIsZero() = runTest {
        whenever(alertsDao.getProximityAlertCount())
                .thenReturn(0)
        whenever(alertsDao.getArrivalAlertCount())
                .thenReturn(0)

        alertManager.ensureTasksRunningIfAlertsExists()

        verify(proximityAlertTaskLauncher, never())
                .launchProximityAlertTask()
    }

    @Test
    fun ensureTasksRunningIfAlertsExistStartsProximityTaskWhenCountIsGreaterThanZero() = runTest {
        whenever(alertsDao.getProximityAlertCount())
                .thenReturn(1)
        whenever(alertsDao.getArrivalAlertCount())
                .thenReturn(0)

        alertManager.ensureTasksRunningIfAlertsExists()

        verify(proximityAlertTaskLauncher)
                .launchProximityAlertTask()
    }
}