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

package uk.org.rivernile.android.bustracker.core.database.busstop

import android.net.Uri
import uk.org.rivernile.android.bustracker.core.dagger.qualifiers.ForBusStopDatabase
import uk.org.rivernile.android.bustracker.core.database.TableContract
import uk.org.rivernile.android.bustracker.core.utils.OpenForTesting
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class represents a [TableContract] for the database information table in the bus stop
 * database.
 *
 * @param authority The authority URI [String] of the database.
 * @author Niall Scott
 */
@Singleton
@OpenForTesting
internal class DatabaseInformationContract @Inject constructor(
        @ForBusStopDatabase authority: String)
    : TableContract {

    companion object {

        /**
         * The current topology ID. Column name.
         *
         * Type: STRING
         */
        const val CURRENT_TOPOLOGY_ID = "current_topo_id"

        /**
         * The timestamp in milliseconds when the database was last updated. Column name.
         *
         * Type: LONG
         */
        const val LAST_UPDATE_TIMESTAMP = "updateTS"

        private const val TABLE_NAME = "database_info"
    }

    private val type = "${TableContract.SUBTYPE_MULTIPLE}vnd.$authority.$TABLE_NAME"
    private val uri = Uri.parse("content://$authority/$TABLE_NAME")

    override fun getType() = type

    override fun getContentUri(): Uri = uri
}