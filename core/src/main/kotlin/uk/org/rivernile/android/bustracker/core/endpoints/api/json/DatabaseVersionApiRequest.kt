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

import retrofit2.Call
import uk.org.rivernile.android.bustracker.core.endpoints.api.ApiException
import uk.org.rivernile.android.bustracker.core.endpoints.api.ApiRequest
import uk.org.rivernile.android.bustracker.core.endpoints.api.DatabaseVersion
import java.io.IOException

/**
 * This class represents an [ApiRequest] to get the database version information. It is a wrapper
 * around the Retrofit [Call].
 *
 * @property call The Retrofit [Call] instance to use for the request.
 * @author Niall Scott
 */
internal class DatabaseVersionApiRequest(private val call: Call<JsonDatabaseVersion>)
    : ApiRequest<DatabaseVersion> {

    override fun performRequest() = try {
        val response = call.execute()

        if (response.isSuccessful) {
            convertToModelObject(response.body()) ?: throw ApiException("Body is null")
        } else {
            throw ApiException("Failed with error code ${response.code()}")
        }
    } catch (e: IOException) {
        throw ApiException(e)
    }

    override fun cancel() {
        call.cancel()
    }

    /**
     * Convert a [JsonDatabaseVersion] to a [DatabaseVersion]. Returns `null` when the input is
     * `null`.
     */
    private fun convertToModelObject(jsonDatabaseVersion: JsonDatabaseVersion?) =
            jsonDatabaseVersion?.let {
                DatabaseVersion(it.schemaVersion, it.topologyId, it.databaseUrl, it.checksum)
            }
}