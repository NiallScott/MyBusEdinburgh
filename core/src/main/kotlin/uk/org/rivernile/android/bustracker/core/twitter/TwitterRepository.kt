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

package uk.org.rivernile.android.bustracker.core.twitter

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.LatestTweetsResponse
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.Tweet
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.TwitterEndpoint
import javax.inject.Inject

/**
 * This repository is used to access Tweets.
 *
 * @param twitterEndpoint The endpoint to receive [Tweet]s from.
 * @author Niall Scott
 */
class TwitterRepository @Inject internal constructor(
        private val twitterEndpoint: TwitterEndpoint) {

    /**
     * A [Flow] object which emits the [LatestTweetsResult] of loading the latest [Tweet]s.
     *
     * This instance will have loading events propagated to it, including the in-progress state,
     * success state and the error states.
     */
    val latestTweetsFlow: Flow<LatestTweetsResult> get() = flow {
        emit(LatestTweetsResult.InProgress)
        emit(fetchLatestTweets())
    }

    /**
     * This suspending function fetches the latest [Tweet]s and returns the appropriate
     * [LatestTweetsResult] object.
     *
     * @return A [LatestTweetsResult] object encapsulating the result of the request.
     */
    private suspend fun fetchLatestTweets(): LatestTweetsResult {
        return when (val response = twitterEndpoint.getLatestTweets()) {
            is LatestTweetsResponse.Success -> LatestTweetsResult.Success(response.tweets)
            is LatestTweetsResponse.Error.NoConnectivity -> LatestTweetsResult.Error.NoConnectivity
            is LatestTweetsResponse.Error.Io -> LatestTweetsResult.Error.Io(response.throwable)
            else -> LatestTweetsResult.Error.Server
        }
    }
}