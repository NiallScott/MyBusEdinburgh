/*
 * Copyright (C) 2022 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.neareststops

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.favourites.FavouritesRepository
import javax.inject.Inject

/**
 * This class retrieves the favourites state and passes this state on to
 * [NearestStopsFragmentViewModel]. This class exists to reduce complexity of the view model
 * implementation, so logic is broken out in to here.
 *
 * @param favouritesRepository The favourites repository.
 * @author Niall Scott
 */
class FavouritesStateRetriever @Inject constructor(
    private val favouritesRepository: FavouritesRepository
) {

    /**
     * Get a [Flow] which uses the [selectedStopIdentifierFlow] as the currently selected stop and
     * this [Flow] emits whether the given stop is added as a favourite. `null` will be emitted when
     * loading and when there is no stop identifier.
     *
     * @param selectedStopIdentifierFlow A [Flow] which emits the currently selected stop.
     * @return A [Flow] which emits whether the selected stop is added as a favourite or not, or
     * emits `null` when loading or no stop identifier is selected.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getIsAddedAsFavouriteStopFlow(selectedStopIdentifierFlow: Flow<StopIdentifier?>) =
        selectedStopIdentifierFlow
            .flatMapLatest(this::loadIsFavouriteStop)

    /**
     * Load whether the given [stopIdentifier] is added as a user favourite or not. If the [stopIdentifier]
     * is `null` or empty, the returned [kotlinx.coroutines.flow.Flow] emits `null`. `null` will
     * also be emitted in lieu of a value while the status is loading.
     *
     * @param stopIdentifier The stop to get favourite status for.
     * @return A [kotlinx.coroutines.flow.Flow] which emits whether the given stop identifier is
     * added as a user favourite.
     */
    private fun loadIsFavouriteStop(stopIdentifier: StopIdentifier?) = stopIdentifier?.let {
        favouritesRepository.isStopAddedAsFavouriteFlow(it)
            .onStart<Boolean?> { emit(null) }
    } ?: flowOf(null)
}
