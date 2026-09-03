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

import com.google.android.gms.maps.model.LatLng
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopDetails
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopLocation
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopName
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.toAtcoStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for `UiStopDetails.kt`.
 *
 * @author Niall Scott
 */
class UiStopDetailsKtTest {

    @Test
    fun toUiStopDetailsMapToUiStopDetailsWhenStopDistanceIsNull() {
        val result = stopDetails
            .toUiStopDetails(
                stopDistance = null,
                isMapShown = true
            )

        assertEquals(
            UiStopDetails(
                naptanCode = "123456".toNaptanStopIdentifier(),
                atcoCode = "atco123456".toAtcoStopIdentifier(),
                latLon = UiLatLon(
                    latitude = 1.1,
                    longitude = 2.2
                ),
                orientation = StopOrientation.NORTH_EAST,
                stopDistance = null,
                isMapShown = true
            ),
            result
        )
    }

    @Test
    fun toUiStopDetailsMapToUiStopDetailsWhenStopDistanceIsNotNull() {
        val result = stopDetails
            .toUiStopDetails(
                stopDistance = UiStopDistance.Distance.Meters(
                    distance = 123
                ),
                isMapShown = true
            )

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
            result
        )
    }

    @Test
    fun toGoogleMapsLatLngMapsToLatLng() {
        val result = UiLatLon(
            latitude = 1.1,
            longitude = 2.2
        ).toGoogleMapsLatLng()

        assertEquals(
            LatLng(1.1, 2.2),
            result
        )
    }

    private val stopDetails get() = FakeStopDetails(
        naptanStopIdentifier = "123456".toNaptanStopIdentifier(),
        atcoStopIdentifier = "atco123456".toAtcoStopIdentifier(),
        stopName = FakeStopName(
            name = "Stop Name",
            locality = "Locality"
        ),
        location = FakeStopLocation(
            latitude = 1.1,
            longitude = 2.2
        ),
        orientation = StopOrientation.NORTH_EAST
    )
}
