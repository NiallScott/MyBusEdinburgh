/*
 * Copyright (C) 2023 - 2024 Niall 'Rivernile' Scott
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

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import java.io.File

private const val HTTP_CACHE_DIR_NAME = "http_cache"
private const val CACHE_DIRECTORY_MAX_SIZE_BYTES = 20971520L // 20MB

/**
 * This [Module] provides dependencies for core HTTP.
 *
 * @author Niall Scott
 */
@Suppress("unused")
@InstallIn(SingletonComponent::class)
@Module(includes = [ CoreHttpModule::class ])
internal interface AndroidCoreHttpModule {

    companion object {

        @Provides
        fun provideOkhttpCache(context: Context): Cache {
            return Cache(
                directory = File(context.cacheDir, HTTP_CACHE_DIR_NAME),
                maxSize = CACHE_DIRECTORY_MAX_SIZE_BYTES
            )
        }
    }
}