/*
 * Copyright (C) 2019 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.busstop.di

import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uk.org.rivernile.android.bustracker.core.database.busstop.AndroidBusStopDatabase
import uk.org.rivernile.android.bustracker.core.database.busstop.AndroidBusStopDatabaseRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.BusStopDatabaseRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.database.DatabaseDao
import uk.org.rivernile.android.bustracker.core.database.busstop.service.ServiceDao
import uk.org.rivernile.android.bustracker.core.database.busstop.servicepoint.ServicePointDao
import uk.org.rivernile.android.bustracker.core.database.busstop.servicestop.ServiceStopDao
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopDao

/**
 * This is a Dagger [Module] to provide dependencies for the bus stop database.
 *
 * @author Niall Scott
 */
@InstallIn(SingletonComponent::class)
@Module
internal class BusStopDatabaseModule {

    @Provides
    fun provideFrameworkSQLiteOpenHelperFactory(): FrameworkSQLiteOpenHelperFactory =
        FrameworkSQLiteOpenHelperFactory()

    @Provides
    fun provideDatabaseDao(database: AndroidBusStopDatabase): DatabaseDao =
        database.databaseDao

    @Provides
    fun provideServiceDao(database: AndroidBusStopDatabase): ServiceDao =
        database.serviceDao

    @Provides
    fun provideServicePointDao(database: AndroidBusStopDatabase): ServicePointDao =
        database.servicePointDao

    @Provides
    fun provideServiceStopDao(database: AndroidBusStopDatabase): ServiceStopDao =
        database.serviceStopDao

    @Provides
    fun provideStopDao(database: AndroidBusStopDatabase): StopDao =
        database.stopDao

    @InstallIn(SingletonComponent::class)
    @Module
    interface Bindings {

        @Suppress("unused")
        @Binds
        fun bindBusStopDatabaseRepository(
            androidBusStopDatabaseRepository: AndroidBusStopDatabaseRepository):
                BusStopDatabaseRepository
    }
}