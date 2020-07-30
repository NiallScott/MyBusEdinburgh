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

import retrofit2.Call
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.AuthenticationException
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.NetworkException
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.NoConnectivityException
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.Tweet
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.TwitterRequest
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.UnrecognisedServerErrorException
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityChecker
import java.io.IOException

/**
 * This [TwitterRequest] provides a way to obtain the latest [Tweet]s from the endpoint.
 *
 * @param call The Retrofit [Call] which performs the request.
 * @param connectivityChecker Used to check the current device connectivity prior to initiating the
 * request.
 * @param mapper The implementation used to map the server data in to our domain objects.
 * @author Niall Scott
 */
internal class LatestTweetsTwitterRequest(
        private val call: Call<List<JsonTweet>>,
        private val connectivityChecker: ConnectivityChecker,
        private val mapper: TweetsMapper) : TwitterRequest<List<Tweet>?> {

    override fun performRequest() = if (connectivityChecker.hasInternetConnectivity()) {
        try {
            val response = call.execute()

            if (response.isSuccessful) {
                mapper.mapTweets(response.body())
            } else {
                when (response.code()) {
                    401 -> throw AuthenticationException("Incorrect API key.")
                    else -> throw UnrecognisedServerErrorException()
                }
            }
        } catch (e: IOException) {
            throw NetworkException(e)
        }
    } else {
        throw NoConnectivityException()
    }

    override fun cancel() {
        call.cancel()
    }
}