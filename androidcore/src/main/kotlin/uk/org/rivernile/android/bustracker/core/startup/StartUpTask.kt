/*
 * Copyright (C) 2019 - 2021 Niall 'Rivernile' Scott
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import uk.org.rivernile.android.bustracker.core.alerts.AlertManager
import uk.org.rivernile.android.bustracker.core.backup.BackupObserver
import uk.org.rivernile.android.bustracker.core.database.busstop.UpdateBusStopDatabaseJobScheduler
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.di.ForApplicationCoroutineScope
import uk.org.rivernile.android.bustracker.core.notifications.AppNotificationChannels
import javax.inject.Inject

/**
 * This class runs start-up tasks which are run every time the app process is started. This may be
 * things such as housekeeping, or ensuring consistent state.
 *
 * The task is begun in [performStartUpTasks] and this is executed on another thread.
 *
 * @param appNotificationChannels Implementation to set up notification channels.
 * @param busStopDatabaseUpdateJobScheduler Implementation to schedule updates to the bus stop
 * database.
 * @param cleanUpTask Implementation to perform clean up of app data - usually to remove data from
 * old installations of the app.
 * @param alertManager The [AlertManager] - for controlling user set alerts.
 * @param applicationCoroutineScope The application [CoroutineScope].
 * @param defaultDispatcher The default dispatcher to dispatch coroutines on.
 * @author Niall Scott
 */
class StartUpTask @Inject internal constructor(
        private val appNotificationChannels: AppNotificationChannels,
        private val backupObserver: BackupObserver,
        private val busStopDatabaseUpdateJobScheduler: UpdateBusStopDatabaseJobScheduler,
        private val cleanUpTask: CleanUpTask?,
        private val alertManager: AlertManager,
        @ForApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
        @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher) {

    /**
     * Run the app startup tasks. The tasks will be executed on a background thread so that the UI
     * is not blocked.
     */
    fun performStartUpTasks() {
        applicationCoroutineScope.launch(defaultDispatcher) {
            // In-order tasks that are pre-requisites should be executed first, before launching
            // the async tasks.
            appNotificationChannels.createNotificationChannels()
            backupObserver.beginObserving()

            awaitAll(
                    launchScheduleUpdateBusStopDatabaseAsync(this),
                    launchPerformCleanupAsync(this),
                    launchEnsureAlertTasksRunningAsync(this))
        }
    }

    /**
     * Launch the async task to schedule updates to the bus stop database.
     *
     * @param scope The [CoroutineScope] to execute the async task under.
     * @return The deferred task.
     */
    private fun launchScheduleUpdateBusStopDatabaseAsync(scope: CoroutineScope) = scope.async {
        busStopDatabaseUpdateJobScheduler.scheduleUpdateBusStopDatabaseJob()
    }

    /**
     * Launch the async task to perform app cleanup.
     *
     * @param scope The [CoroutineScope] to execute the async task under.
     * @return The deferred task.
     */
    private fun launchPerformCleanupAsync(scope: CoroutineScope) = scope.async {
        cleanUpTask?.performCleanUp()
    }

    /**
     * Launch the async task to start alert tasks, if required.
     *
     * @param scope The [CoroutineScope] to execute the async task under.
     * @return The deferred task.
     */
    private fun launchEnsureAlertTasksRunningAsync(scope: CoroutineScope) = scope.async {
        alertManager.ensureTasksRunningIfAlertsExists()
    }
}