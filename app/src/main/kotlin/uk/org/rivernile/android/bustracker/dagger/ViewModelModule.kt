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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import uk.org.rivernile.android.bustracker.ui.about.AboutViewModel
import uk.org.rivernile.android.bustracker.ui.busstopmap.BusStopMapViewModel
import uk.org.rivernile.android.bustracker.ui.news.TwitterUpdatesFragmentViewModel
import uk.org.rivernile.android.bustracker.ui.settings.ClearSearchHistoryDialogFragmentViewModel
import uk.org.rivernile.android.bustracker.ui.settings.SettingsFragmentViewModel

/**
 * This [Module] is used for injecting classes with [ViewModel] instances
 *
 * @author Niall Scott
 */
@Module
interface ViewModelModule {

    /**
     * Inject an [AboutViewModel] when requested.
     *
     * @param viewModel An [AboutViewModel] instance.
     * @return The [ViewModel] instance.
     */
    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(AboutViewModel::class)
    fun bindAboutViewModel(viewModel: AboutViewModel): ViewModel

    /**
     * Inject an [SettingsFragmentViewModel] when requested.
     *
     * @param viewModel An [SettingsFragmentViewModel] instance.
     * @return The [ViewModel] instance.
     */
    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(SettingsFragmentViewModel::class)
    fun bindSettingsFragmentViewModel(viewModel: SettingsFragmentViewModel): ViewModel

    /**
     * Inject an [ClearSearchHistoryDialogFragmentViewModel] when requested.
     *
     * @param viewModel An [ClearSearchHistoryDialogFragmentViewModel] instance.
     * @return The [ViewModel] instance.
     */
    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(ClearSearchHistoryDialogFragmentViewModel::class)
    fun bindClearSearchHistoryDialogFragmentViewModel(
            viewModel: ClearSearchHistoryDialogFragmentViewModel): ViewModel

    /**
     * Inject a [BusStopMapViewModel] when requested.
     *
     * @param viewModel A [BusStopMapViewModel] instance.
     * @return The [ViewModel] instance.
     */
    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(BusStopMapViewModel::class)
    fun bindBusStopMapViewModel(viewModel: BusStopMapViewModel): ViewModel

    /**
     * Inject a [TwitterUpdatesFragmentViewModel] when request.
     *
     * @param viewModel A [TwitterUpdatesFragmentViewModel] instance.
     * @return The [ViewModel] instance.
     */
    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(TwitterUpdatesFragmentViewModel::class)
    fun bindTwitterUpdatesFragmentViewModel(viewModel: TwitterUpdatesFragmentViewModel): ViewModel

    /**
     * Inject a [ViewModelFactory] when requested.
     *
     * @param viewModelFactory An [ViewModelFactory] instance.
     * @return The [ViewModelProvider.Factory] instance.
     */
    @Suppress("unused")
    @Binds
    fun bindViewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory
}