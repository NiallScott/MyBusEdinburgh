/*
 * Copyright (C) 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.time

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [RealElapsedTimeCalculator].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RealElapsedTimeCalculatorTest {

    private companion object {

        private const val MORE_THAN_ONE_HOUR_MILLIS = 7200000L
    }

    @Test
    fun getElapsedTimeMinutesFlowWithNegativeRefreshTimeOnlyEmitsNone() = runTest {
        val calculator = createElapsedTimeCalculator()

        calculator.getElapsedTimeMinutesFlow(-1).test {
            advanceTimeBy(MORE_THAN_ONE_HOUR_MILLIS)

            assertEquals(ElapsedTimeMinutes.None, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getElapsedTimeMinutesFlowWithZeroRefreshTimeOnlyEmitsNone() = runTest {
        val calculator = createElapsedTimeCalculator()

        calculator.getElapsedTimeMinutesFlow(0).test {
            advanceTimeBy(MORE_THAN_ONE_HOUR_MILLIS)

            assertEquals(ElapsedTimeMinutes.None, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getElapsedTimeMinutesFlowWithNegativeNumberOfMinutesEmitsNone() = runTest {
        val calculator = createElapsedTimeCalculator(
            timeUtils = FakeTimeUtils(
                onGetCurrentTimeMillis = { 350000L }
            )
        )

        calculator.getElapsedTimeMinutesFlow(3600000L).test {
            advanceTimeBy(MORE_THAN_ONE_HOUR_MILLIS)

            assertEquals(ElapsedTimeMinutes.None, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getElapsedTimeMinutesFlowWithTimeProgressingEmitsExpectedElements() = runTest {
        val currentTimes = ArrayDeque(
            listOf(50001L, 600001L, 600002L, 1800001L, 3540001L, 3700001L)
        )
        val calculator = createElapsedTimeCalculator(
            timeUtils = FakeTimeUtils(
                onGetCurrentTimeMillis = { currentTimes.removeFirst() }
            )
        )

        calculator.getElapsedTimeMinutesFlow(1L).test {
            advanceTimeBy(MORE_THAN_ONE_HOUR_MILLIS)

            assertEquals(ElapsedTimeMinutes.Now, awaitItem())
            assertEquals(ElapsedTimeMinutes.Minutes(10), awaitItem())
            assertEquals(ElapsedTimeMinutes.Minutes(30), awaitItem())
            assertEquals(ElapsedTimeMinutes.Minutes(59), awaitItem())
            assertEquals(ElapsedTimeMinutes.MoreThanOneHour, awaitItem())
            awaitComplete()
        }
    }

    private fun createElapsedTimeCalculator(
        timeUtils: TimeUtils = FakeTimeUtils()
    ): RealElapsedTimeCalculator {
        return RealElapsedTimeCalculator(timeUtils)
    }
}