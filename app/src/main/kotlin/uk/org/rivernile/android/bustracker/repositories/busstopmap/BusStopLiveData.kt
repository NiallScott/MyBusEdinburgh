/*
 * Copyright (C) 2018 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.repositories.busstopmap

import android.content.Context
import android.database.Cursor
import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract
import uk.org.rivernile.android.bustracker.utils.CursorLiveData

/**
 * This [android.arch.lifecycle.LiveData] instance is responsible for providing data for a single
 * given stop.
 *
 * @author Niall Scott
 * @param context A [Context] instance.
 * @param stopCode The code of the stop to return.
 */
class BusStopLiveData(private val context: Context,
                      private val stopCode: String) : CursorLiveData<SelectedStop>() {

    override fun onBeginObservingCursor() {
        context.contentResolver.registerContentObserver(BusStopContract.BusStops.CONTENT_URI,
                false, contentObserver)
    }

    override fun loadCursor(): Cursor? = context.contentResolver.query(
            BusStopContract.BusStops.CONTENT_URI,
            arrayOf(BusStopContract.BusStops.STOP_CODE,
                    BusStopContract.BusStops.LATITUDE,
                    BusStopContract.BusStops.LONGITUDE,
                    BusStopContract.BusStops.SERVICE_LISTING),
            "${BusStopContract.BusStops.STOP_CODE} = ?",
            arrayOf(stopCode),
            null)

    override fun processCursor(cursor: Cursor?): SelectedStop? {
        return if (cursor != null && cursor.moveToFirst()) {
            val stopCodeColumn = cursor.getColumnIndex(BusStopContract.BusStops.STOP_CODE)
            val latitudeColumn = cursor.getColumnIndex(BusStopContract.BusStops.LATITUDE)
            val longitudeColumn = cursor.getColumnIndex(BusStopContract.BusStops.LONGITUDE)
            val serviceListingColumn = cursor.getColumnIndex(
                    BusStopContract.BusStops.SERVICE_LISTING)

            SelectedStop(cursor.getString(stopCodeColumn),
                    cursor.getDouble(latitudeColumn),
                    cursor.getDouble(longitudeColumn),
                    cursor.getString(serviceListingColumn))
        } else {
            null
        }
    }

    override fun onStopObservingCursor() {
        context.contentResolver.unregisterContentObserver(contentObserver)
    }
}