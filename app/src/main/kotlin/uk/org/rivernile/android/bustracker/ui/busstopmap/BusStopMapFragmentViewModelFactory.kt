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

package uk.org.rivernile.android.bustracker.ui.busstopmap

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineDispatcher
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceManager
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import uk.org.rivernile.android.bustracker.repositories.busstopmap.BusStopMapRepository
import uk.org.rivernile.android.bustracker.viewmodel.ViewModelSavedStateFactory
import javax.inject.Inject

/**
 * This is used to create [BusStopMapViewModel]s with its dependencies and access to
 * [SavedStateHandle].
 *
 * @param locationRepository Used to access location-related data.
 * @param servicesRepository Used to access services data.
 * @param isMyLocationEnabledDetector Used to detect whether the My Location feature is enabled or
 * not.
 * @param repository The [BusStopMapRepository].
 * @param preferenceManager The [PreferenceManager].
 * @param defaultDispatcher The default [CoroutineDispatcher].
 * @author Niall Scott
 */
class BusStopMapFragmentViewModelFactory @Inject constructor(
        private val locationRepository: LocationRepository,
        private val servicesRepository: ServicesRepository,
        private val isMyLocationEnabledDetector: IsMyLocationEnabledDetector,
        private val repository: BusStopMapRepository,
        private val preferenceManager: PreferenceManager,
        @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher)
    : ViewModelSavedStateFactory<BusStopMapViewModel> {

    override fun create(handle: SavedStateHandle) =
            BusStopMapViewModel(
                    handle,
                    locationRepository,
                    servicesRepository,
                    isMyLocationEnabledDetector,
                    repository,
                    preferenceManager,
                    defaultDispatcher)
}