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

package uk.org.rivernile.android.bustracker.core.servicestops

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import uk.org.rivernile.android.bustracker.core.database.busstop.daos.ServiceStopsDao
import javax.inject.Inject

/**
 * This repository is used to access service stops data.
 *
 * @param serviceStopsDao The DAO to access the service stops data store.
 * @author Niall Scott
 */
class ServiceStopsRepository @Inject internal constructor(
        private val serviceStopsDao: ServiceStopsDao) {

    /**
     * Get a [Flow] which emits the [List] of service names which serve the given `stopCode`. If the
     * services for the stop is updated later, these will be emitted as updates.
     *
     * @param stopCode The stop code to get the services for.
     * @return The [Flow] which emits the [List] of services for the given stop code.
     */
    @ExperimentalCoroutinesApi
    fun getServicesForStopFlow(stopCode: String): Flow<List<String>?> = callbackFlow {
        val listener = object: ServiceStopsDao.OnServiceStopsChangedListener {
            override fun onServiceStopsChanged() {
                launch {
                    getAndSendServicesForStop(channel, stopCode)
                }
            }
        }

        serviceStopsDao.addOnServiceStopsChangedListener(listener)
        getAndSendServicesForStop(channel, stopCode)

        awaitClose {
            serviceStopsDao.removeOnServiceStopsChangedListener(listener)
        }
    }

    /**
     * A suspended function which gets the [List] of services for the given `stopCode` and sends
     * these details to the given `channel`. This might send `null` to the channel when no details
     * were found for the given `stopCode`.
     *
     * @param channel The [SendChannel] that emissions should be sent to.
     * @param stopCode The `stopCode` to obtain the [List] of services for.
     */
    private suspend fun getAndSendServicesForStop(
            channel: SendChannel<List<String>?>,
            stopCode: String) {
        channel.send(serviceStopsDao.getServicesForStop(stopCode))
    }
}