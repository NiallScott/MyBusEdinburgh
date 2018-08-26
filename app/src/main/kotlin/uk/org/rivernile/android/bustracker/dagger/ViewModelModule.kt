/*
 * Copyright (C) 2018 Niall 'Rivernile' Scott
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

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import uk.org.rivernile.android.bustracker.ui.about.AboutViewModel
import uk.org.rivernile.android.bustracker.ui.busstopmap.BusStopMapViewModel

/**
 * This [Module] is used for injecting classes with [ViewModel] instances
 *
 * @author Niall Scott
 */
@Module
abstract class ViewModelModule {

    /**
     * Inject an [AboutViewModel] when requested.
     *
     * @param viewModel An [AboutViewModel] instance.
     * @return The [ViewModel] instance.
     */
    @Binds
    @IntoMap
    @ViewModelKey(AboutViewModel::class)
    abstract fun bindAboutViewModel(viewModel: AboutViewModel): ViewModel

    /**
     * Inject a [BusStopMapViewModel] when requested.
     *
     * @param viewModel A [BusStopMapViewModel] instance.
     * @return The [ViewModel] instance.
     */
    @Binds
    @IntoMap
    @ViewModelKey(BusStopMapViewModel::class)
    abstract fun bindBusStopMapViewModel(viewModel: BusStopMapViewModel): ViewModel

    /**
     * Inject a [ViewModelFactory] when requested.
     *
     * @param viewModelFactory An [ViewModelFactory] instance.
     * @return The [ViewModelProvider.Factory] instance.
     */
    @Binds
    abstract fun bindViewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory
}