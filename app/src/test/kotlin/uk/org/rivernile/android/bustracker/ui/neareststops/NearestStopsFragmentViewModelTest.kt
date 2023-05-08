/*
 * Copyright (C) 2022 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.neareststops

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopName
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceRepository
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import uk.org.rivernile.android.bustracker.testutils.test

/**
 * Tests for [NearestStopsFragmentViewModel].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class NearestStopsFragmentViewModelTest {

    companion object {

        private const val STATE_ASKED_TURN_ON_GPS = "askedTurnOnGps"
        private const val STATE_ASKED_FOR_PERMISSIONS = "askedForPermissions"
        private const val STATE_SELECTED_STOP_CODE = "selectedStopCode"
        private const val STATE_SELECTED_SERVICES = "selectedServices"
    }

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var servicesRepository: ServicesRepository
    @Mock
    private lateinit var busStopsRepository: BusStopsRepository
    @Mock
    private lateinit var favouritesStateRetriever: FavouritesStateRetriever
    @Mock
    private lateinit var alertsStateRetriever: AlertsStateRetriever
    @Mock
    private lateinit var featureRepository: FeatureRepository
    @Mock
    private lateinit var locationRepository: LocationRepository
    @Mock
    private lateinit var preferenceRepository: PreferenceRepository
    @Mock
    private lateinit var uiStateRetriever: UiStateRetriever

    @Test
    fun itemsLiveDataEmitsNullWhenUiStateIsProgress() = runTest {
        whenever(uiStateRetriever.getUiStateFlow(any(), any()))
                .thenReturn(emptyFlow())
        val viewModel = createViewModel()

        val observer = viewModel.itemsLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun itemsLiveDataEmitsNullWhenUiStateIsErrorType() = runTest {
        whenever(uiStateRetriever.getUiStateFlow(any(), any()))
                .thenReturn(flowOf(UiState.Error.NoLocationFeature))
        val viewModel = createViewModel()

        val observer = viewModel.itemsLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun itemsLiveDataEmitsItemsWhenUiStateIsSuccess() = runTest {
        val items = listOf(
                UiNearestStop(
                        "111111",
                        StopName(
                                "Stop name 1",
                                "Locality 1"),
                        "1, 2, 3",
                        1,
                        1,
                        false),
                UiNearestStop(
                        "222222",
                        StopName(
                                "Stop name 2",
                                "Locality 3"),
                        "1, 2, 3",
                        2,
                        2,
                        false),
                UiNearestStop(
                        "333333",
                        StopName(
                                "Stop name 3",
                                "Locality 3"),
                        "1, 2, 3",
                        3,
                        3,
                        false))
        whenever(uiStateRetriever.getUiStateFlow(any(), any()))
                .thenReturn(flowOf(UiState.Success(items)))
        val viewModel = createViewModel()

        val observer = viewModel.itemsLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
                null,
                items)
    }

    @Test
    fun itemsLiveDataEmitsItemsWhenUiStateIsSuccessAndStopIsSelected() = runTest {
        val items1 = listOf(
                UiNearestStop(
                        "111111",
                        StopName(
                                "Stop name 1",
                                "Locality 1"),
                        "1, 2, 3",
                        1,
                        1,
                        false),
                UiNearestStop(
                        "222222",
                        StopName(
                                "Stop name 2",
                                "Locality 3"),
                        "1, 2, 3",
                        2,
                        2,
                        false),
                UiNearestStop(
                        "333333",
                        StopName(
                                "Stop name 3",
                                "Locality 3"),
                        "1, 2, 3",
                        3,
                        3,
                        false))
        val items2 = listOf(
                UiNearestStop(
                        "111111",
                        StopName(
                                "Stop name 1",
                                "Locality 1"),
                        "1, 2, 3",
                        1,
                        1,
                        false),
                UiNearestStop(
                        "222222",
                        StopName(
                                "Stop name 2",
                                "Locality 3"),
                        "1, 2, 3",
                        2,
                        2,
                        true),
                UiNearestStop(
                        "333333",
                        StopName(
                                "Stop name 3",
                                "Locality 3"),
                        "1, 2, 3",
                        3,
                        3,
                        false))
        whenever(uiStateRetriever.getUiStateFlow(any(), any()))
                .thenReturn(flowOf(UiState.Success(items1)))
        val viewModel = createViewModel()

        val observer = viewModel.itemsLiveData.test()
        advanceUntilIdle()
        viewModel.onNearestStopLongClicked("222222")
        advanceUntilIdle()
        viewModel.onNearestStopUnselected()
        advanceUntilIdle()

        observer.assertValues(
                null,
                items1,
                items2,
                items1)
    }

    @Test
    fun uiStateLiveDataEmitsErrorStateFromUiStateRetriever() = runTest {
        whenever(uiStateRetriever.getUiStateFlow(any(), any()))
                .thenReturn(flowOf(UiState.Error.NoLocationFeature))
        val viewModel = createViewModel()

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
                UiState.InProgress,
                UiState.Error.NoLocationFeature)
    }

    @Test
    fun uiStateLiveDataEmitsSuccessStateFromUiStateRetriever() = runTest {
        val items = listOf<UiNearestStop>(mock(), mock(), mock())
        whenever(uiStateRetriever.getUiStateFlow(any(), any()))
                .thenReturn(flowOf(UiState.Success(items)))
        val viewModel = createViewModel()

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
                UiState.InProgress,
                UiState.Success(items))
    }

    @Test
    fun isFilterEnabledLiveDataEmitsFalseWhenNoLocationFeatureAndNullServices() = runTest {
        whenever(servicesRepository.allServiceNamesFlow)
                .thenReturn(flowOf(null))
        whenever(locationRepository.hasLocationFeature)
                .thenReturn(false)
        val viewModel = createViewModel()

        val observer = viewModel.isFilterEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isFilterEnabledLiveDataEmitsFalseWhenHasLocationFeatureAndNullServices() = runTest {
        whenever(servicesRepository.allServiceNamesFlow)
                .thenReturn(flowOf(null))
        whenever(locationRepository.hasLocationFeature)
                .thenReturn(true)
        val viewModel = createViewModel()

        val observer = viewModel.isFilterEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isFilterEnabledLiveDataEmitsFalseWhenHasLocationFeatureAndEmptyServices() = runTest {
        whenever(servicesRepository.allServiceNamesFlow)
                .thenReturn(flowOf(emptyList()))
        whenever(locationRepository.hasLocationFeature)
                .thenReturn(true)
        val viewModel = createViewModel()

        val observer = viewModel.isFilterEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isFilterEnabledLiveDataEmitsTrueWhenHasLocationFeatureAndHasServices() = runTest {
        whenever(servicesRepository.allServiceNamesFlow)
                .thenReturn(flowOf(listOf("1")))
        whenever(locationRepository.hasLocationFeature)
                .thenReturn(true)
        val viewModel = createViewModel()

        val observer = viewModel.isFilterEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
                false,
                true)
    }

    @Test
    fun askForLocationPermissionsLiveDataDoesNotEmitWhenResolveClickedAndNotPermissionError() =
            runTest {
        whenever(uiStateRetriever.getUiStateFlow(any(), any()))
                .thenReturn(flowOf(UiState.Error.NoLocationFeature))
        val viewModel = createViewModel()

        viewModel.uiStateLiveData.test() // Kick-start collection
        val observer = viewModel.askForLocationPermissionsLiveData.test()
        advanceUntilIdle()
        viewModel.onResolveErrorButtonClicked()
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun askForLocationPermissionsLiveDataEmitsWhenResolveClickedAndHasPermissionError() = runTest {
        whenever(uiStateRetriever.getUiStateFlow(any(), any()))
                .thenReturn(flowOf(UiState.Error.InsufficientLocationPermissions))
        val viewModel = createViewModel()

        viewModel.uiStateLiveData.test() // Kick-start collection
        val observer = viewModel.askForLocationPermissionsLiveData.test()
        advanceUntilIdle()
        viewModel.onResolveErrorButtonClicked()
        advanceUntilIdle()

        observer.assertSize(1)
    }

    @Test
    fun askForLocationPermissionsLiveDataEmitsOnFirstRunWhenBothUngranted() = runTest {
        val viewModel = createViewModel()
        viewModel.permissionsState = PermissionsState(
                PermissionState.UNGRANTED,
                PermissionState.UNGRANTED)

        val observer = viewModel.askForLocationPermissionsLiveData.test()
        advanceUntilIdle()

        observer.assertSize(1)
    }

    @Test
    fun askForLocationPermissionsLiveDataDoesNotEmitOnFirstRunWhenFineUngranted() = runTest {
        val viewModel = createViewModel()
        viewModel.permissionsState = PermissionsState(
                PermissionState.UNGRANTED,
                PermissionState.GRANTED)

        val observer = viewModel.askForLocationPermissionsLiveData.test()
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun askForLocationPermissionsLiveDataDoesNotEmitOnFirstRunWhenCoarseUngranted() = runTest {
        val viewModel = createViewModel()
        viewModel.permissionsState = PermissionsState(
                PermissionState.GRANTED,
                PermissionState.UNGRANTED)

        val observer = viewModel.askForLocationPermissionsLiveData.test()
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun askForLocationPermissionsLiveDataDoesNotEmitOnFirstRunWhenBothGranted() = runTest {
        val viewModel = createViewModel()
        viewModel.permissionsState = PermissionsState(
                PermissionState.GRANTED,
                PermissionState.GRANTED)

        val observer = viewModel.askForLocationPermissionsLiveData.test()
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun askForLocationPermissionsLiveDataEmitsOnSubsequentRunWhenBothUngranted() = runTest {
        val viewModel = createViewModel(
                SavedStateHandle(
                        mapOf(STATE_ASKED_FOR_PERMISSIONS to true)))
        viewModel.permissionsState = PermissionsState(
                PermissionState.UNGRANTED,
                PermissionState.UNGRANTED)

        val observer = viewModel.askForLocationPermissionsLiveData.test()
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun askForLocationPermissionsLiveDataDoesNotEmitOnSubsequentRunWhenFineUngranted() = runTest {
        val viewModel = createViewModel(
                SavedStateHandle(
                        mapOf(STATE_ASKED_FOR_PERMISSIONS to true)))
        viewModel.permissionsState = PermissionsState(
                PermissionState.UNGRANTED,
                PermissionState.GRANTED)

        val observer = viewModel.askForLocationPermissionsLiveData.test()
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun askForLocationPermissionsLiveDataDoesNotEmitOnSubsequentRunWhenCoarseUngranted() = runTest {
        val viewModel = createViewModel(
                SavedStateHandle(
                        mapOf(STATE_ASKED_FOR_PERMISSIONS to true)))
        viewModel.permissionsState = PermissionsState(
                PermissionState.GRANTED,
                PermissionState.UNGRANTED)

        val observer = viewModel.askForLocationPermissionsLiveData.test()
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun askForLocationPermissionsLiveDataDoesNotEmitOnSubsequentRunWhenBothGranted() = runTest {
        val viewModel = createViewModel(
                SavedStateHandle(
                        mapOf(STATE_ASKED_FOR_PERMISSIONS to true)))
        viewModel.permissionsState = PermissionsState(
                PermissionState.GRANTED,
                PermissionState.GRANTED)

        val observer = viewModel.askForLocationPermissionsLiveData.test()
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun showContextMenuLiveDataEmitsFalseByDefault() = runTest {
        val viewModel = createViewModel()

        val observer = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun showContextMenuLiveDataEmitsFalseWhenHasNullSelectedStop() = runTest {
        val viewModel = createViewModel(
                SavedStateHandle(
                        mapOf(STATE_SELECTED_STOP_CODE to null)))

        val observer = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun showContextMenuLiveDataEmitsFalseWhenHasEmptySelectedStop() = runTest {
        val viewModel = createViewModel(
                SavedStateHandle(
                        mapOf(STATE_SELECTED_STOP_CODE to "")))

        val observer = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun showContextMenuLiveDataEmitsTrueWhenHasNonEmptySelectedStop() = runTest {
        val viewModel = createViewModel(
                SavedStateHandle(
                        mapOf(STATE_SELECTED_STOP_CODE to "123456")))

        val observer = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()

        observer.assertValues(true)
    }

    @Test
    fun showContextMenuLiveDataEmitsFalseWhenHasEmptyLongClick() = runTest {
        val viewModel = createViewModel()

        val observer = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()
        val result = viewModel.onNearestStopLongClicked("")
        advanceUntilIdle()

        assertFalse(result)
        observer.assertValues(false)
    }

    @Test
    fun showContextMenuLiveDataEmitsTrueWhenHasStopCode() = runTest {
        val viewModel = createViewModel()

        val observer = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()
        val result = viewModel.onNearestStopLongClicked("123456")
        advanceUntilIdle()

        assertTrue(result)
        observer.assertValues(false, true)
    }

    @Test
    fun showContextMenuLiveDataReturnsTrueWhenAlreadyHasSelectedStop() = runTest {
        val viewModel = createViewModel()

        val observer = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()
        viewModel.onNearestStopLongClicked("123456")
        advanceUntilIdle()
        val result = viewModel.onNearestStopLongClicked("987654")
        advanceUntilIdle()

        assertTrue(result)
        observer.assertValues(false, true)
    }

    @Test
    fun onNearestStopsUnselectedDoesNotEmitFromShowContentMenuWhenMenuNotShowing() = runTest {
        val viewModel = createViewModel()

        val observer = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()
        viewModel.onNearestStopUnselected()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun onNearestStopsUnselectedEmitsFromShowContentMenuWhenMenuShowing() = runTest {
        val viewModel = createViewModel()

        val observer = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()
        viewModel.onNearestStopLongClicked("123456")
        advanceUntilIdle()
        viewModel.onNearestStopUnselected()
        advanceUntilIdle()

        observer.assertValues(false, true, false)
    }

    @Test
    fun selectedStopNameLiveDataEmitsNullByDefault() = runTest {
        val viewModel = createViewModel()

        val observer = viewModel.selectedStopNameLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun selectedStopNameLiveDataEmitsNullWhenSelectedStopCodeStateIsNull() = runTest {
        val viewModel = createViewModel(
                SavedStateHandle(
                        mapOf(STATE_SELECTED_STOP_CODE to null)))

        val observer = viewModel.selectedStopNameLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun selectedStopNameLiveDataEmitsNullWhenSelectedStopCodeStateIsEmptyString() = runTest {
        val viewModel = createViewModel(
                SavedStateHandle(
                        mapOf(STATE_SELECTED_STOP_CODE to "")))

        val observer = viewModel.selectedStopNameLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun selectedStopNameLiveDataEmitsStopNameWhenHasSelectedStopCodeState() = runTest {
        val stopName = StopName("Name", "Locality")
        whenever(busStopsRepository.getNameForStopFlow("123456"))
                .thenReturn(flowOf(stopName))
        val viewModel = createViewModel(
                SavedStateHandle(
                        mapOf(STATE_SELECTED_STOP_CODE to "123456")))

        val observer = viewModel.selectedStopNameLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
                UiNearestStopName(
                        "123456",
                        null),
                UiNearestStopName(
                        "123456",
                        StopName(
                                "Name",
                                "Locality")))
    }

    @Test
    fun selectedStopNameLiveDataEmitsStopNameWhenStopIsSelected() = runTest {
        val stopName = StopName("Name", "Locality")
        whenever(busStopsRepository.getNameForStopFlow("123456"))
                .thenReturn(flowOf(stopName))
        val viewModel = createViewModel()

        val observer = viewModel.selectedStopNameLiveData.test()
        advanceUntilIdle()
        viewModel.onNearestStopLongClicked("123456")
        advanceUntilIdle()

        observer.assertValues(
                null,
                UiNearestStopName(
                        "123456",
                        null),
                UiNearestStopName(
                        "123456",
                        StopName(
                                "Name",
                                "Locality")))
    }

    @Test
    fun selectedStopNameLiveDataEmitsNullWhenStopIsUnselected() = runTest {
        val stopName = StopName("Name", "Locality")
        whenever(busStopsRepository.getNameForStopFlow("123456"))
                .thenReturn(flowOf(stopName))
        val viewModel = createViewModel()

        val observer = viewModel.selectedStopNameLiveData.test()
        advanceUntilIdle()
        viewModel.onNearestStopLongClicked("123456")
        advanceUntilIdle()
        viewModel.onNearestStopUnselected()
        advanceUntilIdle()

        observer.assertValues(
                null,
                UiNearestStopName(
                        "123456",
                        null),
                UiNearestStopName(
                        "123456",
                        StopName(
                                "Name",
                                "Locality")),
                null)
    }

    @Test
    fun isStopMapVisibleLiveDataEmitsFalseWhenDoesNotHaveStopMapUiFeature() = runTest {
        whenever(featureRepository.hasStopMapUiFeature)
                .thenReturn(false)
        val viewModel = createViewModel()

        val observer = viewModel.isStopMapVisibleLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isStopMapVisibleLiveDataEmitsTrueWhenHasStopMapUiFeature() = runTest {
        whenever(featureRepository.hasStopMapUiFeature)
                .thenReturn(true)
        val viewModel = createViewModel()

        val observer = viewModel.isStopMapVisibleLiveData.test()
        advanceUntilIdle()

        observer.assertValues(true)
    }

    @Test
    fun isArrivalAlertVisibleLiveDataEmitsValuesFromAlertsStateRetriever() = runTest {
        whenever(alertsStateRetriever.isArrivalAlertVisibleFlow)
                .thenReturn(flowOf(false, true, false))
        val viewModel = createViewModel()

        val observer = viewModel.isArrivalAlertVisibleLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false, true, false)
    }

    @Test
    fun isProximityAlertVisibleLiveDataEmitsValuesFromAlertsStateRetriever() = runTest {
        whenever(alertsStateRetriever.isProximityAlertVisibleFlow)
                .thenReturn(flowOf(false, true, false))
        val viewModel = createViewModel()

        val observer = viewModel.isProximityAlertVisibleLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false, true, false)
    }

    @Test
    fun isFavouriteEnabledLiveDataEmitsValuesFromFavouritesStateRetriever() = runTest {
        whenever(favouritesStateRetriever.getIsAddedAsFavouriteStopFlow(any()))
                .thenReturn(intervalFlowOf(0L, 10L, false, true, false))
        val viewModel = createViewModel()

        val observer = viewModel.isFavouriteEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false, true)
    }

    @Test
    fun isArrivalAlertEnabledLiveDataEmitsValuesFromAlertsStateRetriever() = runTest {
        whenever(alertsStateRetriever.getHasArrivalAlertFlow(any()))
                .thenReturn(intervalFlowOf(0L, 10L, false, true, false))
        val viewModel = createViewModel()

        val observer = viewModel.isArrivalAlertEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false, true)
    }

    @Test
    fun isProximityAlertEnabledLiveDataEmitsValuesFromAlertsStateRetriever() = runTest {
        whenever(alertsStateRetriever.getHasProximityAlertFlow(any()))
                .thenReturn(flowOf(null, false, true, false))
        val viewModel = createViewModel()

        val observer = viewModel.isProximityAlertEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false, true)
    }

    @Test
    fun isAddedAsFavouriteStopLiveDataEmitsValuesFromFavouritesStateRetriever() = runTest {
        whenever(favouritesStateRetriever.getIsAddedAsFavouriteStopFlow(any()))
                .thenReturn(intervalFlowOf(0L, 10L, null, false, true, false))
        val viewModel = createViewModel()

        val observer = viewModel.isAddedAsFavouriteStopLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false, true, false)
    }

    @Test
    fun hasArrivalAlertLiveDataEmitsValuesFromAlertsStateRetriever() = runTest {
        whenever(alertsStateRetriever.getHasArrivalAlertFlow(any()))
                .thenReturn(intervalFlowOf(0L, 10L, null, false, true, false))
        val viewModel = createViewModel()

        val observer = viewModel.hasArrivalAlertLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false, true, false)
    }

    @Test
    fun hasProximityAlertLiveDataEmitsValuesFromAlertsStateRetriever() = runTest {
        whenever(alertsStateRetriever.getHasProximityAlertFlow(any()))
                .thenReturn(intervalFlowOf(0L, 10L, null, false, true, false))
        val viewModel = createViewModel()

        val observer = viewModel.hasProximityAlertLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false, true, false)
    }

    @Test
    fun onNearestStopClickedDoesNotShowStopDataWhenSelectedStopExists() {
        val viewModel = createViewModel()
        val nearestStop = UiNearestStop(
                "123456",
                null,
                null,
                0,
                0,
                false)

        viewModel.onNearestStopLongClicked("123456")
        val observer = viewModel.showStopDataLiveData.test()
        viewModel.onNearestStopClicked(nearestStop)

        observer.assertEmpty()
    }

    @Test
    fun onNearestStopClickedShowsStopData() {
        val viewModel = createViewModel()
        val nearestStop = UiNearestStop(
                "123456",
                null,
                null,
                0,
                0,
                false)

        val observer = viewModel.showStopDataLiveData.test()
        viewModel.onNearestStopClicked(nearestStop)

        observer.assertSize(1)
    }

    @Test
    fun onNearestStopLongClickedReturnsFalseWhenStopCodeIsEmpty() = runTest {
        val viewModel = createViewModel()

        val observer = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()
        val result = viewModel.onNearestStopLongClicked("")
        advanceUntilIdle()

        assertFalse(result)
        observer.assertValues(false)
    }

    @Test
    fun onNearestStopLongClickedReturnsTrueWhenStopCodeIsValidAndNoCurrentSelectedStop() = runTest {
        val viewModel = createViewModel()

        val observer = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()
        val result = viewModel.onNearestStopLongClicked("123456")
        advanceUntilIdle()

        assertTrue(result)
        observer.assertValues(false, true)
    }

    @Test
    fun onNearestStopLongClickedReturnsTrueWhenNewStopIsSelected() = runTest {
        val viewModel = createViewModel()

        val observer = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()
        viewModel.onNearestStopLongClicked("123456")
        advanceUntilIdle()
        val result = viewModel.onNearestStopLongClicked("987654")
        advanceUntilIdle()

        assertTrue(result)
        observer.assertValues(false, true)
    }

    @Test
    fun onNearestStopLongClickedReturnsTrueWhenStopSelectedAfterUnselection() = runTest {
        val viewModel = createViewModel()

        val observer = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()
        viewModel.onNearestStopLongClicked("123456")
        advanceUntilIdle()
        viewModel.onNearestStopUnselected()
        advanceUntilIdle()
        val result = viewModel.onNearestStopLongClicked("987654")
        advanceUntilIdle()

        assertTrue(result)
        observer.assertValues(false, true, false, true)
    }

    @Test
    fun onFilterMenuItemClickedDoesNotShowServicesChooserWhenServicesNotLoaded() = runTest {
        val viewModel = createViewModel()

        val observer = viewModel.showServicesChooserLiveData.test()
        advanceUntilIdle()
        viewModel.onFilterMenuItemClicked()
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun onFilterMenuItemClickedDoesNotShowServiceChooserWhenServicesAreNull() = runTest {
        whenever(servicesRepository.allServiceNamesFlow)
                .thenReturn(flowOf(null))
        val viewModel = createViewModel()

        val observer = viewModel.showServicesChooserLiveData.test()
        advanceUntilIdle()
        viewModel.onFilterMenuItemClicked()
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun onFilterMenuItemClickedDoesNotShowServiceChooserWhenServicesAreEmpty() = runTest {
        whenever(servicesRepository.allServiceNamesFlow)
                .thenReturn(flowOf(emptyList()))
        val viewModel = createViewModel()

        val observer = viewModel.showServicesChooserLiveData.test()
        advanceUntilIdle()
        viewModel.onFilterMenuItemClicked()
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun onFilterMenuItemClickedShowsServicesChooserWithNullChosenWhenServicesExist() = runTest {
        val services = listOf("1", "2", "3")
        whenever(servicesRepository.allServiceNamesFlow)
                .thenReturn(flowOf(services))
        val viewModel = createViewModel()

        viewModel.isFilterEnabledLiveData.test()
        val observer = viewModel.showServicesChooserLiveData.test()
        advanceUntilIdle()
        viewModel.onFilterMenuItemClicked()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun onFilterMenuItemClickedShowsServicesChooserWithChosenWhenHasNullState() = runTest {
        val services = listOf("1", "2", "3")
        whenever(servicesRepository.allServiceNamesFlow)
                .thenReturn(flowOf(services))
        val viewModel = createViewModel(
                SavedStateHandle(
                        mapOf(STATE_SELECTED_SERVICES to null)))

        viewModel.isFilterEnabledLiveData.test()
        val observer = viewModel.showServicesChooserLiveData.test()
        advanceUntilIdle()
        viewModel.onFilterMenuItemClicked()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun onFilterMenuItemClickedShowsServicesChooserWithChosenWhenHasEmptyState() = runTest {
        val services = listOf("1", "2", "3")
        whenever(servicesRepository.allServiceNamesFlow)
                .thenReturn(flowOf(services))
        val viewModel = createViewModel(
                SavedStateHandle(
                        mapOf(STATE_SELECTED_SERVICES to emptyArray<String>())))

        viewModel.isFilterEnabledLiveData.test()
        val observer = viewModel.showServicesChooserLiveData.test()
        advanceUntilIdle()
        viewModel.onFilterMenuItemClicked()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun onFilterMenuItemClickedShowsServicesChooserWithChosenWhenHasState() = runTest {
        val services = listOf("1", "2", "3")
        whenever(servicesRepository.allServiceNamesFlow)
                .thenReturn(flowOf(services))
        val viewModel = createViewModel(
                SavedStateHandle(
                        mapOf(STATE_SELECTED_SERVICES to arrayOf("1", "2"))))

        viewModel.isFilterEnabledLiveData.test()
        val observer = viewModel.showServicesChooserLiveData.test()
        advanceUntilIdle()
        viewModel.onFilterMenuItemClicked()
        advanceUntilIdle()

        observer.assertValues(listOf("1", "2"))
    }

    @Test
    fun onFavouriteMenuItemClickedReturnsFalseWhenItemNotSelected() = runTest {
        val viewModel = createViewModel()

        val showConfirmDeleteFavouriteObserver = viewModel.showConfirmDeleteFavouriteLiveData.test()
        val showAddFavouriteStopObserver = viewModel.showAddFavouriteStopLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()
        val result = viewModel.onFavouriteMenuItemClicked()
        advanceUntilIdle()

        assertFalse(result)
        showConfirmDeleteFavouriteObserver.assertEmpty()
        showAddFavouriteStopObserver.assertEmpty()
        showContextMenuObserver.assertValues(false)
    }

    @Test
    fun onFavouriteMenuItemClickedShowConfirmDeleteFavouriteWhenAddedAsFavourite() = runTest {
        whenever(favouritesStateRetriever.getIsAddedAsFavouriteStopFlow(any()))
                .thenReturn(flowOf(true))
        val viewModel = createViewModel()

        val showConfirmDeleteFavouriteObserver = viewModel.showConfirmDeleteFavouriteLiveData.test()
        val showAddFavouriteStopObserver = viewModel.showAddFavouriteStopLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        viewModel.isAddedAsFavouriteStopLiveData.test()
        viewModel.onNearestStopLongClicked("123456")
        advanceUntilIdle()
        val result = viewModel.onFavouriteMenuItemClicked()
        advanceUntilIdle()

        assertTrue(result)
        showConfirmDeleteFavouriteObserver.assertValues("123456")
        showAddFavouriteStopObserver.assertEmpty()
        showContextMenuObserver.assertValues(true, false)
    }

    @Test
    fun onFavouriteMenuItemClickedShowAddFavouriteStopWhenNotAddedAsFavourite() = runTest {
        whenever(favouritesStateRetriever.getIsAddedAsFavouriteStopFlow(any()))
                .thenReturn(flowOf(false))
        val viewModel = createViewModel()

        val showConfirmDeleteFavouriteObserver = viewModel.showConfirmDeleteFavouriteLiveData.test()
        val showAddFavouriteStopObserver = viewModel.showAddFavouriteStopLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        viewModel.isAddedAsFavouriteStopLiveData.test()
        viewModel.onNearestStopLongClicked("123456")
        advanceUntilIdle()
        val result = viewModel.onFavouriteMenuItemClicked()
        advanceUntilIdle()

        assertTrue(result)
        showConfirmDeleteFavouriteObserver.assertEmpty()
        showAddFavouriteStopObserver.assertValues("123456")
        showContextMenuObserver.assertValues(true, false)
    }

    @Test
    fun onFavouriteMenuItemClickedDoesNotShowAnythingWhenFavouriteStatusIsUnknown() = runTest {
        whenever(favouritesStateRetriever.getIsAddedAsFavouriteStopFlow(any()))
                .thenReturn(flowOf(null))
        val viewModel = createViewModel()

        val showConfirmDeleteFavouriteObserver = viewModel.showConfirmDeleteFavouriteLiveData.test()
        val showAddFavouriteStopObserver = viewModel.showAddFavouriteStopLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        viewModel.isAddedAsFavouriteStopLiveData.test()
        viewModel.onNearestStopLongClicked("123456")
        advanceUntilIdle()
        val result = viewModel.onFavouriteMenuItemClicked()
        advanceUntilIdle()

        assertTrue(result)
        showConfirmDeleteFavouriteObserver.assertEmpty()
        showAddFavouriteStopObserver.assertEmpty()
        showContextMenuObserver.assertValues(true, false)
    }

    @Test
    fun onProxAlertMenuItemClickedReturnsFalseWhenItemNotSelected() = runTest {
        val viewModel = createViewModel()

        val showConfirmDeleteProxAlertObserver = viewModel.showConfirmDeleteProximityAlertLiveData
                .test()
        val showAddProxAlertObserver = viewModel.showAddProximityAlertLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()
        val result = viewModel.onProximityAlertMenuItemClicked()
        advanceUntilIdle()

        assertFalse(result)
        showConfirmDeleteProxAlertObserver.assertEmpty()
        showAddProxAlertObserver.assertEmpty()
        showContextMenuObserver.assertValues(false)
    }

    @Test
    fun onProxAlertMenuItemClickedShowConfirmDeleteProxAlertWhenAddedAsProxAlert() = runTest {
        whenever(alertsStateRetriever.getHasProximityAlertFlow(any()))
                .thenReturn(flowOf(true))
        val viewModel = createViewModel()

        val showConfirmDeleteProxAlertObserver = viewModel.showConfirmDeleteProximityAlertLiveData
                .test()
        val showAddProxAlertObserver = viewModel.showAddProximityAlertLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        viewModel.isProximityAlertEnabledLiveData.test()
        viewModel.onNearestStopLongClicked("123456")
        advanceUntilIdle()
        val result = viewModel.onProximityAlertMenuItemClicked()
        advanceUntilIdle()

        assertTrue(result)
        showConfirmDeleteProxAlertObserver.assertValues("123456")
        showAddProxAlertObserver.assertEmpty()
        showContextMenuObserver.assertValues(true, false)
    }

    @Test
    fun onProxAlertMenuItemClickedShowAddProxAlertWhenNotAddedAsProxAlert() = runTest {
        whenever(alertsStateRetriever.getHasProximityAlertFlow(any()))
                .thenReturn(flowOf(false))
        val viewModel = createViewModel()

        val showConfirmDeleteProxAlertObserver = viewModel.showConfirmDeleteProximityAlertLiveData
                .test()
        val showAddProxAlertObserver = viewModel.showAddProximityAlertLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        viewModel.isProximityAlertEnabledLiveData.test()
        viewModel.onNearestStopLongClicked("123456")
        advanceUntilIdle()
        val result = viewModel.onProximityAlertMenuItemClicked()
        advanceUntilIdle()

        assertTrue(result)
        showConfirmDeleteProxAlertObserver.assertEmpty()
        showAddProxAlertObserver.assertValues("123456")
        showContextMenuObserver.assertValues(true, false)
    }

    @Test
    fun onProxAlertMenuItemClickedShowAddProxAlertWhenProxAlertStatusIsUnknown() = runTest {
        whenever(alertsStateRetriever.getHasProximityAlertFlow(any()))
                .thenReturn(flowOf(null))
        val viewModel = createViewModel()

        val showConfirmDeleteProxAlertObserver = viewModel.showConfirmDeleteProximityAlertLiveData
                .test()
        val showAddProxAlertObserver = viewModel.showAddProximityAlertLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        viewModel.isProximityAlertEnabledLiveData.test()
        viewModel.onNearestStopLongClicked("123456")
        advanceUntilIdle()
        val result = viewModel.onProximityAlertMenuItemClicked()
        advanceUntilIdle()

        assertTrue(result)
        showConfirmDeleteProxAlertObserver.assertEmpty()
        showAddProxAlertObserver.assertEmpty()
        showContextMenuObserver.assertValues(true, false)
    }

    @Test
    fun onTimeAlertMenuItemClickedReturnsFalseWhenItemNotSelected() = runTest {
        val viewModel = createViewModel()

        val showConfirmDeleteTimeAlertObserver = viewModel.showConfirmDeleteArrivalAlertLiveData
                .test()
        val showAddTimeAlertObserver = viewModel.showAddArrivalAlertLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()
        val result = viewModel.onTimeAlertMenuItemClicked()
        advanceUntilIdle()

        assertFalse(result)
        showConfirmDeleteTimeAlertObserver.assertEmpty()
        showAddTimeAlertObserver.assertEmpty()
        showContextMenuObserver.assertValues(false)
    }

    @Test
    fun onTimeAlertMenuItemClickedShowConfirmDeleteTimeAlertWhenAddedAsTimeAlert() = runTest {
        whenever(alertsStateRetriever.getHasArrivalAlertFlow(any()))
                .thenReturn(flowOf(true))
        val viewModel = createViewModel()

        val showConfirmDeleteTimeAlertObserver = viewModel.showConfirmDeleteArrivalAlertLiveData
                .test()
        val showAddTimeAlertObserver = viewModel.showAddArrivalAlertLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        viewModel.isArrivalAlertEnabledLiveData.test()
        viewModel.onNearestStopLongClicked("123456")
        advanceUntilIdle()
        val result = viewModel.onTimeAlertMenuItemClicked()
        advanceUntilIdle()

        assertTrue(result)
        showConfirmDeleteTimeAlertObserver.assertValues("123456")
        showAddTimeAlertObserver.assertEmpty()
        showContextMenuObserver.assertValues(true, false)
    }

    @Test
    fun onTimeAlertMenuItemClickedShowAddTimeAlertWhenNotAddedAsTimeAlert() = runTest {
        whenever(alertsStateRetriever.getHasArrivalAlertFlow(any()))
                .thenReturn(flowOf(false))
        val viewModel = createViewModel()

        val showConfirmDeleteTimeAlertObserver = viewModel.showConfirmDeleteArrivalAlertLiveData
                .test()
        val showAddTimeAlertObserver = viewModel.showAddArrivalAlertLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        viewModel.isArrivalAlertEnabledLiveData.test()
        viewModel.onNearestStopLongClicked("123456")
        advanceUntilIdle()
        val result = viewModel.onTimeAlertMenuItemClicked()
        advanceUntilIdle()

        assertTrue(result)
        showConfirmDeleteTimeAlertObserver.assertEmpty()
        showAddTimeAlertObserver.assertValues("123456")
        showContextMenuObserver.assertValues(true, false)
    }

    @Test
    fun onTimeAlertMenuItemClickedShowAddTimeAlertWhenTimeAlertStatusIsUnknown() = runTest {
        whenever(alertsStateRetriever.getHasArrivalAlertFlow(any()))
                .thenReturn(flowOf(null))
        val viewModel = createViewModel()

        val showConfirmDeleteTimeAlertObserver = viewModel.showConfirmDeleteArrivalAlertLiveData
                .test()
        val showAddTimeAlertObserver = viewModel.showAddArrivalAlertLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        viewModel.isArrivalAlertEnabledLiveData.test()
        viewModel.onNearestStopLongClicked("123456")
        advanceUntilIdle()
        val result = viewModel.onTimeAlertMenuItemClicked()
        advanceUntilIdle()

        assertTrue(result)
        showConfirmDeleteTimeAlertObserver.assertEmpty()
        showAddTimeAlertObserver.assertEmpty()
        showContextMenuObserver.assertValues(true, false)
    }

    @Test
    fun onShowOnMapMenuItemClickedReturnsFalseWhenNoSelectedStopCode() = runTest {
        val viewModel = createViewModel()

        val showOnMapObserver = viewModel.showOnMapLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        val result = viewModel.onShowOnMapMenuItemClicked()

        assertFalse(result)
        showOnMapObserver.assertEmpty()
        showContextMenuObserver.assertEmpty()
    }

    @Test
    fun onShowOnMapMenuItemClickedReturnsTrueWhenSelectedStopCode() = runTest {
        val viewModel = createViewModel()

        val showOnMapObserver = viewModel.showOnMapLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        viewModel.onNearestStopLongClicked("123456")
        advanceUntilIdle()
        val result = viewModel.onShowOnMapMenuItemClicked()
        advanceUntilIdle()

        assertTrue(result)
        showOnMapObserver.assertValues("123456")
        showContextMenuObserver.assertValues(true, false)
    }

    @Test
    fun onResolveErrorButtonClickedShowsLocationSettingsWhenStateIsLocationOff() = runTest {
        whenever(uiStateRetriever.getUiStateFlow(any(), any()))
                .thenReturn(flowOf(UiState.Error.LocationOff))
        val viewModel = createViewModel()

        viewModel.uiStateLiveData.test() // Kick-start collection
        val observer = viewModel.showLocationSettingsLiveData.test()
        advanceUntilIdle()
        viewModel.onResolveErrorButtonClicked()
        advanceUntilIdle()

        observer.assertSize(1)
    }

    @Test
    fun showTurnOnGpsLiveDataDoesNotEmitWhenPermissionsStateNotSet() = runTest {
        whenever(preferenceRepository.isGpsPromptDisabledFlow)
            .thenReturn(flowOf(false))
        val viewModel = createViewModel()

        val observer = viewModel.showTurnOnGpsLiveData.test()
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun showTurnOnGpsLiveDataDoesNotEmitWhenPermissionsAreDenied() = runTest {
        whenever(preferenceRepository.isGpsPromptDisabledFlow)
            .thenReturn(flowOf(false))
        val viewModel = createViewModel()

        val observer = viewModel.showTurnOnGpsLiveData.test()
        viewModel.permissionsState = PermissionsState(
                PermissionState.DENIED,
                PermissionState.DENIED)
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun showTurnOnGpsLiveDataDoesNotEmitWhenCoarseLocationIsDenied() = runTest {
        whenever(preferenceRepository.isGpsPromptDisabledFlow)
            .thenReturn(flowOf(false))
        val viewModel = createViewModel()

        val observer = viewModel.showTurnOnGpsLiveData.test()
        viewModel.permissionsState = PermissionsState(
                PermissionState.GRANTED,
                PermissionState.DENIED)
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun showTurnOnGpsLiveDataDoesNotEmitWhenFineLocationIsDenied() = runTest {
        whenever(preferenceRepository.isGpsPromptDisabledFlow)
            .thenReturn(flowOf(false))
        val viewModel = createViewModel()

        val observer = viewModel.showTurnOnGpsLiveData.test()
        viewModel.permissionsState = PermissionsState(
                PermissionState.DENIED,
                PermissionState.GRANTED)
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun showTurnOnGpsLiveDataDoesNotEmitWhenDoesNotHaveGpsLocationProvider() = runTest {
        whenever(locationRepository.hasGpsLocationProvider)
                .thenReturn(false)
        whenever(preferenceRepository.isGpsPromptDisabledFlow)
            .thenReturn(flowOf(false))
        val viewModel = createViewModel()

        val observer = viewModel.showTurnOnGpsLiveData.test()
        viewModel.permissionsState = PermissionsState(
                PermissionState.GRANTED,
                PermissionState.GRANTED)
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun showTurnOnGpsLiveDataDoesNotEmitWhenGpsPromptIsDisabled() = runTest {
        whenever(locationRepository.hasGpsLocationProvider)
            .thenReturn(true)
        whenever(preferenceRepository.isGpsPromptDisabledFlow)
            .thenReturn(flowOf(true))
        val viewModel = createViewModel()

        val observer = viewModel.showTurnOnGpsLiveData.test()
        viewModel.permissionsState = PermissionsState(
                PermissionState.GRANTED,
                PermissionState.GRANTED)
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun showTurnOnGpsLiveDataDoesNotEmitWhenUserHasAlreadyBeenAskedToTurnOnGps() = runTest {
        whenever(locationRepository.hasGpsLocationProvider)
            .thenReturn(true)
        whenever(preferenceRepository.isGpsPromptDisabledFlow)
            .thenReturn(flowOf(false))
        val viewModel = createViewModel(
            SavedStateHandle(
                mapOf(STATE_ASKED_TURN_ON_GPS to true)))

        val observer = viewModel.showTurnOnGpsLiveData.test()
        viewModel.permissionsState = PermissionsState(
            PermissionState.GRANTED,
            PermissionState.GRANTED)
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun showTurnOnGpsLiveDataDoesNotEmitGpsProviderIsEnabled() = runTest {
        whenever(locationRepository.hasGpsLocationProvider)
            .thenReturn(true)
        whenever(preferenceRepository.isGpsPromptDisabledFlow)
            .thenReturn(flowOf(false))
        whenever(locationRepository.isGpsLocationProviderEnabled)
            .thenReturn(true)
        val viewModel = createViewModel()

        val observer = viewModel.showTurnOnGpsLiveData.test()
        viewModel.permissionsState = PermissionsState(
            PermissionState.GRANTED,
            PermissionState.GRANTED)
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun showTurnOnGpsLiveDataEmitsWhenGpsProviderIsNotEnabled() = runTest {
        whenever(locationRepository.hasGpsLocationProvider)
            .thenReturn(true)
        whenever(preferenceRepository.isGpsPromptDisabledFlow)
            .thenReturn(flowOf(false))
        whenever(locationRepository.isGpsLocationProviderEnabled)
            .thenReturn(false)
        val viewModel = createViewModel()

        val observer = viewModel.showTurnOnGpsLiveData.test()
        viewModel.permissionsState = PermissionsState(
            PermissionState.GRANTED,
            PermissionState.GRANTED)
        advanceUntilIdle()

        observer.assertSize(1)
    }

    private fun createViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) =
            NearestStopsFragmentViewModel(
                    savedStateHandle,
                    servicesRepository,
                    busStopsRepository,
                    favouritesStateRetriever,
                    alertsStateRetriever,
                    featureRepository,
                    locationRepository,
                    preferenceRepository,
                    uiStateRetriever,
                    coroutineRule.testDispatcher)
}