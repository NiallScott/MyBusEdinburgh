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

package uk.org.rivernile.android.bustracker.ui.alerts.time

/**
 * This is the enumeration of possible top-level UI states for
 * [AddTimeAlertDialogFragmentViewModel].
 *
 * @author Niall Scott
 */
enum class UiState {

    /**
     * There was no stop code set.
     */
    ERROR_NO_STOP_CODE,
    /**
     * There are no known services for the stop.
     */
    ERROR_NO_SERVICES,
    /**
     * There are insufficient permissions enabled for this feature to work and the user should be
     * asked to grant them.
     */
    ERROR_PERMISSION_REQUIRED,
    /**
     * There are insufficient permissions enabled for this feature to work and the user has denied
     * us the permissions.
     */
    ERROR_PERMISSION_DENIED,
    /**
     * Show progress while required content is loading.
     */
    PROGRESS,
    /**
     * Show the content layout.
     */
    CONTENT
}