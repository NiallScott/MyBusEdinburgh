/*
 * Copyright (C) 2024 - 2026 Niall 'Rivernile' Scott
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
 * A fake [FavouriteStopsDao] to be used in testing.
 *
 * @author Niall Scott
 */
class FakeFavouriteStopsDao(
    private val onIsStopAddedAsFavouriteFlow: (StopIdentifier) -> Flow<Boolean> =
        { throw NotImplementedError() },
    private val onGetFavouriteStopFlow: (StopIdentifier) -> Flow<FavouriteStop?> =
        { throw NotImplementedError() },
    private val onAllFavouriteStopsFlow: () -> Flow<List<FavouriteStop>?> =
        { throw NotImplementedError() }
) : FavouriteStopsDao {

    val addedOrUpdatedFavouriteStops get() = _addedOrUpdatedFavouriteStops.toList()
    private val _addedOrUpdatedFavouriteStops = mutableListOf<FavouriteStop>()

    val removedFavouriteStops get() = _removedFavouriteStops.toList()
    private val _removedFavouriteStops = mutableListOf<StopIdentifier>()

    override suspend fun addOrUpdateFavouriteStop(favouriteStop: FavouriteStop) {
        _addedOrUpdatedFavouriteStops += favouriteStop
    }

    override suspend fun removeFavouriteStop(stopIdentifier: StopIdentifier) {
        _removedFavouriteStops += stopIdentifier
    }

    override fun isStopAddedAsFavouriteFlow(stopIdentifier: StopIdentifier) =
        onIsStopAddedAsFavouriteFlow(stopIdentifier)

    override fun getFavouriteStopFlow(stopIdentifier: StopIdentifier) =
        onGetFavouriteStopFlow(stopIdentifier)

    override val allFavouriteStopsFlow: Flow<List<FavouriteStop>?>
        get() = onAllFavouriteStopsFlow()
}
