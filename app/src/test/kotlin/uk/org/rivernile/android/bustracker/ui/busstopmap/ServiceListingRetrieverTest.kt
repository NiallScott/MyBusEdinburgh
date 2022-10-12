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
import uk.org.rivernile.android.bustracker.core.servicestops.ServiceStopsRepository
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [ServiceListingRetriever].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class ServiceListingRetrieverTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var serviceStopsRepository: ServiceStopsRepository

    private lateinit var retriever: ServiceListingRetriever

    @Before
    fun setUp() {
        retriever = ServiceListingRetriever(serviceStopsRepository)
    }

    @Test
    fun getServiceListingFlowWithNullStopCodeReturnsFlowOfNull() = runTest {
        val observer = retriever.getServiceListingFlow(null).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(null)
    }

    @Test
    fun getServiceListingFlowWithEmptyStopCodeReturnsFlowOfNull() = runTest {
        val observer = retriever.getServiceListingFlow("").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(null)
    }

    @Test
    fun getServiceListingFlowWithNullServiceListingEmitsCorrectItems() = runTest {
        whenever(serviceStopsRepository.getServicesForStopFlow("123456"))
                .thenReturn(flowOf(null))

        val observer = retriever.getServiceListingFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                UiServiceListing.InProgress("123456"),
                UiServiceListing.Empty("123456"))
    }

    @Test
    fun getServiceListingFlowWithEmptyServiceListingEmitsCorrectItems() = runTest {
        whenever(serviceStopsRepository.getServicesForStopFlow("123456"))
                .thenReturn(flowOf(emptyList()))

        val observer = retriever.getServiceListingFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                UiServiceListing.InProgress("123456"),
                UiServiceListing.Empty("123456"))
    }

    @Test
    fun getServiceListingFlowWithServiceListingEmitsCorrectItems() = runTest {
        whenever(serviceStopsRepository.getServicesForStopFlow("123456"))
                .thenReturn(flowOf(listOf("1", "2", "3")))

        val observer = retriever.getServiceListingFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                UiServiceListing.InProgress("123456"),
                UiServiceListing.Success(
                        "123456",
                        listOf("1", "2", "3")))
    }
}