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

package uk.org.rivernile.android.bustracker.core.alerts.proximity

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.alerts.ProximityAlert
import javax.inject.Inject

/**
 * This implementation runs the tracking of proximity alerts.
 *
 * @param alertsRepository The repository to access the currently set proximity alerts.
 * @param proximityAlertTracker Proximity alerts to begin or stop tracking are managed through this.
 * @author Niall Scott
 */
class ManageProximityAlertsRunner @Inject internal constructor(
    private val alertsRepository: AlertsRepository,
    private val proximityAlertTracker: ProximityAlertTracker
) {

    /**
     * Run this [ManageProximityAlertsRunner]. This responds to changes in the set proximity alerts
     * and propagates out starting and stopping of proximity alerts. If the number of proximity
     * alerts is less than 1, a [CancellationException] will be thrown.
     */
    suspend fun run() {
        alertsRepository
            .allProximityAlertsFlow
            .manageProximityAlerts()
            .collect {
                if (it < 1) {
                    throw CancellationException()
                }
            }
    }

    /**
     * Manages the set proximity alerts by consuming the [Flow] of [ProximityAlert]s and adding and
     * removing proximity alerts as appropriate. Upon cancellation, all set proximity alerts will
     * be removed.
     *
     * The [Flow] will emit the current number of [ProximityAlert]s being tracked.
     *
     * @return A [Flow] which manages the set [ProximityAlert]s.
     */
    private fun Flow<List<ProximityAlert>?>.manageProximityAlerts() = flow {
        val trackedAlerts = mutableMapOf<Int, ProximityAlert>()

        try {
            collect { proximityAlerts ->
                val alertsMap = proximityAlerts?.associateBy { it.id } ?: emptyMap()
                val toAdd = alertsMap.toMutableMap().apply {
                    keys.removeAll(trackedAlerts.keys)
                }
                val toRemove = trackedAlerts.keys.toMutableSet().apply {
                    removeAll(alertsMap.keys)
                }

                trackedAlerts.keys.removeAll(toRemove)
                trackedAlerts.putAll(toAdd)

                toAdd.values.forEach {
                    proximityAlertTracker.trackProximityAlert(it)
                }

                toRemove.forEach(proximityAlertTracker::removeProximityAlert)

                emit(trackedAlerts.size)
            }
        } finally {
            // Upon any termination reason (e.g. cancellation), make sure we cancel all set alerts.
            trackedAlerts.keys.forEach(proximityAlertTracker::removeProximityAlert)
        }
    }
}