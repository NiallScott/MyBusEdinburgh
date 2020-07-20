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

package uk.org.rivernile.android.bustracker.core.database.busstop

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import uk.org.rivernile.android.bustracker.core.job.setPrefetchCompat
import uk.org.rivernile.android.bustracker.core.job.setRequiresBatteryNotLowCompat
import uk.org.rivernile.android.bustracker.core.preferences.AndroidPreferenceManager
import uk.org.rivernile.android.bustracker.core.preferences.OnPreferenceChangedListener
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceKey
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceListener
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The purpose of this class is to schedule the job to perform the bus stop database update checks.
 *
 * @param context The application [Context].
 * @param jobScheduler The Android [JobScheduler].
 * @param preferenceManager An [AndroidPreferenceManager] instance.
 * @author Niall Scott
 */
@Singleton
internal class UpdateBusStopDatabaseJobScheduler @Inject constructor(
        private val context: Context,
        private val jobScheduler: JobScheduler,
        private val preferenceManager: AndroidPreferenceManager) {

    companion object {

        private const val JOB_ID = 1
        private const val JOB_PERIOD = 43200000L // 12 hours
    }

    private val preferenceChangedListener = object : OnPreferenceChangedListener {
        override fun onPreferenceChanged(preference: PreferenceKey?) {
            scheduleJob()
        }
    }

    init {
        PreferenceListener(preferenceChangedListener,
                setOf(PreferenceKey.DATABASE_UPDATE_WIFI_ONLY))
                .let(preferenceManager::addOnPreferenceChangedListener)
    }

    /**
     * Schedules the recurring job to update the bus stop database, if required.
     */
    fun scheduleUpdateBusStopDatabaseJob() {
        if (requiresScheduling()) {
            scheduleJob()
        }
    }

    /**
     * Does the job require scheduling?
     *
     * @return `true` if the job requires scheduling, which means there is no previously scheduled
     * job. Otherwise `false`.
     */
    private fun requiresScheduling() =
            jobScheduler.allPendingJobs
                    .find { it.id == JOB_ID } == null

    /**
     * Schedule the job.
     */
    private fun scheduleJob() {
        val networkType = if (preferenceManager.isBusStopDatabaseUpdateWifiOnly()) {
            JobInfo.NETWORK_TYPE_UNMETERED
        } else {
            JobInfo.NETWORK_TYPE_ANY
        }

        val componentName = ComponentName(context, DatabaseUpdateJobService::class.java)
        JobInfo.Builder(JOB_ID, componentName)
                .setPeriodic(JOB_PERIOD)
                .setRequiredNetworkType(networkType)
                .setPrefetchCompat(true)
                .setRequiresBatteryNotLowCompat(true)
                .setPersisted(true)
                .build()
                .also {
                    jobScheduler.schedule(it)
                }
    }
}