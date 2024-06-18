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

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.FakeStopDetails
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.FakeStopLocation
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.FakeStopName
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopOrientation
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [StopMarkersRetriever].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class StopMarkersRetrieverTest {

    companion object {

        private const val STATE_SELECTED_SERVICES = "selectedServices"
        private const val STATE_SELECTED_STOP_CODE = "selectedStopCode"
    }

    @Mock
    private lateinit var busStopsRepository: BusStopsRepository
    @Mock
    private lateinit var serviceListingRetriever: ServiceListingRetriever

    private val stopDetails1 = FakeStopDetails(
        "123456",
        FakeStopName(
            "Stop name 1",
            "Locality 1"
        ),
        FakeStopLocation(
            1.1,
            2.1
        ),
        StopOrientation.NORTH
    )
    private val stopDetails2 = FakeStopDetails(
        "987654",
        FakeStopName(
            "Stop name 2",
            "Locality 2"
        ),
        FakeStopLocation(
            1.2,
            2.2
        ),
        StopOrientation.NORTH_EAST
    )
    private val stopDetails3 = FakeStopDetails(
        "246802",
        FakeStopName(
            "Stop name 3",
            "Locality 4"
        ),
        FakeStopLocation(
            1.3,
            2.3
        ),
        StopOrientation.EAST
    )

    @Test
    fun stopMarkersFlowEmitsNullWhenStopsAreNull() = runTest {
        whenever(busStopsRepository.getStopDetailsWithServiceFilterFlow(null))
            .thenReturn(flowOf(null))
        whenever(serviceListingRetriever.getServiceListingFlow(null))
            .thenReturn(flowOf(null))
        val retriever = createRetriever()

        retriever.stopMarkersFlow.test {
            assertNull(awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun stopMarkersFlowEmitsNullWhenStopsAreEmpty() = runTest {
        whenever(busStopsRepository.getStopDetailsWithServiceFilterFlow(null))
            .thenReturn(flowOf(emptyList()))
        whenever(serviceListingRetriever.getServiceListingFlow(null))
            .thenReturn(flowOf(null))
        val retriever = createRetriever()

        retriever.stopMarkersFlow.test {
            assertNull(awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun stopMarkersFlowEmitsStopsFromBusStopsRepository() = runTest {
        whenever(busStopsRepository.getStopDetailsWithServiceFilterFlow(null))
            .thenReturn(
                flowOf(
                    listOf(
                        stopDetails1,
                        stopDetails2,
                        stopDetails3
                    )
                )
            )
        whenever(serviceListingRetriever.getServiceListingFlow(null))
            .thenReturn(flowOf(null))
        val retriever = createRetriever()
        val expected1 = UiStopMarker(
            "123456",
            FakeStopName(
                "Stop name 1",
                "Locality 1"
            ),
            LatLng(1.1, 2.1),
            StopOrientation.NORTH,
            null
        )
        val expected2 = UiStopMarker(
            "987654",
            FakeStopName(
                "Stop name 2",
                "Locality 2"
            ),
            LatLng(1.2, 2.2),
            StopOrientation.NORTH_EAST,
            null
        )
        val expected3 = UiStopMarker(
            "246802",
            FakeStopName(
                "Stop name 3",
                "Locality 3"
            ),
            LatLng(1.3, 2.3),
            StopOrientation.EAST,
            null
        )

        retriever.stopMarkersFlow.test {
            assertEquals(listOf(expected1, expected2, expected3), awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun stopMarkersEmitsStopsWithFilteredServicesFromState() = runTest {
        whenever(busStopsRepository.getStopDetailsWithServiceFilterFlow(setOf("1", "2", "3")))
            .thenReturn(
                flowOf(
                    listOf(stopDetails1)
                )
            )
        whenever(serviceListingRetriever.getServiceListingFlow(null))
            .thenReturn(flowOf(null))
        val retriever = createRetriever(
            SavedStateHandle(
                mapOf(STATE_SELECTED_SERVICES to arrayOf("1", "2", "3"))
            )
        )
        val expected1 = UiStopMarker(
            "123456",
            FakeStopName(
                "Stop name 1",
                "Locality 1"
            ),
            LatLng(1.1, 2.1),
            StopOrientation.NORTH,
            null
        )

        retriever.stopMarkersFlow.test {
            assertEquals(listOf(expected1), awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun stopMarkersFlowEmitsStopsWithFilteredServices() = runTest {
        whenever(busStopsRepository.getStopDetailsWithServiceFilterFlow(null))
            .thenReturn(
                flowOf(
                    listOf(
                        stopDetails1,
                        stopDetails2,
                        stopDetails3
                    )
                )
            )
        whenever(busStopsRepository.getStopDetailsWithServiceFilterFlow(setOf("1", "2", "3")))
            .thenReturn(
                flowOf(
                    listOf(stopDetails1)
                )
            )
        whenever(serviceListingRetriever.getServiceListingFlow(null))
            .thenReturn(flowOf(null))
        val savedState = SavedStateHandle()
        val retriever = createRetriever(savedState)
        val expected1 = UiStopMarker(
            "123456",
            FakeStopName(
                "Stop name 1",
                "Locality 1"
            ),
            LatLng(1.1, 2.1),
            StopOrientation.NORTH,
            null
        )
        val expected2 = UiStopMarker(
            "987654",
            FakeStopName(
                "Stop name 2",
                "Locality 2"
            ),
            LatLng(1.2, 2.2),
            StopOrientation.NORTH_EAST,
            null
        )
        val expected3 = UiStopMarker(
            "246802",
            FakeStopName(
                "Stop name 3",
                "Locality 3"
            ),
            LatLng(1.3, 2.3),
            StopOrientation.EAST,
            null
        )

        retriever.stopMarkersFlow.test {
            savedState[STATE_SELECTED_SERVICES] = arrayOf("1", "2", "3")

            assertEquals(listOf(expected1, expected2, expected3), awaitItem())
            assertEquals(listOf(expected1), awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun stopMarkersFlowDoesNotApplyServiceListingToNonMatchingStopCodes() = runTest {
        whenever(busStopsRepository.getStopDetailsWithServiceFilterFlow(null))
            .thenReturn(flowOf(listOf(stopDetails1)))
        whenever(serviceListingRetriever.getServiceListingFlow(null))
            .thenReturn(
                intervalFlowOf(
                    0L,
                    10L,
                    UiServiceListing.InProgress("192837"),
                    UiServiceListing.Empty("192837"),
                    UiServiceListing.Success("192837", listOf("1", "2", "3"))
                )
            )
        val retriever = createRetriever()
        val expected = UiStopMarker(
            "123456",
            FakeStopName(
                "Stop name 1",
                "Locality 1"
            ),
            LatLng(1.1, 2.1),
            StopOrientation.NORTH,
            null
        )

        retriever.stopMarkersFlow.test {
            assertEquals(listOf(expected), awaitItem())
            assertEquals(listOf(expected), awaitItem())
            assertEquals(listOf(expected), awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun stopMarkersFlowAppliesServiceListingToMatchingStopCodesFromState() = runTest {
        whenever(busStopsRepository.getStopDetailsWithServiceFilterFlow(null))
            .thenReturn(flowOf(listOf(stopDetails1)))
        whenever(serviceListingRetriever.getServiceListingFlow("123456"))
            .thenReturn(
                intervalFlowOf(
                    0L,
                    10L,
                    UiServiceListing.InProgress("123456"),
                    UiServiceListing.Empty("123456"),
                    UiServiceListing.Success("123456", listOf("1", "2", "3"))
                )
            )
        val retriever = createRetriever(
            SavedStateHandle(
                mapOf(STATE_SELECTED_STOP_CODE to "123456")
            )
        )
        val expected1 = UiStopMarker(
            "123456",
            FakeStopName(
                "Stop name 1",
                "Locality 1"
            ),
            LatLng(1.1, 2.1),
            StopOrientation.NORTH,
            UiServiceListing.InProgress("123456")
        )
        val expected2 = UiStopMarker(
            "123456",
            FakeStopName(
                "Stop name 1",
                "Locality 1"
            ),
            LatLng(1.1, 2.1),
            StopOrientation.NORTH,
            UiServiceListing.Empty("123456")
        )
        val expected3 = UiStopMarker(
            "123456",
            FakeStopName(
                "Stop name 1",
                "Locality 1"
            ),
            LatLng(1.1, 2.1),
            StopOrientation.NORTH,
            UiServiceListing.Success("123456", listOf("1", "2", "3"))
        )

        retriever.stopMarkersFlow.test {
            assertEquals(listOf(expected1), awaitItem())
            assertEquals(listOf(expected2), awaitItem())
            assertEquals(listOf(expected3), awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun stopMarkersFlowAppliesServiceListingToMatchingStopCodes() = runTest {
        whenever(busStopsRepository.getStopDetailsWithServiceFilterFlow(null))
            .thenReturn(flowOf(listOf(stopDetails1)))
        whenever(serviceListingRetriever.getServiceListingFlow(null))
            .thenReturn(flowOf(null))
        whenever(serviceListingRetriever.getServiceListingFlow("123456"))
            .thenReturn(
                intervalFlowOf(
                    0L,
                    10L,
                    UiServiceListing.InProgress("123456"),
                    UiServiceListing.Empty("123456"),
                    UiServiceListing.Success("123456", listOf("1", "2", "3"))
                )
            )
        val savedState = SavedStateHandle()
        val retriever = createRetriever(savedState)
        val expected1 = UiStopMarker(
            "123456",
            FakeStopName(
                "Stop name 1",
                "Locality 1"
            ),
            LatLng(1.1, 2.1),
            StopOrientation.NORTH,
            null
        )
        val expected2 = UiStopMarker(
            "123456",
            FakeStopName(
                "Stop name 1",
                "Locality 1"
            ),
            LatLng(1.1, 2.1),
            StopOrientation.NORTH,
            UiServiceListing.InProgress("123456")
        )
        val expected3 = UiStopMarker(
            "123456",
            FakeStopName(
                "Stop name 1",
                "Locality 1"
            ),
            LatLng(1.1, 2.1),
            StopOrientation.NORTH,
            UiServiceListing.Empty("123456")
        )
        val expected4 = UiStopMarker(
            "123456",
            FakeStopName(
                "Stop name 1",
                "Locality 1"
            ),
            LatLng(1.1, 2.1),
            StopOrientation.NORTH,
            UiServiceListing.Success("123456", listOf("1", "2", "3"))
        )

        retriever.stopMarkersFlow.test {
            savedState[STATE_SELECTED_STOP_CODE] = "123456"

            assertEquals(listOf(expected1), awaitItem())
            assertEquals(listOf(expected2), awaitItem())
            assertEquals(listOf(expected3), awaitItem())
            assertEquals(listOf(expected4), awaitItem())
            ensureAllEventsConsumed()
        }
    }

    private fun createRetriever(savedStateHandle: SavedStateHandle = SavedStateHandle()) =
        StopMarkersRetriever(
            savedStateHandle,
            busStopsRepository,
            serviceListingRetriever
        )
}