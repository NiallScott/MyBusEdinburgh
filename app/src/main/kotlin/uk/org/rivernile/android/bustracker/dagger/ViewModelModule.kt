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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import uk.org.rivernile.android.bustracker.ui.about.AboutViewModel
import uk.org.rivernile.android.bustracker.ui.alerts.AlertManagerFragmentViewModel
import uk.org.rivernile.android.bustracker.ui.alerts.proximity.AddProximityAlertDialogFragmentViewModel
import uk.org.rivernile.android.bustracker.ui.alerts.proximity.DeleteProximityAlertDialogFragmentViewModel
import uk.org.rivernile.android.bustracker.ui.alerts.time.DeleteTimeAlertDialogFragmentViewModel
import uk.org.rivernile.android.bustracker.ui.bustimes.DisplayStopDataActivityViewModel
import uk.org.rivernile.android.bustracker.ui.favourites.remove.DeleteFavouriteDialogFragmentViewModel
import uk.org.rivernile.android.bustracker.ui.main.MainActivityViewModel
import uk.org.rivernile.android.bustracker.ui.news.TwitterUpdatesFragmentViewModel
import uk.org.rivernile.android.bustracker.ui.search.SearchActivityViewModel
import uk.org.rivernile.android.bustracker.ui.settings.ClearSearchHistoryDialogFragmentViewModel
import uk.org.rivernile.android.bustracker.ui.settings.SettingsFragmentViewModel
import uk.org.rivernile.android.bustracker.ui.turnongps.TurnOnGpsDialogFragmentViewModel

/**
 * This [Module] is used for injecting classes with [ViewModel] instances
 *
 * @author Niall Scott
 */
@Module
interface ViewModelModule {

    /**
     * Inject a [MainActivityViewModel] when requested.
     *
     * @param viewModel A [MainActivityViewModel] instance.
     * @return The [ViewModel] instance.
     */
    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    fun bindMainActivityViewModel(viewModel: MainActivityViewModel): ViewModel

    /**
     * Inject a [DisplayStopDataActivityViewModel] when requested.
     *
     * @param viewModel A [DisplayStopDataActivityViewModel] instance.
     * @return The [ViewModel] instance.
     */
    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(DisplayStopDataActivityViewModel::class)
    fun bindDisplayStopDataActivityViewModel(
            viewModel: DisplayStopDataActivityViewModel): ViewModel

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
     * Inject a [DeleteFavouriteDialogFragmentViewModel] when requested.
     *
     * @param viewModel A [DeleteFavouriteDialogFragmentViewModel] instance.
     * @return The [ViewModel] instance.
     */
    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(DeleteFavouriteDialogFragmentViewModel::class)
    fun bindDeleteFavouriteDialogFragmentViewModel(
            viewModel: DeleteFavouriteDialogFragmentViewModel): ViewModel

    /**
     * Inject a [AlertManagerFragmentViewModel] when requested.
     *
     * @param viewModel A [AlertManagerFragmentViewModel] instance.
     * @return The [ViewModel] instance.
     */
    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(AlertManagerFragmentViewModel::class)
    fun bindAlertManagerFragmentViewModel(viewModel: AlertManagerFragmentViewModel): ViewModel

    /**
     * Inject a [AddProximityAlertDialogFragmentViewModel] when requested.
     *
     * @param viewModel A [AddProximityAlertDialogFragmentViewModel] instance.
     * @return The [ViewModel] instance.
     */
    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(AddProximityAlertDialogFragmentViewModel::class)
    fun bindAddProximityAlertDialogFragmentViewModel(
            viewModel: AddProximityAlertDialogFragmentViewModel): ViewModel

    /**
     * Inject a [DeleteProximityAlertDialogFragmentViewModel] when requested.
     *
     * @param viewModel A [DeleteProximityAlertDialogFragmentViewModel] instance.
     * @return The [ViewModel] instance.
     */
    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(DeleteProximityAlertDialogFragmentViewModel::class)
    fun bindDeleteProximityAlertDialogFragmentViewModel(
            viewModel: DeleteProximityAlertDialogFragmentViewModel): ViewModel

    /**
     * Inject a [DeleteTimeAlertDialogFragmentViewModel] when requested.
     *
     * @param viewModel A [DeleteTimeAlertDialogFragmentViewModel] instance.
     * @return The [ViewModel] instance.
     */
    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(DeleteTimeAlertDialogFragmentViewModel::class)
    fun bindDeleteTimeAlertDialogFragmentViewModel(
            viewModel: DeleteTimeAlertDialogFragmentViewModel): ViewModel

    /**
     * Inject a [TurnOnGpsDialogFragmentViewModel] when requested.
     *
     * @param viewModel A [TurnOnGpsDialogFragmentViewModel] instance.
     * @return The [ViewModel] instance.
     */
    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(TurnOnGpsDialogFragmentViewModel::class)
    fun bindTurnOnGpsDialogFragmentViewModel(
            viewModel: TurnOnGpsDialogFragmentViewModel): ViewModel

    /**
     * Inject a [SearchActivityViewModel] when requested.
     *
     * @param viewModel A [SearchActivityViewModel] instance.
     * @return The [ViewModel] instance.
     */
    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(SearchActivityViewModel::class)
    fun bindSearchActivityViewModel(
            viewModel: SearchActivityViewModel): ViewModel

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