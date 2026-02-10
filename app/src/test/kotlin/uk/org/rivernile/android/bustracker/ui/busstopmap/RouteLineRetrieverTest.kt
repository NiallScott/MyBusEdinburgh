/*
 * Copyright (C) 2022 - 2026 Niall 'Rivernile' Scott
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

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.servicepoints.FakeServicePoint
import uk.org.rivernile.android.bustracker.core.servicepoints.ServicePointsRepository
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [RouteLineRetriever].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class RouteLineRetrieverTest {

    @Mock
    private lateinit var servicePointsRepository: ServicePointsRepository
    @Mock
    private lateinit var servicesRepository: ServicesRepository

    private lateinit var retriever: RouteLineRetriever

    @BeforeTest
    fun setUp() {
        retriever = RouteLineRetriever(
            servicePointsRepository,
            servicesRepository
        )
    }

    @Test
    fun getRouteLinesFlowWithNullSelectedServicesReturnsFlowOfNull() = runTest {
        retriever.getRouteLinesFlow(null).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getRouteLinesFlowWithEmptySelectedServicesReturnsFlowOfNull() = runTest {
        retriever.getRouteLinesFlow(emptySet()).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getRouteLinesFlowWithSelectedServicesWithNoServicePointsEmitsNull() = runTest {
        val selectedServices = setOf(service(1), service(2), service(3))
        whenever(servicePointsRepository.getServicePointsFlow(selectedServices))
            .thenReturn(flowOf(null))
        whenever(servicesRepository.getColoursForServicesFlow(selectedServices))
            .thenReturn(
                flowOf(
                    mapOf(
                        service(1) to ServiceColours(1, 10),
                        service(2) to ServiceColours(2, 20),
                        service(3) to ServiceColours(3, 30)
                    )
                )
            )

        retriever.getRouteLinesFlow(selectedServices).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getRouteLinesFlowWithServicePointAndNullColoursEmitsCorrectValue() = runTest {
        val selectedServices = setOf(service(1), service(2), service(3))
        whenever(servicePointsRepository.getServicePointsFlow(selectedServices))
            .thenReturn(
                flowOf(
                    listOf(
                        FakeServicePoint(service(1), 1, 1.1, 2.2)
                    )
                )
            )
        whenever(servicesRepository.getColoursForServicesFlow(selectedServices))
            .thenReturn(flowOf(null))
        val expected = listOf(
            UiServiceRoute(
                service(1),
                null,
                listOf(
                    UiServiceLine(
                        listOf(
                            UiLatLon(1.1, 2.2)
                        )
                    )
                )
            )
        )

        retriever.getRouteLinesFlow(selectedServices).test {
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getRouteLinesFlowWithServicePointAndEmptyColoursEmitsCorrectValue() = runTest {
        val selectedServices = setOf(service(1), service(2), service(3))
        whenever(servicePointsRepository.getServicePointsFlow(selectedServices))
            .thenReturn(
                flowOf(
                    listOf(
                        FakeServicePoint(service(1), 1, 1.1, 2.2)
                    )
                )
            )
        whenever(servicesRepository.getColoursForServicesFlow(selectedServices))
            .thenReturn(flowOf(emptyMap()))
        val expected = listOf(
            UiServiceRoute(
                service(1),
                null,
                listOf(
                    UiServiceLine(
                        listOf(
                            UiLatLon(1.1, 2.2)
                        )
                    )
                )
            )
        )

        retriever.getRouteLinesFlow(selectedServices).test {
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getRouteLinesFlowWithServicePointAndPopulatedColoursEmitsCorrectValue() = runTest {
        val selectedServices = setOf(service(1), service(2), service(3))
        whenever(servicePointsRepository.getServicePointsFlow(selectedServices))
            .thenReturn(
                flowOf(
                    listOf(
                        FakeServicePoint(service(1), 1, 1.1, 2.2)
                    )
                )
            )
        whenever(servicesRepository.getColoursForServicesFlow(selectedServices))
            .thenReturn(flowOf(mapOf(service(1) to ServiceColours(1, 10))))
        val expected = listOf(
            UiServiceRoute(
                service(1),
                1,
                listOf(
                    UiServiceLine(
                        listOf(
                            UiLatLon(1.1, 2.2)
                        )
                    )
                )
            )
        )

        retriever.getRouteLinesFlow(selectedServices).test {
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getRouteLinesFlowWithMultiplePointsEmitsCorrectValue() = runTest {
        val selectedServices = setOf(service(1), service(2), service(3))
        whenever(servicePointsRepository.getServicePointsFlow(selectedServices))
            .thenReturn(
                flowOf(
                    listOf(
                        FakeServicePoint(service(1), 1, 1.1, 2.2),
                        FakeServicePoint(service(1), 1, 3.3, 4.4),
                        FakeServicePoint(service(1), 1, 5.5, 6.6)
                    )
                )
            )
        whenever(servicesRepository.getColoursForServicesFlow(selectedServices))
            .thenReturn(flowOf(mapOf(service(1) to ServiceColours(1, 10))))
        val expected = listOf(
            UiServiceRoute(
                service(1),
                1,
                listOf(
                    UiServiceLine(
                        listOf(
                            UiLatLon(1.1, 2.2),
                            UiLatLon(3.3, 4.4),
                            UiLatLon(5.5, 6.6)
                        )
                    )
                )
            )
        )

        retriever.getRouteLinesFlow(selectedServices).test {
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getRouteLinesFlowWithMultipleLinesEmitsCorrectValue() = runTest {
        val selectedServices = setOf(service(1), service(2), service(3))
        whenever(servicePointsRepository.getServicePointsFlow(selectedServices))
            .thenReturn(
                flowOf(
                    listOf(
                        FakeServicePoint(service(1), 1, 1.1, 2.2),
                        FakeServicePoint(service(1), 1, 3.3, 4.4),
                        FakeServicePoint(service(1), 1, 5.5, 6.6),
                        FakeServicePoint(service(1), 2, 7.7, 8.8),
                        FakeServicePoint(service(1), 2, 9.9, 10.10),
                        FakeServicePoint(service(1), 2, 11.11, 12.12),
                        FakeServicePoint(service(1), 3, 13.13, 14.14),
                        FakeServicePoint(service(1), 3, 15.15, 16.16),
                        FakeServicePoint(service(1), 3, 17.17, 18.18)
                    )
                )
            )
        whenever(servicesRepository.getColoursForServicesFlow(selectedServices))
            .thenReturn(flowOf(mapOf(service(1) to ServiceColours(1, 10))))
        val expected = listOf(
            UiServiceRoute(
                service(1),
                1,
                listOf(
                    UiServiceLine(
                        listOf(
                            UiLatLon(1.1, 2.2),
                            UiLatLon(3.3, 4.4),
                            UiLatLon(5.5, 6.6)
                        )
                    ),
                    UiServiceLine(
                        listOf(
                            UiLatLon(7.7, 8.8),
                            UiLatLon(9.9, 10.10),
                            UiLatLon(11.11, 12.12)
                        )
                    ),
                    UiServiceLine(
                        listOf(
                            UiLatLon(13.13, 14.14),
                            UiLatLon(15.15, 16.16),
                            UiLatLon(17.17, 18.18)
                        )
                    )
                )
            )
        )

        retriever.getRouteLinesFlow(selectedServices).test {
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getRouteLinesFlowWithMultipleServicesEmitsCorrectValue() = runTest {
        val selectedServices = setOf(service(1), service(2), service(3))
        whenever(servicePointsRepository.getServicePointsFlow(selectedServices))
            .thenReturn(
                flowOf(
                    listOf(
                        FakeServicePoint(service(1), 1, 1.1, 2.2),
                        FakeServicePoint(service(1), 1, 3.3, 4.4),
                        FakeServicePoint(service(1), 1, 5.5, 6.6),
                        FakeServicePoint(service(2), 1, 7.7, 8.8),
                        FakeServicePoint(service(2), 1, 9.9, 10.10),
                        FakeServicePoint(service(2), 1, 11.11, 12.12),
                        FakeServicePoint(service(3), 1, 13.13, 14.14),
                        FakeServicePoint(service(3), 1, 15.15, 16.16),
                        FakeServicePoint(service(3), 1, 17.17, 18.18)
                    )
                )
            )
        whenever(servicesRepository.getColoursForServicesFlow(selectedServices))
            .thenReturn(
                flowOf(
                    mapOf(
                        service(1) to ServiceColours(1, 10),
                        service(3) to ServiceColours(3, 30)
                    )
                )
            )
        val expected = listOf(
            UiServiceRoute(
                service(1),
                1,
                listOf(
                    UiServiceLine(
                        listOf(
                            UiLatLon(1.1, 2.2),
                            UiLatLon(3.3, 4.4),
                            UiLatLon(5.5, 6.6)
                        )
                    )
                )
            ),
            UiServiceRoute(
                service(2),
                null,
                listOf(
                    UiServiceLine(
                        listOf(
                            UiLatLon(7.7, 8.8),
                            UiLatLon(9.9, 10.10),
                            UiLatLon(11.11, 12.12)
                        )
                    )
                )
            ),
            UiServiceRoute(
                service(3),
                3,
                listOf(
                    UiServiceLine(
                        listOf(
                            UiLatLon(13.13, 14.14),
                            UiLatLon(15.15, 16.16),
                            UiLatLon(17.17, 18.18)
                        )
                    )
                )
            )
        )

        retriever.getRouteLinesFlow(selectedServices).test {
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getRouteLinesFlowWithServicePointAndChangingColoursEmitsNewValues() = runTest {
        val selectedServices = setOf(service(1), service(2), service(3))
        whenever(servicePointsRepository.getServicePointsFlow(selectedServices))
            .thenReturn(
                flowOf(
                    listOf(
                        FakeServicePoint(service(1), 1, 1.1, 2.2)
                    )
                )
            )
        whenever(servicesRepository.getColoursForServicesFlow(selectedServices))
            .thenReturn(
                intervalFlowOf(
                    0L,
                    10L,
                    mapOf(service(1) to ServiceColours(1, 10)),
                    null,
                    mapOf(service(1) to ServiceColours(2, 20))
                )
            )
        val expected1 = listOf(
            UiServiceRoute(
                service(1),
                1,
                listOf(
                    UiServiceLine(
                        listOf(
                            UiLatLon(1.1, 2.2))))))
        val expected2 = listOf(
            UiServiceRoute(
                service(1),
                null,
                listOf(
                    UiServiceLine(
                        listOf(
                            UiLatLon(1.1, 2.2))))))
        val expected3 = listOf(
            UiServiceRoute(
                service(1),
                2,
                listOf(
                    UiServiceLine(
                        listOf(
                            UiLatLon(1.1, 2.2))))))

        retriever.getRouteLinesFlow(selectedServices).test {
            assertEquals(expected1, awaitItem())
            assertEquals(expected2, awaitItem())
            assertEquals(expected3, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getRouteLinesFlowWithColoursAndChangingServicePointsEmitsNewValues() = runTest {
        val selectedServices = setOf(service(1), service(2), service(3))
        whenever(servicePointsRepository.getServicePointsFlow(selectedServices))
            .thenReturn(
                intervalFlowOf(
                    0L,
                    10L,
                    listOf(FakeServicePoint(service(1), 1, 1.1, 2.2)),
                    listOf(FakeServicePoint(service(1), 1, 10.1, 20.2)),
                    listOf(FakeServicePoint(service(1), 1, 1.1, 2.2))
                )
            )
        whenever(servicesRepository.getColoursForServicesFlow(selectedServices))
            .thenReturn(flowOf(mapOf(service(1) to ServiceColours(1, 10))))
        val expected1 = listOf(
            UiServiceRoute(
                service(1),
                1,
                listOf(
                    UiServiceLine(listOf(UiLatLon(1.1, 2.2)))
                )
            )
        )
        val expected2 = listOf(
            UiServiceRoute(
                service(1),
                1,
                listOf(
                    UiServiceLine(listOf(UiLatLon(10.1, 20.2)))
                )
            )
        )
        val expected3 = listOf(
            UiServiceRoute(
                service(1),
                1,
                listOf(
                    UiServiceLine(listOf(UiLatLon(1.1, 2.2)))
                )
            )
        )

        retriever.getRouteLinesFlow(selectedServices).test {
            assertEquals(expected1, awaitItem())
            assertEquals(expected2, awaitItem())
            assertEquals(expected3, awaitItem())
            awaitComplete()
        }
    }

    private fun service(id: Int): ServiceDescriptor {
        return FakeServiceDescriptor(
            serviceName = id.toString(),
            operatorCode = "TEST$id"
        )
    }
}
