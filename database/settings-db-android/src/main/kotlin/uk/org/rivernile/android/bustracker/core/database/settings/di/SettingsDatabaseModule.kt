/*
 * Copyright (C) 2019 - 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.settings.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uk.org.rivernile.android.bustracker.core.database.settings.RoomSettingsDatabase
import uk.org.rivernile.android.bustracker.core.database.settings.SettingsDatabaseCallback
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.AlertsDao
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.RoomAlertsDao
import uk.org.rivernile.android.bustracker.core.database.settings.favouritestops.FavouriteStopEntityFactory
import uk.org.rivernile.android.bustracker.core.database.settings.favouritestops.FavouriteStopsDao
import uk.org.rivernile.android.bustracker.core.database.settings.favouritestops.RoomFavouriteStopEntityFactory
import uk.org.rivernile.android.bustracker.core.database.settings.favouritestops.RoomFavouriteStopsDao
import uk.org.rivernile.android.bustracker.core.database.settings.migrations.Migration1To4
import uk.org.rivernile.android.bustracker.core.database.settings.migrations.Migration2To4
import uk.org.rivernile.android.bustracker.core.database.settings.migrations.Migration3To4
import javax.inject.Singleton

/**
 * This is a Dagger [Module] to provide dependencies for the settings database.
 *
 * @author Niall Scott
 */
@InstallIn(SingletonComponent::class)
@Module
internal class SettingsDatabaseModule {

    companion object {

        private const val DATABASE_NAME = "settings.db"
    }

    @Provides
    @Singleton
    fun provideSettingsDatabase(
        context: Context,
        callback: SettingsDatabaseCallback
    ): RoomSettingsDatabase = Room
        .databaseBuilder(context, RoomSettingsDatabase::class.java, DATABASE_NAME)
        .addMigrations(*allMigrations)
        .addCallback(callback)
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideAlertsDao(database: RoomSettingsDatabase): RoomAlertsDao =
        database.alertsDao

    @Provides
    fun provideFavouriteStopsDao(database: RoomSettingsDatabase): RoomFavouriteStopsDao =
        database.favouriteStopsDao

    private val allMigrations get() = arrayOf(
        Migration1To4(),
        Migration2To4(),
        Migration3To4()
    )

    @Suppress("unused")
    @InstallIn(SingletonComponent::class)
    @Module
    interface Bindings {

        @Suppress("unused")
        @Binds
        fun bindAlertsDao(roomAlertsDao: RoomAlertsDao): AlertsDao

        @Suppress("unused")
        @Binds
        fun bindFavouriteStopsDao(roomFavouriteStopsDao: RoomFavouriteStopsDao): FavouriteStopsDao

        @Suppress("unused")
        @Binds
        fun bindFavouriteStopsEntityFactory(
            roomFavouriteStopEntityFactory: RoomFavouriteStopEntityFactory
        ): FavouriteStopEntityFactory
    }
}