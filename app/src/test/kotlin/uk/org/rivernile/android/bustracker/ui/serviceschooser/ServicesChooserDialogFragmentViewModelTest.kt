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

package uk.org.rivernile.android.bustracker.ui.serviceschooser

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import uk.org.rivernile.android.bustracker.testutils.test

/**
 * Tests for [ServicesChooserDialogFragmentViewModel].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class ServicesChooserDialogFragmentViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var arguments: Arguments
    @Mock
    private lateinit var state: State
    @Mock
    private lateinit var servicesLoader: ServicesLoader

    @Test
    fun servicesLiveDataEmitsValuesUpstreamFromServicesLoader() = runTest {
        val service1 = mock<UiService>()
        val service2 = mock<UiService>()
        val service3 = mock<UiService>()
        val flow = intervalFlowOf(
            0L,
            10L,
            emptyList<UiService>(),
            listOf(service1, service2, service3),
            listOf(service1, service2, service3),
            listOf(service1))
        whenever(servicesLoader.servicesFlow)
            .thenReturn(flow)
        whenever(state.hasSelectedServicesFlow)
            .thenReturn(flowOf(false))
        val viewModel = createViewModel()

        val observer = viewModel.servicesLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
            null,
            emptyList(),
            listOf(service1, service2, service3),
            listOf(service1))
    }

    @Test
    fun uiStateLiveDataEmitsProgressThenContentWhenServicesIsPopulated() = runTest {
        whenever(arguments.paramsFlow)
            .thenReturn(flowOf(null))
        whenever(servicesLoader.servicesFlow)
            .thenReturn(intervalFlowOf(10L, 10L, listOf(mock())))
        whenever(state.hasSelectedServicesFlow)
            .thenReturn(flowOf(false))
        val viewModel = createViewModel()

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
            UiState.PROGRESS,
            UiState.CONTENT)
    }

    @Test
    fun uiStateLiveDataEmitsProgressThenErrorWhenServicesNotPopulated() = runTest {
        whenever(arguments.paramsFlow)
            .thenReturn(flowOf(null))
        whenever(servicesLoader.servicesFlow)
            .thenReturn(intervalFlowOf(10L, 10L, emptyList()))
        whenever(state.hasSelectedServicesFlow)
            .thenReturn(flowOf(false))
        val viewModel = createViewModel()

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
            UiState.PROGRESS,
            UiState.ERROR_NO_SERVICES_GLOBAL)
    }

    @Test
    fun uiStateLiveDataEmitsGlobalErrorWhenServicesEMptyAndParamsAllServices() = runTest {
        whenever(arguments.paramsFlow)
            .thenReturn(flowOf(ServicesChooserParams.AllServices(0, null)))
        whenever(servicesLoader.servicesFlow)
            .thenReturn(intervalFlowOf(10L, 10L, emptyList()))
        whenever(state.hasSelectedServicesFlow)
            .thenReturn(flowOf(false))
        val viewModel = createViewModel()

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
            UiState.PROGRESS,
            UiState.ERROR_NO_SERVICES_GLOBAL)
    }

    @Test
    fun uiStateLiveDataEmitsGlobalErrorWhenServicesEMptyAndParamsStop() = runTest {
        whenever(arguments.paramsFlow)
            .thenReturn(flowOf(ServicesChooserParams.Stop(0, null, "123456")))
        whenever(servicesLoader.servicesFlow)
            .thenReturn(intervalFlowOf(10L, 10L, emptyList()))
        whenever(state.hasSelectedServicesFlow)
            .thenReturn(flowOf(false))
        val viewModel = createViewModel()

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
            UiState.PROGRESS,
            UiState.ERROR_NO_SERVICES_STOP)
    }

    @Test
    fun uiStateLiveDataEmitsValues() = runTest {
        whenever(arguments.paramsFlow)
            .thenReturn(flowOf(ServicesChooserParams.Stop(0, null, "123456")))
        whenever(servicesLoader.servicesFlow)
            .thenReturn(intervalFlowOf(
                10L,
                10L,
                emptyList(),
                emptyList(),
                listOf(mock()),
                emptyList()))
        whenever(state.hasSelectedServicesFlow)
            .thenReturn(flowOf(false))
        val viewModel = createViewModel()

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
            UiState.PROGRESS,
            UiState.ERROR_NO_SERVICES_STOP,
            UiState.CONTENT,
            UiState.ERROR_NO_SERVICES_STOP)
    }

    @Test
    fun isClearAllButtonEnabledLiveDataEmitsValuesFromState() = runTest {
        whenever(servicesLoader.servicesFlow)
            .thenReturn(flowOf(emptyList()))
        whenever(state.hasSelectedServicesFlow)
            .thenReturn(intervalFlowOf(0L, 10L, false, true, false))
        val viewModel = createViewModel()

        val observer = viewModel.isClearAllButtonEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false, true, false)
    }

    @Test
    fun selectedServicesReturnsValueFromState() {
        whenever(servicesLoader.servicesFlow)
            .thenReturn(flowOf(emptyList()))
        whenever(state.hasSelectedServicesFlow)
            .thenReturn(flowOf(false))
        val viewModel = createViewModel()
        val expected = mock<ArrayList<String>?>()
        whenever(state.selectedServices)
            .thenReturn(expected)

        val result = viewModel.selectedServices

        assertSame(expected, result)
    }

    @Test
    fun onServiceClickedPassesThroughToState() {
        whenever(servicesLoader.servicesFlow)
            .thenReturn(flowOf(emptyList()))
        whenever(state.hasSelectedServicesFlow)
            .thenReturn(flowOf(false))
        val viewModel = createViewModel()

        viewModel.onServiceClicked("1")

        verify(state)
            .onServiceClicked("1")
    }

    @Test
    fun onClearAllClickedPassesThroughToState() {
        whenever(servicesLoader.servicesFlow)
            .thenReturn(flowOf(emptyList()))
        whenever(state.hasSelectedServicesFlow)
            .thenReturn(flowOf(false))
        val viewModel = createViewModel()

        viewModel.onClearAllClicked()

        verify(state)
            .onClearAllClicked()
    }

    private fun createViewModel() =
        ServicesChooserDialogFragmentViewModel(
            arguments,
            state,
            servicesLoader,
            coroutineRule.testDispatcher)
}