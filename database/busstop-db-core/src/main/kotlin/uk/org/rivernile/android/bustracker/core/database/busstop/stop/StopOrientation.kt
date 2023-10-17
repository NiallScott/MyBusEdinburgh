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

package uk.org.rivernile.android.bustracker.core.database.busstop.stop

/**
 * This enum represents the orientation of a stop (the direction of travel).
 *
 * @author Niall Scott
 */
enum class StopOrientation {

    /**
     * The stop is facing north.
     */
    NORTH,
    /**
     * The stop is facing north-east.
     */
    NORTH_EAST,
    /**
     * The stop is facing east.
     */
    EAST,
    /**
     * The stop is facing south-east.
     */
    SOUTH_EAST,
    /**
     * The stop is facing south.
     */
    SOUTH,
    /**
     * The stop is facing south-west.
     */
    SOUTH_WEST,
    /**
     * The stop is facing west.
     */
    WEST,
    /**
     * The stop is facing north-west.
     */
    NORTH_WEST,
    /**
     * The stop orientation is unknown.
     */
    UNKNOWN
}