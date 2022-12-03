/*
 * Copyright (C) 2019 - 2022 Niall 'Rivernile' Scott
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
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import uk.org.rivernile.android.bustracker.core.app.AndroidAppRepository
import uk.org.rivernile.android.bustracker.core.app.AppRepository
import uk.org.rivernile.android.bustracker.core.backup.AndroidBackupInvoker
import uk.org.rivernile.android.bustracker.core.backup.BackupInvoker
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.di.ForApplicationCoroutineScope
import uk.org.rivernile.android.bustracker.core.di.ForIoDispatcher
import uk.org.rivernile.android.bustracker.core.di.ForMainDispatcher
import uk.org.rivernile.android.bustracker.core.features.AndroidFeatureRepository
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityChecker
import uk.org.rivernile.android.bustracker.core.networking.LegacyConnectivityChecker
import uk.org.rivernile.android.bustracker.core.networking.V24ConnectivityChecker
import uk.org.rivernile.android.bustracker.core.notifications.AppNotificationChannels
import uk.org.rivernile.android.bustracker.core.notifications.LegacyAppNotificationChannels
import uk.org.rivernile.android.bustracker.core.notifications.V26AppNotificationChannels
import uk.org.rivernile.android.bustracker.core.permission.AndroidPermissionChecker
import uk.org.rivernile.android.bustracker.core.permission.PermissionChecker
import uk.org.rivernile.android.bustracker.core.preferences.AndroidPreferenceManager
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceManager
import javax.inject.Provider
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
    BackupAgentModule::class,
    BroadcastReceiversModule::class,
    ContentProvidersModule::class,
    CoreModule.Bindings::class,
    DatabaseModule::class,
    FlavourModule::class,
    GooglePlayServicesModule::class,
    LocationModule::class,
    HttpModule::class,
    ServiceModule::class,
    TwitterModule::class
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
     * Provide the app-wide [Json] instance.
     *
     * @return The app-wide [Json] instance.
     */
    @Provides
    @Singleton
    internal fun provideKotlinJsonSerialisation() = Json { ignoreUnknownKeys = true }

    /**
     * Provide the [AppNotificationChannels] instance for dealing with notification channels.
     *
     * @param context The application [Context].
     * @param notificationManager The system [NotificationManagerCompat].
     * @return An [AppNotificationChannels] instance.
     */
    @Provides
    internal fun provideAppNotificationChannels(
            context: Context,
            notificationManager: Provider<NotificationManagerCompat>): AppNotificationChannels {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            V26AppNotificationChannels(context, notificationManager.get())
        } else {
            LegacyAppNotificationChannels()
        }
    }

    @Provides
    internal fun provideConnectivityChecker(
            legacyConnectivityChecker: Provider<LegacyConnectivityChecker>,
            v24ConnectivityChecker: Provider<V24ConnectivityChecker>): ConnectivityChecker {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            v24ConnectivityChecker.get()
        } else {
            legacyConnectivityChecker.get()
        }
    }

    /**
     * Provide a [CoroutineScope] which lives for the entire time the application is alive.
     *
     * @return A [CoroutineScope] which lives for the entire time the application is alive.
     */
    @Provides
    @Singleton
    @ForApplicationCoroutineScope
    internal fun provideApplicationCoroutineScope(
            @ForDefaultDispatcher defaultDispatcher: CoroutineDispatcher): CoroutineScope =
            CoroutineScope(SupervisorJob() + defaultDispatcher)

    /**
     * Provide a [CoroutineDispatcher] for performing operations on the main thread.
     *
     * @return A [CoroutineDispatcher] for performing operations on the main thread.
     */
    @Provides
    @Singleton
    @ForMainDispatcher
    internal fun provideCoroutineMainDispatcher(): CoroutineDispatcher = Dispatchers.Main.immediate

    /**
     * Provide a [CoroutineDispatcher] for performing operations on the default dispatcher. See
     * [Dispatchers.Default].
     *
     * @return A [CoroutineDispatcher] for performing operations on the default dispatcher.
     * @see Dispatchers.Default
     */
    @Provides
    @Singleton
    @ForDefaultDispatcher
    internal fun provideCoroutineDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    /**
     * Provide a [CoroutineDispatcher] for performing IO operations on.
     *
     * @return A [CoroutineDispatcher] for performing IO operations on.
     */
    @Provides
    @Singleton
    @ForIoDispatcher
    internal fun provideCoroutineIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    /**
     * An internal module for declaring type bindings.
     */
    @Module
    internal interface Bindings {

        @Suppress("unused")
        @Binds
        fun bindBackupInvoker(androidBackupInvoker: AndroidBackupInvoker): BackupInvoker

        @Suppress("unused")
        @Binds
        fun bindFeatureRepository(androidFeatureRepository: AndroidFeatureRepository)
                : FeatureRepository

        @Suppress("unused")
        @Binds
        fun bindPermissionChecker(androidPermissionChecker: AndroidPermissionChecker)
                : PermissionChecker

        @Suppress("unused")
        @Binds
        fun bindPreferenceManager(androidPreferenceManager: AndroidPreferenceManager)
                : PreferenceManager

        @Suppress("unused")
        @Binds
        fun bindAppRepository(androidAppRepository: AndroidAppRepository): AppRepository
    }
}