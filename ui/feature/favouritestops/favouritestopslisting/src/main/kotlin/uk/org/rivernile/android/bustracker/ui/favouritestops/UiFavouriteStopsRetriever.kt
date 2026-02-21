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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import javax.inject.Inject

/**
 * This retrieves [UiFavouriteStop]s, ready to be displayed on the UI.
 *
 * @author Niall Scott
 */
internal interface UiFavouriteStopsRetriever {

    /**
     * A [Flow] which emits [List] of [UiFavouriteStop], which are ready to be displayed on the UI.
     * This may emit `null` when there are no favourite stops.
     */
    val allFavouriteStopsFlow: Flow<List<UiFavouriteStop>?>
}

internal class RealUiFavouriteStopsRetriever @Inject constructor(
    private val favouriteStopsRetriever: FavouriteStopsRetriever,
    private val servicesRepository: ServicesRepository,
    private val dropdownMenuGenerator: UiFavouriteDropdownMenuGenerator
) : UiFavouriteStopsRetriever {

    override val allFavouriteStopsFlow get() =
        combine(
            _allFavouriteStopsFlow,
            servicesRepository.getColoursForServicesFlow(),
            ::createUiFavouriteStops
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _allFavouriteStopsFlow get() = favouriteStopsRetriever
        .allFavouriteStopsFlow
        .flatMapLatest(::getFavouritesWithDropdownMenusFlow)

    private fun createUiFavouriteStops(
        favouriteStopsWithDropdownMenus: FavouritesWithDropdownMenus?,
        serviceColours: Map<ServiceDescriptor, ServiceColours>?
    ): List<UiFavouriteStop>? {
        return favouriteStopsWithDropdownMenus
            ?.favouriteStops
            ?.ifEmpty { null }
            ?.toUiFavouriteStops(
                serviceColours = serviceColours,
                dropdownMenus = favouriteStopsWithDropdownMenus.dropdownMenus
            )
    }

    private fun getFavouritesWithDropdownMenusFlow(
        favouriteStops: List<FavouriteStopWithServices>?
    ): Flow<FavouritesWithDropdownMenus?> {
        val stopIdentifiers = favouriteStops?.map { it.stopIdentifier }?.toSet()

        return if (!stopIdentifiers.isNullOrEmpty()) {
            dropdownMenuGenerator
                .getDropdownMenuItemsForStopsFlow(stopIdentifiers)
                .map {
                    FavouritesWithDropdownMenus(
                        favouriteStops = favouriteStops,
                        dropdownMenus = it
                    )
                }
        } else {
            flowOf(null)
        }
    }

    private data class FavouritesWithDropdownMenus(
        val favouriteStops: List<FavouriteStopWithServices>,
        val dropdownMenus: Map<StopIdentifier, UiFavouriteDropdownMenu>?
    )
}
