/*
 * Copyright (C) 2022 - 2024 Niall 'Rivernile' Scott
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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import javax.inject.Inject

/**
 * This is used to detect whether the My Location feature should be enabled on the map.
 *
 * @param locationRepository Used to access location-related data.
 * @author Niall Scott
 */
class IsMyLocationEnabledDetector @Inject constructor(
    private val locationRepository: LocationRepository
) {

    /**
     * Get a [Flow] which emits whether the My Location feature should be enabled or not.
     *
     * @param permissionStateFlow A [Flow] which contains the current [PermissionsState].
     * @return A [Flow] which emits whether the My Location feature should be enabled or not.
     */
    fun getIsMyLocationFeatureEnabledFlow(
            permissionStateFlow: Flow<PermissionsState>
    ): Flow<Boolean> {
        return if (locationRepository.hasLocationFeature) {
            permissionStateFlow.combine(
                locationRepository.isLocationEnabledFlow,
                this::calculateIsMyLocationFeatureEnabled)
                .distinctUntilChanged()
        } else {
            flowOf(false)
        }
    }

    /**
     * Based on the current permission state and the availability of the system location services,
     * determine if the My Location feature is enabled or not.
     *
     * @param permissionState The current state of the permissions.
     * @param isLocationEnabled Is the system location services available?
     * @return `true` if the My Location feature should be enabled, otherwise `false`.
     */
    private fun calculateIsMyLocationFeatureEnabled(
            permissionState: PermissionsState,
            isLocationEnabled: Boolean
    ) = isLocationEnabled &&
            (permissionState.coarseLocationPermission == PermissionState.GRANTED ||
                    permissionState.fineLocationPermission == PermissionState.GRANTED)
}