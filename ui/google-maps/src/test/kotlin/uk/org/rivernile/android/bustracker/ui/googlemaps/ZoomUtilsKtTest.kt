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

package uk.org.rivernile.android.bustracker.ui.googlemaps

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for `ZoomUtils.kt`.
 *
 * @author Niall Scott
 */
class ZoomUtilsKtTest {

    @Test(expected = IllegalArgumentException::class)
    fun calculateZoomThrowsIllegalArgumentExceptionWhenLatitudeIsTooSmall() {
        calculateZoom(
            latitude = -90.1,
            smallestDimension = 200.dp,
            spanMeters = 100f,
            paddingFactor = 1.2f,
            minZoom = 10f,
            maxZoom = 20f
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun calculateZoomThrowsIllegalArgumentExceptionWhenLatitudeIsTooLarge() {
        calculateZoom(
            latitude = 90.1,
            smallestDimension = 200.dp,
            spanMeters = 100f,
            paddingFactor = 1.2f,
            minZoom = 10f,
            maxZoom = 20f
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun calculateZoomThrowsIllegalArgumentExceptionWhenSmallestDimensionIsTooSmall() {
        calculateZoom(
            latitude = 1.23,
            smallestDimension = (-1).dp,
            spanMeters = 100f,
            paddingFactor = 1.2f,
            minZoom = 10f,
            maxZoom = 20f
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun calculateZoomThrowsIllegalArgumentExceptionWhenSpanMetersIsTooSmall() {
        calculateZoom(
            latitude = 1.23,
            smallestDimension = 200.dp,
            spanMeters = 0f,
            paddingFactor = 1.2f,
            minZoom = 10f,
            maxZoom = 20f
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun calculateZoomThrowsIllegalArgumentExceptionWhenPaddingFactorIsTooSmall() {
        calculateZoom(
            latitude = 1.23,
            smallestDimension = 200.dp,
            spanMeters = 100f,
            paddingFactor = 0f,
            minZoom = 10f,
            maxZoom = 20f
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun calculateZoomThrowsIllegalArgumentExceptionWhenMinZoomIsTooSmall() {
        calculateZoom(
            latitude = 1.23,
            smallestDimension = 200.dp,
            spanMeters = 100f,
            paddingFactor = 1.2f,
            minZoom = -1f,
            maxZoom = 20f
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun calculateZoomThrowsIllegalArgumentExceptionWhenMaxZoomIsLessThanMinZoom() {
        calculateZoom(
            latitude = 1.23,
            smallestDimension = 200.dp,
            spanMeters = 100f,
            paddingFactor = 0f,
            minZoom = 10f,
            maxZoom = 9f
        )
    }

    @Test
    fun calculateZoomClampsToMinZoomWhenCalculatedValueIsTooSmall() {
        val result = calculateZoom(
            latitude = 1.23,
            smallestDimension = 200.dp,
            spanMeters = Float.MAX_VALUE,
            paddingFactor = 1.2f,
            minZoom = 10f,
            maxZoom = 20f
        )

        assertEquals(10f, result)
    }

    @Test
    fun calculateZoomClampsToMaxZoomWhenCalculatedValueIsTooLarge() {
        val result = calculateZoom(
            latitude = 1.23,
            smallestDimension = 200.dp,
            spanMeters = 0.00001f,
            paddingFactor = 1.2f,
            minZoom = 10f,
            maxZoom = 20f
        )

        assertEquals(20f, result)
    }
}
