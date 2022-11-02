/*
 * Copyright (C) 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.busstopmap

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.ServicePoint
import uk.org.rivernile.android.bustracker.core.servicepoints.ServicePointsRepository
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [RouteLineRetriever].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class RouteLineRetrieverTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var servicePointsRepository: ServicePointsRepository
    @Mock
    private lateinit var servicesRepository: ServicesRepository

    private lateinit var retriever: RouteLineRetriever

    @Before
    fun setUp() {
        retriever = RouteLineRetriever(
                servicePointsRepository,
                servicesRepository)
    }

    @Test
    fun getRouteLinesFlowWithNullSelectedServicesReturnsFlowOfNull() = runTest {
        val observer = retriever.getRouteLinesFlow(null).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(null)
    }

    @Test
    fun getRouteLinesFlowWithEmptySelectedServicesReturnsFlowOfNull() = runTest {
        val observer = retriever.getRouteLinesFlow(emptySet()).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(null)
    }

    @Test
    fun getRouteLinesFlowWithSelectedServicesWithNoServicePointsEmitsNull() = runTest {
        val selectedServices = setOf("1", "2", "3")
        whenever(servicePointsRepository.getServicePointsFlow(selectedServices))
                .thenReturn(flowOf(null))
        whenever(servicesRepository.getColoursForServicesFlow(selectedServices))
                .thenReturn(flowOf(mapOf(
                        "1" to 1,
                        "2" to 2,
                        "3" to 3)))

        val observer = retriever.getRouteLinesFlow(selectedServices).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(null)
    }

    @Test
    fun getRouteLinesFlowWithServicePointAndNullColoursEmitsCorrectValue() = runTest {
        val selectedServices = setOf("1", "2", "3")
        whenever(servicePointsRepository.getServicePointsFlow(selectedServices))
                .thenReturn(flowOf(listOf(
                        ServicePoint("1", 1, 1.1, 2.2))))
        whenever(servicesRepository.getColoursForServicesFlow(selectedServices))
                .thenReturn(flowOf(null))
        val expected = listOf(
                UiServiceRoute(
                        "1",
                        null,
                        listOf(
                                UiServiceLine(
                                        listOf(
                                                UiLatLon(1.1, 2.2))))))

        val observer = retriever.getRouteLinesFlow(selectedServices).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(expected)
    }

    @Test
    fun getRouteLinesFlowWithServicePointAndEmptyColoursEmitsCorrectValue() = runTest {
        val selectedServices = setOf("1", "2", "3")
        whenever(servicePointsRepository.getServicePointsFlow(selectedServices))
                .thenReturn(flowOf(listOf(
                        ServicePoint("1", 1, 1.1, 2.2))))
        whenever(servicesRepository.getColoursForServicesFlow(selectedServices))
                .thenReturn(flowOf(emptyMap()))
        val expected = listOf(
                UiServiceRoute(
                        "1",
                        null,
                        listOf(
                                UiServiceLine(
                                        listOf(
                                                UiLatLon(1.1, 2.2))))))

        val observer = retriever.getRouteLinesFlow(selectedServices).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(expected)
    }

    @Test
    fun getRouteLinesFlowWithServicePointAndPopulatedColoursEmitsCorrectValue() = runTest {
        val selectedServices = setOf("1", "2", "3")
        whenever(servicePointsRepository.getServicePointsFlow(selectedServices))
                .thenReturn(flowOf(listOf(
                        ServicePoint("1", 1, 1.1, 2.2))))
        whenever(servicesRepository.getColoursForServicesFlow(selectedServices))
                .thenReturn(flowOf(mapOf("1" to 100)))
        val expected = listOf(
                UiServiceRoute(
                        "1",
                        100,
                        listOf(
                                UiServiceLine(
                                        listOf(
                                                UiLatLon(1.1, 2.2))))))

        val observer = retriever.getRouteLinesFlow(selectedServices).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(expected)
    }

    @Test
    fun getRouteLinesFlowWithMultiplePointsEmitsCorrectValue() = runTest {
        val selectedServices = setOf("1", "2", "3")
        whenever(servicePointsRepository.getServicePointsFlow(selectedServices))
                .thenReturn(flowOf(listOf(
                        ServicePoint("1", 1, 1.1, 2.2),
                        ServicePoint("1", 1, 3.3, 4.4),
                        ServicePoint("1", 1, 5.5, 6.6))))
        whenever(servicesRepository.getColoursForServicesFlow(selectedServices))
                .thenReturn(flowOf(mapOf("1" to 100)))
        val expected = listOf(
                UiServiceRoute(
                        "1",
                        100,
                        listOf(
                                UiServiceLine(
                                        listOf(
                                                UiLatLon(1.1, 2.2),
                                                UiLatLon(3.3, 4.4),
                                                UiLatLon(5.5, 6.6))))))

        val observer = retriever.getRouteLinesFlow(selectedServices).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(expected)
    }

    @Test
    fun getRouteLinesFlowWithMultipleLinesEmitsCorrectValue() = runTest {
        val selectedServices = setOf("1", "2", "3")
        whenever(servicePointsRepository.getServicePointsFlow(selectedServices))
                .thenReturn(flowOf(listOf(
                        ServicePoint("1", 1, 1.1, 2.2),
                        ServicePoint("1", 1, 3.3, 4.4),
                        ServicePoint("1", 1, 5.5, 6.6),
                        ServicePoint("1", 2, 7.7, 8.8),
                        ServicePoint("1", 2, 9.9, 10.10),
                        ServicePoint("1", 2, 11.11, 12.12),
                        ServicePoint("1", 3, 13.13, 14.14),
                        ServicePoint("1", 3, 15.15, 16.16),
                        ServicePoint("1", 3, 17.17, 18.18))))
        whenever(servicesRepository.getColoursForServicesFlow(selectedServices))
                .thenReturn(flowOf(mapOf("1" to 100)))
        val expected = listOf(
                UiServiceRoute(
                        "1",
                        100,
                        listOf(
                                UiServiceLine(
                                        listOf(
                                                UiLatLon(1.1, 2.2),
                                                UiLatLon(3.3, 4.4),
                                                UiLatLon(5.5, 6.6))),
                                UiServiceLine(
                                        listOf(
                                                UiLatLon(7.7, 8.8),
                                                UiLatLon(9.9, 10.10),
                                                UiLatLon(11.11, 12.12))),
                                UiServiceLine(
                                        listOf(
                                                UiLatLon(13.13, 14.14),
                                                UiLatLon(15.15, 16.16),
                                                UiLatLon(17.17, 18.18))))))

        val observer = retriever.getRouteLinesFlow(selectedServices).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(expected)
    }

    @Test
    fun getRouteLinesFlowWithMultipleServicesEmitsCorrectValue() = runTest {
        val selectedServices = setOf("1", "2", "3")
        whenever(servicePointsRepository.getServicePointsFlow(selectedServices))
                .thenReturn(flowOf(listOf(
                        ServicePoint("1", 1, 1.1, 2.2),
                        ServicePoint("1", 1, 3.3, 4.4),
                        ServicePoint("1", 1, 5.5, 6.6),
                        ServicePoint("2", 1, 7.7, 8.8),
                        ServicePoint("2", 1, 9.9, 10.10),
                        ServicePoint("2", 1, 11.11, 12.12),
                        ServicePoint("3", 1, 13.13, 14.14),
                        ServicePoint("3", 1, 15.15, 16.16),
                        ServicePoint("3", 1, 17.17, 18.18))))
        whenever(servicesRepository.getColoursForServicesFlow(selectedServices))
                .thenReturn(flowOf(mapOf(
                        "1" to 100,
                        "3" to 300)))
        val expected = listOf(
                UiServiceRoute(
                        "1",
                        100,
                        listOf(
                                UiServiceLine(
                                        listOf(
                                                UiLatLon(1.1, 2.2),
                                                UiLatLon(3.3, 4.4),
                                                UiLatLon(5.5, 6.6))))),
                UiServiceRoute(
                        "2",
                        null,
                        listOf(
                                UiServiceLine(
                                        listOf(
                                                UiLatLon(7.7, 8.8),
                                                UiLatLon(9.9, 10.10),
                                                UiLatLon(11.11, 12.12))))),
                UiServiceRoute(
                        "3",
                        300,
                        listOf(
                                UiServiceLine(
                                        listOf(
                                                UiLatLon(13.13, 14.14),
                                                UiLatLon(15.15, 16.16),
                                                UiLatLon(17.17, 18.18))))))

        val observer = retriever.getRouteLinesFlow(selectedServices).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(expected)
    }

    @Test
    fun getRouteLinesFlowWithServicePointAndChangingColoursEmitsNewValues() = runTest {
        val selectedServices = setOf("1", "2", "3")
        whenever(servicePointsRepository.getServicePointsFlow(selectedServices))
                .thenReturn(flowOf(listOf(
                        ServicePoint("1", 1, 1.1, 2.2))))
        whenever(servicesRepository.getColoursForServicesFlow(selectedServices))
                .thenReturn(intervalFlowOf(
                        0L,
                        10L,
                        mapOf("1" to 100),
                        null,
                        mapOf("1" to 200)))
        val expected1 = listOf(
                UiServiceRoute(
                        "1",
                        100,
                        listOf(
                                UiServiceLine(
                                        listOf(
                                                UiLatLon(1.1, 2.2))))))
        val expected2 = listOf(
                UiServiceRoute(
                        "1",
                        null,
                        listOf(
                                UiServiceLine(
                                        listOf(
                                                UiLatLon(1.1, 2.2))))))
        val expected3 = listOf(
                UiServiceRoute(
                        "1",
                        200,
                        listOf(
                                UiServiceLine(
                                        listOf(
                                                UiLatLon(1.1, 2.2))))))

        val observer = retriever.getRouteLinesFlow(selectedServices).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(expected1, expected2, expected3)
    }

    @Test
    fun getRouteLinesFlowWithColoursAndChangingServicePointsEmitsNewValues() = runTest {
        val selectedServices = setOf("1", "2", "3")
        whenever(servicePointsRepository.getServicePointsFlow(selectedServices))
                .thenReturn(intervalFlowOf(
                        0L,
                        10L,
                        listOf(ServicePoint("1", 1, 1.1, 2.2)),
                        listOf(ServicePoint("1", 1, 10.1, 20.2)),
                        listOf(ServicePoint("1", 1, 1.1, 2.2))))
        whenever(servicesRepository.getColoursForServicesFlow(selectedServices))
                .thenReturn(flowOf(mapOf("1" to 100)))
        val expected1 = listOf(
                UiServiceRoute(
                        "1",
                        100,
                        listOf(
                                UiServiceLine(
                                        listOf(
                                                UiLatLon(1.1, 2.2))))))
        val expected2 = listOf(
                UiServiceRoute(
                        "1",
                        100,
                        listOf(
                                UiServiceLine(
                                        listOf(
                                                UiLatLon(10.1, 20.2))))))
        val expected3 = listOf(
                UiServiceRoute(
                        "1",
                        100,
                        listOf(
                                UiServiceLine(
                                        listOf(
                                                UiLatLon(1.1, 2.2))))))

        val observer = retriever.getRouteLinesFlow(selectedServices).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(expected1, expected2, expected3)
    }
}