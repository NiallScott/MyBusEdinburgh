/*
 * Copyright (C) 2020 - 2026 Niall 'Rivernile' Scott
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
import uk.org.rivernile.android.bustracker.core.endpoints.api.di.ForInternalApiSchemaVersion
import uk.org.rivernile.android.bustracker.core.endpoints.api.ApiEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.api.DatabaseVersionResponse
import uk.org.rivernile.android.bustracker.core.endpoints.api.di.ForInternalApiAppName
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.SocketFactory

/**
 * This class represents the JSON version of the [ApiEndpoint] that connects over HTTP(S).
 *
 * @param apiServiceFactory An implementation to retrieve instances of [ApiService].
 * @param appName The name of the app/schema to get database version information for.
 * @param schemaVersion The version of the schema to get database information for.
 * @param exceptionLogger Used to report exceptions.
 * @author Niall Scott
 */
@Singleton
internal class JsonApiEndpoint @Inject constructor(
    private val apiServiceFactory: ApiServiceFactory,
    @param:ForInternalApiAppName private val appName: String,
    @param:ForInternalApiSchemaVersion private val schemaVersion: Int,
    private val exceptionLogger: ExceptionLogger
) : ApiEndpoint {

    override suspend fun getDatabaseVersion(
        socketFactory: SocketFactory?
    ): DatabaseVersionResponse {
        return try {
            val response = apiServiceFactory
                .getApiInstance(socketFactory)
                .getDatabaseVersion(
                    appName = appName,
                    schemaVersion = schemaVersion
                )

            if (response.isSuccessful) {
                val databaseVersion = response.body()

                databaseVersion
                    ?.takeIf {
                        it.verify(
                            expectedSchemaName = appName,
                            expectedSchemaVersionCode = schemaVersion
                        )
                    }
                    ?.toDatabaseVersionOrNull()
                    ?.let {
                        DatabaseVersionResponse.Success(it)
                    }
                    ?: run {
                        exceptionLogger.log(
                            Throwable(
                                "Unable to accept database version data. Data = $databaseVersion"
                            )
                        )
                        DatabaseVersionResponse.Error.ServerError
                    }
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
}
