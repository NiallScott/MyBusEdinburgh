/*
 * Copyright (C) 2021 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.alerts.proximity

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.alerts.proximity.ProximityAlertRequest
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.busstops.StopName
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForApplicationCoroutineScope
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.domain.ParcelableStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toStopIdentifier
import uk.org.rivernile.android.bustracker.utils.SingleLiveEvent
import javax.inject.Inject

/**
 * This is the [ViewModel] for [AddProximityAlertDialogFragment].
 *
 * @param savedState The saved state to obtain state from previous instances.
 * @param permissionsTracker Used to track the state of required permissions.
 * @param busStopsRepository Used to get stop details.
 * @param uiStateCalculator Used to calculate the current [UiState].
 * @param alertsRepository Used to add the proximity alert.
 * @param applicationCoroutineScope The [CoroutineScope] to add the alert under.
 * @param defaultDispatcher The default [CoroutineDispatcher].
 * @author Niall Scott
 */
@HiltViewModel
class AddProximityAlertDialogFragmentViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val permissionsTracker: PermissionsTracker,
    private val busStopsRepository: BusStopsRepository,
    private val uiStateCalculator: UiStateCalculator,
    private val alertsRepository: AlertsRepository,
    @param:ForApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
    @param:ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    companion object {

        /**
         * This constant contains the key where the stop identifier is stored in the saved state.
         */
        const val STATE_STOP_IDENTIFIER = "stopIdentifier"
    }

    /**
     * This property is used to get and set the stop identifier the proximity alert should be added for.
     */
    var stopIdentifier: ParcelableStopIdentifier?
        get() = savedState[STATE_STOP_IDENTIFIER]
        set(value) {
            savedState[STATE_STOP_IDENTIFIER] = value
        }

    private val parcelableStopIdentifierFlow = savedState
        .getStateFlow<ParcelableStopIdentifier?>(STATE_STOP_IDENTIFIER, null)

    private val stopIdentifierFlow get() = parcelableStopIdentifierFlow
        .map { it?.toStopIdentifier() }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val stopDetailsFlow = stopIdentifierFlow
        .flatMapLatest(this::loadStopDetails)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    /**
     * This [LiveData] emits the current [StopDetails] for the given stop identifier.
     */
    val stopDetailsLiveData = stopDetailsFlow
        .asLiveData(viewModelScope.coroutineContext)
        .distinctUntilChanged()

    private val uiStateFlow = uiStateCalculator
        .createUiStateFlow(
            permissionsTracker.permissionsStateFlow,
            permissionsTracker.backgroundLocationPermissionStateFlow,
            stopDetailsFlow
        )
        .flowOn(defaultDispatcher)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), UiState.PROGRESS)

    /**
     * This [LiveData] emits the current [UiState].
     */
    val uiStateLiveData = uiStateFlow
        .asLiveData(viewModelScope.coroutineContext)
        .distinctUntilChanged()

    /**
     * This [LiveData] emits the enabled state of the 'Add' button.
     */
    val addButtonEnabledLiveData = uiStateFlow
        .map { it == UiState.CONTENT }
        .distinctUntilChanged()
        .asLiveData(viewModelScope.coroutineContext)

    /**
     * When this [LiveData] emits a new item, the limitations dialog should be shown.
     */
    val showLimitationsLiveData: LiveData<Unit> get() = showLimitations
    private val showLimitations = SingleLiveEvent<Unit>()

    /**
     * When this [LiveData] emits a new item, the system location settings screen should be shown.
     */
    val showLocationSettingsLiveData: LiveData<Unit> get() = showLocationSettings
    private val showLocationSettings = SingleLiveEvent<Unit>()

    /**
     * When this [LiveData] emits a new item, the request permission system dialog should be shown.
     */
    val requestPermissionsLiveData get() = permissionsTracker.requestPermissionsLiveData

    /**
     * When this [LiveData] emits a new item, the request background location permission system UI
     * should be shown.
     */
    val requestBackgroundLocationPermissionLiveData
        get() = permissionsTracker.requestBackgroundLocationPermissionLiveData

    /**
     * When this [LiveData] emits a new item, the app settings screen should be shown.
     */
    val showAppSettingsLiveData: LiveData<Unit> get() = showAppSettings
    private val showAppSettings = SingleLiveEvent<Unit>()

    /**
     * This is called when the permissions have been updated.
     *
     * @param permissionsState The current permission state.
     */
    fun onPermissionsUpdated(permissionsState: UiPermissionsState) {
        permissionsTracker.permissionsState = permissionsState
    }

    /**
     * This is called when the result comes back from requesting permissions from the user.
     *
     * @param permissionsState The new permission state.
     */
    fun onPermissionsResult(permissionsState: UiPermissionsState) {
        permissionsTracker.permissionsState = permissionsState
    }

    /**
     * Handle the user clicking on the 'Show limitations' button.
     */
    fun onLimitationsButtonClicked() {
        showLimitations.call()
    }

    /**
     * Handle the user clicking on the resolution button when an error state is being shown.
     */
    fun onResolveErrorButtonClicked() {
        when (uiStateFlow.value) {
            UiState.ERROR_LOCATION_DISABLED -> showLocationSettings.call()
            UiState.ERROR_PERMISSION_UNGRANTED -> permissionsTracker.onRequestPermissionsClicked()
            UiState.ERROR_PERMISSION_DENIED -> showAppSettings.call()
            UiState.ERROR_NO_BACKGROUND_LOCATION_PERMISSION ->
                permissionsTracker.onRequestBackgroundLocationPermissionClicked()
            else -> Unit
        }
    }

    /**
     * Handle the user clicking on the 'Add' button which signals their intent to add a new
     * proximity alert for the given stop identifier.
     *
     * @param meters The number of meters the user selected which is set as the bounds of the
     * proximity alert.
     */
    fun handleAddClicked(meters: Int) {
        stopIdentifier?.let {
            // Uses the application CoroutineScope as the Dialog dismisses immediately, and we need
            // this task to finish. Fire and forget is fine here.
            applicationCoroutineScope.launch(defaultDispatcher) {
                alertsRepository.addProximityAlert(
                    ProximityAlertRequest(it.toStopIdentifier(), meters)
                )
            }
        }
    }

    /**
     * Load the details for the given [stopIdentifier].
     *
     * @param stopIdentifier The stop to load details for.
     * @return A [kotlinx.coroutines.flow.Flow] containing the [StopDetails] (which emits new items
     * if the stop details change), or a [kotlinx.coroutines.flow.Flow] of `null` if the stop
     * identifier is `null`.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadStopDetails(stopIdentifier: StopIdentifier?) = if (stopIdentifier != null) {
        busStopsRepository.getNameForStopFlow(stopIdentifier)
            .mapLatest<StopName?, StopDetails?> {
                StopDetails(stopIdentifier, it)
            }
            .onStart {
                // Emit null as the first item to denote loading.
                emit(null)
            }
            .flowOn(defaultDispatcher)
    } else {
        flowOf(null)
    }
}
