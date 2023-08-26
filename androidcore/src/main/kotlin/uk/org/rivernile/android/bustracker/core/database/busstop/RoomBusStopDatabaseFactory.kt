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
import uk.org.rivernile.android.bustracker.core.database.busstop.migrations.Migration1To2
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class is a factory to create [RoomBusStopDatabase] objects.
 *
 * @param context The application [Context].
 * @param migration1To2 An implementation which migrates the database from version 1 to 2.
 * @param bundledDatabaseOpenHelperFactory The open helper used to copy the bundled database if
 * required.
 * @author Niall Scott
 */
@Singleton
internal class RoomBusStopDatabaseFactory @Inject constructor(
    private val context: Context,
    private val migration1To2: Migration1To2,
    private val bundledDatabaseOpenHelperFactory: BundledDatabaseOpenHelperFactory) {

    /**
     * Create an instance of [RoomBusStopDatabase].
     *
     * @param databaseName The name of the database. This is the name of the file contained within
     * the database path.
     * @param allowAssetExtraction Should it be allowed for the database to be extracted from assets
     * if it doesn't yet exist?
     * @return A new [RoomBusStopDatabase] instance.
     */
    fun createRoomBusStopDatabase(
        databaseName: String,
        allowAssetExtraction: Boolean): RoomBusStopDatabase {
        val builder = Room.databaseBuilder(context, RoomBusStopDatabase::class.java, databaseName)
            .addMigrations(migration1To2)

        if (allowAssetExtraction) {
            builder.openHelperFactory(bundledDatabaseOpenHelperFactory)
        }

        return builder.build()
    }
}