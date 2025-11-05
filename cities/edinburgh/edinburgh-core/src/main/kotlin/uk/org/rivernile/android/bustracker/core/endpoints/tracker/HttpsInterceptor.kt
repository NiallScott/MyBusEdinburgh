/*
 * Copyright (C) 2025 Niall 'Rivernile' Scott
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

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.net.ssl.SSLException

/**
 * Because bus tracker seems to be flipping between supporting HTTP and HTTPS, this [Interceptor]
 * attempts the request to the server firstly on HTTPS, and if it receives a [SSLException] it then
 * attempts on plain HTTP.
 *
 * This is not intended to be a long-term fix. It's intended to be a hotfix until bus tracker
 * supports HTTPS on a stable footing. Once it's demonstrated that bus tracker stops flipping
 * between SSL and non-SSL support, this interceptor will be removed.
 *
 * @author Niall Scott
 */
internal class HttpsInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            chain.proceed(chain.request())
        } catch (_: SSLException) {
            chain.proceed(chain.request().downgradeToHttp())
        }
    }

    private fun Request.downgradeToHttp(): Request {
        return newBuilder()
            .url(url.downgradeToHttp())
            .build()
    }

    private fun HttpUrl.downgradeToHttp(): HttpUrl {
        return newBuilder()
            .scheme("http")
            .build()
    }
}
