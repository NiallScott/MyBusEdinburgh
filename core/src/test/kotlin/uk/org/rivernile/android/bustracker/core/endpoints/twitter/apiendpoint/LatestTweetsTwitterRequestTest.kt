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

package uk.org.rivernile.android.bustracker.core.endpoints.twitter.apiendpoint

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Call
import retrofit2.Response
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.AuthenticationException
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.NetworkException
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.NoConnectivityException
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.Tweet
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.UnrecognisedServerErrorException
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityChecker
import java.io.IOException

/**
 * Tests for [LatestTweetsTwitterRequest].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class LatestTweetsTwitterRequestTest {

    @Mock
    private lateinit var call: Call<List<JsonTweet>>
    @Mock
    private lateinit var connectivityChecker: ConnectivityChecker
    @Mock
    private lateinit var mapper: TweetsMapper

    @Mock
    private lateinit var response: Response<List<JsonTweet>>
    @Mock
    private lateinit var jsonTweets: List<JsonTweet>
    @Mock
    private lateinit var tweets: List<Tweet>

    private lateinit var request: LatestTweetsTwitterRequest

    @Before
    fun setUp() {
        request = LatestTweetsTwitterRequest(call, connectivityChecker, mapper)
    }

    @Test
    fun cancelCallsCancelOnCall() {
        request.cancel()

        verify(call)
                .cancel()
    }

    @Test(expected = NoConnectivityException::class)
    fun performRequestWithNoConnectivityThrowsNoConnectivityException() {
        givenHasNoConnectivity()

        request.performRequest()
    }

    @Test(expected = NetworkException::class)
    fun performRequestWithConnectivityButHasIoExceptionThrowsNetworkException() {
        givenHasConnectivity()
        whenever(call.execute())
                .thenThrow(IOException::class.java)

        request.performRequest()
    }

    @Test(expected = AuthenticationException::class)
    fun performRequestWithConnectivityButWithErrorCode401ThrowsAuthenticationException() {
        givenHasConnectivity()
        givenExecuteCallReturnsResponse()
        givenResponseIsNotSuccessful()
        whenever(response.code())
                .thenReturn(401)

        request.performRequest()
    }

    @Test(expected = UnrecognisedServerErrorException::class)
    fun performRequestWithConnectivityWithRandomErrorCodeThrowsUnrecognisedServerErrorException() {
        givenHasConnectivity()
        givenExecuteCallReturnsResponse()
        givenResponseIsNotSuccessful()
        whenever(response.code())
                .thenReturn(402)

        request.performRequest()
    }

    @Test
    fun performRequestWithConnectivityAndIsSuccessfulReturnsMappedResponse() {
        givenHasConnectivity()
        givenExecuteCallReturnsResponse()
        givenResponseIsSuccessful()
        whenever(response.body())
                .thenReturn(jsonTweets)
        whenever(mapper.mapTweets(jsonTweets))
                .thenReturn(tweets)

        val result = request.performRequest()

        assertEquals(tweets, result)
    }

    private fun givenHasNoConnectivity() {
        whenever(connectivityChecker.hasInternetConnectivity())
                .thenReturn(false)
    }

    private fun givenHasConnectivity() {
        whenever(connectivityChecker.hasInternetConnectivity())
                .thenReturn(true)
    }

    private fun givenExecuteCallReturnsResponse() {
        whenever(call.execute())
                .thenReturn(response)
    }

    private fun givenResponseIsNotSuccessful() {
        whenever(response.isSuccessful)
                .thenReturn(false)
    }

    private fun givenResponseIsSuccessful() {
        whenever(response.isSuccessful)
                .thenReturn(true)
    }
}