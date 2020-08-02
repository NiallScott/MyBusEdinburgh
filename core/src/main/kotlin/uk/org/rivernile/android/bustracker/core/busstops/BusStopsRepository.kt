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

package uk.org.rivernile.android.bustracker.core.busstops

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.org.rivernile.android.bustracker.core.database.busstop.daos.BusStopsDao
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopDetails
import uk.org.rivernile.android.bustracker.core.di.ForIoDispatcher
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This repository is used to access bus stop data.
 *
 * @param busStopsDao The DAO to access the bus stop data store.
 * @param ioDispatcher The [CoroutineDispatcher] to perform IO operations on.
 * @author Niall Scott
 */
@Singleton
class BusStopsRepository @Inject internal constructor(
        private val busStopsDao: BusStopsDao,
        @ForIoDispatcher private val ioDispatcher: CoroutineDispatcher) {

    /**
     * Get a [Flow] which returns [StopDetails] for the given `stopCode`. If stop details are
     * updated later, these will be be emitted.
     *
     * @param stopCode The stop code to get details for.
     * @return The [Flow] which emits [StopDetails] for the given stop code.
     */
    @ExperimentalCoroutinesApi
    fun getBusStopDetailsFlow(stopCode: String): Flow<StopDetails?> = callbackFlow {
        val listener = object : BusStopsDao.OnBusStopsChangedListener {
            override fun onBusStopsChanged() {
                launch {
                    getAndSendStopDetails(channel, stopCode)
                }
            }
        }

        busStopsDao.addOnBusStopsChangedListener(listener)
        getAndSendStopDetails(channel, stopCode)

        awaitClose {
            busStopsDao.removeOnBusStopsChangedListener(listener)
        }
    }

    /**
     * A suspended function which gets [StopDetails] for the given `stopCode` and sends these
     * details to the given `channel`. This might send `null` to the channel when no details were
     * found for the given `stopCode`.
     *
     * @param channel The [SendChannel] that emissions should be sent to.
     * @param stopCode The `stopCode` to obtain [StopDetails] for.
     */
    private suspend fun getAndSendStopDetails(
            channel: SendChannel<StopDetails?>,
            stopCode: String) = withContext(ioDispatcher) {
        channel.send(busStopsDao.getStopDetails(stopCode))
    }
}