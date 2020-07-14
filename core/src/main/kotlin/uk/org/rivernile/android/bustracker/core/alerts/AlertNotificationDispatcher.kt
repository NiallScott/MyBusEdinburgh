/*
 * Copyright (C) 2019 - 2020 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.alerts

import uk.org.rivernile.android.bustracker.core.database.settings.entities.ArrivalAlert
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ProximityAlert
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.Service

/**
 * This is used to dispatch user notifications for when an app alert has been triggered.
 *
 * @author Niall Scott
 */
interface AlertNotificationDispatcher {

    /**
     * Dispatch a new time alert notification to show to the user.
     *
     * @param arrivalAlert The [ArrivalAlert] that caused the notification.
     * @param qualifyingServices What services caused the notification to be fired.
     */
    fun dispatchTimeAlertNotification(arrivalAlert: ArrivalAlert, qualifyingServices: List<Service>)

    /**
     * Dispatch a new proximity alert notification to show to the user.
     *
     * @param proximityAlert The [ProximityAlert] that caused the notification.
     */
    fun dispatchProximityAlertNotification(proximityAlert: ProximityAlert)
}