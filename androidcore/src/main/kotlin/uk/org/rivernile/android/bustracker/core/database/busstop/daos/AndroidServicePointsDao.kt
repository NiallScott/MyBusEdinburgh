/*
 * Copyright (C) 2022 Niall 'Rivernile' Scott
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
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import uk.org.rivernile.android.bustracker.core.database.busstop.ServicePointsContract
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.ServicePoint
import uk.org.rivernile.android.bustracker.core.di.ForIoDispatcher
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This is an Android concrete implementation of [ServicePointsDao].
 *
 * @param context The application [Context].
 * @param contract The database contract, so we know how to talk to it.
 * @param ioDispatcher The [CoroutineDispatcher] that database operations are performed on.
 * @author Niall Scott
 */
@Singleton
internal class AndroidServicePointsDao @Inject constructor(
        private val context: Context,
        private val contract: ServicePointsContract,
        @ForIoDispatcher private val ioDispatcher: CoroutineDispatcher) : ServicePointsDao {

    override fun getServicePointsFlow(serviceNames: Set<String>?) = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {

            override fun deliverSelfNotifications() = true

            override fun onChange(selfChange: Boolean) {
                launch {
                    getAndSendServicePoints(serviceNames)
                }
            }
        }

        context.contentResolver.registerContentObserver(
                contract.getContentUri(),
                true,
                observer)
        getAndSendServicePoints(serviceNames)

        awaitClose {
            context.contentResolver.unregisterContentObserver(observer)
        }
    }

    /**
     * Get all the [ServicePoint]s for the [serviceNames]s and send them to the [ProducerScope]'s
     * channel.
     *
     * @param serviceNames The service names to get the [ServicePoint]s for.
     */
    private suspend fun ProducerScope<List<ServicePoint>?>.getAndSendServicePoints(
            serviceNames: Set<String>?) {
        send(getServicePoints(serviceNames))
    }

    /**
     * Get a [List] of [ServicePoint]s for the given [serviceNames]. `null` may be omitted if
     * there are no results.
     *
     * @param serviceNames Only [ServicePoint]s for the supplied [Set] of service names are
     * returned. `null` means all [ServicePoint]s are returned - this could be an expensive
     * operation.
     * @return A [List] of [ServicePoint]s for the given [serviceNames].
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @VisibleForTesting
    suspend fun getServicePoints(serviceNames: Set<String>?): List<ServicePoint>? {
        val cancellationSignal = CancellationSignal()

        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation {
                    cancellationSignal.cancel()
                }

                val selection: String?
                val selectionArgs: Array<String>?

                if (!serviceNames.isNullOrEmpty()) {
                    selection = "${ServicePointsContract.SERVICE_NAME} IN (" +
                            "${generateInClausePlaceholders(serviceNames.size)})"
                    selectionArgs = serviceNames.toTypedArray()
                } else {
                    selection = null
                    selectionArgs = null
                }

                val sortClause = "${ServicePointsContract.SERVICE_NAME} ASC, " +
                        "${ServicePointsContract.CHAINAGE} ASC, " +
                        "${ServicePointsContract.ORDER_VALUE} ASC"

                val result = context.contentResolver.query(
                        contract.getContentUri(),
                        arrayOf(
                                ServicePointsContract.SERVICE_NAME,
                                ServicePointsContract.CHAINAGE,
                                ServicePointsContract.LATITUDE,
                                ServicePointsContract.LONGITUDE),
                        selection,
                        selectionArgs,
                        sortClause,
                        cancellationSignal)
                        ?.use {
                            // Fill the Cursor window.
                            val count = it.count

                            if (count > 0) {
                                val result = ArrayList<ServicePoint>(count)
                                val serviceNameColumn =
                                        it.getColumnIndexOrThrow(ServicePointsContract.SERVICE_NAME)
                                val chainageColumn =
                                        it.getColumnIndexOrThrow(ServicePointsContract.CHAINAGE)
                                val latitudeColumn =
                                        it.getColumnIndexOrThrow(ServicePointsContract.LATITUDE)
                                val longitudeColumn =
                                        it.getColumnIndexOrThrow(ServicePointsContract.LONGITUDE)

                                while (it.moveToNext()) {
                                    result.add(ServicePoint(
                                            it.getString(serviceNameColumn),
                                            it.getInt(chainageColumn),
                                            it.getDouble(latitudeColumn),
                                            it.getDouble(longitudeColumn)))
                                }

                                result
                            } else {
                                null
                            }
                        }

                continuation.resume(result, null)
            }
        }
    }

    /**
     * Generate placeholders for a `WHERE` clause that contains an `IN` element.
     *
     * @param length The number of placeholders required.
     * @return A [String] containing the required number of placeholders for an `IN` element.
     */
    private fun generateInClausePlaceholders(length: Int) =
            Array(length) { "?" }
                    .joinToString(",")
}