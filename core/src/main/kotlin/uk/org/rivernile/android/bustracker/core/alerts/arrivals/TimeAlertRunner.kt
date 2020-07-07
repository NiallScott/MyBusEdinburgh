/*
 * Copyright (C) 2019 - 2020 Niall 'Rivernile' Scott
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

import uk.org.rivernile.android.bustracker.core.database.settings.daos.AlertsDao
import uk.org.rivernile.android.bustracker.core.di.ForArrivalAlerts
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * This class runs checks of the arrival alerts on a time period. Start this runner by calling
 * [TimeAlertRunner.start]. While running, this runner will stop itself if there are no more active
 * alerts set.
 *
 * @param checkTimesTask The implementation for performing the check of the arrival alerts.
 * @param alertsDao The DAO to access the currently set arrival alerts.
 * @param executorService A [ScheduledExecutorService] to run the period checks on.
 * @author Niall Scott
 */
class TimeAlertRunner @Inject internal constructor(
        private val checkTimesTask: CheckTimesTask,
        private val alertsDao: AlertsDao,
        @ForArrivalAlerts private val executorService: ScheduledExecutorService) {

    companion object {

        private const val CHECK_TIMES_INTERVAL_SECS = 60L
    }

    private val hasBeenStarted = AtomicBoolean(false)
    private val hasBeenStopped = AtomicBoolean(false)
    private var onStopListener: (() -> Unit)? = null

    /**
     * Start this runner. If the runner has been started before, calling this method has no effect.
     */
    fun start(onStopListener: (() -> Unit)?) {
        if (!hasBeenStopped.get() && hasBeenStarted.compareAndSet(false, true)) {
            this.onStopListener = onStopListener
            alertsDao.addOnAlertsChangedListener(alertsChangedListener)
            // Perform customary check on the alert count.
            executeAlertCountCheck()
            executorService.scheduleWithFixedDelay(checkTimesTask::checkTimes, 0L,
                    CHECK_TIMES_INTERVAL_SECS, TimeUnit.SECONDS)
        }
    }

    /**
     * Stop this runner.
     */
    fun stop() {
        if (hasBeenStopped.compareAndSet(false, true)) {
            alertsDao.removeOnAlertsChangedListener(alertsChangedListener)
            stopRunner()
            onStopListener?.invoke()
            onStopListener = null
        }
    }

    /**
     * Perform a check of the arrival alert count. If there are no active alerts, we will stop
     * ourselves.
     */
    private fun checkAlertCount() {
        if (alertsDao.getArrivalAlertCount() <= 0) {
            stop()
        }
    }

    /**
     * Stop this runner.
     */
    private fun stopRunner() {
        executorService.shutdownNow()
    }

    /**
     * Run an alert count check on the executor thread.
     */
    private fun executeAlertCountCheck() {
        executorService.execute(this::checkAlertCount)
    }

    private val alertsChangedListener = object : AlertsDao.OnAlertsChangedListener {
        override fun onAlertsChanged() {
            executeAlertCountCheck()
        }
    }
}