/*
 * Copyright (C) 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.neareststops

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceRepository
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import uk.org.rivernile.android.bustracker.viewmodel.ViewModelSavedStateFactory
import javax.inject.Inject

/**
 * This is used to create [NearestStopsFragmentViewModel]s with its dependencies and access to
 * [SavedStateHandle].
 *
 * @param servicesRepository Used to get the services in the network.
 * @param busStopsRepository Used to get data relating to stops.
 * @param favouritesStateRetriever Used to retrieve favourites state.
 * @param alertsStateRetriever Used to retrieve alerts state.
 * @param featureRepository Used to determine what features a device has access to.
 * @param locationRepository Used to access data relating to device location.
 * @param preferenceRepository Used to access user preferences.
 * @param uiStateRetriever Used to get the current UI state.
 * @param defaultDispatcher The default [CoroutineDispatcher].
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
class NearestStopsFragmentViewModelFactory @Inject constructor(
        private val servicesRepository: ServicesRepository,
        private val busStopsRepository: BusStopsRepository,
        private val favouritesStateRetriever: FavouritesStateRetriever,
        private val alertsStateRetriever: AlertsStateRetriever,
        private val featureRepository: FeatureRepository,
        private val locationRepository: LocationRepository,
        private val preferenceRepository: PreferenceRepository,
        private val uiStateRetriever: UiStateRetriever,
        @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher)
    : ViewModelSavedStateFactory<NearestStopsFragmentViewModel> {

    override fun create(handle: SavedStateHandle) =
            NearestStopsFragmentViewModel(
                    handle,
                    servicesRepository,
                    busStopsRepository,
                    favouritesStateRetriever,
                    alertsStateRetriever,
                    featureRepository,
                    locationRepository,
                    preferenceRepository,
                    uiStateRetriever,
                    defaultDispatcher)
}