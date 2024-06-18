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

package uk.org.rivernile.android.bustracker.core.livetimes

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.TrackerEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimes
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimesResponse
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [LiveTimesRepository].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class LiveTimesRepositoryTest {

    @Mock
    private lateinit var trackerEndpoint: TrackerEndpoint
    @Mock
    private lateinit var liveTimesMapper: LiveTimesMapper

    private lateinit var repository: LiveTimesRepository

    @BeforeTest
    fun setUp() {
        repository = LiveTimesRepository(
            trackerEndpoint,
            liveTimesMapper
        )
    }

    @Test
    fun getLiveTimesFlowEmitsExpectedValues() = runTest {
        val liveTimes = mock<LiveTimes>()
        val response = LiveTimesResponse.Success(liveTimes)
        val expected = LiveTimesResult.Success(liveTimes)
        whenever(trackerEndpoint.getLiveTimes("123456", 4))
            .thenReturn(response)
        whenever(liveTimesMapper.mapToLiveTimesResult(response))
            .thenReturn(expected)

        repository.getLiveTimesFlow("123456", 4).test {
            assertEquals(LiveTimesResult.InProgress, awaitItem())
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }
}