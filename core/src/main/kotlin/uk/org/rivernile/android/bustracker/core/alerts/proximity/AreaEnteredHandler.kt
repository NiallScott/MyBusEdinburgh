/*
 * Copyright (C) 2020 - 2023 Niall 'Rivernile' Scott
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

import uk.org.rivernile.android.bustracker.core.alerts.AlertNotificationDispatcher
import uk.org.rivernile.android.bustracker.core.database.settings.daos.AlertsDao
import javax.inject.Inject

/**
 * This class contains the business logic for handling a proximity are being entered.
 *
 * @param alertsDao The DAO to access set alerts.
 * @param geofencingManager The geofencing implementation used.
 * @param notificationDispatcher Used to dispatch the notification to the user.
 * @author Niall Scott
 */
class AreaEnteredHandler @Inject internal constructor(
        private val alertsDao: AlertsDao,
        private val geofencingManager: GeofencingManager,
        private val notificationDispatcher: AlertNotificationDispatcher) {

    /**
     * Handle the area being entered.
     *
     * @param alertId The ID of the alert which triggered this method being called.
     */
    suspend fun handleAreaEntered(alertId: Int) {
        alertsDao.getProximityAlert(alertId)
            ?.let {
                notificationDispatcher.dispatchProximityAlertNotification(it)
            }

        geofencingManager.removeGeofence(alertId)
        alertsDao.removeProximityAlert(alertId)
    }
}