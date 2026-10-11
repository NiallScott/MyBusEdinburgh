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

package uk.org.rivernile.android.bustracker.ui.alerts.manager

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.busstops.FakeBusStopsRepository
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopDetails
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopLocation
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopName
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toAtcoStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.text.UiStopName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [RealUiStopDetailsRetriever].
 *
 * @author Niall Scott
 */
class RealUiStopDetailsRetrieverTest {

    @Test
    fun getUiStopDetailsFlowEmitsNullWhenBusStopsRepositoryEmitsNull() = runTest {
        val retriever = createRetriever(
            busStopsRepository = FakeBusStopsRepository(
                onGetBusStopsDetailsFlow = {
                    assertEquals(
                        setOf(
                            "123".toNaptanStopIdentifier()
                        ),
                        it
                    )

                    flowOf(null)
                }
            )
        )

        retriever
            .getUiStopDetailsFlow(
                stopIdentifiers = setOf("123".toNaptanStopIdentifier())
            )
            .test {
                assertNull(awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun getUiStopDetailsFlowEmitsNullWhenBusStopsRepositoryEmitsEmptyMap() = runTest {
        val retriever = createRetriever(
            busStopsRepository = FakeBusStopsRepository(
                onGetBusStopsDetailsFlow = {
                    assertEquals(
                        setOf(
                            "123".toNaptanStopIdentifier()
                        ),
                        it
                    )

                    flowOf(emptyMap())
                }
            )
        )

        retriever
            .getUiStopDetailsFlow(
                stopIdentifiers = setOf("123".toNaptanStopIdentifier())
            )
            .test {
                assertNull(awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun getUiStopDetailsFlowEmitsStopDetails() = runTest {
        val retriever = createRetriever(
            busStopsRepository = FakeBusStopsRepository(
                onGetBusStopsDetailsFlow = {
                    assertEquals(
                        setOf(
                            "123".toNaptanStopIdentifier(),
                            "456".toNaptanStopIdentifier(),
                            "789".toNaptanStopIdentifier()
                        ),
                        it
                    )

                    flowOf(
                        mapOf(
                            "123".toNaptanStopIdentifier() to FakeStopDetails(
                                naptanStopIdentifier = "123".toNaptanStopIdentifier(),
                                atcoStopIdentifier = "atco123".toAtcoStopIdentifier(),
                                stopName = FakeStopName(
                                    name = "Stop Name 1",
                                    locality = "Locality 1"
                                ),
                                location = FakeStopLocation(
                                    latitude = 1.1,
                                    longitude = 2.2
                                ),
                                orientation = StopOrientation.NORTH_EAST
                            ),
                            "789".toNaptanStopIdentifier() to FakeStopDetails(
                                naptanStopIdentifier = "789".toNaptanStopIdentifier(),
                                atcoStopIdentifier = "atco789".toAtcoStopIdentifier(),
                                stopName = FakeStopName(
                                    name = "Stop Name 3",
                                    locality = "Locality 3"
                                ),
                                location = FakeStopLocation(
                                    latitude = 3.3,
                                    longitude = 4.4
                                ),
                                orientation = StopOrientation.WEST
                            )
                        )
                    )
                }
            )
        )

        retriever
            .getUiStopDetailsFlow(
                stopIdentifiers = setOf(
                    "123".toNaptanStopIdentifier(),
                    "456".toNaptanStopIdentifier(),
                    "789".toNaptanStopIdentifier()
                )
            )
            .test {
                assertEquals(
                    mapOf<StopIdentifier, UiStopDetails>(
                        "123".toNaptanStopIdentifier() to UiStopDetails(
                            stopName = UiStopName(
                                name = "Stop Name 1",
                                locality = "Locality 1"
                            ),
                            latLon = UiLatLon(
                                latitude = 1.1,
                                longitude = 2.2
                            ),
                            orientation = StopOrientation.NORTH_EAST
                        ),
                        "789".toNaptanStopIdentifier() to UiStopDetails(
                            stopName = UiStopName(
                                name = "Stop Name 3",
                                locality = "Locality 3"
                            ),
                            latLon = UiLatLon(
                                latitude = 3.3,
                                longitude = 4.4
                            ),
                            orientation = StopOrientation.WEST
                        )
                    ),
                    awaitItem()
                )
                awaitComplete()
            }
    }

    private fun createRetriever(
        busStopsRepository: BusStopsRepository = FakeBusStopsRepository()
    ): RealUiStopDetailsRetriever {
        return RealUiStopDetailsRetriever(
            busStopsRepository = busStopsRepository
        )
    }
}
