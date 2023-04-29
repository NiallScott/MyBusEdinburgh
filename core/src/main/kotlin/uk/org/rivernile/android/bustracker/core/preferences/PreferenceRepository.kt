/*
 * Copyright (C) 2020 - 2023 Niall 'Rivernile' Scott
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

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * This repository is used to access application preference data.
 *
 * @param preferenceDataStorage Preference data storage.
 * @author Niall Scott
 */
class PreferenceRepository @Inject internal constructor(
    private val preferenceDataStorage: PreferenceDataStorage) {

    /**
     * A [Flow] which emits whether database updates should be over Wi-Fi only.
     */
    val isDatabaseUpdateWifiOnlyFlow get() = preferenceDataStorage.isDatabaseUpdateWifiOnlyFlow

    /**
     * A [Flow] which emits the [AppTheme] and will emit further values when this preference
     * changes.
     */
    val appThemeFlow get() = preferenceDataStorage.appThemeFlow

    /**
     * A [Flow] which emits alert notification preferences.
     */
    val alertNotificationPreferencesFlow get() =
        preferenceDataStorage.alertNotificationPreferencesFlow

    /**
     * A [Flow] which emits whether auto refresh is enabled by default, and will emit further values
     * when this preference changes.
     */
    val isLiveTimesAutoRefreshEnabledFlow get() =
        preferenceDataStorage.isLiveTimesAutoRefreshEnabledFlow

    /**
     * A [Flow] which emits whether night services should be shown or not, and will emit further
     * values when this preference changes.
     */
    val isLiveTimesShowNightServicesEnabledFlow get() =
        preferenceDataStorage.isLiveTimesShowNightServicesEnabledFlow

    /**
     * A [Flow] which emits whether live times are sorted by time, and will emit further values when
     * this preference changes.
     */
    val isLiveTimesSortByTimeFlow get() = preferenceDataStorage.isLiveTimesSortByTimeFlow

    /**
     * A [Flow] which emits the number of departures preference value and will emit further values
     * when this preference changes.
     */
    val liveTimesNumberOfDeparturesFlow get() =
        preferenceDataStorage.liveTimesNumberOfDeparturesFlow

    /**
     * A [Flow] which emits whether the GPS prompt is disabled.
     */
    val isGpsPromptDisabledFlow get() = preferenceDataStorage.isGpsPromptDisabledFlow

    /**
     * A [Flow] which emits whether the zoom controls should be visible on the map or not.
     */
    val isMapZoomControlsVisibleFlow get() =
        preferenceDataStorage.isMapZoomControlsVisibleFlow

    /**
     * A [Flow] which emits the last (most recently recorded) map camera location.
     */
    val lastMapCameraLocationFlow get() = preferenceDataStorage.lastMapCameraLocationFlow

    /**
     * A [Flow] which emits the last set map type.
     */
    val mapTypeFlow get() = preferenceDataStorage.mapTypeFlow

    /**
     * Toggle the sort by time preference.
     */
    suspend fun toggleSortByTime() {
        preferenceDataStorage.toggleSortByTime()
    }

    /**
     * Toggle the auto-refresh preference.
     */
    suspend fun toggleAutoRefresh() {
        preferenceDataStorage.toggleAutoRefresh()
    }

    /**
     * Set the value of the GPS prompt disabled preference.
     *
     * @param isDisabled Is the GPS prompt disabled?
     */
    suspend fun setIsGpsPromptDisabled(isDisabled: Boolean) {
        preferenceDataStorage.setIsGpsPromptDisabled(isDisabled)
    }

    /**
     * Set the value of the last map camera location preference.
     *
     * @param cameraLocation The last map camera location.
     */
    suspend fun setLastMapCameraLocation(cameraLocation: LastMapCameraLocation) {
        preferenceDataStorage.setLastMapCameraLocation(cameraLocation)
    }

    /**
     * Set the value of the map type preference.
     *
     * @param mapType The new value of the map type preference.
     */
    suspend fun setMapType(mapType: Int) {
        preferenceDataStorage.setMapType(mapType)
    }
}