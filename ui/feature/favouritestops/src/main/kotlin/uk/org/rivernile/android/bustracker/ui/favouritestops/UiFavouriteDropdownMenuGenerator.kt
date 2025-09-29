/*
 * Copyright (C) 2025 Niall 'Rivernile' Scott
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

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import javax.inject.Inject

/**
 * This generates a [UiFavouriteDropdownMenu] for a selected stop.
 *
 * @author Niall Scott
 */
internal interface UiFavouriteDropdownMenuGenerator {

    /**
     * This emits a [Pair] of [String] (the stop code) to [UiFavouriteDropdownMenu] for a selected
     * stop. This may emit `null` when there is no selected stop.
     */
    val uiFavouriteDropdownItemsForStopFlow: Flow<Pair<String, UiFavouriteDropdownMenu>?>
}

internal class RealUiFavouriteDropdownMenuGenerator @Inject constructor(
    private val state: State,
    private val featureRepository: FeatureRepository,
    private val alertsRepository: AlertsRepository
) : UiFavouriteDropdownMenuGenerator {

    private val hasArrivalAlertFeature by lazy { featureRepository.hasArrivalAlertFeature }
    private val hasProximityAlertFeature by lazy { featureRepository.hasProximityAlertFeature }
    private val hasStopMapFeature by lazy { featureRepository.hasStopMapUiFeature }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val uiFavouriteDropdownItemsForStopFlow get() = state
        .selectedStopCodeFlow
        .flatMapLatest(::loadDropdownItemsForStopCode)

    private fun loadDropdownItemsForStopCode(
        stopCode: String?
    ): Flow<Pair<String, UiFavouriteDropdownMenu>?> {
        return stopCode?.ifBlank { null }?.let {
            createUiFavouriteDropdownItemsForStopFlow(it)
                .map { items ->
                    stopCode to UiFavouriteDropdownMenu(
                        items = items
                    )
                }
                .distinctUntilChanged()
                .onStart {
                    emit(createDefault(stopCode = it))
                }
        } ?: flowOf(null)
    }

    private fun createUiFavouriteDropdownItemsForStopFlow(stopCode: String) =
        combine(
            getHasArrivalAlertFlow(stopCode),
            getHasProximityAlertFlow(stopCode)
        ) { hasArrivalAlert, hasProximityAlert ->
            createUiFavouriteDropdownItems(
                isLoading = false,
                hasArrivalAlert = hasArrivalAlert,
                hasProximityAlert = hasProximityAlert
            )
        }

    private fun getHasArrivalAlertFlow(stopCode: String): Flow<Boolean?> {
        return if (hasArrivalAlertFeature) {
            alertsRepository.hasArrivalAlertFlow(stopCode)
        } else {
            flowOf(null)
        }
    }

    private fun getHasProximityAlertFlow(stopCode: String): Flow<Boolean?> {
        return if (hasProximityAlertFeature) {
            alertsRepository.hasProximityAlertFlow(stopCode)
        } else {
            flowOf(null)
        }
    }

    private fun createUiFavouriteDropdownItems(
        isLoading: Boolean,
        hasArrivalAlert: Boolean?,
        hasProximityAlert: Boolean?
    ): ImmutableList<UiFavouriteDropdownItem> {
        return buildList {
            this += UiFavouriteDropdownItem.EditFavouriteName
            this += UiFavouriteDropdownItem.RemoveFavourite

            when (hasArrivalAlert) {
                true -> this += UiFavouriteDropdownItem.RemoveArrivalAlert
                false -> this += UiFavouriteDropdownItem.AddArrivalAlert(isEnabled = !isLoading)
                null -> Unit // null indicates feature not available, so do not add.
            }

            when (hasProximityAlert) {
                true -> this += UiFavouriteDropdownItem.RemoveProximityAlert
                false -> this += UiFavouriteDropdownItem.AddProximityAlert(isEnabled = !isLoading)
                null -> Unit // null indicates feature not available, so do not add.
            }

            if (hasStopMapFeature) {
                this += UiFavouriteDropdownItem.ShowOnMap
            }
        }.toImmutableList()
    }

    private fun createDefault(stopCode: String): Pair<String, UiFavouriteDropdownMenu> {
        return stopCode to UiFavouriteDropdownMenu(
            items = createUiFavouriteDropdownItems(
                isLoading = true,
                hasArrivalAlert = if (hasArrivalAlertFeature) false else null,
                hasProximityAlert = if (hasProximityAlertFeature) false else null
            )
        )
    }
}
