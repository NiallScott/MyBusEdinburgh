/*
 * Copyright (C) 2019 Niall 'Rivernile' Scott
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

import android.content.ContentValues
import android.content.Context
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

    override fun addArrivalAlert(arrivalAlert: ArrivalAlert) {
        val values = ContentValues().apply {
            put(AlertsContract.TYPE, AlertsContract.ALERTS_TYPE_TIME)
            put(AlertsContract.TIME_ADDED, arrivalAlert.timeAdded)
            put(AlertsContract.STOP_CODE, arrivalAlert.stopCode)
            put(AlertsContract.SERVICE_NAMES, arrivalAlert.serviceNames.joinToString(","))
            put(AlertsContract.TIME_TRIGGER, arrivalAlert.timeTrigger)
        }

        context.contentResolver.insert(contract.getContentUri(), values)
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

    override fun removeProximityAlert(id: Int) {
        context.contentResolver.delete(contract.getContentUri(),
                "${AlertsContract.ID} = ? AND ${AlertsContract.TYPE} = ?",
                arrayOf(id.toString(), AlertsContract.ALERTS_TYPE_PROXIMITY.toString()))
    }
}