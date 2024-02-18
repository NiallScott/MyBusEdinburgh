/*
 * Copyright (C) 2019 - 2023 Niall 'Rivernile' Scott
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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimesResponse
import uk.org.rivernile.edinburghbustrackerapi.FaultCode
import uk.org.rivernile.edinburghbustrackerapi.bustimes.BusTimes
import java.net.HttpURLConnection

/**
 * Tests for [ErrorMapper].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class ErrorMapperTest {

    @Mock
    lateinit var busTimes: BusTimes

    private lateinit var errorMapper: ErrorMapper

    @Before
    fun setUp() {
        errorMapper = ErrorMapper()
    }

    @Test
    fun nullFaultCodeMeansNoErrorIsReturned() {
        whenever(busTimes.faultCode)
                .thenReturn(null)

        val result = errorMapper.extractError(busTimes)

        assertNull(result)
    }

    @Test
    fun unknownFaultCodeMeansGenericErrorIsReturned() {
        whenever(busTimes.faultCode)
                .thenReturn("unknown")

        val result = errorMapper.extractError(busTimes)

        assertEquals(LiveTimesResponse.Error.ServerError.Other("Fault code = unknown"), result)
    }

    @Test
    fun invalidAppKeyErrorMeansAuthenticationExceptionIsReturned() {
        whenever(busTimes.faultCode)
                .thenReturn(FaultCode.INVALID_APP_KEY.name)

        val result = errorMapper.extractError(busTimes)

        assertEquals(LiveTimesResponse.Error.ServerError.Authentication, result)
    }

    @Test
    fun invalidParameterErrorMeansServerErrorExceptionIsReturned() {
        whenever(busTimes.faultCode)
                .thenReturn(FaultCode.INVALID_PARAMETER.name)

        val result = errorMapper.extractError(busTimes)

        assertEquals(LiveTimesResponse.Error.ServerError.Other("INVALID_PARAMETER"), result)
    }

    @Test
    fun processingErrorMeansServerErrorExceptionIsReturned() {
        whenever(busTimes.faultCode)
                .thenReturn(FaultCode.PROCESSING_ERROR.name)

        val result = errorMapper.extractError(busTimes)

        assertEquals(LiveTimesResponse.Error.ServerError.Other("PROCESSING_ERROR"), result)
    }

    @Test
    fun systemMaintenanceErrorMeansMaintenanceExceptionIsReturned() {
        whenever(busTimes.faultCode)
                .thenReturn(FaultCode.SYSTEM_MAINTENANCE.name)

        val result = errorMapper.extractError(busTimes)

        assertEquals(LiveTimesResponse.Error.ServerError.Maintenance, result)
    }

    @Test
    fun systemOverloadedErrorMeansSystemOverloadedExceptionIsReturned() {
        whenever(busTimes.faultCode)
                .thenReturn(FaultCode.SYSTEM_OVERLOADED.name)

        val result = errorMapper.extractError(busTimes)

        assertEquals(LiveTimesResponse.Error.ServerError.SystemOverloaded, result)
    }

    @Test
    fun unauthorisedHttpStatusCodeMapsToAuthenticationException() {
        val result = errorMapper.mapHttpStatusCode(HttpURLConnection.HTTP_UNAUTHORIZED)

        assertEquals(LiveTimesResponse.Error.ServerError.Authentication, result)
    }

    @Test
    fun forbiddenHttpStatusCodeMapsToAuthenticationException() {
        val result = errorMapper.mapHttpStatusCode(HttpURLConnection.HTTP_FORBIDDEN)

        assertEquals(LiveTimesResponse.Error.ServerError.Authentication, result)
    }

    @Test
    fun http500StatusCodeMapsToServerErrorException() {
        val result = errorMapper.mapHttpStatusCode(500)

        assertEquals(LiveTimesResponse.Error.ServerError.Other("Server error: 500"), result)
    }

    @Test
    fun http599StatusCodeMapsToServerErrorException() {
        val result = errorMapper.mapHttpStatusCode(599)

        assertEquals(LiveTimesResponse.Error.ServerError.Other("Server error: 599"), result)
    }

    @Test
    fun randomStatusCodeMapsToGenericTrackerException() {
        val result = errorMapper.mapHttpStatusCode(499)

        assertEquals(LiveTimesResponse.Error.ServerError.Other("Server error: 499"), result)
    }

    @Test
    fun randomStatusCode2MapsToGenericTrackerException() {
        val result = errorMapper.mapHttpStatusCode(600)

        assertEquals(LiveTimesResponse.Error.ServerError.Other("Server error: 600"), result)
    }
}