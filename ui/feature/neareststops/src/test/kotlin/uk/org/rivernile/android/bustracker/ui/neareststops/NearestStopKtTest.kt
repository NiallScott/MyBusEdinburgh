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

import uk.org.rivernile.android.bustracker.core.busstops.FakeStopDetailsWithServices
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopLocation
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopName
import uk.org.rivernile.android.bustracker.core.busstops.StopDetailsWithServices
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Tests for `NearestStop.kt`.
 *
 * @author Niall Scott
 */
class NearestStopKtTest {

    @Test
    fun toNearestStopsMapsEmptyToEmpty() {
        val result = emptyList<StopDetailsWithServices>().toNearestStops { 1 }

        assertTrue(result.isEmpty())
    }

    @Test
    fun toNearestStopsMapsSingleItemToNearestStop() {
        val result = listOf(
            FakeStopDetailsWithServices(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = FakeStopName(
                    name = "Stop Name",
                    locality = "Locality"
                ),
                location = FakeStopLocation(
                    latitude = 1.1,
                    longitude = 2.2
                ),
                orientation = StopOrientation.NORTH,
                serviceListing = null
            )
        ).toNearestStops {
            if (it.latitude == 1.1 && it.longitude == 2.2) 123 else fail()
        }

        assertEquals(
            listOf(
                NearestStop(
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    stopName = FakeStopName(
                        name = "Stop Name",
                        locality = "Locality"
                    ),
                    distanceMeters = 123,
                    orientation = StopOrientation.NORTH,
                    serviceListing = null
                )
            ),
            result
        )
    }

    @Test
    fun toNearestStopsMapsMultipleItemsToNearestStops() {
        val stopDetails = listOf(
            FakeStopDetailsWithServices(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = FakeStopName(
                    name = "Stop Name 1",
                    locality = "Locality 1"
                ),
                location = FakeStopLocation(
                    latitude = 1.1,
                    longitude = 2.2
                ),
                orientation = StopOrientation.NORTH,
                serviceListing = null
            ),
            FakeStopDetailsWithServices(
                stopIdentifier = "987654".toNaptanStopIdentifier(),
                stopName = FakeStopName(
                    name = "Stop Name 2",
                    locality = "Locality 2"
                ),
                location = FakeStopLocation(
                    latitude = 3.3,
                    longitude = 4.4
                ),
                orientation = StopOrientation.SOUTH,
                serviceListing = null
            )
        )
        val result = stopDetails.toNearestStops {
            when (it) {
                stopDetails[0].location -> 123
                stopDetails[1].location -> 456
                else -> fail()
            }
        }

        assertEquals(
            listOf(
                NearestStop(
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    stopName = FakeStopName(
                        name = "Stop Name 1",
                        locality = "Locality 1"
                    ),
                    distanceMeters = 123,
                    orientation = StopOrientation.NORTH,
                    serviceListing = null
                ),
                NearestStop(
                    stopIdentifier = "987654".toNaptanStopIdentifier(),
                    stopName = FakeStopName(
                        name = "Stop Name 2",
                        locality = "Locality 2"
                    ),
                    distanceMeters = 456,
                    orientation = StopOrientation.SOUTH,
                    serviceListing = null
                )
            ),
            result
        )
    }
}
