/*
 * Copyright (C) 2021 - 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.alerts.proximity

import app.cash.turbine.test
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.FakeStopName
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [UiStateCalculator].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class UiStateCalculatorTest {

    @Mock
    private lateinit var locationRepository: LocationRepository

    private lateinit var calculator: UiStateCalculator

    @BeforeTest
    fun setUp() {
        calculator = UiStateCalculator(locationRepository)
    }

    @Test
    fun createUiStateFlowEmitsNoLocationFeatureWhenNoLocationFeature() = runTest {
        whenever(locationRepository.hasLocationFeature)
            .thenReturn(false)
        val permissionStateFlow = flowOf(PermissionState.GRANTED)
        val backgroundLocationPermissionStateFlow = flowOf(PermissionState.GRANTED)
        val stopDetailsFlow = flowOf(StopDetails("123456", null))

        calculator
            .createUiStateFlow(
                permissionStateFlow,
                backgroundLocationPermissionStateFlow,
                stopDetailsFlow
            ).test {
                assertEquals(UiState.ERROR_NO_LOCATION_FEATURE, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun createUiStateFlowEmitsPermissionUngrantedWhenPermissionUngranted() = runTest {
        whenever(locationRepository.hasLocationFeature)
            .thenReturn(true)
        whenever(locationRepository.isLocationEnabledFlow)
            .thenReturn(flowOf(true))
        val permissionStateFlow = flowOf(PermissionState.UNGRANTED)
        val backgroundLocationPermissionStateFlow = flowOf(PermissionState.UNGRANTED)
        val stopDetailsFlow = flowOf(StopDetails("123456", null))

        calculator
            .createUiStateFlow(
                permissionStateFlow,
                backgroundLocationPermissionStateFlow,
                stopDetailsFlow
            ).test {
                assertEquals(UiState.ERROR_PERMISSION_UNGRANTED, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun createUiStateFlowEmitsPermissionDeniedWhenPermissionDenied() = runTest {
        whenever(locationRepository.hasLocationFeature)
            .thenReturn(true)
        whenever(locationRepository.isLocationEnabledFlow)
            .thenReturn(flowOf(true))
        val permissionStateFlow = flowOf(PermissionState.DENIED)
        val backgroundLocationPermissionStateFlow = flowOf(PermissionState.UNGRANTED)
        val stopDetailsFlow = flowOf(StopDetails("123456", null))

        calculator
            .createUiStateFlow(
                permissionStateFlow,
                backgroundLocationPermissionStateFlow,
                stopDetailsFlow
            ).test {
                assertEquals(UiState.ERROR_PERMISSION_DENIED, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun createUiStateFlowEmitsPermissionDeniedWhenBgLocationPermissionDenied() = runTest {
        whenever(locationRepository.hasLocationFeature)
            .thenReturn(true)
        whenever(locationRepository.isLocationEnabledFlow)
            .thenReturn(flowOf(true))
        val permissionStateFlow = flowOf(PermissionState.GRANTED)
        val backgroundLocationPermissionStateFlow = flowOf(PermissionState.DENIED)
        val stopDetailsFlow = flowOf(StopDetails("123456", null))

        calculator
            .createUiStateFlow(
                permissionStateFlow,
                backgroundLocationPermissionStateFlow,
                stopDetailsFlow
            ).test {
                assertEquals(UiState.ERROR_PERMISSION_DENIED, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun createUiStateFlowEmitsPermissionUngrantedWhenBgLocationPermissionUngranted() = runTest {
        whenever(locationRepository.hasLocationFeature)
            .thenReturn(true)
        whenever(locationRepository.isLocationEnabledFlow)
            .thenReturn(flowOf(true))
        val permissionStateFlow = flowOf(PermissionState.GRANTED)
        val backgroundLocationPermissionStateFlow = flowOf(PermissionState.UNGRANTED)
        val stopDetailsFlow = flowOf(StopDetails("123456", null))

        calculator
            .createUiStateFlow(
                permissionStateFlow,
                backgroundLocationPermissionStateFlow,
                stopDetailsFlow
            ).test {
                assertEquals(UiState.ERROR_NO_BACKGROUND_LOCATION_PERMISSION, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun createUiStateFlowEmitsProgressWhenStopDetailsIsNull() = runTest {
        whenever(locationRepository.hasLocationFeature)
            .thenReturn(true)
        whenever(locationRepository.isLocationEnabledFlow)
            .thenReturn(flowOf(true))
        val permissionStateFlow = flowOf(PermissionState.GRANTED)
        val backgroundLocationPermissionStateFlow = flowOf(PermissionState.GRANTED)
        val stopDetailsFlow = flowOf(null)

        calculator
            .createUiStateFlow(
                permissionStateFlow,
                backgroundLocationPermissionStateFlow,
                stopDetailsFlow
            ).test {
                assertEquals(UiState.PROGRESS, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun createUiStateFlowEmitsContentWhenStopDetailsIsAvailable() = runTest {
        whenever(locationRepository.hasLocationFeature)
            .thenReturn(true)
        whenever(locationRepository.isLocationEnabledFlow)
            .thenReturn(flowOf(true))
        val permissionStateFlow = flowOf(PermissionState.GRANTED)
        val backgroundLocationPermissionStateFlow = flowOf(PermissionState.GRANTED)
        val stopDetailsFlow = flowOf(StopDetails("123456", null))

        calculator
            .createUiStateFlow(
                permissionStateFlow,
                backgroundLocationPermissionStateFlow,
                stopDetailsFlow
            ).test {
                assertEquals(UiState.CONTENT, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun createUiStateFlowEmitsCorrectValuesWithRepresentativeExample() = runTest {
        whenever(locationRepository.hasLocationFeature)
            .thenReturn(true)
        whenever(locationRepository.isLocationEnabledFlow)
            .thenReturn(intervalFlowOf(0L, 600L, false, true))
        val permissionStateFlow = intervalFlowOf(
            0L,
            100L,
            PermissionState.UNGRANTED,
            PermissionState.DENIED,
            PermissionState.GRANTED
        )
        val backgroundLocationPermissionStateFlow = flow {
            emit(PermissionState.UNGRANTED)
            delay(300L)
            emit(PermissionState.DENIED)
            delay(100L)
            emit(PermissionState.GRANTED)
        }
        val stopDetailsFlow = flow {
            emit(null)
            delay(700L)
            emit(StopDetails("123456", null))
            delay(100L)
            emit(StopDetails("123456", FakeStopName("Name", "Locality")))
        }

        calculator
            .createUiStateFlow(
                permissionStateFlow,
                backgroundLocationPermissionStateFlow,
                stopDetailsFlow
            ).test {
                assertEquals(UiState.ERROR_PERMISSION_UNGRANTED, awaitItem())
                assertEquals(UiState.ERROR_PERMISSION_DENIED, awaitItem())
                assertEquals(UiState.ERROR_NO_BACKGROUND_LOCATION_PERMISSION, awaitItem())
                assertEquals(UiState.ERROR_PERMISSION_DENIED, awaitItem())
                assertEquals(UiState.ERROR_LOCATION_DISABLED, awaitItem())
                assertEquals(UiState.PROGRESS, awaitItem())
                assertEquals(UiState.CONTENT, awaitItem())
                awaitComplete()
            }
    }
}