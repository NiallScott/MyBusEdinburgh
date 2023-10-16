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

package uk.org.rivernile.android.bustracker.core.location

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [LocationRepository].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class LocationRepositoryTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var hasLocationFeatureDetector: HasLocationFeatureDetector
    @Mock
    private lateinit var isLocationEnabledDetector: IsLocationEnabledDetector
    @Mock
    private lateinit var locationSource: LocationSource
    @Mock
    private lateinit var distanceCalculator: DistanceCalculator

    private lateinit var locationRepository: LocationRepository

    @Before
    fun setUp() {
        locationRepository = LocationRepository(
                hasLocationFeatureDetector,
                isLocationEnabledDetector,
                locationSource,
                distanceCalculator)
    }

    @Test
    fun hasLocationFeatureIsFalseWhenHasLocationFeatureDetectorReturnsFalse() {
        whenever(hasLocationFeatureDetector.hasLocationFeature)
                .thenReturn(false)

        val result = locationRepository.hasLocationFeature

        assertFalse(result)
    }

    @Test
    fun hasLocationFeatureIsTrueWhenHasLocationFeatureDetectorReturnsTrue() {
        whenever(hasLocationFeatureDetector.hasLocationFeature)
                .thenReturn(true)

        val result = locationRepository.hasLocationFeature

        assertTrue(result)
    }

    @Test
    fun hasLocationFeatureCallsHasLocationFeatureDetectorOnlyOnce() {
        whenever(hasLocationFeatureDetector.hasLocationFeature)
                .thenReturn(true)

        locationRepository.hasLocationFeature
        locationRepository.hasLocationFeature
        val result = locationRepository.hasLocationFeature

        assertTrue(result)
        verify(hasLocationFeatureDetector, times(1))
                .hasLocationFeature
    }

    @Test
    fun hasGpsLocationProviderIsFalseWhenLocationFeatureDetectorReturnsFalse() {
        whenever(hasLocationFeatureDetector.hasGpsLocationProvider)
                .thenReturn(false)

        val result = locationRepository.hasGpsLocationProvider

        assertFalse(result)
    }

    @Test
    fun hasGpsLocationProviderIsTrueWhenLocationFeatureDetectorReturnsTrue() {
        whenever(hasLocationFeatureDetector.hasGpsLocationProvider)
                .thenReturn(true)

        val result = locationRepository.hasGpsLocationProvider

        assertTrue(result)
    }

    @Test
    fun hasGpsLocationProviderCallsHasLocationFeatureDetectorOnlyOnce() {
        whenever(hasLocationFeatureDetector.hasGpsLocationProvider)
                .thenReturn(true)

        locationRepository.hasGpsLocationProvider
        locationRepository.hasGpsLocationProvider
        val result = locationRepository.hasGpsLocationProvider

        assertTrue(result)
        verify(hasLocationFeatureDetector, times(1))
                .hasGpsLocationProvider
    }

    @Test
    fun getIsLocationEnabledFlowReturnsFlowFromIsLocationEnabledDetector() = runTest {
        whenever(isLocationEnabledDetector.isLocationEnabledFlow)
                .thenReturn(flowOf(false, true, false))

        val observer = locationRepository.isLocationEnabledFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(false, true, false)
    }

    @Test
    fun isGpsLocationProviderEnabledReturnsFalseWhenIsLocationEnabledDetectorReturnsFalse() {
        whenever(isLocationEnabledDetector.isGpsLocationProviderEnabled)
                .thenReturn(false)

        val result = locationRepository.isGpsLocationProviderEnabled

        assertFalse(result)
    }

    @Test
    fun isGpsLocationProviderEnabledReturnsTrueWhenIsLocationEnabledDetectorReturnsTrue() {
        whenever(isLocationEnabledDetector.isGpsLocationProviderEnabled)
                .thenReturn(true)

        val result = locationRepository.isGpsLocationProviderEnabled

        assertTrue(result)
    }

    @Test
    fun userVisibleLocationFlowDoesNotEmitWhenDoesNotHaveLocationFeature() = runTest {
        whenever(hasLocationFeatureDetector.hasLocationFeature)
                .thenReturn(false)

        val observer = locationRepository.userVisibleLocationFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertNoValues()
    }

    @Test
    fun userVisibleLocationFlowDoesNotEmitWhenLocationIsNotEnabled() = runTest {
        whenever(hasLocationFeatureDetector.hasLocationFeature)
                .thenReturn(true)
        whenever(isLocationEnabledDetector.isLocationEnabledFlow)
                .thenReturn(flowOf(false))

        val observer = locationRepository.userVisibleLocationFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertNoValues()
    }

    @Test
    fun userVisibleLocationFlowEmitsFromUpstreamWhenLocationIsEnabled() = runTest {
        whenever(hasLocationFeatureDetector.hasLocationFeature)
                .thenReturn(true)
        whenever(isLocationEnabledDetector.isLocationEnabledFlow)
                .thenReturn(flowOf(true))
        whenever(locationSource.userVisibleLocationFlow)
                .thenReturn(flowOf(DeviceLocation(1.0, 2.0)))

        val observer = locationRepository.userVisibleLocationFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(DeviceLocation(1.0, 2.0))
    }

    @Test
    fun userVisibleLocationFlowDoesNotEmitAfterLocationIsDisabled() = runTest {
        whenever(hasLocationFeatureDetector.hasLocationFeature)
                .thenReturn(true)
        whenever(isLocationEnabledDetector.isLocationEnabledFlow)
                .thenReturn(flowOf(false, false, true, true, false))
        whenever(locationSource.userVisibleLocationFlow)
                .thenReturn(
                        flowOf(
                                DeviceLocation(1.0, 2.0),
                                DeviceLocation(3.0, 4.0),
                                DeviceLocation(5.0, 6.0)))

        val observer = locationRepository.userVisibleLocationFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                DeviceLocation(1.0, 2.0),
                DeviceLocation(3.0, 4.0),
                DeviceLocation(5.0, 6.0))
    }

    @Test
    fun distanceBetweenDelegatesToDistanceCalculator() {
        val first = DeviceLocation(1.0, 2.0)
        val second = DeviceLocation(3.0, 4.0)
        whenever(distanceCalculator.distanceBetween(first, second))
                .thenReturn(10f)

        val result = locationRepository.distanceBetween(first, second)

        assertEquals(10f, result)
    }
}