/*
 * Copyright (C) 2020 - 2022 Niall 'Rivernile' Scott
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

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create
import uk.org.rivernile.android.bustracker.core.di.ForApi
import javax.inject.Inject
import javax.net.SocketFactory

/**
 * The purpose of this class is to provide instances of [ApiService] depending on the criteria
 * supplied in the parameters to the methods of this class.
 *
 * @param retrofit The base [Retrofit] instance to use for the [ApiService] instance.
 * @param okHttpClient The base [OkHttpClient] instance to use for the [ApiService] instance.
 * @author Niall Scott
 */
internal class ApiServiceFactory @Inject constructor(
        @ForApi private val retrofit: Retrofit,
        @ForApi private val okHttpClient: OkHttpClient) {

    /**
     * An instance which goes over the default network route. This is lazily initialised.
     */
    private val defaultInstance: ApiService by lazy {
        createApiService(retrofit)
    }

    /**
     * Get the [ApiService] instance to use for calls to this service. Optionally, a [SocketFactory]
     * may be supplied to define how network calls are made.
     *
     * @param socketFactory The optional [SocketFactory] instance to use for network calls.
     * @return The [ApiService] instance to use to contact the service.
     */
    fun getApiInstance(socketFactory: SocketFactory? = null): ApiService {
        return socketFactory?.let { sf ->
            val newOkHttpClient = okHttpClient.newBuilder()
                    .socketFactory(sf)
                    .build()
            val newRetrofit = retrofit.newBuilder()
                    .client(newOkHttpClient)
                    .build()
            createApiService(newRetrofit)
        } ?: defaultInstance
    }

    /**
     * Given a [Retrofit] instance, create the [ApiService] instance for this.
     *
     * @param retrofit The [Retrofit] instance to use.
     * @return A new [ApiService] instance, created from the provided [Retrofit] instance.
     */
    private fun createApiService(retrofit: Retrofit) = retrofit.create<ApiService>()
}