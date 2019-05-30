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

package uk.org.rivernile.android.bustracker.core.database

import android.net.Uri
import android.provider.BaseColumns

/**
 * This interface defines methods common to a table contract when dealing with Android
 * [android.content.ContentProvider]s.
 *
 * @author Niall Scott
 */
internal interface TableContract : BaseColumns {

    companion object {

        /**
         * This forms the first part of the type [String] for a result set containing multiple
         * entries.
         */
        const val SUBTYPE_MULTIPLE = "vnd.android.cursor.dir/"
    }

    /**
     * Get the type for the table, as returned by [android.content.ContentProvider.getType].
     *
     * @return The type for the table.
     */
    fun getType(): String

    /**
     * Get the content [Uri] for the table.
     *
     * @return The content [Uri] for the table.
     */
    fun getContentUri(): Uri
}