/*
 * Copyright (C) 2025 Niall 'Rivernile' Scott
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

/**
 * This supports replacing the database with a new database file.
 *
 * @author Niall Scott
 */
internal interface DatabaseReplacer {

    /**
     * Given a [newDatabaseFile], attempt to replace the existing database with this file. If the
     * new database does not pass some internal checks, the operation will fail and the existing
     * database will continue to be used.
     *
     * @param newDatabaseFile The new database file. This is assumed to already be in the database
     * directory.
     * @return `true` if the database was replaced, `false` if not.
     */
    suspend fun replaceDatabase(newDatabaseFile: BusStopDatabaseFile): Boolean
}