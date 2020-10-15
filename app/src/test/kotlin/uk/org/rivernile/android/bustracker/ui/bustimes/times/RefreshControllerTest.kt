/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
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
 * Tests for [RefreshController].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
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
    fun setActiveStateDoesNotCauseRefreshWhenNotActive() = coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)

        controller.setActiveState(false)
        observer.finish()

        observer.assertEmpty()
    }

    @Test
    fun setActiveStateCausesRefreshOnFirstActive() = coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)

        controller.setActiveState(true)
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun setActiveStateDoesNotCauseRefreshOnSubsequentActive() = coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)

        controller.setActiveState(true)
        controller.setActiveState(false)
        controller.setActiveState(true)
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun setActiveStateCausesRefreshIfPendingRefreshWhenActive() = coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)

        controller.setActiveState(true)
        controller.setActiveState(false)
        controller.requestRefresh()
        controller.setActiveState(true)
        observer.finish()

        observer.assertValues(Unit, Unit)
    }

    @Test
    fun requestRefreshDoesNotCauseRefreshWhenNotActive() = coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)

        controller.setActiveState(true)
        controller.setActiveState(false)
        controller.requestRefresh()
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun requestRefreshCausesRefreshWhenActive() = coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)

        controller.setActiveState(true)
        controller.requestRefresh()
        observer.finish()

        observer.assertValues(Unit, Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsNullAndEnabledIsFalse() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(null, false)
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsNullAndEnabledIsTrue() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(null, true)
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsInProgressAndEnabledIsFalse() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(UiTransformedResult.InProgress, false)
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsInProgressAndEnabledIsTrue() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(UiTransformedResult.InProgress, true)
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsErrorAndEnabledIsFalseAndDelayLessThanInterval() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120001L - AUTO_REFRESH_INTERVAL_MILLIS,
                ErrorType.SERVER_ERROR)

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(data, false)
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsErrorAndEnabledIsFalseAndDelayEqualsInterval() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS,
                ErrorType.SERVER_ERROR)

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(data, false)
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsErrorAndEnabledIsFalseAndDelayMoreThanInterval() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS,
                ErrorType.SERVER_ERROR)

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(data, false)
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsErrorAndEnabledIsTrueAndDelayLessThanInterval() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120001L - AUTO_REFRESH_INTERVAL_MILLIS,
                ErrorType.SERVER_ERROR)

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(data, true)
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedCauseRefreshWhenResultIsErrorAndEnabledIsTrueAndDelayEqualsInterval() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS,
                ErrorType.SERVER_ERROR)

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(data, true)
        observer.finish()

        observer.assertValues(Unit, Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedCauseRefreshWhenResultIsErrorAndEnabledIsTrueAndDelayMoreThanInterval() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS - 1L,
                ErrorType.SERVER_ERROR)

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(data, true)
        observer.finish()

        observer.assertValues(Unit, Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsSuccessAndEnabledFalseAndDelayLessThanInterval() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120001L - AUTO_REFRESH_INTERVAL_MILLIS, emptyList())

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(data, false)
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsSuccessAndEnabledIsFalseAndDelayEqualsInterval() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS, emptyList())

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(data, false)
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsSuccessAndEnabledFalseAndDelayMoreThanInterval() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS, emptyList())

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(data, false)
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedDoesNotCauseRefreshWhenResultIsSuccessAndEnabledTrueAndDelayLessThanInterval() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120001L - AUTO_REFRESH_INTERVAL_MILLIS, emptyList())

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(data, true)
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedCausesRefreshWhenResultIsSuccessAndEnabledIsTrueAndDelayEqualsInterval() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS, emptyList())

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(data, true)
        observer.finish()

        observer.assertValues(Unit, Unit)
    }

    @Test
    fun onAutoRefreshPreferenceChangedCausesRefreshWhenResultIsSuccessAndEnabledIsTrueAndDelayMoreThanInterval() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS - 1L,
                emptyList())

        controller.setActiveState(true)
        controller.onAutoRefreshPreferenceChanged(data, true)
        observer.finish()

        observer.assertValues(Unit, Unit)
    }

    @Test
    fun performAutoRefreshDelayDoesNotCauseRefreshWhenResultIsInProgress() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)

        controller.setActiveState(true)
        controller.performAutoRefreshDelay(UiTransformedResult.InProgress) { true }
        observer.finish()

        observer.assertValues(Unit)
    }

    @Test
    fun performAutoRefreshDelayReturnsImmediatelyWhenResultIsErrorAndCalculatedDelayIsNegative() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS - 1L,
                ErrorType.SERVER_ERROR)

        controller.setActiveState(true)
        val startTime = currentTime
        controller.performAutoRefreshDelay(data) { false }
        val endTime = currentTime
        observer.finish()

        observer.assertValues(Unit)
        assertEquals(0, endTime - startTime)
    }

    @Test
    fun performAutoRefreshDelayCausesRefreshWhenResultIsErrorAndCalculatedDelayIsNegativeAndPredicateIsTrue() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS - 1L,
                ErrorType.SERVER_ERROR)

        controller.setActiveState(true)
        val startTime = currentTime
        controller.performAutoRefreshDelay(data) { true }
        val endTime = currentTime
        observer.finish()

        observer.assertValues(Unit, Unit)
        assertEquals(0, endTime - startTime)
    }

    @Test
    fun performAutoRefreshDelayReturnsImmediatelyWhenResultIsErrorAndCalculatedDelayIsZero() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS,
                ErrorType.SERVER_ERROR)

        controller.setActiveState(true)
        val startTime = currentTime
        controller.performAutoRefreshDelay(data) { false }
        val endTime = currentTime
        observer.finish()

        observer.assertValues(Unit)
        assertEquals(0, endTime - startTime)
    }

    @Test
    fun performAutoRefreshDelayCausesRefreshWhenResultIsErrorAndCalculatedDelayIsZeroAndPredicateIsTrue() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS,
                ErrorType.SERVER_ERROR)

        controller.setActiveState(true)
        val startTime = currentTime
        controller.performAutoRefreshDelay(data) { true }
        val endTime = currentTime
        observer.finish()

        observer.assertValues(Unit, Unit)
        assertEquals(0, endTime - startTime)
    }

    @Test
    fun performAutoRefreshDelayDelaysWhenResultIsErrorAndCalculatedDelayIsPositive() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS + 1L,
                ErrorType.SERVER_ERROR)

        controller.setActiveState(true)
        val startTime = currentTime
        controller.performAutoRefreshDelay(data) { false }
        val endTime = currentTime
        observer.finish()

        observer.assertValues(Unit)
        assertEquals(1L, endTime - startTime)
    }

    @Test
    fun performAutoRefreshDelayCausesRefreshWhenResultIsErrorAndCalculatedDelayIsPositiveAndPredicateIsTrue() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS + 1L,
                ErrorType.SERVER_ERROR)

        controller.setActiveState(true)
        val startTime = currentTime
        controller.performAutoRefreshDelay(data) { true }
        val endTime = currentTime
        observer.finish()

        observer.assertValues(Unit, Unit)
        assertEquals(1L, endTime - startTime)
    }

    @Test
    fun performAutoRefreshDelayReturnsImmediatelyWhenResultIsSuccessAndCalculatedDelayIsNegative() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS - 1L,
                emptyList())

        controller.setActiveState(true)
        val startTime = currentTime
        controller.performAutoRefreshDelay(data) { false }
        val endTime = currentTime
        observer.finish()

        observer.assertValues(Unit)
        assertEquals(0, endTime - startTime)
    }

    @Test
    fun performAutoRefreshDelayCausesRefreshWhenResultIsSuccessAndCalculatedDelayIsNegative() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS - 1L,
                emptyList())

        controller.setActiveState(true)
        val startTime = currentTime
        controller.performAutoRefreshDelay(data) { true }
        val endTime = currentTime
        observer.finish()

        observer.assertValues(Unit, Unit)
        assertEquals(0, endTime - startTime)
    }

    @Test
    fun performAutoRefreshDelayReturnsImmediatelyWhenResultIsSuccessAndCalculatedDelayIsZero() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS, emptyList())

        controller.setActiveState(true)
        val startTime = currentTime
        controller.performAutoRefreshDelay(data) { false }
        val endTime = currentTime
        observer.finish()

        observer.assertValues(Unit)
        assertEquals(0, endTime - startTime)
    }

    @Test
    fun performAutoRefreshDelayCausesRefreshWhenResultIsSuccessAndCalculatedDelayIsZero() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS, emptyList())

        controller.setActiveState(true)
        val startTime = currentTime
        controller.performAutoRefreshDelay(data) { true }
        val endTime = currentTime
        observer.finish()

        observer.assertValues(Unit, Unit)
        assertEquals(0, endTime - startTime)
    }

    @Test
    fun performAutoRefreshDelayDelaysWhenResultIsSuccessAndCalculatedDelayIsPositive() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS + 1L,
                emptyList())

        controller.setActiveState(true)
        val startTime = currentTime
        controller.performAutoRefreshDelay(data) { false }
        val endTime = currentTime
        observer.finish()

        observer.assertValues(Unit)
        assertEquals(1L, endTime - startTime)
    }

    @Test
    fun performAutoRefreshDelayCausesRefreshWhenResultIsSuccessAndCalculatedDelayIsPositive() =
            coroutineRule.runBlockingTest {
        val observer = controller.refreshTriggerReceiveChannel.test(this)
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS + 1L,
                emptyList())

        controller.setActiveState(true)
        val startTime = currentTime
        controller.performAutoRefreshDelay(data) { true }
        val endTime = currentTime
        observer.finish()

        observer.assertValues(Unit, Unit)
        assertEquals(1L, endTime - startTime)
    }

    private fun givenReturnsTimestamp() {
        whenever(timeUtils.getCurrentTimeMillis())
                .thenReturn(120000L)
    }
}