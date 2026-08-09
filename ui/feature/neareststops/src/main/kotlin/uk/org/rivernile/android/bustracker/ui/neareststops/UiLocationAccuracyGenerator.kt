/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.neareststops

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceRepository
import javax.inject.Inject

/**
 * This generates [UiLocationAccuracy] state so that the user can be warned of anything adverse
 * which is affecting the accuracy of their device locations.
 *
 * @author Niall Scott
 */
internal interface UiLocationAccuracyGenerator {

    /**
     * Get a [Flow] which emits [UiLocationAccuracy] state. A `null` state means there is no issue
     * with the accuracy.
     *
     * @param permissionsState The permissions state.
     * @return A [Flow] which emits [UiLocationAccuracy] state. A `null` state means there is no
     * issue with the accuracy.
     */
    fun getUiLocationAccuracyFlow(
        permissionsState: PermissionsState
    ): Flow<UiLocationAccuracy?>
}

internal class RealUiLocationAccuracyGenerator @Inject constructor(
    private val locationRepository: LocationRepository,
    private val preferenceRepository: PreferenceRepository
) : UiLocationAccuracyGenerator {

    override fun getUiLocationAccuracyFlow(
        permissionsState: PermissionsState
    ): Flow<UiLocationAccuracy?> {
        return locationRepository
            .isGpsLocationProviderEnabledFlow
            .combine(
                preferenceRepository.isGpsPromptDisabledFlow
            ) { isGpsProviderEnabled, isGpsPromptDisabled ->
                createUiLocationAccuracyOrNull(
                    isGpsPromptDisabled = isGpsPromptDisabled,
                    isDeviceGpsCapable = locationRepository.hasGpsLocationProvider,
                    permissionsState = permissionsState,
                    isGpsLocationProviderEnabled = isGpsProviderEnabled
                )
            }
    }

    private fun createUiLocationAccuracyOrNull(
        isGpsPromptDisabled: Boolean,
        isDeviceGpsCapable: Boolean,
        permissionsState: PermissionsState,
        isGpsLocationProviderEnabled: Boolean
    ): UiLocationAccuracy? {
        return when {
            isGpsPromptDisabled -> null
            !isDeviceGpsCapable -> UiLocationAccuracy.GPS_NOT_PRESENT
            permissionsState.fineLocationPermission != PermissionState.GRANTED ->
                UiLocationAccuracy.PERMISSIONS_NOT_SUFFICIENT
            !isGpsLocationProviderEnabled -> UiLocationAccuracy.GPS_DISABLED
            else -> null
        }
    }
}
