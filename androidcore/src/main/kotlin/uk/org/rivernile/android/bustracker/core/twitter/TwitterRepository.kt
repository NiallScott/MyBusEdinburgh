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

package uk.org.rivernile.android.bustracker.core.twitter

import androidx.lifecycle.liveData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import uk.org.rivernile.android.bustracker.core.di.ForIoDispatcher
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.Tweet
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.TwitterEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.TwitterException
import javax.inject.Inject

/**
 * The [TwitterRepository] is used to access Tweets.
 *
 * @param twitterEndpoint The endpoint to receive [Tweet]s from.
 * @param ioDispatcher The [CoroutineDispatcher] to perform IO operations on.
 * @author Niall Scott
 */
class TwitterRepository @Inject constructor(
        private val twitterEndpoint: TwitterEndpoint,
        @ForIoDispatcher private val ioDispatcher: CoroutineDispatcher) {

    /**
     * Get a [androidx.lifecycle.LiveData] object which contains the [Result] of loading the latest
     * [Tweet]s.
     *
     * This instance will have loading events propagated to it, including the in-progress state,
     * success state and the error states.
     *
     * @return A [androidx.lifecycle.LiveData] object containing the [Result] of loading the latest
     * [Tweet]s.
     */
    fun getLatestTweets() = liveData {
        emit(Result.InProgress)
        emit(fetchLatestTweets())
    }

    /**
     * This suspending function, executed on the IO [CoroutineDispatcher], fetches the latest
     * [Tweet]s and returns the appropriate [Result] object.
     *
     * @return A [Result] object encapsulating the result of the request.
     */
    private suspend fun fetchLatestTweets(): Result<List<Tweet>?> = withContext(ioDispatcher) {
        val request = twitterEndpoint.createLatestTweetsRequest()

        try {
            Result.Success(request.performRequest())
        } catch (e: TwitterException) {
            Result.Error(e)
        } catch (e: CancellationException) {
            request.cancel()
            throw e
        }
    }
}