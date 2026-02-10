/*
 * Copyright (C) 2023 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.busstop.stop

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [StopTypeConverter].
 *
 * @author Niall Scott
 */
class StopTypeConverterTest {

    private lateinit var converter: StopTypeConverter

    @BeforeTest
    fun setUp() {
        converter = StopTypeConverter()
    }

    @Test
    fun convertToStopOrientationWithValue0MapsToNorth() {
        val result = converter.convertToStopOrientation("N")

        assertEquals(StopOrientation.NORTH, result)
    }

    @Test
    fun convertToStopOrientationWithValue1MapsToNorthEast() {
        val result = converter.convertToStopOrientation("NE")

        assertEquals(StopOrientation.NORTH_EAST, result)
    }

    @Test
    fun convertToStopOrientationWithValue2MapsToEast() {
        val result = converter.convertToStopOrientation("E")

        assertEquals(StopOrientation.EAST, result)
    }

    @Test
    fun convertToStopOrientationWithValue3MapsToSouthEast() {
        val result = converter.convertToStopOrientation("SE")

        assertEquals(StopOrientation.SOUTH_EAST, result)
    }

    @Test
    fun convertToStopOrientationWithValue4MapsToSouth() {
        val result = converter.convertToStopOrientation("S")

        assertEquals(StopOrientation.SOUTH, result)
    }

    @Test
    fun convertToStopOrientationWithValue5MapsToSouthWest() {
        val result = converter.convertToStopOrientation("SW")

        assertEquals(StopOrientation.SOUTH_WEST, result)
    }

    @Test
    fun convertToStopOrientationWithValue6MapsToWest() {
        val result = converter.convertToStopOrientation("W")

        assertEquals(StopOrientation.WEST, result)
    }

    @Test
    fun convertToStopOrientationWithValue7MapsToNorthWest() {
        val result = converter.convertToStopOrientation("NW")

        assertEquals(StopOrientation.NORTH_WEST, result)
    }

    @Test
    fun convertToStopOrientationWithNullValueMapsToUnknown() {
        val result = converter.convertToStopOrientation(null)

        assertEquals(StopOrientation.UNKNOWN, result)
    }

    @Test
    fun convertToStopOrientationWithInvalidValueMapsToUnknown() {
        val result = converter.convertToStopOrientation("invalid")

        assertEquals(StopOrientation.UNKNOWN, result)
    }
}
