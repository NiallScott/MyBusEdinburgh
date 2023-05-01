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

import uk.org.rivernile.android.bustracker.core.alerts.ProximityAlert
import uk.org.rivernile.android.bustracker.core.database.busstop.daos.BusStopsDao
import uk.org.rivernile.android.bustracker.core.utils.TimeUtils
import javax.inject.Inject

/**
 * This deals with the tracking and untracking of proximity alerts.
 *
 * @param busStopsDao Used to access the bus stop data store.
 * @param geofencingManager Used to set and remove geofences.
 * @param timeUtils Used to obtain timestamps.
 * @author Niall Scott
 */
internal class ProximityAlertTracker @Inject constructor(
    private val busStopsDao: BusStopsDao,
    private val geofencingManager: GeofencingManager,
    private val timeUtils: TimeUtils) {

    companion object {

        private const val MAX_DURATION_MILLIS = 3600000L
    }

    /**
     * Given a newly detected [ProximityAlert], track it by adding it to the [GeofencingManager].
     *
     * @param alert The alert to track.
     */
    suspend fun trackProximityAlert(alert: ProximityAlert) {
        busStopsDao.getLocationForStop(alert.stopCode)
            ?.let {
                val duration = alert.timeAdded + MAX_DURATION_MILLIS - timeUtils.currentTimeMills

                if (duration > 0) {
                    geofencingManager.addGeofence(
                        alert.id,
                        it.latitude,
                        it.longitude,
                        alert.distanceFrom.toFloat(),
                        duration)
                }
            }
    }

    /**
     * Remove a proximity alert.
     *
     * @param id The ID of the parameter.
     */
    fun removeProximityAlert(id: Int) {
        geofencingManager.removeGeofence(id)
    }
}