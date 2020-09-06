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

import uk.org.rivernile.android.bustracker.core.endpoints.tracker.AuthenticationException
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.MaintenanceException
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.NetworkException
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.NoConnectivityException
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.SystemOverloadedException
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.TrackerException
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.UnrecognisedServerErrorException
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimes
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.Service
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.Stop
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.Vehicle
import uk.org.rivernile.android.bustracker.core.livetimes.Result
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * This class maps a [LiveTimes] [Result] in to its [UiResult] form.
 *
 * @author Niall Scott
 */
class LiveTimesMapper @Inject constructor() {

    /**
     * Given a [Result] from attempting to load [LiveTimes], map it to a [UiResult]. Optionally,
     * [serviceColours] may be provided to assign colours to services.
     *
     * @param stopCode The stop code to retrieve from the [LiveTimes].
     * @param liveTimesResult The [Result] from attempting to load [LiveTimes].
     * @param serviceColours Optional service colours to assign against services in the mapping
     * process. This is used in the UI to give services their own colours.
     * @return A [UiResult] representing the [Result], but corrected for UI display.
     */
    fun mapLiveTimesAndColoursToUiResult(
            stopCode: String,
            liveTimesResult: Result<LiveTimes>,
            serviceColours: Map<String, Int>?): UiResult {
        return when (liveTimesResult) {
            is Result.InProgress -> UiResult.InProgress
            is Result.Success -> mapSuccess(stopCode, liveTimesResult.result, serviceColours)
            is Result.Error -> mapError(liveTimesResult)
        }
    }

    /**
     * Map the success case to a [UiResult]. This may not necessarily yield a [UiResult.Success] as
     * it may not be considered a success for UI display purposes. For example, we may get a
     * successful response from the endpoint which contains no data, but we consider that as an
     * error here.
     *
     * @param stopCode The stop code we're retrieving live times for. This is required here as the
     * response contains a [Map] of stop code -> [LiveTimes], as more than one stop can be
     * requested at a time. We need the stop code to get the right stop out of the [Map].
     * @param liveTimes The [LiveTimes] data.
     * @param serviceColours Known colours for services.
     * @return A [UiResult], which represents the correct UI representation of the [Result].
     */
    private fun mapSuccess(
            stopCode: String,
            liveTimes: LiveTimes,
            serviceColours: Map<String, Int>?): UiResult {
        val receiveTime = liveTimes.receiveTime

        return liveTimes.stops[stopCode]?.let {
            val stop = mapStopToUiStop(it, serviceColours)

            if (stop.services.isNotEmpty()) {
                UiResult.Success(receiveTime, stop)
            } else {
                createNoDataError(receiveTime)
            }
        } ?: createNoDataError(receiveTime)
    }

    /**
     * Given a [Stop], map it to a [UiStop].
     *
     * @param stop The [Stop] to map
     * @param serviceColours The service colours to be used in service mapping.
     * @return The mapped [Stop] as a [UiStop].
     */
    private fun mapStopToUiStop(stop: Stop, serviceColours: Map<String, Int>?) =
            UiStop(
                    stop.stopCode,
                    stop.stopName,
                    stop.services.mapNotNull {
                        mapServiceToUiService(it, serviceColours)
                    })

    /**
     * Given a [Service], map it to a [UiService]. Also combine this with [serviceColours], if
     * available. If not available, the service colour will be set as `null`.
     *
     * If the [Service] [Vehicle] listing is empty, then `null` will be returned.
     *
     * @param service The [Service]s to map.
     * @param serviceColours Service colour data to combine with the mapped [Service].
     * @return The mapped [Service], or `null` if there were no [Vehicle]s.
     */
    private fun mapServiceToUiService(service: Service, serviceColours: Map<String, Int>?) =
            service.vehicles.ifEmpty { null }?.let {
                UiService(
                        service.serviceName,
                        serviceColours?.get(service.serviceName),
                        it.map(this::mapVehicleToUiVehicle))
            }

    /**
     * Given a [Vehicle], map it to a [UiVehicle].
     *
     * @param vehicle The source [Vehicle] data.
     * @return The mapped [Vehicle].
     */
    private fun mapVehicleToUiVehicle(vehicle: Vehicle) =
            UiVehicle(
                    vehicle.destination,
                    vehicle.isDiverted,
                    vehicle.departureTime,
                    vehicle.departureMinutes,
                    vehicle.isEstimatedTime)

    /**
     * Given a [Result.Error], map it to a [UiResult.Error].
     *
     * @param error The error while loading the data.
     * @return The error as a [UiResult.Error].
     */
    private fun mapError(error: Result.Error) =
            UiResult.Error(error.receiveTime, mapErrorException(error.exception))

    /**
     * Given a [TrackerException], map it to the appropriate [ErrorType].
     *
     * @param exception The [TrackerException] received from a [Result.Error].
     * @return The error expressed as an [ErrorType].
     */
    private fun mapErrorException(exception: TrackerException) = when (exception) {
        is NoConnectivityException -> ErrorType.NO_CONNECTIVITY
        is NetworkException -> {
            if (exception.cause is UnknownHostException) {
                ErrorType.UNKNOWN_HOST
            } else {
                ErrorType.COMMUNICATION
            }
        }
        is UnrecognisedServerErrorException -> ErrorType.SERVER_ERROR
        is AuthenticationException -> ErrorType.AUTHENTICATION
        is MaintenanceException -> ErrorType.DOWN_FOR_MAINTENANCE
        is SystemOverloadedException -> ErrorType.SYSTEM_OVERLOADED
    }

    /**
     * Create a [UiResult.Error] which represents the [ErrorType.NO_DATA] error. This is used when
     * we're dealing with a successful response but we find it wasn't quite as successful as we
     * were expecting.
     *
     * @param receiveTime The time we received the data at.
     * @return A [UiResult.Error] which contains [ErrorType.NO_DATA].
     */
    private fun createNoDataError(receiveTime: Long) =
            UiResult.Error(receiveTime, ErrorType.NO_DATA)
}