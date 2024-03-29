/*
 * Copyright (C) 2018 - 2023 Niall 'Rivernile' Scott
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

import android.os.Build
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uk.org.rivernile.android.bustracker.core.deeplinking.DeeplinkIntentFactory
import uk.org.rivernile.android.bustracker.core.features.StopMapFeatureAvailabilityProvider
import uk.org.rivernile.android.bustracker.deeplinking.AppDeeplinkIntentFactory
import uk.org.rivernile.android.bustracker.features.AppStopMapFeatureAvailabilityProvider
import uk.org.rivernile.android.bustracker.startup.AppThemeObserver
import uk.org.rivernile.android.bustracker.startup.LegacyAppThemeObserver
import uk.org.rivernile.android.bustracker.startup.V31AppThemeObserver
import javax.inject.Provider

/**
 * The main application [Module].
 *
 * @author Niall Scott
 */
@InstallIn(SingletonComponent::class)
@Module
class ApplicationModule {

    @Provides
    fun provideAppThemeObserver(
        legacyAppThemeObserver: Provider<LegacyAppThemeObserver>,
        v31AppThemeObserver: Provider<V31AppThemeObserver>): AppThemeObserver {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            v31AppThemeObserver.get()
        } else {
            legacyAppThemeObserver.get()
        }
    }

    @Suppress("unused")
    @InstallIn(SingletonComponent::class)
    @Module
    interface Bindings {

        @Suppress("unused")
        @Binds
        fun bindDeeplinkIntentFactory(appDeeplinkIntentFactory: AppDeeplinkIntentFactory)
                : DeeplinkIntentFactory

        @Suppress("unused")
        @Binds
        fun bindStopMapFeatureAvailabilityProvider(
            appStopMapFeatureAvailabilityProvider: AppStopMapFeatureAvailabilityProvider)
                : StopMapFeatureAvailabilityProvider
    }
}