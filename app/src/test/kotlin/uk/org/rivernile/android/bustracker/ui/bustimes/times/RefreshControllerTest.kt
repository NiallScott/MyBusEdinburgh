/*
 * Copyright (C) 2020 - 2024 Niall 'Rivernile' Scott
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

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.time.TimeUtils
import kotlin.test.assertEquals

/**
 * Tests for [RefreshController].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class RefreshControllerTest {

    companion object {

        private const val AUTO_REFRESH_INTERVAL_MILLIS = 60000L
    }

    @Mock
    private lateinit var timeUtils: TimeUtils

    private lateinit var controller: RefreshController

    @Before
    fun setUp() {
        controller = RefreshController(timeUtils)
    }

    @Test
    fun setActiveStateDoesNotCauseRefreshWhenNotActive() = runTest {
        controller.refreshTriggerFlow.test {
            controller.setActiveState(false)

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun setActiveStateCausesRefreshOnFirstActive() = runTest {
        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)

            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun setActiveStateDoesNotCauseRefreshOnSubsequentActive() = runTest {
        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            controller.setActiveState(false)
            controller.setActiveState(true)

            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun setActiveStateCausesRefreshIfPendingRefreshWhenActive() = runTest {
        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            controller.setActiveState(false)
            controller.requestRefresh()
            controller.setActiveState(true)

            assertEquals(Unit, awaitItem())
            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun requestRefreshDoesNotCauseRefreshWhenNotActive() = runTest {
        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            controller.setActiveState(false)
            controller.requestRefresh()

            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun requestRefreshCausesRefreshWhenActive() = runTest {
        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            controller.requestRefresh()

            assertEquals(Unit, awaitItem())
            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsNullAndEnabledIsFalse() =
            runTest {
        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            controller.onAutoRefreshPreferenceChanged(null, false)

            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsNullAndEnabledIsTrue() =
            runTest {
        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            controller.onAutoRefreshPreferenceChanged(null, true)

            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsInProgressAndEnabledIsFalse() =
            runTest {
        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            controller.onAutoRefreshPreferenceChanged(UiTransformedResult.InProgress, false)

            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsInProgressAndEnabledIsTrue() =
            runTest {
        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            controller.onAutoRefreshPreferenceChanged(UiTransformedResult.InProgress, true)

            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsErrorAndEnabledIsFalseAndDelayLessThanInterval() =
            runTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120001L - AUTO_REFRESH_INTERVAL_MILLIS,
            ErrorType.SERVER_ERROR)

        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            controller.onAutoRefreshPreferenceChanged(data, false)

            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsErrorAndEnabledIsFalseAndDelayEqualsInterval() =
            runTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS,
                ErrorType.SERVER_ERROR)

        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            controller.onAutoRefreshPreferenceChanged(data, false)

            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsErrorAndEnabledIsFalseAndDelayMoreThanInterval() =
            runTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS,
                ErrorType.SERVER_ERROR)

        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            controller.onAutoRefreshPreferenceChanged(data, false)

            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsErrorAndEnabledIsTrueAndDelayLessThanInterval() =
            runTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120001L - AUTO_REFRESH_INTERVAL_MILLIS,
                ErrorType.SERVER_ERROR)

        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            controller.onAutoRefreshPreferenceChanged(data, true)

            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun onAutoRefreshPreferenceChangedCauseRefreshWhenResultIsErrorAndEnabledIsTrueAndDelayEqualsInterval() =
            runTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS,
                ErrorType.SERVER_ERROR)

        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            controller.onAutoRefreshPreferenceChanged(data, true)

            assertEquals(Unit, awaitItem())
            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun onAutoRefreshPreferenceChangedCauseRefreshWhenResultIsErrorAndEnabledIsTrueAndDelayMoreThanInterval() =
            runTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS - 1L,
                ErrorType.SERVER_ERROR)

        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            controller.onAutoRefreshPreferenceChanged(data, true)

            assertEquals(Unit, awaitItem())
            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsSuccessAndEnabledFalseAndDelayLessThanInterval() =
            runTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120001L - AUTO_REFRESH_INTERVAL_MILLIS, emptyList())

        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            controller.onAutoRefreshPreferenceChanged(data, false)

            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsSuccessAndEnabledIsFalseAndDelayEqualsInterval() =
            runTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS, emptyList())

        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            controller.onAutoRefreshPreferenceChanged(data, false)

            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsSuccessAndEnabledFalseAndDelayMoreThanInterval() =
            runTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS, emptyList())

        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            controller.onAutoRefreshPreferenceChanged(data, false)

            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsSuccessAndEnabledTrueAndDelayLessThanInterval() =
            runTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120001L - AUTO_REFRESH_INTERVAL_MILLIS, emptyList())

        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            controller.onAutoRefreshPreferenceChanged(data, true)

            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun onAutoRefreshPreferenceChangedCausesRefreshWhenResultIsSuccessAndEnabledIsTrueAndDelayEqualsInterval() =
            runTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS, emptyList())

        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            controller.onAutoRefreshPreferenceChanged(data, true)

            assertEquals(Unit, awaitItem())
            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun onAutoRefreshPreferenceChangedCausesRefreshWhenResultIsSuccessAndEnabledIsTrueAndDelayMoreThanInterval() =
            runTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS - 1L,
                emptyList())

        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            controller.onAutoRefreshPreferenceChanged(data, true)

            assertEquals(Unit, awaitItem())
            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun performAutoRefreshDelayDoesNotCauseRefreshWhenResultIsInProgress() = runTest {
        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            controller.performAutoRefreshDelay(UiTransformedResult.InProgress) { true }

            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun performAutoRefreshDelayReturnsImmediatelyWhenResultIsErrorAndCalculatedDelayIsNegative() =
            runTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS - 1L,
                ErrorType.SERVER_ERROR)

        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            val startTime = currentTime
            controller.performAutoRefreshDelay(data) { false }
            val endTime = currentTime

            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
            assertEquals(0, endTime - startTime)
        }
    }

    @Test
    fun performAutoRefreshDelayCausesRefreshWhenResultIsErrorAndCalculatedDelayIsNegativeAndPredicateIsTrue() =
            runTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS - 1L,
                ErrorType.SERVER_ERROR)

        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            val startTime = currentTime
            controller.performAutoRefreshDelay(data) { true }
            val endTime = currentTime

            assertEquals(Unit, awaitItem())
            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
            assertEquals(0, endTime - startTime)
        }
    }

    @Test
    fun performAutoRefreshDelayReturnsImmediatelyWhenResultIsErrorAndCalculatedDelayIsZero() =
            runTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS,
                ErrorType.SERVER_ERROR)

        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            val startTime = currentTime
            controller.performAutoRefreshDelay(data) { false }
            val endTime = currentTime

            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
            assertEquals(0, endTime - startTime)
        }
    }

    @Test
    fun performAutoRefreshDelayCausesRefreshWhenResultIsErrorAndCalculatedDelayIsZeroAndPredicateIsTrue() =
            runTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS,
                ErrorType.SERVER_ERROR)

        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            val startTime = currentTime
            controller.performAutoRefreshDelay(data) { true }
            val endTime = currentTime

            assertEquals(Unit, awaitItem())
            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
            assertEquals(0, endTime - startTime)
        }
    }

    @Test
    fun performAutoRefreshDelayDelaysWhenResultIsErrorAndCalculatedDelayIsPositive() = runTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS + 1L,
                ErrorType.SERVER_ERROR)

        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            val startTime = currentTime
            controller.performAutoRefreshDelay(data) { false }
            val endTime = currentTime

            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
            assertEquals(1L, endTime - startTime)
        }
    }

    @Test
    fun performAutoRefreshDelayCausesRefreshWhenResultIsErrorAndCalculatedDelayIsPositiveAndPredicateIsTrue() =
            runTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS + 1L,
                ErrorType.SERVER_ERROR)

        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            val startTime = currentTime
            controller.performAutoRefreshDelay(data) { true }
            val endTime = currentTime

            assertEquals(Unit, awaitItem())
            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
            assertEquals(1L, endTime - startTime)
        }
    }

    @Test
    fun performAutoRefreshDelayReturnsImmediatelyWhenResultIsSuccessAndCalculatedDelayIsNegative() =
            runTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS - 1L,
                emptyList())

            controller.refreshTriggerFlow.test {
                controller.setActiveState(true)
                val startTime = currentTime
                controller.performAutoRefreshDelay(data) { false }
                val endTime = currentTime

                assertEquals(Unit, awaitItem())
                ensureAllEventsConsumed()
                assertEquals(0, endTime - startTime)
            }
    }

    @Test
    fun performAutoRefreshDelayCausesRefreshWhenResultIsSuccessAndCalculatedDelayIsNegative() =
            runTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS - 1L,
                emptyList())

        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            val startTime = currentTime
            controller.performAutoRefreshDelay(data) { true }
            val endTime = currentTime

            assertEquals(Unit, awaitItem())
            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
            assertEquals(0, endTime - startTime)
        }
    }

    @Test
    fun performAutoRefreshDelayReturnsImmediatelyWhenResultIsSuccessAndCalculatedDelayIsZero() =
            runTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS, emptyList())

        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            val startTime = currentTime
            controller.performAutoRefreshDelay(data) { false }
            val endTime = currentTime

            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
            assertEquals(0, endTime - startTime)
        }
    }

    @Test
    fun performAutoRefreshDelayCausesRefreshWhenResultIsSuccessAndCalculatedDelayIsZero() =
            runTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS, emptyList())

        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            val startTime = currentTime
            controller.performAutoRefreshDelay(data) { true }
            val endTime = currentTime

            assertEquals(Unit, awaitItem())
            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
            assertEquals(0, endTime - startTime)
        }
    }

    @Test
    fun performAutoRefreshDelayDelaysWhenResultIsSuccessAndCalculatedDelayIsPositive() = runTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS + 1L,
                emptyList())

        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            val startTime = currentTime
            controller.performAutoRefreshDelay(data) { false }
            val endTime = currentTime

            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
            assertEquals(1L, endTime - startTime)
        }
    }

    @Test
    fun performAutoRefreshDelayCausesRefreshWhenResultIsSuccessAndCalculatedDelayIsPositive() =
            runTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS + 1L,
                emptyList())

        controller.refreshTriggerFlow.test {
            controller.setActiveState(true)
            val startTime = currentTime
            controller.performAutoRefreshDelay(data) { true }
            val endTime = currentTime

            assertEquals(Unit, awaitItem())
            assertEquals(Unit, awaitItem())
            ensureAllEventsConsumed()
            assertEquals(1L, endTime - startTime)
        }
    }

    private fun givenReturnsTimestamp() {
        whenever(timeUtils.currentTimeMills)
            .thenReturn(120000L)
    }
}