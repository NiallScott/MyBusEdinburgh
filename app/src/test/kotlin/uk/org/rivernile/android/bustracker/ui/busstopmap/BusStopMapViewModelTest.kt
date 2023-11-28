/*
 * Copyright (C) 2018 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.busstopmap

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopLocation
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.core.preferences.LastMapCameraLocation
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceRepository
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import uk.org.rivernile.android.bustracker.testutils.test

/**
 * Tests for [BusStopMapViewModel].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class BusStopMapViewModelTest {

    companion object {

        private const val STATE_SELECTED_SERVICES = "selectedServices"
        private const val STATE_SELECTED_STOP_CODE = "selectedStopCode"
        private const val STATE_TRAFFIC_VIEW_ENABLED = "trafficViewEnabled"

        private const val DEFAULT_ZOOM = 14f
        private const val STOP_ZOOM = 20f
    }

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var permissionHandler: PermissionHandler
    @Mock
    private lateinit var playServicesAvailabilityChecker: PlayServicesAvailabilityChecker
    @Mock
    private lateinit var servicesRepository: ServicesRepository
    @Mock
    private lateinit var busStopsRepository: BusStopsRepository
    @Mock
    private lateinit var stopMarkersRetriever: StopMarkersRetriever
    @Mock
    private lateinit var routeLineRetriever: RouteLineRetriever
    @Mock
    private lateinit var isMyLocationEnabledDetector: IsMyLocationEnabledDetector
    @Mock
    private lateinit var preferenceRepository: PreferenceRepository

    @Test
    fun setPermissionsStateSetsValueOnPermissionHandler() {
        val expected = PermissionsState(PermissionState.GRANTED, PermissionState.GRANTED)
        val viewModel = createViewModel()

        viewModel.permissionsState = expected

        verify(permissionHandler)
                .permissionsState = expected
    }

    @Test
    fun getPermissionsStateGetsValueOnPermissionsHandler() {
        val expected = PermissionsState(PermissionState.GRANTED, PermissionState.GRANTED)
        whenever(permissionHandler.permissionsState)
                .thenReturn(expected)
        val viewModel = createViewModel()

        val result = viewModel.permissionsState

        assertEquals(expected, result)
    }

    @Test
    fun requestLocationPermissionsLiveDataDoesNotEmitWhenPermissionsHandlerDoesNotEmit() = runTest {
        whenever(permissionHandler.requestLocationPermissionsFlow)
                .thenReturn(emptyFlow())
        val viewModel = createViewModel()

        val observer = viewModel.requestLocationPermissionsLiveData.test()
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun requestLocationPermissionsLiveDataEmitsWhenPermissionsHandlerEmits() = runTest {
        whenever(permissionHandler.requestLocationPermissionsFlow)
                .thenReturn(flowOf(123L))
        val viewModel = createViewModel()

        val observer = viewModel.requestLocationPermissionsLiveData.test()
        advanceUntilIdle()

        observer.assertSize(1)
    }

    @Test
    fun uiStateLiveDataEmitsProgressWhenPlayServicesAvailabilityIsInProgress() = runTest {
        whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
                .thenReturn(flowOf(PlayServicesAvailabilityResult.InProgress))
        val viewModel = createViewModel()

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(UiState.PROGRESS)
    }

    @Test
    fun uiStateLiveDataEmitsContentWhenPlayServicesAvailabilityIsAvailable() = runTest {
        whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
                .thenReturn(flowOf(PlayServicesAvailabilityResult.Available))
        val viewModel = createViewModel()

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
                UiState.PROGRESS,
                UiState.CONTENT)
    }

    @Test
    fun uiStateLiveDataEmitsErrorWhenPlayServicesAvailabilityIsUnavailable() = runTest {
        whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
                .thenReturn(flowOf(PlayServicesAvailabilityResult.Unavailable.Resolvable(1)))
        val viewModel = createViewModel()

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
                UiState.PROGRESS,
                UiState.ERROR)
    }

    @Test
    fun playServicesErrorLiveDataEmitsNullWhenPlayServicesAvailabilityInProgress() = runTest {
        whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
                .thenReturn(flowOf(PlayServicesAvailabilityResult.InProgress))
        val viewModel = createViewModel()

        val observer = viewModel.playServicesErrorLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun playServicesErrorLiveDataEmitsNullWhenPlayServicesAvailabilityAvailable() = runTest {
        whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
                .thenReturn(flowOf(PlayServicesAvailabilityResult.Available))
        val viewModel = createViewModel()

        val observer = viewModel.playServicesErrorLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun playServicesErrorLiveDataEmitsErrorCodeWhenPlayServicesAvailabilityError() = runTest {
        whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
                .thenReturn(flowOf(PlayServicesAvailabilityResult.Unavailable.Resolvable(1)))
        val viewModel = createViewModel()

        val observer = viewModel.playServicesErrorLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null, 1)
    }

    @Test
    fun isErrorResolveButtonVisibleLiveDataEmitsFalseWhenPlayServicesAvailabilityInProgress() {
        return runTest {
            whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
                    .thenReturn(flowOf(PlayServicesAvailabilityResult.InProgress))
            val viewModel = createViewModel()

            val observer = viewModel.isErrorResolveButtonVisibleLiveData.test()
            advanceUntilIdle()

            observer.assertValues(false)
        }
    }

    @Test
    fun isErrorResolveButtonVisibleLiveDataEmitsFalseWhenPlayServicesAvailabilityAvailable() {
        return runTest {
            whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
                    .thenReturn(flowOf(PlayServicesAvailabilityResult.Available))
            val viewModel = createViewModel()

            val observer = viewModel.isErrorResolveButtonVisibleLiveData.test()
            advanceUntilIdle()

            observer.assertValues(false)
        }
    }

    @Test
    fun isErrorResolveButtonVisibleLiveDataEmitsFalseWhenPlayServicesAvailabilityUnresolvable() {
        return runTest {
            whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
                    .thenReturn(flowOf(PlayServicesAvailabilityResult.Unavailable.Unresolvable(1)))
            val viewModel = createViewModel()

            val observer = viewModel.isErrorResolveButtonVisibleLiveData.test()
            advanceUntilIdle()

            observer.assertValues(false)
        }
    }

    @Test
    fun isErrorResolveButtonVisibleLiveDataEmitsTrueWhenPlayServicesAvailabilityResolvable() {
        return runTest {
            whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
                    .thenReturn(flowOf(PlayServicesAvailabilityResult.Unavailable.Resolvable(1)))
            val viewModel = createViewModel()

            val observer = viewModel.isErrorResolveButtonVisibleLiveData.test()
            advanceUntilIdle()

            observer.assertValues(false, true)
        }
    }

    @Test
    fun stopMarkersLiveDataEmitsNullValuesFromStopMarkersRetriever() = runTest {
        whenever(stopMarkersRetriever.stopMarkersFlow)
                .thenReturn(flowOf(null))
        val viewModel = createViewModel()

        val observer = viewModel.stopMarkersLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun stopMarkersLiveDataEmitsValuesFromStopMarkersRetriever() = runTest {
        val expected = listOf<UiStopMarker>(
                mock(),
                mock(),
                mock())
        whenever(stopMarkersRetriever.stopMarkersFlow)
                .thenReturn(flowOf(expected))
        val viewModel = createViewModel()

        val observer = viewModel.stopMarkersLiveData.test()
        advanceUntilIdle()

        observer.assertValues(expected)
    }

    @Test
    fun routeLinesLiveDataEmitsNullValues() = runTest {
        whenever(routeLineRetriever.getRouteLinesFlow(null))
                .thenReturn(flowOf(null))
        val viewModel = createViewModel()

        val observer = viewModel.routeLinesLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun routeLinesLiveDataEmitsValuesFromRouteLineRetriever() = runTest {
        val expected = listOf<UiServiceRoute>(
                mock(),
                mock(),
                mock())
        whenever(routeLineRetriever.getRouteLinesFlow(null))
                .thenReturn(flowOf(expected))
        val viewModel = createViewModel()

        val observer = viewModel.routeLinesLiveData.test()
        advanceUntilIdle()

        observer.assertValues(expected)
    }

    @Test
    fun routeLinesLiveDataEmitsValuesFromRouteLineRetrieverWithServicesFromSelected() = runTest {
        val expected = listOf<UiServiceRoute>(
                mock(),
                mock(),
                mock())
        whenever(routeLineRetriever.getRouteLinesFlow(null))
                .thenReturn(flowOf(null))
        whenever(routeLineRetriever.getRouteLinesFlow(setOf("1", "2", "3")))
                .thenReturn(flowOf(expected))
        val viewModel = createViewModel()

        val observer = viewModel.routeLinesLiveData.test()
        advanceUntilIdle()
        viewModel.onServicesSelected(listOf("1", "2", "3"))
        advanceUntilIdle()

        observer.assertValues(null, expected)
    }

    @Test
    fun routeLinesLiveDataEmitsValuesFromRouteLineRetrieverWithServicesFromState() = runTest {
        val expected = listOf<UiServiceRoute>(
                mock(),
                mock(),
                mock())
        whenever(routeLineRetriever.getRouteLinesFlow(setOf("1", "2", "3")))
                .thenReturn(flowOf(expected))
        val viewModel = createViewModel(
                SavedStateHandle(
                        mapOf(STATE_SELECTED_SERVICES to arrayOf("1", "2", "3"))))

        val observer = viewModel.routeLinesLiveData.test()
        advanceUntilIdle()

        observer.assertValues(expected)
    }

    @Test
    fun isFilterMenuItemEnabledLiveDataEmitsFalseWhenHasServicesHasNotEmitted() = runTest {
        whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
                .thenReturn(flowOf(PlayServicesAvailabilityResult.Available))
        whenever(servicesRepository.hasServicesFlow)
                .thenReturn(emptyFlow())
        val viewModel = createViewModel()

        val observer = viewModel.isFilterMenuItemEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isFilterMenuItemEnabledLiveDataEmitsFalseHasServicesEmitsFalse() = runTest {
        whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
                .thenReturn(flowOf(PlayServicesAvailabilityResult.Available))
        whenever(servicesRepository.hasServicesFlow)
                .thenReturn(flowOf(false))
        val viewModel = createViewModel()

        val observer = viewModel.isFilterMenuItemEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isFilterMenuItemEnabledLiveDataEmitsFalseWhenPlayServicesInProgress() = runTest {
        whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
                .thenReturn(flowOf(PlayServicesAvailabilityResult.InProgress))
        whenever(servicesRepository.hasServicesFlow)
                .thenReturn(flowOf(true))
        val viewModel = createViewModel()

        val observer = viewModel.isFilterMenuItemEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isFilterMenuItemEnabledLiveDataEmitsFalseWhenPlayServicesUnavailable() = runTest {
        whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
                .thenReturn(flowOf(PlayServicesAvailabilityResult.Unavailable.Resolvable(1)))
        whenever(servicesRepository.hasServicesFlow)
                .thenReturn(flowOf(true))
        val viewModel = createViewModel()

        val observer = viewModel.isFilterMenuItemEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isFilterMenuItemEnabledLiveDataEmitsTrueWhenPlayServicesAvailableAndHasServices() {
            return runTest {
                whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
                        .thenReturn(flowOf(PlayServicesAvailabilityResult.Available))
                whenever(servicesRepository.hasServicesFlow)
                        .thenReturn(flowOf(true))
                val viewModel = createViewModel()

                val observer = viewModel.isFilterMenuItemEnabledLiveData.test()
                advanceUntilIdle()

                observer.assertValues(false, true)
            }
    }

    @Test
    fun isMapTypeMenuItemEnabledLiveDataEmitsFalseWhenPlayServicesInProgress() = runTest {
        whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
                .thenReturn(flowOf(PlayServicesAvailabilityResult.InProgress))
        val viewModel = createViewModel()

        val observer = viewModel.isMapTypeMenuItemEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isMapTypeMenuItemEnabledLiveDataEmitsTrueWhenPlayServicesAvailable() = runTest {
        whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
                .thenReturn(flowOf(PlayServicesAvailabilityResult.Available))
        val viewModel = createViewModel()

        val observer = viewModel.isMapTypeMenuItemEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false, true)
    }

    @Test
    fun isMapTypeMenuItemEnabledLiveDataEmitsFalseWhenPlayServicesUnavailable() = runTest {
        whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
                .thenReturn(flowOf(PlayServicesAvailabilityResult.Unavailable.Resolvable(1)))
        val viewModel = createViewModel()

        val observer = viewModel.isMapTypeMenuItemEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isTrafficViewMenuItemEnabledLiveDataEmitsFalseWhenPlayServicesInProgress() = runTest {
        whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
                .thenReturn(flowOf(PlayServicesAvailabilityResult.InProgress))
        val viewModel = createViewModel()

        val observer = viewModel.isTrafficViewMenuItemEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isTrafficViewMenuItemEnabledLiveDataEmitsTrueWhenPlayServicesAvailable() = runTest {
        whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
                .thenReturn(flowOf(PlayServicesAvailabilityResult.Available))
        val viewModel = createViewModel()

        val observer = viewModel.isTrafficViewMenuItemEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false, true)
    }

    @Test
    fun isTrafficViewMenuItemEnabledLiveDataEmitsFalseWhenPlayServicesUnavailable() = runTest {
        whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
                .thenReturn(flowOf(PlayServicesAvailabilityResult.Unavailable.Resolvable(1)))
        val viewModel = createViewModel()

        val observer = viewModel.isTrafficViewMenuItemEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isMyLocationFeatureEnabledLiveDataEmitsValues() = runTest {
        whenever(isMyLocationEnabledDetector.getIsMyLocationFeatureEnabledFlow(any()))
                .thenReturn(intervalFlowOf(0L, 0L, false, true, false))
        val viewModel = createViewModel()

        val observer = viewModel.isMyLocationFeatureEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false, true, false)
    }

    @Test
    fun isTrafficViewEnabledLiveDataEmitsFalseByDefault() = runTest {
        val viewModel = createViewModel()

        val observer = viewModel.isTrafficViewEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isTrafficViewEnabledLiveDataEmitsValues() = runTest {
        val viewModel = createViewModel()

        val observer = viewModel.isTrafficViewEnabledLiveData.test()
        advanceUntilIdle()
        viewModel.onTrafficViewMenuItemClicked()
        advanceUntilIdle()
        viewModel.onTrafficViewMenuItemClicked()
        advanceUntilIdle()

        observer.assertValues(false, true, false)
    }

    @Test
    fun isTrafficViewEnabledLiveDataEmitsStateValueIfAvailable() = runTest {
        val viewModel = createViewModel(
                SavedStateHandle(
                        mapOf(STATE_TRAFFIC_VIEW_ENABLED to true)))

        val observer = viewModel.isTrafficViewEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(true)
    }

    @Test
    fun isZoomControlsVisibleLiveDataEmitsItemsFromPreferenceRepository() = runTest {
        whenever(preferenceRepository.isMapZoomControlsVisibleFlow)
                .thenReturn(intervalFlowOf(0L, 10L, false, true, false))
        val viewModel = createViewModel()

        val observer = viewModel.isZoomControlsVisibleLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false, true, false)
    }

    @Test
    fun mapTypeLiveDataEmitsItemsFromPreferenceRepository() = runTest {
        whenever(preferenceRepository.mapTypeFlow)
                .thenReturn(intervalFlowOf(0L, 10L, 1, 2, 3, 4))
        val viewModel = createViewModel()

        val observer = viewModel.mapTypeLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
                MapType.NORMAL,
                MapType.SATELLITE,
                MapType.HYBRID,
                MapType.NORMAL)
    }

    @Test
    fun lastCameraLocationLiveDataEmitsItemsFromPreferenceRepository() = runTest {
        val lastMapCameraLocation = LastMapCameraLocation(1.1, 2.2, 3f)
        whenever(preferenceRepository.lastMapCameraLocationFlow)
            .thenReturn(flowOf(lastMapCameraLocation))
        val viewModel = createViewModel()
        val expected = UiCameraLocation(
            UiLatLon(
                1.1,
                2.2),
            3f)

        val observer = viewModel.lastCameraLocationLiveData.test()
        advanceUntilIdle()

        observer.assertValues(expected)
    }

    @Test
    fun showPlayServicesErrorResolutionLiveDataDoesNotEmitWhenPlayServicesAvailabilityInProgress() {
        return runTest {
            whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
                    .thenReturn(flowOf(PlayServicesAvailabilityResult.InProgress))
            val viewModel = createViewModel()

            viewModel.isErrorResolveButtonVisibleLiveData.test()
            val observer = viewModel.showPlayServicesErrorResolutionLiveData.test()
            advanceUntilIdle()
            viewModel.onErrorResolveButtonClicked()

            observer.assertEmpty()
        }
    }

    @Test
    fun showPlayServicesErrorResolutionLiveDataDoesNotEmitWhenPlayServicesAvailabilityAvailable() {
        return runTest {
            whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
                    .thenReturn(flowOf(PlayServicesAvailabilityResult.Available))
            val viewModel = createViewModel()

            viewModel.isErrorResolveButtonVisibleLiveData.test()
            val observer = viewModel.showPlayServicesErrorResolutionLiveData.test()
            advanceUntilIdle()
            viewModel.onErrorResolveButtonClicked()

            observer.assertEmpty()
        }
    }

    @Test
    fun showPlayServicesErrorResolutionLiveDataDoesNotEmitWhenAvailabilityUnresolvable() {
        return runTest {
            whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
                    .thenReturn(flowOf(PlayServicesAvailabilityResult.Unavailable.Unresolvable(1)))
            val viewModel = createViewModel()

            viewModel.isErrorResolveButtonVisibleLiveData.test()
            val observer = viewModel.showPlayServicesErrorResolutionLiveData.test()
            advanceUntilIdle()
            viewModel.onErrorResolveButtonClicked()

            observer.assertEmpty()
        }
    }

    @Test
    fun showPlayServicesErrorResolutionLiveDataEmitsWhenAvailabilityResolvable() {
        return runTest {
            whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
                    .thenReturn(flowOf(PlayServicesAvailabilityResult.Unavailable.Resolvable(1)))
            val viewModel = createViewModel()

            viewModel.isErrorResolveButtonVisibleLiveData.test()
            val observer = viewModel.showPlayServicesErrorResolutionLiveData.test()
            advanceUntilIdle()
            viewModel.onErrorResolveButtonClicked()

            observer.assertValues(1)
        }
    }

    @Test
    fun showMapTypeSelectionLiveDataEmitsWhenMapTypeMenuItemIsClicked() {
        val viewModel = createViewModel()

        val observer = viewModel.showMapTypeSelectionLiveData.test()
        viewModel.onMapTypeMenuItemClicked()

        observer.assertSize(1)
    }

    @Test
    fun showStopMarkerInfoWindowLiveDataDoesNotEmitByDefault() = runTest {
        val viewModel = createViewModel()

        val observer = viewModel.showStopMarkerInfoWindowLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun showStopMarkerInfoWindowLiveDataEmitsStopCodeWhenClicked() = runTest {
        val viewModel = createViewModel()
        val stopMarker = mock<UiStopMarker>()
        whenever(stopMarker.stopCode)
                .thenReturn("123456")

        val observer = viewModel.showStopMarkerInfoWindowLiveData.test()
        viewModel.onMapMarkerClicked(stopMarker)
        advanceUntilIdle()

        observer.assertValues(null, "123456")
    }

    @Test
    fun showStopMarkerInfoWindowLiveDataEmitsStopCodeWhenInSavedState() = runTest {
        val viewModel = createViewModel(
                SavedStateHandle(mapOf(STATE_SELECTED_STOP_CODE to "123456")))

        val observer = viewModel.showStopMarkerInfoWindowLiveData.test()
        advanceUntilIdle()

        observer.assertValues("123456")
    }

    @Test
    fun showStopMarkerInfoWindowLiveDataEmitsNullWhenInfoWindowClosed() = runTest {
        val viewModel = createViewModel()
        val stopMarker = mock<UiStopMarker>()
        whenever(stopMarker.stopCode)
                .thenReturn("123456")

        val observer = viewModel.showStopMarkerInfoWindowLiveData.test()
        viewModel.onMapMarkerClicked(stopMarker)
        advanceUntilIdle()
        viewModel.onInfoWindowClosed()
        advanceUntilIdle()

        observer.assertValues(null, "123456", null)
    }

    @Test
    fun showStopDetailsLiveDataEmitsWhenMarkerBubbleHasBeenClicked() {
        val stopMarker = mock<UiStopMarker>()
        whenever(stopMarker.stopCode)
                .thenReturn("123456")
        val viewModel = createViewModel()

        val observer = viewModel.showStopDetailsLiveData.test()
        viewModel.onMarkerBubbleClicked(stopMarker)

        observer.assertValues("123456")
    }

    @Test
    fun showServicesChooserLiveDataDoesNotEmitWhenHasServicesDoesNotEmit() = runTest {
        whenever(servicesRepository.hasServicesFlow)
                .thenReturn(emptyFlow())
        whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
            .thenReturn(flowOf(PlayServicesAvailabilityResult.Available))
        val viewModel = createViewModel()

        viewModel.isFilterMenuItemEnabledLiveData.test()
        val observer = viewModel.showServicesChooserLiveData.test()
        advanceUntilIdle()
        viewModel.onServicesMenuItemClicked()

        observer.assertEmpty()
    }

    @Test
    fun showServicesChooserLiveDataDoesNotEmitWhenHasServicesEmitsFalse() = runTest {
        whenever(servicesRepository.hasServicesFlow)
                .thenReturn(flowOf(false))
        whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
            .thenReturn(flowOf(PlayServicesAvailabilityResult.Available))
        val viewModel = createViewModel()

        viewModel.isFilterMenuItemEnabledLiveData.test()
        val observer = viewModel.showServicesChooserLiveData.test()
        advanceUntilIdle()
        viewModel.onServicesMenuItemClicked()

        observer.assertEmpty()
    }

    @Test
    fun showServicesChooserLiveDataEmitsWheHasServicesEmitsTrue() = runTest {
        whenever(servicesRepository.hasServicesFlow)
                .thenReturn(flowOf(true))
        whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
            .thenReturn(flowOf(PlayServicesAvailabilityResult.Available))
        val viewModel = createViewModel()

        viewModel.isFilterMenuItemEnabledLiveData.test()
        val observer = viewModel.showServicesChooserLiveData.test()
        advanceUntilIdle()
        viewModel.onServicesMenuItemClicked()

        observer.assertValues(null)
    }

    @Test
    fun showServicesChooserLiveDataEmitsParamsWithSelectedServices() = runTest {
        val selectedServices = listOf("1", "2")
        whenever(servicesRepository.hasServicesFlow)
                .thenReturn(flowOf(true))
        whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
            .thenReturn(flowOf(PlayServicesAvailabilityResult.Available))
        val viewModel = createViewModel()

        viewModel.onServicesSelected(selectedServices)
        viewModel.isFilterMenuItemEnabledLiveData.test()
        val observer = viewModel.showServicesChooserLiveData.test()
        advanceUntilIdle()
        viewModel.onServicesMenuItemClicked()

        observer.assertValues(selectedServices)
    }

    @Test
    fun showServicesChooserLiveDataEmitsParamsWithSelectedServicesFromState() = runTest {
        val selectedServices = listOf("1", "2")
        whenever(servicesRepository.hasServicesFlow)
                .thenReturn(flowOf(true))
        whenever(playServicesAvailabilityChecker.apiAvailabilityFlow)
            .thenReturn(flowOf(PlayServicesAvailabilityResult.Available))
        val viewModel = createViewModel(
                SavedStateHandle(
                        mapOf(STATE_SELECTED_SERVICES to arrayOf("1", "2"))))

        viewModel.isFilterMenuItemEnabledLiveData.test()
        val observer = viewModel.showServicesChooserLiveData.test()
        advanceUntilIdle()
        viewModel.onServicesMenuItemClicked()

        observer.assertValues(selectedServices)
    }

    @Test
    fun showStopShowsInfoWindowButDoesNotMoveCameraWhenNoStopLocation() = runTest {
        whenever(busStopsRepository.getStopLocation("123456"))
            .thenReturn(null)
        val viewModel = createViewModel()

        val showStopMarkerInfoWindowObserver = viewModel.showStopMarkerInfoWindowLiveData.test()
        val cameraLocationObserver = viewModel.cameraLocationLiveData.test()
        viewModel.showStop("123456")
        advanceUntilIdle()

        showStopMarkerInfoWindowObserver.assertValues(null, "123456")
        cameraLocationObserver.assertEmpty()
        verify(preferenceRepository, never())
            .setLastMapCameraLocation(any())
    }

    @Test
    fun showStopShowsInfoWindowAndMovesCameraToStopLocation() = runTest {
        whenever(busStopsRepository.getStopLocation("123456"))
            .thenReturn(MockStopLocation(1.1, 2.2))
        val viewModel = createViewModel()

        val showStopMarkerInfoWindowObserver = viewModel.showStopMarkerInfoWindowLiveData.test()
        val cameraLocationObserver = viewModel.cameraLocationLiveData.test()
        viewModel.showStop("123456")
        advanceUntilIdle()

        showStopMarkerInfoWindowObserver.assertValues(null, "123456")
        cameraLocationObserver.assertValues(
            UiCameraLocation(UiLatLon(1.1, 2.2), STOP_ZOOM))
        verify(preferenceRepository)
            .setLastMapCameraLocation(LastMapCameraLocation(1.1, 2.2, STOP_ZOOM))
    }

    @Test
    fun onStopSearchResultShowsInfoWindowButDoesNotMoveCameraWhenNoStopLocation() = runTest {
        whenever(busStopsRepository.getStopLocation("123456"))
            .thenReturn(null)
        val viewModel = createViewModel()

        val showStopMarkerInfoWindowObserver = viewModel.showStopMarkerInfoWindowLiveData.test()
        val cameraLocationObserver = viewModel.cameraLocationLiveData.test()
        viewModel.onStopSearchResult("123456")
        advanceUntilIdle()

        showStopMarkerInfoWindowObserver.assertValues(null, "123456")
        cameraLocationObserver.assertEmpty()
        verify(preferenceRepository, never())
            .setLastMapCameraLocation(any())
    }

    @Test
    fun onStopSearchResultShowsInfoWindowAndMovesCameraToStopLocation() = runTest {
        whenever(busStopsRepository.getStopLocation("123456"))
            .thenReturn(MockStopLocation(1.1, 2.2))
        val viewModel = createViewModel()

        val showStopMarkerInfoWindowObserver = viewModel.showStopMarkerInfoWindowLiveData.test()
        val cameraLocationObserver = viewModel.cameraLocationLiveData.test()
        viewModel.onStopSearchResult("123456")
        advanceUntilIdle()

        showStopMarkerInfoWindowObserver.assertValues(null, "123456")
        cameraLocationObserver.assertValues(
            UiCameraLocation(UiLatLon(1.1, 2.2), STOP_ZOOM))
        verify(preferenceRepository)
            .setLastMapCameraLocation(LastMapCameraLocation(1.1, 2.2, STOP_ZOOM))
    }

    @Test
    fun showLocationMovesCameraToLocation() = runTest {
        val viewModel = createViewModel()
        val latLon = UiLatLon(1.1, 2.2)
        val expectedLastCameraLocation = LastMapCameraLocation(1.1, 2.2, DEFAULT_ZOOM)
        val expectedCameraLocation = UiCameraLocation(latLon, DEFAULT_ZOOM)

        val observer = viewModel.cameraLocationLiveData.test()
        viewModel.showLocation(latLon)
        advanceUntilIdle()

        verify(preferenceRepository)
            .setLastMapCameraLocation(expectedLastCameraLocation)
        observer.assertValues(expectedCameraLocation)
    }

    @Test
    fun onMapTypeSelectedWithNormalMapSetsPreference() = runTest {
        val viewModel = createViewModel()

        viewModel.onMapTypeSelected(MapType.NORMAL)
        advanceUntilIdle()

        verify(preferenceRepository)
            .setMapType(MapType.NORMAL.value)
    }

    @Test
    fun onMapTypeSelectedWithSatelliteMapSetsPreference() = runTest {
        val viewModel = createViewModel()

        viewModel.onMapTypeSelected(MapType.SATELLITE)
        advanceUntilIdle()

        verify(preferenceRepository)
            .setMapType(MapType.SATELLITE.value)
    }

    @Test
    fun onMapTypeSelectedWithHybridMapSetsPreference() = runTest {
        val viewModel = createViewModel()

        viewModel.onMapTypeSelected(MapType.HYBRID)
        advanceUntilIdle()

        verify(preferenceRepository)
            .setMapType(MapType.HYBRID.value)
    }

    private fun createViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) =
            BusStopMapViewModel(
                    savedStateHandle,
                    permissionHandler,
                    playServicesAvailabilityChecker,
                    servicesRepository,
                    busStopsRepository,
                    stopMarkersRetriever,
                    routeLineRetriever,
                    isMyLocationEnabledDetector,
                    preferenceRepository,
                    coroutineRule.scope,
                    coroutineRule.testDispatcher)

    private data class MockStopLocation(
        override val latitude: Double,
        override val longitude: Double) : StopLocation
}