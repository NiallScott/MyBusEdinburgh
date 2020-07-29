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

import android.app.Application
import android.app.backup.BackupManager
import android.app.job.JobScheduler
import android.content.Context
import android.content.SharedPreferences
import android.location.LocationManager
import android.net.ConnectivityManager
import android.provider.SearchRecentSuggestions
import androidx.core.app.NotificationManagerCompat
import dagger.Binds
import dagger.Module
import dagger.Provides
import uk.org.rivernile.android.bustracker.core.database.search.SearchDatabaseContract
import uk.org.rivernile.android.bustracker.core.di.ForNoBackup
import uk.org.rivernile.android.bustracker.core.di.ForSearchDatabase
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceManager
import javax.inject.Singleton

/**
 * This [Module] provides resources owned by the Android platform.
 *
 * @author Niall Scott
 */
@Module(includes = [ AndroidModule.Bindings::class ])
internal class AndroidModule {

    /**
     * Provide the [BackupManager].
     *
     * @param context The application [Context].
     * @return The [BackupManager].
     */
    @Provides
    @Singleton
    fun provideBackupManager(context: Context) = BackupManager(context)

    /**
     * Provide the [ConnectivityManager].
     *
     * @param context The application [Context].
     * @return The [ConnectivityManager] instance.
     */
    @Provides
    @Singleton
    fun provideConnectivityManager(context: Context): ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Provide the [JobScheduler].
     *
     * @param context The application [Context].
     * @return The [JobScheduler] instance.
     */
    @Provides
    @Singleton
    fun provideJobScheduler(context: Context): JobScheduler =
            context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

    /**
     * Provide the [LocationManager].
     *
     * @param context The application [Context].
     * @return The [LocationManager] instance.
     */
    @Provides
    @Singleton
    fun provideLocationManager(context: Context): LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    /**
     * Provide the [NotificationManagerCompat].
     *
     * @param context The application [Context].
     * @return The [NotificationManagerCompat] instance.
     */
    @Provides
    @Singleton
    fun provideNotificationManagerCompat(context: Context): NotificationManagerCompat =
            NotificationManagerCompat.from(context)

    /**
     * Provide the [SharedPreferences].
     *
     * @param context The application [Context].
     * @return The [SharedPreferences] instance.
     */
    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences =
            context.getSharedPreferences(PreferenceManager.PREF_FILE, Context.MODE_PRIVATE)

    /**
     * Provide the no-backup [SharedPreferences].
     *
     * @param context The application [Context].
     * @return The no-backup [SharedPreferences] instance.
     */
    @Provides
    @Singleton
    @ForNoBackup
    fun provideNoBackupSharedPreferences(context: Context): SharedPreferences =
            context.getSharedPreferences("no-backup", Context.MODE_PRIVATE)

    /**
     * Provide the [SearchRecentSuggestions] implementation, provided by the Android platform,
     * which manages the user's recent search items.
     *
     * @param context The application [Context].
     * @param authority The authority of the search database.
     * @return The [SearchRecentSuggestions] instance.
     */
    @Provides
    @Singleton
    fun provideSearchRecentSuggestions(
            context: Context,
            @ForSearchDatabase authority: String) =
            SearchRecentSuggestions(context, authority, SearchDatabaseContract.MODE)

    /**
     * This interface contains Dagger bindings for pre-provided types.
     */
    @Module
    interface Bindings {

        @Suppress("unused")
        @Binds
        fun bindContext(application: Application): Context
    }
}