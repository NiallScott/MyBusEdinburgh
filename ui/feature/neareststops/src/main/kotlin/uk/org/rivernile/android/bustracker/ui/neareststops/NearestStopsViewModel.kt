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
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForViewModelCoroutineScope
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import javax.inject.Inject

@HiltViewModel
internal class NearestStopsViewModel @Inject constructor(
    private val state: State,
    private val uiContentRetriever: UiContentRetriever,
    private val uiActionButtonsGenerator: UiActionButtonsGenerator,
    @ForDefaultDispatcher defaultCoroutineDispatcher: CoroutineDispatcher,
    @ForViewModelCoroutineScope viewModelCoroutineScope: CoroutineScope
) : ViewModel(viewModelCoroutineScope) {

    val uiStateFlow: StateFlow<UiState> = _uiStateFlow
        .flowOn(defaultCoroutineDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState()
        )

    fun onItemClicked(stopIdentifier: StopIdentifier) {
        state.action = UiAction.ShowStopData(
            stopIdentifier = stopIdentifier
        )
    }

    fun onOpenDropdownMenuClicked(stopIdentifier: StopIdentifier) {
        state.selectedStopIdentifier = stopIdentifier
    }

    fun onDropdownMenuDismissed() {
        dismissDropdownMenu()
    }

    fun onAddFavouriteStopClicked(stopIdentifier: StopIdentifier) {
        state.action = UiAction.ShowAddFavouriteStop(
            stopIdentifier = stopIdentifier
        )
        dismissDropdownMenu()
    }

    fun onRemoveFavouriteStopClicked(stopIdentifier: StopIdentifier) {
        state.action = UiAction.ShowRemoveFavouriteStop(
            stopIdentifier = stopIdentifier
        )
        dismissDropdownMenu()
    }

    fun onAddArrivalAlertClicked(stopIdentifier: StopIdentifier) {
        state.action = UiAction.ShowAddArrivalAlert(
            stopIdentifier = stopIdentifier
        )
        dismissDropdownMenu()
    }

    fun onRemoveArrivalAlertClicked(stopIdentifier: StopIdentifier) {
        state.action = UiAction.ShowRemoveArrivalAlert(
            stopIdentifier = stopIdentifier
        )
        dismissDropdownMenu()
    }

    fun onAddProximityAlertClicked(stopIdentifier: StopIdentifier) {
        state.action = UiAction.ShowAddProximityAlert(
            stopIdentifier = stopIdentifier
        )
        dismissDropdownMenu()
    }

    fun onRemoveProximityAlertCLicked(stopIdentifier: StopIdentifier) {
        state.action = UiAction.ShowRemoveProximityAlert(
            stopIdentifier = stopIdentifier
        )
        dismissDropdownMenu()
    }

    fun onShowOnMapClicked(stopIdentifier: StopIdentifier) {
        state.action = UiAction.ShowOnMap(
            stopIdentifier = stopIdentifier
        )
        dismissDropdownMenu()
    }

    fun onGrantPermissionClicked() {
        state.action = UiAction.RequestLocationPermissions
    }

    fun onOpenSettingsClicked() {
        state.action = UiAction.ShowLocationSettings
    }

    fun onShowServicesChooserClicked() {
        state.action = UiAction.ShowServicesChooser(
            selectedServices = state.selectedServices?.toImmutableSet()
        )
    }

    fun onLocationAccuracyOpenAppSettingsClicked() {
        state.action = UiAction.ShowAppPermissionSettings
    }

    fun onLocationAccuracyOpenSystemLocationSettingsClicked() {
        state.action = UiAction.ShowLocationSettings
    }

    fun onActionLaunched() {
        state.action = null
    }

    fun onUpdatePermissionsState(permissionsState: PermissionsState) {
        state.permissionsState = permissionsState
    }

    fun onServicesSelected(selectedServices: Set<ServiceDescriptor>?) {
        state.selectedServices = selectedServices
    }

    private val _uiStateFlow get() = combine(
        uiContentRetriever.uiContentFlow,
        uiActionButtonsGenerator.uiActionButtonsFlow,
        state.actionFlow,
        ::UiState
    )

    private fun dismissDropdownMenu() {
        state.selectedStopIdentifier = null
    }
}
