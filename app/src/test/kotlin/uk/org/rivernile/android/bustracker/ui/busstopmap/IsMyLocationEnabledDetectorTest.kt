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
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [IsMyLocationEnabledDetector].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class IsMyLocationEnabledDetectorTest {

    @Mock
    private lateinit var locationRepository: LocationRepository

    private lateinit var  detector: IsMyLocationEnabledDetector

    @BeforeTest
    fun setUp() {
        detector = IsMyLocationEnabledDetector(locationRepository)
    }

    @Test
    fun getIsMyLocationFeatureEnabledFlowEmitsFalseWhenDoesNotHaveLocationFeature() = runTest  {
        whenever(locationRepository.hasLocationFeature)
            .thenReturn(false)
        val permissionsState = PermissionsState(PermissionState.GRANTED, PermissionState.GRANTED)

        detector.getIsMyLocationFeatureEnabledFlow(flowOf(permissionsState)).test {
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getIsMyLocationFeatureEnabledFlowEmitsFalseWhenLocationIsNotEnabled() = runTest  {
        givenHasLocationFeature()
        whenever(locationRepository.isLocationEnabledFlow)
            .thenReturn(flowOf(false))
        val permissionsState = PermissionsState(PermissionState.GRANTED, PermissionState.GRANTED)

        detector.getIsMyLocationFeatureEnabledFlow(flowOf(permissionsState)).test {
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getIsMyLocationFeatureEnabledFlowEmitsFalseWhenDoesNotHaveAnyLocationPermission() = runTest  {
        givenHasLocationFeature()
        whenever(locationRepository.isLocationEnabledFlow)
            .thenReturn(flowOf(true))
        val permissionsState = PermissionsState(
                PermissionState.UNGRANTED,
                PermissionState.UNGRANTED
        )

        detector.getIsMyLocationFeatureEnabledFlow(flowOf(permissionsState)).test {
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getIsMyLocationFeatureEnabledFlowEmitsTrueWhenHasCoarseLocationPermission() = runTest  {
        givenHasLocationFeature()
        whenever(locationRepository.isLocationEnabledFlow)
            .thenReturn(flowOf(true))
        val permissionsState = PermissionsState(PermissionState.UNGRANTED, PermissionState.GRANTED)

        detector.getIsMyLocationFeatureEnabledFlow(flowOf(permissionsState)).test {
            assertTrue(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getIsMyLocationFeatureEnabledFlowEmitsTrueWhenHasFineLocationPermission() = runTest  {
        givenHasLocationFeature()
        whenever(locationRepository.isLocationEnabledFlow)
            .thenReturn(flowOf(true))
        val permissionsState = PermissionsState(PermissionState.GRANTED, PermissionState.UNGRANTED)

        detector.getIsMyLocationFeatureEnabledFlow(flowOf(permissionsState)).test {
            assertTrue(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getIsMyLocationFeatureEnabledFlowEmitsTrueWhenHasAllLocationPermissions() = runTest  {
        givenHasLocationFeature()
        whenever(locationRepository.isLocationEnabledFlow)
            .thenReturn(flowOf(true))
        val permissionsState = PermissionsState(PermissionState.GRANTED, PermissionState.GRANTED)

        detector.getIsMyLocationFeatureEnabledFlow(flowOf(permissionsState)).test {
            assertTrue(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getIsMyLocationFeatureEnabledTransitionsStateWhenLocationEnabledChanges() = runTest {
        givenHasLocationFeature()
        whenever(locationRepository.isLocationEnabledFlow)
            .thenReturn(intervalFlowOf(0L, 10L, false, true, false))
        val permissionsState = PermissionsState(PermissionState.GRANTED, PermissionState.GRANTED)

        detector.getIsMyLocationFeatureEnabledFlow(flowOf(permissionsState)).test {
            assertFalse(awaitItem())
            assertTrue(awaitItem())
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getIsMyLocationFeatureEnabledTransitionsStateWhenPermissionStateChanges() = runTest {
        givenHasLocationFeature()
        whenever(locationRepository.isLocationEnabledFlow)
            .thenReturn(flowOf(true))
        val permissionsStateFlow = intervalFlowOf(
            0L,
            10L,
            PermissionsState(PermissionState.UNGRANTED, PermissionState.UNGRANTED),
            PermissionsState(PermissionState.GRANTED, PermissionState.GRANTED),
            PermissionsState(PermissionState.UNGRANTED, PermissionState.UNGRANTED)
        )

        detector.getIsMyLocationFeatureEnabledFlow(permissionsStateFlow).test {
            assertFalse(awaitItem())
            assertTrue(awaitItem())
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    private fun givenHasLocationFeature() {
        whenever(locationRepository.hasLocationFeature)
            .thenReturn(true)
    }
}