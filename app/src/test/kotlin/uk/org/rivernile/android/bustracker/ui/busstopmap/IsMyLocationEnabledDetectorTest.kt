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
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [IsMyLocationEnabledDetector].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class IsMyLocationEnabledDetectorTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var locationRepository: LocationRepository

    private lateinit var  detector: IsMyLocationEnabledDetector

    @Before
    fun setUp() {
        detector = IsMyLocationEnabledDetector(locationRepository)
    }

    @Test
    fun getIsMyLocationFeatureEnabledFlowEmitsFalseWhenDoesNotHaveLocationFeature()  {
        runTest(UnconfinedTestDispatcher()) {
            whenever(locationRepository.hasLocationFeature)
                    .thenReturn(false)
            val permissionsState = PermissionsState(
                    PermissionState.GRANTED,
                    PermissionState.GRANTED)

            val observer = detector.getIsMyLocationFeatureEnabledFlow(flowOf(permissionsState))
                    .test(this)
            observer.finish()

            observer.assertValues(false)
        }
    }

    @Test
    fun getIsMyLocationFeatureEnabledFlowEmitsFalseWhenLocationIsNotEnabled()  {
        runTest(UnconfinedTestDispatcher()) {
            givenHasLocationFeature()
            whenever(locationRepository.isLocationEnabledFlow)
                    .thenReturn(flowOf(false))
            val permissionsState = PermissionsState(
                    PermissionState.GRANTED,
                    PermissionState.GRANTED)

            val observer = detector.getIsMyLocationFeatureEnabledFlow(flowOf(permissionsState))
                    .test(this)
            observer.finish()

            observer.assertValues(false)
        }
    }

    @Test
    fun getIsMyLocationFeatureEnabledFlowEmitsFalseWhenDoesNotHaveAnyLocationPermission()  {
        runTest(UnconfinedTestDispatcher()) {
            givenHasLocationFeature()
            whenever(locationRepository.isLocationEnabledFlow)
                    .thenReturn(flowOf(true))
            val permissionsState = PermissionsState(
                    PermissionState.UNGRANTED,
                    PermissionState.UNGRANTED)

            val observer = detector.getIsMyLocationFeatureEnabledFlow(flowOf(permissionsState))
                    .test(this)
            observer.finish()

            observer.assertValues(false)
        }
    }

    @Test
    fun getIsMyLocationFeatureEnabledFlowEmitsTrueWhenHasCoarseLocationPermission()  {
        runTest(UnconfinedTestDispatcher()) {
            givenHasLocationFeature()
            whenever(locationRepository.isLocationEnabledFlow)
                    .thenReturn(flowOf(true))
            val permissionsState = PermissionsState(
                    PermissionState.UNGRANTED,
                    PermissionState.GRANTED)

            val observer = detector.getIsMyLocationFeatureEnabledFlow(flowOf(permissionsState))
                    .test(this)
            observer.finish()

            observer.assertValues(true)
        }
    }

    @Test
    fun getIsMyLocationFeatureEnabledFlowEmitsTrueWhenHasFineLocationPermission()  {
        runTest(UnconfinedTestDispatcher()) {
            givenHasLocationFeature()
            whenever(locationRepository.isLocationEnabledFlow)
                    .thenReturn(flowOf(true))
            val permissionsState = PermissionsState(
                    PermissionState.GRANTED,
                    PermissionState.UNGRANTED)

            val observer = detector.getIsMyLocationFeatureEnabledFlow(flowOf(permissionsState))
                    .test(this)
            observer.finish()

            observer.assertValues(true)
        }
    }

    @Test
    fun getIsMyLocationFeatureEnabledFlowEmitsTrueWhenHasAllLocationPermissions()  {
        runTest(UnconfinedTestDispatcher()) {
            givenHasLocationFeature()
            whenever(locationRepository.isLocationEnabledFlow)
                    .thenReturn(flowOf(true))
            val permissionsState = PermissionsState(
                    PermissionState.GRANTED,
                    PermissionState.GRANTED)

            val observer = detector.getIsMyLocationFeatureEnabledFlow(flowOf(permissionsState))
                    .test(this)
            observer.finish()

            observer.assertValues(true)
        }
    }

    @Test
    fun getIsMyLocationFeatureEnabledTransitionsStateWhenLocationEnabledChanges() = runTest {
        givenHasLocationFeature()
        whenever(locationRepository.isLocationEnabledFlow)
                .thenReturn(intervalFlowOf(0L, 10L, false, true, false))
        val permissionsState = PermissionsState(
                PermissionState.GRANTED,
                PermissionState.GRANTED)

        val observer = detector.getIsMyLocationFeatureEnabledFlow(flowOf(permissionsState))
                .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(false, true, false)
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
                PermissionsState(PermissionState.UNGRANTED, PermissionState.UNGRANTED))

        val observer = detector.getIsMyLocationFeatureEnabledFlow(permissionsStateFlow).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(false, true, false)
    }

    private fun givenHasLocationFeature() {
        whenever(locationRepository.hasLocationFeature)
                .thenReturn(true)
    }
}