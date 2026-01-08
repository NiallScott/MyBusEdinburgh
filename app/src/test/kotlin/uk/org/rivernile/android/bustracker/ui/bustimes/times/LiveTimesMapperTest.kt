/*
 * Copyright (C) 2020 - 2026 Niall 'Rivernile' Scott
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

import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimes
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.Service
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.Stop
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.Vehicle
import uk.org.rivernile.android.bustracker.core.livetimes.LiveTimesResult
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Tests for [LiveTimesMapper].
 *
 * @author Niall Scott
 */
class LiveTimesMapperTest {

    private val date = Clock.System.now()

    private lateinit var mapper: LiveTimesMapper

    @BeforeTest
    fun setUp() {
        mapper = LiveTimesMapper()
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultWithProgressMapsToUiProgress() {
        val result = mapper.mapLiveTimesAndColoursToUiResult(
            "123456",
            LiveTimesResult.InProgress,
            null
        )

        assertEquals(UiResult.InProgress, result)
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultWithNoConnectivityErrorReturnsError() {
        val liveTimesResult = LiveTimesResult.Error.NoConnectivity(123L)
        val expected = UiResult.Error(123L, ErrorType.NO_CONNECTIVITY)

        val result = mapper.mapLiveTimesAndColoursToUiResult("123456", liveTimesResult, null)

        assertEquals(expected, result)
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultWithUnknownHostErrorReturnsError() {
        val liveTimesResult = LiveTimesResult.Error.Io(123L, UnknownHostException())
        val expected = UiResult.Error(123L, ErrorType.UNKNOWN_HOST)

        val result = mapper.mapLiveTimesAndColoursToUiResult("123456", liveTimesResult, null)

        assertEquals(expected, result)
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultWithCommunicationErrorReturnsError() {
        val liveTimesResult = LiveTimesResult.Error.Io(123L, SocketTimeoutException())
        val expected = UiResult.Error(123L, ErrorType.COMMUNICATION)

        val result = mapper.mapLiveTimesAndColoursToUiResult("123456", liveTimesResult, null)

        assertEquals(expected, result)
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultWithUnrecognisedServerErrorReturnsError() {
        val liveTimesResult = LiveTimesResult.Error.ServerError.Other(123L)
        val expected = UiResult.Error(123L, ErrorType.SERVER_ERROR)

        val result = mapper.mapLiveTimesAndColoursToUiResult("123456", liveTimesResult, null)

        assertEquals(expected, result)
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultWithAuthenticationErrorReturnsError() {
        val liveTimesResult = LiveTimesResult.Error.ServerError.Authentication(123L)
        val expected = UiResult.Error(123L, ErrorType.AUTHENTICATION)

        val result = mapper.mapLiveTimesAndColoursToUiResult("123456", liveTimesResult, null)

        assertEquals(expected, result)
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultWithMaintenanceErrorReturnsError() {
        val liveTimesResult = LiveTimesResult.Error.ServerError.Maintenance(123L)
        val expected = UiResult.Error(123L, ErrorType.DOWN_FOR_MAINTENANCE)

        val result = mapper.mapLiveTimesAndColoursToUiResult("123456", liveTimesResult, null)

        assertEquals(expected, result)
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultWithSystemOverloadedErrorReturnsError() {
        val liveTimesResult = LiveTimesResult.Error.ServerError.SystemOverloaded(123L)
        val expected = UiResult.Error(123L, ErrorType.SYSTEM_OVERLOADED)

        val result = mapper.mapLiveTimesAndColoursToUiResult("123456", liveTimesResult, null)

        assertEquals(expected, result)
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultWithStopNotFoundReturnsNoDataError() {
        val liveTimesResult = LiveTimesResult.Success(
            LiveTimes(
                emptyMap(),
                Instant.fromEpochMilliseconds(123L)
            )
        )
        val expected = UiResult.Error(123L, ErrorType.NO_DATA)

        val result = mapper.mapLiveTimesAndColoursToUiResult("123456", liveTimesResult, null)

        assertEquals(expected, result)
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultWithEmptyServicesReturnsNoDataError() {
        val liveTimesResult = LiveTimesResult.Success(
            LiveTimes(
                mapOf(
                    "123456" to Stop(
                        "123456",
                        emptyList()
                    )
                ),
                Instant.fromEpochMilliseconds(123L)
            )
        )
        val expected = UiResult.Error(123L, ErrorType.NO_DATA)

        val result = mapper.mapLiveTimesAndColoursToUiResult("123456", liveTimesResult, null)

        assertEquals(expected, result)
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultWithEmptyVehiclesInServicesReturnsNoDataError() {
        val liveTimesResult = LiveTimesResult.Success(
            LiveTimes(
                mapOf(
                    "123456" to Stop(
                        "123456",
                        listOf(
                            Service(
                                "1",
                                emptyList()
                            )
                        )
                    )
                ),
                Instant.fromEpochMilliseconds(123L)
            )
        )
        val expected = UiResult.Error(123L, ErrorType.NO_DATA)

        val result = mapper.mapLiveTimesAndColoursToUiResult("123456", liveTimesResult, null)

        assertEquals(expected, result)
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultHasNullColourWhenNoColoursAreProvided() {
        val liveTimesResult = LiveTimesResult.Success(
            LiveTimes(
                mapOf(
                    "123456" to Stop(
                        "123456",
                        listOf(
                            Service(
                                "1",
                                listOf(createVehicle())
                            )
                        )
                    )
                ),
                Instant.fromEpochMilliseconds(123L)
            )
        )
        val expected = UiResult.Success(
            123L,
            UiStop(
                "123456",
                listOf(
                    UiService(
                        "1",
                        null,
                        listOf(createUiVehicle())
                    )
                )
            )
        )

        val result = mapper.mapLiveTimesAndColoursToUiResult("123456", liveTimesResult, null)

        assertEquals(expected, result)
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultHasNonNullColourWhenColoursAreProvided() {
        val liveTimesResult = LiveTimesResult.Success(
            LiveTimes(
                mapOf(
                    "123456" to Stop(
                        "123456",
                        listOf(
                            Service(
                                "1",
                                listOf(createVehicle())
                            )
                        )
                    )
                ),
                Instant.fromEpochMilliseconds(123L)
            )
        )
        val expected = UiResult.Success(
            123L,
            UiStop(
                "123456",
                listOf(
                    UiService(
                        "1",
                        ServiceColours(1, 2),
                        listOf(createUiVehicle())
                    )
                )
            )
        )
        val colours = mapOf("1" to ServiceColours(1, 2))

        val result = mapper.mapLiveTimesAndColoursToUiResult("123456", liveTimesResult, colours)

        assertEquals(expected, result)
    }

    @Test
    fun mapLiveTimesAndColoursToUiResultHasNonNullColourWhenColoursAreProvidedMultiple() {
        val liveTimesResult = LiveTimesResult.Success(
            LiveTimes(
                mapOf(
                    "123456" to Stop(
                        "123456",
                        listOf(
                            Service(
                                "1",
                                listOf(createVehicle())
                            ),
                            Service(
                                "2",
                                listOf(createVehicle())
                            ),
                            Service(
                                "3",
                                listOf(createVehicle())
                            )
                        )
                    )
                ),
                Instant.fromEpochMilliseconds(123L)
            )
        )
        val expected = UiResult.Success(
            123L,
            UiStop(
                "123456",
                listOf(
                    UiService(
                        "1",
                        ServiceColours(1, 2),
                        listOf(createUiVehicle())
                    ),
                    UiService(
                        "2",
                        null,
                        listOf(createUiVehicle())
                    ),
                    UiService(
                        "3",
                        ServiceColours(3, 4),
                        listOf(createUiVehicle())
                    )
                )
            )
        )
        val colours = mapOf(
            "1" to ServiceColours(1, 2),
            "3" to ServiceColours(3, 4)
        )

        val result = mapper.mapLiveTimesAndColoursToUiResult("123456", liveTimesResult, colours)

        assertEquals(expected, result)
    }

    private fun createVehicle() = Vehicle(
        destination = null,
        departureTime = date,
        departureMinutes = 2,
        isEstimatedTime = false,
        isDiverted = false,
    )

    private fun createUiVehicle() = UiVehicle(
        destination = null,
        isDiverted = false,
        departureTime = date,
        departureMinutes = 2,
        isEstimatedTime = false
    )
}
