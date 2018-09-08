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

/**
 * This [CursorLiveData] loads all known service names from the database. The names will be reloaded
 * when a change is detected in the database.
 *
 * @author Niall Scott
 * @param context A [Context] instance.
 */
internal class ServiceNamesLiveData(private val context: Context)
    : CursorLiveData<Array<String>>() {

    override fun onBeginObservingCursor() {
        context.contentResolver.registerContentObserver(BusStopContract.Services.CONTENT_URI,
                false, contentObserver)
    }

    override fun loadCursor(): Cursor? =
            context.contentResolver.query(BusStopContract.Services.CONTENT_URI,
                    arrayOf(BusStopContract.Services.NAME),
                    null,
                    null,
                    BusStopDatabase.getServicesSortByCondition(BusStopContract.Services.NAME),
                    cancellationSignal)

    override fun processCursor(cursor: Cursor?): Array<String>? {
        cursor?.let {
            val count = it.count
            val serviceNameColumn = it.getColumnIndex(BusStopContract.Services.NAME)
            val result = Array(count) { _ -> "" }

            for (i in 0..count) {
                if (it.moveToPosition(i)) {
                    result[i] = it.getString(serviceNameColumn)
                }
            }

            return result
        }

        return null
    }

    override fun onStopObservingCursor() {
        context.contentResolver.unregisterContentObserver(contentObserver)
    }
}