/*
 * Copyright (C) 2024 - 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.http

import okhttp3.Interceptor
import okhttp3.OkHttp
import okhttp3.Response
import uk.org.rivernile.android.bustracker.core.app.AppRepository
import uk.org.rivernile.android.bustracker.core.http.di.ForUserAgentAppName
import javax.inject.Inject

private const val HEADER_USER_AGENT = "User-Agent"

/**
 * This [Interceptor] adds the `User-Agent` header to outbound requests, to share app version
 * information with the server.
 *
 * @param appRepository Used to get application version data.
 * @param userAgentAppName The application name for the user agent.
 * @author Niall Scott
 */
internal class UserAgentInterceptor @Inject constructor(
    private val appRepository: AppRepository,
    @param:ForUserAgentAppName private val userAgentAppName: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.request()
            .newBuilder()
            .header(HEADER_USER_AGENT, userAgent)
            .build()
            .let(chain::proceed)
    }

    /**
     * The user agent [String], which is lazily computed on first access and then held.
     */
    private val userAgent by lazy {
        "$userAgentAppName/${appRepository.appVersion.versionName} okhttp/${OkHttp.VERSION}"
    }
}