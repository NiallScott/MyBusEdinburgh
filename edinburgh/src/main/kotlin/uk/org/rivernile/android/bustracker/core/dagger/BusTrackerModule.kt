/*
 * Copyright (C) 2022 - 2023 Niall 'Rivernile' Scott
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

import dagger.Binds
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.create
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.di.ForTracker
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.EdinburghTrackerEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.TrackerEndpoint
import uk.org.rivernile.android.bustracker.core.http.di.ForKotlinJsonSerialization
import uk.org.rivernile.edinburghbustrackerapi.ApiKeyGenerator
import uk.org.rivernile.edinburghbustrackerapi.EdinburghBusTrackerApi
import java.util.concurrent.TimeUnit

/**
 * This [Module] provides dependencies for the Edinburgh Bus Tracker API.
 *
 * @author Niall Scott
 */
@Module(includes = [ BusTrackerModule.Bindings::class ])
internal class BusTrackerModule {

    @Provides
    fun provideApiKeyGenerator(@ForBusTrackerApiKey apiKey: String): ApiKeyGenerator =
        ApiKeyGenerator(apiKey)

    @Provides
    fun provideEdinburghBusTrackerApi(@ForTracker retrofit: Retrofit): EdinburghBusTrackerApi =
        retrofit.create()

    @Provides
    @ForTracker
    fun provideRetrofit(
        @ForTracker baseUrl: String,
        @ForTracker okHttpClient: OkHttpClient,
        @ForKotlinJsonSerialization jsonConverterFactory: Converter.Factory): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(jsonConverterFactory)
            .build()

    @Provides
    @ForTracker
    fun provideOkhttpClient(okHttpClient: OkHttpClient): OkHttpClient =
        okHttpClient.newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .followRedirects(false)
            .build()

    @Module
    interface Bindings {

        @Suppress("unused")
        @Binds
        fun bindTrackerEndpoint(edinburghTrackerEndpoint: EdinburghTrackerEndpoint): TrackerEndpoint
    }
}