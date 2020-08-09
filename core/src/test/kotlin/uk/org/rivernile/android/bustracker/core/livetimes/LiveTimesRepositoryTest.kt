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

package uk.org.rivernile.android.bustracker.core.livetimes

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.NoConnectivityException
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.TrackerEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.TrackerRequest
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimes
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [LiveTimesRepository].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class LiveTimesRepositoryTest {

    @Rule
    @JvmField
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var trackerEndpoint: TrackerEndpoint

    @Mock
    private lateinit var trackerRequest: TrackerRequest<LiveTimes>
    @Mock
    private lateinit var liveTimes: LiveTimes

    private lateinit var repository: LiveTimesRepository

    @Before
    fun setUp() {
        repository = LiveTimesRepository(trackerEndpoint, coroutineRule.testDispatcher)

        whenever(trackerEndpoint.createLiveTimesRequest(any<String>(), any()))
                .thenReturn(trackerRequest)
    }

    @Test
    fun getLiveTimesFlowWithNoExceptionProducesSuccessfulResult() = coroutineRule.runBlockingTest {
        whenever(trackerRequest.performRequest())
                .thenReturn(liveTimes)

        val observer = repository.getLiveTimesFlow("123456", 4).test(this)
        observer.finish()

        observer.assertValues(Result.InProgress, Result.Success(liveTimes))
    }

    @Test
    fun getLiveTimesFlowWithExceptionProducesErrorResult() = coroutineRule.runBlockingTest {
        val exception = NoConnectivityException()
        whenever(trackerRequest.performRequest())
                .thenThrow(exception)

        val observer = repository.getLiveTimesFlow("123456", 4).test(this)
        observer.finish()

        observer.assertValues(
                Result.InProgress,
                Result.Error(exception))
    }

    @Test
    fun getLiveTimesFlowWithCancellationCausesCancellationEvent() = coroutineRule.runBlockingTest {
        whenever(trackerRequest.performRequest())
                .thenThrow(CancellationException::class.java)

        val observer = repository.getLiveTimesFlow("123456", 4).test(this)
        observer.finish()

        verify(trackerRequest)
                .cancel()
    }
}