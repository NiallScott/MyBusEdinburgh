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
import uk.org.rivernile.android.bustracker.database.busstop.BusStopDatabase
import uk.org.rivernile.android.bustracker.utils.CursorLiveData
import uk.org.rivernile.android.bustracker.utils.Strings
import uk.org.rivernile.edinburghbustracker.android.R
import java.util.Collections

/**
 * This [CursorLiveData] loads stops from the database. An optional [String] array may be supplied
 * (`filteredServices`) which means only stops which have the included services should be returned.
 * This class watches for updates in the database and automatically reloads the data.
 *
 * @author Niall Scott
 * @param context A [Context] instance.
 * @param strings A [Strings] instance.
 * @param filteredServices An optional filter to specify so that only stops which have these
 * services are returned.
 */
internal class BusStopsLiveData(private val context: Context,
                                private val strings: Strings,
                                private val filteredServices: Array<String>?)
    : CursorLiveData<Map<String, Stop>>() {

    override fun onBeginObservingCursor() {
        context.contentResolver.registerContentObserver(BusStopContract.BusStops.CONTENT_URI,
                false, contentObserver)
    }

    override fun loadCursor(): Cursor? {
        val selection: String?
        val selectionArgs: Array<String>?

        if (filteredServices != null && filteredServices.isNotEmpty()) {
            selection = BusStopContract.BusStops.STOP_CODE + " IN (" +
                    "SELECT " + BusStopContract.ServiceStops.STOP_CODE + " FROM " +
                    BusStopContract.ServiceStops.TABLE_NAME + " WHERE " +
                    BusStopContract.ServiceStops.SERVICE_NAME + " IN (" +
                    BusStopDatabase.generateInPlaceholders(filteredServices.size) + "))"
            selectionArgs = filteredServices
        } else {
            selection = null
            selectionArgs = null
        }

        return context.contentResolver.query(BusStopContract.BusStops.CONTENT_URI,
                arrayOf(BusStopContract.BusStops.STOP_CODE,
                        BusStopContract.BusStops.STOP_NAME,
                        BusStopContract.BusStops.LATITUDE,
                        BusStopContract.BusStops.LONGITUDE,
                        BusStopContract.BusStops.ORIENTATION,
                        BusStopContract.BusStops.LOCALITY),
                selection,
                selectionArgs,
                null,
                cancellationSignal)
    }

    override fun processCursor(cursor: Cursor?): Map<String, Stop>? {
        return cursor?.let {
            val result = HashMap<String, Stop>(it.count)
            val stopCodeColumn = it.getColumnIndex(BusStopContract.BusStops.STOP_CODE)
            val stopNameColumn = it.getColumnIndex(BusStopContract.BusStops.STOP_NAME)
            val latitudeColumn = it.getColumnIndex(BusStopContract.BusStops.LATITUDE)
            val longitudeColumn = it.getColumnIndex(BusStopContract.BusStops.LONGITUDE)
            val orientationColumn = it.getColumnIndex(BusStopContract.BusStops.ORIENTATION)
            val localityColumn = it.getColumnIndex(BusStopContract.BusStops.LOCALITY)

            while (it.moveToNext()) {
                val stopCode = it.getString(stopCodeColumn)
                val stopName = it.getString(stopNameColumn)
                val locality = it.getString(localityColumn)
                val latitude = it.getDouble(latitudeColumn)
                val longitude = it.getDouble(longitudeColumn)
                val orientation = it.getInt(orientationColumn)

                val displayName = if (locality != null) {
                    strings.getString(R.string.busstop_locality, stopName, locality, stopCode)
                } else {
                    strings.getString(R.string.busstop, stopName, stopCode)
                }

                result[stopCode] = Stop(stopCode, displayName, latitude, longitude, orientation)
            }

            return Collections.unmodifiableMap(result)
        }
    }

    override fun onStopObservingCursor() {
        context.contentResolver.unregisterContentObserver(contentObserver)
    }
}