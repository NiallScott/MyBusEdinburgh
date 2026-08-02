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

import uk.org.rivernile.android.bustracker.core.permission.PermissionState

/**
 * This enum captures the possible states regarding the accuracy of the device's location.
 *
 * @author Niall Scott
 */
internal enum class UiLocationAccuracy {

    /**
     * The GPS is not present on the device. So an error will be shown but the user is unable to
     * rectify the error.
     */
    GPS_NOT_PRESENT,
    /**
     * The permissions are not sufficient to allow for GPS level accuracy. So an error will be shown
     * and the user may be able to rectify this error.
     */
    PERMISSIONS_NOT_SUFFICIENT,
    /**
     * The GPS is present on the device and it is disabled. So the user is able to rectify this by
     * turning GPS on.
     */
    GPS_DISABLED
}

/**
 * Given some state, create the resulting [UiLocationAccuracy] or `null` if no action is to be
 * taken.
 *
 * @param isGpsPromptDisabled Has the GPS prompt been disabled by the user?
 * @param isDeviceGpsCapable Is the device capable of receiving GPS locations? This is distinct from
 * whether this functionality is currently active.
 * @param permissionsState The state of the necessary permissions.
 * @param isGpsLocationProviderEnabled Is the system GPS location provider enabled?
 * @return The resulting [UiLocationAccuracy] or `null` if no action is to be taken.
 */
internal fun createUiLocationAccuracyOrNull(
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
