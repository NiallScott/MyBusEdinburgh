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

package uk.org.rivernile.android.bustracker.core.location

/**
 * This class defines a location. A location can be made up of a [LatLon] plus other data, such as
 * speed, altitude etc.
 *
 * Implementation note: this is a value class for now. It may be made in to a data class later if
 * and when further fields are required.
 *
 * @property latLon The lat/lon coordinates which makes up this location.
 * @property horizontalAccuracy The horizontal accuracy of the location in meters - or `null` if
 * accuracy is unknown.
 * @author Niall Scott
 */
public data class Location(
    val latLon: LatLon,
    val horizontalAccuracy: Float?
)
