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

import androidx.room.TypeConverter

/**
 * This class contains [TypeConverter]s for stops.
 *
 * @author Niall Scott
 */
internal class StopTypeConverter {

    /**
     * Convert a [String] value representing the stop bearing to the [StopOrientation] enum
     * value.
     *
     * @param value The [String] value from the database.
     * @return The value mapped to its [StopOrientation] value.
     */
    @TypeConverter
    fun convertToStopOrientation(value: String?) = when (value?.uppercase()) {
        "N" -> StopOrientation.NORTH
        "NE" -> StopOrientation.NORTH_EAST
        "E" -> StopOrientation.EAST
        "SE" -> StopOrientation.SOUTH_EAST
        "S" -> StopOrientation.SOUTH
        "SW" -> StopOrientation.SOUTH_WEST
        "W" -> StopOrientation.WEST
        "NW" -> StopOrientation.NORTH_WEST
        else -> StopOrientation.UNKNOWN
    }
}
