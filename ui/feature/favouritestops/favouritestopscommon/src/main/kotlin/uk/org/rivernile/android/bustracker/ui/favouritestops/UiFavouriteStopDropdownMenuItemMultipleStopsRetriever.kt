/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.favourites.FavouritesRepository
import javax.inject.Inject

/**
 * This is used to retrieve favourite stop menu items when multiple stops are being referenced.
 *
 * @author Niall Scott
 */
public interface UiFavouriteStopDropdownMenuItemMultipleStopsRetriever {

    /**
     * Get a mapping of [StopIdentifier] to [UiFavouriteStopDropdownMenuItem] for all requested
     * [stopIdentifiers] which creates the arrival alert menu item data.
     *
     * @param stopIdentifiers The [StopIdentifier]s to get [UiFavouriteStopDropdownMenuItem] items
     * for.
     * @return The mapping of [StopIdentifier] to [UiFavouriteStopDropdownMenuItem]s for all
     * requested [stopIdentifiers].
     */
    public fun getUiFavouriteStopDropdownMenuItemsFlow(
        stopIdentifiers: Set<StopIdentifier>
    ): Flow<Map<StopIdentifier, UiFavouriteStopDropdownMenuItem>?>
}

internal class RealUiFavouriteStopDropdownMenuItemMultipleStopsRetriever @Inject constructor(
    private val favouritesRepository: FavouritesRepository
) : UiFavouriteStopDropdownMenuItemMultipleStopsRetriever {

    override fun getUiFavouriteStopDropdownMenuItemsFlow(
        stopIdentifiers: Set<StopIdentifier>
    ): Flow<Map<StopIdentifier, UiFavouriteStopDropdownMenuItem>?> {
        return if (stopIdentifiers.isNotEmpty()) {
            favouritesRepository
                .allFavouriteStopsStopIdentifiersFlow
                .map {
                    createFavouriteStopMenuItems(
                        requestedStopIdentifiers = stopIdentifiers,
                        favouriteStopsStopIdentifiers = it
                    )
                }
                .distinctUntilChanged()
        } else {
            flowOf(null)
        }
    }

    private fun createFavouriteStopMenuItems(
        requestedStopIdentifiers: Set<StopIdentifier>,
        favouriteStopsStopIdentifiers: Set<StopIdentifier>?
    ): Map<StopIdentifier, UiFavouriteStopDropdownMenuItem>? {
        return requestedStopIdentifiers
            .associateWith {
                UiFavouriteStopDropdownMenuItem(
                    isFavouriteStop = favouriteStopsStopIdentifiers?.contains(it) ?: false
                )
            }
            .ifEmpty { null }
    }
}
