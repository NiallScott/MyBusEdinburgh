/*
 * Copyright (C) 2019 Niall 'Rivernile' Scott
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
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uk.org.rivernile.android.bustracker.androidcore.BuildConfig
import uk.org.rivernile.android.bustracker.core.dagger.qualifiers.ForApi
import uk.org.rivernile.android.bustracker.core.endpoints.api.ApiEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.api.ApiKeyGenerator
import uk.org.rivernile.android.bustracker.core.endpoints.api.ApiService
import uk.org.rivernile.android.bustracker.core.endpoints.api.JsonApiEndpoint
import javax.inject.Singleton

/**
 * This Dagger module provides dependencies for the API.
 *
 * @author Niall Scott
 */
@Module
internal class ApiModule {

    /**
     * Provide the [ApiEndpoint] implementation.
     *
     * @param apiService An [ApiService] instance.
     * @param apiKeyGenerator An [ApiKeyGenerator] instance.
     * @return The [ApiEndpoint] implementation.
     */
    @Provides
    @Singleton
    fun provideApiEndpoint(apiService: ApiService,
                           apiKeyGenerator: ApiKeyGenerator): ApiEndpoint =
            JsonApiEndpoint(apiService, apiKeyGenerator, BuildConfig.SCHEMA_NAME)

    /**
     * Provide the [ApiKeyGenerator] implementation.
     *
     * @return The [ApiKeyGenerator] implementation.
     */
    @Provides
    fun provideApiKeyGenerator() = ApiKeyGenerator(BuildConfig.API_KEY)

    /**
     * Provide the [ApiService] implementation.
     *
     * @param retrofit The [Retrofit] instance to create the [ApiService] from.
     * @return The [ApiService] implementation.
     */
    @Provides
    fun provideApiService(@ForApi retrofit: Retrofit) = retrofit.create(ApiService::class.java)

    /**
     * Provide the [Retrofit] instance for the API.
     *
     * @param okHttpClient The [OkHttpClient] to use for requests.
     * @param gsonConverterFactory The [GsonConverterFactory] instance.
     * @return The [Retrofit] instance.
     */
    @Provides
    @ForApi
    fun provideRetrofit(@ForApi okHttpClient: OkHttpClient,
                        gsonConverterFactory: GsonConverterFactory) =
            Retrofit.Builder()
                    .baseUrl(BuildConfig.API_BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(gsonConverterFactory)
                    .build()

    /**
     * Provide the [OkHttpClient] instance for the API.
     *
     * @param okHttpClientBuilder The base builder to build upon.
     * @param httpLoggingInterceptor The logging interceptor. Will be `null` on non-debug builds.
     * @return The [OkHttpClient] instance for the API.
     */
    @Provides
    @ForApi
    fun provideOkhttpClient(okHttpClientBuilder: OkHttpClient.Builder,
                            httpLoggingInterceptor: HttpLoggingInterceptor?): OkHttpClient {
        httpLoggingInterceptor?.let { okHttpClientBuilder.addNetworkInterceptor(it) }

        return okHttpClientBuilder.build()
    }
}