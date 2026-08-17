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

package uk.org.rivernile.android.bustracker.ui.neareststops

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.busstops.FakeBusStopsRepository
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopDetailsWithServices
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopLocation
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopName
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.config.ConfigRepository
import uk.org.rivernile.android.bustracker.core.config.FakeConfigRepository
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.location.FakeLocationRepository
import uk.org.rivernile.android.bustracker.core.location.LatLon
import uk.org.rivernile.android.bustracker.core.location.Location
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.location.LocationUpdate
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private const val LOCATION_PRECISION = 0.000001

/**
 * Tests for [RealNearestStopsRetriever].
 *
 * @author Niall Scott
 */
class RealNearestStopsRetrieverTest {

    @Test
    fun nearestStopsStateFlowDoesNotEmitWhenPermissionsStateIsUnset() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onPermissionsStateFlow = { flowOf(null) }
            )
        )

        retriever.nearestStopsStateFlow.test {
            awaitComplete()
        }
    }

    @Test
    fun nearestStopsStateFlowEmitsNoLocationFeatureWhenNoLocationFeature() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onPermissionsStateFlow = { flowOf(sufficientPermissions) },
                onSelectedServicesFlow = { flowOf(null) }
            ),
            locationRepository = FakeLocationRepository(
                onLocationUpdatesFlow = { flowOf(LocationUpdate.Error.NoLocationFeature) }
            ),
            locationAccuracyGenerator = FakeUiLocationAccuracyGenerator(
                onGetUiLocationAccuracyFlow = { permissionsState ->
                    assertEquals(sufficientPermissions, permissionsState)
                    flowOf(null)
                }
            )
        )

        retriever.nearestStopsStateFlow.test {
            assertEquals(NearestStopsState.Error.NoLocationFeature, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun nearestStopsStateFlowEmitsInsufficientLocationPermissionsWhenNoPermissions() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onPermissionsStateFlow = { flowOf(insufficientPermissions) },
                onSelectedServicesFlow = { flowOf(null) }
            ),
            locationRepository = FakeLocationRepository(
                onLocationUpdatesFlow = {
                    flowOf(LocationUpdate.Error.InsufficientLocationPermissions)
                }
            ),
            locationAccuracyGenerator = FakeUiLocationAccuracyGenerator(
                onGetUiLocationAccuracyFlow = { permissionsState ->
                    assertEquals(insufficientPermissions, permissionsState)
                    flowOf(null)
                }
            )
        )

        retriever.nearestStopsStateFlow.test {
            assertEquals(NearestStopsState.Error.InsufficientLocationPermissions, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun nearestStopsStateFlowEmitsLocationOffWhenLocationIsNotEnabled() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onPermissionsStateFlow = { flowOf(sufficientPermissions) },
                onSelectedServicesFlow = { flowOf(null) }
            ),
            locationRepository = FakeLocationRepository(
                onLocationUpdatesFlow = { flowOf(LocationUpdate.Error.LocationOff) }
            ),
            locationAccuracyGenerator = FakeUiLocationAccuracyGenerator(
                onGetUiLocationAccuracyFlow = { permissionsState ->
                    assertEquals(sufficientPermissions, permissionsState)
                    flowOf(null)
                }
            )
        )

        retriever.nearestStopsStateFlow.test {
            assertEquals(NearestStopsState.Error.LocationOff, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun nearestStopsStateFlowEmitsLocationUnknownWhilstDeterminingLocation() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onPermissionsStateFlow = { flowOf(sufficientPermissions) },
                onSelectedServicesFlow = { flowOf(null) }
            ),
            locationRepository = FakeLocationRepository(
                onLocationUpdatesFlow = { flowOf(LocationUpdate.AwaitingLocation) }
            ),
            locationAccuracyGenerator = FakeUiLocationAccuracyGenerator(
                onGetUiLocationAccuracyFlow = { permissionsState ->
                    assertEquals(sufficientPermissions, permissionsState)
                    flowOf(null)
                }
            )
        )

        retriever.nearestStopsStateFlow.test {
            assertEquals(NearestStopsState.AwaitingLocation, awaitItem())
            assertEquals(NearestStopsState.Error.LocationUnknown, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun nearestStopsStateFlowEmitsStopsWithNullStopsWhenStopDetailsIsNull() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onPermissionsStateFlow = { flowOf(sufficientPermissions) },
                onSelectedServicesFlow = { flowOf(null) }
            ),
            locationRepository = FakeLocationRepository(
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
                        )
                    )
                }
            ),
            configRepository = FakeConfigRepository(
                onNearestStopsLatitudeSpan = { 0.1 },
                onNearestStopsLongitudeSpan = { 0.2 }
            ),
            busStopsRepository = FakeBusStopsRepository(
                onGetStopDetailsWithinSpanFlow = { minLat, minLon, maxLat, maxLon, sel ->
                    assertEquals(1.0, minLat, LOCATION_PRECISION)
                    assertEquals(1.2, maxLat, LOCATION_PRECISION)
                    assertEquals(2.0, minLon, LOCATION_PRECISION)
                    assertEquals(2.4, maxLon, LOCATION_PRECISION)
                    assertNull(sel)

                    flowOf(null)
                }
            ),
            locationAccuracyGenerator = FakeUiLocationAccuracyGenerator(
                onGetUiLocationAccuracyFlow = { permissionsState ->
                    assertEquals(sufficientPermissions, permissionsState)
                    flowOf(null)
                }
            )
        )

        retriever.nearestStopsStateFlow.test {
            assertEquals(NearestStopsState.AwaitingLocation, awaitItem())
            assertEquals(
                NearestStopsState.Stops(
                    stops = null,
                    locationAccuracy = null
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun nearestStopsStateFlowEmitsStopsWithNullStopsWhenStopDetailsIsEmpty() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onPermissionsStateFlow = { flowOf(sufficientPermissions) },
                onSelectedServicesFlow = { flowOf(null) }
            ),
            locationRepository = FakeLocationRepository(
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
                        )
                    )
                }
            ),
            configRepository = FakeConfigRepository(
                onNearestStopsLatitudeSpan = { 0.1 },
                onNearestStopsLongitudeSpan = { 0.2 }
            ),
            busStopsRepository = FakeBusStopsRepository(
                onGetStopDetailsWithinSpanFlow = { minLat, minLon, maxLat, maxLon, sel ->
                    assertEquals(1.0, minLat, LOCATION_PRECISION)
                    assertEquals(1.2, maxLat, LOCATION_PRECISION)
                    assertEquals(2.0, minLon, LOCATION_PRECISION)
                    assertEquals(2.4, maxLon, LOCATION_PRECISION)
                    assertNull(sel)

                    flowOf(emptyList())
                }
            ),
            locationAccuracyGenerator = FakeUiLocationAccuracyGenerator(
                onGetUiLocationAccuracyFlow = { permissionsState ->
                    assertEquals(sufficientPermissions, permissionsState)
                    flowOf(null)
                }
            )
        )

        retriever.nearestStopsStateFlow.test {
            assertEquals(NearestStopsState.AwaitingLocation, awaitItem())
            assertEquals(
                NearestStopsState.Stops(
                    stops = null,
                    locationAccuracy = null
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun nearestStopsStateFlowEmitsStopsWhenStopDetailsIsPopulated() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onPermissionsStateFlow = { flowOf(sufficientPermissions) },
                onSelectedServicesFlow = { flowOf(null) }
            ),
            locationRepository = FakeLocationRepository(
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
                        )
                    )
                },
                onDistanceBetween = { stopLocation, deviceLocation ->
                    assertEquals(
                        LatLon(
                            latitude = 11.1,
                            longitude = 12.2
                        ),
                        stopLocation
                    )
                    assertEquals(
                        LatLon(
                            latitude = 1.1,
                            longitude = 2.2
                        ),
                        deviceLocation
                    )
                    -12.34f
                }
            ),
            configRepository = FakeConfigRepository(
                onNearestStopsLatitudeSpan = { 0.1 },
                onNearestStopsLongitudeSpan = { 0.2 }
            ),
            busStopsRepository = FakeBusStopsRepository(
                onGetStopDetailsWithinSpanFlow = { minLat, minLon, maxLat, maxLon, sel ->
                    assertEquals(1.0, minLat, LOCATION_PRECISION)
                    assertEquals(1.2, maxLat, LOCATION_PRECISION)
                    assertEquals(2.0, minLon, LOCATION_PRECISION)
                    assertEquals(2.4, maxLon, LOCATION_PRECISION)
                    assertNull(sel)

                    flowOf(listOf(stopDetailsWithServices))
                }
            ),
            locationAccuracyGenerator = FakeUiLocationAccuracyGenerator(
                onGetUiLocationAccuracyFlow = { permissionsState ->
                    assertEquals(sufficientPermissions, permissionsState)
                    flowOf(null)
                }
            )
        )

        retriever.nearestStopsStateFlow.test {
            assertEquals(NearestStopsState.AwaitingLocation, awaitItem())
            assertEquals(
                NearestStopsState.Stops(
                    stops = listOf(nearestStop),
                    locationAccuracy = null
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun nearestStopsStateFlowWithFilteredServicesEmitsStopsWhenStopDetailsIsPopulated() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onPermissionsStateFlow = { flowOf(sufficientPermissions) },
                onSelectedServicesFlow = { flowOf(setOf(serviceDescriptor)) }
            ),
            locationRepository = FakeLocationRepository(
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
                        )
                    )
                },
                onDistanceBetween = { stopLocation, deviceLocation ->
                    assertEquals(
                        LatLon(
                            latitude = 11.1,
                            longitude = 12.2
                        ),
                        stopLocation
                    )
                    assertEquals(
                        LatLon(
                            latitude = 1.1,
                            longitude = 2.2
                        ),
                        deviceLocation
                    )
                    -12.34f
                }
            ),
            configRepository = FakeConfigRepository(
                onNearestStopsLatitudeSpan = { 0.1 },
                onNearestStopsLongitudeSpan = { 0.2 }
            ),
            busStopsRepository = FakeBusStopsRepository(
                onGetStopDetailsWithinSpanFlow = { minLat, minLon, maxLat, maxLon, sel ->
                    assertEquals(1.0, minLat, LOCATION_PRECISION)
                    assertEquals(1.2, maxLat, LOCATION_PRECISION)
                    assertEquals(2.0, minLon, LOCATION_PRECISION)
                    assertEquals(2.4, maxLon, LOCATION_PRECISION)
                    assertEquals(setOf(serviceDescriptor), sel)

                    flowOf(listOf(stopDetailsWithServices))
                }
            ),
            locationAccuracyGenerator = FakeUiLocationAccuracyGenerator(
                onGetUiLocationAccuracyFlow = { permissionsState ->
                    assertEquals(sufficientPermissions, permissionsState)
                    flowOf(null)
                }
            )
        )

        retriever.nearestStopsStateFlow.test {
            assertEquals(NearestStopsState.AwaitingLocation, awaitItem())
            assertEquals(
                NearestStopsState.Stops(
                    stops = listOf(nearestStop),
                    locationAccuracy = null
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun nearestStopsStateFlowEmitsStopsWithLocationAccuracy() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onPermissionsStateFlow = { flowOf(sufficientPermissions) },
                onSelectedServicesFlow = { flowOf(null) }
            ),
            locationRepository = FakeLocationRepository(
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
                        )
                    )
                },
                onDistanceBetween = { stopLocation, deviceLocation ->
                    assertEquals(
                        LatLon(
                            latitude = 11.1,
                            longitude = 12.2
                        ),
                        stopLocation
                    )
                    assertEquals(
                        LatLon(
                            latitude = 1.1,
                            longitude = 2.2
                        ),
                        deviceLocation
                    )
                    -12.34f
                }
            ),
            configRepository = FakeConfigRepository(
                onNearestStopsLatitudeSpan = { 0.1 },
                onNearestStopsLongitudeSpan = { 0.2 }
            ),
            busStopsRepository = FakeBusStopsRepository(
                onGetStopDetailsWithinSpanFlow = { minLat, minLon, maxLat, maxLon, sel ->
                    assertEquals(1.0, minLat, LOCATION_PRECISION)
                    assertEquals(1.2, maxLat, LOCATION_PRECISION)
                    assertEquals(2.0, minLon, LOCATION_PRECISION)
                    assertEquals(2.4, maxLon, LOCATION_PRECISION)
                    assertNull(sel)

                    flowOf(listOf(stopDetailsWithServices))
                }
            ),
            locationAccuracyGenerator = FakeUiLocationAccuracyGenerator(
                onGetUiLocationAccuracyFlow = { permissionsState ->
                    assertEquals(sufficientPermissions, permissionsState)
                    flowOf(UiLocationAccuracy.GPS_DISABLED)
                }
            )
        )

        retriever.nearestStopsStateFlow.test {
            assertEquals(NearestStopsState.AwaitingLocation, awaitItem())
            assertEquals(
                NearestStopsState.Stops(
                    stops = listOf(nearestStop),
                    locationAccuracy = UiLocationAccuracy.GPS_DISABLED
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    private fun createRetriever(
        state: State = FakeState(),
        locationRepository: LocationRepository = FakeLocationRepository(),
        configRepository: ConfigRepository = FakeConfigRepository(),
        busStopsRepository: BusStopsRepository = FakeBusStopsRepository(),
        locationAccuracyGenerator: UiLocationAccuracyGenerator = FakeUiLocationAccuracyGenerator()
    ): RealNearestStopsRetriever {
        return RealNearestStopsRetriever(
            state = state,
            locationRepository = locationRepository,
            configRepository = configRepository,
            busStopsRepository = busStopsRepository,
            locationAccuracyGenerator = locationAccuracyGenerator
        )
    }

    private val insufficientPermissions: PermissionsState get() = PermissionsState(
        fineLocationPermission = PermissionState.UNGRANTED,
        coarseLocationPermission = PermissionState.UNGRANTED
    )

    private val sufficientPermissions: PermissionsState get() = PermissionsState(
        fineLocationPermission = PermissionState.GRANTED,
        coarseLocationPermission = PermissionState.GRANTED
    )

    private val stopDetailsWithServices get() = FakeStopDetailsWithServices(
        stopIdentifier = "123456".toNaptanStopIdentifier(),
        stopName = FakeStopName(
            name = "Name",
            locality = "Locality"
        ),
        location = FakeStopLocation(
            latitude = 11.1,
            longitude = 12.2
        ),
        orientation = StopOrientation.NORTH_EAST,
        serviceListing = null
    )

    private val nearestStop get() = NearestStop(
        stopIdentifier = "123456".toNaptanStopIdentifier(),
        stopName = FakeStopName(
            name = "Name",
            locality = "Locality"
        ),
        distanceMeters = 12,
        orientation = StopOrientation.NORTH_EAST,
        serviceListing = null
    )

    private val serviceDescriptor get() = FakeServiceDescriptor(
        serviceName = "1",
        operatorCode = "TEST1"
    )
}
