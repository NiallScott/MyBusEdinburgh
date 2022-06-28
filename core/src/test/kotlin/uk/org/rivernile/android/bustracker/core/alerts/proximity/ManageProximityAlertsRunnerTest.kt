/*
 * Copyright (C) 2020 - 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.alerts.proximity

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.database.settings.daos.AlertsDao
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ProximityAlert
import java.util.concurrent.ExecutorService

/**
 * Tests for [ManageProximityAlertsRunner].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class ManageProximityAlertsRunnerTest {

    @Mock
    private lateinit var alertsDao: AlertsDao
    @Mock
    private lateinit var proximityAlertTracker: ProximityAlertTracker
    @Mock
    private lateinit var backgroundExecutor: ExecutorService

    @Mock
    private lateinit var stopListener: () -> Unit

    private lateinit var runner: ManageProximityAlertsRunner

    @Before
    fun setUp() {
        runner = ManageProximityAlertsRunner(
                alertsDao,
                proximityAlertTracker,
                backgroundExecutor)
    }

    @Test
    fun startRunnerRegistersCallbacksListenerWithAlertsDao() {
        runner.start(null)

        verify(alertsDao)
                .addOnAlertsChangedListener(any())
    }

    @Test
    fun startRunnerFetchesCurrentProximityAlertsFromDao() {
        givenBackgroundExecutorExecutesTask()

        runner.start(null)

        verify(alertsDao)
                .getAllProximityAlerts()
    }

    @Test
    fun stopRunnerRemovesCallbackListenerWithAlertDao() {
        runner.start(null)
        runner.stop()

        verify(alertsDao)
                .removeOnAlertsChangedListener(any())
    }

    @Test
    fun stopRunnerShutsDownExecutorService() {
        runner.start(null)
        runner.stop()

        verify(backgroundExecutor)
                .shutdownNow()
    }

    @Test
    fun stopRunnerCallsStopListener() {
        runner.start(stopListener)
        runner.stop()

        verify(stopListener)
                .invoke()
    }

    @Test
    fun startRunnerOnlyExecutesOnce() {
        givenBackgroundExecutorExecutesTask()
        runner.start(null)
        runner.start(null)

        verify(alertsDao, times(1))
                .addOnAlertsChangedListener(any())
        verify(alertsDao, times(1))
                .getAllProximityAlerts()
    }

    @Test
    fun stoppedRunnerCannotBeStarted() {
        runner.stop()
        runner.start(null)

        verify(alertsDao, never())
                .addOnAlertsChangedListener(any())
        verify(alertsDao, never())
                .getAllProximityAlerts()
    }

    @Test
    fun runnerIsStoppedIfStartedWithNullProximityAlerts() {
        givenBackgroundExecutorExecutesTask()
        whenever(alertsDao.getAllProximityAlerts())
                .thenReturn(null)

        runner.start(stopListener)

        verify(stopListener)
                .invoke()
    }

    @Test
    fun runnerIsStoppedIfStartedWithEmptyProximityAlerts() {
        givenBackgroundExecutorExecutesTask()
        whenever(alertsDao.getAllProximityAlerts())
                .thenReturn(emptyList())

        runner.start(stopListener)

        verify(stopListener)
                .invoke()
    }

    @Test
    fun initialAlertsAreTracked() {
        givenBackgroundExecutorExecutesTask()
        val alert1 = ProximityAlert(1, 101L, "100001", 10)
        val alert2 = ProximityAlert(2, 102L, "100002", 20)
        val alert3 = ProximityAlert(3, 103L, "100003", 30)
        val alerts = listOf(alert1, alert2, alert3)
        whenever(alertsDao.getAllProximityAlerts())
                .thenReturn(alerts)

        runner.start(stopListener)

        verify(proximityAlertTracker)
                .trackProximityAlert(alert1)
        verify(proximityAlertTracker)
                .trackProximityAlert(alert2)
        verify(proximityAlertTracker)
                .trackProximityAlert(alert3)
        verify(proximityAlertTracker, never())
                .removeProximityAlert(any())
        verify(stopListener, never())
                .invoke()
    }

    @Test
    fun alertsChangedWithNoProximityAlertsRemovesAllTrackedAlerts() {
        givenBackgroundExecutorExecutesTask()
        val alert1 = ProximityAlert(1, 101L, "100001", 10)
        val alert2 = ProximityAlert(2, 102L, "100002", 20)
        val alert3 = ProximityAlert(3, 103L, "100003", 30)
        val alerts = listOf(alert1, alert2, alert3)
        whenever(alertsDao.getAllProximityAlerts())
                .thenReturn(alerts, null)

        runner.start(stopListener)
        argumentCaptor<AlertsDao.OnAlertsChangedListener> {
            verify(alertsDao)
                    .addOnAlertsChangedListener(capture())
            firstValue.onAlertsChanged()
        }

        verify(proximityAlertTracker)
                .removeProximityAlert(1)
        verify(proximityAlertTracker)
                .removeProximityAlert(2)
        verify(proximityAlertTracker)
                .removeProximityAlert(3)
        verify(stopListener)
                .invoke()
    }

    @Test
    fun alertsChangedWithRemovalRemovesTrackedAlert() {
        givenBackgroundExecutorExecutesTask()
        val alert1 = ProximityAlert(1, 101L, "100001", 10)
        val alert2 = ProximityAlert(2, 102L, "100002", 20)
        val alert3 = ProximityAlert(3, 103L, "100003", 30)
        val alerts1 = listOf(alert1, alert2, alert3)
        val alerts2 = listOf(alert1, alert3)
        whenever(alertsDao.getAllProximityAlerts())
                .thenReturn(alerts1, alerts2)

        runner.start(stopListener)
        argumentCaptor<AlertsDao.OnAlertsChangedListener> {
            verify(alertsDao)
                    .addOnAlertsChangedListener(capture())
            firstValue.onAlertsChanged()
        }

        verify(proximityAlertTracker, never())
                .removeProximityAlert(1)
        verify(proximityAlertTracker)
                .removeProximityAlert(2)
        verify(proximityAlertTracker, never())
                .removeProximityAlert(3)
        verify(stopListener, never())
                .invoke()
    }

    @Test
    fun alertsChangedWithAdditionAddsTrackedAlert() {
        givenBackgroundExecutorExecutesTask()
        val alert1 = ProximityAlert(1, 101L, "100001", 10)
        val alert2 = ProximityAlert(2, 102L, "100002", 20)
        val alert3 = ProximityAlert(3, 103L, "100003", 30)
        val alerts1 = listOf(alert1, alert2)
        val alerts2 = listOf(alert1, alert2, alert3)
        whenever(alertsDao.getAllProximityAlerts())
                .thenReturn(alerts1, alerts2)

        runner.start(stopListener)
        argumentCaptor<AlertsDao.OnAlertsChangedListener> {
            verify(alertsDao)
                    .addOnAlertsChangedListener(capture())
            firstValue.onAlertsChanged()
        }

        verify(proximityAlertTracker)
                .trackProximityAlert(alert1)
        verify(proximityAlertTracker)
                .trackProximityAlert(alert2)
        verify(proximityAlertTracker)
                .trackProximityAlert(alert3)
        verify(proximityAlertTracker, never())
                .removeProximityAlert(any())
        verify(stopListener, never())
                .invoke()
    }

    private fun givenBackgroundExecutorExecutesTask() {
        doAnswer {
            it.getArgument<Runnable>(0)
                    .run()
        }.whenever(backgroundExecutor).execute(any())
    }
}