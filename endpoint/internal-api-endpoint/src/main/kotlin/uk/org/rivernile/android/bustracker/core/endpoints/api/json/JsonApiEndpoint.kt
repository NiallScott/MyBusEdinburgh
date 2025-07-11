/*
 * Copyright (C) 2020 - 2025 Niall 'Rivernile' Scott
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

import kotlinx.serialization.SerializationException
import okio.IOException
import uk.org.rivernile.android.bustracker.core.endpoints.api.di.ForInternalApiSchemaName
import uk.org.rivernile.android.bustracker.core.endpoints.api.ApiEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.api.ApiKeyGenerator
import uk.org.rivernile.android.bustracker.core.endpoints.api.DatabaseVersion
import uk.org.rivernile.android.bustracker.core.endpoints.api.DatabaseVersionResponse
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.SocketFactory

/**
 * This class represents the JSON version of the [ApiEndpoint] that connects over HTTP(S).
 *
 * @param apiServiceFactory An implementation to retrieve instances of [ApiService].
 * @param apiKeyGenerator An implementation to generate API keys.
 * @param schemaType The schema type.
 * @param exceptionLogger Used to report exceptions.
 * @author Niall Scott
 */
@Singleton
internal class JsonApiEndpoint @Inject constructor(
    private val apiServiceFactory: ApiServiceFactory,
    private val apiKeyGenerator: ApiKeyGenerator,
    @param:ForInternalApiSchemaName private val schemaType: String,
    private val exceptionLogger: ExceptionLogger
) : ApiEndpoint {

    override suspend fun getDatabaseVersion(
        socketFactory: SocketFactory?
    ): DatabaseVersionResponse {
        return try {
            val response = apiServiceFactory.getApiInstance(socketFactory)
                .getDatabaseVersion(apiKeyGenerator.generateHashedApiKey(), schemaType)

            if (response.isSuccessful) {
                mapToDatabaseVersion(response.body())?.let {
                    DatabaseVersionResponse.Success(it)
                } ?: DatabaseVersionResponse.Error.ServerError
            } else {
                DatabaseVersionResponse.Error.ServerError
            }
        } catch (e: IOException) {
            exceptionLogger.log(e)
            DatabaseVersionResponse.Error.Io(e)
        } catch (e: SerializationException) {
            exceptionLogger.log(e)
            DatabaseVersionResponse.Error.ServerError
        }
    }

    /**
     * Map a [JsonDatabaseVersion] to a [DatabaseVersion]. Returns `null` when the input is `null`.
     *
     * @param jsonDatabaseVersion The JSON representation of the database version.
     * @return A [DatabaseVersion] of the mapped JSON, or `null` if the root object or expected
     * fields are `null`.
     */
    private fun mapToDatabaseVersion(jsonDatabaseVersion: JsonDatabaseVersion?): DatabaseVersion? {
        return jsonDatabaseVersion?.let {
            val schemaVersion = it.schemaVersion ?: return null
            val topologyId = it.topologyId ?: return null
            val databaseUrl = it.databaseUrl ?: return null
            val checksum = it.checksum ?: return null

            DatabaseVersion(
                schemaVersion,
                topologyId,
                databaseUrl,
                checksum
            )
        }
    }
}