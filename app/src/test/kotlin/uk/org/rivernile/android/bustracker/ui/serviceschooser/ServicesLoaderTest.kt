/*
 * Copyright (C) 2023 - 2026 Niall 'Rivernile' Scott
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

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.ParcelableServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toParcelableNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.services.ServiceWithColour
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [ServicesLoader].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class ServicesLoaderTest {

    @Mock
    private lateinit var arguments: Arguments
    @Mock
    private lateinit var state: State
    @Mock
    private lateinit var servicesRepository: ServicesRepository

    private lateinit var loader: ServicesLoader

    @BeforeTest
    fun setUp() {
        loader = ServicesLoader(
            arguments,
            state,
            servicesRepository
        )
    }

    @Test
    fun servicesFlowWithNullParamsEmitsEmptyList() = runTest {
        whenever(arguments.paramsFlow)
            .thenReturn(flowOf(null))
        whenever(state.selectedServicesFlow)
            .thenReturn(flowOf(emptySet()))

        loader.servicesFlow.test {
            assertEquals(emptyList(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun servicesFlowWithAllServicesLoadsAllServices() = runTest {
        whenever(arguments.paramsFlow)
            .thenReturn(flowOf(ServicesChooserParams.AllServices(0, null)))
        whenever(servicesRepository.allServiceNamesWithColourFlow)
            .thenReturn(
                intervalFlowOf(
                    0L,
                    10L,
                    null,
                    emptyList(),
                    listOf(ServiceWithColour(service1, ServiceColours(1, 10))),
                    listOf(
                        ServiceWithColour(service1, ServiceColours(1, 10)),
                        ServiceWithColour(service2, null),
                        ServiceWithColour(service3, ServiceColours(3, 30))
                    )
                )
            )
        whenever(state.selectedServicesFlow)
            .thenReturn(flowOf(emptySet()))

        loader.servicesFlow.test {
            assertEquals(emptyList(), awaitItem())
            assertEquals(emptyList(), awaitItem())
            assertEquals(
                listOf(UiService(service1, ServiceColours(1, 10), false)),
                awaitItem()
            )
            assertEquals(
                listOf(
                    UiService(service1, ServiceColours(1, 10), false),
                    UiService(service2, null, false),
                    UiService(service3, ServiceColours(3, 30), false)
                ),
                awaitItem()
            )
            awaitComplete()
        }

        verify(servicesRepository, never())
            .getServiceNamesWithColourFlow(any())
    }

    @Test
    fun servicesFlowWithStopServicesLoadsStopServices() = runTest {
        whenever(arguments.paramsFlow)
            .thenReturn(
                flowOf(
                    ServicesChooserParams.Stop(
                        titleResId = 0,
                        selectedServices = null,
                        stopIdentifier = "123456".toParcelableNaptanStopIdentifier()
                    )
                )
            )
        whenever(servicesRepository
            .getServiceNamesWithColourFlow("123456".toNaptanStopIdentifier()))
            .thenReturn(
                intervalFlowOf(
                    0L,
                    10L,
                    null,
                    emptyList(),
                    listOf(ServiceWithColour(service1, ServiceColours(1, 10))),
                    listOf(
                        ServiceWithColour(service1, ServiceColours(1, 10)),
                        ServiceWithColour(service2, null),
                        ServiceWithColour(service3, ServiceColours(3, 30))
                    )
                )
            )
        whenever(state.selectedServicesFlow)
            .thenReturn(flowOf(emptySet()))

        loader.servicesFlow.test {
            assertEquals(emptyList(), awaitItem())
            assertEquals(emptyList(), awaitItem())
            assertEquals(
                listOf(UiService(service1, ServiceColours(1, 10), false)),
                awaitItem()
            )
            assertEquals(
                listOf(
                    UiService(service1, ServiceColours(1, 10), false),
                    UiService(service2, null, false),
                    UiService(service3, ServiceColours(3, 30), false)
                ),
                awaitItem()
            )
            awaitComplete()
        }

        verify(servicesRepository, never())
            .allServiceNamesWithColourFlow
    }

    @Test
    fun servicesFlowWithAllServicesRespondsToSelectionChanges() = runTest {
        whenever(arguments.paramsFlow)
            .thenReturn(flowOf(ServicesChooserParams.AllServices(0, null)))
        whenever(servicesRepository.allServiceNamesWithColourFlow)
            .thenReturn(
                flowOf(
                    listOf(
                        ServiceWithColour(service1, ServiceColours(1, 10)),
                        ServiceWithColour(service2, null),
                        ServiceWithColour(service3, ServiceColours(3, 30))
                    )
                )
            )
        whenever(state.selectedServicesFlow)
            .thenReturn(
                intervalFlowOf(
                    0L,
                    10L,
                    emptySet(),
                    setOf(parcelableService1),
                    setOf(parcelableService1, parcelableService3),
                    setOf(parcelableService4),
                    emptySet()
                )
            )

        loader.servicesFlow.test {
            assertEquals(
                listOf(
                    UiService(service1, ServiceColours(1, 10), false),
                    UiService(service2, null, false),
                    UiService(service3, ServiceColours(3, 30), false)
                ),
                awaitItem()
            )
            assertEquals(
                listOf(
                    UiService(service1, ServiceColours(1, 10), true),
                    UiService(service2, null, false),
                    UiService(service3, ServiceColours(3, 30), false)
                ),
                awaitItem()
            )
            assertEquals(
                listOf(
                    UiService(service1, ServiceColours(1, 10), true),
                    UiService(service2, null, false),
                    UiService(service3, ServiceColours(3, 30), true)
                ),
                awaitItem()
            )
            assertEquals(
                listOf(
                    UiService(service1, ServiceColours(1, 10), false),
                    UiService(service2, null, false),
                    UiService(service3, ServiceColours(3, 30), false)
                ),
                awaitItem()
            )
            assertEquals(
                listOf(
                    UiService(service1, ServiceColours(1, 10), false),
                    UiService(service2, null, false),
                    UiService(service3, ServiceColours(3, 30), false)
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun servicesFlowWithStopServicesRespondsToSelectionChanges() = runTest {
        whenever(arguments.paramsFlow)
            .thenReturn(
                flowOf(
                    ServicesChooserParams.Stop(
                        titleResId = 0,
                        selectedServices = null,
                        stopIdentifier = "123456".toParcelableNaptanStopIdentifier()
                    )
                )
            )
        whenever(servicesRepository
            .getServiceNamesWithColourFlow("123456".toNaptanStopIdentifier()))
            .thenReturn(
                flowOf(
                    listOf(
                        ServiceWithColour(service1, ServiceColours(1, 10)),
                        ServiceWithColour(service2, null),
                        ServiceWithColour(service3, ServiceColours(3, 30))
                    )
                )
            )
        whenever(state.selectedServicesFlow)
            .thenReturn(
                intervalFlowOf(
                    0L,
                    10L,
                    emptySet(),
                    setOf(parcelableService1),
                    setOf(parcelableService1, parcelableService3),
                    setOf(parcelableService4),
                    emptySet()
                )
            )

        loader.servicesFlow.test {
            assertEquals(
                listOf(
                    UiService(service1, ServiceColours(1, 10), false),
                    UiService(service2, null, false),
                    UiService(service3, ServiceColours(3, 30), false)
                ),
                awaitItem()
            )
            assertEquals(
                listOf(
                    UiService(service1, ServiceColours(1, 10), true),
                    UiService(service2, null, false),
                    UiService(service3, ServiceColours(3, 30), false)
                ),
                awaitItem()
            )
            assertEquals(
                listOf(
                    UiService(service1, ServiceColours(1, 10), true),
                    UiService(service2, null, false),
                    UiService(service3, ServiceColours(3, 30), true)
                ),
                awaitItem()
            )
            assertEquals(
                listOf(
                    UiService(service1, ServiceColours(1, 10), false),
                    UiService(service2, null, false),
                    UiService(service3, ServiceColours(3, 30), false)
                ),
                awaitItem()
            )
            assertEquals(
                listOf(
                    UiService(service1, ServiceColours(1, 10), false),
                    UiService(service2, null, false),
                    UiService(service3, ServiceColours(3, 30), false)
                ),
                awaitItem()
            )
        }
    }

    private val service1 get() = FakeServiceDescriptor(
        serviceName = "1",
        operatorCode = "TEST1"
    )

    private val service2 get() = FakeServiceDescriptor(
        serviceName = "2",
        operatorCode = "TEST2"
    )

    private val service3 get() = FakeServiceDescriptor(
        serviceName = "3",
        operatorCode = "TEST3"
    )

    private val parcelableService1 get() = ParcelableServiceDescriptor(
        serviceName = "1",
        operatorCode = "TEST1"
    )

    private val parcelableService3 get() = ParcelableServiceDescriptor(
        serviceName = "3",
        operatorCode = "TEST3"
    )

    private val parcelableService4 get() = ParcelableServiceDescriptor(
        serviceName = "4",
        operatorCode = "TEST4"
    )
}
