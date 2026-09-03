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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.busstops.FakeBusStopsRepository
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopDetails
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopLocation
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopName
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.toAtcoStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.features.FakeFeatureRepository
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
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
    fun getUiStopDetailsFlowEmitsNullWhenStopDetailsIsNull() = runTest {
        val retriever = createRetriever(
            busStopsRepository = FakeBusStopsRepository(
                onGetBusStopDetailsFlow = {
                    assertEquals("123456".toNaptanStopIdentifier(), it)
                    flowOf(null)
                }
            ),
            featureRepository = FakeFeatureRepository(
                onHasStopMapUiFeature = { true }
            )
        )

        retriever.getUiStopDetailsFlow("123456".toNaptanStopIdentifier()).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getUiStopDetailsFlowEmitsUiStopDetailsWhenStopDetailsIsNotNull() = runTest {
        val retriever = createRetriever(
            busStopsRepository = FakeBusStopsRepository(
                onGetBusStopDetailsFlow = {
                    assertEquals("123456".toNaptanStopIdentifier(), it)
                    flowOf(
                        FakeStopDetails(
                            naptanStopIdentifier = "123456".toNaptanStopIdentifier(),
                            atcoStopIdentifier = "atco123456".toAtcoStopIdentifier(),
                            stopName = FakeStopName(
                                name = "Name",
                                locality = "Locality"
                            ),
                            location = FakeStopLocation(
                                latitude = 1.1,
                                longitude = 2.2
                            ),
                            orientation = StopOrientation.NORTH_EAST
                        )
                    )
                }
            ),
            featureRepository = FakeFeatureRepository(
                onHasStopMapUiFeature = { true }
            ),
            stopDistanceRetriever = FakeUiStopDistanceRetriever(
                onGetUiStopDistanceFlow = {
                    assertEquals(
                        FakeStopLocation(
                            latitude = 1.1,
                            longitude = 2.2
                        ),
                        it
                    )
                    flowOf(
                        UiStopDistance.Distance.Meters(
                            distance = 123
                        )
                    )
                }
            )
        )

        retriever.getUiStopDetailsFlow("123456".toNaptanStopIdentifier()).test {
            assertEquals(
                UiStopDetails(
                    naptanCode = "123456".toNaptanStopIdentifier(),
                    atcoCode = "atco123456".toAtcoStopIdentifier(),
                    latLon = UiLatLon(
                        latitude = 1.1,
                        longitude = 2.2
                    ),
                    orientation = StopOrientation.NORTH_EAST,
                    stopDistance = UiStopDistance.Distance.Meters(
                        distance = 123
                    ),
                    isMapShown = true
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    private fun createRetriever(
        busStopsRepository: BusStopsRepository = FakeBusStopsRepository(),
        featureRepository: FeatureRepository = FakeFeatureRepository(),
        stopDistanceRetriever: UiStopDistanceRetriever = FakeUiStopDistanceRetriever()
    ): RealUiStopDetailsRetriever {
        return RealUiStopDetailsRetriever(
            busStopsRepository = busStopsRepository,
            featureRepository = featureRepository,
            stopDistanceRetriever = stopDistanceRetriever
        )
    }
}
