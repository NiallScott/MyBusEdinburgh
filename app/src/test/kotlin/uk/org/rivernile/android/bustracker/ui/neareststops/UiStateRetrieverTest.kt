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

package uk.org.rivernile.android.bustracker.ui.neareststops

import app.cash.turbine.test
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.config.ConfigRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.FakeStopDetailsWithServices
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.FakeStopLocation
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.FakeStopName
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopOrientation
import uk.org.rivernile.android.bustracker.core.location.DeviceLocation
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [UiStateRetriever].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class UiStateRetrieverTest {

    @Mock
    private lateinit var locationRepository: LocationRepository
    @Mock
    private lateinit var busStopsRepository: BusStopsRepository
    @Mock
    private lateinit var configRepository: ConfigRepository

    private lateinit var retriever: UiStateRetriever

    @BeforeTest
    fun setUp() {
        retriever = UiStateRetriever(
            locationRepository,
            busStopsRepository,
            configRepository
        )
    }

    @Test
    fun getUiStateFlowWithNoLocationFeatureEmitsNoLocationFeatureError() = runTest {
        whenever(locationRepository.hasLocationFeature)
            .thenReturn(false)

        retriever.getUiStateFlow(emptyFlow(), emptyFlow()).test {
            assertEquals(UiState.Error.NoLocationFeature, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getUiStateFlowWithNoPermissionsEmitsPermissionsError() = runTest {
        givenHasLocationFeature()

        retriever
            .getUiStateFlow(
                flowOf(PermissionsState(PermissionState.UNGRANTED, PermissionState.UNGRANTED)),
                flowOf(null)
            )
            .test {
                assertEquals(UiState.Error.InsufficientLocationPermissions, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun getUiStateFlowWithLocationNotEnabledEmitsLocationOffError() = runTest {
        givenHasLocationFeature()
        whenever(locationRepository.isLocationEnabledFlow)
            .thenReturn(flowOf(false))

        retriever
            .getUiStateFlow(
                flowOf(PermissionsState(PermissionState.GRANTED, PermissionState.GRANTED)),
                flowOf(null)
            )
            .test {
                assertEquals(UiState.Error.LocationOff, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun getUiStateFlowWithNoLocationAvailableEmitsLocationUnknown() = runTest {
        givenHasLocationFeature()
        givenLocationIsEnabled()
        whenever(locationRepository.userVisibleLocationFlow)
            .thenReturn(emptyFlow())

        retriever
            .getUiStateFlow(
                flowOf(PermissionsState(PermissionState.GRANTED, PermissionState.GRANTED)),
                flowOf(null)
            )
            .test {
                assertEquals(UiState.Error.LocationUnknown, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun getUiStateFlowOnlyFineLocationPermissionDoesNotEmitPermissionError() = runTest {
        givenHasLocationFeature()
        givenLocationIsEnabled()
        whenever(locationRepository.userVisibleLocationFlow)
            .thenReturn(emptyFlow())

        retriever
            .getUiStateFlow(
                flowOf(PermissionsState(PermissionState.GRANTED, PermissionState.UNGRANTED)),
                flowOf(null)
            )
            .test {
                assertEquals(UiState.Error.LocationUnknown, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun getUiStateFlowOnlyCoarseLocationPermissionDoesNotEmitPermissionError() = runTest {
        givenHasLocationFeature()
        givenLocationIsEnabled()
        whenever(locationRepository.userVisibleLocationFlow)
            .thenReturn(emptyFlow())

        retriever
            .getUiStateFlow(
                flowOf(PermissionsState(PermissionState.UNGRANTED, PermissionState.GRANTED)),
                flowOf(null)
            )
            .test {
                assertEquals(UiState.Error.LocationUnknown, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun getUiStateFlowWithNullStopResultsEmitsNoNearestStops() = runTest {
        givenHasLocationFeature()
        givenLocationIsEnabled()
        whenever(locationRepository.userVisibleLocationFlow)
            .thenReturn(flowOf(DeviceLocation(10.0, 20.5)))
        whenever(configRepository.nearestStopsLatitudeSpan)
            .thenReturn(1.1)
        whenever(configRepository.nearestStopsLongitudeSpan)
            .thenReturn(2.2)
        whenever(busStopsRepository
            .getStopDetailsWithinSpanFlow(
                8.9,
                18.3,
                11.1,
                22.7,
                null
            )
        ).thenReturn(flowOf(null))

        retriever
            .getUiStateFlow(
                flowOf(PermissionsState(PermissionState.GRANTED, PermissionState.GRANTED)),
                flowOf(null)
            )
            .test {
                assertEquals(UiState.Error.LocationUnknown, awaitItem())
                assertEquals(UiState.Error.NoNearestStops, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun getUiStateFlowWithEmptyStopResultsEmitsNoNearestStops() = runTest {
        givenHasLocationFeature()
        givenLocationIsEnabled()
        whenever(locationRepository.userVisibleLocationFlow)
            .thenReturn(flowOf(DeviceLocation(10.0, 20.5)))
        whenever(configRepository.nearestStopsLatitudeSpan)
            .thenReturn(1.1)
        whenever(configRepository.nearestStopsLongitudeSpan)
            .thenReturn(2.2)
        whenever(busStopsRepository
            .getStopDetailsWithinSpanFlow(
                8.9,
                18.3,
                11.1,
                22.7,
                null
            )
        ).thenReturn(flowOf(emptyList()))

        retriever
            .getUiStateFlow(
                flowOf(PermissionsState(PermissionState.GRANTED, PermissionState.GRANTED)),
                flowOf(null)
            )
            .test {
                assertEquals(UiState.Error.LocationUnknown, awaitItem())
                assertEquals(UiState.Error.NoNearestStops, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun getUiStateFlowWithStopResultsEmitsNearestStops() = runTest {
        givenHasLocationFeature()
        givenLocationIsEnabled()
        val deviceLocation = DeviceLocation(10.0, 20.5)
        whenever(locationRepository.userVisibleLocationFlow)
            .thenReturn(flowOf(deviceLocation))
        whenever(configRepository.nearestStopsLatitudeSpan)
            .thenReturn(1.1)
        whenever(configRepository.nearestStopsLongitudeSpan)
            .thenReturn(2.2)
        val stopDetails1 = FakeStopDetailsWithServices(
            "111111",
            FakeStopName("Name 1", "Locality 1"),
            FakeStopLocation(1.1, 1.2),
            StopOrientation.NORTH,
            "1, 2, 3"
        )
        val stopDetails2 = FakeStopDetailsWithServices(
            "222222",
            FakeStopName("Name 2", "Locality 2"),
            FakeStopLocation(2.1, 2.2),
            StopOrientation.NORTH_EAST,
            "4, 5, 6"
        )
        val stopDetails3 = FakeStopDetailsWithServices(
            "333333",
            FakeStopName("Name 3", null),
            FakeStopLocation(3.1, 3.2),
            StopOrientation.EAST,
            null
        )
        whenever(busStopsRepository
            .getStopDetailsWithinSpanFlow(
                8.9,
                18.3,
                11.1,
                22.7,
                null
            )
        ).thenReturn(flowOf(listOf(stopDetails1, stopDetails2, stopDetails3)))
        whenever(locationRepository
            .distanceBetween(
                DeviceLocation(1.1, 1.2),
                deviceLocation
            )
        ).thenReturn(2.2f)
        whenever(locationRepository
            .distanceBetween(
                DeviceLocation(2.1, 2.2),
                deviceLocation
            )
        ).thenReturn(3.3f)
        whenever(locationRepository
            .distanceBetween(
                DeviceLocation(3.1, 3.2),
                deviceLocation
            )
        ).thenReturn(1.1f)

        retriever
            .getUiStateFlow(
                flowOf(PermissionsState(PermissionState.GRANTED, PermissionState.GRANTED)),
                flowOf(null)
            )
            .test {
                assertEquals(UiState.Error.LocationUnknown, awaitItem())
                assertEquals(
                    UiState.Success(
                        listOf(
                            UiNearestStop(
                                "333333",
                                FakeStopName("Name 3", null),
                                null,
                                1,
                                StopOrientation.EAST,
                                false
                            ),
                            UiNearestStop(
                                "111111",
                                FakeStopName("Name 1", "Locality 1"),
                                "1, 2, 3",
                                2,
                                StopOrientation.NORTH,
                                false
                            ),
                            UiNearestStop(
                                "222222",
                                FakeStopName("Name 2", "Locality 2"),
                                "4, 5, 6",
                                3,
                                StopOrientation.NORTH_EAST,
                                false
                            )
                        )
                    ),
                    awaitItem()
                )
                awaitComplete()
            }
    }

    @Test
    fun getUiStateFlowWithStopResultsEmitsNearestStopsWhenEmptyFilter() = runTest {
        givenHasLocationFeature()
        givenLocationIsEnabled()
        val deviceLocation = DeviceLocation(10.0, 20.5)
        whenever(locationRepository.userVisibleLocationFlow)
            .thenReturn(flowOf(deviceLocation))
        whenever(configRepository.nearestStopsLatitudeSpan)
            .thenReturn(1.1)
        whenever(configRepository.nearestStopsLongitudeSpan)
            .thenReturn(2.2)
        val stopDetails1 = FakeStopDetailsWithServices(
            "111111",
            FakeStopName("Name 1", "Locality 1"),
            FakeStopLocation(1.1, 1.2),
            StopOrientation.NORTH,
            "1, 2, 3"
        )
        val stopDetails2 = FakeStopDetailsWithServices(
            "222222",
            FakeStopName("Name 2", "Locality 2"),
            FakeStopLocation(2.1, 2.2),
            StopOrientation.NORTH_EAST,
            "4, 5, 6"
        )
        val stopDetails3 = FakeStopDetailsWithServices(
            "333333",
            FakeStopName("Name 3", null),
            FakeStopLocation(3.1, 3.2),
            StopOrientation.EAST,
            null
        )
        whenever(busStopsRepository
            .getStopDetailsWithinSpanFlow(
                8.9,
                18.3,
                11.1,
                22.7,
                emptySet()
            )
        ).thenReturn(flowOf(listOf(stopDetails1, stopDetails2, stopDetails3)))
        whenever(locationRepository
            .distanceBetween(
                DeviceLocation(1.1, 1.2),
                deviceLocation
            )
        ).thenReturn(2.2f)
        whenever(locationRepository
            .distanceBetween(
                DeviceLocation(2.1, 2.2),
                deviceLocation
            )
        ).thenReturn(3.3f)
        whenever(locationRepository
            .distanceBetween(
                DeviceLocation(3.1, 3.2),
                deviceLocation
            )
        ).thenReturn(1.1f)

        retriever
            .getUiStateFlow(
                flowOf(PermissionsState(PermissionState.GRANTED, PermissionState.GRANTED)),
                flowOf(emptySet())
            )
            .test {
                assertEquals(UiState.Error.LocationUnknown, awaitItem())
                assertEquals(
                    UiState.Success(
                        listOf(
                            UiNearestStop(
                                "333333",
                                FakeStopName("Name 3", null),
                                null,
                                1,
                                StopOrientation.EAST,
                                false
                            ),
                            UiNearestStop(
                                "111111",
                                FakeStopName("Name 1", "Locality 1"),
                                "1, 2, 3",
                                2,
                                StopOrientation.NORTH,
                                false
                            ),
                            UiNearestStop(
                                "222222",
                                FakeStopName("Name 2", "Locality 2"),
                                "4, 5, 6",
                                3,
                                StopOrientation.NORTH_EAST,
                                false
                            )
                        )
                    ),
                    awaitItem()
                )
                awaitComplete()
            }
    }

    @Test
    fun getUiStateFlowWithStopResultsEmitsNearestStopsWhenFilterSet() = runTest {
        givenHasLocationFeature()
        givenLocationIsEnabled()
        val deviceLocation = DeviceLocation(10.0, 20.5)
        whenever(locationRepository.userVisibleLocationFlow)
            .thenReturn(flowOf(deviceLocation))
        whenever(configRepository.nearestStopsLatitudeSpan)
            .thenReturn(1.1)
        whenever(configRepository.nearestStopsLongitudeSpan)
            .thenReturn(2.2)
        val stopDetails1 = FakeStopDetailsWithServices(
            "111111",
            FakeStopName("Name 1", "Locality 1"),
            FakeStopLocation(1.1, 1.2),
            StopOrientation.NORTH,
            "1, 2, 3"
        )
        val stopDetails2 = FakeStopDetailsWithServices(
            "222222",
            FakeStopName("Name 2", "Locality 2"),
            FakeStopLocation(2.1, 2.2),
            StopOrientation.NORTH_EAST,
            "4, 5, 6"
        )
        val stopDetails3 = FakeStopDetailsWithServices(
            "333333",
            FakeStopName("Name 3", null),
            FakeStopLocation(3.1, 3.2),
            StopOrientation.EAST,
            null
        )
        whenever(busStopsRepository
            .getStopDetailsWithinSpanFlow(
                8.9,
                18.3,
                11.1,
                22.7,
                setOf("1", "2", "3")
            )
        ).thenReturn(flowOf(listOf(stopDetails1, stopDetails2, stopDetails3)))
        whenever(locationRepository
            .distanceBetween(
                DeviceLocation(1.1, 1.2),
                deviceLocation
            )
        ).thenReturn(2.2f)
        whenever(locationRepository
            .distanceBetween(
                DeviceLocation(2.1, 2.2),
                deviceLocation
            )
        ).thenReturn(3.3f)
        whenever(locationRepository
            .distanceBetween(
                DeviceLocation(3.1, 3.2),
                deviceLocation
            )
        ).thenReturn(1.1f)

        retriever
            .getUiStateFlow(
                flowOf(PermissionsState(PermissionState.GRANTED, PermissionState.GRANTED)),
                flowOf(setOf("1", "2", "3"))
            )
            .test {
                assertEquals(UiState.Error.LocationUnknown, awaitItem())
                assertEquals(
                    UiState.Success(
                        listOf(
                            UiNearestStop(
                                "333333",
                                FakeStopName("Name 3", null),
                                null,
                                1,
                                StopOrientation.EAST,
                                false
                            ),
                            UiNearestStop(
                                "111111",
                                FakeStopName("Name 1", "Locality 1"),
                                "1, 2, 3",
                                2,
                                StopOrientation.NORTH,
                                false
                            ),
                            UiNearestStop(
                                "222222",
                                FakeStopName("Name 2", "Locality 2"),
                                "4, 5, 6",
                                3,
                                StopOrientation.NORTH_EAST,
                                false
                            )
                        )
                    ),
                    awaitItem()
                )
                awaitComplete()
            }
    }

    private fun givenHasLocationFeature() {
        whenever(locationRepository.hasLocationFeature)
            .thenReturn(true)
    }

    private fun givenLocationIsEnabled() {
        whenever(locationRepository.isLocationEnabledFlow)
            .thenReturn(flowOf(true))
    }
}