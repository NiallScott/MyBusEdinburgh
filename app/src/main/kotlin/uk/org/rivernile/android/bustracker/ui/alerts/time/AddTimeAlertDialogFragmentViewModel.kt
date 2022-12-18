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

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.ArrivalAlertRequest
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.di.ForApplicationCoroutineScope
import uk.org.rivernile.android.bustracker.core.servicestops.ServiceStopsRepository
import uk.org.rivernile.android.bustracker.utils.SingleLiveEvent
import javax.inject.Inject

/**
 * This is the [ViewModel] for [AddTimeAlertDialogFragment].
 *
 * @param savedState The saved state to obtain state from previous instances.
 * @param busStopsRepository Used to get stop details.
 * @param serviceStopsRepository Used to get the services for the selected stop code.
 * @param uiStateCalculator Used to calculate the current [UiState].
 * @param alertsRepository Used to add the arrival alert.
 * @param applicationCoroutineScope The [CoroutineScope] to add the alert under.
 * @param defaultDispatcher The default [CoroutineDispatcher].
 * @author Niall Scott
 */
@HiltViewModel
class AddTimeAlertDialogFragmentViewModel @Inject constructor(
        private val savedState: SavedStateHandle,
        private val busStopsRepository: BusStopsRepository,
        private val serviceStopsRepository: ServiceStopsRepository,
        uiStateCalculator: UiStateCalculator,
        private val alertsRepository: AlertsRepository,
        @ForApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
        @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher) : ViewModel() {

    companion object {

        /**
         * This constant contains the key where the stop code is stored in the saved state.
         */
        const val STATE_STOP_CODE = "stopCode"

        /**
         * This constant contains the key where the selected services are stored in the saved state.
         */
        const val STATE_SELECTED_SERVICES = "selectedServices"
    }

    /**
     * This property is used to get and set the stop code the arrival alert alert should be added
     * for.
     */
    var stopCode: String?
        get() = savedState[STATE_STOP_CODE]
        set(value) {
            savedState[STATE_STOP_CODE] = value
        }

    /**
     * This property is used to get and set the current selected services.
     */
    var selectedServices: List<String>?
        get() = selectedServicesLiveData.value
        set(value) {
            savedState[STATE_SELECTED_SERVICES] = value?.ifEmpty { null }?.toTypedArray()
        }

    private val stopCodeFlow = savedState.getStateFlow<String?>(STATE_STOP_CODE, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val stopDetailsFlow = stopCodeFlow.flatMapLatest {
        loadStopDetails(it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val availableServicesFlow = stopCodeFlow.flatMapLatest {
        loadServicesForStop(it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    /**
     * This [LiveData] emits the user's selected services.
     */
    val selectedServicesLiveData =
            savedState.getLiveData<Array<String>?>(STATE_SELECTED_SERVICES, null).map {
                it?.ifEmpty { null }?.asList()
            }

    /**
     * This [LiveData] emits the current [StopDetails] for the given stop code.
     */
    val stopDetailsLiveData = stopDetailsFlow.asLiveData(viewModelScope.coroutineContext)

    private val uiStateFlow = uiStateCalculator.createUiStateFlow(
            stopCodeFlow,
            stopDetailsFlow,
            availableServicesFlow)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), UiState.ERROR_NO_STOP_CODE)
    /**
     * This [LiveData] emits the current [UiState].
     */
    val uiStateLiveData =
            uiStateFlow.asLiveData(viewModelScope.coroutineContext + defaultDispatcher)

    /**
     * This [LiveData] emits the enabled state of the 'Add' button.
     */
    val addButtonEnabledLiveData =
            uiStateFlow.combine(selectedServicesLiveData.asFlow()) { uiState, ss ->
                uiState == UiState.CONTENT && ss?.isNotEmpty() == true
            }.distinctUntilChanged().asLiveData(viewModelScope.coroutineContext)

    /**
     * When this [LiveData] emits a new item, the limitations dialog should be shown.
     */
    val showLimitationsLiveData: LiveData<Unit> get() = showLimitations
    private val showLimitations = SingleLiveEvent<Unit>()

    /**
     * When this [LiveData] emits a new item, the services chooser should be shown. The data that is
     * emitted is the parameters which should be passed to the chooser UI.
     */
    val showServicesChooserLiveData: LiveData<ServicesChooserParams> get() = showServicesChooser
    private val showServicesChooser = SingleLiveEvent<ServicesChooserParams>()

    /**
     * Handle the user clicking on the 'Show limitations' button.
     */
    fun onLimitationsButtonClicked() {
        showLimitations.call()
    }

    /**
     * This is called when the user has clicked on the button to select services.
     */
    fun onSelectServicesClicked() {
        availableServicesFlow.value?.ifEmpty { null }?.let {
            showServicesChooser.value = ServicesChooserParams(it, selectedServices)
        }
    }

    /**
     * Handle the user clicking on the 'Add' button which signals their intent to add a new arrival
     * alert for the given stop code.
     *
     * @param timeTrigger The number of minutes to be used as the time trigger.
     */
    fun onAddClicked(timeTrigger: Int) {
        stopCode?.ifEmpty { null }?.let { sc ->
            selectedServices?.ifEmpty { null }?.let { ss ->
                // Uses the application CoroutineScope as the Dialog dismisses immediately, and we
                // need this task to finish. Fire and forget is fine here.
                applicationCoroutineScope.launch(defaultDispatcher) {
                    alertsRepository.addArrivalAlert(ArrivalAlertRequest(sc, ss, timeTrigger))
                }
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
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadStopDetails(stopCode: String?) = if (stopCode?.isNotEmpty() == true) {
        busStopsRepository.getNameForStopFlow(stopCode)
                .mapLatest {
                    StopDetails(stopCode, it)
                }
                .onStart {
                    // Emit just the stop code initially. If the name is available, it should
                    // asynchronously follow behind.
                    emit(StopDetails(stopCode, null))
                }
                .distinctUntilChanged()
                .flowOn(defaultDispatcher)
    } else {
        flowOf(null)
    }

    /**
     * Load the services for the given [stopCode].
     *
     * @param stopCode The stop code to load services for.
     * @return A [kotlinx.coroutines.flow.Flow] containing the [List] of services (which emits new
     * items if the data changes), or a [kotlinx.coroutines.flow.Flow] of `null` if the stop code is
     * `null` or empty.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadServicesForStop(stopCode: String?) = if (stopCode?.isNotEmpty() == true) {
        serviceStopsRepository.getServicesForStopFlow(stopCode)
                .mapLatest<List<String>?, List<String>?> {
                    // If upstream gives us a null, convert it to an emptyList() as null denotes
                    // loading here.
                    it ?: emptyList()
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