/*
 * Copyright (C) 2019 Niall 'Rivernile' Scott
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

import uk.org.rivernile.edinburghbustrackerapi.bustimes.TimeData
import javax.inject.Inject

/**
 * This class is used to map from the Edinburgh bus tracker API to [Vehicle] objects.
 *
 * @param departureTimeCalculator Used to calculate the departure times.
 * @author Niall Scott
 */
internal class VehicleMapper @Inject constructor(
        private val departureTimeCalculator: DepartureTimeCalculator) {

    /**
     * Given a [TimeData] object, map it to a [Vehicle] object.
     *
     * @param timeData The [TimeData] object representing a single [Vehicle].
     * @return A [Vehicle] object representing the [TimeData] object.
     */
    fun mapToVehicle(timeData: TimeData): Vehicle {
        val departureMinutes = timeData.minutes
        val departureTime = departureTimeCalculator.calculateDepartureTime(departureMinutes)

        var isEstimatedTime = false
        var isDelayed = false
        var isDiverted = false
        var isTerminus = false
        var isPartRoute = false

        // Loop through all the reliability characters, checking to see if any match the flags we
        // track.
        timeData.reliability?.toCharArray()?.forEach {
            when (it) {
                TimeData.RELIABILITY_ESTIMATED_TIME -> isEstimatedTime = true
                TimeData.RELIABILITY_DELAYED -> isDelayed = true
                TimeData.RELIABILITY_DIVERTED -> isDiverted = true
            }
        }

        // Loop through all the type characters, checking to see if any match the flags we track.
        timeData.type?.toCharArray()?.forEach {
            when (it) {
                TimeData.TYPE_TERMINUS_STOP -> isTerminus = true
                TimeData.TYPE_PART_ROUTE -> isPartRoute = true
            }
        }

        return Vehicle(
                timeData.nameDest,
                departureTime,
                departureMinutes,
                timeData.terminus,
                timeData.journeyId,
                isEstimatedTime,
                isDelayed,
                isDiverted,
                isTerminus,
                isPartRoute)
    }
}