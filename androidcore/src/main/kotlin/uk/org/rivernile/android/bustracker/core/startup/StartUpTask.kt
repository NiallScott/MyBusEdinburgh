/*
 * Copyright (C) 2019 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.startup

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.UpdateBusStopDatabaseWorkScheduler
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForApplicationCoroutineScope
import javax.inject.Inject

/**
 * This class runs start-up tasks which are run every time the app process is started. This may be
 * things such as housekeeping, or ensuring consistent state.
 *
 * The task is begun in [performStartUpTasks] and this is executed on another thread.
 *
 * @param busStopDatabaseUpdateJobScheduler Implementation to schedule updates to the bus stop
 * database.
 * @param cleanUpTask Implementation to perform clean up of app data - usually to remove data from
 * old installations of the app.
 * @param alertsRepository The [AlertsRepository] - for controlling user set alerts.
 * @param applicationCoroutineScope The application [CoroutineScope].
 * @param defaultDispatcher The default dispatcher to dispatch coroutines on.
 * @author Niall Scott
 */
class StartUpTask @Inject internal constructor(
    private val busStopDatabaseUpdateJobScheduler: UpdateBusStopDatabaseWorkScheduler,
    private val cleanUpTask: CleanUpTask?,
    private val alertsRepository: AlertsRepository,
    @ForApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
    @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher) {

    /**
     * Run the app startup tasks. The tasks will be executed on the default [CoroutineDispatcher].
     */
    fun performStartUpTasks() {
        applicationCoroutineScope.launch(defaultDispatcher) {
            supervisorScope {
                launchScheduleUpdateBusStopDatabaseAsync()
                launchPerformCleanupAsync()
                launchEnsureAlertTasksRunningAsync()
            }
        }
    }

    /**
     * Launch the async task to schedule updates to the bus stop database.
     *
     * @return The deferred task.
     */
    private fun CoroutineScope.launchScheduleUpdateBusStopDatabaseAsync() = launch {
        busStopDatabaseUpdateJobScheduler.scheduleUpdateBusStopDatabaseJob()
    }

    /**
     * Launch the async task to perform app cleanup.
     *
     * @return The deferred task.
     */
    private fun CoroutineScope.launchPerformCleanupAsync() = launch {
        cleanUpTask?.performCleanUp()
    }

    /**
     * Launch the async task to start alert tasks, if required.
     *
     * @return The deferred task.
     */
    private fun CoroutineScope.launchEnsureAlertTasksRunningAsync() = launch {
        alertsRepository.ensureTasksRunningIfAlertsExists()
    }
}