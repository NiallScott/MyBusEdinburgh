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

package uk.org.rivernile.android.bustracker.core.database.busstop.servicestop

import kotlinx.coroutines.flow.Flow

/**
 * This DAO is used to access service stop data in the bus stop database.
 *
 * @author Niall Scott
 */
interface ServiceStopDao {

    /**
     * Get a [Flow] which emits [List]s of services which stop at the given [stopCode].
     *
     * @param stopCode The stop code to get services for.
     * @return A [Flow] which emits [List]s of services which stop at the given [stopCode].
     */
    fun getServicesForStopFlow(stopCode: String): Flow<List<String>?>

    /**
     * Given a [Set] of stop codes, get a [Flow] which emits a [Map] containing the mappings between
     * the stop code and the [List] of services for that particular stop.
     *
     * @param stopCodes The stop codes to get services for.
     * @return A [Flow] which emits a [Map] containing the mappings between
     * the stop code and the [List] of services for that particular stop, or emits `null` when there
     * is no data or there is an error.
     */
    fun getServicesForStopsFlow(stopCodes: Set<String>): Flow<Map<String, List<String>>?>
}