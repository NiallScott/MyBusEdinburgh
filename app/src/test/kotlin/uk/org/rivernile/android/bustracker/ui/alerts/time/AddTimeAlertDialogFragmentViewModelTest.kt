/*
 * Copyright (C) 2021 Niall 'Rivernile' Scott
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
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.ArrivalAlertRequest
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopName
import uk.org.rivernile.android.bustracker.core.servicestops.ServiceStopsRepository
import uk.org.rivernile.android.bustracker.coroutines.FlowTestObserver
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.testutils.LiveDataTestObserver

/**
 * Tests for [AddTimeAlertDialogFragmentViewModel].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AddTimeAlertDialogFragmentViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var busStopsRepository: BusStopsRepository
    @Mock
    private lateinit var serviceStopsRepository: ServiceStopsRepository
    @Mock
    private lateinit var uiStateCalculator: UiStateCalculator
    @Mock
    private lateinit var alertsRepository: AlertsRepository

    @Test
    fun selectedServicesLiveDataEmitsNullAsInitialState() {
        val viewModel = createViewModel()
        val observer = LiveDataTestObserver<List<String>?>()

        viewModel.selectedServicesLiveData.observeForever(observer)

        observer.assertValues(null)
    }

    @Test
    fun selectedServicesLiveDataEmitsNullWhenInitialStateIsNull() {
        val initialState = mapOf(
                AddTimeAlertDialogFragmentViewModel.STATE_SELECTED_SERVICES to null)
        val savedState = SavedStateHandle(initialState)
        val viewModel = createViewModel(savedState)
        val observer = LiveDataTestObserver<List<String>?>()

        viewModel.selectedServicesLiveData.observeForever(observer)

        observer.assertValues(null)
    }

    @Test
    fun selectedServicesLiveDataEmitsNullWhenInitialStateIsEmpty() {
        val initialState = mapOf(
                AddTimeAlertDialogFragmentViewModel.STATE_SELECTED_SERVICES to emptyArray<String>())
        val savedState = SavedStateHandle(initialState)
        val viewModel = createViewModel(savedState)
        val observer = LiveDataTestObserver<List<String>?>()

        viewModel.selectedServicesLiveData.observeForever(observer)

        observer.assertValues(null)
    }

    @Test
    fun selectedServicesLiveDataEmitsInitialStateWhenSet() {
        val initialState = mapOf(
                AddTimeAlertDialogFragmentViewModel.STATE_SELECTED_SERVICES to arrayOf("1", "2"))
        val savedState = SavedStateHandle(initialState)
        val viewModel = createViewModel(savedState)
        val observer = LiveDataTestObserver<List<String>?>()

        viewModel.selectedServicesLiveData.observeForever(observer)

        observer.assertValues(listOf("1", "2"))
    }

    @Test
    fun selectedServicesLiveDataEmitsChanges() {
        val viewModel = createViewModel()
        val observer = LiveDataTestObserver<List<String>?>()

        viewModel.selectedServicesLiveData.observeForever(observer)
        viewModel.selectedServices = listOf("1")
        viewModel.selectedServices = emptyList()
        viewModel.selectedServices = listOf("1", "2")

        observer.assertValues(
                null,
                listOf("1"),
                null,
                listOf("1", "2"))
    }

    @Test
    fun stopDetailsLiveDataEmitsNullWhenNoStopCodeIsSet() = coroutineRule.runBlockingTest {
        val viewModel = createViewModel()
        val observer = LiveDataTestObserver<StopDetails?>()
        viewModel.stopDetailsLiveData.observeForever(observer)

        observer.assertValues(null)
    }

    @Test
    fun stopDetailsLiveDataEmitsNullWhenStopCodeIsSetAsNull() = coroutineRule.runBlockingTest {
        val viewModel = createViewModel()
        val observer = LiveDataTestObserver<StopDetails?>()
        viewModel.stopDetailsLiveData.observeForever(observer)

        viewModel.stopCode = null

        observer.assertValues(null)
    }

    @Test
    fun stopDetailsLiveDataEmitsStopDetailsWithNullNameWhenRepositoryReturnsNullName() =
            coroutineRule.runBlockingTest {
        val viewModel = createViewModel()
        val observer = LiveDataTestObserver<StopDetails?>()
        viewModel.stopDetailsLiveData.observeForever(observer)
        whenever(busStopsRepository.getNameForStopFlow("123456"))
                .thenReturn(flowOf(null))

        viewModel.stopCode = "123456"

        observer.assertValues(
                null,
                StopDetails("123456", null))
    }

    @Test
    fun stopDetailsLiveDataEmitsStopDetailsWithNameWhenRepositoryReturnsName() =
            coroutineRule.runBlockingTest {
        val viewModel = createViewModel()
        val observer = LiveDataTestObserver<StopDetails?>()
        viewModel.stopDetailsLiveData.observeForever(observer)
        whenever(busStopsRepository.getNameForStopFlow("123456"))
                .thenReturn(flowOf(StopName("Name", "Locality")))

        viewModel.stopCode = "123456"

        observer.assertValues(
                null,
                StopDetails("123456", null),
                StopDetails("123456", StopName("Name", "Locality")))
    }

    @Test
    fun stopDetailsLiveDataEmitsCorrectDataOnChanges() = coroutineRule.runBlockingTest {
        val viewModel = createViewModel()
        val observer = LiveDataTestObserver<StopDetails?>()
        viewModel.stopDetailsLiveData.observeForever(observer)
        whenever(busStopsRepository.getNameForStopFlow("123456"))
                .thenReturn(flowOf(
                        StopName("Name", "Locality"),
                        StopName("Name 2", null)))
        whenever(busStopsRepository.getNameForStopFlow("987654"))
                .thenReturn(flowOf(StopName("Name 3", "Locality 3")))

        viewModel.stopCode = "123456"
        viewModel.stopCode = "987654"

        observer.assertValues(
                null,
                StopDetails("123456", null),
                StopDetails("123456", StopName("Name", "Locality")),
                StopDetails("123456", StopName("Name 2", null)),
                StopDetails("987654", null),
                StopDetails("987654", StopName("Name 3", "Locality 3")))
    }

    @Test
    fun uiStateLiveDataEmitsCalculatedState() = coroutineRule.runBlockingTest {
        whenever(uiStateCalculator.createUiStateFlow(any(), any(), any()))
                .thenReturn(flow {
                    emit(UiState.ERROR_NO_STOP_CODE)
                    delay(100L)
                    emit(UiState.ERROR_NO_SERVICES)
                    delay(100L)
                    emit(UiState.PROGRESS)
                    delay(100L)
                    emit(UiState.CONTENT)
                })
        val viewModel = createViewModel()
        val observer = LiveDataTestObserver<UiState>()
        viewModel.uiStateLiveData.observeForever(observer)
        advanceUntilIdle()

        observer.assertValues(
                UiState.ERROR_NO_STOP_CODE,
                UiState.ERROR_NO_SERVICES,
                UiState.PROGRESS,
                UiState.CONTENT)
    }

    @Test
    fun uiStateCalculatorIsPassedCorrectFlowObjects() = coroutineRule.runBlockingTest {
        val stopCodeFlow = FlowTestObserver<String?>(this)
        val stopDetailsFlow = FlowTestObserver<StopDetails?>(this)
        val availableServicesFlow = FlowTestObserver<List<String>?>(this)
        doAnswer {
            stopCodeFlow.observe(it.getArgument(0))
            stopDetailsFlow.observe(it.getArgument(1))
            availableServicesFlow.observe(it.getArgument(2))

            flowOf(UiState.CONTENT)
        }.whenever(uiStateCalculator).createUiStateFlow(any(), any(), any())
        whenever(busStopsRepository.getNameForStopFlow("123456"))
                .thenReturn(flowOf(StopName("Name 1", "Locality 1")))
        whenever(busStopsRepository.getNameForStopFlow("987654"))
                .thenReturn(flowOf(StopName("Name 2", "Locality 2")))
        whenever(serviceStopsRepository.getServicesForStopFlow("123456"))
                .thenReturn(flowOf(listOf("1", "2", "3")))
        whenever(serviceStopsRepository.getServicesForStopFlow("987654"))
                .thenReturn(flowOf(listOf("4", "5", "6")))
        val uiStateObserver = LiveDataTestObserver<UiState>()
        val viewModel = createViewModel()

        viewModel.uiStateLiveData.observeForever(uiStateObserver)
        viewModel.stopCode = "123456"
        viewModel.stopCode = "987654"
        stopCodeFlow.finish()
        stopDetailsFlow.finish()
        availableServicesFlow.finish()

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
    fun addButtonEnabledLiveDataEmitsFalseWhenSelectedServicesIsNull() =
            coroutineRule.runBlockingTest {
        val uiStateFlow = flow {
            emit(UiState.ERROR_NO_STOP_CODE)
            delay(100L)
            emit(UiState.ERROR_NO_SERVICES)
            delay(100L)
            emit(UiState.PROGRESS)
            delay(100L)
            emit(UiState.CONTENT)
        }
        whenever(uiStateCalculator.createUiStateFlow(any(), any(), any()))
                .thenReturn(uiStateFlow)
        val observer = LiveDataTestObserver<Boolean>()
        val viewModel = createViewModel()

        viewModel.addButtonEnabledLiveData.observeForever(observer)
        viewModel.selectedServices = null
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun addButtonEnabledLiveDataEmitsFalseWhenSelectedServicesIsEmpty() =
            coroutineRule.runBlockingTest {
        val uiStateFlow = flow {
            emit(UiState.ERROR_NO_STOP_CODE)
            delay(100L)
            emit(UiState.ERROR_NO_SERVICES)
            delay(100L)
            emit(UiState.PROGRESS)
            delay(100L)
            emit(UiState.CONTENT)
        }
        whenever(uiStateCalculator.createUiStateFlow(any(), any(), any()))
                .thenReturn(uiStateFlow)
        val observer = LiveDataTestObserver<Boolean>()
        val viewModel = createViewModel()

        viewModel.addButtonEnabledLiveData.observeForever(observer)
        viewModel.selectedServices = emptyList()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun addButtonEnabledLiveDataEmitsTrueWhenSelectedServicesPopulatedAndShowingContent() =
            coroutineRule.runBlockingTest {
        val uiStateFlow = flow {
            emit(UiState.ERROR_NO_STOP_CODE)
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
        whenever(uiStateCalculator.createUiStateFlow(any(), any(), any()))
                .thenReturn(uiStateFlow)
        val observer = LiveDataTestObserver<Boolean>()
        val viewModel = createViewModel()

        viewModel.addButtonEnabledLiveData.observeForever(observer)
        viewModel.selectedServices = listOf("1", "2", "3")
        advanceUntilIdle()

        observer.assertValues(false, true, false, true)
    }

    @Test
    fun onLimitationsButtonClickedShowsLimitations() {
        val observer = LiveDataTestObserver<Nothing?>()
        val viewModel = createViewModel()

        viewModel.showLimitationsLiveData.observeForever(observer)
        viewModel.onLimitationsButtonClicked()

        observer.assertValues(null)
    }

    @Test
    fun onSelectServicesClickedDoesNotShowServicesSelectionWhenAvailableServicesIsNull() =
            coroutineRule.runBlockingTest {
        val availableServicesFlow = FlowTestObserver<List<String>?>(this)
        doAnswer {
            availableServicesFlow.observe(it.getArgument(2))

            flowOf(UiState.CONTENT)
        }.whenever(uiStateCalculator).createUiStateFlow(any(), any(), any())
        whenever(serviceStopsRepository.getServicesForStopFlow("123456"))
                .thenReturn(flowOf(null))
        val observer = LiveDataTestObserver<ServicesChooserParams>()
        val viewModel = createViewModel()

        viewModel.showServicesChooserLiveData.observeForever(observer)
        viewModel.stopCode = "123456"
        viewModel.onSelectServicesClicked()
        availableServicesFlow.finish()

        observer.assertEmpty()
    }

    @Test
    fun onSelectServicesClickedDoesNotShowServicesSelectionWhenAvailableServicesIsEmpty() =
            coroutineRule.runBlockingTest {
        val availableServicesFlow = FlowTestObserver<List<String>?>(this)
        doAnswer {
            availableServicesFlow.observe(it.getArgument(2))

            flowOf(UiState.CONTENT)
        }.whenever(uiStateCalculator).createUiStateFlow(any(), any(), any())
        whenever(serviceStopsRepository.getServicesForStopFlow("123456"))
                .thenReturn(flowOf(emptyList()))
        val observer = LiveDataTestObserver<ServicesChooserParams>()
        val viewModel = createViewModel()

        viewModel.showServicesChooserLiveData.observeForever(observer)
        viewModel.stopCode = "123456"
        viewModel.onSelectServicesClicked()
        availableServicesFlow.finish()

        observer.assertEmpty()
    }

    @Test
    fun onSelectServicesClickedShowsServicesSelectionWithNullSelectedServicesFirstTime() =
            coroutineRule.runBlockingTest {
        val availableServicesFlow = FlowTestObserver<List<String>?>(this)
        doAnswer {
            availableServicesFlow.observe(it.getArgument(2))

            flowOf(UiState.CONTENT)
        }.whenever(uiStateCalculator).createUiStateFlow(any(), any(), any())
        whenever(serviceStopsRepository.getServicesForStopFlow("123456"))
                .thenReturn(flowOf(listOf("1", "2", "3")))
        val observer = LiveDataTestObserver<ServicesChooserParams>()
        val viewModel = createViewModel()

        viewModel.showServicesChooserLiveData.observeForever(observer)
        viewModel.stopCode = "123456"
        viewModel.onSelectServicesClicked()
        availableServicesFlow.finish()

        observer.assertValues(
                ServicesChooserParams(listOf("1", "2", "3"), null))
    }

    @Test
    fun onSelectServicesClickedShowsServicesSelectionWithInitialStateSelectedServices() =
            coroutineRule.runBlockingTest {
        val availableServicesFlow = FlowTestObserver<List<String>?>(this)
        doAnswer {
            availableServicesFlow.observe(it.getArgument(2))

            flowOf(UiState.CONTENT)
        }.whenever(uiStateCalculator).createUiStateFlow(any(), any(), any())
        whenever(serviceStopsRepository.getServicesForStopFlow("123456"))
                .thenReturn(flowOf(listOf("1", "2", "3")))
        val chooserObserver = LiveDataTestObserver<ServicesChooserParams>()
        val selectedServicesObserver = LiveDataTestObserver<List<String>?>()
        val initialState = mapOf(
                AddTimeAlertDialogFragmentViewModel.STATE_SELECTED_SERVICES to arrayOf("2", "3"))
        val savedState = SavedStateHandle(initialState)
        val viewModel = createViewModel(savedState)

        viewModel.showServicesChooserLiveData.observeForever(chooserObserver)
        viewModel.selectedServicesLiveData.observeForever(selectedServicesObserver)
        viewModel.stopCode = "123456"
        viewModel.onSelectServicesClicked()
        availableServicesFlow.finish()

        chooserObserver.assertValues(
                ServicesChooserParams(listOf("1", "2", "3"), listOf("2", "3")))
    }

    @Test
    fun onSelectServicesClickedShowsServicesSelectionWithSelectedServicesSet() =
            coroutineRule.runBlockingTest {
        val availableServicesFlow = FlowTestObserver<List<String>?>(this)
        doAnswer {
            availableServicesFlow.observe(it.getArgument(2))

            flowOf(UiState.CONTENT)
        }.whenever(uiStateCalculator).createUiStateFlow(any(), any(), any())
        whenever(serviceStopsRepository.getServicesForStopFlow("123456"))
                .thenReturn(flowOf(listOf("1", "2", "3")))
        val chooserObserver = LiveDataTestObserver<ServicesChooserParams>()
        val selectedServicesObserver = LiveDataTestObserver<List<String>?>()
        val viewModel = createViewModel()

        viewModel.showServicesChooserLiveData.observeForever(chooserObserver)
        viewModel.selectedServicesLiveData.observeForever(selectedServicesObserver)
        viewModel.stopCode = "123456"
        viewModel.selectedServices = listOf("2", "3")
        viewModel.onSelectServicesClicked()
        availableServicesFlow.finish()

        chooserObserver.assertValues(
                ServicesChooserParams(listOf("1", "2", "3"), listOf("2", "3")))
    }

    @Test
    fun onSelectServicesClickedShowsServicesSelectionWithRepresentativeExample() =
            coroutineRule.runBlockingTest {
        val availableServicesFlow = FlowTestObserver<List<String>?>(this)
        doAnswer {
            availableServicesFlow.observe(it.getArgument(2))

            flowOf(UiState.CONTENT)
        }.whenever(uiStateCalculator).createUiStateFlow(any(), any(), any())
        whenever(serviceStopsRepository.getServicesForStopFlow("123456"))
                .thenReturn(flowOf(listOf("1", "2", "3")))
        val chooserObserver = LiveDataTestObserver<ServicesChooserParams>()
        val selectedServicesObserver = LiveDataTestObserver<List<String>?>()
        val initialState = mapOf(
                AddTimeAlertDialogFragmentViewModel.STATE_SELECTED_SERVICES to arrayOf("2", "3"))
        val savedState = SavedStateHandle(initialState)
        val viewModel = createViewModel(savedState)

        viewModel.showServicesChooserLiveData.observeForever(chooserObserver)
        viewModel.selectedServicesLiveData.observeForever(selectedServicesObserver)
        viewModel.stopCode = "123456"
        viewModel.onSelectServicesClicked()
        viewModel.selectedServices = listOf("2")
        viewModel.onSelectServicesClicked()
        availableServicesFlow.finish()

        chooserObserver.assertValues(
                ServicesChooserParams(listOf("1", "2", "3"), listOf("2", "3")),
                ServicesChooserParams(listOf("1", "2", "3"), listOf("2")))
    }

    @Test
    fun onAddClickedDoesNotAddAlertWhenStopCodeIsNull() = coroutineRule.runBlockingTest {
        val viewModel = createViewModel()
        viewModel.stopCode = null
        viewModel.selectedServices = listOf("1", "2", "3")
        viewModel.selectedServicesLiveData.observeForever(LiveDataTestObserver())

        viewModel.onAddClicked(10)

        verify(alertsRepository, never())
                .addArrivalAlert(any())
    }

    @Test
    fun onAddClickedDoesNotAddAlertWhenStopCodeIsEmpty() = coroutineRule.runBlockingTest {
        val viewModel = createViewModel()
        viewModel.stopCode = ""
        viewModel.selectedServices = listOf("1", "2", "3")
        viewModel.selectedServicesLiveData.observeForever(LiveDataTestObserver())

        viewModel.onAddClicked(10)

        verify(alertsRepository, never())
                .addArrivalAlert(any())
    }

    @Test
    fun onAddClickedDoesNotAddAlertWhenSelectedServicesIsNull() = coroutineRule.runBlockingTest {
        val viewModel = createViewModel()
        viewModel.stopCode = "123456"
        viewModel.selectedServices = null
        viewModel.selectedServicesLiveData.observeForever(LiveDataTestObserver())

        viewModel.onAddClicked(10)

        verify(alertsRepository, never())
                .addArrivalAlert(any())
    }

    @Test
    fun onAddClickedDoesNotAddAlertWhenSelectedServicesIsEmpty() = coroutineRule.runBlockingTest {
        val viewModel = createViewModel()
        viewModel.stopCode = "123456"
        viewModel.selectedServices = listOf()
        viewModel.selectedServicesLiveData.observeForever(LiveDataTestObserver())

        viewModel.onAddClicked(10)

        verify(alertsRepository, never())
                .addArrivalAlert(any())
    }

    @Test
    fun onAddClickedAddsAlertsWhenConditionsAreSatisfied() = coroutineRule.runBlockingTest {
        val viewModel = createViewModel()
        viewModel.stopCode = "123456"
        viewModel.selectedServices = listOf("1", "2", "3")
        val expected = ArrivalAlertRequest("123456", listOf("1", "2", "3"), 10)
        viewModel.selectedServicesLiveData.observeForever(LiveDataTestObserver())

        viewModel.onAddClicked(10)

        verify(alertsRepository)
                .addArrivalAlert(expected)
    }

    private fun createViewModel(savedState: SavedStateHandle = SavedStateHandle()) =
            AddTimeAlertDialogFragmentViewModel(
                    savedState,
                    busStopsRepository,
                    serviceStopsRepository,
                    uiStateCalculator,
                    alertsRepository,
                    coroutineRule,
                    coroutineRule.testDispatcher)
}