/*
 * Copyright (C) 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.time

import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

/**
 * An implementation which provides helpers to calculate ongoing elapsed time. For example, to
 * display how old a piece of data is.
 *
 * @author Niall Scott
 */
public interface ElapsedTimeCalculator {

    /**
     * Get a [Flow] which emits the [ElapsedTimeMinutes] based on [eventTime] and the current
     * device wall time, as a continuously updating [Flow]. The [Flow] will terminate itself upon
     * cancellation, an unexpected state or if the number of minutes exceeds `59`.
     *
     * Emitted items are distinct.
     *
     * @param eventTime The event time to base the elapsed time on.
     * @return A [Flow] which emits [ElapsedTimeMinutes]s on a continual basis until cancelled,
     * unexpected state is encountered or 60 minutes is reached.
     */
    public fun getElapsedTimeMinutesFlow(eventTime: Long): Flow<ElapsedTimeMinutes>
}

/**
 * This class enumerates the different types of display of elapsed time for minutes.
 *
 * @author Niall Scott
 */
public sealed interface ElapsedTimeMinutes {

    /**
     * There is no event time. This is an unusual state, designed to encapsulate the case that the
     * current time is prior to the supplied event time.
     */
    public data object None : ElapsedTimeMinutes

    /**
     * The elapsed time is less than 1 minute.
     */
    public data object Now : ElapsedTimeMinutes

    /**
     * The elapsed time is these number of minutes.
     *
     * @property minutes The number of minutes for the elapsed time.
     */
    public data class Minutes(
        val minutes: Int
    ) : ElapsedTimeMinutes

    /**
     * The elapsed time is more than one hour ago.
     */
    public data object MoreThanOneHour : ElapsedTimeMinutes
}

private const val MILLISECONDS_PER_MINUTE = 60000
private const val CHECK_INTERVAL_MILLIS = 10000L

/**
 * This is a real implementation of [ElapsedTimeCalculator].
 *
 * @param timeUtils Used to get the real device time.
 * @author Niall Scott
 */
internal class RealElapsedTimeCalculator @Inject constructor(
    private val timeUtils: TimeUtils
) : ElapsedTimeCalculator {

    override fun getElapsedTimeMinutesFlow(eventTime: Long): Flow<ElapsedTimeMinutes> {
        return flow {
            if (eventTime > 0) {
                while (true) {
                    coroutineContext.ensureActive()
                    val lastRefreshTime = calculateLastRefreshTime(eventTime)
                    emit(lastRefreshTime)

                    if (lastRefreshTime is ElapsedTimeMinutes.Now ||
                        lastRefreshTime is ElapsedTimeMinutes.Minutes) {
                        delay(CHECK_INTERVAL_MILLIS)
                    } else {
                        break
                    }
                }
            } else {
                // If the event time isn't valid then emit the None value and stop.
                emit(ElapsedTimeMinutes.None)
            }
        }.distinctUntilChanged()
    }

    /**
     * Calculate the [ElapsedTimeMinutes] to show the user.
     *
     * @param eventTime The event time in UNIX milliseconds.
     * @return The [ElapsedTimeMinutes] to show the user.
     */
    private fun calculateLastRefreshTime(eventTime: Long): ElapsedTimeMinutes {
        val numberOfMinutes = calculateNumberOfMinutes(eventTime)

        return when {
            // We should hopefully never see this condition - this should only be seen if the user
            // changes their device clock. Not really sure what to do here, except not show a
            // negative time.
            numberOfMinutes < 0 -> ElapsedTimeMinutes.None
            // If number of minutes is 0, the elapsed time is roughly now.
            numberOfMinutes == 0 -> ElapsedTimeMinutes.Now
            // We stop counting after 59 minutes.
            numberOfMinutes > 59 -> ElapsedTimeMinutes.MoreThanOneHour
            // If we reached here, the number of minutes must be between 1 and 59 (inclusive). In
            // that case, the user is shown the number of minutes.
            else -> ElapsedTimeMinutes.Minutes(numberOfMinutes)
        }
    }

    /**
     * Get the number of minutes between the [eventTime] and the current device time.
     *
     * @param eventTime The event time in UNIX milliseconds.
     * @return The number of minutes between now and the event time.
     */
    private fun calculateNumberOfMinutes(eventTime: Long): Int {
        val timeDiff = timeUtils.currentTimeMills - eventTime

        return (timeDiff / MILLISECONDS_PER_MINUTE).toInt()
    }
}