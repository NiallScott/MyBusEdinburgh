/*
 * Copyright (C) 2019 Niall 'Rivernile' Scott
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

import android.app.job.JobParameters
import android.app.job.JobService
import android.os.AsyncTask
import dagger.android.AndroidInjection
import uk.org.rivernile.android.bustracker.core.job.getNetworkCompat
import javax.inject.Inject

/**
 * This is the [JobService] for running the database update job. This class is minimal - the work
 * is performed in [DatabaseUpdateChecker].
 *
 * @author Niall Scott
 */
class DatabaseUpdateJobService : JobService() {

    @Inject
    lateinit var updateChecker: DatabaseUpdateChecker

    private var updateTask: UpdateTask? = null

    override fun onCreate() {
        AndroidInjection.inject(this)

        super.onCreate()
    }

    override fun onStartJob(params: JobParameters): Boolean {
        val network = params.getNetworkCompat()
        val socketFactory = network?.socketFactory
        val updateSession = updateChecker.createNewSession(socketFactory)
        updateTask = UpdateTask(params, updateSession, this::handleResult).apply {
            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null)
        }

        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        updateTask?.cancel()
        updateTask = null

        return true
    }

    private fun handleResult(params: JobParameters, result: Boolean) {
        jobFinished(params, !result)
        updateTask = null
    }

    private class UpdateTask(
            private val jobParameters: JobParameters,
            private val updateSession: DatabaseUpdateCheckerSession,
            private val resultHandler: (JobParameters, Boolean) -> Unit)
        : AsyncTask<Unit, Unit, Boolean>() {

        override fun doInBackground(vararg params: Unit?): Boolean {
            return updateSession.checkForDatabaseUpdates()
        }

        override fun onPostExecute(result: Boolean) {
            resultHandler(jobParameters, result)
        }

        fun cancel() {
            cancel(false)
            updateSession.cancel()
        }
    }
}