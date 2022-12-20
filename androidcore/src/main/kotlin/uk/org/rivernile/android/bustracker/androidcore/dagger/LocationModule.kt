/*
 * Copyright (C) 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.androidcore.dagger

import android.content.Context
import android.os.Build
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uk.org.rivernile.android.bustracker.core.location.AndroidLocationSupport
import uk.org.rivernile.android.bustracker.core.location.DistanceCalculator
import uk.org.rivernile.android.bustracker.core.location.HasLocationFeatureDetector
import uk.org.rivernile.android.bustracker.core.location.IsLocationEnabledDetector
import uk.org.rivernile.android.bustracker.core.location.IsLocationEnabledFetcher
import uk.org.rivernile.android.bustracker.core.location.LegacyIsLocationEnabledFetcher
import uk.org.rivernile.android.bustracker.core.location.LocationSource
import uk.org.rivernile.android.bustracker.core.location.V28IsLocationEnabledFetcher
import uk.org.rivernile.android.bustracker.core.location.googleplay.GooglePlayLocationSource
import uk.org.rivernile.android.bustracker.core.location.platform.PlatformLocationSource
import javax.inject.Provider

/**
 * This Dagger [Module] provides dependencies for all things location.
 *
 * @author Niall Scott
 */
@InstallIn(SingletonComponent::class)
@Module
internal class LocationModule {

    @Provides
    fun provideIsLocationEnabledFetcher(
            legacyIsLocationEnabledFetcher: Provider<LegacyIsLocationEnabledFetcher>,
            v28IsLocationEnabledFetcher: Provider<V28IsLocationEnabledFetcher>)
            : IsLocationEnabledFetcher {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            v28IsLocationEnabledFetcher.get()
        } else {
            legacyIsLocationEnabledFetcher.get()
        }
    }

    @Provides
    fun provideLocationSource(
            context: Context,
            platformLocationSourceProvider: Provider<PlatformLocationSource>,
            googlePlayLocationSourceProvider: Provider<GooglePlayLocationSource>): LocationSource {
        return if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) ==
                ConnectionResult.SUCCESS) {
            googlePlayLocationSourceProvider.get()
        } else {
            platformLocationSourceProvider.get()
        }
    }

    @InstallIn(SingletonComponent::class)
    @Module
    interface Bindings {

        @Suppress("unused")
        @Binds
        fun bindHasLocationFeatureDetector(
                androidLocationSupport: AndroidLocationSupport): HasLocationFeatureDetector

        @Suppress("unused")
        @Binds
        fun bindIsLocationEnabledDetector(
                androidLocationSupport: AndroidLocationSupport): IsLocationEnabledDetector

        @Suppress("unused")
        @Binds
        fun bindDistanceCalculator(
                androidLocationSupport: AndroidLocationSupport): DistanceCalculator
    }
}