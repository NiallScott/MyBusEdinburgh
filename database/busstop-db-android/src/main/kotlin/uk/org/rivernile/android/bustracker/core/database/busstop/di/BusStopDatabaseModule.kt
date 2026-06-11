/*
 * Copyright (C) 2019 - 2026 Niall 'Rivernile' Scott
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
import uk.org.rivernile.android.bustracker.core.database.busstop.BusStopDatabase
import uk.org.rivernile.android.bustracker.core.database.busstop.BusStopDatabaseRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.DatabaseReplacer
import uk.org.rivernile.android.bustracker.core.database.busstop.DownloadedDatabasePreparer
import uk.org.rivernile.android.bustracker.core.database.busstop.RealDownloadedDatabasePreparer
import uk.org.rivernile.android.bustracker.core.database.busstop.RealRoomBusStopDatabaseFactory
import uk.org.rivernile.android.bustracker.core.database.busstop.RoomBusStopDatabaseFactory
import uk.org.rivernile.android.bustracker.core.database.busstop.database.DatabaseDao
import uk.org.rivernile.android.bustracker.core.database.busstop.database.ProxyDatabaseDao
import uk.org.rivernile.android.bustracker.core.database.busstop.operator.OperatorDao
import uk.org.rivernile.android.bustracker.core.database.busstop.operator.ProxyOperatorDao
import uk.org.rivernile.android.bustracker.core.database.busstop.service.ProxyServiceDao
import uk.org.rivernile.android.bustracker.core.database.busstop.service.ServiceDao
import uk.org.rivernile.android.bustracker.core.database.busstop.servicepoint.ProxyServicePointDao
import uk.org.rivernile.android.bustracker.core.database.busstop.servicepoint.ServicePointDao
import uk.org.rivernile.android.bustracker.core.database.busstop.servicestop.ProxyServiceStopDao
import uk.org.rivernile.android.bustracker.core.database.busstop.servicestop.ServiceStopDao
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.ProxyStopDao
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

    @Suppress("unused")
    @InstallIn(SingletonComponent::class)
    @Module
    interface Bindings {

        @Binds
        fun bindBusStopDatabase(androidBusStopDatabase: AndroidBusStopDatabase): BusStopDatabase

        @Binds
        fun bindBusStopDatabaseRepository(
            androidBusStopDatabaseRepository: AndroidBusStopDatabaseRepository
        ): BusStopDatabaseRepository

        @Binds
        fun bindDatabaseDao(proxyDatabaseDao: ProxyDatabaseDao): DatabaseDao

        @Binds
        fun bindDatabaseReplacer(androidBusStopDatabase: AndroidBusStopDatabase): DatabaseReplacer

        @Binds
        fun bindDownloadedDatabasePreparer(
            realDownloadedDatabasePreparer: RealDownloadedDatabasePreparer
        ): DownloadedDatabasePreparer

        @Binds
        fun bindOperatorDao(proxyOperatorDao: ProxyOperatorDao): OperatorDao

        @Binds
        fun bindRoomBusStopDatabaseFactory(
            realRoomBusStopDatabaseFactory: RealRoomBusStopDatabaseFactory
        ): RoomBusStopDatabaseFactory

        @Binds
        fun bindServiceDao(proxyServiceDao: ProxyServiceDao): ServiceDao

        @Binds
        fun bindServicePointDao(proxyServicePointDao: ProxyServicePointDao): ServicePointDao

        @Binds
        fun bindServiceStopDao(proxyServiceStopDao: ProxyServiceStopDao): ServiceStopDao

        @Binds
        fun bindStopDao(proxyStopDao: ProxyStopDao): StopDao
    }
}
