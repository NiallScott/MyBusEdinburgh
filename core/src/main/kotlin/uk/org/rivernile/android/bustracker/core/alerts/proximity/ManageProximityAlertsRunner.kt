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

package uk.org.rivernile.android.bustracker.core.alerts.proximity

import uk.org.rivernile.android.bustracker.core.database.settings.daos.AlertsDao
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ProximityAlert
import uk.org.rivernile.android.bustracker.core.di.ForProximityAlerts
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * This implementation runs the tracking of proximity alerts.
 *
 * @param alertsDao Used to access the alerts data store.
 * @param proximityAlertTracker Proximity alerts to begin or stop tracking are managed through this.
 * @param backgroundExecutor Used to run tasks in the background.
 * @author Niall Scott
 */
class ManageProximityAlertsRunner @Inject internal constructor(
        private val alertsDao: AlertsDao,
        private val proximityAlertTracker: ProximityAlertTracker,
        @ForProximityAlerts private val backgroundExecutor: ExecutorService) {

    private val hasBeenStarted = AtomicBoolean(false)
    private val hasBeenStopped = AtomicBoolean(false)
    private var onStopListener: (() -> Unit)? = null

    private val trackedAlerts = mutableMapOf<Int, ProximityAlert>()

    /**
     * Start the runner.
     *
     * @param onStopListener An optional listener which is called when this runner stops.
     */
    fun start(onStopListener: (() -> Unit)?) {
        if (!hasBeenStopped.get() && hasBeenStarted.compareAndSet(false, true)) {
            this.onStopListener = onStopListener
            alertsDao.addOnAlertsChangedListener(alertsChangedListener)
            executeFetchCurrentProximityAlerts()
        }
    }

    /**
     * Stop the runner.
     */
    fun stop() {
        if (hasBeenStopped.compareAndSet(false, true)) {
            alertsDao.removeOnAlertsChangedListener(alertsChangedListener)
            stopExecutor()
            onStopListener?.invoke()
            onStopListener = null
        }
    }

    /**
     * Java finalizer.
     */
    protected fun finalize() {
        stop()
    }

    /**
     * Stop the background [ExecutorService].
     */
    private fun stopExecutor() {
        backgroundExecutor.shutdownNow()
    }

    /**
     * Begins the process of fetching current proximity alerts on the background [ExecutorService].
     */
    private fun executeFetchCurrentProximityAlerts() {
        backgroundExecutor.execute(this::fetchCurrentProximityAlerts)
    }

    /**
     * Fetch the current proximity alerts and compares them to what we know we have set. Additions
     * and removals are handled by propagating this as appropriate to the [GeofencingManager] and
     * we update our internal state.
     *
     * If no alerts are set, this will call [stop].
     */
    private fun fetchCurrentProximityAlerts() {
        val alertsMap = alertsDao.getAllProximityAlerts()?.associateBy { it.id } ?: emptyMap()
        val toAdd = HashMap<Int, ProximityAlert>(alertsMap).apply {
            keys.removeAll(trackedAlerts.keys)
        }
        val toRemove = HashSet<Int>(trackedAlerts.keys).apply {
            removeAll(alertsMap.keys)
        }

        trackedAlerts.keys.removeAll(toRemove)
        trackedAlerts.putAll(toAdd)

        toAdd.values.forEach(proximityAlertTracker::trackProximityAlert)
        toRemove.forEach(proximityAlertTracker::removeProximityAlert)

        trackedAlerts.ifEmpty { stop() }
    }

    private val alertsChangedListener = object : AlertsDao.OnAlertsChangedListener {
        override fun onAlertsChanged() {
            executeFetchCurrentProximityAlerts()
        }
    }
}