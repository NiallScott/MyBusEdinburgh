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

package uk.org.rivernile.android.bustracker.core.database.busstop

import android.net.Uri
import uk.org.rivernile.android.bustracker.core.di.ForBusStopDatabase
import uk.org.rivernile.android.bustracker.core.utils.OpenForTesting
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class defines the contract for the Bus Stop Database as a whole entity. It does not define
 * the contract for individual tables within this database - these are defined in the other
 * contract classes.
 *
 * @param authority The authority URI [String] of the database
 * @author Niall Scott
 */
@Singleton
@OpenForTesting
internal class BusStopDatabaseContract @Inject constructor(
        @ForBusStopDatabase authority: String) {

    companion object {

        /**
         * The method to "call" on the [android.content.ContentProvider] to replace the database,
         * for example when a new version of the database has been downloaded.
         */
        internal const val METHOD_REPLACE_DATABASE = "replaceDatabase"
    }

    private val uri = Uri.parse("content://$authority")

    /**
     * Get the content [Uri] for the database.
     *
     * @return The content [Uri] for the database.
     */
    fun getContentUri(): Uri = uri
}