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

import androidx.annotation.FloatRange
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.log2

private const val BASE_ZOOM_METERS_PER_PIXEL_AT_EQUATOR = 156543.03392

/**
 * Calculate the zoom level required on a Google Maps object to fit a span of [spanMeters] so that
 * it entirely fits within the bounds, as defined by [smallestDimension] at the given [latitude],
 * plus adding in some padding as defined by [paddingFactor].
 *
 * This uses the Web Mercator projection formula.
 *
 * @param latitude The latitude the calculation should be performed at. The latitude matters as the
 * distance from the equator affects the resulting zoom level. This must be in the range of
 * -90.0..+90.0
 * @param smallestDimension The smallest dimension (smallest side) of the UI component this will be
 * rendered inside. This is to ensure that [spanMeters] fits entirely. This must be `>= 0`. If the
 * value is 0, then the result will be clamped at [minZoom].
 * @param spanMeters The number of meters of the span which should fit. This must be `> 0`.
 * @param paddingFactor This adds a padding factor to the calculation, but multiplying this value
 * with [spanMeters]. If a padding factor is not added then the zoom will fit [spanMeters]
 * almost perfectly. In some cases, such as drawing a circle, you may want to have padding between
 * the map edge and the circle - this value achieves this. Use the default value of `1f` if you do
 * not wish to add padding. This value must always be `> 0`.
 * @param minZoom The minimum zoom level to return. This defaults to `0f`. This must be `>= 0f`.
 * @param maxZoom The maximum zoom level to return. This defaults to [Float.MAX_VALUE]. This must be
 * `>= minZoom`.
 * @return The calculated zoom level which is clamped to be within [minZoom] and [maxZoom].
 * @throws IllegalArgumentException When any of the parameters are not within their acceptable
 * bounds. See the documentation for each parameter to determine the acceptable bounds.
 * @author Niall Scott
 */
public fun calculateZoom(
    @FloatRange(from = -90.0, to = 90.0) latitude: Double,
    smallestDimension: Dp,
    @FloatRange(from = 0.0, fromInclusive = false) spanMeters: Float,
    @FloatRange(from = 0.0, fromInclusive = false) paddingFactor: Float = 1f,
    @FloatRange(from = 0.0) minZoom: Float = 0f,
    @FloatRange(from = 0.0) maxZoom: Float = Float.MAX_VALUE
): Float {
    require(latitude in -90.0..90.0) { "latitude must be in the range of -90 to +90" }
    require(smallestDimension >= 0.dp) { "smallestDimension must be >= 0" }
    require(spanMeters > 0f) { "spanMeters must be > 0" }
    require(paddingFactor > 0f) { "paddingFactor must be > 0" }
    require(minZoom >= 0f) { "The minimum zoom must be >= 0" }
    require(maxZoom >= minZoom) { "The maximum zoom must be >= minZoom" }

    val zoom = log2(
        BASE_ZOOM_METERS_PER_PIXEL_AT_EQUATOR *
            cos(latitude.toRadians()) *
            smallestDimension.value /
            (spanMeters * paddingFactor)
    )

    return zoom
        .toFloat()
        .coerceIn(
            minimumValue = minZoom,
            maximumValue = maxZoom
        )
}

private fun Double.toRadians() = this / 180.0 * PI
