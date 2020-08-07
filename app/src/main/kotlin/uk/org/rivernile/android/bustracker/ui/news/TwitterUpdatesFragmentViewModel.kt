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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.AuthenticationException
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.NetworkException
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.NoConnectivityException
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.Tweet
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.TwitterException
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.UnrecognisedServerErrorException
import uk.org.rivernile.android.bustracker.core.twitter.Result
import uk.org.rivernile.android.bustracker.core.twitter.TwitterRepository
import javax.inject.Inject

/**
 * This [ViewModel] is used by [TwitterUpdatesFragment].
 *
 * @param twitterRepository The repository which is used to obtain [Tweet]s.
 * @author Niall Scott
 */
class TwitterUpdatesFragmentViewModel @Inject constructor(
        private val twitterRepository: TwitterRepository): ViewModel() {

    private val refreshTweets = MutableLiveData(Unit)
    private val loadTweets = refreshTweets.switchMap {
        twitterRepository.getLatestTweets().asLiveData()
    }

    /**
     * This [LiveData] is used to expose the current UI state.
     */
    val uiStateLiveData: LiveData<UiState> by lazy {
        loadTweets.map {
            calculateNewUiState(uiStateLiveData.value, it)
        }
    }

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
        refreshTweets.value = Unit
    }

    /**
     * Given the previous [UiState] and the newly loaded [Result], calculate the new state which
     * should be applied on the UI.
     *
     * @param oldState The old [UiState] to base the calculation on.
     * @param result The result of loading the latest data.
     * @return The new [UiState] which should be applied.
     */
    private fun calculateNewUiState(oldState: UiState?, result: Result<List<Tweet>?>): UiState {
        return when (result) {
            is Result.InProgress -> calculateInProgressState(oldState)
            is Result.Success -> calculateSuccessState(oldState, result.result)
            is Result.Error -> calculateErrorState(oldState, result.exception)
        }
    }

    /**
     * Given a [Result] which represents an in-progress request, calculate the new UI state.
     *
     * @param oldState The old state to base the calculation on to arrive at the new state.
     * @return The new [UiState].
     */
    private fun calculateInProgressState(oldState: UiState?): UiState {
        return if (oldState is UiState.ShowPopulatedProgress ||
                oldState is UiState.ShowContent ||
                oldState is UiState.ShowRefreshError) {
            UiState.ShowPopulatedProgress
        } else {
            UiState.ShowEmptyProgress
        }
    }

    /**
     * Given a [Result] which represents a successful request, calculate the new UI state.
     *
     * @param oldState The old state to base the calculation on to arrive at the new state.
     * @param tweets The newly loaded [List] of [Tweet]s.
     * @return The new [UiState].
     */
    private fun calculateSuccessState(oldState: UiState?, tweets: List<Tweet>?): UiState {
        return if (tweets?.isNotEmpty() == true) {
            UiState.ShowContent(tweets)
        } else {
            if (oldState is UiState.ShowContent ||
                    oldState is UiState.ShowPopulatedProgress ||
                    oldState is UiState.ShowRefreshError) {
                UiState.ShowRefreshError(Error.NO_DATA)
            } else {
                UiState.ShowEmptyError(Error.NO_DATA)
            }
        }
    }

    /**
     * Given a [Result] which represents a failed request, calculate the new UI state.
     *
     * @param oldState The old state to base the calculation on to arrive at the new state.
     * @param error The error which caused this state.
     * @return The new [UiState].
     */
    private fun calculateErrorState(oldState: UiState?, error: TwitterException): UiState {
        return mapTwitterExceptionToError(error).let {
            if (oldState is UiState.ShowContent ||
                    oldState is UiState.ShowPopulatedProgress ||
                    oldState is UiState.ShowRefreshError) {
                UiState.ShowRefreshError(it)
            } else {
                UiState.ShowEmptyError(it)
            }
        }
    }

    /**
     * Given a [TwitterException], map it to an [Error].
     *
     * @param error The [TwitterException] to be mapped.
     * @return The [Error] representing the [TwitterException].
     */
    private fun mapTwitterExceptionToError(error: TwitterException) = when (error) {
        is NoConnectivityException -> Error.NO_CONNECTIVITY
        is NetworkException -> Error.COMMUNICATION
        is UnrecognisedServerErrorException -> Error.SERVER
        is AuthenticationException -> Error.SERVER
    }
}