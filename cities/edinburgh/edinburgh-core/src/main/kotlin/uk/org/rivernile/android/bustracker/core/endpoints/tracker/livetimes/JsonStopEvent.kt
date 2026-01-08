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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * This describes an event from the events API. An event is usually an arrival/departure.
 *
 * @property publicServiceName The publicly displayed name of the service.
 * @property destination The destination of the service.
 * @property scheduledDepartureTime What is the scheduled (timetabled) time of the departure?
 * @property departureTime The departure time. See [ArrivalDepartureTime] for more information.
 * @author Niall Scott
 */
@Serializable
internal data class JsonStopEvent(
    @SerialName("publicservicename") val publicServiceName: String? = null,
    @SerialName("destination") val destination: String? = null,
    @SerialName("scheduledeparturetime") val scheduledDepartureTime: LocalTime? = null,
    @SerialName("departuretime") val departureTime: ArrivalDepartureTime? = null
)

/**
 * The arrival and departure time fields can be either an integer number of seconds under the event,
 * or a string in `HH:mm` format representing the clock time the event will occur at. This sealed
 * interface encapsulates these possibilities for custom serialisation/deserialisation.
 */
@Serializable(with = EventTimeSerialiser::class)
internal sealed interface ArrivalDepartureTime {

    /**
     * The field is an integer, indicating a number of minutes until the event.
     */
    @JvmInline
    value class Seconds(
        val seconds: Int
    ) : ArrivalDepartureTime

    /**
     * The field is a string in the `HH:mm` format. So we use a [LocalTime] for this.
     */
    @JvmInline
    value class ClockTime(
        val clockTime: LocalTime
    ): ArrivalDepartureTime
}

private object EventTimeSerialiser : KSerializer<ArrivalDepartureTime> {

    override val descriptor = PrimitiveSerialDescriptor(
        serialName = checkNotNull(ArrivalDepartureTime::class.qualifiedName),
        kind = PrimitiveKind.STRING
    )

    override fun deserialize(decoder: Decoder): ArrivalDepartureTime {
        val jsonDecoder = decoder as JsonDecoder
        val element = jsonDecoder.decodeJsonElement()
        val primitive = element.jsonPrimitive

        return when {
            primitive.isString -> ArrivalDepartureTime.ClockTime(
                clockTime = try {
                    LocalTime.parse(primitive.content)
                } catch (e: IllegalArgumentException) {
                    throw SerializationException(e)
                }
            )
            primitive.intOrNull != null -> ArrivalDepartureTime.Seconds(seconds = primitive.int)
            else -> throw SerializationException("Invalid value: $element")
        }
    }

    override fun serialize(encoder: Encoder, value: ArrivalDepartureTime) {
        when (value) {
            is ArrivalDepartureTime.Seconds -> encoder.encodeInt(value.seconds)
            is ArrivalDepartureTime.ClockTime -> encoder.encodeString(value.clockTime.toString())
        }
    }
}

/**
 * Maps a [JsonStopEvent] to a [Vehicle], or `null` if this fails.
 *
 * @param serverTime The server time to compare any timestamps against to perform time calculations.
 * @param timeZone The time zone for which the live times are based in.
 */
internal fun JsonStopEvent.toVehicleOrNull(
    serverTime: Instant,
    timeZone: TimeZone
): Vehicle? {
    val isEstimatedTime: Boolean

    val departureTime = when (departureTime) {
        is ArrivalDepartureTime.Seconds -> {
            // When the departure time is set as seconds, then we add those seconds on to the server
            // time to get the expected time of arrival.
            isEstimatedTime = false
            serverTime + departureTime.seconds.seconds
        }
        is ArrivalDepartureTime.ClockTime -> {
            // When the departure time is set as a clock time, then we need to find when that clock
            // time next occurs after the server time. This could wrap to the next day.
            isEstimatedTime = false
            departureTime.clockTime.nextOccurrenceAfterInstant(
                instant = serverTime,
                timeZone = timeZone
            )
        }
        null -> {
            // When the departure time has not been given, we need to fall back to the scheduled
            // departure time and mark the time as estimated. The scheduled departure time is always
            // in HH:mm format, so we need to find out when that clock time next occurs after the
            // given server time. This could wrap to the next day.
            isEstimatedTime = true
            scheduledDepartureTime?.nextOccurrenceAfterInstant(
                instant = serverTime,
                timeZone = timeZone
            )
        }
    }

    // If we have no departure time at all for this vehicle, then it's useless to us, so we will
    // return null in this case.
    return departureTime?.let {
        Vehicle(
            destination = destination,
            departureTime = it,
            departureMinutes = (it - serverTime).inWholeMinutes.toInt(),
            isEstimatedTime = isEstimatedTime,
            isDiverted = false
        )
    }
}

private fun LocalTime.nextOccurrenceAfterInstant(
    instant: Instant,
    timeZone: TimeZone
): Instant {
    val instantAsDateTime = instant.toLocalDateTime(timeZone)

    val todayAtTarget = LocalDateTime(
        year = instantAsDateTime.year,
        month = instantAsDateTime.month,
        day = instantAsDateTime.day,
        hour = hour,
        minute = minute,
        second = second
    )

    val todayAtTargetInstant = todayAtTarget.toInstant(timeZone)

    return if (todayAtTarget < instantAsDateTime) {
        todayAtTargetInstant + 1.days
    } else {
        todayAtTargetInstant
    }
}
