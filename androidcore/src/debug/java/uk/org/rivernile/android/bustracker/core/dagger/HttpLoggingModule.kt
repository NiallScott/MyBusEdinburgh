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

package uk.org.rivernile.android.bustracker.core.dagger

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import uk.org.rivernile.android.bustracker.core.di.ForHttpLogging
import javax.inject.Singleton

/**
 * This is a Dagger [Module] which supplies HTTP logging dependencies.
 *
 * This is the debug version of this class. The release version does not do HTTP logging.
 *
 * @author Niall Scott
 */
@InstallIn(SingletonComponent::class)
@Module
internal class HttpLoggingModule {

    companion object {

        private const val HTTP_LOG_TAG = "MyBusHttp"
    }

    @Provides
    @Singleton
    @ForHttpLogging
    fun provideLoggingInterceptor(): Interceptor =
            HttpLoggingInterceptor { message ->
                Log.v(HTTP_LOG_TAG, message)
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
}