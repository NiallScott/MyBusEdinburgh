/*
 * Copyright (C) 2020 - 2025 Niall 'Rivernile' Scott
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
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.FakeTrackerEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.TrackerEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimes
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimesResponse
import uk.org.rivernile.android.bustracker.core.time.FakeTimeUtils
import uk.org.rivernile.android.bustracker.core.time.TimeUtils
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [LiveTimesRepository].
 *
 * @author Niall Scott
 */
class RealLiveTimesRepositoryTest {

    @Test
    fun getLiveTimesFlowEmitsExpectedValues() = runTest {
        val liveTimes = LiveTimes(
            stops = emptyMap(),
            receiveTime = 123L,
            hasGlobalDisruption = false
        )
        val response = LiveTimesResponse.Success(liveTimes)
        val expected = LiveTimesResult.Success(liveTimes)
        val repository = createLiveTimesRepository(
            trackerEndpoint = FakeTrackerEndpoint(
                onGetLiveTimesWithSingleStop = { stopCode, numberOfDepartures ->
                    assertEquals("123456", stopCode)
                    assertEquals(4, numberOfDepartures)
                    response
                }
            )
        )

        repository.getLiveTimesFlow("123456", 4).test {
            assertEquals(LiveTimesResult.InProgress, awaitItem())
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    private fun createLiveTimesRepository(
        trackerEndpoint: TrackerEndpoint = FakeTrackerEndpoint(),
        timeUtils: TimeUtils = FakeTimeUtils(
            onGetCurrentTimeMillis = { 123L }
        )
    ): RealLiveTimesRepository {
        return RealLiveTimesRepository(
            trackerEndpoint = trackerEndpoint,
            timeUtils = timeUtils
        )
    }
}