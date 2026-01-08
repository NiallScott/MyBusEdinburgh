/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.endpoints.tracker

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import uk.org.rivernile.android.bustracker.core.edinburgh.di.ForBusTrackerApiKey
import javax.inject.Inject

private const val HEADER_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key"

/**
 * This interceptor adds on the necessary headers for requests to the Edinburgh Bus Tracker (Novus)
 * API.
 *
 * @param apiKey The API key to access the service.
 * @author Niall Scott
 */
internal class HeadersInterceptor @Inject constructor(
    @param:ForBusTrackerApiKey private val apiKey: String
) : Interceptor {

    private val noCache = CacheControl
        .Builder()
        .noCache()
        .build()

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request().getModifiedRequest())
    }

    private fun Request.getModifiedRequest(): Request {
        return newBuilder()
            .cacheControl(noCache)
            .addHeader(HEADER_SUBSCRIPTION_KEY, apiKey)
            .build()
    }
}
