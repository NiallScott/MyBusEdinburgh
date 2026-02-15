/*
 * Copyright (C) 2020 - 2026 Niall 'Rivernile' Scott
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.ArrivalAlertRequest
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.ArrivalAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.toArrivalAlert
import uk.org.rivernile.android.bustracker.core.alerts.proximity.ProximityAlertRequest
import uk.org.rivernile.android.bustracker.core.alerts.proximity.ProximityAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.alerts.proximity.toProximityAlert
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.arrival.ArrivalAlert
    as DatabaseArrivalAlert
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.arrival.ArrivalAlertDao
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.proximity.ProximityAlert
    as DatabaseProximityAlert
import uk.org.rivernile.android.bustracker.core.database.settings.alerts.proximity.ProximityAlertDao
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
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
     * Remove any set arrival alerts for the given stop.
     *
     * @param stopIdentifier The stop to remove arrival alerts for.
     */
    public suspend fun removeArrivalAlert(stopIdentifier: StopIdentifier)

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
     * Remove any set proximity alerts for the given stop.
     *
     * @param stopIdentifier The stop to remove proximity alerts for.
     */
    public suspend fun removeProximityAlert(stopIdentifier: StopIdentifier)

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
     * Get the [StopIdentifier]s of all active arrival alerts.
     *
     * @return The [StopIdentifier]s of all active arrival alerts.
     */
    public suspend fun getAllArrivalAlertStops(): Set<StopIdentifier>?

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
     * Get a [Flow] which emits whether the given [stopIdentifier] has an arrival alert set or not,
     * and will emit further items when the status changes.
     *
     * @param stopIdentifier The [StopIdentifier] to watch.
     * @return The [Flow] which emits the arrival alert status of the given [stopIdentifier].
     */
    public fun hasArrivalAlertFlow(stopIdentifier: StopIdentifier): Flow<Boolean>

    /**
     * Get a [Flow] which emits whether the given [stopIdentifier] has a proximity alert set or not,
     * and will emit further items when the status changes.
     *
     * @param stopIdentifier The [StopIdentifier] to watch.
     * @return The [Flow] which emits the proximity alert status of the given [stopIdentifier].
     */
    public fun hasProximityAlertFlow(stopIdentifier: StopIdentifier): Flow<Boolean>

    /**
     * Get the active arrival alert count.
     *
     * @return The active arrival alert count.
     */
    public suspend fun getArrivalAlertCount(): Int

    /**
     * Get the active proximity alert count.
     *
     * @return The active proximity alert count.
     */
    public suspend fun getProximityAlertCount(): Int

    /**
     * A [Flow] which emits the number of arrival alerts.
     */
    public val arrivalAlertCountFlow: Flow<Int>

    /**
     * A [Flow] which emits all the stop codes which have arrival alerts set.
     */
    public val arrivalAlertStopIdentifiersFlow: Flow<Set<StopIdentifier>?>

    /**
     * A [Flow] which emits all [ProximityAlert]s.
     */
    public val allProximityAlertsFlow: Flow<List<ProximityAlert>?>

    /**
     * A [Flow] which emits all the stop codes which have proximity alerts set.
     */
    public val proximityAlertStopIdentifiersFlow: Flow<Set<StopIdentifier>?>

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
    private val arrivalAlertDao: ArrivalAlertDao,
    private val proximityAlertDao: ProximityAlertDao,
    private val timeUtils: TimeUtils
) : AlertsRepository {

    override suspend fun addArrivalAlert(request: ArrivalAlertRequest) {
        arrivalAlertDao.addArrivalAlert(request.toArrivalAlert(timeUtils.now))
        arrivalAlertTaskLauncher.launchArrivalAlertTask()
    }

    override suspend fun addProximityAlert(request: ProximityAlertRequest) {
        proximityAlertDao.addProximityAlert(request.toProximityAlert(timeUtils.now))
        proximityAlertTaskLauncher.launchProximityAlertTask()
    }

    override suspend fun removeArrivalAlert(stopIdentifier: StopIdentifier) {
        arrivalAlertDao.removeArrivalAlert(stopIdentifier)
    }

    override suspend fun removeArrivalAlert(id: Int) {
        arrivalAlertDao.removeArrivalAlert(id)
    }

    override suspend fun removeAllArrivalAlerts() {
        arrivalAlertDao.removeAllArrivalAlerts()
    }

    override suspend fun removeProximityAlert(stopIdentifier: StopIdentifier) {
        proximityAlertDao.removeProximityAlert(stopIdentifier)
    }

    override suspend fun removeProximityAlert(id: Int) {
        proximityAlertDao.removeProximityAlert(id)
    }

    override suspend fun removeAllProximityAlerts() {
        proximityAlertDao.removeAllProximityAlerts()
    }

    override suspend fun getAllArrivalAlerts(): List<ArrivalAlert>? =
        arrivalAlertDao.getAllArrivalAlerts()
            ?.map { it.toArrivalAlert() }

    override suspend fun getAllArrivalAlertStops(): Set<StopIdentifier>? =
        arrivalAlertDao.getAllArrivalAlertStops()?.toSet()

    override suspend fun getProximityAlert(id: Int): ProximityAlert? =
        proximityAlertDao
            .getProximityAlert(id)
            ?.toProximityAlert()

    override fun hasArrivalAlertFlow(stopIdentifier: StopIdentifier): Flow<Boolean> =
        arrivalAlertDao
            .getHasArrivalAlertFlow(stopIdentifier)
            .distinctUntilChanged()

    override fun hasProximityAlertFlow(stopIdentifier: StopIdentifier): Flow<Boolean> =
        proximityAlertDao
            .getHasProximityAlertFlow(stopIdentifier)
            .distinctUntilChanged()

    override suspend fun getArrivalAlertCount() =
        arrivalAlertDao.getArrivalAlertCount()

    override suspend fun getProximityAlertCount() =
        proximityAlertDao.getProximityAlertCount()

    override val arrivalAlertCountFlow: Flow<Int> get() =
        arrivalAlertDao
            .arrivalAlertCountFlow
            .distinctUntilChanged()

    override val arrivalAlertStopIdentifiersFlow get() =
        arrivalAlertDao
            .allArrivalAlertStopsFlow
            .map {
                it?.toSet()
            }
            .distinctUntilChanged()

    override val allProximityAlertsFlow: Flow<List<ProximityAlert>?> get() =
        proximityAlertDao
            .allProximityAlertsFlow
            .distinctUntilChanged()
            .map { alertEntities ->
                alertEntities?.map { it.toProximityAlert() }
            }

    override val proximityAlertStopIdentifiersFlow get() =
        proximityAlertDao
            .allProximityAlertStopsFlow
            .map {
                it?.toSet()
            }
            .distinctUntilChanged()

    override val allAlertsFlow: Flow<List<Alert>?> get() =
        combine(
            arrivalAlertDao.allArrivalAlertsFlow,
            proximityAlertDao.allProximityAlertsFlow,
            ::combineAlerts
        ).distinctUntilChanged()

    override fun ensureTasksRunning() {
        arrivalAlertTaskLauncher.launchArrivalAlertTask()
        proximityAlertTaskLauncher.launchProximityAlertTask()
    }

    override suspend fun ensureTasksRunningIfAlertsExists() {
        supervisorScope {
            launch {
                if (arrivalAlertDao.getArrivalAlertCount() > 0) {
                    arrivalAlertTaskLauncher.launchArrivalAlertTask()
                }
            }

            launch {
                if (proximityAlertDao.getProximityAlertCount() > 0) {
                    proximityAlertTaskLauncher.launchProximityAlertTask()
                }
            }
        }
    }

    private fun combineAlerts(
        arrivalAlerts: List<DatabaseArrivalAlert>?,
        proximityAlerts: List<DatabaseProximityAlert>?
    ): List<Alert>? {
        return buildList {
            if (arrivalAlerts != null) {
                addAll(arrivalAlerts.toArrivalAlertList())
            }

            if (proximityAlerts != null) {
                addAll(proximityAlerts.toProximityAlertList())
            }
        }.ifEmpty { null }
    }
}
