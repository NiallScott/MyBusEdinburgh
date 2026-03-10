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

/**
 * Tests for `StopOrientationExtensions.kt`.
 *
 * @author Niall Scott
 */
class StopOrientationExtensionsKtTest {

    @Test
    fun toIconDrawableResIdMapsNorth() {
        assertEquals(R.drawable.mapmarker_n, StopOrientation.NORTH.toIconDrawableResId())
    }

    @Test
    fun toIconDrawableResIdMapsNorthEast() {
        assertEquals(R.drawable.mapmarker_ne, StopOrientation.NORTH_EAST.toIconDrawableResId())
    }

    @Test
    fun toIconDrawableResIdMapsEast() {
        assertEquals(R.drawable.mapmarker_e, StopOrientation.EAST.toIconDrawableResId())
    }

    @Test
    fun toIconDrawableResIdMapsSouthEast() {
        assertEquals(R.drawable.mapmarker_se, StopOrientation.SOUTH_EAST.toIconDrawableResId())
    }

    @Test
    fun toIconDrawableResIdMapsSouth() {
        assertEquals(R.drawable.mapmarker_s, StopOrientation.SOUTH.toIconDrawableResId())
    }

    @Test
    fun toIconDrawableResIdMapsSouthWest() {
        assertEquals(R.drawable.mapmarker_sw, StopOrientation.SOUTH_WEST.toIconDrawableResId())
    }

    @Test
    fun toIconDrawableResIdMapsWest() {
        assertEquals(R.drawable.mapmarker_w, StopOrientation.WEST.toIconDrawableResId())
    }

    @Test
    fun toIconDrawableResIdMapsNorthWest() {
        assertEquals(R.drawable.mapmarker_nw, StopOrientation.NORTH_WEST.toIconDrawableResId())
    }

    @Test
    fun toIconDrawableResIdMapsUnknown() {
        assertEquals(R.drawable.mapmarker, StopOrientation.UNKNOWN.toIconDrawableResId())
    }

    @Test
    fun toContentDescriptionStringResIdMapsNorth() {
        assertEquals(
            R.string.stop_marker_content_description_n,
            StopOrientation.NORTH.toContentDescriptionStringResId()
        )
    }

    @Test
    fun toContentDescriptionStringResIdMapsNorthEast() {
        assertEquals(
            R.string.stop_marker_content_description_ne,
            StopOrientation.NORTH_EAST.toContentDescriptionStringResId()
        )
    }

    @Test
    fun toContentDescriptionStringResIdMapsEast() {
        assertEquals(
            R.string.stop_marker_content_description_e,
            StopOrientation.EAST.toContentDescriptionStringResId()
        )
    }

    @Test
    fun toContentDescriptionStringResIdMapsSouthEast() {
        assertEquals(
            R.string.stop_marker_content_description_se,
            StopOrientation.SOUTH_EAST.toContentDescriptionStringResId()
        )
    }

    @Test
    fun toContentDescriptionStringResIdMapsSouth() {
        assertEquals(
            R.string.stop_marker_content_description_s,
            StopOrientation.SOUTH.toContentDescriptionStringResId()
        )
    }

    @Test
    fun toContentDescriptionStringResIdMapsSouthWest() {
        assertEquals(
            R.string.stop_marker_content_description_sw,
            StopOrientation.SOUTH_WEST.toContentDescriptionStringResId()
        )
    }

    @Test
    fun toContentDescriptionStringResIdMapsWest() {
        assertEquals(
            R.string.stop_marker_content_description_w,
            StopOrientation.WEST.toContentDescriptionStringResId()
        )
    }

    @Test
    fun toContentDescriptionStringResIdMapsNorthWest() {
        assertEquals(
            R.string.stop_marker_content_description_nw,
            StopOrientation.NORTH_WEST.toContentDescriptionStringResId()
        )
    }

    @Test
    fun toContentDescriptionStringResIdMapsUnknown() {
        assertEquals(
            R.string.stop_marker_content_description_unknown,
            StopOrientation.UNKNOWN.toContentDescriptionStringResId()
        )
    }
}
