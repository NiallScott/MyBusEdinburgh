/*
 * Copyright (C) 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.busstopmap

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.core.utils.TimeUtils
import javax.inject.Inject

/**
 * This class handles permission state for [BusStopMapViewModel].
 *
 * @param savedState The [SavedStateHandle].
 * @param locationRepository The location repository.
 * @param timeUtils Time utils.
 * @author Niall Scott
 */
@ViewModelScoped
class PermissionHandler @Inject constructor(
        private val savedState: SavedStateHandle,
        private val locationRepository: LocationRepository,
        private val timeUtils: TimeUtils) {

    companion object {

        private const val STATE_REQUESTED_LOCATION_PERMISSIONS = "requestedLocationPermissions"
    }

    /**
     * The current state of permissions pertaining to this view.
     */
    var permissionsState: PermissionsState
        get() = mutablePermissionsStateFlow.value ?: PermissionsState()
        set(value) {
            mutablePermissionsStateFlow.value = value
            handlePermissionsSet(value)
        }

    /**
     * This [Flow] emits the current permissions state.
     */
    val permissionsStateFlow: Flow<PermissionsState?> get() = mutablePermissionsStateFlow
    private val mutablePermissionsStateFlow = MutableStateFlow<PermissionsState?>(null)

    /**
     * This [Flow] emits new items when permissions should be requested from the user.
     */
    val requestLocationPermissionsFlow: Flow<Long> get() =
        requestLocationPermissions.filterNotNull()
    private val requestLocationPermissions = MutableStateFlow<Long?>(null)

    /**
     * Handle the permissions being updated. The logic in here determines if the user should be
     * asked to grant permission(s).
     *
     * @param permissionsState The newly-set [PermissionsState].
     */
    private fun handlePermissionsSet(permissionsState: PermissionsState) {
        val requestedPermissions: Boolean? = savedState[STATE_REQUESTED_LOCATION_PERMISSIONS]

        if (requestedPermissions != true) {
            savedState[STATE_REQUESTED_LOCATION_PERMISSIONS] = true

            if (locationRepository.hasLocationFeature &&
                    permissionsState.fineLocationPermission == PermissionState.UNGRANTED &&
                    permissionsState.coarseLocationPermission == PermissionState.UNGRANTED) {
                requestLocationPermissions.value = timeUtils.getCurrentTimeMillis()
            }
        }
    }
}