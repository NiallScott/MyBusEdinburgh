/*
 * Copyright (C) 2024 - 2026 Niall 'Rivernile' Scott
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
 * A fake [PreferenceDataStorage] for testing.
 *
 * @author Niall Scott
 */
class FakePreferenceDataStorage(
    private val onIsDatabaseUpdateWifiOnlyFlow: () -> Flow<Boolean> =
        { throw NotImplementedError() },
    private val onAppThemeFlow: () -> Flow<AppTheme> = { throw NotImplementedError() },
    private val onIsLiveTimesAutoRefreshEnabledFlow: () -> Flow<Boolean> =
        { throw NotImplementedError() },
    private val onIsLiveTimesSortByTimeFlow: () -> Flow<Boolean> = { throw NotImplementedError() },
    private val onLiveTimesNumberOfDeparturesFlow: () -> Flow<Int> =
        { throw NotImplementedError() },
    private val onIsMapZoomControlsVisibleFlow: () -> Flow<Boolean> =
        { throw NotImplementedError() },
    private val onIsGpsPromptDisabledFlow: () -> Flow<Boolean> = { throw NotImplementedError() },
    private val onLastMapCameraLocationFlow: () -> Flow<LastMapCameraLocation> =
        { throw NotImplementedError() },
    private val onMapTypeFlow: () -> Flow<Int> = { throw NotImplementedError() }
) : PreferenceDataStorage {

    var toggleSortByTimeInvocationCount = 0
        private set

    var toggleAutoRefreshInvocationCount = 0
        private set

    var isGpsPromptDisabled = false
        private set

    var lastMapCameraLocation: LastMapCameraLocation? = null
        private set

    var mapType = -1
        private set

    override val isDatabaseUpdateWifiOnlyFlow: Flow<Boolean>
        get() = onIsDatabaseUpdateWifiOnlyFlow()

    override val appThemeFlow: Flow<AppTheme>
        get() = onAppThemeFlow()

    override val isLiveTimesAutoRefreshEnabledFlow: Flow<Boolean>
        get() = onIsLiveTimesAutoRefreshEnabledFlow()

    override val isLiveTimesSortByTimeFlow: Flow<Boolean>
        get() = onIsLiveTimesSortByTimeFlow()

    override val liveTimesNumberOfDeparturesFlow: Flow<Int>
        get() = onLiveTimesNumberOfDeparturesFlow()

    override val isMapZoomControlsVisibleFlow: Flow<Boolean>
        get() = onIsMapZoomControlsVisibleFlow()

    override val isGpsPromptDisabledFlow: Flow<Boolean>
        get() = onIsGpsPromptDisabledFlow()

    override val lastMapCameraLocationFlow: Flow<LastMapCameraLocation>
        get() = onLastMapCameraLocationFlow()

    override val mapTypeFlow: Flow<Int>
        get() = onMapTypeFlow()

    override suspend fun toggleSortByTime() {
        toggleSortByTimeInvocationCount++
    }

    override suspend fun toggleAutoRefresh() {
        toggleAutoRefreshInvocationCount++
    }

    override suspend fun setIsGpsPromptDisabled(isDisabled: Boolean) {
        isGpsPromptDisabled = isDisabled
    }

    override suspend fun setLastMapCameraLocation(cameraLocation: LastMapCameraLocation) {
        lastMapCameraLocation = cameraLocation
    }

    override suspend fun setMapType(mapType: Int) {
        this.mapType = mapType
    }
}
