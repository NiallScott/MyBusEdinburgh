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

package uk.org.rivernile.android.bustracker.core.backup

import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import java.io.File
import java.io.IOException
import javax.inject.Inject

/**
 * The implementation of backup persistence.
 *
 * @param gson An instance of [Gson] to serialise and deserialise data.
 * @author Niall Scott
 */
internal class BackupPersistence @Inject constructor(
        private val gson: Gson) {

    /**
     * Persist the backup data in to the specified [File].
     *
     * @param backupFile Where to persist the backup data.
     * @param backup The backup contents.
     */
    fun persistBackup(backupFile: File, backup: Backup) {
        try {
            backupFile.bufferedWriter().use {
                gson.toJson(backup, it)
            }
        } catch (ignored: IOException) {
            // Fail silently - no recovery path.
        } catch (ignored: JsonIOException) {
            // Fail silently - no recovery path.
        }
    }

    /**
     * Read backup data from the specified [File].
     *
     * @param backupFile The [File] containing the backup data.
     */
    fun readBackup(backupFile: File) = try {
        backupFile.bufferedReader().use {
            gson.fromJson(it, Backup::class.java)
        }
    } catch (ignored: IOException) {
        // Fail silently - no recovery path.
        null
    } catch (ignored: JsonIOException) {
        // Fail silently - no recovery path.
        null
    } catch (ignored: JsonSyntaxException) {
        // Fail silently - no recovery path.
        null
    }
}