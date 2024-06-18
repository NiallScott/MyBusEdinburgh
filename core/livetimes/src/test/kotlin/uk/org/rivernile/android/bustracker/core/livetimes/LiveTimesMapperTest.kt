/*
 * Copyright (C) 2023 - 2024 Niall 'Rivernile' Scott
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

import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimes
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimesResponse
import uk.org.rivernile.android.bustracker.core.utils.TimeUtils
import java.net.UnknownHostException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [LiveTimesMapper].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class LiveTimesMapperTest {

    @Mock
    private lateinit var timeUtils: TimeUtils

    private lateinit var mapper: LiveTimesMapper

    @BeforeTest
    fun setUp() {
        mapper = LiveTimesMapper(timeUtils)
    }

    @Test
    fun mapToLiveTimesResultWithSuccessResponseMapsToSuccess() {
        val liveTimes = mock<LiveTimes>()
        val response = LiveTimesResponse.Success(liveTimes)
        val expected = LiveTimesResult.Success(liveTimes)

        val result = mapper.mapToLiveTimesResult(response)

        assertEquals(expected, result)
    }

    @Test
    fun mapToLiveTimesResultWithNoConnectivityMapsToNoConnectivity() {
        givenHasTimestamp()
        val response = LiveTimesResponse.Error.NoConnectivity
        val expected = LiveTimesResult.Error.NoConnectivity(123L)

        val result = mapper.mapToLiveTimesResult(response)

        assertEquals(expected, result)
    }

    @Test
    fun mapToLiveTimesResultWithIoErrorMapsToIoError() {
        givenHasTimestamp()
        val throwable = UnknownHostException()
        val response = LiveTimesResponse.Error.Io(throwable)
        val expected = LiveTimesResult.Error.Io(123L, throwable)

        val result = mapper.mapToLiveTimesResult(response)

        assertEquals(expected, result)
    }

    @Test
    fun mapToLiveTimesResultWithAuthenticationErrorMapsToAuthenticationError() {
        givenHasTimestamp()
        val response = LiveTimesResponse.Error.ServerError.Authentication
        val expected = LiveTimesResult.Error.ServerError.Authentication(123L)

        val result = mapper.mapToLiveTimesResult(response)

        assertEquals(expected, result)
    }

    @Test
    fun mapToLiveTimesResultWithMaintenanceErrorMapsToMaintenanceError() {
        givenHasTimestamp()
        val response = LiveTimesResponse.Error.ServerError.Maintenance
        val expected = LiveTimesResult.Error.ServerError.Maintenance(123L)

        val result = mapper.mapToLiveTimesResult(response)

        assertEquals(expected, result)
    }

    @Test
    fun mapToLiveTimesResultWithSystemOverloadedErrorMapsToSystemOverloadedError() {
        givenHasTimestamp()
        val response = LiveTimesResponse.Error.ServerError.SystemOverloaded
        val expected = LiveTimesResult.Error.ServerError.SystemOverloaded(123L)

        val result = mapper.mapToLiveTimesResult(response)

        assertEquals(expected, result)
    }

    @Test
    fun mapToLiveTimesResultWithOtherErrorMapsToOtherError() {
        givenHasTimestamp()
        val response = LiveTimesResponse.Error.ServerError.Other()
        val expected = LiveTimesResult.Error.ServerError.Other(123L)

        val result = mapper.mapToLiveTimesResult(response)

        assertEquals(expected, result)
    }

    private fun givenHasTimestamp() {
        whenever(timeUtils.currentTimeMills)
            .thenReturn(123L)
    }
}