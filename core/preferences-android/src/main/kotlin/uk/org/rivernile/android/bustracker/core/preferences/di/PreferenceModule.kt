/*
 * Copyright (C) 2023 - 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.preferences.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import uk.org.rivernile.android.bustracker.core.preferences.AndroidPreferenceDataStorage
import uk.org.rivernile.android.bustracker.core.preferences.PREF_APP_THEME
import uk.org.rivernile.android.bustracker.core.preferences.PREF_AUTO_REFRESH
import uk.org.rivernile.android.bustracker.core.preferences.PREF_BUS_STOP_DATABASE_WIFI_ONLY
import uk.org.rivernile.android.bustracker.core.preferences.PREF_DISABLE_GPS_PROMPT
import uk.org.rivernile.android.bustracker.core.preferences.PREF_MAP_LAST_LATITUDE
import uk.org.rivernile.android.bustracker.core.preferences.PREF_MAP_LAST_LONGITUDE
import uk.org.rivernile.android.bustracker.core.preferences.PREF_MAP_LAST_MAP_TYPE
import uk.org.rivernile.android.bustracker.core.preferences.PREF_MAP_LAST_ZOOM
import uk.org.rivernile.android.bustracker.core.preferences.PREF_NUMBER_OF_SHOWN_DEPARTURES_PER_SERVICE
import uk.org.rivernile.android.bustracker.core.preferences.PREF_SERVICE_SORTING
import uk.org.rivernile.android.bustracker.core.preferences.PREF_SHOW_NIGHT_BUSES
import uk.org.rivernile.android.bustracker.core.preferences.PREF_ZOOM_BUTTONS
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceDataStorage
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceDataStoreSource
import uk.org.rivernile.android.bustracker.core.preferences.RealPreferenceDataStoreSource
import javax.inject.Singleton

/**
 * This Dagger module provides dependencies for preferences.
 *
 * @author Niall Scott
 */
@InstallIn(SingletonComponent::class)
@Module
internal class PreferenceModule {

    companion object {

        private const val DATA_STORE_PREFERENCES_NAME = "preferences"
        private const val SHARED_PREFERENCES_NAME = "preferences"
    }

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        context: Context,
        exceptionLogger: ExceptionLogger
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler {
                exceptionLogger.log(it)
                emptyPreferences()
            },
            migrations = listOf(
                SharedPreferencesMigration(
                    context,
                    SHARED_PREFERENCES_NAME,
                    keysToMigrate
                )
            ),
            produceFile = { context.preferencesDataStoreFile(DATA_STORE_PREFERENCES_NAME) }
        )
    }

    private val keysToMigrate get() = setOf(
        PREF_BUS_STOP_DATABASE_WIFI_ONLY,
        PREF_APP_THEME,
        PREF_AUTO_REFRESH,
        PREF_SHOW_NIGHT_BUSES,
        PREF_SERVICE_SORTING,
        PREF_NUMBER_OF_SHOWN_DEPARTURES_PER_SERVICE,
        PREF_ZOOM_BUTTONS,
        PREF_DISABLE_GPS_PROMPT,
        PREF_MAP_LAST_LATITUDE,
        PREF_MAP_LAST_LONGITUDE,
        PREF_MAP_LAST_ZOOM,
        PREF_MAP_LAST_MAP_TYPE
    )

    @Suppress("unused")
    @InstallIn(SingletonComponent::class)
    @Module
    interface Bindings {

        @Suppress("unused")
        @Binds
        fun bindPreferenceDataStorage(
            androidPreferenceDataStorage: AndroidPreferenceDataStorage
        ): PreferenceDataStorage

        @Suppress("unused")
        @Binds
        fun bindPreferenceDataStoreSource(
            realPreferenceDataStoreSource: RealPreferenceDataStoreSource
        ): PreferenceDataStoreSource
    }
}
