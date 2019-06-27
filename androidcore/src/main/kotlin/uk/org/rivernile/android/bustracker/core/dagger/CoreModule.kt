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

import android.content.SharedPreferences
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import uk.org.rivernile.android.bustracker.core.concurrency.NewThreadExecutor
import uk.org.rivernile.android.bustracker.core.dagger.qualifiers.ForStartUpTask
import uk.org.rivernile.android.bustracker.core.preferences.AndroidPreferenceManager
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceManager
import java.util.concurrent.Executor
import javax.inject.Singleton

/**
 * This Dagger module is the root module in the core project.
 *
 * @author Niall Scott
 */
@Module(includes = [
    AndroidModule::class,
    DatabaseModule::class,
    HttpModule::class,
    ApiModule::class,
    FlavourModule::class
])
class CoreModule {

    /**
     * Provide the app-wide [Gson] instance.
     *
     * @return The app-wide [Gson] instance.
     */
    @Provides
    @Singleton
    fun provideGson() = Gson()

    /**
     * Provide the [Executor] to run the start-up tasks on.
     *
     * @return The [Executor] to run the start-up tasks on,
     */
    @Provides
    @ForStartUpTask
    fun provideStartUpTaskExecutor(): Executor = NewThreadExecutor()

    /**
     * Provide the [PreferenceManager].
     *
     * @param preferences The Android [SharedPreferences] for this [PreferenceManager].
     * @return The [PreferenceManager].
     */
    @Provides
    @Singleton
    fun providePreferenceManager(preferences: SharedPreferences): PreferenceManager =
            AndroidPreferenceManager(preferences)
}