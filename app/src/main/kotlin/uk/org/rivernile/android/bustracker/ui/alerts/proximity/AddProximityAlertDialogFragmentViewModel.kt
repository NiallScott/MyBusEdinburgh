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

package uk.org.rivernile.android.bustracker.ui.alerts.proximity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.alerts.proximity.ProximityAlertRequest
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopName
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.di.ForGlobalCoroutineScope
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.utils.SingleLiveEvent
import javax.inject.Inject

/**
 * This is the [ViewModel] for [AddProximityAlertDialogFragment].
 *
 * @param busStopsRepository Used to get stop details.
 * @param uiStateCalculator Used to calculate the current [UiState].
 * @param alertsRepository Used to add the proximity alert.
 * @param globalCoroutineScope The [CoroutineScope] to add the alert under.
 * @param defaultDispatcher The default [CoroutineDispatcher].
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
class AddProximityAlertDialogFragmentViewModel @Inject constructor(
        private val busStopsRepository: BusStopsRepository,
        private val uiStateCalculator: UiStateCalculator,
        private val alertsRepository: AlertsRepository,
        @ForGlobalCoroutineScope private val globalCoroutineScope: CoroutineScope,
        @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher) : ViewModel() {

    /**
     * This property is used to get and set the stop code the proximity alert should be added for.
     */
    var stopCode: String?
        get() = stopCodeFlow.value
        set(value) {
            stopCodeFlow.value = value
        }

    /**
     * The UI layer calls this method to update the status of the location permission.
     */
    var locationPermissionState: PermissionState
        get() = locationPermissionStateFlow.value
        set(value) {
            locationPermissionStateFlow.value = value
        }

    private val stopCodeFlow = MutableStateFlow<String?>(null)
    private val locationPermissionStateFlow = MutableStateFlow(PermissionState.UNGRANTED)

    private val stopDetailsFlow = stopCodeFlow.flatMapLatest {
        loadStopDetails(it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    /**
     * This [LiveData] emits the current [StopDetails] for the given stop code.
     */
    val stopDetailsLiveData = stopDetailsFlow.asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits the current [UiState].
     */
    val uiStateLiveData by lazy {
        uiStateCalculator.createUiStateFlow(
                locationPermissionStateFlow,
                stopDetailsFlow)
                .asLiveData(viewModelScope.coroutineContext + defaultDispatcher)
    }

    /**
     * This [LiveData] emits the enabled state of the 'Add' button.
     */
    val addButtonEnabledLiveData by lazy {
        uiStateLiveData.map {
            it == UiState.CONTENT
        }.distinctUntilChanged()
    }

    /**
     * When this [LiveData] emits a new item, the limitations dialog should be shown.
     */
    val showLimitationsLiveData: LiveData<Nothing> get() = showLimitations
    private val showLimitations = SingleLiveEvent<Nothing>()

    /**
     * When this [LiveData] emits a new item, the system location settings screen should be shown.
     */
    val showLocationSettingsLiveData: LiveData<Nothing> get() = showLocationSettings
    private val showLocationSettings = SingleLiveEvent<Nothing>()

    /**
     * When this [LiveData] emits a new item, the request location permission system dialog should
     * be shown.
     */
    val requestLocationPermissionLiveData: LiveData<Nothing> get() = requestLocationPermission
    private val requestLocationPermission = SingleLiveEvent<Nothing>()

    /**
     * When this [LiveData] emits a new item, the app settings screen should be shown.
     */
    val showAppSettingsLiveData: LiveData<Nothing> get() = showAppSettings
    private val showAppSettings = SingleLiveEvent<Nothing>()

    /**
     * Handle the user clicking on the 'Show limitations' button.
     */
    fun onLimitationsButtonClicked() {
        showLimitations.call()
    }

    /**
     * Handle the user clicking on the resolution button when an error state is being shown.
     */
    @Suppress("NON_EXHAUSTIVE_WHEN") // We don't respond to all states.
    fun onResolveErrorButtonClicked() {
        when (uiStateLiveData.value) {
            UiState.ERROR_LOCATION_DISABLED -> showLocationSettings.call()
            UiState.ERROR_PERMISSION_UNGRANTED -> requestLocationPermission.call()
            UiState.ERROR_PERMISSION_DENIED -> showAppSettings.call()
        }
    }

    /**
     * Handle the user clicking on the 'Add' button which signals their intent to add a new
     * proximity alert for the given stop code.
     *
     * @param meters The number of meters the user selected which is set as the bounds of the
     * proximity alert.
     */
    fun handleAddClicked(meters: Int) {
        stopCode?.ifEmpty { null }?.let {
            // Uses the global CoroutineScope as the Dialog dismisses immediately, and we need
            // this task to finish. Fire and forget is fine here.
            globalCoroutineScope.launch(defaultDispatcher) {
                alertsRepository.addProximityAlert(ProximityAlertRequest(it, meters))
            }
        }
    }

    /**
     * Load the details for the given [stopCode].
     *
     * @param stopCode The stop code to load details for.
     * @return A [kotlinx.coroutines.flow.Flow] containing the [StopDetails] (which emits new items
     * if the stop details change), or a [kotlinx.coroutines.flow.Flow] of `null` if the stop
     * code is `null` or empty.
     */
    private fun loadStopDetails(stopCode: String?) = if (stopCode?.isNotEmpty() == true) {
        busStopsRepository.getNameForStopFlow(stopCode)
                .mapLatest<StopName?, StopDetails?> {
                    StopDetails(stopCode, it)
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