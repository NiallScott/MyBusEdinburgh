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

package uk.org.rivernile.android.bustracker.ui.bustimes.details

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.FakeStopDetails
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.FakeStopLocation
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.FakeStopName
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopOrientation
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [UiItemRetriever].
 */
@RunWith(MockitoJUnitRunner::class)
class UiItemRetrieverTest {

    @Mock
    private lateinit var busStopsRepository: BusStopsRepository
    @Mock
    private lateinit var distanceRetriever: DistanceRetriever
    @Mock
    private lateinit var servicesRetriever: ServicesRetriever
    @Mock
    private lateinit var featureRepository: FeatureRepository

    private lateinit var retriever: UiItemRetriever

    @BeforeTest
    fun setUp() {
        retriever = UiItemRetriever(
            busStopsRepository,
            distanceRetriever,
            servicesRetriever,
            featureRepository
        )
    }

    @Test
    fun createUiItemFlowEmitsUnknownDistanceAndNoServicesWhenStopCodeIsNull() = runTest {
        whenever(distanceRetriever.createDistanceFlow(any(), any()))
            .thenReturn(flowOf(UiItem.Distance.Unknown))

        retriever
            .createUiItemFlow(
                MutableStateFlow<String?>(null),
                flowOf(
                    PermissionsState(
                        PermissionState.GRANTED,
                        PermissionState.GRANTED
                    )
                ),
                backgroundScope
            )
            .test {
                assertEquals(
                    listOf(
                        UiItem.Distance.Unknown,
                        UiItem.NoServices
                    ),
                    awaitItem()
                )
                ensureAllEventsConsumed()
            }
    }

    @Test
    fun createUiItemFlowEmitsUnknownDistanceAndNoServicesWhenStopServicesNull() = runTest {
        whenever(distanceRetriever.createDistanceFlow(any(), any()))
            .thenReturn(flowOf(UiItem.Distance.Unknown))
        whenever(busStopsRepository.getBusStopDetailsFlow("123456"))
            .thenReturn(flowOf(null))
        whenever(servicesRetriever.getServicesFlow("123456"))
            .thenReturn(flowOf(null))

        retriever
            .createUiItemFlow(
                MutableStateFlow<String?>("123456"),
                flowOf(
                    PermissionsState(
                        PermissionState.GRANTED,
                        PermissionState.GRANTED
                    )
                ),
                backgroundScope
            )
            .test {
                assertEquals(
                    listOf(
                        UiItem.Distance.Unknown,
                        UiItem.NoServices
                    ),
                    awaitItem()
                )
                ensureAllEventsConsumed()
            }
    }

    @Test
    fun createUiItemFlowEmitsNoServicesWhenStopDetailsIsNullAndServicesEmpty() = runTest {
        val services = emptyList<UiItem.Service>()
        whenever(distanceRetriever.createDistanceFlow(any(), any()))
            .thenReturn(flowOf(UiItem.Distance.Unknown))
        whenever(busStopsRepository.getBusStopDetailsFlow("123456"))
            .thenReturn(flowOf(null))
        whenever(servicesRetriever.getServicesFlow("123456"))
            .thenReturn(flowOf(services))

        retriever
            .createUiItemFlow(
                MutableStateFlow<String?>("123456"),
                flowOf(
                    PermissionsState(
                        PermissionState.GRANTED,
                        PermissionState.GRANTED
                    )
                ),
                backgroundScope
            )
            .test {
                assertEquals(
                    listOf(
                        UiItem.Distance.Unknown,
                        UiItem.NoServices
                    ),
                    awaitItem()
                )
                ensureAllEventsConsumed()
            }
    }

    @Test
    fun createUiItemFlowEmitsServicesWhenStopDetailsIsNullAndServicesPopulated() = runTest {
        val service1 = mock<UiItem.Service>()
        val service2 = mock<UiItem.Service>()
        val service3 = mock<UiItem.Service>()
        val services = listOf(service1, service2, service3)
        whenever(distanceRetriever.createDistanceFlow(any(), any()))
            .thenReturn(flowOf(UiItem.Distance.Unknown))
        whenever(busStopsRepository.getBusStopDetailsFlow("123456"))
            .thenReturn(flowOf(null))
        whenever(servicesRetriever.getServicesFlow("123456"))
            .thenReturn(flowOf(services))

        retriever
            .createUiItemFlow(
                MutableStateFlow<String?>("123456"),
                flowOf(
                    PermissionsState(
                        PermissionState.GRANTED,
                        PermissionState.GRANTED
                    )
                ),
                backgroundScope
            )
            .test {
                assertEquals(
                    listOf(
                        UiItem.Distance.Unknown,
                        service1,
                        service2,
                        service3),
                    awaitItem()
                )
                ensureAllEventsConsumed()
            }
    }

    @Test
    fun createUiItemFlowDoesNotEmitMapItemWhenDoesNotHaveMapFeature() = runTest {
        val stopDetails = createStopDetails()
        givenStopMapFeatureAvailability(false)
        whenever(distanceRetriever.createDistanceFlow(any(), any()))
            .thenReturn(flowOf(UiItem.Distance.Known(1.2f)))
        whenever(busStopsRepository.getBusStopDetailsFlow("123456"))
            .thenReturn(flowOf(stopDetails))
        whenever(servicesRetriever.getServicesFlow("123456"))
            .thenReturn(flowOf(null))

        retriever
            .createUiItemFlow(
                MutableStateFlow<String?>("123456"),
                flowOf(
                    PermissionsState(
                        PermissionState.GRANTED,
                        PermissionState.GRANTED
                    )
                ),
                backgroundScope
            )
            .test {
                assertEquals(
                    listOf(
                        UiItem.Distance.Known(1.2f),
                        UiItem.NoServices
                    ),
                    awaitItem()
                )
                ensureAllEventsConsumed()
            }
    }

    @Test
    fun createUiItemFlowEmitsMapItemWhenHasMapFeature() = runTest {
        val stopDetails = createStopDetails()
        givenStopMapFeatureAvailability(true)
        whenever(distanceRetriever.createDistanceFlow(any(), any()))
            .thenReturn(flowOf(UiItem.Distance.Known(1.2f)))
        whenever(busStopsRepository.getBusStopDetailsFlow("123456"))
            .thenReturn(flowOf(stopDetails))
        whenever(servicesRetriever.getServicesFlow("123456"))
            .thenReturn(flowOf(null))

        retriever
            .createUiItemFlow(
                MutableStateFlow<String?>("123456"),
                flowOf(
                    PermissionsState(
                        PermissionState.GRANTED,
                        PermissionState.GRANTED
                    )
                ),
                backgroundScope
            )
            .test {
                assertEquals(
                    listOf(
                        UiItem.Map(
                            1.1,
                            2.2,
                            StopOrientation.EAST
                        ),
                        UiItem.Distance.Known(1.2f),
                        UiItem.NoServices
                    ),
                    awaitItem()
                )
                ensureAllEventsConsumed()
            }
    }

    @Test
    fun createUiItemFlowWithRepresentativeExample() = runTest {
        val stopDetails = createStopDetails()
        val service1 = mock<UiItem.Service>()
        val service2 = mock<UiItem.Service>()
        val service3 = mock<UiItem.Service>()
        val services = listOf(service1, service2, service3)
        givenStopMapFeatureAvailability(true)
        whenever(distanceRetriever.createDistanceFlow(any(), any()))
            .thenReturn(flowOf(UiItem.Distance.Known(1.2f)))
        whenever(busStopsRepository.getBusStopDetailsFlow("123456"))
            .thenReturn(flowOf(stopDetails))
        whenever(servicesRetriever.getServicesFlow("123456"))
            .thenReturn(flowOf(services))

        retriever
            .createUiItemFlow(
                MutableStateFlow<String?>("123456"),
                flowOf(
                    PermissionsState(
                        PermissionState.GRANTED,
                        PermissionState.GRANTED
                    )
                ),
                backgroundScope
            )
            .test {
                assertEquals(
                    listOf(
                        UiItem.Map(
                            1.1,
                            2.2,
                            StopOrientation.EAST
                        ),
                        UiItem.Distance.Known(1.2f),
                        service1,
                        service2,
                        service3
                    ),
                    awaitItem()
                )
                ensureAllEventsConsumed()
            }
    }

    private fun givenStopMapFeatureAvailability(isAvailable: Boolean) {
        whenever(featureRepository.hasStopMapUiFeature)
            .thenReturn(isAvailable)
    }

    private fun createStopDetails() = FakeStopDetails(
        "123456",
        FakeStopName(
            "Stop name",
            "Locality"
        ),
        FakeStopLocation(
            1.1,
            2.2
        ),
        StopOrientation.EAST
    )
}