/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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
import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityRepository
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceRepository
import uk.org.rivernile.android.bustracker.coroutines.FlowTestObserver
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.testutils.LiveDataTestObserver
import uk.org.rivernile.android.bustracker.utils.Event

/**
 * Tests for [BusTimesFragmentViewModel].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class BusTimesFragmentViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var expandedServicesTracker: ExpandedServicesTracker
    @Mock
    private lateinit var liveTimesFlowFactory: LiveTimesFlowFactory
    @Mock
    private lateinit var lastRefreshTimeCalculator: LastRefreshTimeCalculator
    @Mock
    private lateinit var refreshController: RefreshController
    @Mock
    private lateinit var preferenceRepository: PreferenceRepository
    @Mock
    private lateinit var connectivityRepository: ConnectivityRepository

    private val refreshChannel = Channel<Unit>(Channel.CONFLATED)

    private val booleanObserver = LiveDataTestObserver<Boolean>()
    private val nullableBooleanObserver = LiveDataTestObserver<Boolean?>()
    private val liveTimesObserver = LiveDataTestObserver<List<UiLiveTimesItem>?>()

    private lateinit var viewModel: BusTimesFragmentViewModel

    @Before
    fun setUp() {
        viewModel = BusTimesFragmentViewModel(
                expandedServicesTracker,
                liveTimesFlowFactory,
                lastRefreshTimeCalculator,
                refreshController,
                preferenceRepository,
                connectivityRepository,
                coroutineRule.testDispatcher)

        whenever(refreshController.refreshTriggerReceiveChannel)
                .thenReturn(refreshChannel)
    }

    @Test
    fun hasConnectivityLiveDataEmitsFromFlow() = coroutineRule.runBlockingTest {
        val items = flowOf(true, false, true)
        whenever(connectivityRepository.hasInternetConnectivityFlow())
                .thenReturn(items)

        viewModel.hasConnectivityLiveData.observeForever(booleanObserver)

        booleanObserver.assertValues(true, false, true)
    }

    @Test
    fun hasConnectivityLiveDataOnlyEmitsDistinctValues() = coroutineRule.runBlockingTest {
        val items = flowOf(true, true, false, false, true)
        whenever(connectivityRepository.hasInternetConnectivityFlow())
                .thenReturn(items)

        viewModel.hasConnectivityLiveData.observeForever(booleanObserver)

        booleanObserver.assertValues(true, false, true)
    }

    @Test
    fun isSortedByTimeEmitsNullWhenStopCodeIsNull() {
        viewModel.stopCode = null

        viewModel.isSortedByTimeLiveData.observeForever(nullableBooleanObserver)

        nullableBooleanObserver.assertValues(null)
        verify(preferenceRepository, never())
                .isLiveTimesSortByTimeFlow()
    }

    @Test
    fun isSortedByTimeEmitsValuesFromRepository() = coroutineRule.runBlockingTest {
        val flow = flowOf(true, false, true)
        whenever(preferenceRepository.isLiveTimesSortByTimeFlow())
                .thenReturn(flow)
        viewModel.stopCode = "123456"

        viewModel.isSortedByTimeLiveData.observeForever(nullableBooleanObserver)

        nullableBooleanObserver.assertValues(true, false, true)
    }

    @Test
    fun isSortedByTimeEmitsDistinctValuesFromRepository() = coroutineRule.runBlockingTest {
        val flow = flowOf(true, true, false, false, true)
        whenever(preferenceRepository.isLiveTimesSortByTimeFlow())
                .thenReturn(flow)
        viewModel.stopCode = "123456"

        viewModel.isSortedByTimeLiveData.observeForever(nullableBooleanObserver)

        nullableBooleanObserver.assertValues(true, false, true)
    }

    @Test
    fun isAutoRefreshEmitsNullWhenStopCodeIsNull() {
        viewModel.stopCode = null

        viewModel.isAutoRefreshLiveData.observeForever(nullableBooleanObserver)

        nullableBooleanObserver.assertValues(null)
        verify(preferenceRepository, never())
                .isLiveTimesAutoRefreshEnabledFlow()
    }

    @Test
    fun isAutoRefreshEmitsValuesFromRepository() = coroutineRule.runBlockingTest {
        val flow = flowOf(true, false, true)
        whenever(preferenceRepository.isLiveTimesAutoRefreshEnabledFlow())
                .thenReturn(flow)
        viewModel.stopCode = "123456"

        viewModel.isAutoRefreshLiveData.observeForever(nullableBooleanObserver)

        nullableBooleanObserver.assertValues(true, false, true)
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
    fun isAutoRefreshEmitsDistinctValuesFromRepository() = coroutineRule.runBlockingTest {
        val flow = flowOf(true, true, false, false, true)
        whenever(preferenceRepository.isLiveTimesAutoRefreshEnabledFlow())
                .thenReturn(flow)
        viewModel.stopCode = "123456"

        viewModel.isAutoRefreshLiveData.observeForever(nullableBooleanObserver)

        nullableBooleanObserver.assertValues(true, false, true)
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
    fun isAutoRefreshPassesCurrentResultToRefreshControllerWhenAutoRefreshIsFalse() =
            coroutineRule.runBlockingTest {
        givenExpandServicesReturnsEmptySet()
        val loadResult = UiTransformedResult.Success(123L, emptyList())
        val stopCodeObserver = FlowTestObserver<String?>(this)
        doAnswer {
            val stopCodeFlow = it.getArgument<Flow<String>>(0)
            stopCodeObserver.observe(stopCodeFlow)
            flowOf<UiTransformedResult>(loadResult)
        }.whenever(liveTimesFlowFactory).createLiveTimesFlow(any(), any(), any())
        whenever(preferenceRepository.isLiveTimesAutoRefreshEnabledFlow())
                .thenReturn(flowOf(false))

        viewModel.stopCode = "123456"
        viewModel.liveTimesLiveData.observeForever(liveTimesObserver)
        viewModel.isAutoRefreshLiveData.observeForever(nullableBooleanObserver)
        stopCodeObserver.finish()

        stopCodeObserver.assertValues("123456")
        verify(refreshController)
                .onAutoRefreshPreferenceChanged(loadResult, false)
    }

    @Test
    fun isAutoRefreshPassesCurrentResultToRefreshControllerWhenAutoRefreshIsTrue() =
            coroutineRule.runBlockingTest {
        givenExpandServicesReturnsEmptySet()
        val loadResult = UiTransformedResult.Success(123L, emptyList())
        val stopCodeObserver = FlowTestObserver<String?>(this)
        doAnswer {
            val stopCodeFlow = it.getArgument<Flow<String>>(0)
            stopCodeObserver.observe(stopCodeFlow)
            flowOf<UiTransformedResult>(loadResult)
        }.whenever(liveTimesFlowFactory).createLiveTimesFlow(any(), any(), any())
        whenever(preferenceRepository.isLiveTimesAutoRefreshEnabledFlow())
                .thenReturn(flowOf(true))

        viewModel.stopCode = "123456"
        viewModel.liveTimesLiveData.observeForever(liveTimesObserver)
        viewModel.isAutoRefreshLiveData.observeForever(nullableBooleanObserver)
        stopCodeObserver.finish()

        stopCodeObserver.assertValues("123456")
        verify(refreshController)
                .onAutoRefreshPreferenceChanged(loadResult, true)
    }

    @Test
    fun showProgressLiveDataEmitsNullWhenStopCodeIsNull() {
        viewModel.stopCode = null

        viewModel.showProgressLiveData.observeForever(nullableBooleanObserver)

        nullableBooleanObserver.assertValues(null)
    }

    @Test
    fun showProgressLiveDataEmitsFalseWhenResultIsError() = coroutineRule.runBlockingTest {
        givenExpandServicesReturnsEmptySet()
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
                .thenReturn(flowOf(UiTransformedResult.Error(123L, ErrorType.SERVER_ERROR)))

        viewModel.stopCode = "123456"
        viewModel.showProgressLiveData.observeForever(nullableBooleanObserver)

        nullableBooleanObserver.assertValues(false)
    }

    @Test
    fun showProgressLiveDataEmitsFalseWhenResultIsSuccess() = coroutineRule.runBlockingTest {
        givenExpandServicesReturnsEmptySet()
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
                .thenReturn(flowOf(UiTransformedResult.Success(123L, emptyList())))

        viewModel.stopCode = "123456"
        viewModel.showProgressLiveData.observeForever(nullableBooleanObserver)

        nullableBooleanObserver.assertValues(false)
    }

    @Test
    fun showProgressLiveDataEmitsTrueWhenResultIsInProgress() = coroutineRule.runBlockingTest {
        givenExpandServicesReturnsEmptySet()
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
                .thenReturn(flowOf(UiTransformedResult.InProgress))

        viewModel.stopCode = "123456"
        viewModel.showProgressLiveData.observeForever(nullableBooleanObserver)

        nullableBooleanObserver.assertValues(true)
    }

    @Test
    fun showProgressLiveDataEmitsCorrectValues() = coroutineRule.runBlockingTest {
        givenExpandServicesReturnsEmptySet()
        val values = flowOf(
                UiTransformedResult.InProgress,
                UiTransformedResult.Error(123L, ErrorType.SERVER_ERROR),
                UiTransformedResult.InProgress,
                UiTransformedResult.Success(123L, emptyList()))
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
                .thenReturn(values)

        viewModel.stopCode = "123456"
        viewModel.showProgressLiveData.observeForever(nullableBooleanObserver)

        nullableBooleanObserver.assertValues(true, false, true, false)
    }

    @Test
    fun errorLiveDataEmitsNothingWhenIsInProgress() = coroutineRule.runBlockingTest {
        givenExpandServicesReturnsEmptySet()
        val flow = flowOf(UiTransformedResult.InProgress)
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
                .thenReturn(flow)
        val observer = LiveDataTestObserver<ErrorType?>()

        viewModel.stopCode = "123456"
        viewModel.errorLiveData.observeForever(observer)

        observer.assertEmpty()
    }

    @Test
    fun errorLiveDataEmitsNullWhenIsSuccessWithData() = coroutineRule.runBlockingTest {
        givenExpandServicesReturnsEmptySet()
        val flow = flowOf(
                UiTransformedResult.Success(
                        123L,
                        listOf(mock())))
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
                .thenReturn(flow)
        val observer = LiveDataTestObserver<ErrorType?>()

        viewModel.stopCode = "123456"
        viewModel.errorLiveData.observeForever(observer)

        observer.assertValues(null)
    }

    @Test
    fun errorLiveDataEmitsErrorWhenIsSuccessWithNoData() = coroutineRule.runBlockingTest {
        givenExpandServicesReturnsEmptySet()
        val flow = flowOf(UiTransformedResult.Success(123L, emptyList()))
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
                .thenReturn(flow)
        val observer = LiveDataTestObserver<ErrorType?>()

        viewModel.stopCode = "123456"
        viewModel.errorLiveData.observeForever(observer)

        observer.assertValues(ErrorType.NO_DATA)
    }

    @Test
    fun errorLiveDataEmitsErrorWhenError() = coroutineRule.runBlockingTest {
        givenExpandServicesReturnsEmptySet()
        val flow = flowOf(UiTransformedResult.Error(123L, ErrorType.SERVER_ERROR))
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
                .thenReturn(flow)
        val observer = LiveDataTestObserver<ErrorType?>()

        viewModel.stopCode = "123456"
        viewModel.errorLiveData.observeForever(observer)

        observer.assertValues(ErrorType.SERVER_ERROR)
    }

    @Test
    fun errorLiveDataEmitsCorrectValues() = coroutineRule.runBlockingTest {
        givenExpandServicesReturnsEmptySet()
        val flow = flowOf(
                UiTransformedResult.InProgress,
                UiTransformedResult.Error(123L, ErrorType.SERVER_ERROR),
                UiTransformedResult.InProgress,
                UiTransformedResult.Success(123L, emptyList()),
                UiTransformedResult.InProgress,
                UiTransformedResult.Success(123L, listOf(mock()))
        )
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
                .thenReturn(flow)
        val observer = LiveDataTestObserver<ErrorType?>()

        viewModel.stopCode = "123456"
        viewModel.errorLiveData.observeForever(observer)

        observer.assertValues(
                ErrorType.SERVER_ERROR,
                ErrorType.NO_DATA,
                null)
    }

    @Test
    fun liveTimesLiveDataEmitsNullWhenSuccessIsEmpty() = coroutineRule.runBlockingTest {
        givenExpandServicesReturnsEmptySet()
        val flow = flowOf(UiTransformedResult.Success(123L, emptyList()))
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
                .thenReturn(flow)
        val observer = LiveDataTestObserver<List<UiLiveTimesItem>?>()

        viewModel.stopCode = "123456"
        viewModel.liveTimesLiveData.observeForever(observer)

        observer.assertValues(null)
    }

    @Test
    fun liveTimesLiveDataEmitsValuesWhenSuccessHasValues() = coroutineRule.runBlockingTest {
        givenExpandServicesReturnsEmptySet()
        val values = listOf<UiLiveTimesItem>(mock())
        val flow = flowOf(UiTransformedResult.Success(123L, values))
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
                .thenReturn(flow)
        val observer = LiveDataTestObserver<List<UiLiveTimesItem>?>()

        viewModel.stopCode = "123456"
        viewModel.liveTimesLiveData.observeForever(observer)

        observer.assertValues(values)
    }

    @Test
    fun liveTimesLiveDataEmitsCorrectValues() = coroutineRule.runBlockingTest {
        givenExpandServicesReturnsEmptySet()
        val values = listOf<UiLiveTimesItem>(mock())
        val flow = flowOf(
                UiTransformedResult.Success(123L, emptyList()),
                UiTransformedResult.Success(123L, values))
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
                .thenReturn(flow)
        val observer = LiveDataTestObserver<List<UiLiveTimesItem>?>()

        viewModel.stopCode = "123456"
        viewModel.liveTimesLiveData.observeForever(observer)

        observer.assertValues(null, values)
    }

    @Test
    fun uiStateLiveDataEmitsInitialProgress() = coroutineRule.runBlockingTest {
        pauseDispatcher()
        givenExpandServicesReturnsEmptySet()
        val flow = flowOf(UiTransformedResult.InProgress)
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
                .thenReturn(flow)
        val observer = LiveDataTestObserver<UiState>()

        viewModel.stopCode = "123456"
        viewModel.uiStateLiveData.observeForever(observer)
        resumeDispatcher()

        observer.assertValues(UiState.PROGRESS)
    }

    @Test
    fun uiStateLiveDataTransitionsToErrorWhenError() = coroutineRule.runBlockingTest {
        pauseDispatcher()
        givenExpandServicesReturnsEmptySet()
        val flow = flowOf(
            UiTransformedResult.InProgress,
            UiTransformedResult.Error(123L, ErrorType.SERVER_ERROR))
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
                .thenReturn(flow)
        val observer = LiveDataTestObserver<UiState>()

        viewModel.stopCode = "123456"
        viewModel.uiStateLiveData.observeForever(observer)
        resumeDispatcher()

        observer.assertValues(
                UiState.PROGRESS,
                UiState.ERROR)
    }

    @Test
    fun uiStateLiveDataTransitionsToContentOnSuccess() = coroutineRule.runBlockingTest {
        pauseDispatcher()
        givenExpandServicesReturnsEmptySet()
        val flow = flowOf(
                UiTransformedResult.InProgress,
                UiTransformedResult.Success(123L, listOf(mock())))
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
                .thenReturn(flow)
        val observer = LiveDataTestObserver<UiState>()

        viewModel.stopCode = "123456"
        viewModel.uiStateLiveData.observeForever(observer)
        resumeDispatcher()

        observer.assertValues(
                UiState.PROGRESS,
                UiState.CONTENT)
    }

    @Test
    fun uiStateLiveDataStaysOnContentWhenTransitionToProgress() = coroutineRule.runBlockingTest {
        pauseDispatcher()
        givenExpandServicesReturnsEmptySet()
        val flow = flowOf(
                UiTransformedResult.InProgress,
                UiTransformedResult.Success(123L, listOf(mock())),
                UiTransformedResult.InProgress)
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
                .thenReturn(flow)
        val observer = LiveDataTestObserver<UiState>()

        viewModel.stopCode = "123456"
        viewModel.uiStateLiveData.observeForever(observer)
        resumeDispatcher()

        observer.assertValues(
                UiState.PROGRESS,
                UiState.CONTENT)
    }

    @Test
    fun uiStateLiveDataStaysOnContentWhenTransitionToError() = coroutineRule.runBlockingTest {
        pauseDispatcher()
        givenExpandServicesReturnsEmptySet()
        val flow = flowOf(
                UiTransformedResult.InProgress,
                UiTransformedResult.Success(123L, listOf(mock())),
                UiTransformedResult.InProgress,
                UiTransformedResult.Error(123L, ErrorType.SERVER_ERROR))
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
                .thenReturn(flow)
        val observer = LiveDataTestObserver<UiState>()

        viewModel.stopCode = "123456"
        viewModel.uiStateLiveData.observeForever(observer)
        resumeDispatcher()

        observer.assertValues(
                UiState.PROGRESS,
                UiState.CONTENT)
    }

    @Test
    fun uiStateLiveDataTransitionsToErrorWhenSuccessIsEmpty() = coroutineRule.runBlockingTest {
        pauseDispatcher()
        givenExpandServicesReturnsEmptySet()
        val flow = flowOf(
                UiTransformedResult.InProgress,
                UiTransformedResult.Success(123L, listOf(mock())),
                UiTransformedResult.InProgress,
                UiTransformedResult.Success(123L, emptyList()))
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
                .thenReturn(flow)
        val observer = LiveDataTestObserver<UiState>()

        viewModel.stopCode = "123456"
        viewModel.uiStateLiveData.observeForever(observer)
        resumeDispatcher()

        observer.assertValues(
                UiState.PROGRESS,
                UiState.CONTENT,
                UiState.ERROR)
    }

    @Test
    fun uiStateLiveDataOnlyEmitsDistinctValues() = coroutineRule.runBlockingTest {
        pauseDispatcher()
        givenExpandServicesReturnsEmptySet()
        val flow = flowOf(
                UiTransformedResult.InProgress,
                UiTransformedResult.InProgress,
                UiTransformedResult.InProgress)
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
                .thenReturn(flow)
        val observer = LiveDataTestObserver<UiState>()

        viewModel.stopCode = "123456"
        viewModel.uiStateLiveData.observeForever(observer)
        resumeDispatcher()

        observer.assertValues(UiState.PROGRESS)
    }

    @Test
    fun uiStateLiveDataTransitionsCorrectly() = coroutineRule.runBlockingTest {
        pauseDispatcher()
        givenExpandServicesReturnsEmptySet()
        val flow = flowOf(
                UiTransformedResult.InProgress,
                UiTransformedResult.Error(123L, ErrorType.SERVER_ERROR),
                UiTransformedResult.InProgress,
                UiTransformedResult.Success(123L, listOf(mock())),
                UiTransformedResult.InProgress,
                UiTransformedResult.Success(123L, emptyList()),
                UiTransformedResult.InProgress,
                UiTransformedResult.Success(123L, listOf(mock())))
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
                .thenReturn(flow)
        val observer = LiveDataTestObserver<UiState>()

        viewModel.stopCode = "123456"
        viewModel.uiStateLiveData.observeForever(observer)
        resumeDispatcher()

        observer.assertValues(
                UiState.PROGRESS,
                UiState.ERROR,
                UiState.CONTENT,
                UiState.ERROR,
                UiState.CONTENT)
    }

    @Test
    fun errorWithContentLiveDataEmitsNullWhenUiStateIsError() = coroutineRule.runBlockingTest {
        pauseDispatcher()
        givenExpandServicesReturnsEmptySet()
        val flow = flowOf(UiTransformedResult.Error(123L, ErrorType.SERVER_ERROR))
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
                .thenReturn(flow)
        val errorObserver = LiveDataTestObserver<ErrorType?>()
        val errorWithContentObserver = LiveDataTestObserver<Event<ErrorType>?>()

        viewModel.stopCode = "123456"
        viewModel.liveTimesLiveData.observeForever { }
        viewModel.errorLiveData.observeForever(errorObserver)
        viewModel.errorWithContentLiveData.observeForever(errorWithContentObserver)
        resumeDispatcher()

        errorObserver.assertValues(ErrorType.SERVER_ERROR)
        errorWithContentObserver.assertValues(null)
    }

    @Test
    fun errorWithContentLiveDataEmitsNullWhenUiStateIsContent() = coroutineRule.runBlockingTest {
        pauseDispatcher()
        givenExpandServicesReturnsEmptySet()
        val flow = flowOf(UiTransformedResult.Success(123L, listOf(mock())))
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
                .thenReturn(flow)
        val errorObserver = LiveDataTestObserver<ErrorType?>()
        val errorWithContentObserver = LiveDataTestObserver<Event<ErrorType>?>()

        viewModel.stopCode = "123456"
        viewModel.liveTimesLiveData.observeForever { }
        viewModel.errorLiveData.observeForever(errorObserver)
        viewModel.errorWithContentLiveData.observeForever(errorWithContentObserver)
        resumeDispatcher()

        errorObserver.assertValues(null)
        errorWithContentObserver.assertValues(null)
    }

    @Test
    fun errorWithContentLiveDataEmitsEventWhenUiStateIsContentAfterError() =
            coroutineRule.runBlockingTest {
        pauseDispatcher()
        givenExpandServicesReturnsEmptySet()
        val flow = flowOf(
                UiTransformedResult.Success(123L, listOf(mock())),
                UiTransformedResult.Error(123L, ErrorType.SERVER_ERROR))
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
                .thenReturn(flow)
        val errorObserver = LiveDataTestObserver<ErrorType?>()
        val errorWithContentObserver = LiveDataTestObserver<Event<ErrorType>?>()

        viewModel.stopCode = "123456"
        viewModel.liveTimesLiveData.observeForever { }
        viewModel.errorLiveData.observeForever(errorObserver)
        viewModel.errorWithContentLiveData.observeForever(errorWithContentObserver)
        resumeDispatcher()

        errorObserver.assertValues(null, ErrorType.SERVER_ERROR)
        assertEquals(2, errorWithContentObserver.observedValues.size)
        assertNull(errorWithContentObserver.observedValues[0])
        assertEquals(ErrorType.SERVER_ERROR, errorWithContentObserver.observedValues[1]?.peek())
    }

    @Test
    fun lastRefreshLiveDataDoesNotEmitWhenThereIsNoLastSuccess() = coroutineRule.runBlockingTest {
        pauseDispatcher()
        givenExpandServicesReturnsEmptySet()
        val flow = flowOf(UiTransformedResult.InProgress)
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
                .thenReturn(flow)
        val observer = LiveDataTestObserver<LastRefreshTime>()

        viewModel.stopCode = "123456"
        viewModel.lastRefreshLiveData.observeForever(observer)
        resumeDispatcher()

        observer.assertEmpty()
    }

    @Test
    fun lastRefreshLiveDataEmitsUponFirstSuccess() = coroutineRule.runBlockingTest {
        pauseDispatcher()
        givenExpandServicesReturnsEmptySet()
        val flow = flowOf(UiTransformedResult.Success(123L, listOf(mock())))
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
                .thenReturn(flow)
        val lastRefreshFlow = flowOf(
                LastRefreshTime.Now,
                LastRefreshTime.Minutes(1),
                LastRefreshTime.MoreThanOneHour)
        whenever(lastRefreshTimeCalculator.getLastRefreshTimeFlow(123L))
                .thenReturn(lastRefreshFlow)
        val observer = LiveDataTestObserver<LastRefreshTime>()

        viewModel.stopCode = "123456"
        viewModel.lastRefreshLiveData.observeForever(observer)
        resumeDispatcher()

        observer.assertValues(
                LastRefreshTime.Now,
                LastRefreshTime.Minutes(1),
                LastRefreshTime.MoreThanOneHour)
    }

    @Test
    fun lastRefreshLiveDataReEmitsUponNextSuccess() = coroutineRule.runBlockingTest {
        pauseDispatcher()
        givenExpandServicesReturnsEmptySet()
        val flow = flowOf(
                UiTransformedResult.InProgress,
                UiTransformedResult.Success(123L, listOf(mock())),
                UiTransformedResult.InProgress,
                UiTransformedResult.Error(124L, ErrorType.SERVER_ERROR),
                UiTransformedResult.InProgress,
                UiTransformedResult.Success(125L, listOf(mock())))
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
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
        val observer = LiveDataTestObserver<LastRefreshTime>()

        viewModel.stopCode = "123456"
        viewModel.lastRefreshLiveData.observeForever(observer)
        resumeDispatcher()

        observer.assertValues(
                LastRefreshTime.Now,
                LastRefreshTime.Minutes(1),
                LastRefreshTime.Now,
                LastRefreshTime.Minutes(2),
                LastRefreshTime.MoreThanOneHour)
    }

    @Test
    fun onRefreshMenuItemClickedCausesRefresh() = coroutineRule.runBlockingTest {
        givenExpandServicesReturnsEmptySet()
        val loadResult = UiTransformedResult.Success(123L, emptyList())
        val stopCodeObserver = FlowTestObserver<String?>(this)
        doAnswer {
            val stopCodeFlow = it.getArgument<Flow<String>>(0)
            stopCodeObserver.observe(stopCodeFlow)
            flowOf<UiTransformedResult>(loadResult)
        }.whenever(liveTimesFlowFactory).createLiveTimesFlow(any(), any(), any())

        viewModel.stopCode = "123456"
        viewModel.liveTimesLiveData.observeForever(liveTimesObserver)
        viewModel.onRefreshMenuItemClicked()
        stopCodeObserver.finish()

        stopCodeObserver.assertValues("123456")
        verify(refreshController)
                .requestRefresh()
    }

    @Test
    fun onSortMenuItemClickedTogglesSortByTime() {
        viewModel.onSortMenuItemClicked()

        verify(preferenceRepository)
                .toggleSortByTime()
    }

    @Test
    fun onAutoRefreshMenuItemClickedTogglesAutoRefresh() {
        viewModel.onAutoRefreshMenuItemClicked()

        verify(preferenceRepository)
                .toggleAutoRefresh()
    }

    @Test
    fun onSwipeToRefreshCausesRefresh() = coroutineRule.runBlockingTest {
        givenExpandServicesReturnsEmptySet()
        val loadResult = UiTransformedResult.Success(123L, emptyList())
        val stopCodeObserver = FlowTestObserver<String?>(this)
        doAnswer {
            val stopCodeFlow = it.getArgument<Flow<String>>(0)
            stopCodeObserver.observe(stopCodeFlow)
            flowOf<UiTransformedResult>(loadResult)
        }.whenever(liveTimesFlowFactory).createLiveTimesFlow(any(), any(), any())

        viewModel.stopCode = "123456"
        viewModel.liveTimesLiveData.observeForever(liveTimesObserver)
        viewModel.onSwipeToRefresh()
        stopCodeObserver.finish()

        stopCodeObserver.assertValues("123456")
        verify(refreshController)
                .requestRefresh()
    }

    @Test
    fun onParentItemClickedPassesItemToExpandedServicesTracker() {
        viewModel.onParentItemClicked("123")

        verify(expandedServicesTracker)
                .onServiceClicked("123")
    }

    @Test
    fun newLoadOfLiveTimesDoesNotCauseAutoRefreshWhenAutoRefreshIsDisabled() =
            coroutineRule.runBlockingTest {
        pauseDispatcher()
        givenExpandServicesReturnsEmptySet()
        val loadResult = UiTransformedResult.Success(123L, emptyList())
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
                .thenReturn(flowOf(UiTransformedResult.Success(123L, emptyList())))
        whenever(preferenceRepository.isLiveTimesAutoRefreshEnabledFlow())
                .thenReturn(flowOf(false))

        viewModel.stopCode = "123456"
        viewModel.isAutoRefreshLiveData.observeForever(nullableBooleanObserver)
        viewModel.liveTimesLiveData.observeForever(liveTimesObserver)
        resumeDispatcher()

        nullableBooleanObserver.assertValues(false)
        argumentCaptor<() -> Boolean>().apply {
            verify(refreshController)
                    .performAutoRefreshDelay(eq(loadResult), capture())
            assertFalse(firstValue.invoke())
        }
    }

    @Test
    fun newLoadOfLiveTimesCausesAutoRefreshWhenAutoRefreshIsEnabled() =
            coroutineRule.runBlockingTest {
        pauseDispatcher()
        givenExpandServicesReturnsEmptySet()
        val loadResult = UiTransformedResult.Success(123L, emptyList())
        whenever(liveTimesFlowFactory.createLiveTimesFlow(any(), any(), any()))
                .thenReturn(flowOf(UiTransformedResult.Success(123L, emptyList())))
        whenever(preferenceRepository.isLiveTimesAutoRefreshEnabledFlow())
                .thenReturn(flowOf(true))

        viewModel.stopCode = "123456"
        viewModel.isAutoRefreshLiveData.observeForever(nullableBooleanObserver)
        viewModel.liveTimesLiveData.observeForever(liveTimesObserver)
        resumeDispatcher()

        nullableBooleanObserver.assertValues(true)
        argumentCaptor<() -> Boolean>().apply {
            verify(refreshController)
                    .performAutoRefreshDelay(eq(loadResult), capture())
            assertTrue(firstValue.invoke())
        }
    }

    private fun givenExpandServicesReturnsEmptySet() {
        whenever(expandedServicesTracker.expandedServicesLiveData)
                .thenReturn(MutableLiveData(emptySet()))
    }
}