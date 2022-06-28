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

package uk.org.rivernile.android.bustracker.core.twitter

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.NoConnectivityException
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.Tweet
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.TwitterEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.TwitterRequest
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

    @Mock
    private lateinit var latestTweetsRequest: TwitterRequest<List<Tweet>?>

    private lateinit var repository: TwitterRepository

    @Before
    fun setUp() {
        repository = TwitterRepository(twitterEndpoint, coroutineRule.testDispatcher)

        whenever(twitterEndpoint.createLatestTweetsRequest())
                .thenReturn(latestTweetsRequest)
    }

    @Test
    fun getLatestTweetsWithNullListOfTweetsReturnsSuccess() = runTest {
        whenever(latestTweetsRequest.performRequest())
                .thenReturn(null)

        val observer = repository.getLatestTweets().test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                Result.InProgress,
                Result.Success(null))
    }

    @Test
    fun getLatestTweetsWithEmptyListOfTweetsReturnsSuccess() = runTest {
        whenever(latestTweetsRequest.performRequest())
                .thenReturn(emptyList())

        val observer = repository.getLatestTweets().test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                Result.InProgress,
                Result.Success(emptyList()))
    }

    @Test
    fun getLatestTweetsWithPopulatedListOfTweetsReturnsSuccess() = runTest {
        val tweet = mock<Tweet>()
        val result = listOf(tweet)
        whenever(latestTweetsRequest.performRequest())
                .thenReturn(result)

        val observer = repository.getLatestTweets().test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                Result.InProgress,
                Result.Success(result))
    }

    @Test
    fun getLatestTweetsWithExceptionThrownReturnsFailure() = runTest {
        val exception = NoConnectivityException()
        whenever(latestTweetsRequest.performRequest())
                .thenThrow(exception)

        val observer = repository.getLatestTweets().test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                Result.InProgress,
                Result.Error(exception))
    }

    @Test
    fun getLatestTweetWithCancellationCausesCancellationEvent() = runTest {
        whenever(latestTweetsRequest.performRequest())
                .thenThrow(CancellationException::class.java)

        val observer = repository.getLatestTweets().test(this)
        advanceUntilIdle()
        observer.finish()

        verify(latestTweetsRequest)
                .cancel()
    }
}