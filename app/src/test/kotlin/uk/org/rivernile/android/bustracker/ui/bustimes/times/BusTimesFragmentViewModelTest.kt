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

package uk.org.rivernile.android.bustracker.ui.bustimes.times

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityRepository
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceRepository
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import uk.org.rivernile.android.bustracker.testutils.test

/**
 * Tests for [BusTimesFragmentViewModel].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class BusTimesFragmentViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var arguments: Arguments
    @Mock
    private lateinit var expandedServicesTracker: ExpandedServicesTracker
    @Mock
    private lateinit var liveTimesLoader: LiveTimesLoader
    @Mock
    private lateinit var lastRefreshTimeCalculator: LastRefreshTimeCalculator
    @Mock
    private lateinit var refreshController: RefreshController
    @Mock
    private lateinit var preferenceRepository: PreferenceRepository
    @Mock
    private lateinit var connectivityRepository: ConnectivityRepository

    @Test
    fun hasConnectivityLiveDataEmitsFromFlow() = runTest {
        whenever(connectivityRepository.hasInternetConnectivityFlow)
                .thenReturn(intervalFlowOf(0L, 10L, true, false, true))
        val viewModel = createViewModel(stopCode = null)

        val observer = viewModel.hasConnectivityLiveData.test()
        advanceUntilIdle()

        observer.assertValues(true, false, true)
    }

    @Test
    fun hasConnectivityLiveDataOnlyEmitsDistinctValues() = runTest {
        whenever(connectivityRepository.hasInternetConnectivityFlow)
                .thenReturn(intervalFlowOf(0L, 10L, true, true, false, false, true))
        val viewModel = createViewModel(stopCode = null)

        val observer = viewModel.hasConnectivityLiveData.test()
        advanceUntilIdle()

        observer.assertValues(true, false, true)
    }

    @Test
    fun isSortedByTimeEnabledEmitsFalseWhenStopCodeIsNull() = runTest {
        val viewModel = createViewModel(stopCode = null)

        val observer = viewModel.isSortedByTimeEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isSortedByTimeEnabledEmitsFalseWhenStopCodeIsEmpty() = runTest {
        val viewModel = createViewModel(stopCode = "")

        val observer = viewModel.isSortedByTimeEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isSortedByTimeEnabledEmitsTrueWhenStopCodeIsNotNull() = runTest {
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.isSortedByTimeEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(true)
    }

    @Test
    fun isSortedByTimeEmitsValuesFromRepository() = runTest {
        whenever(preferenceRepository.isLiveTimesSortByTimeFlow)
            .thenReturn(flowOf(true, false, true))
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.isSortedByTimeLiveData.test()
        advanceUntilIdle()

        observer.assertValues(true, false, true)
    }

    @Test
    fun isSortedByTimeEmitsDistinctValuesFromRepository() = runTest {
        whenever(preferenceRepository.isLiveTimesSortByTimeFlow)
            .thenReturn(intervalFlowOf(0L, 10L, true, true, false, false, true))
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.isSortedByTimeLiveData.test()
        advanceUntilIdle()

        observer.assertValues(true, false, true)
    }

    @Test
    fun isAutoRefreshEnabledEmitsFalseWhenStopCodeIsNull() = runTest {
        val viewModel = createViewModel(stopCode = null)

        val observer = viewModel.isAutoRefreshEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isAutoRefreshEnabledEmitsFalseWhenStopCodeIsEmpty() = runTest {
        val viewModel = createViewModel(stopCode = "")

        val observer = viewModel.isAutoRefreshEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isAutoRefreshEnabledEmitsTrueWhenStopCodeIsNotNull() = runTest {
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.isAutoRefreshEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(true)
    }

    @Test
    fun isAutoRefreshEmitsValuesFromRepository() = runTest {
        whenever(preferenceRepository.isLiveTimesAutoRefreshEnabledFlow)
            .thenReturn(flowOf(true, false, true))
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.isAutoRefreshLiveData.test()
        advanceUntilIdle()

        observer.assertValues(true, false, true)
        inOrder(refreshController) {
            verify(refreshController)
                    .onAutoRefreshPreferenceChanged(null, true)
            verify(refreshController)
                    .onAutoRefreshPreferenceChanged(null, false)
            verify(refreshController)
                    .onAutoRefreshPreferenceChanged(null, true)
        }
    }

    @Test
    fun isAutoRefreshEmitsDistinctValuesFromRepository() = runTest {
        whenever(preferenceRepository.isLiveTimesAutoRefreshEnabledFlow)
            .thenReturn(flowOf(true, true, false, false, true))
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.isAutoRefreshLiveData.test()
        advanceUntilIdle()

        observer.assertValues(true, false, true)
        inOrder(refreshController) {
            verify(refreshController)
                    .onAutoRefreshPreferenceChanged(null, true)
            verify(refreshController)
                    .onAutoRefreshPreferenceChanged(null, false)
            verify(refreshController)
                    .onAutoRefreshPreferenceChanged(null, true)
        }
    }

    @Test
    fun isAutoRefreshPassesCurrentResultToRefreshControllerWhenAutoRefreshIsFalse() = runTest {
        val loadResult = UiTransformedResult.Success(123L, emptyList())
        whenever(liveTimesLoader.liveTimesFlow)
            .thenReturn(flowOf(loadResult))
        whenever(preferenceRepository.isLiveTimesAutoRefreshEnabledFlow)
            .thenReturn(flowOf(false))
        val viewModel = createViewModel(stopCode = "123456")

        viewModel.liveTimesLiveData.test()
        advanceUntilIdle()
        viewModel.isAutoRefreshLiveData.test()
        advanceUntilIdle()

        verify(refreshController)
            .onAutoRefreshPreferenceChanged(loadResult, false)
    }

    @Test
    fun isAutoRefreshPassesCurrentResultToRefreshControllerWhenAutoRefreshIsTrue() = runTest {
        val loadResult = UiTransformedResult.Success(123L, emptyList())
        whenever(liveTimesLoader.liveTimesFlow)
            .thenReturn(flowOf(loadResult))
        whenever(preferenceRepository.isLiveTimesAutoRefreshEnabledFlow)
            .thenReturn(flowOf(true))
        val viewModel = createViewModel(stopCode = "123456")

        viewModel.liveTimesLiveData.test()
        advanceUntilIdle()
        viewModel.isAutoRefreshLiveData.test()
        advanceUntilIdle()

        verify(refreshController)
            .onAutoRefreshPreferenceChanged(loadResult, true)
    }

    @Test
    fun showProgressLiveDataEmitsNullWhenStopCodeIsNull() = runTest {
        val viewModel = createViewModel(stopCode = null)

        val observer = viewModel.showProgressLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun showProgressLiveDataEmitsFalseWhenResultIsError() = runTest {
        whenever(liveTimesLoader.liveTimesFlow)
                .thenReturn(flowOf(UiTransformedResult.Error(123L, ErrorType.SERVER_ERROR)))
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.showProgressLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun showProgressLiveDataEmitsFalseWhenResultIsSuccess() = runTest {
        whenever(liveTimesLoader.liveTimesFlow)
                .thenReturn(flowOf(UiTransformedResult.Success(123L, emptyList())))
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.showProgressLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun showProgressLiveDataEmitsTrueWhenResultIsInProgress() = runTest {
        whenever(liveTimesLoader.liveTimesFlow)
                .thenReturn(flowOf(UiTransformedResult.InProgress))
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.showProgressLiveData.test()
        advanceUntilIdle()

        observer.assertValues(true)
    }

    @Test
    fun showProgressLiveDataEmitsCorrectValues() = runTest {
        val values = flowOf(
                UiTransformedResult.InProgress,
                UiTransformedResult.Error(123L, ErrorType.SERVER_ERROR),
                UiTransformedResult.InProgress,
                UiTransformedResult.Success(123L, emptyList()))
        whenever(liveTimesLoader.liveTimesFlow)
                .thenReturn(values)
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.showProgressLiveData.test()
        advanceUntilIdle()

        observer.assertValues(true, false, true, false)
    }

    @Test
    fun errorLiveDataEmitsNothingWhenIsInProgress() = runTest {
        val flow = flowOf(UiTransformedResult.InProgress)
        whenever(liveTimesLoader.liveTimesFlow)
                .thenReturn(flow)
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.errorLiveData.test()
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun errorLiveDataEmitsNullWhenIsSuccessWithData() = runTest {
        val flow = flowOf(
                UiTransformedResult.Success(
                        123L,
                        listOf(mock())))
        whenever(liveTimesLoader.liveTimesFlow)
                .thenReturn(flow)
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.errorLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun errorLiveDataEmitsErrorWhenIsSuccessWithNoData() = runTest {
        val flow = flowOf(UiTransformedResult.Success(123L, emptyList()))
        whenever(liveTimesLoader.liveTimesFlow)
                .thenReturn(flow)
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.errorLiveData.test()
        advanceUntilIdle()

        observer.assertValues(ErrorType.NO_DATA)
    }

    @Test
    fun errorLiveDataEmitsErrorWhenError() = runTest {
        val flow = flowOf(UiTransformedResult.Error(123L, ErrorType.SERVER_ERROR))
        whenever(liveTimesLoader.liveTimesFlow)
                .thenReturn(flow)
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.errorLiveData.test()
        advanceUntilIdle()

        observer.assertValues(ErrorType.SERVER_ERROR)
    }

    @Test
    fun errorLiveDataEmitsCorrectValues() = runTest {
        val flow = flowOf(
                UiTransformedResult.InProgress,
                UiTransformedResult.Error(123L, ErrorType.SERVER_ERROR),
                UiTransformedResult.InProgress,
                UiTransformedResult.Success(123L, emptyList()),
                UiTransformedResult.InProgress,
                UiTransformedResult.Success(123L, listOf(mock()))
        )
        whenever(liveTimesLoader.liveTimesFlow)
                .thenReturn(flow)
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.errorLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
                ErrorType.SERVER_ERROR,
                ErrorType.NO_DATA,
                null)
    }

    @Test
    fun liveTimesLiveDataEmitsNullWhenSuccessIsEmpty() = runTest {
        val flow = flowOf(UiTransformedResult.Success(123L, emptyList()))
        whenever(liveTimesLoader.liveTimesFlow)
                .thenReturn(flow)
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.liveTimesLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun liveTimesLiveDataEmitsValuesWhenSuccessHasValues() = runTest {
        val values = listOf<UiLiveTimesItem>(mock())
        val flow = flowOf(UiTransformedResult.Success(123L, values))
        whenever(liveTimesLoader.liveTimesFlow)
                .thenReturn(flow)
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.liveTimesLiveData.test()
        advanceUntilIdle()

        observer.assertValues(values)
    }

    @Test
    fun liveTimesLiveDataEmitsCorrectValues() = runTest {
        val values = listOf<UiLiveTimesItem>(mock())
        val flow = flowOf(
                UiTransformedResult.Success(123L, emptyList()),
                UiTransformedResult.Success(123L, values))
        whenever(liveTimesLoader.liveTimesFlow)
                .thenReturn(flow)
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.liveTimesLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null, values)
    }

    @Test
    fun uiStateLiveDataEmitsInitialProgress() = runTest {
        val flow = flowOf(UiTransformedResult.InProgress)
        whenever(liveTimesLoader.liveTimesFlow)
                .thenReturn(flow)
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(UiState.PROGRESS)
    }

    @Test
    fun uiStateLiveDataTransitionsToErrorWhenError() = runTest {
        val flow = flowOf(
            UiTransformedResult.InProgress,
            UiTransformedResult.Error(123L, ErrorType.SERVER_ERROR))
        whenever(liveTimesLoader.liveTimesFlow)
                .thenReturn(flow)
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
                UiState.PROGRESS,
                UiState.ERROR)
    }

    @Test
    fun uiStateLiveDataTransitionsToContentOnSuccess() = runTest {
        val flow = flowOf(
                UiTransformedResult.InProgress,
                UiTransformedResult.Success(123L, listOf(mock())))
        whenever(liveTimesLoader.liveTimesFlow)
                .thenReturn(flow)
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
                UiState.PROGRESS,
                UiState.CONTENT)
    }

    @Test
    fun uiStateLiveDataStaysOnContentWhenTransitionToProgress() = runTest {
        val flow = flowOf(
                UiTransformedResult.InProgress,
                UiTransformedResult.Success(123L, listOf(mock())),
                UiTransformedResult.InProgress)
        whenever(liveTimesLoader.liveTimesFlow)
                .thenReturn(flow)
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
                UiState.PROGRESS,
                UiState.CONTENT)
    }

    @Test
    fun uiStateLiveDataStaysOnContentWhenTransitionToError() = runTest {
        val flow = flowOf(
                UiTransformedResult.InProgress,
                UiTransformedResult.Success(123L, listOf(mock())),
                UiTransformedResult.InProgress,
                UiTransformedResult.Error(123L, ErrorType.SERVER_ERROR))
        whenever(liveTimesLoader.liveTimesFlow)
                .thenReturn(flow)
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
                UiState.PROGRESS,
                UiState.CONTENT)
    }

    @Test
    fun uiStateLiveDataTransitionsToErrorWhenSuccessIsEmpty() = runTest {
        val flow = flowOf(
            UiTransformedResult.InProgress,
            UiTransformedResult.Success(123L, listOf(mock())),
            UiTransformedResult.InProgress,
            UiTransformedResult.Success(123L, emptyList()))
        whenever(liveTimesLoader.liveTimesFlow)
            .thenReturn(flow)
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
            UiState.PROGRESS,
            UiState.CONTENT,
            UiState.PROGRESS,
            UiState.ERROR)
    }

    @Test
    fun uiStateLiveDataOnlyEmitsDistinctValues() = runTest {
        val flow = flowOf(
                UiTransformedResult.InProgress,
                UiTransformedResult.InProgress,
                UiTransformedResult.InProgress)
        whenever(liveTimesLoader.liveTimesFlow)
                .thenReturn(flow)
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(UiState.PROGRESS)
    }

    @Test
    fun uiStateLiveDataTransitionsCorrectly() = runTest {
        val flow = flowOf(
            UiTransformedResult.InProgress,
            UiTransformedResult.Error(123L, ErrorType.SERVER_ERROR),
            UiTransformedResult.InProgress,
            UiTransformedResult.Success(123L, listOf(mock())),
            UiTransformedResult.InProgress,
            UiTransformedResult.Success(123L, emptyList()),
            UiTransformedResult.InProgress,
            UiTransformedResult.Success(123L, listOf(mock())))
        whenever(liveTimesLoader.liveTimesFlow)
            .thenReturn(flow)
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
            UiState.PROGRESS,
            UiState.ERROR,
            UiState.CONTENT,
            UiState.PROGRESS,
            UiState.ERROR,
            UiState.CONTENT)
    }

    @Test
    fun errorWithContentLiveDataEmitsNullWhenUiStateIsError() = runTest {
        val flow = flowOf(UiTransformedResult.Error(123L, ErrorType.SERVER_ERROR))
        whenever(liveTimesLoader.liveTimesFlow)
                .thenReturn(flow)
        val viewModel = createViewModel(stopCode = "123456")

        viewModel.liveTimesLiveData.test()
        val errorObserver = viewModel.errorLiveData.test()
        val errorWithContentObserver = viewModel.errorWithContentLiveData.test()
        advanceUntilIdle()

        errorObserver.assertValues(ErrorType.SERVER_ERROR)
        errorWithContentObserver.assertValues(null)
    }

    @Test
    fun errorWithContentLiveDataEmitsNullWhenUiStateIsContent() = runTest {
        val flow = flowOf(UiTransformedResult.Success(123L, listOf(mock())))
        whenever(liveTimesLoader.liveTimesFlow)
                .thenReturn(flow)
        val viewModel = createViewModel(stopCode = "123456")

        viewModel.liveTimesLiveData.test()
        val errorObserver = viewModel.errorLiveData.test()
        val errorWithContentObserver = viewModel.errorWithContentLiveData.test()
        advanceUntilIdle()

        errorObserver.assertValues(null)
        errorWithContentObserver.assertValues(null)
    }

    @Test
    fun errorWithContentLiveDataEmitsEventWhenUiStateIsContentAfterError() = runTest {
        val flow = flowOf(
                UiTransformedResult.Success(123L, listOf(mock())),
                UiTransformedResult.Error(123L, ErrorType.SERVER_ERROR))
        whenever(liveTimesLoader.liveTimesFlow)
                .thenReturn(flow)
        val viewModel = createViewModel(stopCode = "123456")

        viewModel.liveTimesLiveData.test()
        val errorObserver = viewModel.errorLiveData.test()
        val errorWithContentObserver = viewModel.errorWithContentLiveData.test()
        advanceUntilIdle()

        errorObserver.assertValues(null, ErrorType.SERVER_ERROR)
        assertEquals(2, errorWithContentObserver.observedValues.size)
        assertNull(errorWithContentObserver.observedValues[0])
        assertEquals(ErrorType.SERVER_ERROR, errorWithContentObserver.observedValues[1]?.peek())
    }

    @Test
    fun lastRefreshLiveDataDoesNotEmitWhenThereIsNoLastSuccess() = runTest {
        val flow = flowOf(UiTransformedResult.InProgress)
        whenever(liveTimesLoader.liveTimesFlow)
                .thenReturn(flow)
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.lastRefreshLiveData.test()
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun lastRefreshLiveDataEmitsUponFirstSuccess() = runTest {
        val flow = flowOf(UiTransformedResult.Success(123L, listOf(mock())))
        whenever(liveTimesLoader.liveTimesFlow)
                .thenReturn(flow)
        val lastRefreshFlow = flowOf(
                LastRefreshTime.Now,
                LastRefreshTime.Minutes(1),
                LastRefreshTime.MoreThanOneHour)
        whenever(lastRefreshTimeCalculator.getLastRefreshTimeFlow(123L))
                .thenReturn(lastRefreshFlow)
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.lastRefreshLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
                LastRefreshTime.Now,
                LastRefreshTime.Minutes(1),
                LastRefreshTime.MoreThanOneHour)
    }

    @Test
    fun lastRefreshLiveDataReEmitsUponNextSuccess() = runTest {
        val flow = flowOf(
                UiTransformedResult.InProgress,
                UiTransformedResult.Success(123L, listOf(mock())),
                UiTransformedResult.InProgress,
                UiTransformedResult.Error(124L, ErrorType.SERVER_ERROR),
                UiTransformedResult.InProgress,
                UiTransformedResult.Success(125L, listOf(mock())))
        whenever(liveTimesLoader.liveTimesFlow)
                .thenReturn(flow)
        val lastRefreshFlow1 = flowOf(
                LastRefreshTime.Now,
                LastRefreshTime.Minutes(1))
        val lastRefreshFlow2 = flowOf(
                LastRefreshTime.Now,
                LastRefreshTime.Minutes(2),
                LastRefreshTime.MoreThanOneHour)
        whenever(lastRefreshTimeCalculator.getLastRefreshTimeFlow(123L))
                .thenReturn(lastRefreshFlow1)
        whenever(lastRefreshTimeCalculator.getLastRefreshTimeFlow(125L))
                .thenReturn(lastRefreshFlow2)
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.lastRefreshLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
                LastRefreshTime.Now,
                LastRefreshTime.Minutes(1),
                LastRefreshTime.Now,
                LastRefreshTime.Minutes(2),
                LastRefreshTime.MoreThanOneHour)
    }

    @Test
    fun onRefreshMenuItemClickedCausesRefresh() = runTest {
        val loadResult = UiTransformedResult.Success(123L, emptyList())
        whenever(liveTimesLoader.liveTimesFlow)
            .thenReturn(flowOf(loadResult))
        val viewModel = createViewModel(stopCode = "123456")

        viewModel.liveTimesLiveData.test()
        advanceUntilIdle()
        viewModel.onRefreshMenuItemClicked()
        advanceUntilIdle()

        verify(refreshController)
            .requestRefresh()
    }

    @Test
    fun onSortMenuItemClickedTogglesSortByTime() = runTest {
        val viewModel = createViewModel(stopCode = null)

        viewModel.onSortMenuItemClicked()
        advanceUntilIdle()

        verify(preferenceRepository)
                .toggleSortByTime()
    }

    @Test
    fun onAutoRefreshMenuItemClickedTogglesAutoRefresh() = runTest {
        val viewModel = createViewModel(stopCode = null)

        viewModel.onAutoRefreshMenuItemClicked()
        advanceUntilIdle()

        verify(preferenceRepository)
                .toggleAutoRefresh()
    }

    @Test
    fun onSwipeToRefreshCausesRefresh() = runTest {
        val loadResult = UiTransformedResult.Success(123L, emptyList())
        whenever(liveTimesLoader.liveTimesFlow)
            .thenReturn(flowOf(loadResult))
        val viewModel = createViewModel(stopCode = "123456")

        viewModel.liveTimesLiveData.test()
        advanceUntilIdle()
        viewModel.onSwipeToRefresh()
        advanceUntilIdle()
        advanceUntilIdle()

        verify(refreshController)
            .requestRefresh()
    }

    @Test
    fun onParentItemClickedPassesItemToExpandedServicesTracker() {
        val viewModel = createViewModel(stopCode = null)

        viewModel.onParentItemClicked("123")

        verify(expandedServicesTracker)
                .onServiceClicked("123")
    }

    @Test
    fun newLoadOfLiveTimesDoesNotCauseAutoRefreshWhenAutoRefreshIsDisabled() = runTest {
        val loadResult = UiTransformedResult.Success(123L, emptyList())
        whenever(liveTimesLoader.liveTimesFlow)
                .thenReturn(flowOf(UiTransformedResult.Success(123L, emptyList())))
        whenever(preferenceRepository.isLiveTimesAutoRefreshEnabledFlow)
                .thenReturn(flowOf(false))
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.isAutoRefreshLiveData.test()
        viewModel.liveTimesLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
        argumentCaptor<() -> Boolean>().apply {
            verify(refreshController)
                    .performAutoRefreshDelay(eq(loadResult), capture())
            assertFalse(firstValue.invoke())
        }
    }

    @Test
    fun newLoadOfLiveTimesCausesAutoRefreshWhenAutoRefreshIsEnabled() = runTest {
        val loadResult = UiTransformedResult.Success(123L, emptyList())
        whenever(liveTimesLoader.liveTimesFlow)
                .thenReturn(flowOf(UiTransformedResult.Success(123L, emptyList())))
        whenever(preferenceRepository.isLiveTimesAutoRefreshEnabledFlow)
                .thenReturn(flowOf(true))
        val viewModel = createViewModel(stopCode = "123456")

        val observer = viewModel.isAutoRefreshLiveData.test()
        viewModel.liveTimesLiveData.test()
        advanceUntilIdle()

        observer.assertValues(true)
        argumentCaptor<() -> Boolean>().apply {
            verify(refreshController)
                    .performAutoRefreshDelay(eq(loadResult), capture())
            assertTrue(firstValue.invoke())
        }
    }

    private fun createViewModel(
        stopCode: String?): BusTimesFragmentViewModel {
        whenever(arguments.stopCodeFlow)
            .thenReturn(flowOf(stopCode))

        return BusTimesFragmentViewModel(
            arguments,
            expandedServicesTracker,
            liveTimesLoader,
            lastRefreshTimeCalculator,
            refreshController,
            preferenceRepository,
            connectivityRepository,
            coroutineRule.scope,
            coroutineRule.testDispatcher)
    }
}