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
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * This interface defines a Retrofit interface for accessing the API.
 *
 * @author Niall Scott
 */
interface ApiService {

    /**
     * Get the database version.
     *
     * @param apiKey The API key.
     * @param schemaType The schema type.
     * @return A Retrofit [Call] object.
     */
    @GET("DatabaseVersion")
    fun getDatabaseVersion(
            @Query("key") apiKey: String,
            @Query("schemaType") schemaType: String): Call<JsonDatabaseVersion>
}