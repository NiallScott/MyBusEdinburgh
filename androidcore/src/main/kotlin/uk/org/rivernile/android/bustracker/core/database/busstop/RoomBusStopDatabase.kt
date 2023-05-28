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

import androidx.room.Database
import androidx.room.RoomDatabase
import uk.org.rivernile.android.bustracker.core.database.busstop.database.RoomDatabaseDao
import uk.org.rivernile.android.bustracker.core.database.busstop.database.RoomDatabaseInfoEntity
import uk.org.rivernile.android.bustracker.core.database.busstop.service.RoomServiceDao
import uk.org.rivernile.android.bustracker.core.database.busstop.service.RoomServiceEntity
import uk.org.rivernile.android.bustracker.core.database.busstop.servicepoint.RoomServicePointDao
import uk.org.rivernile.android.bustracker.core.database.busstop.servicepoint.RoomServicePointEntity
import uk.org.rivernile.android.bustracker.core.database.busstop.servicestop.RoomServiceStopDao
import uk.org.rivernile.android.bustracker.core.database.busstop.servicestop.RoomServiceStopEntity
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.RoomStopDao
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.RoomStopEntity

/**
 * This is the Room implementation of the bus stop database.
 *
 * @author Niall Scott
 */
@Database(
    version = 2,
    entities = [
        RoomDatabaseInfoEntity::class,
        RoomServiceEntity::class,
        RoomServicePointEntity::class,
        RoomServiceStopEntity::class,
        RoomStopEntity::class
    ]
)
internal abstract class RoomBusStopDatabase : RoomDatabase() {

    /**
     * The [RoomDatabaseDao].
     */
    abstract val databaseDao: RoomDatabaseDao

    /**
     * The [RoomServiceDao].
     */
    abstract val serviceDao: RoomServiceDao

    /**
     * The [RoomServicePointDao].
     */
    abstract val servicePointDao: RoomServicePointDao

    /**
     * The [RoomServiceStopDao].
     */
    abstract val serviceStopDao: RoomServiceStopDao

    /**
     * The [RoomStopDao].
     */
    abstract val stopDao: RoomStopDao
}