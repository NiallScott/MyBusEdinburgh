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

package uk.org.rivernile.android.bustracker.ui.bustimes.details

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
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
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopDetails
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopName
import uk.org.rivernile.android.bustracker.core.location.DeviceLocation
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [DistanceRetriever].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class DistanceRetrieverTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var locationRepository: LocationRepository

    private lateinit var retriever: DistanceRetriever

    @Before
    fun setUp() {
        retriever = DistanceRetriever(locationRepository)
    }

    @Test
    fun createDistanceFlowWithoutLocationFeatureEmitsNoLocationFeature() = runTest {
        val permissionsStateFlow = flowOf(grantedPermissionsState)
        val stopDetailsFlow = flowOf(stopDetails)
        givenHasLocationFeatureState(false)

        val observer = retriever.createDistanceFlow(permissionsStateFlow, stopDetailsFlow)
                .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiItem.Distance.NoLocationFeature)
    }

    @Test
    fun createDistanceFlowWithoutLocationPermissionsEmitsPermissionDenied() = runTest {
        val permissionsStateFlow = flowOf(
                PermissionsState(PermissionState.DENIED, PermissionState.UNGRANTED))
        val stopDetailsFlow = flowOf(stopDetails)
        givenHasLocationFeatureState(true)

        val observer = retriever.createDistanceFlow(permissionsStateFlow, stopDetailsFlow)
                .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiItem.Distance.PermissionDenied)
    }

    @Test
    fun createDistanceFlowWhenLocationIsOffEmitsLocationOff() = runTest {
        val permissionsStateFlow = flowOf(grantedPermissionsState)
        val stopDetailsFlow = flowOf(stopDetails)
        givenHasLocationFeatureState(true)
        whenever(locationRepository.isLocationEnabledFlow)
                .thenReturn(flowOf(false))

        val observer = retriever.createDistanceFlow(permissionsStateFlow, stopDetailsFlow)
                .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiItem.Distance.LocationOff)
    }

    @Test
    fun createDistanceFlowWithNullStopDetailsEmitsUnknown() = runTest {
        val permissionsStateFlow = flowOf(grantedPermissionsState)
        val stopDetailsFlow = flowOf<StopDetails?>(null)
        givenHasLocationFeatureState(true)
        whenever(locationRepository.isLocationEnabledFlow)
                .thenReturn(flowOf(true))
        whenever(locationRepository.userVisibleLocationFlow)
                .thenReturn(flowOf(DeviceLocation(9.0, 8.0)))

        val observer = retriever.createDistanceFlow(permissionsStateFlow, stopDetailsFlow)
                .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiItem.Distance.Unknown)
    }

    @Test
    fun createDistanceFlowWithNoLocationEmitsUnknown() = runTest {
        val permissionsStateFlow = flowOf(grantedPermissionsState)
        val stopDetailsFlow = flowOf<StopDetails?>(stopDetails)
        givenHasLocationFeatureState(true)
        whenever(locationRepository.isLocationEnabledFlow)
                .thenReturn(flowOf(true))
        whenever(locationRepository.userVisibleLocationFlow)
                .thenReturn(emptyFlow())

        val observer = retriever.createDistanceFlow(permissionsStateFlow, stopDetailsFlow)
                .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiItem.Distance.Unknown)
    }

    @Test
    fun createDistanceFlowWithNegativeDistanceEmitsUnknown() = runTest {
        val permissionsStateFlow = flowOf(grantedPermissionsState)
        val stopDetailsFlow = flowOf<StopDetails?>(stopDetails)
        givenHasLocationFeatureState(true)
        whenever(locationRepository.isLocationEnabledFlow)
                .thenReturn(flowOf(true))
        whenever(locationRepository.userVisibleLocationFlow)
                .thenReturn(flowOf(DeviceLocation(9.0, 8.0)))
        whenever(locationRepository.distanceBetween(
                DeviceLocation(9.0, 8.0),
                DeviceLocation(1.0, 2.0)))
                .thenReturn(-1f)

        val observer = retriever.createDistanceFlow(permissionsStateFlow, stopDetailsFlow)
                .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiItem.Distance.Unknown)
    }

    @Test
    fun createDistanceFlowWithZeroDistanceEmitsDistance() = runTest {
        val permissionsStateFlow = flowOf(grantedPermissionsState)
        val stopDetailsFlow = flowOf<StopDetails?>(stopDetails)
        givenHasLocationFeatureState(true)
        whenever(locationRepository.isLocationEnabledFlow)
                .thenReturn(flowOf(true))
        whenever(locationRepository.userVisibleLocationFlow)
                .thenReturn(flowOf(DeviceLocation(9.0, 8.0)))
        whenever(locationRepository.distanceBetween(
                DeviceLocation(9.0, 8.0),
                DeviceLocation(1.0, 2.0)))
                .thenReturn(0f)

        val observer = retriever.createDistanceFlow(permissionsStateFlow, stopDetailsFlow)
                .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                UiItem.Distance.Unknown,
                UiItem.Distance.Known(0f))
    }

    @Test
    fun createDistanceFlowWithPositiveDistanceEmitsDistance() = runTest {
        val permissionsStateFlow = flowOf(grantedPermissionsState)
        val stopDetailsFlow = flowOf<StopDetails?>(stopDetails)
        givenHasLocationFeatureState(true)
        whenever(locationRepository.isLocationEnabledFlow)
                .thenReturn(flowOf(true))
        whenever(locationRepository.userVisibleLocationFlow)
                .thenReturn(flowOf(DeviceLocation(9.0, 8.0)))
        whenever(locationRepository.distanceBetween(
                DeviceLocation(9.0, 8.0),
                DeviceLocation(1.0, 2.0)))
                .thenReturn(5200f)

        val observer = retriever.createDistanceFlow(permissionsStateFlow, stopDetailsFlow)
                .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                UiItem.Distance.Unknown,
                UiItem.Distance.Known(5.2f))
    }

    @Test
    fun createDistanceFlowWithRepresentativeExample() = runTest {
        val permissionsStateFlow = flow {
            emit(PermissionsState(PermissionState.DENIED, PermissionState.UNGRANTED))
            delay(100L)
            emit(grantedPermissionsState)
        }
        givenHasLocationFeatureState(true)
        whenever(locationRepository.isLocationEnabledFlow)
                .thenReturn(flow {
                    emit(false)
                    delay(200L)
                    emit(true)
                })
        val stopDetailsFlow = flow {
            emit(null)
            delay(300L)
            emit(stopDetails)
        }
        whenever(locationRepository.userVisibleLocationFlow)
                .thenReturn(flow {
                    delay(400L)
                    emit(DeviceLocation(9.0, 8.0))
                })
        whenever(locationRepository.distanceBetween(
                DeviceLocation(9.0, 8.0),
                DeviceLocation(1.0, 2.0)))
                .thenReturn(5200f)

        val observer = retriever.createDistanceFlow(permissionsStateFlow, stopDetailsFlow)
                .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                UiItem.Distance.PermissionDenied,
                UiItem.Distance.LocationOff,
                UiItem.Distance.Unknown,
                UiItem.Distance.Known(5.2f))
    }

    private fun givenHasLocationFeatureState(hasLocationFeature: Boolean) {
        whenever(locationRepository.hasLocationFeature)
                .thenReturn(hasLocationFeature)
    }

    private val grantedPermissionsState get() =
        PermissionsState(PermissionState.GRANTED, PermissionState.GRANTED)

    private val stopDetails get() = StopDetails(
            "123456",
            StopName(
                    "Name",
                    "Locality"),
            1.0,
            2.0,
            3)
}