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

package uk.org.rivernile.android.bustracker.core.permission

/**
 * This enumeration encapsulates the permission states.
 *
 * @author Niall Scott
 */
enum class PermissionState {

    /**
     * The permission has been granted to the app and we're free to use the capabilities it enables.
     */
    GRANTED,
    /**
     * Show the user the permission rationale before asking them to grant us the permission.
     */
    SHOW_RATIONALE,
    /**
     * The permission has not yet been granted to us.
     */
    UNGRANTED,
    /**
     * The user has denied the permission to us and we're unable to ask the user for the permission
     * again using the normal method. The only way for the user to now grant it is in the system
     * settings.
     */
    DENIED
}