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

import uk.org.rivernile.android.bustracker.core.endpoints.twitter.Tweet
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

/**
 * This class maps the [JsonTweet]s returned by the endpoint in to [Tweet]s.
 *
 * @author Niall Scott
 */
class TweetsMapper @Inject constructor() {

    companion object {

        private const val TWITTER_BASE_URL = "https://twitter.com"
    }

    private val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH)

    /**
     * Map a [List] of [JsonTweet]s in to a [List] of [Tweet]s.
     *
     * @param jsonTweets The [List] of [JsonTweet]s to be mapped.
     * @return The input mapped to a [List] of [Tweet]s. If `null` is given, `null` will be
     * returned.
     */
    fun mapTweets(jsonTweets: List<JsonTweet>?) =
            jsonTweets?.mapNotNull(this::mapToTweet)
                    ?.ifEmpty { null }

    /**
     * Map a single [JsonTweet] in to a [Tweet]. If parsing fails, due to required data not being
     * present, then `null` will be returned.
     *
     * @param jsonTweet The JSON representation of the Tweet.
     * @return The JSON Tweet data mapped in to a [Tweet] object.
     */
    private fun mapToTweet(jsonTweet: JsonTweet): Tweet? {
        val time = try {
            jsonTweet.createdAt?.let(dateFormat::parse)
        } catch (ignored: ParseException) {
            null
        } ?: return null

        val body = jsonTweet.text?.let { text ->
            jsonTweet.entities?.urls?.let { urls ->
                replaceUrlsInBody(text, urls)
            } ?: text
        } ?: return null

        val user = jsonTweet.user ?: return null
        val name = user.name ?: return null
        val screenName = user.screenName ?: return null
        val profileUrl = "$TWITTER_BASE_URL/$screenName"

        return Tweet(body, name, time, user.profileImageUrl, profileUrl)
    }

    /**
     * Given a body [String] and a [List] of [JsonUrlEntity] objects, replace all URLs in the body
     * with their expanded version in the URL entities.
     *
     * @param body The Tweet body.
     * @param urls A [List] of [JsonUrlEntity] objects, which contains the short and long versions
     * of URLs.
     * @return The body with URLs expanded.
     */
    private fun replaceUrlsInBody(body: String, urls: List<JsonUrlEntity>): String {
        var result = body

        urls.forEach {
            val url = it.url
            val expandedUrl = it.expandedUrl

            if (url != null && expandedUrl != null) {
                val regex = "\\b$url\\b".toRegex()
                result = result.replace(regex, expandedUrl)
            }
        }

        return result
    }
}