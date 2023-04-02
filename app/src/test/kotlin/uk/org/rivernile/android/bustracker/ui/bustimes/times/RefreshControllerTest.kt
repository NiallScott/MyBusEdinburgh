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

package uk.org.rivernile.android.bustracker.ui.bustimes.times

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.utils.TimeUtils
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test

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

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var timeUtils: TimeUtils

    private lateinit var controller: RefreshController

    @Before
    fun setUp() {
        controller = RefreshController(timeUtils)
    }

    @Test
    fun setActiveStateDoesNotCauseRefreshWhenNotActive() = runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)

        controller.setActiveState(false)
        advanceUntilIdle()
        observer.finish()

        observer.assertEmpty()
    }

    @Test
    fun setActiveStateCausesRefreshOnFirstActive() = runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)

        controller.setActiveState(true)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun setActiveStateDoesNotCauseRefreshOnSubsequentActive() = runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)

        controller.setActiveState(true)
        controller.setActiveState(false)
        controller.setActiveState(true)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun setActiveStateCausesRefreshIfPendingRefreshWhenActive() = runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)

        controller.setActiveState(true)
        advanceUntilIdle()
        controller.setActiveState(false)
        advanceUntilIdle()
        controller.requestRefresh()
        advanceUntilIdle()
        controller.setActiveState(true)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit, Unit)
    }

    @Test
    fun requestRefreshDoesNotCauseRefreshWhenNotActive() = runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)

        controller.setActiveState(true)
        controller.setActiveState(false)
        controller.requestRefresh()
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun requestRefreshCausesRefreshWhenActive() = runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)

        controller.setActiveState(true)
        advanceUntilIdle()
        controller.requestRefresh()
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit, Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsNullAndEnabledIsFalse() =
            runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(null, false)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsNullAndEnabledIsTrue() =
            runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(null, true)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsInProgressAndEnabledIsFalse() =
            runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(UiTransformedResult.InProgress, false)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsInProgressAndEnabledIsTrue() =
            runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(UiTransformedResult.InProgress, true)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsErrorAndEnabledIsFalseAndDelayLessThanInterval() =
            runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120001L - AUTO_REFRESH_INTERVAL_MILLIS,
                ErrorType.SERVER_ERROR)

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(data, false)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsErrorAndEnabledIsFalseAndDelayEqualsInterval() =
            runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS,
                ErrorType.SERVER_ERROR)

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(data, false)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsErrorAndEnabledIsFalseAndDelayMoreThanInterval() =
            runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS,
                ErrorType.SERVER_ERROR)

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(data, false)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsErrorAndEnabledIsTrueAndDelayLessThanInterval() =
            runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120001L - AUTO_REFRESH_INTERVAL_MILLIS,
                ErrorType.SERVER_ERROR)

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(data, true)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedCauseRefreshWhenResultIsErrorAndEnabledIsTrueAndDelayEqualsInterval() =
            runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS,
                ErrorType.SERVER_ERROR)

        controller.setActiveState(true)
        advanceUntilIdle()
        controller.onAutoRefreshPreferenceChanged(data, true)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit, Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedCauseRefreshWhenResultIsErrorAndEnabledIsTrueAndDelayMoreThanInterval() =
            runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS - 1L,
                ErrorType.SERVER_ERROR)

        controller.setActiveState(true)
        advanceUntilIdle()
        controller.onAutoRefreshPreferenceChanged(data, true)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit, Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsSuccessAndEnabledFalseAndDelayLessThanInterval() =
            runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120001L - AUTO_REFRESH_INTERVAL_MILLIS, emptyList())

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(data, false)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsSuccessAndEnabledIsFalseAndDelayEqualsInterval() =
            runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS, emptyList())

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(data, false)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsSuccessAndEnabledFalseAndDelayMoreThanInterval() =
            runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS, emptyList())

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(data, false)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsSuccessAndEnabledTrueAndDelayLessThanInterval() =
            runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120001L - AUTO_REFRESH_INTERVAL_MILLIS, emptyList())

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(data, true)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedCausesRefreshWhenResultIsSuccessAndEnabledIsTrueAndDelayEqualsInterval() =
            runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS, emptyList())

        controller.setActiveState(true)
        advanceUntilIdle()
        controller.onAutoRefreshPreferenceChanged(data, true)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit, Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedCausesRefreshWhenResultIsSuccessAndEnabledIsTrueAndDelayMoreThanInterval() =
            runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS - 1L,
                emptyList())

        controller.setActiveState(true)
        advanceUntilIdle()
        controller.onAutoRefreshPreferenceChanged(data, true)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit, Unit)
    }

    @Test
    fun performAutoRefreshDelayDoesNotCauseRefreshWhenResultIsInProgress() = runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)

        controller.setActiveState(true)
        controller.performAutoRefreshDelay(UiTransformedResult.InProgress) { true }
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun performAutoRefreshDelayReturnsImmediatelyWhenResultIsErrorAndCalculatedDelayIsNegative() =
            runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS - 1L,
                ErrorType.SERVER_ERROR)

        controller.setActiveState(true)
        val startTime = currentTime
        controller.performAutoRefreshDelay(data) { false }
        val endTime = currentTime
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit)
        assertEquals(0, endTime - startTime)
    }

    @Test
    fun performAutoRefreshDelayCausesRefreshWhenResultIsErrorAndCalculatedDelayIsNegativeAndPredicateIsTrue() =
            runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS - 1L,
                ErrorType.SERVER_ERROR)

        controller.setActiveState(true)
        advanceUntilIdle()
        val startTime = currentTime
        controller.performAutoRefreshDelay(data) { true }
        val endTime = currentTime
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit, Unit)
        assertEquals(0, endTime - startTime)
    }

    @Test
    fun performAutoRefreshDelayReturnsImmediatelyWhenResultIsErrorAndCalculatedDelayIsZero() =
            runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS,
                ErrorType.SERVER_ERROR)

        controller.setActiveState(true)
        val startTime = currentTime
        controller.performAutoRefreshDelay(data) { false }
        val endTime = currentTime
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit)
        assertEquals(0, endTime - startTime)
    }

    @Test
    fun performAutoRefreshDelayCausesRefreshWhenResultIsErrorAndCalculatedDelayIsZeroAndPredicateIsTrue() =
            runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS,
                ErrorType.SERVER_ERROR)

        controller.setActiveState(true)
        advanceUntilIdle()
        val startTime = currentTime
        controller.performAutoRefreshDelay(data) { true }
        val endTime = currentTime
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit, Unit)
        assertEquals(0, endTime - startTime)
    }

    @Test
    fun performAutoRefreshDelayDelaysWhenResultIsErrorAndCalculatedDelayIsPositive() = runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS + 1L,
                ErrorType.SERVER_ERROR)

        controller.setActiveState(true)
        val startTime = currentTime
        controller.performAutoRefreshDelay(data) { false }
        val endTime = currentTime
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit)
        assertEquals(1L, endTime - startTime)
    }

    @Test
    fun performAutoRefreshDelayCausesRefreshWhenResultIsErrorAndCalculatedDelayIsPositiveAndPredicateIsTrue() =
            runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS + 1L,
                ErrorType.SERVER_ERROR)

        controller.setActiveState(true)
        val startTime = currentTime
        controller.performAutoRefreshDelay(data) { true }
        val endTime = currentTime
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit, Unit)
        assertEquals(1L, endTime - startTime)
    }

    @Test
    fun performAutoRefreshDelayReturnsImmediatelyWhenResultIsSuccessAndCalculatedDelayIsNegative() =
            runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS - 1L,
                emptyList())

        controller.setActiveState(true)
        val startTime = currentTime
        controller.performAutoRefreshDelay(data) { false }
        val endTime = currentTime
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit)
        assertEquals(0, endTime - startTime)
    }

    @Test
    fun performAutoRefreshDelayCausesRefreshWhenResultIsSuccessAndCalculatedDelayIsNegative() =
            runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS - 1L,
                emptyList())

        controller.setActiveState(true)
        advanceUntilIdle()
        val startTime = currentTime
        controller.performAutoRefreshDelay(data) { true }
        val endTime = currentTime
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit, Unit)
        assertEquals(0, endTime - startTime)
    }

    @Test
    fun performAutoRefreshDelayReturnsImmediatelyWhenResultIsSuccessAndCalculatedDelayIsZero() =
            runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS, emptyList())

        controller.setActiveState(true)
        val startTime = currentTime
        controller.performAutoRefreshDelay(data) { false }
        val endTime = currentTime
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit)
        assertEquals(0, endTime - startTime)
    }

    @Test
    fun performAutoRefreshDelayCausesRefreshWhenResultIsSuccessAndCalculatedDelayIsZero() =
            runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS, emptyList())

        controller.setActiveState(true)
        advanceUntilIdle()
        val startTime = currentTime
        controller.performAutoRefreshDelay(data) { true }
        val endTime = currentTime
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit, Unit)
        assertEquals(0, endTime - startTime)
    }

    @Test
    fun performAutoRefreshDelayDelaysWhenResultIsSuccessAndCalculatedDelayIsPositive() = runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS + 1L,
                emptyList())

        controller.setActiveState(true)
        val startTime = currentTime
        controller.performAutoRefreshDelay(data) { false }
        val endTime = currentTime
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit)
        assertEquals(1L, endTime - startTime)
    }

    @Test
    fun performAutoRefreshDelayCausesRefreshWhenResultIsSuccessAndCalculatedDelayIsPositive() =
            runTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS + 1L,
                emptyList())

        controller.setActiveState(true)
        val startTime = currentTime
        controller.performAutoRefreshDelay(data) { true }
        val endTime = currentTime
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(Unit, Unit)
        assertEquals(1L, endTime - startTime)
    }

    private fun givenReturnsTimestamp() {
        whenever(timeUtils.currentTimeMills)
                .thenReturn(120000L)
    }
}