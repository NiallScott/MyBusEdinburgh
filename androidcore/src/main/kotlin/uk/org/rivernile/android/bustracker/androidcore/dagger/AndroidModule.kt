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

import android.app.Application
import android.app.SearchManager
import android.app.backup.BackupManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.provider.SearchRecentSuggestions
import androidx.core.app.NotificationManagerCompat
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uk.org.rivernile.android.bustracker.core.database.search.SearchDatabaseContract
import uk.org.rivernile.android.bustracker.core.di.ForSearchDatabase
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceManager
import javax.inject.Singleton

/**
 * This [Module] provides resources owned by the Android platform.
 *
 * @author Niall Scott
 */
@InstallIn(SingletonComponent::class)
@Module
internal class AndroidModule {

    @Provides
    @Singleton
    fun provideBackupManager(context: Context): BackupManager = BackupManager(context)

    @Provides
    fun provideConnectivityManager(context: Context): ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Provides
    fun provideLocationManager(context: Context): LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @Provides
    @Singleton
    fun provideNotificationManagerCompat(context: Context): NotificationManagerCompat =
            NotificationManagerCompat.from(context)

    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences =
            context.getSharedPreferences(PreferenceManager.PREF_FILE, Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideSearchRecentSuggestions(
            context: Context,
            @ForSearchDatabase authority: String): SearchRecentSuggestions =
            SearchRecentSuggestions(context, authority, SearchDatabaseContract.MODE)

    @Provides
    fun providePackageManager(context: Context): PackageManager = context.packageManager

    @Provides
    fun provideSearchManager(context: Context): SearchManager =
            context.getSystemService(Context.SEARCH_SERVICE) as SearchManager

    @InstallIn(SingletonComponent::class)
    @Module
    interface Bindings {

        @Suppress("unused")
        @Binds
        fun bindContext(application: Application): Context
    }
}