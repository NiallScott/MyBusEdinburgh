/*
 * Copyright (C) 2025 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.favouritestops

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import uk.org.rivernile.android.bustracker.core.favourites.FavouriteStop
import uk.org.rivernile.android.bustracker.core.favourites.FavouritesRepository
import uk.org.rivernile.android.bustracker.core.servicestops.ServiceStopsRepository
import javax.inject.Inject

/**
 * This class is used to retrieve [FavouriteStopWithServices]s. It contains the logic to
 * tie [FavouriteStop]s together with the service listing for each stop.
 *
 * @author Niall Scott
 */
internal interface FavouriteStopsRetriever {

    /**
     * This produces a [Flow] which emits [List]s of [FavouriteStopWithServices].
     */
    val allFavouriteStopsFlow: Flow<List<FavouriteStopWithServices>?>
}

internal class RealFavouriteStopsRetriever @Inject constructor(
    private val favouritesRepository: FavouritesRepository,
    private val serviceStopsRepository: ServiceStopsRepository
) : FavouriteStopsRetriever {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val allFavouriteStopsFlow get() = favouritesRepository
        .allFavouriteStopsFlow
        .flatMapLatest(::loadServices)

    private fun loadServices(
        favourites: List<FavouriteStop>?
    ): Flow<List<FavouriteStopWithServices>?> {
        return favourites
            ?.ifEmpty { null }
            ?.let { favourites ->
                serviceStopsRepository
                    .getServicesForStopsFlow(favourites.toStopIdentifiersSet())
                    .map(favourites::toFavouriteStopsWithServices)
            }
            ?: flowOf(null)
    }

    private fun List<FavouriteStop>.toStopIdentifiersSet() =
        map(FavouriteStop::stopIdentifier).toHashSet()
}
