/*
 * Copyright (C) 2020 - 2025 Niall 'Rivernile' Scott
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
import uk.org.rivernile.android.bustracker.core.time.TimeUtils
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This repository is used to access alerts data.
 *
 * @author Niall Scott
 */
public interface AlertsRepository {

    /**
     * Add a new arrival alert.
     *
     * @param request The arrival alert to add.
     */
    public suspend fun addArrivalAlert(request: ArrivalAlertRequest)

    /**
     * Add a new proximity alert.
     *
     * @param request The proximity alert to add.
     */
    public suspend fun addProximityAlert(request: ProximityAlertRequest)

    /**
     * Remove any set arrival alerts for the given stop code.
     *
     * @param stopCode The stop code to remove arrival alerts for.
     */
    public suspend fun removeArrivalAlert(stopCode: String)

    /**
     * Remove any set arrival alerts with the given ID.
     *
     * @param id ID of the alert to remove.
     */
    public suspend fun removeArrivalAlert(id: Int)

    /**
     * Remove all arrival alerts.
     */
    public suspend fun removeAllArrivalAlerts()

    /**
     * Remove any set proximity alerts for the given stop code.
     *
     * @param stopCode The stop code to remove proximity alerts for.
     */
    public suspend fun removeProximityAlert(stopCode: String)

    /**
     * Remove any set proximity alerts with the given ID.
     *
     * @param id ID of the alert to remove.
     */
    public suspend fun removeProximityAlert(id: Int)

    /**
     * Remove all proximity alerts.
     */
    public suspend fun removeAllProximityAlerts()

    /**
     * Get all current [ArrivalAlert]s.
     *
     * @return All current [ArrivalAlert]s.
     */
    public suspend fun getAllArrivalAlerts(): List<ArrivalAlert>?

    /**
     * Get the stop codes of all active arrival alerts.
     *
     * @return The stop codes of all active arrival alerts.
     */
    public suspend fun getAllArrivalAlertStopCodes(): Set<String>?

    /**
     * Get the [ProximityAlert] with the given ID. If this alert does not not exist, `null` will be
     * returned.
     *
     * @param id The ID of the proximity alert.
     * @return The [ProximityAlert] with the given ID. If this alert does not not exist, `null`
     * will be returned.
     */
    public suspend fun getProximityAlert(id: Int): ProximityAlert?

    /**
     * Get a [Flow] which emits whether the given `stopCode` has an arrival alert set or not, and
     * will emit further items when the status changes.
     *
     * @param stopCode The `stopCode` to watch.
     * @return The [Flow] which emits the arrival alert status of the given `stopCode`.
     */
    public fun hasArrivalAlertFlow(stopCode: String): Flow<Boolean>

    /**
     * Get a [Flow] which emits whether the given `stopCode` has a proximity alert set or not, and
     * will emit further items when the status changes.
     *
     * @param stopCode The `stopCode` to watch.
     * @return The [Flow] which emits the proximity alert status of the given `stopCode`.
     */
    public fun hasProximityAlertFlow(stopCode: String): Flow<Boolean>

    /**
     * A [Flow] which emits the number of arrival alerts.
     */
    public val arrivalAlertCountFlow: Flow<Int>

    /**
     * A [Flow] which emits all [ProximityAlert]s.
     */
    public val allProximityAlertsFlow: Flow<List<ProximityAlert>?>

    /**
     * Get a [Flow] which emits a [List] of all the currently set user alerts.
     *
     * @return A [Flow] which emits a [List] of all the currently set user alerts.
     */
    public val allAlertsFlow: Flow<List<Alert>?>

    /**
     * Ensure the arrival and proximity alert tasks are running. These tasks will auto-cancel
     * themselves if there are no alerts to track - but this method at least starts these tasks so
     * the tasks themselves can verify this.
     */
    public fun ensureTasksRunning()

    /**
     * Ensure that tasks required to fulfil alerts are running.
     */
    public suspend fun ensureTasksRunningIfAlertsExists()
}

@Singleton
internal class RealAlertsRepository @Inject constructor(
    private val arrivalAlertTaskLauncher: ArrivalAlertTaskLauncher,
    private val proximityAlertTaskLauncher: ProximityAlertTaskLauncher,
    private val alertsDao: AlertsDao,
    private val timeUtils: TimeUtils
) : AlertsRepository {

    override suspend fun addArrivalAlert(request: ArrivalAlertRequest) {
        val alert = ArrivalAlertEntity(
            0,
            timeUtils.currentTimeMills,
            request.stopCode,
            request.serviceNames,
            request.timeTrigger
        )

        alertsDao.addArrivalAlert(alert)
        arrivalAlertTaskLauncher.launchArrivalAlertTask()
    }

    override suspend fun addProximityAlert(request: ProximityAlertRequest) {
        val alert = ProximityAlertEntity(
            0,
            timeUtils.currentTimeMills,
            request.stopCode,
            request.distanceFrom
        )

        alertsDao.addProximityAlert(alert)
        proximityAlertTaskLauncher.launchProximityAlertTask()
    }

    override suspend fun removeArrivalAlert(stopCode: String) {
        alertsDao.removeArrivalAlert(stopCode)
    }

    override suspend fun removeArrivalAlert(id: Int) {
        alertsDao.removeArrivalAlert(id)
    }

    override suspend fun removeAllArrivalAlerts() {
        alertsDao.removeAllArrivalAlerts()
    }

    override suspend fun removeProximityAlert(stopCode: String) {
        alertsDao.removeProximityAlert(stopCode)
    }

    override suspend fun removeProximityAlert(id: Int) {
        alertsDao.removeProximityAlert(id)
    }

    override suspend fun removeAllProximityAlerts() {
        alertsDao.removeAllProximityAlerts()
    }

    override suspend fun getAllArrivalAlerts(): List<ArrivalAlert>? =
        alertsDao.getAllArrivalAlerts()
            ?.map { it.toArrivalAlert() }

    override suspend fun getAllArrivalAlertStopCodes(): Set<String>? =
        alertsDao.getAllArrivalAlertStopCodes()?.toSet()

    override suspend fun getProximityAlert(id: Int): ProximityAlert? =
        alertsDao
            .getProximityAlert(id)
            ?.toProximityAlert()

    override fun hasArrivalAlertFlow(stopCode: String): Flow<Boolean> =
        alertsDao
            .getHasArrivalAlertFlow(stopCode)
            .distinctUntilChanged()

    override fun hasProximityAlertFlow(stopCode: String): Flow<Boolean> =
        alertsDao
            .getHasProximityAlertFlow(stopCode)
            .distinctUntilChanged()

    override val arrivalAlertCountFlow: Flow<Int> get() =
        alertsDao
            .arrivalAlertCountFlow
            .distinctUntilChanged()

    override val allProximityAlertsFlow: Flow<List<ProximityAlert>?> get() =
        alertsDao
            .allProximityAlertsFlow
            .distinctUntilChanged()
            .map { alertEntities ->
                alertEntities?.map { it.toProximityAlert() }
            }

    override val allAlertsFlow: Flow<List<Alert>?> get() =
        alertsDao
            .allAlertsFlow
            .distinctUntilChanged()
            .map { alertEntities ->
                alertEntities?.map { it.toAlert() }
            }

    override fun ensureTasksRunning() {
        arrivalAlertTaskLauncher.launchArrivalAlertTask()
        proximityAlertTaskLauncher.launchProximityAlertTask()
    }

    override suspend fun ensureTasksRunningIfAlertsExists() {
        supervisorScope {
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
    }

    /**
     * Map this [AlertEntity] to an [Alert].
     *
     * @return This [AlertEntity] mapped to an [Alert].
     */
    private fun AlertEntity.toAlert(): Alert {
        return when (this) {
            is ArrivalAlertEntity -> toArrivalAlert()
            is ProximityAlertEntity -> toProximityAlert()
        }
    }

    /**
     * Map this [ArrivalAlertEntity] to an [ArrivalAlert].
     *
     * @return The [ArrivalAlertEntity] mapped to an [ArrivalAlertEntity].
     */
    private fun ArrivalAlertEntity.toArrivalAlert() =
        ArrivalAlert(
            id = id,
            timeAdded = timeAdded,
            stopCode = stopCode,
            serviceNames = serviceNames,
            timeTrigger = timeTrigger
        )

    /**
     * Map this [ProximityAlertEntity] to a [ProximityAlert].
     *
     * @return This [ProximityAlertEntity] mapped to a [ProximityAlert].
     */
    private fun ProximityAlertEntity.toProximityAlert() =
        ProximityAlert(
            id = id,
            timeAdded = timeAdded,
            stopCode = stopCode,
            distanceFrom = distanceFrom
        )
}