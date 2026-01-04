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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForViewModelCoroutineScope
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import javax.inject.Inject

/**
 * This generates a [UiFavouriteDropdownMenu] for a selected stop.
 *
 * @author Niall Scott
 */
internal interface UiFavouriteDropdownMenuGenerator {

    /**
     * For the given [Set] of [stopCodes], return a [Flow] which emits a mapping of the supplied
     * stop codes to its associated [UiFavouriteDropdownMenu], if available.
     *
     * @param stopCodes The stop codes to get [UiFavouriteDropdownMenu]s for.
     * @return A [Flow] which emits a mapping of the supplied stop codes to its associated
     * [UiFavouriteDropdownMenu], if available.
     */
    fun getDropdownMenuItemsForStopsFlow(
        stopCodes: Set<String>
    ): Flow<Map<String, UiFavouriteDropdownMenu>?>
}

@ViewModelScoped
internal class RealUiFavouriteDropdownMenuGenerator @Inject constructor(
    private val arguments: Arguments,
    private val state: State,
    private val featureRepository: FeatureRepository,
    private val alertsRepository: AlertsRepository,
    @param:ForDefaultDispatcher private val defaultCoroutineDispatcher: CoroutineDispatcher,
    @param:ForViewModelCoroutineScope private val viewModelCoroutineScope: CoroutineScope
) : UiFavouriteDropdownMenuGenerator {

    private val hasArrivalAlertFeature by lazy { featureRepository.hasArrivalAlertFeature }
    private val hasProximityAlertFeature by lazy { featureRepository.hasProximityAlertFeature }
    private val hasStopMapFeature by lazy { featureRepository.hasStopMapUiFeature }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getDropdownMenuItemsForStopsFlow(
        stopCodes: Set<String>
    ): Flow<Map<String, UiFavouriteDropdownMenu>?> {
        return if (stopCodes.isNotEmpty()) {
            arguments
                .isShortcutModeFlow
                .flatMapLatest {
                    getDropdownMenuItemsForStopsFlow(
                        stopCodes = stopCodes,
                        isShortcutMode = it
                    )
                }
        } else {
            flowOf(null)
        }
    }

    private fun getDropdownMenuItemsForStopsFlow(
        stopCodes: Set<String>,
        isShortcutMode: Boolean
    ): Flow<Map<String, UiFavouriteDropdownMenu>?> {
        return if (!isShortcutMode) {
            combine(
                state.selectedStopCodeFlow,
                arrivalAlertStopCodes,
                proximityAlertStopCodes
            ) { selectedStopCode, arrivalAlertStopCodes, proximityAlertStopCodes ->
                createDropdownMenusForStops(
                    stopCodes = stopCodes,
                    arrivalAlertStopCodes = arrivalAlertStopCodes,
                    proximityAlertStopCodes = proximityAlertStopCodes,
                    selectedStopCode = selectedStopCode
                )
            }
        } else {
            flowOf(null)
        }
    }

    private val arrivalAlertStopCodes = _arrivalAlertStopCodes
        .shareIn(
            scope = viewModelCoroutineScope,
            started = SharingStarted.WhileSubscribed(
                stopTimeoutMillis = 1000L,
                replayExpirationMillis = 0L
            ),
            replay = 1
        )

    private val proximityAlertStopCodes = _proximityAlertStopCodes
        .shareIn(
            scope = viewModelCoroutineScope,
            started = SharingStarted.WhileSubscribed(
                stopTimeoutMillis = 1000L,
                replayExpirationMillis = 0L
            ),
            replay = 1
        )

    private val _arrivalAlertStopCodes: Flow<Set<String>?> get() {
        return if (hasArrivalAlertFeature) {
            alertsRepository
                .arrivalAlertStopCodesFlow
                .distinctUntilChanged()
                .flowOn(defaultCoroutineDispatcher)
        } else {
            flowOf(null)
        }
    }

    private val _proximityAlertStopCodes: Flow<Set<String>?> get() {
        return if (hasProximityAlertFeature) {
            alertsRepository
                .proximityAlertStopCodesFlow
                .distinctUntilChanged()
                .flowOn(defaultCoroutineDispatcher)
        } else {
            flowOf(null)
        }
    }

    private fun createDropdownMenusForStops(
        stopCodes: Set<String>,
        arrivalAlertStopCodes: Set<String>?,
        proximityAlertStopCodes: Set<String>?,
        selectedStopCode: String?
    ): Map<String, UiFavouriteDropdownMenu> {
        return stopCodes
            .associateWith { stopCode ->
                UiFavouriteDropdownMenu(
                    isShown = stopCode == selectedStopCode,
                    arrivalAlertDropdownItem = createUiArrivalAlertDropdownItem(
                        stopCode = stopCode,
                        arrivalAlertStopCodes = arrivalAlertStopCodes
                    ),
                    proximityAlertDropdownItem = createUiProximityAlertDropdownItem(
                        stopCode = stopCode,
                        proximityAlertStopCodes = proximityAlertStopCodes
                    ),
                    isStopMapItemShown = hasStopMapFeature
                )
            }
    }

    private fun createUiArrivalAlertDropdownItem(
        stopCode: String,
        arrivalAlertStopCodes: Set<String>?
    ): UiArrivalAlertDropdownItem? {
        return if (hasArrivalAlertFeature) {
            UiArrivalAlertDropdownItem(
                hasArrivalAlert = arrivalAlertStopCodes?.contains(stopCode) ?: false
            )
        } else {
            null
        }
    }

    private fun createUiProximityAlertDropdownItem(
        stopCode: String,
        proximityAlertStopCodes: Set<String>?
    ): UiProximityAlertDropdownItem? {
        return if (hasProximityAlertFeature) {
            UiProximityAlertDropdownItem(
                hasProximityAlert = proximityAlertStopCodes?.contains(stopCode) ?: false
            )
        } else {
            null
        }
    }
}
