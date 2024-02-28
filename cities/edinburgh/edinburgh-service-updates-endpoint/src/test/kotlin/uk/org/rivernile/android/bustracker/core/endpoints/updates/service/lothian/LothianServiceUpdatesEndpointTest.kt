/*
 * Copyright (C) 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.endpoints.updates.service.lothian

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.IOException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.isA
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdate
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdateType
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdatesEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdatesResponse
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityRepository
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Tests for [LothianServiceUpdatesEndpoint].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class LothianServiceUpdatesEndpointTest {

    @Mock
    private lateinit var lothianServiceUpdatesApi: LothianServiceUpdatesApi
    @Mock
    private lateinit var connectivityRepository: ConnectivityRepository
    @Mock
    private lateinit var serviceUpdatesMapper: ServiceUpdatesMapper
    @Mock
    private lateinit var exceptionLogger: ExceptionLogger

    private lateinit var endpoint: ServiceUpdatesEndpoint

    @Before
    fun setUp() {
        endpoint = LothianServiceUpdatesEndpoint(
            lothianServiceUpdatesApi,
            connectivityRepository,
            serviceUpdatesMapper,
            exceptionLogger
        )
    }

    @Test
    fun getServiceUpdatesWithNoConnectivityReturnsNoConnectivity() = runTest {
        givenHasInternetConnectivity(false)

        val result = endpoint.getServiceUpdates()

        assertEquals(ServiceUpdatesResponse.Error.NoConnectivity, result)
    }

    @Test
    fun getServiceUpdatesReturnsServerErrorWhenSerializationExceptionIsThrown() = runTest {
        givenHasInternetConnectivity(true)
        val exception = SerializationException()
        whenever(lothianServiceUpdatesApi.getServiceUpdates())
            .doAnswer {
                throw exception
            }

        val result = endpoint.getServiceUpdates()

        assertIs<ServiceUpdatesResponse.Error.ServerError>(result)
        verify(exceptionLogger)
            .log(exception)
    }

    @Test
    fun getServiceUpdatesReturnsIoErrorWhenIOExceptionIsThrown() = runTest {
        givenHasInternetConnectivity(true)
        val exception = IOException()
        whenever(lothianServiceUpdatesApi.getServiceUpdates())
            .doAnswer {
                throw exception
            }

        val result = endpoint.getServiceUpdates()

        assertEquals(ServiceUpdatesResponse.Error.Io(exception), result)
        verify(exceptionLogger)
            .log(exception)
    }

    @Test
    fun getServiceUpdatesReturnsServerErrorWhenServerReturnsNonSuccessCode() = runTest {
        givenHasInternetConnectivity(true)
        whenever(lothianServiceUpdatesApi.getServiceUpdates())
            .thenReturn(Response.error(400, "Unauthorized".toResponseBody()))

        val result = endpoint.getServiceUpdates()

        assertIs<ServiceUpdatesResponse.Error.ServerError>(result)
        verify(exceptionLogger)
            .log(isA<RuntimeException>())
    }

    @Test
    fun getServiceUpdatesReturnsSuccessWhenServerReturnsSuccessCode() = runTest {
        givenHasInternetConnectivity(true)
        val responseBody = JsonServiceUpdateEvents()
        whenever(lothianServiceUpdatesApi.getServiceUpdates())
            .thenReturn(Response.success(responseBody))
        val serviceUpdates = listOf(
            ServiceUpdate(
                id = "id",
                lastUpdated = Instant.fromEpochMilliseconds(123L),
                serviceUpdateType = ServiceUpdateType.INCIDENT,
                summary = "Description",
                affectedServices = setOf("1"),
                url = "https://some/url"
            )
        )
        whenever(serviceUpdatesMapper.mapToServiceUpdates(responseBody))
            .thenReturn(serviceUpdates)

        val result = endpoint.getServiceUpdates()

        assertEquals(ServiceUpdatesResponse.Success(serviceUpdates), result)
    }

    private fun givenHasInternetConnectivity(hasInternetConnectivity: Boolean) {
        whenever(connectivityRepository.hasInternetConnectivity)
            .thenReturn(hasInternetConnectivity)
    }
}