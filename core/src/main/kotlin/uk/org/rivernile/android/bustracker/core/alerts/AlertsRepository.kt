/*
 * Copyright (C) 2020 - 2022 Niall 'Rivernile' Scott
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

import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.ArrivalAlertRequest
import uk.org.rivernile.android.bustracker.core.alerts.proximity.ProximityAlertRequest
import uk.org.rivernile.android.bustracker.core.database.settings.daos.AlertsDao
import uk.org.rivernile.android.bustracker.core.database.settings.entities.Alert
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ArrivalAlert
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ProximityAlert
import uk.org.rivernile.android.bustracker.core.utils.TimeUtils
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This repository is used to access alerts data.
 *
 * @param alertManager The [AlertManager] implementation.
 * @param alertsDao The DAO to access the alerts data store.
 * @param timeUtils Used to access timestamp data.
 * @author Niall Scott
 */
@Singleton
class AlertsRepository @Inject internal constructor(
        private val alertManager: AlertManager,
        private val alertsDao: AlertsDao,
        private val timeUtils: TimeUtils) {

    /**
     * Get a [Flow] which returns whether the given `stopCode` has an arrival alert set or not, and
     * will emit further items when the status changes.
     *
     * @param stopCode The `stopCode` to watch.
     * @return The [Flow] which emits the arrival alert status of the given `stopCode`.
     */
    fun hasArrivalAlertFlow(stopCode: String): Flow<Boolean> = callbackFlow {
        val listener = object : AlertsDao.OnAlertsChangedListener {
            override fun onAlertsChanged() {
                launch {
                    getAndSendHasArrivalAlertSet(channel, stopCode)
                }
            }
        }

        alertsDao.addOnAlertsChangedListener(listener)
        getAndSendHasArrivalAlertSet(channel, stopCode)

        awaitClose {
            alertsDao.removeOnAlertsChangedListener(listener)
        }
    }

    /**
     * Get a [Flow] which returns whether the given `stopCode` has a proximity alert set or not, and
     * will emit further items when the status changes.
     *
     * @param stopCode The `stopCode` to watch.
     * @return The [Flow] which emits the proximity alert status of the given `stopCode`.
     */
    fun hasProximityAlertFlow(stopCode: String): Flow<Boolean> = callbackFlow {
        val listener = object : AlertsDao.OnAlertsChangedListener {
            override fun onAlertsChanged() {
                launch {
                    getAndSendHasProximityAlertSet(channel, stopCode)
                }
            }
        }

        alertsDao.addOnAlertsChangedListener(listener)
        getAndSendHasProximityAlertSet(channel, stopCode)

        awaitClose {
            alertsDao.removeOnAlertsChangedListener(listener)
        }
    }

    /**
     * Add a new arrival alert.
     *
     * @param request The arrival alert to add.
     */
    suspend fun addArrivalAlert(request: ArrivalAlertRequest) {
        val alert = ArrivalAlert(
                0,
                timeUtils.getCurrentTimeMillis(),
                request.stopCode,
                request.serviceNames,
                request.timeTrigger)
        alertManager.addArrivalAlert(alert)
    }

    /**
     * Add a new proximity alert.
     *
     * @param request The proximity alert to add.
     */
    suspend fun addProximityAlert(request: ProximityAlertRequest) {
        val alert = ProximityAlert(
                0,
                timeUtils.getCurrentTimeMillis(),
                request.stopCode,
                request.distanceFrom)
        alertManager.addProximityAlert(alert)
    }

    /**
     * Remove any set arrival alerts for the given stop code.
     *
     * @param stopCode The stop code to remove arrival alerts for.
     */
    suspend fun removeArrivalAlert(stopCode: String) {
        alertManager.removeArrivalAlert(stopCode)
    }

    /**
     * Remove any set proximity alerts for the given stop code.
     *
     * @param stopCode The stop code to remove proximity alerts for.
     */
    suspend fun removeProximityAlert(stopCode: String) {
        alertManager.removeProximityAlert(stopCode)
    }

    /**
     * Get a [Flow] which emits a [List] of all the currently set user alerts.
     *
     * @return A [Flow] which emits a [List] of all the currently set user alerts.
     */
    fun getAllAlertsFlow(): Flow<List<Alert>?> = callbackFlow {
        val listener = object : AlertsDao.OnAlertsChangedListener {
            override fun onAlertsChanged() {
                launch {
                    getAndSendAllAlerts(channel)
                }
            }
        }

        alertsDao.addOnAlertsChangedListener(listener)
        getAndSendAllAlerts(channel)

        awaitClose {
            alertsDao.removeOnAlertsChangedListener(listener)
        }
    }

    /**
     * A suspended function which obtains the arrival alert status of the given `stopCode` and then
     * sends it to the given `channel`.
     *
     * @param channel The [SendChannel] that emissions should be sent to.
     * @param stopCode The `stopCode` to obtain the arrival alert status for.
     */
    private suspend fun getAndSendHasArrivalAlertSet(
            channel: SendChannel<Boolean>,
            stopCode: String) {
        channel.send(alertsDao.hasArrivalAlert(stopCode))
    }

    /**
     * A suspended function which obtains the proximity alert status of the given `stopCode` and
     * then sends it to the given `channel`.
     *
     * @param channel The [SendChannel] that emissions should be sent to.
     * @param stopCode The `stopCode` to obtain the proximity alert status for.
     */
    private suspend fun getAndSendHasProximityAlertSet(
            channel: SendChannel<Boolean>,
            stopCode: String) {
        channel.send(alertsDao.hasProximityAlert(stopCode))
    }

    /**
     * A suspended function which obtains all set user alerts.
     *
     * @param channel The [SendChannel] that emissions should be sent to.
     */
    private suspend fun getAndSendAllAlerts(channel: SendChannel<List<Alert>?>) {
        channel.send(alertsDao.getAllAlerts())
    }
}