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

package uk.org.rivernile.android.bustracker.ui.bustimes.times

import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.utils.TimeUtils
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [LastRefreshTimeCalculator].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class LastRefreshTimeCalculatorTest {

    private companion object {

        private const val MORE_THAN_ONE_HOUR_MILLIS = 7200000L
    }

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var timeUtils: TimeUtils

    private lateinit var calculator: LastRefreshTimeCalculator

    @Before
    fun setUp() {
        calculator = LastRefreshTimeCalculator(timeUtils)
    }

    @Test
    fun getLastRefreshTimeFlowWithNegativeRefreshTimeOnlyEmitsNever() = runTest {
        val observer = calculator.getLastRefreshTimeFlow(-1).test(this)
        advanceTimeBy(MORE_THAN_ONE_HOUR_MILLIS)
        observer.finish()

        observer.assertValues(LastRefreshTime.Never)
    }

    @Test
    fun getLastRefreshTimeFlowWithZeroRefreshTimeOnlyEmitsNever() = runTest {
        val observer = calculator.getLastRefreshTimeFlow(0).test(this)
        advanceTimeBy(MORE_THAN_ONE_HOUR_MILLIS)
        observer.finish()

        observer.assertValues(LastRefreshTime.Never)
    }

    @Test
    fun getLastRefreshTimeFlowWithNegativeNumberOfMinutesEmitsNever() = runTest {
        whenever(timeUtils.getCurrentTimeMillis())
                .thenReturn(350000L)

        val observer = calculator.getLastRefreshTimeFlow(3600000L).test(this)
        advanceTimeBy(MORE_THAN_ONE_HOUR_MILLIS)
        observer.finish()

        observer.assertValues(LastRefreshTime.Never)
    }

    @Test
    fun getLastRefreshTimeFlowWithTimeProgressingEmitsExpectedElements() = runTest {
        whenever(timeUtils.getCurrentTimeMillis())
                .thenReturn(50001L, 600001L, 600002L, 1800001L, 3540001L, 3700001L)

        val observer = calculator.getLastRefreshTimeFlow(1L).test(this)
        advanceTimeBy(MORE_THAN_ONE_HOUR_MILLIS)
        observer.finish()

        observer.assertValues(
                LastRefreshTime.Now,
                LastRefreshTime.Minutes(10),
                LastRefreshTime.Minutes(30),
                LastRefreshTime.Minutes(59),
                LastRefreshTime.MoreThanOneHour)
    }

    @Test
    fun getLastRefreshTimeFlowWithCancellationEmitsExpectedItems() = runTest {
        whenever(timeUtils.getCurrentTimeMillis())
                .thenReturn(50001L, 600001L, 600002L, 1800001L, 3540001L, 3700001L)

        val observer = calculator.getLastRefreshTimeFlow(1L).test(this)
        advanceTimeBy(25000L)
        observer.finish()
        advanceTimeBy(MORE_THAN_ONE_HOUR_MILLIS)

        observer.assertValues(
                LastRefreshTime.Now,
                LastRefreshTime.Minutes(10))
    }
}