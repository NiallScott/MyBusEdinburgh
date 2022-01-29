/*
 * Copyright (C) 2020 - 2022 Niall 'Rivernile' Scott
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

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import uk.org.rivernile.android.bustracker.core.utils.TimeUtils
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * This class contains business logic for controlling the refresh and auto-refresh functionality.
 *
 * @param timeUtils Used to provide timestamps.
 * @author Niall Scott
 */
class RefreshController @Inject constructor(
        private val timeUtils: TimeUtils) {

    companion object {

        private const val AUTO_REFRESH_INTERVAL_MILLIS = 60000L
    }

    /**
     * This [ReceiveChannel] is used to trigger refreshes. Consumers will receive a new [Unit] when
     * a refresh is requested.
     */
    val refreshTriggerReceiveChannel: ReceiveChannel<Unit> get() = refreshTriggerChannel
    private val refreshTriggerChannel = Channel<Unit>(Channel.CONFLATED)

    // Initially set to true to cause the initial load.
    private var pendingRefresh = AtomicBoolean(true)

    private var isActive = false

    /**
     * Set the active state of this controller. Refreshes will not occur while in an inactive state.
     * This is based on the UI lifecycle, to prevent new refresh requests occurring while the UI is
     * not visible. If a refresh request is made while in the inactive state, it will be recorded
     * and then the refresh will be requested when next in the active state.
     *
     * @param isActive For refreshing purposes, are we active?
     */
    fun setActiveState(isActive: Boolean) {
        this.isActive = isActive

        if (isActive && pendingRefresh.compareAndSet(true, false)) {
            refreshTriggerChannel.trySend(Unit)
        }
    }

    /**
     * This suspend function requests that the live times are refreshed.
     *
     * If this object is in an active state, the refresh request will occur immediately. If
     * inactive, the request will occur when this object is next active.
     */
    suspend fun requestRefresh() {
        if (isActive) {
            refreshTriggerChannel.send(Unit)
        } else {
            pendingRefresh.set(true)
        }
    }

    /**
     * This should be called when the auto-refresh preference has been changed. It will determine
     * from [result] and [enabled] whether a refresh should take place now, and if so, it will
     * request the refresh.
     *
     * @param result The currently available [UiTransformedResult].
     * @param enabled `true` if auto-refresh is enabled, otherwise `false`.
     */
    suspend fun onAutoRefreshPreferenceChanged(result: UiTransformedResult?, enabled: Boolean) {
        if (enabled) {
            result?.let {
                getDelayUntilNextRefresh(it)?.let { delayMillis ->
                    if (delayMillis <= 0) {
                        requestRefresh()
                    }
                }
            }
        }
    }

    /**
     * Perform the auto-refresh delay if the current [result] is not
     * [UiTransformedResult.InProgress], and after the delay, execute the [predicate] to determine
     * if a refresh should be performed.
     *
     * The auto-refresh period is defined as [AUTO_REFRESH_INTERVAL_MILLIS], therefore the next
     * refresh will happen at the time of the last refresh + [AUTO_REFRESH_INTERVAL_MILLIS]. This
     * method uses the amount of time between now and the calculated timestamp to calculate the
     * length of delay. If the calculated delay is less than `0`, that is, the next refresh time
     * is in the past, the next refresh should happen immediately (if the [predicate] allows).
     *
     * @param result The last loaded [UiTransformedResult], used to calculate when the next refresh
     * time should be.
     * @param predicate This [predicate] is executed at the end of the auto-refresh delay period to
     * determine if a refresh should be performed or not.
     */
    suspend fun performAutoRefreshDelay(
            result: UiTransformedResult,
            predicate: () -> Boolean) {
        getDelayUntilNextRefresh(result)?.let {
            delay(it)

            if (predicate()) {
                requestRefresh()
            }
        }
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