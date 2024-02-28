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

import retrofit2.Response
import retrofit2.http.GET

/**
 * This class provides the API specification for interacting with the Lothian Buses service updates
 * API.
 *
 * @author Niall Scott
 */
internal interface LothianServiceUpdatesApi {

    /**
     * Get the current service updates.
     *
     * @return A [Response] which contains the result of attempting to obtain the latest service
     * updates.
     * @throws okio.IOException When there was an IO issue trying to obtain the data.
     */
    @GET("api/public/getServiceUpdates")
    suspend fun getServiceUpdates(): Response<JsonServiceUpdateEvents>
}