/*
 * Copyright (C) 2019 - 2025 Niall 'Rivernile' Scott
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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.alerts.FakeAlertsRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Tests for [TimeAlertRunner].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TimeAlertRunnerTest {

    @Test
    fun runWithZeroArrivalAlertsOnStartThrowsImmediateCancellationException() = runTest {
        val runner = createTimeAlertRunner(
            checkTimesTask = FakeCheckTimesTask(
                onCheckTimes = { fail("Not expecting to check times.") }
            ),
            alertsRepository = FakeAlertsRepository(
                onArrivalAlertCountFlow = { flowOf(0) }
            )
        )

        val job = launch {
            runner.run()
        }
        job.join()

        // As the launched Coroutine is self-cancelling when there are no arrival alerts, and we did
        // not cancel it in this test, we're expecting the Job to be in a cancelled state.
        assertTrue(job.isCancelled)
    }

    @Test
    fun runWithOneArrivalAlertOnStartCausesCheckTimesTaskToRun() = runTest {
        val invocationCounter = InvocationCounter()
        val runner = createTimeAlertRunner(
            checkTimesTask = FakeCheckTimesTask(
                onCheckTimes = invocationCounter
            ),
            alertsRepository = FakeAlertsRepository(
                onArrivalAlertCountFlow = { flowOf(1) }
            )
        )

        val job = launch {
            runner.run()
        }
        advanceTimeBy(1L)
        job.cancel()

        assertEquals(1, invocationCounter.invocationCount)
    }

    @Test
    fun runWithRepresentativeExampleYieldsCorrectResults() = runTest {
        // Count change: 0s(1), 30s(2), 61s(3), 91s(0)
        // Check:        0s, 60s, (cancelled before next check at 120s)
        val flow = flow {
            emit(1)
            delay(30000L)
            emit(2)
            delay(31000L)
            emit(3)
            delay(30000L)
            emit(0)
        }
        val invocationCounter = InvocationCounter()
        val runner = createTimeAlertRunner(
            checkTimesTask = FakeCheckTimesTask(
                onCheckTimes = invocationCounter
            ),
            alertsRepository = FakeAlertsRepository(
                onArrivalAlertCountFlow = { flow }
            )
        )

        val job = launch {
            runner.run()
        }
        // Advance time by 1 million millis. This should be enough to determine the check task only
        // ran twice.
        advanceTimeBy(1000000L)
        job.join()

        assertTrue(job.isCancelled)
        assertEquals(2, invocationCounter.invocationCount)
    }

    private fun createTimeAlertRunner(
        checkTimesTask: CheckTimesTask = FakeCheckTimesTask(),
        alertsRepository: AlertsRepository = FakeAlertsRepository()
    ): TimeAlertRunner {
        return TimeAlertRunner(
            checkTimesTask,
            alertsRepository
        )
    }

    private class InvocationCounter : () -> Unit {

        var invocationCount = 0
            private set

        override fun invoke() {
            invocationCount++
        }
    }
}