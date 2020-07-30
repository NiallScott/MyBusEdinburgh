/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uk.org.rivernile.android.bustracker.androidcore.BuildConfig
import uk.org.rivernile.android.bustracker.core.di.ForApi
import uk.org.rivernile.android.bustracker.core.di.ForTwitter
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.TwitterEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.apiendpoint.ApiTwitterEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.apiendpoint.TwitterService

/**
 * This Dagger [Module] provides dependencies for the Twitter API.
 *
 * @author Niall Scott
 */
@Module(includes = [
    TwitterModule.Bindings::class
])
class TwitterModule {

    /**
     * Provide the Retrofit service for the Twitter API.
     *
     * @param retrofit The [Retrofit] instance.
     * @return The [TwitterService] instance.
     */
    @Provides
    fun provideTwitterService(@ForTwitter retrofit: Retrofit): TwitterService =
            retrofit.create(TwitterService::class.java)

    /**
     * Provide the [Retrofit] instance for the Twitter API.
     *
     * @param okHttpClient The [OkHttpClient] to use for requests. This intentionally re-uses the
     * API [OkHttpClient] as the current implementation uses the same endpoint.
     * @param gsonConverterFactory The [GsonConverterFactory] instance.
     * @return The [Retrofit] instance.
     */
    @Provides
    @ForTwitter
    fun provideRetrofit(
            @ForApi okHttpClient: OkHttpClient,
            gsonConverterFactory: GsonConverterFactory): Retrofit =
            Retrofit.Builder()
                    .baseUrl(BuildConfig.API_BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(gsonConverterFactory)
                    .build()

    @Module
    interface Bindings {

        @Suppress("unused")
        @Binds
        fun bindTwitterEndpoint(apiTwitterEndpoint: ApiTwitterEndpoint): TwitterEndpoint
    }
}