/*
 * Copyright (C) 2021 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.favourites

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import uk.org.rivernile.android.bustracker.viewmodel.ViewModelSavedStateFactory
import javax.inject.Inject

/**
 * This is used to create [FavouriteStopsFragmentViewModel]s with its dependencies and access to
 * [SavedStateHandle].
 *
 * @param favouriteStopsRetriever Used to retrieve favourite stops and process them as required.
 * @param alertsRepository Used to access alert data.
 * @param featureRepository Used to determine which features are available.
 * @param defaultDispatcher The default [CoroutineDispatcher].
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
class FavouriteStopsFragmentViewModelFactory @Inject constructor(
        private val favouriteStopsRetriever: FavouriteStopsRetriever,
        private val alertsRepository: AlertsRepository,
        private val featureRepository: FeatureRepository,
        @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher)
    : ViewModelSavedStateFactory<FavouriteStopsFragmentViewModel> {

    override fun create(handle: SavedStateHandle) =
            FavouriteStopsFragmentViewModel(
                    handle,
                    favouriteStopsRetriever,
                    alertsRepository,
                    featureRepository,
                    defaultDispatcher)
}