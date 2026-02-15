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

package uk.org.rivernile.android.bustracker.core.database.settings.favouritestops

import kotlinx.coroutines.flow.Flow
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier

/**
 * This DAO is used to access favourites created in the app.
 *
 * @author Niall Scott
 */
public interface FavouriteStopsDao {

    /**
     * Add a stop as a favourite or update it if it already exists.
     *
     * @param favouriteStop The stop to add as a favourite.
     */
    public suspend fun addOrUpdateFavouriteStop(favouriteStop: FavouriteStop)

    /**
     * Remove an existing favourite stop.
     *
     * @param stopIdentifier The saved favourite with this stop to remove.
     */
    public suspend fun removeFavouriteStop(stopIdentifier: StopIdentifier)

    /**
     * Get a [Flow] which emits whether the given [stopIdentifier] is added as a favourite stop or
     * not.
     *
     * @param stopIdentifier The stop code to determine if it is added as a favourite stop or not.
     * @return A [Flow] which emits whether the given [stopIdentifier] is added as a favourite stop
     * or not.
     */
    public fun isStopAddedAsFavouriteFlow(stopIdentifier: StopIdentifier): Flow<Boolean>

    /**
     * Get a [Flow] which emits a specific user-saved favourite stop. This will emit `null` if the
     * [stopIdentifier] is not a favourite stop.
     *
     * @param stopIdentifier The code of the stop to obtain from favourites.
     * @return a [Flow] which emits a specific user-saved favourite stop. This will emit `null` if
     * the [stopIdentifier] is not a favourite stop.
     */
    public fun getFavouriteStopFlow(stopIdentifier: StopIdentifier): Flow<FavouriteStop?>

    /**
     * Get a [Flow] which emits all user-saved favourite stops. This will emit `null` when there
     * are no items.
     */
    public val allFavouriteStopsFlow: Flow<List<FavouriteStop>?>
}
