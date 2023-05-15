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

package uk.org.rivernile.android.bustracker.ui.alerts

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.testutils.test

/**
 * Tests for [AlertManagerFragmentViewModel].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class AlertManagerFragmentViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var alertsRetriever: AlertsRetriever

    private val viewModel: AlertManagerFragmentViewModel by lazy {
        // This is lazily initialised so we can mock the behaviour of dependencies before
        // instantiation.
        AlertManagerFragmentViewModel(
                alertsRetriever,
                coroutineRule.testDispatcher)
    }

    @Test
    fun alertsLiveDataEmitsNullWhenRetrieverEmitsNull() = runTest {
        whenever(alertsRetriever.allAlertsFlow)
                .thenReturn(flowOf(null))

        val observer = viewModel.alertsLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun alertsLiveDataEmitsEmptyListWhenRetrieverEmitsEmptyList() = runTest {
        whenever(alertsRetriever.allAlertsFlow)
                .thenReturn(flowOf(emptyList()))

        val observer = viewModel.alertsLiveData.test()
        advanceUntilIdle()

        observer.assertValues(emptyList())
    }

    @Test
    fun alertsLiveDataEmitsPopulatedListWhenRetrieverEmitsPopulatedList() = runTest {
        val list = listOf(
                mock<UiAlert.ArrivalAlert>(),
                mock<UiAlert.ProximityAlert>())
        whenever(alertsRetriever.allAlertsFlow)
                .thenReturn(flowOf(list))

        val observer = viewModel.alertsLiveData.test()
        advanceUntilIdle()

        observer.assertValues(list)
    }

    @Test
    fun alertsLiveDataEmitsRepresentativeFlowCorrectly() = runTest {
        val list1 = listOf(
                mock<UiAlert.ArrivalAlert>(),
                mock<UiAlert.ProximityAlert>())
        val list2 = listOf(
                mock<UiAlert.ProximityAlert>())
        whenever(alertsRetriever.allAlertsFlow)
                .thenReturn(flowOf(
                    null,
                    emptyList(),
                    list1,
                    list2))

        val observer = viewModel.alertsLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null, emptyList(), list1, list2)
    }

    @Test
    fun uiStateLiveDataEmitsInProgressWhenFlowEmitsNull() = runTest {
        whenever(alertsRetriever.allAlertsFlow)
                .thenReturn(flowOf(null))

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(UiState.PROGRESS)
    }

    @Test
    fun uiStateLiveDataEmitsErrorWhenFlowEmitsEmptyList() = runTest {
        whenever(alertsRetriever.allAlertsFlow)
                .thenReturn(flowOf(emptyList()))

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(UiState.ERROR)
    }

    @Test
    fun uiStateLiveDataEmitsContentWhenFlowEmitsPopulatedList() = runTest {
        whenever(alertsRetriever.allAlertsFlow)
            .thenReturn(flowOf(listOf(mock<UiAlert.ArrivalAlert>())))

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(UiState.CONTENT)
    }

    @Test
    fun uiStateLiveDataEmitsExpectedValuesWithRepresentativeValues() = runTest {
        whenever(alertsRetriever.allAlertsFlow)
                .thenReturn(flowOf(
                        null,
                        emptyList(),
                        listOf(mock<UiAlert.ArrivalAlert>()),
                        emptyList(),
                        listOf(mock<UiAlert.ArrivalAlert>(), mock<UiAlert.ProximityAlert>())))

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
                UiState.PROGRESS,
                UiState.ERROR,
                UiState.CONTENT,
                UiState.ERROR,
                UiState.CONTENT)
    }

    @Test
    fun showLocationSettingsLiveDataEmitsNoValuesByDefault() {
        val observer = viewModel.showLocationSettingsLiveData.test()

        observer.assertEmpty()
    }

    @Test
    fun showLocationSettingsLiveDataEmitsEventWhenShowLocationSettingsClickedIsCalled() {
        val observer = viewModel.showLocationSettingsLiveData.test()
        viewModel.onShowLocationSettingsClicked()

        observer.assertSize(1)
    }

    @Test
    fun showRemoveArrivalAlertLiveDataEmitsNoValuesByDefault() {
        val observer = viewModel.showRemoveArrivalAlertLiveData.test()

        observer.assertEmpty()
    }

    @Test
    fun showRemoveArrivalAlertLiveDataEmitsEventWhenRemoveArrivalAlertClickedIsCalled() {
        val observer = viewModel.showRemoveArrivalAlertLiveData.test()
        viewModel.onRemoveArrivalAlertClicked("123456")

        observer.assertValues("123456")
    }

    @Test
    fun showRemoveProximityAlertLiveDataEmitsNoValuesByDefault() {
        val observer = viewModel.showRemoveProximityAlertLiveData.test()

        observer.assertEmpty()
    }

    @Test
    fun showRemoveProximityAlertLiveDataEmitsEventWhenRemoveArrivalAlertClickedIsCalled() {
        val observer = viewModel.showRemoveProximityAlertLiveData.test()
        viewModel.onRemoveProximityAlertClicked("123456")

        observer.assertValues("123456")
    }
}