/*
 * Copyright (C) 2021 Niall 'Rivernile' Scott
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

/**
 * This DAO is used to access service stops.
 *
 * @author Niall Scott
 */
interface ServiceStopsDao {

    /**
     * Add a new [OnServiceStopsChangedListener] to be informed when the service stops data has
     * changed.
     *
     * @param listener The listener to add.
     */
    fun addOnServiceStopsChangedListener(listener: OnServiceStopsChangedListener)

    /**
     * Remove a [OnServiceStopsChangedListener] so it is no longer informed that service stops data
     * has changed.
     *
     * @param listener The listener to remove.
     */
    fun removeOnServiceStopsChangedListener(listener: OnServiceStopsChangedListener)

    /**
     * Given a stop code, get the services which serve this stop.
     *
     * @param stopCode The stop code to get services for.
     * @return The services which serve this stop. This may return `null` if there is no data.
     */
    suspend fun getServicesForStop(stopCode: String): List<String>?

    /**
     * This interface should be implemented to listen for changes to the service stops. Call
     * [addOnServiceStopsChangedListener] to register the listener.
     */
    interface OnServiceStopsChangedListener {

        /**
         * This is called when service stop data has changed.
         */
        fun onServiceStopsChanged()
    }
}