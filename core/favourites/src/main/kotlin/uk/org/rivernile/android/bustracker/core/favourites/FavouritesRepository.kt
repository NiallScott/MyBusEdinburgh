/*
 * Copyright (C) 2020 - 2026 Niall 'Rivernile' Scott
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
import uk.org.rivernile.android.bustracker.core.database.settings.favouritestops.FavouriteStopsDao
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
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
     * @param stopIdentifier The saved favourite with this stop identifier to remove.
     */
    public suspend fun removeFavouriteStop(stopIdentifier: StopIdentifier)

    /**
     * Get a [Flow] which returns whether the given [stopIdentifier] is added as a favourite or not,
     * and will emit further items when the status changes.
     *
     * @param stopIdentifier The stop identifier to watch.
     * @return The [Flow] which emits the favourite status of the given [stopIdentifier].
     */
    public fun isStopAddedAsFavouriteFlow(stopIdentifier: StopIdentifier): Flow<Boolean>

    /**
     * Get a [Flow] which emits [FavouriteStop] objects for the given [stopIdentifier]. `null` will
     * be emitted if the [FavouriteStop] does not exist.
     *
     * @param stopIdentifier The stop identifier to watch.
     * @return The [Flow] which emits the [FavouriteStop]s for the given [stopIdentifier].
     */
    public fun getFavouriteStopFlow(stopIdentifier: StopIdentifier): Flow<FavouriteStop?>

    /**
     * Get a [Flow] which emits [List]s of the user saved [FavouriteStop]s. `null` will be emitted
     * if there was an error or there are no items.
     */
    public val allFavouriteStopsFlow: Flow<List<FavouriteStop>?>

    /**
     * Get a [Flow] which emits a [Set] containing the [StopIdentifier]s of all currently saved
     * favourite stops.
     */
    public val allFavouriteStopsStopIdentifiersFlow: Flow<Set<StopIdentifier>?>
}

@Singleton
internal class DefaultFavouritesRepository @Inject constructor(
    private val favouriteStopsDao: FavouriteStopsDao
) : FavouritesRepository {

    override suspend fun addOrUpdateFavouriteStop(favouriteStop: FavouriteStop) {
        favouriteStopsDao.addOrUpdateFavouriteStop(favouriteStop.toDatabaseFavouriteStop())
    }

    override suspend fun removeFavouriteStop(stopIdentifier: StopIdentifier) {
        favouriteStopsDao.removeFavouriteStop(stopIdentifier)
    }

    override fun isStopAddedAsFavouriteFlow(stopIdentifier: StopIdentifier) =
        favouriteStopsDao
            .isStopAddedAsFavouriteFlow(stopIdentifier)
            .distinctUntilChanged()

    override fun getFavouriteStopFlow(stopIdentifier: StopIdentifier) =
        favouriteStopsDao
            .getFavouriteStopFlow(stopIdentifier)
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

    override val allFavouriteStopsStopIdentifiersFlow get() =
        favouriteStopsDao
            .allFavouriteStopsStopIdentifiersFlow
            .distinctUntilChanged()
            .map {
                it?.toSet()
            }
}
