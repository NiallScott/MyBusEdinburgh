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

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.Tweet
import uk.org.rivernile.android.bustracker.core.twitter.LatestTweetsResult
import uk.org.rivernile.android.bustracker.core.twitter.TwitterRepository
import uk.org.rivernile.android.bustracker.core.utils.TimeUtils
import uk.org.rivernile.android.bustracker.utils.Event
import javax.inject.Inject

/**
 * This [ViewModel] is used by [TwitterUpdatesFragment].
 *
 * @param twitterRepository The repository which is used to obtain [Tweet]s.
 * @param timeUtils Time utilities.
 * @param defaultDispatcher The default [CoroutineDispatcher].
 * @author Niall Scott
 */
class TwitterUpdatesFragmentViewModel @Inject constructor(
        private val twitterRepository: TwitterRepository,
        private val timeUtils: TimeUtils,
        @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher)
    : ViewModel() {

    private val refreshTweetsFlow = MutableStateFlow(-1L)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val tweetDataFlow = refreshTweetsFlow
            .flatMapLatest { twitterRepository.latestTweetsFlow }
            .flowOn(defaultDispatcher)
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 1)

    private val tweetsFlow = tweetDataFlow
            .filterIsInstance<LatestTweetsResult.Success>()
            .map { it.tweets?.ifEmpty { null } }
            .onStart { emit(null) }
            .distinctUntilChanged()
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 1)

    private val uiStateFlow = tweetDataFlow
            .combine(tweetsFlow, this::calculateUiState)
            .distinctUntilChanged()
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 1)

    /**
     * This [LiveData] emits the current tweet data.
     */
    val tweetsLiveData = tweetsFlow.asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits the current [UiState].
     */
    val uiStateLiveData = uiStateFlow.asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits the current error.
     */
    val errorLiveData = tweetDataFlow
            .combine(tweetsFlow, this::calculateError)
            .onStart { emit(null) }
            .distinctUntilChanged()
            .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits the error to be shown by the snackbar.
     */
    val snackbarErrorLiveData = tweetDataFlow
            .combine(uiStateFlow, this::calculateSnackbarError)
            .filterNotNull()
            .map { Event(it) }
            .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits the enabled state of the refresh menu item.
     */
    val isRefreshMenuItemEnabledLiveData = tweetDataFlow
            .map { it !is LatestTweetsResult.InProgress }
            .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits the refresh state of the refresh menu item.
     */
    val isRefreshMenuItemRefreshingLiveData = tweetDataFlow
            .map { it is LatestTweetsResult.InProgress }
            .asLiveData(viewModelScope.coroutineContext)

    /**
     * This [LiveData] emits the current refreshing state of swipe to refresh.
     */
    val isSwipeToRefreshRefreshingLiveData = tweetDataFlow
            .map { it is LatestTweetsResult.InProgress }
            .asLiveData(viewModelScope.coroutineContext)

    /**
     * Called when the refresh menu item has been clicked.
     */
    fun onRefreshMenuItemClicked() {
        refreshTweets()
    }

    /**
     * Called when the user invokes a refresh from pull-to-refresh.
     */
    fun onSwipeToRefresh() {
        refreshTweets()
    }

    /**
     * Causes a refresh to happen.
     */
    private fun refreshTweets() {
        refreshTweetsFlow.value = timeUtils.getCurrentTimeMillis()
    }

    /**
     * Calculate the overall [UiState].
     *
     * @param tweetData The current [LatestTweetsResult].
     * @param tweets The current [List] of [Tweet]s.
     */
    private fun calculateUiState(tweetData: LatestTweetsResult, tweets: List<Tweet>?): UiState {
        return when  {
            tweets != null -> UiState.CONTENT
            tweetData is LatestTweetsResult.Error -> UiState.ERROR
            tweetData is LatestTweetsResult.Success &&
                    tweetData.tweets?.ifEmpty { null } == null -> UiState.ERROR
            else -> UiState.PROGRESS
        }
    }

    /**
     * Calculate the error to show to the user.
     *
     * @param tweetData The current [LatestTweetsResult].
     * @param tweets The current [List] of [Tweet]s.
     * @return The [Error] to be shown to the user, or `null` if no error is to be shown.
     */
    private fun calculateError(tweetData: LatestTweetsResult, tweets: List<Tweet>?): Error? {
        return when {
            tweetData is LatestTweetsResult.Success && tweets == null -> Error.NO_DATA
            tweetData is LatestTweetsResult.Error.NoConnectivity -> Error.NO_CONNECTIVITY
            tweetData is LatestTweetsResult.Error.Io -> Error.COMMUNICATION
            tweetData is LatestTweetsResult.Error.Server -> Error.SERVER
            else -> null
        }
    }

    /**
     * Calculate the error which should be shown by the snackbar. `null` means no error should be
     * shown.
     *
     * @param tweetData The current [LatestTweetsResult].
     * @param uiState The current [UiState].
     * @return The [Error] to be shown by the snackbar, or `null` if no error is to be shown.
     */
    private fun calculateSnackbarError(
            tweetData: LatestTweetsResult,
            uiState: UiState): Error? {
        return if (uiState == UiState.CONTENT) {
            when (tweetData) {
                is LatestTweetsResult.Error.NoConnectivity -> Error.NO_CONNECTIVITY
                is LatestTweetsResult.Error.Io -> Error.COMMUNICATION
                is LatestTweetsResult.Error.Server -> Error.SERVER
                else -> null
            }
        } else {
            null
        }
    }
}