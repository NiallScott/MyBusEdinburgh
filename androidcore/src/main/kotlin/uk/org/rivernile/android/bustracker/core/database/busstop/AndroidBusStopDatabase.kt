/*
 * Copyright (C) 2023 Niall 'Rivernile' Scott
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

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.MutableStateFlow
import uk.org.rivernile.android.bustracker.core.database.busstop.migrations.Migration1To2
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author Niall Scott
 */
@Singleton
internal class AndroidBusStopDatabase @Inject constructor(
    private val context: Context,
    private val migration1To2: Migration1To2,
    private val openHelperFactory: VersionCheckOpenHelperFactory) {

    companion object {

        private const val DATABASE_NAME = "busstops10.db"
    }

    private val isDatabaseOpenStateFlow = MutableStateFlow(false)

    suspend fun replaceDatabase(newDatabaseFile: File) {

    }

    private fun createRoomDatabase(): RoomBusStopDatabase {
        return Room.databaseBuilder(context, RoomBusStopDatabase::class.java, DATABASE_NAME)
            .openHelperFactory(openHelperFactory)
            .createFromAsset(DATABASE_NAME)
            .addMigrations(migration1To2)
            .build()
    }
}