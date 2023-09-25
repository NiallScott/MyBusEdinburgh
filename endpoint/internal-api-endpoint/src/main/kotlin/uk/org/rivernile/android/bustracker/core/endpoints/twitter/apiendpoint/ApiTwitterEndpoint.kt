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

package uk.org.rivernile.android.bustracker.core.endpoints.twitter.apiendpoint

import kotlinx.serialization.SerializationException
import okio.IOException
import uk.org.rivernile.android.bustracker.core.endpoints.api.di.ForInternalApiAppName
import uk.org.rivernile.android.bustracker.core.endpoints.api.ApiKeyGenerator
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.LatestTweetsResponse
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.Tweet
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.TwitterEndpoint
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class represents a [TwitterEndpoint] that actually uses the API endpoint to obtain tweets.
 *
 * @param twitterService The Retrofit service for accessing Tweets.
 * @param connectivityRepository Used to check internet connectivity prior to initiating a request.
 * @param apiKeyGenerator An implementation to generate API keys.
 * @param appName The app name to identify this app on the server.
 * @param tweetsMapper An implementation to map the response to [Tweet]s.
 * @param exceptionLogger Used to log exceptions.
 * @author Niall Scott
 */
@Singleton
internal class ApiTwitterEndpoint @Inject constructor(
    private val twitterService: TwitterService,
    private val connectivityRepository: ConnectivityRepository,
    private val apiKeyGenerator: ApiKeyGenerator,
    @ForInternalApiAppName private val appName: String,
    private val tweetsMapper: TweetsMapper,
    private val exceptionLogger: ExceptionLogger) : TwitterEndpoint {

    override suspend fun getLatestTweets(): LatestTweetsResponse {
        return if (connectivityRepository.hasInternetConnectivity) {
            try {
                val response = twitterService.getLatestTweets(
                    apiKeyGenerator.generateHashedApiKey(),
                    appName)

                if (response.isSuccessful) {
                    LatestTweetsResponse.Success(tweetsMapper.mapTweets(response.body()))
                } else {
                    when (response.code()) {
                        401 -> LatestTweetsResponse.Error.Authentication
                        else -> LatestTweetsResponse.Error.UnrecognisedServerError
                    }
                }
            } catch (e: IOException) {
                exceptionLogger.log(e)
                LatestTweetsResponse.Error.Io(e)
            } catch (e: SerializationException) {
                exceptionLogger.log(e)
                LatestTweetsResponse.Error.UnrecognisedServerError
            }
        } else {
            LatestTweetsResponse.Error.NoConnectivity
        }
    }
}