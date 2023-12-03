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

import kotlinx.coroutines.flow.Flow

/**
 * This DAO is used to access service data in the bus stop database.
 *
 * @author Niall Scott
 */
interface ServiceDao {

    /**
     * A [Flow] which emits a [List] of all [ServiceWithColour], if available.
     */
    val allServiceNamesWithColourFlow: Flow<List<ServiceWithColour>?>

    /**
     * Get a [Flow] which emits a [List] of all [ServiceWithColour] for a given [stopCode], if
     * available.
     *
     * @return A [Flow] which emits a [List] of all [ServiceWithColour] for a given [stopCode], if
     * available.
     */
    fun getServiceNamesWithColourFlow(stopCode: String): Flow<List<ServiceWithColour>?>

    /**
     * A [Flow] which emits the number of services.
     */
    val serviceCountFlow: Flow<Int?>

    /**
     * Get a [Flow] which emits colours for services. If [services] is specified, then only the
     * given services will be returned, otherwise colours will be returned for all known services.
     *
     * @param services The services to get colours for, or `null` if colours for all services should
     * be returned.
     * @return A [Flow] which emits [Map]s where the service name is the key and the colour for the
     * service is the value. The [Flow] may emit `null` items.
     */
    fun getColoursForServicesFlow(services: Set<String>?): Flow<Map<String, Int?>?>

    /**
     * Get a [Flow] which emits [ServiceDetails] for the given [stopCode].
     *
     * @param stopCode The stop code to get [ServiceDetails] for.
     * @return A [Flow] which emits a [List] of [ServiceDetails] for the given stop code. May be
     * `null` if there are no known services for the given stop code, or if there was some other
     * issue getting the details.
     */
    fun getServiceDetailsFlow(stopCode: String): Flow<List<ServiceDetails>?>
}