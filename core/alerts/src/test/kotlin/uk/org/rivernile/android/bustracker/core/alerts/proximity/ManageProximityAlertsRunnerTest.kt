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

package uk.org.rivernile.android.bustracker.core.alerts.proximity

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.alerts.FakeAlertsRepository
import uk.org.rivernile.android.bustracker.core.alerts.ProximityAlert
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Tests for [ManageProximityAlertsRunner].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ManageProximityAlertsRunnerTest {

    @Test
    fun runnerIsStoppedIfStartedWithNullProximityAlerts() = runTest {
        val runner = createManageProximityAlertsRunner(
            alertsRepository = FakeAlertsRepository(
                onAllProximityAlertsFlow = { flowOf(null) }
            ),
            proximityAlertTracker = FakeProximityAlertTracker(
                onTrackProximityAlert = { fail("Not expecting to track any proximity alerts") },
                onRemoveProximityAlert = { fail("Not expecting to remove any proximity alerts") }
            )
        )

        launch {
            runner.run()
        }
        advanceUntilIdle()
    }

    @Test
    fun runnerIsStoppedIfStartedWithEmptyProximityAlerts() = runTest {
        val runner = createManageProximityAlertsRunner(
            alertsRepository = FakeAlertsRepository(
                onAllProximityAlertsFlow = { flowOf(emptyList()) }
            ),
            proximityAlertTracker = FakeProximityAlertTracker(
                onTrackProximityAlert = { fail("Not expecting to track any proximity alerts") },
                onRemoveProximityAlert = { fail("Not expecting to remove any proximity alerts") }
            )
        )

        launch {
            runner.run()
        }
        advanceUntilIdle()
    }

    @Test
    fun initialAlertsAreTracked() = runTest {
        val alert1 = ProximityAlert(1, 101L, "100001", 10)
        val alert2 = ProximityAlert(2, 102L, "100002", 20)
        val alert3 = ProximityAlert(3, 103L, "100003", 30)
        val alerts = listOf(alert1, alert2, alert3)
        val trackProximityAlertTracker = TrackProximityAlertTracker()
        val removedProximityAlertTracker = RemoveProximityAlertTracker()
        val runner = createManageProximityAlertsRunner(
            alertsRepository = FakeAlertsRepository(
                onAllProximityAlertsFlow = { intervalFlowOf(0L, 10L, alerts, null) }
            ),
            proximityAlertTracker = FakeProximityAlertTracker(
                onTrackProximityAlert = trackProximityAlertTracker,
                onRemoveProximityAlert = removedProximityAlertTracker
            )
        )

        val job = launch {
            runner.run()
        }
        advanceTimeBy(1L)

        assertEquals(
            listOf(alert1, alert2, alert3),
            trackProximityAlertTracker.proximityAlerts
        )
        assertTrue(removedProximityAlertTracker.removedIds.isEmpty())
        job.cancel()
    }

    @Test
    fun alertsChangedWithNoProximityAlertsRemovesAllTrackedAlerts() = runTest {
        val alert1 = ProximityAlert(1, 101L, "100001", 10)
        val alert2 = ProximityAlert(2, 102L, "100002", 20)
        val alert3 = ProximityAlert(3, 103L, "100003", 30)
        val alerts = listOf(alert1, alert2, alert3)
        val removedProximityAlertTracker = RemoveProximityAlertTracker()
        val runner = createManageProximityAlertsRunner(
            alertsRepository = FakeAlertsRepository(
                onAllProximityAlertsFlow = { intervalFlowOf(0L, 10L, alerts, null, null) }
            ),
            proximityAlertTracker = FakeProximityAlertTracker(
                onTrackProximityAlert = { },
                onRemoveProximityAlert = removedProximityAlertTracker
            )
        )

        val job = launch {
            runner.run()
        }
        advanceTimeBy(15L)

        assertEquals(listOf(1, 2, 3), removedProximityAlertTracker.removedIds)
        job.cancel()
    }

    @Test
    fun alertsChangedWithRemovalRemovesTrackedAlert() = runTest {
        val alert1 = ProximityAlert(1, 101L, "100001", 10)
        val alert2 = ProximityAlert(2, 102L, "100002", 20)
        val alert3 = ProximityAlert(3, 103L, "100003", 30)
        val alerts1 = listOf(alert1, alert2, alert3)
        val alerts2 = listOf(alert1, alert3)
        val removedProximityAlertTracker = RemoveProximityAlertTracker()
        val runner = createManageProximityAlertsRunner(
            alertsRepository = FakeAlertsRepository(
                onAllProximityAlertsFlow = { intervalFlowOf(0L, 10L, alerts1, alerts2, null) }
            ),
            proximityAlertTracker = FakeProximityAlertTracker(
                onTrackProximityAlert = { },
                onRemoveProximityAlert = removedProximityAlertTracker
            )
        )

        val job = launch {
            runner.run()
        }
        advanceTimeBy(15L)

        assertEquals(listOf(2), removedProximityAlertTracker.removedIds)
        job.cancel()
    }

    @Test
    fun alertsChangedWithAdditionAddsTrackedAlert() = runTest {
        val alert1 = ProximityAlert(1, 101L, "100001", 10)
        val alert2 = ProximityAlert(2, 102L, "100002", 20)
        val alert3 = ProximityAlert(3, 103L, "100003", 30)
        val alerts1 = listOf(alert1, alert2)
        val alerts2 = listOf(alert1, alert2, alert3)
        val trackProximityAlertTracker = TrackProximityAlertTracker()
        val removedProximityAlertTracker = RemoveProximityAlertTracker()
        val runner = createManageProximityAlertsRunner(
            alertsRepository = FakeAlertsRepository(
                onAllProximityAlertsFlow = { intervalFlowOf(0L, 10L, alerts1, alerts2, null) }
            ),
            proximityAlertTracker = FakeProximityAlertTracker(
                onTrackProximityAlert = trackProximityAlertTracker,
                onRemoveProximityAlert = removedProximityAlertTracker
            )
        )

        val job = launch {
            runner.run()
        }
        advanceTimeBy(15L)

        assertEquals(listOf(alert1, alert2, alert3), trackProximityAlertTracker.proximityAlerts)
        assertTrue(removedProximityAlertTracker.removedIds.isEmpty())
        job.cancel()
    }

    @Test
    fun allAlertsAreUntrackedWhenCoroutineIsCancelled() = runTest {
        val alert1 = ProximityAlert(1, 101L, "100001", 10)
        val alert2 = ProximityAlert(2, 102L, "100002", 20)
        val alert3 = ProximityAlert(3, 103L, "100003", 30)
        val alerts = listOf(alert1, alert2, alert3)
        val removedProximityAlertTracker = RemoveProximityAlertTracker()
        val runner = createManageProximityAlertsRunner(
            alertsRepository = FakeAlertsRepository(
                onAllProximityAlertsFlow = { intervalFlowOf(0L, 10L, alerts, null) }
            ),
            proximityAlertTracker = FakeProximityAlertTracker(
                onTrackProximityAlert = { },
                onRemoveProximityAlert = removedProximityAlertTracker
            )
        )

        val job = launch {
            runner.run()
        }
        advanceTimeBy(1L)
        job.cancel()
        advanceUntilIdle()

        assertEquals(listOf(1, 2, 3), removedProximityAlertTracker.removedIds)
    }

    @Test
    fun allAlertsAreUntrackedWhenProximityAlertsFlowTerminates() = runTest {
        val alert1 = ProximityAlert(1, 101L, "100001", 10)
        val alert2 = ProximityAlert(2, 102L, "100002", 20)
        val alert3 = ProximityAlert(3, 103L, "100003", 30)
        val alerts = listOf(alert1, alert2, alert3)
        val removedProximityAlertTracker = RemoveProximityAlertTracker()
        val runner = createManageProximityAlertsRunner(
            alertsRepository = FakeAlertsRepository(
                onAllProximityAlertsFlow = { intervalFlowOf(0L, 10L, alerts) }
            ),
            proximityAlertTracker = FakeProximityAlertTracker(
                onTrackProximityAlert = { },
                onRemoveProximityAlert = removedProximityAlertTracker
            )
        )

        val job = launch {
            runner.run()
        }
        advanceUntilIdle()
        job.cancel()

        assertEquals(listOf(1, 2, 3), removedProximityAlertTracker.removedIds)
    }

    private fun createManageProximityAlertsRunner(
        alertsRepository: AlertsRepository = FakeAlertsRepository(),
        proximityAlertTracker: ProximityAlertTracker = FakeProximityAlertTracker()
    ): ManageProximityAlertsRunner {
        return ManageProximityAlertsRunner(
            alertsRepository,
            proximityAlertTracker
        )
    }

    private class TrackProximityAlertTracker : (ProximityAlert) -> Unit {

        val proximityAlerts get() = _proximityAlerts.toList()
        private val _proximityAlerts = mutableListOf<ProximityAlert>()

        override fun invoke(p1: ProximityAlert) {
            _proximityAlerts += p1
        }
    }

    private class RemoveProximityAlertTracker : (Int) -> Unit {

        val removedIds get() = _removedIds.toList()
        private val _removedIds = mutableListOf<Int>()

        override fun invoke(p1: Int) {
            _removedIds += p1
        }
    }
}