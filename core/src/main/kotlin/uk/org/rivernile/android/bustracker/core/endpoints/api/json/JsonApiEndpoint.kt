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

package uk.org.rivernile.android.bustracker.core.endpoints.api.json

import uk.org.rivernile.android.bustracker.core.endpoints.api.ApiEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.api.ApiKeyGenerator
import uk.org.rivernile.android.bustracker.core.endpoints.api.ApiRequest
import uk.org.rivernile.android.bustracker.core.endpoints.api.DatabaseVersion
import javax.net.SocketFactory

/**
 * This class represents the JSON version of the [ApiEndpoint] that connects over HTTP(S).
 *
 * @property apiServiceFactory An implementation to retrieve instances of [ApiService].
 * @property apiKeyGenerator An implementation to generate API keys.
 * @property schemaType The schema type.
 * @author Niall Scott
 */
class JsonApiEndpoint(
        private val apiServiceFactory: ApiServiceFactory,
        private val apiKeyGenerator: ApiKeyGenerator,
        private val schemaType: String) : ApiEndpoint {

    override fun createDatabaseVersionRequest(socketFactory: SocketFactory?)
            : ApiRequest<DatabaseVersion> {
        val hashedApiKey = apiKeyGenerator.generateHashedApiKey()
        val call = apiServiceFactory.getApiInstance(socketFactory)
                .getDatabaseVersion(hashedApiKey, schemaType)

        return DatabaseVersionApiRequest(call)
    }
}