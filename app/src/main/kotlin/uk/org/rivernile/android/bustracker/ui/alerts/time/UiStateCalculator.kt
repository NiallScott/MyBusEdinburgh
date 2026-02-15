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

package uk.org.rivernile.android.bustracker.ui.alerts.time

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
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
     * @param stopIdentifierFlow A [Flow] which emits the set stop identifier.
     * @param stopDetailsFlow A [Flow] which emits the latest [StopDetails]. A `null` value denotes
     * that the data is loading.
     * @param availableServicesFlow A [Flow] which emits the latest available services for the stop
     * code.
     * @param permissionsFlow A [Flow] which emits the current permission state.
     * @return A [Flow] of [UiState]s, which emits new items when relevant states change.
     */
    fun createUiStateFlow(
        stopIdentifierFlow: Flow<StopIdentifier?>,
        stopDetailsFlow: Flow<StopDetails?>,
        availableServicesFlow: Flow<List<ServiceDescriptor>?>,
        permissionsFlow: Flow<PermissionsState>
    ) = combine(
        stopIdentifierFlow,
        stopDetailsFlow,
        availableServicesFlow,
        permissionsFlow,
        this::calculateUiState
    ).distinctUntilChanged()

    /**
     * Calculate the current [UiState] based upon the state of other data streams.
     *
     * @param stopIdentifier The stop this pertains to.
     * @param stopDetails The current stop details.
     * @param availableServices The current [List] of available services.
     * @param permissionsState The current permissions state.
     * @return The calculated [UiState].
     */
    private fun calculateUiState(
        stopIdentifier: StopIdentifier?,
        stopDetails: StopDetails?,
        availableServices: List<ServiceDescriptor>?,
        permissionsState: PermissionsState
    ): UiState {
        val postNotificationPermissionState = permissionsState.postNotificationsPermission

        return when {
            stopIdentifier == null -> UiState.ERROR_NO_STOP_IDENTIFIER
            postNotificationPermissionState == PermissionState.DENIED ->
                UiState.ERROR_PERMISSION_DENIED
            postNotificationPermissionState == PermissionState.UNGRANTED ||
                    postNotificationPermissionState == PermissionState.SHOW_RATIONALE ->
                UiState.ERROR_PERMISSION_REQUIRED
            stopDetails == null || availableServices == null -> UiState.PROGRESS
            availableServices.isEmpty() -> UiState.ERROR_NO_SERVICES
            else -> UiState.CONTENT
        }
    }
}
