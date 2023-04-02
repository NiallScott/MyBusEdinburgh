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

package uk.org.rivernile.android.bustracker.core.endpoints.tracker

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okio.IOException
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimesMapper
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimesResponse
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityRepository
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.edinburghbustrackerapi.ApiKeyGenerator
import uk.org.rivernile.edinburghbustrackerapi.EdinburghBusTrackerApi

/**
 * Tests for [EdinburghTrackerEndpoint].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class EdinburghTrackerEndpointTest {

    companion object {

        private const val MOCK_API_KEY = "api_key"
    }

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var api: EdinburghBusTrackerApi
    @Mock
    private lateinit var apiKeyGenerator: ApiKeyGenerator
    @Mock
    private lateinit var liveTimesMapper: LiveTimesMapper
    @Mock
    private lateinit var errorMapper: ErrorMapper
    @Mock
    private lateinit var responseHandler: ResponseHandler
    @Mock
    private lateinit var connectivityRepository: ConnectivityRepository

    private lateinit var endpoint: EdinburghTrackerEndpoint

    @Before
    fun setUp() {
        endpoint = EdinburghTrackerEndpoint(
            api,
            apiKeyGenerator,
            liveTimesMapper,
            errorMapper,
            responseHandler,
            connectivityRepository)
    }

    @Test
    fun getLiveBusTimesSingleWithNoConnectivityReturnsNoConnectivity() = runTest {
        whenever(connectivityRepository.hasInternetConnectivity)
            .thenReturn(false)

        val result = endpoint.getLiveTimes("123456", 1)

        assertEquals(LiveTimesResponse.Error.NoConnectivity, result)
    }

    @Test
    fun getLiveBusTimesSingleWithIoExceptionReturnsIoError() = runTest {
        whenever(connectivityRepository.hasInternetConnectivity)
            .thenReturn(true)
        whenever(apiKeyGenerator.hashedApiKey)
            .thenReturn(MOCK_API_KEY)
        val throwable = IOException()
        whenever(api.getBusTimes(MOCK_API_KEY, 1, "123456"))
            .thenAnswer { throw throwable }

        val result = endpoint.getLiveTimes("123456", 1)

        assertEquals(LiveTimesResponse.Error.Io(throwable), result)
    }

    @Test
    fun getLiveBusTimesMultipleWithNoConnectivityReturnsNoConnectivity() = runTest {
        whenever(connectivityRepository.hasInternetConnectivity)
            .thenReturn(false)

        val result = endpoint.getLiveTimes(listOf("1", "2", "3", "4", "5"), 1)

        assertEquals(LiveTimesResponse.Error.NoConnectivity, result)
    }

    @Test
    fun getLiveBusTimesMultipleWithIoExceptionReturnsIoError() = runTest {
        whenever(connectivityRepository.hasInternetConnectivity)
            .thenReturn(true)
        whenever(apiKeyGenerator.hashedApiKey)
            .thenReturn(MOCK_API_KEY)
        val throwable = IOException()
        whenever(api.getBusTimes(
            MOCK_API_KEY,
            1,
            "1",
            "2",
            "3",
            "4",
            "5"))
            .thenAnswer { throw throwable }

        val result = endpoint.getLiveTimes(listOf("1", "2", "3", "4", "5"), 1)

        assertEquals(LiveTimesResponse.Error.Io(throwable), result)
    }
}