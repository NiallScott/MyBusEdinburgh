/*
 * Copyright (C) 2024 - 2025 Niall 'Rivernile' Scott
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
import retrofit2.Response
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdate
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdateType
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdatesResponse
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import uk.org.rivernile.android.bustracker.core.log.FakeExceptionLogger
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityRepository
import uk.org.rivernile.android.bustracker.core.networking.FakeConnectivityRepository
import uk.org.rivernile.android.bustracker.core.time.FakeTimeUtils
import uk.org.rivernile.android.bustracker.core.time.TimeUtils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

/**
 * Tests for [LothianServiceUpdatesEndpoint].
 *
 * @author Niall Scott
 */
class LothianServiceUpdatesEndpointTest {

    @Test
    fun getServiceUpdatesWithNoConnectivityReturnsNoConnectivity() = runTest {
        val endpoint = createLothianServiceUpdatesEndpoint(
            connectivityRepository = FakeConnectivityRepository(
                onHasInternetConnectivity = { false }
            )
        )

        val result = endpoint.getServiceUpdates()

        assertEquals(ServiceUpdatesResponse.Error.NoConnectivity, result)
    }

    @Test
    fun getServiceUpdatesReturnsServerErrorWhenSerializationExceptionIsThrown() = runTest {
        val exception = SerializationException()
        val exceptionLogger = FakeExceptionLogger()
        val endpoint = createLothianServiceUpdatesEndpoint(
            connectivityRepository = connectivityRepositoryHasConnectivity,
            lothianServiceUpdatesApi = FakeLothianServiceUpdatesApi(
                onGetServiceUpdates = { throw exception }
            ),
            exceptionLogger = exceptionLogger
        )

        val result = endpoint.getServiceUpdates()

        assertIs<ServiceUpdatesResponse.Error.ServerError>(result)
        assertEquals(1, exceptionLogger.loggedThrowables.size)
        assertSame(exception, exceptionLogger.loggedThrowables.last())
    }

    @Test
    fun getServiceUpdatesReturnsIoErrorWhenIOExceptionIsThrown() = runTest {
        val exception = IOException()
        val exceptionLogger = FakeExceptionLogger()
        val endpoint = createLothianServiceUpdatesEndpoint(
            connectivityRepository = connectivityRepositoryHasConnectivity,
            lothianServiceUpdatesApi = FakeLothianServiceUpdatesApi(
                onGetServiceUpdates = { throw exception }
            ),
            exceptionLogger = exceptionLogger
        )

        val result = endpoint.getServiceUpdates()

        assertEquals(ServiceUpdatesResponse.Error.Io(exception), result)
        assertEquals(1, exceptionLogger.loggedThrowables.size)
        assertSame(exception, exceptionLogger.loggedThrowables.last())
    }

    @Test
    fun getServiceUpdatesReturnsServerErrorWhenServerReturnsNonSuccessCode() = runTest {
        val exceptionLogger = FakeExceptionLogger()
        val endpoint = createLothianServiceUpdatesEndpoint(
            connectivityRepository = connectivityRepositoryHasConnectivity,
            lothianServiceUpdatesApi = FakeLothianServiceUpdatesApi(
                onGetServiceUpdates = {
                    Response.error(400, "Unauthorized".toResponseBody())
                }
            ),
            exceptionLogger = exceptionLogger
        )

        val result = endpoint.getServiceUpdates()

        assertIs<ServiceUpdatesResponse.Error.ServerError>(result)
        assertEquals(1, exceptionLogger.loggedThrowables.size)
        assertIs<RuntimeException>(exceptionLogger.loggedThrowables.last())
    }

    @Test
    fun getServiceUpdatesReturnsSuccessWhenServerReturnsSuccessCode() = runTest {
        val responseBody = JsonServiceUpdateEvents(
            events = listOf(
                JsonEvent(
                    id = "id",
                    lastUpdatedTime = Instant.fromEpochMilliseconds(123L),
                    severity = "incident",
                    titles = mapOf("en" to "Title"),
                    descriptions = mapOf("en" to "Description"),
                    routesAffected = listOf(
                        JsonRouteAffected(name = "1")
                    ),
                    url = "https://some/url"
                )
            )
        )
        val endpoint = createLothianServiceUpdatesEndpoint(
            connectivityRepository = connectivityRepositoryHasConnectivity,
            lothianServiceUpdatesApi = FakeLothianServiceUpdatesApi(
                onGetServiceUpdates = {
                    Response.success(responseBody)
                }
            ),
            timeUtils = FakeTimeUtils(
                onGetCurrentTimeMillis = { 123L }
            )
        )
        val serviceUpdates = listOf(
            ServiceUpdate(
                id = "id",
                lastUpdated = Instant.fromEpochMilliseconds(123L),
                serviceUpdateType = ServiceUpdateType.INCIDENT,
                title = "Title",
                summary = "Description",
                affectedServices = setOf("1"),
                url = "https://some/url"
            )
        )

        val result = endpoint.getServiceUpdates()

        assertEquals(
            ServiceUpdatesResponse.Success(
                serviceUpdates = serviceUpdates,
                loadTimeMillis = 123L
            ),
            result
        )
    }

    private val connectivityRepositoryHasConnectivity get() = FakeConnectivityRepository(
        onHasInternetConnectivity = { true }
    )

    private fun createLothianServiceUpdatesEndpoint(
        lothianServiceUpdatesApi: LothianServiceUpdatesApi = FakeLothianServiceUpdatesApi(),
        connectivityRepository: ConnectivityRepository = FakeConnectivityRepository(),
        timeUtils: TimeUtils = FakeTimeUtils(),
        exceptionLogger: ExceptionLogger = FakeExceptionLogger()
    ): LothianServiceUpdatesEndpoint {
        return LothianServiceUpdatesEndpoint(
            lothianServiceUpdatesApi = lothianServiceUpdatesApi,
            connectivityRepository = connectivityRepository,
            timeUtils = timeUtils,
            exceptionLogger = exceptionLogger
        )
    }

    private class FakeLothianServiceUpdatesApi(
        private val onGetServiceUpdates: () -> Response<JsonServiceUpdateEvents> =
            { throw NotImplementedError() }
    ) : LothianServiceUpdatesApi {

        override suspend fun getServiceUpdates() = onGetServiceUpdates()
    }
}