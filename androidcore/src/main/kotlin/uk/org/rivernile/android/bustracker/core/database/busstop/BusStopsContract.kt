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

package uk.org.rivernile.android.bustracker.core.database.busstop

import android.net.Uri
import uk.org.rivernile.android.bustracker.core.dagger.qualifiers.ForBusStopDatabase
import uk.org.rivernile.android.bustracker.core.database.TableContract
import uk.org.rivernile.android.bustracker.core.utils.OpenForTesting
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class represents a [TableContract] for the bus stops table in the bus stop database.
 *
 * @author Niall Scott
 */
@Singleton
@OpenForTesting
internal class BusStopsContract @Inject constructor(
        @ForBusStopDatabase authority: String)
    : TableContract {

    companion object {

        /**
         * The unique code of the bus stop. Column name.
         *
         * Type: STRING
         */
        const val STOP_CODE = "stopCode"

        /**
         * The name of the bus stop. Column name.
         *
         * Type: STRING
         */
        const val STOP_NAME = "stopName"

        /**
         * The latitude of the bus stop. Column name.
         *
         * Type: DOUBLE
         */
        const val LATITUDE = "x"

        /**
         * The longitude of the bus stop. Column name.
         *
         * Type: DOUBLE
         */
        const val LONGITUDE = "y"

        /**
         * The orientation of the bus stop, expressed between 0 and 7, where 0 is north and 7 is
         * north-west, and the rest of the numbers are filled in clockwise. Column name.
         *
         * Type: INTEGER
         */
        const val ORIENTATION = "orientation"

        /**
         * The locality of the bus stop - for example, the name of the local area in the city.
         * Column name.
         *
         * Type: STRING
         */
        const val LOCALITY = "locality"

        /**
         * The listing of services as a comma separated list, suitable for displaying to users.
         * Column name.
         *
         * Type: STRING
         */
        const val SERVICE_LISTING = "serviceListing"

        private const val TABLE_NAME = "view_bus_stops"
    }

    private val typeSingle = "${TableContract.SUBTYPE_SINGLE}/vnd.$authority.$TABLE_NAME"
    private val typeMultiple = "${TableContract.SUBTYPE_MULTIPLE}/vnd.$authority.$TABLE_NAME"
    private val uri = Uri.parse("content://$authority/$TABLE_NAME")

    override fun getSingleItemType() = typeSingle

    override fun getMultipleItemsType() = typeMultiple

    override fun getContentUri(): Uri = uri
}