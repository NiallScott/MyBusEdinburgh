/*
 * Copyright (C) 2019 - 2023 Niall 'Rivernile' Scott
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule

/**
 * Tests for [TimeAlertRunner].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class TimeAlertRunnerTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var checkTimesTask: CheckTimesTask
    @Mock
    private lateinit var alertsRepository: AlertsRepository

    private lateinit var runner: TimeAlertRunner

    @Before
    fun setUp() {
        runner = TimeAlertRunner(
            checkTimesTask,
            alertsRepository)
    }

    @Test
    fun runWithZeroArrivalAlertsOnStartThrowsImmediateCancellationException() = runTest {
        whenever(alertsRepository.arrivalAlertCountFlow)
            .thenReturn(flowOf(0))

        val job = launch {
            runner.run()
        }
        job.join()

        // As the launched Coroutine is self-cancelling when there are no arrival alerts, and we did
        // not cancel it in this test, we're expecting the Job to be in a cancelled state.
        assertTrue(job.isCancelled)
        verify(checkTimesTask, never())
            .checkTimes()
    }

    @Test
    fun runWithOneArrivalAlertOnStartCausesCheckTimesTaskToRun() = runTest {
        whenever(alertsRepository.arrivalAlertCountFlow)
            .thenReturn(flowOf(1))

        val job = launch {
            runner.run()
        }
        advanceTimeBy(1L)
        job.cancel()

        verify(checkTimesTask)
            .checkTimes()
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
        whenever(alertsRepository.arrivalAlertCountFlow)
            .thenReturn(flow)

        val job = launch {
            runner.run()
        }
        // Advance time by 1 million millis. This should be enough to determine the check task only
        // ran twice.
        advanceTimeBy(1000000L)
        job.join()

        assertTrue(job.isCancelled)
        verify(checkTimesTask, times(2))
            .checkTimes()
    }
}