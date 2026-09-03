/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
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
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [AndroidLocationRepository].
 *
 * @author Niall Scott
 */
class AndroidLocationRepositoryTest {

    @Test
    fun hasLocationFeatureReturnsValuesFromLocationSupport() = runTest {
        val repository = createRepository(
            androidLocationSupport = FakeAndroidLocationSupport(
                onHasLocationFeature = { true },
                onIsLocationEnabledFlow = ::emptyFlow,
                onIsGpsLocationProviderEnabledFlow = ::emptyFlow
            )
        )

        val result = repository.hasLocationFeature

        assertTrue(result)
    }

    @Test
    fun hasGpsLocationProviderReturnsValuesFromLocationSupport() = runTest {
        val repository = createRepository(
            androidLocationSupport = FakeAndroidLocationSupport(
                onHasGpsLocationProvider = { true },
                onIsLocationEnabledFlow = ::emptyFlow,
                onIsGpsLocationProviderEnabledFlow = ::emptyFlow
            )
        )

        val result = repository.hasGpsLocationProvider

        assertTrue(result)
    }

    @Test
    fun isLocationEnabledFlowEmitsValuesFromLocationSupport() = runTest {
        val repository = createRepository(
            androidLocationSupport = FakeAndroidLocationSupport(
                onIsLocationEnabledFlow = { flowOf(true) },
                onIsGpsLocationProviderEnabledFlow = ::emptyFlow
            )
        )

        repository.isLocationEnabledFlow.test {
            assertTrue(awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun isGpsLocationProviderEnabledFlowEmitsValuesFromLocationSupport() = runTest {
        val repository = createRepository(
            androidLocationSupport = FakeAndroidLocationSupport(
                onIsLocationEnabledFlow = ::emptyFlow,
                onIsGpsLocationProviderEnabledFlow = { flowOf(true) }
            )
        )

        repository.isGpsLocationProviderEnabledFlow.test {
            assertTrue(awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun locationUpdatesFlowEmitsNoLocationFeatureWhenNoLocationFeature() = runTest {
        val repository = createRepository(
            androidLocationSupport = FakeAndroidLocationSupport(
                onHasLocationFeature = { false },
                onIsLocationEnabledFlow = ::emptyFlow,
                onIsGpsLocationProviderEnabledFlow = ::emptyFlow
            )
        )

        repository.locationUpdatesFlow.test {
            assertEquals(LocationUpdate.Error.NoLocationFeature, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun locationUpdatesFlowEmitsInsufficientLocationPermissionsWhenPermsNotSufficient() = runTest {
        val repository = createRepository(
            androidLocationSupport = FakeAndroidLocationSupport(
                onHasLocationFeature = { true },
                onIsLocationEnabledFlow = ::emptyFlow,
                onIsGpsLocationProviderEnabledFlow = ::emptyFlow
            ),
            androidLocationPermissionChecker = FakeAndroidLocationPermissionChecker(
                onCheckHasEitherFineOrCoarseLocationPermission = { false }
            )
        )

        repository.locationUpdatesFlow.test {
            assertEquals(LocationUpdate.Error.InsufficientLocationPermissions, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun locationUpdatesFlowEmitsLocationOffWhenLocationIsNotEnabled() = runTest {
        val repository = createRepository(
            androidLocationSupport = FakeAndroidLocationSupport(
                onHasLocationFeature = { true },
                onIsLocationEnabledFlow = { flowOf(false) },
                onIsGpsLocationProviderEnabledFlow = ::emptyFlow
            ),
            androidLocationPermissionChecker = FakeAndroidLocationPermissionChecker(
                onCheckHasEitherFineOrCoarseLocationPermission = { true }
            )
        )

        repository.locationUpdatesFlow.test {
            assertEquals(LocationUpdate.Error.LocationOff, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun locationUpdatesFlowEmitsLocationUpdatesWhenLocationIsEnabled() = runTest {
        val repository = createRepository(
            androidLocationSupport = FakeAndroidLocationSupport(
                onHasLocationFeature = { true },
                onIsLocationEnabledFlow = { flowOf(true) },
                onIsGpsLocationProviderEnabledFlow = ::emptyFlow
            ),
            androidLocationPermissionChecker = FakeAndroidLocationPermissionChecker(
                onCheckHasEitherFineOrCoarseLocationPermission = { true }
            ),
            locationSource = FakeLocationSource(
                onLocationUpdatesFlow = {
                    flowOf(
                        LocationUpdate.AwaitingLocation,
                        LocationUpdate.Update(
                            location = Location(
                                latLon = LatLon(
                                    latitude = 1.1,
                                    longitude = 2.2
                                )
                            )
                        ),
                        LocationUpdate.Update(
                            location = Location(
                                latLon = LatLon(
                                    latitude = 3.3,
                                    longitude = 4.4
                                )
                            )
                        )
                    )
                }
            )
        )

        repository.locationUpdatesFlow.test {
            assertEquals(LocationUpdate.AwaitingLocation, awaitItem())
            assertEquals(
                LocationUpdate.Update(
                    location = Location(
                        latLon = LatLon(
                            latitude = 1.1,
                            longitude = 2.2
                        )
                    )
                ),
                awaitItem()
            )
            assertEquals(
                LocationUpdate.Update(
                    location = Location(
                        latLon = LatLon(
                            latitude = 3.3,
                            longitude = 4.4
                        )
                    )
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun distanceBetweenReturnsValueFromLocationSupport() = runTest {
        val firstLocation = LatLon(
            latitude = 1.1,
            longitude = 2.2
        )
        val secondLocation = LatLon(
            latitude = 3.3,
            longitude = 4.4
        )
        val repository = createRepository(
            androidLocationSupport = FakeAndroidLocationSupport(
                onIsLocationEnabledFlow = ::emptyFlow,
                onIsGpsLocationProviderEnabledFlow = ::emptyFlow,
                onDistanceBetween = { first, second ->
                    assertEquals(firstLocation, first)
                    assertEquals(secondLocation, second)

                    123f
                }
            )
        )

        val result = repository.distanceBetween(
            first = firstLocation,
            second = secondLocation
        )

        assertEquals(123f, result)
    }

    private fun TestScope.createRepository(
        androidLocationSupport: AndroidLocationSupport = FakeAndroidLocationSupport(
            onIsLocationEnabledFlow = ::emptyFlow,
            onIsGpsLocationProviderEnabledFlow = ::emptyFlow
        ),
        androidLocationPermissionChecker: AndroidLocationPermissionChecker =
            FakeAndroidLocationPermissionChecker(),
        locationSource: LocationSource = FakeLocationSource()
    ): AndroidLocationRepository {
        return AndroidLocationRepository(
            androidLocationSupport = androidLocationSupport,
            androidLocationPermissionChecker = androidLocationPermissionChecker,
            locationSource = locationSource,
            applicationCoroutineScope = backgroundScope
        )
    }
}
