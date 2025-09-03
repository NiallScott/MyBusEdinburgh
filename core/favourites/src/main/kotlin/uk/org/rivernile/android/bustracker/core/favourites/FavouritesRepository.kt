/*
 * Copyright (C) 2020 - 2025 Niall 'Rivernile' Scott
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
 * @author Niall Scott
 */
public interface FavouritesRepository {

    /**
     * Add a new favourite stop, or if it already exists, update the stop with new data.
     *
     * @param favouriteStop The [FavouriteStop] to add or update.
     */
    public suspend fun addOrUpdateFavouriteStop(favouriteStop: FavouriteStop)

    /**
     * Remove an existing favourite stop.
     *
     * @param stopCode The saved favourite with this stop code to remove.
     */
    public suspend fun removeFavouriteStop(stopCode: String)

    /**
     * Get a [Flow] which returns whether the given `stopCode` is added as a favourite or not, and
     * will emit further items when the status changes.
     *
     * @param stopCode The `stopCode` to watch.
     * @return The [Flow] which emits the favourite status of the given `stopCode`.
     */
    public fun isStopAddedAsFavouriteFlow(stopCode: String): Flow<Boolean>

    /**
     * Get a [Flow] which emits [FavouriteStop] objects for the given `stopCode`. `null` will be
     * emitted if the [FavouriteStop] does not exist.
     *
     * @param stopCode The `stopCode` to watch.
     * @return The [Flow] which emits the [FavouriteStop]s for the given `stopCode`.
     */
    public fun getFavouriteStopFlow(stopCode: String): Flow<FavouriteStop?>

    /**
     * Get a [Flow] which emits [List]s of the user saved [FavouriteStop]s. `null` will be emitted
     * if there was an error or there are no items.
     */
    public val allFavouriteStopsFlow: Flow<List<FavouriteStop>?>
}

@Singleton
internal class DefaultFavouritesRepository @Inject constructor(
    private val favouriteStopsDao: FavouriteStopsDao,
    private val entityFactory: FavouriteStopEntityFactory
) : FavouritesRepository {

    override suspend fun addOrUpdateFavouriteStop(favouriteStop: FavouriteStop) {
        val entity = entityFactory.createFavouriteStopEntity(
            favouriteStop.stopCode,
            favouriteStop.stopName
        )

        favouriteStopsDao.addOrUpdateFavouriteStop(entity)
    }

    override suspend fun removeFavouriteStop(stopCode: String) {
        favouriteStopsDao.removeFavouriteStop(stopCode)
    }

    override fun isStopAddedAsFavouriteFlow(stopCode: String) =
        favouriteStopsDao
            .isStopAddedAsFavouriteFlow(stopCode)
            .distinctUntilChanged()

    override fun getFavouriteStopFlow(stopCode: String) =
        favouriteStopsDao
            .getFavouriteStopFlow(stopCode)
            .distinctUntilChanged()
            .map { it?.toFavouriteStop() }

    override val allFavouriteStopsFlow get() =
        favouriteStopsDao
            .allFavouriteStopsFlow
            .distinctUntilChanged()
            .map { entities ->
                entities
                    ?.map { it.toFavouriteStop() }
                    ?.ifEmpty { null }
            }

    private fun FavouriteStopEntity.toFavouriteStop(): FavouriteStop {
        return FavouriteStop(
            stopCode = stopCode,
            stopName = stopName
        )
    }
}
