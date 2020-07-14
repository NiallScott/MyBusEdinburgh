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

package uk.org.rivernile.android.bustracker.core.database.busstop.daos

import android.content.Context
import uk.org.rivernile.android.bustracker.core.database.busstop.BusStopsContract
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopLocation
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopName

/**
 * This is an Android concrete implementation of the [BusStopsDao].
 *
 * @param context The application [Context].
 * @param contract The database contract, so we know how to talk to it.
 * @author Niall Scott
 */
internal class AndroidBusStopsDao(
        private val context: Context,
        private val contract: BusStopsContract)
    : BusStopsDao {

    override fun getNameForStop(stopCode: String) = context.contentResolver.query(
            contract.getContentUri(),
            arrayOf(
                    BusStopsContract.STOP_NAME,
                    BusStopsContract.LOCALITY),
            "${BusStopsContract.STOP_CODE} = ?",
            arrayOf(stopCode),
            null)?.use {
        // Fill the Cursor window.
        it.count

        if (it.moveToFirst()) {
            it.getString(it.getColumnIndex(BusStopsContract.STOP_NAME))?.let { name ->
                val locality = it.getString(it.getColumnIndex(BusStopsContract.LOCALITY))
                StopName(name, locality)
            }
        } else {
            null
        }
    }

    override fun getLocationForStop(stopCode: String) = context.contentResolver.query(
            contract.getContentUri(),
            arrayOf(
                    BusStopsContract.LATITUDE,
                    BusStopsContract.LONGITUDE),
            "${BusStopsContract.STOP_CODE} = ?",
            arrayOf(stopCode),
            null)?.use {
        // Fill the Cursor window.
        it.count

        if (it.moveToFirst()) {
            val latitudeColumn = it.getColumnIndex(BusStopsContract.LATITUDE)
            val longitudeColumn = it.getColumnIndex(BusStopsContract.LONGITUDE)

            StopLocation(
                    stopCode,
                    it.getDouble(latitudeColumn),
                    it.getDouble(longitudeColumn))
        } else {
            null
        }
    }
}