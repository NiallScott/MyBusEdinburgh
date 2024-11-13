/*
 * Copyright (C) 2020 - 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.news

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okio.IOException
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.Tweet
import uk.org.rivernile.android.bustracker.core.twitter.LatestTweetsResult
import uk.org.rivernile.android.bustracker.core.twitter.TwitterRepository
import uk.org.rivernile.android.bustracker.core.time.TimeUtils
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.testutils.test

/**
 * Tests for [TwitterUpdatesFragmentViewModel].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class TwitterUpdatesFragmentViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var twitterRepository: TwitterRepository
    @Mock
    private lateinit var timeUtils: TimeUtils

    private lateinit var viewModel: TwitterUpdatesFragmentViewModel

    @Before
    fun setUp() {
        viewModel = TwitterUpdatesFragmentViewModel(
                twitterRepository,
                timeUtils,
                coroutineRule.testDispatcher)
    }

    @Test
    fun tweetsLiveDataEmitsNullByDefault() = runTest {
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(flowOf(LatestTweetsResult.InProgress))

        val observer = viewModel.tweetsLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun tweetsLiveDataDoesNotEmitOnError() = runTest {
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(flowOf(
                        LatestTweetsResult.InProgress,
                        LatestTweetsResult.Error.NoConnectivity))

        val observer = viewModel.tweetsLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun tweetsLiveDataEmitsOnSuccess() = runTest {
        val tweets = listOf<Tweet>(mock(), mock(), mock())
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(flowOf(
                        LatestTweetsResult.InProgress,
                        LatestTweetsResult.Success(tweets)))

        val observer = viewModel.tweetsLiveData.test()
        advanceUntilIdle()
        observer.assertValues(null, tweets)
    }

    @Test
    fun tweetsLiveDataEmitsNullSuccessAfterPreviousSuccess() = runTest {
        val tweets = listOf<Tweet>(mock(), mock(), mock())
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(
                        flowOf(
                                LatestTweetsResult.InProgress,
                                LatestTweetsResult.Success(tweets)),
                        flowOf(
                                LatestTweetsResult.InProgress,
                                LatestTweetsResult.Success(null)))

        val observer = viewModel.tweetsLiveData.test()
        advanceUntilIdle()
        viewModel.onSwipeToRefresh()
        advanceUntilIdle()

        observer.assertValues(null, tweets, null)
    }

    @Test
    fun tweetsLiveDataEmitsNullSuccessAfterPreviousSuccessWhenResultIsEmptyList() = runTest {
        val tweets = listOf<Tweet>(mock(), mock(), mock())
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(
                        flowOf(
                                LatestTweetsResult.InProgress,
                                LatestTweetsResult.Success(tweets)),
                        flowOf(
                                LatestTweetsResult.InProgress,
                                LatestTweetsResult.Success(emptyList())))

        val observer = viewModel.tweetsLiveData.test()
        advanceUntilIdle()
        viewModel.onSwipeToRefresh()
        advanceUntilIdle()

        observer.assertValues(null, tweets, null)
    }

    @Test
    fun tweetsLiveDataDoesNotEmitNewValueWhenResultIsErrorAfterPreviousSuccess() = runTest {
        val tweets = listOf<Tweet>(mock(), mock(), mock())
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(
                        flowOf(
                                LatestTweetsResult.InProgress,
                                LatestTweetsResult.Success(tweets)),
                        flowOf(
                                LatestTweetsResult.InProgress,
                                LatestTweetsResult.Error.NoConnectivity))

        val observer = viewModel.tweetsLiveData.test()
        advanceUntilIdle()
        viewModel.onSwipeToRefresh()
        advanceUntilIdle()

        observer.assertValues(null, tweets)
    }

    @Test
    fun uiStateLiveDataEmitsProgressByDefault() = runTest {
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(flowOf(LatestTweetsResult.InProgress))

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(UiState.PROGRESS)
    }

    @Test
    fun uiStateLiveDataEmitsErrorWhenTransitioningFromProgressOnError() = runTest {
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(flowOf(
                        LatestTweetsResult.InProgress,
                        LatestTweetsResult.Error.NoConnectivity))

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(UiState.PROGRESS, UiState.ERROR)
    }

    @Test
    fun uiStateLiveDataEmitsErrorWhenNullData() = runTest {
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(flowOf(
                        LatestTweetsResult.InProgress,
                        LatestTweetsResult.Success(null)))

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(UiState.PROGRESS, UiState.ERROR)
    }

    @Test
    fun uiStateLiveDataEmitsErrorWhenEmptyData() = runTest {
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(flowOf(
                        LatestTweetsResult.InProgress,
                        LatestTweetsResult.Success(emptyList())))

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(UiState.PROGRESS, UiState.ERROR)
    }

    @Test
    fun uiStateLiveDataEmitsContentWhenDataAvailable() = runTest {
        val tweets = listOf<Tweet>(mock(), mock(), mock())
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(flowOf(
                        LatestTweetsResult.InProgress,
                        LatestTweetsResult.Success(tweets)))

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(UiState.PROGRESS, UiState.CONTENT)
    }

    @Test
    fun uiStateLiveDataDoesNotEmitWhenErrorAfterSuccess() = runTest {
        val tweets = listOf<Tweet>(mock(), mock(), mock())
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(
                        flowOf(
                                LatestTweetsResult.InProgress,
                                LatestTweetsResult.Success(tweets)),
                        flowOf(
                                LatestTweetsResult.InProgress,
                                LatestTweetsResult.Error.NoConnectivity))

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()
        viewModel.onSwipeToRefresh()
        advanceUntilIdle()

        observer.assertValues(UiState.PROGRESS, UiState.CONTENT)
    }

    @Test
    fun uiStateLiveDataEmitsContentWhenSuccessAfterError() = runTest {
        val tweets = listOf<Tweet>(mock(), mock(), mock())
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(
                        flowOf(
                                LatestTweetsResult.InProgress,
                                LatestTweetsResult.Error.NoConnectivity),
                        flowOf(
                                LatestTweetsResult.InProgress,
                                LatestTweetsResult.Success(tweets)))

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()
        viewModel.onSwipeToRefresh()
        advanceUntilIdle()

        observer.assertValues(
                UiState.PROGRESS,
                UiState.ERROR,
                UiState.PROGRESS,
                UiState.CONTENT)
    }

    @Test
    fun uiStateLiveDataDoesNotEmitFurtherContentOnSubsequentSuccess() = runTest {
        val tweets = listOf<Tweet>(mock(), mock(), mock())
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(
                        flowOf(
                                LatestTweetsResult.InProgress,
                                LatestTweetsResult.Success(tweets)),
                        flowOf(
                                LatestTweetsResult.InProgress,
                                LatestTweetsResult.Success(tweets)))

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()
        viewModel.onSwipeToRefresh()
        advanceUntilIdle()

        observer.assertValues(
                UiState.PROGRESS,
                UiState.CONTENT)
    }

    @Test
    fun errorLiveDataEmitsNullByDefault() = runTest {
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(flowOf(LatestTweetsResult.InProgress))

        val observer = viewModel.errorLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun errorLiveDataEmitsNullOnSuccessWithTweets() = runTest {
        val tweets = listOf<Tweet>(mock(), mock(), mock())
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(flowOf(
                        LatestTweetsResult.InProgress,
                        LatestTweetsResult.Success(tweets)))

        val observer = viewModel.errorLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun errorLiveDataEmitsNoDataOnSuccessWithNullData() = runTest {
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(flowOf(
                        LatestTweetsResult.InProgress,
                        LatestTweetsResult.Success(null)))

        val observer = viewModel.errorLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null, Error.NO_DATA)
    }

    @Test
    fun errorLiveDataEmitsNoDataOnSuccessWithEmptyData() = runTest {
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(flowOf(
                        LatestTweetsResult.InProgress,
                        LatestTweetsResult.Success(emptyList())))

        val observer = viewModel.errorLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null, Error.NO_DATA)
    }

    @Test
    fun errorLiveDataEmitsNoConnectivityOnNoConnectivityError() = runTest {
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(flowOf(
                        LatestTweetsResult.InProgress,
                        LatestTweetsResult.Error.NoConnectivity))

        val observer = viewModel.errorLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null, Error.NO_CONNECTIVITY)
    }

    @Test
    fun errorLiveDataEmitsCommunicationErrorOnIoError() = runTest {
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(flowOf(
                        LatestTweetsResult.InProgress,
                        LatestTweetsResult.Error.Io(IOException())))

        val observer = viewModel.errorLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null, Error.COMMUNICATION)
    }

    @Test
    fun errorLiveDataEmitsServerErrorOnServerError() = runTest {
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(flowOf(
                        LatestTweetsResult.InProgress,
                        LatestTweetsResult.Error.Server))

        val observer = viewModel.errorLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null, Error.SERVER)
    }

    @Test
    fun snackbarErrorLiveDataDoesNotEmitByDefault() = runTest {
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(flowOf(LatestTweetsResult.InProgress))

        val observer = viewModel.snackbarErrorLiveData.test()
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun snackbarErrorLiveDataDoesNotEmitOnSuccess() = runTest {
        val tweets = listOf<Tweet>(mock(), mock(), mock())
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(flowOf(
                        LatestTweetsResult.InProgress,
                        LatestTweetsResult.Success(tweets)))

        val observer = viewModel.snackbarErrorLiveData.test()
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun snackbarErrorLiveDataDoesNotEmitAfterInitialError() = runTest {
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(flowOf(
                        LatestTweetsResult.InProgress,
                        LatestTweetsResult.Error.NoConnectivity))

        val observer = viewModel.snackbarErrorLiveData.test()
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun snackbarErrorLiveDataEmitsNoConnectivityWhenErrorAfterSuccess() = runTest {
        val tweets = listOf<Tweet>(mock(), mock(), mock())
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(
                        flowOf(
                                LatestTweetsResult.InProgress,
                                LatestTweetsResult.Success(tweets)),
                        flowOf(
                                LatestTweetsResult.InProgress,
                                LatestTweetsResult.Error.NoConnectivity))

        val observer = viewModel.snackbarErrorLiveData.test()
        advanceUntilIdle()
        viewModel.onSwipeToRefresh()
        advanceUntilIdle()
        val result = observer.observedValues

        assertEquals(1, result.size)
        assertEquals(Error.NO_CONNECTIVITY, result[0].peek())
    }

    @Test
    fun snackbarErrorLiveDataEmitsCommunicationErrorWhenErrorAfterSuccess() = runTest {
        val tweets = listOf<Tweet>(mock(), mock(), mock())
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(
                        flowOf(
                                LatestTweetsResult.InProgress,
                                LatestTweetsResult.Success(tweets)),
                        flowOf(
                                LatestTweetsResult.InProgress,
                                LatestTweetsResult.Error.Io(IOException())))

        val observer = viewModel.snackbarErrorLiveData.test()
        advanceUntilIdle()
        viewModel.onSwipeToRefresh()
        advanceUntilIdle()
        val result = observer.observedValues

        assertEquals(1, result.size)
        assertEquals(Error.COMMUNICATION, result[0].peek())
    }

    @Test
    fun snackbarErrorLiveDataEmitsServerErrorWhenErrorAfterSuccess() = runTest {
        val tweets = listOf<Tweet>(mock(), mock(), mock())
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(
                        flowOf(
                                LatestTweetsResult.InProgress,
                                LatestTweetsResult.Success(tweets)),
                        flowOf(
                                LatestTweetsResult.InProgress,
                                LatestTweetsResult.Error.Server))

        val observer = viewModel.snackbarErrorLiveData.test()
        advanceUntilIdle()
        viewModel.onSwipeToRefresh()
        advanceUntilIdle()
        val result = observer.observedValues

        assertEquals(1, result.size)
        assertEquals(Error.SERVER, result[0].peek())
    }

    @Test
    fun snackbarErrorLiveDataDoesNotEmitWhenErrorFollowsError() = runTest {
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(
                        flowOf(
                                LatestTweetsResult.InProgress,
                                LatestTweetsResult.Error.NoConnectivity),
                        flowOf(
                                LatestTweetsResult.InProgress,
                                LatestTweetsResult.Error.Server))

        val observer = viewModel.snackbarErrorLiveData.test()
        advanceUntilIdle()
        viewModel.onSwipeToRefresh()
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun snackbarErrorLiveDataDoesNotEmitWhenSuccessFollowsSuccess() = runTest {
        val tweets = listOf<Tweet>(mock(), mock(), mock())
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(
                        flowOf(
                                LatestTweetsResult.InProgress,
                                LatestTweetsResult.Success(tweets)),
                        flowOf(
                                LatestTweetsResult.InProgress,
                                LatestTweetsResult.Success(tweets)))

        val observer = viewModel.snackbarErrorLiveData.test()
        advanceUntilIdle()
        viewModel.onSwipeToRefresh()
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun isRefreshMenuItemEnabledLiveDataEmitsFalseWhenRefreshing() = runTest {
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(flowOf(LatestTweetsResult.InProgress))

        val observer = viewModel.isRefreshMenuItemEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isRefreshMenuItemEnabledLiveDataEmitsTrueWhenSuccess() = runTest {
        val tweets = listOf<Tweet>(mock(), mock(), mock())
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(flowOf(LatestTweetsResult.Success(tweets)))

        val observer = viewModel.isRefreshMenuItemEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(true)
    }

    @Test
    fun isRefreshMenuItemEnabledLiveDataEmitsTrueWhenError() = runTest {
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(flowOf(LatestTweetsResult.Error.NoConnectivity))

        val observer = viewModel.isRefreshMenuItemEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(true)
    }

    @Test
    fun isRefreshMenuItemRefreshingLiveDataEmitsTrueWhenRefreshing() = runTest {
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(flowOf(LatestTweetsResult.InProgress))

        val observer = viewModel.isRefreshMenuItemRefreshingLiveData.test()
        advanceUntilIdle()

        observer.assertValues(true)
    }

    @Test
    fun isRefreshMenuItemRefreshingLiveDataEmitsFalseWhenSuccess() = runTest {
        val tweets = listOf<Tweet>(mock(), mock(), mock())
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(flowOf(LatestTweetsResult.Success(tweets)))

        val observer = viewModel.isRefreshMenuItemRefreshingLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isRefreshMenuItemRefreshingLiveDataEmitsFalseWhenError() = runTest {
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(flowOf(LatestTweetsResult.Error.NoConnectivity))

        val observer = viewModel.isRefreshMenuItemRefreshingLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isSwipeToRefreshRefreshingLiveDataEmitsTrueWhenRefreshing() = runTest {
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(flowOf(LatestTweetsResult.InProgress))

        val observer = viewModel.isSwipeToRefreshRefreshingLiveData.test()
        advanceUntilIdle()

        observer.assertValues(true)
    }

    @Test
    fun isSwipeToRefreshRefreshingLiveDataEmitsFalseWhenSuccess() = runTest {
        val tweets = listOf<Tweet>(mock(), mock(), mock())
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(flowOf(LatestTweetsResult.Success(tweets)))

        val observer = viewModel.isSwipeToRefreshRefreshingLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isSwipeToRefreshRefreshingLiveDataEmitsFalseWhenError() = runTest {
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(flowOf(LatestTweetsResult.Error.NoConnectivity))

        val observer = viewModel.isSwipeToRefreshRefreshingLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun onRefreshMenuItemClickedCausesRefresh() = runTest {
        val tweets = listOf<Tweet>(mock(), mock(), mock())
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(
                        flowOf(
                                LatestTweetsResult.InProgress,
                                LatestTweetsResult.Error.NoConnectivity),
                        flowOf(
                                LatestTweetsResult.InProgress,
                                LatestTweetsResult.Success(tweets)))

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()
        viewModel.onRefreshMenuItemClicked()
        advanceUntilIdle()

        observer.assertValues(
                UiState.PROGRESS,
                UiState.ERROR,
                UiState.PROGRESS,
                UiState.CONTENT)
    }

    @Test
    fun onSwipeToRefreshCausesRefresh() = runTest {
        val tweets = listOf<Tweet>(mock(), mock(), mock())
        whenever(twitterRepository.latestTweetsFlow)
                .thenReturn(
                        flowOf(
                                LatestTweetsResult.InProgress,
                                LatestTweetsResult.Error.NoConnectivity),
                        flowOf(
                                LatestTweetsResult.InProgress,
                                LatestTweetsResult.Success(tweets)))

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()
        viewModel.onRefreshMenuItemClicked()
        advanceUntilIdle()

        observer.assertValues(
                UiState.PROGRESS,
                UiState.ERROR,
                UiState.PROGRESS,
                UiState.CONTENT)
    }
}