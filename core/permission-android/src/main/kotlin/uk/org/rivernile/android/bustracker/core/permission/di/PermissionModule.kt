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

package uk.org.rivernile.android.bustracker.core.permission.di

import android.os.Build
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uk.org.rivernile.android.bustracker.core.permission.BackgroundLocationPermissionChecker
import uk.org.rivernile.android.bustracker.core.permission.LegacyBackgroundLocationPermissionChecker
import uk.org.rivernile.android.bustracker.core.permission.LegacyNotificationPermissionChecker
import uk.org.rivernile.android.bustracker.core.permission.NotificationPermissionChecker
import uk.org.rivernile.android.bustracker.core.permission.V29BackgroundLocationPermissionChecker
import uk.org.rivernile.android.bustracker.core.permission.V33NotificationPermissionChecker
import javax.inject.Provider

/**
 * A Hilt [Module] for permissions.
 *
 * @author Niall Scott
 */
@InstallIn(SingletonComponent::class)
@Module
internal class PermissionModule {

    @Provides
    fun provideNotificationPermissionChecker(
        legacyNotificationPermissionChecker: Provider<LegacyNotificationPermissionChecker>,
        v33NotificationPermissionChecker: Provider<V33NotificationPermissionChecker>
    ): NotificationPermissionChecker {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            v33NotificationPermissionChecker.get()
        } else {
            legacyNotificationPermissionChecker.get()
        }
    }

    @Provides
    fun provideBackgroundLocationPermissionChecker(
        legBackgroundLocationPermissionChecker: Provider<LegacyBackgroundLocationPermissionChecker>,
        v29BackgroundLocationPermissionChecker: Provider<V29BackgroundLocationPermissionChecker>
    ): BackgroundLocationPermissionChecker {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            v29BackgroundLocationPermissionChecker.get()
        } else {
            legBackgroundLocationPermissionChecker.get()
        }
    }
}