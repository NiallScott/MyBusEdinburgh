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

package uk.org.rivernile.android.bustracker.repositories.about

import android.content.Context
import android.database.Cursor
import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract
import uk.org.rivernile.android.bustracker.utils.CursorLiveData
import java.util.Date

/**
 * This is a concrete implementation of [CursorLiveData] to provide database metadata for the
 * 'about' screen.
 *
 * @property context A [Context] instance.
 * @author Niall Scott
 */
internal class AboutDatabaseLiveData(private val context: Context)
    : CursorLiveData<DatabaseMetadata>() {

    override fun loadCursor(): Cursor? = context.contentResolver.query(
            BusStopContract.DatabaseInformation.CONTENT_URI,
            arrayOf(BusStopContract.DatabaseInformation.CURRENT_TOPOLOGY_ID,
                BusStopContract.DatabaseInformation.LAST_UPDATE_TIMESTAMP),
            null, null, null, cancellationSignal)

    override fun processCursor(cursor: Cursor?): DatabaseMetadata? {
        return if (cursor != null && cursor.moveToFirst()) {
            val versionColumn = cursor.getColumnIndex(
                    BusStopContract.DatabaseInformation.LAST_UPDATE_TIMESTAMP)
            val topologyColumn = cursor.getColumnIndex(
                    BusStopContract.DatabaseInformation.CURRENT_TOPOLOGY_ID)
            val version = Date(cursor.getLong(versionColumn))

            DatabaseMetadata(version, cursor.getString(topologyColumn))
        } else {
            null
        }
    }
}