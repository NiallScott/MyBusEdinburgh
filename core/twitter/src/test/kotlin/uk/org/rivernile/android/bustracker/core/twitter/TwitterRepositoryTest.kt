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

package uk.org.rivernile.android.bustracker.core.twitter

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okio.IOException
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.LatestTweetsResponse
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.Tweet
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.TwitterEndpoint
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [TwitterRepository].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class TwitterRepositoryTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var twitterEndpoint: TwitterEndpoint

    private lateinit var repository: TwitterRepository

    @Before
    fun setUp() {
        repository = TwitterRepository(twitterEndpoint)
    }

    @Test
    fun latestTweetsFlowWithNullListOfTweetsEmitsSuccess() = runTest {
        whenever(twitterEndpoint.getLatestTweets())
            .thenReturn(LatestTweetsResponse.Success(null))

        val observer = repository.latestTweetsFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            LatestTweetsResult.InProgress,
            LatestTweetsResult.Success(null))
    }

    @Test
    fun latestTweetsFlowWithEmptyListOfTweetsEmitsSuccess() = runTest {
        whenever(twitterEndpoint.getLatestTweets())
            .thenReturn(LatestTweetsResponse.Success(emptyList()))

        val observer = repository.latestTweetsFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            LatestTweetsResult.InProgress,
            LatestTweetsResult.Success(emptyList()))
    }

    @Test
    fun latestTweetsFlowWithPopulatedListOfTweetsEmitsSuccess() = runTest {
        val result = listOf(mock<Tweet>())
        whenever(twitterEndpoint.getLatestTweets())
            .thenReturn(LatestTweetsResponse.Success(result))

        val observer = repository.latestTweetsFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            LatestTweetsResult.InProgress,
            LatestTweetsResult.Success(result))
    }

    @Test
    fun latestTweetsFlowWithNoConnectivityEmitsNoConnectivityError() = runTest {
        whenever(twitterEndpoint.getLatestTweets())
            .thenReturn(LatestTweetsResponse.Error.NoConnectivity)

        val observer = repository.latestTweetsFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            LatestTweetsResult.InProgress,
            LatestTweetsResult.Error.NoConnectivity)
    }

    @Test
    fun latestTweetsFlowWithIoErrorEmitsIoError() = runTest {
        val exception = IOException()
        whenever(twitterEndpoint.getLatestTweets())
            .thenReturn(LatestTweetsResponse.Error.Io(exception))

        val observer = repository.latestTweetsFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            LatestTweetsResult.InProgress,
            LatestTweetsResult.Error.Io(exception))
    }

    @Test
    fun latestTweetsFlowWithAuthenticationErrorEmitsServerError() = runTest {
        whenever(twitterEndpoint.getLatestTweets())
            .thenReturn(LatestTweetsResponse.Error.Authentication)

        val observer = repository.latestTweetsFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            LatestTweetsResult.InProgress,
            LatestTweetsResult.Error.Server)
    }

    @Test
    fun latestTweetsFlowWithUnrecognisedServerErrorEmitsServerError() = runTest {
        whenever(twitterEndpoint.getLatestTweets())
            .thenReturn(LatestTweetsResponse.Error.UnrecognisedServerError)

        val observer = repository.latestTweetsFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            LatestTweetsResult.InProgress,
            LatestTweetsResult.Error.Server)
    }
}