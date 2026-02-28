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

import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForViewModelCoroutineScope
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import uk.org.rivernile.android.bustracker.ui.alerts.UiAlertDropdownMenuItemMultipleStopsRetriever
import uk.org.rivernile.android.bustracker.ui.alerts.UiArrivalAlertDropdownMenuItem
import uk.org.rivernile.android.bustracker.ui.alerts.UiProximityAlertDropdownMenuItem
import javax.inject.Inject

/**
 * This generates a [UiFavouriteDropdownMenu] for a selected stop.
 *
 * @author Niall Scott
 */
internal interface UiFavouriteDropdownMenuGenerator {

    /**
     * For the given [Set] of [stopIdentifiers], return a [Flow] which emits a mapping of the
     * supplied stop identifiers to its associated [UiFavouriteDropdownMenu], if available.
     *
     * @param stopIdentifiers The stop identifiers to get [UiFavouriteDropdownMenu]s for.
     * @return A [Flow] which emits a mapping of the supplied stop identifiers to its associated
     * [UiFavouriteDropdownMenu], if available.
     */
    fun getDropdownMenuItemsForStopsFlow(
        stopIdentifiers: Set<StopIdentifier>
    ): Flow<Map<StopIdentifier, UiFavouriteDropdownMenu>?>
}

@ViewModelScoped
internal class RealUiFavouriteDropdownMenuGenerator @Inject constructor(
    private val arguments: Arguments,
    private val state: State,
    private val featureRepository: FeatureRepository,
    private val alertMenuItemsRetriever: UiAlertDropdownMenuItemMultipleStopsRetriever,
    @param:ForDefaultDispatcher private val defaultCoroutineDispatcher: CoroutineDispatcher,
    @param:ForViewModelCoroutineScope private val viewModelCoroutineScope: CoroutineScope
) : UiFavouriteDropdownMenuGenerator {

    private val hasStopMapFeature by lazy { featureRepository.hasStopMapUiFeature }
    private val hasShortcutFeature by lazy { featureRepository.hasPinShortcutFeature }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getDropdownMenuItemsForStopsFlow(
        stopIdentifiers: Set<StopIdentifier>
    ): Flow<Map<StopIdentifier, UiFavouriteDropdownMenu>?> {
        return if (stopIdentifiers.isNotEmpty()) {
            arguments
                .isShortcutModeFlow
                .flatMapLatest {
                    getDropdownMenuItemsForStopsFlow(
                        stopIdentifiers = stopIdentifiers,
                        isShortcutMode = it
                    )
                }
        } else {
            flowOf(null)
        }
    }

    private fun getDropdownMenuItemsForStopsFlow(
        stopIdentifiers: Set<StopIdentifier>,
        isShortcutMode: Boolean
    ): Flow<Map<StopIdentifier, UiFavouriteDropdownMenu>?> {
        return if (!isShortcutMode) {
            combine(
                state.selectedStopIdentifierFlow,
                alertMenuItemsRetriever.getUiArrivalAlertDropdownMenuItemsFlow(stopIdentifiers),
                alertMenuItemsRetriever.getUiProximityAlertDropdownMenuItemsFlow(stopIdentifiers)
            ) { selectedStopIdentifier, arrivalAlertMenuItems, proximityAlertMenuItems ->
                createDropdownMenusForStops(
                    stopIdentifiers = stopIdentifiers,
                    arrivalAlertMenuItems = arrivalAlertMenuItems,
                    proximityAlertMenuItems = proximityAlertMenuItems,
                    selectedStopIdentifier = selectedStopIdentifier
                )
            }
        } else {
            flowOf(null)
        }
    }

    private fun createDropdownMenusForStops(
        stopIdentifiers: Set<StopIdentifier>,
        arrivalAlertMenuItems: Map<StopIdentifier, UiArrivalAlertDropdownMenuItem>?,
        proximityAlertMenuItems: Map<StopIdentifier, UiProximityAlertDropdownMenuItem>?,
        selectedStopIdentifier: StopIdentifier?
    ): Map<StopIdentifier, UiFavouriteDropdownMenu> {
        return stopIdentifiers
            .associateWith { stopIdentifier ->
                UiFavouriteDropdownMenu(
                    isShown = stopIdentifier == selectedStopIdentifier,
                    isShortcutItemShown = hasShortcutFeature,
                    arrivalAlertDropdownItem = arrivalAlertMenuItems?.get(stopIdentifier),
                    proximityAlertDropdownItem = proximityAlertMenuItems?.get(stopIdentifier),
                    isStopMapItemShown = hasStopMapFeature
                )
            }
    }
}
