/*
 * Copyright (C) 2019 - 2022 Niall 'Rivernile' Scott
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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.inOrder
import uk.org.rivernile.android.bustracker.core.alerts.AlertManager
import uk.org.rivernile.android.bustracker.core.backup.BackupObserver
import uk.org.rivernile.android.bustracker.core.database.busstop.UpdateBusStopDatabaseWorkScheduler
import uk.org.rivernile.android.bustracker.core.notifications.AppNotificationChannels
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule

/**
 * Tests for [StartUpTask].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class StartUpTaskTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var appNotificationChannels: AppNotificationChannels
    @Mock
    private lateinit var backupObserver: BackupObserver
    @Mock
    private lateinit var busStopDatabaseJobScheduler: UpdateBusStopDatabaseWorkScheduler
    @Mock
    private lateinit var cleanUpTask: CleanUpTask
    @Mock
    private lateinit var alertManager: AlertManager

    @Test
    fun performsStartUpTasks() = runTest {
        val startUpTask = StartUpTask(
                appNotificationChannels,
                backupObserver,
                busStopDatabaseJobScheduler,
                cleanUpTask,
                alertManager,
                coroutineRule.scope,
                coroutineRule.testDispatcher)

        startUpTask.performStartUpTasks()
        advanceUntilIdle()

        inOrder(appNotificationChannels, backupObserver, busStopDatabaseJobScheduler, cleanUpTask,
                alertManager) {
            verify(appNotificationChannels)
                    .createNotificationChannels()
            verify(backupObserver)
                    .beginObserving()
            verify(busStopDatabaseJobScheduler)
                    .scheduleUpdateBusStopDatabaseJob()
            verify(cleanUpTask)
                    .performCleanUp()
            verify(alertManager)
                    .ensureTasksRunningIfAlertsExists()
        }
    }

    @Test
    fun doesNotThrowNullPointerExceptionWhenNoCleanUpTaskIsSupplied() {
        val startUpTask = StartUpTask(
                appNotificationChannels,
                backupObserver,
                busStopDatabaseJobScheduler,
                null,
                alertManager,
                coroutineRule.scope,
                coroutineRule.testDispatcher)

        startUpTask.performStartUpTasks()
    }
}