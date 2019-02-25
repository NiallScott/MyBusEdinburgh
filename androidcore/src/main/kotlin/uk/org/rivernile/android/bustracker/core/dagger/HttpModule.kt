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

import android.util.Log
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.gson.GsonConverterFactory
import uk.org.rivernile.android.bustracker.androidcore.BuildConfig
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * This Dagger module defines standard HTTP dependencies across the project.
 *
 * @author Niall Scott
 */
@Module
internal class HttpModule {

    companion object {

        /**
         * The log tag to use for [HttpLoggingInterceptor].
         */
        private const val HTTP_LOG_TAG = "MyBusHttp"
    }

    /**
     * If the module is built using the debug target, then this method provides a configured
     * [HttpLoggingInterceptor] to output HTTP logs. Otherwise, `null` is returned.
     *
     * @return A configured [HttpLoggingInterceptor] when in debug mode, otherwise `null`.
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor() = if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor { message -> Log.v(HTTP_LOG_TAG, message) }
                .level = HttpLoggingInterceptor.Level.BODY
    } else {
        null
    }

    /**
     * Provide a minimally configured [OkHttpClient.Builder] instance to be used to construct
     * [OkHttpClient] instances elsewhere.
     *
     * @return A [OkHttpClient.Builder] to be used to construct [OkHttpClient] instances.
     */
    @Provides
    fun provideOkhttpClientBuilder() = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .followRedirects(false)

    /**
     * Provide a [GsonConverterFactory] instance which uses the app-wide [Gson] instance.
     *
     * @param gson The app-wide Gson instance.
     * @return A [GsonConverterFactory] instance.
     */
    @Provides
    @Singleton
    fun provideGsonConverterFactory(gson: Gson) = GsonConverterFactory.create(gson)
}