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

package uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes

import uk.org.rivernile.android.bustracker.core.endpoints.tracker.ErrorMapper
import uk.org.rivernile.android.bustracker.core.utils.TimeUtils
import uk.org.rivernile.edinburghbustrackerapi.bustimes.BusTimes
import javax.inject.Inject

/**
 * This class maps the Edinburgh live times data received from the tracker service in to
 * app-specific model objects.
 *
 * @param errorMapper An implementation to map errors.
 * @param serviceMapper An implementation to map [Service]s.
 * @param timeUtils Time utility methods.
 * @author Niall Scott
 */
internal class LiveTimesMapper @Inject constructor(
    private val errorMapper: ErrorMapper,
    private val serviceMapper: ServiceMapper,
    private val timeUtils: TimeUtils) {

    /**
     * Given a [BusTimes] response object from the tracker service, map this in to our app-specific
     * model objects, namely, a [LiveTimes] object.
     *
     * @param busTimes The received [BusTimes] object from the tracker service.
     * @return On success, the mapped [LiveTimes] object.
     */
    fun mapToLiveTimes(busTimes: BusTimes): LiveTimesResponse {
        errorMapper.extractError(busTimes)?.let {
            return it
        }

        val receiveTime = timeUtils.currentTimeMills
        var globalDisruption = false

        return (busTimes.busTimes?.let {
            if (it.isNotEmpty()) {
                val tempStops = HashMap<String, TempStop>(it.size)

                it.forEach { busTime ->
                    val stopCode = busTime.stopId

                    if (!stopCode.isNullOrEmpty()) {
                        serviceMapper.mapToService(busTime)?.let { service ->
                            tempStops.getOrPut(stopCode) {
                                TempStop(
                                    stopCode,
                                    busTime.stopName,
                                    busTime.busStopDisruption ?: false)
                            }.apply {
                                services.add(service)
                            }

                            globalDisruption = busTime.globalDisruption ?: false
                        }
                    }
                }

                tempStops.mapValues { (k, v) ->
                    Stop(k, v.stopName, v.services, v.isDisrupted)
                }.let { stops ->
                    if (stops.isNotEmpty()) {
                        LiveTimes(stops, receiveTime, globalDisruption)
                    } else {
                        emptyLiveTimes(receiveTime, globalDisruption)
                    }
                }
            } else {
                emptyLiveTimes(receiveTime, false)
            }
        } ?: emptyLiveTimes(receiveTime, false)).let {
            LiveTimesResponse.Success(it)
        }
    }

    /**
     * Produce an instance of [LiveTimes] which does not contain any live departures. That is, its
     * empty state.
     *
     * @return The empty version of [LiveTimes].
     */
    fun emptyLiveTimes(): LiveTimesResponse =
        LiveTimesResponse.Success(
            emptyLiveTimes(timeUtils.currentTimeMills, false))

    /**
     * Produce an instance of [LiveTimes] which does not contain any live departures. That is, its
     * empty state.
     *
     * @param receiveTime The time the data was received from the endpoint at.
     * @param globalDisruption Is there a global disruption active?
     * @return The empty version of [LiveTimes].
     */
    private fun emptyLiveTimes(receiveTime: Long, globalDisruption: Boolean) =
        LiveTimes(emptyMap(), receiveTime, globalDisruption)

    /**
     * A place to temporarily store stop parameters prior to finalising the stop data when a [Stop]
     * is created.
     *
     * @property stopCode The code for the stop.
     * @property stopName The name of the stop.
     * @property isDisrupted Is there a current disruption for this stop?
     */
    private data class TempStop(
        var stopCode: String,
        var stopName: String?,
        var isDisrupted: Boolean) {

        /**
         * A [MutableList] to store services in.
         */
        val services = mutableListOf<Service>()
    }
}