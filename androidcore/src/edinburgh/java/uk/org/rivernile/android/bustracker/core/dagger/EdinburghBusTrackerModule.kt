/*
 * Copyright (C) 2019 - 2020 Niall 'Rivernile' Scott
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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uk.org.rivernile.android.bustracker.androidcore.BuildConfig
import uk.org.rivernile.android.bustracker.core.dagger.qualifiers.ForTracker
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.EdinburghTrackerEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.ErrorMapper
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.TrackerEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimesMapper
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityChecker
import uk.org.rivernile.edinburghbustrackerapi.ApiKeyGenerator
import uk.org.rivernile.edinburghbustrackerapi.EdinburghBusTrackerApi
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * This [Module] provides dependencies for the Edinburgh Bus Tracker API.
 *
 * @author Niall Scott
 */
@Module
internal class EdinburghBusTrackerModule {

    /**
     * Provide the [TrackerEndpoint] instance.
     *
     * @param api The [EdinburghBusTrackerApi] instance.
     * @param apiKeyGenerator An implementation to generate API keys for this service.
     * @param liveTimesMapper Used to map responses to our model objects.
     * @param errorMapper Used to map errors.
     * @param connectivityChecker The [ConnectivityChecker] instance.
     */
    @Provides
    @Singleton
    fun provideTrackerEndpoint(
            api: EdinburghBusTrackerApi,
            apiKeyGenerator: ApiKeyGenerator,
            liveTimesMapper: LiveTimesMapper,
            errorMapper: ErrorMapper,
            connectivityChecker: ConnectivityChecker): TrackerEndpoint =
            EdinburghTrackerEndpoint(api, apiKeyGenerator, liveTimesMapper, errorMapper,
                    connectivityChecker)

    /**
     * Provide the [EdinburghBusTrackerApi] instance.
     *
     * @param retrofit The [Retrofit] instance to create the [EdinburghBusTrackerApi] instance from.
     * @return A new [EdinburghBusTrackerApi] instance.
     */
    @Provides
    fun provideEdinburghBusTrackerApi(@ForTracker retrofit: Retrofit): EdinburghBusTrackerApi =
            retrofit.create(EdinburghBusTrackerApi::class.java)

    /**
     * Provide the [Retrofit] instance for the API.
     *
     * @param okHttpClient The [OkHttpClient] to use for requests.
     * @param gsonConverterFactory The [GsonConverterFactory] instance.
     * @return The [Retrofit] instance.
     */
    @Provides
    @ForTracker
    fun provideRetrofit(@ForTracker okHttpClient: OkHttpClient,
                        gsonConverterFactory: GsonConverterFactory): Retrofit =
            Retrofit.Builder()
                    .baseUrl(BuildConfig.TRACKER_BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(gsonConverterFactory)
                    .build()

    /**
     * Provide the [OkHttpClient] instance for the API.
     *
     * @param okHttpClient The base [OkHttpClient] to build upon.
     * @return The [OkHttpClient] instance for the API.
     */
    @Provides
    @ForTracker
    fun provideOkhttpClient(okHttpClient: OkHttpClient): OkHttpClient =
            okHttpClient.newBuilder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .followRedirects(false)
                    .build()

    /**
     * Provide an [ApiKeyGenerator] instance.
     *
     * @return An [ApiKeyGenerator] instance.
     */
    @Provides
    @Singleton
    fun provideApiKeyGenerator() = ApiKeyGenerator(BuildConfig.API_KEY)
}