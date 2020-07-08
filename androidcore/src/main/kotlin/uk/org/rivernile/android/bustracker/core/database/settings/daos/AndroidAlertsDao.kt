/*
 * Copyright (C) 2019 - 2020 Niall 'Rivernile' Scott
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
import android.os.Handler
import android.os.Looper
import uk.org.rivernile.android.bustracker.core.database.settings.AlertsContract
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ArrivalAlert
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ProximityAlert

/**
 * This is an Android-specific implementation of [AlertsDao] which uses a
 * [android.content.ContentProvider] to communicate with the database.
 *
 * @param context The application [Context].
 * @param contract The contract for talking with the alerts table.
 * @author Niall Scott
 */
internal class AndroidAlertsDao(private val context: Context,
                                private val contract: AlertsContract): AlertsDao {

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

    override fun addArrivalAlert(arrivalAlert: ArrivalAlert): Long {
        val values = ContentValues().apply {
            put(AlertsContract.TYPE, AlertsContract.ALERTS_TYPE_TIME)
            put(AlertsContract.TIME_ADDED, arrivalAlert.timeAdded)
            put(AlertsContract.STOP_CODE, arrivalAlert.stopCode)
            put(AlertsContract.SERVICE_NAMES, arrivalAlert.serviceNames.joinToString(","))
            put(AlertsContract.TIME_TRIGGER, arrivalAlert.timeTrigger)
        }

        return context.contentResolver.insert(contract.getContentUri(), values)
                ?.let(ContentUris::parseId)
                ?: -1
    }

    override fun addProximityAlert(proximityAlert: ProximityAlert) {
        val values = ContentValues().apply {
            put(AlertsContract.TYPE, AlertsContract.ALERTS_TYPE_PROXIMITY)
            put(AlertsContract.TIME_ADDED, proximityAlert.timeAdded)
            put(AlertsContract.STOP_CODE, proximityAlert.stopCode)
            put(AlertsContract.DISTANCE_FROM, proximityAlert.distanceFrom)
        }

        context.contentResolver.insert(contract.getContentUri(), values)
    }

    override fun removeArrivalAlert(id: Int) {
        context.contentResolver.delete(contract.getContentUri(),
                "${AlertsContract.ID} = ? AND ${AlertsContract.TYPE} = ?",
                arrayOf(id.toString(), AlertsContract.ALERTS_TYPE_TIME.toString()))
    }

    override fun removeAllArrivalAlerts() {
        context.contentResolver.delete(
                contract.getContentUri(),
                "${AlertsContract.TYPE} = ?",
                arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString()))
    }

    override fun removeProximityAlert(id: Int) {
        context.contentResolver.delete(contract.getContentUri(),
                "${AlertsContract.ID} = ? AND ${AlertsContract.TYPE} = ?",
                arrayOf(id.toString(), AlertsContract.ALERTS_TYPE_PROXIMITY.toString()))
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

    override fun getArrivalAlertCount() = context.contentResolver.query(
            contract.getContentUri(),
            arrayOf(
                    AlertsContract.COUNT),
            "${AlertsContract.TYPE} = ?",
            arrayOf(
                    AlertsContract.ALERTS_TYPE_TIME.toString()),
            null)?.use {
        // Fill the Cursor window.
        it.count

        if (it.moveToFirst()) {
            val countColumn = it.getColumnIndex(AlertsContract.COUNT)
            it.getInt(countColumn)
        } else {
            0
        }
    } ?: 0

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