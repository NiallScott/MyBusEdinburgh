/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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

import uk.org.rivernile.android.bustracker.core.alerts.arrivals.ArrivalAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.alerts.proximity.ProximityAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.database.settings.daos.AlertsDao
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ArrivalAlert
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ProximityAlert
import uk.org.rivernile.android.bustracker.core.di.ForShortBackgroundTasks
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This is the Alert Manager, where user-added alerts are controlled from.
 *
 * @param alertsDao The DAO to access user alerts.
 * @param arrivalAlertTaskLauncher Used to launch the arrival alert checker task.
 * @param proximityAlertTaskLauncher Used to launch the proximity alert checker task.
 * @param backgroundExecutor An [Executor] to execute background tasks on.
 * @author Niall Scott
 */
@Singleton
class AlertManager @Inject internal constructor(
        private val alertsDao: AlertsDao,
        private val arrivalAlertTaskLauncher: ArrivalAlertTaskLauncher,
        private val proximityAlertTaskLauncher: ProximityAlertTaskLauncher,
        @ForShortBackgroundTasks private val backgroundExecutor: Executor) {

    /**
     * Add a new arrival alert to be tracked.
     *
     * @param arrivalAlert The new [ArrivalAlert] to add.
     */
    fun addArrivalAlert(arrivalAlert: ArrivalAlert) {
        backgroundExecutor.execute {
            // As we currently only allow one arrival alert at a time, the newly added alert
            // overwrites any existing alerts. This may change going forwards.
            alertsDao.removeAllArrivalAlerts()
            alertsDao.addArrivalAlert(arrivalAlert)
            arrivalAlertTaskLauncher.launchArrivalAlertTask()
        }
    }

    /**
     * Remove an arrival alert.
     */
    fun removeArrivalAlert() {
        backgroundExecutor.execute {
            alertsDao.removeAllArrivalAlerts()
        }
    }

    /**
     * Add a new proximity alert to be tracked.
     *
     * @param proximityAlert The new [ProximityAlert] to add.
     */
    fun addProximityAlert(proximityAlert: ProximityAlert) {
        backgroundExecutor.execute {
            alertsDao.addProximityAlert(proximityAlert)
            proximityAlertTaskLauncher.launchProximityAlertTask()
        }
    }

    /**
     * Remove all proximity alerts for the given stop code.
     *
     * @param stopCode The stop code to remove proximity alerts for.
     */
    suspend fun removeProximityAlert(stopCode: String) {
        alertsDao.removeProximityAlert(stopCode)
    }

    /**
     * Remove all current proximity alerts.
     */
    suspend fun removeAllProximityAlerts() {
        alertsDao.removeAllProximityAlerts()
    }

    /**
     * Ensure that tasks required to fulfil alerts are running.
     */
    fun ensureTasksRunningIfAlertsExists() {
        if (alertsDao.getArrivalAlertCount() > 0) {
            arrivalAlertTaskLauncher.launchArrivalAlertTask()
        }

        if (alertsDao.getProximityAlertCount() > 0) {
            proximityAlertTaskLauncher.launchProximityAlertTask()
        }
    }
}