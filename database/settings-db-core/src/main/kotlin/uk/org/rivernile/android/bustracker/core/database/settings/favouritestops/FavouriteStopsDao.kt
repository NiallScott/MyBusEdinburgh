/*
 * Copyright (C) 2020 - 2023 Niall 'Rivernile' Scott
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

import kotlinx.coroutines.flow.Flow

/**
 * This DAO is used to access favourites created in the app.
 *
 * @author Niall Scott
 */
interface FavouriteStopsDao {

    /**
     * Add a stop as a favourite.
     *
     * @param favouriteStop The stop to add as a favourite.
     */
    suspend fun addOrUpdateFavouriteStop(favouriteStop: FavouriteStopEntity)

    /**
     * Remove an existing favourite stop.
     *
     * @param stopCode The saved favourite with this stop code to remove.
     */
    suspend fun removeFavouriteStop(stopCode: String)

    /**
     * Get a [Flow] which emits whether the given [stopCode] is added as a favourite stop or not.
     *
     * @param stopCode The stop code to determine if it is added as a favourite stop or not.
     * @return A [Flow] which emits whether the given [stopCode] is added as a favourite stop or
     * not.
     */
    fun isStopAddedAsFavouriteFlow(stopCode: String): Flow<Boolean>

    /**
     * Get a [Flow] which emits a specific user-saved favourite stop. This will emit `null` if the
     * [stopCode] is not a favourite stop.
     *
     * @param stopCode The code of the stop to obtain from favourites.
     * @return a [Flow] which emits a specific user-saved favourite stop. This will emit `null` if
     * the [stopCode] is not a favourite stop.
     */
    fun getFavouriteStopFlow(stopCode: String): Flow<FavouriteStopEntity?>

    /**
     * Get a [Flow] which emits all user-saved favourite stops. This will emit `null` when there
     * are no items.
     */
    val allFavouriteStopsFlow: Flow<List<FavouriteStopEntity>?>
}