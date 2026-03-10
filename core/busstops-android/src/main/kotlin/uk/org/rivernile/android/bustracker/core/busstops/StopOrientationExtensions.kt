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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * Maps this [StopOrientation] to a suitable marker icon.
 *
 * @return A drawable resource ID suitable for this [StopOrientation].
 */
@DrawableRes
public fun StopOrientation.toIconDrawableResId(): Int = when (this) {
    StopOrientation.NORTH -> R.drawable.mapmarker_n
    StopOrientation.NORTH_EAST -> R.drawable.mapmarker_ne
    StopOrientation.EAST -> R.drawable.mapmarker_e
    StopOrientation.SOUTH_EAST -> R.drawable.mapmarker_se
    StopOrientation.SOUTH -> R.drawable.mapmarker_s
    StopOrientation.SOUTH_WEST -> R.drawable.mapmarker_sw
    StopOrientation.WEST -> R.drawable.mapmarker_w
    StopOrientation.NORTH_WEST -> R.drawable.mapmarker_nw
    StopOrientation.UNKNOWN -> R.drawable.mapmarker
}

/**
 * Maps this [StopOrientation] to a suitable string for content description.
 *
 * @return A string resource ID suitable for this [StopOrientation].
 */
@StringRes
public fun StopOrientation.toContentDescriptionStringResId(): Int = when (this) {
    StopOrientation.NORTH -> R.string.stop_marker_content_description_n
    StopOrientation.NORTH_EAST -> R.string.stop_marker_content_description_ne
    StopOrientation.EAST -> R.string.stop_marker_content_description_e
    StopOrientation.SOUTH_EAST -> R.string.stop_marker_content_description_se
    StopOrientation.SOUTH -> R.string.stop_marker_content_description_s
    StopOrientation.SOUTH_WEST -> R.string.stop_marker_content_description_sw
    StopOrientation.WEST -> R.string.stop_marker_content_description_w
    StopOrientation.NORTH_WEST -> R.string.stop_marker_content_description_nw
    StopOrientation.UNKNOWN -> R.string.stop_marker_content_description_unknown
}
