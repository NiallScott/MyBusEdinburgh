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

package uk.org.rivernile.android.bustracker.core.endpoints.tracker

import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Call
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimesMapper
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.SingleLiveTimesRequest
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityChecker
import uk.org.rivernile.edinburghbustrackerapi.ApiKeyGenerator
import uk.org.rivernile.edinburghbustrackerapi.EdinburghBusTrackerApi
import uk.org.rivernile.edinburghbustrackerapi.bustimes.BusTimes

/**
 * Tests for [EdinburghTrackerEndpoint].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class EdinburghTrackerEndpointTest {

    @Mock
    private lateinit var api: EdinburghBusTrackerApi
    @Mock
    private lateinit var apiKeyGenerator: ApiKeyGenerator
    @Mock
    private lateinit var liveTimesMapper: LiveTimesMapper
    @Mock
    private lateinit var errorMapper: ErrorMapper
    @Mock
    private lateinit var connectivityChecker: ConnectivityChecker

    @Mock
    private lateinit var liveTimesCall: Call<BusTimes>

    private lateinit var endpoint: EdinburghTrackerEndpoint

    @Before
    fun setUp() {
        endpoint = EdinburghTrackerEndpoint(
                api,
                apiKeyGenerator,
                liveTimesMapper,
                errorMapper,
                connectivityChecker)
    }

    @Test
    fun createLiveTimesRequestWithSingleStopCodeCreatesCorrectRequestObject() {
        whenever(apiKeyGenerator.hashedApiKey)
                .thenReturn("API_KEY")
        whenever(api.getBusTimes("API_KEY", 4, "123456"))
                .thenReturn(liveTimesCall)

        val result = endpoint.createLiveTimesRequest("123456", 4)

        assertTrue(result is SingleLiveTimesRequest)
        verify(api)
                .getBusTimes("API_KEY", 4, "123456")
    }

    @Test
    fun createLiveTimesRequestWithFourStopCodesCreatesCorrectRequestObject() {
        whenever(apiKeyGenerator.hashedApiKey)
                .thenReturn("API_KEY")
        whenever(api.getBusTimes("API_KEY", 4, "1", "2", "3", "4", null))
                .thenReturn(liveTimesCall)

        val result = endpoint.createLiveTimesRequest(arrayOf("1", "2", "3", "4"), 4)

        assertTrue(result is SingleLiveTimesRequest)
        verify(api)
                .getBusTimes("API_KEY", 4, "1", "2", "3", "4", null)
    }

    @Test
    fun createLiveTimesRequestWithFiveStopCodesCreatesCorrectRequestObject() {
        whenever(apiKeyGenerator.hashedApiKey)
                .thenReturn("API_KEY")
        whenever(api.getBusTimes("API_KEY", 4, "1", "2", "3", "4", "5"))
                .thenReturn(liveTimesCall)

        val result = endpoint.createLiveTimesRequest(arrayOf("1", "2", "3", "4", "5"), 4)

        assertTrue(result is SingleLiveTimesRequest)
        verify(api)
                .getBusTimes("API_KEY", 4, "1", "2", "3", "4", "5")
    }

    @Test
    fun createLiveTimesRequestWithSixStopCodesOnlyTakesFirstFiveCodes() {
        whenever(apiKeyGenerator.hashedApiKey)
                .thenReturn("API_KEY")
        whenever(api.getBusTimes("API_KEY", 4, "1", "2", "3", "4", "5"))
                .thenReturn(liveTimesCall)

        val result = endpoint.createLiveTimesRequest(arrayOf("1", "2", "3", "4", "5", "6"), 4)

        assertTrue(result is SingleLiveTimesRequest)
        verify(api)
                .getBusTimes("API_KEY", 4, "1", "2", "3", "4", "5")
    }
}