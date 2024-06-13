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

package uk.org.rivernile.android.bustracker.core.twitter

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import okio.IOException
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.FakeTwitterEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.LatestTweetsResponse
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.Tweet
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.TwitterEndpoint
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [TwitterRepository].
 *
 * @author Niall Scott
 */
class TwitterRepositoryTest {

    @Test
    fun latestTweetsFlowWithNullListOfTweetsEmitsSuccess() = runTest {
        val repository = createTwitterRepository(
            twitterEndpoint = FakeTwitterEndpoint(
                onGetLatestTweets = { LatestTweetsResponse.Success(null) }
            )
        )

        repository.latestTweetsFlow.test {
            assertEquals(LatestTweetsResult.InProgress, awaitItem())
            assertEquals(LatestTweetsResult.Success(null), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun latestTweetsFlowWithEmptyListOfTweetsEmitsSuccess() = runTest {
        val repository = createTwitterRepository(
            twitterEndpoint = FakeTwitterEndpoint(
                onGetLatestTweets = { LatestTweetsResponse.Success(emptyList()) }
            )
        )

        repository.latestTweetsFlow.test {
            assertEquals(LatestTweetsResult.InProgress, awaitItem())
            assertEquals(LatestTweetsResult.Success(emptyList()), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun latestTweetsFlowWithPopulatedListOfTweetsEmitsSuccess() = runTest {
        val result = listOf(
            Tweet(
                body = "Body 1",
                displayName = "Display name 1",
                time = Date(123L),
                profileImageUrl = "https://profile/image/url",
                profileUrl = "https://profile/url"
            ),
            Tweet(
                body = "Body 2",
                displayName = "Display name 2",
                time = Date(456L),
                profileImageUrl = null,
                profileUrl = "https://profile/url2"
            )
        )
        val repository = createTwitterRepository(
            twitterEndpoint = FakeTwitterEndpoint(
                onGetLatestTweets = { LatestTweetsResponse.Success(result) }
            )
        )

        repository.latestTweetsFlow.test {
            assertEquals(LatestTweetsResult.InProgress, awaitItem())
            assertEquals(LatestTweetsResult.Success(result), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun latestTweetsFlowWithNoConnectivityEmitsNoConnectivityError() = runTest {
        val repository = createTwitterRepository(
            twitterEndpoint = FakeTwitterEndpoint(
                onGetLatestTweets = { LatestTweetsResponse.Error.NoConnectivity }
            )
        )

        repository.latestTweetsFlow.test {
            assertEquals(LatestTweetsResult.InProgress, awaitItem())
            assertEquals(LatestTweetsResult.Error.NoConnectivity, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun latestTweetsFlowWithIoErrorEmitsIoError() = runTest {
        val exception = IOException()
        val repository = createTwitterRepository(
            twitterEndpoint = FakeTwitterEndpoint(
                onGetLatestTweets = { LatestTweetsResponse.Error.Io(exception) }
            )
        )

        repository.latestTweetsFlow.test {
            assertEquals(LatestTweetsResult.InProgress, awaitItem())
            assertEquals(LatestTweetsResult.Error.Io(exception), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun latestTweetsFlowWithAuthenticationErrorEmitsServerError() = runTest {
        val repository = createTwitterRepository(
            twitterEndpoint = FakeTwitterEndpoint(
                onGetLatestTweets = { LatestTweetsResponse.Error.Authentication }
            )
        )

        repository.latestTweetsFlow.test {
            assertEquals(LatestTweetsResult.InProgress, awaitItem())
            assertEquals(LatestTweetsResult.Error.Server, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun latestTweetsFlowWithUnrecognisedServerErrorEmitsServerError() = runTest {
        val repository = createTwitterRepository(
            twitterEndpoint = FakeTwitterEndpoint(
                onGetLatestTweets = { LatestTweetsResponse.Error.UnrecognisedServerError }
            )
        )

        repository.latestTweetsFlow.test {
            assertEquals(LatestTweetsResult.InProgress, awaitItem())
            assertEquals(LatestTweetsResult.Error.Server, awaitItem())
            awaitComplete()
        }
    }

    private fun createTwitterRepository(
        twitterEndpoint: TwitterEndpoint = FakeTwitterEndpoint()
    ): TwitterRepository {
        return TwitterRepository(twitterEndpoint)
    }
}