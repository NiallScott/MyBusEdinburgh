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

package uk.org.rivernile.android.bustracker.core.database.busstop.daos

import android.annotation.SuppressLint
import android.content.Context
import uk.org.rivernile.android.bustracker.core.database.busstop.DatabaseInformationContract

/**
 * This is an Android concrete implementation of the [DatabaseInformationDao].
 *
 * @param context The application [Context].
 * @param contract The database contract, so we know how to talk to it.
 * @author Niall Scott
 */
internal class AndroidDatabaseInformationDao(private val context: Context,
                                             private val contract: DatabaseInformationContract)
    : DatabaseInformationDao {

    @SuppressLint("Recycle") // Stops the Cursor close warning, even though we close it.
    override fun getTopologyId() = context.contentResolver.query(
            contract.getContentUri(),
            arrayOf(DatabaseInformationContract.CURRENT_TOPOLOGY_ID),
            null,
            null,
            null)?.let {
        // Fill the Cursor window.
        it.count

        val result = if (it.moveToFirst()) {
            it.getString(it.getColumnIndex(DatabaseInformationContract.CURRENT_TOPOLOGY_ID))
        } else {
            null
        }

        it.close()

        result
    }
}