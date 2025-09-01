/*
 * Copyright (C) 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.alerts.proximity

/**
 * Handles the situation when proximity alerts are unable to be checked because the system is
 * preventing us from doing so.
 *
 * Because proximity alerts are time-sensitive, when we are prevented from checking them, it is
 * better to remove the alert and inform the user that the alert is removed. That way, the user
 * won't have alerts silently go unchecked or missing. And it can prompt them to re-add the alert.
 *
 * @author Niall Scott
 */
interface UnableToRunProximityAlertsHandler {

    /**
     * Handle the situation when proximity alerts are unable to be checked because the system is
     * preventing us from doing so.
     */
    fun handleUnableToRunProximityAlerts()
}
