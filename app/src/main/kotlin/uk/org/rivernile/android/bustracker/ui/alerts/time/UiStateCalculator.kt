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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

/**
 * This is used to calculate the [UiState] for [AddTimeAlertDialogFragmentViewModel].
 *
 * @author Niall Scott
 */
class UiStateCalculator @Inject constructor() {

    /**
     * Create a [Flow] which emits [UiState]s based on other relevant states.
     *
     * @param stopCodeFlow A [Flow] which emits the set stop code.
     * @param stopDetailsFlow A [Flow] which emits the latest [StopDetails]. A `null` value denotes
     * that the data is loading.
     * @param availableServicesFlow A [Flow] which emits the latest available services for the stop
     * code.
     * @return A [Flow] of [UiState]s, which emits new items when relevant states change.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun createUiStateFlow(
            stopCodeFlow: Flow<String?>,
            stopDetailsFlow: Flow<StopDetails?>,
            availableServicesFlow: Flow<List<String>?>) =
            combine(
                    stopCodeFlow,
                    stopDetailsFlow,
                    availableServicesFlow) { stopCode, stopDetails, availableServices ->
                CurrentState(stopCode, stopDetails, availableServices)
            }.mapLatest {
                calculateUiState(it)
            }.distinctUntilChanged()

    /**
     * Based on the current state of various elements described in [CurrentState], calculate the
     * [UiState].
     *
     * @param currentState The current state to calculate the [UiState] from.
     * @return The calculated [UiState].
     */
    private fun calculateUiState(currentState: CurrentState) = when {
        currentState.stopCode?.ifEmpty { null } == null -> UiState.ERROR_NO_STOP_CODE
        currentState.stopDetails == null || currentState.availableServices == null ->
            UiState.PROGRESS
        currentState.availableServices.isEmpty() -> UiState.ERROR_NO_SERVICES
        else -> UiState.CONTENT
    }

    /**
     * Instances of this data class hold the current state of properties which are used to calculate
     * the [UiState].
     *
     * @property stopCode The stop code.
     * @property stopDetails The loaded stop details.
     * @property availableServices The loaded [List] of available services.
     */
    private data class CurrentState(
            val stopCode: String?,
            val stopDetails: StopDetails?,
            val availableServices: List<String>?)
}