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
import uk.org.rivernile.android.bustracker.core.utils.TimeUtils
import javax.inject.Inject

/**
 * This class contains business logic for controlling the auto-refresh functionality.
 *
 * @param timeUtils Used to provide timestamps.
 * @author Niall Scott
 */
class AutoRefreshController @Inject constructor(
        private val timeUtils: TimeUtils) {

    companion object {

        private const val AUTO_REFRESH_INTERVAL_MILLIS = 60000L
    }

    /**
     * Should a refresh be triggered now? If this preference has been changed to the enabled
     * state, the time of the last refresh, as reported by [result], will be inspected and if
     * this is older than [AUTO_REFRESH_INTERVAL_MILLIS], this method will return `true`.
     *
     * @param result The currently available [UiTransformedResult].
     * @param enabled `true` if auto-refresh has been enabled, otherwise `false`.
     * @return `true` if a refresh should occur now, otherwise `false`.
     */
    fun shouldCauseRefresh(result: UiTransformedResult?, enabled: Boolean): Boolean {
        if (enabled) {
            result?.let {
                getDelayUntilNextRefresh(it)?.let { delayMillis ->
                    if (delayMillis <= 0) {
                        return true
                    }
                }
            }
        }

        return false
    }

    /**
     * Perform the auto-refresh delay if the current [result] is not
     * [UiTransformedResult.InProgress], and then return `true` if this was the case, otherwise
     * return `false`.
     *
     * The auto-refresh period is defined as [AUTO_REFRESH_INTERVAL_MILLIS], therefore the next
     * refresh will happen at the time of the last refresh + [AUTO_REFRESH_INTERVAL_MILLIS]. This
     * method uses the amount of time between now and the calculated timestamp to calculate the
     * length of delay. If the calculated delay is less than `0`, that is, the next refresh time
     * is in the past, the next refresh should happen immediately.
     *
     * @param result The last loaded [UiTransformedResult], used to calculate when the next refresh
     * time should be.
     * @return `true` if the [result] state allows for a auto-refresh, otherwise `false`.
     */
    suspend fun performAutoRefreshDelay(result: UiTransformedResult): Boolean {
        return getDelayUntilNextRefresh(result)?.let {
            delay(it)

            true
        } ?: false
    }

    /**
     * Get the amount of time in milliseconds until the next refresh should occur (if auto-refresh
     * is enabled). A negative or `0` value means a refresh should occur immediately. A positive
     * value means there should be a delay until the next refresh happens. A `null` value means no
     * refresh or delay should happen.
     *
     * @param result The last loaded [UiTransformedResult], used to calculate when the next refresh
     * time should be.
     * @return The number of milliseconds until the next refresh, or `null` if a refresh should not
     * occur.
     */
    private fun getDelayUntilNextRefresh(result: UiTransformedResult): Long? {
        return when (result) {
            is UiTransformedResult.Success -> result.receiveTime
            is UiTransformedResult.Error -> result.receiveTime
            else -> null
        }?.let {
            it + AUTO_REFRESH_INTERVAL_MILLIS - timeUtils.getCurrentTimeMillis()
        }
    }
}