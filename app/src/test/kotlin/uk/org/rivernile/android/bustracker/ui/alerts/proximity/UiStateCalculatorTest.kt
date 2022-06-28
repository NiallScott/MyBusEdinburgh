/*
 * Copyright (C) 2021 - 2022 Niall 'Rivernile' Scott
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

import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
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
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopName
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [UiStateCalculator].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class UiStateCalculatorTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var locationRepository: LocationRepository

    private lateinit var calculator: UiStateCalculator

    @Before
    fun setUp() {
        calculator = UiStateCalculator(locationRepository)
    }

    @Test
    fun createUiStateFlowEmitsNoLocationFeatureWhenNoLocationFeature() = runTest {
        whenever(locationRepository.hasLocationFeature)
                .thenReturn(false)
        val locationPermissionFlow = flowOf(PermissionState.GRANTED)
        val stopDetailsFlow = flowOf(StopDetails("123456", null))

        val observer = calculator.createUiStateFlow(locationPermissionFlow, stopDetailsFlow)
                .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiState.ERROR_NO_LOCATION_FEATURE)
    }

    @Test
    fun createUiStateFlowEmitsPermissionUngrantedWhenPermissionUngranted() = runTest {
        whenever(locationRepository.hasLocationFeature)
                .thenReturn(true)
        whenever(locationRepository.isLocationEnabledFlow)
                .thenReturn(flowOf(true))
        val locationPermissionFlow = flowOf(PermissionState.UNGRANTED)
        val stopDetailsFlow = flowOf(StopDetails("123456", null))

        val observer = calculator.createUiStateFlow(locationPermissionFlow, stopDetailsFlow)
                .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiState.ERROR_PERMISSION_UNGRANTED)
    }

    @Test
    fun createUiStateFlowEmitsPermissionDeniedWhenPermissionDenied() = runTest {
        whenever(locationRepository.hasLocationFeature)
                .thenReturn(true)
        whenever(locationRepository.isLocationEnabledFlow)
                .thenReturn(flowOf(true))
        val locationPermissionFlow = flowOf(PermissionState.DENIED)
        val stopDetailsFlow = flowOf(StopDetails("123456", null))

        val observer = calculator.createUiStateFlow(locationPermissionFlow, stopDetailsFlow)
                .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiState.ERROR_PERMISSION_DENIED)
    }

    @Test
    fun createUiStateFlowEmitsProgressWhenStopDetailsIsNull() = runTest {
        whenever(locationRepository.hasLocationFeature)
                .thenReturn(true)
        whenever(locationRepository.isLocationEnabledFlow)
                .thenReturn(flowOf(true))
        val locationPermissionFlow = flowOf(PermissionState.GRANTED)
        val stopDetailsFlow = flowOf(null)

        val observer = calculator.createUiStateFlow(locationPermissionFlow, stopDetailsFlow)
                .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiState.PROGRESS)
    }

    @Test
    fun createUiStateFlowEmitsContentWhenStopDetailsIsAvailable() = runTest {
        whenever(locationRepository.hasLocationFeature)
                .thenReturn(true)
        whenever(locationRepository.isLocationEnabledFlow)
                .thenReturn(flowOf(true))
        val locationPermissionFlow = flowOf(PermissionState.GRANTED)
        val stopDetailsFlow = flowOf(StopDetails("123456", null))

        val observer = calculator.createUiStateFlow(locationPermissionFlow, stopDetailsFlow)
                .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiState.CONTENT)
    }

    @Test
    fun createUiStateFlowEmitsCorrectValuesWithRepresentativeExample() = runTest {
        whenever(locationRepository.hasLocationFeature)
                .thenReturn(true)
        whenever(locationRepository.isLocationEnabledFlow)
                .thenReturn(flow {
                    emit(false)
                    delay(300L)
                    emit(true)
                })
        val locationPermissionFlow = flow {
            emit(PermissionState.UNGRANTED)
            delay(100L)
            emit(PermissionState.DENIED)
            delay(100L)
            emit(PermissionState.GRANTED)
        }
        val stopDetailsFlow = flow {
            emit(null)
            delay(400L)
            emit(StopDetails("123456", null))
            delay(100L)
            emit(StopDetails("123456", StopName("Name", "Locality")))
        }

        val observer = calculator.createUiStateFlow(locationPermissionFlow, stopDetailsFlow)
                .test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                UiState.ERROR_PERMISSION_UNGRANTED,
                UiState.ERROR_PERMISSION_DENIED,
                UiState.ERROR_LOCATION_DISABLED,
                UiState.PROGRESS,
                UiState.CONTENT)
    }
}