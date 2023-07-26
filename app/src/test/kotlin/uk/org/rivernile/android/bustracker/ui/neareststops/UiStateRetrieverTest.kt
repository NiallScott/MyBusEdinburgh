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

package uk.org.rivernile.android.bustracker.ui.neareststops

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
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
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.config.ConfigRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopDetailsWithServices
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopLocation
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopName
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopOrientation
import uk.org.rivernile.android.bustracker.core.location.DeviceLocation
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [UiStateRetriever].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class UiStateRetrieverTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var locationRepository: LocationRepository
    @Mock
    private lateinit var busStopsRepository: BusStopsRepository
    @Mock
    private lateinit var configRepository: ConfigRepository

    private lateinit var retriever: UiStateRetriever

    @Before
    fun setUp() {
        retriever = UiStateRetriever(
                locationRepository,
                busStopsRepository,
                configRepository)
    }

    @Test
    fun getUiStateFlowWithNoLocationFeatureEmitsNoLocationFeatureError() = runTest {
        whenever(locationRepository.hasLocationFeature)
                .thenReturn(false)

        val observer = retriever.getUiStateFlow(emptyFlow(), emptyFlow()).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiState.Error.NoLocationFeature)
    }

    @Test
    fun getUiStateFlowWithNoPermissionsEmitsPermissionsError() = runTest {
        givenHasLocationFeature()

        val observer = retriever.getUiStateFlow(
                flowOf(PermissionsState(PermissionState.UNGRANTED, PermissionState.UNGRANTED)),
                flowOf(null))
                .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiState.Error.InsufficientLocationPermissions)
    }

    @Test
    fun getUiStateFlowWithLocationNotEnabledEmitsLocationOffError() = runTest {
        givenHasLocationFeature()
        whenever(locationRepository.isLocationEnabledFlow)
                .thenReturn(flowOf(false))

        val observer = retriever.getUiStateFlow(
                flowOf(PermissionsState(PermissionState.GRANTED, PermissionState.GRANTED)),
                flowOf(null))
                .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiState.Error.LocationOff)
    }

    @Test
    fun getUiStateFlowWithNoLocationAvailableEmitsLocationUnknown() = runTest {
        givenHasLocationFeature()
        givenLocationIsEnabled()
        whenever(locationRepository.userVisibleLocationFlow)
                .thenReturn(emptyFlow())

        val observer = retriever.getUiStateFlow(
                flowOf(PermissionsState(PermissionState.GRANTED, PermissionState.GRANTED)),
                flowOf(null))
                .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiState.Error.LocationUnknown)
    }

    @Test
    fun getUiStateFlowOnlyFineLocationPermissionDoesNotEmitPermissionError() = runTest {
        givenHasLocationFeature()
        givenLocationIsEnabled()
        whenever(locationRepository.userVisibleLocationFlow)
                .thenReturn(emptyFlow())

        val observer = retriever.getUiStateFlow(
                flowOf(PermissionsState(PermissionState.GRANTED, PermissionState.UNGRANTED)),
                flowOf(null))
                .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiState.Error.LocationUnknown)
    }

    @Test
    fun getUiStateFlowOnlyCoarseLocationPermissionDoesNotEmitPermissionError() = runTest {
        givenHasLocationFeature()
        givenLocationIsEnabled()
        whenever(locationRepository.userVisibleLocationFlow)
                .thenReturn(emptyFlow())

        val observer = retriever.getUiStateFlow(
                flowOf(PermissionsState(PermissionState.UNGRANTED, PermissionState.GRANTED)),
                flowOf(null))
                .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiState.Error.LocationUnknown)
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
        whenever(busStopsRepository.getStopDetailsWithinSpanFlow(
                8.9,
                18.3,
                11.1,
                22.7,
                null))
                .thenReturn(flowOf(null))

        val observer = retriever.getUiStateFlow(
                flowOf(PermissionsState(PermissionState.GRANTED, PermissionState.GRANTED)),
                flowOf(null))
                .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                UiState.Error.LocationUnknown,
                UiState.Error.NoNearestStops)
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
        whenever(busStopsRepository.getStopDetailsWithinSpanFlow(
                8.9,
                18.3,
                11.1,
                22.7,
                null))
                .thenReturn(flowOf(emptyList()))

        val observer = retriever.getUiStateFlow(
                flowOf(PermissionsState(PermissionState.GRANTED, PermissionState.GRANTED)),
                flowOf(null))
                .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                UiState.Error.LocationUnknown,
                UiState.Error.NoNearestStops)
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
        val stopDetails1 = MockStopDetailsWithServices(
            "111111",
            MockStopName("Name 1", "Locality 1"),
            MockStopLocation(1.1, 1.2),
            StopOrientation.NORTH,
            "1, 2, 3")
        val stopDetails2 = MockStopDetailsWithServices(
            "222222",
            MockStopName("Name 2", "Locality 2"),
            MockStopLocation(2.1, 2.2),
            StopOrientation.NORTH_EAST,
            "4, 5, 6")
        val stopDetails3 = MockStopDetailsWithServices(
            "333333",
            MockStopName("Name 3", null),
            MockStopLocation(3.1, 3.2),
            StopOrientation.EAST,
            null)
        whenever(busStopsRepository.getStopDetailsWithinSpanFlow(
            8.9,
            18.3,
            11.1,
            22.7,
            null))
            .thenReturn(flowOf(listOf(stopDetails1, stopDetails2, stopDetails3)))
        whenever(locationRepository.distanceBetween(
            DeviceLocation(1.1, 1.2),
            deviceLocation))
            .thenReturn(2.2f)
        whenever(locationRepository.distanceBetween(
            DeviceLocation(2.1, 2.2),
            deviceLocation))
            .thenReturn(3.3f)
        whenever(locationRepository.distanceBetween(
            DeviceLocation(3.1, 3.2),
            deviceLocation))
            .thenReturn(1.1f)

        val observer = retriever.getUiStateFlow(
            flowOf(PermissionsState(PermissionState.GRANTED, PermissionState.GRANTED)),
            flowOf(null))
            .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            UiState.Error.LocationUnknown,
            UiState.Success(
                listOf(
                    UiNearestStop(
                        "333333",
                        MockStopName("Name 3", null),
                        null,
                        1,
                        StopOrientation.EAST,
                        false),
                    UiNearestStop(
                        "111111",
                        MockStopName("Name 1", "Locality 1"),
                        "1, 2, 3",
                        2,
                        StopOrientation.NORTH,
                        false),
                    UiNearestStop(
                        "222222",
                        MockStopName("Name 2", "Locality 2"),
                        "4, 5, 6",
                        3,
                        StopOrientation.NORTH_EAST,
                        false))))
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
        val stopDetails1 = MockStopDetailsWithServices(
            "111111",
            MockStopName("Name 1", "Locality 1"),
            MockStopLocation(1.1, 1.2),
            StopOrientation.NORTH,
            "1, 2, 3")
        val stopDetails2 = MockStopDetailsWithServices(
            "222222",
            MockStopName("Name 2", "Locality 2"),
            MockStopLocation(2.1, 2.2),
            StopOrientation.NORTH_EAST,
            "4, 5, 6")
        val stopDetails3 = MockStopDetailsWithServices(
            "333333",
            MockStopName("Name 3", null),
            MockStopLocation(3.1, 3.2),
            StopOrientation.EAST,
            null)
        whenever(busStopsRepository.getStopDetailsWithinSpanFlow(
            8.9,
            18.3,
            11.1,
            22.7,
            emptySet()))
            .thenReturn(flowOf(listOf(stopDetails1, stopDetails2, stopDetails3)))
        whenever(locationRepository.distanceBetween(
            DeviceLocation(1.1, 1.2),
            deviceLocation))
            .thenReturn(2.2f)
        whenever(locationRepository.distanceBetween(
            DeviceLocation(2.1, 2.2),
            deviceLocation))
            .thenReturn(3.3f)
        whenever(locationRepository.distanceBetween(
            DeviceLocation(3.1, 3.2),
            deviceLocation))
            .thenReturn(1.1f)

        val observer = retriever.getUiStateFlow(
            flowOf(PermissionsState(PermissionState.GRANTED, PermissionState.GRANTED)),
            flowOf(emptySet()))
            .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            UiState.Error.LocationUnknown,
            UiState.Success(
                listOf(
                    UiNearestStop(
                        "333333",
                        MockStopName("Name 3", null),
                        null,
                        1,
                        StopOrientation.EAST,
                        false),
                    UiNearestStop(
                        "111111",
                        MockStopName("Name 1", "Locality 1"),
                        "1, 2, 3",
                        2,
                        StopOrientation.NORTH,
                        false),
                    UiNearestStop(
                        "222222",
                        MockStopName("Name 2", "Locality 2"),
                        "4, 5, 6",
                        3,
                        StopOrientation.NORTH_EAST,
                        false))))
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
        val stopDetails1 = MockStopDetailsWithServices(
            "111111",
            MockStopName("Name 1", "Locality 1"),
            MockStopLocation(1.1, 1.2),
            StopOrientation.NORTH,
            "1, 2, 3")
        val stopDetails2 = MockStopDetailsWithServices(
            "222222",
            MockStopName("Name 2", "Locality 2"),
            MockStopLocation(2.1, 2.2),
            StopOrientation.NORTH_EAST,
            "4, 5, 6")
        val stopDetails3 = MockStopDetailsWithServices(
            "333333",
            MockStopName("Name 3", null),
            MockStopLocation(3.1, 3.2),
            StopOrientation.EAST,
            null)
        whenever(busStopsRepository.getStopDetailsWithinSpanFlow(
            8.9,
            18.3,
            11.1,
            22.7,
            setOf("1", "2", "3")))
            .thenReturn(flowOf(listOf(stopDetails1, stopDetails2, stopDetails3)))
        whenever(locationRepository.distanceBetween(
            DeviceLocation(1.1, 1.2),
            deviceLocation))
            .thenReturn(2.2f)
        whenever(locationRepository.distanceBetween(
            DeviceLocation(2.1, 2.2),
            deviceLocation))
            .thenReturn(3.3f)
        whenever(locationRepository.distanceBetween(
            DeviceLocation(3.1, 3.2),
            deviceLocation))
            .thenReturn(1.1f)

        val observer = retriever.getUiStateFlow(
            flowOf(PermissionsState(PermissionState.GRANTED, PermissionState.GRANTED)),
            flowOf(setOf("1", "2", "3")))
            .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            UiState.Error.LocationUnknown,
            UiState.Success(
                listOf(
                    UiNearestStop(
                        "333333",
                        MockStopName("Name 3", null),
                        null,
                        1,
                        StopOrientation.EAST,
                        false),
                    UiNearestStop(
                        "111111",
                        MockStopName("Name 1", "Locality 1"),
                        "1, 2, 3",
                        2,
                        StopOrientation.NORTH,
                        false),
                    UiNearestStop(
                        "222222",
                        MockStopName("Name 2", "Locality 2"),
                        "4, 5, 6",
                        3,
                        StopOrientation.NORTH_EAST,
                        false))))
    }

    private fun givenHasLocationFeature() {
        whenever(locationRepository.hasLocationFeature)
                .thenReturn(true)
    }

    private fun givenLocationIsEnabled() {
        whenever(locationRepository.isLocationEnabledFlow)
                .thenReturn(flowOf(true))
    }

    private data class MockStopName(
        override val name: String,
        override val locality: String?) : StopName

    private data class MockStopLocation(
        override val latitude: Double,
        override val longitude: Double) : StopLocation

    private data class MockStopDetailsWithServices(
        override val stopCode: String,
        override val stopName: StopName,
        override val location: StopLocation,
        override val orientation: StopOrientation,
        override val serviceListing: String?) : StopDetailsWithServices
}