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

package uk.org.rivernile.android.bustracker.core.database.settings.favouritestops

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * This is the Room-specific implementation of [FavouriteStopsDao].
 *
 * @author Niall Scott
 */
@Dao
internal abstract class RoomFavouriteStopsDao : FavouriteStopsDao {

    override suspend fun addOrUpdateFavouriteStop(favouriteStop: FavouriteStopEntity) {
        if (favouriteStop is RoomFavouriteStopEntity) {
            addOrUpdateFavouriteStopInternal(favouriteStop)
        }
    }

    @Query("""
        DELETE FROM favourite_stops 
        WHERE stopCode = :stopCode
    """)
    abstract override suspend fun removeFavouriteStop(stopCode: String)

    override fun isStopAddedAsFavouriteFlow(stopCode: String) =
        isStopAddedAsFavouriteFlowInternal(stopCode)
            .map { it > 0 }

    override fun getFavouriteStopFlow(stopCode: String): Flow<FavouriteStopEntity?> =
        getFavouriteStopFlowInternal(stopCode)

    override val allFavouriteStopsFlow: Flow<List<FavouriteStopEntity>?> get() =
        allFavouriteStopsFlowInternal

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun addOrUpdateFavouriteStopInternal(favouriteStop: RoomFavouriteStopEntity)

    @Query("""
        SELECT COUNT(*) 
        FROM favourite_stops 
        WHERE stopCode = :stopCode
    """)
    abstract fun isStopAddedAsFavouriteFlowInternal(stopCode: String): Flow<Int>

    @Query("""
        SELECT stopCode, stopName 
        FROM favourite_stops 
        WHERE stopCode = :stopCode 
        LIMIT 1
    """)
    abstract fun getFavouriteStopFlowInternal(stopCode: String): Flow<RoomFavouriteStopEntity?>

    @get:Query("""
        SELECT stopCode, stopName 
        FROM favourite_stops 
        ORDER BY stopName ASC
    """)
    abstract val allFavouriteStopsFlowInternal: Flow<List<RoomFavouriteStopEntity>?>
}