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

package uk.org.rivernile.android.bustracker.ui.bustimes.times

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import uk.org.rivernile.android.bustracker.core.utils.TimeUtils
import javax.inject.Inject

/**
 * This class calculates the last refresh time to show to the user and emits updates via a
 * continuous [Flow] until cancelled.
 *
 * @param timeUtils The [TimeUtils] implementation.
 * @author Niall Scott
 */
class LastRefreshTimeCalculator @Inject constructor(
        private val timeUtils: TimeUtils) {

    private companion object {

        private const val CHECK_INTERVAL_MILLIS = 10000L
    }

    /**
     * Get a [Flow] which emits the [LastRefreshTime] based up [refreshTime] and the current device
     * time, as a continuously updating [Flow]. The [Flow] will terminate itself upon cancellation,
     * an unexpected state or if the number of minutes exceeds `59`.
     *
     * Emitted items are distinct.
     *
     * @param refreshTime The refresh time of the data.
     * @return A [Flow] which emits [LastRefreshTime]s on a continual basis until cancelled,
     * unexpected state is encountered or 60 minutes is reached.
     */
    fun getLastRefreshTimeFlow(refreshTime: Long): Flow<LastRefreshTime> = flow {
        if (refreshTime > 0) {
            while (true) {
                val lastRefreshTime = calculateLastRefreshTime(refreshTime)
                emit(lastRefreshTime)

                if (lastRefreshTime is LastRefreshTime.Now ||
                        lastRefreshTime is LastRefreshTime.Minutes) {
                    delay(CHECK_INTERVAL_MILLIS)
                } else {
                    break
                }
            }
        } else {
            // If the refresh time isn't valid, we've never refreshed so emit and terminate.
            emit(LastRefreshTime.Never)
        }
    }.distinctUntilChanged()

    /**
     * Calculate the [LastRefreshTime] to show the user.
     *
     * @param refreshTime The refresh time in UNIX milliseconds.
     * @return The [LastRefreshTime] to show the user.
     */
    private fun calculateLastRefreshTime(refreshTime: Long): LastRefreshTime {
        val numberOfMinutes = calculateNumberOfMinutes(refreshTime)

        return when {
            // We should hopefully never see this condition - this should only be seen if the user
            // changes their device clock. Not really sure what to do here, except not show a
            // negative time.
            numberOfMinutes < 0 -> LastRefreshTime.Never
            // If number of minutes is 0, the refresh happened within the last minute.
            numberOfMinutes == 0 -> LastRefreshTime.Now
            // We stop counting after 59 minutes.
            numberOfMinutes > 59 -> LastRefreshTime.MoreThanOneHour
            // If we reached here, the number of minutes must be between 1 and 59 (inclusive). In
            // that case, the user is shown the number of minutes.
            else -> LastRefreshTime.Minutes(numberOfMinutes)
        }
    }

    /**
     * Get the number of minutes between the [refreshTime] and the current device time.
     *
     * @param refreshTime The refresh time in UNIX milliseconds.
     * @return The number of minutes between now and the refresh time.
     */
    private fun calculateNumberOfMinutes(refreshTime: Long): Int {
        val timeDiff = timeUtils.getCurrentTimeMillis() - refreshTime

        return (timeDiff / 60000).toInt()
    }
}