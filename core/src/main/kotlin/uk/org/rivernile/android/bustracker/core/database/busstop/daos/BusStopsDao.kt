/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.busstop.daos

import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopLocation
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopName

/**
 * This DAO is used to access bus stops.
 *
 * @author Niall Scott
 */
interface BusStopsDao {

    /**
     * Given a stop code, get the name for this stop.
     *
     * @param stopCode The stop to get the name for.
     * @return The name of the stop, or `null` if the name is not known or the stop cannot be found.
     */
    fun getNameForStop(stopCode: String): StopName?

    /**
     * Given a stop code, get the latitude and longitude for this stop.
     *
     * @param stopCode The stop to get the location for.
     * @return The location of the stop, or `null` if the stop is not found.
     */
    fun getLocationForStop(stopCode: String): StopLocation?
}