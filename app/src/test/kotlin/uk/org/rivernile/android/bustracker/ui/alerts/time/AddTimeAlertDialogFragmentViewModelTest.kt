/*
 * Copyright (C) 2021 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.alerts.time

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.ArrivalAlertRequest
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopName
import uk.org.rivernile.android.bustracker.core.servicestops.ServiceStopsRepository
import uk.org.rivernile.android.bustracker.coroutines.FlowTestObserver
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.testutils.test
import uk.org.rivernile.android.bustracker.utils.SingleLiveEvent

/**
 * Tests for [AddTimeAlertDialogFragmentViewModel].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class AddTimeAlertDialogFragmentViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var permissionsTracker: PermissionsTracker
    @Mock
    private lateinit var busStopsRepository: BusStopsRepository
    @Mock
    private lateinit var serviceStopsRepository: ServiceStopsRepository
    @Mock
    private lateinit var uiStateCalculator: UiStateCalculator
    @Mock
    private lateinit var alertsRepository: AlertsRepository

    @Test
    fun selectedServicesLiveDataEmitsNullAsInitialState() = runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        val viewModel = createViewModel()

        val observer = viewModel.selectedServicesLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun selectedServicesLiveDataEmitsNullWhenInitialStateIsNull() = runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        val initialState = mapOf(
                AddTimeAlertDialogFragmentViewModel.STATE_SELECTED_SERVICES to null)
        val savedState = SavedStateHandle(initialState)
        val viewModel = createViewModel(savedState)

        val observer = viewModel.selectedServicesLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun selectedServicesLiveDataEmitsNullWhenInitialStateIsEmpty() = runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        val initialState = mapOf(
                AddTimeAlertDialogFragmentViewModel.STATE_SELECTED_SERVICES to emptyArray<String>())
        val savedState = SavedStateHandle(initialState)
        val viewModel = createViewModel(savedState)

        val observer = viewModel.selectedServicesLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun selectedServicesLiveDataEmitsInitialStateWhenSet() = runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        val initialState = mapOf(
                AddTimeAlertDialogFragmentViewModel.STATE_SELECTED_SERVICES to arrayOf("1", "2"))
        val savedState = SavedStateHandle(initialState)
        val viewModel = createViewModel(savedState)

        val observer = viewModel.selectedServicesLiveData.test()
        advanceUntilIdle()

        observer.assertValues(listOf("1", "2"))
    }

    @Test
    fun selectedServicesLiveDataEmitsChanges() = runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        val viewModel = createViewModel()

        val observer = viewModel.selectedServicesLiveData.test()
        advanceUntilIdle()
        viewModel.selectedServices = listOf("1")
        advanceUntilIdle()
        viewModel.selectedServices = emptyList()
        advanceUntilIdle()
        viewModel.selectedServices = listOf("1", "2")
        advanceUntilIdle()

        observer.assertValues(
                null,
                listOf("1"),
                null,
                listOf("1", "2"))
    }

    @Test
    fun requestPermissionsLiveDataReturnsLiveDataFromPermissionsTracker() {
        givenPermissionsTrackerFlowHasPermissionsState()
        val expected = SingleLiveEvent<Unit>()
        whenever(permissionsTracker.requestPermissionsLiveData)
                .thenReturn(expected)
        val viewModel = createViewModel()

        val result = viewModel.requestPermissionsLiveData

        assertSame(expected, result)
    }

    @Test
    fun stopDetailsLiveDataEmitsNullWhenNoStopCodeIsSet() = runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        val viewModel = createViewModel()

        val observer = viewModel.stopDetailsLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun stopDetailsLiveDataEmitsNullWhenStopCodeIsSetAsNull() = runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        val viewModel = createViewModel()

        val observer = viewModel.stopDetailsLiveData.test()
        viewModel.stopCode = null
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun stopDetailsLiveDataEmitsStopDetailsWithNullNameWhenRepositoryReturnsNullName() = runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        val viewModel = createViewModel()
        whenever(busStopsRepository.getNameForStopFlow("123456"))
                .thenReturn(flowOf(null))

        val observer = viewModel.stopDetailsLiveData.test()
        viewModel.stopCode = "123456"
        advanceUntilIdle()

        observer.assertValues(
                null,
                StopDetails("123456", null))
    }

    @Test
    fun stopDetailsLiveDataEmitsStopDetailsWithNameWhenRepositoryReturnsName() = runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        val viewModel = createViewModel()
        whenever(busStopsRepository.getNameForStopFlow("123456"))
                .thenReturn(flowOf(StopName("Name", "Locality")))

        val observer = viewModel.stopDetailsLiveData.test()
        viewModel.stopCode = "123456"
        advanceUntilIdle()

        observer.assertValues(
                null,
                StopDetails("123456", null),
                StopDetails("123456", StopName("Name", "Locality")))
    }

    @Test
    fun stopDetailsLiveDataEmitsCorrectDataOnChanges() = runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        val viewModel = createViewModel()
        whenever(busStopsRepository.getNameForStopFlow("123456"))
                .thenReturn(flow{
                    emit(StopName("Name", "Locality"))
                    delay(10L)
                    emit(StopName("Name 2", null))
                })
        whenever(busStopsRepository.getNameForStopFlow("987654"))
                .thenReturn(flowOf(StopName("Name 3", "Locality 3")))

        val observer = viewModel.stopDetailsLiveData.test()
        advanceUntilIdle()
        viewModel.stopCode = "123456"
        advanceUntilIdle()
        viewModel.stopCode = "987654"
        advanceUntilIdle()

        observer.assertValues(
                null,
                StopDetails("123456", null),
                StopDetails("123456", StopName("Name", "Locality")),
                StopDetails("123456", StopName("Name 2", null)),
                StopDetails("987654", null),
                StopDetails("987654", StopName("Name 3", "Locality 3")))
    }

    @Test
    fun uiStateLiveDataEmitsCalculatedState() = runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        whenever(uiStateCalculator.createUiStateFlow(any(), any(), any(), any()))
                .thenReturn(flow {
                    emit(UiState.ERROR_NO_STOP_CODE)
                    delay(100L)
                    emit(UiState.ERROR_PERMISSION_REQUIRED)
                    delay(100L)
                    emit(UiState.ERROR_PERMISSION_DENIED)
                    delay(100L)
                    emit(UiState.ERROR_NO_SERVICES)
                    delay(100L)
                    emit(UiState.PROGRESS)
                    delay(100L)
                    emit(UiState.CONTENT)
                })
        val viewModel = createViewModel()
        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
                UiState.ERROR_NO_STOP_CODE,
                UiState.ERROR_PERMISSION_REQUIRED,
                UiState.ERROR_PERMISSION_DENIED,
                UiState.ERROR_NO_SERVICES,
                UiState.PROGRESS,
                UiState.CONTENT)
    }

    @Test
    fun uiStateCalculatorIsPassedCorrectFlowObjects() = runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        val stopCodeFlow = FlowTestObserver<String?>(this)
        val stopDetailsFlow = FlowTestObserver<StopDetails?>(this)
        val availableServicesFlow = FlowTestObserver<List<String>?>(this)
        val permissionsStateFlow = FlowTestObserver<PermissionsState>(this)
        doAnswer {
            stopCodeFlow.observe(it.getArgument(0))
            stopDetailsFlow.observe(it.getArgument(1))
            availableServicesFlow.observe(it.getArgument(2))
            permissionsStateFlow.observe(it.getArgument(3))

            flowOf(UiState.CONTENT)
        }.whenever(uiStateCalculator).createUiStateFlow(any(), any(), any(), any())
        whenever(busStopsRepository.getNameForStopFlow("123456"))
                .thenReturn(flowOf(StopName("Name 1", "Locality 1")))
        whenever(busStopsRepository.getNameForStopFlow("987654"))
                .thenReturn(flowOf(StopName("Name 2", "Locality 2")))
        whenever(serviceStopsRepository.getServicesForStopFlow("123456"))
                .thenReturn(flowOf(listOf("1", "2", "3")))
        whenever(serviceStopsRepository.getServicesForStopFlow("987654"))
                .thenReturn(flowOf(listOf("4", "5", "6")))
        val viewModel = createViewModel()

        viewModel.uiStateLiveData.test()
        advanceUntilIdle()
        viewModel.stopCode = "123456"
        advanceUntilIdle()
        viewModel.stopCode = "987654"
        advanceUntilIdle()
        stopCodeFlow.finish()
        stopDetailsFlow.finish()
        availableServicesFlow.finish()
        permissionsStateFlow.finish()

        stopCodeFlow.assertValues(null, "123456", "987654")
        stopDetailsFlow.assertValues(
                null,
                StopDetails("123456", null),
                StopDetails("123456", StopName("Name 1", "Locality 1")),
                StopDetails("987654", null),
                StopDetails("987654", StopName("Name 2", "Locality 2")))
        availableServicesFlow.assertValues(
                null,
                listOf("1", "2", "3"),
                null,
                listOf("4", "5", "6"))
    }

    @Test
    fun addButtonEnabledLiveDataEmitsFalseWhenSelectedServicesIsNull() = runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        val uiStateFlow = flow {
            emit(UiState.ERROR_NO_STOP_CODE)
            delay(100L)
            emit(UiState.ERROR_PERMISSION_REQUIRED)
            delay(100L)
            emit(UiState.ERROR_PERMISSION_DENIED)
            delay(100L)
            emit(UiState.ERROR_NO_SERVICES)
            delay(100L)
            emit(UiState.PROGRESS)
            delay(100L)
            emit(UiState.CONTENT)
        }
        whenever(uiStateCalculator.createUiStateFlow(any(), any(), any(), any()))
                .thenReturn(uiStateFlow)
        val viewModel = createViewModel()

        val observer = viewModel.addButtonEnabledLiveData.test()
        viewModel.selectedServices = null
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun addButtonEnabledLiveDataEmitsFalseWhenSelectedServicesIsEmpty() = runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        val uiStateFlow = flow {
            emit(UiState.ERROR_NO_STOP_CODE)
            delay(100L)
            emit(UiState.ERROR_PERMISSION_REQUIRED)
            delay(100L)
            emit(UiState.ERROR_PERMISSION_DENIED)
            delay(100L)
            emit(UiState.ERROR_NO_SERVICES)
            delay(100L)
            emit(UiState.PROGRESS)
            delay(100L)
            emit(UiState.CONTENT)
        }
        whenever(uiStateCalculator.createUiStateFlow(any(), any(), any(), any()))
                .thenReturn(uiStateFlow)
        val viewModel = createViewModel()

        val observer = viewModel.addButtonEnabledLiveData.test()
        viewModel.selectedServices = emptyList()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun addButtonEnabledLiveDataEmitsTrueWhenSelectedServicesPopulatedAndShowingContent() =
            runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        val uiStateFlow = flow {
            emit(UiState.ERROR_NO_STOP_CODE)
            delay(100L)
            emit(UiState.ERROR_PERMISSION_REQUIRED)
            delay(100L)
            emit(UiState.ERROR_PERMISSION_DENIED)
            delay(100L)
            emit(UiState.ERROR_NO_SERVICES)
            delay(100L)
            emit(UiState.PROGRESS)
            delay(100L)
            emit(UiState.CONTENT)
            delay(100L)
            emit(UiState.CONTENT)
            delay(100L)
            emit(UiState.PROGRESS)
            delay(100L)
            emit(UiState.CONTENT)
        }
        whenever(uiStateCalculator.createUiStateFlow(any(), any(), any(), any()))
                .thenReturn(uiStateFlow)
        val viewModel = createViewModel()

        val observer = viewModel.addButtonEnabledLiveData.test()
        viewModel.selectedServices = listOf("1", "2", "3")
        advanceUntilIdle()

        observer.assertValues(false, true, false, true)
    }

    @Test
    fun onLimitationsButtonClickedShowsLimitations() {
        givenPermissionsTrackerFlowHasPermissionsState()
        val viewModel = createViewModel()

        val observer = viewModel.showLimitationsLiveData.test()
        viewModel.onLimitationsButtonClicked()

        observer.assertSize(1)
    }

    @Test
    fun onSelectServicesClickedDoesNotShowServicesSelectionWhenAvailableServicesIsNull() = runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        val availableServicesFlow = FlowTestObserver<List<String>?>(this)
        doAnswer {
            availableServicesFlow.observe(it.getArgument(2))

            flowOf(UiState.CONTENT)
        }.whenever(uiStateCalculator).createUiStateFlow(any(), any(), any(), any())
        whenever(serviceStopsRepository.getServicesForStopFlow("123456"))
                .thenReturn(flowOf(null))
        val viewModel = createViewModel()

        val observer = viewModel.showServicesChooserLiveData.test()
        viewModel.stopCode = "123456"
        advanceUntilIdle()
        viewModel.onSelectServicesClicked()
        advanceUntilIdle()
        availableServicesFlow.finish()

        observer.assertEmpty()
    }

    @Test
    fun onSelectServicesClickedDoesNotShowServicesSelectionWhenAvailableServicesIsEmpty() =
            runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        val availableServicesFlow = FlowTestObserver<List<String>?>(this)
        doAnswer {
            availableServicesFlow.observe(it.getArgument(2))

            flowOf(UiState.CONTENT)
        }.whenever(uiStateCalculator).createUiStateFlow(any(), any(), any(), any())
        whenever(serviceStopsRepository.getServicesForStopFlow("123456"))
                .thenReturn(flowOf(emptyList()))
        val viewModel = createViewModel()

        val observer = viewModel.showServicesChooserLiveData.test()
        viewModel.stopCode = "123456"
        advanceUntilIdle()
        viewModel.onSelectServicesClicked()
        advanceUntilIdle()
        availableServicesFlow.finish()

        observer.assertEmpty()
    }

    @Test
    fun onSelectServicesClickedShowsServicesSelectionWithNullSelectedServicesFirstTime() = runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        val availableServicesFlow = FlowTestObserver<List<String>?>(this)
        doAnswer {
            availableServicesFlow.observe(it.getArgument(2))

            flowOf(UiState.CONTENT)
        }.whenever(uiStateCalculator).createUiStateFlow(any(), any(), any(), any())
        whenever(serviceStopsRepository.getServicesForStopFlow("123456"))
                .thenReturn(flowOf(listOf("1", "2", "3")))
        val viewModel = createViewModel()

        val observer = viewModel.showServicesChooserLiveData.test()
        viewModel.stopCode = "123456"
        advanceUntilIdle()
        viewModel.onSelectServicesClicked()
        advanceUntilIdle()
        availableServicesFlow.finish()

        observer.assertValues(
                ServicesChooserParams(listOf("1", "2", "3"), null))
    }

    @Test
    fun onSelectServicesClickedShowsServicesSelectionWithInitialStateSelectedServices() = runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        val availableServicesFlow = FlowTestObserver<List<String>?>(this)
        doAnswer {
            availableServicesFlow.observe(it.getArgument(2))

            flowOf(UiState.CONTENT)
        }.whenever(uiStateCalculator).createUiStateFlow(any(), any(), any(), any())
        whenever(serviceStopsRepository.getServicesForStopFlow("123456"))
                .thenReturn(flowOf(listOf("1", "2", "3")))
        val initialState = mapOf(
                AddTimeAlertDialogFragmentViewModel.STATE_SELECTED_SERVICES to arrayOf("2", "3"))
        val savedState = SavedStateHandle(initialState)
        val viewModel = createViewModel(savedState)

        val chooserObserver = viewModel.showServicesChooserLiveData.test()
        viewModel.selectedServicesLiveData.test()
        viewModel.stopCode = "123456"
        advanceUntilIdle()
        viewModel.onSelectServicesClicked()
        advanceUntilIdle()
        availableServicesFlow.finish()

        chooserObserver.assertValues(
                ServicesChooserParams(listOf("1", "2", "3"), listOf("2", "3")))
    }

    @Test
    fun onSelectServicesClickedShowsServicesSelectionWithSelectedServicesSet() = runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        val availableServicesFlow = FlowTestObserver<List<String>?>(this)
        doAnswer {
            availableServicesFlow.observe(it.getArgument(2))

            flowOf(UiState.CONTENT)
        }.whenever(uiStateCalculator).createUiStateFlow(any(), any(), any(), any())
        whenever(serviceStopsRepository.getServicesForStopFlow("123456"))
                .thenReturn(flowOf(listOf("1", "2", "3")))
        val viewModel = createViewModel()

        val chooserObserver = viewModel.showServicesChooserLiveData.test()
        viewModel.selectedServicesLiveData.test()
        viewModel.stopCode = "123456"
        advanceUntilIdle()
        viewModel.selectedServices = listOf("2", "3")
        advanceUntilIdle()
        viewModel.onSelectServicesClicked()
        advanceUntilIdle()
        availableServicesFlow.finish()

        chooserObserver.assertValues(
                ServicesChooserParams(listOf("1", "2", "3"), listOf("2", "3")))
    }

    @Test
    fun onSelectServicesClickedShowsServicesSelectionWithRepresentativeExample() = runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        val availableServicesFlow = FlowTestObserver<List<String>?>(this)
        doAnswer {
            availableServicesFlow.observe(it.getArgument(2))

            flowOf(UiState.CONTENT)
        }.whenever(uiStateCalculator).createUiStateFlow(any(), any(), any(), any())
        whenever(serviceStopsRepository.getServicesForStopFlow("123456"))
                .thenReturn(flowOf(listOf("1", "2", "3")))
        val initialState = mapOf(
                AddTimeAlertDialogFragmentViewModel.STATE_SELECTED_SERVICES to arrayOf("2", "3"))
        val savedState = SavedStateHandle(initialState)
        val viewModel = createViewModel(savedState)

        val chooserObserver = viewModel.showServicesChooserLiveData.test()
        viewModel.selectedServicesLiveData.test()
        viewModel.stopCode = "123456"
        advanceUntilIdle()
        viewModel.onSelectServicesClicked()
        advanceUntilIdle()
        viewModel.selectedServices = listOf("2")
        advanceUntilIdle()
        viewModel.onSelectServicesClicked()
        advanceUntilIdle()
        availableServicesFlow.finish()

        chooserObserver.assertValues(
                ServicesChooserParams(listOf("1", "2", "3"), listOf("2", "3")),
                ServicesChooserParams(listOf("1", "2", "3"), listOf("2")))
    }

    @Test
    fun onPermissionsUpdatedUpdatesStateInPermissionsTracker() {
        givenPermissionsTrackerFlowHasPermissionsState()
        val viewModel = createViewModel()
        val expected = UiPermissionsState(true)

        viewModel.onPermissionsUpdated(expected)

        verify(permissionsTracker)
                .permissionsState = expected
    }

    @Test
    fun onPermissionsResultUpdatesStateInPermissionsTracker() {
        givenPermissionsTrackerFlowHasPermissionsState()
        val viewModel = createViewModel()
        val expected = UiPermissionsState(true)

        viewModel.onPermissionsResult(expected)

        verify(permissionsTracker)
                .permissionsState = expected
    }

    @Test
    fun onResolveButtonClickedDoesNotTriggerEventWhenNotHandled() = runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        whenever(uiStateCalculator.createUiStateFlow(any(), any(), any(), any()))
                .thenReturn(flowOf(UiState.CONTENT))
        val viewModel = createViewModel()
        viewModel.uiStateLiveData.test()
        val showAppSettingsObserver = viewModel.showAppSettingsLiveData.test()

        advanceUntilIdle()
        viewModel.onResolveButtonClicked()

        verify(permissionsTracker, never())
                .onRequestPermissionsClicked()
        showAppSettingsObserver.assertEmpty()
    }

    @Test
    fun onResolveButtonClickedRequestsPermissionsWhenPermissionRequired() = runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        whenever(uiStateCalculator.createUiStateFlow(any(), any(), any(), any()))
                .thenReturn(flowOf(UiState.ERROR_PERMISSION_REQUIRED))
        val viewModel = createViewModel()
        viewModel.uiStateLiveData.test()
        val showAppSettingsObserver = viewModel.showAppSettingsLiveData.test()

        advanceUntilIdle()
        viewModel.onResolveButtonClicked()

        verify(permissionsTracker)
                .onRequestPermissionsClicked()
        showAppSettingsObserver.assertEmpty()
    }

    @Test
    fun onResolveButtonClickedShowsAppSettingsWhenPermissionDenied() = runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        whenever(uiStateCalculator.createUiStateFlow(any(), any(), any(), any()))
                .thenReturn(flowOf(UiState.ERROR_PERMISSION_DENIED))
        val viewModel = createViewModel()
        viewModel.uiStateLiveData.test()
        val showAppSettingsObserver = viewModel.showAppSettingsLiveData.test()

        advanceUntilIdle()
        viewModel.onResolveButtonClicked()

        verify(permissionsTracker, never())
                .onRequestPermissionsClicked()
        showAppSettingsObserver.assertSize(1)
    }

    @Test
    fun onAddClickedDoesNotAddAlertWhenStopCodeIsNull() = runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        val viewModel = createViewModel()
        viewModel.stopCode = null
        viewModel.selectedServices = listOf("1", "2", "3")
        viewModel.selectedServicesLiveData.test()

        viewModel.onAddClicked(10)

        verify(alertsRepository, never())
                .addArrivalAlert(any())
    }

    @Test
    fun onAddClickedDoesNotAddAlertWhenStopCodeIsEmpty() = runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        val viewModel = createViewModel()
        viewModel.stopCode = ""
        viewModel.selectedServices = listOf("1", "2", "3")
        viewModel.selectedServicesLiveData.test()

        viewModel.onAddClicked(10)

        verify(alertsRepository, never())
                .addArrivalAlert(any())
    }

    @Test
    fun onAddClickedDoesNotAddAlertWhenSelectedServicesIsNull() = runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        val viewModel = createViewModel()
        viewModel.stopCode = "123456"
        viewModel.selectedServices = null
        viewModel.selectedServicesLiveData.test()

        viewModel.onAddClicked(10)

        verify(alertsRepository, never())
                .addArrivalAlert(any())
    }

    @Test
    fun onAddClickedDoesNotAddAlertWhenSelectedServicesIsEmpty() = runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        val viewModel = createViewModel()
        viewModel.stopCode = "123456"
        viewModel.selectedServices = listOf()
        viewModel.selectedServicesLiveData.test()

        viewModel.onAddClicked(10)

        verify(alertsRepository, never())
                .addArrivalAlert(any())
    }

    @Test
    fun onAddClickedAddsAlertsWhenConditionsAreSatisfied() = runTest {
        givenPermissionsTrackerFlowHasPermissionsState()
        val viewModel = createViewModel()
        viewModel.stopCode = "123456"
        viewModel.selectedServices = listOf("1", "2", "3")
        val expected = ArrivalAlertRequest("123456", listOf("1", "2", "3"), 10)
        viewModel.selectedServicesLiveData.test()
        advanceUntilIdle()

        viewModel.onAddClicked(10)
        advanceUntilIdle()

        verify(alertsRepository)
                .addArrivalAlert(expected)
    }

    private fun givenPermissionsTrackerFlowHasPermissionsState() {
        whenever(permissionsTracker.permissionsStateFlow)
                .thenReturn(flowOf(PermissionsState()))
    }

    private fun createViewModel(savedState: SavedStateHandle = SavedStateHandle()) =
            AddTimeAlertDialogFragmentViewModel(
                    savedState,
                    permissionsTracker,
                    busStopsRepository,
                    serviceStopsRepository,
                    uiStateCalculator,
                    alertsRepository,
                    coroutineRule.scope,
                    coroutineRule.testDispatcher)
}