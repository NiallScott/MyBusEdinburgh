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

import android.app.job.JobParameters
import android.app.job.JobService
import dagger.android.AndroidInjection
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.org.rivernile.android.bustracker.core.di.ForIoDispatcher
import uk.org.rivernile.android.bustracker.core.di.ForMainDispatcher
import uk.org.rivernile.android.bustracker.core.job.getNetworkCompat
import javax.inject.Inject
import javax.net.SocketFactory

/**
 * This is the [JobService] for running the database update job. This class is minimal - the work
 * is performed in [DatabaseUpdateChecker].
 *
 * @author Niall Scott
 */
class DatabaseUpdateJobService : JobService() {

    @Inject
    lateinit var updateChecker: DatabaseUpdateChecker
    @Inject
    @ForMainDispatcher
    lateinit var mainDispatcher: CoroutineDispatcher
    @Inject
    @ForIoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    private val jobServiceScope by lazy {
        CoroutineScope(mainDispatcher + SupervisorJob())
    }

    override fun onCreate() {
        AndroidInjection.inject(this)

        super.onCreate()
    }

    override fun onStartJob(params: JobParameters): Boolean {
        jobServiceScope.launch {
            val socketFactory = params.getNetworkCompat()?.socketFactory
            val taskResult = performUpdateTask(socketFactory)
            jobFinished(params, !taskResult)
        }

        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        jobServiceScope.cancel()

        return true
    }

    /**
     * Perform the update task on a background thread as a coroutine.
     *
     * @param socketFactory An optional [SocketFactory], if one was specified to be used by the
     * platform in the [JobParameters].
     * @return `true` if the job was successful, otherwise `false`.
     */
    private suspend fun performUpdateTask(
            socketFactory: SocketFactory?) = withContext(ioDispatcher) {
        val updateSession = updateChecker.createNewSession(socketFactory)

        try {
            updateSession.checkForDatabaseUpdates()
        } catch (e: CancellationException) {
            updateSession.cancel()
            throw e
        }
    }
}