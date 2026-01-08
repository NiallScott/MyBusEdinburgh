/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
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

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/**
 * Tests for `JsonStopEvents.kt`.
 *
 * @author Niall Scott
 */
class JsonStopEventsKtTest {

    @Test
    fun toLiveTimesReturnsEmptyLiveTimesWhenTimeIsNull() {
        val result = JsonStopEvents(
            time = null,
            events = listOf(
                JsonStopEvent(
                    publicServiceName = "1",
                    destination = "Destination",
                    scheduledDepartureTime = LocalTime(hour = 12, minute = 34),
                    departureTime = ArrivalDepartureTime.Seconds(seconds = 999)
                )
            )
        ).toLiveTimes(
            stopCode = "123456",
            numberOfDepartures = 4,
            receiveTime = Instant.fromEpochMilliseconds(123L),
            timeZone = TimeZone.UTC
        )

        assertEquals(
            emptyLiveTimes(receiveTime = Instant.fromEpochMilliseconds(123L)),
            result
        )
    }

    @Test
    fun toLiveTimesReturnsEmptyLiveTimesWhenEventsAreNull() {
        val result = JsonStopEvents(
            time = Instant.fromEpochMilliseconds(456L),
            events = null
        ).toLiveTimes(
            stopCode = "123456",
            numberOfDepartures = 4,
            receiveTime = Instant.fromEpochMilliseconds(123L),
            timeZone = TimeZone.UTC
        )

        assertEquals(
            emptyLiveTimes(receiveTime = Instant.fromEpochMilliseconds(123L)),
            result
        )
    }

    @Test
    fun toLiveTimesReturnsEmptyLiveTimesWhenEventsAreEmpty() {
        val result = JsonStopEvents(
            time = Instant.fromEpochMilliseconds(456L),
            events = emptyList()
        ).toLiveTimes(
            stopCode = "123456",
            numberOfDepartures = 4,
            receiveTime = Instant.fromEpochMilliseconds(123L),
            timeZone = TimeZone.UTC
        )

        assertEquals(
            emptyLiveTimes(receiveTime = Instant.fromEpochMilliseconds(123L)),
            result
        )
    }

    @Test
    fun toLiveTimesReturnsEmptyLiveTimesWhenNoEventsSuccessfullyMapped() {
        val result = JsonStopEvents(
            time = Instant.fromEpochMilliseconds(456L),
            events = listOf(JsonStopEvent())
        ).toLiveTimes(
            stopCode = "123456",
            numberOfDepartures = 4,
            receiveTime = Instant.fromEpochMilliseconds(123L),
            timeZone = TimeZone.UTC
        )

        assertEquals(
            emptyLiveTimes(receiveTime = Instant.fromEpochMilliseconds(123L)),
            result
        )
    }

    @Test
    fun toLiveTimesReturnsPopulatedLiveTimesWhenSingleEventSuccessfullyMapped() {
        val result = JsonStopEvents(
            time = LocalDateTime(
                year = 2026,
                month = Month.JANUARY,
                day = 8,
                hour = 12,
                minute = 34
            ).toInstant(TimeZone.UTC),
            events = listOf(
                JsonStopEvent(
                    publicServiceName = "1",
                    destination = "Destination",
                    scheduledDepartureTime = LocalTime(hour = 12, minute = 40),
                    departureTime = null
                )
            )
        ).toLiveTimes(
            stopCode = "123456",
            numberOfDepartures = 4,
            receiveTime = Instant.fromEpochMilliseconds(123L),
            timeZone = TimeZone.UTC
        )

        assertEquals(
            LiveTimes(
                stops = mapOf(
                    "123456" to Stop(
                        stopCode = "123456",
                        services = listOf(
                            Service(
                                serviceName = "1",
                                vehicles = listOf(
                                    Vehicle(
                                        destination = "Destination",
                                        departureTime = LocalDateTime(
                                            year = 2026,
                                            month = Month.JANUARY,
                                            day = 8,
                                            hour = 12,
                                            minute = 40
                                        ).toInstant(TimeZone.UTC),
                                        departureMinutes = 6,
                                        isEstimatedTime = true,
                                        isDiverted = false
                                    )
                                )
                            )
                        )
                    )
                ),
                receiveTime = Instant.fromEpochMilliseconds(123L)
            ),
            result
        )
    }

    @Test
    fun toLiveTimesTakesMaximumOfNumberOfDeparturesForService() {
        val result = JsonStopEvents(
            time = LocalDateTime(
                year = 2026,
                month = Month.JANUARY,
                day = 8,
                hour = 12,
                minute = 34
            ).toInstant(TimeZone.UTC),
            events = listOf(
                JsonStopEvent(
                    publicServiceName = "1",
                    destination = "Destination",
                    scheduledDepartureTime = LocalTime(hour = 12, minute = 40),
                    departureTime = null
                ),
                JsonStopEvent(
                    publicServiceName = "1",
                    destination = "Destination",
                    scheduledDepartureTime = LocalTime(hour = 12, minute = 39),
                    departureTime = null
                ),
                JsonStopEvent(
                    publicServiceName = "1",
                    destination = "Destination",
                    scheduledDepartureTime = LocalTime(hour = 12, minute = 42),
                    departureTime = null
                ),
                JsonStopEvent(
                    publicServiceName = "1",
                    destination = "Destination",
                    scheduledDepartureTime = LocalTime(hour = 12, minute = 41),
                    departureTime = null
                )
            )
        ).toLiveTimes(
            stopCode = "123456",
            numberOfDepartures = 2,
            receiveTime = Instant.fromEpochMilliseconds(123L),
            timeZone = TimeZone.UTC
        )

        assertEquals(
            LiveTimes(
                stops = mapOf(
                    "123456" to Stop(
                        stopCode = "123456",
                        services = listOf(
                            Service(
                                serviceName = "1",
                                vehicles = listOf(
                                    Vehicle(
                                        destination = "Destination",
                                        departureTime = LocalDateTime(
                                            year = 2026,
                                            month = Month.JANUARY,
                                            day = 8,
                                            hour = 12,
                                            minute = 39
                                        ).toInstant(TimeZone.UTC),
                                        departureMinutes = 5,
                                        isEstimatedTime = true,
                                        isDiverted = false
                                    ),
                                    Vehicle(
                                        destination = "Destination",
                                        departureTime = LocalDateTime(
                                            year = 2026,
                                            month = Month.JANUARY,
                                            day = 8,
                                            hour = 12,
                                            minute = 40
                                        ).toInstant(TimeZone.UTC),
                                        departureMinutes = 6,
                                        isEstimatedTime = true,
                                        isDiverted = false
                                    )
                                )
                            )
                        )
                    )
                ),
                receiveTime = Instant.fromEpochMilliseconds(123L)
            ),
            result
        )
    }

    @Test
    fun toLiveTimesExcludesEventsWhichDoNotMap() {
        val result = JsonStopEvents(
            time = LocalDateTime(
                year = 2026,
                month = Month.JANUARY,
                day = 8,
                hour = 12,
                minute = 34
            ).toInstant(TimeZone.UTC),
            events = listOf(
                JsonStopEvent(
                    publicServiceName = null,
                    destination = "Destination",
                    scheduledDepartureTime = LocalTime(hour = 12, minute = 40),
                    departureTime = null
                ),
                JsonStopEvent(
                    publicServiceName = "",
                    destination = "Destination",
                    scheduledDepartureTime = LocalTime(hour = 12, minute = 40),
                    departureTime = null
                ),
                JsonStopEvent(
                    publicServiceName = "1",
                    destination = "Destination",
                    scheduledDepartureTime = LocalTime(hour = 12, minute = 39),
                    departureTime = null
                ),
                JsonStopEvent(
                    publicServiceName = "1",
                    destination = "Destination",
                    scheduledDepartureTime = null,
                    departureTime = null
                ),
                JsonStopEvent(
                    publicServiceName = "1",
                    destination = "Destination",
                    scheduledDepartureTime = LocalTime(hour = 12, minute = 41),
                    departureTime = null
                )
            )
        ).toLiveTimes(
            stopCode = "123456",
            numberOfDepartures = 4,
            receiveTime = Instant.fromEpochMilliseconds(123L),
            timeZone = TimeZone.UTC
        )

        assertEquals(
            LiveTimes(
                stops = mapOf(
                    "123456" to Stop(
                        stopCode = "123456",
                        services = listOf(
                            Service(
                                serviceName = "1",
                                vehicles = listOf(
                                    Vehicle(
                                        destination = "Destination",
                                        departureTime = LocalDateTime(
                                            year = 2026,
                                            month = Month.JANUARY,
                                            day = 8,
                                            hour = 12,
                                            minute = 39
                                        ).toInstant(TimeZone.UTC),
                                        departureMinutes = 5,
                                        isEstimatedTime = true,
                                        isDiverted = false
                                    ),
                                    Vehicle(
                                        destination = "Destination",
                                        departureTime = LocalDateTime(
                                            year = 2026,
                                            month = Month.JANUARY,
                                            day = 8,
                                            hour = 12,
                                            minute = 41
                                        ).toInstant(TimeZone.UTC),
                                        departureMinutes = 7,
                                        isEstimatedTime = true,
                                        isDiverted = false
                                    )
                                )
                            )
                        )
                    )
                ),
                receiveTime = Instant.fromEpochMilliseconds(123L)
            ),
            result
        )
    }

    @Test
    fun toLiveTimeCopesWithMultipleServicesWithMultipleVehicles() {
        val result = JsonStopEvents(
            time = LocalDateTime(
                year = 2026,
                month = Month.JANUARY,
                day = 8,
                hour = 12,
                minute = 34
            ).toInstant(TimeZone.UTC),
            events = listOf(
                JsonStopEvent(
                    publicServiceName = "1",
                    destination = "Destination",
                    scheduledDepartureTime = LocalTime(hour = 12, minute = 40),
                    departureTime = null
                ),
                JsonStopEvent(
                    publicServiceName = "2",
                    destination = "Destination",
                    scheduledDepartureTime = LocalTime(hour = 12, minute = 41),
                    departureTime = null
                ),
                JsonStopEvent(
                    publicServiceName = "3",
                    destination = "Destination",
                    scheduledDepartureTime = LocalTime(hour = 12, minute = 45),
                    departureTime = null
                ),
                JsonStopEvent(
                    publicServiceName = "1",
                    destination = "Destination",
                    scheduledDepartureTime = LocalTime(hour = 12, minute = 42),
                    departureTime = null
                ),
                JsonStopEvent(
                    publicServiceName = "2",
                    destination = "Destination",
                    scheduledDepartureTime = LocalTime(hour = 12, minute = 43),
                    departureTime = null
                ),
                JsonStopEvent(
                    publicServiceName = "1",
                    destination = "Destination",
                    scheduledDepartureTime = LocalTime(hour = 12, minute = 44),
                    departureTime = null
                )
            )
        ).toLiveTimes(
            stopCode = "123456",
            numberOfDepartures = 4,
            receiveTime = Instant.fromEpochMilliseconds(123L),
            timeZone = TimeZone.UTC
        )

        assertEquals(
            LiveTimes(
                stops = mapOf(
                    "123456" to Stop(
                        stopCode = "123456",
                        services = listOf(
                            Service(
                                serviceName = "1",
                                vehicles = listOf(
                                    Vehicle(
                                        destination = "Destination",
                                        departureTime = LocalDateTime(
                                            year = 2026,
                                            month = Month.JANUARY,
                                            day = 8,
                                            hour = 12,
                                            minute = 40
                                        ).toInstant(TimeZone.UTC),
                                        departureMinutes = 6,
                                        isEstimatedTime = true,
                                        isDiverted = false
                                    ),
                                    Vehicle(
                                        destination = "Destination",
                                        departureTime = LocalDateTime(
                                            year = 2026,
                                            month = Month.JANUARY,
                                            day = 8,
                                            hour = 12,
                                            minute = 42
                                        ).toInstant(TimeZone.UTC),
                                        departureMinutes = 8,
                                        isEstimatedTime = true,
                                        isDiverted = false
                                    ),
                                    Vehicle(
                                        destination = "Destination",
                                        departureTime = LocalDateTime(
                                            year = 2026,
                                            month = Month.JANUARY,
                                            day = 8,
                                            hour = 12,
                                            minute = 44
                                        ).toInstant(TimeZone.UTC),
                                        departureMinutes = 10,
                                        isEstimatedTime = true,
                                        isDiverted = false
                                    )
                                )
                            ),
                            Service(
                                serviceName = "2",
                                vehicles = listOf(
                                    Vehicle(
                                        destination = "Destination",
                                        departureTime = LocalDateTime(
                                            year = 2026,
                                            month = Month.JANUARY,
                                            day = 8,
                                            hour = 12,
                                            minute = 41
                                        ).toInstant(TimeZone.UTC),
                                        departureMinutes = 7,
                                        isEstimatedTime = true,
                                        isDiverted = false
                                    ),
                                    Vehicle(
                                        destination = "Destination",
                                        departureTime = LocalDateTime(
                                            year = 2026,
                                            month = Month.JANUARY,
                                            day = 8,
                                            hour = 12,
                                            minute = 43
                                        ).toInstant(TimeZone.UTC),
                                        departureMinutes = 9,
                                        isEstimatedTime = true,
                                        isDiverted = false
                                    )
                                )
                            ),
                            Service(
                                serviceName = "3",
                                vehicles = listOf(
                                    Vehicle(
                                        destination = "Destination",
                                        departureTime = LocalDateTime(
                                            year = 2026,
                                            month = Month.JANUARY,
                                            day = 8,
                                            hour = 12,
                                            minute = 45
                                        ).toInstant(TimeZone.UTC),
                                        departureMinutes = 11,
                                        isEstimatedTime = true,
                                        isDiverted = false
                                    )
                                )
                            )
                        )
                    )
                ),
                receiveTime = Instant.fromEpochMilliseconds(123L)
            ),
            result
        )
    }

    @Test
    fun toLiveTimesConvertsT50ServiceToTram() {
        val result = JsonStopEvents(
            time = LocalDateTime(
                year = 2026,
                month = Month.JANUARY,
                day = 8,
                hour = 12,
                minute = 34
            ).toInstant(TimeZone.UTC),
            events = listOf(
                JsonStopEvent(
                    publicServiceName = "T50",
                    destination = "Destination",
                    scheduledDepartureTime = LocalTime(hour = 12, minute = 40),
                    departureTime = null
                )
            )
        ).toLiveTimes(
            stopCode = "123456",
            numberOfDepartures = 4,
            receiveTime = Instant.fromEpochMilliseconds(123L),
            timeZone = TimeZone.UTC
        )

        assertEquals(
            LiveTimes(
                stops = mapOf(
                    "123456" to Stop(
                        stopCode = "123456",
                        services = listOf(
                            Service(
                                serviceName = "TRAM",
                                vehicles = listOf(
                                    Vehicle(
                                        destination = "Destination",
                                        departureTime = LocalDateTime(
                                            year = 2026,
                                            month = Month.JANUARY,
                                            day = 8,
                                            hour = 12,
                                            minute = 40
                                        ).toInstant(TimeZone.UTC),
                                        departureMinutes = 6,
                                        isEstimatedTime = true,
                                        isDiverted = false
                                    )
                                )
                            )
                        )
                    )
                ),
                receiveTime = Instant.fromEpochMilliseconds(123L)
            ),
            result
        )
    }
}
