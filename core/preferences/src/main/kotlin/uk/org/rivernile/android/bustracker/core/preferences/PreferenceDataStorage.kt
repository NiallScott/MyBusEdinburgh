/*
 * Copyright (C) 2023 - 2026 Niall 'Rivernile' Scott
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

/**
 * This interface defines data storage access for preferences.
 *
 * @author Niall Scott
 */
public interface PreferenceDataStorage {

    /**
     * A [Flow] which emits whether database updates should happen over Wi-Fi only.
     */
    public val isDatabaseUpdateWifiOnlyFlow: Flow<Boolean>

    /**
     * A [Flow] which emits the [AppTheme] and will emit further values when this preference
     * changes.
     */
    public val appThemeFlow: Flow<AppTheme>

    /**
     * A [Flow] which emits whether auto refresh is enabled by default, and will emit further
     * values when this preference changes.
     */
    public val isLiveTimesAutoRefreshEnabledFlow: Flow<Boolean>

    /**
     * A [Flow] which emits whether live times are sorted by time, and will emit further values when
     * this preference changes.
     */
    public val isLiveTimesSortByTimeFlow: Flow<Boolean>

    /**
     * A [Flow] which returns the number of departures preference value and will emit further values
     * when this preference changes.
     */
    public val liveTimesNumberOfDeparturesFlow: Flow<Int>

    /**
     * A [Flow] which emits whether the zoom controls should be visible on the map or not.
     */
    public val isMapZoomControlsVisibleFlow: Flow<Boolean>

    /**
     * A [Flow] which emits whether the GPS prompt is disabled.
     */
    public val isGpsPromptDisabledFlow: Flow<Boolean>

    /**
     * A [Flow] which emits the last (most recently recorded) map camera location.
     */
    public val lastMapCameraLocationFlow: Flow<LastMapCameraLocation>

    /**
     * A [Flow] which emits the last set map type.
     */
    public val mapTypeFlow: Flow<Int>

    /**
     * Toggle the sort by time preference.
     */
    public suspend fun toggleSortByTime()

    /**
     * Toggle the auto-refresh preference.
     */
    public suspend fun toggleAutoRefresh()

    /**
     * Set the value of the GPS prompt disabled preference.
     *
     * @param isDisabled Is the GPS prompt disabled?
     */
    public suspend fun setIsGpsPromptDisabled(isDisabled: Boolean)

    /**
     * Set the value of the last map camera location preference.
     *
     * @param cameraLocation The last map camera location.
     */
    public suspend fun setLastMapCameraLocation(cameraLocation: LastMapCameraLocation)

    /**
     * Set the value of the map type preference.
     *
     * @param mapType The new value of the map type preference.
     */
    public suspend fun setMapType(mapType: Int)
}
