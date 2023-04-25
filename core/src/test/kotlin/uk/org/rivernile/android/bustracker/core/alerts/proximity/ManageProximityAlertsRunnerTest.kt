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

package uk.org.rivernile.android.bustracker.core.alerts.proximity

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ProximityAlert
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf

/**
 * Tests for [ManageProximityAlertsRunner].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class ManageProximityAlertsRunnerTest {

    @Mock
    private lateinit var alertsRepository: AlertsRepository
    @Mock
    private lateinit var proximityAlertTracker: ProximityAlertTracker

    private lateinit var runner: ManageProximityAlertsRunner

    @Before
    fun setUp() {
        runner = ManageProximityAlertsRunner(
            alertsRepository,
            proximityAlertTracker)
    }

    @Test
    fun runnerIsStoppedIfStartedWithNullProximityAlerts() = runTest {
        whenever(alertsRepository.allProximityAlertsFlow)
            .thenReturn(flowOf(null))

        launch {
            runner.run()
        }
        advanceUntilIdle()

        verifyNoInteractions(proximityAlertTracker)
    }

    @Test
    fun runnerIsStoppedIfStartedWithEmptyProximityAlerts() = runTest {
        whenever(alertsRepository.allProximityAlertsFlow)
            .thenReturn(flowOf(emptyList()))

        launch {
            runner.run()
        }
        advanceUntilIdle()

        verifyNoInteractions(proximityAlertTracker)
    }

    @Test
    fun initialAlertsAreTracked() = runTest {
        val alert1 = ProximityAlert(1, 101L, "100001", 10)
        val alert2 = ProximityAlert(2, 102L, "100002", 20)
        val alert3 = ProximityAlert(3, 103L, "100003", 30)
        val alerts = listOf(alert1, alert2, alert3)
        whenever(alertsRepository.allProximityAlertsFlow)
            .thenReturn(intervalFlowOf(0L, 10L, alerts, null))

        val job = launch {
            runner.run()
        }
        advanceTimeBy(1L)

        verify(proximityAlertTracker)
                .trackProximityAlert(alert1)
        verify(proximityAlertTracker)
                .trackProximityAlert(alert2)
        verify(proximityAlertTracker)
                .trackProximityAlert(alert3)
        verify(proximityAlertTracker, never())
                .removeProximityAlert(any())
        job.cancel()
    }

    @Test
    fun alertsChangedWithNoProximityAlertsRemovesAllTrackedAlerts() = runTest {
        val alert1 = ProximityAlert(1, 101L, "100001", 10)
        val alert2 = ProximityAlert(2, 102L, "100002", 20)
        val alert3 = ProximityAlert(3, 103L, "100003", 30)
        val alerts = listOf(alert1, alert2, alert3)
        whenever(alertsRepository.allProximityAlertsFlow)
            .thenReturn(intervalFlowOf(0L, 10L, alerts, null, null))

        val job = launch {
            runner.run()
        }
        advanceTimeBy(15L)

        verify(proximityAlertTracker)
                .removeProximityAlert(1)
        verify(proximityAlertTracker)
                .removeProximityAlert(2)
        verify(proximityAlertTracker)
                .removeProximityAlert(3)
        job.cancel()
    }

    @Test
    fun alertsChangedWithRemovalRemovesTrackedAlert() = runTest {
        val alert1 = ProximityAlert(1, 101L, "100001", 10)
        val alert2 = ProximityAlert(2, 102L, "100002", 20)
        val alert3 = ProximityAlert(3, 103L, "100003", 30)
        val alerts1 = listOf(alert1, alert2, alert3)
        val alerts2 = listOf(alert1, alert3)
        whenever(alertsRepository.allProximityAlertsFlow)
            .thenReturn(intervalFlowOf(0L, 10L, alerts1, alerts2, null))

        val job = launch {
            runner.run()
        }
        advanceTimeBy(15L)

        verify(proximityAlertTracker, never())
                .removeProximityAlert(1)
        verify(proximityAlertTracker)
                .removeProximityAlert(2)
        verify(proximityAlertTracker, never())
                .removeProximityAlert(3)
        job.cancel()
    }

    @Test
    fun alertsChangedWithAdditionAddsTrackedAlert() = runTest {
        val alert1 = ProximityAlert(1, 101L, "100001", 10)
        val alert2 = ProximityAlert(2, 102L, "100002", 20)
        val alert3 = ProximityAlert(3, 103L, "100003", 30)
        val alerts1 = listOf(alert1, alert2)
        val alerts2 = listOf(alert1, alert2, alert3)
        whenever(alertsRepository.allProximityAlertsFlow)
            .thenReturn(intervalFlowOf(0L, 10L, alerts1, alerts2, null))

        val job = launch {
            runner.run()
        }
        advanceTimeBy(15L)

        verify(proximityAlertTracker)
                .trackProximityAlert(alert1)
        verify(proximityAlertTracker)
                .trackProximityAlert(alert2)
        verify(proximityAlertTracker)
                .trackProximityAlert(alert3)
        verify(proximityAlertTracker, never())
                .removeProximityAlert(any())
        job.cancel()
    }

    @Test
    fun allAlertsAreUntrackedWhenCoroutineIsCancelled() = runTest {
        val alert1 = ProximityAlert(1, 101L, "100001", 10)
        val alert2 = ProximityAlert(2, 102L, "100002", 20)
        val alert3 = ProximityAlert(3, 103L, "100003", 30)
        val alerts = listOf(alert1, alert2, alert3)
        whenever(alertsRepository.allProximityAlertsFlow)
            .thenReturn(intervalFlowOf(0L, 10L, alerts, null))

        val job = launch {
            runner.run()
        }
        advanceTimeBy(1L)
        job.cancel()
        advanceUntilIdle()

        verify(proximityAlertTracker)
            .removeProximityAlert(1)
        verify(proximityAlertTracker)
            .removeProximityAlert(2)
        verify(proximityAlertTracker)
            .removeProximityAlert(3)
    }

    @Test
    fun allAlertsAreUntrackedWhenProximityAlertsFlowTerminates() = runTest {
        val alert1 = ProximityAlert(1, 101L, "100001", 10)
        val alert2 = ProximityAlert(2, 102L, "100002", 20)
        val alert3 = ProximityAlert(3, 103L, "100003", 30)
        val alerts = listOf(alert1, alert2, alert3)
        whenever(alertsRepository.allProximityAlertsFlow)
            .thenReturn(intervalFlowOf(0L, 10L, alerts))

        val job = launch {
            runner.run()
        }
        advanceUntilIdle()
        job.cancel()

        verify(proximityAlertTracker)
            .removeProximityAlert(1)
        verify(proximityAlertTracker)
            .removeProximityAlert(2)
        verify(proximityAlertTracker)
            .removeProximityAlert(3)
    }
}