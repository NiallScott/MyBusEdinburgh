/*
 * Copyright (C) 2022 - 2024 Niall 'Rivernile' Scott
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
import uk.org.rivernile.android.bustracker.core.servicestops.ServiceStopsRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [ServiceListingRetriever].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class ServiceListingRetrieverTest {

    @Mock
    private lateinit var serviceStopsRepository: ServiceStopsRepository

    private lateinit var retriever: ServiceListingRetriever

    @BeforeTest
    fun setUp() {
        retriever = ServiceListingRetriever(serviceStopsRepository)
    }

    @Test
    fun getServiceListingFlowWithNullStopCodeReturnsFlowOfNull() = runTest {
        retriever.getServiceListingFlow(null).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getServiceListingFlowWithEmptyStopCodeReturnsFlowOfNull() = runTest {
        retriever.getServiceListingFlow("").test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getServiceListingFlowWithNullServiceListingEmitsCorrectItems() = runTest {
        whenever(serviceStopsRepository.getServicesForStopFlow("123456"))
            .thenReturn(flowOf(null))

        retriever.getServiceListingFlow("123456").test {
            assertEquals(UiServiceListing.InProgress("123456"), awaitItem())
            assertEquals(UiServiceListing.Empty("123456"), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getServiceListingFlowWithEmptyServiceListingEmitsCorrectItems() = runTest {
        whenever(serviceStopsRepository.getServicesForStopFlow("123456"))
            .thenReturn(flowOf(emptyList()))

        retriever.getServiceListingFlow("123456").test {
            assertEquals(UiServiceListing.InProgress("123456"), awaitItem())
            assertEquals(UiServiceListing.Empty("123456"), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getServiceListingFlowWithServiceListingEmitsCorrectItems() = runTest {
        whenever(serviceStopsRepository.getServicesForStopFlow("123456"))
            .thenReturn(flowOf(listOf("1", "2", "3")))

        retriever.getServiceListingFlow("123456").test {
            assertEquals(UiServiceListing.InProgress("123456"), awaitItem())
            assertEquals(
                UiServiceListing.Success(
                    "123456",
                    listOf("1", "2", "3")
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }
}