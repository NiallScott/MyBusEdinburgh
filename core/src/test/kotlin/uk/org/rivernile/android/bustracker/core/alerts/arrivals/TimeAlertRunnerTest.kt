/*
 * Copyright (C) 2019 - 2021 Niall 'Rivernile' Scott
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
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.database.settings.daos.AlertsDao
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Tests for [TimeAlertRunner].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class TimeAlertRunnerTest {

    companion object {

        private const val CHECK_TIMES_INTERVAL_SECS = 60L
    }

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var checkTimesTask: CheckTimesTask
    @Mock
    private lateinit var alertsDao: AlertsDao
    @Mock
    private lateinit var executorService: ScheduledExecutorService
    @Mock
    private lateinit var stopListener: () -> Unit

    private lateinit var runner: TimeAlertRunner

    @Before
    fun setUp() {
        runner = TimeAlertRunner(checkTimesTask, alertsDao, executorService)
    }

    @Test
    fun startingRunnerRegistersCallbackListenerWithDao() {
        runner.start(null)

        verify(alertsDao)
                .addOnAlertsChangedListener(any())
    }

    @Test
    fun startingRunnerSchedulesPollingWithCheckTimesTask() {
        runner.start(null)

        verify(executorService)
                .scheduleWithFixedDelay(any(), eq(0L), eq(CHECK_TIMES_INTERVAL_SECS),
                        eq(TimeUnit.SECONDS))
    }

    @Test
    fun stopRunnerRemovesCallbackListenerWithDao() {
        runner.start(null)
        runner.stop()

        verify(alertsDao)
                .removeOnAlertsChangedListener(any())
    }

    @Test
    fun stopRunnerShutsDownExecutorService() {
        runner.start(null)
        runner.stop()

        verify(executorService)
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
    fun runnerCanOnlyBeStartedOnce() {
        runner.start(null)
        runner.stop()
        runner.start(null)

        verify(alertsDao, times(1))
                .addOnAlertsChangedListener(any())
        verify(executorService, times(1))
                .scheduleWithFixedDelay(any(), any(), any(), any())
    }

    @Test
    fun onAlertsChangedWithNonZeroCountDoesNotStopRunner() = coroutineRule.runBlockingTest {
        doAnswer {
            val runnable = it.getArgument<Runnable>(0)
            runnable.run()
        }.whenever(executorService).execute(any())
        whenever(alertsDao.getArrivalAlertCount())
                .thenReturn(1)

        runner.start(null)
        argumentCaptor<AlertsDao.OnAlertsChangedListener> {
            verify(alertsDao)
                    .addOnAlertsChangedListener(capture())
            firstValue.onAlertsChanged()
        }

        verify(alertsDao, never())
                .removeOnAlertsChangedListener(any())
        verify(executorService, never())
                .shutdownNow()
    }

    @Test
    fun onAlertsChangedWithZeroCountStopsRunner() = coroutineRule.runBlockingTest {
        doAnswer {
            val runnable = it.getArgument<Runnable>(0)
            runnable.run()
        }.whenever(executorService).execute(any())
        whenever(alertsDao.getArrivalAlertCount())
                .thenReturn(1, 0)

        runner.start(stopListener)
        argumentCaptor<AlertsDao.OnAlertsChangedListener> {
            verify(alertsDao)
                    .addOnAlertsChangedListener(capture())
            firstValue.onAlertsChanged()
        }

        verify(alertsDao)
                .removeOnAlertsChangedListener(any())
        verify(executorService)
                .shutdownNow()
        verify(stopListener)
                .invoke()
    }
}