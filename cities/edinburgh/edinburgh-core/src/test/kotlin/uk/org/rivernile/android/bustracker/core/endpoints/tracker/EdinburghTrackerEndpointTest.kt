/*
 * Copyright (C) 2020 - 2024 Niall 'Rivernile' Scott
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

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okio.IOException
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimesMapper
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimesResponse
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityRepository
import uk.org.rivernile.edinburghbustrackerapi.ApiKeyGenerator
import uk.org.rivernile.edinburghbustrackerapi.EdinburghBusTrackerApi
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [EdinburghTrackerEndpoint].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class EdinburghTrackerEndpointTest {

    companion object {

        private const val MOCK_API_KEY = "api_key"
    }

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
    @Mock
    private lateinit var exceptionLogger: ExceptionLogger

    private lateinit var endpoint: EdinburghTrackerEndpoint

    @BeforeTest
    fun setUp() {
        endpoint = EdinburghTrackerEndpoint(
            api,
            apiKeyGenerator,
            liveTimesMapper,
            errorMapper,
            responseHandler,
            connectivityRepository,
            exceptionLogger
        )
    }

    @Test
    fun getLiveBusTimesSingleWithNoConnectivityReturnsNoConnectivity() = runTest {
        whenever(connectivityRepository.hasInternetConnectivity)
            .thenReturn(false)

        val result = endpoint.getLiveTimes("123456", 1)

        assertEquals(LiveTimesResponse.Error.NoConnectivity, result)
        verify(exceptionLogger, never())
            .log(any())
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
        verify(exceptionLogger)
            .log(throwable)
    }

    @Test
    fun getLiveBusTimesSingleWithSerializationExceptionReturnsIoError() = runTest {
        whenever(connectivityRepository.hasInternetConnectivity)
            .thenReturn(true)
        whenever(apiKeyGenerator.hashedApiKey)
            .thenReturn(MOCK_API_KEY)
        val throwable = SerializationException()
        whenever(api.getBusTimes(MOCK_API_KEY, 1, "123456"))
            .thenAnswer { throw throwable }

        val result = endpoint.getLiveTimes("123456", 1)

        assertEquals(LiveTimesResponse.Error.Io(throwable), result)
        verify(exceptionLogger)
            .log(throwable)
    }

    @Test
    fun getLiveBusTimesMultipleWithNoConnectivityReturnsNoConnectivity() = runTest {
        whenever(connectivityRepository.hasInternetConnectivity)
            .thenReturn(false)

        val result = endpoint.getLiveTimes(listOf("1", "2", "3", "4", "5"), 1)

        assertEquals(LiveTimesResponse.Error.NoConnectivity, result)
        verify(exceptionLogger, never())
            .log(any())
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
        verify(exceptionLogger)
            .log(throwable)
    }

    @Test
    fun getLiveBusTimesMultipleWithSerializationExceptionReturnsIoError() = runTest {
        whenever(connectivityRepository.hasInternetConnectivity)
            .thenReturn(true)
        whenever(apiKeyGenerator.hashedApiKey)
            .thenReturn(MOCK_API_KEY)
        val throwable = SerializationException()
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
        verify(exceptionLogger)
            .log(throwable)
    }
}