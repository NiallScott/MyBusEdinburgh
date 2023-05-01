/*
 * Copyright (C) 2021 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.favourites

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import uk.org.rivernile.android.bustracker.core.favourites.FavouriteStop
import uk.org.rivernile.android.bustracker.core.favourites.FavouritesRepository
import uk.org.rivernile.android.bustracker.core.servicestops.ServiceStopsRepository
import javax.inject.Inject

/**
 * This class is used to retrieve [UiFavouriteStop]s for display on the UI. It contains the logic to
 * tie [UiFavouriteStop]s together with the service listing for each stop.
 *
 * @param favouritesRepository Used to retrieve favourite stop data.
 * @param serviceStopsRepository Used to retrieve the service listing for the stops.
 * @author Niall Scott
 */
class FavouriteStopsRetriever @Inject constructor(
        private val favouritesRepository: FavouritesRepository,
        private val serviceStopsRepository: ServiceStopsRepository) {

    /**
     * This produces a [Flow] which emits [List]s of [UiFavouriteStop]s.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val allFavouriteStopsFlow: Flow<List<UiFavouriteStop>?> get() =
            favouritesRepository.allFavouriteStopsFlow
                    .flatMapLatest(this::loadServices)
                    .onStart { emit(null) }

    /**
     * Given a [List] of [FavouriteStop]s, load the service listing for each favourite stop and
     * combine in to a new [Flow] which is a [List] of [UiFavouriteStop].
     *
     * If the input favourites is `null` or empty, a [Flow] will be returned which only emits an
     * empty [List].
     *
     * @param favourites The user's saved favourites. This may be `null` or empty.
     * @return A [Flow] which emits the user's favourites combined with the service names listing,
     * or a [Flow] which emits an empty [List] if the user's favourites is `null` or empty.
     */
    private fun loadServices(favourites: List<FavouriteStop>?): Flow<List<UiFavouriteStop>?> {
        return favourites?.takeIf(List<FavouriteStop>::isNotEmpty)?.let { f ->
            val stopCodes = f.map(FavouriteStop::stopCode).toHashSet()

            serviceStopsRepository.getServicesForStopsFlow(stopCodes)
                    .map { combineFavouritesAndServices(f, it) }
        } ?: flowOf(emptyList())
    }

    /**
     * Given a [List] of [FavouriteStop]s and a [Map] of stop codes to service listings, combine
     * them together to create a [List] of [UiFavouriteStop] for display on the UI.
     *
     * @param favouriteStops The user's favourite stops.
     * @param stopServices A [Map] of stop code to service listing for the favourite stops, to be
     * combined with the favourite stops for display on the UI.
     * @return A [List] of [UiFavouriteStop]s, which is the favourites combined with the service
     * listing for that stop.
     */
    private fun combineFavouritesAndServices(
            favouriteStops: List<FavouriteStop>,
            stopServices: Map<String, List<String>>?) =
            favouriteStops.map {
                UiFavouriteStop(
                        it,
                        stopServices?.get(it.stopCode),
                        false)
            }
}