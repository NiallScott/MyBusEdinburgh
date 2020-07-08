/*
 * Copyright (C) 2019 - 2020 Niall 'Rivernile' Scott
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

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import uk.org.rivernile.android.bustracker.core.concurrency.NewThreadExecutor
import uk.org.rivernile.android.bustracker.core.dagger.qualifiers.ForStartUpTask
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityChecker
import uk.org.rivernile.android.bustracker.core.networking.LegacyConnectivityChecker
import uk.org.rivernile.android.bustracker.core.networking.V29ConnectivityChecker
import uk.org.rivernile.android.bustracker.core.notifications.AppNotificationChannels
import uk.org.rivernile.android.bustracker.core.notifications.LegacyAppNotificationChannels
import uk.org.rivernile.android.bustracker.core.notifications.V26AppNotificationChannels
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
    AlertsModule::class,
    AndroidModule::class,
    ApiModule::class,
    DatabaseModule::class,
    FlavourModule::class,
    HttpModule::class,
    ServiceModule::class
])
class CoreModule {

    /**
     * Provide the app-wide [Gson] instance.
     *
     * @return The app-wide [Gson] instance.
     */
    @Provides
    @Singleton
    internal fun provideGson() = Gson()

    /**
     * Provide the [Executor] to run the start-up tasks on.
     *
     * @return The [Executor] to run the start-up tasks on,
     */
    @Provides
    @ForStartUpTask
    internal fun provideStartUpTaskExecutor(): Executor = NewThreadExecutor()

    /**
     * Provide the [AndroidPreferenceManager]. This is a special case for exposing the real
     * implementation within this module.
     *
     * @param sharedPreferences The Android [SharedPreferences] for this [AndroidPreferenceManager].
     * @return The [AndroidPreferenceManager].
     */
    @Provides
    @Singleton
    internal fun provideAndroidPreferenceManager(sharedPreferences: SharedPreferences) =
            AndroidPreferenceManager(sharedPreferences)

    /**
     * Provide the [PreferenceManager].
     *
     * @param androidPreferenceManager The [AndroidPreferenceManager].
     * @return The [PreferenceManager].
     */
    @Provides
    @Singleton
    internal fun providePreferenceManager(androidPreferenceManager: AndroidPreferenceManager)
            : PreferenceManager = androidPreferenceManager

    /**
     * Provide the [AppNotificationChannels] instance for dealing with notification channels.
     *
     * @param context The application [Context].
     * @param notificationManager The system [NotificationManagerCompat].
     * @return An [AppNotificationChannels] instance.
     */
    @Provides
    internal fun provideAppNotificationChannels(context: Context,
            notificationManager: NotificationManagerCompat): AppNotificationChannels {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            V26AppNotificationChannels(context, notificationManager)
        } else {
            LegacyAppNotificationChannels()
        }
    }

    /**
     * Provide a [ConnectivityChecker] instance.
     *
     * @param connectivityManager The system [ConnectivityManager].
     * @return A [ConnectivityChecker] instance.
     */
    @Provides
    internal fun provideConnectivityChecker(
            connectivityManager: ConnectivityManager): ConnectivityChecker {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            V29ConnectivityChecker(connectivityManager)
        } else {
            LegacyConnectivityChecker(connectivityManager)
        }
    }
}