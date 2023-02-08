/*
 * Copyright (C) 2021 - 2023 Niall 'Rivernile' Scott
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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import javax.inject.Inject

/**
 * This is used to calculate the [UiState] for [AddProximityAlertDialogFragmentViewModel].
 *
 * The code in here used to live inside the View Model, but due to a problem with using [combine]
 * during testing when using a different dispatcher, it has been moved in to here to be able to
 * control test conditions.
 *
 * @param locationRepository The [LocationRepository], which is used to determine the location
 * capabilities of the device.
 * @author Niall Scott
 */
class UiStateCalculator @Inject constructor(
        private val locationRepository: LocationRepository) {

    /**
     * Create a [Flow] which emits [UiState]s based on other relevant states.
     *
     * @param permissionStateFlow A [Flow] which emits the latest [PermissionState] for the
     * required permissions.
     * @param stopDetailsFlow A [Flow] which emits the latest [StopDetails]. A `null` value denotes
     * that the data is loading or is not available.
     * @return A [Flow] of [UiState]s, which emits new items when relevant states change.
     */
    fun createUiStateFlow(
            permissionStateFlow: Flow<PermissionState>,
            stopDetailsFlow: Flow<StopDetails?>) =
            combine(
                    locationEnabledFlow,
                    permissionStateFlow,
                    stopDetailsFlow,
                    this::calculateUiState)
                    .distinctUntilChanged()

    /**
     * A [Flow] which emits the enabled state of device location services.
     *
     * If [LocationRepository.hasLocationFeature] returns `false`, this [Flow] will only emit a
     * single `false` value.
     */
    private val locationEnabledFlow get() = if (locationRepository.hasLocationFeature) {
        locationRepository.isLocationEnabledFlow
    } else {
        flowOf(false)
    }

    /**
     * Calculate the current [UiState] based upon the state of other data streams.
     *
     * @param locationEnabled Whether location services are currently enabled on the device or not.
     * @param permissionState The current permission state.
     * @param stopDetails The current stop details.
     * @return The calculated [UiState].
     */
    private fun calculateUiState(
            locationEnabled: Boolean,
            permissionState: PermissionState,
            stopDetails: StopDetails?) = when {
        !locationRepository.hasLocationFeature -> UiState.ERROR_NO_LOCATION_FEATURE
        permissionState == PermissionState.UNGRANTED -> UiState.ERROR_PERMISSION_UNGRANTED
        permissionState == PermissionState.DENIED -> UiState.ERROR_PERMISSION_DENIED
        !locationEnabled -> UiState.ERROR_LOCATION_DISABLED
        stopDetails == null -> UiState.PROGRESS
        else -> UiState.CONTENT
    }
}