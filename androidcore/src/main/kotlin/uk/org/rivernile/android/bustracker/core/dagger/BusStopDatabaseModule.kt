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
import dagger.Binds
import dagger.Module
import dagger.Provides
import uk.org.rivernile.android.bustracker.core.di.ForBusStopDatabase
import uk.org.rivernile.android.bustracker.core.database.busstop.AndroidBusStopDatabaseRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.BusStopDatabaseRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.daos.AndroidBusStopsDao
import uk.org.rivernile.android.bustracker.core.database.busstop.daos.AndroidDatabaseInformationDao
import uk.org.rivernile.android.bustracker.core.database.busstop.daos.AndroidServicesDao
import uk.org.rivernile.android.bustracker.core.database.busstop.daos.BusStopsDao
import uk.org.rivernile.android.bustracker.core.database.busstop.daos.DatabaseInformationDao
import uk.org.rivernile.android.bustracker.core.database.busstop.daos.ServicesDao
import javax.inject.Singleton

/**
 * This is a Dagger [Module] to provide dependencies for the bus stop database.
 *
 * @author Niall Scott
 */
@Module(includes = [ BusStopDatabaseModule.Bindings::class ])
internal class BusStopDatabaseModule {

    /**
     * Provide the bus stop database authority URI [String].
     *
     * @param context The application [Context].
     * @return The bus stop database authority URI [String].
     */
    @Provides
    @Singleton
    @ForBusStopDatabase
    fun provideAuthority(context: Context) = "${context.packageName}.provider.busstop"

    /**
     * This interface contains Dagger bindings for pre-provided types.
     */
    @Module
    interface Bindings {

        @Suppress("unused")
        @Binds
        fun bindBusStopDatabaseRepository(
                androidBusStopDatabaseRepository: AndroidBusStopDatabaseRepository)
                : BusStopDatabaseRepository

        @Suppress("unused")
        @Binds
        fun bindDatabaseInformationDao(
                androidDatabaseInformationDao: AndroidDatabaseInformationDao)
                : DatabaseInformationDao

        @Suppress("unused")
        @Binds
        fun bindBusStopsDao(androidBusStopsDao: AndroidBusStopsDao): BusStopsDao

        @Suppress("unused")
        @Binds
        fun bindServicesDao(androidServicesDao: AndroidServicesDao): ServicesDao
    }
}