/*
 * Copyright (C) 2020 - 2022 Niall 'Rivernile' Scott
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
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.AuthenticationException
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.NetworkException
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.NoConnectivityException
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.Tweet
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.UnrecognisedServerErrorException
import uk.org.rivernile.android.bustracker.core.twitter.Result
import uk.org.rivernile.android.bustracker.core.twitter.TwitterRepository
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.testutils.test
import java.io.IOException

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

    private val tweets = listOf(mock<Tweet>())

    private lateinit var viewModel: TwitterUpdatesFragmentViewModel

    @Before
    fun setUp() {
        viewModel = TwitterUpdatesFragmentViewModel(twitterRepository)
    }

    @Test
    fun initialStateBeginsLoadingTweets() = runTest {
        whenever(twitterRepository.getLatestTweets())
                .thenReturn(flowOf(Result.InProgress))

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(UiState.ShowEmptyProgress)
    }

    @Test
    fun errorAfterInitialStateShowsEmptyErrorState() = runTest {
        whenever(twitterRepository.getLatestTweets())
                .thenReturn(flowOf(
                        Result.InProgress,
                        Result.Error(NoConnectivityException())))

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
                UiState.ShowEmptyProgress,
                UiState.ShowEmptyError(Error.NO_CONNECTIVITY))
    }

    @Test
    fun nullSuccessAfterInitialStateShowsEmptyErrorState() = runTest {
        whenever(twitterRepository.getLatestTweets())
                .thenReturn(flowOf(
                        Result.InProgress,
                        Result.Success(null)))

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
                UiState.ShowEmptyProgress,
                UiState.ShowEmptyError(Error.NO_DATA))
    }

    @Test
    fun emptySuccessAfterInitialStateShowsEmptyErrorState() = runTest {
        whenever(twitterRepository.getLatestTweets())
                .thenReturn(flowOf(
                        Result.InProgress,
                        Result.Success(emptyList())))

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
                UiState.ShowEmptyProgress,
                UiState.ShowEmptyError(Error.NO_DATA))
    }

    @Test
    fun nonEmptySuccessAfterInitialStateShowsContentState() = runTest {
        whenever(twitterRepository.getLatestTweets())
                .thenReturn(flowOf(
                        Result.InProgress,
                        Result.Success(tweets)))

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
                UiState.ShowEmptyProgress,
                UiState.ShowContent(tweets))
    }

    @Test
    fun successiveErrorsAfterInitialStateContinuesToShowEmptyErrorState() = runTest {
        val flow1 = flowOf(
                Result.InProgress,
                Result.Error(NoConnectivityException()))
        val flow2 = flowOf(
                Result.InProgress,
                Result.Error(NetworkException(IOException())))
        val flow3 = flowOf(
                Result.InProgress,
                Result.Error(UnrecognisedServerErrorException()))
        val flow4 = flowOf(
                Result.InProgress,
                Result.Error(AuthenticationException()))
        whenever(twitterRepository.getLatestTweets())
                .thenReturn(flow1, flow2, flow3, flow4)

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()
        viewModel.onRefreshMenuItemClicked()
        advanceUntilIdle()
        viewModel.onSwipeToRefresh()
        advanceUntilIdle()
        viewModel.onRefreshMenuItemClicked()
        advanceUntilIdle()

        observer.assertValues(
                UiState.ShowEmptyProgress,
                UiState.ShowEmptyError(Error.NO_CONNECTIVITY),
                UiState.ShowEmptyProgress,
                UiState.ShowEmptyError(Error.COMMUNICATION),
                UiState.ShowEmptyProgress,
                UiState.ShowEmptyError(Error.SERVER),
                UiState.ShowEmptyProgress,
                UiState.ShowEmptyError(Error.SERVER))
    }

    @Test
    fun errorAfterContentShownShowsRefreshErrorState() = runTest {
        val flow1 = flowOf(
                Result.InProgress,
                Result.Success(tweets))
        val flow2 = flowOf(
                Result.InProgress,
                Result.Error(NoConnectivityException()))
        whenever(twitterRepository.getLatestTweets())
                .thenReturn(flow1, flow2)

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()
        viewModel.onRefreshMenuItemClicked()
        advanceUntilIdle()

        observer.assertValues(
                UiState.ShowEmptyProgress,
                UiState.ShowContent(tweets),
                UiState.ShowPopulatedProgress,
                UiState.ShowRefreshError(Error.NO_CONNECTIVITY))
    }

    @Test
    fun noDataErrorAfterContentShownContinuesToShowContentAndDisplaysError() = runTest {
        val flow1 = flowOf(
                Result.InProgress,
                Result.Success(tweets))
        val flow2 = flowOf(
                Result.InProgress,
                Result.Success(null))
        whenever(twitterRepository.getLatestTweets())
                .thenReturn(flow1, flow2)

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()
        viewModel.onRefreshMenuItemClicked()
        advanceUntilIdle()

        observer.assertValues(
                UiState.ShowEmptyProgress,
                UiState.ShowContent(tweets),
                UiState.ShowPopulatedProgress,
                UiState.ShowRefreshError(Error.NO_DATA))
    }

    @Test
    fun successAfterSuccessContinuesToShowSuccess() = runTest {
        val flow1 = flowOf(
                Result.InProgress,
                Result.Success(tweets))
        val flow2 = flowOf(
                Result.InProgress,
                Result.Success(tweets))
        whenever(twitterRepository.getLatestTweets())
                .thenReturn(flow1, flow2)

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()
        viewModel.onRefreshMenuItemClicked()
        advanceUntilIdle()

        observer.assertValues(
                UiState.ShowEmptyProgress,
                UiState.ShowContent(tweets),
                UiState.ShowPopulatedProgress,
                UiState.ShowContent(tweets))
    }
}