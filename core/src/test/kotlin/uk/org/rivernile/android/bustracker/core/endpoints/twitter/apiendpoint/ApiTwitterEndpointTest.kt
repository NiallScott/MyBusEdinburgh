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

import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Call
import uk.org.rivernile.android.bustracker.core.endpoints.api.ApiKeyGenerator
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityChecker

/**
 * Tests for [ApiTwitterEndpoint].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class ApiTwitterEndpointTest {

    @Mock
    private lateinit var twitterService: TwitterService
    @Mock
    private lateinit var connectivityChecker: ConnectivityChecker
    @Mock
    private lateinit var apiKeyGenerator: ApiKeyGenerator
    private val appName = "TEST"
    @Mock
    private lateinit var tweetsMapper: TweetsMapper

    @Mock
    private lateinit var latestTweetsCall: Call<List<JsonTweet>>

    private lateinit var endpoint: ApiTwitterEndpoint

    @Before
    fun setUp() {
        endpoint = ApiTwitterEndpoint(twitterService, connectivityChecker, apiKeyGenerator,
                appName, tweetsMapper)
    }

    @Test
    fun createLatestTweetsRequestCreatesCorrectRequestObject() {
        whenever(apiKeyGenerator.generateHashedApiKey())
                .thenReturn("API_KEY")
        whenever(twitterService.getLatestTweets("API_KEY", appName))
                .thenReturn(latestTweetsCall)

        val result = endpoint.createLatestTweetsRequest()

        assertTrue(result is LatestTweetsTwitterRequest)
        verify(twitterService)
                .getLatestTweets("API_KEY", appName)
    }
}