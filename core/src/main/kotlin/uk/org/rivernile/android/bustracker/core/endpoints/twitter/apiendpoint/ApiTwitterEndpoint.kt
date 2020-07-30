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

package uk.org.rivernile.android.bustracker.core.endpoints.twitter.apiendpoint

import uk.org.rivernile.android.bustracker.core.di.ForApi
import uk.org.rivernile.android.bustracker.core.endpoints.api.ApiKeyGenerator
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.Tweet
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.TwitterEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.TwitterRequest
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityChecker
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class represents a [TwitterEndpoint] that actually uses the API endpoint to obtain tweets.
 *
 * @param twitterService The Retrofit service for accessing Tweets.
 * @param connectivityChecker Used to check internet connectivity prior to initiating a request.
 * @param apiKeyGenerator An implementation to generate API keys.
 * @param appName The app name to identify this app on the server.
 * @param tweetsMapper An implementation to map the response to [Tweet]s.
 * @author Niall Scott
 */
@Singleton
class ApiTwitterEndpoint @Inject constructor(
        private val twitterService: TwitterService,
        private val connectivityChecker: ConnectivityChecker,
        private val apiKeyGenerator: ApiKeyGenerator,
        @ForApi private val appName: String,
        private val tweetsMapper: TweetsMapper) : TwitterEndpoint {

    override fun createLatestTweetsRequest(): TwitterRequest<List<Tweet>?> {
        val hashedApiKey = apiKeyGenerator.generateHashedApiKey()
        val call = twitterService.getLatestTweets(hashedApiKey, appName)

        return LatestTweetsTwitterRequest(call, connectivityChecker, tweetsMapper)
    }
}