/*
 * Copyright (C) 2022 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.bustimes.details

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
import uk.org.rivernile.android.bustracker.core.services.ServiceDetails
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import uk.org.rivernile.android.bustracker.core.servicestops.ServiceStopsRepository
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [ServicesRetriever].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class ServicesRetrieverTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var serviceStopsRepository: ServiceStopsRepository
    @Mock
    private lateinit var servicesRepository: ServicesRepository

    private lateinit var retriever: ServicesRetriever

    @Before
    fun setUp() {
        retriever = ServicesRetriever(serviceStopsRepository, servicesRepository)
    }

    @Test
    fun getServicesFlowEmitsNullWhenNullServicesForStop() = runTest {
        whenever(serviceStopsRepository.getServicesForStopFlow("123456"))
                .thenReturn(flowOf(null))

        val observer = retriever.getServicesFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(null)
    }

    @Test
    fun getServicesFlowEmitsNullWhenEmptyServicesForStop() = runTest {
        whenever(serviceStopsRepository.getServicesForStopFlow("123456"))
                .thenReturn(flowOf(emptyList()))

        val observer = retriever.getServicesFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(null)
    }

    @Test
    fun getServicesFlowEmitsServicesWhenServiceDetailsIsNull() = runTest {
        whenever(serviceStopsRepository.getServicesForStopFlow("123456"))
                .thenReturn(flowOf(listOf("1", "2", "3")))
        whenever(servicesRepository.getServiceDetailsFlow(setOf("1", "2", "3")))
                .thenReturn(flowOf(null))

        val observer = retriever.getServicesFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                listOf(
                        UiItem.Service(
                                "1".hashCode().toLong(),
                                "1",
                                null,
                                null),
                        UiItem.Service(
                                "2".hashCode().toLong(),
                                "2",
                                null,
                                null),
                        UiItem.Service(
                                "3".hashCode().toLong(),
                                "3",
                                null,
                                null)))
    }

    @Test
    fun getServicesFlowEmitsServicesWhenServiceDetailsIsEmpty() = runTest {
        whenever(serviceStopsRepository.getServicesForStopFlow("123456"))
                .thenReturn(flowOf(listOf("1", "2", "3")))
        whenever(servicesRepository.getServiceDetailsFlow(setOf("1", "2", "3")))
                .thenReturn(flowOf(emptyMap()))

        val observer = retriever.getServicesFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                listOf(
                        UiItem.Service(
                                "1".hashCode().toLong(),
                                "1",
                                null,
                                null),
                        UiItem.Service(
                                "2".hashCode().toLong(),
                                "2",
                                null,
                                null),
                        UiItem.Service(
                                "3".hashCode().toLong(),
                                "3",
                                null,
                                null)))
    }

    @Test
    fun getServicesFlowEmitsServicesWhenServiceDetailsIsPopulated() = runTest {
        whenever(serviceStopsRepository.getServicesForStopFlow("123456"))
                .thenReturn(flowOf(listOf("1", "2", "3")))
        whenever(servicesRepository.getServiceDetailsFlow(setOf("1", "2", "3")))
                .thenReturn(flowOf(mapOf(
                        "1" to ServiceDetails(
                                "1",
                                "Service 1",
                                0xAABBCC),
                        "3" to ServiceDetails(
                                "3",
                                "Service 3",
                                null))))

        val observer = retriever.getServicesFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                listOf(
                        UiItem.Service(
                                "1".hashCode().toLong(),
                                "1",
                                "Service 1",
                                0xAABBCC),
                        UiItem.Service(
                                "2".hashCode().toLong(),
                                "2",
                                null,
                                null),
                        UiItem.Service(
                                "3".hashCode().toLong(),
                                "3",
                                "Service 3",
                                null)))
    }
}