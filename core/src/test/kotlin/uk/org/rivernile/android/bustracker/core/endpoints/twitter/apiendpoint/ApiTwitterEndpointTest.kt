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

package uk.org.rivernile.android.bustracker.core.endpoints.twitter.apiendpoint

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.IOException
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response
import uk.org.rivernile.android.bustracker.core.endpoints.api.ApiKeyGenerator
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.LatestTweetsResponse
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.Tweet
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityRepository
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule

/**
 * Tests for [ApiTwitterEndpoint].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class ApiTwitterEndpointTest {

    companion object {

        private const val MOCK_API_KEY = "apiKey"
    }

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var twitterService: TwitterService
    @Mock
    private lateinit var connectivityRepository: ConnectivityRepository
    @Mock
    private lateinit var apiKeyGenerator: ApiKeyGenerator
    private val appName = "TEST"
    @Mock
    private lateinit var tweetsMapper: TweetsMapper
    @Mock
    private lateinit var exceptionLogger: ExceptionLogger

    private lateinit var endpoint: ApiTwitterEndpoint

    @Before
    fun setUp() {
        endpoint = ApiTwitterEndpoint(
                twitterService,
                connectivityRepository,
                apiKeyGenerator,
                appName,
                tweetsMapper,
                exceptionLogger)
    }

    @Test
    fun getLatestTweetsWithNoInternetConnectivityReturnsNoInternetConnectivity() = runTest {
        givenHasInternetConnectivity(false)

        val result = endpoint.getLatestTweets()

        assertEquals(LatestTweetsResponse.Error.NoConnectivity, result)
        verify(exceptionLogger, never())
            .log(any())
    }

    @Test
    fun getLatestTweetsThrowsIoExceptionReturnsIoError() = runTest {
        givenHasInternetConnectivity(true)
        givenHasGeneratedHashedApiKey()
        val exception = IOException()
        whenever(twitterService.getLatestTweets(MOCK_API_KEY, appName))
                .thenAnswer { throw exception }

        val result = endpoint.getLatestTweets()

        assertEquals(LatestTweetsResponse.Error.Io(exception), result)
        verify(exceptionLogger)
            .log(exception)
    }

    @Test
    fun getLatestTweetsWithUnrecognisedServerErrorReturnsUnrecognisedServerError() = runTest {
        givenHasInternetConnectivity(true)
        givenHasGeneratedHashedApiKey()
        whenever(twitterService.getLatestTweets(MOCK_API_KEY, appName))
                .thenReturn(Response.error(500, "Server error".toResponseBody()))

        val result = endpoint.getLatestTweets()

        assertEquals(LatestTweetsResponse.Error.UnrecognisedServerError, result)
        verify(exceptionLogger, never())
            .log(any())
    }

    @Test
    fun getLatestTweetsWith401ErrorReturnsAuthenticationError() = runTest {
        givenHasInternetConnectivity(true)
        givenHasGeneratedHashedApiKey()
        whenever(twitterService.getLatestTweets(MOCK_API_KEY, appName))
                .thenReturn(Response.error(401, "Unauthorized".toResponseBody()))

        val result = endpoint.getLatestTweets()

        assertEquals(LatestTweetsResponse.Error.Authentication, result)
        verify(exceptionLogger, never())
            .log(any())
    }

    @Test
    fun getLatestTweetsWithSuccessAndNullTweetsReturnsSuccess() = runTest {
        givenHasInternetConnectivity(true)
        givenHasGeneratedHashedApiKey()
        whenever(tweetsMapper.mapTweets(null))
                .thenReturn(null)
        whenever(twitterService.getLatestTweets(MOCK_API_KEY, appName))
                .thenReturn(Response.success(null))

        val result = endpoint.getLatestTweets()

        assertEquals(LatestTweetsResponse.Success(null), result)
        verify(exceptionLogger, never())
            .log(any())
    }

    @Test
    fun getLatestTweetsWithSuccessAndEmptyTweetsReturnsSuccess() = runTest {
        givenHasInternetConnectivity(true)
        givenHasGeneratedHashedApiKey()
        whenever(tweetsMapper.mapTweets(emptyList()))
                .thenReturn(null)
        whenever(twitterService.getLatestTweets(MOCK_API_KEY, appName))
                .thenReturn(Response.success(emptyList()))

        val result = endpoint.getLatestTweets()

        assertEquals(LatestTweetsResponse.Success(null), result)
        verify(exceptionLogger, never())
            .log(any())
    }

    @Test
    fun getLatestTweetsWithSuccessAndPopulatedTweetsReturnsSuccess() = runTest {
        givenHasInternetConnectivity(true)
        givenHasGeneratedHashedApiKey()
        val jsonTweets = listOf<JsonTweet>(mock(), mock(), mock())
        val tweets = listOf<Tweet>(mock(), mock(), mock())
        whenever(tweetsMapper.mapTweets(jsonTweets))
                .thenReturn(tweets)
        whenever(twitterService.getLatestTweets(MOCK_API_KEY, appName))
                .thenReturn(Response.success(jsonTweets))

        val result = endpoint.getLatestTweets()

        assertEquals(LatestTweetsResponse.Success(tweets), result)
        verify(exceptionLogger, never())
            .log(any())
    }

    private fun givenHasInternetConnectivity(hasInternetConnectivity: Boolean) {
        whenever(connectivityRepository.hasInternetConnectivity)
                .thenReturn(hasInternetConnectivity)
    }

    private fun givenHasGeneratedHashedApiKey() {
        whenever(apiKeyGenerator.generateHashedApiKey())
                .thenReturn(MOCK_API_KEY)
    }
}