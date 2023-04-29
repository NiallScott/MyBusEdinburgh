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

package uk.org.rivernile.android.bustracker.core.database.busstop

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.collectIndexed
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceRepository
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The purpose of this class is to schedule the work to perform the bus stop database update checks.
 *
 * @param context The application [Context].
 * @param workManager The [WorkManager].
 * @param preferenceRepository Used to access preferences.
 * @author Niall Scott
 */
@Singleton
internal class UpdateBusStopDatabaseWorkScheduler @Inject constructor(
        private val context: Context,
        private val workManager: WorkManager,
        private val preferenceRepository: PreferenceRepository) {

    companion object {

        private const val WORKER_NAME = "StopDatabaseSync"
        private const val WORKER_PERIOD_HOURS = 6L
        private const val WORK_UUID = "4f4f5891-73ee-4ddf-8b8c-9fc28873dd12" // Randomly generated
    }

    private val workUuid = UUID.fromString(WORK_UUID)

    /**
     * Schedules the recurring job to update the bus stop database, if required.
     */
    suspend fun scheduleUpdateBusStopDatabaseJob() {
        preferenceRepository.isDatabaseUpdateWifiOnlyFlow.collectIndexed { index, value ->
            if (index == 0) {
                workManager.enqueueUniquePeriodicWork(
                    WORKER_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    createWorkRequest(value))
            } else {
                workManager.updateWork(createWorkRequest(value))
            }
        }
    }

    /**
     * Create a [androidx.work.PeriodicWorkRequest] for the database update work.
     *
     * @param wifiOnly Should the database update only happen on Wi-Fi? This is a user preference.
     * @return A [androidx.work.PeriodicWorkRequest] for the database update work.
     */
    private fun createWorkRequest(wifiOnly: Boolean) =
        PeriodicWorkRequestBuilder<StopDatabaseUpdateWorker>(WORKER_PERIOD_HOURS, TimeUnit.HOURS)
                .setId(workUuid)
                .setConstraints(createConstraints(wifiOnly))
                .build()

    /**
     * Create a [Constraints] object which defines the work constraints.
     *
     * @param wifiOnly Should the database update only happen on Wi-Fi? This is a user preference.
     * @return A [Constraints] object which defines the work constraints.
     */
    private fun createConstraints(wifiOnly: Boolean) = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .build()
}