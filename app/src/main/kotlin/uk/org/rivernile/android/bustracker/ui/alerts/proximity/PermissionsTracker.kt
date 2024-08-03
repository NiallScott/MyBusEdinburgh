/*
 * Copyright (C) 2023 - 2024 Niall 'Rivernile' Scott
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
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.utils.SingleLiveEvent
import javax.inject.Inject

/**
 * This is used to track the permission state for [AddProximityAlertDialogFragment].
 *
 * @param savedStateHandle The saved state to obtain state from previous instances.
 * @author Niall Scott
 */
@ViewModelScoped
class PermissionsTracker @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) {

    companion object {

        private const val STATE_REQUESTED_PERMISSIONS = "requestedPermissions"
        private const val STATE_REQUESTED_BACKGROUND_LOCATION_PERMISSION =
            "requestedBackgroundLocationPermission"
    }

    /**
     * The current permission state from the perspective of the UI.
     */
    var permissionsState: UiPermissionsState
        get() = _permissionsStateFlow.value
        set(value) {
            _permissionsStateFlow.value = value
        }

    /**
     * A [kotlinx.coroutines.flow.Flow] which emits the current [PermissionState], so that other
     * components can modify their behaviour.
     */
    val permissionsStateFlow get() = _permissionsStateFlow
        .combine(
            savedStateHandle.getStateFlow(STATE_REQUESTED_PERMISSIONS, false),
            this::mapToPermissionsState
        )

    /**
     * A [kotlinx.coroutines.flow.Flow] which emits whether the background location permission is
     * granted or not.
     */
    val backgroundLocationPermissionStateFlow get() = _permissionsStateFlow
        .map { it.hasBackgroundLocationPermission }
        .combine(
            savedStateHandle.getStateFlow(STATE_REQUESTED_BACKGROUND_LOCATION_PERMISSION, false),
            this::mapToBackgroundLocationPermissionsState
        )

    /**
     * A [LiveData] which emits when permissions should be requested from the user.
     */
    val requestPermissionsLiveData: LiveData<Unit> get() = requestPermissions
    private val requestPermissions = SingleLiveEvent<Unit>()

    /**
     * A [LiveData] which emits when the background location permission should be requested from
     * the user.
     */
    val requestBackgroundLocationPermissionLiveData: LiveData<Unit>
        get() = requestBackgroundLocation
    private val requestBackgroundLocation = SingleLiveEvent<Unit>()

    private val _permissionsStateFlow = MutableStateFlow(UiPermissionsState())

    /**
     * This is called when the user has clicked the button to request permissions.
     */
    fun onRequestPermissionsClicked() {
        if (savedStateHandle.get<Boolean>(STATE_REQUESTED_PERMISSIONS) != true) {
            savedStateHandle[STATE_REQUESTED_PERMISSIONS] = true
            requestPermissions.call()
        }
    }

    /**
     * This is called when the user has clicked the button to request the background location
     * permission.
     */
    fun onRequestBackgroundLocationPermissionClicked() {
        if (savedStateHandle.get<Boolean>(STATE_REQUESTED_BACKGROUND_LOCATION_PERMISSION) != true) {
            savedStateHandle[STATE_REQUESTED_BACKGROUND_LOCATION_PERMISSION] = true
            requestBackgroundLocation.call()
        }
    }

    /**
     * Given the UI permissions state and the current state of whether the permissions have been
     * requested from the user, map this to a [PermissionState].
     *
     * @param uiPermissionsState The current [UiPermissionsState].
     * @param requestedPermissions Whether the permissions have already been requested from the user
     * or not.
     * @return The resulting [PermissionState].
     */
    private fun mapToPermissionsState(
        uiPermissionsState: UiPermissionsState,
        requestedPermissions: Boolean?
    ): PermissionState {
        return when {
            uiPermissionsState.hasFineLocationPermission &&
                    uiPermissionsState.hasPostNotificationsPermission -> PermissionState.GRANTED
            requestedPermissions != true -> PermissionState.UNGRANTED
            else -> PermissionState.DENIED
        }
    }

    /**
     * Given the background location permission state, and the state of whether this has already
     * been requested from the user in this session, map this to a [PermissionState].
     *
     * @param isBackgroundLocationPermissionGranted Is background location permission granted?
     * @param requestedPermissions Whether the permission has already been requested from the user
     * or not.
     * @return The resulting [PermissionState].
     */
    private fun mapToBackgroundLocationPermissionsState(
        isBackgroundLocationPermissionGranted: Boolean,
        requestedPermissions: Boolean?
    ): PermissionState {
        return when {
            isBackgroundLocationPermissionGranted -> PermissionState.GRANTED
            requestedPermissions != true -> PermissionState.UNGRANTED
            else -> PermissionState.DENIED
        }
    }
}