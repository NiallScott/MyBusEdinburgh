/*
 * Copyright (C) 2020 - 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Call
import retrofit2.Response
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.ErrorMapper
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.NetworkException
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.NoConnectivityException
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.UnrecognisedServerErrorException
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityChecker
import uk.org.rivernile.edinburghbustrackerapi.bustimes.BusTimes
import java.io.IOException

/**
 * Tests for [SingleLiveTimesRequest].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class SingleLiveTimesRequestTest {

    @Mock
    private lateinit var call: Call<BusTimes>
    @Mock
    private lateinit var liveTimesMapper: LiveTimesMapper
    @Mock
    private lateinit var errorMapper: ErrorMapper
    @Mock
    private lateinit var connectivityChecker: ConnectivityChecker

    @Mock
    private lateinit var response: Response<BusTimes>
    @Mock
    private lateinit var liveTimes: LiveTimes
    @Mock
    private lateinit var busTimes: BusTimes

    private lateinit var request: SingleLiveTimesRequest

    @Before
    fun setUp() {
        request = SingleLiveTimesRequest(call, liveTimesMapper, errorMapper, connectivityChecker)
    }

    @Test
    fun cancelCallsCancelOnCall() {
        request.cancel()

        verify(call)
                .cancel()
    }

    @Test(expected = NoConnectivityException::class)
    fun performRequestWithNoConnectivityThrowsNoConnectivityException() {
        givenHasNoConnectivity()

        request.performRequest()
    }

    @Test(expected = NetworkException::class)
    fun performRequestWithConnectivityButHasIoExceptionThrowsNetworkException() {
        givenHasConnectivity()
        whenever(call.execute())
                .thenThrow(IOException::class.java)

        request.performRequest()
    }

    @Test(expected = UnrecognisedServerErrorException::class)
    fun performRequestWithErrorThrowsException() {
        givenHasConnectivity()
        givenCallReturnsResponse()
        whenever(response.isSuccessful)
                .thenReturn(false)
        whenever(response.code())
                .thenReturn(400)
        whenever(errorMapper.mapHttpStatusCode(400))
                .thenReturn(UnrecognisedServerErrorException())

        request.performRequest()
    }

    @Test
    fun performRequestWithSuccessfulNullBodyReturnsEmptyLiveTimes() {
        givenHasConnectivity()
        givenCallReturnsResponse()
        whenever(response.isSuccessful)
                .thenReturn(true)
        whenever(response.body())
                .thenReturn(null)
        whenever(liveTimesMapper.emptyLiveTimes())
                .thenReturn(liveTimes)

        val request = request.performRequest()

        assertEquals(liveTimes, request)
    }

    @Test
    fun performRequestWithSuccessfulNonNullBodyReturnsMappedLiveTimes() {
        givenHasConnectivity()
        givenCallReturnsResponse()
        whenever(response.isSuccessful)
                .thenReturn(true)
        whenever(response.body())
                .thenReturn(busTimes)
        whenever(liveTimesMapper.mapToLiveTimes(busTimes))
                .thenReturn(liveTimes)

        val request = request.performRequest()

        assertEquals(liveTimes, request)
    }

    private fun givenHasNoConnectivity() {
        whenever(connectivityChecker.hasInternetConnectivity())
                .thenReturn(false)
    }

    private fun givenHasConnectivity() {
        whenever(connectivityChecker.hasInternetConnectivity())
                .thenReturn(true)
    }

    private fun givenCallReturnsResponse() {
        whenever(call.execute())
                .thenReturn(response)
    }
}