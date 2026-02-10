/*
 * Copyright (C) 2021 - 2026 Niall 'Rivernile' Scott
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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import uk.org.rivernile.android.bustracker.core.alerts.Alert
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.alerts.ArrivalAlert
import uk.org.rivernile.android.bustracker.core.alerts.ProximityAlert
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.busstops.StopDetails
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.sortByServiceName
import javax.inject.Inject

/**
 * This class is used to retrieve [UiAlert]s for display on the UI. It contains the logic to tie
 * [UiAlert]s together with stop details.
 *
 * @param alertsRepository Where [Alert]s are sourced from.
 * @param busStopsRepository Where the stop details are sourced from.
 * @param serviceNameComparator Used to compare and sort services.
 * @author Niall Scott
 */
class AlertsRetriever @Inject constructor(
    private val alertsRepository: AlertsRepository,
    private val busStopsRepository: BusStopsRepository,
    private val serviceNameComparator: Comparator<String>
) {

    /**
     * This produces a [Flow] which emits [List]s of [UiAlert]s.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val allAlertsFlow: Flow<List<UiAlert>?> get() =
        alertsRepository.allAlertsFlow
            .flatMapLatest(this::loadStopDetailsForAlerts)
            .onStart<List<UiAlert>?> { emit(null) }

    /**
     * Given a [List] of loaded [Alert]s, load the stop details for every alert and combine these
     * details together to provide a [List] of [UiAlert]s instead.
     *
     * @param alerts The previously loaded [List] of [Alert]s. If this is `null` or empty, this
     * method will return a [Flow] of a single empty [List].
     * @return A [Flow] which emits [List]s of [UiAlert]s.
     */
    private fun loadStopDetailsForAlerts(alerts: List<Alert>?): Flow<List<UiAlert>> {
        return alerts?.takeIf(List<Alert>::isNotEmpty)?.let { a ->
            val stopIdentifiers = a.map(Alert::stopIdentifier).toHashSet()

            busStopsRepository.getBusStopDetailsFlow(stopIdentifiers)
                .map { combineAlertsAndStopDetails(a, it) }
        } ?: flowOf(emptyList())
    }

    /**
     * Given a [List] of [Alert]s and a [Map] of [StopDetails], return the mapped version of this
     * data where [StopDetails] are populated for each alert we have details for.
     *
     * @param alerts The [List] of alerts which need [StopDetails] matched up with it.
     * @param stopDetailsMap The [Map] of [StopDetails] to combine with alerts.
     * @return A new [List], where the [StopDetails] have been populated in to the [Alert] if
     * available.
     */
    private fun combineAlertsAndStopDetails(
        alerts: List<Alert>,
        stopDetailsMap: Map<StopIdentifier, StopDetails>?
    ) = alerts.map {
        val stopDetails = stopDetailsMap?.get(it.stopIdentifier)

        when (it) {
            is ArrivalAlert -> combineArrivalAlertAndStopDetails(it, stopDetails)
            is ProximityAlert -> combineProximityAlertAndStopDetails(it, stopDetails)
        }
    }

    /**
     * Given an [ArrivalAlert] and possible [StopDetails], combine them in to a single
     * [UiAlert.ArrivalAlert], to be consumed by the UI.
     *
     * @param alert The [ArrivalAlert].
     * @param stopDetails The stop details. May be `null`.
     * @return An object containing the combined [ArrivalAlert] and [StopDetails].
     */
    private fun combineArrivalAlertAndStopDetails(
        alert: ArrivalAlert,
        stopDetails: StopDetails?
    ) = UiAlert.ArrivalAlert(
        alert.id,
        alert.stopIdentifier,
        stopDetails,
        alert.services.sortByServiceName(serviceNameComparator),
        alert.timeTriggerMinutes
    )

    /**
     * Given a [ProximityAlert] and possible [StopDetails], combine them in to a single
     * [UiAlert.ProximityAlert], to be consumed by the UI.
     *
     * @param alert The [ProximityAlert].
     * @param stopDetails The stop details. May be `null`.
     * @return An object containing the combined [ProximityAlert] and [StopDetails].
     */
    private fun combineProximityAlertAndStopDetails(
        alert: ProximityAlert,
        stopDetails: StopDetails?
    ) = UiAlert.ProximityAlert(
        alert.id,
        alert.stopIdentifier,
        stopDetails,
        alert.distanceFromMeters
    )
}
