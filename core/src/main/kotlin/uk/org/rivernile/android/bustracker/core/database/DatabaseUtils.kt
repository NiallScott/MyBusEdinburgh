/*
 * Copyright (C) 2019 - 2023 Niall 'Rivernile' Scott
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

import okio.IOException
import java.io.File

/**
 * This interface contains methods for performing common database actions.
 *
 * @author Niall Scott
 */
interface DatabaseUtils {

    /**
     * Create a temporary [File] where database updates should be performed. This [File] will have
     * [File.deleteOnExit] already called on it, so it will be deleted when the VM dies.
     *
     * @param prefix The filename prefix of the temporary file.
     * @return A [File] pointing to the new temporary file.
     * @throws IOException When the temporary file could not be created.
     */
    @Throws(IOException::class)
    suspend fun createTemporaryFile(prefix: String): File

    /**
     * Given the name of a database, return a [File] object representing this database on the file
     * system.
     *
     * @param dbFileName The name of the database.
     * @return A [File] object representing the database on the file system.
     */
    fun getDatabasePath(dbFileName: String): File
}