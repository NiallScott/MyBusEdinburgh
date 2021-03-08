/*
 * Copyright (C) 2020 - 2021 Niall 'Rivernile' Scott
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

import android.content.Context
import android.database.ContentObserver
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import uk.org.rivernile.android.bustracker.core.database.busstop.BusStopsContract
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopDetails
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopLocation
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopName
import uk.org.rivernile.android.bustracker.core.di.ForIoDispatcher
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * This is an Android concrete implementation of the [BusStopsDao].
 *
 * @param context The application [Context].
 * @param contract The database contract, so we know how to talk to it.
 * @param ioDispatcher The [CoroutineDispatcher] that database operations are performed on.
 * @author Niall Scott
 */
@Singleton
internal class AndroidBusStopsDao @Inject constructor(
        private val context: Context,
        private val contract: BusStopsContract,
        @ForIoDispatcher private val ioDispatcher: CoroutineDispatcher): BusStopsDao {

    private val listeners = mutableListOf<BusStopsDao.OnBusStopsChangedListener>()
    private val observer = Observer()

    override fun addOnBusStopsChangedListener(listener: BusStopsDao.OnBusStopsChangedListener) {
        synchronized(listeners) {
            listeners.apply {
                add(listener)

                if (size == 1) {
                    context.contentResolver.registerContentObserver(contract.getContentUri(), true,
                            observer)
                }
            }
        }
    }

    override fun removeOnBusStopsChangedListener(listener: BusStopsDao.OnBusStopsChangedListener) {
        synchronized(listeners) {
            listeners.apply {
                remove(listener)

                if (isEmpty()) {
                    context.contentResolver.unregisterContentObserver(observer)
                }
            }
        }
    }

    override suspend fun getNameForStop(stopCode: String): StopName? {
        val cancellationSignal = CancellationSignal()

        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation {
                    cancellationSignal.cancel()
                }

                val result = context.contentResolver.query(
                        contract.getContentUri(),
                        arrayOf(
                                BusStopsContract.STOP_NAME,
                                BusStopsContract.LOCALITY),
                        "${BusStopsContract.STOP_CODE} = ?",
                        arrayOf(stopCode),
                        null,
                        cancellationSignal)
                        ?.use {
                            // Fill the Cursor window.
                            it.count

                            if (it.moveToFirst()) {
                                it.getString(it.getColumnIndex(BusStopsContract.STOP_NAME))
                                        ?.let { name ->
                                            val locality = it.getString(
                                                    it.getColumnIndex(BusStopsContract.LOCALITY))
                                            StopName(name, locality)
                                        }
                            } else {
                                null
                            }
                        }

                continuation.resume(result)
            }
        }
    }

    override suspend fun getLocationForStop(stopCode: String): StopLocation? {
        val cancellationSignal = CancellationSignal()

        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation {
                    cancellationSignal.cancel()
                }

                val result = context.contentResolver.query(
                        contract.getContentUri(),
                        arrayOf(
                                BusStopsContract.LATITUDE,
                                BusStopsContract.LONGITUDE),
                        "${BusStopsContract.STOP_CODE} = ?",
                        arrayOf(stopCode),
                        null,
                        cancellationSignal)
                        ?.use {
                            // Fill the Cursor window.
                            it.count

                            if (it.moveToFirst()) {
                                val latitudeColumn = it.getColumnIndex(BusStopsContract.LATITUDE)
                                val longitudeColumn = it.getColumnIndex(BusStopsContract.LONGITUDE)

                                StopLocation(stopCode, it.getDouble(latitudeColumn),
                                        it.getDouble(longitudeColumn))
                            } else {
                                null
                            }
                        }

                continuation.resume(result)
            }
        }
    }

    override suspend fun getStopDetails(stopCode: String): StopDetails? {
        val cancellationSignal = CancellationSignal()

        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation {
                    cancellationSignal.cancel()
                }

                val result = context.contentResolver.query(
                        contract.getContentUri(),
                        arrayOf(
                                BusStopsContract.STOP_NAME,
                                BusStopsContract.LOCALITY,
                                BusStopsContract.LATITUDE,
                                BusStopsContract.LONGITUDE),
                        "${BusStopsContract.STOP_CODE} = ?", arrayOf(stopCode),
                        null,
                        cancellationSignal)
                        ?.use {
                            // Fill the Cursor window.
                            it.count

                            if (it.moveToFirst()) {
                                val stopNameColumn = it.getColumnIndex(BusStopsContract.STOP_NAME)
                                val localityColumn = it.getColumnIndex(BusStopsContract.LOCALITY)
                                val latitudeColumn = it.getColumnIndex(BusStopsContract.LATITUDE)
                                val longitudeColumn = it.getColumnIndex(BusStopsContract.LONGITUDE)

                                StopDetails(
                                        stopCode,
                                        StopName(
                                                it.getString(stopNameColumn),
                                                it.getString(localityColumn)),
                                        it.getDouble(latitudeColumn),
                                        it.getDouble(longitudeColumn))
                            } else {
                                null
                            }
                        }

                continuation.resume(result)
            }
        }
    }

    /**
     * For all of the currently registers listeners, dispatch a data change event to them.
     */
    private fun dispatchOnBusStopsChangedListeners() {
        synchronized(listeners) {
            listeners.forEach { listener ->
                listener.onBusStopsChanged()
            }
        }
    }

    /**
     * This inner class is used as the [ContentObserver] for observing changes to bus stop data.
     */
    private inner class Observer : ContentObserver(Handler(Looper.getMainLooper())) {

        override fun deliverSelfNotifications() = true

        override fun onChange(selfChange: Boolean) {
            dispatchOnBusStopsChangedListeners()
        }
    }
}