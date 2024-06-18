/*
 * Copyright (C) 2020 - 2024 Niall 'Rivernile' Scott
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
import uk.org.rivernile.android.bustracker.core.log.FakeExceptionLogger
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [TweetsMapper].
 *
 * @author Niall Scott
 */
class TweetsMapperTest {

    companion object {

        private const val TEST_TIMESTAMP = "Wed Jan 22 20:25:18 +0000 2014"
        private const val TEST_BODY = "Tweet text"
        private const val TEST_USER = "A user"
        private const val TEST_PROFILE_IMAGE_URL = "http://a.com/image.png"
        private const val TEST_SCREEN_NAME = "Auser"
        private const val TEST_PROFILE_URL = "https://twitter.com/Auser"

        private val DATE_FORMAT = SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH)
        private val TEST_TIME = DATE_FORMAT.parse(TEST_TIMESTAMP)
    }

    private lateinit var mapper: TweetsMapper

    @BeforeTest
    fun setUp() {
        mapper = TweetsMapper(FakeExceptionLogger())
    }

    @Test
    fun mapTweetsWithNullJsonTweetsReturnsNull() {
        val result = mapper.mapTweets(null)

        assertNull(result)
    }

    @Test
    fun mapTweetsWithEmptyJsonTweetsReturnsNull() {
        val result = mapper.mapTweets(emptyList())

        assertNull(result)
    }

    @Test
    fun mapTweetsWithSingleTweetWithNullTimeDoesNotGetMapped() {
        val input = JsonTweet(
            null,
            TEST_BODY,
            null,
            JsonUser(
                TEST_USER,
                TEST_PROFILE_IMAGE_URL,
                TEST_SCREEN_NAME
            )
        )

        val result = mapper.mapTweets(listOf(input))

        assertNull(result)
    }

    @Test
    fun mapTweetsWithSingleTweetWithIncorrectlyFormattedTimeDoesNotGetMapped() {
        val input = JsonTweet(
            "",
            TEST_BODY,
            null,
            JsonUser(
                TEST_USER,
                TEST_PROFILE_IMAGE_URL,
                TEST_SCREEN_NAME
            )
        )

        val result = mapper.mapTweets(listOf(input))

        assertNull(result)
    }

    @Test
    fun mapTweetsWithSingleTweetWithNullTextDoesNotGetMapped() {
        val input = JsonTweet(
            TEST_TIMESTAMP,
            null,
            null,
            JsonUser(
                TEST_USER,
                TEST_PROFILE_IMAGE_URL,
                TEST_SCREEN_NAME
            )
        )

        val result = mapper.mapTweets(listOf(input))

        assertNull(result)
    }

    @Test
    fun mapTweetsWithSingleTweetWithNullUserDoesNotGetMapped() {
        val input = JsonTweet(
            TEST_TIMESTAMP,
            TEST_BODY,
            null,
            null
        )

        val result = mapper.mapTweets(listOf(input))

        assertNull(result)
    }

    @Test
    fun mapTweetsWithSingleTweetWithNullNameDoesNotGetMapped() {
        val input = JsonTweet(
            TEST_TIMESTAMP,
            TEST_BODY,
            null,
            JsonUser(
                null,
                TEST_PROFILE_IMAGE_URL,
                TEST_SCREEN_NAME
            )
        )

        val result = mapper.mapTweets(listOf(input))

        assertNull(result)
    }

    @Test
    fun mapTweetsWithSingleTweetWithNullScreenNameDoesNotGetMapped() {
        val input = JsonTweet(
            TEST_TIMESTAMP,
            TEST_BODY,
            null,
            JsonUser(
                TEST_USER,
                TEST_PROFILE_IMAGE_URL,
                null
            )
        )

        val result = mapper.mapTweets(listOf(input))

        assertNull(result)
    }

    @Test
    fun mapTweetsWithSingleValidTweetIsMapped() {
        val input = JsonTweet(
            TEST_TIMESTAMP,
            TEST_BODY,
            null,
            JsonUser(
                TEST_USER,
                TEST_PROFILE_IMAGE_URL,
                TEST_SCREEN_NAME
            )
        )
        val expected = listOf(
            Tweet(
                TEST_BODY,
                TEST_USER,
                TEST_TIME,
                TEST_PROFILE_IMAGE_URL,
                TEST_PROFILE_URL
            )
        )

        val result = mapper.mapTweets(listOf(input))

        assertEquals(expected, result)
    }

    @Test
    fun mapTweetsWithSingleTweetWithMissingProfileImageUrlIsMapped() {
        val input = JsonTweet(
            TEST_TIMESTAMP,
            TEST_BODY,
            null,
            JsonUser(
                TEST_USER,
                null,
                TEST_SCREEN_NAME
            )
        )
        val expected = listOf(
            Tweet(
                TEST_BODY,
                TEST_USER,
                TEST_TIME,
                null,
                TEST_PROFILE_URL
            )
        )

        val result = mapper.mapTweets(listOf(input))

        assertEquals(expected, result)
    }

    @Test
    fun mapTweetsWithMultipleTweetsWithOneInvalidMapsValidTweets() {
        val timestamps = arrayOf(
            "Wed Jan 22 20:25:18 +0000 2014",
            "Wed Jan 22 20:23:18 +0000 2014",
            "Wed Jan 22 20:21:18 +0000 2014"
        )
        val times = timestamps.map(DATE_FORMAT::parse)
        val input = listOf(
            JsonTweet(
                timestamps[0],
                "Body 1",
                null,
                JsonUser(
                    "User 1",
                    null,
                    "User1"
                )
            ),
            JsonTweet(
                timestamps[1],
                null,
                null,
                JsonUser(
                    "User 2",
                    null,
                    "User2"
                )
            ),
            JsonTweet(
                timestamps[2],
                "Body 3",
                null,
                JsonUser(
                    "User 3",
                    null,
                    "User3"
                )
            )
        )
        val expected = listOf(
            Tweet(
                "Body 1",
                "User 1",
                times[0],
                null,
                "https://twitter.com/User1"
            ),
            Tweet(
                "Body 3",
                "User 3",
                times[2],
                null,
                "https://twitter.com/User3"
            )
        )

        val result = mapper.mapTweets(input)

        assertEquals(expected, result)
    }

    @Test
    fun mapTweetsWithSingleValidTweetDoesNotReplaceUrlsWhenNullEntitiesList() {
        val input = JsonTweet(
            TEST_TIMESTAMP,
            "A tweet with a URL https://t.co/foobar",
            JsonEntities(null),
            JsonUser(
                TEST_USER,
                null,
                TEST_SCREEN_NAME
            )
        )
        val expected = listOf(
            Tweet(
                "A tweet with a URL https://t.co/foobar",
                TEST_USER,
                TEST_TIME,
                null,
                TEST_PROFILE_URL
            )
        )

        val result = mapper.mapTweets(listOf(input))

        assertEquals(expected, result)
    }

    @Test
    fun mapTweetsWithSingleValidTweetDoesNotReplaceUrlsWhenEmptyEntitiesList() {
        val input = JsonTweet(
            TEST_TIMESTAMP,
            "A tweet with a URL https://t.co/foobar",
            JsonEntities(
                emptyList()
            ),
            JsonUser(
                TEST_USER,
                null,
                TEST_SCREEN_NAME
            )
        )
        val expected = listOf(
            Tweet(
                "A tweet with a URL https://t.co/foobar",
                TEST_USER,
                TEST_TIME,
                null,
                TEST_PROFILE_URL
            )
        )

        val result = mapper.mapTweets(listOf(input))

        assertEquals(expected, result)
    }

    @Test
    fun mapTweetsWithSingleValidTweetDoesNotReplaceNotFoundUrls() {
        val input = JsonTweet(
            TEST_TIMESTAMP,
            "A tweet with a URL https://t.co/foobar",
            JsonEntities(
                listOf(
                    JsonUrlEntity(
                        "https://t.co/notfound",
                        "https://foo.bar/notfound"
                    )
                )
            ),
            JsonUser(
                TEST_USER,
                null,
                TEST_SCREEN_NAME
            )
        )
        val expected = listOf(
            Tweet(
                "A tweet with a URL https://t.co/foobar",
                TEST_USER,
                TEST_TIME,
                null,
                TEST_PROFILE_URL
            )
        )

        val result = mapper.mapTweets(listOf(input))

        assertEquals(expected, result)
    }

    @Test
    fun mapTweetsWithSingleValidTweetDoesNotReplaceFoundUrlsWhenReplacementIsNull() {
        val input = JsonTweet(
            TEST_TIMESTAMP,
            "A tweet with a URL https://t.co/foobar",
            JsonEntities(
                listOf(
                    JsonUrlEntity(
                        "https://t.co/foobar",
                        null
                    )
                )
            ),
            JsonUser(
                TEST_USER,
                null,
                TEST_SCREEN_NAME
            )
        )
        val expected = listOf(
            Tweet(
                "A tweet with a URL https://t.co/foobar",
                TEST_USER,
                TEST_TIME,
                null,
                TEST_PROFILE_URL
            )
        )

        val result = mapper.mapTweets(listOf(input))

        assertEquals(expected, result)
    }

    @Test
    fun mapTweetsWithSingleValidTweetIgnoresNullReplacementUrl() {
        val input = JsonTweet(
            TEST_TIMESTAMP,
            "A tweet with a URL https://t.co/foobar",
            JsonEntities(
                listOf(
                    JsonUrlEntity(
                        null,
                        "https://foo.bar/found"
                    )
                )
            ),
            JsonUser(
                TEST_USER,
                null,
                TEST_SCREEN_NAME
            )
        )
        val expected = listOf(
            Tweet(
                "A tweet with a URL https://t.co/foobar",
                TEST_USER,
                TEST_TIME,
                null,
                TEST_PROFILE_URL
            )
        )

        val result = mapper.mapTweets(listOf(input))

        assertEquals(expected, result)
    }

    @Test
    fun mapTweetsWithSingleValidTweetReplacesSingleUrl() {
        val input = JsonTweet(
            TEST_TIMESTAMP,
            "A tweet with a URL https://t.co/foobar",
            JsonEntities(
                listOf(
                    JsonUrlEntity(
                        "https://t.co/foobar",
                        "https://foo.bar/found"
                    )
                )
            ),
            JsonUser(
                TEST_USER,
                null,
                TEST_SCREEN_NAME
            )
        )
        val expected = listOf(
            Tweet(
                "A tweet with a URL https://foo.bar/found",
                TEST_USER,
                TEST_TIME,
                null,
                TEST_PROFILE_URL
            )
        )

        val result = mapper.mapTweets(listOf(input))

        assertEquals(expected, result)
    }

    @Test
    fun mapTweetsWithSingleValidTweetReplacesMultipleUrls() {
        val input = JsonTweet(
            TEST_TIMESTAMP,
            "A tweet with a URL https://t.co/foobar1 and another URL https://t.co/foobar2 " +
                    "and another https://t.co/foobar3",
            JsonEntities(
                listOf(
                    JsonUrlEntity(
                        "https://t.co/foobar1",
                        "https://foo.bar/found1"
                    ),
                    JsonUrlEntity(
                        "https://t.co/foobar2",
                        "https://foo.bar/found2"
                    ),
                    JsonUrlEntity(
                        "https://t.co/foobar3",
                        "https://foo.bar/found3"
                    )
                )
            ),
            JsonUser(
                TEST_USER,
                null,
                TEST_SCREEN_NAME
            )
        )
        val expected = listOf(
            Tweet(
                "A tweet with a URL https://foo.bar/found1 and another URL " +
                        "https://foo.bar/found2 and another https://foo.bar/found3",
                TEST_USER,
                TEST_TIME,
                null,
                TEST_PROFILE_URL
            )
        )

        val result = mapper.mapTweets(listOf(input))

        assertEquals(expected, result)
    }
}