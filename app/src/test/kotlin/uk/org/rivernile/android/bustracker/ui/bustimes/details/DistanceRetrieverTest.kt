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
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopDetails
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopLocation
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopName
import uk.org.rivernile.android.bustracker.core.busstops.StopDetails
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.location.DeviceLocation
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [DistanceRetriever].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class DistanceRetrieverTest {

    @Mock
    private lateinit var locationRepository: LocationRepository

    private lateinit var retriever: DistanceRetriever

    @BeforeTest
    fun setUp() {
        retriever = DistanceRetriever(locationRepository)
    }

    @Test
    fun createDistanceFlowWithoutLocationFeatureEmitsNoLocationFeature() = runTest {
        val permissionsStateFlow = flowOf(grantedPermissionsState)
        val stopDetailsFlow = flowOf(stopDetails)
        givenHasLocationFeatureState(false)

        retriever.createDistanceFlow(permissionsStateFlow, stopDetailsFlow).test {
            assertEquals(UiItem.Distance.NoLocationFeature, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun createDistanceFlowWithoutLocationPermissionsEmitsPermissionDenied() = runTest {
        val permissionsStateFlow = flowOf(
            PermissionsState(PermissionState.DENIED, PermissionState.UNGRANTED)
        )
        val stopDetailsFlow = flowOf(stopDetails)
        givenHasLocationFeatureState(true)

        retriever.createDistanceFlow(permissionsStateFlow, stopDetailsFlow).test {
            assertEquals(UiItem.Distance.PermissionDenied, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun createDistanceFlowWhenLocationIsOffEmitsLocationOff() = runTest {
        val permissionsStateFlow = flowOf(grantedPermissionsState)
        val stopDetailsFlow = flowOf(stopDetails)
        givenHasLocationFeatureState(true)
        whenever(locationRepository.isLocationEnabledFlow)
            .thenReturn(flowOf(false))

        retriever.createDistanceFlow(permissionsStateFlow, stopDetailsFlow).test {
            assertEquals(UiItem.Distance.LocationOff, awaitItem())
            awaitComplete()
        }
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

        retriever.createDistanceFlow(permissionsStateFlow, stopDetailsFlow).test {
            assertEquals(UiItem.Distance.Unknown, awaitItem())
            awaitComplete()
        }
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

        retriever.createDistanceFlow(permissionsStateFlow, stopDetailsFlow).test {
            assertEquals(UiItem.Distance.Unknown, awaitItem())
            awaitComplete()
        }
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
        whenever(locationRepository
            .distanceBetween(
                DeviceLocation(9.0, 8.0),
                DeviceLocation(1.0, 2.0)
            )
        ).thenReturn(-1f)

        retriever.createDistanceFlow(permissionsStateFlow, stopDetailsFlow).test {
            assertEquals(UiItem.Distance.Unknown, awaitItem())
            awaitComplete()
        }
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
        whenever(locationRepository
            .distanceBetween(
                DeviceLocation(9.0, 8.0),
                DeviceLocation(1.0, 2.0)
            )
        ).thenReturn(0f)

        retriever.createDistanceFlow(permissionsStateFlow, stopDetailsFlow).test {
            assertEquals(UiItem.Distance.Unknown, awaitItem())
            assertEquals(UiItem.Distance.Known(0f), awaitItem())
            awaitComplete()
        }
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
        whenever(locationRepository
            .distanceBetween(
                DeviceLocation(9.0, 8.0),
                DeviceLocation(1.0, 2.0)
            )
        ).thenReturn(5200f)

        retriever.createDistanceFlow(permissionsStateFlow, stopDetailsFlow).test {
            assertEquals(UiItem.Distance.Unknown, awaitItem())
            assertEquals(UiItem.Distance.Known(5.2f), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun createDistanceFlowWithRepresentativeExample() = runTest {
        val permissionsStateFlow = intervalFlowOf(
            0L,
            100L,
            PermissionsState(PermissionState.DENIED, PermissionState.UNGRANTED),
            grantedPermissionsState
        )
        givenHasLocationFeatureState(true)
        whenever(locationRepository.isLocationEnabledFlow)
            .thenReturn(intervalFlowOf(0L, 200L, false, true))
        val stopDetailsFlow = intervalFlowOf(0L, 300L, null, stopDetails)
        whenever(locationRepository.userVisibleLocationFlow)
            .thenReturn(
                intervalFlowOf(0L, 400L, DeviceLocation(9.0, 8.0))
            )
        whenever(locationRepository
            .distanceBetween(
                DeviceLocation(9.0, 8.0),
                DeviceLocation(1.0, 2.0)
            )
        ).thenReturn(5200f)

        retriever.createDistanceFlow(permissionsStateFlow, stopDetailsFlow).test {
            assertEquals(UiItem.Distance.PermissionDenied, awaitItem())
            assertEquals(UiItem.Distance.LocationOff, awaitItem())
            assertEquals(UiItem.Distance.Unknown, awaitItem())
            assertEquals(UiItem.Distance.Known(5.2f), awaitItem())
            awaitComplete()
        }
    }

    private fun givenHasLocationFeatureState(hasLocationFeature: Boolean) {
        whenever(locationRepository.hasLocationFeature)
            .thenReturn(hasLocationFeature)
    }

    private val grantedPermissionsState get() =
        PermissionsState(PermissionState.GRANTED, PermissionState.GRANTED)

    private val stopDetails get() = FakeStopDetails(
        "123456".toNaptanStopIdentifier(),
        FakeStopName(
            "Name",
            "Locality"
        ),
        FakeStopLocation(
            1.0,
            2.0
        ),
        StopOrientation.EAST
    )
}
