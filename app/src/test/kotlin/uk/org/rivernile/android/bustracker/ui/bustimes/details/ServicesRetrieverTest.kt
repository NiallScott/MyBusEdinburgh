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

package uk.org.rivernile.android.bustracker.ui.bustimes.details

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.services.ServiceDetails
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [ServicesRetriever].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class ServicesRetrieverTest {

    @Mock
    private lateinit var servicesRepository: ServicesRepository

    private lateinit var retriever: ServicesRetriever

    @BeforeTest
    fun setUp() {
        retriever = ServicesRetriever(servicesRepository)
    }

    @Test
    fun getServicesFlowEmitsNullWhenNullServicesForStop() = runTest {
        whenever(servicesRepository.getServiceDetailsFlow("123456".toNaptanStopIdentifier()))
            .thenReturn(flowOf(null))

        retriever.getServicesFlow("123456".toNaptanStopIdentifier()).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getServicesFlowEmitsNullWhenEmptyServicesForStop() = runTest {
        whenever(servicesRepository.getServiceDetailsFlow("123456".toNaptanStopIdentifier()))
            .thenReturn(flowOf(emptyList()))

        retriever.getServicesFlow("123456".toNaptanStopIdentifier()).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getServicesFlowEmitsServicesWhenServiceDetailsIsPopulated() = runTest {
        whenever(servicesRepository.getServiceDetailsFlow("123456".toNaptanStopIdentifier()))
            .thenReturn(
                flowOf(
                    listOf(
                        ServiceDetails(
                            service1,
                            "Service 1",
                            ServiceColours(1, 10)
                        ),
                        ServiceDetails(
                            service2,
                            null,
                            null
                        ),
                        ServiceDetails(
                            service3,
                            "Service 3",
                            null
                        )
                    )
                )
            )

        retriever.getServicesFlow("123456".toNaptanStopIdentifier()).test {
            assertEquals(
                listOf(
                    UiItem.Service(
                        service1.hashCode().toLong(),
                        "1",
                        "Service 1",
                        ServiceColours(1, 10)
                    ),
                    UiItem.Service(
                        service2.hashCode().toLong(),
                        "2",
                        null,
                        null
                    ),
                    UiItem.Service(
                        service3.hashCode().toLong(),
                        "3",
                        "Service 3",
                        null
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    private val service1 = FakeServiceDescriptor(
        serviceName = "1",
        operatorCode = "TEST1"
    )

    private val service2 = FakeServiceDescriptor(
        serviceName = "2",
        operatorCode = "TEST2"
    )

    private val service3 = FakeServiceDescriptor(
        serviceName = "3",
        operatorCode = "TEST3"
    )
}
