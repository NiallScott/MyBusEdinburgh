/*
 * Copyright (C) 2021 - 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.servicestops

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.database.busstop.daos.BusStopsDao
import uk.org.rivernile.android.bustracker.core.database.busstop.daos.ServiceStopsDao
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [ServiceStopsRepository].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class ServiceStopsRepositoryTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var serviceStopsDao: ServiceStopsDao
    @Mock
    private lateinit var busStopsDao: BusStopsDao

    private lateinit var repository: ServiceStopsRepository

    @Before
    fun setUp() {
        repository = ServiceStopsRepository(serviceStopsDao, busStopsDao)
    }

    @Test
    fun getServicesForStopFlowGetsInitialValue() = runTest {
        val expected = listOf("1", "2", "3")
        whenever(serviceStopsDao.getServicesForStop("123456"))
                .thenReturn(expected)

        val observer = repository.getServicesForStopFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()
        advanceUntilIdle()

        observer.assertValues(expected)
        verify(serviceStopsDao)
                .removeOnServiceStopsChangedListener(any())
    }

    @Test
    fun getServicesForStopFlowRespondsToDataChanges() = runTest {
        doAnswer {
            val listener = it.getArgument<ServiceStopsDao.OnServiceStopsChangedListener>(0)
            listener.onServiceStopsChanged()
            listener.onServiceStopsChanged()
        }.whenever(serviceStopsDao).addOnServiceStopsChangedListener(any())
        val expected1 = listOf("1")
        val expected3 = listOf("1", "3", "5")
        whenever(serviceStopsDao.getServicesForStop("123456"))
                .thenReturn(expected1, null, expected3)

        val observer = repository.getServicesForStopFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()
        advanceUntilIdle()

        observer.assertValues(expected1, null, expected3)
        verify(serviceStopsDao)
                .removeOnServiceStopsChangedListener(any())
    }

    @Test
    fun getServicesForStopsFlowGetsInitialValue() = runTest {
        val expected = mapOf(
                "111111" to listOf("1", "2", "3"),
                "222222" to listOf("4", "5", "6"),
                "333333" to listOf("7", "8", "9"))
        val stopCodes = setOf("111111", "222222", "333333")
        whenever(busStopsDao.getServicesForStops(stopCodes))
                .thenReturn(expected)

        val observer = repository.getServicesForStopsFlow(stopCodes).test(this)
        advanceUntilIdle()
        observer.finish()
        advanceUntilIdle()

        observer.assertValues(expected)
        verify(busStopsDao)
                .removeOnBusStopsChangedListener(any())
    }

    @Test
    fun getServicesForStopsFlowRespondsToDataChanges() = runTest {
        doAnswer {
            val listener = it.getArgument<BusStopsDao.OnBusStopsChangedListener>(0)
            listener.onBusStopsChanged()
            listener.onBusStopsChanged()
        }.whenever(busStopsDao).addOnBusStopsChangedListener(any())
        val expected1 = mapOf(
                "111111" to listOf("1", "2", "3"),
                "222222" to listOf("4", "5", "6"),
                "333333" to listOf("7", "8", "9"))
        val expected3 = mapOf(
                "111111" to listOf("1", "2", "3"),
                "222222" to listOf("4", "5", "7"))
        val stopCodes = setOf("111111", "222222", "333333")
        whenever(busStopsDao.getServicesForStops(stopCodes))
                .thenReturn(expected1, null, expected3)

        val observer = repository.getServicesForStopsFlow(stopCodes).test(this)
        advanceUntilIdle()
        observer.finish()
        advanceUntilIdle()

        observer.assertValues(expected1, null, expected3)
        verify(busStopsDao)
                .removeOnBusStopsChangedListener(any())
    }
}