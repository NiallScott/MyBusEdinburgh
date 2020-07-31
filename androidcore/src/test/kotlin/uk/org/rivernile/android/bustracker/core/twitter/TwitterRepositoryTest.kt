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

package uk.org.rivernile.android.bustracker.core.twitter

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.mock
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
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.NoConnectivityException
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.Tweet
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.TwitterEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.TwitterRequest
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule

/**
 * Tests for [TwitterRepository].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class TwitterRepositoryTest {

    @Rule
    @JvmField
    val coroutineRule = MainCoroutineRule()
    @Rule
    @JvmField
    val liveDataTaskExecutor = InstantTaskExecutorRule()

    @Mock
    private lateinit var twitterEndpoint: TwitterEndpoint

    @Mock
    private lateinit var latestTweetsRequest: TwitterRequest<List<Tweet>?>
    @Mock
    private lateinit var latestTweetsObserver: Observer<Result<List<Tweet>?>>

    private lateinit var repository: TwitterRepository

    @Before
    fun setUp() {
        repository = TwitterRepository(twitterEndpoint, coroutineRule.testDispatcher)

        whenever(twitterEndpoint.createLatestTweetsRequest())
                .thenReturn(latestTweetsRequest)
    }

    @Test
    fun getLatestTweetsSetsInitialStateToInProgress() = coroutineRule.runBlockingTest {
        repository.getLatestTweets().observeForever(latestTweetsObserver)

        verify(latestTweetsObserver)
                .onChanged(Result.InProgress)
    }

    @Test
    fun getLatestTweetsWithNullListOfTweetsReturnsSuccess() = coroutineRule.runBlockingTest {
        whenever(latestTweetsRequest.performRequest())
                .thenReturn(null)

        repository.getLatestTweets().observeForever(latestTweetsObserver)

        verify(latestTweetsObserver)
                .onChanged(Result.Success(null))
    }

    @Test
    fun getLatestTweetsWithEmptyListOfTweetsReturnsSuccess() = coroutineRule.runBlockingTest {
        whenever(latestTweetsRequest.performRequest())
                .thenReturn(emptyList())

        repository.getLatestTweets().observeForever(latestTweetsObserver)

        verify(latestTweetsObserver)
                .onChanged(Result.Success(emptyList()))
    }

    @Test
    fun getLatestTweetsWithPopulatedListOfTweetsReturnsSuccess() = coroutineRule.runBlockingTest {
        val tweet = mock<Tweet>()
        val result = listOf(tweet)
        whenever(latestTweetsRequest.performRequest())
                .thenReturn(result)

        repository.getLatestTweets().observeForever(latestTweetsObserver)

        verify(latestTweetsObserver)
                .onChanged(Result.Success(result))
    }

    @Test
    fun getLatestTweetsWithExceptionThrownReturnsFailure() = coroutineRule.runBlockingTest {
        val exception = NoConnectivityException()
        whenever(latestTweetsRequest.performRequest())
                .thenThrow(exception)

        repository.getLatestTweets().observeForever(latestTweetsObserver)

        verify(latestTweetsObserver)
                .onChanged(Result.Error(exception))
    }

    @Test
    fun getLatestTweetWithCancellationCausesCancellationEvent() = coroutineRule.runBlockingTest {
        whenever(latestTweetsRequest.performRequest())
                .thenThrow(CancellationException::class.java)

        repository.getLatestTweets().observeForever(latestTweetsObserver)

        verify(latestTweetsRequest)
                .cancel()
    }
}