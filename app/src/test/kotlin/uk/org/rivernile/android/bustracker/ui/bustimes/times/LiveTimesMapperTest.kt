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

package uk.org.rivernile.android.bustracker.ui.bustimes.times

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.AuthenticationException
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.MaintenanceException
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.NetworkException
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.NoConnectivityException
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.SystemOverloadedException
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.UnrecognisedServerErrorException
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimes
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.Service
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.Stop
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.Vehicle
import uk.org.rivernile.android.bustracker.core.livetimes.Result
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.Date

/**
 * Tests for [LiveTimesMapper].
 *
 * @author Niall Scott
 */
class LiveTimesMapperTest {

    private val date = Date()

    private lateinit var mapper: LiveTimesMapper

    @Before
    fun setUp() {
        mapper = LiveTimesMapper()
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultWithProgressMapsToUiProgress() {
        val result = mapper.mapLiveTimesAndColoursToUiResult("123456", Result.InProgress, null)

        assertEquals(UiResult.InProgress, result)
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultWithNoConnectivityErrorReturnsError() {
        val liveTimesResult = Result.Error(123L, NoConnectivityException())
        val expected = UiResult.Error(123L, ErrorType.NO_CONNECTIVITY)

        val result = mapper.mapLiveTimesAndColoursToUiResult("123456", liveTimesResult, null)

        assertEquals(expected, result)
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultWithUnknownHostErrorReturnsError() {
        val exception = NetworkException(UnknownHostException())
        val liveTimesResult = Result.Error(123L, exception)
        val expected = UiResult.Error(123L, ErrorType.UNKNOWN_HOST)

        val result = mapper.mapLiveTimesAndColoursToUiResult("123456", liveTimesResult, null)

        assertEquals(expected, result)
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultWithCommunicationErrorReturnsError() {
        val exception = NetworkException(SocketTimeoutException())
        val liveTimesResult = Result.Error(123L, exception)
        val expected = UiResult.Error(123L, ErrorType.COMMUNICATION)

        val result = mapper.mapLiveTimesAndColoursToUiResult("123456", liveTimesResult, null)

        assertEquals(expected, result)
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultWithUnrecognisedServerErrorReturnsError() {
        val liveTimesResult = Result.Error(123L, UnrecognisedServerErrorException())
        val expected = UiResult.Error(123L, ErrorType.SERVER_ERROR)

        val result = mapper.mapLiveTimesAndColoursToUiResult("123456", liveTimesResult, null)

        assertEquals(expected, result)
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultWithAuthenticationErrorReturnsError() {
        val liveTimesResult = Result.Error(123L, AuthenticationException())
        val expected = UiResult.Error(123L, ErrorType.AUTHENTICATION)

        val result = mapper.mapLiveTimesAndColoursToUiResult("123456", liveTimesResult, null)

        assertEquals(expected, result)
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultWithMaintenanceErrorReturnsError() {
        val liveTimesResult = Result.Error(123L, MaintenanceException())
        val expected = UiResult.Error(123L, ErrorType.DOWN_FOR_MAINTENANCE)

        val result = mapper.mapLiveTimesAndColoursToUiResult("123456", liveTimesResult, null)

        assertEquals(expected, result)
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultWithSystemOverloadedErrorReturnsError() {
        val liveTimesResult = Result.Error(123L, SystemOverloadedException())
        val expected = UiResult.Error(123L, ErrorType.SYSTEM_OVERLOADED)

        val result = mapper.mapLiveTimesAndColoursToUiResult("123456", liveTimesResult, null)

        assertEquals(expected, result)
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultWithStopNotFoundReturnsNoDataError() {
        val liveTimesResult = Result.Success(
                LiveTimes(
                        emptyMap(),
                        123L,
                        false))
        val expected = UiResult.Error(123L, ErrorType.NO_DATA)

        val result = mapper.mapLiveTimesAndColoursToUiResult("123456", liveTimesResult, null)

        assertEquals(expected, result)
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultWithEmptyServicesReturnsNoDataError() {
        val liveTimesResult = Result.Success(
                LiveTimes(
                        mapOf(
                                "123456" to Stop(
                                        "123456",
                                        "Stop name",
                                        emptyList(),
                                        false)
                        ),
                        123L,
                        false))
        val expected = UiResult.Error(123L, ErrorType.NO_DATA)

        val result = mapper.mapLiveTimesAndColoursToUiResult("123456", liveTimesResult, null)

        assertEquals(expected, result)
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultWithEmptyVehiclesInServicesReturnsNoDataError() {
        val liveTimesResult = Result.Success(
                LiveTimes(
                        mapOf(
                                "123456" to Stop(
                                        "123456",
                                        "Stop name",
                                        listOf(
                                                Service(
                                                        "1",
                                                        emptyList(),
                                                        null,
                                                        null,
                                                        isDisrupted = false,
                                                        isDiverted = false)
                                        ),
                                        false)
                        ),
                        123L,
                        false))
        val expected = UiResult.Error(123L, ErrorType.NO_DATA)

        val result = mapper.mapLiveTimesAndColoursToUiResult("123456", liveTimesResult, null)

        assertEquals(expected, result)
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultHasNullColourWhenNoColoursAreProvided() {
        val liveTimesResult = Result.Success(
                LiveTimes(
                        mapOf(
                                "123456" to Stop(
                                        "123456",
                                        "Stop name",
                                        listOf(
                                                Service(
                                                        "1",
                                                        listOf(
                                                                createVehicle()),
                                                        null,
                                                        null,
                                                        isDisrupted = false,
                                                        isDiverted = false)
                                        ),
                                        false)
                        ),
                        123L,
                        false))
        val expected = UiResult.Success(
                123L,
                UiStop(
                        "123456",
                        "Stop name",
                        listOf(
                                UiService(
                                        "1",
                                        null,
                                        listOf(
                                                createUiVehicle())
                                )
                        )
                )
        )

        val result = mapper.mapLiveTimesAndColoursToUiResult("123456", liveTimesResult, null)

        assertEquals(expected, result)
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultHasNonNullColourWhenColoursAreProvided() {
        val liveTimesResult = Result.Success(
                LiveTimes(
                        mapOf(
                                "123456" to Stop(
                                        "123456",
                                        "Stop name",
                                        listOf(
                                                Service(
                                                        "1",
                                                        listOf(
                                                                createVehicle()),
                                                        null,
                                                        null,
                                                        isDisrupted = false,
                                                        isDiverted = false)
                                        ),
                                        false)
                        ),
                        123L,
                        false))
        val expected = UiResult.Success(
                123L,
                UiStop(
                        "123456",
                        "Stop name",
                        listOf(
                                UiService(
                                        "1",
                                        0xFFFFFF,
                                        listOf(
                                                createUiVehicle())
                                )
                        )
                )
        )
        val colours = mapOf("1" to 0xFFFFFF)

        val result = mapper.mapLiveTimesAndColoursToUiResult("123456", liveTimesResult, colours)

        assertEquals(expected, result)
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultHasNonNullColourWhenColoursAreProvidedMultiple() {
        val liveTimesResult = Result.Success(
                LiveTimes(
                        mapOf(
                                "123456" to Stop(
                                        "123456",
                                        "Stop name",
                                        listOf(
                                                Service(
                                                        "1",
                                                        listOf(
                                                                createVehicle()),
                                                        null,
                                                        null,
                                                        isDisrupted = false,
                                                        isDiverted = false),
                                                Service(
                                                        "2",
                                                        listOf(
                                                                createVehicle()),
                                                        null,
                                                        null,
                                                        isDisrupted = false,
                                                        isDiverted = false),
                                                Service(
                                                        "3",
                                                        listOf(
                                                                createVehicle()),
                                                        null,
                                                        null,
                                                        isDisrupted = false,
                                                        isDiverted = false)
                                        ),
                                        false)
                        ),
                        123L,
                        false))
        val expected = UiResult.Success(
                123L,
                UiStop(
                        "123456",
                        "Stop name",
                        listOf(
                                UiService(
                                        "1",
                                        0xFFFFFF,
                                        listOf(
                                                createUiVehicle())),
                                UiService(
                                        "2",
                                        null,
                                        listOf(
                                                createUiVehicle())),
                                UiService(
                                        "3",
                                        0xFF0000,
                                        listOf(
                                                createUiVehicle()))
                        )
                )
        )
        val colours = mapOf(
                "1" to 0xFFFFFF,
                "3" to 0xFF0000)

        val result = mapper.mapLiveTimesAndColoursToUiResult("123456", liveTimesResult, colours)

        assertEquals(expected, result)
    }

    private fun createVehicle() = Vehicle(
            null,
            date,
            2,
            null,
            null,
            isEstimatedTime = false,
            isDelayed = false,
            isDiverted = false,
            isTerminus = false,
            isPartRoute = false)

    private fun createUiVehicle() = UiVehicle(
            null,
            false,
            date,
            2,
            false)
}