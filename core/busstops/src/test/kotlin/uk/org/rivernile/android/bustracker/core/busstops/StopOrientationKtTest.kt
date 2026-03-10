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

package uk.org.rivernile.android.bustracker.core.busstops

import kotlin.test.Test
import kotlin.test.assertEquals
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopOrientation
    as DatabaseStopOrientation

/**
 * Tests for `StopOrientation.kt`.
 *
 * @author Niall Scott
 */
class StopOrientationKtTest {

    @Test
    fun northMapsToNorth() {
        assertEquals(StopOrientation.NORTH, DatabaseStopOrientation.NORTH.toStopOrientation())
    }

    @Test
    fun northEastMapsToNorthEast() {
        assertEquals(
            StopOrientation.NORTH_EAST,
            DatabaseStopOrientation.NORTH_EAST.toStopOrientation()
        )
    }

    @Test
    fun eastMapsToEast() {
        assertEquals(StopOrientation.EAST, DatabaseStopOrientation.EAST.toStopOrientation())
    }

    @Test
    fun southEastMapsToSouthEast() {
        assertEquals(
            StopOrientation.SOUTH_EAST,
            DatabaseStopOrientation.SOUTH_EAST.toStopOrientation()
        )
    }

    @Test
    fun southMapsToSouth() {
        assertEquals(StopOrientation.SOUTH, DatabaseStopOrientation.SOUTH.toStopOrientation())
    }

    @Test
    fun southWestMapsToSouthWest() {
        assertEquals(
            StopOrientation.SOUTH_WEST,
            DatabaseStopOrientation.SOUTH_WEST.toStopOrientation()
        )
    }

    @Test
    fun westMapsToWest() {
        assertEquals(StopOrientation.WEST, DatabaseStopOrientation.WEST.toStopOrientation())
    }

    @Test
    fun northWestMapsToNorthWest() {
        assertEquals(
            StopOrientation.NORTH_WEST,
            DatabaseStopOrientation.NORTH_WEST.toStopOrientation()
        )
    }

    @Test
    fun unknownMapsToUnknown() {
        assertEquals(StopOrientation.UNKNOWN, DatabaseStopOrientation.UNKNOWN.toStopOrientation())
    }
}
