/*
 * Copyright (C) 2021 - 2023 Niall 'Rivernile' Scott
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
import android.os.OperationCanceledException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import uk.org.rivernile.android.bustracker.core.database.busstop.ServiceStopsContract
import uk.org.rivernile.android.bustracker.core.di.ForIoDispatcher
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * This is an Android concrete implementation of the [ServiceStopsDao].
 *
 * @param context The application [Context].
 * @param contract The database contract, so we know how to talk to it.
 * @param ioDispatcher The [CoroutineDispatcher] that databases operations are performed on.
 * @author Niall Scott
 */
@Singleton
internal class AndroidServiceStopsDao @Inject constructor(
        private val context: Context,
        private val contract: ServiceStopsContract,
        @ForIoDispatcher private val ioDispatcher: CoroutineDispatcher) : ServiceStopsDao {

    companion object {

        private const val SERVICE_SORT_CLAUSE =
                "CASE WHEN ${ServiceStopsContract.SERVICE_NAME} GLOB '[^0-9.]*' THEN " +
                        "${ServiceStopsContract.SERVICE_NAME} ELSE " +
                        "cast(${ServiceStopsContract.SERVICE_NAME} AS int) END"
    }

    private val listeners = mutableListOf<ServiceStopsDao.OnServiceStopsChangedListener>()
    private val observer = Observer()

    override fun addOnServiceStopsChangedListener(
            listener: ServiceStopsDao.OnServiceStopsChangedListener) {
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

    override fun removeOnServiceStopsChangedListener(
            listener: ServiceStopsDao.OnServiceStopsChangedListener) {
        synchronized(listeners) {
            listeners.apply {
                remove(listener)

                if (isEmpty()) {
                    context.contentResolver.unregisterContentObserver(observer)
                }
            }
        }
    }

    override suspend fun getServicesForStop(stopCode: String): List<String>? {
        val cancellationSignal = CancellationSignal()

        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation {
                    cancellationSignal.cancel()
                }

                try {
                    val result = context.contentResolver.query(
                        contract.getContentUri(),
                        arrayOf(ServiceStopsContract.SERVICE_NAME),
                        "${ServiceStopsContract.STOP_CODE} = ?",
                        arrayOf(stopCode),
                        SERVICE_SORT_CLAUSE,
                        cancellationSignal)
                        ?.use {
                            // Fill the Cursor window
                            val count = it.count

                            if (count > 0) {
                                val result = ArrayList<String>(count)
                                val serviceNameColumn = it.getColumnIndex(
                                    ServiceStopsContract.SERVICE_NAME)

                                while (it.moveToNext()) {
                                    result.add(it.getString(serviceNameColumn))
                                }

                                result
                            } else {
                                null
                            }
                        }

                    continuation.resume(result)
                } catch (ignored: OperationCanceledException) {
                    // Do nothing.
                }
            }
        }
    }

    /**
     * For all of the currently registered listeners, dispatch a data change event to them.
     */
    private fun dispatchOnServiceStopsChangedListeners() {
        synchronized(listeners) {
            listeners.forEach { listener ->
                listener.onServiceStopsChanged()
            }
        }
    }

    /**
     * This inner class is used as the [ContentObserver] for observing changes to service stop data.
     */
    private inner class Observer : ContentObserver(Handler(Looper.getMainLooper())) {

        override fun deliverSelfNotifications() = true

        override fun onChange(selfChange: Boolean) {
            dispatchOnServiceStopsChangedListeners()
        }
    }
}