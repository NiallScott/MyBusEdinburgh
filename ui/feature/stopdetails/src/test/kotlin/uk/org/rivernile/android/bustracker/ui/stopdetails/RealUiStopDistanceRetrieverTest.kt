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

package uk.org.rivernile.android.bustracker.ui.stopdetails

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopLocation
import uk.org.rivernile.android.bustracker.core.location.FakeLocationRepository
import uk.org.rivernile.android.bustracker.core.location.LatLon
import uk.org.rivernile.android.bustracker.core.location.Location
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.location.LocationUpdate
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [RealUiStopDistanceRetriever].
 *
 * @author Niall Scott
 */
class RealUiStopDistanceRetrieverTest {

    @Test
    fun getUiStopDistanceFlowDoesNotEmitWhenIsResumedIsFalse() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onIsResumedFlow = { flowOf(false) },
                onPermissionsStateFlow = {
                    flowOf(
                        PermissionsState(
                            fineLocationPermission = PermissionState.GRANTED,
                            coarseLocationPermission = PermissionState.GRANTED
                        )
                    )
                }
            )
        )

        retriever.getUiStopDistanceFlow(stopLocation).test {
            awaitComplete()
        }
    }

    @Test
    fun getUiStopDistanceFlowDoesNotEmitWhenPermissionsIsNull() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onIsResumedFlow = { flowOf(true) },
                onPermissionsStateFlow = { flowOf(null) }
            )
        )

        retriever.getUiStopDistanceFlow(stopLocation).test {
            awaitComplete()
        }
    }

    @Test
    fun getUiStopDistanceFlowEmitsInsufficientLocationPermissionsWhenBadPermissions() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onIsResumedFlow = { flowOf(true) },
                onPermissionsStateFlow = {
                    flowOf(
                        PermissionsState(
                            fineLocationPermission = PermissionState.UNGRANTED,
                            coarseLocationPermission = PermissionState.UNGRANTED
                        )
                    )
                }
            )
        )

        retriever.getUiStopDistanceFlow(stopLocation).test {
            assertEquals(UiStopDistance.InsufficientLocationPermissions, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getUiStopDistanceFlowEmitsObtainingLocationFollowedByLocationUnknown() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onIsResumedFlow = { flowOf(true) },
                onPermissionsStateFlow = {
                    flowOf(
                        PermissionsState(
                            fineLocationPermission = PermissionState.GRANTED,
                            coarseLocationPermission = PermissionState.GRANTED
                        )
                    )
                }
            ),
            locationRepository = FakeLocationRepository(
                onLocationUpdatesFlow = { flowOf(LocationUpdate.AwaitingLocation) }
            )
        )

        retriever.getUiStopDistanceFlow(stopLocation).test {
            assertEquals(UiStopDistance.ObtainingLocation, awaitItem())
            assertEquals(UiStopDistance.LocationUnknown, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getUiStopDistanceFlowEmitsKilometersDistance() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onIsResumedFlow = { flowOf(true) },
                onPermissionsStateFlow = {
                    flowOf(
                        PermissionsState(
                            fineLocationPermission = PermissionState.GRANTED,
                            coarseLocationPermission = PermissionState.GRANTED
                        )
                    )
                }
            ),
            locationRepository = FakeLocationRepository(
                onLocationUpdatesFlow = {
                    flowOf(
                        LocationUpdate.Update(
                            location = Location(
                                latLon = LatLon(
                                    latitude = 3.3,
                                    longitude = 4.4
                                )
                            )
                        )
                    )
                },
                onDistanceBetween = { first, second ->
                    assertEquals(
                        LatLon(
                            latitude = 1.1,
                            longitude = 2.2
                        ),
                        first
                    )
                    assertEquals(
                        LatLon(
                            latitude = 3.3,
                            longitude = 4.4
                        ),
                        second
                    )
                    1000f
                }
            )
        )

        retriever.getUiStopDistanceFlow(stopLocation).test {
            assertEquals(
                UiStopDistance.Distance.Kilometers(
                    distance = 1.0f
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getUiStopDistanceFlowEmitsMetersDistance() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onIsResumedFlow = { flowOf(true) },
                onPermissionsStateFlow = {
                    flowOf(
                        PermissionsState(
                            fineLocationPermission = PermissionState.GRANTED,
                            coarseLocationPermission = PermissionState.GRANTED
                        )
                    )
                }
            ),
            locationRepository = FakeLocationRepository(
                onLocationUpdatesFlow = {
                    flowOf(
                        LocationUpdate.Update(
                            location = Location(
                                latLon = LatLon(
                                    latitude = 3.3,
                                    longitude = 4.4
                                )
                            )
                        )
                    )
                },
                onDistanceBetween = { first, second ->
                    assertEquals(
                        LatLon(
                            latitude = 1.1,
                            longitude = 2.2
                        ),
                        first
                    )
                    assertEquals(
                        LatLon(
                            latitude = 3.3,
                            longitude = 4.4
                        ),
                        second
                    )
                    999.99f
                }
            )
        )

        retriever.getUiStopDistanceFlow(stopLocation).test {
            assertEquals(
                UiStopDistance.Distance.Meters(
                    distance = 999
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getUiStopDistanceFlowEmitsNullWhenNoLocationFeature() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onIsResumedFlow = { flowOf(true) },
                onPermissionsStateFlow = {
                    flowOf(
                        PermissionsState(
                            fineLocationPermission = PermissionState.GRANTED,
                            coarseLocationPermission = PermissionState.GRANTED
                        )
                    )
                }
            ),
            locationRepository = FakeLocationRepository(
                onLocationUpdatesFlow = { flowOf(LocationUpdate.Error.NoLocationFeature) }
            )
        )

        retriever.getUiStopDistanceFlow(stopLocation).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getUiStopDistanceFlowEmitsInsufficientLocationPermissions() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onIsResumedFlow = { flowOf(true) },
                onPermissionsStateFlow = {
                    flowOf(
                        PermissionsState(
                            fineLocationPermission = PermissionState.GRANTED,
                            coarseLocationPermission = PermissionState.GRANTED
                        )
                    )
                }
            ),
            locationRepository = FakeLocationRepository(
                onLocationUpdatesFlow = {
                    flowOf(LocationUpdate.Error.InsufficientLocationPermissions)
                }
            )
        )

        retriever.getUiStopDistanceFlow(stopLocation).test {
            assertEquals(UiStopDistance.InsufficientLocationPermissions, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getUiStopDistanceFlowEmitsLocationOff() = runTest {
        val retriever = createRetriever(
            state = FakeState(
                onIsResumedFlow = { flowOf(true) },
                onPermissionsStateFlow = {
                    flowOf(
                        PermissionsState(
                            fineLocationPermission = PermissionState.GRANTED,
                            coarseLocationPermission = PermissionState.GRANTED
                        )
                    )
                }
            ),
            locationRepository = FakeLocationRepository(
                onLocationUpdatesFlow = {
                    flowOf(LocationUpdate.Error.LocationOff)
                }
            )
        )

        retriever.getUiStopDistanceFlow(stopLocation).test {
            assertEquals(UiStopDistance.LocationOff, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getUiStopDistanceFlowEmitsAfterResumedBecomesTrue() = runTest {
        val isResumedFlow = MutableSharedFlow<Boolean>()
        val retriever = createRetriever(
            state = FakeState(
                onIsResumedFlow = { isResumedFlow },
                onPermissionsStateFlow = {
                    flowOf(
                        PermissionsState(
                            fineLocationPermission = PermissionState.GRANTED,
                            coarseLocationPermission = PermissionState.GRANTED
                        )
                    )
                }
            ),
            locationRepository = FakeLocationRepository(
                onLocationUpdatesFlow = {
                    flowOf(LocationUpdate.Error.LocationOff)
                }
            )
        )

        retriever.getUiStopDistanceFlow(stopLocation).test {
            ensureAllEventsConsumed()
            isResumedFlow.emit(true)
            assertEquals(UiStopDistance.LocationOff, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun getUiStopDistanceFlowStopsEmittingWhenResumedBecomesFalse() = runTest {
        val isResumedFlow = MutableSharedFlow<Boolean>()
        val locationUpdatesFlow = MutableSharedFlow<LocationUpdate>()
        var locationFlowStartedCount = 0
        var locationFlowStoppedCount = 0
        val retriever = createRetriever(
            state = FakeState(
                onIsResumedFlow = { isResumedFlow },
                onPermissionsStateFlow = {
                    flowOf(
                        PermissionsState(
                            fineLocationPermission = PermissionState.GRANTED,
                            coarseLocationPermission = PermissionState.GRANTED
                        )
                    )
                }
            ),
            locationRepository = FakeLocationRepository(
                onLocationUpdatesFlow = {
                    locationUpdatesFlow
                        .onStart { locationFlowStartedCount++ }
                        .onCompletion { locationFlowStoppedCount++ }
                }
            )
        )

        retriever.getUiStopDistanceFlow(stopLocation).test {
            isResumedFlow.emit(true)
            locationUpdatesFlow.emit(LocationUpdate.Error.LocationOff)
            assertEquals(UiStopDistance.LocationOff, awaitItem())
            assertEquals(1, locationFlowStartedCount)
            assertEquals(0, locationFlowStoppedCount)

            isResumedFlow.emit(false)
            assertEquals(1, locationFlowStartedCount)
            assertEquals(1, locationFlowStoppedCount)

            ensureAllEventsConsumed()
        }
    }

    private fun createRetriever(
        state: State = FakeState(),
        locationRepository: LocationRepository = FakeLocationRepository()
    ): RealUiStopDistanceRetriever {
        return RealUiStopDistanceRetriever(
            state = state,
            locationRepository = locationRepository
        )
    }

    private val stopLocation get() = FakeStopLocation(
        latitude = 1.1,
        longitude = 2.2
    )
}
