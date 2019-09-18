/*
 * Copyright (C) 2019 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.alerts.arrivals

import uk.org.rivernile.android.bustracker.core.alerts.AlertNotificationDispatcher
import uk.org.rivernile.android.bustracker.core.database.settings.daos.AlertsDao
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ArrivalAlert
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.TrackerEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.TrackerException
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.TrackerRequest
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimes
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.Service
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.Stop
import javax.inject.Inject

/**
 * This class performs the task of obtaining all known [ArrivalAlert]s from the [AlertsDao] and then
 * obtaining live departure times for each stop. Each successfully loaded stop is then checked to
 * see if any matching services are within the time trigger. Each matching stop will generate a
 * notification with [AlertNotificationDispatcher].
 *
 * This class can handle multiple current [ArrivalAlert]s.
 *
 * If there was an error retrieving live times from the tracker service, the error will be silently
 * ignored. This is because there is no appropriate error handler, and we'd be expected to be called
 * again in a short while anyway.
 *
 * @param alertsDao A reference to the [AlertsDao].
 * @param trackerEndpoint A reference to the [TrackerEndpoint].
 * @param alertNotificationDispatcher An implementation used to dispatch notifications of arrival
 * alerts.
 * @author Niall Scott
 */
class CheckTimesTask @Inject internal constructor(
        private val alertsDao: AlertsDao,
        private val trackerEndpoint: TrackerEndpoint,
        private val alertNotificationDispatcher: AlertNotificationDispatcher) {

    private var currentRequest: TrackerRequest<LiveTimes>? = null

    /**
     * Perform a check of the arrival times for all known [ArrivalAlert]s to see if any arrivals
     * match the constraints of each [ArrivalAlert].
     */
    fun checkTimes() {
        alertsDao.getAllArrivalAlertStopCodes()?.let {
            if (it.isNotEmpty()) {
                val request = trackerEndpoint.createLiveTimesRequest(it.toTypedArray(), 1)
                currentRequest = request
                executeRequest(request)
            }
        }
    }

    /**
     * Cancel any currently executing request to the tracker service.
     */
    fun cancel() {
        currentRequest?.cancel()
    }

    /**
     * Perform the request to the tracker service to load the [LiveTimes]s for the stops we are
     * interested in.
     *
     * @param request An object describing the request, which will be executed in this method.
     */
    private fun executeRequest(request: TrackerRequest<LiveTimes>) {
        try {
            val response = request.performRequest()
            handleResponse(response)
        } catch (ignored: TrackerException) {
            // There is no handling of the Exception required as this fails silently.
        } finally {
            currentRequest = null
        }
    }

    /**
     * Handle a successful response of the [LiveTimes]. This will pair up the loaded [Stop]s with
     * the set [ArrivalAlert]s and check to see if the criteria in the alert is satisfied.
     *
     * @param liveTimes The successfully loaded [LiveTimes] object.
     */
    private fun handleResponse(liveTimes: LiveTimes) {
        alertsDao.getAllArrivalAlerts()?.forEach { alert ->
            liveTimes.stops[alert.stopCode]?.let { stop ->
                checkArrivalsForStop(alert, stop)
            }
        }
    }

    /**
     * Check the arrivals at the [Stop] to see if any of them match the criteria in the given
     * [ArrivalAlert]. Matches will be sent to the [AlertNotificationDispatcher].
     *
     * @param arrivalAlert The [ArrivalAlert] for the [Stop].
     * @param stop The loaded [Stop].
     */
    private fun checkArrivalsForStop(arrivalAlert: ArrivalAlert, stop: Stop) {
        val servicesToLookFor = arrivalAlert.serviceNames.toSet()
        val timeTrigger = arrivalAlert.timeTrigger

        val qualifyingServices = stop.services.filter {
            servicesToLookFor.contains(it.serviceName) &&
                    isServiceArrivingWithinTimeTrigger(it, timeTrigger)
        }

        if (qualifyingServices.isNotEmpty()) {
            alertNotificationDispatcher.dispatchTimeAlertNotification(arrivalAlert,
                    qualifyingServices)
            alertsDao.removeArrivalAlert(arrivalAlert.id)
        }
    }

    /**
     * Is the first vehicle for the given [Service] arriving within the time trigger?
     *
     * @param service The [Service] to check the time trigger against.
     * @param timeTrigger The time trigger.
     * @return `true` if the first vehicle for the [Service] is arriving within the time trigger.
     * `false` if the time trigger is not matched, or there is no vehicles in the [Service].
     */
    private fun isServiceArrivingWithinTimeTrigger(service: Service, timeTrigger: Int) =
            service.vehicles.firstOrNull()?.let {
                it.departureMinutes <= timeTrigger
            } ?: false
}