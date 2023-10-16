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

package uk.org.rivernile.android.bustracker.core.favourites

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import uk.org.rivernile.android.bustracker.core.database.settings.favouritestops.FavouriteStopEntity
import uk.org.rivernile.android.bustracker.core.database.settings.favouritestops.FavouriteStopEntityFactory
import uk.org.rivernile.android.bustracker.core.database.settings.favouritestops.FavouriteStopsDao
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This repository is used to access favourites data.
 *
 * @param favouriteStopsDao The DAO to access the favourites stops data store.
 * @param entityFactory Used to create entities for the [FavouriteStopsDao].
 * @author Niall Scott
 */
@Singleton
class FavouritesRepository @Inject internal constructor(
    private val favouriteStopsDao: FavouriteStopsDao,
    private val entityFactory: FavouriteStopEntityFactory) {

    /**
     * Add a new favourite stop, or if it already exists, update the stop with new data.
     *
     * @param favouriteStop The [FavouriteStop] to add or update.
     */
    suspend fun addOrUpdateFavouriteStop(favouriteStop: FavouriteStop) {
        val entity = entityFactory.createFavouriteStopEntity(
            favouriteStop.stopCode,
            favouriteStop.stopName)

        favouriteStopsDao.addOrUpdateFavouriteStop(entity)
    }

    /**
     * Remove an existing favourite stop.
     *
     * @param stopCode The saved favourite with this stop code to remove.
     */
    suspend fun removeFavouriteStop(stopCode: String) {
        favouriteStopsDao.removeFavouriteStop(stopCode)
    }

    /**
     * Get a [Flow] which returns whether the given `stopCode` is added as a favourite or not, and
     * will emit further items when the status changes.
     *
     * @param stopCode The `stopCode` to watch.
     * @return The [Flow] which emits the favourite status of the given `stopCode`.
     */
    fun isStopAddedAsFavouriteFlow(stopCode: String): Flow<Boolean> =
        favouriteStopsDao
            .isStopAddedAsFavouriteFlow(stopCode)
            .distinctUntilChanged()

    /**
     * Get a [Flow] which emits [FavouriteStop] objects for the given `stopCode`. `null` will be
     * emitted if the [FavouriteStop] does not exist.
     *
     * @param stopCode The `stopCode` to watch.
     * @return The [Flow] which emits the [FavouriteStop]s for the given `stopCode`.
     */
    fun getFavouriteStopFlow(stopCode: String): Flow<FavouriteStop?> =
        favouriteStopsDao
            .getFavouriteStopFlow(stopCode)
            .distinctUntilChanged()
            .map(this::mapToFavouriteStop)

    /**
     * Get a [Flow] which emits [List]s of the user saved [FavouriteStop]s. `null` will be emitted
     * if there was an error or there are no items.
     */
    val allFavouriteStopsFlow: Flow<List<FavouriteStop>?> get() =
        favouriteStopsDao
            .allFavouriteStopsFlow
            .distinctUntilChanged()
            .map { entities ->
                entities
                    ?.mapNotNull(this::mapToFavouriteStop)
                    ?.ifEmpty { null }
            }

    /**
     * Map a given [FavouriteStopEntity] to a [FavouriteStop].
     *
     * @param entity The entity to map.
     * @return The mapped [FavouriteStop].
     */
    private fun mapToFavouriteStop(entity: FavouriteStopEntity?): FavouriteStop? {
        return entity?.let {
            FavouriteStop(
                it.stopCode,
                it.stopName)
        }
    }
}