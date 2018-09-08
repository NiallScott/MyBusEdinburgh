/*
 * Copyright (C) 2018 Niall 'Rivernile' Scott
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
 */

package uk.org.rivernile.android.bustracker.dagger

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import uk.org.rivernile.android.bustracker.dagger.about.AboutDataModule
import uk.org.rivernile.android.bustracker.dagger.busstopmap.BusStopMapDataModule
import uk.org.rivernile.android.bustracker.data.platform.AndroidPlatformDataSource
import uk.org.rivernile.android.bustracker.data.platform.PlatformDataSource
import uk.org.rivernile.android.bustracker.preferences.PreferenceManager
import uk.org.rivernile.android.bustracker.preferences.PreferenceManagerImpl
import javax.inject.Singleton

/**
 * The main application [Module].
 *
 * @author Niall Scott
 */
@Module(includes = [
    ViewModelModule::class,
    AboutDataModule::class,
    BusStopMapDataModule::class
])
class ApplicationModule {

    /**
     * Provide the [Application] [Context] to Dagger.
     *
     * @param application The [Application] instance.
     * @return The [Application] [Context].
     */
    @Provides
    fun provideApplicationContext(application: Application): Context = application

    /**
     * Provide a [PlatformDataSource] to Dagger.
     *
     * @param context A [Context] instance.
     * @return A [PlatformDataSource].
     */
    @Provides
    @Singleton
    fun providePlatformDataSource(context: Context): PlatformDataSource {
        return AndroidPlatformDataSource(context)
    }

    /**
     * Provide a [PreferenceManager] to Dagger.
     *
     * @param context A [Context] instance.
     * @return A [PreferenceManager].
     */
    @Provides
    @Singleton
    fun providePreferenceManager(context: Context): PreferenceManager {
        return PreferenceManagerImpl(context)
    }
}