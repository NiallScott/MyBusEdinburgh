/*
 * Copyright (C) 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.busstopmap

/**
 * An enumeration of map types.
 *
 * @param value The value of the enum instance.
 * @author Niall Scott
 */
enum class MapType(val value: Int) {

    /** Normal map. */
    NORMAL(1),
    /** Satellite map. */
    SATELLITE(2),
    /** Hybrid map. */
    HYBRID(3);

    companion object {

        /**
         * Given an integer [value], convert this to a [MapType].
         *
         * @param value The integer value of the [MapType].
         * @return The [MapType] associated with this value. Returns [MapType.NORMAL] when the value
         * is unknown.
         */
        fun fromValue(value: Int) = values().firstOrNull {
            it.value == value
        } ?: NORMAL
    }
}