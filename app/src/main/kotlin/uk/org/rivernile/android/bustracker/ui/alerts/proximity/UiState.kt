/*
 * Copyright (C) 2020 - 2024 Niall 'Rivernile' Scott
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

/**
 * This is the enumeration of possible top-level UI states for
 * [AddProximityAlertDialogFragmentViewModel].
 *
 * @author Niall Scott
 */
enum class UiState {

    /**
     * The device does not have any location service features.
     */
    ERROR_NO_LOCATION_FEATURE,
    /**
     * The device location services are disabled. This needs to be rectified by the user before
     * continuing.
     */
    ERROR_LOCATION_DISABLED,
    /**
     * The user needs to grant us location permission before we can continue.
     */
    ERROR_PERMISSION_UNGRANTED,
    /**
     * The user has fully denied us the location permission and this can only be rectified from
     * system settings.
     */
    ERROR_PERMISSION_DENIED,
    /**
     * We do not have background location permission.
     */
    ERROR_NO_BACKGROUND_LOCATION_PERMISSION,
    /**
     * Show progress while stop details are loading.
     */
    PROGRESS,
    /**
     * Show the regular content view.
     */
    CONTENT
}