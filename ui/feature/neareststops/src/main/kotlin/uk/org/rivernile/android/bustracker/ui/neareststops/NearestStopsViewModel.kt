/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
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

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForViewModelCoroutineScope
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import javax.inject.Inject

@HiltViewModel
internal class NearestStopsViewModel @Inject constructor(
    @ForViewModelCoroutineScope viewModelCoroutineScope: CoroutineScope
) : ViewModel(viewModelCoroutineScope) {

    val uiStateFlow: StateFlow<UiState> = MutableStateFlow(UiState())

    fun onItemClicked(stopIdentifier: StopIdentifier) {

    }

    fun onOpenDropdownMenuClicked(stopIdentifier: StopIdentifier) {

    }

    fun onDropdownMenuDismissed() {

    }

    fun onAddFavouriteStopClicked(stopIdentifier: StopIdentifier) {

    }

    fun onRemoveFavouriteStopClicked(stopIdentifier: StopIdentifier) {

    }

    fun onAddArrivalAlertClicked(stopIdentifier: StopIdentifier) {

    }

    fun onRemoveArrivalAlertClicked(stopIdentifier: StopIdentifier) {

    }

    fun onAddProximityAlertClicked(stopIdentifier: StopIdentifier) {

    }

    fun onRemoveProximityAlertCLicked(stopIdentifier: StopIdentifier) {

    }

    fun onShowOnMapClicked(stopIdentifier: StopIdentifier) {

    }

    fun onGrantPermissionClicked() {

    }

    fun onOpenSettingsClicked() {

    }

    fun onActionLaunched() {

    }
}
