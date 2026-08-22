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
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopName
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.services.FakeServicesRepository
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import uk.org.rivernile.android.bustracker.core.text.UiStopName
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [UiContentRetriever].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RealUiContentRetrieverTest {

    @Test
    fun uiContentFlowEmitsInProgressWhenStateIsAwaitingLocation() = runTest {
        val retriever = createRetriever(
            nearestStopsRetriever = FakeNearestStopsRetriever(
                onNearestStopsStateFlow = { flowOf(NearestStopsState.AwaitingLocation) }
            ),
            servicesRepository = FakeServicesRepository(
                onGetColoursForServicesFlow = {
                    assertNull(it)
                    flowOf(null)
                }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun uiContentFlowEmitsNoNearestStopsWhenStateStopsIsNull() = runTest {
        val retriever = createRetriever(
            nearestStopsRetriever = FakeNearestStopsRetriever(
                onNearestStopsStateFlow = {
                    flowOf(
                        NearestStopsState.Stops(
                            stops = null,
                            locationAccuracy = UiLocationAccuracy.PERMISSIONS_NOT_SUFFICIENT
                        )
                    )
                }
            ),
            servicesRepository = FakeServicesRepository(
                onGetColoursForServicesFlow = {
                    assertNull(it)
                    flowOf(null)
                }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(
                UiContent.Error.NoNearestStops(
                    locationAccuracy = UiLocationAccuracy.PERMISSIONS_NOT_SUFFICIENT
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun uiContentFlowEmitsNoNearestStopsWhenStateStopsIsEmpty() = runTest {
        val retriever = createRetriever(
            nearestStopsRetriever = FakeNearestStopsRetriever(
                onNearestStopsStateFlow = {
                    flowOf(
                        NearestStopsState.Stops(
                            stops = emptyList(),
                            locationAccuracy = UiLocationAccuracy.PERMISSIONS_NOT_SUFFICIENT
                        )
                    )
                }
            ),
            servicesRepository = FakeServicesRepository(
                onGetColoursForServicesFlow = {
                    assertNull(it)
                    flowOf(null)
                }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(
                UiContent.Error.NoNearestStops(
                    locationAccuracy = UiLocationAccuracy.PERMISSIONS_NOT_SUFFICIENT
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun uiContentFlowEmitsNearestStops() = runTest {
        val retriever = createRetriever(
            nearestStopsRetriever = FakeNearestStopsRetriever(
                onNearestStopsStateFlow = {
                    flowOf(
                        NearestStopsState.Stops(
                            stops = listOf(
                                NearestStop(
                                    stopIdentifier = "123".toNaptanStopIdentifier(),
                                    stopName = FakeStopName(
                                        name = "Stop 1",
                                        locality = "Locality 1"
                                    ),
                                    distanceMeters = 3,
                                    orientation = StopOrientation.NORTH_EAST,
                                    serviceListing = listOf(
                                        createService(2),
                                        createService(1)
                                    )
                                ),
                                NearestStop(
                                    stopIdentifier = "456".toNaptanStopIdentifier(),
                                    stopName = FakeStopName(
                                        name = "Stop 2",
                                        locality = "Locality 2"
                                    ),
                                    distanceMeters = 1,
                                    orientation = StopOrientation.SOUTH_WEST,
                                    serviceListing = null
                                ),
                                NearestStop(
                                    stopIdentifier = "789".toNaptanStopIdentifier(),
                                    stopName = FakeStopName(
                                        name = "Stop 3",
                                        locality = "Locality 3"
                                    ),
                                    distanceMeters = 2,
                                    orientation = StopOrientation.WEST,
                                    serviceListing = listOf(createService(3))
                                )
                            ),
                            locationAccuracy = UiLocationAccuracy.PERMISSIONS_NOT_SUFFICIENT
                        )
                    )
                }
            ),
            servicesRepository = FakeServicesRepository(
                onGetColoursForServicesFlow = { services ->
                    assertNull(services)

                    flowOf(
                        mapOf(
                            createService(1) to ServiceColours(
                                colourPrimary = 1,
                                colourOnPrimary = 2
                            ),
                            createService(3) to ServiceColours(
                                colourPrimary = 3,
                                colourOnPrimary = 4
                            )
                        )
                    )
                }
            ),
            dropdownMenuGenerator = FakeUiNearestStopDropdownMenuGenerator(
                onGetDropdownMenuItemsForStopsFlow = { stops ->
                    assertEquals(
                        setOf(
                            "123".toNaptanStopIdentifier(),
                            "456".toNaptanStopIdentifier(),
                            "789".toNaptanStopIdentifier()
                        ),
                        stops
                    )

                    flowOf(
                        mapOf(
                            "123".toNaptanStopIdentifier() to UiNearestStopDropdownMenu(
                                isStopMapItemShown = true
                            ),
                            "456".toNaptanStopIdentifier() to UiNearestStopDropdownMenu(
                                isStopMapItemShown = true
                            )
                        )
                    )
                }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(
                UiContent.Content(
                    nearestStops = persistentListOf(
                        UiNearestStop(
                            stopIdentifier = "456".toNaptanStopIdentifier(),
                            stopName = UiStopName(
                                name = "Stop 2",
                                locality = "Locality 2"
                            ),
                            services = null,
                            orientation = StopOrientation.SOUTH_WEST,
                            distanceMeters = 1,
                            dropdownMenu = UiNearestStopDropdownMenu(
                                isStopMapItemShown = true
                            )
                        ),
                        UiNearestStop(
                            stopIdentifier = "789".toNaptanStopIdentifier(),
                            stopName = UiStopName(
                                name = "Stop 3",
                                locality = "Locality 3"
                            ),
                            services = persistentListOf(
                                UiServiceName(
                                    serviceName = "3",
                                    colours = UiServiceColours(
                                        backgroundColour = 3,
                                        textColour = 4
                                    )
                                )
                            ),
                            orientation = StopOrientation.WEST,
                            distanceMeters = 2,
                            dropdownMenu = UiNearestStopDropdownMenu()
                        ),
                        UiNearestStop(
                            stopIdentifier = "123".toNaptanStopIdentifier(),
                            stopName = UiStopName(
                                name = "Stop 1",
                                locality = "Locality 1"
                            ),
                            services = persistentListOf(
                                UiServiceName(
                                    serviceName = "1",
                                    colours = UiServiceColours(
                                        backgroundColour = 1,
                                        textColour = 2
                                    )
                                ),
                                UiServiceName(
                                    serviceName = "2",
                                    colours = null
                                )
                            ),
                            orientation = StopOrientation.NORTH_EAST,
                            distanceMeters = 3,
                            dropdownMenu = UiNearestStopDropdownMenu(
                                isStopMapItemShown = true
                            )
                        )
                    ),
                    locationAccuracy = UiLocationAccuracy.PERMISSIONS_NOT_SUFFICIENT
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun uiContentFlowEmitsNoLocationFeatureWhenStateIsNoLocationFeature() = runTest {
        val retriever = createRetriever(
            nearestStopsRetriever = FakeNearestStopsRetriever(
                onNearestStopsStateFlow = { flowOf(NearestStopsState.Error.NoLocationFeature) }
            ),
            servicesRepository = FakeServicesRepository(
                onGetColoursForServicesFlow = {
                    assertNull(it)
                    flowOf(null)
                }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(UiContent.Error.NoLocationFeature, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun uiContentFlowEmitsInsufficientLocationPermissionsWhenInsufficientLocationPerms() = runTest {
        val retriever = createRetriever(
            nearestStopsRetriever = FakeNearestStopsRetriever(
                onNearestStopsStateFlow = {
                    flowOf(NearestStopsState.Error.InsufficientLocationPermissions)
                }
            ),
            servicesRepository = FakeServicesRepository(
                onGetColoursForServicesFlow = {
                    assertNull(it)
                    flowOf(null)
                }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(UiContent.Error.InsufficientLocationPermissions, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun uiContentFlowEmitsLocationOffWhenStateIsLocationOff() = runTest {
        val retriever = createRetriever(
            nearestStopsRetriever = FakeNearestStopsRetriever(
                onNearestStopsStateFlow = { flowOf(NearestStopsState.Error.LocationOff) }
            ),
            servicesRepository = FakeServicesRepository(
                onGetColoursForServicesFlow = {
                    assertNull(it)
                    flowOf(null)
                }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(UiContent.Error.LocationOff, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun uiContentFlowEmitsInProgressThenLocationUnknownWhenStateIsLocationUnknown() = runTest {
        val retriever = createRetriever(
            nearestStopsRetriever = FakeNearestStopsRetriever(
                onNearestStopsStateFlow = { flowOf(NearestStopsState.Error.LocationUnknown) }
            ),
            servicesRepository = FakeServicesRepository(
                onGetColoursForServicesFlow = {
                    assertNull(it)
                    flowOf(null)
                }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(UiContent.Error.LocationUnknown, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    private fun createService(id: Int) = FakeServiceDescriptor(
        serviceName = id.toString(),
        operatorCode = "TEST$id"
    )

    private fun TestScope.createRetriever(
        nearestStopsRetriever: NearestStopsRetriever = FakeNearestStopsRetriever(),
        servicesRepository: ServicesRepository = FakeServicesRepository(),
        dropdownMenuGenerator: UiNearestStopDropdownMenuGenerator =
            FakeUiNearestStopDropdownMenuGenerator(),
        serviceNameComparator: Comparator<String> = naturalOrder()
    ): RealUiContentRetriever {
        return RealUiContentRetriever(
            nearestStopsRetriever = nearestStopsRetriever,
            servicesRepository = servicesRepository,
            dropdownMenuGenerator = dropdownMenuGenerator,
            serviceNameComparator = serviceNameComparator,
            defaultCoroutineDispatcher = UnconfinedTestDispatcher(scheduler = testScheduler),
            viewModelCoroutineScope = backgroundScope
        )
    }
}
