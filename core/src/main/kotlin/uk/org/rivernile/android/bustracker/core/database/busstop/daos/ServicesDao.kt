/*
 * Copyright (C) 2020 - 2022 Niall 'Rivernile' Scott
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

import kotlinx.coroutines.flow.Flow
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.ServiceDetails

/**
 * This DAO is used to access services.
 *
 * @author Niall Scott
 */
interface ServicesDao {

    /**
     * Add a new [OnServicesChangedListener] to be informed when the services data has changed.
     *
     * @param listener The listener to add.
     */
    fun addOnServicesChangedListener(listener: OnServicesChangedListener)

    /**
     * Remove a [OnServicesChangedListener] so it is no longer informed that services data has
     * changed.
     *
     * @param listener The listener to remove.
     */
    fun removeOnServicesChangedListener(listener: OnServicesChangedListener)

    /**
     * Get colours for services. If [services] is specified, then only the given services will be
     * returned, otherwise colours will be returned for all known services.
     *
     * @param services The services to get colours for, or `null` if colours for all services should
     * be returned.
     * @return A [Map] where the service name is the key and the colour for the service is the
     * value. May be `null`.
     */
    suspend fun getColoursForServices(services: Set<String>?): Map<String, Int>?

    /**
     * Get [ServiceDetails] for the given [services].
     *
     * @param services The services to get [ServiceDetails]s for.
     * @return A [Map] of service name to [ServiceDetails]. May be `null` if [services] is empty,
     * or if there are no items, or if there was some other issue getting the details.
     */
    suspend fun getServiceDetails(services: Set<String>): Map<String, ServiceDetails>?

    /**
     * This is a [Flow] which emits a [List] of the names of all known services.
     */
    val allServiceNamesFlow: Flow<List<String>?>

    /**
     * This interface should be implemented to listen for changes to services. Call
     * [addOnServicesChangedListener] to register the listener.
     */
    interface OnServicesChangedListener {

        /**
         * This is called when services data has changed.
         */
        fun onServicesChanged()
    }
}