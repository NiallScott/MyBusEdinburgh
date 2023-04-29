/*
 * Copyright (C) 2022 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.endpoints.api.json

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
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response
import uk.org.rivernile.android.bustracker.core.endpoints.api.ApiKeyGenerator
import uk.org.rivernile.android.bustracker.core.endpoints.api.DatabaseVersion
import uk.org.rivernile.android.bustracker.core.endpoints.api.DatabaseVersionResponse
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule

/**
 * Tests for [JsonApiEndpoint].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class JsonApiEndpointTest {

    companion object {

        private const val MOCK_SCHEMA_TYPE = "schemaType"

        private const val HASHED_API_KEY = "abc123"

        private const val MOCK_SCHEMA_VERSION = "schemaVersion"
        private const val MOCK_TOPOLOGY_ID = "topologyId"
        private const val MOCK_DATABASE_URL = "databaseUrl"
        private const val MOCK_CHECKSUM = "checksum"
    }

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var apiServiceFactory: ApiServiceFactory
    @Mock
    private lateinit var apiKeyGenerator: ApiKeyGenerator
    @Mock
    private lateinit var exceptionLogger: ExceptionLogger

    @Mock
    private lateinit var apiService: ApiService

    private lateinit var endpoint: JsonApiEndpoint

    @Before
    fun setUp() {
        endpoint = JsonApiEndpoint(
                apiServiceFactory,
                apiKeyGenerator,
                MOCK_SCHEMA_TYPE,
                exceptionLogger)

        whenever(apiServiceFactory.getApiInstance(anyOrNull()))
                .thenReturn(apiService)
    }

    @Test
    fun getDatabaseVersionReturnsIoErrorWhenApiServiceThrowsIoException() = runTest {
        givenApiKeyGeneratedReturnsHashedApiKey()
        val ioException = IOException()
        whenever(apiService.getDatabaseVersion(HASHED_API_KEY, MOCK_SCHEMA_TYPE))
                .thenAnswer { throw ioException }
        val expected = DatabaseVersionResponse.Error.Io(ioException)

        val result = endpoint.getDatabaseVersion(null)

        assertEquals(expected, result)
        verify(exceptionLogger)
            .log(ioException)
    }

    @Test
    fun getDatabaseVersionReturnsServerErrorWhenApiServiceHasErrorCode() = runTest {
        givenApiKeyGeneratedReturnsHashedApiKey()
        whenever(apiService.getDatabaseVersion(HASHED_API_KEY, MOCK_SCHEMA_TYPE))
                .thenReturn(Response.error(500, "Server error".toResponseBody()))
        val expected = DatabaseVersionResponse.Error.ServerError

        val result = endpoint.getDatabaseVersion(null)

        assertEquals(expected, result)
        verify(exceptionLogger, never())
            .log(any())
    }

    @Test
    fun getDatabaseVersionReturnsServerErrorWhenApiServiceIsSuccessWithEmptyBody() = runTest {
        givenApiKeyGeneratedReturnsHashedApiKey()
        whenever(apiService.getDatabaseVersion(HASHED_API_KEY, MOCK_SCHEMA_TYPE))
                .thenReturn(Response.success<JsonDatabaseVersion>(200, null))
        val expected = DatabaseVersionResponse.Error.ServerError

        val result = endpoint.getDatabaseVersion(null)

        assertEquals(expected, result)
        verify(exceptionLogger, never())
            .log(any())
    }

    @Test
    fun getDatabaseVersionReturnsServerErrorWhenApiServiceReturnsMissingSchemaVersion() = runTest {
        givenApiKeyGeneratedReturnsHashedApiKey()
        val jsonDatabaseVersion = JsonDatabaseVersion(
                null,
                MOCK_TOPOLOGY_ID,
                MOCK_DATABASE_URL,
                MOCK_CHECKSUM)
        whenever(apiService.getDatabaseVersion(HASHED_API_KEY, MOCK_SCHEMA_TYPE))
                .thenReturn(Response.success(200, jsonDatabaseVersion))
        val expected = DatabaseVersionResponse.Error.ServerError

        val result = endpoint.getDatabaseVersion(null)

        assertEquals(expected, result)
        verify(exceptionLogger, never())
            .log(any())
    }

    @Test
    fun getDatabaseVersionReturnsServerErrorWhenApiServiceReturnsMissingTopologyId() = runTest {
        givenApiKeyGeneratedReturnsHashedApiKey()
        val jsonDatabaseVersion = JsonDatabaseVersion(
                MOCK_SCHEMA_VERSION,
                null,
                MOCK_DATABASE_URL,
                MOCK_CHECKSUM)
        whenever(apiService.getDatabaseVersion(HASHED_API_KEY, MOCK_SCHEMA_TYPE))
                .thenReturn(Response.success(200, jsonDatabaseVersion))
        val expected = DatabaseVersionResponse.Error.ServerError

        val result = endpoint.getDatabaseVersion(null)

        assertEquals(expected, result)
        verify(exceptionLogger, never())
            .log(any())
    }

    @Test
    fun getDatabaseVersionReturnsServerErrorWhenApiServiceReturnsMissingDatabaseUrl() = runTest {
        givenApiKeyGeneratedReturnsHashedApiKey()
        val jsonDatabaseVersion = JsonDatabaseVersion(
                MOCK_SCHEMA_VERSION,
                MOCK_TOPOLOGY_ID,
                null,
                MOCK_CHECKSUM)
        whenever(apiService.getDatabaseVersion(HASHED_API_KEY, MOCK_SCHEMA_TYPE))
                .thenReturn(Response.success(200, jsonDatabaseVersion))
        val expected = DatabaseVersionResponse.Error.ServerError

        val result = endpoint.getDatabaseVersion(null)

        assertEquals(expected, result)
        verify(exceptionLogger, never())
            .log(any())
    }

    @Test
    fun getDatabaseVersionReturnsServerErrorWhenApiServiceReturnsMissingChecksum() = runTest {
        givenApiKeyGeneratedReturnsHashedApiKey()
        val jsonDatabaseVersion = JsonDatabaseVersion(
                MOCK_SCHEMA_VERSION,
                MOCK_TOPOLOGY_ID,
                MOCK_DATABASE_URL,
                null)
        whenever(apiService.getDatabaseVersion(HASHED_API_KEY, MOCK_SCHEMA_TYPE))
                .thenReturn(Response.success(200, jsonDatabaseVersion))
        val expected = DatabaseVersionResponse.Error.ServerError

        val result = endpoint.getDatabaseVersion(null)

        assertEquals(expected, result)
        verify(exceptionLogger, never())
            .log(any())
    }

    @Test
    fun getDatabaseVersionReturnsSuccessWhenApiServiceReturnsValidObject() = runTest {
        givenApiKeyGeneratedReturnsHashedApiKey()
        val jsonDatabaseVersion = JsonDatabaseVersion(
                MOCK_SCHEMA_VERSION,
                MOCK_TOPOLOGY_ID,
                MOCK_DATABASE_URL,
                MOCK_CHECKSUM)
        whenever(apiService.getDatabaseVersion(HASHED_API_KEY, MOCK_SCHEMA_TYPE))
                .thenReturn(Response.success(200, jsonDatabaseVersion))
        val expected = DatabaseVersionResponse.Success(
                DatabaseVersion(
                        MOCK_SCHEMA_VERSION,
                        MOCK_TOPOLOGY_ID,
                        MOCK_DATABASE_URL,
                        MOCK_CHECKSUM))

        val result = endpoint.getDatabaseVersion(null)

        assertEquals(expected, result)
        verify(exceptionLogger, never())
            .log(any())
    }

    private fun givenApiKeyGeneratedReturnsHashedApiKey() {
        whenever(apiKeyGenerator.generateHashedApiKey())
                .thenReturn(HASHED_API_KEY)
    }
}