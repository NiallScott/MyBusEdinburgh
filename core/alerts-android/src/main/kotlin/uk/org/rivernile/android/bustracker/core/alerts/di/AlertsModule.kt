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

package uk.org.rivernile.android.bustracker.core.alerts.di

import android.os.Build
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uk.org.rivernile.android.bustracker.core.alerts.AlertNotificationDispatcher
import uk.org.rivernile.android.bustracker.core.alerts.AndroidAlertNotificationDispatcher
import uk.org.rivernile.android.bustracker.core.alerts.LegacyNotificationPreferences
import uk.org.rivernile.android.bustracker.core.alerts.NotificationPreferences
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.ArrivalAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.LegacyAndroidArrivalAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.V31AndroidArrivalAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.alerts.proximity.GeofencingManager
import uk.org.rivernile.android.bustracker.core.alerts.proximity.LegacyAndroidProximityAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.alerts.proximity.ProximityAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.alerts.proximity.V31AndroidProximityAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.alerts.proximity.android.AndroidGeofencingManager
import javax.inject.Provider

/**
 * This Dagger module provides dependencies for dealing with alerts.
 *
 * @author Niall Scott
 */
@InstallIn(SingletonComponent::class)
@Module
internal interface AlertsModule {

    @Suppress("unused")
    @Binds
    fun bindAlertNotificationDispatcher(
        androidAlertNotificationDispatcher: AndroidAlertNotificationDispatcher
    ): AlertNotificationDispatcher

    @Suppress("unused")
    @Binds
    fun bindGeofencingManager(
        androidGeofencingManager: AndroidGeofencingManager): GeofencingManager

    companion object {

        @Provides
        fun provideNotificationPreferences(
            legacyNotificationPreferences: Provider<LegacyNotificationPreferences>
        ) : NotificationPreferences? {
            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                legacyNotificationPreferences.get()
            } else {
                null
            }
        }

        @Provides
        fun provideProximityAlertTaskLauncher(
            legacyProximityAlertTaskLauncher: Provider<LegacyAndroidProximityAlertTaskLauncher>,
            v31ProximityAlertTaskLauncher: Provider<V31AndroidProximityAlertTaskLauncher>
        ): ProximityAlertTaskLauncher {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                v31ProximityAlertTaskLauncher.get()
            } else {
                legacyProximityAlertTaskLauncher.get()
            }
        }

        @Provides
        fun provideArrivalAlertTaskLauncher(
            legacyAndroidArrivalAlertTaskLauncher: Provider<LegacyAndroidArrivalAlertTaskLauncher>,
            v31AndroidArrivalAlertTaskLauncher: Provider<V31AndroidArrivalAlertTaskLauncher>
        ): ArrivalAlertTaskLauncher {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                v31AndroidArrivalAlertTaskLauncher.get()
            } else {
                legacyAndroidArrivalAlertTaskLauncher.get()
            }
        }
    }
}