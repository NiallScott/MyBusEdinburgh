/*
 * Copyright (C) 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.busstop.service

import android.graphics.Color
import androidx.room.TypeConverter

/**
 * This [TypeConverter] is used to convert colours in the database (stored in hex representation) in
 * to Android's colour integer representation.
 *
 * @author Niall Scott
 */
internal class ColourTypeConverter {

    /**
     * Convert a colour hex [String] in to an integer representing the colour. This will return
     * `null` if [hexColour] is `null`, empty or could not be parsed.
     *
     * @param hexColour A [String] representing the colour in hex representation.
     * @return The [hexColour] as a colour integer, or `null` if [hexColour] is null, empty or could
     * not be parsed.
     */
    @TypeConverter
    fun convertToColourInt(hexColour: String?): Int? {
        return hexColour
            ?.ifBlank { null }
            ?.let {
                try {
                    Color.parseColor(it)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
    }
}