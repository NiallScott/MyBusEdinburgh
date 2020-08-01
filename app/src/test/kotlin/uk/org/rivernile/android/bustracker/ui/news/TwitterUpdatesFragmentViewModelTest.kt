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

package uk.org.rivernile.android.bustracker.ui.news

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
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
import java.io.IOException

/**
 * Tests for [TwitterUpdatesFragmentViewModel].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class TwitterUpdatesFragmentViewModelTest {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var twitterRepository: TwitterRepository

    @Mock
    private lateinit var uiStateObserver: Observer<UiState>
    private val tweets = listOf(mock<Tweet>())

    private lateinit var viewModel: TwitterUpdatesFragmentViewModel

    @Before
    fun setUp() {
        viewModel = TwitterUpdatesFragmentViewModel(twitterRepository)
    }

    @Test
    fun initialStateBeginsLoadingTweets() {
        val latestTweetsLiveData = MutableLiveData<Result<List<Tweet>?>>(Result.InProgress)
        whenever(twitterRepository.getLatestTweets())
                .thenReturn(latestTweetsLiveData)

        viewModel.uiStateLiveData.observeForever(uiStateObserver)

        verify(uiStateObserver)
                .onChanged(UiState.ShowEmptyProgress)
    }

    @Test
    fun errorAfterInitialStateShowsEmptyErrorState() {
        val latestTweetsLiveData = MutableLiveData<Result<List<Tweet>?>>(Result.InProgress)
        whenever(twitterRepository.getLatestTweets())
                .thenReturn(latestTweetsLiveData)

        viewModel.uiStateLiveData.observeForever(uiStateObserver)
        latestTweetsLiveData.value = Result.Error(NoConnectivityException())

        inOrder(uiStateObserver) {
            verify(uiStateObserver)
                    .onChanged(UiState.ShowEmptyProgress)
            verify(uiStateObserver)
                    .onChanged(UiState.ShowEmptyError(Error.NO_CONNECTIVITY))
        }
    }

    @Test
    fun nullSuccessAfterInitialStateShowsEmptyErrorState() {
        val latestTweetsLiveData = MutableLiveData<Result<List<Tweet>?>>(Result.InProgress)
        whenever(twitterRepository.getLatestTweets())
                .thenReturn(latestTweetsLiveData)

        viewModel.uiStateLiveData.observeForever(uiStateObserver)
        latestTweetsLiveData.value = Result.Success(null)

        inOrder(uiStateObserver) {
            verify(uiStateObserver)
                    .onChanged(UiState.ShowEmptyProgress)
            verify(uiStateObserver)
                    .onChanged(UiState.ShowEmptyError(Error.NO_DATA))
        }
    }

    @Test
    fun emptySuccessAfterInitialStateShowsEmptyErrorState() {
        val latestTweetsLiveData = MutableLiveData<Result<List<Tweet>?>>(Result.InProgress)
        whenever(twitterRepository.getLatestTweets())
                .thenReturn(latestTweetsLiveData)

        viewModel.uiStateLiveData.observeForever(uiStateObserver)
        latestTweetsLiveData.value = Result.Success(null)

        inOrder(uiStateObserver) {
            verify(uiStateObserver)
                    .onChanged(UiState.ShowEmptyProgress)
            verify(uiStateObserver)
                    .onChanged(UiState.ShowEmptyError(Error.NO_DATA))
        }
    }

    @Test
    fun nonEmptySuccessAfterInitialStateShowsContentState() {
        val latestTweetsLiveData = MutableLiveData<Result<List<Tweet>?>>(Result.InProgress)
        whenever(twitterRepository.getLatestTweets())
                .thenReturn(latestTweetsLiveData)

        viewModel.uiStateLiveData.observeForever(uiStateObserver)
        latestTweetsLiveData.value = Result.Success(tweets)

        inOrder(uiStateObserver) {
            verify(uiStateObserver)
                    .onChanged(UiState.ShowEmptyProgress)
            verify(uiStateObserver)
                    .onChanged(UiState.ShowContent(tweets))
        }
    }

    @Test
    fun successiveErrorsAfterInitialStateContinuesToShowEmptyErrorState() {
        val latestTweetsLiveData1 = MutableLiveData<Result<List<Tweet>?>>(Result.InProgress)
        val latestTweetsLiveData2 = MutableLiveData<Result<List<Tweet>?>>(Result.InProgress)
        val latestTweetsLiveData3 = MutableLiveData<Result<List<Tweet>?>>(Result.InProgress)
        val latestTweetsLiveData4 = MutableLiveData<Result<List<Tweet>?>>(Result.InProgress)
        whenever(twitterRepository.getLatestTweets())
                .thenReturn(latestTweetsLiveData1, latestTweetsLiveData2, latestTweetsLiveData3,
                        latestTweetsLiveData4)

        viewModel.uiStateLiveData.observeForever(uiStateObserver)
        latestTweetsLiveData1.value = Result.Error(NoConnectivityException())
        viewModel.onRefreshMenuItemClicked()
        latestTweetsLiveData2.value = Result.Error(NetworkException(IOException()))
        viewModel.onSwipeToRefresh()
        latestTweetsLiveData3.value = Result.Error(UnrecognisedServerErrorException())
        viewModel.onRefreshMenuItemClicked()
        latestTweetsLiveData4.value = Result.Error(AuthenticationException())

        inOrder(uiStateObserver) {
            verify(uiStateObserver)
                    .onChanged(UiState.ShowEmptyProgress)
            verify(uiStateObserver)
                    .onChanged(UiState.ShowEmptyError(Error.NO_CONNECTIVITY))
            verify(uiStateObserver)
                    .onChanged(UiState.ShowEmptyProgress)
            verify(uiStateObserver)
                    .onChanged(UiState.ShowEmptyError(Error.COMMUNICATION))
            verify(uiStateObserver)
                    .onChanged(UiState.ShowEmptyProgress)
            verify(uiStateObserver)
                    .onChanged(UiState.ShowEmptyError(Error.SERVER))
            verify(uiStateObserver)
                    .onChanged(UiState.ShowEmptyProgress)
            verify(uiStateObserver)
                    .onChanged(UiState.ShowEmptyError(Error.SERVER))
        }
    }

    @Test
    fun errorAfterContentShownShowsRefreshErrorState() {
        val latestTweetsLiveData1 = MutableLiveData<Result<List<Tweet>?>>(Result.InProgress)
        val latestTweetsLiveData2 = MutableLiveData<Result<List<Tweet>?>>(Result.InProgress)
        whenever(twitterRepository.getLatestTweets())
                .thenReturn(latestTweetsLiveData1, latestTweetsLiveData2)

        viewModel.uiStateLiveData.observeForever(uiStateObserver)
        latestTweetsLiveData1.value = Result.Success(tweets)
        viewModel.onRefreshMenuItemClicked()
        latestTweetsLiveData2.value = Result.Error(NoConnectivityException())

        inOrder(uiStateObserver) {
            verify(uiStateObserver)
                    .onChanged(UiState.ShowEmptyProgress)
            verify(uiStateObserver)
                    .onChanged(UiState.ShowContent(tweets))
            verify(uiStateObserver)
                    .onChanged(UiState.ShowPopulatedProgress)
            verify(uiStateObserver)
                    .onChanged(UiState.ShowRefreshError(Error.NO_CONNECTIVITY))
        }
    }

    @Test
    fun noDataErrorAfterContentShownContinuesToShowContentAndDisplaysError() {
        val latestTweetsLiveData1 = MutableLiveData<Result<List<Tweet>?>>(Result.InProgress)
        val latestTweetsLiveData2 = MutableLiveData<Result<List<Tweet>?>>(Result.InProgress)
        whenever(twitterRepository.getLatestTweets())
                .thenReturn(latestTweetsLiveData1, latestTweetsLiveData2)

        viewModel.uiStateLiveData.observeForever(uiStateObserver)
        latestTweetsLiveData1.value = Result.Success(tweets)
        viewModel.onRefreshMenuItemClicked()
        latestTweetsLiveData2.value = Result.Success(null)

        inOrder(uiStateObserver) {
            verify(uiStateObserver)
                    .onChanged(UiState.ShowEmptyProgress)
            verify(uiStateObserver)
                    .onChanged(UiState.ShowContent(tweets))
            verify(uiStateObserver)
                    .onChanged(UiState.ShowPopulatedProgress)
            verify(uiStateObserver)
                    .onChanged(UiState.ShowRefreshError(Error.NO_DATA))
        }
    }

    @Test
    fun successAfterSuccessContinuesToShowSuccess() {
        val latestTweetsLiveData1 = MutableLiveData<Result<List<Tweet>?>>(Result.InProgress)
        val latestTweetsLiveData2 = MutableLiveData<Result<List<Tweet>?>>(Result.InProgress)
        whenever(twitterRepository.getLatestTweets())
                .thenReturn(latestTweetsLiveData1, latestTweetsLiveData2)

        viewModel.uiStateLiveData.observeForever(uiStateObserver)
        latestTweetsLiveData1.value = Result.Success(tweets)
        viewModel.onRefreshMenuItemClicked()
        latestTweetsLiveData2.value = Result.Success(tweets)

        inOrder(uiStateObserver) {
            verify(uiStateObserver)
                    .onChanged(UiState.ShowEmptyProgress)
            verify(uiStateObserver)
                    .onChanged(UiState.ShowContent(tweets))
            verify(uiStateObserver)
                    .onChanged(UiState.ShowPopulatedProgress)
            verify(uiStateObserver)
                    .onChanged(UiState.ShowContent(tweets))
        }
    }
}