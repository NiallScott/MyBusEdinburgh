/*
 * Copyright (C) 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.serviceschooser

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
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import uk.org.rivernile.android.bustracker.core.servicestops.ServiceStopsRepository
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [ServicesLoader].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class ServicesLoaderTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var arguments: Arguments
    @Mock
    private lateinit var state: State
    @Mock
    private lateinit var servicesRepository: ServicesRepository
    @Mock
    private lateinit var serviceStopsRepository: ServiceStopsRepository

    private lateinit var loader: ServicesLoader

    @Before
    fun setUp() {
        loader = ServicesLoader(
            arguments,
            state,
            servicesRepository,
            serviceStopsRepository)
    }

    @Test
    fun servicesFlowWithNullParamsEmitsEmptyList() = runTest {
        whenever(arguments.paramsFlow)
            .thenReturn(flowOf(null))
        whenever(state.selectedServicesFlow)
            .thenReturn(flowOf(emptySet()))

        val observer = loader.servicesFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(emptyList())
    }

    @Test
    fun servicesFlowWithAllServicesLoadsAllServices() = runTest {
        whenever(arguments.paramsFlow)
            .thenReturn(flowOf(ServicesChooserParams.AllServices(0, null)))
        whenever(servicesRepository.allServiceNamesFlow)
            .thenReturn(
                intervalFlowOf(
                    0L,
                    10L,
                    null,
                    emptyList(),
                    listOf("1"),
                    listOf("1", "2", "3")))
        whenever(servicesRepository.getColoursForServicesFlow(setOf("1")))
            .thenReturn(flowOf(mapOf("1" to 1)))
        whenever(servicesRepository.getColoursForServicesFlow(setOf("1", "2", "3")))
            .thenReturn(flowOf(
                mapOf(
                    "1" to 1,
                    "3" to 3)))
        whenever(state.selectedServicesFlow)
            .thenReturn(flowOf(emptySet()))

        val observer = loader.servicesFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            emptyList(),
            emptyList(),
            listOf(UiService("1", 1, false)),
            listOf(
                UiService("1", 1, false),
                UiService("2", null, false),
                UiService("3", 3, false)))
        verify(serviceStopsRepository, never())
            .getServicesForStopFlow(any())
    }

    @Test
    fun servicesFlowWithStopServicesLoadsStopServices() = runTest {
        whenever(arguments.paramsFlow)
            .thenReturn(flowOf(ServicesChooserParams.Stop(0, null, "123456")))
        whenever(serviceStopsRepository.getServicesForStopFlow("123456"))
            .thenReturn(
                intervalFlowOf(
                    0L,
                    10L,
                    null,
                    emptyList(),
                    listOf("1"),
                    listOf("1", "2", "3")))
        whenever(servicesRepository.getColoursForServicesFlow(setOf("1")))
            .thenReturn(flowOf(mapOf("1" to 1)))
        whenever(servicesRepository.getColoursForServicesFlow(setOf("1", "2", "3")))
            .thenReturn(flowOf(
                mapOf(
                    "1" to 1,
                    "3" to 3)))
        whenever(state.selectedServicesFlow)
            .thenReturn(flowOf(emptySet()))

        val observer = loader.servicesFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            emptyList(),
            emptyList(),
            listOf(UiService("1", 1, false)),
            listOf(
                UiService("1", 1, false),
                UiService("2", null, false),
                UiService("3", 3, false)))
        verify(servicesRepository, never())
            .allServiceNamesFlow
    }

    @Test
    fun servicesFlowWithAllServicesRespondsToSelectionChanges() = runTest {
        whenever(arguments.paramsFlow)
            .thenReturn(flowOf(ServicesChooserParams.AllServices(0, null)))
        whenever(servicesRepository.allServiceNamesFlow)
            .thenReturn(flowOf(listOf("1", "2", "3")))
        whenever(servicesRepository.getColoursForServicesFlow(setOf("1", "2", "3")))
            .thenReturn(flowOf(
                mapOf(
                    "1" to 1,
                    "3" to 3)))
        whenever(state.selectedServicesFlow)
            .thenReturn(intervalFlowOf(
                0L,
                10L,
                emptySet(),
                setOf("1"),
                setOf("1", "3"),
                setOf("4"),
                emptySet()))

        val observer = loader.servicesFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            listOf(
                UiService("1", 1, false),
                UiService("2", null, false),
                UiService("3", 3, false)),
            listOf(
                UiService("1", 1, true),
                UiService("2", null, false),
                UiService("3", 3, false)),
            listOf(
                UiService("1", 1, true),
                UiService("2", null, false),
                UiService("3", 3, true)),
            listOf(
                UiService("1", 1, false),
                UiService("2", null, false),
                UiService("3", 3, false)),
            listOf(
                UiService("1", 1, false),
                UiService("2", null, false),
                UiService("3", 3, false)))
    }

    @Test
    fun servicesFlowWithStopServicesRespondsToSelectionChanges() = runTest {
        whenever(arguments.paramsFlow)
            .thenReturn(flowOf(ServicesChooserParams.Stop(0, null, "123456")))
        whenever(serviceStopsRepository.getServicesForStopFlow("123456"))
            .thenReturn(flowOf(listOf("1", "2", "3")))
        whenever(servicesRepository.getColoursForServicesFlow(setOf("1", "2", "3")))
            .thenReturn(flowOf(
                mapOf(
                    "1" to 1,
                    "3" to 3)))
        whenever(state.selectedServicesFlow)
            .thenReturn(intervalFlowOf(
                0L,
                10L,
                emptySet(),
                setOf("1"),
                setOf("1", "3"),
                setOf("4"),
                emptySet()))

        val observer = loader.servicesFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            listOf(
                UiService("1", 1, false),
                UiService("2", null, false),
                UiService("3", 3, false)),
            listOf(
                UiService("1", 1, true),
                UiService("2", null, false),
                UiService("3", 3, false)),
            listOf(
                UiService("1", 1, true),
                UiService("2", null, false),
                UiService("3", 3, true)),
            listOf(
                UiService("1", 1, false),
                UiService("2", null, false),
                UiService("3", 3, false)),
            listOf(
                UiService("1", 1, false),
                UiService("2", null, false),
                UiService("3", 3, false)))
    }
}