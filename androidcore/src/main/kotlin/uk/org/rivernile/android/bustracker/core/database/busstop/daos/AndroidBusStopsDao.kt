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

import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import uk.org.rivernile.android.bustracker.core.database.busstop.BusStopsContract
import uk.org.rivernile.android.bustracker.core.database.busstop.ServiceStopsContract
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopDetails
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopDetailsWithServices
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
                                it.getString(it.getColumnIndexOrThrow(BusStopsContract.STOP_NAME))
                                        ?.let { name ->
                                            val locality = it.getString(
                                                    it.getColumnIndexOrThrow(
                                                            BusStopsContract.LOCALITY))
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
                                val latitudeColumn =
                                        it.getColumnIndexOrThrow(BusStopsContract.LATITUDE)
                                val longitudeColumn =
                                        it.getColumnIndexOrThrow(BusStopsContract.LONGITUDE)

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
                                BusStopsContract.LONGITUDE,
                                BusStopsContract.ORIENTATION),
                        "${BusStopsContract.STOP_CODE} = ?",
                        arrayOf(stopCode),
                        null,
                        cancellationSignal)
                        ?.use {
                            // Fill the Cursor window.
                            it.count

                            if (it.moveToFirst()) {
                                val stopNameColumn =
                                        it.getColumnIndexOrThrow(BusStopsContract.STOP_NAME)
                                val localityColumn =
                                        it.getColumnIndexOrThrow(BusStopsContract.LOCALITY)
                                val latitudeColumn =
                                        it.getColumnIndexOrThrow(BusStopsContract.LATITUDE)
                                val longitudeColumn =
                                        it.getColumnIndexOrThrow(BusStopsContract.LONGITUDE)
                                val orientationColumn =
                                        it.getColumnIndexOrThrow(BusStopsContract.ORIENTATION)

                                StopDetails(
                                        stopCode,
                                        StopName(
                                                it.getString(stopNameColumn),
                                                it.getString(localityColumn)),
                                        it.getDouble(latitudeColumn),
                                        it.getDouble(longitudeColumn),
                                        it.getInt(orientationColumn))
                            } else {
                                null
                            }
                        }

                continuation.resume(result)
            }
        }
    }

    override suspend fun getStopDetails(stopCodes: Set<String>): Map<String, StopDetails>? {
        if (stopCodes.isEmpty()) {
            return null
        }

        val cancellationSignal = CancellationSignal()

        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation {
                    cancellationSignal.cancel()
                }

                val inPlaceholders = Array(stopCodes.size) { '?' }
                val result = context.contentResolver.query(
                        contract.getContentUri(),
                        arrayOf(
                                BusStopsContract.STOP_CODE,
                                BusStopsContract.STOP_NAME,
                                BusStopsContract.LOCALITY,
                                BusStopsContract.LATITUDE,
                                BusStopsContract.LONGITUDE,
                                BusStopsContract.ORIENTATION),
                        "${BusStopsContract.STOP_CODE} IN (${inPlaceholders.joinToString(",")})",
                        stopCodes.toTypedArray(),
                        null,
                        cancellationSignal)
                        ?.use {
                            val count = it.count

                            if (count > 0) {
                                val result = mutableMapOf<String, StopDetails>()
                                val stopCodeColumn =
                                        it.getColumnIndexOrThrow(BusStopsContract.STOP_CODE)
                                val stopNameColumn =
                                        it.getColumnIndexOrThrow(BusStopsContract.STOP_NAME)
                                val localityColumn =
                                        it.getColumnIndexOrThrow(BusStopsContract.LOCALITY)
                                val latitudeColumn =
                                        it.getColumnIndexOrThrow(BusStopsContract.LATITUDE)
                                val longitudeColumn =
                                        it.getColumnIndexOrThrow(BusStopsContract.LONGITUDE)
                                val orientationColumn =
                                        it.getColumnIndexOrThrow(BusStopsContract.ORIENTATION)

                                while (it.moveToNext()) {
                                    val stopCode = it.getString(stopCodeColumn)
                                    result[stopCode] = StopDetails(
                                            stopCode,
                                            StopName(
                                                    it.getString(stopNameColumn),
                                                    it.getString(localityColumn)),
                                            it.getDouble(latitudeColumn),
                                            it.getDouble(longitudeColumn),
                                            it.getInt(orientationColumn))
                                }

                                result
                            } else {
                                null
                            }
                        }

                continuation.resume(result)
            }
        }
    }

    override suspend fun getServicesForStops(stopCodes: Set<String>): Map<String, List<String>>? {
        if (stopCodes.isEmpty()) {
            return null
        }

        val cancellationSignal = CancellationSignal()

        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation {
                    cancellationSignal.cancel()
                }

                val inPlaceholders = Array(stopCodes.size) { '?' }
                val result = context.contentResolver.query(
                        contract.getContentUri(),
                        arrayOf(
                                BusStopsContract.STOP_CODE,
                                BusStopsContract.SERVICE_LISTING),
                        "${BusStopsContract.STOP_CODE} IN (${inPlaceholders.joinToString(",")})",
                        stopCodes.toTypedArray(),
                        null,
                        cancellationSignal)?.use {
                    val count = it.count

                    if (count > 0) {
                        val result = mutableMapOf<String, List<String>>()
                        val stopCodeColumn = it.getColumnIndexOrThrow(BusStopsContract.STOP_CODE)
                        val serviceListingColumn =
                                it.getColumnIndexOrThrow(BusStopsContract.SERVICE_LISTING)

                        while (it.moveToNext()) {
                            val stopCode = it.getString(stopCodeColumn)
                            val serviceListing = it.getString(serviceListingColumn)

                            serviceListing?.split(',')
                                    ?.mapNotNull { name ->
                                        name.trim().ifEmpty { null }
                                    }
                                    ?.ifEmpty { null }
                                    ?.let { names ->
                                        result.put(stopCode, names)
                                    }
                        }

                        result
                    } else {
                        null
                    }
                }

                continuation.resume(result)
            }
        }
    }

    @ExperimentalCoroutinesApi
    override fun getStopDetailsWithinSpanFlow(
            minLatitude: Double,
            minLongitude: Double,
            maxLatitude: Double,
            maxLongitude: Double) = callbackFlow {
        val listener = object : BusStopsDao.OnBusStopsChangedListener {
            override fun onBusStopsChanged() {
                launch {
                    getAndSendStopDetailsWithinSpanFlow(
                            minLatitude,
                            minLongitude,
                            maxLatitude,
                            maxLongitude)
                }
            }
        }

        addOnBusStopsChangedListener(listener)
        getAndSendStopDetailsWithinSpanFlow(
                minLatitude,
                minLongitude,
                maxLatitude,
                maxLongitude)

        awaitClose {
            removeOnBusStopsChangedListener(listener)
        }
    }

    @ExperimentalCoroutinesApi
    override fun getStopDetailsWithinSpanFlow(
            minLatitude: Double,
            minLongitude: Double,
            maxLatitude: Double,
            maxLongitude: Double,
            serviceFilter: List<String>) = callbackFlow {
        val listener = object : BusStopsDao.OnBusStopsChangedListener {
            override fun onBusStopsChanged() {
                launch {
                    getAndSendStopDetailsWithinSpanFlow(
                            minLatitude,
                            minLongitude,
                            maxLatitude,
                            maxLongitude,
                            serviceFilter)
                }
            }
        }

        addOnBusStopsChangedListener(listener)
        getAndSendStopDetailsWithinSpanFlow(
                minLatitude,
                minLongitude,
                maxLatitude,
                maxLongitude,
                serviceFilter)

        awaitClose {
            removeOnBusStopsChangedListener(listener)
        }
    }

    /**
     * Get a [List] of [StopDetailsWithServices]s from the database using the parameters as the
     * filter and send it to the channel.
     *
     * @param minLatitude The minimum latitude.
     * @param minLongitude The minimum longitude.
     * @param maxLatitude The maximum latitude.
     * @param maxLongitude The maximum longitude.
     */
    @ExperimentalCoroutinesApi
    private suspend fun ProducerScope<List<StopDetailsWithServices>?>.
            getAndSendStopDetailsWithinSpanFlow(
            minLatitude: Double,
            minLongitude: Double,
            maxLatitude: Double,
            maxLongitude: Double) {
        channel.send(getStopDetailsWithinSpan(
                minLatitude,
                minLongitude,
                maxLatitude,
                maxLongitude))
    }

    /**
     * Get a [List] of [StopDetailsWithServices]s from the database using the parameters as the
     * filter and send it to the channel.
     *
     * @param minLatitude The minimum latitude.
     * @param minLongitude The minimum longitude.
     * @param maxLatitude The maximum latitude.
     * @param maxLongitude The maximum longitude.
     * @param serviceFilter Service names to filter the results on.
     */
    @ExperimentalCoroutinesApi
    private suspend fun ProducerScope<List<StopDetailsWithServices>?>.
            getAndSendStopDetailsWithinSpanFlow(
            minLatitude: Double,
            minLongitude: Double,
            maxLatitude: Double,
            maxLongitude: Double,
            serviceFilter: List<String>) {
        channel.send(getStopDetailsWithinSpan(
                minLatitude,
                minLongitude,
                maxLatitude,
                maxLongitude,
                serviceFilter))
    }

    /**
     * Query the database to return a [List] of [StopDetailsWithServices] based on the given filter
     * parameters. Will return `null` when there are no results.
     *
     * @param minLatitude The minimum latitude.
     * @param minLongitude The minimum longitude.
     * @param maxLatitude The maximum latitude.
     * @param maxLongitude The maximum longitude.
     * @return A [List] of [StopDetailsWithServices] objects, or `null`.
     */
    @VisibleForTesting
    suspend fun getStopDetailsWithinSpan(
            minLatitude: Double,
            minLongitude: Double,
            maxLatitude: Double,
            maxLongitude: Double): List<StopDetailsWithServices>? {
        val cancellationSignal = CancellationSignal()

        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation {
                    cancellationSignal.cancel()
                }

                val selection = "(${BusStopsContract.LATITUDE} BETWEEN ? AND ?) AND " +
                        "(${BusStopsContract.LONGITUDE} BETWEEN ? AND ?)"

                val result = context.contentResolver.query(
                        contract.getContentUri(),
                        arrayOf(
                                BusStopsContract.STOP_CODE,
                                BusStopsContract.STOP_NAME,
                                BusStopsContract.LOCALITY,
                                BusStopsContract.LATITUDE,
                                BusStopsContract.LONGITUDE,
                                BusStopsContract.ORIENTATION,
                                BusStopsContract.SERVICE_LISTING),
                        selection,
                        arrayOf(
                                minLatitude.toString(),
                                maxLatitude.toString(),
                                minLongitude.toString(),
                                maxLongitude.toString()),
                        null,
                        cancellationSignal)
                        ?.use(this@AndroidBusStopsDao::mapCursorToStopDetailsWithServicesListing)

                continuation.resume(result)
            }
        }
    }

    /**
     * Query the database to return a [List] of [StopDetailsWithServices] based on the given filter
     * parameters. Will return `null` when there are no results.
     *
     * @param minLatitude The minimum latitude.
     * @param minLongitude The minimum longitude.
     * @param maxLatitude The maximum latitude.
     * @param maxLongitude The maximum longitude.
     * @param serviceFilter A [List] of service names to filter results by.
     * @return A [List] of [StopDetailsWithServices] objects, or `null`.
     */
    @VisibleForTesting
    suspend fun getStopDetailsWithinSpan(
            minLatitude: Double,
            minLongitude: Double,
            maxLatitude: Double,
            maxLongitude: Double,
            serviceFilter: List<String>): List<StopDetailsWithServices>? {
        val cancellationSignal = CancellationSignal()

        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation {
                    cancellationSignal.cancel()
                }

                val inPlaceholders = Array(serviceFilter.size) { '?' }
                val selection = "(${BusStopsContract.LATITUDE} BETWEEN ? AND ?) AND " +
                        "(${BusStopsContract.LONGITUDE} BETWEEN ? AND ?) " +
                        "AND ${BusStopsContract.STOP_CODE} IN (" +
                        "SELECT ${ServiceStopsContract.STOP_CODE} " +
                        "FROM ${ServiceStopsContract.TABLE_NAME} " +
                        "WHERE ${ServiceStopsContract.SERVICE_NAME} IN (" +
                        "${inPlaceholders.joinToString(",")}))"

                val selectionArgs = arrayOf(
                        minLatitude.toString(),
                        maxLatitude.toString(),
                        minLongitude.toString(),
                        maxLongitude.toString()) + serviceFilter

                val result = context.contentResolver.query(
                        contract.getContentUri(),
                        arrayOf(
                                BusStopsContract.STOP_CODE,
                                BusStopsContract.STOP_NAME,
                                BusStopsContract.LOCALITY,
                                BusStopsContract.LATITUDE,
                                BusStopsContract.LONGITUDE,
                                BusStopsContract.ORIENTATION,
                                BusStopsContract.SERVICE_LISTING),
                        selection,
                        selectionArgs,
                        null,
                        cancellationSignal)
                        ?.use(this@AndroidBusStopsDao::mapCursorToStopDetailsWithServicesListing)

                continuation.resume(result)
            }
        }
    }

    /**
     * Given a [Cursor], map its contents to a [List] of [StopDetailsWithServices].
     *
     * @param cursor The [Cursor] to map.
     * @return A [List] of [StopDetailsWithServices] objects when the mapping was successful and has
     * items, otherwise `null`.
     */
    private fun mapCursorToStopDetailsWithServicesListing(
            cursor: Cursor): List<StopDetailsWithServices>? {
        // Fill Cursor window.
        val count = cursor.count

        return if (count > 0) {
            val result = mutableListOf<StopDetailsWithServices>()
            val stopCodeColumn = cursor.getColumnIndexOrThrow(BusStopsContract.STOP_CODE)
            val stopNameColumn = cursor.getColumnIndexOrThrow(BusStopsContract.STOP_NAME)
            val localityColumn = cursor.getColumnIndexOrThrow(BusStopsContract.LOCALITY)
            val latitudeColumn = cursor.getColumnIndexOrThrow(BusStopsContract.LATITUDE)
            val longitudeColumn = cursor.getColumnIndexOrThrow(BusStopsContract.LONGITUDE)
            val orientationColumn = cursor.getColumnIndexOrThrow(BusStopsContract.ORIENTATION)
            val serviceListingColumn =
                    cursor.getColumnIndexOrThrow(BusStopsContract.SERVICE_LISTING)

            while (cursor.moveToNext()) {
                result += StopDetailsWithServices(
                        cursor.getString(stopCodeColumn),
                        StopName(
                                cursor.getString(stopNameColumn),
                                cursor.getString(localityColumn)),
                        cursor.getDouble(latitudeColumn),
                        cursor.getDouble(longitudeColumn),
                        cursor.getInt(orientationColumn),
                        cursor.getString(serviceListingColumn))
            }

            result
        } else {
            null
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