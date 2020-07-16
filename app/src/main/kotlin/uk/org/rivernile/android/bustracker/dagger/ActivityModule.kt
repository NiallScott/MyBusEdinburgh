/*
 * Copyright (C) 2018 - 2020 Niall 'Rivernile' Scott
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
import uk.org.rivernile.android.bustracker.dagger.about.AboutFragmentsModule
import uk.org.rivernile.android.bustracker.dagger.busstopmap.BusStopMapFragmentsModule
import uk.org.rivernile.android.bustracker.ui.about.AboutActivity
import uk.org.rivernile.android.bustracker.ui.busstopmap.BusStopMapActivity
import uk.org.rivernile.android.bustracker.ui.main.MainActivity

/**
 * This [Module] is used to inject [android.app.Activity] instance in this application.
 *
 * @author Niall Scott
 */
@Module
interface ActivityModule {

    /**
     * Presents an instance of [MainActivity] as an item to be injected.
     *
     * @return An instance of [MainActivity] to be injected.
     */
    @Suppress("unused")
    @ContributesAndroidInjector(modules = [BusStopMapFragmentsModule::class])
    fun contributeMainActivity(): MainActivity

    /**
     * Presents an instance of [AboutActivity] as an item to be injected.
     *
     * @return An instance of [AboutActivity] to be injected.
     */
    @Suppress("unused")
    @ContributesAndroidInjector(modules = [AboutFragmentsModule::class])
    fun contributeAboutActivity(): AboutActivity

    /**
     * Presents an instance of [BusStopMapActivity] as an item to be injected.
     *
     * @return An instance of [BusStopMapActivity] to be injected.
     */
    @Suppress("unused")
    @ContributesAndroidInjector(modules = [BusStopMapFragmentsModule::class])
    fun contributeBusStopMapActivity(): BusStopMapActivity
}