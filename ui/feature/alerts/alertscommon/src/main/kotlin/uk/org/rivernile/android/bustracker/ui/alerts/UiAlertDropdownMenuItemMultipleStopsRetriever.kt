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

package uk.org.rivernile.android.bustracker.ui.alerts

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import javax.inject.Inject

/**
 * This is used to retrieve alert menu items when multiple stops are being referenced.
 *
 * @author Niall Scott
 */
public interface UiAlertDropdownMenuItemMultipleStopsRetriever {

    /**
     * Get a mapping of [StopIdentifier] to [UiArrivalAlertDropdownMenuItem] for all requested
     * [stopIdentifiers] which creates the arrival alert menu item data.
     *
     * @param stopIdentifiers The [StopIdentifier]s to get [UiArrivalAlertDropdownMenuItem] items
     * for.
     * @return The mapping of [StopIdentifier] to [UiArrivalAlertDropdownMenuItem]s for all
     * requested [stopIdentifiers].
     */
    public fun getUiArrivalAlertDropdownMenuItemsFlow(
        stopIdentifiers: Set<StopIdentifier>
    ): Flow<Map<StopIdentifier, UiArrivalAlertDropdownMenuItem>?>

    /**
     * Get a mapping of [StopIdentifier] to [UiProximityAlertDropdownMenuItem] for all requested
     * [stopIdentifiers] which creates the proximity alert menu item data.
     *
     * @param stopIdentifiers The [StopIdentifier]s to get [UiProximityAlertDropdownMenuItem] items
     * for.
     * @return The mapping of [StopIdentifier] to [UiProximityAlertDropdownMenuItem]s for all
     * requested [stopIdentifiers].
     */
    public fun getUiProximityAlertDropdownMenuItemsFlow(
        stopIdentifiers: Set<StopIdentifier>
    ): Flow<Map<StopIdentifier, UiProximityAlertDropdownMenuItem>?>
}

internal class RealUiAlertDropdownMenuItemMultipleStopsRetriever @Inject constructor(
    private val alertsRepository: AlertsRepository,
    private val featureRepository: FeatureRepository
) : UiAlertDropdownMenuItemMultipleStopsRetriever {

    private val hasArrivalAlertFeature by lazy { featureRepository.hasArrivalAlertFeature }
    private val hasProximityAlertFeature by lazy { featureRepository.hasProximityAlertFeature }

    override fun getUiArrivalAlertDropdownMenuItemsFlow(
        stopIdentifiers: Set<StopIdentifier>
    ): Flow<Map<StopIdentifier, UiArrivalAlertDropdownMenuItem>?> {
        return if (hasArrivalAlertFeature && stopIdentifiers.isNotEmpty()) {
            alertsRepository
                .arrivalAlertStopIdentifiersFlow
                .map {
                    createArrivalAlertMenuItems(
                        requestedStopIdentifiers = stopIdentifiers,
                        arrivalAlertStopIdentifiers = it
                    )
                }
                .distinctUntilChanged()
        } else {
            flowOf(null)
        }
    }

    override fun getUiProximityAlertDropdownMenuItemsFlow(
        stopIdentifiers: Set<StopIdentifier>
    ): Flow<Map<StopIdentifier, UiProximityAlertDropdownMenuItem>?> {
        return if (hasProximityAlertFeature && stopIdentifiers.isNotEmpty()) {
            alertsRepository
                .proximityAlertStopIdentifiersFlow
                .map {
                    createProximityAlertMenuItems(
                        requestedStopIdentifiers = stopIdentifiers,
                        proximityAlertStopIdentifiers = it
                    )
                }
                .distinctUntilChanged()
        } else {
            flowOf(null)
        }
    }

    private fun createArrivalAlertMenuItems(
        requestedStopIdentifiers: Set<StopIdentifier>,
        arrivalAlertStopIdentifiers: Set<StopIdentifier>?
    ): Map<StopIdentifier, UiArrivalAlertDropdownMenuItem>? {
        return requestedStopIdentifiers
            .associateWith {
                UiArrivalAlertDropdownMenuItem(
                    hasArrivalAlert = arrivalAlertStopIdentifiers?.contains(it) ?: false
                )
            }
            .ifEmpty { null }
    }

    private fun createProximityAlertMenuItems(
        requestedStopIdentifiers: Set<StopIdentifier>,
        proximityAlertStopIdentifiers: Set<StopIdentifier>?
    ): Map<StopIdentifier, UiProximityAlertDropdownMenuItem>? {
        return requestedStopIdentifiers
            .associateWith {
                UiProximityAlertDropdownMenuItem(
                    hasProximityAlert = proximityAlertStopIdentifiers?.contains(it) ?: false
                )
            }
            .ifEmpty { null }
    }
}
