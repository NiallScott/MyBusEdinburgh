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

import kotlinx.datetime.TimeZone
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import kotlin.time.Instant

/**
 * This describes a holder for events from the events API. An event is usually an arrival/departure.
 *
 * @property time The time on the server that the response was generated at.
 * @property events The [List] of [JsonStopEvent]s.
 * @author Niall Scott
 */
@Serializable
internal data class JsonStopEvents(
    @SerialName("time") val time: Instant? = null,
    @SerialName("events") val events: List<JsonStopEvent>? = null
)

/**
 * Map a [JsonStopEvents] to [LiveTimes].
 *
 * @param stopIdentifier The requested stop identifier of the live times.
 * @param numberOfDepartures The number of departures per service requested. This is used to
 * only display at most this number of services per service.
 * @param receiveTime The time the data was received from the server.
 * @param timeZone The time zone the live times pertain to.
 * @return This [JsonStopEvents] mapped to a [LiveTimes].
 */
internal fun JsonStopEvents.toLiveTimes(
    stopIdentifier: StopIdentifier,
    numberOfDepartures: Int,
    receiveTime: Instant,
    timeZone: TimeZone
): LiveTimes {
    return if (time != null) {
        toStopOrNull(
            stopIdentifier = stopIdentifier,
            numberOfDepartures = numberOfDepartures,
            serverTime = time,
            timeZone = timeZone
        )?.let {
            LiveTimes(
                stops = mapOf(stopIdentifier to it),
                receiveTime = receiveTime
            )
        } ?: emptyLiveTimes(receiveTime)
    } else {
        emptyLiveTimes(receiveTime)
    }
}

private fun JsonStopEvents.toStopOrNull(
    stopIdentifier: StopIdentifier,
    numberOfDepartures: Int,
    serverTime: Instant,
    timeZone: TimeZone
): Stop? {
    return events
        ?.toServicesOrNull(
            numberOfDepartures = numberOfDepartures,
            serverTime = serverTime,
            timeZone = timeZone
        )?.let {
            Stop(
                stopIdentifier = stopIdentifier,
                services = it
            )
        }
}

private fun List<JsonStopEvent>.toServicesOrNull(
    numberOfDepartures: Int,
    serverTime: Instant,
    timeZone: TimeZone
): List<Service>? {
    return if (isNotEmpty()) {
        val serviceVehicles = mutableMapOf<ServiceDescriptor, MutableList<Vehicle>>()

        forEach { event ->
            val serviceName = event.publicServiceName
            val operatorCode = event.operator

            if (!serviceName.isNullOrBlank() && !operatorCode.isNullOrBlank()) {
                val vehicle = event.toVehicleOrNull(
                    serverTime = serverTime,
                    timeZone = timeZone
                )

                if (vehicle != null) {
                    val serviceDescriptor = ServiceDescriptor(
                        serviceName = serviceName.normaliseServiceName(operatorCode),
                        operatorCode = operatorCode
                    )
                    serviceVehicles.getOrPut(serviceDescriptor) { mutableListOf() } += vehicle
                }
            }
        }

        serviceVehicles
            .mapNotNull { (serviceDescriptor, vehicles) ->
                if (vehicles.isNotEmpty()) {
                    val sortedVehicles = vehicles
                        .apply {
                            sort()
                        }
                        .take(numberOfDepartures)

                    Service(
                        serviceDescriptor = serviceDescriptor,
                        vehicles = sortedVehicles
                    )
                } else {
                    null
                }
            }
            .ifEmpty { null }
    } else {
        null
    }
}

private fun String.normaliseServiceName(operatorCode: String): String {
    return when {
        this == "T50" && operatorCode == "EDT" -> "TRAM"
        else -> this
    }
}
