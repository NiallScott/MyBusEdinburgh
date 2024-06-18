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

package uk.org.rivernile.android.bustracker.core.location

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [LocationRepository].
 *
 * @author Niall Scott
 */
class LocationRepositoryTest {

    @Test
    fun hasLocationFeatureIsFalseWhenHasLocationFeatureDetectorReturnsFalse() {
        val repository = createLocationRepository(
            hasLocationFeatureDetector = FakeHasLocationFeatureDetector(
                onHasLocationFeature = { false }
            )
        )

        val result = repository.hasLocationFeature

        assertFalse(result)
    }

    @Test
    fun hasLocationFeatureIsTrueWhenHasLocationFeatureDetectorReturnsTrue() {
        val repository = createLocationRepository(
            hasLocationFeatureDetector = FakeHasLocationFeatureDetector(
                onHasLocationFeature = { true }
            )
        )

        val result = repository.hasLocationFeature

        assertTrue(result)
    }

    @Test
    fun hasLocationFeatureCallsHasLocationFeatureDetectorOnlyOnce() {
        var callCount = 0
        val repository = createLocationRepository(
            hasLocationFeatureDetector = FakeHasLocationFeatureDetector(
                onHasLocationFeature = {
                    callCount ++
                    true
                }
            )
        )

        repository.hasLocationFeature
        repository.hasLocationFeature
        val result = repository.hasLocationFeature

        assertTrue(result)
        assertEquals(1, callCount)
    }

    @Test
    fun hasGpsLocationProviderIsFalseWhenLocationFeatureDetectorReturnsFalse() {
        val repository = createLocationRepository(
            hasLocationFeatureDetector = FakeHasLocationFeatureDetector(
                onHasGpsLocationProvider = { false }
            )
        )

        val result = repository.hasGpsLocationProvider

        assertFalse(result)
    }

    @Test
    fun hasGpsLocationProviderIsTrueWhenLocationFeatureDetectorReturnsTrue() {
        val repository = createLocationRepository(
            hasLocationFeatureDetector = FakeHasLocationFeatureDetector(
                onHasGpsLocationProvider = { true }
            )
        )

        val result = repository.hasGpsLocationProvider

        assertTrue(result)
    }

    @Test
    fun hasGpsLocationProviderCallsHasLocationFeatureDetectorOnlyOnce() {
        var callCount = 0
        val repository = createLocationRepository(
            hasLocationFeatureDetector = FakeHasLocationFeatureDetector(
                onHasGpsLocationProvider = {
                    callCount ++
                    true
                }
            )
        )

        repository.hasGpsLocationProvider
        repository.hasGpsLocationProvider
        val result = repository.hasGpsLocationProvider

        assertTrue(result)
        assertEquals(1, callCount)
    }

    @Test
    fun getIsLocationEnabledFlowReturnsFlowFromIsLocationEnabledDetector() = runTest {
        val repository = createLocationRepository(
            isLocationEnabledDetector = FakeIsLocationEnabledDetector(
                onIsLocationEnabledFlow = { flowOf(false, true, false) }
            )
        )

        repository.isLocationEnabledFlow.test {
            assertFalse(awaitItem())
            assertTrue(awaitItem())
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun isGpsLocationProviderEnabledReturnsFalseWhenIsLocationEnabledDetectorReturnsFalse() {
        val repository = createLocationRepository(
            isLocationEnabledDetector = FakeIsLocationEnabledDetector(
                onIsGpsLocationProviderEnabled = { false }
            )
        )

        val result = repository.isGpsLocationProviderEnabled

        assertFalse(result)
    }

    @Test
    fun isGpsLocationProviderEnabledReturnsTrueWhenIsLocationEnabledDetectorReturnsTrue() {
        val repository = createLocationRepository(
            isLocationEnabledDetector = FakeIsLocationEnabledDetector(
                onIsGpsLocationProviderEnabled = { true }
            )
        )

        val result = repository.isGpsLocationProviderEnabled

        assertTrue(result)
    }

    @Test
    fun userVisibleLocationFlowDoesNotEmitWhenDoesNotHaveLocationFeature() = runTest {
        val repository = createLocationRepository(
            hasLocationFeatureDetector = FakeHasLocationFeatureDetector(
                onHasLocationFeature = { false }
            )
        )

        repository.userVisibleLocationFlow.test {
            awaitComplete()
        }
    }

    @Test
    fun userVisibleLocationFlowDoesNotEmitWhenLocationIsNotEnabled() = runTest {
        val repository = createLocationRepository(
            hasLocationFeatureDetector = FakeHasLocationFeatureDetector(
                onHasLocationFeature = { true }
            ),
            isLocationEnabledDetector = FakeIsLocationEnabledDetector(
                onIsLocationEnabledFlow = { flowOf(false) }
            )
        )

        repository.userVisibleLocationFlow.test {
            awaitComplete()
        }
    }

    @Test
    fun userVisibleLocationFlowEmitsFromUpstreamWhenLocationIsEnabled() = runTest {
        val repository = createLocationRepository(
            hasLocationFeatureDetector = FakeHasLocationFeatureDetector(
                onHasLocationFeature = { true }
            ),
            isLocationEnabledDetector = FakeIsLocationEnabledDetector(
                onIsLocationEnabledFlow = { flowOf(true) }
            ),
            locationSource = FakeLocationSource(
                onUserVisibleLocationFlow = { flowOf(DeviceLocation(1.0, 2.0)) }
            )
        )

        repository.userVisibleLocationFlow.test {
            assertEquals(DeviceLocation(1.0, 2.0), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun userVisibleLocationFlowDoesNotEmitAfterLocationIsDisabled() = runTest {
        val repository = createLocationRepository(
            hasLocationFeatureDetector = FakeHasLocationFeatureDetector(
                onHasLocationFeature = { true }
            ),
            isLocationEnabledDetector = FakeIsLocationEnabledDetector(
                onIsLocationEnabledFlow = { flowOf(false, false, true, true, false) }
            ),
            locationSource = FakeLocationSource(
                onUserVisibleLocationFlow = {
                    flowOf(
                        DeviceLocation(1.0, 2.0),
                        DeviceLocation(3.0, 4.0),
                        DeviceLocation(5.0, 6.0)
                    )
                }
            )
        )

        repository.userVisibleLocationFlow.test {
            assertEquals(DeviceLocation(1.0, 2.0), awaitItem())
            assertEquals(DeviceLocation(3.0, 4.0), awaitItem())
            assertEquals(DeviceLocation(5.0, 6.0), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun distanceBetweenDelegatesToDistanceCalculator() {
        val first = DeviceLocation(1.0, 2.0)
        val second = DeviceLocation(3.0, 4.0)
        val repository = createLocationRepository(
            distanceCalculator = FakeDistanceCalculator(
                onDistanceBetween = { f, s ->
                    assertEquals(first, f)
                    assertEquals(second, s)
                    10f
                }
            )
        )

        val result = repository.distanceBetween(first, second)

        assertEquals(10f, result)
    }

    private fun createLocationRepository(
        hasLocationFeatureDetector: HasLocationFeatureDetector = FakeHasLocationFeatureDetector(),
        isLocationEnabledDetector: IsLocationEnabledDetector = FakeIsLocationEnabledDetector(),
        locationSource: LocationSource = FakeLocationSource(),
        distanceCalculator: DistanceCalculator = FakeDistanceCalculator()
    ): LocationRepository {
        return LocationRepository(
            hasLocationFeatureDetector,
            isLocationEnabledDetector,
            locationSource,
            distanceCalculator
        )
    }
}