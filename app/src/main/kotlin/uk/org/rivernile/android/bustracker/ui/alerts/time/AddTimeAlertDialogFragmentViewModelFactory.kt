/*
 * Copyright (C) 2021 - 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.alerts.time

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.di.ForApplicationCoroutineScope
import uk.org.rivernile.android.bustracker.core.servicestops.ServiceStopsRepository
import uk.org.rivernile.android.bustracker.viewmodel.ViewModelSavedStateFactory
import javax.inject.Inject

/**
 * This [ViewModelSavedStateFactory] creates an [AddTimeAlertDialogFragmentViewModel] with the
 * ability to use [SavedStateHandle].
 *
 * @param busStopsRepository Used to get stop details.
 * @param serviceStopsRepository Used to get the services for the selected stop code.
 * @param uiStateCalculator Used to calculate the current [UiState].
 * @param alertsRepository Used to add the arrival alert.
 * @param applicationCoroutineScope The [CoroutineScope] to add the alert under.
 * @param defaultDispatcher The default [CoroutineDispatcher].
 * @author Niall Scott
 */
class AddTimeAlertDialogFragmentViewModelFactory @Inject constructor(
        private val busStopsRepository: BusStopsRepository,
        private val serviceStopsRepository: ServiceStopsRepository,
        private val uiStateCalculator: UiStateCalculator,
        private val alertsRepository: AlertsRepository,
        @ForApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
        @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher)
    : ViewModelSavedStateFactory<AddTimeAlertDialogFragmentViewModel> {

    override fun create(handle: SavedStateHandle) =
            AddTimeAlertDialogFragmentViewModel(
                    handle,
                    busStopsRepository,
                    serviceStopsRepository,
                    uiStateCalculator,
                    alertsRepository,
                    applicationCoroutineScope,
                    defaultDispatcher)
}