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

package uk.org.rivernile.android.bustracker.ui.alerts

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.testutils.LiveDataTestObserver

/**
 * Tests for [AlertManagerFragmentViewModel].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AlertManagerFragmentViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var alertsRetriever: AlertsRetriever

    private val alertsObserver by lazy { LiveDataTestObserver<List<UiAlert>?>() }
    private val uiStateObserver by lazy { LiveDataTestObserver<UiState>() }
    private val showLocationSettingsObserver by lazy { LiveDataTestObserver<Nothing?>() }
    private val showRemoveArrivalAlertObserver by lazy { LiveDataTestObserver<String>() }
    private val showRemoveProximityAlertObserver by lazy { LiveDataTestObserver<String>() }

    private val viewModel: AlertManagerFragmentViewModel by lazy {
        // This is lazily initialised so we can mock the behaviour of dependencies before
        // instantiation.
        AlertManagerFragmentViewModel(
                alertsRetriever,
                coroutineRule.testDispatcher)
    }

    @Test
    fun alertsLiveDataEmitsNullWhenRetrieverEmitsNull() {
        whenever(alertsRetriever.allAlertsFlow)
                .thenReturn(flowOf(null))

        viewModel.alertsLiveData.observeForever(alertsObserver)

        alertsObserver.assertValues(null)
    }

    @Test
    fun alertsLiveDataEmitsEmptyListWhenRetrieverEmitsEmptyList() {
        whenever(alertsRetriever.allAlertsFlow)
                .thenReturn(flowOf(emptyList()))

        viewModel.alertsLiveData.observeForever(alertsObserver)

        alertsObserver.assertValues(emptyList())
    }

    @Test
    fun alertsLiveDataEmitsPopulatedListWhenRetrieverEmitsPopulatedList() {
        val list = listOf(
                mock<UiAlert.ArrivalAlert>(),
                mock<UiAlert.ProximityAlert>())
        whenever(alertsRetriever.allAlertsFlow)
                .thenReturn(flowOf(list))

        viewModel.alertsLiveData.observeForever(alertsObserver)

        alertsObserver.assertValues(list)
    }

    @Test
    fun alertsLiveDataEmitsRepresentativeFlowCorrectly() = coroutineRule.runBlockingTest {
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

        viewModel.alertsLiveData.observeForever(alertsObserver)
        advanceUntilIdle()

        alertsObserver.assertValues(null, emptyList(), list1, list2)
    }

    @Test
    fun uiStateLiveDataEmitsInProgressWhenFlowEmitsNull() {
        whenever(alertsRetriever.allAlertsFlow)
                .thenReturn(flowOf(null))

        viewModel.uiStateLiveData.observeForever(uiStateObserver)

        uiStateObserver.assertValues(UiState.PROGRESS)
    }

    @Test
    fun uiStateLiveDataEmitsErrorWhenFlowEmitsEmptyList() {
        whenever(alertsRetriever.allAlertsFlow)
                .thenReturn(flowOf(emptyList()))

        viewModel.uiStateLiveData.observeForever(uiStateObserver)

        uiStateObserver.assertValues(UiState.ERROR)
    }

    @Test
    fun uiStateLiveDataEmitsContentWhenFlowEmitsPopulatedList() {
        whenever(alertsRetriever.allAlertsFlow)
                .thenReturn(flowOf(listOf(mock())))

        viewModel.uiStateLiveData.observeForever(uiStateObserver)

        uiStateObserver.assertValues(UiState.CONTENT)
    }

    @Test
    fun uiStateLiveDataEmitsExpectedValuesWithRepresentativeValues() =
            coroutineRule.runBlockingTest {
        whenever(alertsRetriever.allAlertsFlow)
                .thenReturn(flowOf(
                        null,
                        emptyList(),
                        listOf(mock()),
                        emptyList(),
                        listOf(mock(), mock())))

        viewModel.uiStateLiveData.observeForever(uiStateObserver)
        advanceUntilIdle()

        uiStateObserver.assertValues(
                UiState.PROGRESS,
                UiState.ERROR,
                UiState.CONTENT,
                UiState.ERROR,
                UiState.CONTENT)
    }

    @Test
    fun showLocationSettingsLiveDataEmitsNoValuesByDefault() {
        viewModel.showLocationSettingsLiveData.observeForever(showLocationSettingsObserver)

        showLocationSettingsObserver.assertEmpty()
    }

    @Test
    fun showLocationSettingsLiveDataEmitsEventWhenShowLocationSettingsClickedIsCalled() {
        viewModel.showLocationSettingsLiveData.observeForever(showLocationSettingsObserver)
        viewModel.onShowLocationSettingsClicked()

        showLocationSettingsObserver.assertValues(null)
    }

    @Test
    fun showRemoveArrivalAlertLiveDataEmitsNoValuesByDefault() {
        viewModel.showRemoveArrivalAlertLiveData.observeForever(showRemoveArrivalAlertObserver)

        showRemoveArrivalAlertObserver.assertEmpty()
    }

    @Test
    fun showRemoveArrivalAlertLiveDataEmitsEventWhenRemoveArrivalAlertClickedIsCalled() {
        viewModel.showRemoveArrivalAlertLiveData.observeForever(showRemoveArrivalAlertObserver)
        viewModel.onRemoveArrivalAlertClicked("123456")

        showRemoveArrivalAlertObserver.assertValues("123456")
    }

    @Test
    fun showRemoveProximityAlertLiveDataEmitsNoValuesByDefault() {
        viewModel.showRemoveProximityAlertLiveData.observeForever(showRemoveProximityAlertObserver)

        showRemoveProximityAlertObserver.assertEmpty()
    }

    @Test
    fun showRemoveProximityAlertLiveDataEmitsEventWhenRemoveArrivalAlertClickedIsCalled() {
        viewModel.showRemoveProximityAlertLiveData.observeForever(showRemoveProximityAlertObserver)
        viewModel.onRemoveProximityAlertClicked("123456")

        showRemoveProximityAlertObserver.assertValues("123456")
    }
}