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

package uk.org.rivernile.android.bustracker.core.preferences

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.di.ForApplicationCoroutineScope
import javax.inject.Inject

/**
 * This repository is used to access application preference data.
 *
 * @param preferenceManager Used to access the preference backing store.
 * @param applicationCoroutineScope The application [CoroutineScope].
 * @param defaultDispatcher The [CoroutineDispatcher] to perform processing operations on.
 * @author Niall Scott
 */
class PreferenceRepository @Inject constructor(
        private val preferenceManager: PreferenceManager,
        @ForApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
        @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher) {

    /**
     * Get a [Flow] which returns whether auto refresh is enabled by default, and will emit further
     * values when this preference changes.
     *
     * @return The [Flow] which emits whether auto refresh is enabled by default.
     */
    fun isLiveTimesAutoRefreshEnabledFlow(): Flow<Boolean> {
        return getPreferenceFlow(PreferenceKey.LIVE_TIMES_AUTO_REFRESH_ENABLED) {
            preferenceManager.isBusTimesAutoRefreshEnabled()
        }
    }

    /**
     * Get a [Flow] which returns whether night services should be shown or not, and will emit
     * further values when this preference changes.
     *
     * @return The [Flow] which emits whether night services should be shown or not.
     */
    fun isLiveTimesShowNightServicesEnabledFlow(): Flow<Boolean> {
        return getPreferenceFlow(PreferenceKey.LIVE_TIMES_SHOW_NIGHT_SERVICES) {
            preferenceManager.isBusTimesShowingNightServices()
        }
    }

    /**
     * Get a [Flow] which returns whether live times are sorted by time, and will emit further
     * values when this preference changes.
     *
     * @return The [Flow] which emits whether live times are sorted by time.
     */
    fun isLiveTimesSortByTimeFlow(): Flow<Boolean> {
        return getPreferenceFlow(PreferenceKey.LIVE_TIMES_SORT_BY_TIME) {
            preferenceManager.isBusTimesSortedByTime()
        }
    }

    /**
     * Get a [Flow] which returns the number of departures preference value and will emit further
     * values when this preference changes.
     *
     * @return The [Flow] which emits the number of departures to show.
     */
    fun getLiveTimesNumberOfDeparturesFlow(): Flow<Int> {
        return getPreferenceFlow(PreferenceKey.LIVE_TIMES_NUMBER_OF_DEPARTURES) {
            preferenceManager.getBusTimesNumberOfDeparturesToShowPerService()
        }
    }

    /**
     * Is the GPS prompt disabled?
     */
    var isGpsPromptDisabled
        get() = preferenceManager.isGpsPromptDisabled()
        set(value) {
            preferenceManager.setGpsPromptDisabled(value)
        }

    /**
     * Toggle the sort by time preference.
     */
    fun toggleSortByTime() {
        applicationCoroutineScope.launch(defaultDispatcher) {
            preferenceManager.setBusTimesSortedByTime(!preferenceManager.isBusTimesSortedByTime())
        }
    }

    /**
     * Toggle the auto-refresh preference.
     */
    fun toggleAutoRefresh() {
        applicationCoroutineScope.launch(defaultDispatcher) {
            preferenceManager.setBusTimesAutoRefreshEnabled(
                    !preferenceManager.isBusTimesAutoRefreshEnabled())
        }
    }

    /**
     * The last last (most recently recorded) map camera location.
     */
    var lastMapCameraLocation: LastMapCameraLocation
        get() {
            return LastMapCameraLocation(
                    preferenceManager.getLastMapLatitude(),
                    preferenceManager.getLastMapLongitude(),
                    preferenceManager.getLastMapZoomLevel())
        }
        set(value) {
            preferenceManager.setLastMapLatitude(value.latitude)
            preferenceManager.setLastMapLongitude(value.longitude)
            preferenceManager.setLastMapZoomLevel(value.zoomLevel)
        }

    /**
     * Get a [Flow] which returns the preference value obtained from [block], and identified by the
     * supplied [key], which is observed for changes. Changes will cause further emissions.
     *
     * @param T The type of data for the preference.
     * @param key The [PreferenceKey] to observe for.
     * @param block This block is executed to retrieve the preference value. It will be executed on
     * the default [CoroutineDispatcher].
     */
    private fun <T> getPreferenceFlow(key: PreferenceKey, block: () -> T) = callbackFlow {
        val listener = object : OnPreferenceChangedListener {
            override fun onPreferenceChanged(preference: PreferenceKey?) {
                if (key == preference) {
                    launch {
                        getAndSendPreference(channel, block)
                    }
                }
            }
        }

        preferenceManager.addOnPreferenceChangedListener(PreferenceListener(listener, setOf(key)))
        getAndSendPreference(channel, block)

        awaitClose {
            preferenceManager.removeOnPreferenceChangedListener(listener)
        }
    }

    /**
     * A suspended function which obtains the preference from the supplied [block], and then sends
     * the retrieved preference to the given [channel].
     *
     * @param T The type of data for the preference.
     * @param channel The [SendChannel] that emissions should be sent to.
     * @param block This block is executed to retrieve the preference value. It will be executed on
     * the default [CoroutineDispatcher].
     */
    private suspend fun <T> getAndSendPreference(
            channel: SendChannel<T>,
            block: () -> T) = withContext(defaultDispatcher) {
        channel.send(block())
    }
}