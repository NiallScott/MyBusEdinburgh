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

package uk.org.rivernile.android.bustracker.ui.search

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import uk.org.rivernile.android.bustracker.ui.alerts.UiAlertDropdownMenuItemMultipleStopsRetriever
import uk.org.rivernile.android.bustracker.ui.alerts.UiArrivalAlertDropdownMenuItem
import uk.org.rivernile.android.bustracker.ui.alerts.UiProximityAlertDropdownMenuItem
import uk.org.rivernile.android.bustracker.ui.favouritestops.UiFavouriteStopDropdownMenuItem
import uk.org.rivernile.android.bustracker.ui.favouritestops.UiFavouriteStopDropdownMenuItemMultipleStopsRetriever
import javax.inject.Inject

/**
 * This generates a [UiStopSearchResultDropdownMenu] for all displayed stops.
 *
 * @author Niall Scott
 */
internal interface UiStopSearchResultDropdownMenuGenerator {

    /**
     * For the given [Set] of [stopIdentifiers], return a [Flow] which emits a mapping of the
     * supplied stop identifiers to its associated [UiStopSearchResultDropdownMenu], if available.
     *
     * @param stopIdentifiers The stop identifiers to get [UiStopSearchResultDropdownMenu]s for.
     * @return A [Flow] which emits a mapping of the supplied stop identifiers to its associated
     * [UiStopSearchResultDropdownMenu], if available.
     */
    fun getDropdownMenuItemsForStopsFlow(
        stopIdentifiers: Set<StopIdentifier>
    ): Flow<Map<StopIdentifier, UiStopSearchResultDropdownMenu>?>
}

internal class RealUiStopSearchResultDropdownMenuGenerator @Inject constructor(
    private val featureRepository: FeatureRepository,
    private val favouriteMenuItemRetriever: UiFavouriteStopDropdownMenuItemMultipleStopsRetriever,
    private val alertMenuItemsRetriever: UiAlertDropdownMenuItemMultipleStopsRetriever
) : UiStopSearchResultDropdownMenuGenerator {

    private val hasStopMapFeature by lazy { featureRepository.hasStopMapUiFeature }

    override fun getDropdownMenuItemsForStopsFlow(
        stopIdentifiers: Set<StopIdentifier>
    ): Flow<Map<StopIdentifier, UiStopSearchResultDropdownMenu>?> {
        return if (stopIdentifiers.isNotEmpty()) {
            combine(
                favouriteMenuItemRetriever.getUiFavouriteStopDropdownMenuItemsFlow(stopIdentifiers),
                alertMenuItemsRetriever.getUiArrivalAlertDropdownMenuItemsFlow(stopIdentifiers),
                alertMenuItemsRetriever.getUiProximityAlertDropdownMenuItemsFlow(stopIdentifiers)
            ) { favouriteMenus, arrivalAlertMenus, proxAlertMenus ->
                createDropdownMenusForStops(
                    stopIdentifiers = stopIdentifiers,
                    favouriteStopMenuItems = favouriteMenus,
                    arrivalAlertMenuItems = arrivalAlertMenus,
                    proximityAlertMenuItems = proxAlertMenus
                )
            }
        } else {
            flowOf(null)
        }
    }

    private fun createDropdownMenusForStops(
        stopIdentifiers: Set<StopIdentifier>,
        favouriteStopMenuItems: Map<StopIdentifier, UiFavouriteStopDropdownMenuItem>?,
        arrivalAlertMenuItems: Map<StopIdentifier, UiArrivalAlertDropdownMenuItem>?,
        proximityAlertMenuItems: Map<StopIdentifier, UiProximityAlertDropdownMenuItem>?,
    ): Map<StopIdentifier, UiStopSearchResultDropdownMenu> {
        return stopIdentifiers
            .associateWith { stopIdentifier ->
                UiStopSearchResultDropdownMenu(
                    favouriteStopDropdownItem = favouriteStopMenuItems?.get(stopIdentifier),
                    arrivalAlertDropdownItem = arrivalAlertMenuItems?.get(stopIdentifier),
                    proximityAlertDropdownItem = proximityAlertMenuItems?.get(stopIdentifier),
                    isStopMapItemShown = hasStopMapFeature
                )
            }
    }
}
