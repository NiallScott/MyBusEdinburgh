/*
 * Copyright (C) 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.endpoints.tracker

import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Response
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimes
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimesMapper
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimesResponse
import uk.org.rivernile.edinburghbustrackerapi.bustimes.BusTimes

/**
 * Tests for [ResponseHandler].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class ResponseHandlerTest {

    @Mock
    private lateinit var liveTimesMapper: LiveTimesMapper
    @Mock
    private lateinit var errorMapper: ErrorMapper

    private lateinit var handler: ResponseHandler

    @Before
    fun setUp() {
        handler = ResponseHandler(
            liveTimesMapper,
            errorMapper)
    }

    @Test
    fun handleLiveTimesResponseWithNonSuccessfulResponseDelegatesToErrorMapper() {
        val response = Response.error<BusTimes>(400, "error".toResponseBody())
        val expected = LiveTimesResponse.Error.ServerError.Other()
        whenever(errorMapper.mapHttpStatusCode(400))
            .thenReturn(expected)

        val result = handler.handleLiveTimesResponse(response)

        assertEquals(expected, result)
    }

    @Test
    fun handleLiveTimesResponseWithSuccessfulResponseWithNullBodyReturnsEmptyLiveTimes() {
        val response = Response.success<BusTimes>(null)
        val expected = LiveTimesResponse.Error.ServerError.Other()
        whenever(liveTimesMapper.emptyLiveTimes())
            .thenReturn(expected)

        val result = handler.handleLiveTimesResponse(response)

        assertEquals(expected, result)
    }

    @Test
    fun handleLiveTimesResponseWithSuccessfulResponseWithBodyReturnsLiveTimes() {
        val busTimes = mock<BusTimes>()
        val response = Response.success(busTimes)
        val liveTimes = mock<LiveTimes>()
        val expected = LiveTimesResponse.Success(liveTimes)
        whenever(liveTimesMapper.mapToLiveTimes(busTimes))
            .thenReturn(expected)

        val result = handler.handleLiveTimesResponse(response)

        assertEquals(expected, result)
    }
}