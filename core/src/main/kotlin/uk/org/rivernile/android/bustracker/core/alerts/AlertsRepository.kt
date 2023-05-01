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

package uk.org.rivernile.android.bustracker.core.alerts

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.ArrivalAlertRequest
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.ArrivalAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.alerts.proximity.ProximityAlertRequest
import uk.org.rivernile.android.bustracker.core.alerts.proximity.ProximityAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.AlertEntity
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.AlertsDao
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.ArrivalAlertEntity
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.ProximityAlertEntity
import uk.org.rivernile.android.bustracker.core.utils.TimeUtils
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This repository is used to access alerts data.
 *
 * @param arrivalAlertTaskLauncher Used to launch the arrival alert checker task.
 * @param proximityAlertTaskLauncher Used to launch the proximity alert checker task.
 * @param alertsDao The DAO to access the alerts data store.
 * @param timeUtils Used to access timestamp data.
 * @author Niall Scott
 */
@Singleton
class AlertsRepository @Inject internal constructor(
    private val arrivalAlertTaskLauncher: ArrivalAlertTaskLauncher,
    private val proximityAlertTaskLauncher: ProximityAlertTaskLauncher,
    private val alertsDao: AlertsDao,
    private val timeUtils: TimeUtils) {

    /**
     * Add a new arrival alert.
     *
     * @param request The arrival alert to add.
     */
    suspend fun addArrivalAlert(request: ArrivalAlertRequest) {
        val alert = ArrivalAlertEntity(
            0,
            timeUtils.currentTimeMills,
            request.stopCode,
            request.serviceNames,
            request.timeTrigger)
        alertsDao.addArrivalAlert(alert)
        arrivalAlertTaskLauncher.launchArrivalAlertTask()
    }

    /**
     * Add a new proximity alert.
     *
     * @param request The proximity alert to add.
     */
    suspend fun addProximityAlert(request: ProximityAlertRequest) {
        val alert = ProximityAlertEntity(
            0,
            timeUtils.currentTimeMills,
            request.stopCode,
            request.distanceFrom)
        alertsDao.addProximityAlert(alert)
        proximityAlertTaskLauncher.launchProximityAlertTask()
    }

    /**
     * Remove any set arrival alerts for the given stop code.
     *
     * @param stopCode The stop code to remove arrival alerts for.
     */
    suspend fun removeArrivalAlert(stopCode: String) {
        alertsDao.removeArrivalAlert(stopCode)
    }

    /**
     * Remove any set arrival alerts with the given ID.
     *
     * @param id ID of the alert to remove.
     */
    suspend fun removeArrivalAlert(id: Int) {
        alertsDao.removeArrivalAlert(id)
    }

    /**
     * Remove all arrival alerts.
     */
    suspend fun removeAllArrivalAlerts() {
        alertsDao.removeAllArrivalAlerts()
    }

    /**
     * Remove any set proximity alerts for the given stop code.
     *
     * @param stopCode The stop code to remove proximity alerts for.
     */
    suspend fun removeProximityAlert(stopCode: String) {
        alertsDao.removeProximityAlert(stopCode)
    }

    /**
     * Remove any set proximity alerts with the given ID.
     *
     * @param id ID of the alert to remove.
     */
    suspend fun removeProximityAlert(id: Int) {
        alertsDao.removeProximityAlert(id)
    }

    /**
     * Remove all proximity alerts.
     */
    suspend fun removeAllProximityAlerts() {
        alertsDao.removeAllProximityAlerts()
    }

    /**
     * Get all current [ArrivalAlert]s.
     *
     * @return All current [ArrivalAlert]s.
     */
    suspend fun getAllArrivalAlerts(): List<ArrivalAlert>? =
        alertsDao.getAllArrivalAlerts()
            ?.map(this::mapToArrivalAlert)

    /**
     * Get the stop codes of all active arrival alerts.
     *
     * @return The stop codes of all active arrival alerts.
     */
    suspend fun getAllArrivalAlertStopCodes(): Set<String>? =
        alertsDao.getAllArrivalAlertStopCodes()?.toSet()

    /**
     * Get the [ProximityAlert] with the given ID. If this alert does not not exist, `null` will be
     * returned.
     *
     * @param id The ID of the proximity alert.
     * @return The [ProximityAlert] with the given ID. If this alert does not not exist, `null`
     * will be returned.
     */
    suspend fun getProximityAlert(id: Int): ProximityAlert? =
        alertsDao.getProximityAlert(id)?.let(this::mapToProximityAlert)

    /**
     * Get a [Flow] which emits whether the given `stopCode` has an arrival alert set or not, and
     * will emit further items when the status changes.
     *
     * @param stopCode The `stopCode` to watch.
     * @return The [Flow] which emits the arrival alert status of the given `stopCode`.
     */
    fun hasArrivalAlertFlow(stopCode: String): Flow<Boolean> =
        alertsDao
            .getHasArrivalAlertFlow(stopCode)
            .distinctUntilChanged()

    /**
     * Get a [Flow] which emits whether the given `stopCode` has a proximity alert set or not, and
     * will emit further items when the status changes.
     *
     * @param stopCode The `stopCode` to watch.
     * @return The [Flow] which emits the proximity alert status of the given `stopCode`.
     */
    fun hasProximityAlertFlow(stopCode: String): Flow<Boolean> =
        alertsDao
            .getHasProximityAlertFlow(stopCode)
            .distinctUntilChanged()

    /**
     * A [Flow] which emits the number of arrival alerts.
     */
    val arrivalAlertCountFlow: Flow<Int> get() =
        alertsDao
            .arrivalAlertCountFlow
            .distinctUntilChanged()

    /**
     * A [Flow] which emits all [ProximityAlert]s.
     */
    val allProximityAlertsFlow: Flow<List<ProximityAlert>?> get() =
        alertsDao
            .allProximityAlertsFlow
            .distinctUntilChanged()
            .map {
                it?.map(this::mapToProximityAlert)
            }

    /**
     * Get a [Flow] which emits a [List] of all the currently set user alerts.
     *
     * @return A [Flow] which emits a [List] of all the currently set user alerts.
     */
    val allAlertsFlow: Flow<List<Alert>?> get() =
        alertsDao
            .allAlertsFlow
            .distinctUntilChanged()
            .map {
                it?.map(this::mapToAlert)
            }

    /**
     * Ensure that tasks required to fulfil alerts are running.
     */
    suspend fun ensureTasksRunningIfAlertsExists() = supervisorScope {
        launch {
            if (alertsDao.getArrivalAlertCount() > 0) {
                arrivalAlertTaskLauncher.launchArrivalAlertTask()
            }
        }

        launch {
            if (alertsDao.getProximityAlertCount() > 0) {
                proximityAlertTaskLauncher.launchProximityAlertTask()
            }
        }
    }

    /**
     * Map a given [entity] to an [Alert].
     *
     * @param entity The entity to map.
     * @return The [entity] mapped to an [AlertEntity].
     */
    private fun mapToAlert(entity: AlertEntity): Alert {
        return when (entity) {
            is ArrivalAlertEntity -> mapToArrivalAlert(entity)
            is ProximityAlertEntity -> mapToProximityAlert(entity)
        }
    }

    /**
     * Map a given [entity] to an [ArrivalAlert].
     *
     * @param entity The entity to map.
     * @return The [entity] mapped to an [ArrivalAlertEntity].
     */
    private fun mapToArrivalAlert(entity: ArrivalAlertEntity) =
        ArrivalAlert(
            entity.id,
            entity.timeAdded,
            entity.stopCode,
            entity.serviceNames,
            entity.timeTrigger)

    /**
     * Map a given [entity] to a [ProximityAlert].
     *
     * @param entity The entity to map.
     * @return The [entity] mapped to a [ProximityAlert].
     */
    private fun mapToProximityAlert(entity: ProximityAlertEntity) =
        ProximityAlert(
            entity.id,
            entity.timeAdded,
            entity.stopCode,
            entity.distanceFrom)
}