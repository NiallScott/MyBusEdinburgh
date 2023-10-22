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
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule

/**
 * Tests for [PreferenceRepository].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class PreferenceRepositoryTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var dataStorage: PreferenceDataStorage

    private lateinit var repository: PreferenceRepository

    @Before
    fun setUp() {
        repository = PreferenceRepository(dataStorage)
    }

    @Test
    fun isDatabaseUpdateWifiOnlyFlowReturnsFlowFromPreferenceDataStorage() = runTest {
        val flow = mock<Flow<Boolean>>()
        whenever(dataStorage.isDatabaseUpdateWifiOnlyFlow)
            .thenReturn(flow)

        val result = repository.isDatabaseUpdateWifiOnlyFlow

        assertSame(flow, result)
    }

    @Test
    fun appThemeFlowReturnsFlowFromPreferenceDataStorage() = runTest {
        val flow = mock<Flow<AppTheme>>()
        whenever(dataStorage.appThemeFlow)
            .thenReturn(flow)

        val result = repository.appThemeFlow

        assertSame(flow, result)
    }

    @Test
    fun alertNotificationPreferencesFlowReturnsFlowFromPreferenceDataStorage() = runTest {
        val flow = mock<Flow<AlertNotificationPreferences>>()
        whenever(dataStorage.alertNotificationPreferencesFlow)
            .thenReturn(flow)

        val result = repository.alertNotificationPreferencesFlow

        assertSame(flow, result)
    }

    @Test
    fun isLiveTimesAutoRefreshEnabledFlowReturnsFlowFromPreferenceDataStorage() = runTest {
        val flow = mock<Flow<Boolean>>()
        whenever(dataStorage.isLiveTimesAutoRefreshEnabledFlow)
            .thenReturn(flow)

        val result = repository.isLiveTimesAutoRefreshEnabledFlow

        assertSame(flow, result)
    }

    @Test
    fun isLiveTimesShowNightServicesEnabledFlowReturnsFlowFromPreferenceDataStorage() = runTest {
        val flow = mock<Flow<Boolean>>()
        whenever(dataStorage.isLiveTimesShowNightServicesEnabledFlow)
            .thenReturn(flow)

        val result = repository.isLiveTimesShowNightServicesEnabledFlow

        assertSame(flow, result)
    }

    @Test
    fun isLiveTimesSortByTimeFlowReturnsFlowFromPreferenceDataStorage() = runTest {
        val flow = mock<Flow<Boolean>>()
        whenever(dataStorage.isLiveTimesSortByTimeFlow)
            .thenReturn(flow)

        val result = repository.isLiveTimesSortByTimeFlow

        assertSame(flow, result)
    }

    @Test
    fun liveTimesNumberOfDeparturesFlowReturnsFlowFromPreferenceDataStorage() = runTest {
        val flow = mock<Flow<Int>>()
        whenever(dataStorage.liveTimesNumberOfDeparturesFlow)
            .thenReturn(flow)

        val result = repository.liveTimesNumberOfDeparturesFlow

        assertSame(flow, result)
    }

    @Test
    fun isGpsPromptDisabledFlowReturnsFlowFromPreferenceDataStorage() = runTest {
        val flow = mock<Flow<Boolean>>()
        whenever(dataStorage.isGpsPromptDisabledFlow)
            .thenReturn(flow)

        val result = repository.isGpsPromptDisabledFlow

        assertSame(flow, result)
    }

    @Test
    fun isMapZoomControlsVisibleFlowReturnsFlowFromPreferenceDataStorage() = runTest {
        val flow = mock<Flow<Boolean>>()
        whenever(dataStorage.isMapZoomControlsVisibleFlow)
            .thenReturn(flow)

        val result = repository.isMapZoomControlsVisibleFlow

        assertSame(flow, result)
    }

    @Test
    fun lastMapCameraLocationFlowReturnsFlowFromPreferenceDataStorage() = runTest {
        val flow = mock<Flow<LastMapCameraLocation>>()
        whenever(dataStorage.lastMapCameraLocationFlow)
            .thenReturn(flow)

        val result = repository.lastMapCameraLocationFlow

        assertSame(flow, result)
    }

    @Test
    fun mapTypeFlowReturnsFlowFromPreferenceDatsStorage() = runTest {
        val flow = mock<Flow<Int>>()
        whenever(dataStorage.mapTypeFlow)
            .thenReturn(flow)

        val result = repository.mapTypeFlow

        assertSame(flow, result)
    }

    @Test
    fun toggleSortByTimeCallsMethodInPreferenceDataStorage() = runTest {
        repository.toggleSortByTime()

        verify(dataStorage)
            .toggleSortByTime()
    }

    @Test
    fun toggleAutoRefreshCallsMethodInPreferenceDataStorage() = runTest {
        repository.toggleAutoRefresh()

        verify(dataStorage)
            .toggleAutoRefresh()
    }

    @Test
    fun setIsGpsPromptDisabledCallsMethodInPreferenceDataStorage() = runTest {
        repository.setIsGpsPromptDisabled(true)

        verify(dataStorage)
            .setIsGpsPromptDisabled(true)
    }

    @Test
    fun setLastMapCameraLocationCallsMethodInPreferenceDataStorage() = runTest {
        val cameraLocation = LastMapCameraLocation(1.1, 2.2, 3f)
        repository.setLastMapCameraLocation(cameraLocation)

        verify(dataStorage)
            .setLastMapCameraLocation(cameraLocation)
    }

    @Test
    fun setMapTypeCallsMethodInPreferenceDataStorage() = runTest {
        repository.setMapType(1)

        verify(dataStorage)
            .setMapType(1)
    }
}