/*
 * Copyright (C) 2022 - 2026 Niall 'Rivernile' Scott
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

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.IOException
import retrofit2.Response
import uk.org.rivernile.android.bustracker.core.endpoints.api.DatabaseVersion
import uk.org.rivernile.android.bustracker.core.endpoints.api.DatabaseVersionResponse
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import uk.org.rivernile.android.bustracker.core.log.FakeExceptionLogger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private const val FAKE_APP_NAME = "AppName"
private const val FAKE_SCHEMA_VERSION = 123

/**
 * Tests for [JsonApiEndpoint].
 *
 * @author Niall Scott
 */
class JsonApiEndpointTest {

    @Test
    fun getDatabaseVersionReturnsServerErrorWhenSerializationExceptionThrown() = runTest {
        val endpoint = createJsonApiEndpoint(
            apiServiceFactory = FakeApiServiceFactory(
                onGetApiInstance = {
                    assertNull(it)
                    FakeApiService(
                        onGetDatabaseVersion = { appName, schemaVersion ->
                            assertEquals(FAKE_APP_NAME, appName)
                            assertEquals(FAKE_SCHEMA_VERSION, schemaVersion)
                            throw SerializationException()
                        }
                    )
                }
            )
        )

        val result = endpoint.getDatabaseVersion(null)

        assertEquals(DatabaseVersionResponse.Error.ServerError, result)
    }

    @Test
    fun getDatabaseVersionReturnsIoErrorWhenIoExceptionThrown() = runTest {
        val exception = IOException()
        val endpoint = createJsonApiEndpoint(
            apiServiceFactory = FakeApiServiceFactory(
                onGetApiInstance = {
                    assertNull(it)
                    FakeApiService(
                        onGetDatabaseVersion = { appName, schemaVersion ->
                            assertEquals(FAKE_APP_NAME, appName)
                            assertEquals(FAKE_SCHEMA_VERSION, schemaVersion)
                            throw exception
                        }
                    )
                }
            )
        )

        val result = endpoint.getDatabaseVersion(null)

        assertEquals(DatabaseVersionResponse.Error.Io(exception), result)
    }

    @Test
    fun getDatabaseVersionReturnsServerErrorWhenServerReturnsNonSuccessCode() = runTest {
        val endpoint = createJsonApiEndpoint(
            apiServiceFactory = FakeApiServiceFactory(
                onGetApiInstance = {
                    assertNull(it)
                    FakeApiService(
                        onGetDatabaseVersion = { appName, schemaVersion ->
                            assertEquals(FAKE_APP_NAME, appName)
                            assertEquals(FAKE_SCHEMA_VERSION, schemaVersion)
                            Response.error(401, "Unauthorized".toResponseBody())
                        }
                    )
                }
            )
        )

        val result = endpoint.getDatabaseVersion(null)

        assertEquals(DatabaseVersionResponse.Error.ServerError, result)
    }

    @Test
    fun getDatabaseVersionReturnsServerErrorWhenResponseBodyIsNull() = runTest {
        val endpoint = createJsonApiEndpoint(
            apiServiceFactory = FakeApiServiceFactory(
                onGetApiInstance = {
                    assertNull(it)
                    FakeApiService(
                        onGetDatabaseVersion = { appName, schemaVersion ->
                            assertEquals(FAKE_APP_NAME, appName)
                            assertEquals(FAKE_SCHEMA_VERSION, schemaVersion)
                            Response.success(null)
                        }
                    )
                }
            )
        )

        val result = endpoint.getDatabaseVersion(null)

        assertEquals(DatabaseVersionResponse.Error.ServerError, result)
    }

    @Test
    fun getDatabaseVersionReturnsServerErrorWhenResponseVerificationFails() = runTest {
        val endpoint = createJsonApiEndpoint(
            apiServiceFactory = FakeApiServiceFactory(
                onGetApiInstance = {
                    assertNull(it)
                    FakeApiService(
                        onGetDatabaseVersion = { appName, schemaVersion ->
                            assertEquals(FAKE_APP_NAME, appName)
                            assertEquals(FAKE_SCHEMA_VERSION, schemaVersion)
                            Response.success(
                                JsonDatabaseVersion(
                                    timestampInSeconds = 123456L,
                                    databaseUrl = "https://database.url",
                                    sha256Checksum = "abc123"
                                )
                            )
                        }
                    )
                }
            )
        )

        val result = endpoint.getDatabaseVersion(null)

        assertEquals(DatabaseVersionResponse.Error.ServerError, result)
    }

    @Test
    fun getDatabaseVersionReturnsServerErrorWhenResponseCouldNotBeMapped() = runTest {
        val endpoint = createJsonApiEndpoint(
            apiServiceFactory = FakeApiServiceFactory(
                onGetApiInstance = {
                    assertNull(it)
                    FakeApiService(
                        onGetDatabaseVersion = { appName, schemaVersion ->
                            assertEquals(FAKE_APP_NAME, appName)
                            assertEquals(FAKE_SCHEMA_VERSION, schemaVersion)
                            Response.success(
                                JsonDatabaseVersion(
                                    schemaName = FAKE_APP_NAME,
                                    schemaVersionCode = FAKE_SCHEMA_VERSION
                                )
                            )
                        }
                    )
                }
            )
        )

        val result = endpoint.getDatabaseVersion(null)

        assertEquals(DatabaseVersionResponse.Error.ServerError, result)
    }

    @Test
    fun getDatabaseVersionReturnsSuccessWhenResponseIsVerifiedAndMapped() = runTest {
        val endpoint = createJsonApiEndpoint(
            apiServiceFactory = FakeApiServiceFactory(
                onGetApiInstance = {
                    assertNull(it)
                    FakeApiService(
                        onGetDatabaseVersion = { appName, schemaVersion ->
                            assertEquals(FAKE_APP_NAME, appName)
                            assertEquals(FAKE_SCHEMA_VERSION, schemaVersion)
                            Response.success(
                                JsonDatabaseVersion(
                                    schemaName = FAKE_APP_NAME,
                                    schemaVersionCode = FAKE_SCHEMA_VERSION,
                                    timestampInSeconds = 123456L,
                                    databaseUrl = "https://database.url",
                                    sha256Checksum = "abc123"
                                )
                            )
                        }
                    )
                }
            )
        )

        val result = endpoint.getDatabaseVersion(null)

        assertEquals(
            DatabaseVersionResponse.Success(
                databaseVersion = DatabaseVersion(
                    timestampInSeconds = 123456L,
                    databaseUrl = "https://database.url",
                    sha256Checksum = "abc123"
                )
            ),
            result
        )
    }

    private fun createJsonApiEndpoint(
        apiServiceFactory: ApiServiceFactory = FakeApiServiceFactory(),
        appName: String = FAKE_APP_NAME,
        schemaVersion: Int = FAKE_SCHEMA_VERSION,
        exceptionLogger: ExceptionLogger = FakeExceptionLogger()
    ): JsonApiEndpoint {
        return JsonApiEndpoint(
            apiServiceFactory = apiServiceFactory,
            appName = appName,
            schemaVersion = schemaVersion,
            exceptionLogger = exceptionLogger
        )
    }
}
