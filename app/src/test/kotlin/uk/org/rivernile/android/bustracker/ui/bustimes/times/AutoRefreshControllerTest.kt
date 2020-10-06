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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.utils.TimeUtils
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule

/**
 * Tests for [AutoRefreshController].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AutoRefreshControllerTest {

    companion object {

        private const val AUTO_REFRESH_INTERVAL_MILLIS = 60000L
    }

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var timeUtils: TimeUtils

    private lateinit var controller: AutoRefreshController

    @Before
    fun setUp() {
        controller = AutoRefreshController(timeUtils)
    }

    @Test
    fun shouldCauseRefreshReturnsFalseWhenResultIsNullAndEnabledIsFalse() {
        val result = controller.shouldCauseRefresh(null, false)

        assertFalse(result)
    }

    @Test
    fun shouldCauseRefreshReturnsFalseWhenResultIsNullAndEnabledIsTrue() {
        val result = controller.shouldCauseRefresh(null, true)

        assertFalse(result)
    }

    @Test
    fun shouldCauseRefreshReturnsFalseWhenResultIsInProgressAndEnabledIsFalse() {
        val result = controller.shouldCauseRefresh(UiTransformedResult.InProgress, false)

        assertFalse(result)
    }

    @Test
    fun shouldCauseRefreshReturnsFalseWhenResultIsInProgressAndEnabledIsTrue() {
        val result = controller.shouldCauseRefresh(UiTransformedResult.InProgress, true)

        assertFalse(result)
    }

    @Test
    fun shouldCauseRefreshReturnsFalseWhenResultIsErrorAndEnabledIsFalseAndDelayLessThanInterval() {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120001L - AUTO_REFRESH_INTERVAL_MILLIS,
                ErrorType.SERVER_ERROR)

        val result = controller.shouldCauseRefresh(data, false)

        assertFalse(result)
    }

    @Test
    fun shouldCauseRefreshReturnsFalseWhenResultIsErrorAndEnabledIsFalseAndDelayEqualsInterval() {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS,
                ErrorType.SERVER_ERROR)

        val result = controller.shouldCauseRefresh(data, false)

        assertFalse(result)
    }

    @Test
    fun shouldCauseRefreshReturnsFalseWhenResultIsErrorAndEnabledIsFalseAndDelayMoreThanInterval() {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS,
                ErrorType.SERVER_ERROR)

        val result = controller.shouldCauseRefresh(data, false)

        assertFalse(result)
    }

    @Test
    fun shouldCauseRefreshReturnsFalseWhenResultIsErrorAndEnabledIsTrueAndDelayLessThanInterval() {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120001L - AUTO_REFRESH_INTERVAL_MILLIS,
                ErrorType.SERVER_ERROR)

        val result = controller.shouldCauseRefresh(data, true)

        assertFalse(result)
    }

    @Test
    fun shouldCauseRefreshReturnsTrueWhenResultIsErrorAndEnabledIsTrueAndDelayEqualsInterval() {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS,
                ErrorType.SERVER_ERROR)

        val result = controller.shouldCauseRefresh(data, true)

        assertTrue(result)
    }

    @Test
    fun shouldCauseRefreshReturnsTrueWhenResultIsErrorAndEnabledIsTrueAndDelayMoreThanInterval() {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS - 1L,
                ErrorType.SERVER_ERROR)

        val result = controller.shouldCauseRefresh(data, true)

        assertTrue(result)
    }

    @Test
    fun shouldCauseRefreshReturnsFalseWhenResultIsSuccessAndEnabledFalseAndDelayLessThanInterval() {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120001L - AUTO_REFRESH_INTERVAL_MILLIS, emptyList())

        val result = controller.shouldCauseRefresh(data, false)

        assertFalse(result)
    }

    @Test
    fun shouldCauseRefreshReturnsFalseWhenResultIsSuccessAndEnabledIsFalseAndDelayEqualsInterval() {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS, emptyList())

        val result = controller.shouldCauseRefresh(data, false)

        assertFalse(result)
    }

    @Test
    fun shouldCauseRefreshReturnsFalseWhenResultIsSuccessAndEnabledFalseAndDelayMoreThanInterval() {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS, emptyList())

        val result = controller.shouldCauseRefresh(data, false)

        assertFalse(result)
    }

    @Test
    fun shouldCauseRefreshReturnsFalseWhenResultIsSuccessAndEnabledTrueAndDelayLessThanInterval() {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120001L - AUTO_REFRESH_INTERVAL_MILLIS, emptyList())

        val result = controller.shouldCauseRefresh(data, true)

        assertFalse(result)
    }

    @Test
    fun shouldCauseRefreshReturnsTrueWhenResultIsSuccessAndEnabledIsTrueAndDelayEqualsInterval() {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS, emptyList())

        val result = controller.shouldCauseRefresh(data, true)

        assertTrue(result)
    }

    @Test
    fun shouldCauseRefreshReturnsTrueWhenResultIsSuccessAndEnabledIsTrueAndDelayMoreThanInterval() {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS - 1L,
                emptyList())

        val result = controller.shouldCauseRefresh(data, true)

        assertTrue(result)
    }

    @Test
    fun performAutoRefreshDelayReturnsFalseWhenResultIsInProgress() =
            coroutineRule.runBlockingTest {
        val result = controller.performAutoRefreshDelay(UiTransformedResult.InProgress)

        assertFalse(result)
    }

    @Test
    fun performAutoRefreshDelayReturnsImmediatelyWhenResultIsErrorAndCalculatedDelayIsNegative() =
            coroutineRule.runBlockingTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS - 1L,
                ErrorType.SERVER_ERROR)

        val startTime = currentTime
        val result = controller.performAutoRefreshDelay(data)
        val endTime = currentTime

        assertTrue(result)
        assertEquals(0, endTime - startTime)
    }

    @Test
    fun performAutoRefreshDelayReturnsImmediatelyWhenResultIsErrorAndCalculatedDelayIsZero() =
            coroutineRule.runBlockingTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS,
                ErrorType.SERVER_ERROR)

        val startTime = currentTime
        val result = controller.performAutoRefreshDelay(data)
        val endTime = currentTime

        assertTrue(result)
        assertEquals(0, endTime - startTime)
    }

    @Test
    fun performAutoRefreshDelayDelaysWhenResultIsErrorAndCalculatedDelayIsPositive() =
            coroutineRule.runBlockingTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Error(120000L - AUTO_REFRESH_INTERVAL_MILLIS + 1L,
                ErrorType.SERVER_ERROR)

        val startTime = currentTime
        val result = controller.performAutoRefreshDelay(data)
        val endTime = currentTime

        assertTrue(result)
        assertEquals(1L, endTime - startTime)
    }

    @Test
    fun performAutoRefreshDelayReturnsImmediatelyWhenResultIsSuccessAndCalculatedDelayIsNegative() =
            coroutineRule.runBlockingTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS - 1L,
                emptyList())

        val startTime = currentTime
        val result = controller.performAutoRefreshDelay(data)
        val endTime = currentTime

        assertTrue(result)
        assertEquals(0, endTime - startTime)
    }

    @Test
    fun performAutoRefreshDelayReturnsImmediatelyWhenResultIsSuccessAndCalculatedDelayIsZero() =
            coroutineRule.runBlockingTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS, emptyList())

        val startTime = currentTime
        val result = controller.performAutoRefreshDelay(data)
        val endTime = currentTime

        assertTrue(result)
        assertEquals(0, endTime - startTime)
    }

    @Test
    fun performAutoRefreshDelayDelaysWhenResultIsSuccessAndCalculatedDelayIsPositive() =
            coroutineRule.runBlockingTest {
        givenReturnsTimestamp()
        val data = UiTransformedResult.Success(120000L - AUTO_REFRESH_INTERVAL_MILLIS + 1L,
                emptyList())

        val startTime = currentTime
        val result = controller.performAutoRefreshDelay(data)
        val endTime = currentTime

        assertTrue(result)
        assertEquals(1L, endTime - startTime)
    }

    private fun givenReturnsTimestamp() {
        whenever(timeUtils.getCurrentTimeMillis())
                .thenReturn(120000L)
    }
}