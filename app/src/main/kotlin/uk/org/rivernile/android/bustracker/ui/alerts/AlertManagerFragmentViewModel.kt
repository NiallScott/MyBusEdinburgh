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

package uk.org.rivernile.android.bustracker.ui.alerts

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.utils.SingleLiveEvent
import javax.inject.Inject

/**
 * This is the [ViewModel] for [AlertManagerFragment].
 *
 * @param alertsRetriever Used to retrieve the [UiAlert]s for display on the UI.
 * @param defaultDispatcher The default [CoroutineDispatcher].
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
class AlertManagerFragmentViewModel @Inject constructor(
        alertsRetriever: AlertsRetriever,
        @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher) : ViewModel() {

    private val alertsFlow = alertsRetriever.allAlertsFlow
            .flowOn(defaultDispatcher)
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 1)

    /**
     * This [LiveData] emits the current [UiAlert] items to present as a list on the UI.
     */
    val alertsLiveData = alertsFlow.asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits the current [UiState].
     */
    val uiStateLiveData = alertsFlow
            .map(this::calculateUiState)
            .asLiveData(viewModelScope.coroutineContext + defaultDispatcher)

    /**
     * When this [LiveData] emits a new item, the system location settings screen should be shown.
     */
    val showLocationSettingsLiveData: LiveData<Nothing> get() = showLocationSettings
    private val showLocationSettings = SingleLiveEvent<Nothing>()

    /**
     * When this [LiveData] emits a new item, the user should be prompted to remove the arrival
     * alert for the given stop code.
     */
    val showRemoveArrivalAlertLiveData: LiveData<String> get() = showRemoveArrivalAlert
    private val showRemoveArrivalAlert = SingleLiveEvent<String>()

    /**
     * When this [LiveData] emits a new item, the user should be prompted to remove the proximity
     * alert for the given stop code.
     */
    val showRemoveProximityAlertLiveData: LiveData<String> get() = showRemoveProximityAlert
    private val showRemoveProximityAlert = SingleLiveEvent<String>()

    /**
     * Handle the user clicking on the button to invoke the system location settings.
     */
    fun onShowLocationSettingsClicked() {
        showLocationSettings.call()
    }

    /**
     * Handle the user clicking on the 'Remove' button on an arrival alert item.
     *
     * @param stopCode The stop code for the arrival alert.
     */
    fun onRemoveArrivalAlertClicked(stopCode: String) {
        showRemoveArrivalAlert.value = stopCode
    }

    /**
     * Handle the user clicking on the 'Remove' button on a proximity alert item.
     *
     * @param stopCode The stop code for the proximity alert.
     */
    fun onRemoveProximityAlertClicked(stopCode: String) {
        showRemoveProximityAlert.value = stopCode
    }

    /**
     * Given a [List] of [UiAlert]s, calculate the current [UiState].
     *
     * @param alerts The [List] of [UiAlert] to use as the basis of the calculation.
     * @return The calculated [UiState].
     */
    private fun calculateUiState(alerts: List<UiAlert>?) = when {
        alerts == null -> UiState.PROGRESS
        alerts.isEmpty() -> UiState.ERROR
        else -> UiState.CONTENT
    }
}