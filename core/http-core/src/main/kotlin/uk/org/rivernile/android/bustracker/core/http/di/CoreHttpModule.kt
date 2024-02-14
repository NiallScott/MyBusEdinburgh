/*
 * Copyright (C) 2022 - 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.http.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Converter
import uk.org.rivernile.android.bustracker.core.http.UserAgentInterceptor
import javax.inject.Singleton

/**
 * This module provides dependencies related to core HTTP.
 *
 * @author Niall Scott
 */
@Module
class CoreHttpModule {

    /**
     * Provide a minimally configured [OkHttpClient] instance. When used elsewhere, call
     * [OkHttpClient.newBuilder] to build upon the existing instance. This allows the thread and
     * connection pools to be shared between the instances, thus improving performance.
     *
     * @param userAgentInterceptor An [Interceptor] which adds the app user agent to requests.
     * @param httpLoggingInterceptor The logging interceptor. Will be `null` on non-debug builds.
     * @return An [OkHttpClient] instance.
     */
    @Provides
    @Singleton
    internal fun provideOkhttpClient(
        userAgentInterceptor: UserAgentInterceptor,
        @ForHttpLogging httpLoggingInterceptor: Interceptor?
    ): OkHttpClient {
        val builder = OkHttpClient.Builder().addInterceptor(userAgentInterceptor)
        httpLoggingInterceptor?.let(builder::addNetworkInterceptor)

        return builder.build()
    }

    @Provides
    @Singleton
    internal fun provideKotlinJsonSerialisation(): Json = Json { ignoreUnknownKeys = true }

    /**
     * Provide a [Converter.Factory] instance which uses the app-wide [Json] instance to perform
     * (de-)serialisation of JSON data in Retrofit.
     *
     * @param json The app-wide [Json] instance.
     * @return A [Converter.Factory] which does Kotlin JSON (de-)serialisation.
     */
    @Singleton
    @Provides
    @ForKotlinJsonSerialization
    internal fun provideKotlinJsonConverterFactory(json: Json): Converter.Factory =
        json.asConverterFactory("application/json".toMediaType())
}