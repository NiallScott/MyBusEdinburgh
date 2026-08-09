/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
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
 * A fake [PreferenceRepository] for testing.
 *
 * @author Niall Scott
 */
class FakePreferenceRepository(
    private val onIsDatabaseUpdateWifiOnlyFlow: () -> Flow<Boolean> =
        { throw NotImplementedError() },
    private val onAppThemeFlow: () -> Flow<AppTheme> = { throw NotImplementedError() },
    private val onIsLiveTimesAutoRefreshEnabledFlow: () -> Flow<Boolean> =
        { throw NotImplementedError() },
    private val onIsLiveTimesSortByTimeFlow: () -> Flow<Boolean> = { throw NotImplementedError() },
    private val onLiveTimesNumberOfDeparturesFlow: () -> Flow<Int> =
        { throw NotImplementedError() },
    private val onIsGpsPromptDisabledFlow: () -> Flow<Boolean> = { throw NotImplementedError() },
    private val onIsMapZoomControlsVisibleFlow: () -> Flow<Boolean> =
        { throw NotImplementedError() },
    private val onLastMapCameraLocationFlow: () -> Flow<LastMapCameraLocation> =
        { throw NotImplementedError() },
    private val onMapTypeFlow: () -> Flow<Int> = { throw NotImplementedError() },
    private val onToggleSortByTime: () -> Unit = { throw NotImplementedError() },
    private val onToggleAutoRefresh: () -> Unit = { throw NotImplementedError() },
    private val onSetLastMapCameraLocation: (LastMapCameraLocation) -> Unit =
        { throw NotImplementedError() },
    private val onSetMapType: (Int) -> Unit = { throw NotImplementedError() }
) : PreferenceRepository {

    override val isDatabaseUpdateWifiOnlyFlow get() = onIsDatabaseUpdateWifiOnlyFlow()

    override val appThemeFlow get() = onAppThemeFlow()

    override val isLiveTimesAutoRefreshEnabledFlow get() = onIsLiveTimesAutoRefreshEnabledFlow()

    override val isLiveTimesSortByTimeFlow get() = onIsLiveTimesSortByTimeFlow()

    override val liveTimesNumberOfDeparturesFlow get() = onLiveTimesNumberOfDeparturesFlow()

    override val isGpsPromptDisabledFlow get() = onIsGpsPromptDisabledFlow()

    override val isMapZoomControlsVisibleFlow get() = onIsMapZoomControlsVisibleFlow()

    override val lastMapCameraLocationFlow get() = onLastMapCameraLocationFlow()

    override val mapTypeFlow get() = onMapTypeFlow()

    override suspend fun toggleSortByTime() {
        onToggleSortByTime()
    }

    override suspend fun toggleAutoRefresh() {
        onToggleAutoRefresh()
    }

    override suspend fun setLastMapCameraLocation(cameraLocation: LastMapCameraLocation) {
        onSetLastMapCameraLocation(cameraLocation)
    }

    override suspend fun setMapType(mapType: Int) {
        onSetMapType(mapType)
    }
}
