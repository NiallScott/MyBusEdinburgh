/*
 * Copyright (C) 2020 - 2026 Niall 'Rivernile' Scott
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

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [PreferenceRepository].
 *
 * @author Niall Scott
 */
class PreferenceRepositoryTest {

    @Test
    fun isDatabaseUpdateWifiOnlyFlowReturnsFlowFromPreferenceDataStorage() = runTest {
        val repository = createPreferenceRepository(
            preferenceDataStorage = FakePreferenceDataStorage(
                onIsDatabaseUpdateWifiOnlyFlow = { flowOf(false, true, false) }
            )
        )

        repository.isDatabaseUpdateWifiOnlyFlow.test {
            assertFalse(awaitItem())
            assertTrue(awaitItem())
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun appThemeFlowReturnsFlowFromPreferenceDataStorage() = runTest {
        val repository = createPreferenceRepository(
            preferenceDataStorage = FakePreferenceDataStorage(
                onAppThemeFlow = { flowOf(AppTheme.SYSTEM_DEFAULT, AppTheme.LIGHT, AppTheme.DARK) }
            )
        )

        repository.appThemeFlow.test {
            assertEquals(AppTheme.SYSTEM_DEFAULT, awaitItem())
            assertEquals(AppTheme.LIGHT, awaitItem())
            assertEquals(AppTheme.DARK, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun isLiveTimesAutoRefreshEnabledFlowReturnsFlowFromPreferenceDataStorage() = runTest {
        val repository = createPreferenceRepository(
            preferenceDataStorage = FakePreferenceDataStorage(
                onIsLiveTimesAutoRefreshEnabledFlow = { flowOf(false, true, false) }
            )
        )

        repository.isLiveTimesAutoRefreshEnabledFlow.test {
            assertFalse(awaitItem())
            assertTrue(awaitItem())
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun isLiveTimesSortByTimeFlowReturnsFlowFromPreferenceDataStorage() = runTest {
        val repository = createPreferenceRepository(
            preferenceDataStorage = FakePreferenceDataStorage(
                onIsLiveTimesSortByTimeFlow = { flowOf(false, true, false) }
            )
        )

        repository.isLiveTimesSortByTimeFlow.test {
            assertFalse(awaitItem())
            assertTrue(awaitItem())
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun liveTimesNumberOfDeparturesFlowReturnsFlowFromPreferenceDataStorage() = runTest {
        val repository = createPreferenceRepository(
            preferenceDataStorage = FakePreferenceDataStorage(
                onLiveTimesNumberOfDeparturesFlow = { flowOf(1, 2, 3) }
            )
        )

        repository.liveTimesNumberOfDeparturesFlow.test {
            assertEquals(1, awaitItem())
            assertEquals(2, awaitItem())
            assertEquals(3, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun isGpsPromptDisabledFlowReturnsFlowFromPreferenceDataStorage() = runTest {
        val repository = createPreferenceRepository(
            preferenceDataStorage = FakePreferenceDataStorage(
                onIsGpsPromptDisabledFlow = { flowOf(false, true, false) }
            )
        )

        repository.isGpsPromptDisabledFlow.test {
            assertFalse(awaitItem())
            assertTrue(awaitItem())
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun isMapZoomControlsVisibleFlowReturnsFlowFromPreferenceDataStorage() = runTest {
        val repository = createPreferenceRepository(
            preferenceDataStorage = FakePreferenceDataStorage(
                onIsMapZoomControlsVisibleFlow = { flowOf(false, true, false) }
            )
        )

        repository.isMapZoomControlsVisibleFlow.test {
            assertFalse(awaitItem())
            assertTrue(awaitItem())
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun lastMapCameraLocationFlowReturnsFlowFromPreferenceDataStorage() = runTest {
        val first = LastMapCameraLocation(1.1, 1.2, 1f)
        val second = LastMapCameraLocation(2.1, 2.2, 2f)
        val third = LastMapCameraLocation(3.1, 3.2, 3f)
        val repository = createPreferenceRepository(
            preferenceDataStorage = FakePreferenceDataStorage(
                onLastMapCameraLocationFlow = { flowOf(first, second, third) }
            )
        )

        repository.lastMapCameraLocationFlow.test {
            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
            assertEquals(third, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun mapTypeFlowReturnsFlowFromPreferenceDatsStorage() = runTest {
        val repository = createPreferenceRepository(
            preferenceDataStorage = FakePreferenceDataStorage(
                onMapTypeFlow = { flowOf(1, 2, 3) }
            )
        )

        repository.mapTypeFlow.test {
            assertEquals(1, awaitItem())
            assertEquals(2, awaitItem())
            assertEquals(3, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun toggleSortByTimeCallsMethodInPreferenceDataStorage() = runTest {
        val dataStorage = FakePreferenceDataStorage()
        val repository = createPreferenceRepository(preferenceDataStorage = dataStorage)

        repository.toggleSortByTime()

        assertEquals(1, dataStorage.toggleSortByTimeInvocationCount)
    }

    @Test
    fun toggleAutoRefreshCallsMethodInPreferenceDataStorage() = runTest {
        val dataStorage = FakePreferenceDataStorage()
        val repository = createPreferenceRepository(preferenceDataStorage = dataStorage)

        repository.toggleAutoRefresh()

        assertEquals(1, dataStorage.toggleAutoRefreshInvocationCount)
    }

    @Test
    fun setIsGpsPromptDisabledCallsMethodInPreferenceDataStorage() = runTest {
        val dataStorage = FakePreferenceDataStorage()
        val repository = createPreferenceRepository(preferenceDataStorage = dataStorage)

        repository.setIsGpsPromptDisabled(true)

        assertTrue(dataStorage.isGpsPromptDisabled)
    }

    @Test
    fun setLastMapCameraLocationCallsMethodInPreferenceDataStorage() = runTest {
        val dataStorage = FakePreferenceDataStorage()
        val repository = createPreferenceRepository(preferenceDataStorage = dataStorage)

        val cameraLocation = LastMapCameraLocation(1.1, 2.2, 3f)
        repository.setLastMapCameraLocation(cameraLocation)

        assertEquals(cameraLocation, dataStorage.lastMapCameraLocation)
    }

    @Test
    fun setMapTypeCallsMethodInPreferenceDataStorage() = runTest {
        val dataStorage = FakePreferenceDataStorage()
        val repository = createPreferenceRepository(preferenceDataStorage = dataStorage)

        repository.setMapType(1)

        assertEquals(1, dataStorage.mapType)
    }

    private fun createPreferenceRepository(
        preferenceDataStorage: PreferenceDataStorage = FakePreferenceDataStorage()
    ): PreferenceRepository {
        return PreferenceRepository(preferenceDataStorage)
    }
}
