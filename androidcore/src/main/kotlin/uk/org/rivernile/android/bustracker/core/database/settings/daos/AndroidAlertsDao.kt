/*
 * Copyright (C) 2019 - 2021 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.settings.daos

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import uk.org.rivernile.android.bustracker.core.database.settings.AlertsContract
import uk.org.rivernile.android.bustracker.core.database.settings.entities.Alert
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ArrivalAlert
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ProximityAlert
import uk.org.rivernile.android.bustracker.core.di.ForIoDispatcher
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * This is an Android-specific implementation of [AlertsDao] which uses a
 * [android.content.ContentProvider] to communicate with the database.
 *
 * @param context The application [Context].
 * @param contract The contract for talking with the alerts table.
 * @author Niall Scott
 */
@Singleton
internal class AndroidAlertsDao @Inject constructor(
        private val context: Context,
        private val contract: AlertsContract,
        @ForIoDispatcher private val ioDispatcher: CoroutineDispatcher): AlertsDao {

    private val listeners = mutableListOf<AlertsDao.OnAlertsChangedListener>()
    private val observer = Observer()

    @Synchronized
    override fun addOnAlertsChangedListener(listener: AlertsDao.OnAlertsChangedListener) {
        listeners.apply {
            add(listener)

            if (size == 1) {
                context.contentResolver.registerContentObserver(contract.getContentUri(), true,
                        observer)
            }
        }
    }

    @Synchronized
    override fun removeOnAlertsChangedListener(listener: AlertsDao.OnAlertsChangedListener) {
        listeners.apply {
            remove(listener)

            if (isEmpty()) {
                context.contentResolver.unregisterContentObserver(observer)
            }
        }
    }

    override suspend fun addArrivalAlert(arrivalAlert: ArrivalAlert): Long =
        withContext(ioDispatcher) {
            val values = ContentValues().apply {
                put(AlertsContract.TYPE, AlertsContract.ALERTS_TYPE_TIME)
                put(AlertsContract.TIME_ADDED, arrivalAlert.timeAdded)
                put(AlertsContract.STOP_CODE, arrivalAlert.stopCode)
                put(AlertsContract.SERVICE_NAMES, arrivalAlert.serviceNames.joinToString(","))
                put(AlertsContract.TIME_TRIGGER, arrivalAlert.timeTrigger)
            }

            context.contentResolver.insert(contract.getContentUri(), values)
                    ?.let(ContentUris::parseId) ?: -1
        }

    override suspend fun addProximityAlert(proximityAlert: ProximityAlert) {
        withContext(ioDispatcher) {
            val values = ContentValues().apply {
                put(AlertsContract.TYPE, AlertsContract.ALERTS_TYPE_PROXIMITY)
                put(AlertsContract.TIME_ADDED, proximityAlert.timeAdded)
                put(AlertsContract.STOP_CODE, proximityAlert.stopCode)
                put(AlertsContract.DISTANCE_FROM, proximityAlert.distanceFrom)
            }

            context.contentResolver.insert(contract.getContentUri(), values)
        }
    }

    override suspend fun removeArrivalAlert(id: Int) {
        withContext(ioDispatcher) {
            context.contentResolver.delete(
                    contract.getContentUri(),
                    "${AlertsContract.ID} = ? AND ${AlertsContract.TYPE} = ?",
                    arrayOf(
                            id.toString(),
                            AlertsContract.ALERTS_TYPE_TIME.toString()))
        }
    }

    override suspend fun removeArrivalAlert(stopCode: String) {
        withContext(ioDispatcher) {
            context.contentResolver.delete(
                    contract.getContentUri(),
                    "${AlertsContract.STOP_CODE} = ? AND ${AlertsContract.TYPE} = ?",
                    arrayOf(
                            stopCode,
                            AlertsContract.ALERTS_TYPE_TIME.toString()))
        }
    }

    override suspend fun removeAllArrivalAlerts() {
        withContext(ioDispatcher) {
            context.contentResolver.delete(
                    contract.getContentUri(),
                    "${AlertsContract.TYPE} = ?",
                    arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString()))
        }
    }

    override suspend fun removeProximityAlert(id: Int) {
        withContext(ioDispatcher) {
            context.contentResolver.delete(contract.getContentUri(),
                    "${AlertsContract.ID} = ? AND ${AlertsContract.TYPE} = ?",
                    arrayOf(id.toString(), AlertsContract.ALERTS_TYPE_PROXIMITY.toString()))
        }
    }

    override suspend fun removeProximityAlert(stopCode: String) {
        withContext(ioDispatcher) {
            context.contentResolver.delete(contract.getContentUri(),
                    "${AlertsContract.STOP_CODE} = ? AND ${AlertsContract.TYPE} = ?",
                    arrayOf(stopCode, AlertsContract.ALERTS_TYPE_PROXIMITY.toString()))
        }
    }

    override suspend fun removeAllProximityAlerts() {
        withContext(ioDispatcher) {
            context.contentResolver.delete(contract.getContentUri(),
                    "${AlertsContract.TYPE} = ?",
                    arrayOf(AlertsContract.ALERTS_TYPE_PROXIMITY.toString()))
        }
    }

    override suspend fun getAllAlerts(): List<Alert>? {
        val cancellationSignal = CancellationSignal()

        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation {
                    cancellationSignal.cancel()
                }

                val result = context.contentResolver.query(
                        contract.getContentUri(),
                        arrayOf(
                                AlertsContract.ID,
                                AlertsContract.TIME_ADDED,
                                AlertsContract.STOP_CODE,
                                AlertsContract.TYPE,
                                AlertsContract.DISTANCE_FROM,
                                AlertsContract.SERVICE_NAMES,
                                AlertsContract.TIME_TRIGGER),
                        null,
                        null,
                        "${AlertsContract.TIME_ADDED} ASC",
                        cancellationSignal)?.use {
                    val count = it.count

                    if (count > 0) {
                        val result = ArrayList<Alert>(count)
                        val idColumn = it.getColumnIndex(AlertsContract.ID)
                        val timeAddedColumn = it.getColumnIndex(AlertsContract.TIME_ADDED)
                        val stopCodeColumn = it.getColumnIndex(AlertsContract.STOP_CODE)
                        val typeColumn = it.getColumnIndex(AlertsContract.TYPE)
                        val distanceFromColumn = it.getColumnIndex(AlertsContract.DISTANCE_FROM)
                        val serviceNamesColumn = it.getColumnIndex(AlertsContract.SERVICE_NAMES)
                        val timeTriggerColumn = it.getColumnIndex(AlertsContract.TIME_TRIGGER)

                        while (it.moveToNext()) {
                            val id = it.getInt(idColumn)
                            val timeAdded = it.getLong(timeAddedColumn)
                            val stopCode = it.getString(stopCodeColumn)
                            val type = it.getInt(typeColumn)

                            when (type.toByte()) {
                                AlertsContract.ALERTS_TYPE_TIME -> {
                                    val serviceNames = it.getString(serviceNamesColumn)
                                            .split(',')
                                            .map(String::trim)
                                    val timeTrigger = it.getInt(timeTriggerColumn)

                                    ArrivalAlert(
                                            id,
                                            timeAdded,
                                            stopCode,
                                            serviceNames,
                                            timeTrigger)
                                }
                                AlertsContract.ALERTS_TYPE_PROXIMITY -> {
                                    val distanceFrom = it.getInt(distanceFromColumn)

                                    ProximityAlert(
                                            id,
                                            timeAdded,
                                            stopCode,
                                            distanceFrom)
                                }
                                else -> null
                            }?.let(result::add)
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

    override suspend fun getProximityAlert(id: Int): ProximityAlert? {
        val cancellationSignal = CancellationSignal()

        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation {
                    cancellationSignal.cancel()
                }

                val result = context.contentResolver.query(
                        contract.getContentUri(),
                        arrayOf(
                                AlertsContract.ID,
                                AlertsContract.TIME_ADDED,
                                AlertsContract.STOP_CODE,
                                AlertsContract.DISTANCE_FROM),
                        "${AlertsContract.ID} = ? AND ${AlertsContract.TYPE} = ?",
                        arrayOf(
                                id.toString(),
                                AlertsContract.ALERTS_TYPE_PROXIMITY.toString()),
                        null,
                        cancellationSignal)
                        ?.use {
                            // Fill Cursor window.
                            it.count

                            if (it.moveToFirst()) {
                                val idColumn = it.getColumnIndex(AlertsContract.ID)
                                val timeAddedColumn = it.getColumnIndex(AlertsContract.TIME_ADDED)
                                val stopCodeColumn = it.getColumnIndex(AlertsContract.STOP_CODE)
                                val distanceFromColumn =
                                        it.getColumnIndex(AlertsContract.DISTANCE_FROM)

                                ProximityAlert(
                                        it.getInt(idColumn),
                                        it.getLong(timeAddedColumn),
                                        it.getString(stopCodeColumn),
                                        it.getInt(distanceFromColumn))
                            } else {
                                null
                            }
                        }

                continuation.resume(result)
            }
        }
    }

    override fun getAllArrivalAlerts() = context.contentResolver.query(
            contract.getContentUri(),
            arrayOf(
                    AlertsContract.ID,
                    AlertsContract.TIME_ADDED,
                    AlertsContract.STOP_CODE,
                    AlertsContract.SERVICE_NAMES,
                    AlertsContract.TIME_TRIGGER),
            "${AlertsContract.TYPE} = ?",
            arrayOf(
                    AlertsContract.ALERTS_TYPE_TIME.toString()),
            null)?.use {
        val count = it.count

        if (count > 0) {
            val result = ArrayList<ArrivalAlert>(count)
            val idColumn = it.getColumnIndex(AlertsContract.ID)
            val timeAddedColumn = it.getColumnIndex(AlertsContract.TIME_ADDED)
            val stopCodeColumn = it.getColumnIndex(AlertsContract.STOP_CODE)
            val serviceNamesColumn = it.getColumnIndex(AlertsContract.SERVICE_NAMES)
            val timeTriggerColumn = it.getColumnIndex(AlertsContract.TIME_TRIGGER)

            while (it.moveToNext()) {
                val id = it.getInt(idColumn)
                val timeAdded = it.getLong(timeAddedColumn)
                val stopCode = it.getString(stopCodeColumn)
                val serviceNames = it.getString(serviceNamesColumn)
                        .split(",")
                        .map { name -> name.trim() }
                val timeTrigger = it.getInt(timeTriggerColumn)
                val arrivalAlert = ArrivalAlert(
                        id,
                        timeAdded,
                        stopCode,
                        serviceNames,
                        timeTrigger)
                result.add(arrivalAlert)
            }

            result
        } else {
            null
        }
    }

    override fun getAllArrivalAlertStopCodes() = context.contentResolver.query(
            contract.getContentUri(),
            arrayOf(
                    AlertsContract.STOP_CODE),
            "${AlertsContract.TYPE} = ?",
            arrayOf(
                    AlertsContract.ALERTS_TYPE_TIME.toString()),
            null)?.use {
        val count = it.count

        if (count > 0) {
            val result = ArrayList<String>(count)
            val stopCodeColumn = it.getColumnIndex(AlertsContract.STOP_CODE)

            while (it.moveToNext()) {
                result.add(it.getString(stopCodeColumn))
            }

            result
        } else {
            null
        }
    }

    override suspend fun getArrivalAlertCount(): Int {
        val cancellationSignal = CancellationSignal()

        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation {
                    cancellationSignal.cancel()
                }

                val result = context.contentResolver.query(
                        contract.getContentUri(),
                        arrayOf(AlertsContract.COUNT),
                        "${AlertsContract.TYPE} = ?",
                        arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString()),
                        null,
                        cancellationSignal)
                        ?.use {
                            // Fill the Cursor window.
                            it.count

                            if (it.moveToFirst()) {
                                val countColumn = it.getColumnIndex(AlertsContract.COUNT)
                                it.getInt(countColumn)
                            } else {
                                0
                            }
                        } ?: 0

                continuation.resume(result)
            }
        }
    }

    override fun getAllProximityAlerts() = context.contentResolver.query(
            contract.getContentUri(),
            arrayOf(
                    AlertsContract.ID,
                    AlertsContract.TIME_ADDED,
                    AlertsContract.STOP_CODE,
                    AlertsContract.DISTANCE_FROM),
            "${AlertsContract.TYPE} = ?",
            arrayOf(
                    AlertsContract.ALERTS_TYPE_PROXIMITY.toString()),
            null)?.use {
        val count = it.count

        if (count > 0) {
            val result = ArrayList<ProximityAlert>(count)
            val idColumn = it.getColumnIndex(AlertsContract.ID)
            val timeAddedColumn = it.getColumnIndex(AlertsContract.TIME_ADDED)
            val stopCodeColumn = it.getColumnIndex(AlertsContract.STOP_CODE)
            val distanceFromColumn = it.getColumnIndex(AlertsContract.DISTANCE_FROM)

            while (it.moveToNext()) {
                val id = it.getInt(idColumn)
                val timeAdded = it.getLong(timeAddedColumn)
                val stopCode = it.getString(stopCodeColumn)
                val distanceFrom = it.getInt(distanceFromColumn)
                val proximityAlert = ProximityAlert(
                        id,
                        timeAdded,
                        stopCode,
                        distanceFrom)
                result.add(proximityAlert)
            }

            result
        } else {
            null
        }
    }

    override suspend fun getProximityAlertCount(): Int {
        val cancellationSignal = CancellationSignal()

        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation {
                    cancellationSignal.cancel()
                }

                val result = context.contentResolver.query(
                        contract.getContentUri(),
                        arrayOf(AlertsContract.COUNT),
                        "${AlertsContract.TYPE} = ?",
                        arrayOf(AlertsContract.ALERTS_TYPE_PROXIMITY.toString()),
                        null,
                        cancellationSignal)
                        ?.use {
                            // Fill the Cursor window.
                            it.count

                            if (it.moveToFirst()) {
                                val countColumn = it.getColumnIndex(AlertsContract.COUNT)
                                it.getInt(countColumn)
                            } else {
                                0
                            }
                        } ?: 0

                continuation.resume(result)
            }
        }
    }

    override suspend fun hasArrivalAlert(stopCode: String): Boolean {
        val cancellationSignal = CancellationSignal()

        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation {
                    cancellationSignal.cancel()
                }

                val result = context.contentResolver.query(
                        contract.getContentUri(),
                        arrayOf(AlertsContract.COUNT),
                        "${AlertsContract.TYPE} = ? AND ${AlertsContract.STOP_CODE} = ?",
                        arrayOf(
                                AlertsContract.ALERTS_TYPE_TIME.toString(),
                                stopCode),
                        null,
                        cancellationSignal)
                        ?.use {
                            // Fill the Cursor window.
                            it.count

                            it.moveToFirst() && it.getInt(
                                    it.getColumnIndex(AlertsContract.COUNT)) > 0
                        } ?: false

                continuation.resume(result)
            }
        }
    }

    override suspend fun hasProximityAlert(stopCode: String): Boolean {
        val cancellationSignal = CancellationSignal()

        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation {
                    cancellationSignal.cancel()
                }

                val result = context.contentResolver.query(
                        contract.getContentUri(),
                        arrayOf(AlertsContract.COUNT),
                        "${AlertsContract.TYPE} = ? AND ${AlertsContract.STOP_CODE} = ?",
                        arrayOf(
                                AlertsContract.ALERTS_TYPE_PROXIMITY.toString(),
                                stopCode),
                        null,
                        cancellationSignal)
                        ?.use {
                            // Fill the Cursor window.
                            it.count

                            it.moveToFirst() && it.getInt(
                                    it.getColumnIndex(AlertsContract.COUNT)) > 0
                        } ?: false

                continuation.resume(result)
            }
        }
    }

    /**
     * For all of the currently registers listeners, dispatch an alert change to them.
     */
    private fun dispatchOnAlertsChangedListeners() {
        listeners.forEach { listener ->
            listener.onAlertsChanged()
        }
    }

    /**
     * This inner class is used as the [ContentObserver] for observing changes to alerts.
     */
    private inner class Observer : ContentObserver(Handler(Looper.getMainLooper())) {

        override fun deliverSelfNotifications() = true

        override fun onChange(selfChange: Boolean) {
            dispatchOnAlertsChangedListeners()
        }
    }
}