/*
 * Copyright (C) 2023 Niall 'Rivernile' Scott
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
 * This class encapsulates the permissions that the Add Proximity Alert dialog would like to have,
 * and their current state, from the UI.
 *
 * @property hasCoarseLocationPermission Do we have coarse location permission?
 * @property hasFineLocationPermission Do we have fine location permission?
 * @property hasPostNotificationsPermission Do we have the post notification permission?
 * @author Niall Scott
 */
data class UiPermissionsState(
        val hasCoarseLocationPermission: Boolean = false,
        val hasFineLocationPermission: Boolean = false,
        val hasPostNotificationsPermission: Boolean = false)