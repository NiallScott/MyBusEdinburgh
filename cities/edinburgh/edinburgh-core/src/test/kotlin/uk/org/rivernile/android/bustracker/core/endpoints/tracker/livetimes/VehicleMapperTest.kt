/*
 * Copyright (C) 2019 - 2024 Niall 'Rivernile' Scott
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

import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.edinburghbustrackerapi.bustimes.TimeData
import java.util.Date
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [VehicleMapper].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
internal class VehicleMapperTest {

    @Mock
    lateinit var departureTimeCalculator: DepartureTimeCalculator

    @Mock
    lateinit var timeData: TimeData

    private lateinit var vehicleMapper: VehicleMapper

    @BeforeTest
    fun setUp() {
        vehicleMapper = VehicleMapper(departureTimeCalculator)
    }

    @Test
    fun mapToVehicleReturnsNullWhenMinutesIsNull() {
        whenever(timeData.minutes)
            .thenReturn(null)

        val result = vehicleMapper.mapToVehicle(timeData)

        assertNull(result)
    }

    @Test
    fun mapToVehicleReturnsVehicleWhenMinimumIsSupplied() {
        whenever(timeData.minutes)
            .thenReturn(5)
        val departureTime = Date()
        whenever(departureTimeCalculator.calculateDepartureTime(5))
            .thenReturn(departureTime)
        val expected = Vehicle(
            null,
            departureTime,
            5,
            null,
            null,
            isEstimatedTime = false,
            isDelayed = false,
            isDiverted = false,
            isTerminus = false,
            isPartRoute = false
        )

        val result = vehicleMapper.mapToVehicle(timeData)

        assertEquals(expected, result)
    }

    @Test
    fun mapToVehicleReturnsVehicleWithValuesPopulated() {
        whenever(timeData.minutes)
            .thenReturn(5)
        whenever(timeData.nameDest)
            .thenReturn("Destination")
        whenever(timeData.terminus)
            .thenReturn("123456")
        whenever(timeData.journeyId)
            .thenReturn("9876")
        val departureTime = Date()
        whenever(departureTimeCalculator.calculateDepartureTime(5))
            .thenReturn(departureTime)
        val expected = Vehicle(
            "Destination",
            departureTime,
            5,
            "123456",
            "9876",
            isEstimatedTime = false,
            isDelayed = false,
            isDiverted = false,
            isTerminus = false,
            isPartRoute = false
        )

        val result = vehicleMapper.mapToVehicle(timeData)

        assertEquals(expected, result)
    }

    @Test
    fun mapToVehicleSetsEstimatedTimeFlagAsTrueWhenPresentInPayload() {
        whenever(timeData.minutes)
            .thenReturn(5)
        whenever(timeData.reliability)
            .thenReturn(TimeData.RELIABILITY_ESTIMATED_TIME.toString())
        val departureTime = Date()
        whenever(departureTimeCalculator.calculateDepartureTime(5))
            .thenReturn(departureTime)
        val expected = Vehicle(
            null,
            departureTime,
            5,
            null,
            null,
            isEstimatedTime = true,
            isDelayed = false,
            isDiverted = false,
            isTerminus = false,
            isPartRoute = false
        )

        val result = vehicleMapper.mapToVehicle(timeData)

        assertEquals(expected, result)
    }

    @Test
    fun mapToVehicleSetsIsDelayedFlagAsTrueWhenPresentInPayload() {
        whenever(timeData.minutes)
            .thenReturn(5)
        whenever(timeData.reliability)
            .thenReturn(TimeData.RELIABILITY_DELAYED.toString())
        val departureTime = Date()
        whenever(departureTimeCalculator.calculateDepartureTime(5))
            .thenReturn(departureTime)
        val expected = Vehicle(
            null,
            departureTime,
            5,
            null,
            null,
            isEstimatedTime = false,
            isDelayed = true,
            isDiverted = false,
            isTerminus = false,
            isPartRoute = false
        )

        val result = vehicleMapper.mapToVehicle(timeData)

        assertEquals(expected, result)
    }

    @Test
    fun mapToVehicleSetsIsDivertedFlagAsTrueWhenPresentInPayload() {
        whenever(timeData.minutes)
            .thenReturn(5)
        whenever(timeData.reliability)
            .thenReturn(TimeData.RELIABILITY_DIVERTED.toString())
        val departureTime = Date()
        whenever(departureTimeCalculator.calculateDepartureTime(5))
            .thenReturn(departureTime)
        val expected = Vehicle(
            null,
            departureTime,
            5,
            null,
            null,
            isEstimatedTime = false,
            isDelayed = false,
            isDiverted = true,
            isTerminus = false,
            isPartRoute = false
        )

        val result = vehicleMapper.mapToVehicle(timeData)

        assertEquals(expected, result)
    }

    @Test
    fun mapToVehicleSetsIsTerminusFlagAsTrueWhenPresentInPayload() {
        whenever(timeData.minutes)
            .thenReturn(5)
        whenever(timeData.type)
            .thenReturn(TimeData.TYPE_TERMINUS_STOP.toString())
        val departureTime = Date()
        whenever(departureTimeCalculator.calculateDepartureTime(5))
            .thenReturn(departureTime)
        val expected = Vehicle(
            null,
            departureTime,
            5,
            null,
            null,
            isEstimatedTime = false,
            isDelayed = false,
            isDiverted = false,
            isTerminus = true,
            isPartRoute = false
        )

        val result = vehicleMapper.mapToVehicle(timeData)

        assertEquals(expected, result)
    }

    @Test
    fun mapToVehicleSetsIsPartRouteFlagAsTrueWhenPresentInPayload() {
        whenever(timeData.minutes)
            .thenReturn(5)
        whenever(timeData.type)
            .thenReturn(TimeData.TYPE_PART_ROUTE.toString())
        val departureTime = Date()
        whenever(departureTimeCalculator.calculateDepartureTime(5))
            .thenReturn(departureTime)
        val expected = Vehicle(
            null,
            departureTime,
            5,
            null,
            null,
            isEstimatedTime = false,
            isDelayed = false,
            isDiverted = false,
            isTerminus = false,
            isPartRoute = true
        )

        val result = vehicleMapper.mapToVehicle(timeData)

        assertEquals(expected, result)
    }

    @Test
    fun mapToVehicleReturnsVehicleWithRepresentativeDataSet() {
        val reliability = arrayOf(
            TimeData.RELIABILITY_ESTIMATED_TIME,
            TimeData.RELIABILITY_DELAYED,
            TimeData.RELIABILITY_DIVERTED
        ).joinToString("")
        val type = arrayOf(
            TimeData.TYPE_TERMINUS_STOP,
            TimeData.TYPE_PART_ROUTE
        ).joinToString("")
        whenever(timeData.minutes)
            .thenReturn(5)
        whenever(timeData.nameDest)
            .thenReturn("Destination")
        whenever(timeData.terminus)
            .thenReturn("123456")
        whenever(timeData.journeyId)
            .thenReturn("9876")
        whenever(timeData.reliability)
            .thenReturn(reliability)
        whenever(timeData.type)
            .thenReturn(type)
        val departureTime = Date()
        whenever(departureTimeCalculator.calculateDepartureTime(5))
            .thenReturn(departureTime)
        val expected = Vehicle(
            "Destination",
            departureTime,
            5,
            "123456",
            "9876",
            isEstimatedTime = true,
            isDelayed = true,
            isDiverted = true,
            isTerminus = true,
            isPartRoute = true
        )

        val result = vehicleMapper.mapToVehicle(timeData)

        assertEquals(expected, result)
    }
}