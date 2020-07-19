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

import com.google.gson.annotations.SerializedName

/**
 * This defines the root of the JSON backup object.
 *
 * @property dbVersion The version of the database schema.
 * @property backupSchemaVersion The schema of the backup output.
 * @property createTime The UNIX timestamp, in milliseconds, when the backup was created.
 * @property favouriteStops A [List] of [BackupFavouriteStop]s to backup.
 * @author Niall Scott
 */
internal data class Backup(
        @SerializedName("dbVersion")
        val dbVersion: Int,
        @SerializedName("jsonSchemaVersion")
        val backupSchemaVersion: Int = 1,
        @SerializedName("createTime")
        val createTime: Long,
        @SerializedName("favouriteStops")
        val favouriteStops: List<BackupFavouriteStop>?)