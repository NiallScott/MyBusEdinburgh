/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.bustimes.times

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityRepository
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceRepository
import uk.org.rivernile.android.bustracker.core.utils.TimeUtils
import uk.org.rivernile.android.bustracker.viewmodel.ViewModelSavedStateFactory
import javax.inject.Inject

/**
 * This [ViewModelSavedStateFactory] creates a [BusTimesFragmentViewModel] with the ability to use
 * [SavedStateHandle].
 *
 * @param liveTimesLoader This is used to load live times.
 * @param liveTimesTransform This is used to transform the result of loading live times in to a form
 * consumable by the UI.
 * @param lastRefreshTimeCalculator Used to calculate the amount of time since the last refresh.
 * @param connectivityRepository Used to determine device connectivity.
 * @param preferenceRepository This contains the user's preferences.
 * @param timeUtils An implementation to provide timestamps.
 * @param defaultDispatcher The dispatcher to use to execute background processing on.
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
class BusTimesFragmentViewModelFactory @Inject constructor(
        private val liveTimesLoader: LiveTimesLoader,
        private val liveTimesTransform: LiveTimesTransform,
        private val lastRefreshTimeCalculator: LastRefreshTimeCalculator,
        private val connectivityRepository: ConnectivityRepository,
        private val preferenceRepository: PreferenceRepository,
        private val timeUtils: TimeUtils,
        @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher)
    : ViewModelSavedStateFactory<BusTimesFragmentViewModel> {

    override fun create(handle: SavedStateHandle): BusTimesFragmentViewModel {
        val expandedServicesTracker = ExpandedServicesTracker(handle)

        return BusTimesFragmentViewModel(
                expandedServicesTracker,
                liveTimesLoader,
                liveTimesTransform,
                lastRefreshTimeCalculator,
                preferenceRepository,
                connectivityRepository,
                timeUtils,
                defaultDispatcher)
    }
}