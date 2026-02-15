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

package uk.org.rivernile.android.bustracker.core.endpoints.tracker

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalTime
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.IOException
import retrofit2.Response
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toAtcoStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.ArrivalDepartureTime
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.EdinburghOpenApi
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.JsonStopEvent
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.JsonStopEvents
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimes
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimesResponse
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.Service
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.Stop
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.Vehicle
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.emptyLiveTimes
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import uk.org.rivernile.android.bustracker.core.log.FakeExceptionLogger
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityRepository
import uk.org.rivernile.android.bustracker.core.networking.FakeConnectivityRepository
import uk.org.rivernile.android.bustracker.core.time.FakeTimeUtils
import uk.org.rivernile.android.bustracker.core.time.TimeUtils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Tests for [EdinburghTrackerEndpoint].
 *
 * @author Niall Scott
 */
class EdinburghTrackerEndpointTest {

    @Test
    fun getLiveTimesSingleAtcoReturnsNoConnectivityWhenNoConnectivity() = runTest {
        val endpoint = createEndpoint(
            connectivityRepository = FakeConnectivityRepository(
                onHasInternetConnectivity = { false }
            )
        )

        val result = endpoint.getLiveTimes(
            stopIdentifier = "123456".toAtcoStopIdentifier(),
            numberOfDepartures = 4
        )

        assertEquals(LiveTimesResponse.Error.NoConnectivity, result)
    }

    @Test
    fun getLiveTimesSingleNaptanReturnsNoConnectivityWhenNoConnectivity() = runTest {
        val endpoint = createEndpoint(
            connectivityRepository = FakeConnectivityRepository(
                onHasInternetConnectivity = { false }
            )
        )

        val result = endpoint.getLiveTimes(
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            numberOfDepartures = 4
        )

        assertEquals(LiveTimesResponse.Error.NoConnectivity, result)
    }

    @Test
    fun getLiveTimesSingleAtcoReturnsIoErrorWhenSerializationExceptionIsThrown() = runTest {
        val exception = SerializationException()
        val exceptionLogger = FakeExceptionLogger()
        val endpoint = createEndpoint(
            api = FakeEdinburghOpenApi(
                onGetStopEventsWithAtcoCode = { atcoCode, numberOfDepartures ->
                    assertEquals("123456", atcoCode)
                    assertEquals(4, numberOfDepartures)
                    throw exception
                }
            ),
            connectivityRepository = connectivityRepositoryHasConnectivity,
            exceptionLogger = exceptionLogger
        )

        val result = endpoint.getLiveTimes(
            stopIdentifier = "123456".toAtcoStopIdentifier(),
            numberOfDepartures = 4
        )

        assertEquals(
            LiveTimesResponse.Error.Io(throwable = exception),
            result
        )
        assertEquals(1, exceptionLogger.loggedThrowables.size)
        assertSame(exception, exceptionLogger.loggedThrowables.last())
    }

    @Test
    fun getLiveTimesSingleNaptanReturnsIoErrorWhenSerializationExceptionIsThrown() = runTest {
        val exception = SerializationException()
        val exceptionLogger = FakeExceptionLogger()
        val endpoint = createEndpoint(
            api = FakeEdinburghOpenApi(
                onGetStopEventsWithSmsCode = { smsCode, numberOfDepartures ->
                    assertEquals("123456", smsCode)
                    assertEquals(4, numberOfDepartures)
                    throw exception
                }
            ),
            connectivityRepository = connectivityRepositoryHasConnectivity,
            exceptionLogger = exceptionLogger
        )

        val result = endpoint.getLiveTimes(
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            numberOfDepartures = 4
        )

        assertEquals(
            LiveTimesResponse.Error.Io(throwable = exception),
            result
        )
        assertEquals(1, exceptionLogger.loggedThrowables.size)
        assertSame(exception, exceptionLogger.loggedThrowables.last())
    }

    @Test
    fun getLiveTimesSingleAtcoReturnsIoErrorWhenIoExceptionIsThrown() = runTest {
        val exception = IOException()
        val exceptionLogger = FakeExceptionLogger()
        val endpoint = createEndpoint(
            api = FakeEdinburghOpenApi(
                onGetStopEventsWithAtcoCode = { atcoCode, numberOfDepartures ->
                    assertEquals("123456", atcoCode)
                    assertEquals(4, numberOfDepartures)
                    throw exception
                }
            ),
            connectivityRepository = connectivityRepositoryHasConnectivity,
            exceptionLogger = exceptionLogger
        )

        val result = endpoint.getLiveTimes(
            stopIdentifier = "123456".toAtcoStopIdentifier(),
            numberOfDepartures = 4
        )

        assertEquals(
            LiveTimesResponse.Error.Io(throwable = exception),
            result
        )
        assertEquals(1, exceptionLogger.loggedThrowables.size)
        assertSame(exception, exceptionLogger.loggedThrowables.last())
    }

    @Test
    fun getLiveTimesSingleNaptanReturnsIoErrorWhenIoExceptionIsThrown() = runTest {
        val exception = IOException()
        val exceptionLogger = FakeExceptionLogger()
        val endpoint = createEndpoint(
            api = FakeEdinburghOpenApi(
                onGetStopEventsWithSmsCode = { smsCode, numberOfDepartures ->
                    assertEquals("123456", smsCode)
                    assertEquals(4, numberOfDepartures)
                    throw exception
                }
            ),
            connectivityRepository = connectivityRepositoryHasConnectivity,
            exceptionLogger = exceptionLogger
        )

        val result = endpoint.getLiveTimes(
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            numberOfDepartures = 4
        )

        assertEquals(
            LiveTimesResponse.Error.Io(throwable = exception),
            result
        )
        assertEquals(1, exceptionLogger.loggedThrowables.size)
        assertSame(exception, exceptionLogger.loggedThrowables.last())
    }

    @Test
    fun getLiveTimesSingleAtcoReturnsAuthenticationErrorWhenUnauthorisedIsReturned() = runTest {
        val exceptionLogger = FakeExceptionLogger()
        val endpoint = createEndpoint(
            api = FakeEdinburghOpenApi(
                onGetStopEventsWithAtcoCode = { atcoCode, numberOfDepartures ->
                    assertEquals("123456", atcoCode)
                    assertEquals(4, numberOfDepartures)
                    Response.error(401, "Unauthorized".toResponseBody())
                }
            ),
            connectivityRepository = connectivityRepositoryHasConnectivity,
            exceptionLogger = exceptionLogger
        )

        val result = endpoint.getLiveTimes(
            stopIdentifier = "123456".toAtcoStopIdentifier(),
            numberOfDepartures = 4
        )

        assertEquals(
            LiveTimesResponse.Error.ServerError.Authentication,
            result
        )
        assertEquals(1, exceptionLogger.loggedThrowables.size)
        assertIs<RuntimeException>(exceptionLogger.loggedThrowables.last())
    }

    @Test
    fun getLiveTimesSingleNaptanReturnsAuthenticationErrorWhenUnauthorisedIsReturned() = runTest {
        val exceptionLogger = FakeExceptionLogger()
        val endpoint = createEndpoint(
            api = FakeEdinburghOpenApi(
                onGetStopEventsWithSmsCode = { smsCode, numberOfDepartures ->
                    assertEquals("123456", smsCode)
                    assertEquals(4, numberOfDepartures)
                    Response.error(401, "Unauthorized".toResponseBody())
                }
            ),
            connectivityRepository = connectivityRepositoryHasConnectivity,
            exceptionLogger = exceptionLogger
        )

        val result = endpoint.getLiveTimes(
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            numberOfDepartures = 4
        )

        assertEquals(
            LiveTimesResponse.Error.ServerError.Authentication,
            result
        )
        assertEquals(1, exceptionLogger.loggedThrowables.size)
        assertIs<RuntimeException>(exceptionLogger.loggedThrowables.last())
    }

    @Test
    fun getLiveTimesSingleAtcoReturnsOtherServerErrorOnAnyOtherServerError() = runTest {
        val exceptionLogger = FakeExceptionLogger()
        val endpoint = createEndpoint(
            api = FakeEdinburghOpenApi(
                onGetStopEventsWithAtcoCode = { atcoCode, numberOfDepartures ->
                    assertEquals("123456", atcoCode)
                    assertEquals(4, numberOfDepartures)
                    Response.error(404, "Not found".toResponseBody())
                }
            ),
            connectivityRepository = connectivityRepositoryHasConnectivity,
            exceptionLogger = exceptionLogger
        )

        val result = endpoint.getLiveTimes(
            stopIdentifier = "123456".toAtcoStopIdentifier(),
            numberOfDepartures = 4
        )

        assertEquals(
            LiveTimesResponse.Error.ServerError.Other(error = "Not found"),
            result
        )
        assertEquals(1, exceptionLogger.loggedThrowables.size)
        assertIs<RuntimeException>(exceptionLogger.loggedThrowables.last())
    }

    @Test
    fun getLiveTimesSingleNaptanReturnsOtherServerErrorOnAnyOtherServerError() = runTest {
        val exceptionLogger = FakeExceptionLogger()
        val endpoint = createEndpoint(
            api = FakeEdinburghOpenApi(
                onGetStopEventsWithSmsCode = { smsCode, numberOfDepartures ->
                    assertEquals("123456", smsCode)
                    assertEquals(4, numberOfDepartures)
                    Response.error(404, "Not found".toResponseBody())
                }
            ),
            connectivityRepository = connectivityRepositoryHasConnectivity,
            exceptionLogger = exceptionLogger
        )

        val result = endpoint.getLiveTimes(
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            numberOfDepartures = 4
        )

        assertEquals(
            LiveTimesResponse.Error.ServerError.Other(error = "Not found"),
            result
        )
        assertEquals(1, exceptionLogger.loggedThrowables.size)
        assertIs<RuntimeException>(exceptionLogger.loggedThrowables.last())
    }

    @Test
    fun getLiveTimesSingleAtcoReturnsSuccessWithEmptyLiveTimesWhenBodyIsNull() = runTest {
        val endpoint = createEndpoint(
            api = FakeEdinburghOpenApi(
                onGetStopEventsWithAtcoCode = { atcoCode, numberOfDepartures ->
                    assertEquals("123456", atcoCode)
                    assertEquals(4, numberOfDepartures)
                    Response.success(null)
                }
            ),
            connectivityRepository = connectivityRepositoryHasConnectivity,
            timeUtils = FakeTimeUtils(
                onNow = { Instant.fromEpochMilliseconds(123L) }
            )
        )

        val result = endpoint.getLiveTimes(
            stopIdentifier = "123456".toAtcoStopIdentifier(),
            numberOfDepartures = 4
        )

        assertEquals(
            LiveTimesResponse.Success(
                liveTimes = emptyLiveTimes(
                    receiveTime = Instant.fromEpochMilliseconds(123L)
                )
            ),
            result
        )
    }

    @Test
    fun getLiveTimesSingleNaptanReturnsSuccessWithEmptyLiveTimesWhenBodyIsNull() = runTest {
        val endpoint = createEndpoint(
            api = FakeEdinburghOpenApi(
                onGetStopEventsWithSmsCode = { smsCode, numberOfDepartures ->
                    assertEquals("123456", smsCode)
                    assertEquals(4, numberOfDepartures)
                    Response.success(null)
                }
            ),
            connectivityRepository = connectivityRepositoryHasConnectivity,
            timeUtils = FakeTimeUtils(
                onNow = { Instant.fromEpochMilliseconds(123L) }
            )
        )

        val result = endpoint.getLiveTimes(
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            numberOfDepartures = 4
        )

        assertEquals(
            LiveTimesResponse.Success(
                liveTimes = emptyLiveTimes(
                    receiveTime = Instant.fromEpochMilliseconds(123L)
                )
            ),
            result
        )
    }

    @Test
    fun getLiveTimesSingleAtcoReturnsSuccessWithLiveTimesWhenBodyIsPopulated() = runTest {
        val endpoint = createEndpoint(
            api = FakeEdinburghOpenApi(
                onGetStopEventsWithAtcoCode = { atcoCode, numberOfDepartures ->
                    assertEquals("123456", atcoCode)
                    assertEquals(4, numberOfDepartures)
                    Response.success(
                        JsonStopEvents(
                            time = Instant.fromEpochMilliseconds(122L),
                            events = listOf(
                                JsonStopEvent(
                                    publicServiceName = "100",
                                    operator = "TEST1",
                                    destination = "Destination",
                                    scheduledDepartureTime = LocalTime(hour = 12, minute = 34),
                                    departureTime = ArrivalDepartureTime.Seconds(999)
                                )
                            )
                        )
                    )
                }
            ),
            connectivityRepository = connectivityRepositoryHasConnectivity,
            timeUtils = FakeTimeUtils(
                onNow = { Instant.fromEpochMilliseconds(123L) }
            )
        )

        val result = endpoint.getLiveTimes(
            stopIdentifier = "123456".toAtcoStopIdentifier(),
            numberOfDepartures = 4
        )

        assertEquals(
            LiveTimesResponse.Success(
                liveTimes = LiveTimes(
                    stops = mapOf(
                        "123456".toAtcoStopIdentifier() to Stop(
                            stopIdentifier = "123456".toAtcoStopIdentifier(),
                            services = listOf(
                                Service(
                                    serviceDescriptor = FakeServiceDescriptor(
                                        serviceName = "100",
                                        operatorCode = "TEST1"
                                    ),
                                    vehicles = listOf(
                                        Vehicle(
                                            destination = "Destination",
                                            departureTime = Instant.fromEpochMilliseconds(122L) +
                                                999.seconds,
                                            departureMinutes = 16,
                                            isEstimatedTime = false,
                                            isDiverted = false
                                        )
                                    )
                                )
                            )
                        )
                    ),
                    receiveTime = Instant.fromEpochMilliseconds(123L)
                )
            ),
            result
        )
    }

    @Test
    fun getLiveTimesSingleNaptanReturnsSuccessWithLiveTimesWhenBodyIsPopulated() = runTest {
        val endpoint = createEndpoint(
            api = FakeEdinburghOpenApi(
                onGetStopEventsWithSmsCode = { smsCode, numberOfDepartures ->
                    assertEquals("123456", smsCode)
                    assertEquals(4, numberOfDepartures)
                    Response.success(
                        JsonStopEvents(
                            time = Instant.fromEpochMilliseconds(122L),
                            events = listOf(
                                JsonStopEvent(
                                    publicServiceName = "100",
                                    operator = "TEST1",
                                    destination = "Destination",
                                    scheduledDepartureTime = LocalTime(hour = 12, minute = 34),
                                    departureTime = ArrivalDepartureTime.Seconds(999)
                                )
                            )
                        )
                    )
                }
            ),
            connectivityRepository = connectivityRepositoryHasConnectivity,
            timeUtils = FakeTimeUtils(
                onNow = { Instant.fromEpochMilliseconds(123L) }
            )
        )

        val result = endpoint.getLiveTimes(
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            numberOfDepartures = 4
        )

        assertEquals(
            LiveTimesResponse.Success(
                liveTimes = LiveTimes(
                    stops = mapOf(
                        "123456".toNaptanStopIdentifier() to Stop(
                            stopIdentifier = "123456".toNaptanStopIdentifier(),
                            services = listOf(
                                Service(
                                    serviceDescriptor = FakeServiceDescriptor(
                                        serviceName = "100",
                                        operatorCode = "TEST1"
                                    ),
                                    vehicles = listOf(
                                        Vehicle(
                                            destination = "Destination",
                                            departureTime = Instant.fromEpochMilliseconds(122L) +
                                                999.seconds,
                                            departureMinutes = 16,
                                            isEstimatedTime = false,
                                            isDiverted = false
                                        )
                                    )
                                )
                            )
                        )
                    ),
                    receiveTime = Instant.fromEpochMilliseconds(123L)
                )
            ),
            result
        )
    }

    private val connectivityRepositoryHasConnectivity get() = FakeConnectivityRepository(
        onHasInternetConnectivity = { true }
    )

    private fun createEndpoint(
        api: EdinburghOpenApi = FakeEdinburghOpenApi(),
        connectivityRepository: ConnectivityRepository = FakeConnectivityRepository(),
        exceptionLogger: ExceptionLogger = FakeExceptionLogger(),
        timeUtils: TimeUtils = FakeTimeUtils()
    ): EdinburghTrackerEndpoint {
        return EdinburghTrackerEndpoint(
            api = api,
            connectivityRepository = connectivityRepository,
            exceptionLogger = exceptionLogger,
            timeUtils = timeUtils
        )
    }
}
