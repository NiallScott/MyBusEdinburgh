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

package uk.org.rivernile.android.bustracker.core.alerts.arrivals

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

/**
 * This class runs checks of the arrival alerts on a time period. While running, this runner will
 * stop itself if there are no more active alerts set by throwing a [CancellationException].
 *
 * @param checkTimesTask The implementation for performing the check of the arrival alerts.
 * @param alertsRepository The repository to access the currently set arrival alerts.
 * @author Niall Scott
 */
class TimeAlertRunner @Inject internal constructor(
        private val checkTimesTask: CheckTimesTask,
        private val alertsRepository: AlertsRepository) {

    companion object {

        private const val CHECK_TIMES_INTERVAL_MILLIS = 60000L
    }

    /**
     * Run this [TimeAlertRunner]. This performs the check for live times, and keeps the coroutine
     * going unless and until the number of arrival alerts is less than 1. If the number of arrival
     * alerts is less than 1, a [CancellationException] will be thrown.
     */
    suspend fun run() {
        alertsRepository
            .arrivalAlertCountFlow
            .runTimesCheck()
            .collect {
                if (it < 1) {
                    throw CancellationException()
                }
            }
    }

    /**
     * Run the arrival times check on a timer until the coroutine is cancelled.
     */
    private suspend fun runCheck() {
        while (true) {
            coroutineContext.ensureActive()

            checkTimesTask.checkTimes()
            delay(CHECK_TIMES_INTERVAL_MILLIS)
        }
    }

    /**
     * This [Flow] takes the values from upstream (the number of arrival alerts) and re-emits them
     * downstream. Meanwhile, it also runs a coroutine that performs the live times check on a
     * timer.
     *
     * @return A [Flow] which performs the live times check and emits the number of set arrival
     * alerts.
     */
    private fun Flow<Int>.runTimesCheck() = channelFlow {
        var checkJob: Job? = null

        collect {
            if (it > 0 && checkJob == null) {
                checkJob = launch {
                    runCheck()
                }
            }

            send(it)
        }
    }
}