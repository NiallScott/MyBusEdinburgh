/*
 * Copyright (C) 2019 - 2022 Niall 'Rivernile' Scott
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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.ServiceNameFixer
import uk.org.rivernile.edinburghbustrackerapi.bustimes.BusTime
import uk.org.rivernile.edinburghbustrackerapi.bustimes.TimeData

/**
 * Tests for [ServiceMapper].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
internal class ServiceMapperTest {

    @Mock
    lateinit var vehicleMapper: VehicleMapper
    @Mock
    lateinit var serviceNameFixer: ServiceNameFixer

    @Mock
    lateinit var busTime: BusTime
    @Mock
    lateinit var timeData1: TimeData
    @Mock
    lateinit var timeData2: TimeData
    @Mock
    lateinit var vehicle1: Vehicle
    @Mock
    lateinit var vehicle2: Vehicle

    private lateinit var serviceMapper: ServiceMapper

    @Before
    fun setUp() {
        serviceMapper = ServiceMapper(vehicleMapper, serviceNameFixer)
    }

    @Test
    fun mapToServiceWithNullTimeDatasReturnsNullService() {
        val result = serviceMapper.mapToService(busTime)

        assertNull(result)
    }

    @Test
    fun mapToServiceWithEmptyTimeDatasReturnsNullService() {
        whenever(busTime.timeDatas)
                .thenReturn(emptyList())

        val result = serviceMapper.mapToService(busTime)

        assertNull(result)
        verify(busTime, never())
                .mnemoService
    }

    @Test
    fun mapToServiceWithNullServiceNameReturnsNullService() {
        whenever(busTime.timeDatas)
                .thenReturn(listOf(timeData1))
        whenever(busTime.mnemoService)
                .thenReturn(null)
        whenever(serviceNameFixer.correctServiceName(null))
                .thenReturn(null)

        val result = serviceMapper.mapToService(busTime)

        assertNull(result)
    }

    @Test
    fun mapToServiceWithSingleVehicleReturnsServiceWithSingleVehicle() {
        whenever(busTime.timeDatas)
                .thenReturn(listOf(timeData1))
        whenever(busTime.mnemoService)
                .thenReturn("100")
        whenever(vehicleMapper.mapToVehicle(timeData1))
                .thenReturn(vehicle1)
        whenever(serviceNameFixer.correctServiceName("100"))
                .thenReturn("100")
        val expected = Service(
                "100",
                listOf(vehicle1),
                null,
                null,
                isDisrupted = false,
                isDiverted = false)

        val result = serviceMapper.mapToService(busTime)

        assertEquals(expected, result)
    }

    @Test
    fun mapToServiceWithSingleVehicleUsesCorrectedServiceName() {
        whenever(busTime.timeDatas)
                .thenReturn(listOf(timeData1))
        whenever(busTime.mnemoService)
                .thenReturn("100")
        whenever(vehicleMapper.mapToVehicle(timeData1))
                .thenReturn(vehicle1)
        whenever(serviceNameFixer.correctServiceName("100"))
                .thenReturn("123")
        val expected = Service(
                "123",
                listOf(vehicle1),
                null,
                null,
                isDisrupted = false,
                isDiverted = false)

        val result = serviceMapper.mapToService(busTime)

        assertEquals(expected, result)
    }

    @Test
    fun mapToServiceWithSingleVehicleUsesCorrectFieldsFromBusTimesObject() {
        whenever(busTime.timeDatas)
                .thenReturn(listOf(timeData1))
        whenever(busTime.mnemoService)
                .thenReturn("100")
        whenever(busTime.operatorId)
                .thenReturn("Operator")
        whenever(busTime.nameService)
                .thenReturn("A -> B")
        whenever(busTime.isServiceDisruption)
                .thenReturn(true)
        whenever(busTime.isServiceDiversion)
                .thenReturn(true)
        whenever(vehicleMapper.mapToVehicle(timeData1))
                .thenReturn(vehicle1)
        whenever(serviceNameFixer.correctServiceName("100"))
                .thenReturn("123")
        val expected = Service(
                "123",
                listOf(vehicle1),
                "Operator",
                "A -> B",
                isDisrupted = true,
                isDiverted = true)

        val result = serviceMapper.mapToService(busTime)

        assertEquals(expected, result)
    }

    @Test
    fun mapToServiceWithMultipleVehiclesMapsAsExpected() {
        whenever(busTime.timeDatas)
                .thenReturn(listOf(timeData1, timeData2))
        whenever(busTime.mnemoService)
                .thenReturn("100")
        whenever(vehicleMapper.mapToVehicle(timeData1))
                .thenReturn(vehicle1)
        whenever(vehicleMapper.mapToVehicle(timeData2))
                .thenReturn(vehicle2)
        whenever(serviceNameFixer.correctServiceName("100"))
                .thenReturn("100")
        val expected = Service(
                "100",
                listOf(vehicle1, vehicle2),
                null,
                null,
                isDisrupted = false,
                isDiverted = false)

        val result = serviceMapper.mapToService(busTime)

        assertEquals(expected, result)
    }
}