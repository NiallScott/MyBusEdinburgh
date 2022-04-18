/*
 * Copyright (C) 2021 - 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.alerts.proximity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.alerts.proximity.ProximityAlertRequest
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopName
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.coroutines.FlowTestObserver
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.testutils.test

/**
 * Tests for [AddProximityAlertDialogFragmentViewModel].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AddProximityAlertDialogFragmentViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var busStopsRepository: BusStopsRepository
    @Mock
    private lateinit var uiStateCalculator: UiStateCalculator
    @Mock
    private lateinit var alertsRepository: AlertsRepository

    private lateinit var viewModel: AddProximityAlertDialogFragmentViewModel

    @Before
    fun setUp() {
        viewModel = AddProximityAlertDialogFragmentViewModel(
                busStopsRepository,
                uiStateCalculator,
                alertsRepository,
                coroutineRule.scope,
                coroutineRule.testDispatcher)
    }

    @Test
    fun stopDetailsLiveDataEmitsNullWhenNoStopCodeIsSet() = runTest {
        val observer = viewModel.stopDetailsLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun stopDetailsLiveDataEmitsNullWhenStopCodeIsSetAsNull() = runTest {
        val observer = viewModel.stopDetailsLiveData.test()

        viewModel.stopCode = null
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun stopDetailsLiveDataEmitsStopDetailsWithNullNameWhenRepositoryReturnsNullName() = runTest {
        val observer = viewModel.stopDetailsLiveData.test()
        whenever(busStopsRepository.getNameForStopFlow("123456"))
                .thenReturn(flowOf(null))

        viewModel.stopCode = "123456"
        advanceUntilIdle()

        observer.assertValues(null, StopDetails("123456", null))
    }

    @Test
    fun stopDetailsLiveDataEmitsStopDetailsWithNameWhenRepositoryReturnsName() = runTest {
        val observer = viewModel.stopDetailsLiveData.test()
        whenever(busStopsRepository.getNameForStopFlow("123456"))
                .thenReturn(flowOf(StopName("Name", "Locality")))

        viewModel.stopCode = "123456"
        advanceUntilIdle()

        observer.assertValues(null, StopDetails("123456", StopName("Name", "Locality")))
    }

    @Test
    fun stopDetailsLiveDataEmitsCorrectDataOnChanges() = runTest {
        whenever(busStopsRepository.getNameForStopFlow("123456"))
                .thenReturn(flow {
                    emit(StopName("Name", "Locality"))
                    delay(10L)
                    emit(StopName("Name 2", null))
                })
        whenever(busStopsRepository.getNameForStopFlow("987654"))
                .thenReturn(flowOf(StopName("Name 3", "Locality 3")))

        val observer = viewModel.stopDetailsLiveData.test()
        viewModel.stopCode = "123456"
        advanceUntilIdle()
        viewModel.stopCode = "987654"
        advanceUntilIdle()

        observer.assertValues(
                null,
                StopDetails("123456", StopName("Name", "Locality")),
                StopDetails("123456", StopName("Name 2", null)),
                null,
                StopDetails("987654", StopName("Name 3", "Locality 3")))
    }

    @Test
    fun uiStateLiveDataEmitsCalculatedState() = runTest {
        whenever(uiStateCalculator.createUiStateFlow(any(), any()))
                .thenReturn(flowOf(
                        UiState.ERROR_PERMISSION_UNGRANTED,
                        UiState.PROGRESS,
                        UiState.CONTENT))

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
                UiState.ERROR_PERMISSION_UNGRANTED,
                UiState.PROGRESS,
                UiState.CONTENT)
    }

    @Test
    fun uiStateCalculatorIsPassedCorrectFlowObjects() = runTest {
        val locationPermissionObserver = FlowTestObserver<PermissionState>(this)
        val stopDetailsObserver = FlowTestObserver<StopDetails?>(this)
        doAnswer {
            locationPermissionObserver.observe(it.getArgument(0))
            stopDetailsObserver.observe(it.getArgument(1))

            flowOf(UiState.CONTENT)
        }.whenever(uiStateCalculator).createUiStateFlow(any(), any())
        whenever(busStopsRepository.getNameForStopFlow("123456"))
                .thenReturn(flowOf(null))
        whenever(busStopsRepository.getNameForStopFlow("987654"))
                .thenReturn(flowOf(null))

        viewModel.uiStateLiveData.test()
        advanceUntilIdle()
        viewModel.locationPermissionState = PermissionState.DENIED
        advanceUntilIdle()
        viewModel.locationPermissionState = PermissionState.GRANTED
        advanceUntilIdle()
        viewModel.stopCode = "123456"
        advanceUntilIdle()
        viewModel.stopCode = "987654"
        advanceUntilIdle()
        locationPermissionObserver.finish()
        stopDetailsObserver.finish()

        locationPermissionObserver.assertValues(
                PermissionState.UNGRANTED,
                PermissionState.DENIED,
                PermissionState.GRANTED)
        stopDetailsObserver.assertValues(
                null,
                StopDetails("123456", null),
                null,
                StopDetails("987654", null))
    }

    @Test
    fun addButtonEnabledLiveDataOnlyEmitsTrueWhenShowingContent() = runTest {
        whenever(uiStateCalculator.createUiStateFlow(any(), any()))
                .thenReturn(flowOf(
                        UiState.ERROR_NO_LOCATION_FEATURE,
                        UiState.ERROR_PERMISSION_UNGRANTED,
                        UiState.ERROR_PERMISSION_DENIED,
                        UiState.ERROR_LOCATION_DISABLED,
                        UiState.PROGRESS,
                        UiState.CONTENT,
                        UiState.PROGRESS))

        val observer = viewModel.addButtonEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false, true, false)
    }

    @Test
    fun onLimitationsButtonClickedShowsLimitations() = runTest {
        val observer = viewModel.showLimitationsLiveData.test()
        viewModel.onLimitationsButtonClicked()
        advanceUntilIdle()

        observer.assertSize(1)
    }

    @Test
    fun onResolveErrorButtonClickedDoesNotTriggerEventWhenNotHandled() = runTest {
        whenever(uiStateCalculator.createUiStateFlow(any(), any()))
                .thenReturn(flowOf(UiState.CONTENT))
        val uiStateObserver = viewModel.uiStateLiveData.test()
        val locationSettingsObserver = viewModel.showLocationSettingsLiveData.test()
        val requestLocationPermissionObserver = viewModel.requestLocationPermissionLiveData.test()
        val showAppSettingsObserver = viewModel.showAppSettingsLiveData.test()

        viewModel.onResolveErrorButtonClicked()
        advanceUntilIdle()

        uiStateObserver.assertValues(UiState.CONTENT)
        locationSettingsObserver.assertEmpty()
        requestLocationPermissionObserver.assertEmpty()
        showAppSettingsObserver.assertEmpty()
    }

    @Test
    fun onResolveErrorButtonClickedShowsLocationSettingsWhenLocationDisabled() = runTest {
        whenever(uiStateCalculator.createUiStateFlow(any(), any()))
                .thenReturn(flowOf(UiState.ERROR_LOCATION_DISABLED))
        val uiStateObserver = viewModel.uiStateLiveData.test()
        val locationSettingsObserver = viewModel.showLocationSettingsLiveData.test()
        val requestLocationPermissionObserver = viewModel.requestLocationPermissionLiveData.test()
        val showAppSettingsObserver = viewModel.showAppSettingsLiveData.test()

        advanceUntilIdle()
        viewModel.onResolveErrorButtonClicked()
        advanceUntilIdle()

        uiStateObserver.assertValues(UiState.ERROR_LOCATION_DISABLED)
        locationSettingsObserver.assertSize(1)
        requestLocationPermissionObserver.assertEmpty()
        showAppSettingsObserver.assertEmpty()
    }

    @Test
    fun onResolveErrorButtonClickedShowsRequestLocationPermissionWhenLocationPermissionUngranted() =
            runTest {
        whenever(uiStateCalculator.createUiStateFlow(any(), any()))
                .thenReturn(flowOf(UiState.ERROR_PERMISSION_UNGRANTED))
        val uiStateObserver = viewModel.uiStateLiveData.test()
        val locationSettingsObserver = viewModel.showLocationSettingsLiveData.test()
        val requestLocationPermissionObserver = viewModel.requestLocationPermissionLiveData.test()
        val showAppSettingsObserver = viewModel.showAppSettingsLiveData.test()

        advanceUntilIdle()
        viewModel.onResolveErrorButtonClicked()
        advanceUntilIdle()

        uiStateObserver.assertValues(UiState.ERROR_PERMISSION_UNGRANTED)
        locationSettingsObserver.assertEmpty()
        requestLocationPermissionObserver.assertSize(1)
        showAppSettingsObserver.assertEmpty()
    }

    @Test
    fun onResolveErrorButtonClickedShowsAppSettingsWhenLocationPermissionDenied() = runTest {
        whenever(uiStateCalculator.createUiStateFlow(any(), any()))
                .thenReturn(flowOf(UiState.ERROR_PERMISSION_DENIED))
        val uiStateObserver = viewModel.uiStateLiveData.test()
        val locationSettingsObserver = viewModel.showLocationSettingsLiveData.test()
        val requestLocationPermissionObserver = viewModel.requestLocationPermissionLiveData.test()
        val showAppSettingsObserver = viewModel.showAppSettingsLiveData.test()

        advanceUntilIdle()
        viewModel.onResolveErrorButtonClicked()
        advanceUntilIdle()

        uiStateObserver.assertValues(UiState.ERROR_PERMISSION_DENIED)
        locationSettingsObserver.assertEmpty()
        requestLocationPermissionObserver.assertEmpty()
        showAppSettingsObserver.assertSize(1)
    }

    @Test
    fun handleAddClickedDoesNotAddAlertWhenStopCodeIsNull() = runTest {
        viewModel.stopCode = null

        viewModel.handleAddClicked(250)
        advanceUntilIdle()

        verify(alertsRepository, never())
                .addProximityAlert(any())
    }

    @Test
    fun handleAddClickedDoesNotAddAlertWhenStopCodeIsEmpty() = runTest {
        viewModel.stopCode = ""

        viewModel.handleAddClicked(250)
        advanceUntilIdle()

        verify(alertsRepository, never())
                .addProximityAlert(any())
    }

    @Test
    fun handleAddClickedAddsAlertWhenStopCodeIsPopulated() = runTest {
        viewModel.stopCode = "123456"

        viewModel.handleAddClicked(250)
        advanceUntilIdle()

        verify(alertsRepository)
                .addProximityAlert(ProximityAlertRequest("123456", 250))
    }
}