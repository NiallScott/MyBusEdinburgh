/*
 * Copyright (C) 2021 - 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.favourites.addedit

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopName
import uk.org.rivernile.android.bustracker.core.favourites.FavouriteStop
import uk.org.rivernile.android.bustracker.core.favourites.FavouritesRepository
import javax.inject.Inject

/**
 * This class loads favourite stop details for [AddEditFavouriteStopDialogFragmentViewModel]. It
 * does this by obtaining the existing favourite, if it exists, from the data store, while also
 * getting the details for the stop from [BusStopsRepository] and combining them together for
 * display on the UI.
 *
 * @param favouritesRepository Used to access the user's saved favourite stops.
 * @param busStopsRepository Used to access stop data.
 * @author Niall Scott
 */
class FavouriteStopFetcher @Inject constructor(
    private val favouritesRepository: FavouritesRepository,
    private val busStopsRepository: BusStopsRepository) {

    /**
     * Load combined stop details for the given stop code.
     *
     * If the stop code is `null` or empty, [UiState.InProgress] will be the only state emitted.
     *
     * @param stopCode The stop code to get favourite information for.
     */
    fun loadFavouriteStopAndDetails(stopCode: String?): Flow<UiState> {
        return stopCode?.ifEmpty { null }?.let { sc ->
            createFavouriteStopFlow(sc).combine(createStopNameFlow(sc)) { favourite, name ->
                if (favourite is FavouriteResult.Item &&
                        name is StopNameResult.Item) {
                    favourite.favouriteStop?.let {
                        UiState.Mode.Edit(sc, name.stopName, it)
                    } ?: UiState.Mode.Add(sc, name.stopName)
                } else {
                    UiState.InProgress
                }
            }.distinctUntilChanged()
        } ?: flowOf(UiState.InProgress)
    }

    /**
     * Create a new [Flow] which loads the [FavouriteStop] for the given stop code and emits
     * [FavouriteResult] objects.
     *
     * @param stopCode The stop code to load the [FavouriteStop] for.
     * @return A new [Flow] which loads the [FavouriteStop] for the given stop code.
     */
    private fun createFavouriteStopFlow(stopCode: String): Flow<FavouriteResult> =
        favouritesRepository.getFavouriteStopFlow(stopCode)
            .map { FavouriteResult.Item(it) }
            .onStart<FavouriteResult> { emit(FavouriteResult.InProgress) }

    /**
     * Create a new [Flow] which loads the [StopName] for the given stop code and emits
     * [StopNameResult] objects.
     *
     * @param stopCode The stop code to load the [StopName] for.
     * @return A new [Flow] which loads the [StopName] for the given stop code.
     */
    private fun createStopNameFlow(stopCode: String): Flow<StopNameResult> =
        busStopsRepository.getNameForStopFlow(stopCode)
            .map { StopNameResult.Item(it) }
            .onStart<StopNameResult> { emit(StopNameResult.InProgress) }

    /**
     * This class encapsulates a result from loading a [FavouriteStop] from storage.
     *
     * @author Niall Scott
     */
    private sealed interface FavouriteResult {

        /**
         * Loading is in progress.
         */
        data object InProgress : FavouriteResult

        /**
         * The item has loaded.
         *
         * @property favouriteStop The loaded [FavouriteStop]. This may be `null` if it does not exist.
         */
        data class Item(
            val favouriteStop: FavouriteStop?) : FavouriteResult
    }

    /**
     * This class encapsulates a result from loading a [StopName] from storage.
     *
     * @author Niall Scott
     */
    private sealed interface StopNameResult {

        /**
         * Loading is in progress.
         */
        data object InProgress : StopNameResult

        /**
         * The item has loaded.
         *
         * @property stopName The loaded [StopName]. This may be `null` if it does not exist.
         */
        data class Item(
            val stopName: StopName?) : StopNameResult
    }
}