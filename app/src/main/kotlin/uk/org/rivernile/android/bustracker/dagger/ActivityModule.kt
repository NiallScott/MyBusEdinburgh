/*
 * Copyright (C) 2018 - 2022 Niall 'Rivernile' Scott
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
 */

package uk.org.rivernile.android.bustracker.dagger

import dagger.Module
import dagger.android.ContributesAndroidInjector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import uk.org.rivernile.android.bustracker.dagger.about.AboutFragmentsModule
import uk.org.rivernile.android.bustracker.dagger.alerts.AlertFragmentsModule
import uk.org.rivernile.android.bustracker.dagger.alerts.AlertManagerFragmentModule
import uk.org.rivernile.android.bustracker.dagger.busstopmap.BusStopMapFragmentsModule
import uk.org.rivernile.android.bustracker.dagger.displaystopdata.DisplayStopDataFragmentsModule
import uk.org.rivernile.android.bustracker.dagger.favourites.FavouriteFragmentsModule
import uk.org.rivernile.android.bustracker.dagger.favourites.FavouriteStopsFragmentModule
import uk.org.rivernile.android.bustracker.dagger.main.MainFragmentsModule
import uk.org.rivernile.android.bustracker.dagger.neareststops.NearestStopsFragmentsModule
import uk.org.rivernile.android.bustracker.dagger.news.NewsFragmentsModule
import uk.org.rivernile.android.bustracker.dagger.settings.SettingsFragmentsModule
import uk.org.rivernile.android.bustracker.ui.about.AboutActivity
import uk.org.rivernile.android.bustracker.ui.busstopmap.BusStopMapActivity
import uk.org.rivernile.android.bustracker.ui.bustimes.DisplayStopDataActivity
import uk.org.rivernile.android.bustracker.ui.favourites.SelectFavouriteStopActivity
import uk.org.rivernile.android.bustracker.ui.main.MainActivity
import uk.org.rivernile.android.bustracker.ui.search.SearchActivity
import uk.org.rivernile.android.bustracker.ui.settings.SettingsActivity

/**
 * This [Module] is used to inject [android.app.Activity] instance in this application.
 *
 * @author Niall Scott
 */
@Module
interface ActivityModule {

    @Suppress("unused")
    @ContributesAndroidInjector(modules = [
        AlertFragmentsModule::class,
        AlertManagerFragmentModule::class,
        BusStopMapFragmentsModule::class,
        FavouriteStopsFragmentModule::class,
        FavouriteFragmentsModule::class,
        MainFragmentsModule::class,
        NearestStopsFragmentsModule::class,
        NewsFragmentsModule::class
    ])
    fun contributeMainActivity(): MainActivity

    @Suppress("unused")
    @ContributesAndroidInjector(modules = [ AboutFragmentsModule::class ])
    fun contributeAboutActivity(): AboutActivity

    @Suppress("unused")
    @ContributesAndroidInjector(modules = [ SettingsFragmentsModule::class ])
    fun contributeSettingsActivity(): SettingsActivity

    @ExperimentalCoroutinesApi
    @Suppress("unused")
    @ContributesAndroidInjector(modules = [
        AlertFragmentsModule::class,
        DisplayStopDataFragmentsModule::class,
        FavouriteFragmentsModule::class
    ])
    fun contributeDisplayStopDataActivity(): DisplayStopDataActivity

    @Suppress("unused")
    @ContributesAndroidInjector(modules = [ BusStopMapFragmentsModule::class ])
    fun contributeBusStopMapActivity(): BusStopMapActivity

    @Suppress("unused")
    @ContributesAndroidInjector
    fun contributeSearchActivity(): SearchActivity

    @ExperimentalCoroutinesApi
    @Suppress("unused")
    @ContributesAndroidInjector(modules = [
        FavouriteStopsFragmentModule::class
    ])
    fun contributeSelectFavouriteStopActivity(): SelectFavouriteStopActivity
}