/*
 * Copyright (C) 2019 - 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.dagger

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import uk.org.rivernile.android.bustracker.androidcore.BuildConfig
import uk.org.rivernile.android.bustracker.core.di.ForApi
import uk.org.rivernile.android.bustracker.core.di.ForKotlinJsonSerialization
import uk.org.rivernile.android.bustracker.core.endpoints.api.ApiEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.api.ApiKeyGenerator
import uk.org.rivernile.android.bustracker.core.endpoints.api.json.ApiServiceFactory
import uk.org.rivernile.android.bustracker.core.endpoints.api.json.JsonApiEndpoint
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * This Dagger module provides dependencies for the API.
 *
 * @author Niall Scott
 */
@Module
internal class ApiModule {

    /**
     * Provide the API app name, used to identify this app on the API server.
     *
     * @return The API app name.
     */
    @Provides
    @Singleton
    @ForApi
    fun provideApiAppName() = BuildConfig.API_APP_NAME

    /**
     * Provide the [ApiEndpoint] implementation.
     *
     * @param apiServiceFactory An [ApiServiceFactory] instance.
     * @param apiKeyGenerator An [ApiKeyGenerator] instance.
     * @return The [ApiEndpoint] implementation.
     */
    @Provides
    @Singleton
    fun provideApiEndpoint(
            apiServiceFactory: ApiServiceFactory,
            apiKeyGenerator: ApiKeyGenerator): ApiEndpoint =
            JsonApiEndpoint(apiServiceFactory, apiKeyGenerator, BuildConfig.SCHEMA_NAME)

    /**
     * Provide the [ApiKeyGenerator] implementation.
     *
     * @return The [ApiKeyGenerator] implementation.
     */
    @Provides
    fun provideApiKeyGenerator() = ApiKeyGenerator(BuildConfig.API_KEY)

    /**
     * Provide the [Retrofit] instance for the API.
     *
     * @param okHttpClient The [OkHttpClient] to use for requests.
     * @param jsonConverterFactory The [Converter.Factory] for Kotlin JSON.
     * @return The [Retrofit] instance.
     */
    @Provides
    @ForApi
    fun provideRetrofit(
            @ForApi okHttpClient: OkHttpClient,
            @ForKotlinJsonSerialization jsonConverterFactory: Converter.Factory): Retrofit =
            Retrofit.Builder()
                    .baseUrl(BuildConfig.API_BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(jsonConverterFactory)
                    .build()

    /**
     * Provide the [OkHttpClient] instance for the API.
     *
     * @param okHttpClient The base [OkHttpClient] to build upon.
     * @return The [OkHttpClient] instance for the API.
     */
    @Provides
    @ForApi
    fun provideOkhttpClient(okHttpClient: OkHttpClient): OkHttpClient =
            okHttpClient.newBuilder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .followRedirects(false)
                    .build()
}