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
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for `JsonStopEvent.kt`.
 *
 * @author Niall Scott
 */
class JsonStopEventKtTest {

    @Test
    fun jsonStopEventDeserialisesWithMissingDepartureTime() {
        val event = Json.decodeFromString<JsonStopEvent>("""
            { }
        """.trimIndent())

        assertEquals(
            JsonStopEvent(),
            event
        )
    }

    @Test
    fun jsonStopEventDeserialisesWithNullDepartureTime() {
        val event = Json.decodeFromString<JsonStopEvent>("""
            { "departuretime": null }
        """.trimIndent())

        assertEquals(
            JsonStopEvent(),
            event
        )
    }

    @Test
    fun jsonStopEventDeserialisesIntsInToSecondsArrivalDepartureTime() {
        val event = Json.decodeFromString<JsonStopEvent>("""
            { "departuretime": 99 }
        """.trimIndent())

        assertEquals(
            JsonStopEvent(
                departureTime = ArrivalDepartureTime.Seconds(seconds = 99)
            ),
            event
        )
    }

    @Test
    fun jsonStopEventDeserialisesStringsIntoClockTimeArrivalDepartureTime() {
        val event = Json.decodeFromString<JsonStopEvent>("""
            { "departuretime": "12:34" }
        """.trimIndent())

        assertEquals(
            JsonStopEvent(
                departureTime = ArrivalDepartureTime.ClockTime(
                    clockTime = LocalTime(hour = 12, minute = 34)
                )
            ),
            event
        )
    }

    @Test(expected = SerializationException::class)
    fun jsonStopEventThrowsExceptionWhenDepartureTimeIsUnknownType() {
        Json.decodeFromString<JsonStopEvent>("""
            { "departuretime": true }
        """.trimIndent())
    }

    @Test(expected = SerializationException::class)
    fun jsonStopEventThrowsExceptionWhenStringIsInInvalidFormat() {
        Json.decodeFromString<JsonStopEvent>("""
            { "departuretime": "test" }
        """.trimIndent())
    }

    @Test
    fun toVehicleOrNullWithNoDepartureOrScheduledDepartureTimesReturnsNull() {
        val result = JsonStopEvent(
            publicServiceName = "1",
            destination = "Destination",
            scheduledDepartureTime = null,
            departureTime = null
        ).toVehicleOrNull(
            serverTime = LocalDateTime(
                year = 2026,
                month = Month.JANUARY,
                day = 8,
                hour = 12,
                minute = 34
            ).toInstant(TimeZone.UTC),
            timeZone = TimeZone.UTC
        )

        assertNull(result)
    }

    @Test
    fun toVehicleOrNullWithNoScheduledDepartureButHasRealDepartureReturnsValues() {
        val result = JsonStopEvent(
            publicServiceName = "1",
            destination = "Destination",
            scheduledDepartureTime = null,
            departureTime = ArrivalDepartureTime.Seconds(seconds = 120)
        ).toVehicleOrNull(
            serverTime = LocalDateTime(
                year = 2026,
                month = Month.JANUARY,
                day = 8,
                hour = 12,
                minute = 34
            ).toInstant(TimeZone.UTC),
            timeZone = TimeZone.UTC
        )

        assertEquals(
            Vehicle(
                destination = "Destination",
                departureTime = LocalDateTime(
                    year = 2026,
                    month = Month.JANUARY,
                    day = 8,
                    hour = 12,
                    minute = 36
                ).toInstant(TimeZone.UTC),
                departureMinutes = 2,
                isEstimatedTime = false,
                isDiverted = false
            ),
            result
        )
    }

    @Test
    fun toVehicleOrNullWithNoDepartureUsesScheduledTime() {
        val result = JsonStopEvent(
            publicServiceName = "1",
            destination = "Destination",
            scheduledDepartureTime = LocalTime(hour = 12, minute = 48),
            departureTime = null
        ).toVehicleOrNull(
            serverTime = LocalDateTime(
                year = 2026,
                month = Month.JANUARY,
                day = 8,
                hour = 12,
                minute = 34
            ).toInstant(TimeZone.UTC),
            timeZone = TimeZone.UTC
        )

        assertEquals(
            Vehicle(
                destination = "Destination",
                departureTime = LocalDateTime(
                    year = 2026,
                    month = Month.JANUARY,
                    day = 8,
                    hour = 12,
                    minute = 48
                ).toInstant(TimeZone.UTC),
                departureMinutes = 14,
                isEstimatedTime = true,
                isDiverted = false
            ),
            result
        )
    }

    @Test
    fun toVehicleOrNullWithDepartureInSecondsUsesRealTime() {
        val result = JsonStopEvent(
            publicServiceName = "1",
            destination = "Destination",
            scheduledDepartureTime = LocalTime(hour = 12, minute = 40),
            departureTime = ArrivalDepartureTime.Seconds(seconds = 330)
        ).toVehicleOrNull(
            serverTime = LocalDateTime(
                year = 2026,
                month = Month.JANUARY,
                day = 8,
                hour = 12,
                minute = 34
            ).toInstant(TimeZone.UTC),
            timeZone = TimeZone.UTC
        )

        assertEquals(
            Vehicle(
                destination = "Destination",
                departureTime = LocalDateTime(
                    year = 2026,
                    month = Month.JANUARY,
                    day = 8,
                    hour = 12,
                    minute = 39,
                    second = 30
                ).toInstant(TimeZone.UTC),
                departureMinutes = 5,
                isEstimatedTime = false,
                isDiverted = false
            ),
            result
        )
    }

    @Test
    fun toVehicleOrNullWithDepartureInClockTimeUsesRealTime() {
        val result = JsonStopEvent(
            publicServiceName = "1",
            destination = "Destination",
            scheduledDepartureTime = LocalTime(hour = 12, minute = 40),
            departureTime = ArrivalDepartureTime.ClockTime(
                clockTime = LocalTime(hour = 12, minute = 41)
            )
        ).toVehicleOrNull(
            serverTime = LocalDateTime(
                year = 2026,
                month = Month.JANUARY,
                day = 8,
                hour = 12,
                minute = 34
            ).toInstant(TimeZone.UTC),
            timeZone = TimeZone.UTC
        )

        assertEquals(
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
                isEstimatedTime = false,
                isDiverted = false
            ),
            result
        )
    }

    @Test
    fun toVehicleOrNullWithNoDepartureTimeHandlesClockDayWrapping() {
        val result = JsonStopEvent(
            publicServiceName = "1",
            destination = "Destination",
            scheduledDepartureTime = LocalTime(hour = 1, minute = 9),
            departureTime = null
        ).toVehicleOrNull(
            serverTime = LocalDateTime(
                year = 2026,
                month = Month.JANUARY,
                day = 8,
                hour = 23,
                minute = 50
            ).toInstant(TimeZone.UTC),
            timeZone = TimeZone.UTC
        )

        assertEquals(
            Vehicle(
                destination = "Destination",
                departureTime = LocalDateTime(
                    year = 2026,
                    month = Month.JANUARY,
                    day = 9,
                    hour = 1,
                    minute = 9
                ).toInstant(TimeZone.UTC),
                departureMinutes = 79,
                isEstimatedTime = true,
                isDiverted = false
            ),
            result
        )
    }

    @Test
    fun toVehicleOrNullWithDepartureInSecondsHandlesClockDayWrapping() {
        val result = JsonStopEvent(
            publicServiceName = "1",
            destination = "Destination",
            scheduledDepartureTime = LocalTime(hour = 1, minute = 5),
            departureTime = ArrivalDepartureTime.Seconds(seconds = 4770)
        ).toVehicleOrNull(
            serverTime = LocalDateTime(
                year = 2026,
                month = Month.JANUARY,
                day = 8,
                hour = 23,
                minute = 50
            ).toInstant(TimeZone.UTC),
            timeZone = TimeZone.UTC
        )

        assertEquals(
            Vehicle(
                destination = "Destination",
                departureTime = LocalDateTime(
                    year = 2026,
                    month = Month.JANUARY,
                    day = 9,
                    hour = 1,
                    minute = 9,
                    second = 30
                ).toInstant(TimeZone.UTC),
                departureMinutes = 79,
                isEstimatedTime = false,
                isDiverted = false
            ),
            result
        )
    }

    @Test
    fun toVehicleOrNullWithDepartureInClockTimeHandlesClockDayWrapping() {
        val result = JsonStopEvent(
            publicServiceName = "1",
            destination = "Destination",
            scheduledDepartureTime = LocalTime(hour = 1, minute = 5),
            departureTime = ArrivalDepartureTime.ClockTime(
                clockTime = LocalTime(hour = 1, minute = 9)
            )
        ).toVehicleOrNull(
            serverTime = LocalDateTime(
                year = 2026,
                month = Month.JANUARY,
                day = 8,
                hour = 23,
                minute = 50
            ).toInstant(TimeZone.UTC),
            timeZone = TimeZone.UTC
        )

        assertEquals(
            Vehicle(
                destination = "Destination",
                departureTime = LocalDateTime(
                    year = 2026,
                    month = Month.JANUARY,
                    day = 9,
                    hour = 1,
                    minute = 9
                ).toInstant(TimeZone.UTC),
                departureMinutes = 79,
                isEstimatedTime = false,
                isDiverted = false
            ),
            result
        )
    }
}
